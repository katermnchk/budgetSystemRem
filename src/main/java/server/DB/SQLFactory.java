package server.DB;

import java.sql.SQLException;

public class SQLFactory extends AbstractFactory {
    public SQLUsers getUsers() throws SQLException, ClassNotFoundException {
        return SQLUsers.getInstance();
    }

    public SQLAuthorization getRole() throws SQLException, ClassNotFoundException {
        return SQLAuthorization.getInstance();
    }

}
