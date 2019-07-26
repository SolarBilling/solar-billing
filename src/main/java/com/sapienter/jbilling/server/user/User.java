package com.sapienter.jbilling.server.user;

import java.util.Date;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.user.db.SubscriberStatusDTO;
import com.sapienter.jbilling.server.util.Currency;

public interface User extends BillingObject {
	Currency getCurrency();
	Company getCompany();
	SubscriberStatusDTO getSubscriberStatus();
	Language getLanguage();
	Date getLastLogin();
	String getUserName();
	Date getLastStatusChange();
	Customer getCustomer();
}
