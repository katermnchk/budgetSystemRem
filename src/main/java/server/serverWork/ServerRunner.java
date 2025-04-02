package server.serverWork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunner {
    private static final int PORT = 9006;

    public static void main(String[] args) {
        BudgetServer server = new BudgetServer(PORT);
        new Thread(server).start();
    }
}