package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.AbstractJpaTest;
import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.model.Beverage;
import com.threewks.thundr.jpa.model.LongKeyBeverage;

/**
 * Created by kaushiksen on 17/08/2015.
 */
public class LongKeyJpaRepositoryIT extends AbstractJpaTest<Long, LongKeyBeverage> {
    public LongKeyJpaRepositoryIT() {
        super(new LongKeyBeverage("Beer", true), new LongKeyBeverage("Lemonade", false));
    }

    @Override
    protected JpaRepository<Long, LongKeyBeverage> setupJpaRepository(Jpa jpa) {
        return new JpaRepository<>(LongKeyBeverage.class, jpa);
    }
}
