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
package com.sapienter.jbilling.server.util.db;

import org.hibernate.criterion.Restrictions;

import com.sapienter.jbilling.common.SessionInternalError;
import org.apache.log4j.Logger;

public class JbillingTableDAS extends AbstractDAS<JbillingTable> {

    private static final Logger LOG = Logger.getLogger(JbillingTableDAS.class);

    protected JbillingTableDAS() {
        super();
    }

    // can not cache directly, CGLib can not proxy extenders of <> classes
    public JbillingTable findByName(String name) {
        LOG.debug("Looking for table + " + name);
        JbillingTable table = findByCriteriaSingle(Restrictions.eq("name", name));
        if (table == null) {
            throw new SessionInternalError("Can not find table " + name);
        } 
        return table;
    }

    public static JbillingTableDAS getInstance() {
        return new JbillingTableDAS();
    }
}
