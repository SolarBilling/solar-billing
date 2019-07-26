package com.sapienter.jbilling.server.process;

import java.util.Date;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.user.Company;

public interface BillingProcess extends BillingObject {
	Date getBillingDate();
	Company getEntity();
}
