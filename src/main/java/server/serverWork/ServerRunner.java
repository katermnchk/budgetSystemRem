package server.serverWork;

public class ServerRunner {
    public static final int PORT = 9006;

    public static void main(String[] args) {
        BudgetServer server = new BudgetServer(PORT);
        new Thread(server).start();
    }
}
