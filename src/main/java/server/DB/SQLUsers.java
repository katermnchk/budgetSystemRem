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
            stmt.setString(5, "USER"); // роль по умолчанию
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                r.setId(id);
                r.setRole("USER");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Пользователь уже существует");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public boolean deleteUser(Users obj) {
        // Реализация удаления пользователя (пример)
        return false;
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
        String sql = "SELECT SUM(CASE WHEN t.description = 'Доход' THEN t.amount ELSE -t.amount END) AS balance " +
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
                category.setId(rs.getInt("id")); // Обновляем ID категории
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
}

/*package server.DB;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.*;
import java.util.ArrayList;

public class SQLUsers implements ISQLUsers {
    private Connection conn;

    public SQLUsers(Connection conn) {
        this.conn = conn;
    }
    private static SQLUsers instance;
    private DatabaseConnection dbConnection;

    public SQLUsers() throws SQLException, ClassNotFoundException {
        dbConnection = DatabaseConnection.getInstance();
    }

    public static synchronized SQLUsers getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLUsers();
        }
        return instance;
    }

    @Override
    public ArrayList<Users> findUser(Users obj) {
        String str = "select `keys`.login, firstname, lastname, averageMark, `groups`.numberGroup, payments.payment" +
                " from users" +
                " join `keys` on `keys`.`id_keys` = students.id_keys" +
                " left join `groups` on `groups`.idgroup = users.idgroup" +
                " left join payments on payments.idpayment = users.idpayment" +
                " where `keys`.login = \"" + obj.getLogin() + "\";";
        ArrayList<String[]> result = dbConnection.getArrayResult(str);
        ArrayList<Users> usList = new ArrayList<>();
        for (String[] items: result){
            Users students = new Users();
            students.setLogin(items[0]);
            students.setFirstname(items[1]);
            students.setLastname(items[2]);
            usList.add(students);
        }
        return usList;
    }

    @Override
    public Role insert(Users obj) {
        String proc = "{? = call insert_user(?,?,?,?,?)}";
        Role r = new Role();
        try (CallableStatement callableStatement = DatabaseConnection.dbConnection.prepareCall(proc)) {
            //callableStatement.setString(1, obj.getFirstname());
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, obj.getFirstname());
            callableStatement.setString(3, obj.getLastname());
            callableStatement.setString(4, obj.getLogin());
            callableStatement.setString(5, obj.getPassword());
            callableStatement.setString(6, "USER"); //роль по умолчанию
            //callableStatement.registerOutParameter(5, Types.INTEGER);
            callableStatement.execute();

            int id = callableStatement.getInt(1);
            r.setId(id);
            r.setRole("user");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("ошибка");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public boolean deleteUser(Users obj) {
        return false;
    }

    public ArrayList<Users> get() {
        ArrayList<Users> usersList = new ArrayList<>();
        try {
            String query = "SELECT * FROM users";
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
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
}*/
