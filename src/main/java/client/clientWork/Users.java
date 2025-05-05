package client.clientWork;

import java.io.Serializable;
import java.util.Objects;

public class Users implements Serializable {
    private int id;
    private String firstname;
    private String lastname;
    private String login;
    private String password;
    private String role;
    private String status;

    public Users(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Users() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        return (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Users)) return false;
        Users that = (Users) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(password, that.password) &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(role, that.role) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, firstname, lastname, role, status);
    }

    @Override
    public String toString() {
        return getFullName().trim().isEmpty() ? login : getFullName();
    }
}