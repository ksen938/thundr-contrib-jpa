package com.threewks.thundr.jpa.hibernate;

import org.hibernate.Interceptor;

import javax.persistence.AttributeConverter;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.util.*;

/**
 * Configure Hibernate for use in Thundr
 */

public class HibernateConfig {
	protected String name = "default";
	protected DataSource dataSource;
	protected Map<String, String> properties = new HashMap<>();
	protected List<Class<?>> entities = new ArrayList<>();
	protected PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
	protected boolean releaseResourcesOnCloseEnabled = false;
	protected Class<? extends Interceptor> sessionInterceptorClass;
	protected List<Class<? extends AttributeConverter<?, ?>>> convertors = new ArrayList<>();

	//Hibernate resolves Dialect automatically for common databases
	public HibernateConfig(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public HibernateConfig withName(String name) {
		this.name = name;
		return this;
	}

	public HibernateConfig withProperty(String key, String value) {
		this.properties.put(key, value);
		return this;
	}

	public HibernateConfig withProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
		return this;
	}

	public HibernateConfig withEntity(Class<?> entity) {
		this.entities.add(entity);
		return this;
	}

	public HibernateConfig withEntities(Class<?>... entities) {
		this.entities.addAll(Arrays.asList(entities));
		return this;
	}

	public HibernateConfig withTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
		return this;
	}

	public HibernateConfig withConvertor(Class<? extends AttributeConverter<?, ?>> convertor) {
		this.convertors.add(convertor);
		return this;
	}

	@SuppressWarnings("unchecked")
	public HibernateConfig withConvertors(Class<? extends AttributeConverter<?, ?>>... convertors) {
		this.convertors.addAll(Arrays.asList(convertors));
		return this;
	}

	public HibernateConfig withReleaseResourcesOnCloseEnabled(boolean releaseResourcesOnCloseEnabled) {
		this.releaseResourcesOnCloseEnabled = releaseResourcesOnCloseEnabled;
		return this;
	}

	public HibernateConfig withSessionInterceptorClass(Class<? extends Interceptor> sessionInterceptorClass) {
		this.sessionInterceptorClass = sessionInterceptorClass;
		return this;
	}


	public DataSource getDataSource() {
		return dataSource;
	}

	public List<Class<?>> getEntityClasses() {
		return entities;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return transactionType;
	}

	public Class<? extends Interceptor> getSessionInterceptorClass() {
		return sessionInterceptorClass;
	}

	public boolean isReleaseResourcesOnCloseEnabled() {
		return releaseResourcesOnCloseEnabled;
	}

	public List<Class<? extends AttributeConverter<?, ?>>> getAttributeConvertors() {
		return convertors;
	}
}
