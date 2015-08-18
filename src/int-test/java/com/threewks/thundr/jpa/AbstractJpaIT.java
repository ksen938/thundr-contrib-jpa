package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.atomicleopard.thundr.jdbc.MySqlModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.CompoundKeyEntity;
import com.threewks.thundr.jpa.model.EmbeddedIdCompoundKeyEntity;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.repository.CrudRepository;
import com.threewks.thundr.jpa.repository.StringRepository;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Created by kaushiksen on 13/08/2015.
 */

public abstract class AbstractJpaIT {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static final String HSQL_JDBC_URL = "jdbc:hsqldb:mem";
    public static final String MYSQL_JDBC_URL = "jdbc:mysql://localhost:3306/beverage";
    protected UpdatableInjectionContext injectionContext = new InjectionContextImpl();
    protected HibernateModule hibernateModule = null;
    protected HsqlDbModule hsqlDbModule = null;
    protected MySqlModule mySqlModule = null;
    protected HibernateConfig hibernateConfig = null;
    protected JpaImpl jpa;
    protected CrudRepository<String, StringBeverage> stringKeyRepo = new StringRepository<StringBeverage>(StringBeverage.class, jpa);

    @Before
    protected void before() {
        configureMysql();
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);

        EntityManagerFactory entityManagerFactory = injectionContext.get(EntityManagerFactory.class);
        jpa = new JpaImpl(entityManagerFactory);
    }

    private void configureHsql() {
        String jdbcUrl = HSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule = new HsqlDbModule();
        hsqlDbModule.initialise(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(StringBeverage.class)
                .withEntity(LongBeverage.class)
                .withEntity(CompoundKeyEntity.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false")
                .withProperty(Environment.SHOW_SQL, "true");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }

    private void configureMysql() {
        String jdbcUrl = MYSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        mySqlModule = new MySqlModule();
        mySqlModule.initialise(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(StringBeverage.class)
                .withEntity(LongBeverage.class)
                .withEntity(CompoundKeyEntity.class)
                .withEntity(EmbeddedIdCompoundKeyEntity.class) //TODO - improve error message when this is left out
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false")
                .withProperty(Environment.SHOW_SQL, "true")
                .withProperty(Environment.USER, "root")
                .withProperty(Environment.PASS, "");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }



}

