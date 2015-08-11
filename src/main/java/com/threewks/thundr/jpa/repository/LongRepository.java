package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;

public class LongRepository<E, Long> extends AbstractJpaRepository<E, Long> {
    public LongRepository(Jpa jpa) {
        super(jpa);
    }
}
