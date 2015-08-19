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
package com.threewks.thundr.jpa.deprecated.intercept;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.jpa.deprecated.EntityManagerRegistry;
import com.threewks.thundr.jpa.exception.JpaException;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.route.controller.Interceptor;

//TODO - Not finished

public class JpaSessionInterceptor implements Interceptor<JpaSession> {
	/**
	 * Default transaction isolation level means go with whatever the connection is currently set to.
	 */
	public static int DefaultTransactionIsolation = -1;

	private ThreadLocal<Integer> threadLocalOriginalTransactionIsolation;
	private EntityManagerRegistry entityManagerRegistry;

	public JpaSessionInterceptor(EntityManagerRegistry entityManagerRegistry) {
		this.entityManagerRegistry = entityManagerRegistry;
		this.threadLocalOriginalTransactionIsolation = new ThreadLocal<Integer>();
	}

	@Override
	public <T> T before(JpaSession annotation, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Logger.debug("Initializing entity manager.");
		EntityManager entityManager = getEntityManager(annotation);

		if (annotation.transactional()) {
			Logger.debug("Configuring transaction isolation level to: %s...", annotation.transactionIsolation());
			configureTransactionIsolation(entityManager, annotation.transactionIsolation());
			Logger.debug("Transaction isolation level configured.", annotation.transactionIsolation());

			Logger.debug("Beginning transaction...");
			entityManager.getTransaction().begin();
			Logger.debug("Inside transaction.");
		}
		return null;
	}

	@Override
	public <T> T after(JpaSession annotation, Object view, HttpServletRequest req, HttpServletResponse resp) {
		EntityManager entityManager = getEntityManager(annotation);
		try {
			if (annotation.transactional()) {
				Logger.debug("Committing transaction...");
				entityManager.getTransaction().commit();
				Logger.debug("Transaction committed.");
			}
		} finally {
			Logger.debug("Restoring transaction isolation level...");
			restoreDefaultTransactionIsolation(entityManager);
			Logger.debug("Transaction isolation level restored.");

			Logger.debug("Closing entity manager...");
			entityManager.close();
			Logger.debug("Entity manager closed.");
		}
		return null;
	}

	@Override
	public <T> T exception(JpaSession annotation, Exception e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		EntityManager entityManager = getEntityManager(annotation);
		try {
			if (annotation.transactional()) {
				Logger.error("Unchecked exception, rolling back transaction...");
				entityManager.getTransaction().rollback();
				Logger.debug("Transaction rolled back.");
			}
		} finally {
			Logger.debug("Restoring transaction isolation level...");
			restoreDefaultTransactionIsolation(entityManager);
			Logger.debug("Transaction isolation level restored.");

			Logger.debug("Closing entity manager...");
			entityManager.close();
			Logger.debug("Entity manager closed.");
		}
		return null;
	}

	private Connection getConnection(EntityManager entityManager) {
		return entityManager.unwrap(Connection.class);
	}

	protected EntityManager getEntityManager(JpaSession annotation) {
		String persistenceUnitName = annotation.persistenceUnit();
		return entityManagerRegistry.get(persistenceUnitName);
	}

	private void configureTransactionIsolation(EntityManager entityManager, int isolationLevel) {
		try {
			if (isolationLevel != DefaultTransactionIsolation) {
				Connection connection = getConnection(entityManager);
				threadLocalOriginalTransactionIsolation.set(connection.getTransactionIsolation());
				connection.setTransactionIsolation(isolationLevel);
			}
		} catch (SQLException e) {
			String message = "Error configuring transaction isolation level: %s";
			Logger.error(message, e.getMessage());
			throw new JpaException(e, message, e.getMessage());
		}
	}

	private void restoreDefaultTransactionIsolation(EntityManager entityManager) {
		Integer isolationLevel = threadLocalOriginalTransactionIsolation.get();
		if (isolationLevel != null) {
			try {
				Connection connection = getConnection(entityManager);
				connection.setTransactionIsolation(isolationLevel);
				threadLocalOriginalTransactionIsolation.set(null);
			} catch (SQLException e) {
				String message = "Error restoring transaction isolation level: %s";
				Logger.error(message, e.getMessage());
				throw new JpaException(e, message, e.getMessage());
			}
		}
	}
}
