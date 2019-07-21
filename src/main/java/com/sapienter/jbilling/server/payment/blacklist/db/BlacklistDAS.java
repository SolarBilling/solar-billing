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
package com.sapienter.jbilling.server.payment.blacklist.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.sapienter.jbilling.server.util.db.AbstractDAS;

public class BlacklistDAS extends AbstractDAS<BlacklistDTO> {
    
    public List<BlacklistDTO> findByEntity(Integer entityId) {
        // I need to access an association, so I can't use the parent helper class
        return selectAll(BlacklistDTO.class, ImmutableMap.of("company.id", entityId));
    }

    public List<BlacklistDTO> findByEntityType(Integer entityId, Integer type) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"type", type
        		));
    }

    public List<BlacklistDTO> findByEntitySource(Integer entityId, Integer source) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"source", source
        		));
    }

    public List<BlacklistDTO> findByUser(Integer userId) {
        return selectAll(BlacklistDTO.class, ImmutableMap.of(
        		"user.id", userId
        		));
    }

    public List<BlacklistDTO> findByUserType(Integer userId, Integer type) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"user.id", userId,
        		"type", type
        		));
    }

    // blacklist filter specific queries

    public List<BlacklistDTO> filterByName(final Integer entityId, final String firstName, final String lastName) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"type", BlacklistDTO.TYPE_NAME,
        		"contact.firstName", firstName,
        		"contact.lastName", lastName
        		));
    }

    public List<BlacklistDTO> filterByAddress(Integer entityId, String address1,
            String address2, String city, String stateProvince, 
            String postalCode, String countryCode) {
    	final Map<String, Object> parameters = new HashMap<String,Object>(immutableMapOf(
        		"company.id", entityId,
        		"type", BlacklistDTO.TYPE_ADDRESS,
        		"ct.address1", address1,
        		"ct.address2", address2,
        		"ct.city", city
    			));
    	parameters.putAll(immutableMapOf(
        		"ct.stateProvince", stateProvince,
        		"ct.postalCode", postalCode,
        		"ct.countryCode", countryCode
    			));
    	return selectAll(BlacklistDTO.class, ImmutableMap.copyOf(parameters));
    }

    public List<BlacklistDTO> filterByPhone(Integer entityId, 
            Integer phoneCountryCode, Integer phoneAreaCode, String phoneNumber) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"type", BlacklistDTO.TYPE_PHONE_NUMBER,
        		"contact.phoneCountryCode", phoneCountryCode,
        		"contact.phoneAreaCode", phoneAreaCode,
        		"contact.phoneNumber", phoneNumber
        		));
    }

    public List<BlacklistDTO> filterByCcNumbers(Integer entityId, 
            Collection<String> rawNumbers) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"type", BlacklistDTO.TYPE_CC_NUMBER,
        		"creditCard.rawNumber", rawNumbers
        		));
    }

    public List<BlacklistDTO> filterByIpAddress(Integer entityId, 
            String ipAddress, Integer ccfId) {
        return selectAll(BlacklistDTO.class, immutableMapOf(
        		"company.id", entityId,
        		"type", BlacklistDTO.TYPE_IP_ADDRESS,
        		"contact.fields.type", ccfId,
        		"contact.fields.content", ipAddress
        		));
    }


    public int deleteSource(Integer entityId, Integer source) {
        /*
        List<BlacklistDTO> deleteList = findByEntitySource(entityId, source);

        for (BlacklistDTO entry : deleteList) {
            delete(entry);
        }

        return deleteList.size();
        */

        // should be faster than above, but hql doesn't do cascading deletes :(
    	final ImmutableMap<String, ?> parameters = ImmutableMap.of( "company", entityId, "source", source);
        executeUpdate("DELETE FROM CreditCardDTO WHERE id IN (" +
                "SELECT creditCard.id FROM BlacklistDTO " + 
                "WHERE company.id = :company AND source = :source)", parameters);

        executeUpdate("DELETE FROM ContactFieldDTO WHERE contact.id IN (" +
                "SELECT contact.id FROM BlacklistDTO " + 
                "WHERE company.id = :company AND source = :source)", parameters);

        executeUpdate("DELETE FROM ContactDTO WHERE id IN (" +
                "SELECT contact.id FROM BlacklistDTO " + 
                "WHERE company.id = :company AND source = :source)", parameters);

        return executeUpdate("DELETE FROM BlacklistDTO " +
                "WHERE company.id = :company AND source = :source", parameters);
    }
}
