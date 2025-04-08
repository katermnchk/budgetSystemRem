package server.DB;

import java.sql.SQLException;

public interface ISQLExpense {
    void addTransaction(int userId, int accountId, int categoryId, double amount, String description) throws SQLException;
}
