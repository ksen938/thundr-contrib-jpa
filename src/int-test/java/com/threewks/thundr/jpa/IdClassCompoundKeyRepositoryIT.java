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
import com.threewks.thundr.jpa.model.IdClassCompoundKeyEntity;
import com.threewks.thundr.jpa.model.CompoundKeyEntityId;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.repository.BaseRepository;
import com.threewks.thundr.jpa.repository.IdClassCompoundKeyRepository;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class IdClassCompoundKeyRepositoryIT {
    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureMysql configureMysql = new ConfigureMysql(injectionContext);
    public ConfigureHikari configureHikari = new ConfigureHikari(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, IdClassCompoundKeyEntity.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHikari).around(configureHibernate);

    private Jpa jpa;
    protected IdClassCompoundKeyEntity compoundKeyEntity1;
    protected IdClassCompoundKeyEntity compoundKeyEntity2;
    protected IdClassCompoundKeyEntity compoundKeyEntity3;
    protected BaseRepository<CompoundKeyEntityId, IdClassCompoundKeyEntity> jpaRepository;

    @Before
    @SuppressWarnings(value = "unchecked")
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        compoundKeyEntity1 = new IdClassCompoundKeyEntity(true, "Entity1");
        compoundKeyEntity2 = new IdClassCompoundKeyEntity(false, "Entity2");
        compoundKeyEntity3 = new IdClassCompoundKeyEntity(false, "Entity3");
        jpaRepository = new IdClassCompoundKeyRepository<>(IdClassCompoundKeyEntity.class, jpa);
        deleteTestData();
        createCkEntitys();
    }

    @After
    public void after() {
        deleteTestData();
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                IdClassCompoundKeyEntity toBeDeleted1 = em.merge(compoundKeyEntity1);
                IdClassCompoundKeyEntity toBeDeleted2 = em.merge(compoundKeyEntity2);
                IdClassCompoundKeyEntity toBeDeleted3 = em.merge(compoundKeyEntity3);

                em.remove(toBeDeleted1);
                em.remove(toBeDeleted2);
                em.remove(toBeDeleted3);
            }
        });
    }

    @Test
    public void shouldCreateAndReadSingleEntity() {
        IdClassCompoundKeyEntity localCkEntity = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });
        assertThat(localCkEntity.getName(), Is.is(compoundKeyEntity1.getName()));
    }

    @Test
    public void shouldUpdateSingleEntity() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                IdClassCompoundKeyEntity localCkEntity = jpaRepository.read(compoundKeyEntity1.getId());
                localCkEntity.setName("Water");
                jpaRepository.update(localCkEntity);
            }
        });
        checkUpdated(compoundKeyEntity1.getId());
    }

    protected void checkUpdated(final CompoundKeyEntityId key) {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                IdClassCompoundKeyEntity CkEntity = jpaRepository.read(key);
                assertThat(CkEntity.getName(), Is.is("Water"));
            }
        });
    }

    @Test
    public void shouldUpdateMultipleEntitiesList() {
        final IdClassCompoundKeyEntity[] CkEntitys = startMultipleUpdate();
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.update(Arrays.asList(CkEntitys));
            }
        });
        checkMultipleUpdate(CkEntitys);
    }

    @Test
    public void shouldUpdateMultipleEntitiesArray() {
        final IdClassCompoundKeyEntity[] CkEntitys = startMultipleUpdate();
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.update(CkEntitys);
            }
        });
        checkMultipleUpdate(CkEntitys);
    }

    protected IdClassCompoundKeyEntity[] startMultipleUpdate() {
        return jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity[]>() {
                @Override
                public IdClassCompoundKeyEntity[] run(EntityManager em) {
                    IdClassCompoundKeyEntity localCkEntity1 = jpaRepository.read(compoundKeyEntity1.getId());
                    IdClassCompoundKeyEntity localCkEntity2 = jpaRepository.read(compoundKeyEntity2.getId());
                    localCkEntity1.setName("Water");
                    localCkEntity2.setName("Water");
                    IdClassCompoundKeyEntity[] CkEntitys = new IdClassCompoundKeyEntity[2];
                    CkEntitys[0] = localCkEntity1;
                    CkEntitys[1] = localCkEntity2;
                    return CkEntitys;
                }
            });
    }

    protected void checkMultipleUpdate(IdClassCompoundKeyEntity[] ckEntitys) {
        for (IdClassCompoundKeyEntity CkEntity : ckEntitys) {
            checkUpdated(CkEntity.getId());
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
                jpaRepository.delete(compoundKeyEntity1);
            }
        });
        testOneDeleted();
    }

    @Test
    public void shouldDeleteSingleEntityByKey() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.deleteByKey(compoundKeyEntity1.getId());
            }
        });
        testOneDeleted();
    }

    @Test
    public void shouldDeleteMultipleEntitiesByKey() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.delete(compoundKeyEntity1.getId(), compoundKeyEntity2.getId());
            }
        });
        testAllDeleted();
    }

    protected void testOneDeleted() {
        IdClassCompoundKeyEntity deletedBev = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });

        IdClassCompoundKeyEntity remainingBev = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity2.getId());
            }
        });

        assertThat(deletedBev, Is.is(nullValue()));
        assertThat(remainingBev.getId(), Is.is(compoundKeyEntity2.getId()));
    }

    protected void testAllDeleted() {
        IdClassCompoundKeyEntity deletedBev1 = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });

        IdClassCompoundKeyEntity deletedBev2 = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity2.getId());
            }
        });

        assertThat(deletedBev1, Is.is(nullValue()));
        assertThat(deletedBev2, Is.is(nullValue()));
    }

    @Test
    public void shouldReadMultipleEntities() {

        List<IdClassCompoundKeyEntity> CkEntityList1 = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId(), compoundKeyEntity2.getId());
            }
        });

        List<CompoundKeyEntityId> CkEntityListKeys = new ArrayList<>();

        CkEntityListKeys.add(compoundKeyEntity1.getId());
        CkEntityListKeys.add(compoundKeyEntity2.getId());

        final List<CompoundKeyEntityId> finalCkEntityListKeys = CkEntityListKeys;

        List<IdClassCompoundKeyEntity> CkEntityList2 = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.read(finalCkEntityListKeys);
            }
        });

        containsCkEntitys(CkEntityList1);
        containsCkEntitys(CkEntityList2);
    }

    protected void containsCkEntitys(List<IdClassCompoundKeyEntity> CkEntitys) {
        Map<CompoundKeyEntityId, IdClassCompoundKeyEntity> map = new HashMap<>();
        for (IdClassCompoundKeyEntity CkEntity : CkEntitys) {
            map.put(CkEntity.getId(), CkEntity);
        }
        assertTrue(map.containsKey(compoundKeyEntity1.getId()));
        assertTrue(map.containsKey(compoundKeyEntity2.getId()));
        Assert.assertThat(CkEntitys.size(), Is.is(2));
    }

    protected void createCkEntitys() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(compoundKeyEntity1);
                jpaRepository.create(compoundKeyEntity2);
                jpaRepository.create(compoundKeyEntity3);
            }
        });
    }

    @Test
    public void shouldCreateMultipleEntities() {
        deleteTestData(); //called to delete records created by before()

        final List<IdClassCompoundKeyEntity> CkEntitys = new ArrayList<>();
        CkEntitys.add(compoundKeyEntity1);
        CkEntitys.add(compoundKeyEntity2);

        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(CkEntitys);
            }
        });

        containsCkEntitys(CkEntitys);
        deleteTestData();

        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                jpaRepository.create(compoundKeyEntity1, compoundKeyEntity2);
            }
        });
        containsCkEntitys(CkEntitys);

        deleteTestData();
    }

    @Test
    public void shouldFindByAttribute(){
        IdClassCompoundKeyEntity beverage = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.find("name", "Entity1", 1).iterator().next();
            }
        });
        assertThat(beverage.getName(), is(compoundKeyEntity1.getName()));
        assertThat(beverage.getId(), is(compoundKeyEntity1.getId()));
        assertThat(beverage.isAlcoholic(), is(compoundKeyEntity1.isAlcoholic()));
    }

    @Test
    public void shouldExcludeOnFindByAttribute(){
        IdClassCompoundKeyEntity beverage = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 1).iterator().next();
            }
        });
        assertThat(beverage.getName(), not(compoundKeyEntity1.getName()));
        assertThat(beverage.getId(), not(compoundKeyEntity1.getId()));
        assertThat(beverage.isAlcoholic(), not(compoundKeyEntity1.isAlcoholic()));
    }

    @Test
    public void shouldFindMultipleRecordsByAttribute() {
        List<IdClassCompoundKeyEntity> beverages = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 10);
            }
        });
        boolean bev2Found = false;
        boolean bev3Found = false;
        for (IdClassCompoundKeyEntity beverage : beverages) {
            assertThat(beverage.isAlcoholic(), is(false));
            if (beverage.getId().equals(compoundKeyEntity2.getId())) {
                assertThat(beverage.getName(), is("Entity2"));
                bev2Found = true;
            }
            else if (beverage.getId().equals(compoundKeyEntity3.getId())) {
                assertThat(beverage.getName(), is("Entity3"));
                bev3Found = true;
            }
        }
        assertTrue(bev2Found && bev3Found);
    }

    @Test
    public void shouldLimitResultsOnFindByAttribute() {
        List<IdClassCompoundKeyEntity> beverages = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.find("alcoholic", false, 1);
            }
        });
        assertThat(beverages.size(), is(1));
    }

    @Test
    public void shouldFindOnMultipleAttributes(){
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("alcoholic", false);
        attrs.put("name", "Entity3");
        List<IdClassCompoundKeyEntity> beverages = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.find(attrs, 10);
            }
        });
        assertThat(beverages.size(), is(1));
        assertThat(beverages.get(0).getId(), is(compoundKeyEntity3.getId()));
        assertThat(beverages.get(0).getName(), is(compoundKeyEntity3.getName()));
        assertThat(beverages.get(0).isAlcoholic(), is(compoundKeyEntity3.isAlcoholic()));
    }

    @Test
    public void shouldFindOnMultipleAttributesWithSingleAttribute() {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("alcoholic", true);
        List<IdClassCompoundKeyEntity> beverages = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.find(attrs, 10);
            }
        });
        assertThat(beverages.size(), is(1));
        assertThat(beverages.get(0).getId(), is(compoundKeyEntity1.getId()));
        assertThat(beverages.get(0).getName(), is(compoundKeyEntity1.getName()));
        assertThat(beverages.get(0).isAlcoholic(), is(compoundKeyEntity1.isAlcoholic()));
    }

    @Test
    public void shouldListEntities() {
        List<IdClassCompoundKeyEntity> entities = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.list(5);
            }
        });

        assertThat(entities.size(), is(3));

        Map<CompoundKeyEntityId, IdClassCompoundKeyEntity> map = new HashMap<>();
        for (IdClassCompoundKeyEntity entity: entities) {
            map.put(entity.getId(), entity);
        }

        IdClassCompoundKeyEntity e1 = map.get(compoundKeyEntity1.getId());
        IdClassCompoundKeyEntity e2 = map.get(compoundKeyEntity2.getId());
        IdClassCompoundKeyEntity e3 = map.get(compoundKeyEntity3.getId());

        assertThat(e1.getId(), is (compoundKeyEntity1.getId()));
        assertThat(e1.getName(), is (compoundKeyEntity1.getName()));
        assertThat(e2.getId(), is (compoundKeyEntity2.getId()));
        assertThat(e2.getName(), is (compoundKeyEntity2.getName()));
        assertThat(e3.getId(), is (compoundKeyEntity3.getId()));
        assertThat(e3.getName(), is (compoundKeyEntity3.getName()));
    }
}
