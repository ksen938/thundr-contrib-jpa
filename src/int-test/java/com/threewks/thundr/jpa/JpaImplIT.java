package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.rule.ConfigureHibernate;
import com.threewks.thundr.jpa.rule.ConfigureHikari;
import com.threewks.thundr.jpa.rule.ConfigureHsql;
import com.threewks.thundr.jpa.rule.ConfigureMysql;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JpaImplIT {
    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureMysql configureMysql = new ConfigureMysql(injectionContext);
    public ConfigureHikari configureHikari = new ConfigureHikari(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, LongBeverage.class, StringBeverage.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureMysql).around(configureHikari).around(configureHibernate);

    private LongBeverage bevvie1;
    private LongBeverage bevvie2;
    private JpaImpl jpa;

    @Before
    public void before() {
        jpa = (JpaImpl) injectionContext.get(Jpa.class);
        bevvie1 = new LongBeverage("Beer", true);
        bevvie2 = new LongBeverage("Lemonade", false);
        deleteTestData();
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(bevvie1);
                em.remove(bevvie2);
            }
        });
    }

    protected void shouldReturnPersistedObjects() {
        final LongBeverage finalBev1 = bevvie1;
        final LongBeverage finalBev2 = bevvie2;
        LongBeverage queriedBevvie1 = jpa.run(new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return em.find(LongBeverage.class, finalBev1.getId());
            }
        });
        LongBeverage queriedBevvie2 = jpa.run(new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return em.find(LongBeverage.class, finalBev2.getId());
            }
        });
        assertTrue(queriedBevvie1.getId().equals(bevvie1.getId()));
        assertTrue(queriedBevvie2.getId().equals(bevvie2.getId()));
    }



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

        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(em.getTransaction().isActive());
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        LongBeverage nullBev1 = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return em.find(LongBeverage.class, bevvie1.getId());
            }
        });
        LongBeverage nullBev2 = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return em.find(LongBeverage.class, bevvie1.getId());
            }
        });

        assertThat(nullBev1, is(nullValue()));
        assertThat(nullBev1, is(nullValue()));
    }

    @Test
    public void shouldRollbackOnFailureWithDefaultPropagation() {
        deleteTestData();

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("expected");


        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException("expected");
            }
        });
    }

    @Test
    public void shouldRollbackOnFailureWithRequiredPropagation() {
        deleteTestData();

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("expected");


        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException("expected");
            }
        });
    }

    @Test
    public void shouldRollbackOnFailureWithRequiresNewPropagation() {
        deleteTestData();

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("expected");

        jpa.run(Propagation.RequiresNew, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                throw new RuntimeException("expected");
            }
        });
    }

}
