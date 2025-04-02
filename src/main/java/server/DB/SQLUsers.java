package server.DB;

import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.*;
import java.util.ArrayList;

public class SQLUsers implements ISQLUsers {
    private static SQLUsers instance;
    private DatabaseConnection dbConnection;

    public SQLUsers() throws SQLException, ClassNotFoundException {
        dbConnection = DatabaseConnection.getInstance();
    }

    public static synchronized SQLUsers getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLUsers();
        }
        return instance;
    }

    @Override
    public ArrayList<Users> findUser(Users obj) {
        String str = "select `keys`.login, firstname, lastname, averageMark, `groups`.numberGroup, payments.payment" +
                " from users" +
                " join `keys` on `keys`.`id_keys` = students.id_keys" +
                " left join `groups` on `groups`.idgroup = users.idgroup" +
                " left join payments on payments.idpayment = users.idpayment" +
                " where `keys`.login = \"" + obj.getLogin() + "\";";
        ArrayList<String[]> result = dbConnection.getArrayResult(str);
        ArrayList<Users> usList = new ArrayList<>();
        for (String[] items: result){
            Users students = new Users();
            students.setLogin(items[0]);
            students.setFirstname(items[1]);
            students.setLastname(items[2]);
            usList.add(students);
        }
        return usList;
    }

    @Override
    public Role insert(Users obj) {
        String proc = "{call insert_user(?,?,?,?,?)}";
        Role r = new Role();
        try (CallableStatement callableStatement = DatabaseConnection.dbConnection.prepareCall(proc)) {
            callableStatement.setString(1, obj.getFirstname());
            callableStatement.setString(2, obj.getLastname());
            callableStatement.setString(3, obj.getLogin());
            callableStatement.setString(4, obj.getPassword());
            callableStatement.registerOutParameter(5, Types.INTEGER);
            callableStatement.execute();
            r.setId(callableStatement.getInt(5));
            r.setRole("user");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("ошибка");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public boolean deleteUser(Users obj) {
        return false;
    }

    public ArrayList<Users> get() {
        ArrayList<Users> usersList = new ArrayList<>();
        try {
            String query = "SELECT * FROM users";
            PreparedStatement stmt = dbConnection.getConnection().prepareStatement(query); // Используем dbConnection
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Users user = new Users();
                user.setId(rs.getInt("id"));
                user.setFirstname(rs.getString("firstname"));
                user.setLastname(rs.getString("lastname"));
                user.setLogin(rs.getString("username"));
                usersList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при запросе к базе данных: " + e.getMessage());
        }
        return usersList;
    }

    @Override
    public Users geUser(Role r) {
        return null;
    }
}
