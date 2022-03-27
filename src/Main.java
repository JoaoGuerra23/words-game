import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        //ServerDispatch serverDispatch = new ServerDispatch(args[0], args[1], args[2]);

        //Only for test purpose
        ServerDispatch serverDispatch = new ServerDispatch(8001, 4, "resources/teste.txt");

        try {
            serverDispatch.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}