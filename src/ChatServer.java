import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private LinkedList<ClientConnection> clientConnections;
    private ExecutorService fixedPool;
    private ServerSocket serverSocket;
    private ClientConnection clientConnection;
    private int nThreads;
    private final int port;

    public ChatServer(int port, int nThreads) {
        this.nThreads = nThreads;
        this.port = port;
        clientConnections = new LinkedList<>();
        fixedPool = Executors.newFixedThreadPool(nThreads);

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void checkIfAllReady() {

        int counter = 0;

        for (ClientConnection client : clientConnections) {

            if (client.getIsReady()) {
                System.out.println(client.getUsername() + " is ready to go!");
                counter++;
            }
        }

        if (counter == clientConnections.size()) {

            sendAll("All players are now ready to play. Starting the game.\n");//TODO: remove later

            start();

        }
    }


    public void init() {

        try {


            while (true) {

                clientConnection = new ClientConnection(serverSocket.accept(), this);

                clientConnections.add(clientConnection);

                if (clientConnections.size() > nThreads) {
                    System.out.println("Too many Connections");
                    fixedPool.shutdown();
                }

                fixedPool.submit(clientConnection);
                System.out.println("Connections: " + clientConnections.size());

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void start() {

        briefSummary();

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

    }


    public void sendAll(String message) {

        for (ClientConnection clientConnection : clientConnections) {

            clientConnection.send(message);

        }
    }


    public void sendRulesToAll(String message) {

        for (ClientConnection clientConnection : clientConnections) {

            clientConnection.sendRules(message);

        }

    }


}
