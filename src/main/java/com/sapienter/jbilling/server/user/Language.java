package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.common.BillingObject;

public interface Language extends BillingObject {
	String getCode();
	String getDescription();
}
