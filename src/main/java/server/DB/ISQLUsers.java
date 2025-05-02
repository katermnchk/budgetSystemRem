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

   // ArrayList<Transaction> getTransactionHistory(Integer userId) throws SQLException;

    /*@Override
    public ArrayList<Transaction> getTransactionHistory(Integer userId) throws SQLException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.date, a.name AS account_name, c.name AS category_name, t.amount, t.description " +
                "FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.id " +
                "JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getTimestamp("date"),
                        rs.getString("account_name"),
                        rs.getString("category_name"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                ));
            }
        }
        return transactions;
    }*/
    ArrayList<Transaction> getTransactionHistory(Integer userId, HashMap<String, Object> filters) throws SQLException;

    void addCategory(Category category) throws SQLException;
    void deleteCategory(int categoryId) throws SQLException;
    ArrayList<Category> getAllCategories(Integer userId) throws SQLException;

    HashMap<String, Double> getExpenseChartData(Integer userId) throws SQLException;

    void addAccount(Account account, Integer userId) throws SQLException;

    boolean editUser(Users user);

    Integer getUsersCount();

    Integer getTransactionsCount();

    HashMap<String, Object> getStatistics();

    boolean addGoal(int userId, int categoryId, String type, double targetAmount, String period);

    ArrayList<HashMap<String, Object>> getGoals(int userId);

    ArrayList<String> getRecommendations(int userId);

    boolean deleteAccount(int accountId, int userId) throws SQLException;

    boolean editAccount(int accountId, int userId, String newName) throws SQLException;

    HashMap<String, Object> getAccountInfo(int accountId, int userId) throws SQLException;
}
