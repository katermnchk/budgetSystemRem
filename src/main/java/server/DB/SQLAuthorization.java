package server.DB;

import client.controllers.Authorization;
import server.SystemOrg.Role;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;

public class SQLAuthorization implements ISQLAuthorization {
    private static SQLAuthorization instance;
    private  DatabaseConnection dbConnection;

    private SQLAuthorization() throws SQLException, ClassNotFoundException {
        dbConnection = DatabaseConnection.getInstance();
    }

    public static synchronized SQLAuthorization getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLAuthorization();
        }
        return instance;
    }

    @Override
    public Role getRole(Authorization obj) {
        String proc = "{call find_login(?,?,?,?)}";
        Role role = new Role();
        try (CallableStatement callableStatement = DatabaseConnection.dbConnection.prepareCall(proc)) {
            callableStatement.setString(1, obj.getLogin());
            callableStatement.setString(2, obj.getPassword());
            callableStatement.registerOutParameter(3, Types.INTEGER);
            callableStatement.registerOutParameter(4, Types.VARCHAR);
            callableStatement.execute();
            role.setId(callableStatement.getInt(3));
            role.setRole(callableStatement.getString(4));
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("ошибка");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return role;
    }
}
