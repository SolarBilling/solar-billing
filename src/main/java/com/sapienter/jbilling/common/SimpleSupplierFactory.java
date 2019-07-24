package com.sapienter.jbilling.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

public class SimpleSupplierFactory implements SupplierFactory {
	private final Map<Class<?>, Function<Integer,?>> functionMap = new HashMap<>();
	static private SimpleSupplierFactory instance = new SimpleSupplierFactory();
	
	protected SimpleSupplierFactory() {}
	
	static public SimpleSupplierFactory getInstance()
	{
		return instance;
	}
	
	public <T> void putSupplier(final Class<T> theClass, final Function<Integer,T> function)
	{
		functionMap.put(Objects.requireNonNull(theClass), Objects.requireNonNull(function));
	}
	
	public <U> BiFunction<Integer, Class<U>, Map.Entry<Integer, Supplier<U>>> getSupplierFactory(final Class<U> aClass)
	{
		return new BiFunction<Integer, Class<U>, Map.Entry<Integer, Supplier<U>>>() {
			@Override
			public Map.Entry<Integer, Supplier<U>> apply(Integer id, Class<U> theClass) {
				return getSupplier(id, theClass);
			}};
//		return (id, theClass) -> getSupplier(id, theClass);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map.Entry<Integer,Supplier<T>>  getSupplier(final int id, final Class<T> theClass) {
		final Function<Integer,T> function = (Function<Integer,T>)functionMap.get(theClass);
		if (function == null) {
			throw new IllegalArgumentException("no function registered for class " + theClass.getName());
		}
		//return () -> function.apply(id); // TODO wrap this in a CachingSupplier
		final Supplier<T> supplier = new Supplier<T>() {
			@Override
			public T get() {
				return function.apply(id);
			}};
		return ImmutableMap.of(id, supplier).entrySet().iterator().next();
	}
}
