package client.clientWork;

public class Users {
    String name;
    String surname;
    String login;
    String password;

    public void setName(String name) {
        this.name = name;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }

    public void setLogin(String text) {
        this.login = text;
    }
    public String getLogin() {
        return login;
    }

    public void setPassword(String text) {
        this.password = text;
    }
    public String getPassword() {
        return password;
    }
}
