package com.sapienter.jbilling.client.jsp.taglibs.page;

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


/**
* JSP Tag <b>existsAttribute</b>, used to determine if a PageContext
* attribute exists.
* <p>
* Includes the body of the tag if the attribute exists.
* <p>
* You can set the optional tag attribute <b>value</b> to <i>true</i> or
* <i>false</i>.  The body of the tag is included if existsAttribute matches
* the value.
* <p>
* JSP Tag Lib Descriptor
* <p><pre>
* &lt;name&gt;existsAttribute&lt;/name&gt;
* &lt;tagclass&gt;org.apache.taglibs.page.ExistsAttributeTag&lt;/tagclass&gt;
* &lt;bodycontent&gt;JSP&lt;/bodycontent&gt;
* &lt;info&gt;Includes the body of the tag if the page attribute exists.&lt;/info&gt;
*   &lt;attribute&gt;
*     &lt;name&gt;name&lt;/name&gt;
*     &lt;required&gt;true&lt;/required&gt;
*     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
*   &lt;/attribute&gt;
*   &lt;attribute&gt;
*     &lt;name&gt;value&lt;/name&gt;
*     &lt;required&gt;false&lt;/required&gt;
*     &lt;rtexprvalue&gt;false&lt;/rtexprvalue&gt;
*   &lt;/attribute&gt;
* </pre>
*
* @author Morgan Delagrange
*/

public class ExistsAttributeTag extends TagSupport
{
	private static final long serialVersionUID = 1L;
	private String name = null;
	private boolean value = true;

   /**
    * Includes the body of the tag if the page attribute exists.
    *
    * @return SKIP_BODY if existsAttribute doesn't match value, EVAL_BODY_include if existsAttribute matches value
    */
   public final int doStartTag() throws JspException
   {
	boolean result = false;
	if( pageContext.getAttribute(name) != null )
	    result = true;

	if( value == result )
	    return EVAL_BODY_INCLUDE;

	return SKIP_BODY;
   }

   /**
    * Set the required tag attribute <b>name</b>. 
    *
    * @param String name of page attribute
    */
   public final void setName(String str)
   {   
	name = str;
   }

   /**
    * Set the optional tag attribute <b>value</b> to true or false. 
    *
    * @param boolean true or false
    */
   public final void setValue(boolean value)
   {
	this.value = value;
   }

}