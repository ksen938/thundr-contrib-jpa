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
package com.threewks.thundr.jpa;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;
import java.util.concurrent.Callable;

/**
 * Defines a set of standardised closures  that enable the user to execute transactions against a JPA provider (eg. Hibernate).
 *
 * Note that this is a 'safe' abstraction of JPA that largely handles low-level transaction management.
 * There is a {@link JpaUnsafe} class that enables development of customised transaction handlers
 * (eg. {@link com.threewks.thundr.jpa.intercept.Transactional}
 */
public interface Jpa {

	/**
	 * Executes the specified action without a return value. Propagation is set to default of 'Required'.
	 * @param action
	 */
	public void run(Action action);

	/**
	 * Executes the specified action without a return value. Propagation options are {@link Propagation}
	 * @param propagation
	 * @param action
	 */
	public void run(Propagation propagation, Action action);

	//public void run(Propagation propagation, Runnable runnable);
	//public <R> R run(Propagation propagation, Callable<R> runnable);

	/**
	 * Executes the specified action with a return value. Propagation is set to default of 'Required'.
	 * @param action
	 * @param <R>
	 * @return The return type specified
	 */
	public <R> R run(ResultAction<R> action);

	/**
	 * Executes the specified action with a return value. Propagation options are {@link Propagation}.
	 * @param action
	 * @param <R>
	 * @return The return type specified
	 */
	public <R> R run(Propagation propagation, ResultAction<R> action);

	/**
	 * Provides a metamodel that enables the development of custom queries using JPQL or the Criteria API
	 *
	 * @return metamodel containing all the entities managed by this entity manager
	 */
	public Metamodel getMetamodel();
}
