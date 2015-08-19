package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;

public class IdClassCompoundKeyRepository<K,E> extends BaseRepository<K, E> {
    public IdClassCompoundKeyRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }
}
