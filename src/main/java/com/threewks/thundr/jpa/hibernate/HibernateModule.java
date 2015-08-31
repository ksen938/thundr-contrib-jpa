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
package com.threewks.thundr.jpa.hibernate;

import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.Jpa;
import com.threewks.thundr.jpa.JpaImpl;
import com.threewks.thundr.jpa.JpaUnsafe;
import com.threewks.thundr.jpa.intercept.Transactional;
import com.threewks.thundr.jpa.intercept.TransactionalInterceptor;
import com.threewks.thundr.route.controller.InterceptorRegistry;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.AttributeConverter;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * Configures a basic, persistence.xml free Hibernate SessionFactory (for pure hibernate use) and
 * EntityManagerFactory (for JPA2 use).
 *
 * Configuration is obtained from a {@link HibernateConfig} configuration object which must be configured before applications start.
 */
public class HibernateModule extends BaseModule {
	private WeakHashMap<String, SessionFactory> sessionFactories = new WeakHashMap<>();
	private WeakHashMap<String, EntityManagerFactory> entityManagerFactories = new WeakHashMap<>();

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		HibernateConfig unit = injectionContext.get(HibernateConfig.class);
		createAndInjectSessionFactoryAndEntityManagerFactoryForPersistenceUnit(injectionContext, unit);
	}

	protected void createAndInjectSessionFactoryAndEntityManagerFactoryForPersistenceUnit(UpdatableInjectionContext injectionContext, HibernateConfig unit) {
		Configuration configuration = createHibernateConfiguration(unit);

		StandardServiceRegistry serviceRegistry = createServiceRegistry(configuration);
		SessionFactory sessionFactory = createSessionFactory(configuration, serviceRegistry);

		// LocalSessionFactoryBuilder from Spring ORM is a world of interesting low level hacks
		// If you need to further customise hibernate, check it out and hack away
		// http://grepcode.com/file_/repo1.maven.org/maven2/org.springframework/spring-orm/4.1.6.RELEASE/org/springframework/orm/hibernate4/LocalSessionFactoryBuilder.java/?v=source

		EntityManagerFactory entityManagerFactory = createEntityManagerFactory(unit, configuration, serviceRegistry);
		sessionFactories.put(unit.getName(), sessionFactory);
		entityManagerFactories.put(unit.getName(), entityManagerFactory);
		injectionContext.inject(sessionFactory).named(unit.getName()).as(SessionFactory.class);
		injectionContext.inject(entityManagerFactory).named(unit.getName()).as(EntityManagerFactory.class);
		JpaImpl jpa = new JpaImpl(entityManagerFactory);
		injectionContext.inject(jpa).as(Jpa.class);
		injectionContext.inject(jpa).as(JpaUnsafe.class);
	}

	protected EntityManagerFactory createEntityManagerFactory(HibernateConfig unit, Configuration configuration, StandardServiceRegistry serviceRegistry) {
		return new EntityManagerFactoryImpl(unit.getTransactionType(), unit.isReleaseResourcesOnCloseEnabled(), unit.getSessionInterceptorClass(), configuration,
                    serviceRegistry, unit.getName());
	}

	protected Configuration createHibernateConfiguration(HibernateConfig config) {
		Configuration configuration = new Configuration();
		for (Class<?> entityClass : config.getEntityClasses()) {
			configuration.addAnnotatedClass(entityClass);
		}
		for (Class<? extends AttributeConverter<?, ?>> converter : config.getAttributeConvertors()) {
			configuration.addAttributeConverter(converter);
		}
		Properties cfgProperties = configuration.getProperties();
		cfgProperties.put(Environment.DATASOURCE, config.getDataSource());

		cfgProperties.putAll(config.getProperties());
		return configuration;
	}

	@Override
	public void start(UpdatableInjectionContext injectionContext) {
		super.start(injectionContext);

		InterceptorRegistry interceptorRegistry = injectionContext.get(InterceptorRegistry.class);
		if (interceptorRegistry != null) {
			interceptorRegistry.registerInterceptor(Transactional.class, new TransactionalInterceptor(injectionContext.get(JpaUnsafe.class)));
		}
	}

	@Override
	public void stop(InjectionContext injectionContext) {
		super.stop(injectionContext);

		injectionContext.get(JpaUnsafe.class).shutdown();

		for (EntityManagerFactory factory : entityManagerFactories.values()) {
			factory.close();
		}
		for (SessionFactory sessionFactory : sessionFactories.values()) {
			sessionFactory.close();
		}
	}

	protected StandardServiceRegistry createServiceRegistry(Configuration configuration) {
		return new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
	}

	protected SessionFactory createSessionFactory(Configuration configuration, ServiceRegistry serviceRegistry) {
		return configuration.buildSessionFactory(serviceRegistry);
	}

}
