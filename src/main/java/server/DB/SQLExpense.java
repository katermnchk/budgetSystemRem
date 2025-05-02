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
        if (amount <= 0) {
            LOGGER.warning("Некорректная сумма расходов: " + amount + " для пользователя " + userId);
            throw new SQLException("Сумма расхода должна быть положительной");
        }
        if (!isExpenseCategory(categoryId)) {
            LOGGER.warning("Некорректная категория расходов: categoryId=" + categoryId + " для пользователя " + userId);
            throw new SQLException("Выбранная категория не является категорией расхода");
        }

        connection.setAutoCommit(false);
        try {
            double currentBalance = getAccountBalance(accountId);
            if (currentBalance < amount) {
                LOGGER.warning("Недостаточно средств для пользователя " + userId + " на счету " + accountId + ". " +
                        "Текущий баланс: " + currentBalance + ", недостаточно средств: " + amount);
                throw new SQLException(String.format("Недостаточно средств на счете." +
                        " Текущий баланс: %.2f BYN, попытка потратить: %.2f BYN", currentBalance, amount));
            }

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
                LOGGER.info("Транзакция расходов добавлена для пользователя " + userId + " на счету " + accountId + ": " + expenseAmount);
            }

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

            connection.commit();
        } catch (SQLException e) {
            //откат транзакции при ошибке
            try {
                connection.rollback();
                LOGGER.info("Откат транзакции для пользователя " + userId + " на счету " + accountId);
            } catch (SQLException rollbackEx) {
                LOGGER.severe("Ошибка в откате транзакции: " + rollbackEx.getMessage());
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.severe("Ошибка восстановления автокоммита: " + e.getMessage());
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
                LOGGER.info("Баланс для счета " + accountId + ": " + balance);
                return balance;
            }
        }
        LOGGER.warning("Счет не найден: accountId=" + accountId);
        throw new SQLException("Счет с ID " + accountId + " не найден");
    }

    private boolean isExpenseCategory(int categoryId) throws SQLException {
        String query = "SELECT type FROM categories WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String categoryType = rs.getString("type");
                LOGGER.info("Тип категории для categoryId " + categoryId + ": " + categoryType);
                return "EXPENSE".equals(categoryType);
            }
        }
        LOGGER.warning("Категория не найдена: categoryId=" + categoryId);
        return false;
    }
}