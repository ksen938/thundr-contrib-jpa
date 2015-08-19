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

import javax.persistence.Persistence;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.threewks.thundr.jpa.deprecated.intercept.JpaSession;
import com.threewks.thundr.jpa.deprecated.intercept.JpaSessionInterceptor;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Persistence.class)
public class JeeJpaModuleTest {
/*	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
	private ServletContext mockServletContext;
	private JeeJpaModule jeeJpaModule = new JeeJpaModule();

	@Before
	public void before() {
		mockServletContext = mock(ServletContext.class);
		PowerMockito.mockStatic(Persistence.class);
		when(Persistence.createEntityManagerFactory(Mockito.anyString())).thenReturn(mock(EntityManagerFactory.class));

		injectionContext.inject(mockServletContext).as(ServletContext.class);
		injectionContext.inject("default:local").named(JeeJpaModule.PersistenceManagersConfigName).as(String.class);

		InterceptorRegistry actionInterceptorRegistry = mock(InterceptorRegistry.class);
		injectionContext.inject(actionInterceptorRegistry).as(InterceptorRegistry.class);
	}

	@Test
	public void shouldInjectDbSessionActionInterceptor() {
		jeeJpaModule.configure(injectionContext);

		InterceptorRegistry registry = injectionContext.get(InterceptorRegistry.class);
		verify(registry).registerInterceptor(Matchers.eq(JpaSession.class), Matchers.any(JpaSessionInterceptor.class));
	}

	@Test
	public void shouldInjectPersistenceManagerRegistry() {
		jeeJpaModule.configure(injectionContext);

		EntityManagerRegistry registry = injectionContext.get(EntityManagerRegistry.class);
		assertThat(registry, is(notNullValue()));
		assertThat(registry.get("default"), is(notNullValue()));
	}

	@Test
	public void shouldReturnDefaultPersistenceManagerAndPersistenceUnitNameIfNoneSet() {
		PowerMockito.mockStatic(Persistence.class);
		when(Persistence.createEntityManagerFactory(Mockito.anyString())).thenReturn(mock(EntityManagerFactory.class));

		injectionContext = new InjectionContextImpl();
		injectionContext.inject(mock(ServletContext.class)).as(ServletContext.class);

		InterceptorRegistry actionInterceptorRegistry = mock(InterceptorRegistry.class);
		injectionContext.inject(actionInterceptorRegistry).as(InterceptorRegistry.class);

		Map<String, String> results = jeeJpaModule.getPersistenceUnitNames(injectionContext);
		assertThat(results.size(), is(1));
		assertThat(results, hasEntry("default", "default"));
	}

	@Test
	public void shouldClearPersistenceManagerRegistryOnContextDestroyed() {
		EntityManagerRegistry registry = mock(EntityManagerRegistry.class);

		injectionContext.inject(registry).as(EntityManagerRegistry.class);

		jeeJpaModule.stop(injectionContext);

		verify(registry).clear();
	}*/
}
