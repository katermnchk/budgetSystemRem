package server.serverWork;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Users;
import client.controllers.TransactionRequest;
import models.Authorization;
import server.DB.*;
import server.SystemOrg.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    protected Socket clientSocket = null;
    ObjectInputStream sois;
    ObjectOutputStream soos;

    public ClientHandler (Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            sois = new ObjectInputStream(clientSocket.getInputStream());
            soos = new ObjectOutputStream(clientSocket.getOutputStream());
            while (true) {
                System.out.println("Получение команды от клиента...");
                String choice = sois.readObject().toString();
                System.out.println(choice);
                System.out.println("Команда получена");
                switch (choice) {
                    case "userInf" -> {
                        System.out.println("Запрос к БД на получение информации о пользователях: " + clientSocket.getInetAddress().toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        ArrayList<Users> userList = sqlFactory.getUsers().get();
                        System.out.println(userList.toString());
                        soos.writeObject(userList);
                    }
                    case "findUser" -> {
                        System.out.println("Запрос к БД на поиск пользователя: " + clientSocket.getInetAddress().toString());
                        Users st = (Users) sois.readObject();
                        System.out.println(st.toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        ArrayList<Users> userList = sqlFactory.getUsers().findUser(st);
                        System.out.println(userList.toString());
                        soos.writeObject(userList);
                    }
                    case "deleteUser" -> {
                        System.out.println("Выполняется удаление пользователя...");
                        Users users = (Users) sois.readObject();
                        System.out.println(users.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getUsers().deleteUser(users)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при удалении пользователя");
                        }
                    }
                    case "registrationUser" -> {
                        System.out.println("Запрос к БД на регистрацию пользователя: " +
                                clientSocket.getInetAddress().toString());
                        Users user = (Users) sois.readObject();
                        System.out.println(user.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        Role r = sqlFactory.getUsers().insert(user);
                        System.out.println((r.toString()));

                        if (r.getId() != 0 && !r.getRole().isEmpty()) {
                            soos.writeObject("OK");
                            soos.writeObject(r);
                        } else {
                            soos.writeObject("This user is already existed");
                        }
                    }
                    case "authorization" -> {
                        System.out.println("Выполняется авторизация пользователя....");
                        Authorization auth = (Authorization) sois.readObject();
                        System.out.println(auth.toString());

                        SQLFactory sqlFactory = new SQLFactory();
                        SQLAuthorization authDAO = sqlFactory.getRole();

                        Role r = authDAO.getRole(auth);
                        System.out.println(r.toString());

                        if (r.getId() != 0 && !r.getRole().isEmpty()) {
                            soos.writeObject("OK");
                            soos.writeObject(r);
                        } else {
                            soos.writeObject("There is no data!");
                        }
                    }
                    case "addIncome" -> {
                        TransactionRequest request = (TransactionRequest) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            SQLIncome incomeDAO = sqlFactory.getIncome();
                            incomeDAO.addTransaction(
                                    request.getUserId(),
                                    request.getAccountId(),
                                    request.getCategoryId(),
                                    request.getAmount(),
                                    request.getDescription()
                            );
                            soos.writeObject("OK");
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при добавлении дохода: " + e.getMessage());
                        }
                    }
                    case "getUserAccounts" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Account> accounts = sqlFactory.getUsers().getUserAccounts(userId);
                            soos.writeObject(accounts);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении счетов: " + e.getMessage());
                        }
                    }
                    case "getIncomeCategories" -> {
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Category> categories = sqlFactory.getUsers().getIncomeCategories();
                            soos.writeObject(categories);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении категорий: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            System.out.println("Client disconnected.");
        }
    }
}
