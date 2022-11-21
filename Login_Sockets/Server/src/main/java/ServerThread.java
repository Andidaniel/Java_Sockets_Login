import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

public class ServerThread extends Thread {
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private Server server;

    private STATE state ;
    public ServerThread(Socket socket,Server server) {
        try {
            //For receiving and sending data
            this.in = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.server = server;
            this.state = server.GetState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            Packet receivedPacket = (Packet) this.in.readObject();
            System.out.println("\u001B[35m" + "Current state: "+this.state.toString()+ "\nReceived message: " + receivedPacket.message + "\nMethod: " +receivedPacket.status+"\n");
            execute(receivedPacket);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void execute(Packet receivedPacket) {
        Packet packet = new Packet("","");

        switch (receivedPacket.status) {
            case "GET":
                    packet.message = "\t Choose one of the following: \n" +
                            "1.REGISTER\n" +
                            "2.LOGIN\n" +
                            "3.RECOVER PASSWORD\n" +
                            "4.EXIT";
                    packet.status = "CHOOSING_OPTION";
                    server.SetState(STATE.CHOOSING_OPTION);
                break;
            case "POST":
                switch (this.state){
                    case CHOOSING_OPTION:
                        if(Objects.equals(receivedPacket.message, "1")){
                            packet.message = "\t--- Be careful ---";
                            packet.status = "REGISTERING";
                            server.SetState(STATE.REGISTERING);
                        } else if (Objects.equals(receivedPacket.message, "2")) {
                            packet.message = "\t--- Please enter your credentials ---";
                            packet.status = "LOGGING_IN";
                            server.SetState(STATE.LOGGING_IN);
                        } else if(Objects.equals(receivedPacket.message, "3")){
                            packet.message = "\t--- Please Enter Your Username ---";
                            packet.status = "RECOVERING";
                            server.SetState(STATE.RECOVERING);
                        } else if(Objects.equals(receivedPacket.message,"4")){
                            packet.message = "Exiting Program";
                            packet.status = "EXITING";
                            server.SetState(STATE.EXITING);
                        }
                        break;
                    case REGISTERING:
                        String[] inputRegister = receivedPacket.message.split("\\R");
                        String usernameRegister = inputRegister[0];
                        String passwordRegister = inputRegister[1];
                        String ASQRegister = inputRegister[2];
                        String answerRegister = inputRegister[3];
                        String registerQueryResult = this.server.HandleRegister(usernameRegister,passwordRegister,ASQRegister,answerRegister);
                        if(Objects.equals(registerQueryResult,"SUCCESS")){
                            packet.message = "\t --- Account created successfully! ---" +
                                    "\nChoose one of the following: \n" +
                                    "1.REGISTER\n" +
                                    "2.LOGIN\n" +
                                    "3.RECOVER PASSWORD\n" +
                                    "4.EXIT";
                            packet.status = "CHOOSING_OPTION";
                            server.SetState(STATE.CHOOSING_OPTION);
                        }
                        break;
                    case LOGGING_IN:
                        String[] inputLogin = receivedPacket.message.split("\\R");
                        String usernameLogin = inputLogin[0];
                        String passwordLogin = inputLogin[1];
                        String loginQueryResult = server.HandleLogin(usernameLogin,passwordLogin);
                        if(Objects.equals(loginQueryResult,"SUCCESS")){
                            packet.message = "\t --- Welcome, "+server.GetUsername()+"! ---" +
                                    "\n What would you like to do now?" +
                                    "\n 1.EXIT" +
                                    "\n 2.LOG OUT" +
                                    "\n";
                            packet.status = "LOGGED";
                            server.SetState(STATE.LOGGED);
                        }
                        else if(Objects.equals(loginQueryResult,"INVALID")){
                        packet.message = "\t --- Invalid credentials! Please try again! ---";
                        packet.status = "LOGGING_IN";
                        }
                        break;
                    case LOGGED:
                        if(Objects.equals(receivedPacket.message,"1")){
                            packet.message = "\t --- Goodbye ---";
                            packet.status = "EXITING";
                            server.SetState(STATE.EXITING);
                        }
                        else if(Objects.equals(receivedPacket.message,"2")){
                            packet.message = "\t Choose one of the following: \n" +
                                    "1.REGISTER\n" +
                                    "2.LOGIN\n" +
                                    "3.RECOVER PASSWORD\n" +
                                    "4.EXIT";
                            packet.status = "CHOOSING_OPTION";
                            server.SetState(STATE.CHOOSING_OPTION);
                        }
                        break;
                    case RECOVERING:
                        boolean result = server.GetASCredentialsForUser(receivedPacket.message);
                        packet.message = server.GetASQ();
                        packet.status = "ANSWERING";
                        server.SetState(STATE.ANSWERING);
                        break;
                    case ANSWERING:
                        if(Objects.equals(server.GetASQAnswer(),receivedPacket.message)){
                            packet.message = "\t --- Welcome, "+server.GetUsername()+"! ---" +
                                    "\n What would you like to do now?" +
                                    "\n 1.EXIT" +
                                    "\n 2.LOG OUT" +
                                    "\n";
                            packet.status = "LOGGED";
                            server.SetState(STATE.LOGGED);
                        }else{
                            packet.message = "HACKER DETECTED ----- !!!!!!!!!!!!!!! GO AWAY !!!!!!!!!! POLICE NOTIFIED";
                            packet.status = "EXITING";
                            server.SetState(STATE.EXITING);
                        }

                }
                break;

            default :
                packet = new Packet("Exiting Program","EXITING");
                server.SetState(STATE.EXITING);
                break;

        };

        try {
            this.out.writeObject(packet);
            if(server.GetState()==STATE.EXITING){
                return;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
