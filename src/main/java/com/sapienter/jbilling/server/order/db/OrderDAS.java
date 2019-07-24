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
package com.sapienter.jbilling.server.order.db;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.ImmutableMap;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class OrderDAS extends AbstractDAS<OrderDTO> {
	public OrderProcessDTO findProcessByEndDate(Integer id, Date myDate) {
		return (OrderProcessDTO) getSession().createFilter(find(id).getOrderProcesses(), 
				"where this.periodEnd = :endDate").setDate("endDate", 
						Util.truncateDate(myDate)).uniqueResult();
		
	}

	/**
	 * Finds active recurring orders for a given user
	 * @param userId
	 * @return
	 */
	public List<OrderDTO> findByUserSubscriptions(Integer userId) {
		return selectAll(OrderDTO.class, ImmutableMap.of(
				"orderStatus.id", Constants.ORDER_STATUS_ACTIVE,
				"deleted", 0,
				"baseUserByUserId.id", userId,
				"orderPeriod.id", Constants.ORDER_PERIOD_ONCE
				));
	}
	
	public List<OrderDTO> findByUser_Status(Integer userId,Integer statusId) {
		// I need to access an association, so I can't use the parent helper class
		return selectAll(OrderDTO.class, ImmutableMap.of(
				"orderStatus.id", statusId,
				"deleted", 0,
				"baseUserByUserId.id", userId
				));
	}

    // used for the web services call to get the latest X orders
    public List<Integer> findIdsByUserLatestFirst(Integer userId, int maxResults) {
        Criteria criteria = getSession().createCriteria(OrderDTO.class)
                .add(Restrictions.eq("deleted", 0))
                .createAlias("baseUserByUserId", "u")
                    .add(Restrictions.eq("u.id", userId))
                .setProjection(Projections.id())
                .addOrder(Order.desc("id"))
                .setMaxResults(maxResults)
                .setComment("findIdsByUserLatestFirst " + userId + " " + maxResults);
        return criteria.list();
    }

    // used for the web services call to get the latest X orders that contain an item of a type id
    @SuppressWarnings("unchecked")
    public List<Integer> findIdsByUserAndItemTypeLatestFirst(Integer userId, Integer itemTypeId, int maxResults) {
        // I'm a HQL guy, not Criteria
        String hql = 
            "select distinct(orderObj.id)" +
            " from OrderDTO orderObj" +
            " inner join orderObj.lines line" +
            " inner join line.item.itemTypes itemType" +
            " where itemType.id = :typeId" +
            "   and orderObj.baseUserByUserId.id = :userId" +
            "   and orderObj.deleted = 0" +
            " order by orderObj.id desc";
        List<Integer> data = getSession()
                                .createQuery(hql)
                                .setParameter("userId", userId)
                                .setParameter("typeId", itemTypeId)
                                .setMaxResults(maxResults)
                                .list();
        return data;
    }

	/**
	 * @author othman
	 * @return list of active orders
	 */
	public List<OrderDTO> findToActivateOrders() {
		Date today = Util.truncateDate(new Date());
		Criteria criteria = getSession().createCriteria(OrderDTO.class);

		criteria.add(Restrictions.eq("deleted", 0));
		criteria.add(Restrictions.or(Restrictions.le("activeSince", today),
				Restrictions.isNull("activeSince")));
		criteria.add(Restrictions.or(Restrictions.gt("activeUntil", today),
				Restrictions.isNull("activeUntil")));

		return criteria.list();
	}

	/**
	 * @author othman
	 * @return list of inactive orders
	 */
	public List<OrderDTO> findToDeActiveOrders() {
		Date today = Util.truncateDate(new Date());
		Criteria criteria = getSession().createCriteria(OrderDTO.class);

		criteria.add(Restrictions.eq("deleted", 0));
		criteria.add(Restrictions.or(Restrictions.gt("activeSince", today),
				Restrictions.le("activeUntil", today)));

		return criteria.list();
	}
	
	public BigDecimal findIsUserSubscribedTo(final Integer userId, final Integer itemId) {
		final String hql = 
				"select sum(l.quantity) " +
				"from OrderDTO o " +
				"inner join o.lines l " +
				"where l.item.id = :itemId and " +
				"o.baseUserByUserId.id = :userId and " +
				"o.orderPeriod.id != :periodVal and " +
				"o.orderStatus.id = :status and " +
				"o.deleted = 0 and " +
				"l.deleted = 0";

        final BigDecimal result = uniqueResultHQL(hql, BigDecimal.class, ImmutableMap.of(
                "userId", userId,
                "itemId", itemId,
                "periodVal", Constants.ORDER_PERIOD_ONCE,
                "status", Constants.ORDER_STATUS_ACTIVE));
        return (result == null ? BigDecimal.ZERO : result);
	}
	
	public Integer[] findUserItemsByCategory(Integer userId, 
			Integer categoryId) {
		
		Integer[] result = null;
		
        final String hql =
                "select distinct(i.id) " +
                "from OrderDTO o " +
                "inner join o.lines l " +
                "inner join l.item i " +
                "inner join i.itemTypes t " +
                "where t.id = :catId and " +
                "o.baseUserByUserId.id = :userId and " +
                "o.orderPeriod.id != :periodVal and " +
                "o.deleted = 0 and " +
                "l.deleted = 0";
        final List<Integer> qRes = selectHQL(hql, Integer.class, ImmutableMap.of(
                "userId", userId,
                "catId", categoryId,
                "periodVal", Constants.ORDER_PERIOD_ONCE
        		));
        if (qRes != null && qRes.size() > 0) {
            result = qRes.toArray(new Integer[0]);
        }
        return result;
	}

    public List<OrderDTO> findOneTimersByDate(final Integer userId, final Date activeSince) {
        final String hql = 
        "select o " +
        "  from OrderDTO o " +
        " where o.baseUserByUserId.id = :userId " +
        "   and o.orderPeriod.id = " + Constants.ORDER_PERIOD_ONCE +
        "   and activeSince = :activeSince " + // TODO shouldn't this be greater than or equal to ?
        "   and deleted = 0";

        final List<OrderDTO> result = selectHQL( hql, OrderDTO.class, ImmutableMap.of(
        		"userId", userId,
        		"activeSince", activeSince
        	));  
        return result;
    }

    public OrderDTO findForUpdate(Integer id) {
        OrderDTO retValue = super.findForUpdate(id);
        // lock all the lines
        for (OrderLineDTO line : retValue.getLines()) {
            new OrderLineDAS().findForUpdate(line.getId());
        }
        return retValue;
    }
}
