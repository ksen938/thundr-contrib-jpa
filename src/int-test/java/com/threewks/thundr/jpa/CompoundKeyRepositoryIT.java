package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.IdClassCompoundKeyEntity;
import com.threewks.thundr.jpa.model.CompoundKeyEntityId;
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
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Metamodel;
import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 18/08/2015.
 */
public class CompoundKeyRepositoryIT {
    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, IdClassCompoundKeyEntity.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHibernate);

    private Jpa jpa;
    protected IdClassCompoundKeyEntity compoundKeyEntity1;
    protected IdClassCompoundKeyEntity compoundKeyEntity2;
    protected CrudRepository<CompoundKeyEntityId, IdClassCompoundKeyEntity> jpaRepository;

    @Before
    @SuppressWarnings(value = "unchecked")
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        compoundKeyEntity1 = new IdClassCompoundKeyEntity("Entity1");
        compoundKeyEntity2 = new IdClassCompoundKeyEntity("Entity2");
        jpaRepository = new CompoundKeyRepository(IdClassCompoundKeyEntity.class, jpa) {
            @Override
            protected CriteriaQuery<IdClassCompoundKeyEntity> createGetByKeyCriteria(CriteriaBuilder cb, Metamodel metamodel, List keys) {
                CriteriaQuery<IdClassCompoundKeyEntity> cq = cb.createQuery(entityType);
                Root<IdClassCompoundKeyEntity> entityRoot = cq.from(entityType);

                Path pk1Path = entityRoot.get("pk1");
                Path pk2Path = entityRoot.get("pk2");
                List<Predicate> andPredicates = new ArrayList<>();

                for (Object key : keys) {
                    CompoundKeyEntityId id = (CompoundKeyEntityId) key;
                    Predicate andPredicate = cb.and(cb.equal(pk1Path, id.getPk1()), cb.equal(pk2Path, id.getPk2()));
                    andPredicates.add(andPredicate);
                }

                cq.select(entityRoot).where(cb.or(andPredicates.toArray(new Predicate[andPredicates.size()])));
                return cq;
            }
        };
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
        final IdClassCompoundKeyEntity finalBev1 = compoundKeyEntity1;
        final IdClassCompoundKeyEntity finalBev2 = compoundKeyEntity2;
        IdClassCompoundKeyEntity queriedBevvie1 = jpa.run(new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return em.find(IdClassCompoundKeyEntity.class, finalBev1.getId());
            }
        });
        IdClassCompoundKeyEntity queriedBevvie2 = jpa.run(new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                return em.find(IdClassCompoundKeyEntity.class, finalBev2.getId());
            }
        });
        assertTrue(queriedBevvie1.getId().equals(compoundKeyEntity1.getId()));
        assertTrue(queriedBevvie2.getId().equals(compoundKeyEntity2.getId()));
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
        IdClassCompoundKeyEntity updatedCkEntity = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity>() {
            @Override
            public IdClassCompoundKeyEntity run(EntityManager em) {
                IdClassCompoundKeyEntity localCkEntity = jpaRepository.read(compoundKeyEntity1.getId());
                localCkEntity.setName("Water");
                return jpaRepository.update(localCkEntity);
            }
        });
        checkUpdated(updatedCkEntity);
    }

    protected void checkUpdated(IdClassCompoundKeyEntity CkEntity) {
        assertThat(CkEntity.getName(), Is.is("Water"));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        final IdClassCompoundKeyEntity[] CkEntitys = jpa.run(Propagation.Required, new ResultAction<IdClassCompoundKeyEntity[]>() {
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
        List<IdClassCompoundKeyEntity> CkEntitys1 = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.update(CkEntitys);
            }
        });
        List<IdClassCompoundKeyEntity> CkEntitys2 = jpa.run(Propagation.Required, new ResultAction<List<IdClassCompoundKeyEntity>>() {
            @Override
            public List<IdClassCompoundKeyEntity> run(EntityManager em) {
                return jpaRepository.update(Arrays.asList(CkEntitys));
            }
        });
        for (IdClassCompoundKeyEntity CkEntity : CkEntitys1) {
            checkUpdated(CkEntity);
        }
        for (IdClassCompoundKeyEntity CkEntity : CkEntitys2) {
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

        CkEntityListKeys.add(jpaRepository.getKey(compoundKeyEntity1));
        CkEntityListKeys.add(jpaRepository.getKey(compoundKeyEntity2));

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


}
