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

package com.sapienter.jbilling.server.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import com.sapienter.jbilling.server.user.EntityBL;
import com.sapienter.jbilling.server.user.UserBL;
import com.sapienter.jbilling.server.util.db.JbillingTableDAS;
import com.sapienter.jbilling.server.util.db.PreferenceDAS;
import com.sapienter.jbilling.server.util.db.PreferenceDTO;
import com.sapienter.jbilling.server.util.db.PreferenceTypeDAS;
import com.sapienter.jbilling.server.util.db.PreferenceTypeDTO;

public class PreferenceBL {
    

    // private methods
    final private PreferenceDAS preferenceDas = new PreferenceDAS();
    @Autowired private Function<Integer, PreferenceTypeDTO> typeDas = new PreferenceTypeDAS();
    private PreferenceDTO preference = null;
    private PreferenceTypeDTO type = null;
    private static Logger LOG = Logger.getLogger(PreferenceBL.class);
    private Locale locale = null;
    final private JbillingTableDAS jbDAS = (JbillingTableDAS) Context.getBean(Context.Name.JBILLING_TABLE_DAS);
    
    public PreferenceBL(int preferenceId) {
        preference = preferenceDas.apply(preferenceId);
    }
    
    public PreferenceBL(int entityId, int typeId)
    {
        set(entityId, typeId);
    }
    
    @Deprecated public PreferenceBL() {
    }
    
    /**
     * If the entity does not have the property explicitly set,
     * it will throw an EmptyResultDataAccessException, but will 
     * still find the default.
     * So you need to catch the exception and then use the default.
     * @param entityId
     * @param typeId
     * @throws EmptyResultDataAccessException
     */
    public void set(Integer entityId, Integer typeId) 
            throws EmptyResultDataAccessException {
        LOG.debug("Now looking for preference " + typeId + " ent " +
                entityId + " table " + Constants.TABLE_ENTITY);
        if (entityId != null) {
                EntityBL en = new EntityBL(entityId);
                locale = en.getLocale();
        }
        
        preference = preferenceDas.findByType_Row(
                typeId, entityId, Constants.TABLE_ENTITY);
        if (preference == null) {
            type = typeDas.apply(typeId);
            throw new EmptyResultDataAccessException("Could not find preference " + typeId, 1);
        }
            
    }

    public void setForUser(Integer userId, Integer typeId) {
            UserBL us = new UserBL(userId);
            locale = us.getLocale();

        preference = preferenceDas.findByType_Row(typeId, userId,
                Constants.TABLE_BASE_USER);
        if (preference == null) {
            type = typeDas.apply(typeId);
            throw new EmptyResultDataAccessException("Could not find preference " + typeId, 1);
        }

    }

    public void createUpdateForEntity(Integer entityId, Integer preferenceId, 
            Integer intValue, String strValue, BigDecimal fValue) {
        // lets see first if this exists
        try {
            set(entityId, preferenceId);
            // it does
            preference.setIntValue(intValue);
            preference.setStrValue(strValue);
            preference.setFloatValue((fValue == null) ? null : fValue);
        } catch (EmptyResultDataAccessException e) {
            // we need a new one
            preference = new PreferenceDTO();
            preference.setIntValue(intValue);
            preference.setStrValue(strValue);
            preference.setFloatValue(fValue == null ? null : fValue);
            preference.setForeignId(entityId);
            preference.setJbillingTable(jbDAS.findByName(Constants.TABLE_ENTITY));
            preference.setPreferenceType(new PreferenceTypeDAS().find(preferenceId));
            preference = preferenceDas.save(preference);
        }
    }

    public void createUpdateForUser(Integer userId, Integer typeId, 
            Integer intValue, String strValue, BigDecimal fValue) {
        // lets see first if this exists
        try {
            setForUser(userId, typeId);
            // it does
            preference.setIntValue(intValue);
            preference.setStrValue(strValue);
            preference.setFloatValue((fValue == null) ? null : fValue);
        } catch (EmptyResultDataAccessException e) {
            // we need a new one
            preference = new PreferenceDTO();
            preference.setIntValue(intValue);
            preference.setStrValue(strValue);
            preference.setFloatValue(fValue == null ? null : fValue);
            preference.setForeignId(userId);
            preference.setJbillingTable(jbDAS.findByName(Constants.TABLE_BASE_USER));
            preference.setPreferenceType(new PreferenceTypeDAS().find(typeId));
            preference = preferenceDas.save(preference);

        }
    }

    public int getInt() {
        if (preference != null) {
            return preference.getIntValue().intValue();
        } 
        
        return type.getIntDefValue().intValue();
    }

    public float getFloat() {
        if (preference != null) {
            return preference.getFloatValue().floatValue();
        } 
        
        return type.getFloatDefValue().floatValue();
    }

    public String getString() {
        if (preference != null) {
            return preference.getStrValue();
        }
        
        return type.getStrDefValue();
    }
    
    public String getValueAsString() {
        if (preference.getIntValue() != null) {
            return preference.getIntValue().toString();
        } else if (preference.getStrValue() != null) {
            return preference.getStrValue();
        } else if (preference.getFloatValue() != null) {
            if (locale == null) {
                return preference.getFloatValue().toString();
            } else {
                return Util.decimal2string(preference.getFloatValue(), locale);
            }
        }
        
        return null;
    }
    
    public String getDefaultAsString(int id) {
        LOG.debug("Looking for preference default for type " + id);
        type = typeDas.apply(id);
        if (type.getIntDefValue() != null) {
            return type.getIntDefValue().toString();
        } else if (type.getStrDefValue() != null) {
            return type.getStrDefValue();
        } else if (type.getFloatDefValue() != null) {
            if (locale == null) {
                return  type.getFloatDefValue().toString();
            } else {
                return Util.decimal2string(type.getFloatDefValue(), locale);
            }
        }
        
        return null;
    }
    
    public boolean isNull() {
    	return preference.getIntValue() == null && 
                preference.getStrValue() == null &&
                preference.getFloatValue() == null;
    }
    
    public PreferenceDTO getEntity() {
        return preference;
    }

	public static PreferenceBL getDefaultPreferences() {
		return new PreferenceBL();
	}
    
}
