package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;

/**
 * Created by kaushiksen on 18/08/2015.
 */

public class EmbeddedIdCompoundKeyRepository<K,E> extends BaseRepository<K, E> {
    public EmbeddedIdCompoundKeyRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }
}
