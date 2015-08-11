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
package com.threewks.thundr.jpa;

import com.atomicleopard.thundr.jdbc.HsqlDbModule;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateConfig;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.rule.SetupPersistenceManager;
import com.threewks.thundr.jpa.rule.SetupTransaction;
import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

public class JpaImplIT {
    @ClassRule
    public static SetupPersistenceManager setupPersistenceManager = new SetupPersistenceManager("test");

    @Rule
    public SetupTransaction setupTransaction = SetupTransaction.rollback(setupPersistenceManager.getPersistenceManager());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public static final String JDBC_URL = "jdbc:hsqldb:mem";
    private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
    private HibernateModule hibernateModule = null;
    private HsqlDbModule hsqlDbModule = null;
    HibernateConfig hibernateConfig = null;
    Beverage bevvie1 = new Beverage("Beer", true);
    Beverage bevvie2 = new Beverage("Lemonade", false);


    private JpaImpl jpa;

    @Before
    public void before() {
        String jdbcUrl = JDBC_URL;
        injectionContext.inject(jdbcUrl).as(String.class);
        hsqlDbModule = new HsqlDbModule();
        hsqlDbModule.configure(injectionContext);
        DataSource dataSource = injectionContext.get(DataSource.class);
        hibernateConfig = new HibernateConfig(dataSource)
                .withEntity(Beverage.class)
                .withProperty(Environment.HBM2DDL_AUTO, "create-drop");
        injectionContext.inject(hibernateConfig).as(HibernateConfig.class);
        hibernateModule = new HibernateModule();
        hibernateModule.start(injectionContext);

        EntityManagerFactory entityManagerFactory = injectionContext.get(EntityManagerFactory.class);
        jpa = new JpaImpl(entityManagerFactory);
    }

    @Test
    public void shouldPersistWithDefaultPropagation() {
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

    private void shouldReturnPersistedObjects() {
        final String id1 = bevvie1.getId();
        final String id2 = bevvie1.getId();
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
        assertThat(queriedBevvie1.getId(), is(id1));
        assertThat(queriedBevvie2.getId(), is(id2));
    }

    @Test
    public void shouldRollbackOnFailureWithDefaultPropagation() {
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
