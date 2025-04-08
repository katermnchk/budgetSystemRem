package server.DB;

import java.sql.SQLException;

public interface ISQLIncome {
    void addTransaction(int userId, int accountId, int categoryId, double amount, String description) throws SQLException;
}