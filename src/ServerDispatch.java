import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDispatch {

    private LinkedList<Client> clientsList;
    private ExecutorService fixedPool;
    private ServerSocket serverSocket;
    private Client client;
    private int nThreads;
    private final int portNumber;
    private final Grid grid;
    private int playerCounter;

    public ServerDispatch(int portNumber, int nThreads, String filePath) {

        this.nThreads = Integer.valueOf(nThreads);
        this.portNumber = Integer.valueOf(portNumber);
        this.clientsList = new LinkedList<>();
        this.fixedPool = Executors.newFixedThreadPool(this.nThreads);
        this.grid = new Grid(5, 10, filePath);

        try {
            serverSocket = new ServerSocket(this.portNumber);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {

        System.out.println("SERVER IS ONLINE - Waiting For playing connections");

        //Must have all the players connected to start the game
        while (clientsList.size() < nThreads) {

            Socket clientSocket = serverSocket.accept();
            client = new Client(clientSocket, this); //Blocking statement
            clientsList.add(client);
            System.out.println("SERVER: New Client connected (" + clientsList.size() + ")");

            //Added the Thread to pull and started();
            fixedPool.submit(client);
        }
        //Starts the Game
        start();
    }

    /**
     * Game Logics Start here:
     */

    public void start() {

        while (!checkIfAllReady()) {
        }

        sendServerMessage("All Players are Ready: Game will Start:");
        System.out.println("SERVER: Game is now Starting");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        briefSummary();

    }

    public boolean checkIfAllReady() {

        int counter = 0;

        for (Client client : clientsList) {

            if (client.getIsReady()) {
                counter++;
            }
        }

        if (counter == clientsList.size()) {

            return true;

        }
        return false;
    }

    private void briefSummary() {

        clearScreen();

        sendAll("   ______________________________\n" +
                " / \\                             \\.\n" +
                "|   |                            |.\n" +
                " \\_ |      GAME RULES:           |.\n" +
                "    |                            |.\n" +
                "    |   1. Type the words in the |.\n" +
                "    |      board as fast as      |.\n" +
                "    |      possible              |.\n" +
                "    |                            |.\n" +
                "    |   2. More length more      |.\n" +
                "    |      points                |.\n" +
                "    |                            |.\n" +
                "    |   3. If you miss the word  |.\n" +
                "    |      three times, you lose |.\n" +
                "    |                            |.\n" +
                "    |      GOOD LUCK             |.\n" +
                "    |   _________________________|___\n" +
                "    |  /                            /.\n" +
                "    \\_/____________________________/.");

        sendRulesToAll(Colors.WHITE_UNDERLINED + Colors.RED_BOLD + "Starting the game in:" + Colors.RESET + " ");

        for (int i = 10; i >= 0; i--) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i != 0) {
                sendRulesToAll(String.valueOf(i));
            }
        }
        sendAll("");

        drawGame();
    }

    private void drawGame() {

        //Create the words:
        grid.setWordsForMatrix();

        //ReDraw the Matrix and send it to every1 again
        clearScreen();
        sendAll(String.valueOf(grid.drawMatrix()));
        sendAll("Chose and type a word from the given Matrix: ");

        //Must be here. Cannot be in constructor, otherwise wont work.
        this.playerCounter = clientsList.size();

    }

    public void playGame(String msg, Client client) {
        synchronized (this) {

            //Check player Input
            checkPlayersInput(msg, client);

            //Redraw the Matrix
            clearScreen();
            sendAll(String.valueOf(grid.drawMatrix()));

            //Get Player Score and Lives and Show it in Console
            sendPrivateWarning(("Lives: " + String.valueOf(client.getLives()) + " ~ Personal Score: " + String.valueOf(client.getScore())), client.getName());

            //Check If there are still words available && if Player Lost && if only 1 player playing && if there is a Draw!
            if (client.getLives() == 0) {
                sendPrivateWarning("You Lost the Game!", client.getName());

                playerCounter -= 1; //

                sendChatMessage((client.getName() + " Lost the game and got out! " + playerCounter + " players left!"), client.getName());
                client.closeEverything();
                return;
            }
            if (playerCounter <= 1) {
                sendAll(drawWinner(client.getName())); //TODO: is it working now ?
                sendAll(client.getName() + " is the survivor!");
                closeServer();
                return;
            }

            //If there is a draw:
            if (grid.checkRemainingWords()) {

                String playerWinner = "";
                int maxScore = 0;

                for (Client c : clientsList) {
                    if (c.getSocket().isConnected()) {
                        if (c.getScore() > maxScore) {
                            maxScore = c.getScore();
                            playerWinner = c.getName();
                        }
                    }
                }

                sendAll(playerWinner + " wins the game with a total Score amount of " + maxScore);
                sendAll(drawWinner(playerWinner));

                closeServer();
            }
        }

        sendAll("Chose and type a word from the given Matrix: ");
    }

    public void checkPlayersInput(String str, Client client) {
        synchronized (this) {

            int score = grid.checkPlayerInput(str);

            //This will set the player score
            client.setScore(score);

            //Check if Player Missed the Word:
            if (score == 0) {
                sendPrivateWarning("You missed!", client.getName());
                client.setLives();
                return;
            }
        }
    }

    /**
     * Send Messages to Players:
     */

    public void sendServerMessage(String message) {

        sendAll("SERVER: " + message);

    }

    public void sendAll(String message) {

        for (Client client : clientsList) {

            client.send(message);
        }
    }

    public void sendChatMessage(String message, String user) {

        for (Client client : clientsList) {

            if (!client.getName().equals(user)) {
                client.send(message);
            }
        }
    }

    public void sendRulesToAll(String message) {

        for (Client client : clientsList) {
            client.sendRules(message);
        }
    }

    public void receivePlayerMessage(String msg, Client client) {
        sendAll(client.getName() + ": " + msg);
        playGame(msg, client);
    }

    public void sendPrivateWarning(String msg, String username) {

        for (Client client : clientsList) {

            if (client.getName().equals(username)) {
                client.send(msg);
            }
        }
    }

    public void closeServer() {

        sendAll("End Game!");

        client.closeEverything();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public void clearScreen() {

        sendAll("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        sendAll("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        sendAll("\n" +
                "\n" +
                " __          __           _        _____                      \n" +
                " \\ \\        / /          | |      / ____|                     \n" +
                "  \\ \\  /\\  / /__  _ __ __| |___  | |  __  __ _ _ __ ___   ___ \n" +
                "   \\ \\/  \\/ / _ \\| '__/ _` / __| | | |_ |/ _` | '_ ` _ \\ / _ \\\n" +
                "    \\  /\\  / (_) | | | (_| \\__ \\ | |__| | (_| | | | | | |  __/\n" +
                "     \\/  \\/ \\___/|_|  \\__,_|___/  \\_____|\\__,_|_| |_| |_|\\___|\n" +
                "                                                              \n" +
                "                                                              \n" +
                "\n");

    }

    public String drawWinner(String userName) {
        return
                "    -----------------\n" +
                        "    |@@@@|     |####|\n" +
                        "    |@@@@|     |####|\n" +
                        "    |@@@@|     |####|\n" +
                        "     |@@@|     |###|\n" +
                        "      |@@|     |##|\n" +
                        "      `@@|_____|##'\n" +
                        "           (O)\n" +
                        "        .-'''''-.\n" +
                        "      .'  * * *  `.\n" +
                        "     :  *       *  :\n" +
                        "          " + userName + "\n" +
                        "     :~           ~:\n" +
                        "     :  *       *  :\n" +
                        "      `.  * * *  .'\n" +
                        "        `-.....-'\n";
    }

    public LinkedList<Client> getClientList() {
        return this.clientsList;
    }
}