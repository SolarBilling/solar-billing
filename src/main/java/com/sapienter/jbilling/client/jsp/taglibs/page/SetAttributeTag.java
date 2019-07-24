package com.sapienter.jbilling.client.jsp.taglibs.page;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
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

/*
 * import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
*/

/**
 * JSP Tag <b>setAttribute</b>, used to set a PageContext
 * attribute with name <b>name</b> to a String from the body of the tag.
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name&gt;setAttribute&lt;/name&gt;
 * &lt;tagclass&gt;org.apache.taglibs.page.SetAttributeTag&lt;/tagclass&gt;
 * &lt;bodycontent&gt;JSP&lt;/bodycontent&gt;
 * &lt;info&gt;Sets an attribute from a page.&lt;/info&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;name&lt;/name&gt;
 *     &lt;required&gt;true&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 * </pre>
 *
 * @author Morgan Delagrange
 */

public class SetAttributeTag extends BodyTagSupport
{
	private static final long serialVersionUID = 1L;
	private String name = null;

    /**
     * Returns EVAL_BODY_TAG so the body of the tag can be evaluated.
     *
     * @return EVAL_BODY_TAG
     */
    public final int doStartTag() throws JspException
    {
	return EVAL_BODY_TAG;
    }

    /**
     * Read the body of the setAttribute tag to obtain the attribute value,
     * then set the attribute with <b>name</b> to that value.
     *
     * @return SKIP_BODY
     */
    public final int doAfterBody() throws JspException
    {
	// Use the body of the tag as attribute value
	BodyContent body = getBodyContent();
	String s = body.getString();
	// Clear the body since we only used it as input for the attribute
	// value
	body.clearBody();
	// set the attribute
	pageContext.setAttribute(name,(Object)s);
	return SKIP_BODY;
    }

    /**
     * Set the required tag attribute <b>name</b>.
     *
     * @param String name of page attribute to set value for
     */
    public final void setName(String str)
    {   
	name = str;
    }

}