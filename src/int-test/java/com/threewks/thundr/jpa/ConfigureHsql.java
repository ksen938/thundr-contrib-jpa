package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.CompoundKeyEntity;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import org.hibernate.cfg.Environment;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class ConfigureHsql extends ExternalResource {
    public static final String HSQL_JDBC_URL = "jdbc:hsqldb:mem";
    protected UpdatableInjectionContext injectionContext;
    protected HsqlDbModule hsqlDbModule = new HsqlDbModule();

    public ConfigureHsql(UpdatableInjectionContext injectionContext) {
        this.injectionContext = injectionContext;
    }

    @Override
    protected void before() throws Throwable {
        String jdbcUrl = HSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule.initialise(injectionContext);
    }


    @Override
    protected void after() {
        hsqlDbModule.stop(injectionContext);
    }
}
