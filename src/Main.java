import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ServerDispatch serverDispatch = new ServerDispatch(args[0], args[1], args[2]);
        //ServerDispatch serverDispatch = new ServerDispatch(8000, 2, "rescources/teste.txt");
        try {
            serverDispatch.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}