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

package com.sapienter.jbilling.server.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Function;

import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.item.ItemDecimalsException;
import com.sapienter.jbilling.server.order.db.OrderDTO;
import com.sapienter.jbilling.server.order.db.OrderPeriodDTO;

/**
 *
 * This is the session facade for the orders in general. It is a stateless
 * bean that provides services not directly linked to a particular operation
 *
 * @author emilc
 **/
public interface IOrderSessionBean extends Function<Integer, OrderDTO> {
    
    void reviewNotifications(Date today);

    public OrderDTO getOrder(int orderId);

    OrderDTO getOrderEx(int orderId, int languageId); 

    OrderDTO setStatus(int orderId, int statusId, int executorId, int languageId);

    /**
     * This is a version used by the http api, should be
     * the same as the web service but without the 
     * security check
    public Integer create(OrderWS order, Integer entityId,
            String rootUser, boolean process) throws SessionInternalError;
     */

    void delete(Integer id, Integer executorId); 
 
    OrderPeriodDTO[] getPeriods(Integer entityId, Integer languageId); 

    OrderPeriodDTO getPeriod(Integer languageId, Integer id);

    void setPeriods(Integer languageId, OrderPeriodDTO[] periods);

    void addPeriod(Integer entityId, Integer languageId); 

    boolean deletePeriod(int periodId);

    OrderDTO getMainOrder(int userId);

    public OrderDTO addItem(Integer itemID, BigDecimal quantity, OrderDTO order,
            Integer languageId, Integer userId, Integer entityId) 
            throws SessionInternalError, ItemDecimalsException;
    
    public OrderDTO addItem(Integer itemID, Integer quantity, OrderDTO order,
            Integer languageId, Integer userId, Integer entityId) 
            throws SessionInternalError, ItemDecimalsException;

    public OrderDTO recalculate(OrderDTO modifiedOrder, Integer entityId) 
            throws ItemDecimalsException;

    Integer createUpdate(Integer entityId, Integer executorId, OrderDTO order, Integer languageId);

    Long getCountWithDecimals(Integer itemId);
}
