package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PlataformaUtils {
	
	public static BasicDataSource getQADatasource() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.14:30315/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

		return ds;
	}
	
	public static Connection getQAConnection() throws SQLException {
		Connection conn = getQADatasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	
	public static Connection getQAPEConnection() throws SQLException {
        Connection conn = getQAPEDatasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	public static BasicDataSource getQAPEDatasource() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.15:30115/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

		return ds;
	}
	
	public static Connection getMBIConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.12.90:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}	
	
	public static Connection getMBIPREConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.12.82:30015/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}	
	
	public static Connection getSURAConnection() throws SQLException {
		//String dbURL  = "jdbc:sap://161.238.0.14:30315/?";
		//String dbURL  = "jdbc:sap://161.238.133.100:30415/?";
		String dbURL  = "jdbc:sap://161.238.12.121:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getPRESURAConnection() throws SQLException {
		//String dbURL  = "jdbc:sap://161.238.0.14:30315/?";
		String dbURL  = "jdbc:sap://161.238.133.100:30215/?";
//		String dbURL  = "jdbc:sap://161.238.5.70:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	
	
	public static Connection getBCOCHILEConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.12.186:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";
        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getBCOESTADOConnection() throws SQLException {		
		String dbURL  = "jdbc:sap://161.238.12.154:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";
        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getPREBCOESTADOConnection() throws SQLException {		
		String dbURL  = "jdbc:sap://161.238.12.144:30015/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";
        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getJPMORGANConnection() throws SQLException {		
		String dbURL  = "jdbc:sap://161.238.133.100:30515/?";                 		
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";
        
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getMBConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.4.101:30415/?";
		//String dbURL  = "jdbc:sap://161.238.133.100:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getMBPEConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.69.10:30115/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getDEMOConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.15.15:30215/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getUATConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.14:30315/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getUAT2Connection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.14:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
        
		return conn;
	}
	
	public static Connection getDESAConnection() throws SQLException {
        Connection conn = getDESADatasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	public static BasicDataSource getDESADatasource() throws SQLException {
		//String dbURL  = "jdbc:sap://161.238.37.7:30415/?";
		String dbURL  = "jdbc:sap://161.238.15.29:30441/?";
		String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //String user   = "RULLOA";
        //String pass   = "B0lsa1234567";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

        return ds;
	}
	

	public static BasicDataSource getMASTER01Datasource() throws SQLException {
		//String dbURL  = "jdbc:sap://161.238.37.7:30415/?";
		String dbURL  = "jdbc:sap://161.238.15.14:30441/?";
		String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //String user   = "RULLOA";
        //String pass   = "B0lsa1234567";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

        return ds;
	}
	
	public static Connection getMASTER01Connection() throws SQLException {
        Connection conn = getMASTER01Datasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	
	public static Connection getDESAPEConnection() throws SQLException {
        Connection conn = getDESAPEDatasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	public static BasicDataSource getDESAPEDatasource() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.15:30015/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
		return ds;
	}
	
	public static BasicDataSource getTestDataSource() {
		//String dbURL  = "jdbc:sap://161.238.34.246:30215/?";
		String dbURL  = "jdbc:sap://hana_qa_peru:30115/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
		return ds;
	}
	
	public static String loadResourceToString(String path) {
		FileLoader f = new FileLoader();
		return f.loadResourceToString(path);
	}
	
	public static Connection getBolsaConnection() throws SQLException {
        Connection conn = getBOLSADatasource().getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		return conn;
	}
	
	public static MensajeSalida obtenerMensajeSalida(Connection conn, Long idEmpresa, Long id, String tipo, String codigo, Map<String, String> params) {
		HashMap<String, String> _params = params instanceof HashMap ? (HashMap<String, String>) params : new HashMap<>(params);
		
		MensajeSalida mensajeSalida;
		try {
			mensajeSalida = new MensajeSalida(id, tipo, codigo, _params, conn, idEmpresa);
		} catch (Exception e) {
			mensajeSalida = new MensajeSalida();
			mensajeSalida.codigo = codigo;
			mensajeSalida.id = id;
			mensajeSalida.tipo = tipo;
			mensajeSalida.descripcion = codigo + " sin traduccion";
		}
		
		return mensajeSalida;
	}
	
	public static BasicDataSource getBOLSADatasource() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.82:30415/?";
		String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	

        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

        return ds;
	}
	public static class MensajeSalida {
		public Long id;
		public String tipo;	//ERROR,AUTORIZACION,ADVERTENCIA,INFORMACION
		public String codigo;
		public String descripcion;
		
		public static final String MENSAJE_OK = "OK";
		public static final String MENSAJE_ERROR = "ERROR";
		public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
		public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
		public static final String MENSAJE_INFORMACION = "INFORMACION";
		public static final Object MENSAJE_RECHAZO = "RECHAZO"; //TODO ELIMINAR DE REQUISITOS OPERATIVOS
		
		public MensajeSalida() {
			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public MensajeSalida(Long id,String tipo,String codigo,String descripcion) {
			this.id = id;
			this.tipo = tipo;
			this.codigo = codigo;
			this.descripcion = descripcion;
		}
		
		public MensajeSalida(Long id,String tipo,String codigo,HashMap<String, String> valores,Connection con,Long idEmpresa) throws Exception {
			this.id = id;
			this.tipo = tipo;
			this.codigo = codigo;
			this.descripcion = Diccionario.getMensaje(con,idEmpresa, codigo, valores);
		}
		
		public static long getNumeroErrores(List<MensajeSalida> salida) {
			int c = 0;
			for (MensajeSalida s : salida) {
				if (s.tipo == MENSAJE_ERROR)
					c++;
			}
			return c;
		}
		
	}
	
	public static String concatenarMensajesSalida(List<MensajeSalida> mensajes) {
		String mensajeSalida = "";
		for (MensajeSalida mensaje : mensajes) {
			mensajeSalida += "\n" + mensaje.descripcion;
		}
		return mensajeSalida;
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static List<MensajeSalida> erroresAMensajes (List<ErrorDetalle> errs) {
		List<MensajeSalida> salida = new ArrayList();
		for (ErrorDetalle err :errs) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = err.Codigo;
			m.descripcion = err.Descripcion;
			m.tipo = "ERROR";
			salida.add(m);
		}
		return salida;
	}
	
	public static List<MensajeSalida> advertenciasAMensajes (List<AdvertenciaDetalle> advs) {
		List<MensajeSalida> salida = new ArrayList();
		for (AdvertenciaDetalle adv :advs) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = adv.Codigo;
			m.descripcion = adv.Descripcion;
			if (adv.RequiereAutorizacion == "S")
				m.tipo = "AUTORIZACION";
			else
				m.tipo = "ADVERTENCIA";
			salida.add(m);
		}
		return salida;
	}
	
	public static List<MensajeSalida> informacionesAMensajes (List<InformacionDetalle> infos) {
		List<MensajeSalida> salida = new ArrayList();
		for (InformacionDetalle info :infos) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = info.Codigo;
			m.descripcion = info.Descripcion;
			m.tipo = "INFORMACION";
			salida.add(m);
		}
		return salida;
	}

	public static Connection getSTFConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.14:30315/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
		return conn;
	}
	
	public static BasicDataSource getDESAB1Datasource() {
		//String dbURL  = "jdbc:sap://161.238.37.7:30415/?";
		String dbURL  = "jdbc:sap://161.238.34.67:30415/?";
		String dbprop = "currentschema=DESAINICIAL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        //String user   = "";
        //String pass   = "";	
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

        return ds;
	}
	
	
	public static BasicDataSource getDESAPEB1Datasource() {
		//String dbURL  = "jdbc:sap://161.238.37.7:30415/?";
		String dbURL  = "jdbc:sap://161.238.69.10:30415/?";
		String dbprop = "currentschema=UAT;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        //String user   = "";
        //String pass   = "";	
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);

        return ds;
	}
	
	
	public static Connection getQAPEAZUREConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.15:30115/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}

	public static Connection getTANNERConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.6.70:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}
	
	public static Connection getBOLSAConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.0.82:30415/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}
	
	public static Connection getPreBcoEstadoConnection() throws SQLException {
		return null;
	}
	
	
	public static Connection getMerrillConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.15.125:30441/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}
	
	public static Connection getMerrillPreConnection() throws SQLException {
		String dbURL  = "jdbc:sap://161.238.15.123:30015/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "";
        String pass   = "";	
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
         
        Connection conn = ds.getConnection();
        
        conn.setAutoCommit(false);
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

		return conn;
	}

	
	
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
		String archivoProperties = (rutaProperties!=null) ? rutaProperties : "application.properties" ;
		try {
			System.out.println("Connecting to: " + ambiente);
			//Configuration prop = new Configurations().properties("application.properties");
			CompositeConfiguration prop = new CompositeConfiguration();
			prop.addConfiguration(new SystemConfiguration());
			prop.addConfiguration(new PropertiesConfiguration(archivoProperties));
			
			ambiente = String.format("%s.%s.", ((ambiente!=null) ? ambiente.toLowerCase() : "desa"), "saphana");
			String dbURL  = prop.getString(ambiente+"dbURL");
	        String dbprop = prop.getString(ambiente+"dbprop");
	        String dbdrv  = prop.getString(ambiente+"dbdrv");
	        String dbqry  = prop.getString(ambiente+"dbquery");
	        String user   = prop.getString(ambiente+"dbuser");
	        String pass   = prop.getString(ambiente+"dbpass");	
	        //Datasource
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
	        } 
	        else if (dbName.contains("POSTGRESQL")) {
	        	//something something
	        }
	        
	        System.out.println("CONNECTED TO: " + dbURL);
			return conn;
		} catch (ConfigurationException e) {
			throw new ConfigurationException("Achivo " + archivoProperties + " no existe");
		}
	}
	
	
	/**
	 * Crea y devuelve una conexión JDBC a una base de datos SAP HANA utilizando
	 * los parámetros proporcionados. Este método inicializa un {@link BasicDataSource}
	 * con las propiedades de conexión, establece el esquema activo y deshabilita
	 * el autocommit de transacciones DDL.
	 *
	 * <p>Ejemplo de uso:</p>
	 * <pre>{@code
	 * Connection conn = getHanaConnection(
	 *     "hana-dev.bcs.cl",
	 *     "30015",
	 *     "BSCL",
	 *     "USR_OPTIMUS",
	 *     "Password123"
	 * );
	 * }</pre>
	 *
	 * @param host     Host o dirección del servidor SAP HANA (por ejemplo, "hana-dev.bcs.cl").
	 * @param port     Puerto de conexión JDBC (por ejemplo, "30015").
	 * @param schema   Nombre del esquema por defecto (por ejemplo, "BSCL").
	 * @param user     Usuario de conexión a la base de datos.
	 * @param password Contraseña correspondiente al usuario de conexión.
	 * @return         Una instancia activa de {@link java.sql.Connection} configurada.
	 * @throws SQLException Si ocurre un error al establecer la conexión o ejecutar sentencias iniciales.
	 * @throws IllegalArgumentException Si alguno de los parámetros requeridos es nulo o vacío.
	 *
	 * @see org.apache.commons.dbcp.BasicDataSource
	 * @see java.sql.Connection
	 * @see java.sql.DriverManager
	 */
	public static Connection getHanaConnection(
	        String host,
	        String port,
	        String schema,
	        String user,
	        String password) throws SQLException {

	    // Validaciones basicas de parametros
	    if (host == null || host.isEmpty()) throw new IllegalArgumentException("Host no puede ser nulo o vacío");
	    if (port == null || port.isEmpty()) throw new IllegalArgumentException("Puerto no puede ser nulo o vacío");
	    if (schema == null || schema.isEmpty()) throw new IllegalArgumentException("Schema no puede ser nulo o vacío");

	    // Construccion de la cadena JDBC y propiedades
	    String dbURL  = "jdbc:sap://" + host + ":" + port + "/?";
	    String dbprop = "currentschema=" + schema + ";";
	    String dbdrv  = "com.sap.db.jdbc.Driver";
	    String dbqry  = "select 1 from dummy";

	    // Configuracion del DataSource
	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(dbdrv);
	    ds.setUsername(user);
	    ds.setPassword(password);
	    ds.setUrl(dbURL);
	    ds.setValidationQuery(dbqry);
	    ds.setConnectionProperties(dbprop);
	    ds.setDefaultAutoCommit(false);

	    // Creacion de la conexion
	    Connection conn = ds.getConnection();
	    conn.setAutoCommit(false);

	    // Configuraciones especificas de sesion en SAP HANA
	    SQL.exec(conn, "set schema " + schema);
	    SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");

	    System.out.println("Conectado a SAP HANA (" + host + ":" + port + "), schema=" + schema);
	    return conn;
	}

	
/**
	 * Realiza la evaluacion de algunos casos de una sentencia sql
	 * @param query => se le pasa una sentencia sql
	 * @param params => se le pasa hashmap con parametros de entrada para la sentencia query
	 * @return HashMap<String,String>
	 * 
	 * Uso: Se debe realizar el cambio dentro de la sentencia .sql
	 * 
	 * En el sql:
	 * 
	 * Caso 1 COALESCE:
	 * 		Antes:
	 * 			AND	ALIAS."IdEjemplo" = CASE WHEN IFNULL(:p_IdEjemplo,'') <> '' THEN :p_IdEjemplo ELSE ALIAS."IdEjemplo" END
	 *		Despues:
	 *			<<CASE.ALIAS.p_IdEjemplo>>
	 *	 		
	 * Caso 2 CASE:
	 * 		Antes:
	 *			AND (:p_IdProductoAgrupador IS NULL OR MAP(MP."IdProductoAgrupador", NULL, 0, MP."IdProductoAgrupador") = :p_IdProductoAgrupador)	
	 *		Despues:
	 *			<<COALESCE.ALIAS.p_IdProductoAgrupador>>
	 *
	 * Caso 3 FECHACONSULTA (Solamente funciona cuando segundo parametro es ALIAS.INGFechaLiquidacion):
	 *
	 *      Antes:
	 *			AND
	 *			(
	 *				(:p_TipoFechaConsulta = 'LIQ' AND ALIAS."INGFechaLiquidacion" <= :v_FechaConsulta)
	 *				OR (:p_TipoFechaConsulta = 'ADQ' AND ALIAS."INGFechaOperacion" <= :v_FechaConsulta)
	 *			)
	 *		Despues:
	 *			<<FECHACONSULTA.ALIAS.p_TipoFechaConsulta>>
	 *
	 *
	 * En el java:
	 * 
	 *		 	String queryNuevaCondiciones = CONSULTA_SQL;
	 * 			HashMap<String,Object> InBusquedaAux = new HashMap<String,Object>();
	 * 			InBusquedaAux.putAll(InBusqueda);
	 * 			HashMap<String,String> condiciones = PlataformaUtils.creaCondicionesQuery(queryNuevaCondiciones,InBusquedaAux);
	 * 			for(Map.Entry<String, String> entry : condiciones.entrySet()) {
	 * 				queryNuevaCondiciones = queryNuevaCondiciones.replaceAll(entry.getKey(), entry.getValue());
	 * 			}
	 * 			InBusquedaAux = PlataformaUtils.saneaParametros(queryNuevaCondiciones,InBusquedaAux);
	 *
	 */
	
	public static HashMap<String,String> creaCondicionesQuery(String query, HashMap<String,Object> params) {
        HashMap<String,String> newConditions = new HashMap<String,String>();
        params.forEach((k,v) -> {
            String newCondition="";
            int lastPosition = query.indexOf(k + ">>");
            if(lastPosition != -1) {
            	String tableType = query.substring(query.substring(0,lastPosition).lastIndexOf("<<")+2,lastPosition).split("[.]")[0];
                String tableAlias = query.substring(query.substring(0,lastPosition).lastIndexOf("<<")+2,lastPosition).split("[.]")[1];
            	if(tableType.equals("COALESCE")) {
        			if(v != null) {
        				newCondition = "AND COALESCE("+ tableAlias + ".\"" + k.replace("p_", "") + "\", 0) = " + v;
        				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", newCondition);
        			}else {
        				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", "");
        			}
            	}
            	if(tableType.equals("CASE")) {
                    if (v == null)
                    	v = "";
            		switch(v.getClass().getName()) {
            		case "java.lang.Long":
            			if(((Long)v).compareTo(0L) > 0) {
            				newCondition = "AND "+ tableAlias + ".\"" + k.replace("p_", "") + "\" = " + v;
            				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", newCondition);
            				
            			}
            			else {
            				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", "");
            				
            			}
            			break;
            		case "java.lang.String":
            			if(((String)v).compareTo("") > 0) {
            				newCondition = "AND "+ tableAlias + ".\"" + k.replace("p_", "") + "\" = '" + v + "'";
            				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", newCondition);
            			}
            			else {
            				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", "");
            			}
            			break;
            		}
            	}
            	if(tableType.equals("FECHACONSULTA")){
        			if(v.equals("LIQ")) {
        				newCondition = "AND "+ tableAlias + ".\"INGFechaLiquidacion\" <= :v_FechaConsulta";
        				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", newCondition);
        			}else {
        				newCondition = "AND "+ tableAlias + ".\"INGFechaOperacion\" <= :v_FechaConsulta";
        				newConditions.put("<<"+ tableType + "." + tableAlias + "." + k + ">>", newCondition);
        			}
            	}
            }
        });
        return newConditions;
    }

	
	/**
	 * Elimina parametros no utilizados dentro de una sentencia query de nuestro hashmap de parametros
	 * @param query => string que contenga la sentencia sql
	 * @param params => hashmap de parametros de entrada de la sentencia
	 * @return HashMap<String,Object>
	 * 
	 */
	
	public static HashMap<String,Object> saneaParametros(String query, HashMap<String,Object> params) {
		
		@SuppressWarnings("unchecked")
		HashMap<String,Object> newParams = (HashMap<String,Object>) params.clone();
		
		Iterator<Entry<String, Object>> it = params.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Object> item = it.next();
			if (!query.contains(":" + item.getKey())) {
				newParams.remove(item.getKey());
			}
		}
		
        return newParams;
    }

	
	
}
