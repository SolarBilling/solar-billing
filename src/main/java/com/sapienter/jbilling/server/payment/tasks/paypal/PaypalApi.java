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
package com.sapienter.jbilling.server.payment.tasks.paypal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paypal.api.payments.*;
import com.paypal.sdk.exceptions.PayPalException;
import com.paypal.core.NVPUtil;
import com.paypal.core.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.*;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.CreditCard;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.Payer;
import com.sapienter.jbilling.server.payment.tasks.paypal.dto.Payment;

/**
 * Created by Roman Liberov, 03/02/2010
 * Rewritten to use up-to-date Paypal API by David Hodges, 20/03/2016
 */
public class PaypalApi {

    private final String accessToken;

    public PaypalApi(String userId, String password, String signature,
                     String environment, String subject, int timeout)
            throws PayPalException, PayPalRESTException {

        Map<String, String> map = new HashMap<String, String>();
        map.put("mode", "sandbox");
        accessToken = new OAuthTokenCredential(userId, password, map).getAccessToken();
    }

    
    private Amount getAmount( final Payment payment )
    {
        final Details details = new Details();
        details.setShipping("0");
        details.setSubtotal("" + payment.getAmount());
        details.setTax("0"); // TODO what should tax be ?

        final Amount amount = new Amount();
        amount.setTotal(payment.getAmount());
        amount.setCurrency(payment.getCurrencyCode());
        amount.setDetails(details);
        return amount;
    }
    
    
    private com.paypal.api.payments.Payment getPaypalPayment( final Payment payment, final String intent, final String description )
    {
	    final Amount amount = getAmount(payment);
	
	    final Transaction transaction = new Transaction();
	    transaction.setAmount(amount);
	    transaction.setDescription(description);
	    final List<Transaction> transactions = new ArrayList<Transaction>();
	    transactions.add(transaction);                                                                                                                                        
	                                              
	    final com.paypal.api.payments.Payment paypalPayment = new com.paypal.api.payments.Payment();
	    paypalPayment.setIntent(intent);
	    paypalPayment.setTransactions(transactions);
	
	    return paypalPayment;
    }
    
    
    private com.paypal.api.payments.Payer getPaypalPayer( Payer payer, CreditCard creditCard )
	{
        final com.paypal.api.payments.CreditCard paypalCreditCard = new com.paypal.api.payments.CreditCard();
        paypalCreditCard.setType(creditCard.getType());
        paypalCreditCard.setNumber(creditCard.getAccount());
        paypalCreditCard.setExpireMonth(Integer.parseInt(creditCard.getExpirationMonth()));
        paypalCreditCard.setExpireYear(Integer.parseInt(creditCard.getExpirationYear()));
        paypalCreditCard.setCvv2(Integer.parseInt(creditCard.getCvv2()));
        paypalCreditCard.setFirstName(payer.getFirstName());
        paypalCreditCard.setLastName(payer.getLastName());

        final Address billingAddress = new Address();
        billingAddress.setLine1(payer.getStreet());
        billingAddress.setCity(payer.getCity());
        billingAddress.setState(payer.getState());
        billingAddress.setPostalCode(payer.getZip());
        billingAddress.setCountryCode(payer.getCountryCode());
        paypalCreditCard.setBillingAddress(billingAddress);

        final FundingInstrument fundingInstrument = new FundingInstrument();
        fundingInstrument.setCreditCard(paypalCreditCard);
        final List<FundingInstrument> fundingInstrumentList = new ArrayList<FundingInstrument>();
        fundingInstrumentList.add(fundingInstrument);

        final com.paypal.api.payments.Payer paypalPayer = new com.paypal.api.payments.Payer();
        paypalPayer.setFundingInstruments(fundingInstrumentList);
        paypalPayer.setPaymentMethod("credit_card");
        return paypalPayer;
	}
    
    
    public PaypalResult doDirectPayment(
            PaymentAction paymentAction, Payer payer, CreditCard creditCard, Payment payment, String description)
            throws PayPalRESTException 
    {
    	return getPaymentResult(makePayment(payer, creditCard, payment, "sale", description));
    }


    private com.paypal.api.payments.Payment makePayment(Payer payer, CreditCard creditCard, Payment payment, String intent, String description)
            throws PayPalRESTException 
    {
	    final com.paypal.api.payments.Payment paypalPayment = getPaypalPayment(payment,intent,description);
	    final com.paypal.api.payments.Payer paypalPayer = getPaypalPayer( payer, creditCard );
        paypalPayment.setPayer(paypalPayer);
        return paypalPayment.create(accessToken);
    }
    
    
    private PaypalResult doPayment( final com.paypal.api.payments.Payment paypalPayment ) throws PayPalRESTException
    {
    	return getPaymentResult(paypalPayment.create(accessToken));
    }
    
    
    private PaypalResult getPaymentResult( final com.paypal.api.payments.Payment createdPayment )
    {
        final PaypalResult result = new PaypalResult();
        result.setTransactionId(createdPayment.getId());
        result.setAvs(createdPayment.getState()); // TODO what's AVS ? is it the same as state ?
        return result;
    }
    
    
    public PaypalResult doReferenceTransaction(
            String transactionId, PaymentAction paymentAction, Payment payment, String description)
            throws PayPalRESTException {

	    final com.paypal.api.payments.Payment paypalPayment = getPaypalPayment(payment,"sale",description);
	    paypalPayment.getTransactions().get(1).setReferenceId(transactionId);
	    return doPayment(paypalPayment);
    }

    
    private Authorization createAuthorization( Payer payer, CreditCard creditCard, Payment payment, String description ) throws PayPalRESTException
    {
    	com.paypal.api.payments.Payment createdPayment = makePayment( payer, creditCard, payment, "authorize", description );
    	return createdPayment.getTransactions().get(0)
				.getRelatedResources().get(0).getAuthorization();
    }
    
    
    public PaypalResult doCapture(String authorizationId, Payment payment, CompleteType completeType)
            throws PayPalRESTException 
    {
    	final Authorization authorization = null; // TODO createAuthorization( payment );
        final Capture capture = new Capture();
		capture.setAmount(getAmount(payment));
		
		Capture responseCapture = authorization.capture(accessToken, capture);
        PaypalResult result = new PaypalResult();
    	result.setSucceseeded(true);
        result.setTransactionId(authorizationId);
        return result;
    }

    private Capture getCapture( String transactionId )
    {
    	throw new RuntimeException( "getCapture(transactionId) not implemented");
    }
    
    
    public PaypalResult doVoid(String transactionId)
            throws PayPalRESTException 
    {
    	final Authorization authorization = null; // TODO generate the authorization from the transaction ID
    	authorization.doVoid(accessToken);
    	PaypalResult result = new PaypalResult();
    	result.setSucceseeded(true);
        result.setTransactionId(transactionId);
        return result;
    }
    
    private Amount getAmount( String amount )
    {
    	throw new RuntimeException( "getAmount(String) not implemented");
    }

    
    public PaypalResult refundTransaction(String transactionId, String amountStr, RefundType refundType)
            throws PayPalRESTException 
    {
    	final Refund refund = new Refund();
    	final Amount amount = getAmount( amountStr );
    	refund.setAmount( amount );
    	final Capture capture = getCapture(transactionId);
    	capture.refund(accessToken, refund);
        PaypalResult result = new PaypalResult();
        result.setSucceseeded(true);
        result.setTransactionId(transactionId);
        return result;
    }
}

