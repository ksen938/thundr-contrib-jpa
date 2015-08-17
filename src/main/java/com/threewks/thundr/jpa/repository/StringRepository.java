package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;

public class StringRepository<E> extends AbstractRepository<String, E> {
    public StringRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }
}
