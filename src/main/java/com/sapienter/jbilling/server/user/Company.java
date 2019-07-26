package com.sapienter.jbilling.server.user;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.util.Currency;

public interface Company extends BillingObject {
	Currency getCurrency();
}
