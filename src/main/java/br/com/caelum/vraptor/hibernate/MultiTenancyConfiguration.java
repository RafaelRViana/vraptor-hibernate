package br.com.caelum.vraptor.hibernate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class MultiTenancyConfiguration {

	private HttpServletRequest request;
	
	/**
	 * @deprecated CDI eyes only
	 */
	public MultiTenancyConfiguration() {
	}

	@Inject
	public MultiTenancyConfiguration(HttpServletRequest request) {
		this.request = request;
	}
	
	/**
	 * Override this method in a specialize class (using CDI's @Specializes).
	 */
	protected Boolean hasMultiTenancySupport() {
		return false;
	}
	
	/**
	 * Override this method in a specialize class (using CDI's @Specializes).
	 */
	protected MultiTenancyStrategy getMultiTenancyStrategy() {
		return MultiTenancyStrategy.NONE;
	}
	
	public Session createSession(SessionFactory factory) {
		if(hasMultiTenancySupport()) return factory.withOptions().tenantIdentifier(getTenantIdentifier()).openSession();
		
		return factory.openSession();
	}

	/**
	 * Override this method in a specialize class (using CDI's @Specializes).
	 */
	protected String getTenantIdentifier() {
		if(request == null || request.getRequestURL() == null) return "";
		
		String url = request.getRequestURL().toString();	
		
		if(url.contains("localhost")) return getDefaultTenantId();
		
		String urlSemProtocolo = url.split("//")[1];
		return urlSemProtocolo.split("\\.")[0];
	}
	
	/**
	 * Override this method in a specialize class (using CDI's @Specializes).
	 */
	protected String getDefaultTenantId() {
		return "teste";
	}
	
}