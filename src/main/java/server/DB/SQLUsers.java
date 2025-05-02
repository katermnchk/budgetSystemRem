package server.DB;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Transaction;
import client.clientWork.Users;
import server.SystemOrg.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class SQLUsers implements ISQLUsers {
    private static final Logger LOGGER = Logger.getLogger(SQLUsers.class.getName());

    private Connection conn;
    private static SQLUsers instance;

    public SQLUsers(Connection conn) {
        this.conn = conn;
    }

    private SQLUsers() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/budgetsystem";
        String user = "postgres";
        String password = "postgresql";
        this.conn = DriverManager.getConnection(url, user, password);
    }

    public static synchronized SQLUsers getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLUsers();
        }
        return instance;
    }

    /*public void hashExistingPasswords() {
        String selectSql = "SELECT id, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            ResultSet rs = selectStmt.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("id");
                String plainPassword = rs.getString("password");
                if (!plainPassword.startsWith("$2a$")) {
                    String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
                    updateStmt.setString(1, hashedPassword);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                    System.out.println("Пароль для userId=" + userId + " успешно хеширован.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при хешировании паролей: " + e.getMessage());
        }
    }
*/
    @Override
    public ArrayList<Users> findUser(Users obj) {
        ArrayList<Users> usList = new ArrayList<>();
        String sql = "SELECT u.username, u.firstname, u.lastname " +
                "FROM users u " +
                "WHERE u.username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, obj.getLogin());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Users user = new Users();
                user.setLogin(rs.getString("username"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                usList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usList;
    }

    @Override
    public Role insert(Users obj) {
        Role r = new Role();
        String sql = "SELECT insert_user(?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(obj.getPassword(), BCrypt.gensalt(12));
            stmt.setString(1, obj.getFirstname());
            stmt.setString(2, obj.getLastname());
            stmt.setString(3, obj.getLogin());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, "USER");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                r.setId(id);
                r.setRole("USER");

                Account defaultAccount = new Account(0, "Основной счет");
                addAccount(defaultAccount, id);
                System.out.println("Добавлен базовый счет для пользователя с ID: " + id);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Пользователь уже существует");
        } catch (SQLException e) {
            System.err.println("Ошибка при регистрации пользователя или добавлении счета: " + e.getMessage());
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public boolean deleteUser(Users obj) {
        try {
            String query = "DELETE FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, obj.getLogin());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<Users> get() {
        ArrayList<Users> usersList = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setLogin(rs.getString("username"));
                usersList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при запросе к базе данных: " + e.getMessage());
        }
        return usersList;
    }

    @Override
    public Users getUserById(Integer userId) throws SQLException {
        Users user = null;
        String sql = "SELECT id, username, firstname, lastname FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new Users();
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("username"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
            }
        }
        return user;
    }

    @Override
    public ArrayList<Account> getUserAccounts(Integer userId) throws SQLException {
        ArrayList<Account> accounts = new ArrayList<>();
        String sql = "SELECT a.id, a.name, COALESCE(SUM(t.amount), 0) AS balance " +
                "FROM accounts a " +
                "LEFT JOIN transactions t ON a.id = t.account_id " +
                "WHERE a.user_id = ? " +
                "GROUP BY a.id, a.name";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                accounts.add(new Account(id, name, balance));
                LOGGER.info("Загружен счет: id=" + id + ", name=" + name + ", balance=" + balance + " for user " + userId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка получения счетов для пользователя " + userId + ": " + e.getMessage());
            throw e;
        }
        return accounts;
    }

    @Override
    public ArrayList<Category> getIncomeCategories() throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories WHERE type = 'INCOME'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        }
        return categories;
    }

    @Override
    public ArrayList<Category> getExpenseCategories() throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM categories WHERE type = 'EXPENSE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        }
        return categories;
    }

    @Override
    public double getBalance(Integer userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) AS balance " +
                "FROM transactions t " +
                "WHERE t.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                LOGGER.info("Баланс для пользователя " + userId + ": " + balance);
                return balance;
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка расчета баланса для пользователя " + userId + ": " + e.getMessage());
            throw e;
        }
        LOGGER.info("Не найдено транзакций у пользователя " + userId + ", возвращаем 0.0");
        return 0.0;
    }

    @Override
    public HashMap<String, Double> getAccountBalances(Integer userId) throws SQLException {
        HashMap<String, Double> balances = new HashMap<>();
        String sql = "SELECT a.name, COALESCE(SUM(t.amount), 0) AS balance " +
                "FROM accounts a " +
                "LEFT JOIN transactions t ON a.id = t.account_id " +
                "WHERE a.user_id = ? " +
                "GROUP BY a.name";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String accountName = rs.getString("name");
                double balance = rs.getDouble("balance");
                balances.put(accountName, balance);
                LOGGER.info("Баланс счета для пользователя " + userId + ", счет " + accountName + ": " + balance);
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка получения балансов счетов для пользователя " + userId + ": " + e.getMessage());
            throw e;
        }
        return balances;
    }

    /*@Override
    public ArrayList<Transaction> getTransactionHistory(Integer userId) throws SQLException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.date, a.name AS account_name, c.name AS category_name, t.amount, t.description " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.id " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getTimestamp("date"),
                        rs.getString("account_name"),
                        rs.getString("category_name"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                ));
            }
        }
        return transactions;
    }*/
    @Override
    public ArrayList<Transaction> getTransactionHistory(Integer userId, HashMap<String, Object> filters) throws SQLException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.date, a.name AS account_name, c.name AS category_name, t.amount, t.description " +
                        "FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.id " +
                        "JOIN categories c ON t.category_id = c.id " +
                        "WHERE t.user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (filters.containsKey("startDate") && filters.get("startDate") != null) {
            sql.append(" AND t.date >= ?");
            params.add(Timestamp.valueOf((LocalDateTime) filters.get("startDate")));
        }
        if (filters.containsKey("endDate") && filters.get("endDate") != null) {
            sql.append(" AND t.date < ?");
            params.add(Timestamp.valueOf((LocalDateTime) filters.get("endDate")));
        }

        if (filters.containsKey("categoryId") && filters.get("categoryId") != null) {
            sql.append(" AND t.category_id = ?");
            params.add((Integer) filters.get("categoryId"));
        }

        if (filters.containsKey("categoryType") && filters.get("categoryType") != null) {
            sql.append(" AND c.type = ?");
            params.add((String) filters.get("categoryType"));
        }

        if (filters.containsKey("accountId") && filters.get("accountId") != null) {
            sql.append(" AND t.account_id = ?");
            params.add((Integer) filters.get("accountId"));
        }

        if (filters.containsKey("minAmount") && filters.get("minAmount") != null) {
            sql.append(" AND t.amount >= ?");
            params.add((Double) filters.get("minAmount"));
        }
        if (filters.containsKey("maxAmount") && filters.get("maxAmount") != null) {
            sql.append(" AND t.amount <= ?");
            params.add((Double) filters.get("maxAmount"));
        }

        if (filters.containsKey("description") && filters.get("description") != null) {
            sql.append(" AND t.description ILIKE ?");
            params.add("%" + filters.get("description") + "%");
        }

        sql.append(" ORDER BY t.date DESC");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getTimestamp("date"),
                        rs.getString("account_name"),
                        rs.getString("category_name"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                ));
            }
            LOGGER.info("Loaded " + transactions.size() + " transactions for user " + userId + " with filters: " + filters);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching transaction history for user " + userId + ": " + e.getMessage());
            throw e;
        }
        return transactions;
    }

    @Override
    public void addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, type) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getType());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                category.setId(rs.getInt("id"));
            }
        }
    }

    @Override
    public void deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Категория с ID " + categoryId + " не найдена.");
            }
        }
    }

    @Override
    public ArrayList<Category> getAllCategories(Integer userId) throws SQLException {
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("type")));
            }
        }
        return categories;
    }

    @Override
    public HashMap<String, Double> getExpenseChartData(Integer userId) throws SQLException {
        HashMap<String, Double> expenseData = new HashMap<>();
        String sql = "SELECT c.name, SUM(t.amount) as total " +
                "FROM transactions t " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.amount < 0 " +
                "GROUP BY c.name";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String categoryName = rs.getString("name");
                double total = Math.abs(rs.getDouble("total"));
                expenseData.put(categoryName, total);
            }
        }
        return expenseData;
    }

    @Override
    public void addAccount(Account account, Integer userId) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, name) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, account.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account.setId(rs.getInt("id"));
                LOGGER.info("Счет добавлен: id=" + account.getId() + ", name=" + account.getName() +
                        " для пользователя " + userId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка добавления счета для пользователя " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean editUser(Users user) {
        String sql = "UPDATE users SET firstname = ?, lastname = ?, username = ?, password = ?, role_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getLogin());
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            stmt.setString(4, hashedPassword);
            String role = user.getRole() != null ? user.getRole() : "USER";
            Integer roleId = getRoleId(role);
            if (roleId == null) {
                System.err.println("Ошибка: Роль " + role + " не найдена");
                return false;
            }
            stmt.setInt(5, roleId);
            stmt.setInt(6, user.getId());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Редактирование пользователя с id " + user.getId() + ": " + rowsAffected + " строк затронуто");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Ошибка при редактировании пользователя: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Integer getRoleId(String roleName) {
        String sql = "SELECT id FROM roles WHERE role = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении role_id для " + roleName + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public Integer getUsersCount() {
        String query = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете пользователей: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public Integer getTransactionsCount() {
        String query = "SELECT COUNT(*) FROM transactions";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете транзакций: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public HashMap<String, Object> getStatistics() {
        HashMap<String, Object> stats = new HashMap<>();
        try {
            String userSql = "SELECT COUNT(*) AS total FROM users";
            try (PreparedStatement stmt = conn.prepareStatement(userSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalUsers", rs.getInt("total"));
                }
            }
            String accountSql = "SELECT COUNT(*) AS total FROM accounts";
            try (PreparedStatement stmt = conn.prepareStatement(accountSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalAccounts", rs.getInt("total"));
                }
            }
            String balanceSql = "SELECT SUM(balance) AS total FROM accounts";
            try (PreparedStatement stmt = conn.prepareStatement(balanceSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalBalance", rs.getDouble("total"));
                }
            }
            String transactionSql = "SELECT COUNT(*) AS total FROM transactions";
            try (PreparedStatement stmt = conn.prepareStatement(transactionSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalTransactions", rs.getInt("total"));
                }
            }
            String roleSql = "SELECT r.role, COUNT(u.id) AS count " +
                    "FROM roles r LEFT JOIN users u ON r.id = u.role_id " +
                    "GROUP BY r.role";
            ArrayList<HashMap<String, Object>> roleStats = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(roleSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, Object> role = new HashMap<>();
                    role.put("role", rs.getString("role"));
                    role.put("count", rs.getInt("count"));
                    roleStats.add(role);
                }
            }
            stats.put("roleStats", roleStats);
            String categorySql = "SELECT c.name, c.type, COUNT(t.id) AS transaction_count, " +
                    "COALESCE(SUM(t.amount), 0) AS total_amount " +
                    "FROM categories c LEFT JOIN transactions t ON c.id = t.category_id " +
                    "GROUP BY c.name, c.type";
            ArrayList<HashMap<String, Object>> categoryStats = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(categorySql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, Object> category = new HashMap<>();
                    category.put("name", rs.getString("name"));
                    category.put("type", rs.getString("type"));
                    category.put("transactionCount", rs.getInt("transaction_count"));
                    category.put("totalAmount", rs.getDouble("total_amount"));
                    categoryStats.add(category);
                }
            }
            stats.put("categoryStats", categoryStats);
        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public boolean addGoal(int userId, int categoryId, String type, double targetAmount, String period) {
        String sql = "INSERT INTO goals (user_id, category_id, type, target_amount, period) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            stmt.setString(3, type);
            stmt.setDouble(4, targetAmount);
            stmt.setString(5, period);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении цели: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ArrayList<HashMap<String, Object>> getGoals(int userId) {
        ArrayList<HashMap<String, Object>> goals = new ArrayList<>();
        String sql = "SELECT g.id, g.category_id, g.type, g.target_amount, g.period, c.name AS category_name, " +
                "COALESCE(SUM(t.amount), 0) AS actual_amount " +
                "FROM goals g " +
                "JOIN categories c ON g.category_id = c.id " +
                "LEFT JOIN transactions t ON t.user_id = g.user_id AND t.category_id = g.category_id " +
                "AND t.date >= ? AND t.date < ? " +
                "WHERE g.user_id = ? " +
                "GROUP BY g.id, c.name";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = now.with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0);
            stmt.setTimestamp(1, Timestamp.valueOf(startOfMonth));
            stmt.setTimestamp(2, Timestamp.valueOf(endOfMonth));
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> goal = new HashMap<>();
                goal.put("id", rs.getInt("id"));
                goal.put("categoryId", rs.getInt("category_id"));
                goal.put("categoryName", rs.getString("category_name"));
                goal.put("type", rs.getString("type"));
                double targetAmount = rs.getDouble("target_amount");
                double actualAmount = rs.getDouble("actual_amount");
                goal.put("targetAmount", targetAmount);
                goal.put("actualAmount", actualAmount);
                goal.put("period", rs.getString("period"));
                boolean achieved = rs.getString("type").equals("INCOME") ? actualAmount >= targetAmount : actualAmount <= targetAmount * (-1);
                goal.put("achieved", achieved);
                goals.add(goal);
                System.out.println("Цель добавлена: category=" + rs.getString("category_name") + ", target=" + targetAmount + ", actual=" + actualAmount);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении целей: " + e.getMessage());
        }
        System.out.println("Всего целей для userId=" + userId + ": " + goals.size());
        return goals;
    }

    @Override
    public ArrayList<String> getRecommendations(int userId) {
        ArrayList<String> recommendations = new ArrayList<>();
        ArrayList<HashMap<String, Object>> goals = getGoals(userId);
        for (HashMap<String, Object> goal : goals) {
            Boolean achieved = (Boolean) goal.get("achieved");
            String type = (String) goal.get("type");
            if (achieved != null && !achieved && "EXPENSE".equals(type)) {
                Object targetObj = goal.get("targetAmount");
                Object actualObj = goal.get("actualAmount");
                if (targetObj instanceof Number && actualObj instanceof Number) {
                    double target = ((Number) targetObj).doubleValue();
                    double actual = ((Number) actualObj).doubleValue();
                    double excess = actual - target;
                    String categoryName = (String) goal.get("categoryName");
                    recommendations.add(String.format("Цель по расходам на '%s' превышена на %.2f BYN. Рекомендуем сократить траты.", categoryName, excess));

                    String sql = "SELECT c.name, SUM(t.amount) AS total " +
                            "FROM transactions t " +
                            "JOIN categories c ON t.category_id = c.id " +
                            "WHERE t.user_id = ? AND c.type = 'EXPENSE' AND c.id != ? " +
                            "AND t.date >= ? AND t.date < ? " +
                            "GROUP BY c.name " +
                            "HAVING SUM(t.amount) > 0 " +
                            "ORDER BY total DESC LIMIT 1";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
                        LocalDateTime endOfMonth = now.with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0);
                        stmt.setInt(1, userId);
                        stmt.setInt(2, (Integer) goal.get("categoryId"));
                        stmt.setTimestamp(3, Timestamp.valueOf(startOfMonth));
                        stmt.setTimestamp(4, Timestamp.valueOf(endOfMonth));
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            String otherCategory = rs.getString("name");
                            double otherAmount = rs.getDouble("total");
                            recommendations.add(String.format("Рассмотрите сокращение расходов на '%s' (текущие траты: %.2f BYN).", otherCategory, otherAmount));
                        }
                    } catch (SQLException e) {
                        System.err.println("Ошибка при генерации рекомендаций: " + e.getMessage());
                    }
                } else {
                    System.err.println("Ошибка: targetAmount или actualAmount не являются числами для категории " + goal.get("categoryName"));
                }
            }
        }
        return recommendations;
    }

    @Override
    public boolean editAccount(int accountId, int userId, String newName) throws SQLException {
        if (newName == null || newName.trim().isEmpty()) {
            LOGGER.warning("Некорректное название счета для accountId=" + accountId + ", userId=" + userId);
            throw new SQLException("Название счета не может быть пустым");
        }

        String sql = "UPDATE accounts SET name = ? WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, accountId);
            stmt.setInt(3, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("Счет не найден или доступ запрещен: accountId=" + accountId + ", userId=" + userId);
                throw new SQLException("Счет не найден или доступ запрещен");
            }
            LOGGER.info("Счет обновлен: accountId=" + accountId + ", newName=" + newName);
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Ошибка добавления счетов: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deleteAccount(int accountId, int userId) throws SQLException {
        String checkSql = "SELECT COUNT(*) AS transaction_count, COALESCE(SUM(amount), 0) AS balance " +
                "FROM transactions WHERE account_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, accountId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int transactionCount = rs.getInt("transaction_count");
                double balance = rs.getDouble("balance");
                if (transactionCount > 0 || balance != 0) {
                    LOGGER.warning("Нельзя удалить транзакции с нулевым балансов:" +
                            " accountId=" + accountId + ", transactions=" + transactionCount + ", balance=" + balance);
                    throw new SQLException("Нельзя удалить счет с транзакциями или ненулевым балансом");
                }
            }
        }

        String sql = "DELETE FROM accounts WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("Счет не найден или доступ запрещен: accountId=" + accountId + ", userId=" + userId);
                throw new SQLException("Счет не найден или доступ запрещен");
            }
            LOGGER.info("Счет удален: accountId=" + accountId + ", userId=" + userId);
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Ошибка удаления счета: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public HashMap<String, Object> getAccountInfo(int accountId, int userId) throws SQLException {
        HashMap<String, Object> accountInfo = new HashMap<>();
        String sql = "SELECT account_id, account_name, balance, transaction_count, category_stats " +
                "FROM account_stats WHERE account_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                accountInfo.put("accountId", rs.getInt("account_id"));
                accountInfo.put("accountName", rs.getString("account_name"));
                accountInfo.put("balance", rs.getDouble("balance"));
                accountInfo.put("transactionCount", rs.getInt("transaction_count"));
                accountInfo.put("categoryStats", rs.getObject("category_stats")); // JSON
            }
        }
        return accountInfo;
    }
}