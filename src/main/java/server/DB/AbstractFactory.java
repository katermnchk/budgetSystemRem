package server.DB;

import java.sql.SQLException;

public abstract class AbstractFactory {
    public abstract SQLUsers getUsers() throws SQLException, ClassNotFoundException;
    public abstract SQLAuthorization getRole() throws SQLException, ClassNotFoundException;
}
