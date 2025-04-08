package client.clientWork;

import java.io.Serializable;

public class Account implements Serializable {
    private int id;
    private String name;

    public Account(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}