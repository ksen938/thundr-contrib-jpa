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
import com.threewks.thundr.jpa.repository.CrudRepository;
import com.threewks.thundr.jpa.repository.LongRepository;
import com.threewks.thundr.jpa.rule.ConfigureHibernate;
import com.threewks.thundr.jpa.rule.ConfigureHikari;
import com.threewks.thundr.jpa.rule.ConfigureHsql;
import com.threewks.thundr.jpa.rule.ConfigureMysql;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import javax.persistence.EntityManager;
import java.util.*;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class LongRepositoryIT {
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
    private CrudRepository<Long, LongBeverage> jpaRepository;
    private Jpa jpa;

    @Before
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        bevvie1 = new LongBeverage("Beer", true);
        bevvie2 = new LongBeverage("Lemonade", false);
        jpaRepository = new LongRepository<>(LongBeverage.class, jpa);
        deleteTestData();
        createBeverages();
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

    @Test
    public void shouldCreateAndReadSingleEntity() {
        LongBeverage localBev = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });
        assertThat(localBev.getName(), Is.is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), Is.is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldUpdateSingleEntity() {
        LongBeverage updatedBev = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                LongBeverage localBev = jpaRepository.read(bevvie1.getId());
                localBev.setName("Water");
                localBev.setAlcoholic(false);
                return jpaRepository.update(localBev);
            }
        });
        checkUpdated(updatedBev);
    }

    protected void checkUpdated(final LongBeverage beverage) {

        LongBeverage updatedBev = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                LongBeverage result = em.find(LongBeverage.class, beverage.getId());
                em.refresh(result);
                return result;
            }
        });
        assertThat(updatedBev.getName(), Is.is("Water"));
        assertThat(updatedBev.isAlcoholic(), Is.is(false));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        final LongBeverage[] beverages = jpa.run(Propagation.Required, new ResultAction<LongBeverage[]>() {
            @Override
            public LongBeverage[] run(EntityManager em) {
                LongBeverage localBev1 = jpaRepository.read(bevvie1.getId());
                LongBeverage localBev2 = jpaRepository.read(bevvie2.getId());
                localBev1.setName("Water");
                localBev1.setAlcoholic(false);
                localBev2.setName("Water");
                localBev2.setAlcoholic(false);
                LongBeverage[] beverages = new LongBeverage[2];
                beverages[0] = localBev1;
                beverages[1] = localBev2;
                return beverages;
            }
        });
        List<LongBeverage> beverages1 = jpa.run(Propagation.Required, new ResultAction<List<LongBeverage>>() {
            @Override
            public List<LongBeverage> run(EntityManager em) {
                return jpaRepository.update(beverages);
            }
        });
        List<LongBeverage> beverages2 = jpa.run(Propagation.Required, new ResultAction<List<LongBeverage>>() {
            @Override
            public List<LongBeverage> run(EntityManager em) {
                return jpaRepository.update(Arrays.asList(beverages));
            }
        });
        for (LongBeverage beverage : beverages1) {
            checkUpdated(beverage);
        }
        for (LongBeverage beverage : beverages2) {
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
        testOneDeleted();
    }

    @Test
    public void shouldDeleteSingleEntityByKey() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.deleteByKey(bevvie1.getId());
            }
        });
        testOneDeleted();
    }

    @Test
    public void shouldDeleteMultipleEntitiesByKey() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.delete(bevvie1.getId(), bevvie2.getId());
            }
        });
        testAllDeleted();
    }

    protected void testOneDeleted() {
        LongBeverage deletedBev = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });

        LongBeverage remainingBev = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie2.getId());
            }
        });

        assertThat(deletedBev, Is.is(nullValue()));
        assertThat(remainingBev.getId(), Is.is(bevvie2.getId()));
    }

    protected void testAllDeleted() {
        LongBeverage deletedBev1 = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });

        LongBeverage deletedBev2 = jpa.run(Propagation.Required, new ResultAction<LongBeverage>() {
            @Override
            public LongBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie2.getId());
            }
        });

        assertThat(deletedBev1, Is.is(nullValue()));
        assertThat(deletedBev2, Is.is(nullValue()));
    }

    @Test
    public void shouldReadMultipleEntities() {

        List<LongBeverage> beverageList1 = jpa.run(Propagation.Required, new ResultAction<List<LongBeverage>>() {
            @Override
            public List<LongBeverage> run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId(), bevvie2.getId());
            }
        });

        List<Long> beverageListKeys = new ArrayList<>();

        beverageListKeys.add(bevvie1.getId());
        beverageListKeys.add(bevvie2.getId());

        final List<Long> finalBeverageListKeys = beverageListKeys;

        List<LongBeverage> beverageList2 = jpa.run(Propagation.Required, new ResultAction<List<LongBeverage>>() {
            @Override
            public List<LongBeverage> run(EntityManager em) {
                return jpaRepository.read(finalBeverageListKeys);
            }
        });

        containsBeverages(beverageList1);
        containsBeverages(beverageList2);
    }

    protected void containsBeverages(List<LongBeverage> beverages) {
        Map<Long, LongBeverage> map = new HashMap<>();
        for (LongBeverage beverage : beverages) {
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

        final List<LongBeverage> beverages = new ArrayList<>();
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
}
