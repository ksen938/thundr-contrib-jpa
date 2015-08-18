package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.repository.CrudRepository;
import com.threewks.thundr.jpa.repository.StringRepository;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class StringRepositoryIT {

    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, StringBeverage.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureHsql).around(configureHibernate);
    protected StringBeverage bevvie1;
    protected StringBeverage bevvie2;
    protected CrudRepository<String, StringBeverage> jpaRepository;
    private Jpa jpa;

    @Before
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        bevvie1 = new StringBeverage("Beer", true);
        bevvie2 = new StringBeverage("Lemonade", false);
        jpaRepository = new StringRepository<StringBeverage>(StringBeverage.class, jpa);
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
        final StringBeverage finalBev1 = bevvie1;
        final StringBeverage finalBev2 = bevvie2;
        StringBeverage queriedBevvie1 = jpa.run(new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return em.find(StringBeverage.class, finalBev1.getId());
            }
        });
        StringBeverage queriedBevvie2 = jpa.run(new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                return em.find(StringBeverage.class, finalBev2.getId());
            }
        });
        assertTrue(queriedBevvie1.getId().equals(bevvie1.getId()));
        assertTrue(queriedBevvie2.getId().equals(bevvie2.getId()));
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
        StringBeverage updatedBev = jpa.run(Propagation.Required, new ResultAction<StringBeverage>() {
            @Override
            public StringBeverage run(EntityManager em) {
                StringBeverage localBev = jpaRepository.read(bevvie1.getId());
                localBev.setName("Water");
                localBev.setAlcoholic(false);
                return jpaRepository.update(localBev);
            }
        });
        checkUpdated(updatedBev);
    }

    protected void checkUpdated(StringBeverage beverage) {
        assertThat(beverage.getName(), Is.is("Water"));
        assertThat(beverage.isAlcoholic(), Is.is(false));
    }

    @Test
    public void shouldUpdateMultipleEntities() {
        final StringBeverage[] beverages = jpa.run(Propagation.Required, new ResultAction<StringBeverage[]>() {
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
        List<StringBeverage> beverages1 = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.update(beverages);
            }
        });
        List<StringBeverage> beverages2 = jpa.run(Propagation.Required, new ResultAction<List<StringBeverage>>() {
            @Override
            public List<StringBeverage> run(EntityManager em) {
                return jpaRepository.update(Arrays.asList(beverages));
            }
        });
        for (StringBeverage beverage : beverages1) {
            checkUpdated(beverage);
        }
        for (StringBeverage beverage : beverages2) {
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

}
