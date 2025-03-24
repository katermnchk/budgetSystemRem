package server.DB;

import client.controllers.Authorization;
import server.SystemOrg.Role;

public interface ISQLAuthorization {
    Role getRole(Authorization obj);
}
