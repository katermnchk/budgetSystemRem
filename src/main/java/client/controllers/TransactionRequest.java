package client.controllers;

public class TransactionRequest implements java.io.Serializable {
    private int userId;
    private int accountId;
    private int categoryId;
    private double amount;
    private String description;

    public TransactionRequest(int userId, int accountId, int categoryId, double amount, String description) {
        this.userId = userId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
    }

    public int getUserId() { return userId; }
    public int getAccountId() { return accountId; }
    public int getCategoryId() { return categoryId; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
}
