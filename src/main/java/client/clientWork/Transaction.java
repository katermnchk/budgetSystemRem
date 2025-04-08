package client.clientWork;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction implements Serializable {
    private final String date;
    private final String accountName;
    private final String categoryName;
    private final double amount;
    private final String description;

    public Transaction(Date date, String accountName, String categoryName, double amount, String description) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.date = sdf.format(date);
        this.accountName = accountName;
        this.categoryName = categoryName;
        this.amount = amount;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}