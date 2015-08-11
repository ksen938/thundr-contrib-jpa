package com.threewks.thundr.jpa;

import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.jpa.hibernate.HibernateModule;
import com.threewks.thundr.module.DependencyRegistry;

public class JpaModule extends BaseModule {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		super.requires(dependencyRegistry);
		dependencyRegistry.addDependency(HibernateModule.class);
	}
}
