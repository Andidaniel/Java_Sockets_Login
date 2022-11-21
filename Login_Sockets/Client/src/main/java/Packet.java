import java.io.Serializable;
import java.util.Stack;

public class Packet implements Serializable {
    String message;
    String status;

    public Packet(String message, String status) {
        this.message = message;
        this.status = status;
    }
}
