package client.clientWork;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Account implements Serializable {
    @Setter
    private int id;
    private String name;
    @Setter
    @Getter
    private double balance;

    public Account(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public Account(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public int getId() { return id; }
    public String getName() { return name; }

}