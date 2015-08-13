package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by kaushiksen on 13/08/2015.
 */
public abstract class JpaTestSetup {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static final String JDBC_URL = "jdbc:hsqldb:mem";
    protected UpdatableInjectionContext injectionContext = new InjectionContextImpl();
    protected HibernateModule hibernateModule = null;
    protected HsqlDbModule hsqlDbModule = null;
    protected HibernateConfig hibernateConfig = null;
    protected Beverage bevvie1 = new Beverage("Beer", true);
    protected Beverage bevvie2 = new Beverage("Lemonade", false);
    protected JpaRepository<Beverage> jpaRepository;


    protected JpaImpl jpa;

    protected void before() {
        String jdbcUrl = JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule = new HsqlDbModule();
        hsqlDbModule.configure(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(Beverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);

        EntityManagerFactory entityManagerFactory = injectionContext.get(EntityManagerFactory.class);
        jpa = new JpaImpl(entityManagerFactory);
        jpaRepository = new JpaRepository<>(Beverage.class, jpa);
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

}
