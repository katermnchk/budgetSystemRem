package server.serverWork;

import server.DB.UserDAO;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final UserDAO userDAO;

    public ClientHandler(Socket socket, UserDAO userDAO) {
        this.socket = socket;
        this.userDAO = userDAO;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            while (true) {
                Object received = in.readObject();
                if (!(received instanceof String)) {
                    out.writeObject("INVALID REQUEST");
                    continue;
                }

                String request = (String) received;
                String[] parts = request.split(" ");
                String command = parts[0];

                switch (command) {
                    case "REGISTER":
                        if (parts.length == 3) {
                            boolean success = userDAO.registerUser(parts[1], parts[2]);
                            out.writeObject(success ? "REGISTER_SUCCESS" : "REGISTER_FAIL");
                        } else {
                            out.writeObject("INVALID FORMAT");
                        }
                        break;
                    case "LOGIN":
                        if (parts.length == 3) {
                            boolean success = userDAO.authenticateUser(parts[1], parts[2]);
                            out.writeObject(success ? "LOGIN_SUCCESS" : "LOGIN_FAIL");
                        } else {
                            out.writeObject("INVALID FORMAT");
                        }
                        break;
                    default:
                        out.writeObject("UNKNOWN COMMAND");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка связи с клиентом: " + e.getMessage());
        }
    }
}
