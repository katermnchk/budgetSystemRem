package server.serverWork;

import server.DB.SQLFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;

public class BudgetServer implements Runnable {
    protected int serverPort = 9006;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;

    public BudgetServer(int port) {
        this.serverPort = port;
    }

    @Override
    public void run() {
        openServerSocket();
        while(!isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Сервер остановлен") ;
                    return;
                }
                throw new RuntimeException("Ошибка подключения клиента", e);
            }
            new Thread(new ClientHandler(clientSocket)).start();
            System.out.println("Клиент подключен");
        }
        System.out.println("Сервер остановлен") ;
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
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
