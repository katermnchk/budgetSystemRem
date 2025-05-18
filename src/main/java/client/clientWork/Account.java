package client.clientWork;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Account implements Serializable {
    @Getter
    @Setter
    private int id;
    @Setter
    private int userId;
    @Getter
    @Setter
    private String name;
    @Setter
    @Getter
    private double balance;
    private boolean blockedValue;

    public Account(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public Account(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public Account() {

    }

    public boolean isBlocked() {
        return blockedValue;
    }
    public void setBlocked(boolean blockedValue) {
        this.blockedValue = blockedValue;
    }


}