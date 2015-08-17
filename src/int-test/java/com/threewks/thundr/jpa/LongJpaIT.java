package com.threewks.thundr.jpa;

import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.repository.CrudRepository;
import com.threewks.thundr.jpa.repository.LongRepository;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import java.util.*;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class LongJpaIT extends AbstractJpaIT {

    protected LongBeverage bevvie1;
    protected LongBeverage bevvie2;
    protected CrudRepository<Long, LongBeverage> jpaRepository;

    @Before
    public void before() {
        super.before();
        bevvie1 = new LongBeverage("Beer", true);
        bevvie2 = new LongBeverage("Lemonade", false);
        jpaRepository = new LongRepository<LongBeverage>(LongBeverage.class, jpa);
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

    protected void checkUpdated(LongBeverage beverage) {
        assertThat(beverage.getName(), Is.is("Water"));
        assertThat(beverage.isAlcoholic(), Is.is(false));
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
