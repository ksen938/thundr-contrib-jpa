package com.threewks.thundr.jpa.repository;

import java.util.List;

public interface CrudRepository<K, E> {
    /*
    public void create(E entity);
    public void create(List<E> entities);
    public E update(E entity);
    public E read(E entity); //change to key
    public List<E> readAll();
    public Long count();
    public void delete(E entity);
*/
    public void create(E entity);
    public void create(E...entities);
    public void create(List<E> entities);
    public E update(E entity);
    public List<E> update(E...entities);
    public List<E> update(List<E> entities);

    public E read(K key); //change to key
    public List<E> read(E...key); //change to key
    public List<E> read(List<K> key); //change to key

    public Long count();
    public void delete(E entity);
    public void deleteByKey(K key);
}
