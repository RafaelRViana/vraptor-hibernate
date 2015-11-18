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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Hibernate {@link ServiceRegistry}, once when application starts.
 * 
 * @author Otávio Scherer Garcia
 */
public class ServiceRegistryCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryCreator.class);
	private Configuration cfg;
	private MultiTenancyConfiguration multiTenancyConfiguration;
	
	/**
	 * @deprecated CDI eyes only
	 */
	public ServiceRegistryCreator() {
	}

	@Inject
	public ServiceRegistryCreator(Configuration cfg, MultiTenancyConfiguration multiTenancyConfiguration) {
		this.cfg = cfg;
		this.multiTenancyConfiguration = multiTenancyConfiguration;
	}

	@Produces
	@ApplicationScoped
	public ServiceRegistry getInstance() {
		LOGGER.debug("creating a service registry");
		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
		serviceRegistryBuilder.applySettings(cfg.getProperties());
		
		if(multiTenancyConfiguration.hasMultiTenancySupport()) {
			serviceRegistryBuilder.addService(MultiTenantConnectionProvider.class, getMultiTenantConnectionProvider());	
		}
		
		return serviceRegistryBuilder.build();
	}

	public void destroy(@Disposes ServiceRegistry serviceRegistry) {
		LOGGER.debug("destroying service registry");
		StandardServiceRegistryBuilder.destroy(serviceRegistry);
	}
	
	protected MultiTenantConnectionProvider getMultiTenantConnectionProvider() {
		return new MultiTenantConnectionProviderDefault(cfg, multiTenancyConfiguration) {
			private static final long serialVersionUID = -3482116169215468727L;

			@Override
			protected ConnectionProvider buildConnectionProvider() {
				C3P0ConnectionProvider connectionProvider = new C3P0ConnectionProvider();		
				connectionProvider.injectServices((ServiceRegistryImplementor) getInstance());
				connectionProvider.configure(cfg.getProperties());
				return connectionProvider;
			}
		};
	}
	
}