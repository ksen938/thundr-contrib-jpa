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
package com.threewks.thundr.jpa.deprecated;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.threewks.thundr.jpa.deprecated.exception.PersistenceManagerDoesNotExistException;

import javax.persistence.EntityManager;

public class EntityManagerRegistryImpl implements EntityManagerRegistry {
	private ConcurrentMap<String, EntityManager> instances = new ConcurrentHashMap<String, EntityManager>();

	@Override
	public void register(String persistenceUnit, EntityManager entityManager) {
		instances.putIfAbsent(persistenceUnit, entityManager);
	}

	@Override
	public EntityManager get(String persistenceUnit) {
		EntityManager entityManager = instances.get(persistenceUnit);
		if (entityManager == null) {
			throw new PersistenceManagerDoesNotExistException("Persistence manager matching persistence unit %s not found", persistenceUnit);
		}
		return entityManager;
	}

	@Override
	public void clear() {
		for (EntityManager entityManager : instances.values()) {
			entityManager.close();
		}
		instances.clear();
	}
}
