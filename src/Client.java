import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.PasswordInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

public class Client implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ServerDispatch serverDispatch;
    private PrintStream printStream;
    private Prompt prompt;
    private StringInputScanner question;
    private HashMap<String, String> adminAccount = new HashMap<>();

    private boolean isReady;
    private String msg;
    private String username = "";
    private int score;
    private int lives;

    public Client(Socket socket, ServerDispatch serverDispatch) {
        synchronized (this) {

            this.socket = socket;
            this.serverDispatch = serverDispatch;
            this.lives = 3;

            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.printStream = new PrintStream(socket.getOutputStream());
                this.prompt = new Prompt(socket.getInputStream(), printStream);
                this.question = new StringInputScanner();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        out.println(" __          __ ______  _       _       _____  ____   __  __  ______  _  _ \n" +
                " \\ \\        / /|  ____|| |     | |     / ____|/ __ \\ |  \\/  ||  ____|| || |\n" +
                "  \\ \\  /\\  / / | |__   | |     | |    | |    | |  | || \\  / || |__   | || |\n" +
                "   \\ \\/  \\/ /  |  __|  | |     | |    | |    | |  | || |\\/| ||  __|  | || |\n" +
                "    \\  /\\  /   | |____ | |____ | |____| |____| |__| || |  | || |____ |_||_|\n" +
                "     \\/  \\/    |______||______||______|\\_____|\\____/ |_|  |_||______|(_)(_)\n\n" +
                "         Type /start to START the game or /pm to message other players\n\n");


        //Set player Nickname:
        setName();

        //Check Player inputs
        checkPlayerInput();

    }

    private void setName() {

        question.setMessage("Set your nickname:\n");

        this.username = prompt.getUserInput(question);

        if (username.equals("admin")) {
            adminLogin();
            return;
        }

        serverDispatch.sendPrivateWarning(("\nWelcome " + this.username + "! While we are setting the game for you,\nfeel free to chat with other players.\n"), this.username);
        System.out.println(Colors.WHITE_UNDERLINED + this.username + Colors.RESET + " is connected"); //Server Info

    }

    private void adminLogin() {

        adminAccount.put("admin", "bullshit");

        PasswordInputScanner password = new PasswordInputScanner();
        password.setMessage("Type admin password: ");

        String pass = prompt.getUserInput(password);

        if (adminAccount.get("admin").equals(pass)) {
            System.out.println("SERVER: An Admin is present!");
            serverDispatch.sendAll("SERVER: An admin just logged in server!");
            adminMenu();
        } else {
            serverDispatch.sendPrivateWarning("Incorrect password! Nice Try!", username);
            System.out.println("Someone is trying to access GodMode!");
            setName();
        }

    }

    private void adminMenu() {

        while (true) {

            try {
                this.msg = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (this.msg.equals("/kick")) {

                LinkedList<String> players = new LinkedList<>();
                for (Client c : serverDispatch.getClientList()) {
                    players.offer(c.getName());
                }

                String[] playersList = new String[players.size()];
                for (int i = 0; i < players.size(); i++) {
                    playersList[i] = players.get(i);
                }

                MenuInputScanner scanner = new MenuInputScanner(playersList);
                scanner.setMessage("Users Available: ");
                int answerIndex = prompt.getUserInput(scanner);

                StringInputScanner kickMessageMenu = new StringInputScanner();
                kickMessageMenu.setMessage("Write your message to player:\n");
                String kickMessage = prompt.getUserInput(kickMessageMenu);

                System.out.println(Colors.RED_BOLD_BRIGHT + "[Player KICKED]: " + playersList[answerIndex - 1] + Colors.RESET); //Blocking
                serverDispatch.sendPrivateWarning((Colors.RED_BOLD_BRIGHT + "[You were Kicked]: " + kickMessage), playersList[answerIndex - 1]);

                for (Client cl : serverDispatch.getClientList()) {
                    if (cl.getName() == playersList[answerIndex - 1]) {
                        try {
                            cl.getSocket().close();
                        } catch (IOException e) {
                            System.err.println("<<Socket Close Warning>>");
                        }
                    }
                }
                serverDispatch.sendPrivateWarning("SERVER: Player " + playersList[answerIndex - 1] + " Kicked!", username);
                continue;
            }
            else if(this.msg.equals("/list")) {
                StringBuilder sb = new StringBuilder();

                for (Client c : serverDispatch.getClientList()) {
                    if (!c.getName().equals(username)) {
                        sb.append(". " + c.getName() + " :: Score: " + c.getScore() + " :: Lives: " + c.getLives() + "\n");
                    }
                }

                serverDispatch.sendPrivateWarning(String.valueOf(sb), username);
                continue;
            }
            serverDispatch.sendChatMessage((Colors.RED_BOLD_BRIGHT + "[ADMIN]: " + Colors.RESET + this.msg), this.username);
            System.out.println(Colors.RED_BOLD_BRIGHT + "[ADMIN]: " + Colors.RESET + this.msg);
        }
    }


    private void checkPlayerInput() {

        while (true) {

            try {
                this.msg = in.readLine();
            } catch (IOException e) {
                System.err.println("<<Socket Close Warning>>");
            }

            if (getIsReady()) {
                if (this.msg.equals("")) {
                } else if (this.msg.equals("/start")) {
                    serverDispatch.sendPrivateWarning(("You are already waiting for game start."), username);
                } else if (this.msg.equals("/pm")) {
                    sendPrivateMessage(this.prompt);
                } else {
                    serverDispatch.receivePlayerMessage(this.msg, this);
                }
            } else {
                if (msg.equals("/pm")) {
                    sendPrivateMessage(this.prompt);
                } else if (this.msg.equals("/start")) {
                    serverDispatch.sendChatMessage((this.username + " typed /start to start the game!"), this.username);
                    serverDispatch.sendPrivateWarning(("Waiting for other players"), this.username);
                    setReady(true);
                } else if (this.msg.equals("")) {
                    serverDispatch.sendPrivateWarning("Stop Spamming with blanks!!", this.username);
                } else {
                    serverDispatch.sendChatMessage((this.username + ": " + this.msg), this.username);
                    System.out.println(Colors.BLUE_BRIGHT + this.username + ":" + Colors.RESET + " " + this.msg);
                }
            }
            continue;
        }
    }

    public void sendPrivateMessage(Prompt prompt) {

        String[] strArray = new String[serverDispatch.getClientList().size() - 1];
        LinkedList<String> usersList = new LinkedList<>();

        //Had to add the users to a Linked List first due to bad index management.
        for (Client client : serverDispatch.getClientList()) {
            if (!client.getName().equals(username)) {
                usersList.add(client.getName());
            }
        }

        //Add to array list because Menu only accepts arrays not linkedlists
        for (int i = 0; i < usersList.size(); i++) {
            strArray[i] = usersList.get(i);
        }

        MenuInputScanner scanner = new MenuInputScanner(strArray);
        scanner.setMessage("Users Available: ");

        int answerIndex = prompt.getUserInput(scanner);

        StringInputScanner personalMessage = new StringInputScanner();
        personalMessage.setMessage("Write your message to player:\n");
        String personalM = "(PM) " + username + ": " + prompt.getUserInput(personalMessage); //Blocking
        serverDispatch.sendPrivateWarning(("PM sent."), username);
        System.out.println(username + ": is having a private chat with " + usersList.get(answerIndex - 1) + "." + Colors.PURPLE_BOLD_BRIGHT + " Who knows what ... " + Colors.RESET);

        serverDispatch.sendPrivateWarning(personalM, usersList.get(answerIndex - 1));
    }

    public void closeEverything() {
        try {

            //Make the flush first then close it.
            in.close();
            out.flush();
            out.close();
            printStream.flush();
            printStream.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("Socket Close Exception...");
        }
    }

    /**
     * Getters & Setters
     */
    public void setLives() {
        synchronized (this) {
            this.lives--;
        }
    }

    public void setScore(int score) {
        synchronized (this) {
            this.score += score;
        }
    }

    public void setReady(boolean ready) {
        synchronized (this) {
            isReady = ready;
        }
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