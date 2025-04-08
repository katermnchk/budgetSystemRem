package server.DB;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ISQLUsers {
    ArrayList<Users> findUser(Users obj);
    Role insert(Users obj);
    boolean deleteUser(Users obj);
    ArrayList<Users> get();

    ArrayList<Account> getUserAccounts(Integer userId) throws SQLException;
    ArrayList<Category> getIncomeCategories() throws SQLException;
    ArrayList<Category> getExpenseCategories() throws SQLException;

    double getBalance(Integer userId) throws SQLException;
}
