package com.threewks.thundr.jpa.repository;

import java.util.List;

public interface CrudRepository<K, E> {

    void create(E entity);
    void create(E...entities);
    void create(List<E> entities);

    E update(E entity);
    List<E> update(E...entities);
    List<E> update(List<E> entities);

    E read(K key);
    List<E> read(K...key);
    List<E> read(List<K> key);

    Long count();

    void delete(E entity);
    void delete(K...keys);
    void deleteByKey(K key);
}
