package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.jpa.Action;
import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.JpaImpl;
import com.threewks.thundr.jpa.ResultAction;
import org.reflections.Reflections;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.PersistenceUnitUtil;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class JpaRepository<E> implements CrudRepository<K, E> {

    protected Jpa jpa;
    protected Class<E> entityType;

    public JpaRepository(Class<E> entityType, Jpa jpa) {
        this.entityType = entityType;
        this.jpa = jpa;

    }

    @Override
    public Long count() {
        return jpa.run(new ResultAction<Long>() {
            @Override
            public Long run(EntityManager em) {
                return (Long) em.createQuery("SELECT count(*) FROM " + entityType.getName()).getSingleResult();
            }
        });
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
    public void create(final List<E> entities) {
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                for (E entity : entities) {
                    em.persist(entity);
                }
            }
        });
    }

    @Override
    public E update(final E entity) {
        return jpa.run(new ResultAction<E>() {
            @Override
            public E run(EntityManager em) {
                return em.merge(entity);
            }
        });
    }

    @Override
    public E read(K key) {
        return jpa.run(new ResultAction<E>() {
            @Override
            public E run(EntityManager em) {
                return em.find(entityType, id);
            }
        });
    }

    @Override
    public List<E> readAll() {
        return jpa.run(new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                return em.createQuery("SELECT a FROM " + entityType.getName() + " a", entityType).getResultList();
            }
        });
    }

    @Override
    public void delete(final E entity) {
        jpa.run(new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(entity);
            }
        });
    }
}
