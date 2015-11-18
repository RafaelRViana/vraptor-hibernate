/***
 * Copyright (c) 2011 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.caelum.vraptor.hibernate;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link SessionFactory} object, once when application starts.
 * 
 * @author Otávio Scherer Garcia
 */
public class SessionFactoryCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryCreator.class);
	private Configuration cfg;
	private ServiceRegistry serviceRegistry;
	private MultiTenancyConfiguration multiTenancyConfiguration;

	/**
	 * @deprecated CDI eyes only
	 */
	public SessionFactoryCreator() {
	}

	@Inject
	public SessionFactoryCreator(Configuration cfg, ServiceRegistry serviceRegistry, MultiTenancyConfiguration multiTenancyConfiguration) {
		this.cfg = cfg;
		this.serviceRegistry = serviceRegistry;
		this.multiTenancyConfiguration = multiTenancyConfiguration;
	}

	@Produces
	@ApplicationScoped
	public SessionFactory getInstance() {
		LOGGER.debug("creating a session factory");
		return cfg.buildSessionFactory(serviceRegistry);
	}

	public void destroy(@Disposes SessionFactory sessionFactory) {
		LOGGER.debug("destroying session factory");
		sessionFactory.close();
	}
	
	/**
	 * Override this method in a specialize class (using CDI's @Specializes) to set custom properties.
	 * Like, add multi-tenancy support
	 *
	 * @param configuration
	 */
	protected void extraProperties(Properties properties) {
		if(multiTenancyConfiguration.hasMultiTenancySupport()) {
			properties.put(Environment.MULTI_TENANT, multiTenancyConfiguration.getMultiTenancyStrategy());
		}
	}
	
}