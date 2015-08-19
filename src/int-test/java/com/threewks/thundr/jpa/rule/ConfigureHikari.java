package com.threewks.thundr.jpa.rule;

import com.atomicleopard.thundr.jdbc.HikariModule;
import com.atomicleopard.thundr.jdbc.MySqlModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import org.junit.rules.ExternalResource;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class ConfigureHikari extends ExternalResource {
    protected UpdatableInjectionContext injectionContext;
    protected HikariModule hikariModule = new HikariModule();

    public ConfigureHikari(UpdatableInjectionContext injectionContext) {
        this.injectionContext = injectionContext;
    }

    @Override
    protected void before() throws Throwable {
        hikariModule.configure(injectionContext);
    }
    
    @Override
    protected void after() {
        hikariModule.stop(injectionContext);
    }
}
