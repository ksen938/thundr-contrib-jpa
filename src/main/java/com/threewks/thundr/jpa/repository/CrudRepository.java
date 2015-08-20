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

public interface CrudRepository<K, E> {

    void create(E entity);
    void create(E...entities);
    void create(List<E> entities);

    void update(E entity);
    void update(E...entities);
    void update(List<E> entities);

    E read(K key);
    List<E> read(K...key);
    List<E> read(List<K> key);

    Long count();

    void delete(E entity);
    void delete(K...keys);
    void deleteByKey(K key);
}
