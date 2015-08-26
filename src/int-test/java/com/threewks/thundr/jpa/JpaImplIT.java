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

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.rule.ConfigureHibernate;
import com.threewks.thundr.jpa.rule.ConfigureHikari;
import com.threewks.thundr.jpa.rule.ConfigureHsql;
import com.threewks.thundr.jpa.rule.ConfigureMysql;
import org.junit.After;
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
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHikari).around(configureHibernate);

    private LongBeverage bevvie1;
    private LongBeverage bevvie2;
    private JpaImpl jpa;

    @Before
    public void before() {
        jpa = (JpaImpl) injectionContext.get(Jpa.class);
        bevvie1 = new LongBeverage("Beer", true);
        bevvie2 = new LongBeverage("Lemonade", false);
    }

    @After
    public void after() {
        deleteTestData();
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                LongBeverage toBeDeleted1 = em.merge(bevvie1);
                LongBeverage toBeDeleted2 = em.merge(bevvie2);
                em.remove(toBeDeleted1);
                em.remove(toBeDeleted2);
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

        thrown.expect(IllegalStateException.class);

        jpa.run(Propagation.Never, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(em.getTransaction().isActive());
                em.persist(bevvie1);
                em.persist(bevvie2);
                em.flush();
            }
        });

    }

    @Test
    public void shouldPersistOnRequiredPropagation() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
        shouldReturnPersistedObjects();
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
        emLocal.getTransaction().commit();

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

        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                assertFalse(em.getTransaction().isActive());
                em.persist(bevvie1);
                em.persist(bevvie2);
            }
        });
    }

    @Test
    public void shouldRollbackOnFailureWithDefaultPropagation() {
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
