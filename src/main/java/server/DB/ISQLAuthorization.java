package server.DB;

import models.Authorization;
import server.SystemOrg.Role;

public interface ISQLAuthorization {
    Role getRole(Authorization obj);
}
