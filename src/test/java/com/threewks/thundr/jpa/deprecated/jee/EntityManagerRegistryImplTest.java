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
package com.threewks.thundr.jpa.deprecated.jee;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class EntityManagerRegistryImplTest {
	/*@Rule
	public ExpectedException thrown = ExpectedException.none();

	private EntityManagerRegistry entityManagerRegistry;

	@Before
	public void before() {
		entityManagerRegistry = new EntityManagerRegistryImpl();
	}

	@Test
	public void shouldAddManagerToRegistry() {
		String persistenceUnit = "test";
		PersistenceManager persistenceManager = mock(PersistenceManager.class);

		entityManagerRegistry.register(persistenceUnit, persistenceManager);
		assertThat(entityManagerRegistry.get(persistenceUnit), is(notNullValue()));
		assertThat(entityManagerRegistry.get(persistenceUnit), is(persistenceManager));
	}

	@Test
	public void shouldClearRegistry() {
		thrown.expect(PersistenceManagerDoesNotExistException.class);

		String persistenceUnit = "test";

		entityManagerRegistry.register(persistenceUnit, mock(PersistenceManager.class));
		assertThat(entityManagerRegistry.get(persistenceUnit), is(notNullValue()));

		entityManagerRegistry.clear();
		entityManagerRegistry.get(persistenceUnit);
	}

	@Test
	public void shouldThrowErrorWhenPersistenceUnitNotRegistered() {
		thrown.expect(PersistenceManagerDoesNotExistException.class);

		entityManagerRegistry.get("not registered");
	}*/
}
