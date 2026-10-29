package cl.bcs.utils;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionUtils {

	/**
	 * Genera una conexion obteniendo la configuracion desde Properties/application.properties
	 * @param ambiente => por ejemplo si recibe sura, buscara en el archivo properties las configuraciones de bd sura.saphana...
	 * @return java.sql.Connection
	 * @throws SQLException si no se puede realizar la conexion
	 * @throws ConfigurationException si no se logra obtener alguna configuracion desde el archivo properties
	 */
	public static Connection getPropertiesConnection(String ambiente) throws SQLException, ConfigurationException {
		return getPropertiesConnection(ambiente, null);
	}

	/**
	 * Genera una conexion obteniendo la configuracion desde Properties/application.properties
	 * @param ambiente => por ejemplo si recibe sura, buscara en el archivo properties las configuraciones de bd sura.saphana...
	 * @param rutaProperties => se puede especificar una ruta distinta del archivo application.properties por ejemplo C:\application.properties
	 * @return java.sql.Connection
	 * @throws SQLException si no se puede realizar la conexion
	 * @throws ConfigurationException si no se logra obtener alguna configuracion desde el archivo properties
	 */
	public static Connection getPropertiesConnection(String ambiente, String rutaProperties) throws SQLException, ConfigurationException {
		String archivoProperties = (rutaProperties != null) ? rutaProperties : "application.properties";
		try {
			System.out.println("Connecting to: " + ambiente);
			CompositeConfiguration prop = new CompositeConfiguration();
			prop.addConfiguration(new SystemConfiguration());
			prop.addConfiguration(new PropertiesConfiguration(archivoProperties));

			ambiente = String.format("%s.%s.", ((ambiente != null) ? ambiente.toLowerCase() : "desa"), "saphana");
			String dbURL  = prop.getString(ambiente + "dbURL");
			String dbprop = prop.getString(ambiente + "dbprop");
			String dbdrv  = prop.getString(ambiente + "dbdrv");
			String dbqry  = prop.getString(ambiente + "dbquery");
			String user   = prop.getString(ambiente + "dbuser");
			String pass   = prop.getString(ambiente + "dbpass");

			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(dbdrv);
			ds.setUsername(user);
			ds.setPassword(pass);
			ds.setUrl(dbURL);
			ds.setValidationQuery(dbqry);
			ds.setConnectionProperties(dbprop);
			ds.setDefaultAutoCommit(false);
			ds.addConnectionProperty("ssl", "false");

			Connection conn = ds.getConnection();
			conn.setAutoCommit(false);

			String dbName = conn.getMetaData().getDatabaseProductName().toUpperCase();

			if (dbName.contains("HANA")) {
				SQL.exec(conn, "SET SCHEMA \"BSCL\"");
				SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			} else if (dbName.contains("POSTGRESQL")) {
				// something something
			}

			System.out.println("CONNECTED TO: " + dbURL);
			return conn;
		} catch (ConfigurationException e) {
			throw new ConfigurationException("Archivo " + archivoProperties + " no existe");
		}
	}
}
