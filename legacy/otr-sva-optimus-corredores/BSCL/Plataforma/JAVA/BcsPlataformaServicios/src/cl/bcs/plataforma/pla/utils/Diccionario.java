package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cl.bcs.utils.SQL;

public class Diccionario {
	
	private static List<Long> diccionariosCargados = new ArrayList<>();
	private static HashMap<String,RegistroDiccionario> registros;
	private static String SQL_CONSULTAR_DICCIONARIO;
	private static final String SEPARADOR = "::";
	private static final String SIN_TRADUCCION = "sin traduccion";
	private static FileLoader loader;
	
	private static synchronized void getDiccionario(java.sql.Connection con,Long idEmpresa) throws Exception {
		loader = new FileLoader();
		SQL_CONSULTAR_DICCIONARIO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarDiccionario.sql");
		registros = new HashMap<String,RegistroDiccionario>();
		HashMap<String,Object> parms = new HashMap<>();
		parms.put("IdEmpresa", idEmpresa);
		parms.put("CodigoParametro", "PLA.LocalizacionSistema");
		SQL sql=new SQL();
		List<RegistroDiccionario> ll =sql.query(con, SQL_CONSULTAR_DICCIONARIO,parms,RegistroDiccionario.class);
		for(RegistroDiccionario reg: ll) {
			registros.put(idEmpresa + "-" + reg.CodDiccionario, reg);
		}
		diccionariosCargados.add(idEmpresa);
	}
	
	public static class RegistroDiccionario {
		public String CodLenguaje;
		public String CodDiccionario;
		public String Descripcion;
	}
	
	public static String getMensaje(Connection con,Long idEmpresa,String codigo,Map<String,String> valores) throws Exception {
		if (!diccionariosCargados.contains(idEmpresa)) {
			Diccionario.getDiccionario(con,idEmpresa);
		}
		
		RegistroDiccionario registroDiccionario = registros.get(idEmpresa + "-" +codigo);
		if (registroDiccionario == null) {
			return codigo + " " + SIN_TRADUCCION;
		}
		
		if (valores == null) {
			return codigo + " " + SIN_TRADUCCION;
		}
		
		String mensaje = registroDiccionario.Descripcion;
		Iterator<Entry<String, String>> it = valores.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String,String> pair = (Entry<String, String>) it.next();
	        String valor = pair.getValue() == null ? "" : pair.getValue();
	        mensaje = mensaje.replace(SEPARADOR + pair.getKey() + SEPARADOR, valor);
	    }
		
		return mensaje;
	}
	
	private Long idEmpresa;
	private String objCodigo="";
	private Connection conn;
	private Map<String,String> objValores=new HashMap<String,String>();

	public Diccionario add(String llave,Object valor) {
		if (valor != null)
			objValores.put(llave, valor.toString());
		return this;
	}
	
	public String msg() throws Exception {
		RegistroDiccionario registroDiccionario = registros.get(idEmpresa + "-" +objCodigo);
		if (registroDiccionario == null) {
			return objCodigo + " " + SIN_TRADUCCION;
		}		
		
		return getMensaje(conn,idEmpresa,objCodigo,objValores);
		
		/*Iterator<Entry<String, String>> it = objValores.entrySet().iterator();
		String mensaje = registroDiccionario.Descripcion;
	    while (it.hasNext()) {
	        Entry<String,String> pair = (Entry<String, String>) it.next();
	        mensaje = mensaje.replace(SEPARADOR + pair.getKey() + SEPARADOR, pair.getValue());
	    }
		return mensaje;*/
	}

	/**
	 * Devuelve un diccionario para agregar valores
	 *  -> Diccionario.getMensaje(con,"ERROR_VALOR").add("IdPrecio","1234").add("IdCliente","5555").msg();
	 * @param con
	 * @param codigo
	 * @return
	 * @throws Exception 
	 */
	public static Diccionario getMensaje(Connection con,Long idEmpresa,String codigo) throws Exception {
		Diccionario dicc=new Diccionario();
		dicc.objCodigo=codigo;
		dicc.idEmpresa = idEmpresa;
		dicc.conn = con;
		if (Diccionario.registros == null) {
			Diccionario.getDiccionario(con,idEmpresa);
		}
		return dicc;
	}
	
	
	/*public static void main(String[]args) throws SQLException{
		BasicDataSource ds = getTestDataSource();
		try (Connection con = ds.getConnection()){
			HashMap<String,String> map = new HashMap<>();
			map.put("IdAsignacion", "2345234523452345");
			String trad = Diccionario.obtenerMensaje(con, "ASIGNACION_ENVIADA_A_FACTURAR", map);
			String trad2 = Diccionario.obtenerMensaje(con, "ASIGNACION_ENVIADA_A_FACTURasdfAR", map);
			System.out.println(trad);
			System.out.println(trad2);
			
		}	
	}
	
	
	public static BasicDataSource getTestDataSource() {
		String dbURL  = "jdbc:sap://161.238.34.246:30215/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SYSTEM";
        String pass   = "B0lsa123";		
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
	}*/
	
}