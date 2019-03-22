/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.repository.init;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.support.Repositories;
import org.springframework.util.Assert;

/**
 * Base class for {@link FactoryBean}s creating {@link ResourceReaderRepositoryPopulator}s. Sub-classes have to provide
 * a {@link ResourceReader} to hand into the {@link RepositoryPopulator} instance created.
 * 
 * @author Oliver Gierke
 */
public abstract class AbstractRepositoryPopulatorFactoryBean extends
		AbstractFactoryBean<ResourceReaderRepositoryPopulator> implements ApplicationListener<ContextRefreshedEvent>,
		ApplicationContextAware {

	private Resource[] resources;
	private RepositoryPopulator populator;
	private ApplicationContext context;

	/**
	 * Configures the {@link Resource}s to be used to load objects from and initialize the repositories eventually.
	 * 
	 * @param resources must not be {@literal null}.
	 */
	public void setResources(Resource[] resources) {
		Assert.notNull(resources, "Resources must not be null!");
		this.resources = resources.clone();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.context = applicationContext;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return ResourceReaderRepositoryPopulator.class;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
	 */
	@Override
	protected ResourceReaderRepositoryPopulator createInstance() {

		ResourceReaderRepositoryPopulator initializer = new ResourceReaderRepositoryPopulator(getResourceReader());
		initializer.setResources(resources);
		initializer.setApplicationEventPublisher(context);

		this.populator = initializer;

		return initializer;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(ContextRefreshedEvent event) {

		if (event.getApplicationContext().equals(context)) {
			Repositories repositories = new Repositories(event.getApplicationContext());
			populator.populate(repositories);
		}
	}

	protected abstract ResourceReader getResourceReader();
}
