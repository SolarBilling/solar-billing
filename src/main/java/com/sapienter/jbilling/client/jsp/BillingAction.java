package com.sapienter.jbilling.client.jsp;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.slf4j.LoggerFactory;

public abstract class BillingAction extends Action {
	final private Logger LOG = Logger.getLogger(getClass());
	
	protected void logSessionAttributes(final HttpSession session) {
		LOG.info("session=" + session);
		LOG.info("session id=" + session.getId());
		final Enumeration<String> attributeNames = session.getAttributeNames(); 
		while (attributeNames.hasMoreElements()) {
			final String attributeName = attributeNames.nextElement();
			LOG.info(attributeName + " = " + session.getAttribute(attributeName));
		}
	}
	
    protected ActionForward errorResponse(final ActionMapping mapping, final HttpServletRequest request, final String message)
    {
        final ActionMessages errors = new ActionMessages();
        errors.add(
                ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage(message));
        saveErrors(request, errors);
        return (new ActionForward(mapping.getInput()));
    }

	abstract protected ActionForward doExecute(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception;

    
	public ActionForward execute(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
        {
			try {
				return doExecute(mapping, form, request, response);
			} catch (Exception throwable) {
				LoggerFactory.getLogger(getClass()).error("", throwable);
				throw throwable;
			}
        }
	
}
