package com.threewks.thundr.jpa;

import com.threewks.thundr.jpa.model.LongKeyBeverage;
import com.threewks.thundr.jpa.model.StringKeyBeverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.junit.Before;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class StringKeyJpaImplIT extends AbstractJpaTest<String, StringKeyBeverage> {

    public StringKeyJpaImplIT() {
        super(new StringKeyBeverage("Beer", true), new StringKeyBeverage("Lemonade", false));
    }

    @Override
    protected JpaRepository<String, StringKeyBeverage> setupJpaRepository(Jpa jpa) {
        return new JpaRepository<>(StringKeyBeverage.class, jpa);
    }

    @Before
    public void before() {
        super.before();
    }
}
