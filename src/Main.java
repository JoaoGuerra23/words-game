import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ServerDispatch serverDispatch = new ServerDispatch(8000, 2);

        try {
            serverDispatch.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
