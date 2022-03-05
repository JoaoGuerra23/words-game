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
    private String username;
    private int score;
    private int lives;

    public Client(Socket socket, ServerDispatch serverDispatch) throws IOException {

        this.socket = socket;
        this.serverDispatch = serverDispatch;
        this.lives = 3;

    }

    public void setScore(int score){
        this.score += score;
    }

    public int getScore() {
        return this.score;
    }

    public void send(String str) {
        out.println(str);
    }

    public void sendRules(String str) {
            out.print(str + " ");
            out.flush();
    }

    public boolean getIsReady() {
            return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public String getName() {
        return this.username;
    }

    @Override
    public void run() {

        Prompt prompt = null;
        try {

            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printStream = new PrintStream(socket.getOutputStream());

            prompt = new Prompt(socket.getInputStream(), printStream);

        } catch (IOException e) {
            e.printStackTrace();
        }


        StringInputScanner question = new StringInputScanner();

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

            while(true) {

                msg = in.readLine();

                if(getIsReady()) {
                    if (msg.equals("")) continue;
                    if (msg.equals("/start")) {
                        setReady(true);
                        continue;
                    }
                    //Send message to Server
                    System.out.println(Thread.currentThread().getId()); //TODO: APAGAR LINHA, era apenas para confirmar o ID da thread q esta a correr.
                    serverDispatch.receivePlayerMessage(msg, this);
                } else {
                    if (msg.equals("/start")) {
                        serverDispatch.sendChatMesage((username + " typed /start to start the game!"), username); //TODO: Put some color in this text to highlit it from the rest
                        setReady(true);
                        continue;
                    } else if(msg.equals("")){
                        serverDispatch.sendPrivateWarning("Stop Spamming with blanks!!", username);
                        continue;
                    }

                    System.out.println(Thread.currentThread().getId());
                    serverDispatch.sendChatMesage((username + ": " + msg), username);
                }
            }
        } catch (IOException ex) {
            closeEverything();
        }
    }

    public void setLives() {
        this.lives --;
    }

    public int getLives() {
        return this.lives;
    }

    public void closeEverything() {

        try {

            in.close();
            out.close();
            printStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}