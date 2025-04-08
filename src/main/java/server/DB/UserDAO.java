/*package server.DB;

import client.clientWork.Users;
import java.sql.*;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    /*public boolean registerUser(Users user) throws SQLException {
        String query = "INSERT INTO users (firstname, lastname, username, password, role) VALUES (?, ?, ?, ?, 'USER')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, user.getPassword()); //надо добавить хеширование сос!
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean userExists(String username) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String[] authenticateUserWithRole(String username, String password) throws SQLException {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("id"), rs.getString("role")};
                }
            }
        }
        return null;
    }

    public boolean registerUser(Users user) throws SQLException {
        if (userExists(user.getLogin())) return false;

        String query = "INSERT INTO users (firstname, lastname, username, password, role) VALUES (?, ?, ?, ?, 'USER')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getLogin());
            stmt.setString(4, user.getPassword());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean userExists(String username) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String[] authenticateUserWithRole(String username, String password) throws SQLException {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Тут тоже должен быть хеш
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{String.valueOf(rs.getInt("id")), rs.getString("role")};
                }
            }
        }
        return null;
    }


    public void addTransaction(double amount, String category) throws SQLException {
        String query = "INSERT INTO transactions (user_id, amount, category, transaction_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int userId = 0;
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.setString(3, category);
            String date="";
            stmt.setDate(4, Date.valueOf(date));
            stmt.executeUpdate();
        }
    }

    // Получение списка пользователей
    public String getAllUsers() throws SQLException {
        StringBuilder usersList = new StringBuilder();
        String query = "SELECT id, firstname, lastname, username, role FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usersList.append(String.format("ID: %d, Имя: %s %s, Логин: %s, Роль: %s%n",
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("username"),
                        rs.getString("role")));
            }
        }
        return usersList.toString();
    }

    // Получение баланса пользователя
    public double getBalance(int userId) throws SQLException {
        String query = "SELECT SUM(amount) AS balance FROM transactions WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }
        return 0.0;
    }

    // Получение истории транзакций пользователя
    public String getTransactionHistory(int userId) throws SQLException {
        StringBuilder history = new StringBuilder();
        String query = "SELECT amount, category, transaction_date FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.append(String.format("Сумма: %.2f, Категория: %s, Дата: %s%n",
                            rs.getDouble("amount"),
                            rs.getString("category"),
                            rs.getDate("transaction_date")));
                }
            }
        }
        return history.toString();
    }

    public void addTransaction(int userId, double amount, String category) throws SQLException {
        String query = "INSERT INTO transactions (user_id, amount, category, transaction_date) VALUES (?, ?, ?, CURRENT_DATE)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount); // Доходы — положительные, расходы — отрицательные
            stmt.setString(3, category);
            stmt.executeUpdate();
        }
    }

    public double getBalance() {
        return 0;
    }

    public String getTransactionHistory() {
        return null;
    }
}*/
