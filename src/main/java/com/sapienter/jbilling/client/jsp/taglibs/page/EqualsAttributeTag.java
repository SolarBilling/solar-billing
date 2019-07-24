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
 * JSP Tag <b>equalsAttribute</b>, used to determine if a PageContext
 * attribute equals the value of the "match" tag attribute.
 * <p>
 * Includes the body of the tag if the attribute equals the value of the 
 * "match" tag attribute.
 * <p>
 * You can set the optional tag attribute <b>value</b> to <i>true</i> or
 * <i>false</i>.  The body of the tag is included if equalsAttribute matches
 * the value.
 * <p>
 * You can set the optional tag attribute <b>ignoreCase</b> to <i>true</i> or
 * <i>false</i>.  If ignoreCase is set to true, then the comparison between the
 * application attribute and the "match" tag attribute will <i>not</i> be 
 * case-sensitive.
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name>equalsAttribute&lt;/name>
 * &lt;tagclass>org.apache.taglibs.page.EqualsAttributeTag&lt;/tagclass>
 * &lt;bodycontent>JSP&lt;/bodycontent>
 * &lt;info>Includes the body of the tag if the page attribute equals the
 * value of the "match" tag attribute.&lt;/info>
 *   &lt;attribute>
 *     &lt;name>name&lt;/name>
 *     &lt;required>true&lt;/required>
 *     &lt;rtexprvalue>false&lt;/rtexprvalue>
 *   &lt;/attribute>
 *   &lt;attribute>
 *     &lt;name>value&lt;/name>
 *     &lt;required>false&lt;/required>
 *     &lt;rtexprvalue>false&lt;/rtexprvalue>
 *   &lt;/attribute>
 *   &lt;attribute>
 *     &lt;name>match&lt;/name>
 *     &lt;required>true&lt;/required>
 *     &lt;rtexprvalue>true&lt;/rtexprvalue>
 *   &lt;/attribute>
 *   &lt;attribute>
 *     &lt;name>ignoreCase&lt;/name>
 *     &lt;required>false&lt;/required>
 *     &lt;rtexprvalue>false&lt;/rtexprvalue>
 *   &lt;/attribute>
 * </pre>
 *
 * @author Morgan Delagrange
 */


public class EqualsAttributeTag extends TagSupport
{
    private static final long serialVersionUID = 1L;
	private String name = null;
    private String match = null;
    //default behaviour is don't ignore case and execute body when tag contents
    //equal value set by setMatch
    private boolean ignoreCase = false;
    private boolean value = true;

    /**
     * Includes the body of the tag if the page attribute equals the value set in the 
     * 'match' attribute.
     *
     * @return SKIP_BODY if equalsAttribute body content does not equal the value of 
     * the match attribute, EVAL_BODY_include if it does
     */
    public final int doStartTag() throws JspException
    {
        //result is whether or not tag contents equal the match attribute
        boolean result = false;
        Object attribute = pageContext.getAttribute(name);
      
        if (attribute == null) {
            result = false;
        } else {
            String attributeValue = attribute.toString();
        
            if (ignoreCase) {
                result = attributeValue.equalsIgnoreCase(match);
            } else {
                result = attributeValue.equals(match);
            }
        }
      
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
     * Set the String that will be compared to the page
     * attribute.
     *
     * @param String value to match against the page attribute
     */
    public final void setMatch(String str)
    {
        match = str;
    }
    

    /**
     * If ignoreCase is set to true, then the comparison 
     * between the "match" attribute and the 
     * page attribute will <i>not</i> be case sensitive
     * 
     * @param boolean    true = ignore case<BR>false = case sensitive<BR>
     *                   default value = false
     */
    public final void setIgnoreCase(boolean value)
    {
        this.ignoreCase = value;
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