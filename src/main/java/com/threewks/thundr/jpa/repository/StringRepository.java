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

import com.threewks.thundr.jpa.Jpa;

/**
 * Base repository class for entities that have a String primary key (eg. UUID)
 * @param <E> Entity type managed by this repository
 */
public class StringRepository<E> extends BaseRepository<String, E> {
    public StringRepository(Class<E> entityType, Jpa jpa) {
        super(entityType, String.class, jpa);
    }
}
