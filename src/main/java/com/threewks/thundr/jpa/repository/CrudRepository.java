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

import java.util.List;
import java.util.Map;

/**
 * Describes a common set of CRUD operations to be implemented by a repository.
 * @param <K>
 * @param <E>
 */
public interface CrudRepository<K, E> {

    void create(E entity);
    void create(E...entities);
    void create(List<E> entities);

    E update(E entity);
    List<E> update(E...entities);
    List<E> update(List<E> entities);

    /**
     * Find entities based on a given non-primary key property (eg. find all users where username = '123'). An indexed
     * field is recommended for larger datasets.
     *
     * To find by primary key, use 'read' instead.
     *
     * @param key Name of the property
     * @param value Value of the property
     * @param limit Maximum number of entities to return
     * @return
     */
    List<E> find(String key, Object value, int limit);

    /**
     * Find entities based on a set of non-PK properties (eg. find all users where username = '123' and date of birth =
     * '01/01/2000'). Indexed fields are recommended for larger datasets.
     *
     * To find by primary key, use 'read' instead.
     *
     * @param properties Name/value pairs corresponding to the properties
     * @param limit Maximum number of entities to return
     * @return
     */
    List<E> find(Map<String, Object> properties, int limit);

    E read(K key);
    List<E> read(K...key);
    List<E> read(List<K> key);

    List<E> list(int limit);

    Long count();

    void delete(E entity);
    void delete(K...keys);
    void deleteByKey(K key);
}
