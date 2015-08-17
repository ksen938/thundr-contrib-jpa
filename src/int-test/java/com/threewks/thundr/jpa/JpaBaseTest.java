package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.hamcrest.core.Is;
import org.hibernate.cfg.Environment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 13/08/2015.
 */
public abstract class JpaBaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static final String HSQL_JDBC_URL = "jdbc:hsqldb:mem";
    public static final String MYSQL_JDBC_URL = "jdbc:mysql://localhost:3306/beverage";
    protected UpdatableInjectionContext injectionContext = new InjectionContextImpl();
    protected HibernateModule hibernateModule = null;
    protected HsqlDbModule hsqlDbModule = null;
    protected MySqlModule mySqlModule = null;
    protected HibernateConfig hibernateConfig = null;
    protected Beverage bevvie1 = new Beverage("Beer", true);
    protected Beverage bevvie2 = new Beverage("Lemonade", false);
    protected JpaRepository<String, Beverage> jpaRepository;


    protected JpaImpl jpa;

    protected void before() {
        configureHsql();
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);

        EntityManagerFactory entityManagerFactory = injectionContext.get(EntityManagerFactory.class);
        jpa = new JpaImpl(entityManagerFactory);
        jpaRepository = new JpaRepository<>(Beverage.class, jpa);
    }

    private void configureHsql() {
        String jdbcUrl = HSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule = new HsqlDbModule();
        hsqlDbModule.configure(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(Beverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }

    private void configureMysql() {
        String jdbcUrl = MYSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        mySqlModule = new MySqlModule();
        mySqlModule.configure(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(Beverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false")
                .withProperty(Environment.USER, "root")
                .withProperty(Environment.PASS, "");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }

    protected void shouldReturnPersistedObjects() {
        final String id1 = bevvie1.getId();
        final String id2 = bevvie2.getId();
        Beverage queriedBevvie1 = jpa.run(new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return em.find(Beverage.class, id1);
            }
        });
        Beverage queriedBevvie2 = jpa.run(new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return em.find(Beverage.class, id2);
            }
        });
        assertThat(queriedBevvie1.getId(), is(bevvie1.getId()));
        assertThat(queriedBevvie2.getId(), is(bevvie2.getId()));
    }

    protected void deleteTestData() {
        jpa.begin(Propagation.Required);
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(bevvie1);
                em.remove(bevvie2);
            }
        });
        jpa.commit();
        jpa.dispose();
    }

    protected void containsBeverages(List<Beverage> beverages) {
        Map<String,Beverage> map = new HashMap<>();
        for (Beverage beverage: beverages) {
            map.put(beverage.getId(), beverage);
        }
        assertTrue(map.containsKey(bevvie1.getId()));
        assertTrue(map.containsKey(bevvie2.getId()));
        Assert.assertThat(beverages.size(), Is.is(2));
    }

}
