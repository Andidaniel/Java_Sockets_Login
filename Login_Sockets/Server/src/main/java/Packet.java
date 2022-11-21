import java.io.Serializable;

public class Packet implements Serializable {
    String message;
    String status;


    public Packet(String message, String status) {
        this.message = message;
        this.status = status;
    }
}
