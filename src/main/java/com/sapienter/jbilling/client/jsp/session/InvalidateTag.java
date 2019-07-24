package com.sapienter.jbilling.client.jsp.session;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class InvalidateTag extends TagSupport
{
	private static final long serialVersionUID = 1L;
	
	@Override
    public int doStartTag() throws JspException {
    	throw new JspException("TODO: InvalidateTag is not implemented");
    }
}
