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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

import com.sapienter.jbilling.client.jsp.BillingAction;
import com.sapienter.jbilling.client.util.Constants;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.IUserSessionBean;
import com.sapienter.jbilling.server.user.MutableUser;
import com.sapienter.jbilling.server.util.Context;

public class ChangePasswordAction extends BillingAction {
    private static Logger LOG = Logger.getLogger(ChangePasswordAction.class);
    
    protected ActionForward doExecute(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response)
    {
        final ActionMessages errors = new ActionMessages();
        final DynaValidatorForm info = (DynaValidatorForm) form;
        final String password = (String) info.get("password");
        final String verifyPassword = (String) info.get("verifyPassword");
        final HttpSession session = request.getSession();
        logSessionAttributes(session);
        final int userId = (Integer) session.getAttribute(Constants.SESSION_USER_ID);
        
        if (!password.equals(verifyPassword)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("user.create.error.password_match"));
        }

        final String oldPassword = (String) session.getAttribute("jsp_initial_password");
        if (password.equalsIgnoreCase(oldPassword)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("errors.repeated", "New password"));
        }


        try {
        	final IUserSessionBean myRemoteSession = (IUserSessionBean) Context.getBean(
                    Context.Name.USER_SESSION);
            // do the actual password change
            MutableUser user = myRemoteSession.getUserDTOEx(userId);
            user.setPassword(password);
            myRemoteSession.update(userId, (UserDTOEx)user);

            // I still need to call this method, because it populates the dto
            // with the menu and other fields needed for the login
            user = myRemoteSession.getGUIDTO(user.getUserName(), user.getEntityId());
            LOG.debug("Password changed for user " + userId);
            // now get the session completed. The user is actually logged only now.
            UserLoginAction.logUser(session, user);
            return (mapping.findForward("success"));
        } catch (RuntimeException e) {
        	LOG.error("",e);
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("all.internal"));
            saveErrors(request, errors);
            return (new ActionForward(mapping.getInput()));
        }
    }
}
