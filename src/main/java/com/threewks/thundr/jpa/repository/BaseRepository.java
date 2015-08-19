package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.jpa.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

public class BaseRepository<K, E> implements CrudRepository<K, E> {

    protected Jpa jpa;
    protected Class<E> type;
    protected EntityType<E> entityType;

    public BaseRepository(Class<E> entityType, Jpa jpa) {
        this.type = entityType;
        this.jpa = jpa;
        this.entityType = jpa.getMetamodel().entity(type);


    }

    public BaseRepository(Class<E> entityType, Class<K> keyType, Jpa jpa) {
        this(entityType, jpa);
    }


    @Override
    public Long count() {
        return jpa.run(Propagation.Supports, new ResultAction<Long>() {
            @Override
            public Long run(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                cq.select(cb.count(cq.from(type)));
                return em.createQuery(cq).getSingleResult();
            }
        });
    }

    @Override
    public void create(final E entity) {
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                em.persist(entity);
            }
        });
    }

    @Override
    public void create(final List<E> entities) {
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                for (E entity : entities) {
                    em.persist(entity);
                }
            }
        });
    }

    @Override
    public void create(E... entities) {
        create(Arrays.asList(entities));
    }

    @Override
    public E update(final E entity) {
        return jpa.run(Propagation.Supports, new ResultAction<E>() {
            @Override
            public E run(EntityManager em) {
                return em.merge(entity);
            }
        });
    }

    @Override
    public List<E> update(final E... entities) {
        return update(Arrays.asList(entities));
    }

    @Override
    public List<E> update(final List<E> entities) {
        return jpa.run(Propagation.Supports, new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                List<E> returnList = new ArrayList<>();
                for (E entity : entities) {
                    returnList.add(em.merge(entity));
                }
                return returnList;
            }
        });
    }

    @Override
    public E read(final K key) {
        return jpa.run(Propagation.Supports, new ResultAction<E>() {
            @Override
            public E run(EntityManager em) {
                return em.find(type, key);
            }
        });
    }

    @Override
    public List<E> read(final K... keys) {
        return read(Arrays.asList(keys));
    }

    @Override
    public List<E> read(final List<K> keys) {
        return jpa.run(Propagation.Supports, new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                CriteriaQuery<E> cq = createGetByKeyCriteria(em.getCriteriaBuilder(), keys);
                return em.createQuery(cq).getResultList();
            }
        });
    }

    protected CriteriaQuery<E> createGetByKeyCriteria(CriteriaBuilder cb, List<K> keys) {
        String idField = getIdentifierField();
        CriteriaQuery<E> cq = cb.createQuery(type);
        Root<E> entityRoot = cq.from(type);
        cq.select(entityRoot).where(entityRoot.get(idField).in(keys));
        return cq;
    }


    @Override
    public void delete(final E entity) {
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(entity);
            }
        });
    }

    @Override
    public void deleteByKey(final K key) {
        jpa.run(Propagation.Supports, new Action() {
            @Override
            public void run(EntityManager em) {
                em.remove(em.getReference(type, key));
            }
        });
    }

    @Override
    public void delete(K... keys) {
        for (final K key : keys) {
            jpa.run(Propagation.Supports, new Action() {
                @Override
                public void run(EntityManager em) {
                    em.remove(em.getReference(type, key));
                }
            });
        }
    }

    private String getIdentifierField() {
        if (!entityType.hasSingleIdAttribute()) {
            throw new RepositoryException("Class %s has multiple ID fields, or has an @IdClass annotation which cannot be returned as a single attribute name.", this.entityType);
        }
        for (SingularAttribute<?, ?> attrib : entityType.getSingularAttributes()) {
            if (attrib.isId()) {
                return attrib.getName();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public K getKey(final E entity) {
        return jpa.run(Propagation.Supports, new ResultAction<K>() {
            @Override
            public K run(EntityManager em) {
                PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
                return (K) util.getIdentifier(entity);
            }
        });
    }
}
