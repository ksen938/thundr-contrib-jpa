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
import com.threewks.thundr.jpa.repository.StringRepository;
import com.threewks.thundr.jpa.rule.ConfigureHibernate;
import com.threewks.thundr.jpa.rule.ConfigureHikari;
import com.threewks.thundr.jpa.rule.ConfigureHsql;
import com.threewks.thundr.jpa.rule.ConfigureMysql;
import org.hamcrest.core.Is;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import javax.persistence.EntityManager;
import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class StringRepositoryIT {

    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureMysql configureMysql = new ConfigureMysql(injectionContext);
    public ConfigureHikari configureHikari = new ConfigureHikari(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, StringBeverage.class, LongBeverage.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHikari).around(configureHibernate);


    protected StringBeverage bevvie1;
    protected StringBeverage bevvie2;
    private StringBeverage bevvie3;
    protected CrudRepository<String, StringBeverage> jpaRepository;
    private Jpa jpa;

    @Before
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        bevvie1 = new StringBeverage("Beer", true);
        bevvie2 = new StringBeverage("Lemonade", false);
        bevvie3 = new StringBeverage("Juice", false);
        jpaRepository = new StringRepository<StringBeverage>(StringBeverage.class, jpa);
        deleteTestData();
        createBeverages();
    }

    @After
    public void after() {
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

    @Test
    public void shouldCreateAndReadSingleEntity() {
        StringBeverage localBev = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });
        assertThat(localBev.getName(), Is.is(bevvie1.getName()));
        assertThat(localBev.isAlcoholic(), Is.is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldUpdateSingleEntity() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                StringBeverage localBev = jpaRepository.read(bevvie1.getId());
                localBev.setName("Water");
                localBev.setAlcoholic(false);
                jpaRepository.update(localBev);
            }
        });
        checkUpdated(bevvie1.getId());
    }

    protected void checkUpdated(final String id) {
        StringBeverage beverage = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(id);
            }
        });
        assertThat(beverage.getName(), Is.is("Water"));
        assertThat(beverage.isAlcoholic(), Is.is(false));
    }

    @Test
    public void shouldUpdateMultipleEntitiesArray() {
        final StringBeverage[] beverages = setupMultipleUpdate();
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.update(beverages);
            }
        });
        checkMultipleUpdate(beverages);
    }

    @Test
    public void shouldUpdateMultipleEntitiesList() {
        final StringBeverage[] beverages = setupMultipleUpdate();
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.update(Arrays.asList(beverages));
            }
        });
        checkMultipleUpdate(beverages);
    }

    protected StringBeverage[] setupMultipleUpdate() {
        return jpa.run(Propagation.Required, new ResultAction<StringBeverage[]>() {
                @Override
                public StringBeverage[] run(EntityManager em) {
                    StringBeverage localBev1 = jpaRepository.read(bevvie1.getId());
                    StringBeverage localBev2 = jpaRepository.read(bevvie2.getId());
                    localBev1.setName("Water");
                    localBev1.setAlcoholic(false);
                    localBev2.setName("Water");
                    localBev2.setAlcoholic(false);
                    StringBeverage[] beverages = new StringBeverage[2];
                    beverages[0] = localBev1;
                    beverages[1] = localBev2;
                    return beverages;
                }
            });
    }

    protected void checkMultipleUpdate(StringBeverage[] beverages) {
        for (StringBeverage beverage : beverages) {
            checkUpdated(beverage.getId());
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

        assertThat(count, Is.is(3l));
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
        StringBeverage deletedBev = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });

        StringBeverage remainingBev = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie2.getId());
            }
        });

        assertThat(deletedBev, Is.is(nullValue()));
        assertThat(remainingBev.getId(), Is.is(bevvie2.getId()));
    }

    protected void testAllDeleted() {
        StringBeverage deletedBev1 = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId());
            }
        });

        StringBeverage deletedBev2 = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.read(bevvie2.getId());
            }
        });

        assertThat(deletedBev1, Is.is(nullValue()));
        assertThat(deletedBev2, Is.is(nullValue()));
    }

    @Test
    public void shouldReadMultipleEntities() {

        List<StringBeverage> beverageList1 = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.read(bevvie1.getId(), bevvie2.getId());
            }
        });

        List<String> beverageListKeys = new ArrayList<>();

        beverageListKeys.add(bevvie1.getId());
        beverageListKeys.add(bevvie2.getId());

        final List<String> finalBeverageListKeys = beverageListKeys;

        List<StringBeverage> beverageList2 = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.read(finalBeverageListKeys);
            }
        });

        containsBeverages(beverageList1);
        containsBeverages(beverageList2);
    }

    protected void containsBeverages(List<StringBeverage> beverages) {
        Map<String, StringBeverage> map = new HashMap<>();
        for (StringBeverage beverage : beverages) {
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
                jpaRepository.create(bevvie3);
            }
        });
    }

    @Test
    public void shouldCreateMultipleEntities() {
        deleteTestData(); //called to delete records created by before()

        final List<StringBeverage> beverages = new ArrayList<>();
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

    @Test
    public void shouldFindByAttribute(){
        StringBeverage beverage = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.find("name", "Beer", 1).iterator().next();
            }
        });
        assertThat(beverage.getName(), is(bevvie1.getName()));
        assertThat(beverage.getId(), is(bevvie1.getId()));
        assertThat(beverage.isAlcoholic(), is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldExcludeOnFindByAttribute(){
        StringBeverage beverage = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 1).iterator().next();
            }
        });
        assertThat(beverage.getName(), not(bevvie1.getName()));
        assertThat(beverage.getId(), not(bevvie1.getId()));
        assertThat(beverage.isAlcoholic(), not(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldFindMultipleRecordsByAttribute() {
        List<StringBeverage> beverages = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 10);
            }
        });
        boolean bev2Found = false;
        boolean bev3Found = false;
        for (StringBeverage beverage : beverages) {
            assertThat(beverage.isAlcoholic(), is(false));
            if (beverage.getId().equals(bevvie2.getId())) {
                assertThat(beverage.getName(), is("Lemonade"));
                bev2Found = true;
            }
            else if (beverage.getId().equals(bevvie3.getId())) {
                assertThat(beverage.getName(), is("Juice"));
                bev3Found = true;
            }
        }
        assertTrue(bev2Found && bev3Found);
    }

    @Test
    public void shouldLimitResultsOnFindByAttribute() {
        List<StringBeverage> beverages = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 1);
            }
        });
        assertThat(beverages.size(), is(1));
    }

    @Test
    public void shouldFindOnMultipleAttributes(){
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("alcoholic", false);
        attrs.put("name", "Juice");
        List<StringBeverage> beverages = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.find(attrs, 10);
            }
        });
        assertThat(beverages.size(), is(1));
        assertThat(beverages.get(0).getId(), is(bevvie3.getId()));
        assertThat(beverages.get(0).getName(), is(bevvie3.getName()));
        assertThat(beverages.get(0).isAlcoholic(), is(bevvie3.isAlcoholic()));
    }

    @Test
    public void shouldFindOnMultipleAttributesWithSingleAttribute() {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("alcoholic", true);
        List<StringBeverage> beverages = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.find(attrs, 10);
            }
        });
        assertThat(beverages.size(), is(1));
        assertThat(beverages.get(0).getId(), is(bevvie1.getId()));
        assertThat(beverages.get(0).getName(), is(bevvie1.getName()));
        assertThat(beverages.get(0).isAlcoholic(), is(bevvie1.isAlcoholic()));
    }

    @Test
    public void shouldListEntities() {
        List<StringBeverage> entities = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.list(5);
            }
        });

        assertThat(entities.size(), is(3));

        Map<String, StringBeverage> map = new HashMap<>();
        for (StringBeverage entity: entities) {
            map.put(entity.getId(), entity);
        }

        StringBeverage e1 = map.get(bevvie1.getId());
        StringBeverage e2 = map.get(bevvie2.getId());
        StringBeverage e3 = map.get(bevvie3.getId());

        assertThat(e1.getId(), is (bevvie1.getId()));
        assertThat(e1.getName(), is (bevvie1.getName()));
        assertThat(e1.isAlcoholic(), is (bevvie1.isAlcoholic()));

        assertThat(e2.getId(), is (bevvie2.getId()));
        assertThat(e2.getName(), is (bevvie2.getName()));
        assertThat(e2.isAlcoholic(), is (bevvie2.isAlcoholic()));

        assertThat(e3.getId(), is (bevvie3.getId()));
        assertThat(e3.getName(), is (bevvie3.getName()));
        assertThat(e3.isAlcoholic(), is (bevvie3.isAlcoholic()));
    }
}
