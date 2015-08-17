package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;

public class LongRepository<E> extends AbstractRepository<Long, E> {
    public LongRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }
}