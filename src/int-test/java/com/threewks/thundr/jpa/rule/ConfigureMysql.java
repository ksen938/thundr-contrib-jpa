package com.threewks.thundr.jpa.rule;

import com.atomicleopard.thundr.jdbc.MySqlModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import org.junit.rules.ExternalResource;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class ConfigureMysql extends ExternalResource {
    public static final String MYSQL_JDBC_URL = "jdbc:mysql://localhost:3306/beverage?user=root&password=";
    protected UpdatableInjectionContext injectionContext;
    protected MySqlModule mySqlModule = new MySqlModule();

    public ConfigureMysql(UpdatableInjectionContext injectionContext) {
        this.injectionContext = injectionContext;
    }

    @Override
    protected void before() throws Throwable {
        String jdbcUrl = MYSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        mySqlModule.initialise(injectionContext);
    }


    @Override
    protected void after() {
        mySqlModule.stop(injectionContext);
    }
}
