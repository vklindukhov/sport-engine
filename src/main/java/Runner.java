import java.net.URL;

/**
 * Created by vklin on 12.01.2017.
 */
public class Runner {
    public static void main(String[] args) {
        URL resource = Runner.class.getResource("/path.txt");
    }
}
