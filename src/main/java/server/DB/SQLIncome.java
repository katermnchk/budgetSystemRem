package server.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SQLIncome implements ISQLIncome {
    private final Connection connection;

    public SQLIncome(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addTransaction(int userId, int accountId, int categoryId, double amount, String description) throws SQLException {
        String query = "INSERT INTO transactions (user_id, account_id, category_id, amount, description, date) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, categoryId);
            stmt.setDouble(4, amount);
            stmt.setString(5, description != null ? description : "Доход");
            stmt.executeUpdate();


            String updateBalanceQuery = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateBalanceQuery)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setInt(2, accountId);
                updateStmt.executeUpdate();
            }
        }
    }
}