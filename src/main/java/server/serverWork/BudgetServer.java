package server.serverWork;

import server.DB.SQLFactory;
import server.DB.UserDAO;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class BudgetServer implements Runnable {
    private final int serverPort;
    private ServerSocket serverSocket;
    private boolean isStopped = false;

    public BudgetServer(int port) {
        this.serverPort = port;
    }

    @Override
    public void run() {
        openServerSocket();
        System.out.println("Сервер запущен на порту " + serverPort);

        while (!isStopped) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Клиент подключен: " + clientSocket.getInetAddress());

                Connection connection = SQLFactory.getConnection();
                UserDAO userDAO = new UserDAO(connection);

                ClientHandler clientHandler = new ClientHandler(clientSocket, userDAO);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                if (isStopped) {
                    System.out.println("Сервер остановлен.");
                    return;
                }
                e.printStackTrace();
            }
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
            System.out.println("Сервер ожидает подключения...");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка открытия порта " + this.serverPort, e);
        }
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при закрытии сервера", e);
        }
    }
}
