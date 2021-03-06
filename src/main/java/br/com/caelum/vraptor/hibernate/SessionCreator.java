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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Hibernate {@link Session}, once per request.
 * 
 * @author Otávio Scherer Garcia
 */
public class SessionCreator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionCreator.class);
	private SessionFactory factory;
	private MultiTenancyConfiguration multiTenancyConfiguration;
	
	/**
	 * @deprecated CDI eyes only
	 */
	public SessionCreator() {
	}

	@Inject
	public SessionCreator(SessionFactory factory, MultiTenancyConfiguration multiTenancyConfiguration) {
		this.factory = factory;
		this.multiTenancyConfiguration = multiTenancyConfiguration;
	}

	@Produces
	@RequestScoped
	public Session getInstance() {
		Session session = multiTenancyConfiguration.createSession(factory);
		
		LOGGER.debug("opening a session {}", session);
		return session;
	}

	public void destroy(@Disposes Session session) {
		LOGGER.debug("closing session {}", session);
		session.close();
	}
	
}