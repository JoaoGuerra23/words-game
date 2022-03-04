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
    private final int port;
    private Grid grid;
    private String msg;

    public ServerDispatch(int port, int nThreads) {

            this.nThreads = nThreads;
            this.port = port;
            this.clients = new LinkedList<>();
            this.fixedPool = Executors.newFixedThreadPool(nThreads);
            this.grid = new Grid(5, 10);

        try {

            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {

        //Must have all the players connected to start the game
        while (clients.size() < nThreads) {

            client = new Client(serverSocket.accept(), this, this.grid);
            clients.add(client);

            fixedPool.submit(client);
            System.out.println("Connections: " + clients.size());
        }
        //Starts the Game
        start();
    }

    public void start() {
        synchronized (this) {

            System.out.println("ServerDisatch --> start()"); //TODO:APAGAR

            while(!checkIfAllReady()){
                //empty on purpose//TODO:APAGAR
            }

            briefSummary();
        }

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
                "    \\_/dc__________________________/.");

        sendRulesToAll("Starting the game in: ");

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

    }

    public void playGame(String msg){
        synchronized (this) {

            //Check player Input
            checkPlayersInput(msg);

            //Redraw the Matrix
            System.out.println("Redrawing");
            sendAll(String.valueOf(grid.drawMatrix()));

            //Show Player Score
            grid.showPlayerScore();
        }
        sendAll("Chose and type a word from the given Matrix: ");
    }

    public void sendAll(String message) {

        for (Client client : clients) {

            client.send(message);

        }
    }

    public void sendRulesToAll(String message) {

        for (Client client : clients) {

            client.sendRules(message);

        }
    }

    public void receivePlayerMessage(String msg){
        synchronized (this) {
            playGame(msg);
        }
    }

    public void checkPlayersInput(String str){

        synchronized (this) {
            grid.checkPlayerInput(str);
        }
    }
}