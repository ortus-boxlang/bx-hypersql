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
package ortus.boxlang.modules.hypersql;

import java.util.Map;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.jdbc.drivers.DatabaseDriverType;
import ortus.boxlang.runtime.jdbc.drivers.GenericJDBCDriver;
import ortus.boxlang.runtime.scopes.Key;
import ortus.boxlang.runtime.types.IStruct;
import ortus.boxlang.runtime.types.Struct;
import ortus.boxlang.runtime.types.util.StructUtil;

/**
 * The HyperSQL JDBC Driver
 * https://hsqldb.org/doc/2.0/guide/dbproperties-chapt.html#dpc_connection_url
 */
public class HyperSQLDriver extends GenericJDBCDriver {

	public static final String					DEFAULT_PROTOCOL	= "mem";
	protected static final Map<String, String>	AVAILABLE_PROTOCOLS	= Map.of(
	    "file", "file",
	    "mem", "mem",
	    "res", "res",
	    "hsql", "hsql",
	    "hsqls", "hsqls",
	    "http", "http",
	    "https", "https"
	);

	/**
	 * The protocol in use for the jdbc connection
	 */
	protected String							protocol			= DEFAULT_PROTOCOL;

	/**
	 * Constructor
	 */
	public HyperSQLDriver() {
		super();
		this.name					= new Key( "Hypersql" );
		this.type					= DatabaseDriverType.HYPERSONIC;
		// org.apache.derby.jdbc.ClientDriver For client connections
		this.driverClassName		= "org.hsqldb.jdbc.JDBCDriver";
		this.defaultDelimiter		= ";";
		this.defaultCustomParams	= Struct.of(
		    "create", "true"
		);
		this.defaultProperties		= Struct.of();
	}

	@Override
	public String buildConnectionURL( DatasourceConfig config ) {
		// Validate the database
		String database = ( String ) config.properties.getOrDefault( "database", "" );
		if ( database.isEmpty() ) {
			throw new IllegalArgumentException( "The database property is required for the HyperSQL JDBC Driver" );
		}

		// Default the protocol to mem
		this.protocol = ( String ) config.properties.getOrDefault( "protocol", DEFAULT_PROTOCOL );
		if ( !AVAILABLE_PROTOCOLS.containsKey( protocol ) ) {
			throw new IllegalArgumentException(
			    String.format(
			        "The protocol '%s' is not valid for the Apache Derby JDBC Driver. Available protocols are %s",
			        this.protocol,
			        AVAILABLE_PROTOCOLS.keySet().toString()
			    )
			);
		}

		// If the custom parameters are a string, convert them to a struct
		if ( config.properties.get( Key.custom ) instanceof String castedParams ) {
			config.properties.put( Key.custom, StructUtil.fromQueryString( castedParams, this.defaultDelimiter ) );
		}
		IStruct customParams = config.properties.getAsStruct( Key.custom );

		// Add username if it exists
		if ( config.properties.containsKey( Key.username ) && config.properties.getAsString( Key.username ).length() > 0 ) {
			customParams.put( "user", config.properties.get( Key.username ) );
		}
		// Add password if it exists
		if ( config.properties.containsKey( Key.password ) ) {
			customParams.put( Key.password, config.properties.get( Key.password ) );
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
			    customParamsToQueryString( config )
			);
		} else {
			// Build the connection URL with no host info
			return String.format(
			    "jdbc:hsqldb:%s:%s;%s",
			    protocol,
			    database,
			    customParamsToQueryString( config )
			);
		}
	}

}
