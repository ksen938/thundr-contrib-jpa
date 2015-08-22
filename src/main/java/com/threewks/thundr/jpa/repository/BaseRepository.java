/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.jpa.Action;
import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.Propagation;
import com.threewks.thundr.jpa.ResultAction;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.util.*;

public class BaseRepository<K, E> implements CrudRepository<K, E> {

    protected Jpa jpa;
    protected Class<E> type;
    protected Class<K> keyType;
    protected EntityType<E> entityType;
    private final Predicate[] predicateArray = new Predicate[0];

    public BaseRepository(Class<E> entityType, Jpa jpa) {
        this.type = entityType;
        this.jpa = jpa;
        this.entityType = jpa.getMetamodel().entity(type);
    }

    public BaseRepository(Class<E> entityType, Class<K> keyType, Jpa jpa) {
        this.type = entityType;
        this.jpa = jpa;
        this.entityType = jpa.getMetamodel().entity(type);
        this.keyType = keyType;
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
                List<E> updatedEntities = new ArrayList<>();
                for (E entity : entities) {
                    updatedEntities.add(em.merge(entity));
                }
                return updatedEntities;
            }
        });
    }

    @Override
    public List<E> find(final String key, final Object value, final int limit) {
        return jpa.run(Propagation.Supports, new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<E> cq = cb.createQuery(type);
                Root<E> fromClause = cq.from(entityType);
                cq.select(fromClause).where(cb.equal(fromClause.get(key), value));
                return em.createQuery(cq).setMaxResults(limit).getResultList();
            }
        });
    }

    @Override
    public List<E> find(final Map<String, Object> properties, final int limit) {
        if (properties.size() == 1 ) {
            Map.Entry<String, Object> entry = properties.entrySet().iterator().next();
            return find(entry.getKey(), entry.getValue(), limit);
        }

        return jpa.run(Propagation.Supports, new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<E> cq = cb.createQuery(type);
                Root<E> fromClause = cq.from(entityType);
                List<Predicate> andPredicates = new ArrayList<>(properties.size());
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    andPredicates.add(cb.and(cb.equal(fromClause.get(entry.getKey()), entry.getValue())));
                }
                cq.select(fromClause).where(andPredicates.toArray(predicateArray));
                return em.createQuery(cq).setMaxResults(limit).getResultList();
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

    @Override
    public List<E> read(final List<K> keys) {
        return jpa.run(Propagation.Supports, new ResultAction<List<E>>() {
            @Override
            public List<E> run(EntityManager em) {
                CriteriaQuery<E> cq = createReadByKeysCriteria(em.getCriteriaBuilder(), keys);
                return em.createQuery(cq).getResultList();
            }
        });
    }

    protected CriteriaQuery<E> createReadByKeysCriteria(CriteriaBuilder cb, List<K> keys) {
        CriteriaQuery<E> cq = cb.createQuery(type);
        Root<E> fromClause = cq.from(entityType);
        boolean isSimpleQuery = entityType.hasSingleIdAttribute();
        return isSimpleQuery ? buildSimpleQuery(cb, cq, fromClause, keys) : buildComplexQuery(cb, cq, fromClause, keys);
    }

    protected CriteriaQuery<E> buildSimpleQuery(CriteriaBuilder cb, CriteriaQuery<E> cq, Root<E> fromClause, List<K> keys) {
        SingularAttribute<? super E, K> idField = entityType.getId(keyType);
        cq.select(fromClause).where(fromClause.get(idField).in(keys));
        return cq;
    }

    protected CriteriaQuery<E> buildComplexQuery(CriteriaBuilder cb, CriteriaQuery<E> cq, Root<E> fromClause, List<K> keys) {
        Map<Object, Map<Path, Object>> keyValues = setupMapFromKeysToPathsAndValues(keys);
        Map<SingularAttribute<? super E, ?>, Path> attrsAndPaths = mapAttributesToPaths(fromClause);
        fillOutMapFromKeysToPathsAndValues(keys, keyValues, attrsAndPaths);
        List<Predicate> andPredicates = buildAndPredicates(cb, keyValues);
        cq.select(fromClause).where(cb.or(andPredicates.toArray(predicateArray)));
        return cq;
    }

    private Map<Object, Map<Path, Object>> setupMapFromKeysToPathsAndValues(List<K> keys) {
        Map<Object, Map<Path, Object>> keyValues = new LinkedHashMap<>();
        for (Object key : keys) {
            keyValues.put(key, new HashMap<Path, Object>());
        }
        return keyValues;
    }

    private Map<SingularAttribute<? super E, ?>, Path> mapAttributesToPaths(Root<E> fromClause) {
        Map<SingularAttribute<? super E, ?>, Path> attrsAndPaths = new LinkedHashMap<>();
        for (SingularAttribute<? super E, ?> attr : entityType.getIdClassAttributes()) {
            attrsAndPaths.put(attr, fromClause.get(attr));
        }
        return attrsAndPaths;
    }

    private void fillOutMapFromKeysToPathsAndValues(List<K> keys, Map<Object, Map<Path, Object>> keyValues, Map<SingularAttribute<? super E, ?>, Path> attrsAndPaths) {
        try {
            for (Map.Entry<SingularAttribute<? super E, ?>, Path> keyField : attrsAndPaths.entrySet()) {
                SingularAttribute<? super E, ?> attr = keyField.getKey();
                Path path = keyField.getValue();
                Field f = (Field) attr.getJavaMember();
                for (Object key : keys) {
                    Object value = f.get(key);
                    keyValues.get(key).put(path, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new BaseException(e);
        }
    }

    private List<Predicate> buildAndPredicates(CriteriaBuilder cb, Map<Object, Map<Path, Object>> keyValues) {
        List<Predicate> andPredicates = new ArrayList<>();
        for (Map<Path, Object> pkFields : keyValues.values()) {
            List<Predicate> terms = new ArrayList<>(pkFields.size());
            for (Map.Entry<Path, Object> pkField : pkFields.entrySet()) {
                terms.add(cb.equal(pkField.getKey(), pkField.getValue()));
            }
            andPredicates.add(cb.and(terms.toArray(predicateArray)));
        }
        return andPredicates;
    }
}
