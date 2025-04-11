package server.serverWork;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Transaction;
import client.clientWork.Users;
import client.controllers.TransactionRequest;
import models.Authorization;
import server.DB.*;
import server.SystemOrg.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
                        Integer userId = (Integer) sois.readObject();
                        System.out.println("Запрос информации о пользователе с ID: " + userId);
                        SQLFactory sqlFactory = new SQLFactory();
                        Users user = sqlFactory.getUsers().getUserById(userId);
                        if (user != null) {
                            soos.writeObject(user);
                            System.out.println("Информация о пользователе отправлена: " + user.getLogin());
                        } else {
                            soos.writeObject("Пользователь не найден");
                            System.out.println("Пользователь с ID " + userId + " не найден");
                        }
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
                    case "addExpense" -> {
                        TransactionRequest request = (TransactionRequest) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            SQLExpense expenseDAO = sqlFactory.getExpense();
                            expenseDAO.addTransaction(
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
                    case "getExpenseCategories" -> {
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Category> categories = sqlFactory.getUsers().getExpenseCategories();
                            soos.writeObject(categories);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении категорий: " + e.getMessage());
                        }
                    }
                    case "getBalance" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            double balance = sqlFactory.getUsers().getBalance(userId);
                            soos.writeObject(String.valueOf(balance));
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении баланса: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "getAccountBalances" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            HashMap<String, Double> balances = sqlFactory.getUsers().getAccountBalances(userId);
                            soos.writeObject(balances);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении балансов счетов: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "getTransactionHistory" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Transaction> transactions = sqlFactory.getUsers().getTransactionHistory(userId);
                            soos.writeObject(transactions);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении истории транзакций: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "addCategory" -> {
                        Category category = (Category) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            sqlFactory.getUsers().addCategory(category);
                            soos.writeObject("OK");
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при добавлении категории: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "deleteCategory" -> {
                        Integer categoryId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            sqlFactory.getUsers().deleteCategory(categoryId);
                            soos.writeObject("OK");
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при удалении категории: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "getAllCategories" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Category> categories = sqlFactory.getUsers().getAllCategories(userId);
                            soos.writeObject(categories);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении категорий: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "getExpenseChartData" -> {
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            HashMap<String, Double> expenseData = sqlFactory.getUsers().getExpenseChartData(userId);
                            soos.writeObject(expenseData);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении данных для графика: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "addAccount" -> {
                        Account account = (Account) sois.readObject();
                        Integer userId = (Integer) sois.readObject();
                        SQLFactory sqlFactory = new SQLFactory();
                        try {
                            sqlFactory.getUsers().addAccount(account, userId);
                            soos.writeObject("OK");
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при добавлении счета " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            System.out.println("Client disconnected.");
        }
    }
}
