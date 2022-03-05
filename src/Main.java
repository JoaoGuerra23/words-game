import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        //ServerDispatch serverDispatch = new ServerDispatch(Integer.valueOf(args[1]), Integer.valueOf(args[2]), args[3]);
        ServerDispatch serverDispatch = new ServerDispatch(8000, 2, "rescources/teste.txt");
        try {
            serverDispatch.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}