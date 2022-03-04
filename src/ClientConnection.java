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
    private boolean isReady;
    private String msg;

    public String getMsg() {
        return msg;
    }

    private String username;

    public ClientConnection(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    public void send(String str) {
        out.println(str);
    }

    public void sendRules(String str){
        out.print(str + " ");
        out.flush();
    }

    public boolean getIsReady(){
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public String getUsername(){
        return username;
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
                    "                           Type /start to START the game                   \n" +
                    "                                                                           ");

            Prompt prompt = new Prompt(socket.getInputStream(), printStream);
            StringInputScanner question = new StringInputScanner();
            question.setMessage("What is your Name?");
            username = prompt.getUserInput(question).toUpperCase();


            while (true) {

                // read something from the client
                msg = in.readLine();
                // send to all the other connections
                chatServer.sendAll(username + ": " + msg);

                if(msg.equals("/start")){
                    chatServer.sendAll("Waiting for all player to start the game");
                    setReady(true);
                    System.out.println(username + " is ready to play.");
                    chatServer.checkIfAllReady();
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
