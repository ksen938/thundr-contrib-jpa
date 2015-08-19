package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;


public class EmbeddedIdCompoundKeyRepository<K,E> extends BaseRepository<K, E> {
    public EmbeddedIdCompoundKeyRepository(Class<E> entityType, Class<K> keyType, Jpa jpa) {
        super(entityType, keyType, jpa);
    }
}
