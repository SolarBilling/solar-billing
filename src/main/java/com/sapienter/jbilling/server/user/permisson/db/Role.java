package com.sapienter.jbilling.server.user.permisson.db;

import java.util.Set;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.user.db.UserDTO;

public interface Role extends BillingObject {
	String getTitle(int languageId);
	Set<UserDTO> getBaseUsers();
	Set<PermissionDTO> getPermissions();
}
