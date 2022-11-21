import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;


enum STATE{
    NOT_LOGGED,
    REGISTERING,
    LOGGING_IN,
    RECOVERING,
    LOGGED,
    CHOOSING_OPTION,
    ANSWERING,
    EXITING
}
public class Server {
    public static final int PORT = 6543;
    public static final String connectionString = "jdbc:postgresql://localhost:5432/LOGIN_TW";
    private static final String connectionUsername = "postgres";
    private static final String connectionPassword = "admin";
    private boolean isOpen = false;
    private String clientUsername ="";
    private String accountSecurityQuestion="";
    private String accountSecurityAnswer="";
    private STATE state = STATE.NOT_LOGGED;

    public void start() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                Socket clientSocket = null;
                isOpen = true;

                System.out.println("Server is running");
                while (isOpen) {
                    clientSocket = serverSocket.accept(); // Keeps the program running until it gets a connection
                    new Thread(new ServerThread(clientSocket,this)).start();
                }
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void SetUsername(String username){
        this.clientUsername = username;
    }
    public String GetUsername(){
        return this.clientUsername;
    }
    public void SetState(STATE state){
        this.state = state;
    }
    public STATE GetState(){
        return this.state;
    }
    public String GetASQ(){
        return this.accountSecurityQuestion;
    }
    public String GetASQAnswer(){
        return this.accountSecurityAnswer;
    }


    public String HandleRegister(String username, String password, String question, String answer) {
        try {
            Connection con = DriverManager.getConnection(connectionString, connectionUsername, connectionPassword);

            String select = "SELECT * FROM users WHERE users.username = '" + username + "';";

            Statement statement = con.createStatement();

            ResultSet result = statement.executeQuery(select);

            clientUsername = "";

            if (result.next())
            {
                System.out.println("Username already exists.");
                con.close();
                return "ERROR";
            }
            else
            {
                String insertQuery = "INSERT INTO users (username, password, question, answer)" +
                        " VALUES('" + username + "', '" + password + "','" +  question + "','" + answer + "')";

                Statement createStatement = con.createStatement();
                createStatement.executeUpdate(insertQuery);

            }

            con.close();
            System.out.println("Success!");
            return "SUCCESS";

        } catch (SQLException e) {
            System.out.println("Error connecting to Database");
            e.printStackTrace();
            return "DB ERROR";
        }
    }
    public String HandleLogin(String username, String password) {
        try {
            Connection con = DriverManager.getConnection(connectionString, connectionUsername, connectionPassword);

            String select = "SELECT * FROM users WHERE users.username = '" + username +
                    "' AND users.password = '" + password +"';";

            Statement statement = con.createStatement();

            ResultSet result = statement.executeQuery(select);


            if (result.next()) {
                // System.out.println("Login successful");
                con.close();
                clientUsername = username;
                return "SUCCESS";
            }

            // System.out.println("Wrong username/password");
            con.close();
            return "INVALID";

        } catch (SQLException e) {
            System.out.println("Error connecting to Database");
            e.printStackTrace();
            return "DB ERROR";
        }
    }
    public boolean GetASCredentialsForUser(String username){
        try{
        Connection con = DriverManager.getConnection(connectionString, connectionUsername, connectionPassword);
        String select = "SELECT question,answer FROM users WHERE users.username = '" + username + "' LIMIT 1";
        Statement statement = con.createStatement();
        ResultSet result = statement.executeQuery(select);
        result.next();
        clientUsername = username;
        accountSecurityAnswer = result.getString("answer");
        accountSecurityQuestion = result.getString("question");
        return true;
        }
        catch (SQLException e) {
            System.out.println("Error connecting to Database");
            e.printStackTrace();
            return false;
        }
    }
}

