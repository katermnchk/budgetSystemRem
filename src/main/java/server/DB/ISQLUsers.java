package server.DB;

import client.clientWork.Account;
import client.clientWork.Category;
import client.clientWork.Transaction;
import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ISQLUsers {
    ArrayList<Users> findUser(Users obj);
    Role insert(Users obj);
    boolean deleteUser(Users obj);
    ArrayList<Users> get();
    Users getUserById(Integer userId) throws SQLException;

    ArrayList<Account> getUserAccounts(Integer userId) throws SQLException;
    ArrayList<Category> getIncomeCategories() throws SQLException;
    ArrayList<Category> getExpenseCategories() throws SQLException;

    double getBalance(Integer userId) throws SQLException;
    HashMap<String, Double> getAccountBalances(Integer userId) throws SQLException;

    ArrayList<Transaction> getTransactionHistory(Integer userId) throws SQLException;
    void addCategory(Category category) throws SQLException;
    void deleteCategory(int categoryId) throws SQLException;
    ArrayList<Category> getAllCategories(Integer userId) throws SQLException;

    HashMap<String, Double> getExpenseChartData(Integer userId) throws SQLException;

    void addAccount(Account account, Integer userId) throws SQLException;

}
