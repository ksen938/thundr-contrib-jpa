package com.threewks.thundr.jpa.repository;

import java.util.List;

public interface CrudRepository<E, K> {
    public void create(E entity);
    public void create(E... entities);
    public E update(E entity);
    public E read(K key);
    public List<E> readByEntityType(E entityType);
    public void count(E entityType);
    public void delete(E entity);
    public void deleteByKey(K key);
}
