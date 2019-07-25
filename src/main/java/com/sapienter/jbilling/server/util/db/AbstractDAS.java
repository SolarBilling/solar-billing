/*
    jBilling - The Enterprise Open Source Billing System
    Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde

    This file is part of jbilling.

    jbilling is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jbilling is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.sapienter.jbilling.server.util.db;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.query.Query;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sapienter.jbilling.server.util.Context;
import org.hibernate.LockMode;
import org.springframework.orm.hibernate5.HibernateTemplate;


public abstract class AbstractDAS<T> implements Function<Integer,T>, Supplier<List<T>>, Consumer<T> {
    private final Logger LOG = Logger.getLogger(getClass());

    final private Class<T> persistentClass;
    // if querys will be run cached or not
    private boolean queriesCached = false;
    private Session session;
    private HibernateTemplate hibernateTemplate;
    private SessionFactory sessionFactory;

    private HibernateTemplate getHibernateTemplate() {
    	if (hibernateTemplate == null) {
    		hibernateTemplate = new HibernateTemplate(getSessionFactory());
    	}
    	return hibernateTemplate;
    }
    
    private SessionFactory getSessionFactory() {
    	if (sessionFactory == null) {
    		sessionFactory = (SessionFactory) Context.getBean(Context.Name.HIBERNATE_SESSION);
    		/*
        final Properties properties = new Properties();
        ImmutableMap<String,?> props = ImmutableMap.of(
            	"hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
            	"SecondLevelCacheEnabled", false); 
        properties.putAll(props);
        sessionFactory = new LocalSessionFactoryBuilder(null).addProperties(properties).buildSessionFactory();
        */
    	}
    	return sessionFactory;
    }

    
    protected Session getSession()
    {
        if ((session == null) || !session.isOpen())
        {
            session = getSessionFactory().openSession(); // getCurrentSession();
        }
        return session;
    }
    
    @SuppressWarnings("unchecked")
	public AbstractDAS() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

	@Override
	public T apply(final Integer id) {
//		return uniqueResult(persistentClass, ImmutableMap.of(getIdFieldName(), id));
//		return find(id); // TODO would findNow be better ?
		return findNow(id);
	}

	// child classes can override this method if the primary key is called something else
	protected String getIdFieldName() { return "id"; }
	
    protected <K,V> ImmutableMap<K,V> immutableMapOf( K k1, V v1, K k2, V v2)
    {
    	return immutableMapOf(k1, v1, k2, v2, null, null);
    }
    
    protected <K,V> ImmutableMap<K,V> immutableMapOf( K k1, V v1, K k2, V v2, K k3, V v3)
    {
    	return immutableMapOf(k1, v1, k2, v2, k3, v3, null, null, null, null);
    }
    
    protected <K,V> ImmutableMap<K,V> immutableMapOf( K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4)
    {
    	return immutableMapOf(k1, v1, k2, v2, k3, v3, k4, v4, null, null);
    }
    
    // the same as ImmutableMap.of, except that we discard the key,value pair if either the key or the value is null
    protected <K,V> ImmutableMap<K,V> immutableMapOf( K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5)
    {
    	ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
    	if ((k1 != null) && (v1 != null)) {
    		builder.put(k1, v1);
    	}
    	if ((k2 != null) && (v2 != null)) {
    		builder.put(k2, v2);
    	}
    	if ((k3 != null) && (v3 != null)) {
    		builder.put(k3, v3);
    	}
    	if ((k4 != null) && (v4 != null)) {
    		builder.put(k4, v4);
    	}
    	if ((k5 != null) && (v5 != null)) {
    		builder.put(k5, v5);
    	}
    	return builder.build();
    }

    // this seems to be necessary because otherwise Java seems to limit the number of levels of nested exceptions included in a stack trace
    private void logAllCauses(Throwable throwable) {
    	LoggerFactory.getLogger(getClass()).error("", throwable);
    	LOG.error("", throwable);
		final Throwable cause = throwable.getCause();
		if ((cause != null) && (cause != throwable)) {
			logAllCauses(cause);
		} else {
			throw new RuntimeException(throwable); // TODO this shouldn't be necessary but the logging doesn't seem to be working
		}
    }
    
	@SuppressWarnings("hiding")
	protected <T> List<T> selectAll(final Class<T> theClass, final ImmutableMap<String, ?> values) {
		try {
			return createQuery(theClass, values).list();
		} catch (final RuntimeException rte) {
			logAllCauses(rte);
			throw rte;
		}
	}

	@SuppressWarnings("hiding")
	@Override public List<T> get() {
		try {
			return createQuery(persistentClass).list();
		} catch (final RuntimeException rte) {
			logAllCauses(rte);
			throw rte;
		}
	}

	@SuppressWarnings("hiding")
	protected <T> T uniqueResult(final Class<T> theClass, final ImmutableMap<String, ?> values) {
		return createQuery(theClass, values).uniqueResult();
	}

	@SuppressWarnings("hiding")
    protected <T> List<T> selectHQL(final String hql, Class<T> theClass, final ImmutableMap<String, ?> parameters, final int maxResults) {
    	return createHQLQuery(hql, theClass, parameters, maxResults).list();
    }

    protected int executeUpdate(final String hql, final ImmutableMap<String, ?> parameters) {
    	return createHQLQuery(hql, Object.class, parameters).executeUpdate();
    }
    
	@SuppressWarnings("hiding")
    protected <T> T uniqueResultHQL(final String hql, Class<T> theClass, final ImmutableMap<String, ?> parameters) {
    	return createHQLQuery(hql, theClass, parameters).uniqueResult();
    }
	
	@SuppressWarnings("hiding")
    protected <T> List<T> selectHQL(final String hql, Class<T> theClass, final ImmutableMap<String, ?> parameters) {
    	return createHQLQuery(hql, theClass, parameters).list();
    }
	
	@SuppressWarnings("hiding")
    protected <T> Query<T> createHQLQuery(final String hql, Class<T> theClass, final ImmutableMap<String, ?> parameters) {
    	return createHQLQuery(hql, theClass, parameters, -1);
    }
    
	@SuppressWarnings("hiding")
    protected <T> Query<T> createHQLQuery(final String hql, Class<T> theClass, final ImmutableMap<String, ?> parameters, final int maxResults) {
    	final Query<T> query = getSession().createQuery(hql, theClass);
    	for (Map.Entry<String, ?> parameter : parameters.entrySet()) {
    		query.setParameter( parameter.getKey(), parameter.getValue());
    	}
    	if (maxResults > 0)
    	{
    		query.setMaxResults(maxResults);
    	}
    	return query;
    	
    }
    
	@SuppressWarnings("hiding")
	protected <T> Query<T> createQuery(final Class<T> theClass, final ImmutableMap<String, ?> values) {
		return createQuery(theClass, values, null, false);
	}
	
	@SuppressWarnings("hiding")
	protected <T> Query<T> createQuery(final Class<T> theClass) {
		return createQuery(theClass, null, null, false);
	}
	
	@SuppressWarnings("hiding")
	protected <T> Query<T> createQuery(final Class<T> theClass, final ImmutableMap<String, ?> values, String orderValue, boolean ascending) {
		final Session session = getSession();
		final CriteriaBuilder criteriaBuilder = getSession().getCriteriaBuilder(); // this compiles with Hibernate 	5.4.2 but not 5.2.18
		final CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(theClass);
		final List<Predicate> predicates = new ArrayList<>();
		final Root<T> root = criteriaQuery.from(theClass);
		if (values !=  null) { // we can't supply an empty ImmutableMap because type inference fails, so have to supply null instead
			for (Map.Entry<String, ?> mapEntry : values.entrySet()) {
				predicates.add(criteriaBuilder.equal(root.get(mapEntry.getKey()), mapEntry.getValue()));
			}
		}
		if (orderValue != null) {
			final Expression<?> expression = root.get(orderValue);
			final Order order;
			if (ascending) {
				order = criteriaBuilder.asc(expression);
			} else {
				order = criteriaBuilder.desc(expression);
			}
			criteriaQuery.orderBy(order);
		}
		criteriaQuery.select(root).where(predicates.toArray(new Predicate[]{}));
		return session.createQuery(criteriaQuery);
	}

	public void accept(final T entity) {
		save(entity);
	}
	
   /**
     * Merges the entity, creating or updating as necessary
     * @param newEntity
     * @return
     */
    public T save(final T newEntity) {
        @SuppressWarnings("unchecked")
		final T retValue = (T) getSession().merge(newEntity);
        return retValue;
    }
    
    public void delete(T entity) {
        //em.remove(entity);
        getHibernateTemplate().delete(entity);
    }

    public void refresh(T entity) {
    	getHibernateTemplate().refresh(entity);
    }
    
    protected void initializeProxy(Object proxy) {
    	getSession();
    	getHibernateTemplate().initialize(proxy);
    }
    
    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    /**
     * This will load a proxy. If the row does not exist, it still returns an
     * object (not null) and  it will NOT throw an
     * exception (until the other fields are accessed).
     * Use this by default, if the row is missing, it is an error.
     * @param id
     * @return
     */
    public T find(Serializable id) {
    	if (id == null) return null;
        T entity = (T) getHibernateTemplate().load(getPersistentClass(), id);

        return entity;
    }
    
    /**
     * This will hit the DB. If the row does not exist, it will NOT throw an
     * exception but it WILL return NULL
     * @param id
     * @return
     */
    public T findNow(Serializable id) {
    	if (id == null) return null;
        T entity = (T) getHibernateTemplate().get(getPersistentClass(), id);

        return entity;
    }

    /**
     * This will lock the row for the duration of this transaction. Or wait until the row is
     * unlocked if it is already locked. It genererates a select ... for update
     * @param id
     * @return
     */
    public T findForUpdate(Serializable id) {
        if (id == null) {
            return null;
        }
        T entity = (T) getHibernateTemplate().get(getPersistentClass(), id, LockMode.PESSIMISTIC_WRITE);

        return entity;
    }

    public List<T> findAll() {
        return findByCriteria();
    }

    protected Criteria createCriteria() {
    	return getSession().createCriteria(getPersistentClass());
    }
    
    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        return findCriteriaByExample(exampleInstance, excludeProperty).list();    	
    }
    
    protected Criteria findCriteriaByExample(T exampleInstance, String... excludeProperty) {
        final Criteria crit = createCriteria();
        final Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        crit.setCacheable(queriesCached);
        return crit;
    }

    @SuppressWarnings("unchecked")
    public T findByExampleSingle(T exampleInstance, String... excludeProperty) {
    	return (T)findCriteriaByExample(exampleInstance, excludeProperty).uniqueResult();
    }
    
    public T makePersistent(T entity) {
        getHibernateTemplate().saveOrUpdate(entity);
        return entity;
    }

    public void makeTransient(T entity) {
        getHibernateTemplate().delete(entity);
    }

    public void flush() {
        getHibernateTemplate().flush();
    }

    public void clear() {
        getHibernateTemplate().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        return createCriteria(criterion).list();
    }

    protected Criteria createCriteria(final Criterion... criterion) {
        final Criteria crit = createCriteria();
        for (Criterion c : criterion) {
            crit.add(c);
        }
        crit.setCacheable(queriesCached);
        return crit;
    }
    
    @SuppressWarnings("unchecked")
    protected T findByCriteriaSingle(Criterion... criterion) {
        return (T) createCriteria(criterion).uniqueResult();
   }

    protected void useCache() {
        queriesCached = true;
    }
    
    /**
     * Makes this DTO now attached to the session and part of the persistent context.
     * This WILL trigger an update, which is usually fine since the reason to reattach
     * is to modify the object.
     * @param dto
     */
    public void reattach(T dto) {
    	getSession().update(dto);
    }

    /**
     * Places the DTO in the session without updates or version checks.
     * You have to make sure that the DTO has not been modified to use this
     * @param dto
     */
    public void reattachUnmodified(T dto) {
    	getSession().lock(dto, LockMode.NONE);
    }

    /**
     * Detaches the DTO from the session. Updates to the object will
     * no longer make it to the database.
     */
    public void detach(T dto) {
        getSession().flush(); // without this, get ready for the evil 'nonthreadsafe access to session'
        getSession().evict(dto);
    }    
}
