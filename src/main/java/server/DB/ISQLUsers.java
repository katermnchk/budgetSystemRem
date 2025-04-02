package server.DB;

import client.clientWork.Users;
import server.SystemOrg.Role;

import java.util.ArrayList;

public interface ISQLUsers {
    ArrayList<Users> findUser(Users obj);
    Role insert(Users obj);
    boolean deleteUser(Users obj);
    ArrayList<Users> get();
    Users geUser(Role r);
}
