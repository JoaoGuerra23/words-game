import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
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
                    counter++;
                }
            }

            if (counter == clientConnections.size()) {

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

                System.out.println(clientConnection.getIsReady());

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void start() {

        briefSummary();

    }

    private void briefSummary() {

        clientConnection.send("Lorem Ipsum is simply dummy text of the printing and typesetting industry.\nJust a Description");

    }


    public void sendAll(String message) {

        for (ClientConnection clientConnection : clientConnections) {

            clientConnection.send(message);

        }

    }

}
