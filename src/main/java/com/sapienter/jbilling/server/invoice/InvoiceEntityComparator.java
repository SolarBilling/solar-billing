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

/*
 * Created on Nov 22, 2004
 *
 */
package com.sapienter.jbilling.server.invoice;

import java.util.Comparator;

import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;

/**
 * @author Emil
 */
public class InvoiceEntityComparator implements Comparator<InvoiceDTO> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(InvoiceDTO o1, InvoiceDTO o2) {
        InvoiceDTO parA = (InvoiceDTO) o1;
        InvoiceDTO parB = (InvoiceDTO) o2;
        
        return new Integer(parA.getId()).compareTo(new Integer(parB.getId()));
    }

}
