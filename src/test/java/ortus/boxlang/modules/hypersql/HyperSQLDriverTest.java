package ortus.boxlang.modules.hypersql;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ortus.boxlang.runtime.config.segments.DatasourceConfig;
import ortus.boxlang.runtime.jdbc.drivers.DatabaseDriverType;
import ortus.boxlang.runtime.scopes.Key;

public class HyperSQLDriverTest {

	@Test
	@DisplayName( "Test getName()" )
	public void testGetName() {
		HyperSQLDriver	driver			= new HyperSQLDriver();
		Key				expectedName	= new Key( "Hypersql" );
		assertThat( driver.getName() ).isEqualTo( expectedName );
	}

	@Test
	@DisplayName( "Test getType()" )
	public void testGetType() {
		HyperSQLDriver		driver			= new HyperSQLDriver();
		DatabaseDriverType	expectedType	= DatabaseDriverType.HYPERSONIC;
		assertThat( driver.getType() ).isEqualTo( expectedType );
	}

	@Test
	@DisplayName( "Test buildConnectionURL()" )
	public void testBuildConnectionURL() {
		HyperSQLDriver		driver	= new HyperSQLDriver();
		DatasourceConfig	config	= new DatasourceConfig();
		config.properties.put( "driver", "Hypersql" );
		config.properties.put( "database", "mydb" );

		String expectedURL = "jdbc:hsqldb:mem:mydb;create=true";
		assertThat( driver.buildConnectionURL( config ) ).isEqualTo( expectedURL );
	}

	@DisplayName( "Throw an exception if the database is not found" )
	@Test
	public void testBuildConnectionURLNoDatabase() {
		HyperSQLDriver		driver	= new HyperSQLDriver();
		DatasourceConfig	config	= new DatasourceConfig();

		assertThrows( IllegalArgumentException.class, () -> {
			driver.buildConnectionURL( config );
		} );
	}

	@DisplayName( "Throw an exception if the protocol is not valid" )
	@Test
	public void testBuildConnectionURLInvalidProtocol() {
		HyperSQLDriver		driver	= new HyperSQLDriver();
		DatasourceConfig	config	= new DatasourceConfig();
		config.properties.put( "driver", "Hypersql" );
		config.properties.put( "database", "mydb" );
		config.properties.put( "protocol", "invalid" );

		assertThrows( IllegalArgumentException.class, () -> {
			driver.buildConnectionURL( config );
		} );
	}

	@DisplayName( "Build a http connection URL" )
	@Test
	public void testBuildConnectionURLHttp() {
		HyperSQLDriver		driver	= new HyperSQLDriver();
		DatasourceConfig	config	= new DatasourceConfig();
		config.properties.put( "driver", "Hypersql" );
		config.properties.put( "database", "mydb" );
		config.properties.put( "protocol", "http" );
		config.properties.put( "host", "localhost" );

		String expectedURL = "jdbc:hsqldb:http://localhost/mydb;create=true";
		assertThat( driver.buildConnectionURL( config ) ).isEqualTo( expectedURL );
	}

}
