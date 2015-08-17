package com.threewks.thundr.jpa;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;

import javax.sql.DataSource;

/**
 * Created by kaushiksen on 7/08/2015.
 */
public class MySqlModule extends BaseModule {
    @Override
    public void requires(DependencyRegistry dependencyRegistry) {
        super.requires(dependencyRegistry);
    }

    @Override
    public void configure(UpdatableInjectionContext injectionContext) {
        String jdbcUrl = injectionContext.get(String.class, "jdbc.url");
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl(jdbcUrl);
        injectionContext.inject(mysqlDataSource).as(DataSource.class);
    }
}
