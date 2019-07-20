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

/*
 * Created on Dec 24, 2003
 *
 * Copyright Sapienter Enterprise Software
 */
package com.sapienter.jbilling.server.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.sapienter.jbilling.server.invoice.InvoiceBL;
import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.db.ItemDAS;
import com.sapienter.jbilling.server.order.OrderBL;
import com.sapienter.jbilling.server.order.OrderLineWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.order.db.OrderLineDTO;
import com.sapienter.jbilling.server.payment.PaymentBL;
import com.sapienter.jbilling.server.payment.PaymentWS;
import com.sapienter.jbilling.server.user.ContactWS;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.user.contact.db.ContactTypeDAS;
import com.sapienter.jbilling.server.entity.CreditCardDTO;

/**
 * @author Emil
 */
public class WSMethodSecurityProxy extends WSMethodBaseSecurityProxy {

    private static final Logger LOG = Logger.getLogger(WSMethodSecurityProxy.class);    
    private static final ArrayList<Method> methods = new ArrayList<Method>();
    private static final Class<IWebServicesSessionBean> target = IWebServicesSessionBean.class;

    static {
    	final Class<?>[] singleInteger = new Class[] {Integer.class}; 
    	// getInvoiceWS
       addMethod("getInvoiceWS", singleInteger);
       
       // getLatestInvoice
       addMethod("getLatestInvoice", singleInteger);

       // getLastInvoices
       final Class<?>[] twoIntegers = new Class[] {Integer.class, Integer.class};
       addMethod("getLastInvoices", twoIntegers);
       
       // getUserWS
       addMethod("getUserWS", singleInteger);
       
       // deleteUser
       // the parameters are the same
       addMethod("deleteUser",singleInteger);
       
       // updateUser
       addMethod("updateUser", new Class[] { UserWS.class });

       // updateUserContact
       addMethod("updateUserContact", new Class[] {Integer.class, Integer.class, ContactWS.class});

       final Class<?>[] singleOrderWS = new Class[] {OrderWS.class}; 
       addMethod("createOrder", singleOrderWS);
       addMethod("createOrderPreAuthorize", singleOrderWS);
       addMethod("updateOrder", singleOrderWS);
       
       addMethod("getOrder", singleInteger);
       addMethod("deleteOrder", singleInteger);
       addMethod("getLatestOrder", singleInteger);
       addMethod("getLastOrders", twoIntegers);
       
       addMethod("applyPayment", new Class[] { PaymentWS.class, Integer.class});
       
       addMethod("getPayment", singleInteger);
       addMethod("getLatestPayment", singleInteger);
       
       addMethod("getLastPayments", twoIntegers);
       
       addMethod("getOrderLine", singleInteger);
       
       // updateOrderLine
       Class<?>[] params = new Class[1];
       params[0] = OrderLineWS.class;
       addMethod("updateOrderLine",params);

       // getOrderByPeriod
       addMethod("getOrderByPeriod", twoIntegers);

       // getUserContactsWS
       addMethod("getUserContactsWS", singleInteger);

       // updateCreditCard
       params = new Class[2];
       params[0] = Integer.class;
       params[1] = CreditCardDTO.class;
       addMethod("updateCreditCard",params);
       
       //payInvoice
       addMethod("payInvoice", singleInteger);

       // deleteInvoice
       addMethod("deleteInvoice", singleInteger);
       
       // getItem
       params = new Class[3];
       params[0] = Integer.class;
       params[1] = Integer.class;
       params[2] = String.class;
       addMethod("getItem", params);
       
       addMethod("rateOrder", singleOrderWS);
       
       // updateItem
       params = new Class[1];
       params[0] = ItemDTOEx.class;
       addMethod("updateItem", params);

       // createInvoice
       params = new Class[2];
       params[0] = Integer.class;
       params[1] = Boolean.TYPE;
       addMethod("createInvoice", params);

       // getCurrentOrder
       params = new Class[2];
       params[0] = Integer.class;
       params[1] = Date.class;
       addMethod("getCurrentOrder", params);

       // updateCurrentOrder
       params = new Class[5];
       params[0] = Integer.class;
       params[1] = OrderLineWS[].class;
       params[2] = String.class;
       params[3] = Date.class;
       params[4] = String.class;
       addMethod("updateCurrentOrder", params);
    }

    private static void addMethod(String name, Class<?> params[]) {
        try {
            methods.add(target.getDeclaredMethod(name, params));
        } catch(NoSuchMethodException e) {
            String msg = "Failed to find method " + name;
            LOG.error(msg, e);
            throw new RuntimeException(msg);
         }
    }

    public WSMethodSecurityProxy() {
       // set the parent methods
       setMethods(methods.toArray(new Method[methods.size()]));          
    }
    
    public void before(final Method m, final Object[] args, final Object bean)
            throws SecurityException {
        if (!isMethodPresent(m)) {
            return;
        }

        TransactionTemplate transactionTemplate = new TransactionTemplate(
               (PlatformTransactionManager) Context.getBean(
               Context.Name.TRANSACTION_MANAGER));
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            public void doInTransactionWithoutResult(TransactionStatus status) {
                invokeInTransaction(m, args, bean);
            }
        });
    }

    public void invokeInTransaction(Method m, Object[] args, Object bean) 
            throws SecurityException {
        try {
            if(m.getName().equals("getInvoiceWS") || m.getName().equals("payInvoice") || 
                    m.getName().equals("deleteInvoice") ) {
                Integer arg = (Integer) args[0];
                
                if (arg != null) {
                    InvoiceBL bl = new InvoiceBL(arg);
                    validate(bl.getEntity().getBaseUser().getUserId());
                }
            } else if (m.getName().equals("getLatestInvoice") || 
                    m.getName().equals("getLastInvoices") ||
                    m.getName().equals("createInvoice")) {
                Integer userId = (Integer) args[0];
                
                if (userId != null) {
                    UserBL bl = new UserBL(userId);
                    validate(userId);
                }
            } else if(m.getName().equals("updateUserContact")) {
                Integer userId = (Integer) args[0];
                Integer contactTypeId = (Integer) args[1];
                
                if (userId != null) {
                    validate(userId);
                }
                // check that this is a valid contact type id
                try {
                    Integer entityId = new ContactTypeDAS().find(
                            contactTypeId).getEntity().getId();
                    if (!entityId.equals(WebServicesCaller.getCompanyId())) {
                        throw new SecurityException("Contact type belongs to entity " + entityId);
                    }
                } catch (Exception e) {
                    throw new SecurityException("Invalid contact type " + contactTypeId);
                }
            } else if(m.getName().equals("getUserWS") || 
                    m.getName().equals("deleteUser") ||
                    m.getName().equals("getUserContactsWS") ||
                    m.getName().equals("updateCreditCard") ||
                    m.getName().equals("getCurrentOrder") ||
                    m.getName().equals("updateCurrentOrder")) {
                Integer arg = (Integer) args[0];
            
                if (arg != null) {
                    validate(arg);
                }
            } else if(m.getName().equals("updateUser")) { // it only takes a UserWS as argument
                UserWS arg = (UserWS) args[0];
                if (arg != null) {
                    validate(arg.getUserId());
                }
            } else if(m.getName().equals("createOrder") || 
                    m.getName().equals("updateOrder") ||
                    m.getName().equals("createOrderPreAuthorize") ) {
                OrderWS arg = (OrderWS) args[0];
                
                if (arg != null) {
                    validate(arg.getUserId());
                    if (m.getName().equals("updateOrder")) {
                        OrderBL bl = new OrderBL(arg.getId());
                        validate(bl.getEntity().getUser().getUserId());
                    }
                }
            } else if(m.getName().equals("getOrder") || 
                    m.getName().equals("deleteOrder")) {
                Integer arg = (Integer) args[0];
                
                if (arg != null) {
                    OrderBL bl = new OrderBL(arg);
                    validate(bl.getEntity().getUser().getUserId());
                }
            } else if(m.getName().equals("getOrderLine")) {
                Integer arg = (Integer) args[0];
                
                if (arg != null) {
                    OrderBL bl = new OrderBL();
                    OrderLineDTO line = bl.getOrderLine(arg);
                    validate(line.getPurchaseOrder().getUser().getUserId());
                }
            } else if(m.getName().equals("updateOrderLine")) {
                OrderLineWS arg = (OrderLineWS) args[0];
                
                if (arg != null) {
                    OrderBL bl = new OrderBL();
                    OrderLineDTO line = bl.getOrderLine(arg.getId());
                    validate(line.getPurchaseOrder().getUser().getUserId());
                }
                
            } else if (m.getName().equals("getLatestOrder") || 
                    m.getName().equals("getLastOrders") ||
                    m.getName().equals("getOrderByPeriod")) {
                Integer arg = (Integer) args[0];
                if (arg != null) {
                    validate(arg);
                }
            } else if(m.getName().equals("applyPayment")) {
                PaymentWS arg = (PaymentWS) args[0];
                
                if (arg != null) {
                    validate(arg.getUserId());
                }
            } else if (m.getName().equals("getPayment")) {
                Integer arg = (Integer) args[0];
                
                if (arg != null) {
                    PaymentBL bl = new PaymentBL(arg);
                    validate(bl.getEntity().getBaseUser().getUserId());
                }
            } else if (m.getName().equals("getLatestPayment") ||
                    m.getName().equals("getLastPayments")) {
                Integer arg = (Integer) args[0];
                if (arg != null) {
                    validate(arg);
                }
            } else if (m.getName().equals("getItem")) {
            	Integer itemId = (Integer) args[0];
            	Integer userId = (Integer) args[1];
            	
            	Integer itemEntityId = new ItemDAS().find(itemId).getEntity().getId();
        		if (!itemEntityId.equals(WebServicesCaller.getCompanyId())) {
                    throw new SecurityException("Item belongs to entity " + itemEntityId);
                }
            	if (userId != null) {
            		validate(userId);
            	}
            } else if (m.getName().equals("updateItem")) {
            	ItemDTOEx item = (ItemDTOEx) args[0];
            	Integer itemEntityId = new ItemDAS().find(item.getId()).getEntity().getId();
            	if (!itemEntityId.equals(WebServicesCaller.getCompanyId())) {
            		throw new SecurityException("Item belongs to entity " + itemEntityId);
            	}
            	validate(WebServicesCaller.getUserId());
            }

        } catch (Exception e) {
            LOG.error("Exception ", e);
            throw new SecurityException(e.getMessage());
        } 
    }
}
