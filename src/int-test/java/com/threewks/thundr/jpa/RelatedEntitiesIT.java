package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.jpa.model.LongBeverage;
import com.threewks.thundr.jpa.model.StringBeverage;
import com.threewks.thundr.jpa.repository.CrudRepository;
import com.threewks.thundr.jpa.repository.LongRepository;
import com.threewks.thundr.jpa.repository.StringRepository;
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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class RelatedEntitiesIT {
    public InjectionContextImpl injectionContext = new InjectionContextImpl();

    public ConfigureHsql configureHsql = new ConfigureHsql(injectionContext);
    public ConfigureMysql configureMysql = new ConfigureMysql(injectionContext);
    public ConfigureHikari configureHikari = new ConfigureHikari(injectionContext);
    public ConfigureHibernate configureHibernate = new ConfigureHibernate(injectionContext, LongBeverage.class, StringBeverage.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public RuleChain chain = RuleChain.outerRule(configureMysql).around(configureHikari).around(configureHibernate);

    private LongBeverage longBevvie1;
    private LongBeverage longBevvie2;
    private StringBeverage stringBevvie1;
    private StringBeverage stringBevvie2;
    private CrudRepository<Long, LongBeverage> longBeverageRepo;
    private CrudRepository<String, StringBeverage> stringBeverageRepo;
    private Jpa jpa;

    @Before
    public void before() {
        jpa = injectionContext.get(Jpa.class);
        longBevvie1 = new LongBeverage("Beer", true);
        longBevvie2 = new LongBeverage("Lemonade", false);
        stringBevvie1 = new StringBeverage("Scotch", true);
        stringBevvie2 = new StringBeverage("Soda", false);
        longBeverageRepo = new LongRepository<>(LongBeverage.class, jpa);
        stringBeverageRepo = new StringRepository<>(StringBeverage.class, jpa);
        deleteTestData();
        createBeverages();
    }

    protected void deleteTestData() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(longBevvie1);
                em.remove(longBevvie2);
                em.remove(stringBevvie1);
                em.remove(stringBevvie2);
            }
        });
    }

    protected void createBeverages() {
        jpa.run(Propagation.Required, new Action() {
            @Override
            public void run(EntityManager em) {
                longBeverageRepo.create(longBevvie1);
                longBeverageRepo.create(longBevvie2);
                stringBevvie1.setLongBeverage(longBevvie1);
                stringBevvie2.setLongBeverage(longBevvie1);
                stringBeverageRepo.create(stringBevvie1);
                stringBeverageRepo.create(stringBevvie2);
            }
        });
    }

    @Test
    public void shouldTraverseOneToManyRelationship() {
        LongBeverage longBeverage = longBeverageRepo.read(longBevvie1.getId());
        List<StringBeverage> stringBeverages = longBeverage.getStringBeverages();
        for (StringBeverage stringBeverage: stringBeverages) {
            if (stringBeverage.getId().equals(stringBevvie1.getId())) {
                assertThat(stringBeverage.getId(), is(stringBevvie1.getId()));
                assertThat(stringBeverage.getName(), is(stringBevvie1.getName()));
                assertThat(stringBeverage.isAlcoholic(), is(stringBevvie1.isAlcoholic()));
            }
            else {
                assertThat(stringBeverage.getId(), is(stringBevvie2.getId()));
                assertThat(stringBeverage.getName(), is(stringBevvie2.getName()));
                assertThat(stringBeverage.isAlcoholic(), is(stringBevvie2.isAlcoholic()));
            }
        }
    }

    @Test
    public void shouldTraverseManyToOneRelationship() {
        StringBeverage stringBeverage = stringBeverageRepo.read(stringBevvie1.getId());
        LongBeverage longBeverage = stringBeverage.getLongBeverage();
        assertThat(longBeverage.getId(), is(longBevvie1.getId()));
        assertThat(longBeverage.getName(), is(longBevvie1.getName()));
        assertThat(longBeverage.isAlcoholic(), is(longBevvie1.isAlcoholic()));
    }
}
