package com.sapienter.jbilling.client.jsp.session;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import java.util.*;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.jsp.*;
//import javax.servlet.jsp.tagext.*;

/**
 * JSP Tag <b>attributes</b>, used to get HttpSession attribute information
 * using the standard JSP &lt;jsp:getProperty&gt; tag.
 * <p>
 * The script variable of name <b>id</b> is availble only within the
 * body of the <b>attributes</b> tag.
 * <p>
 * Loops through all the attributes in the HttpSession.
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name&gt;attributes&lt;/name&gt;
 * &lt;tagclass&gt;org.apache.taglibs.session.AttributesTag&lt;/tagclass&gt;
 * &lt;teiclass&gt;org.apache.taglibs.session.AttributesTEI&lt;/teiclass&gt;
 * &lt;bodycontent&gt;JSP&lt;/bodycontent&gt;
 * &lt;info&gt;Used to loop through all session attributes.&lt;/info&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;id&lt;/name&gt;
 *     &lt;required&gt;true&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 * </pre>
 *
 * @author Glenn Nielsen
 */

public class AttributesTag extends BodyTagSupport
{
	private static final long serialVersionUID = 1L;
	// Clients session
    private HttpSession sess = null;
    // All the session attributes
    private Enumeration<String> attributes = null;
    // Name of the current session attribute
    private String attribute = null;

    /**
     * Gets the session attributes.
     *
     * @return SKIP_BODY if there are no session attributes, EVAL_BODY_TAG if an attribute exists
     */
    public final int doStartTag() throws JspException
    {
	// Get the session
	sess = pageContext.getSession();

	// Loop through all session attributes
	attributes = sess.getAttributeNames();
	if( attributes == null || !attributes.hasMoreElements() )
	    return SKIP_BODY;
	attribute = (String)attributes.nextElement();
	if( attribute == null )
	    return SKIP_BODY;

	pageContext.setAttribute(id,this,PageContext.PAGE_SCOPE);
	return EVAL_BODY_TAG;
    }

    /**
     * Method called at end of each iteration of the attributes tag.
     *
     * @return EVAL_BODY_TAG if there is another session attribute, or SKIP_BODY if there are no more attributes
     */
    public final int doAfterBody() throws JspException
    {
	// See if this is the last or a named attribute
	if( !attributes.hasMoreElements() )
	    return SKIP_BODY;
	attribute = (String)attributes.nextElement();
	if( attribute == null )
	    return SKIP_BODY;
	// There is another attribute, so loop again
	return EVAL_BODY_TAG;
    }

    /**
     * Method called at end of attributes tag, outputs the body
     * of the tag.
     *
     * @return EVAL_PAGE
     */
    public final int doEndTag() throws JspException
    {
        pageContext.removeAttribute(id,PageContext.PAGE_SCOPE);
	try
	{
	    if(bodyContent != null)
	    bodyContent.writeOut(bodyContent.getEnclosingWriter());
	} catch(java.io.IOException e)
	{
	    throw new JspException("IO Error: " + e.getMessage());
	}
	return EVAL_PAGE;
    }

    /**
     * Returns the name of the session attribute.
     * <p>
     * &lt;jsp:getProperty name=<i>"id"</i> property="name"/&gt;
     *
     * @return String - the session attribute name
     */
    public final String getName()
    {
	return attribute;
    }

    /**
     * Returns the value of the session attribute.
     * <p>
     * &lt;jsp:getProperty name=<i>"id"</i> property="value"/&gt;
     *
     * @return String - the value of the session attribute
     */
    public final String getValue()
    {
	Object value = sess.getAttribute(attribute);
	if( value == null )
	    return "";
	return "" + value.toString();
    }

}