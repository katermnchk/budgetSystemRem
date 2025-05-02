package server.DB;

import models.Authorization;
import server.SystemOrg.Role;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class SQLAuthorization implements ISQLAuthorization {
    private static SQLAuthorization instance;
    private DatabaseConnection dbConnection;

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
        Role r = new Role();
        String sql = "SELECT id, password, role_id FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, obj.getLogin());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(obj.getPassword(), hashedPassword)) {
                    r.setId(rs.getInt("id"));
                    String role = rs.getInt("role_id") == 1 ? "USER" : "ADMIN";
                    r.setRole(role);
                    System.out.println("Успешная авторизация: " + obj.getLogin());
                } else {
                    System.out.println("Неверный пароль для: " + obj.getLogin());
                }
            } else {
                System.out.println("Пользователь не найден: " + obj.getLogin());
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при авторизации: " + e.getMessage());
            e.printStackTrace();
        }
        return r;
    }
}