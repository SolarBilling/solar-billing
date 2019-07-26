package com.sapienter.jbilling.server.user.permisson.db;

import java.util.Set;

import com.sapienter.jbilling.common.BillingObject;

public interface Permission extends BillingObject {
	PermissionType getPermissionType();
	Integer getForeignId();
	Set<PermissionUserDTO> getPermissionUsers();
}
