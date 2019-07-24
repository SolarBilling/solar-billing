package com.sapienter.jbilling.client.jsp.session;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

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

//package org.apache.taglibs.page;

/**
 * JSP Tag <b>removeAttribute</b>, used to remove a PageContext
 * attribute with name <b>name</b>.
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name&gt;removeAttribute&lt;/name&gt;
 * &lt;tagclass&gt;org.apache.taglibs.page.RemoveAttributeTag&lt;/tagclass&gt;
 * &lt;bodycontent&gt;empty&lt;/bodycontent&gt;
 * &lt;info&gt;Removes an attribute from a page.&lt;/info&gt;
 *   &lt;attribute&gt;
 *     &lt;name&gt;name&lt;/name&gt;
 *     &lt;required&gt;true&lt;/required&gt;
 *     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
 *   &lt;/attribute&gt;
 * </pre>
 *
 * @author Morgan Delagrange
 */

public class RemoveAttributeTag extends TagSupport
{
	private static final long serialVersionUID = 1L;
	private String name = null;

    /**
     * Removes an attribute from a page.
     *
     * @return SKIP_BODY
     */
    public final int doStartTag() throws JspException
    {
	pageContext.removeAttribute(name);
	return SKIP_BODY;
    }

    /**
     * Set the required tag attribute <b>name</b>.
     *
     * @param String name of page attribute to remove
     */
    public final void setName(String str)
    {   
	name = str;
    }

}