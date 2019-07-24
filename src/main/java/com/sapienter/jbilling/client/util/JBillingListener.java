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

package com.sapienter.jbilling.client.util;

import java.util.List;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import com.sapienter.jbilling.client.process.JobScheduler;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
import com.sapienter.jbilling.server.process.task.IScheduledTask;
import com.sapienter.jbilling.server.user.db.CompanyDAS;
import com.sapienter.jbilling.server.user.db.CompanyDTO;
import com.sapienter.jbilling.server.util.Context;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.sapienter.jbilling.client.item.CurrencyArrayWrap;
import com.sapienter.jbilling.client.process.Trigger;
import com.sapienter.jbilling.common.SessionInternalError;
import com.sapienter.jbilling.server.list.IListSessionBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listens for servlet context initialization and destruction. Used to
 * start/stop jBilling services when the servlet container 
 * starts/stops.
 */
public class JBillingListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(JBillingListener.class);
    @Autowired private Supplier<List<CompanyDTO>> companyDAS = new CompanyDAS();

	public void contextInitialized(ServletContextEvent event) {
		LOG.info("log4j logging is working");
		LoggerFactory.getLogger(getClass()).info("slf4j logging is working");
		java.util.logging.Logger.getLogger(getClass().getName()).info("java.util.logging is working");
        // validate that the java version is correct
        validateJava();
        System.setProperty("com.sun.net.ssl.enableECC", "false");
        // initializes scheduled billing batch processes
		Trigger.Initialize();

        // initializes ScheduledTask plug-ins
        JobScheduler scheduler = JobScheduler.getInstance();

        LOG.debug("Processing pluggable scheduled tasks...");

        try {
            for (CompanyDTO entity : companyDAS.get()) {
                PluggableTaskManager<IScheduledTask> taskManager =
                        new PluggableTaskManager<IScheduledTask>
                                (entity.getId(), com.sapienter.jbilling.server.util.Constants.PLUGGABLE_TASK_SCHEDULED);

                LOG.debug(taskManager.getAllTasks().size() + " scheduled tasks for entity " + entity.getId());

                IScheduledTask task = taskManager.getNextClass();
                while (task != null) {
                    LOG.debug("scheduled task '" + task.getTaskName() + "', " + task.getClass());
                    scheduler.scheduleJob(task.getJobDetail(), task.getTrigger());
                    task = taskManager.getNextClass();
                }
            }
        } catch (PluggableTaskException e) {
            LOG.error("Exception occurred scheduling pluggable tasks.", e);
        }

        // start the scheduler now that all the tasks have been scheduled.
        LOG.debug("Starting up the scheduler...");
        scheduler.start();

        // initialize the currencies, which are in application scope
        ServletContext context = event.getServletContext();
        LOG.debug("Loading application currency symbols");
        try {
        	/*
        	DataSource dataSource = (DataSource)Context.getBean(Context.Name.DATA_SOURCE);
        	// if (dataSource instanceof AbstractComboPooledDataSource) 
        	{
        		AbstractComboPooledDataSource pooledDataSource = (AbstractComboPooledDataSource) dataSource; 
        		if (pooledDataSource.getPassword() == null) {
            		pooledDataSource.setPassword("password");
            		throw new RuntimeException("password was not set");
        		}
        		if (pooledDataSource.getUser() == null) {
            		pooledDataSource.setUser("postgres");
            		throw new RuntimeException("user was not set");
        		}
        		java.util.logging.Logger.getLogger("JBillingListener").info("database username and password have been set");
        	}
        	*/
            IListSessionBean myRemoteSession = (IListSessionBean) 
                    Context.getBean(Context.Name.LIST_SESSION);
            context.setAttribute(Constants.APP_CURRENCY_SYMBOLS, 
                    new CurrencyArrayWrap(
                            myRemoteSession.getCurrencySymbolsMap()));
        } catch (RuntimeException e) {
            throw new SessionInternalError(e);
        }
        
    }
    
    private void validateJava() {
        Float version = Float.valueOf(System.getProperty("java.version").substring(0, 3));
        if (version < 1.6F) {
            // can't run!
           LOG.fatal("*********************************************************");
           LOG.fatal("You need Java version 1.6 or higher to run jbilling. " +
                "Your current version is " + version);
           LOG.fatal("*********************************************************"); 
           System.exit(1);
        }
        
        if (!System.getProperty("java.vendor").matches(".*Sun.*")) {
            LOG.warn("Your java vendor is not Sun. Results are unpredicatble");
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
    	LOG.info("JBilling servlet context destroyed. Shutting down...");
        Context.shutdown(); // shutdown Spring container
        // CacheManager.getInstance().shutdown(); // shut down EhCache - unfortunately getInstance() creates a new instance if we've already shut down
        JobScheduler.getInstance().shutdown(); // shutdown Quartz scheduler
    	LOG.info("JBilling finished shutting down.");
    }
}
