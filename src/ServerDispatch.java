import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDispatch {

    private LinkedList<Client> clients;
    private ExecutorService fixedPool;
    private ServerSocket serverSocket;
    private Client client;
    private int nThreads;
    private final int portNumber;
    private final Grid grid;
    private int playerCounter;

    public ServerDispatch(int portNumber, int nThreads, String filePath) {

        this.nThreads = nThreads;
        this.portNumber = portNumber;
        this.clients = new LinkedList<>();
        this.fixedPool = Executors.newFixedThreadPool(nThreads);
        this.grid = new Grid(1, 2, filePath);

        try {

            serverSocket = new ServerSocket(portNumber);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {

        //Must have all the players connected to start the game
        while (clients.size() < nThreads) {

            client = new Client(serverSocket.accept(), this);
            clients.add(client);

            fixedPool.submit(client);
            System.out.println("Connections: " + clients.size());
        }
        //Starts the Game
        start();
    }

    /**
     * Game Logics Start here:
     */

    public void start() {

        while(!checkIfAllReady()){}

        briefSummary();

    }

    public boolean checkIfAllReady() {

        int counter = 0;

        for (Client client : clients) {

            if (client.getIsReady()) {
                counter++;
            }
        }

        if (counter == clients.size()) {

            return true;

        }
        return false;
    }

    private void briefSummary() {

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
                Thread.sleep(0); //TODO Change me
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
        sendAll(String.valueOf(grid.drawMatrix()));
        sendAll("Chose and type a word from the given Matrix: ");

        //Must be here. Cannot be in constructor, otherwise wont work.
        this.playerCounter = clients.size();

    }

    public void playGame(String msg, Client client){
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
            if(playerCounter <= 1 ){
                sendAll(client.getName() + " is the survivor!");
                closeServer();
                return;
            }

            //If there is a draw:
            if(grid.checkRemainingWords()){

                String playerWinner = "";
                int maxScore = 0;

                for(Client c : clients){
                    if(c.getSocket().isConnected()){
                        if(c.getScore() > maxScore){
                            maxScore = c.getScore();
                            playerWinner = c.getName();
                        }
                    }
                }

                sendAll(playerWinner + " wins the game with a total Score amount of " + maxScore);

                String championWordArt =
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
                        "          " + playerWinner + "\n" +
                        "     :~           ~:\n" +
                        "     :  *       *  :\n" +
                        "      `.  * * *  .'\n" +
                        "        `-.....-'\n";


                sendAll(championWordArt);

                closeServer();

                //TODO: Check
            }
        }

        sendAll("Chose and type a word from the given Matrix: ");
    }

    public void checkPlayersInput(String str, Client client){
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

    public void sendAll(String message) {

        for (Client client : clients) {

            client.send(message);
        }
    }

    public void sendChatMessage(String message, String user) {

        for (Client client : clients) {

            if(!client.getName().equals(user)){
                client.send(message);
            }
        }
    }

    public void sendRulesToAll(String message) {

        for (Client client : clients) {
            client.sendRules(message);
        }
    }

    public void receivePlayerMessage(String msg, Client client){
            sendAll(client.getName() + ": " + msg);
            playGame(msg, client);
    }

    public void sendPrivateWarning(String msg, String username) {

        for (Client client : clients) {

            if(client.getName().equals(username)){
                client.send(msg);
            }
        }
    }

    public void closeServer(){

        sendAll("End Game!");

        client.closeEverything();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public void clearScreen(){

        sendAll("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        sendAll("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

    }
}