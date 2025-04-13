package server.DB;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Transaction;
import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLUsers implements ISQLUsers {
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
        String proc = "SELECT insert_user(?, ?, ?, ?, ?)";
        Role r = new Role();
        try (PreparedStatement stmt = conn.prepareStatement(proc)) {
            stmt.setString(1, obj.getFirstname());
            stmt.setString(2, obj.getLastname());
            stmt.setString(3, obj.getLogin());
            stmt.setString(4, obj.getPassword());
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
        String sql = "SELECT id, name FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(rs.getInt("id"), rs.getString("name")));
            }
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
    public ArrayList<Category> getExpenseCategories()  throws SQLException {
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
        String sql = "SELECT SUM(CASE WHEN t.description = 'Доход' THEN t.amount ELSE +t.amount END) AS balance " +
                "FROM transactions t " +
                "WHERE t.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        }
        return 0.0;
    }

    @Override
    public HashMap<String, Double> getAccountBalances(Integer userId) throws SQLException {
        HashMap<String, Double> balances = new HashMap<>();
        String sql = "SELECT a.name, a.balance " +
                "FROM accounts a " +
                "WHERE a.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String accountName = rs.getString("name");
                double balance = rs.getDouble("balance");
                balances.put(accountName, balance);
            }
        }
        return balances;
    }

    @Override
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
        String sql = "INSERT INTO accounts (user_id, name, balance) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, account.getName());
            stmt.setDouble(3, 0.0);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account.setId(rs.getInt("id"));
            }
        }
    }

    public boolean editUser(Users user) {
        String sql = "UPDATE users SET firstname = ?, lastname = ?, username = ?, password = ?, role_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, user.getPassword());
            // Найти role_id по имени роли
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
            //всего пользователей
            String userSql = "SELECT COUNT(*) AS total FROM users";
            try (PreparedStatement stmt = conn.prepareStatement(userSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalUsers", rs.getInt("total"));
                }
            }
            //всего счетов
            String accountSql = "SELECT COUNT(*) AS total FROM accounts";
            try (PreparedStatement stmt = conn.prepareStatement(accountSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalAccounts", rs.getInt("total"));
                }
            }
            //общий баланс
            String balanceSql = "SELECT SUM(balance) AS total FROM accounts";
            try (PreparedStatement stmt = conn.prepareStatement(balanceSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalBalance", rs.getDouble("total"));
                }
            }
            // всего транзакций
            String transactionSql = "SELECT COUNT(*) AS total FROM transactions";
            try (PreparedStatement stmt = conn.prepareStatement(transactionSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalTransactions", rs.getInt("total"));
                }
            }
            //статистика по ролям
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
            // статистика по категориям
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
}