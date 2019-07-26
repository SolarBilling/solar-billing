package com.sapienter.jbilling.server.invoice;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.process.BillingProcess;

public interface Invoice extends BillingObject {
	BillingProcess getBillingProcess();
}
