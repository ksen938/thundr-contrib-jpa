package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.model.CompoundKeyEntityId;

/**
 * Created by kaushiksen on 18/08/2015.
 */

public class CompoundKeyRepository<E> extends BaseRepository<CompoundKeyEntityId, E> {
    public CompoundKeyRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }
}
