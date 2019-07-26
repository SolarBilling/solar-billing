package com.sapienter.jbilling.server.user.permisson.db;

import com.google.common.base.Supplier;
import com.sapienter.jbilling.common.BillingObject;

import java.util.Set;

public interface PermissionType extends Supplier<Set<PermissionDTO>>, BillingObject {
	Set<PermissionDTO> getPermissions();
}
