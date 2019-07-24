package com.sapienter.jbilling.client.jsp.session;
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

// package org.apache.taglibs.session;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * TagExtraInfo for <b>attributes</b> tag, allows use of standard
 * &lt;jsp:getProperty/&gt; with the <b>attributes</b> tag script
 * variable id.
 *
 * @see AttributesTag
 *
 * @author Glenn Nielsen
 */

public class AttributesTEI extends TagExtraInfo
{
    public final VariableInfo[] getVariableInfo(TagData data)
    {
	return new VariableInfo[]
	{
	    new VariableInfo(
		data.getAttributeString("id"),
		"org.apache.taglibs.session.AttributesTag",
		true,
		VariableInfo.NESTED
	    ),
	};
    }
}