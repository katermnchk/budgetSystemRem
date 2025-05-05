package server.DB;

import client.clientWork.Account;
import client.clientWork.Users;
import server.SystemOrg.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SQLChildren {
    private static final Logger LOGGER = Logger.getLogger(SQLUsers.class.getName());

    private Connection conn;
    private static SQLUsers instance;


    public SQLChildren(Connection conn) {
        this.conn = conn;
    }

    public ArrayList<Users> getChildren(int parentId) throws SQLException {
        ArrayList<Users> children = new ArrayList<>();
        String query = "SELECT id, firstname, lastname, username, password, role_id FROM users WHERE " +
                "parent_id = ? AND role_id = 2";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, parentId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Users child = new Users();
            child.setId(rs.getInt("id"));
            child.setFirstname(rs.getString("firstname"));
            child.setLastname(rs.getString("lastname"));
            String username = rs.getString("username");
            child.setLogin(username != null ? username : "");
            child.setPassword(rs.getString("password"));
            child.setRole("CHILD"); // role_id = 2 соответствует CHILD
            children.add(child);
            LOGGER.info("Загружен ребенок: id=" + child.getId() + ", name=" + child.getFullName() + ", username=" + child.getLogin());
        }
        return children;
    }

    public ArrayList<Account> getChildAccounts(int childId, int parentId) throws SQLException {
        ArrayList<Account> accounts = new ArrayList<>();
        String query = "SELECT a.id, a.name, a.balance, a.is_blocked FROM accounts a JOIN users u " +
                "ON a.user_id = u.id WHERE a.user_id = ? AND u.parent_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, childId);
        stmt.setInt(2, parentId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Account account = new Account(rs.getInt("id"), rs.getString("name"), rs.getDouble("balance"));
            account.setBlocked(rs.getBoolean("is_blocked"));
            accounts.add(account);
            LOGGER.info("Загружен счет: id=" + account.getId() + ", name=" + account.getName() +
                    ", balance=" + account.getBalance());
        }
        return accounts;
    }

    public boolean toggleBlockAccount(int accountId, int parentId) throws SQLException {
        String query = "UPDATE accounts SET is_blocked = NOT is_blocked WHERE id = ? AND user_id IN " +
                "(SELECT id FROM users WHERE parent_id = ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, accountId);
        stmt.setInt(2, parentId);
        int rows = stmt.executeUpdate();
        if (rows == 0) {
            throw new SQLException("Счет не найден или не принадлежит ребенку пользователя");
        }
        return true;
    }

    public Role addChild(Users child, int parentId) throws SQLException {
        Role role = new Role();
        String checkQuery = "SELECT id FROM users WHERE username = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setString(1, child.getLogin());
        ResultSet checkRs = checkStmt.executeQuery();
        if (checkRs.next()) {
            return role;
        }

        String parentCheckQuery = "SELECT id FROM users WHERE id = ? AND role_id = 1";
        PreparedStatement parentCheckStmt = conn.prepareStatement(parentCheckQuery);
        parentCheckStmt.setInt(1, parentId);
        ResultSet parentCheckRs = parentCheckStmt.executeQuery();
        if (!parentCheckRs.next()) {
            throw new SQLException("Родитель не найден или не является пользователем с ролью USER");
        }

        String query = "INSERT INTO users (firstname, lastname, username, password, role_id, parent_id) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, child.getFirstname());
        stmt.setString(2, child.getLastname());
        stmt.setString(3, child.getLogin());
        stmt.setString(4, child.getPassword());
        stmt.setInt(5, 2);
        // stmt.setString(6, child.getStatus());
        stmt.setInt(6, parentId);
        int rows = stmt.executeUpdate();
        if (rows > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                role.setId(rs.getInt(1));
                role.setRole("CHILD");
            }
        }
        return role;
    }

    public String topUpAccount(int accountId, double amount, int parentId) {
        if (amount <= 0) {
            LOGGER.severe("Недопустимая сумма пополнения: " + amount);
            return "Ошибка: сумма должна быть положительной";
        }
        String checkQuery = "SELECT accounts.id, accounts.is_blocked FROM accounts JOIN users ON accounts.user_id = users.id " +
                "WHERE accounts.id = ? AND users.parent_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, accountId);
            checkStmt.setInt(2, parentId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    LOGGER.severe("Счет с id=" + accountId + " не существует или не принадлежит ребёнку родителя с id=" + parentId);
                    return "Ошибка: счёт не существует или не принадлежит вашему ребёнку";
                }
                if (rs.getBoolean("is_blocked")) {
                    LOGGER.severe("Счет с id=" + accountId + " заблокирован");
                    return "Ошибка: нельзя пополнить заблокированный счет";
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка проверки счёта: " + e.getMessage());
            return "Ошибка проверки счёта: " + e.getMessage();
        }
        String query = "UPDATE accounts SET balance = balance + ? WHERE accounts.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, accountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Счет успешно пополнен: accountId=" + accountId + ", amount=" + amount);
                return "OK";
            } else {
                LOGGER.warning("Счет с id=" + accountId + " не найден");
                return "Ошибка: счёт не найден";
            }
        } catch (SQLException e) {
            LOGGER.severe("Ошибка при пополнении счета: " + e.getMessage());
            return "Ошибка при пополнении счета: " + e.getMessage();
        }
    }

    public String topUpChildAccount(int childAccountId, int parentAccountId, double amount, int parentId) {
        if (amount <= 0) {
            LOGGER.severe("Недопустимая сумма пополнения: " + amount);
            return "Ошибка: сумма должна быть положительной";
        }
        try {
            conn.setAutoCommit(false);
            String checkParentQuery = "SELECT id, balance, is_blocked FROM accounts WHERE id = ? AND user_id = ?";
            try (PreparedStatement checkParentStmt = conn.prepareStatement(checkParentQuery)) {
                checkParentStmt.setInt(1, parentAccountId);
                checkParentStmt.setInt(2, parentId);
                try (ResultSet rs = checkParentStmt.executeQuery()) {
                    if (!rs.next()) {
                        LOGGER.severe("Счет родителя с id=" + parentAccountId +
                                " не существует или не принадлежит родителю с id=" + parentId);
                        return "Ошибка: счёт родителя не существует или не принадлежит вам";
                    }
                    if (rs.getBoolean("is_blocked")) {
                        LOGGER.severe("Счет родителя с id=" + parentAccountId + " заблокирован");
                        return "Ошибка: нельзя использовать заблокированный счёт родителя";
                    }
                    double parentBalance = rs.getDouble("balance");
                    if (parentBalance < amount) {
                        LOGGER.severe("Недостаточно средств на счете родителя: id=" + parentAccountId +
                                ", balance=" + parentBalance + ", amount=" + amount);
                        return "Ошибка: недостаточно средств на вашем счёте";
                    }
                }
            }
            String checkChildQuery = "SELECT accounts.id, accounts.is_blocked FROM accounts JOIN users " +
                    "ON accounts.user_id = users.id WHERE accounts.id = ? AND users.parent_id = ?";
            try (PreparedStatement checkChildStmt = conn.prepareStatement(checkChildQuery)) {
                checkChildStmt.setInt(1, childAccountId);
                checkChildStmt.setInt(2, parentId);
                try (ResultSet rs = checkChildStmt.executeQuery()) {
                    if (!rs.next()) {
                        LOGGER.severe("Счет ребёнка с id=" + childAccountId +
                                " не существует или не принадлежит ребёнку родителя с id=" + parentId);
                        return "Ошибка: счёт ребёнка не существует или не принадлежит вашему ребёнку";
                    }
                    if (rs.getBoolean("is_blocked")) {
                        LOGGER.severe("Счет ребёнка с id=" + childAccountId + " заблокирован");
                        return "Ошибка: нельзя пополнить заблокированный счёт ребёнка";
                    }
                }
            }
            String debitParentQuery = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement debitStmt = conn.prepareStatement(debitParentQuery)) {
                debitStmt.setDouble(1, amount);
                debitStmt.setInt(2, parentAccountId);
                int rowsAffected = debitStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Не удалось списать средства со счёта родителя: id=" + parentAccountId);
                }
            }
            String creditChildQuery = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement creditStmt = conn.prepareStatement(creditChildQuery)) {
                creditStmt.setDouble(1, amount);
                creditStmt.setInt(2, childAccountId);
                int rowsAffected = creditStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Не удалось пополнить счёт ребёнка: id=" + childAccountId);
                }
            }
            conn.commit();
            LOGGER.info("Транзакция успешна: списано " + amount + " со счёта родителя id=" + parentAccountId +
                    ", пополнен счёт ребёнка id=" + childAccountId);
            return "OK";
        } catch (SQLException e) {
            try {
                conn.rollback();
                LOGGER.severe("Транзакция откатана из-за ошибки: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                LOGGER.severe("Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
            LOGGER.severe("Ошибка при выполнении транзакции: " + e.getMessage());
            return "Ошибка при выполнении транзакции: " + e.getMessage();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.severe("Ошибка при восстановлении автофиксации: " + e.getMessage());
            }
        }
    }


    public ArrayList<Account> getParentAccounts(int parentId) throws SQLException {
        ArrayList<Account> accounts = new ArrayList<>();
        String query = "SELECT id, user_id, name, balance, is_blocked FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, parentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account();
                    account.setId(rs.getInt("id"));
                    account.setName(rs.getString("name"));
                    account.setBalance(rs.getDouble("balance"));
                    account.setBlocked(rs.getBoolean("is_blocked"));
                    accounts.add(account);
                    LOGGER.info("Загружен счет родителя: id=" + account.getId() + ", name=" + account.getName() + ", balance=" + account.getBalance());
                }
            }
        }
        return accounts;
    }

}
