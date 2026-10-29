package cl.bcs.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.utils.AgregarLog.DetalleLog;
import cl.bcs.utils.AgregarLog.InAgregarLog;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


public class Utils {
	
	private static final Logger log		= Logger.getLogger(Utils.class.getName());	
	private static final Map<String, String> kDefCache = new HashMap<String, String>();
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static synchronized String getDefinitionCache(String key) {
		log.log(Level.FINE, "CACHE: "+key+"\r\n"+kDefCache.get(key));
		return kDefCache.get(key);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static synchronized boolean isDefinitionCache(String key) {
		return kDefCache.containsKey(key);
	}
	
	/**
	 * 
	 * @param key
	 * @param val
	 */
	public static synchronized void putDefinitionCache(String key,String val) {
		kDefCache.put(key,val);
	}
	
	/**
	 * 
	 * @param key
	 */
	public static synchronized void delDefinitionCache(String key) {
		kDefCache.remove(key);
	}
	
	
	public final static String getSchema() {
		return "BSCL";
	}
	
	/***
	 * 
	 * @param conn
	 * @param IdEmpresa
	 * @param CodigoParametro
	 * @return
	 * @throws KProcedureException
	 */
	
	public final static String getConfiguracion(Connection conn,String schema,long IdEmpresa,String CodigoParametro) throws StoredProcedureException {
		return getConfiguracion(conn,IdEmpresa,CodigoParametro);
	}
		
	
	public final static String getConfiguracion(Connection conn,long IdEmpresa,String CodigoParametro) throws StoredProcedureException {
		StoredProcedure pSP=new StoredProcedure("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion");
		JsonObject input=new JsonObject();
		input.addProperty("p_IdEmpresa",IdEmpresa);
		input.addProperty("p_CodigoParametro",CodigoParametro);		
		JsonObject output=pSP.call(conn, input);
		return output.get("p_out_Valor".toUpperCase()).getAsString();		
	}
	
	/**
	 * 
	 * @param con
	 * @param IdEmpresa
	 * @param CodigoParametro
	 * @return
	 * @throws SQLException
	 */
	
	static class _ParametroConfiguracion {
		public Long IdEmpresa;
		public String Valor;
		public String Modulo;
		public String CodigoParametro;
		
	}
	
	public static class Mensaje {
		public String Estado;
		public ArrayList<String> status;
		
		
		public Mensaje() {
			this.status = new ArrayList<>();
		}


		public String toString() {
			return Utils.printJson(this, false);
		} 
	}
	
	public static class DatosPais{
		public Long IdPais;	
		public String NombrePais;
		public String UsaDivisionPais;
		public String NombreDivisionPais;		
		public String EsPaisUs;
		public String EsOcde;
		
		public String toString() {
			return Utils.printJson(this, false);
		} 
	}
	
	public static HashMap<String,HashMap<String,String>> getConfiguraciones(Connection con, long idEmpresa) throws SQLException {
		
		HashMap<String,HashMap<String,String>> mapa = new HashMap<>();
		
		SQL _sql = new SQL();
		String query="SELECT \"IdEmpresa\",\"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"FROM\r\n" + 
				"	(SELECT \"IdEmpresa\",'PLATAFORMA' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'CONTABILIDAD' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Contabilidad.BD::CON.ConfiguracionContabilidad\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'ORDENES' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Orden.BD::ORD.ConfiguracionOrdenes\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'CUENTAINVERSION' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'TESORERIA' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'FACTURACION' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'SOCIODENEGOCIO' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'CUSTODIA' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Custodia.BD::CUS.Configuracion\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'FONDOMUTUO' AS \"Modulo\",\"NombreTipoParametro\" AS \"CodigoParametro\",\"ValorParametro\" AS \"Valor\"" + 
				"	FROM \"BSCL.FondoMutuo.BD::FMM.Parametro\"" + 
				"	UNION" + 
				"	SELECT \"IdEmpresa\",'APV' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.APV.BD::PLA.ConfiguracionAPV\"" +
				"   UNION" + 
				"	SELECT \"IdEmpresa\",'MESA' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Mesa.BD::MES.ConfiguracionMesa\"" +
				"   UNION" + 
				"	SELECT \"IdEmpresa\",'PRODUCTO' AS \"Modulo\",\"CodigoParametro\",\"Valor\"" + 
				"	FROM \"BSCL.Plataforma.BD::PRO.Configuracion\"" +
				"	)" + 
				"WHERE \"IdEmpresa\" = :IdEmpresa";
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		List<_ParametroConfiguracion> parametros = 	_sql.query(con, query, params, _ParametroConfiguracion.class);
		
		for (_ParametroConfiguracion p : parametros) {
			HashMap<String,String> parametrosModulo = mapa.get(p.Modulo);
			if (parametrosModulo == null) {
				parametrosModulo = new HashMap<String,String>();
				mapa.put(p.Modulo, parametrosModulo);
			}
			parametrosModulo.put(p.CodigoParametro, p.Valor);
		}
		
		return mapa;
	}
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @return
	 * @throws KProcedureException
	 * @throws StoredProcedureException 
	 */
	
	public final static String getFechaHoraSistema(Connection conn,String schema,long IdEmpresa) throws StoredProcedureException {
		return getFechaHoraSistema(conn,IdEmpresa);
	}
	
	public final static String getFechaHoraSistema(Connection conn,long IdEmpresa) throws StoredProcedureException {
		StoredProcedure pfecha=new StoredProcedure("BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema");
		JsonObject input=new JsonObject();
		input.addProperty("p_IdEmpresa",IdEmpresa);
		JsonObject output=pfecha.call(conn, input);
		return output.get("p_out_FechaHoraSistema".toUpperCase()).getAsString();		
	}
	
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @return
	 * @throws KProcedureException
	 * @throws StoredProcedureException 
	 */
	public final static String getFechaHoraSistemaLocal(Connection conn,String schema,long IdEmpresa) throws StoredProcedureException {
		StoredProcedure pfecha=new StoredProcedure("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas");
		JsonObject input=new JsonObject();
		input.addProperty("p_IdEmpresa",IdEmpresa);			
		JsonObject output=pfecha.call(conn, input);
		return output.get("p_out_FechaHoraSistemaLOCAL".toUpperCase()).getAsString();		
	}	
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @return
	 * @throws KProcedureException
	 * @throws StoredProcedureException 
	 */
	public final static String getFechaSistema(Connection conn,String schema, long IdEmpresa) throws StoredProcedureException {
		return getFechaSistema(conn,IdEmpresa);
	}
	public final static String getFechaSistema(Connection conn,long IdEmpresa) throws StoredProcedureException {
		String dFecha=getFechaHoraSistema(conn,IdEmpresa);
		return dFecha.split(" ")[0];		
	}	
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @param IdUsuario
	 * @param IdModulo
	 * @param CodigoError
	 * @param IdCliente
	 * @param idServicio
	 * @return
	 * @throws Exception 
	 */
	public final static long addErrorLog(Connection conn,long IdEmpresa,long IdUsuario,String IdModulo, String CodigoError, long IdCliente, long idServicio) throws Exception {
		
		AgregarLog agregarLogService = new AgregarLog();
		InAgregarLog entradaLog = new InAgregarLog();
		AgregarLog.Log logEntry = new AgregarLog.Log();
		logEntry.IdTipoLog = "ERROR";
		logEntry.IdModulo = IdModulo;
		logEntry.IdUsuario = IdUsuario;
		logEntry.IdEmpresa = IdEmpresa;
		logEntry.FechaSistemaLog = getFechaSistema(conn,IdEmpresa);
		logEntry.IdUsuarioAtendido = IdUsuario;
		logEntry.IdTipoRegistro = "PLATAFORMA";
		logEntry.IdProceso = System.currentTimeMillis();
		logEntry.NombreProceso = idServicio+"";
		if (CodigoError.length()>250) 
			logEntry.Nemo = CodigoError.substring(0,250);
		else logEntry.Nemo = CodigoError;
		logEntry.IdCliente = IdCliente;
		
		entradaLog.p_Log.add(logEntry );
		DetalleLog detalleLog1 = new DetalleLog();
		detalleLog1.SubRegistro = "ERROR";
		detalleLog1.CampoRegistro = "CODIGO";
		detalleLog1.ValorAntiguo = "";
		detalleLog1.ValorNuevo = logEntry.IdProceso+"";
		
		entradaLog.p_LogDetalle.add(detalleLog1  );
		
		DetalleLog detalleLog2 = new DetalleLog();
		detalleLog2.SubRegistro = "ERROR";
		detalleLog2.CampoRegistro = "PILA";
		detalleLog2.ValorAntiguo = "";
		detalleLog2.ValorNuevo = CodigoError;
		
		entradaLog.p_LogDetalle.add(detalleLog2  );
		
		DetalleLog detalleLog3 = new DetalleLog();
		detalleLog3.SubRegistro = "ERROR";
		detalleLog3.CampoRegistro = "SP";
		detalleLog3.ValorAntiguo = "";
		detalleLog3.ValorNuevo = idServicio+"";
		
		entradaLog.p_LogDetalle.add(detalleLog3  );
		
		agregarLogService.logica(conn, entradaLog );
		return logEntry.IdProceso;
	}
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @param IdUsuario
	 * @param IdModulo
	 * @param TipoLog
	 * @param Mensaje
	 * @param IdCliente
	 * @param idServicio
	 * @return
	 */
	public final static long addLog(Connection conn,String schema,long IdEmpresa,long IdUsuario,String IdModulo, String TipoLog, String Mensaje, long IdCliente, long idServicio) {
		try {
			StoredProcedure plog=new StoredProcedure("BSCL.Plataforma.BD.Servicios::AgregarLog");
			JsonObject input=new JsonObject();
			input.addProperty("p_IdTipoLog",TipoLog);
			input.addProperty("p_IdModulo",IdModulo);
			input.addProperty("p_IdUsuario",IdUsuario);
			input.addProperty("p_IdEmpresa",IdEmpresa);
			input.addProperty("p_FechaSistemaLog",getFechaSistema(conn,IdEmpresa));
			input.addProperty("p_IdUsuarioAtendido",IdUsuario);
			input.addProperty("p_IdTipoRegistro","PLATAFORMA");
			input.addProperty("p_IdProceso",System.currentTimeMillis());
			input.addProperty("p_NombreProceso",idServicio);
			input.addProperty("p_IdCliente",IdCliente);
			JsonArray tabDeta=new JsonArray();
			JsonObject obj1 = new JsonObject();
			obj1.addProperty("SubRegistro", "MENSAJE");
			obj1.addProperty("CampoRegistro", "CODIGO");
			obj1.addProperty("ValorAntiguo","");
			obj1.addProperty("ValorNuevo", Mensaje);
			tabDeta.add(obj1);	
			input.add("p_DetalleLog",tabDeta);
			plog.call(conn,input);
			return input.get("p_IdProceso").getAsLong();			
		}catch(Exception e) {
			return -1;			
		}		
	}
	
	/***
	 * 
	 * @param conn
	 * @param schema
	 * @param IdEmpresa
	 * @param IdUsuario
	 * @param IdModulo
	 * @param TipoLog
	 * @param Mensaje
	 * @param IdCliente
	 * @param idServicio
	 * @return
	 */
	public final static long addLog(Connection conn,String schema,long IdEmpresa,long IdUsuario,String IdModulo, String TipoLog, long IdCliente, long idServicio,String subRegistro, String codigo, String valorAntiguo, String valorNuevo) {
		try {

			StoredProcedure plog=new StoredProcedure("BSCL.Plataforma.BD.Servicios::AgregarLog");
			JsonObject input=new JsonObject();
			input.addProperty("p_IdTipoLog",TipoLog);
			input.addProperty("p_IdModulo",IdModulo);
			input.addProperty("p_IdUsuario",IdUsuario);
			input.addProperty("p_IdEmpresa",IdEmpresa);
			input.addProperty("p_FechaSistemaLog",getFechaSistema(conn,IdEmpresa));
			input.addProperty("p_IdUsuarioAtendido",IdUsuario);
			input.addProperty("p_IdTipoRegistro","PLATAFORMA");
			input.addProperty("p_IdProceso",System.currentTimeMillis());
			input.addProperty("p_NombreProceso",idServicio);
			input.addProperty("p_IdCliente",IdCliente);
			JsonArray tabDeta=new JsonArray();
			JsonObject obj1 = new JsonObject();
			obj1.addProperty("SubRegistro", subRegistro);
			obj1.addProperty("CampoRegistro", codigo);
			obj1.addProperty("ValorAntiguo",valorAntiguo);
			obj1.addProperty("ValorNuevo", valorNuevo);
			tabDeta.add(obj1);	
			input.add("p_DetalleLog",tabDeta);
			plog.call(conn,input);
			return input.get("p_IdProceso").getAsLong();			
		}catch(Exception e) {
			e.printStackTrace();
			return -1;			
		}		
	}
	

	public final static JsonObject getSessionData(Connection conn,String pTokenSession) throws StoredProcedureException {
		JsonObject dataJSIn=new JsonObject();
		dataJSIn.addProperty("p_Token",pTokenSession);
		StoredProcedure kSession=new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario");
		JsonObject dataJSOut=kSession.call(conn, dataJSIn);
		return dataJSOut;					
	}
		
	
	public final static String getWebStaticFile(Connection conn,String ambiente,String sbase,String spath,String sfile) throws IOException {
    	String nfile=sbase+spath.replace('.','/')+"/"+sfile;
		//String nfile = "C:\\Nuam\\Repos\\otr-sva-optimus-corredores\\"+spath.replace('.','/')+"/"+sfile;
    	return FileUtils.readFileToString(new File(nfile),"UTF-8");
    }
    

	public static void leLog(Connection conn, String classname, String log) throws SQLException {
		String query = "insert into log(texto) values(?);";
		PreparedStatement p = conn.prepareStatement(query);
		p.setString(1, classname+"-"+log);
		p.execute();
		p.close();
		
	}
	
	public final static byte[] getFileFromHana(Connection con,String pkg,String obj, String suffix) {		
        String query="SELECT BDATA,CDATA FROM _SYS_REPO.ACTIVE_OBJECT WHERE PACKAGE_ID = ? AND OBJECT_NAME = ? AND OBJECT_SUFFIX = ?  ORDER BY activated_at DESC ";
        try {
	        try (PreparedStatement cs1 = con.prepareStatement(query)) {
	        	cs1.setString(1, pkg);
	        	cs1.setString(2, obj);
	        	cs1.setString(3, suffix);	        	
	            try (ResultSet rset1 = cs1.executeQuery()) {
	            	if (rset1.next()) {
		            	byte[] bdata= rset1.getBytes(rset1.findColumn("BDATA"));
		            	String cdata= rset1.getString(rset1.findColumn("CDATA"));      
		    			if (bdata==null) bdata=cdata.getBytes("UTF-8");	   
		    			return bdata;
	            	}
	            }        	
	        }   
        }catch(Exception er) {
        	System.out.println("ERROR: getFileFromHana p:"+pkg+" o:"+obj+" s:"+suffix);
        	er.printStackTrace();
        }
    	return null;        
	}
	
	public final static String getTextFromUrl(String purl,String userpass) throws IOException {
		URL url = new URL(purl);
		URLConnection urlc = url.openConnection();
		if (userpass!=null) urlc.setRequestProperty ("Authorization", "Basic " + new String(new Base64().encode(userpass.getBytes())));								
		BufferedInputStream in =  new BufferedInputStream(urlc.getInputStream());			     
        byte[] buffer = new byte[4096];		        
        int bytesRead;
        StringBuilder sb = new StringBuilder();
        while( ( bytesRead = in.read(buffer) ) != -1 ) {
        	sb.append(new String(buffer,0,bytesRead));
        }		
        return sb.toString();
	}
	
	/***
	 * 
	 * @param command
	 * @param dir
	 * @param msegWaiting
	 * @return
	 * @throws IOException
	 */
	public final static String startProcess(String command, String dir,long msegWaiting) throws IOException {
		StringBuffer ret = new StringBuffer();
		String[] comm = new String[3];
		comm[0] = "/bin/sh";
		comm[1] = "-c";
		comm[2] = command;
		long start = System.currentTimeMillis();
		try {
			Process ls_proc = Runtime.getRuntime().exec(comm, null, new File(dir));
			BufferedInputStream ls_in = new BufferedInputStream(ls_proc.getInputStream());
			BufferedInputStream ls_err = new BufferedInputStream(ls_proc.getErrorStream());
			boolean end = false;
			while (!end) {
				int c = 0;
				while ((ls_err.available() > 0) && (++c <= 1000)) {
					ret.append("" + (char)ls_err.read());
				}
				c = 0;
				while ((ls_in.available() > 0) && (++c <= 1000)) {
					ret.append("" + (char)ls_in.read());
				}
				try {
					ls_proc.exitValue();
					while (ls_err.available() > 0)
						ret.append("" + (char)ls_err.read());
					while (ls_in.available() > 0)
						ret.append("" + (char)ls_in.read());
					end = true;
				}
				catch (IllegalThreadStateException ex) {
					//Process is running
				}
				//The process is not allowed to run longer than given time.
				if (System.currentTimeMillis() - start > msegWaiting) {
					ls_proc.destroy();
					end = true;
					ret.append("!!!! Process has timed out, destroyed !!!!!");
				}
				try {
					Thread.sleep(50);
				}
				catch (InterruptedException ie) {}
			}
		}
		catch (IOException e) {
			ret.append("Error: " + e);
		}
		return ret.toString();
	}

	/**
	 * Generador de tokens impredecibles
	 * @param size Largo del token a generar
	 * @param characterSet Conjunto de caracteres disponibles para el token
	 * @return Token, de largo size usando caracteres de characterSet
	 */
	private static String generateToken(int size, String characterSet) {
		SecureRandom random = new SecureRandom();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			builder.append(characterSet.charAt(random.nextInt(characterSet.length())));
		}
		return builder.toString();
	}

	/**
	 * Generador de tokens para la recuperacion de passwords usuarios
	 * @param size largo del token a generar
	 * @return retorna un token del tipo HK#8z
	 */
	public static String generateToken(int size){
		String dictionary = "BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz1234567890!#$%&=?@";
		return Utils.generateToken(size, dictionary);
	}

	/**
	 * Generador de tokens de unico uso para la confirmacion de firma de documento
	 * @param size largo del token a generar
	 * @return returna un token del tipo qp2md2
	 */
	public static String generateAlphanumericToken(int size) {
		String dictionary = "abcdefghijklmnopqrstuvwxyz0123456789";
		return Utils.generateToken(size, dictionary);
	}

	/**
	 * Valida Contrasenas ingresadas
	 * 
	 * @param contrasena
	 *            a validar
	 * @return retorna
	 * 
	 * UPDATE 27 oct. 2022: Funcion deprecada, se reemplaza por ValidarNuevaClave
	 */
	
	public static Mensaje validatePassword(Connection con, String contrasena, Long idEmpresa) {
		Mensaje respuesta = new Mensaje();
		
		if(contrasena != null && !contrasena.isEmpty()){
		String query = "SELECT \"CodigoParametro\", \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = ? AND \"CodigoParametro\" IN ( 'ACC.numLongitud', 'ACC.numMayusculas', 'ACC.numMinuscula', 'ACC.numCaracteres')";
		String codigoParametro = "";
		int valor = 1;
		try (PreparedStatement cs1 = con.prepareStatement(query)) {
			cs1.setLong(1, idEmpresa);
			
			try (ResultSet rset1 = cs1.executeQuery()) {
				String act = "";
				String pre = "";
				int contiguales = 0;
				int contMayuscula = 0;
				int contMinuscula = 0;
				int contCaracter = 0;
				boolean error = false;
				
				for (int i = 1; contrasena.length() >= i; i++) {
					act = contrasena.substring(i-1,i);
					if(act.equals(pre)){
						contiguales++;
						pre = act;
					}
					if (act.matches("[A-Z]")) {
						contMayuscula++;
					}
					if (act.matches("[a-z]")) {
						contMinuscula++;
					}
					if (act.matches("[^a-z\\^A-Z\\s]")) {
						contCaracter++;
					}
				}
				respuesta.status.add("La contrase\u00f1a debe contener:");
				
				while (rset1.next()) {
					codigoParametro = rset1.getString(1);
					valor = Integer.parseInt(rset1.getString(2));
					
					if (codigoParametro.equals("ACC.numLongitud")) {
						respuesta.status.add("- Al menos " + valor + " caracteres de largo.");
						if (contrasena.length() < valor) {
							error = true;
						}
					}
					if (codigoParametro.equals("ACC.numMayusculas")) {
						respuesta.status.add("- " + valor + " may\u00fascula.");
						if (contMayuscula < valor) {
							error = true;
						}
					}
					if (codigoParametro.equals("ACC.numMinuscula")) {
						respuesta.status.add("- " + valor + " min\u00fascula.");
						if (contMinuscula < valor) {
							error = true;
						}
					} 
					if (codigoParametro.equals("ACC.numCaracteres")) {
						respuesta.status.add("- " + valor + " car\u00e1cter especial.");
						if (contCaracter < valor) {
							error = true;
						}
					}					
				}
				if(contiguales > 0 || error){
					respuesta.status.add("- " + "Ning\u00fan car\u00e1cter igual junto.");
					error = true;
				}	
				if (!error) {
					respuesta = new Mensaje();
					respuesta.Estado = "OK";
					respuesta.status.add("OK");
				}
				else {
					respuesta.Estado = "NOK";
					
				}

			} catch (Exception er) {
				er.printStackTrace();
			}

		}catch (Exception er) {
			er.printStackTrace();
		}
		
		}else{
			respuesta.Estado = "NOK";
			respuesta.status.add("La contrase\\u00f1a no puede venir vac\u00eda");
		}
		return respuesta;
	}
	
	public static String encryptPassword(String unencryptedPassword) {
		HashFunction passwordTemporary = Hashing.sha256();
		com.google.common.hash.HashCode hc = passwordTemporary.newHasher().putString(unencryptedPassword, Charsets.UTF_8).hash();
		com.google.common.hash.HashCode hc2 = passwordTemporary.newHasher().putString(hc.toString(), Charsets.UTF_8).hash();
		String passwordDefinitive = hc2.toString().toUpperCase();
		
		return passwordDefinitive;
	}

	
	/**
	 * Ofusca el correo como medida de seguridad
	 * @param email ej:correo@dominio.cl
	 * @return c*****@d******.c*
	 */
	public static String obfuscateEmail(String email){
		if(email != null && email.length()>1){
			StringBuilder obfuscatedEmail = new StringBuilder();
			int first = 0;
			int at = email.lastIndexOf("@");
			int dot = email.lastIndexOf(".");
			
			for (int i = 0; i < email.length(); i++) {
				if(i!=first && i!=at && i!=at+1 && i!=dot&& i!=dot+1){
					obfuscatedEmail.append("*");
				}else{
					obfuscatedEmail.append(email.charAt(i));
				}
			}
			email = obfuscatedEmail.toString();
		}
		return (email!=null)?email:"";
	}
	
	/**
	 * print object to json
	 * @param obj
	 * @param pretty
	 * @return
	 */
	public static String printJson(Object obj,boolean pretty) {
		Gson gson;
		if (pretty)  gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").setPrettyPrinting().serializeNulls().create();
		else  gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String msg="";
	    if (obj == null) msg="[]";
	    else msg=gson.toJson(obj);
		return msg;
	}
	
	/**
	 * Convert list to hashmap using key
	 * @param list
	 * @param field
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static <T,K> HashMap<T,Object>  List2Hash(List<K> list,String keyfield) throws Exception {
		HashMap <T,Object> hobj=new HashMap<T, Object>();
		if (list==null) throw new Exception("List must be not null");
		if (list.size()==0) return  hobj;
		Object lobj=list.get(0);
		Field field=lobj.getClass().getField(keyfield);
		if (field==null) throw new SQLException("Object field unknow for "+keyfield);			
		for (Object obj:list) {
			T kf=(T) field.get(obj); 
			if (hobj.containsKey(kf))  {
				throw new Exception("key field is not unique");			
			}
			hobj.put(kf, obj);
		}
		return hobj;
	}
	
	/**
	 * Get one column from list with multi-columns objects
	 * @param list
	 * @param keyfield
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T>  getColumn(List<?> list,String keyfield,boolean duplicate) throws Exception {
		List <T> hobj=new ArrayList<T>();
		HashMap<T,Boolean> dup=new HashMap<T,Boolean>();
		if (list==null) throw new Exception("List must be not null");
		if (list.size()==0) return  hobj;
		Object lobj=list.get(0);
		Field field=lobj.getClass().getField(keyfield);
		if (field==null) throw new SQLException("Object field unknow for "+keyfield);			
		for (Object obj:list) {
			@SuppressWarnings("unchecked")
			T kf=(T) field.get(obj); 					
			if (duplicate==false) {
				if (!dup.containsKey(kf)) {
					dup.put(kf, true);
					hobj.add(kf);
				}
			}else {
				hobj.add(kf);
			}
		}
		return hobj;
	}
	
	
	/***
	 * @deprecated Preferir copia manual, propiedad por propiedad.
	 * copy the content only for the same name and type
	 * @param objSrc
	 * @param typeSrc
	 * @param objTarget
	 * @param typeTarget
	 * @return
	 * @throws Exception 
	 */	
	@Deprecated
	public static <K,T> T copyPojo(K objSrc,T objTarget) throws Exception {	
		return copyPojo(objSrc,objTarget,true);
	}
	
	/***
	 * @deprecated Preferir copia manual, propiedad por propiedad.
	 * copy the content only for the same name and type
	 * @param objSrc
	 * @param typeSrc
	 * @param objTarget
	 * @param typeTarget
	 * @param restrictDate
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Deprecated
	public static <K,T> T copyPojo(K objSrc,T objTarget,boolean restrictDate) throws Exception {		
		if (objSrc==null) throw new Exception("objSrc  must be not null");
		if (objTarget==null) throw new Exception("objTarget  must be not null");
		Class typeSrc=objSrc.getClass();
		Class typeTarget=objTarget.getClass();		
		Field[] tar=typeTarget.getFields();
		for (Field field : tar) {
			String tname=field.getName();
			String ttype=field.getType().getName();
			try {
				Field sfield=typeSrc.getField(tname);
				String stype=sfield.getType().getName();
				if (ttype.equals(stype)) {
					Object  obj=sfield.get(objSrc);
					if (obj==null) field.set(objTarget, null);
					else if (ttype.equals("java.lang.Long")) {
						field.set(objTarget, new Long((Long)obj));
					}else if (ttype.equals("java.lang.Integer")) {
							field.set(objTarget, new Integer((Integer)obj));						
					}else if (ttype.equals("java.lang.String")) {
						field.set(objTarget, new String((String)obj));	
					}else if (ttype.equals("java.math.BigDecimal")) {
						field.set(objTarget, new BigDecimal(((BigDecimal)obj).toString()));		
					}else if (ttype.equals("java.sql.Timestamp")) {
						field.set(objTarget, new java.sql.Timestamp(((java.sql.Timestamp)obj).getTime()));		
					}else if (ttype.equals("java.sql.Date")) {
						field.set(objTarget, new java.sql.Date(((java.sql.Date)obj).getTime()));	
					}else if (ttype.equals("java.sql.Time")) {
						field.set(objTarget, new java.sql.Time(((java.sql.Time)obj).getTime()));	
					}else if (ttype.equals("java.util.List")) {
						List nlist=new ArrayList();
						field.set(objTarget, nlist);	
						for (Object item:(List)obj) {
							Object nobj=item.getClass().newInstance();							
							nlist.add(copyPojo(item,nobj));							
						}													
					}else {
						//System.out.println(stype);
					}
				}else if (restrictDate==false){
					Object  obj=sfield.get(objSrc);
					if (ttype.equals("java.sql.Timestamp") && stype.equals("java.lang.String")) {
						if (obj==null) field.set(objTarget, null);
						else {
							field.set(objTarget, Fecha.toDateTime((String)obj));
						}						
					}
					else if (ttype.equals("java.sql.Date") && stype.equals("java.lang.String")) {
						if (obj==null) field.set(objTarget, null);
						else {
							field.set(objTarget, Fecha.toDate((String)obj));
						}													
					}
					else if (ttype.equals("java.lang.String") && stype.equals("java.sql.Timestamp")) {
						if (obj==null) field.set(objTarget, null);
						else {
							field.set(objTarget, Fecha.toStringTimeStamp((java.sql.Timestamp)obj));
						}													
					}
					else if (ttype.equals("java.lang.String") && stype.equals("java.sql.Date")) {
						if (obj==null) field.set(objTarget, null);
						else {
							field.set(objTarget, Fecha.toStringDate((java.sql.Date)obj));						
						}							
					}					
				}
			} catch (NoSuchFieldException  | IllegalAccessException e) {
				continue;
			} 
		};		
		return (T)objTarget;
	}
	
	public static String stackTraceToString(Exception ex) {
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
	
        private static final String PATTERN_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    
        public static boolean emailValidate(String email) {
    
            // Compiles the given regular expression into a pattern.
            Pattern pattern = Pattern.compile(PATTERN_EMAIL);
    
            // Match the given input against this pattern
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
    
        }
        
        public static String getCurrentTimeStamp() {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            return strDate;
        }
        
        public static void CallBatchAsync(String sURL,String Token,String JarClass,JsonObject in) {
        	new Thread(() -> {
         	   System.out.println("CallBatchAsync RPTA:"+CallBatch(sURL,Token,JarClass,in));
        	}).start();   
        }
        
        public static String CallBatch(String sURL,String Token,String JarClass,JsonObject in) {
        	try {
        		String URLparms=sURL+"?t="+ URLEncoder.encode(JarClass,"UTF-8");
				byte[] postData  = in.toString().getBytes("UTF-8");
				int postDataLen=  postData.length;        		
				URL url = new URL(URLparms);
				HttpURLConnection urlc = (HttpURLConnection) url.openConnection();  
				urlc.setDoOutput(true);
				urlc.setRequestProperty ("X-SESSION-TOKEN",Token);
				urlc.setRequestMethod("POST");
				urlc.setConnectTimeout(3000);
				urlc.setInstanceFollowRedirects(true); 
				urlc.setRequestProperty("Content-Type", "application/json"); 
				urlc.setRequestProperty("charset", "UTF-8");	
				urlc.setRequestProperty( "Content-Length", Integer.toString(postDataLen));
				urlc.setUseCaches( false );
				try( DataOutputStream wr = new DataOutputStream( urlc.getOutputStream())) {
					wr.write( postData );
				}
				String response="";
				try (BufferedReader win = new BufferedReader(new InputStreamReader(urlc.getInputStream()));) {
					String inputLine;
			        while ((inputLine = win.readLine()) != null) {
			        	response+=inputLine;			           
			        }
				}
				return urlc.getResponseCode() + ":"+ response;
        	}catch(Exception er) {
        		return "0:"+er.getMessage();        		
        	}        	
        }
        
        public static class Secuencia {
    		public Long Id;
    	}
        
        public static List<Long> getSecuencia(Connection conn, String nombreSecuencia, Long cantidad) throws SQLException {
            List<Long> salida = new ArrayList<>();
            
            if (cantidad == null || cantidad <= 0) {
                return salida;
            }

            SQL _sql = new SQL();
            String query;
            
            String dbName = conn.getMetaData().getDatabaseProductName().toUpperCase();

            if (dbName.contains("POSTGRESQL")) {
                query = "SELECT nextval('\"" + nombreSecuencia + "\"') AS \"Id\" FROM generate_series(1, :cantidad)";
            } 
            else {
                query = "SELECT \"" + nombreSecuencia + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put("cantidad", cantidad);

            List<Secuencia> res = _sql.query(conn, query, params, Secuencia.class);
            
            for (Secuencia sec : res) {
                if (sec.Id != null) {
                    salida.add(sec.Id);
                }
            }
            
            return salida;
        }
        
        public static Gson getGsonBuilder() {
        	GsonBuilder gbuild=new GsonBuilder();
    		gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    		gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
    		gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
    		Gson gson = gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
        	return gson;
        }
        
        public static ObjectMapper getJacksonBuilder() {
        	ObjectMapper o = new ObjectMapper();
        	o.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        	return o;
        }
        
        /***
         * 
         * @param InputfileName
         * @param zipFilename
         * @throws FileNotFoundException
         * @throws IOException
         */
    	public static void fileToZipFile(String fullfileName,String fullzipFilename,boolean deleteSrc) throws FileNotFoundException, IOException {    		
    		try (FileOutputStream fos = new FileOutputStream(fullzipFilename);) {
				try (ZipOutputStream zos = new ZipOutputStream(fos);) {			    		
		    		File file = new File(fullfileName);
		    		try (FileInputStream fis = new FileInputStream(file);) {
			    		ZipEntry zipEntry = new ZipEntry(file.getName());
			    		zos.putNextEntry(zipEntry);
			    		byte[] bytes = new byte[1024];
			    		int length;
			    		while ((length = fis.read(bytes)) >= 0) {
			    			zos.write(bytes, 0, length);
			    		}
		    		}
		    		if (deleteSrc) file.delete();
	    		}
    		}
    	}

    	/***
    	 * Get PID 
    	 * Work only for Sun/Oracle JVM
    	 * @return
    	 */
    	public static int getPidSystem() {
    		try {
    			String val=ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    			int pid=Integer.parseInt(val);
    			return pid;
    		}catch (Exception er) { }
    		return 0;
    	}
    	
    	/***
    	 * Return true if OS is windows
    	 * @return
    	 */
    	public static boolean isWindowsOS() {
    		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) 
    			return true;
    		return false;		
    	}
        
    	/***
    	 * 
    	 * @param file
    	 * @param timestamp
    	 * @throws IOException
    	 */
    	public static void touch(File file, long timestamp) throws IOException{
    	    if (!file.exists()) {
    	       new FileOutputStream(file).close();
    	    }
    	    file.setLastModified(timestamp);
    	}  
    	
    	
	/**
	 * Funcion para obtener el codigo de un usuario
	 * @param conn
	 * @param idEmpresa
	 * @param idUsuario
	 * @return codigoUsuario
	 */
	public static String getCodigoUsuario(Connection conn, Long idEmpresa, Long idUsuario) {
		String query = "SELECT \"CodigoUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdEmpresa\" = ? AND \"IdUsuario\" = ?";
		try (PreparedStatement ps1 = conn.prepareStatement(query)) {
			ps1.setLong(1, idEmpresa);
			ps1.setLong(2, idUsuario);
			try (ResultSet rs = ps1.executeQuery()) {
				while (rs.next()) {
					return rs.getString("CodigoUsuario");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return String.valueOf(idUsuario);
	}
	
	/**
	 * Funcion para obtener el codigo de un usuario
	 * @param conn
	 * @param idEmpresa
	 * @param idUsuario
	 * @return codigoUsuario
	 */
	public static String getRequiereEventoAutorizacion(Connection conn, Long idEmpresa, String idConcepto) {
		String requiereAutorizacion = "N";
		
		String query = "SELECT COALESCE(\"RequiereAutorizacion\",'N') AS \"RequiereAutorizacion\" FROM \"BSCL.Plataforma.BD::PLA.RequiereAutorizacion\" WHERE \"IdConcepto\" = ? AND \"IdEmpresa\" = ? ";
		try (PreparedStatement ps1 = conn.prepareStatement(query)) {
			ps1.setString(1, idConcepto);
			ps1.setLong(2, idEmpresa);
			try (ResultSet rs = ps1.executeQuery()) {
				while (rs.next()) {
					requiereAutorizacion = rs.getString("RequiereAutorizacion"); 
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requiereAutorizacion;
	}
	
	public static Connection getSAPConnection(String ip, String port,String company,String user, String pass) throws SQLException {
		String dbURL  = "jdbc:sap://" + ip +":" + port +"/?";
		String dbprop = "currentschema=" + company +";";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        	
        //Datasource
        @SuppressWarnings("resource")
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
		SQL.exec(conn, "set schema " + company);
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
		return conn;
	}
	
	static class ConvertirNumeroTexto {	
		
		private static final String[] decenas = {
			    "",
			    " diez",
			    " veinte",
			    " treinta",
			    " cuarenta",
			    " cincuenta",
			    " sesenta",
			    " seventa",
			    " ochenta",
			    " noventa"
			  };
		
		private static final String[] centenas = {
			    "",
			    " ciento",
			    " doscientos",
			    " trescientos",
			    " cuatrocientos",
			    " quinientos",
			    " seiscientos",
			    " setecientos",
			    " ochocientos",
			    " novecientos"
			  };
		
			  private static final String[] numeros = {
			    "",
			    " uno",
			    " dos",
			    " tres",
			    " cuatro",
			    " cinco",
			    " seis",
			    " siete",
			    " ocho",
			    " nueve",
			    " diez",
			    " once",
			    " doce",
			    " trece",
			    " catorec",
			    " quince",
			    " dieciseis",
			    " diecisiete",
			    " dieciocho",
			    " diecinueve",
			    "",
			    " veintiuno",
			    " veintidos",
			    " veintitres",
			    " veinticuatro",
			    " veinticinco",
			    " veintiseis",
			    " veintisiete",
			    " veintiocho",
			    " veintinueve",
			  };
		
		private static String convertLessThanOneThousand(int number) {
		    String soFar;
		    
		    if (number == 100) {
		    	return "cien";
		    }
		    else if (number % 100 < 30){
		      soFar = numeros[number % 100];
		      number /= 100;
		    }
		    //> 20
		    else {
		      soFar = numeros[number % 10];
		      number /= 10;
		 
		      soFar = decenas[number % 10] + " y"+soFar;
		      number /= 10;
		    }
		    if (number == 0) return soFar;
		    return centenas[number] + soFar;
		  }
		public static String convert(long numeroCompleto,String textoMoneda) {
			return convert(new BigDecimal(numeroCompleto),false,textoMoneda);
		}
		public static String convert(BigDecimal numeroCompleto) {
			return convert(numeroCompleto,false,"");
		}
		public static String convert(long numeroCompleto) {
			return convert(new BigDecimal(numeroCompleto),false,"");
		}
		public static String convert(BigDecimal numeroCompleto,boolean decimalesEnFraccion,String textoMoneda) {
		    // 0 to 999 999 999 999
			long number = numeroCompleto.longValue();
			BigDecimal decimales = numeroCompleto.setScale(2, RoundingMode.FLOOR).subtract(numeroCompleto.setScale(0, RoundingMode.FLOOR));
			decimales = decimales.movePointRight(decimales.scale());
			
		    if (number == 0) {
		    	return "cero";
		    }
		 
		    String snumber = Long.toString(number);
		 
		    // pad with "0"
		    String mask = "000000000000";
		    DecimalFormat df = new DecimalFormat(mask);
		    snumber = df.format(number);
		 
		    // XXXnnnnnnnnn
		    int billions = Integer.parseInt(snumber.substring(0,3));
		    // nnnXXXnnnnnn
		    int millions  = Integer.parseInt(snumber.substring(3,6));
		    // nnnnnnXXXnnn
		    int hundredThousands = Integer.parseInt(snumber.substring(6,9));
		    // nnnnnnnnnXXX
		    int thousands = Integer.parseInt(snumber.substring(9,12));    
		 
		    String tradBillions;
		    switch (billions) {
		    case 0:
		    	tradBillions = "";
		    	break;
		    case 1 :
		    	tradBillions = "un billon ";
		    	break;
		    default :
		    	tradBillions = convertLessThanOneThousand(billions) + " billones ";
		    }
		    String result =  tradBillions;
		 
		    String tradMillions;
		    switch (millions) {
		    case 0:
		    	tradMillions = "";
		    	break;
		    case 1 :
		    	tradMillions = "un millon ";
		    	break;
		    default :
		    	tradMillions = convertLessThanOneThousand(millions) + " millones ";
		    }
		    result =  result + tradMillions;
		 
		    String tradHundredThousands;
		    switch (hundredThousands) {
		    case 0:
		    	tradHundredThousands = "";
		    	break;
		    case 1 :
		    	tradHundredThousands = "mil ";
		    	break;
		    default :
		    	tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " mil ";
		    }
		    result =  result + tradHundredThousands;
		 
		    String tradThousand;
		    tradThousand = convertLessThanOneThousand(thousands);
		    result =  result + tradThousand;
		 
		    // remove extra spaces!
		    String res = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
		    if (decimalesEnFraccion == true) {
		    	return res + " con " + decimales + "/100" + " " + textoMoneda;
		    }
		    else {
		    	return res + " " + textoMoneda;
		    }
		}
		
		public static void main(String[] args){
			System.out.println("*** " + convert(new BigDecimal(630.000)));
			
			/*System.out.println("*** " + convert(0));
		    System.out.println("*** " + convert(1));
		    System.out.println("*** " + convert(16));
			System.out.println("*** " + convert(1000000));
			System.out.println("*** " + convert(2000000));
			System.out.println("*** " + convert(new BigDecimal("100.11234234"),true,"SOLES"));
			System.out.println("*** " + convert(1101));
		    System.out.println("*** " + convert(1000));
		    System.out.println("*** " + convert(1001));
		    System.out.println("*** " + convert(219));
		    System.out.println("*** " + convert(800));
		    System.out.println("*** " + convert(801));
		    System.out.println("*** " + convert(1316));
		    
		    System.out.println("*** " + convert(2000000));
		    System.out.println("*** " + convert(3000200));
		    System.out.println("*** " + convert(700000));
		    System.out.println("*** " + convert(9000000));
		    System.out.println("*** " + convert(9001000));
		    System.out.println("*** " + convert(123456789));
		    System.out.println("*** " + convert(2147483647));
		    System.out.println("*** " + convert(3000000010L));*/
		}
	}
	public static String codificarEvento(String idEvento) {
		String padded = String.format("%1$10s", idEvento);
		byte[] t = padded.getBytes();
		byte[] encoded = Base64.encodeBase64(t);
		return new String(encoded);
	}
	public static String decodificarEvento(String codificado) {
		
		byte[] decoded = Base64.decodeBase64(codificado);
		String idEventoDecoded = new String(decoded);
		return idEventoDecoded.trim();
		
	}
		public static int diferenciaEnDias(Date fechaMayor, Date fechaMenor) {
		long diferenciaEn_ms = fechaMayor.getTime() - fechaMenor.getTime();
		long dias = diferenciaEn_ms / (1000 * 60 * 60 * 24);
		return (int) dias;

	}
	public static <K> List<HashMap<String,Object>> List2ListHashGenericObject(List<K> list) throws Exception {
		List<HashMap<String,Object>> listObj = new ArrayList<HashMap<String,Object>>();
		if (list==null) throw new Exception("List must be not null");
		if (list.size()==0) return  listObj;
		for(Object obj:list){
			HashMap<String,Object> hObj = new HashMap<String,Object>();
			Field[] fields = obj.getClass().getFields();
			for(Field field : fields){
				hObj.put(field.getName(), field.get(obj));
			}
			listObj.add(hObj);
		}
		return listObj;
	}
	
	/**
	 * Concatena 1 o mas Strings en el formato "String1"-"String2"-"String3"-"String4"-"StringN"...
	 * @param llaves
	 * @return
	 */
	public static String appendKeys(String... llaves) {
		StringBuilder key = new StringBuilder();
		for(String llave : llaves) {
			key.append(llave);
			key.append("-");
		}
		return key.toString();
	}
	
	/**
	 * Funcion para contar los registros de una tabla de forma generica.
	 * @param conn
	 * @param nombreTabla
	 * @return {@link Optional} con la cantidad de registros de la tabla
	 * @throws RuntimeException
	 * @throws SQLException
	 */
	public static Optional<Long> contarRegistros(Connection conn, String nombreTabla) throws RuntimeException, SQLException {
		Long count = null;
		if(Objects.isNull(conn)) {
			throw new RuntimeException("Connection obligatoria.");
		}
		if (Objects.isNull(nombreTabla) || nombreTabla.isEmpty()) {
			throw new RuntimeException("Nombre Tabla obligatorio.");
		}
		
		String query = String.format("SELECT COUNT(*) AS \"Cantidad\" FROM \"%s\"", nombreTabla);
		try(PreparedStatement pst = conn.prepareStatement(query)){
			try(ResultSet rs = pst.executeQuery()){
				while(rs.next()) {
					count = rs.getLong(1);
				}
			}
		}
		
		return Optional.ofNullable(count);
	}
	
	
	public static <T> String toCSVString(List<T> list) throws JsonProcessingException, JSONException {
		if (list == null || list.isEmpty()) {
			return "";
		}
		
		
		ObjectMapper objectMapper = new ObjectMapper();
		String s = objectMapper.writeValueAsString(list);
		JSONArray j = new JSONArray(s);
		return CDL.toString(j);
		
		
	}
	
	static String insertRegistroUso = "INSERT INTO \"BSCL.Plataforma.BD::PLA.AuditoriaServicios\" "
			+ "(\"FechaRegistro\",\"IdServicio\",\"IdUsuario\",\"RutaServicio\",\"CodigoEvento\",\"Adicional1\","
			+ "\"Adicional2\",\"Adicional3\",\"Adicional4\",\"IdTransaccion\") "
			+ " VALUES(CURRENT_TIMESTAMP,:IdServicio,:IdUsuario,:RutaServicio,:CodigoEvento,"
			+ ":Adicional1,:Adicional2,:Adicional3,:Adicional4,:IdTransaccion)";
	
	public static class RegistroAuditoriaServicio {
		public Timestamp FechaRegistro;
		public int IdServicio;
		public Long IdUsuario;
		public String RutaServicio;
		public String CodigoEvento;
		public String Adicional1;
		public String Adicional2;
		public String Adicional3;
		public String Adicional4;
		public String IdTransaccion;
	}
	
	private static final List<Integer> noAuditService = Arrays.asList(600017, 1381, 14);
	
	public static void registrarAuditoriaServicios(Connection auditConn,int idServicio,String rutaServicio,Long idUsuario,String idTransaccion,
			String codigoEvento,String adicional1,String adicional2,String adicional3,String adicional4) {
		if (noAuditService.contains(idServicio)) return;
		String r1 = StringUtils.substring(adicional1,0,4000);
		String r2 = StringUtils.substring(adicional2,0,4000);
		String r3 = StringUtils.substring(adicional3,0,4000);
		String r4 = StringUtils.substring(adicional4,0,4000);
		
		
		RegistroAuditoriaServicio r = new RegistroAuditoriaServicio();
		r.IdServicio = idServicio;
		r.RutaServicio = rutaServicio;
		r.IdUsuario = idUsuario;
		r.CodigoEvento = codigoEvento;
		r.Adicional1 = r1;
		r.Adicional2 = r2;
		r.Adicional3 = r3;
		r.Adicional4 = r4;
		r.IdTransaccion = idTransaccion;
		
		SQL sql = new SQL();
		try {
			sql.batch(auditConn, insertRegistroUso, Arrays.asList(r),RegistroAuditoriaServicio.class);
			auditConn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Funcion para obtener datos de paises a traves del codigo
	 * @param conn
	 * @param codigoPais
	 */	
	public static DatosPais getDatosPais(Connection conn, String codigoPais) {
		String query = "SELECT \"IdPais\",\"NombrePais\",\"UsaDivisionPais\",\"NombreDivisionPais\",\"EsPaisUs\",\"EsOcde\" FROM \"BSCL.Plataforma.BD::REG.Pais\" WHERE \"CodigoPais\" = ?";
		DatosPais pais = new DatosPais();
		
		try (PreparedStatement ps1 = conn.prepareStatement(query)) {
			ps1.setString(1, codigoPais);
			try (ResultSet rs = ps1.executeQuery()) {
				while (rs.next()) {
					pais.IdPais =  rs.getLong("IdPais");
					pais.NombrePais =  rs.getString("NombrePais");
					pais.UsaDivisionPais =  rs.getString("UsaDivisionPais");
					pais.NombreDivisionPais =  rs.getString("NombreDivisionPais");
					pais.EsPaisUs =  rs.getString("EsPaisUs");
					pais.EsOcde =  rs.getString("EsOcde");
					return pais;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pais;
	}
	
}
