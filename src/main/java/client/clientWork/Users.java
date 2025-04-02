package client.clientWork;

import java.io.Serializable;
import java.util.Objects;

public class Users implements Serializable {
    int id;
    String firstname;
    String lastname;
    String login;
    String password;

    public Users(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Users() {

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

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
    this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Users that = (Users) o;

        return  Objects.equals(this.login, that.login) &&
                Objects.equals(this.password, that.password) &&
                Objects.equals(this.firstname, that.firstname) &&
                Objects.equals(this.lastname, that.lastname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.login, this.password, this.firstname, this.lastname);
    }

    @Override
    public String toString() {
        return "Users{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }
}
