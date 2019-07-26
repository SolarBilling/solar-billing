package com.sapienter.jbilling.server.payment;

import java.util.Set;

import com.sapienter.jbilling.common.BillingObject;
import com.sapienter.jbilling.server.user.db.CompanyDTO;

public interface PaymentMethod extends BillingObject {
	Set<CompanyDTO> getEntities();
}
