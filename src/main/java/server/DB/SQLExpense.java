package server.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SQLExpense implements ISQLExpense {
    private static final Logger LOGGER = Logger.getLogger(SQLExpense.class.getName());
    private final Connection connection;

    public SQLExpense(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void addTransaction(int userId, int accountId, int categoryId, double amount, String description) throws SQLException {
        // Валидация входных данных
        if (amount <= 0) {
            LOGGER.warning("Invalid expense amount: " + amount + " for user " + userId);
            throw new SQLException("Сумма расхода должна быть положительной");
        }
        if (!isExpenseCategory(categoryId)) {
            LOGGER.warning("Invalid category for expense: categoryId=" + categoryId + " for user " + userId);
            throw new SQLException("Выбранная категория не является категорией расхода");
        }

        // Начало SQL-транзакции
        connection.setAutoCommit(false);
        try {
            // Проверка достаточности средств
            double currentBalance = getAccountBalance(accountId);
            if (currentBalance < amount) {
                LOGGER.warning("Insufficient funds for user " + userId + " on account " + accountId + ". Current balance: " + currentBalance + ", attempted expense: " + amount);
                throw new SQLException(String.format("Недостаточно средств на счете. Текущий баланс: %.2f BYN, попытка потратить: %.2f BYN", currentBalance, amount));
            }

            // Вставка транзакции с отрицательной суммой
            String insertQuery = "INSERT INTO transactions (user_id, account_id, category_id, amount, description, date) " +
                    "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, accountId);
                stmt.setInt(3, categoryId);
                double expenseAmount = -amount;
                stmt.setDouble(4, expenseAmount);
                stmt.setString(5, description != null ? description : "Расход");
                stmt.executeUpdate();
                LOGGER.info("Expense transaction added for user " + userId + " on account " + accountId + ": " + expenseAmount);
            }

            // Обновление баланса счета
            String updateBalanceQuery = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateBalanceQuery)) {
                updateStmt.setDouble(1, amount); // Уменьшаем баланс
                updateStmt.setInt(2, accountId);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Счет с ID " + accountId + " не найден");
                }
                LOGGER.info("Account balance updated for account " + accountId + ": reduced by " + amount);
            }

            // Фиксация транзакции
            connection.commit();
        } catch (SQLException e) {
            // Откат транзакции при ошибке
            try {
                connection.rollback();
                LOGGER.info("Transaction rolled back for user " + userId + " on account " + accountId);
            } catch (SQLException rollbackEx) {
                LOGGER.severe("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            throw e;
        } finally {
            // Восстановление автокоммита
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.severe("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }

    private double getAccountBalance(int accountId) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                LOGGER.info("Account balance for account " + accountId + ": " + balance);
                return balance;
            }
        }
        LOGGER.warning("Account not found: accountId=" + accountId);
        throw new SQLException("Счет с ID " + accountId + " не найден");
    }

    private boolean isExpenseCategory(int categoryId) throws SQLException {
        String query = "SELECT type FROM categories WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String categoryType = rs.getString("type");
                LOGGER.info("Category type for categoryId " + categoryId + ": " + categoryType);
                return "EXPENSE".equals(categoryType);
            }
        }
        LOGGER.warning("Category not found: categoryId=" + categoryId);
        return false;
    }
}