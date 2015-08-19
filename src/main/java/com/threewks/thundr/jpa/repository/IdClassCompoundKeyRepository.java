package com.threewks.thundr.jpa.repository;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.jpa.Jpa;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.util.*;

public class IdClassCompoundKeyRepository<K,E> extends BaseRepository<K, E> {
    private final Predicate[] predicateArray = new Predicate[0];

    public IdClassCompoundKeyRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, jpa);
    }

    @Override
    protected CriteriaQuery<E> createGetByKeyCriteria(CriteriaBuilder cb, List<K> keys) {

        try {
            CriteriaQuery<E> cq = cb.createQuery(type);
            Root<E> fromClause = cq.from(entityType);

            Map<Object, Map<Path, Object>> keyValues = setupMapFromKeysToPathsAndValues(keys);

            Map<SingularAttribute<? super E, ?>, Path> attrsAndPaths = mapAttributesToPaths(fromClause);

            fillOutMapFromKeysToPathsAndValues(keys, keyValues, attrsAndPaths);

            List<Predicate> andPredicates = buildAndPredicates(cb, keyValues);

            cq.select(fromClause).where(cb.or(andPredicates.toArray(predicateArray)));
            return cq;
        } catch (IllegalAccessException e) {
            throw new BaseException(e);
        }
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

    private void fillOutMapFromKeysToPathsAndValues(List<K> keys, Map<Object, Map<Path, Object>> keyValues, Map<SingularAttribute<? super E, ?>, Path> attrsAndPaths) throws IllegalAccessException {
        for (Map.Entry<SingularAttribute<? super E, ?>, Path> keyField : attrsAndPaths.entrySet()) {
            SingularAttribute<? super E, ?> attr = keyField.getKey();
            Path path = keyField.getValue();
            Field f = (Field) attr.getJavaMember();
            for (Object key : keys) {
                Object value = f.get(key);
                keyValues.get(key).put(path, value);
            }
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
