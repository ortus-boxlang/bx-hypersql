/**
 * [BoxLang]
 *
 * Copyright [2023] [Ortus Solutions, Corp]
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
package ortus.boxlang.modules.bxhypersql;

import java.util.Map;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.jdbc.drivers.DatabaseDriverType;
import ortus.boxlang.runtime.jdbc.drivers.IJDBCDriver;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.util.StructUtil;

/**
 * The HyperSQL JDBC Driver
 * https://hsqldb.org/doc/2.0/guide/dbproperties-chapt.html#dpc_connection_url
 */
public class HyperSQLDriver implements IJDBCDriver {

	/**
	 * The name of the driver
	 */
	protected static final Key					NAME				= new Key( "Hypersql" );

	/**
	 * The class name of the driver
	 */
	protected static final String				DRIVER_CLASS_NAME	= "org.hsqldb.jdbc.JDBCDriver";

	/**
	 * The default delimiter for the custom parameters
	 */
	protected static final String				DEFAULT_DELIMITER	= ";";

	/**
	 * Default Protocols Map
	 */
	protected static final Map<String, String>	DEFAULT_PROTOCOLS	= Map.of(
	    "file", "file",
	    "mem", "mem",
	    "res", "res",
	    "hsql", "hsql",
	    "hsqls", "hsqls",
	    "http", "http",
	    "https", "https"
	);

	/**
	 * The default parameters for the connection URL
	 * https://hsqldb.org/doc/2.0/guide/dbproperties-chapt.html
	 */
	protected static final IStruct				DEFAULT_PARAMS		= Struct.of(
	    "create", "true"
	);

	@Override
	public Key getName() {
		return NAME;
	}

	@Override
	public DatabaseDriverType getType() {
		return DatabaseDriverType.HYPERSONIC;
	}

	/**
	 * The class name of the driver
	 */
	@Override
	public String getClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public String buildConnectionURL( DatasourceConfig config ) {
		// Validate the database
		String database = ( String ) config.properties.getOrDefault( "database", "" );
		if ( database.isEmpty() ) {
			throw new IllegalArgumentException( "The database property is required for the HyperSQL JDBC Driver" );
		}

		// Default the protocol to mem
		String protocol = ( String ) config.properties.getOrDefault( "protocol", "mem" );
		if ( !DEFAULT_PROTOCOLS.containsKey( protocol ) ) {
			throw new IllegalArgumentException(
			    "The protocol property is invalid for the HyperSQL JDBC Driver: [" + protocol + "]. Available protocols are: " +
			        String.join( ", ", DEFAULT_PROTOCOLS.keySet() )
			);
		}

		// Custom Params
		IStruct params = new Struct( DEFAULT_PARAMS );
		// If the custom parameters are a string, convert them to a struct
		if ( config.properties.get( Key.custom ) instanceof String castedParams ) {
			config.properties.put( Key.custom, StructUtil.fromQueryString( castedParams, DEFAULT_DELIMITER ) );
		}
		// Add the custom parameters
		config.properties.getAsStruct( Key.custom ).forEach( params::put );

		// Add username if it exists
		if ( config.properties.containsKey( Key.username ) && config.properties.getAsString( Key.username ).length() > 0 ) {
			params.put( "user", config.properties.get( Key.username ) );
		}
		// Add password if it exists
		if ( config.properties.containsKey( Key.password ) ) {
			params.put( Key.password, config.properties.get( Key.password ) );
		}

		// If we have a protocol of hsql,hsqls,http,hhtps, we need to add the host and port
		if ( protocol.equals( "hsql" ) || protocol.equals( "hsqls" ) || protocol.equals( "http" ) || protocol.equals( "https" ) ) {
			// Validate the host
			String hostAndPort = ( String ) config.properties.getOrDefault( "host", "" );
			if ( hostAndPort.isEmpty() ) {
				throw new IllegalArgumentException( "The host property is required for the HyperSQL JDBC Driver when using the protocol: " + protocol );
			}

			// Add the port if added
			if ( config.properties.containsKey( Key.port ) && config.properties.getAsInteger( Key.port ) > 0 ) {
				hostAndPort += ":" + config.properties.get( Key.port );
			}

			// Build the connection URL with host info
			return String.format(
			    "jdbc:hsqldb:%s://%s/%s;%s",
			    protocol,
			    hostAndPort,
			    database,
			    StructUtil.toQueryString( params, DEFAULT_DELIMITER )
			);
		} else {
			// Build the connection URL with no host info
			return String.format(
			    "jdbc:hsqldb:%s:%s;%s",
			    protocol,
			    database,
			    StructUtil.toQueryString( params, DEFAULT_DELIMITER )
			);
		}
	}

}
