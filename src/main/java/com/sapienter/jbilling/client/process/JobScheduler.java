/*
 jBilling - The Enterprise Open Source Billing System
 Copyright (C) 2003-2010 Enterprise jBilling Software Ltd. and Emiliano Conde

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
package com.sapienter.jbilling.client.process;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;

/**
 * Singleton wrapper to provide easy access to the Quartz Scheduler. Used to schedule
 * all of jBilling's batch processes and {@link com.sapienter.jbilling.server.process.task.IScheduledTask}
 * plug-ins.
 *
 * @author Brian Cowdery
 * @since 02-02-2010
 */
public class JobScheduler {
    private static final Logger LOG = Logger.getLogger(JobScheduler.class);

    private static JobScheduler instance = null;
    final private Scheduler scheduler;

    private JobScheduler() {
        try {
			scheduler = new StdSchedulerFactory().getScheduler();
		} catch (SchedulerException e) {
            LOG.error("Exception occurred retrieving the scheduler instance.", e);
			throw new PluggableTaskException(e);
		}
    }

    public static JobScheduler getInstance() {
        if (instance == null)
            instance = new JobScheduler();
        return instance;
    }

    public boolean isShutdown() {
    	try {
			return scheduler.isShutdown();
		} catch (SchedulerException e) {
			throw new PluggableTaskException(e);
		}
    }
    
    private Scheduler getScheduler() {
        return scheduler;
    }

    public void start() {
        try {
            getScheduler().start();
        } catch (SchedulerException e) {
            LOG.error("Exception occurred starting the scheduler.", e);
            throw new PluggableTaskException(e);
        }
    }

    public void shutdown() {
        try {
            getScheduler().shutdown();
        } catch (SchedulerException e) {
            LOG.error("Exception occurred shutting down the scheduler.", e);
        }
    }

	public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
		if (isShutdown()) {
			LOG.warn("not scheduling job " + jobDetail + " because the scheduler has shut down");
		} else {
			try {
			getScheduler().scheduleJob(jobDetail, trigger);
			} catch (SchedulerException e) {
	            LOG.error("Exception scheduling job" + jobDetail, e);
	            throw new PluggableTaskException(e);
	        }
		}
	}
}
