package client.clientWork;

import models.TransactionRequest;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    private Socket clientSocket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    private String message;

    public Client(String ipAddress, String port) {
        try {
            clientSocket = new Socket(ipAddress, Integer.parseInt(port));
            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Сервер не нашел: " + e.getMessage());
            System.exit(0);
        }
    }

    public void sendMessage(String message) {
        try {
            outStream.writeObject(message);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object object){
        try {
            outStream.writeObject(object);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readMessage() throws IOException {
        try {
            message = (String) inStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    public Object readObject(){
        Object object = new Object();
        try {
            object = inStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void close() {
        try {
            clientSocket.close();
            inStream.close();
            outStream.close();
        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String topUpAccount(int accountId, double amount) throws IOException, ClassNotFoundException {
        outStream.writeObject("topUpAccount");
        outStream.writeObject(accountId);
        outStream.writeObject(amount);
        return (String) inStream.readObject();
    }

    public ArrayList<Account> getParentAccounts(int parentId) throws IOException, ClassNotFoundException {
        outStream.writeObject("getParentAccounts");
        Object response = inStream.readObject();
        if (response instanceof String) {
            throw new IOException((String) response);
        }
        return (ArrayList<Account>) response;
    }

    public String topUpChildAccount(int childAccountId, int parentAccountId, double amount) throws IOException, ClassNotFoundException {
        outStream.writeObject("topUpChildAccount");
        outStream.writeObject(childAccountId);
        outStream.writeObject(parentAccountId);
        outStream.writeObject(amount);
        return (String) inStream.readObject();
    }

    public String addExpense(TransactionRequest expenseRequest) throws IOException, ClassNotFoundException {
        sendMessage("addExpense");
        sendObject(expenseRequest);
        return (String) readObject();
    }

    public ArrayList<Account> getAccounts(int userId) throws IOException, ClassNotFoundException {
        sendMessage("getAccounts");
        sendObject(userId);
        Object response = readObject();
        if (response instanceof String) {
            throw new IOException((String) response);
        }
        return (ArrayList<Account>) response;
    }
}
