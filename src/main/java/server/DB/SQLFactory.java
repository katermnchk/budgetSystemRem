package server.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLFactory extends AbstractFactory {
    public SQLUsers getUsers() throws SQLException, ClassNotFoundException {
        return SQLUsers.getInstance();
    }

    public SQLAuthorization getRole() throws SQLException, ClassNotFoundException {
        return SQLAuthorization.getInstance();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/budgetsystem", "postgres", "postgresql");
    }

    public SQLIncome getIncome() throws SQLException {
        return new SQLIncome(getConnection());
    }

    public SQLExpense getExpense() throws SQLException {
        return new SQLExpense(getConnection());
    }
}
