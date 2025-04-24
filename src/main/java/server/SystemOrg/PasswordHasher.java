package server.SystemOrg;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static void main(String[] args) {
        String plainPassword = "adminpass";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        System.out.println("Хешированный пароль для 'adminpass': " + hashedPassword);
    }
}