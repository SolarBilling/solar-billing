package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.common.BillingObject;

public interface UserStatus extends BillingObject {
	String getDescription(int languageId);
}
