package com.threewks.thundr.jpa;

import com.threewks.thundr.jpa.model.LongKeyBeverage;
import com.threewks.thundr.jpa.repository.JpaRepository;
import org.junit.Before;

public class LongKeyJpaImplIT extends AbstractJpaTest<Long, LongKeyBeverage> {

    public LongKeyJpaImplIT() {
        super(new LongKeyBeverage("Beer", true), new LongKeyBeverage("Lemonade", false));
    }

    @Override
    protected JpaRepository<Long, LongKeyBeverage> setupJpaRepository(Jpa jpa) {
        return new JpaRepository<>(LongKeyBeverage.class, jpa);
    }

    @Before
    public void before() {
        super.before();
    }
}
