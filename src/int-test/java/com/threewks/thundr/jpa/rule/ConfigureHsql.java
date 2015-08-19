package com.threewks.thundr.jpa.rule;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import org.junit.rules.ExternalResource;

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
