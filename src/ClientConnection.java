import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringSetInputScanner;

import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ClientConnection implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ChatServer chatServer;
    private PrintStream printStream;

    public ClientConnection(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    public void send(String str) {
        out.println(str);
    }



    @Override
    public void run() {

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printStream = new PrintStream(socket.getOutputStream());


            out.println(" __          __ ______  _       _       _____  ____   __  __  ______  _  _ \n" +
                    " \\ \\        / /|  ____|| |     | |     / ____|/ __ \\ |  \\/  ||  ____|| || |\n" +
                    "  \\ \\  /\\  / / | |__   | |     | |    | |    | |  | || \\  / || |__   | || |\n" +
                    "   \\ \\/  \\/ /  |  __|  | |     | |    | |    | |  | || |\\/| ||  __|  | || |\n" +
                    "    \\  /\\  /   | |____ | |____ | |____| |____| |__| || |  | || |____ |_||_|\n" +
                    "     \\/  \\/    |______||______||______|\\_____|\\____/ |_|  |_||______|(_)(_)\n" +
                    "                                                                           \n" +
                    "                                                                           ");

            Prompt prompt = new Prompt(socket.getInputStream(), printStream);
            StringInputScanner question = new StringInputScanner();
            question.setMessage("What is your Name?");
            String username = prompt.getUserInput(question).toUpperCase(Locale.ROOT);


            while (true) {

                // read something from the client
                String msg = in.readLine();
                // send to all the other connections
                chatServer.sendAll(username + ": " + msg);



            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
