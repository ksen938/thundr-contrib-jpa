/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.jpa.rule;

import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import org.hibernate.cfg.Environment;
import org.junit.rules.ExternalResource;

import javax.sql.DataSource;
import java.util.Map;

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
        hibernateModule.configure(injectionContext);
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
