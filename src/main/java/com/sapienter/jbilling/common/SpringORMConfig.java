package com.sapienter.jbilling.common;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@org.springframework.context.annotation.Configuration
@EnableTransactionManagement
public class SpringORMConfig // implements ApplicationContextAware
{
    static private ApplicationContext applicationContext;
    private static final Logger LOG = Logger.getLogger(SpringORMConfig.class);


    static private SpringORMConfig instance;


    public SpringORMConfig()
    {
        if (instance != null)
        {
            throw new UnsupportedOperationException( "SpringORMConfig constructor should only be called once" );
        }
        instance = this;
    }

    static public SpringORMConfig getInstance()
    {
        if ( instance == null )
        {
            instance = new SpringORMConfig();
            LOG.info("SpringORMConfig instance created");
        }
        return instance;
    }

    static public void setApplicationContext(AnnotationConfigApplicationContext _applicationContext)
    {
        if (applicationContext == null)
        {
            applicationContext = _applicationContext;
            _applicationContext.register(SpringORMConfig.class);
        }
        else
        if (applicationContext != _applicationContext)
        {
            throw new IllegalArgumentException("attempt to register SpringORM with application context " + _applicationContext +
                " but it's already registered with " + applicationContext );
        }
    }

    @Bean
    public HibernateTemplate getHibernateTemplate() {
	return new HibernateTemplate(sessionFactory());
    }

    @Bean
    public SessionFactory sessionFactory() {
	SessionFactory sf = new Configuration().configure()
				.buildSessionFactory();
	return sf;
    }
    
    @Bean
    public HibernateTransactionManager getHibernateTransactionManager(){
	return new HibernateTransactionManager(sessionFactory());
    }

}

