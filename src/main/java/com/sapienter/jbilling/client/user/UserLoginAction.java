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

package com.sapienter.jbilling.client.user;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.sapienter.jbilling.client.jsp.BillingAction;
import com.sapienter.jbilling.client.util.Constants;
import com.sapienter.jbilling.common.Util;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.UserWithCredentials;
import com.sapienter.jbilling.server.user.IUserSessionBean;
import com.sapienter.jbilling.server.user.User;
import com.sapienter.jbilling.server.user.UserCredentials;
import com.sapienter.jbilling.server.util.Context;
import com.sapienter.jbilling.server.user.db.CompanyDTO;


public final class UserLoginAction extends BillingAction {

    private static final Logger LOG = Logger.getLogger(UserLoginAction.class);

    // --------------------------------------------------------- Public Methods

    
    private HttpSession getNewSession(final HttpServletRequest request) {
    	final HttpSession session = request.getSession();
        if (!session.isNew()) {
        	session.invalidate();
        	return Objects.requireNonNull(request.getSession());
        }
        return session;
    }
    
    
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     */
    protected ActionForward doExecute(
        final ActionMapping mapping,
        final ActionForm form,
        final HttpServletRequest request,
        final HttpServletResponse response)
        {
            

        // Get the values from the form and do further validation
        // or parsing
        Locale locale = null;
        boolean internalLogin = false;
        final UserLoginForm info = (UserLoginForm) form;
        final String username = info.getUserName().trim();
        final String password = info.getPassword().trim();
        final String entityId = info.getEntityId().trim();

        // create the bean that is going to be passed to the session
        if (username.equals(Util.getSysProp("internal_username"))) {
            // then you have to show me the extra key
            String key = request.getParameter("internalKey");
            if (key != null && key.equals(
                    Util.getSysProp("internal_key"))) {
                internalLogin = true;
                LOG.debug("identified internal login");
            } else {
                LOG.debug("internal failed. Key is not good " + key);
            }
        }
    	int companyId = 1;
        if (!internalLogin) {
        	try { 
        		companyId = Integer.parseInt(entityId);
        	} catch (NumberFormatException nfe) {
                return errorResponse(mapping, request, "errors.company.id.nan");
        	}
        }
        CompanyDTO company;
        company = new CompanyDTO(companyId);
        // bean for authentication
        UserCredentials credentials = UserDTOEx.createCredentials(username, password, company);
        
        // verify that the billing process is not running
        File lock = new File(Util.getSysProp("login_lock"));
        if (lock.exists()) {
            LOG.debug("Denying login. Lock present.");
            return errorResponse(mapping, request, "user.login.lock");
        }

        boolean expired = true;
        String error = null;
        UserWithCredentials user = null;
        	// now do the call to the business object
            // get the value from a Session EJB 
            final IUserSessionBean myRemoteSession = (IUserSessionBean) Context.getBean(
                    Context.Name.USER_SESSION);
            Constants.Authentication result = myRemoteSession.authenticate(credentials);
            if (result.equals(Constants.Authentication.AUTH_WRONG_CREDENTIALS)) {
            	error = "user.login.badpassword";
            } else if (result.equals(Constants.Authentication.AUTH_LOCKED)) {
            	error = "user.login.passwordLocked";
            } else {
            	LOG.info("getting GUI UserDTO...");
                user = myRemoteSession.getGUIDTO(credentials.getUserName(), credentials.getEntityId());
            	LOG.info("got GUI UserDTO " + user);
                // children accounts can not login. They have no invoices and
                // can't make any payments
                if ((user == null) || user.getCustomer() != null && 
                        user.getCustomer().getParent() != null) {
                	error = "user.login.badpassword";
                }
                locale = myRemoteSession.getLocale(user.getId());
                // it is authenticated, let's create the session
                expired = myRemoteSession.isPasswordExpired(user.getId());
            }
        // Report any errors we have discovered back to the original form
        if (error != null) {
            return errorResponse(mapping, request, error);
        }
        
        // Save our logged-in user in the session
        final HttpSession session = Objects.requireNonNull(getNewSession(request));
        
        // this is for struts to pick up the right messages file
        session.setAttribute(Globals.LOCALE_KEY, locale);
        
        final String forwardName;
        // verify that the password has not expired
        if (expired) {
            // can't login, go to the change password page
            // the user id is needed to validate the new password (validators)
            // and for the next action to know who's the user
            session.setAttribute(Constants.SESSION_USER_ID, user.getId());
            // the password will come handy to compare to the new one
            session.setAttribute("jsp_initial_password", password);
            // Leave the session empty, otherwise it'd be possible to go directly 
            // to a page
            forwardName = "changePassword";
        } else {
        	forwardName = "success";
        }
        Objects.requireNonNull(session);
        Objects.requireNonNull(user);
        
        logUser(session, user);
        credentials = user;
        LOG.info("user " + credentials.getUserName() + " logged. entity = " + credentials.getEntityId());
        logSessionAttributes(session);
        // Forward control to the specified success URI
        return mapping.findForward(forwardName);

    }
    
    static void logUser(final HttpSession session, final User user) {
    	final int userId = user.getId();
    	final int companyId = user.getCompany().getId();
    	final int languageId = user.getLanguage().getId();
    	final int currencyId = user.getCurrency().getId();
    	session.setAttribute(Constants.SESSION_USER_ID, userId);
        session.setAttribute(Constants.SESSION_LOGGED_USER_ID, userId);
        session.setAttribute(Constants.SESSION_ENTITY_ID_KEY, companyId); 
        session.setAttribute(Constants.SESSION_LANGUAGE, languageId);
        session.setAttribute(Constants.SESSION_CURRENCY, currencyId);
        // need the whole thing :( for the permissions
        session.setAttribute(Constants.SESSION_USER_DTO, user); 
    }
}
