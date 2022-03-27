import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDispatch {

    private final LinkedList<Client> clientsList;
    private final ExecutorService fixedPool;
    private ServerSocket serverSocket;
    private Client client;
    private final int nThreads;
    private final Grid grid;
    private int playerCounter;
    private int portNumber;

    public ServerDispatch(int portNumber, int nThreads, String filePath) {

        this.nThreads = Integer.valueOf(nThreads);
        this.portNumber = Integer.valueOf(portNumber);
        this.clientsList = new LinkedList<>();
        this.fixedPool = Executors.newFixedThreadPool(this.nThreads);
        this.grid = new Grid(filePath);


        try {
            serverSocket = new ServerSocket(Integer.valueOf(portNumber));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {

        System.out.println(Messages.INFO_SERVER_ON);
        System.out.println(Messages.INFO_PORT + portNumber);

        //Must have all the players connected to start the game
        while (clientsList.size() < nThreads) {

            Socket clientSocket = serverSocket.accept();
            client = new Client(clientSocket, this); //Blocking statement
            clientsList.add(client);
            System.out.println(Messages.INFO_NEWCONNECTION + clientsList.size());

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
            System.out.print("");
        }

        sendServerMessage(Messages.INFO_GAMESTARTING);
        System.out.println(Messages.INFO_GAMESTARTING);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        briefSummary();
    }

    public boolean checkIfAllReady() {

        int counter = 0;

        for (Client client : clientsList) {

            if (client.getIsReady()) counter++;
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
                "    |      READY YOUR FINGERS!   |.\n" +
                "    |   _________________________|___\n" +
                "    |  /                            /.\n" +
                "    \\_/____________________________/.");

        sendRulesToAll(Colors.WHITE_UNDERLINED + Colors.RED_BOLD + "Starting the game in:" + Colors.RESET + " ");

        for (int i = 10; i >= 0; i--) {
            try {
                Thread.sleep(1000);
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
        clearScreenServer();

        sendAll(String.valueOf(grid.drawMatrix()));
        sendAll(Messages.INFO_CHOOSEWORD);

        //Must be here. Cannot be in constructor, otherwise won't work.
        this.playerCounter = clientsList.size();

    }

    public void playGame(String msg, Client client) {
        //Check player Input
        checkPlayersInput(msg, client);

        //Redraw the Matrix
        clearScreen();
        clearScreenServer();
        sendAll(String.valueOf(grid.drawMatrix()));

        //Get Player Score and Lives and Show it in Console
        sendPrivateWarning(("Lives: " + String.valueOf(client.getLives()) + " ~ Personal Score: " + String.valueOf(client.getScore())), client);

        //Check If there are still words available && if Player Lost && if only 1 player playing && if there is a Draw!
        if (client.getLives() == 0) {
            sendPrivateWarning("[INFO] You Lost the Game!", client);

            playerCounter -= 1; //

            sendChatMessage(("[INFO] " + client.getName() + " Lost the game and got out! " + playerCounter + " players left!"), client.getName());
            client.closeEverything();
            return;
        }
        if (playerCounter <= 1) {
            sendAll(drawWinner(client.getName()));
            sendAll("[INFO] " + client.getName() + " is the survivor!");
            System.out.println("[INFO] " + client.getName() + " is the survivor!");
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

            sendAll("[INFO] " + playerWinner + " wins the game with a total Score amount of " + maxScore);
            sendAll(drawWinner(playerWinner));

            closeServer();
        }
        sendAll(Messages.INFO_CHOOSEWORD);

    }


    //Other Methods
    public void checkPlayersInput(String str, Client client) {
        synchronized (this) {

            int score = grid.checkPlayerInput(str);

            //This will set the player score
            client.setScore(score);

            //Check if Player Missed the Word and decrement 1 live
            if (score == 0) client.setLives();

        }
    }

    public void receivePlayerMessage(String msg, Client client) {

        sendAll(client.getName() + ": " + msg);
        playGame(msg, client);

    }

    public void closeServer() {

        sendAll(Messages.INFO_ENDGAME);

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

    public void clearScreenServer() {

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        System.out.println("\n" +
                "\n" +
                " __          __           _        _____                      \n" +
                " \\ \\        / /          | |      / ____|                     \n" +
                "  \\ \\  /\\  / /__  _ __ __| |___  | |  __  __ _ _ __ ___   ___ \n" +
                "   \\ \\/  \\/ / _ \\| '__/ _` / __| | | |_ |/ _` | '_ ` _ \\ / _ \\\n" +
                "    \\  /\\  / (_) | | | (_| \\__ \\ | |__| | (_| | | | | | |  __/\n" +
                "     \\/  \\/ \\___/|_|  \\__,_|___/  \\_____|\\__,_|_| |_| |_|\\___|\n" +
                "\n");

        playersRunningScores();

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

    private void playersRunningScores() {

        //Design the grid rows, cols. cols = players qty. rows = scores by levels
        //Inside grid will show the players as a number representing the lives they have left

        int cols = getClientList().size();
        int rows = 7;

        //GRID:
        String[][] grid = new String[rows][cols];

        //Fill the grid first
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = " ";
            }
        }

        //Add player data to grid:
        for (int j = 0; j < cols; j++) {
            if (getClientList().get(j).getScore() >= 300) {
                grid[0][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() >= 200 && getClientList().get(j).getScore() < 300) {
                grid[1][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() >= 150 && getClientList().get(j).getScore() < 200) {
                grid[2][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() >= 100 && getClientList().get(j).getScore() < 150) {
                grid[3][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() >= 50 && getClientList().get(j).getScore() < 100) {
                grid[4][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() >= 15 && getClientList().get(j).getScore() < 50) {
                grid[5][j] = String.valueOf(getClientList().get(j).getLives());

            } else if (getClientList().get(j).getScore() < 15) {
                grid[6][j] = String.valueOf(getClientList().get(j).getLives());
            }
        }

        //Draw Grid:
        System.out.println("* * * LIVE PLAYER SCORES * * * \n");

        System.out.print("Players Name::  ");
        for (Client player : clientsList) {
            System.out.print(player.getName().charAt(0) + "" + player.getName().charAt(1) + " ");
        }
        System.out.println("");
        for (int i = 0; i < rows; i++) {
            switch (i) {
                case 0:
                    System.out.print("P.Score > 300| ");
                    break;
                case 1:
                    System.out.print("P.Score > 200| ");
                    break;
                case 2:
                    System.out.print("P.Score > 150| ");
                    break;
                case 3:
                    System.out.print("P.Score > 100| ");
                    break;
                case 4:
                    System.out.print("P.Score >  50| ");
                    break;
                case 5:
                    System.out.print("P.Score >  30| ");
                    break;
                case 6:
                    System.out.print("P.Score >   0| ");
                    break;
            }
            for (int j = 0; j < cols; j++) {
                System.out.print("[" + grid[i][j] + "]");
            }
            System.out.println(" |");
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

    public void sendPrivateWarning(String msg, Client clientObj) {

        for (Client client : clientsList) {

            if (client.equals(clientObj)) {
                client.send(msg);
            }
        }

    }

}