import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
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

    public void send(String str) {
        out.println(str);
    }

    public void sendRules(String str) {
        out.print(str + " ");
        out.flush();
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

        out.println(" __          __ ______  _       _       _____  ____   __  __  ______  _  _ \n" +
                " \\ \\        / /|  ____|| |     | |     / ____|/ __ \\ |  \\/  ||  ____|| || |\n" +
                "  \\ \\  /\\  / / | |__   | |     | |    | |    | |  | || \\  / || |__   | || |\n" +
                "   \\ \\/  \\/ /  |  __|  | |     | |    | |    | |  | || |\\/| ||  __|  | || |\n" +
                "    \\  /\\  /   | |____ | |____ | |____| |____| |__| || |  | || |____ |_||_|\n" +
                "     \\/  \\/    |______||______||______|\\_____|\\____/ |_|  |_||______|(_)(_)\n\n" +
                "         Type /start to START the game or /pm to message other players\n\n");


        StringInputScanner question = new StringInputScanner();
        question.setMessage("Set your nickname:\n");
        username = prompt.getUserInput(question).toUpperCase();
        serverDispatch.sendPrivateWarning(("\nWelcome " + username + "! While we are setting the game for you,\nfeel free to chat with other players.\n"), username);
        System.out.println(Colors.WHITE_UNDERLINED + username + Colors.RESET + " is connected"); //Server Info

        try {
            while (true) {

                msg = in.readLine();

                if (getIsReady()) {
                    if (msg.equals("")) continue;
                    if (msg.equals("/start")) {
                        serverDispatch.sendPrivateWarning(("This command will not take effect now."), username);
                        continue;
                    } else if (msg.equals("/pm")) {
                        sendPrivateMessage(prompt);
                        continue;
                    }
                    serverDispatch.receivePlayerMessage(msg, this);
                } else {
                    if (msg.equals("/pm")) {
                        sendPrivateMessage(prompt);
                        continue;
                    }
                    if (msg.equals("/start")) {
                        serverDispatch.sendChatMessage((username + " typed /start to start the game!"), username);
                        serverDispatch.sendPrivateWarning(("Waiting for other players"), username);
                        setReady(true);
                        continue;

                    } else if (msg.equals("")) {
                        serverDispatch.sendPrivateWarning("Stop Spamming with blanks!!", username);
                        continue;
                    }

                    serverDispatch.sendChatMessage((username + ": " + msg), username);
                    System.out.println(Colors.BLUE_BRIGHT + username + ":" + Colors.RESET + " " + msg);
                }
            }
        } catch (IOException ex) {
            closeEverything();
        }

    }

    public void sendPrivateMessage(Prompt prompt){
        String[] strArray = new String[serverDispatch.getClientList().size() - 1];

        for (int i = 0; i < serverDispatch.getClientList().size(); i++) {
            if (!serverDispatch.getClientList().get(i).getName().equals(username)) {
                strArray[i] = serverDispatch.getClientList().get(i).getName();
            }
        }


        MenuInputScanner scanner = new MenuInputScanner(strArray);
        scanner.setMessage("Users Available: ");

        int answerIndex = prompt.getUserInput(scanner);

        StringInputScanner personalMessage = new StringInputScanner();
        personalMessage.setMessage("Write your message to player:\n");
        String personalM = "(PM) " + username + ": " + prompt.getUserInput(personalMessage); //Blocking
        serverDispatch.sendPrivateWarning(("PM sent."), username);

        serverDispatch.sendPrivateWarning(personalM, strArray[answerIndex - 1]);
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

    /**
     * Getters & Setters
     */
    public void setLives() {
        this.lives--;
    }

    public void setScore(int score) {
        this.score += score;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public int getLives() {
        return this.lives;
    }

    public Socket getSocket() {
        synchronized (this) {
            return this.socket;
        }
    }

    public String getName() {
        return this.username;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public int getScore() {
        return this.score;
    }
}