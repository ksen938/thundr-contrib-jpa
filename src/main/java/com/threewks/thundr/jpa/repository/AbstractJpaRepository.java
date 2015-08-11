package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.Action;
import com.threewks.thundr.jpa.Jpa;

import javax.persistence.EntityManager;
import java.util.List;

public abstract class AbstractJpaRepository<E, K> implements CrudRepository<E, K> {

    private Jpa jpa;

    public AbstractJpaRepository(Jpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void count(E entityType) {
    }

    @Override
    public void create(final E entity) {
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(entity);
            }
        });
    }

    @Override
    public void create(final E... entities) {
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                for (E entity:entities) {
                    em.persist(entity);
                }
            }
        });
    }

    @Override
    public E update(E entity) {
        return null;
    }

    @Override
    public E read(K key) {
        return null;
    }

    @Override
    public List<E> readByEntityType(E entityType) {
        return null;
    }

    @Override
    public void delete(E entity) {

    }

    @Override
    public void deleteByKey(K key) {

    }
}
