import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static final int PORT = 6543;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;
    private String requestType = "";
    public void start() throws Exception {
        Socket socket = null;
        Packet packetToSend= new Packet("Get-Welcome-Prompt","GET");
        //For receiving and sending data
        boolean isClose = false;
        while (!isClose) {
            socket = new Socket("localhost", PORT);

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(packetToSend);
            Packet receivePacket = (Packet) inputStream.readObject();

            if(receivePacket.message!=""){
                System.out.println(receivePacket.message);
            }
            Scanner scanner = new Scanner(System.in);
            String message = new String();
            switch (receivePacket.status){
                case "CHOOSING_OPTION":
                        message = scanner.nextLine();
                        packetToSend.status = "POST";
                        packetToSend.message = message;
                    break;
                case "REGISTERING":
                    StringBuilder stringBuilderRegister = new StringBuilder();
                    System.out.println("Please enter your username:");
                    stringBuilderRegister.append(scanner.nextLine()+"\n");
                    System.out.println("Please enter your password:");
                    stringBuilderRegister.append(scanner.nextLine()+"\n");
                    System.out.println("Please enter your Account Security Question:");
                    stringBuilderRegister.append(scanner.nextLine()+"\n");
                    System.out.println("Please enter the answer to your Account Security Question:");
                    stringBuilderRegister.append(scanner.nextLine()+"\n");
                    packetToSend.message = stringBuilderRegister.toString();
                    packetToSend.status = "POST";
                    break;
                case "LOGGING_IN":
                    StringBuilder stringBuilderLogin = new StringBuilder();
                    System.out.println("Please enter your username:");
                    stringBuilderLogin.append(scanner.nextLine()+"\n");
                    System.out.println("Please enter your password:");
                    stringBuilderLogin.append(scanner.nextLine()+"\n");
                    packetToSend.message = stringBuilderLogin.toString();
                    packetToSend.status = "POST";
                    break;
                case "LOGGED":
                    message = scanner.nextLine();
                    packetToSend.status = "POST";
                    packetToSend.message = message;
                    break;
                case "RECOVERING":
                    message = scanner.nextLine();
                    packetToSend.status = "POST";
                    packetToSend.message = message;
                    break;
                case "ANSWERING":
                    message = scanner.nextLine();
                    packetToSend.status = "POST";
                    packetToSend.message = message;
                    break;
                case "EXITING":
                    isClose=true;
                    break;

                default:
                    isClose=true;
                    break;

            }


        }
        socket.close();
    }
}
