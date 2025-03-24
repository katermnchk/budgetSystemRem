package server.DB;

import java.sql.SQLException;

public abstract class AbstractFactory {
    public abstract SQLUser getTeachers() throws SQLException, ClassNotFoundException;
    public abstract SQLAuthorization getRole() throws SQLException, ClassNotFoundException;
}
