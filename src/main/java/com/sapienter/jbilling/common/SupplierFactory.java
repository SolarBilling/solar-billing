package com.sapienter.jbilling.common;

import java.util.Map;
import java.util.function.Supplier;

public interface SupplierFactory {
	<T> Map.Entry<Integer,Supplier<T>> getSupplier(int id, Class<T> theClass);
}
