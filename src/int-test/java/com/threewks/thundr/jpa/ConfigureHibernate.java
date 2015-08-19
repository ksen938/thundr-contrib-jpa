package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import org.hibernate.cfg.Environment;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class ConfigureHibernate extends ExternalResource {
    private final Class<?>[] entityTypes;
    protected UpdatableInjectionContext injectionContext;
    protected HibernateModule hibernateModule = null;

    public ConfigureHibernate(UpdatableInjectionContext injectionContext, Class<?>... entityTypes) {
        this.injectionContext = injectionContext;
        this.entityTypes = entityTypes;
    }

    @Override
    protected void before() throws Throwable {
        prepareHibernateConfig();
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);
    }

    protected void prepareHibernateConfig() {
        HibernateConfig hibernateConfig = injectionContext.get(HibernateConfig.class);
        if (hibernateConfig == null) {
            hibernateConfig = createDefaultTestHibernateConfig(injectionContext);
        }
        if (this.entityTypes.length > 0) {
            hibernateConfig.withEntities(this.entityTypes);
        }
    }

    protected HibernateConfig createDefaultTestHibernateConfig(UpdatableInjectionContext injectionContext) {
        DataSource dataSource = injectionContext.get(DataSource.class);

        HibernateConfig config = new HibernateConfig(dataSource)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false")
                .withProperty(Environment.SHOW_SQL, "true");
        injectionContext.inject(config).as(HibernateConfig.class);
        return config;
    }

    @Override
    protected void after() {
        hibernateModule.stop(injectionContext);
    }
}
