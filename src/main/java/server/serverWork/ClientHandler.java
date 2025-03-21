package server.serverWork;

public class ClientHandler {
    public static final int PORT_WORK = 9006;

    public static void main(String[] args) {
        BudgetServer server = new BudgetServer(PORT_WORK);
        new Thread(server).start();
    }
}
