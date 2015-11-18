package br.com.caelum.vraptor.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides default Hibernate multi tenant implementation
 * 
 * @author Rafael Viana
 *
 */
public abstract class MultiTenantConnectionProviderDefault extends AbstractMultiTenantConnectionProvider {

	private static final long serialVersionUID = 7923571834863288510L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiTenantConnectionProviderDefault.class);
	
	private final Map<String, ConnectionProvider> connectionPool;
	private Configuration configuration;
	private MultiTenancyConfiguration multiTenancyConfiguration;
	
	public MultiTenantConnectionProviderDefault(Configuration configuration, MultiTenancyConfiguration multiTenancyConfiguration) {
		this.connectionPool = new HashMap<>();
		this.configuration = configuration;
		this.multiTenancyConfiguration = multiTenancyConfiguration;
	}
	
	private ConnectionProvider get(String tenantIdentifier) {
		ConnectionProvider connection = connectionPool.get(tenantIdentifier);
		if (connection == null) {
			LOGGER.debug("Initializing tenant %s", tenantIdentifier);
			connection = buildConnectionProvider();
			if (connection == null) return null;
			connectionPool.put(tenantIdentifier, connection);
			if (!tenantIdentifier.equals(getTenantAny())) { //Update database schema
				SchemaUpdate schema = new SchemaUpdate(configuration);
				schema.execute(false, true);
			}
		}
		return connection;
	}

	@Override
	protected ConnectionProvider getAnyConnectionProvider() {
		return get(getTenantAny());
	}
	
	@Override
	protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
		return get(tenantIdentifier);
	}
	
	protected abstract ConnectionProvider buildConnectionProvider();
	
	/**
	 * It's used to get database metadata.
	 * @return
	 */
	protected String getTenantAny() {
		return multiTenancyConfiguration.getDefaultTenantId();
	}
	
}