package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.atomicleopard.thundr.jdbc.MySqlModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.model.LongKeyBeverage;
import com.threewks.thundr.jpa.model.StringKeyBeverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.hamcrest.core.Is;
import org.hibernate.cfg.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;
import javax.sql.DataSource;

import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 13/08/2015.
 */
public abstract class AbstractJpaTest <K, T extends Beverage> {

    public AbstractJpaTest(T bevvie1, T bevvie2) {
        before();
        this.bevvie1 = bevvie1;
        this.bevvie2 = bevvie2;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static final String HSQL_JDBC_URL = "jdbc:hsqldb:mem";
    public static final String MYSQL_JDBC_URL = "jdbc:mysql://localhost:3306/beverage";
    protected UpdatableInjectionContext injectionContext = new InjectionContextImpl();
    protected HibernateModule hibernateModule = null;
    protected HsqlDbModule hsqlDbModule = null;
    protected MySqlModule mySqlModule = null;
    protected HibernateConfig hibernateConfig = null;
    protected Beverage<K> bevvie1;
    protected Beverage<K> bevvie2;
    protected JpaImpl jpa;
    protected JpaRepository<K, T> jpaRepository;

    @Before
    protected void before() {
        configureMysql();
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);

        EntityManagerFactory entityManagerFactory = injectionContext.get(EntityManagerFactory.class);
        jpa = new JpaImpl(entityManagerFactory);
        jpaRepository = setupJpaRepository(jpa);
        deleteTestData();
        createBeverages();
    }

    protected abstract JpaRepository<K,T> setupJpaRepository(Jpa jpa);

    private void configureHsql() {
        String jdbcUrl = HSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule = new HsqlDbModule();
        hsqlDbModule.initialise(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(StringKeyBeverage.class)
                .withEntity(LongKeyBeverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }

    private void configureMysql() {
        String jdbcUrl = MYSQL_JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        mySqlModule = new MySqlModule();
        mySqlModule.initialise(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(StringKeyBeverage.class)
                .withEntity(LongKeyBeverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop")
                .withProperty(Environment.AUTOCOMMIT, "false")
                .withProperty(Environment.USER, "root")
                .withProperty(Environment.PASS, "");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(bevvie1);
                em.remove(bevvie2);
            }
        });
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                assertTrue(em.createQuery("select b from StringBeverage b", StringKeyBeverage.class).getResultList().isEmpty());
            }
        });
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                assertTrue(em.createQuery("select b from LongBeverage b", LongKeyBeverage.class).getResultList().isEmpty());
            }
        });
    }

    protected void shouldReturnPersistedObjects() {
        final Beverage finalBev1 = bevvie1;
        final Beverage finalBev2 = bevvie2;
        Beverage queriedBevvie1 = jpa.run(new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return em.find(Beverage.class, finalBev1.getId());
            }
        });
        Beverage queriedBevvie2 = jpa.run(new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return em.find(Beverage.class, finalBev2.getId());
            }
        });
        Assert.assertThat(queriedBevvie1.getId(), is(bevvie1.getId()));
        Assert.assertThat(queriedBevvie2.getId(), is(bevvie2.getId()));
    }

    @Test
    public void shouldCreateAndReadSingleEntity() {
        Beverage localBev;
        localBev = jpaRepository.read(bevvie1.getId());
        assertThat(localBev.getName(), Is.is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), Is.is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldUpdateSingleEntity() {
        Beverage updatedBev = jpa.run(Propagation.Required, new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                Beverage localBev = jpaRepository.read(bevvie1.getId());
                localBev.setName("Water");
                localBev.setAlcoholic(false);
                return jpaRepository.update(localBev);
            }
        });
        checkUpdated(updatedBev);
    }

    protected void checkUpdated(Beverage beverage) {
        assertThat(beverage.getName(), Is.is("Water"));
        assertThat(beverage.isAlcoholic(), Is.is(false));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        final Beverage[] beverages = jpa.run(Propagation.Required, new ResultAction<Beverage[]>() {
            @Override
            public Beverage[] run(EntityManager em) {
                Beverage localBev1 = jpaRepository.read(bevvie1.getId());
                Beverage localBev2 = jpaRepository.read(bevvie2.getId());
                localBev1.setName("Water");
                localBev1.setAlcoholic(false);
                localBev2.setName("Water");
                localBev2.setAlcoholic(false);
                Beverage[] beverages = new Beverage[2];
                beverages[0] = localBev1;
                beverages[1] = localBev2;
                return beverages;
            }
        });
        List<Beverage> beverages1 = jpa.run(Propagation.Required, new ResultAction<List<Beverage>>() {
            @Override
            public List<Beverage> run(EntityManager em) {
                return jpaRepository.update(beverages);
            }
        });
        List<Beverage> beverages2 = jpa.run(Propagation.Required, new ResultAction<List<Beverage>>() {
            @Override
            public List<Beverage> run(EntityManager em) {
                return jpaRepository.update(Arrays.asList(beverages));
            }
        });
        for (Beverage beverage: beverages1) {
            checkUpdated(beverage);
        }
        for (Beverage beverage: beverages2) {
            checkUpdated(beverage);
        }
    }

    @Test
    public void shouldReturnAccurateCount() {
        Long count = jpa.run(Propagation.Required, new ResultAction<Long>() {
            @Override
            public Long run(EntityManager em) {
                return jpaRepository.count();
            }
        });

        assertThat(count, Is.is(2l));
    }

    @Test
    public void shouldDeleteSingleEntity() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.delete(bevvie1);
            }
        });
        testDeleted();
    }

    @Test
    public void shouldDeleteSingleEntityByKey() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.deleteByKey(bevvie1.getId());
            }
        });
        testDeleted();
    }

    protected void testDeleted() {
        Beverage deletedBev = jpa.run(Propagation.Required, new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });

        Beverage remainingBev = jpa.run(Propagation.Required, new ResultAction<Beverage>() {
            @Override
            public Beverage run(EntityManager em) {
                return jpaRepository.read(bevvie2.getId());
            }
        });

        assertThat(deletedBev, Is.is(nullValue()));
        assertThat(remainingBev.getId(), Is.is(bevvie2.getId()));
    }

    @Test
    public void shouldReadMultipleEntities() {

        List<Beverage> beverageList1 = jpa.run(Propagation.Required, new ResultAction<List<Beverage>>() {
            @Override
            public List<Beverage> run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId(), bevvie2.getId());
            }
        });

        List<String> beverageListKeys = new ArrayList<>();

        beverageListKeys.add(bevvie1.getId());
        beverageListKeys.add(bevvie2.getId());

        final List<String> finalBeverageListKeys = beverageListKeys;

        List<Beverage> beverageList2 = jpa.run(Propagation.Required, new ResultAction<List<Beverage>>() {
            @Override
            public List<Beverage> run(EntityManager em) {
                return jpaRepository.read(finalBeverageListKeys);
            }
        });

        containsBeverages(beverageList1);
        containsBeverages(beverageList2);
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

    protected void createBeverages() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(bevvie1);
                jpaRepository.create(bevvie2);
            }
        });
    }

    @Test
    public void shouldCreateMultipleEntities() {
        deleteTestData(); //called to delete records created by before()

        final List<Beverage> beverages = new ArrayList<>();
        beverages.add(bevvie1);
        beverages.add(bevvie2);

        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(beverages);
            }
        });

        containsBeverages(beverages);
        deleteTestData();

        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(bevvie1, bevvie2);
            }
        });
        containsBeverages(beverages);

        deleteTestData();
    }

    //JPA IT

    @Test
    public void shouldPersistWithDefaultPropagation() {
        deleteTestData();

        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        shouldReturnPersistedObjects();
    }

    @Test
    public void shouldPersistWithMandatoryPropagation() {
        deleteTestData();

        jpa.getExistingEntityManager();
        jpa.createNewEntityManager().getTransaction().begin();
        jpa.run(Propagation.Mandatory, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        jpa.getExistingEntityManager().getTransaction().commit();
        shouldReturnPersistedObjects();
    }

    @Test
    public void shouldNotCreateTransactionOrPersistForNeverPropagation() {
        deleteTestData();

        thrown.expect(NullPointerException.class);

        jpa.run(Propagation.Never, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(em.getTransaction().isActive());
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        shouldReturnPersistedObjects();
    }

    @Test
    public void shouldPersistOnRequiredPropagation() {
        deleteTestData();

        jpa.getExistingEntityManager();
        final EntityManager emLocal = jpa.createNewEntityManager();
        emLocal.getTransaction().begin();
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
    }

    @Test
    public void shouldCreateNewTransactionOnRequiresNewPropagation() {
        deleteTestData();
        bevvie1.getId()
        jpa.getExistingEntityManager();
        final EntityManager emLocal = jpa.createNewEntityManager();
        emLocal.getTransaction().begin();
        jpa.run(Propagation.RequiresNew, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(emLocal.getTransaction().equals(em.getTransaction()));
            }
        });
    }

    @Test
    public void shouldPersistOnRequiresNewPropagation() {
        deleteTestData();

        jpa.getExistingEntityManager();
        final EntityManager emLocal = jpa.createNewEntityManager();
        emLocal.getTransaction().begin();
        jpa.run(Propagation.RequiresNew, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
    }

    @Test
    public void shouldPersistUnderExistingTransactionForSupportsPropagation() {
        deleteTestData();

        jpa.getExistingEntityManager();
        final EntityManager emLocal = jpa.createNewEntityManager();
        emLocal.getTransaction().begin();
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        emLocal.getTransaction().commit();
        shouldReturnPersistedObjects();
    }

    @Test
    public void shouldNotCreateTransactionUnderSupportsPropagation() {
        deleteTestData();

        thrown.expect(NullPointerException.class);
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(em.getTransaction().isActive());
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        shouldReturnPersistedObjects();
    }

    @Test
    public void shouldRollbackOnFailureWithDefaultPropagation() {
        deleteTestData();

        thrown.expect(RollbackException.class);
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException();
            }
        });
    }

    @Test
    public void shouldRollbackOnFailureWithRequiredPropagation() {
        deleteTestData();

        thrown.expect(RollbackException.class);
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException();
            }
        });
    }

    @Test
    public void shouldRollbackOnFailureWithRequiresNewPropagation() {
        deleteTestData();

        thrown.expect(RollbackException.class);
        jpa.run(Propagation.RequiresNew, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException();
            }
        });
    }
}
