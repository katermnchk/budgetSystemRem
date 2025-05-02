package client.clientWork;

import lombok.Getter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
public class Transaction implements Serializable {
    private Timestamp date;
    private String accountName;
    private String categoryName;
    private double amount;
    private String description;

    public Transaction(Timestamp date, String accountName, String categoryName, double amount, String description) {
        this.date = date;
        this.accountName = accountName != null ? accountName : "";
        this.categoryName = categoryName != null ? categoryName : "";
        this.amount = amount;
        this.description = description != null ? description : "";
    }

}
