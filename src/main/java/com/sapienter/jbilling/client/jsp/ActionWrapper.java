package com.sapienter.jbilling.client.jsp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.slf4j.LoggerFactory;

public class ActionWrapper extends Action {
	private Action action;
	
	public ActionForward execute(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
        {
			try {
				return action.execute(mapping, form, request, response);
			} catch (Exception throwable) {
				LoggerFactory.getLogger(getClass()).error("", throwable);
				throw throwable;
			}
        }

}
