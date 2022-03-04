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
    private int nThreads;
    private int port;

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

    public void init() {

        try {



            while (true) {

                ClientConnection clientConnection = new ClientConnection(serverSocket.accept(), this);

                clientConnections.add(clientConnection);

                if(clientConnections.size() > nThreads){
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

    public void sendAll(String message) {

        for (ClientConnection clientConnection : clientConnections) {

            clientConnection.send(message);

        }

    }


    public static void main(String[] args) {

        ChatServer chatServer = new ChatServer(3000, 2);
        chatServer.init();

    }
}
