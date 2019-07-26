package com.sapienter.jbilling.server.util;

import com.sapienter.jbilling.common.BillingObject;

public interface Currency extends BillingObject {
	String getSymbol();
	String getCode();
	String getCountryCode();
}
