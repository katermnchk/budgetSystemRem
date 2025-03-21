package server.serverWork;

//import DB.SQLFactory;
//import SchoolOrg.*;


import java.io.*;
import java.net.Socket;


public class Worker implements Runnable {
    protected Socket clientSocket = null;
    ObjectInputStream sois;
    ObjectOutputStream soos;

    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {

    }
}
