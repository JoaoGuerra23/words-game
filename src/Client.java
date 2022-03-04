import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ServerDispatch serverDispatch;
    private PrintStream printStream;
    private boolean isReady;
    private String msg;
    private Grid grid;
    private String username;
    private Prompt prompt;
    private StringInputScanner question;

    public Client(Socket socket, ServerDispatch serverDispatch, Grid grid) throws IOException {

        this.socket = socket;
        this.serverDispatch = serverDispatch;
        this.grid = grid;

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printStream = new PrintStream(socket.getOutputStream());

        this.prompt = new Prompt(socket.getInputStream(), printStream);
        this.question = new StringInputScanner();

    }

    public String getMsg() {
        return msg;
    }

    public void send(String str) {
        out.println(str);
    }

    public void sendRules(String str) {
        synchronized (this) {
            out.print(str + " ");
            out.flush();
        }
    }

    public boolean getIsReady() {
        synchronized (this) {
            return isReady;
        }
    }

    public void setReady(boolean ready) {
        synchronized (this) {
            isReady = ready;
        }
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {

        out.println(" __          __ ______  _       _       _____  ____   __  __  ______  _  _ \n" +
                " \\ \\        / /|  ____|| |     | |     / ____|/ __ \\ |  \\/  ||  ____|| || |\n" +
                "  \\ \\  /\\  / / | |__   | |     | |    | |    | |  | || \\  / || |__   | || |\n" +
                "   \\ \\/  \\/ /  |  __|  | |     | |    | |    | |  | || |\\/| ||  __|  | || |\n" +
                "    \\  /\\  /   | |____ | |____ | |____| |____| |__| || |  | || |____ |_||_|\n" +
                "     \\/  \\/    |______||______||______|\\_____|\\____/ |_|  |_||______|(_)(_)\n" +
                "                           Type /start to START the game\n\n");

        question.setMessage("What is your Name?");
        username = prompt.getUserInput(question).toUpperCase();

        try {

            while(true){

                msg = in.readLine();

                if(getIsReady()) {
                    if (msg.equals("")) continue;
                    if (msg.equals("/start")) {
                        setReady(true);
                        continue;
                    }
                    //Send message to Server
                    serverDispatch.receivePlayerMessage(msg);
                } else {
                    if (msg.equals("/start")) {
                        setReady(true);
                        continue;
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}