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
package com.sapienter.jbilling.server.mediation.db;

import com.sapienter.jbilling.server.util.db.AbstractDAS;

import java.util.List;

public class MediationRecordDAS extends AbstractDAS<MediationRecordDTO> {

    private static final String findByKeyHQL =
            "select mediationRecord "
                   + " FROM MediationRecordDTO mediationRecord "
                   + " WHERE mediationRecord.key = :key "
                   + " order by mediationRecord.started desc";

    @SuppressWarnings("unchecked")
    public MediationRecordDTO findNewestByKey(String key) {
        List<MediationRecordDTO> results = getSession().createQuery(findByKeyHQL).setParameter("key", key).list();
        return (results.isEmpty() ? null : results.get(0));
    }

    private static final String isProcessedSQL =
            "select id_key " +
                    "from mediation_record " +
                    "where id_key = :key " +
                    "and ( " +
                    "    status_id = 29 " +    // MEDIATION_RECORD_STATUS_DONE_AND_BILLABLE
                    "    or status_id = 30 " + // MEDIATION_RECORD_STATUS_DONE_AND_NOT_BILLABLE
                    ")";
    
    /**
     * Returns true if a mediation record exists for the given id key and has been processed (done).
     *
     * @param key id key of mediated record to check
     * @return true if record exists with status, false if not
     */
    public boolean processed(String key) {
        /* note: the query must be fast, as it is executed for every incoming mediation event (record).
                 Native query using hardcoded status id's to avoid joins keeps query execution time low, please
                 do not try and replace this query with HQL or Hibernate Criteria!
         */
        return getSession().createSQLQuery(isProcessedSQL)
                .setString("key", key).scroll().first();
    }

    private static final String countMediationRecordsByEntityAndStatusHQL =
            "SELECT count(distinct mediationRecord) " +
                    " FROM MediationRecordDTO mediationRecord " +
                    " WHERE mediationRecord.process.configuration.entityId = :entity " +
                    " and mediationRecord.recordStatus = :status";

    public int countMediationRecordsByEntityIdAndStatus(Integer entityId, MediationRecordStatusDTO status) {
        return getSession().createQuery(countMediationRecordsByEntityAndStatusHQL).
        		setParameter("entity", entityId).
        		setParameter("status", status).list().size();
    }

    private static final String findByMediationProcessHQL =
            " select mediationRecord " +
                    " FROM MediationRecordDTO mediationRecord join fetch mediationRecord.recordStatus " +
                    " WHERE mediationRecord.process.id = :processId";

    public List<MediationRecordDTO> findByProcess(Integer mediationProcessId) {
        return getSession().createQuery(findByMediationProcessHQL).
            setParameter("processId", mediationProcessId).list();
    }
}
