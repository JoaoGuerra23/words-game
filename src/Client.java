import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Stream;


public class Client implements Runnable {

    private ServerDispatch serverDispatch;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private PrintStream printStream;
    private Prompt prompt;
    private StringInputScanner question;
    private HashMap<String, String> adminAccount = new HashMap<>();

    private String msg;
    private String username;
    private String role;
    private int score;
    private int lives;
    private boolean isKicked;
    private boolean isReady;

    public Client(Socket socket, ServerDispatch serverDispatch) throws IOException {
        this.socket = socket;
        this.serverDispatch = serverDispatch;
        this.lives = 3;
        this.role = "Player";
        this.username = "";

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printStream = new PrintStream(socket.getOutputStream());
        this.prompt = new Prompt(socket.getInputStream(), printStream);
        this.question = new StringInputScanner();

    }

    @Override
    public void run() {

        out.println(" __          __ ______  __      _____  ____   __  __  ______  _  _ \n" +
                " \\ \\        / /|  ____|| |     / ____|/ __ \\ |  \\/  ||  ____|| || |\n" +
                "  \\ \\  /\\  / / | |__   | |    | |    | |  | || \\  / || |__   | || |\n" +
                "   \\ \\/  \\/ /  |  __|  | |    | |    | |  | || |\\/| ||  __|  | || |\n" +
                "    \\  /\\  /   | |____ | |____  |____| |__| || |  | || |____ |_||_|\n" +
                "     \\/  \\/    |______||______|\\_____|\\____/ |_|  |_||______|(_)(_)\n\n" +
                "         Type /start to START the game or /pm to message other players\n\n");

        //Set player Nickname:
        setName();

        //Check Player inputs
        checkPlayerInput();
    }

    //Check Player Inputs
    private void checkPlayerInput() {

        try {
            while (true) {
                this.msg = in.readLine();

                if (this.isKicked == true) this.closeEverything();

                if (getIsReady()) {
                    if (this.msg.equals("")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDBLANKS, this);
                    } else if (this.msg.equals("/start")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDINPUT, this);
                    } else if (this.msg.equals("/start -a")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDINPUT, this);
                    } else if (this.msg.equals("/pm")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDINPUT, this);
                    } else if (this.msg.equals("/list")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDINPUT, this);
                    } else if (this.msg.equals("/kick")) {
                        if (role == "admin") kickMenu();
                        continue;
                    } else {
                        serverDispatch.receivePlayerMessage(this.msg, this);
                    }
                } else {
                    if (msg.equals("/pm")) {
                        sendPrivateMessage(this.prompt);
                    } else if (this.msg.equals("/kick")) {
                        if (role == "admin") kickMenu();
                    } else if (this.msg.equals("/start")) {
                        serverDispatch.sendChatMessage(("[INFO] " + this.username + " typed /start to start the game! Join him!"), this.username);
                        serverDispatch.sendPrivateWarning(("[INFO] Waiting for other players"), this);
                        setReady(true);
                    } else if (this.msg.equals("/start -a")) {
                        if (role == "admin") startAll();
                        continue;
                    } else if (this.msg.equals("")) {
                        serverDispatch.sendPrivateWarning(Messages.INFO_INVALIDBLANKS, this);
                    } else if (this.msg.equals("/list")) {
                        listMenu();
                    } else {
                        if (role == "admin") {
                            serverDispatch.sendChatMessage((Colors.RED_BOLD_BRIGHT + "[ADMIN]: " + Colors.RESET + this.msg), this.username);
                            System.out.println(Colors.RED_BOLD_BRIGHT + "[ADMIN]: " + Colors.RESET + this.msg);
                            continue;
                        }
                        serverDispatch.sendChatMessage(("[" + this.username + "]" + ": " + this.msg), this.username);
                        System.out.println(Colors.BLUE_BRIGHT + "[" + this.username + "]" + ":" + Colors.RESET + " " + this.msg);
                    }
                }
            }
        } catch (IOException e) {
            closeEverything();
            return;
        }

    }

    public void sendPrivateMessage(Prompt prompt) {

        String[] strArray = new String[serverDispatch.getClientList().size() - 1];
        LinkedList<Client> usersList = new LinkedList<>();

        //Had to add the users to a Linked List first due to bad index management.
        for (Client c : serverDispatch.getClientList()) {
            if (!c.getName().equals(this.username)) {
                usersList.add(c);
            }
        }

        //Add to array list because Menu only accepts arrays not linkedlists
        for (int i = 0; i < usersList.size(); i++) {
            strArray[i] = usersList.get(i).getName();
        }

        MenuInputScanner scanner = new MenuInputScanner(strArray);
        scanner.setMessage("Users Available: ");

        int answerIndex = prompt.getUserInput(scanner);

        StringInputScanner personalMessage = new StringInputScanner();
        personalMessage.setMessage("Write your message to player:\n");
        String personalM = "[PM] " + username + ": " + prompt.getUserInput(personalMessage); //Blocking
        serverDispatch.sendPrivateWarning(("PM sent."), this);
        System.out.println("[INFO]" + username + ": is having a private chat with " + usersList.get(answerIndex - 1) + "." + Colors.PURPLE_BOLD_BRIGHT + " Who knows what ... " + Colors.RESET);

        serverDispatch.sendPrivateWarning(personalM, usersList.get(answerIndex - 1));

    }

    private void startAll() {

        serverDispatch.getClientList().forEach(client -> client.setReady(true));
    }

    //Admin Management:
    private void adminLogin() {

        adminAccount.put("admin", "bullshit");

        StringInputScanner password = new StringInputScanner();
        password.setMessage(Messages.INPUT_PASSWORD);

        String pass = prompt.getUserInput(password);

        if (adminAccount.get("admin").equals(pass)) {
            System.out.println(Messages.INFO_ADMINPRESENT);
            serverDispatch.sendAll(Messages.INFO_ADMINPRESENT);
            this.role = "admin";
            setName();
            //checkPlayerInput();
        } else {
            serverDispatch.sendPrivateWarning(Messages.INFO_INCORRECTPASSWORD, this);
            System.out.println(Messages.INFO_INCORRECTPASSWORD);
            setName();
        }
    }

    public void kickMenu() {

        LinkedList<Client> players = new LinkedList<>();
        for (Client c : serverDispatch.getClientList()) {
            if (!c.getName().equals("admin") && !c.isKicked) players.offer(c);
        }

        String[] playersList = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            playersList[i] = players.get(i).getName();
        }


        MenuInputScanner scanner = new MenuInputScanner(playersList);
        scanner.setMessage("Users Available: ");
        int answerIndex = prompt.getUserInput(scanner);

        StringInputScanner kickMessageMenu = new StringInputScanner();
        kickMessageMenu.setMessage("Write your message to player:\n");
        String kickMessage = prompt.getUserInput(kickMessageMenu);

        System.out.println(Colors.RED_BOLD_BRIGHT + "[KICK] Player: " + Colors.RESET + playersList[answerIndex - 1] + Colors.RED_BOLD_BRIGHT + " Reason: " + Colors.RESET + kickMessage); //Blocking
        serverDispatch.sendPrivateWarning((Colors.RED_BOLD_BRIGHT + "[SYSTEM]: " + Colors.RESET + "You were Kicked! " + Colors.RED_BOLD_BRIGHT + "Reason:" + Colors.RESET + kickMessage), players.get(answerIndex - 1));
        serverDispatch.sendChatMessage(Colors.RED_BOLD_BRIGHT + "[KICK] Player: " + Colors.RESET + playersList[answerIndex - 1] + Colors.RED_BOLD_BRIGHT + " Reason: " + Colors.RESET + kickMessage, username); //Blocking

        for (Client cl : serverDispatch.getClientList()) {
            if (cl.getName() == playersList[answerIndex - 1]) {
                cl.setIsKicked(true);
                cl.setReady(true);
            }
        }
        serverDispatch.sendPrivateWarning("[SERVER]: Player " + playersList[answerIndex - 1] + " Kicked!", this);

    }

    public void listMenu() {
        StringBuilder sb = new StringBuilder();

        for (Client c : serverDispatch.getClientList()) {

            sb.append(". " + c.getRole() + c.getName() +
                    " :: Score: " + c.getScore() +
                    " :: Lives: " + c.getLives() +
                    " :: setReadY: " + c.getIsReady() +
                    " :: isKicked: " + c.getIsKicked() + "\n");
        }


        serverDispatch.sendPrivateWarning(String.valueOf(sb), this);
    }



    //Send Messages:
    public void send(String str) {
        out.println(str);
    }

    public void sendRules(String str) {
        out.print(str + " ");
        out.flush();
    }


    //Close Sockets && Buffers
    public void closeEverything() {
        try {

            //Make the flush first then close it.
            printStream.flush();
            printStream.close();

            in.close();

            out.flush();
            out.close();
            socket.close();

        } catch (IOException e) {
            //nothing here...
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Getters & Setters
     */
    public void setLives() {
        this.lives--;
    }

    private void setName() {

        question.setMessage(Messages.INFO_SET_NICKNAME + "\n");
        this.username = prompt.getUserInput(question);

        if (username.equals("admin")) {
            if (role == "admin") {
                serverDispatch.sendPrivateWarning(Messages.INFO_SET_NICKNAME, this);
                setName();
                return;
            }
            adminLogin();
            return;
        }

        serverDispatch.sendPrivateWarning(Messages.MESSAGE_WELCOME, this);
        System.out.println(Colors.WHITE_UNDERLINED + this.username + Colors.RESET + " is connected"); //Server Info
    }

    public void setIsKicked(boolean kicked) {
        this.isKicked = kicked;

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
        return this.socket;

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

    public String getRole() {
        return this.role;
    }

    private boolean getIsKicked() {

        return this.isKicked;
    }


}