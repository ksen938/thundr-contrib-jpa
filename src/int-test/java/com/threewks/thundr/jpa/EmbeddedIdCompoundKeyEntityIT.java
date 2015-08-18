package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.EmbeddedIdCompoundKeyEntity;
import com.threewks.thundr.jpa.model.CompoundKeyEntityId;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.repository.CompoundKeyRepository;
import com.threewks.thundr.jpa.repository.CrudRepository;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class EmbeddedIdCompoundKeyEntityIT {
    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, EmbeddedIdCompoundKeyEntity.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHibernate);
    protected EmbeddedIdCompoundKeyEntity compoundKeyEntity1;
    protected EmbeddedIdCompoundKeyEntity compoundKeyEntity2;
    protected CrudRepository<CompoundKeyEntityId, EmbeddedIdCompoundKeyEntity> jpaRepository;
    private Jpa jpa;

    @Before
    public void before() {
        this.jpa
                = injectionContext.get(Jpa.class);
        compoundKeyEntity1 = new EmbeddedIdCompoundKeyEntity("Entity1");
        compoundKeyEntity2 = new EmbeddedIdCompoundKeyEntity("Entity2");
        jpaRepository = new CompoundKeyRepository<>(EmbeddedIdCompoundKeyEntity.class, jpa);
        deleteTestData();
        createCkEntitys();
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(compoundKeyEntity1);
                em.remove(compoundKeyEntity2);
            }
        });
    }

    protected void shouldReturnPersistedObjects() {
        final EmbeddedIdCompoundKeyEntity finalBev1 = compoundKeyEntity1;
        final EmbeddedIdCompoundKeyEntity finalBev2 = compoundKeyEntity2;
        EmbeddedIdCompoundKeyEntity queriedBevvie1 = jpa.run(new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return em.find(EmbeddedIdCompoundKeyEntity.class, finalBev1.getId());
            }
        });
        EmbeddedIdCompoundKeyEntity queriedBevvie2 = jpa.run(new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return em.find(EmbeddedIdCompoundKeyEntity.class, finalBev2.getId());
            }
        });
        assertTrue(queriedBevvie1.getId().equals(compoundKeyEntity1.getId()));
        assertTrue(queriedBevvie2.getId().equals(compoundKeyEntity2.getId()));
    }

    @Test
    public void shouldCreateAndReadSingleEntity() {
        EmbeddedIdCompoundKeyEntity localCkEntity = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });
        assertThat(localCkEntity.getName(), Is.is(compoundKeyEntity1.getName()));
    }

    @Test
    public void shouldUpdateSingleEntity() {
        EmbeddedIdCompoundKeyEntity updatedCkEntity = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                EmbeddedIdCompoundKeyEntity localCkEntity = jpaRepository.read(compoundKeyEntity1.getId());
                localCkEntity.setName("Water");
                return jpaRepository.update(localCkEntity);
            }
        });
        checkUpdated(updatedCkEntity);
    }

    protected void checkUpdated(EmbeddedIdCompoundKeyEntity CkEntity) {
        assertThat(CkEntity.getName(), Is.is("Water"));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        final EmbeddedIdCompoundKeyEntity[] CkEntitys = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity[]>() {
            @Override
            public EmbeddedIdCompoundKeyEntity[] run(EntityManager em) {
                EmbeddedIdCompoundKeyEntity localCkEntity1 = jpaRepository.read(compoundKeyEntity1.getId());
                EmbeddedIdCompoundKeyEntity localCkEntity2 = jpaRepository.read(compoundKeyEntity2.getId());
                localCkEntity1.setName("Water");
                localCkEntity2.setName("Water");
                EmbeddedIdCompoundKeyEntity[] CkEntitys = new EmbeddedIdCompoundKeyEntity[2];
                CkEntitys[0] = localCkEntity1;
                CkEntitys[1] = localCkEntity2;
                return CkEntitys;
            }
        });
        List<EmbeddedIdCompoundKeyEntity> CkEntitys1 = jpa.run(Propagation.Required, new ResultAction<List<EmbeddedIdCompoundKeyEntity>>() {
            @Override
            public List<EmbeddedIdCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.update(CkEntitys);
            }
        });
        List<EmbeddedIdCompoundKeyEntity> CkEntitys2 = jpa.run(Propagation.Required, new ResultAction<List<EmbeddedIdCompoundKeyEntity>>() {
            @Override
            public List<EmbeddedIdCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.update(Arrays.asList(CkEntitys));
            }
        });
        for (EmbeddedIdCompoundKeyEntity CkEntity : CkEntitys1) {
            checkUpdated(CkEntity);
        }
        for (EmbeddedIdCompoundKeyEntity CkEntity : CkEntitys2) {
            checkUpdated(CkEntity);
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
        EmbeddedIdCompoundKeyEntity deletedBev = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });

        EmbeddedIdCompoundKeyEntity remainingBev = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity2.getId());
            }
        });

        assertThat(deletedBev, Is.is(nullValue()));
        assertThat(remainingBev.getId(), Is.is(compoundKeyEntity2.getId()));
    }

    protected void testAllDeleted() {
        EmbeddedIdCompoundKeyEntity deletedBev1 = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity1.getId());
            }
        });

        EmbeddedIdCompoundKeyEntity deletedBev2 = jpa.run(Propagation.Required, new ResultAction<EmbeddedIdCompoundKeyEntity>() {
            @Override
            public EmbeddedIdCompoundKeyEntity run(EntityManager em) {
                return jpaRepository.read(compoundKeyEntity2.getId());
            }
        });

        assertThat(deletedBev1, Is.is(nullValue()));
        assertThat(deletedBev2, Is.is(nullValue()));
    }

    @Test
    public void shouldReadMultipleEntities() {

        List<EmbeddedIdCompoundKeyEntity> CkEntityList1 = jpa.run(Propagation.Required, new ResultAction<List<EmbeddedIdCompoundKeyEntity>>() {
            @Override
            public List<EmbeddedIdCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.read(jpaRepository.getKey(compoundKeyEntity1), jpaRepository.getKey(compoundKeyEntity2));
            }
        });

        List<CompoundKeyEntityId> CkEntityListKeys = new ArrayList<>();

        CkEntityListKeys.add(jpaRepository.getKey(compoundKeyEntity1));
        CkEntityListKeys.add(jpaRepository.getKey(compoundKeyEntity2));

        final List<CompoundKeyEntityId> finalCkEntityListKeys = CkEntityListKeys;

        List<EmbeddedIdCompoundKeyEntity> CkEntityList2 = jpa.run(Propagation.Required, new ResultAction<List<EmbeddedIdCompoundKeyEntity>>() {
            @Override
            public List<EmbeddedIdCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.read(finalCkEntityListKeys);
            }
        });

        containsCkEntitys(CkEntityList1);
        containsCkEntitys(CkEntityList2);
    }

    protected void containsCkEntitys(List<EmbeddedIdCompoundKeyEntity> CkEntitys) {
        Map<CompoundKeyEntityId, EmbeddedIdCompoundKeyEntity> map = new HashMap<>();
        for (EmbeddedIdCompoundKeyEntity CkEntity : CkEntitys) {
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
            }
        });
    }

    @Test
    public void shouldCreateMultipleEntities() {
        deleteTestData(); //called to delete records created by before()

        final List<EmbeddedIdCompoundKeyEntity> CkEntitys = new ArrayList<>();
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


}
