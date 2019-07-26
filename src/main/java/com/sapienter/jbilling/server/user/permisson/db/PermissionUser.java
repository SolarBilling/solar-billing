package com.sapienter.jbilling.server.user.permisson.db;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.user.db.UserDTO;

public interface PermissionUser extends BillingObject {
	UserDTO getBaseUser();
	short getIsGrant();
	Permission getPermission();
}
