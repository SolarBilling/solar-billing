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
package com.sapienter.jbilling.server.pluggableTask.admin;

import java.util.List;

import org.hibernate.Query;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.sapienter.jbilling.server.util.db.AbstractDAS;
import org.apache.log4j.Logger;

public class PluggableTaskDAS extends AbstractDAS<PluggableTaskDTO> {

    private static final Logger LOG = Logger.getLogger(PluggableTaskDAS.class);


    // QUERIES
    private static final String findAllByEntitySQL =
        "SELECT b " +
        "  FROM PluggableTaskDTO b " + 
        " WHERE b.entityId = :entity";
    
    private static final String findByEntityTypeSQL =
        "SELECT b " +
        "  FROM PluggableTaskDTO b " + 
        " WHERE b.entityId = :entity " +
        "   AND b.type.id = :type";

    private static final String findByEntityCategorySQL =
        "SELECT b " +
        "  FROM PluggableTaskDTO b " + 
        " WHERE b.entityId = :entity " +
        "   AND b.type.category.id = :category" +
        " ORDER BY b.processingOrder";

    // END OF QUERIES
   
    private PluggableTaskDAS() {
        super();
    }
    
    public List<PluggableTaskDTO> findAllByEntity(Integer entityId) {
        Query query = getSession().createQuery(findAllByEntitySQL);
        query.setParameter("entity", entityId);
        query.setCacheable(true);
        query.setComment("PluggableTaskDAS.findAllByEntity");
        return query.list();
    }
    
    public PluggableTaskDTO findByEntityType(Integer entityId, Integer typeId) {
        Query query = getSession().createQuery(findByEntityTypeSQL);
        query.setCacheable(true);
        query.setParameter("entity", entityId);
        query.setParameter("type", typeId);
        query.setComment("PluggableTaskDAS.findByEntityType");
        return (PluggableTaskDTO) query.uniqueResult();
    }

    @Cacheable("pluggabletask")
    public List<PluggableTaskDTO> findByEntityCategory(Integer entityId, Integer categoryId) {
        Query query = getSession().createQuery(findByEntityCategorySQL);
        query.setCacheable(true);
        query.setParameter("entity", entityId);
        query.setParameter("category", categoryId);
        query.setComment("PluggableTaskDAS.findByEntityCategory");

        List<PluggableTaskDTO> ret = query.list();
        return ret;
    }

    @CacheEvict(value = "pluggabletask", allEntries=true)
    public void invalidateCache() {
    }

    public static PluggableTaskDAS getInstance() {
        return new PluggableTaskDAS();
    }

}
