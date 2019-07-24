package com.sapienter.jbilling.common;

import java.util.Map;

import com.google.common.base.Supplier;

public interface KeyedSupplier<K,V> extends Supplier<V>, Map.Entry<K,Supplier<V>> {

}
