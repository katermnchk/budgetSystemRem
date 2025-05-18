package server.serverWork;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Transaction;
import client.clientWork.Users;
import models.TransactionRequest;
import models.Authorization;
import server.DB.*;
import server.SystemOrg.*;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    protected Socket clientSocket = null;
    ObjectInputStream sois;
    ObjectOutputStream soos;
    private int currentUserId = 0;

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
                SQLFactory sqlFactory = new SQLFactory();
                switch (choice) {
                    case "userInf" -> {
                        Integer userId = (Integer) sois.readObject();
                        System.out.println("Запрос информации о пользователе с ID: " + userId);

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
                        //SQLFactory sqlFactory = new SQLFactory();
                        ArrayList<Users> userList = sqlFactory.getUsers().findUser(st);
                        System.out.println(userList.toString());
                        soos.writeObject(userList);
                    }
                    case "getUsers" -> {
                        System.out.println("Запрос списка всех пользователей: " + clientSocket.getInetAddress().toString());
                        ArrayList<Users> usersList = sqlFactory.getUsers().get();
                        soos.writeObject(usersList);
                    }
                    case "deleteUser" -> {
                        System.out.println("Запрос на удаление пользователя: " + clientSocket.getInetAddress().toString());
                        Users user = (Users) sois.readObject();
                        boolean success = sqlFactory.getUsers().deleteUser(user);
                        soos.writeObject(success ? "OK" : "Ошибка при удалении пользователя");
                    }
                    case "registrationUser" -> {
                        System.out.println("Запрос к БД на регистрацию пользователя: " + clientSocket.getInetAddress().toString());
                        Users user = (Users) sois.readObject();
                        System.out.println(user.toString());
                        Role r = sqlFactory.getUsers().insert(user);
                        System.out.println(r.toString());

                        if (r.getId() != 0 && !r.getRole().isEmpty()) {
                            currentUserId = r.getId();
                            soos.writeObject("OK");
                            soos.writeObject(r);
                        } else {
                            currentUserId = 0;
                            soos.writeObject("Этот пользователь уже существует");
                        }
                    }
                    case "authorization" -> {
                        System.out.println("Выполняется авторизация пользователя....");
                        Authorization auth = (Authorization) sois.readObject();
                        System.out.println(auth.toString());
                        SQLAuthorization authDAO = sqlFactory.getRole();
                        Role r = authDAO.getRole(auth);
                        System.out.println(r.toString());

                        if (r.getId() != 0 && !r.getRole().isEmpty()) {
                            currentUserId = r.getId();
                            soos.writeObject("OK");
                            soos.writeObject(r);
                        } else {
                            currentUserId = 0;
                            soos.writeObject("There is no data!");
                        }
                    }
                    case "addIncome" -> {
                        TransactionRequest request = (TransactionRequest) sois.readObject();
                        //SQLFactory sqlFactory = new SQLFactory();
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
                        //SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Account> accounts = sqlFactory.getUsers().getUserAccounts(currentUserId);
                            soos.writeObject(accounts);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении счетов: " + e.getMessage());
                        }
                    }
                    case "getIncomeCategories" -> {
                       // SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Category> categories = sqlFactory.getUsers().getIncomeCategories();
                            soos.writeObject(categories);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении категорий: " + e.getMessage());
                        }
                    }
                    case "addExpense" -> {
                        TransactionRequest request = (TransactionRequest) sois.readObject();
                        //SQLFactory sqlFactory = new SQLFactory();
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
                       // SQLFactory sqlFactory = new SQLFactory();
                        try {
                            ArrayList<Category> categories = sqlFactory.getUsers().getExpenseCategories();
                            soos.writeObject(categories);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении категорий: " + e.getMessage());
                        }
                    }
                    case "getBalance" -> {
                        Integer userId = (Integer) sois.readObject();
                        //SQLFactory sqlFactory = new SQLFactory();
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
                        try {
                            HashMap<String, Double> balances = sqlFactory.getUsers().getAccountBalances(userId);
                            soos.writeObject(balances);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении балансов счетов: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "getAccounts" -> {
                        Integer userId = (Integer) sois.readObject();
                        try {
                            ArrayList<Account> accounts = sqlFactory.getUsers().getAccounts(userId);
                            LOGGER.info("Отправлено " + accounts.size() + " счетов для userId: " + userId);
                            soos.writeObject(accounts);
                        } catch (SQLException e) {
                            String errorMessage = "Ошибка при получении счетов: " + e.getMessage();
                            LOGGER.severe(errorMessage);
                            soos.writeObject(errorMessage);
                            e.printStackTrace();
                        }
                    }
                    case "getTransactionHistory" -> {
                        Integer userId = (Integer) sois.readObject();
                        HashMap<String, Object> filters = (HashMap<String, Object>) sois.readObject();
                        LOGGER.info("Processing getTransactionHistory for userId=" + userId + ", filters=" + filters);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован или доступ запрещен");
                            LOGGER.warning("Unauthorized getTransactionHistory attempt: userId=" + userId);
                            return;
                        }
                        try {
                            ArrayList<Transaction> transactions = sqlFactory.getUsers().getTransactionHistory(userId, filters);
                            soos.writeObject(transactions);
                            LOGGER.info("Sent " + transactions.size() + " transactions for userId=" + userId);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при получении истории транзакций: " + e.getMessage());
                            LOGGER.severe("Error fetching transaction history: " + e.getMessage());
                        }
                    }
                    case "addCategory" -> {
                        Category category = (Category) sois.readObject();
                       // SQLFactory sqlFactory = new SQLFactory();
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
                       // SQLFactory sqlFactory = new SQLFactory();
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
                      //  SQLFactory sqlFactory = new SQLFactory();
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
                        //SQLFactory sqlFactory = new SQLFactory();
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
                        //SQLFactory sqlFactory = new SQLFactory();
                        try {
                            sqlFactory.getUsers().addAccount(account, userId);
                            soos.writeObject("OK");
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при добавлении счета " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "manageCategories" -> {
                        System.out.println("Запрос на получение категорий: " + clientSocket.getInetAddress().toString());
                        //SQLFactory sqlFactory = new SQLFactory();
                        ArrayList<Category> categories = sqlFactory.getUsers().getAllCategories(0); // 0 для всех категорий
                        soos.writeObject(categories);
                    }
                    case "editUser" -> {
                        System.out.println("Запрос на редактирование пользователя: " + clientSocket.getInetAddress().toString());
                        Users user = (Users) sois.readObject();
                        boolean success = sqlFactory.getUsers().editUser(user);
                        soos.writeObject(success ? "OK" : "Ошибка при редактировании пользователя");
                    }
                    case "logout" -> {
                        System.out.println("Пользователь вышел: " + clientSocket.getInetAddress().toString());
                        currentUserId = 0;
                        soos.writeObject("OK");
                    }
                    case "getStatistics" -> {
                        System.out.println("Запрос статистики: " + clientSocket.getInetAddress().toString());
                        HashMap<String, Object> stats = sqlFactory.getUsers().getStatistics();
                        soos.writeObject(stats);
                    }
                    case "addGoal" -> {
                        System.out.println("Запрос на добавление цели: " + clientSocket.getInetAddress());
                        HashMap<String, Object> goalData = (HashMap<String, Object>) sois.readObject();
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                        } else {
                            boolean success = sqlFactory.getUsers().addGoal(
                                    currentUserId,
                                    ((Number) goalData.get("categoryId")).intValue(),
                                    (String) goalData.get("type"),
                                    ((Number) goalData.get("targetAmount")).doubleValue(),
                                    (String) goalData.get("period")
                            );
                            soos.writeObject(success ? "OK" : "Ошибка при добавлении цели");
                        }
                    }
                    case "getGoals" -> {
                        System.out.println("Запрос целей пользователя: " + clientSocket.getInetAddress());
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                        } else {
                            ArrayList<HashMap<String, Object>> goals = sqlFactory.getUsers().getGoals(currentUserId);
                            soos.writeObject(goals);
                        }
                    }
                    case "getRecommendations" -> {
                        System.out.println("Запрос рекомендаций: " + clientSocket.getInetAddress());
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                        } else {
                            ArrayList<String> recommendations = sqlFactory.getUsers().getRecommendations(currentUserId);
                            soos.writeObject(recommendations);
                        }
                    }
                    case "editAccount" -> {
                        HashMap<String, Object> accountData = (HashMap<String, Object>) sois.readObject();
                        Integer accountId = (Integer) accountData.get("accountId");
                        String newName = (String) accountData.get("newName");
                        LOGGER.info("Обработка editAccount: accountId=" + accountId + ", newName=" + newName + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Пользователь не авторизован: accountId=" + accountId);
                            return;
                        }
                        try {
                            sqlFactory.getUsers().editAccount(accountId, currentUserId, newName);
                            soos.writeObject("OK");
                            LOGGER.info("Счета успешно изменен: accountId=" + accountId);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при редактировании счета: " + e.getMessage());
                            LOGGER.severe("Ошибка при редактирования счета: " + e.getMessage());
                        }
                    }
                    case "deleteAccount" -> {
                        Integer accountId = (Integer) sois.readObject();
                        LOGGER.info("Обработка deleteAccount: accountId=" + accountId + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Пользователь не авторизован: accountId=" + accountId);
                            return;
                        }
                        try {
                            sqlFactory.getUsers().deleteAccount(accountId, currentUserId);
                            soos.writeObject("OK");
                            LOGGER.info("Счет успешно удален: accountId=" + accountId);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при удалении счета: " + e.getMessage());
                            LOGGER.severe("Ошибка при удалении счета: " + e.getMessage());
                        }
                    }
                    case "getAccountInfo" -> {
                        System.out.println("Запрос информации о счете: " + clientSocket.getInetAddress());
                        Integer accountId = (Integer) sois.readObject();
                        HashMap<String, Object> accountInfo = sqlFactory.getUsers().getAccountInfo(accountId, currentUserId);
                        soos.writeObject(accountInfo.isEmpty() ? "Ошибка: счет не найден или не принадлежит пользователю" : "OK");
                        if (!accountInfo.isEmpty()) {
                            soos.writeObject(accountInfo);
                        }
                    }
                    case "getChildren" -> {
                        LOGGER.info("Запрос списка детей для userId: " + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос списка детей");
                            continue;
                        }
                        try {
                            ArrayList<Users> children = sqlFactory.getChildren().getChildren(currentUserId);
                            soos.writeObject(children);
                            LOGGER.info("Отправлено " + children.size() + " детей для userId=" + currentUserId);
                        } catch (SQLException e) {
                            soos.writeObject(new ArrayList<Users>()); // Пустой список вместо строки ошибки
                            LOGGER.severe("Ошибка при получении списка детей: " + e.getMessage());
                        }
                    }
                    case "getChildAccounts" -> {
                        Integer childId = (Integer) sois.readObject();
                        LOGGER.info("Запрос счетов ребенка: childId=" + childId + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос счетов ребенка: childId=" + childId);
                            continue;
                        }
                        try {
                            ArrayList<Account> accounts = sqlFactory.getChildren().getChildAccounts(childId, currentUserId);
                            soos.writeObject(accounts);
                            LOGGER.info("Отправлено " + accounts.size() + " счетов для childId=" + childId);
                        } catch (SQLException e) {
                            soos.writeObject(new ArrayList<Account>());
                            LOGGER.severe("Ошибка при получении счетов ребенка: " + e.getMessage());
                        }
                    }
                    case "addChild" -> {
                        Users child = (Users) sois.readObject();
                        Integer parentId = (Integer) sois.readObject();
                        LOGGER.info("Добавление ребенка для parentId: " + parentId + ", child=" + child.toString());
                        if (currentUserId == 0 || currentUserId != parentId) {
                            soos.writeObject("Ошибка: пользователь не авторизован или не имеет прав");
                            LOGGER.warning("Неавторизованный запрос добавления ребенка: parentId=" + parentId);
                            continue;
                        }
                        if (child.getLogin() == null || child.getLogin().isEmpty()) {
                            soos.writeObject("Ошибка: поле login не заполнено");
                            LOGGER.warning("Попытка добавить ребенка с пустым login");
                            continue;
                        }
                        try {
                            Role r = sqlFactory.getChildren().addChild(child, parentId);
                            if (r.getId() != 0 && !r.getRole().isEmpty()) {
                                soos.writeObject("OK");
                                LOGGER.info("Ребенок успешно добавлен: login=" + child.getLogin());
                            } else {
                                soos.writeObject("Этот логин уже существует");
                                LOGGER.warning("Добавление ребенка не удалось: логин уже существует");
                            }
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при добавлении ребенка: " + e.getMessage());
                            LOGGER.severe("Ошибка при добавлении ребенка: " + e.getMessage());
                        }
                    }
                    case "toggleBlockAccount" -> {
                        Integer accountId = (Integer) sois.readObject();
                        LOGGER.info("Обработка toggleBlockAccount: accountId=" + accountId + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос блокировки счета: accountId=" + accountId);
                            continue;
                        }
                        try {
                            boolean success = sqlFactory.getChildren().toggleBlockAccount(accountId, currentUserId);
                            soos.writeObject(success ? "OK" : "Ошибка при блокировке/разблокировке счета");
                            LOGGER.info("Блокировка/разблокировка счета: " + (success ? "успешно" : "неудачно") + ", accountId=" + accountId);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при блокировке/разблокировке счета: " + e.getMessage());
                            LOGGER.severe("Ошибка при блокировке/разблокировке счета: " + e.getMessage());
                        }
                    }
                    case "topUpAccount" -> {
                        int accountId = (int) sois.readObject();
                        double amount = (double) sois.readObject();
                        LOGGER.info("Обработка topUpAccount: accountId=" + accountId + ", amount=" + amount + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос пополнения счета");
                            continue;
                        }
                        String result = sqlFactory.getChildren().topUpAccount(accountId, amount, currentUserId);
                        soos.writeObject(result);
                        if ("OK".equals(result)) {
                            LOGGER.info("Счет успешно пополнен: accountId=" + accountId + ", amount=" + amount);
                        } else {
                            LOGGER.warning("Ошибка при пополнении счета: " + result);
                        }
                    }
                    case "topUpChildAccount" -> {
                        int childAccountId = (int) sois.readObject();
                        int parentAccountId = (int) sois.readObject();
                        double amount = (double) sois.readObject();
                        LOGGER.info("Обработка topUpChildAccountFromParentAccount: childAccountId=" + childAccountId +
                                ", parentAccountId=" + parentAccountId + ", amount=" + amount + ", userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос пополнения счета");
                            continue;
                        }
                        String result = sqlFactory.getChildren().topUpChildAccount (
                                childAccountId, parentAccountId, amount, currentUserId);
                        soos.writeObject(result);
                        if ("OK".equals(result)) {
                            LOGGER.info("Счет ребёнка успешно пополнен: childAccountId=" + childAccountId +
                                    ", parentAccountId=" + parentAccountId + ", amount=" + amount);
                        } else {
                            LOGGER.warning("Ошибка при пополнении счета: " + result);
                        }
                    }
                    case "getParentAccounts" -> {
                        LOGGER.info("Запрос счетов родителя: userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("Неавторизованный запрос счетов родителя");
                            continue;
                        }
                        try {
                            ArrayList<Account> accounts = sqlFactory.getChildren().getParentAccounts(currentUserId);
                            soos.writeObject(accounts);
                            LOGGER.info("Отправлено " + accounts.size() + " счетов для родителя userId=" + currentUserId);
                        } catch (SQLException e) {
                            soos.writeObject(new ArrayList<Account>());
                            LOGGER.severe("Ошибка при получении счетов родителя: " + e.getMessage());
                        }
                    }
                    case "filterExpenseData" -> {
                        LOGGER.info("[" + LocalDateTime.now() + "] Processing filterExpenseData for userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("[" + LocalDateTime.now() + "] Unauthorized filterExpenseData attempt: userId=" + currentUserId);
                            continue;
                        }
                        try {

                            Object[] params = (Object[]) sois.readObject();
                            Integer userId = (Integer) params[0];
                            String period = (String) params[1];

                            HashMap<String, Double> filteredExpenseData = filterExpensesByPeriod(userId, period, sqlFactory);
                            soos.writeObject(filteredExpenseData);
                            LOGGER.info("[" + LocalDateTime.now() + "] Sent filtered expense data for userId=" + userId + ", period=" + period);
                        } catch (IOException | ClassNotFoundException e) {
                            soos.writeObject("Ошибка при фильтрации данных расходов: " + e.getMessage());
                            LOGGER.severe("[" + LocalDateTime.now() + "] Error filtering expense data: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    case "deleteGoal" -> {
                        LOGGER.info("[" + LocalDateTime.now() + "] Processing deleteGoal for userId=" + currentUserId);
                        if (currentUserId == 0) {
                            soos.writeObject("Ошибка: пользователь не авторизован");
                            LOGGER.warning("[" + LocalDateTime.now() + "] Unauthorized deleteGoal attempt: userId=" + currentUserId);
                            continue;
                        }
                        try {
                            Integer goalId = (Integer) sois.readObject();
                            boolean success = sqlFactory.getUsers().deleteGoal(goalId, currentUserId);
                            soos.writeObject(success ? "OK" : "Ошибка при удалении цели");
                            LOGGER.info("[" + LocalDateTime.now() + "] Goal deletion " + (success ? "successful" : "failed") + ": goalId=" + goalId);
                        } catch (SQLException e) {
                            soos.writeObject("Ошибка при удалении цели: " + e.getMessage());
                            LOGGER.severe("[" + LocalDateTime.now() + "] Error deleting goal: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | SQLException e) {
            System.out.println("Клиент отключен");
        }
    }

    private HashMap<String, Double> filterExpensesByPeriod(Integer userId, String period, SQLFactory sqlFactory) throws SQLException, ClassNotFoundException {
        HashMap<String, Double> expenseData = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        switch (period) {
            case "За день":
                startDate = now.truncatedTo(ChronoUnit.DAYS);
                break;
            case "За неделю":
                startDate = now.minusDays(7);
                break;
            case "За месяц":
                startDate = now.minusMonths(1);
                break;
            default:
                startDate = now.minusMonths(1);
                LOGGER.warning("[" + LocalDateTime.now() + "] Unknown period '" + period + "', defaulting to 1 month");
        }

        ArrayList<Transaction> transactions = sqlFactory.getUsers().getTransactionHistory(userId, new HashMap<>());
        for (Transaction transaction : transactions) {
            LocalDateTime transactionDate = transaction.getDate().toLocalDateTime();

            if (transaction.getAmount() < 0 && transactionDate.isAfter(startDate)) {
                String categoryName = transaction.getCategoryName();
                double amount = Math.abs(transaction.getAmount());
                expenseData.merge(categoryName, amount, Double::sum);
            }
        }

        return expenseData;
    }
}
