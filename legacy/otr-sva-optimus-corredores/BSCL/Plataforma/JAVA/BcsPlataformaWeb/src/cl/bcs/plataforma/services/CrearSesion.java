package cl.bcs.plataforma.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.AgregarLog;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.ProcesaLlamadaServletCall.SesionUpd;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Sesion;
import cl.bcs.utils.SesionUtils;
import cl.bcs.utils.Utils;

/**
 * Servlet implementation class CrearSesion
 */
@WebServlet("/CrearSesion")
public class CrearSesion extends HttpServlet {
	
	//private static final Logger logger = LogManager.getLogger(CrearSesion.class);
	
	private static final long serialVersionUID = 1L;
	
	private static final String CANAL_CORREDORES = "COR";
	private static final String CANAL_NEGOCIOS = "AGE";
	private static final String CANAL_CLIENTES = "CLI";
	private static final String CANAL_WEB_SERVICES = "WS";
	
	public static final String INTENTO_SESION = "INICIO SESION";
	public static final String INICIO_SESION_EXITO = "INICIO SESION EXITOSA";
	public static final String INICIO_SESION_FALLIDA = "INICIO SESION FALLIDA";
	
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrearSesion() {
        super();
    }
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();						
		}
	}
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}

	/**
	 * @throws SQLException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    
    class ErrorDetalle {
    	public String Codigo;
    	public String Descripcion;
    }
    
    class CrearSesionSalida {
    	private String P_OUT_ENTIDAD;
    	public String P_OUT_TOKEN;
    	public Long P_OUT_ADVERTENCIASCONTADOR;
    	public Long P_OUT_ERRORESCONTADOR;
    	public String[] P_OUT_ADVERTENCIASDETALLE; //NO SE USA, SE MANTIENE POR COMPATIBILIDAD
    	public ErrorDetalle[] P_OUT_ERRORESDETALLE;
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
				
		StringBuilder sb = new StringBuilder();
        String s;
		while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        } 
		String bodyParams=sb.toString();
		JsonParser parser 		= new JsonParser();
        JsonObject dataJsonIn	= parser.parse(bodyParams).getAsJsonObject();
        
        String IdEmpresa = (dataJsonIn.get("p_IdEmpresa")!= null && !dataJsonIn.get("p_IdEmpresa").isJsonNull())? dataJsonIn.get("p_IdEmpresa").getAsString() : "";
        String NombreUsuario = (dataJsonIn.get("p_NombreUsuario") !=null &&!dataJsonIn.get("p_NombreUsuario").isJsonNull())? dataJsonIn.get("p_NombreUsuario").getAsString() : "";
        String ClaveUsuario = (dataJsonIn.get("p_ClaveUsuario") != null && !dataJsonIn.get("p_ClaveUsuario").isJsonNull())? dataJsonIn.get("p_ClaveUsuario").getAsString() : "";
        String idCanal = (dataJsonIn.get("p_IdCanal")!= null && !dataJsonIn.get("p_IdCanal").isJsonNull())? dataJsonIn.get("p_IdCanal").getAsString() : "";
        
        
    	try (Connection conn = dataSource.getConnection();){
    		
    		Long idEmpresa = Long.parseLong(IdEmpresa);
    		CrearSesionSalida salida = crearSesionUsuario(conn,idEmpresa,NombreUsuario,ClaveUsuario,idCanal);
    		
    		if (idCanal != null && !salida.P_OUT_ENTIDAD.equalsIgnoreCase("")) {
    			salida.P_OUT_ENTIDAD = "";
    			response.setStatus(200);
				response.getOutputStream().write(salida.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();	
				
    		}
    		else {
     			defaultError(request, response, idCanal);
     		}
    		conn.commit(); 		
		} catch (Exception e) {
			System.out.println("Error al iniciar sesion usuario:" + NombreUsuario);
			e.printStackTrace();
			defaultError(request, response, idCanal);
		}
	}
	
	public static class UsuarioProceso {
		public Long IdUsuario;
		public String Clave;
		public Date FechaVigencia;
		public String IdEstado;
		public Integer IntentosConexion;
		public Timestamp FechaBloqueoTemporal;
		public String EsAutorizado;
		public String TipoEntidad;
		public Long IdEntidad;
		public String UsuarioClientes;
		public String UsuarioNegocios;
		public String UsuarioServicios;
		public String UsuarioCorredores;
		public String IdTokenSesion;
		public String Param_FechaSistema;
		public String Param_IntentosMaximo;
		public Timestamp CurrentTimestamp;
		public String Param_ClaveUsuarioTiempoBloqueo;
		public String IdCanal;
	}
	
	public static class SocioNegocio {
		public Long IdEmpleado;
		public Long IdCliente;
		public Long IdPersona;
		public String IdEstado;
	}
	
	public static class Ses {
		public Long IdUsuario;
		public java.sql.Timestamp FechaUltimoUso;
	}
	
	String queryUsuarios = 
			"SELECT \"IdUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\",\"FechaBloqueoTemporal\",\"EsAutorizado\", \"TipoEntidad\", \"IdEntidad\","
			+ "\"UsuarioClientes\", \"UsuarioNegocios\", \"UsuarioServicios\", \"UsuarioCorredores\",c1.\"Valor\" as \"Param_FechaSistema\",c2.\"Valor\" as \"Param_IntentosMaximo\","
			+ "CURRENT_UTCTIMESTAMP as \"CurrentTimestamp\",c3.\"Valor\" AS \"Param_ClaveUsuarioTiempoBloqueo\" "
			+ "FROM \"BSCL.Plataforma.BD::ACC.Usuario\" u "
			+ "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c1 ON u.\"IdEmpresa\" = c1.\"IdEmpresa\" AND c1.\"CodigoParametro\" = 'PLA.FechaSistema' "
			+ "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c2 ON u.\"IdEmpresa\" = c2.\"IdEmpresa\" AND c2.\"CodigoParametro\" = 'PLA.ClaveUsuarioIntentos' "
			+ "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" c3 ON u.\"IdEmpresa\" = c3.\"IdEmpresa\" AND c3.\"CodigoParametro\" = 'PLA.ClaveUsuarioTiempoBloqueo' "
			+ "WHERE u.\"IdEmpresa\" = :p_IdEmpresa AND \"CodigoUsuario\" = :p_NombreUsuario;";
	
	String updateIntentos = " UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" "
			+ " SET \"IntentosConexion\" = :IntentosConexion "
			+ " WHERE \"IdUsuario\" = :IdUsuario;";
	
	String updateBloqueoTemporal = " UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" "
			+ " SET \"FechaBloqueoTemporal\" = ADD_SECONDS (CURRENT_UTCTIMESTAMP, to_int(:Param_ClaveUsuarioTiempoBloqueo)*60),\"IntentosConexion\" = 0 "
			+ " WHERE \"IdUsuario\" = :IdUsuario;";
	
	String queryEmpleado = "SELECT \"IdEmpleado\","
			+ "CASE \"EMP\".\"IdEstado\" WHEN 'ACT' THEN CASE \"EMP\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
			+ " FROM \"BSCL.SocioNegocio.BD::EMP.Empleado\" \"EMP\""
			+ " WHERE \"IdEmpleado\" = :IdEntidad";
	
	String queryPersona = "SELECT \"IdPersona\","
			+ "CASE \"PER\".\"IdEstado\" WHEN 'ACT' THEN CASE \"PER\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
			+ " FROM \"BSCL.SocioNegocio.BD::PER.Persona\" \"PER\""
			+ " WHERE \"IdPersona\" = :IdEntidad";
	
	String queryCliente = "SELECT \"IdCliente\","
			+ "CASE \"CLI\".\"IdEstado\" WHEN 'ACT' THEN CASE \"CLI\".\"EsEnrolado\" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS \"IdEstado\""
			+ " FROM \"BSCL.SocioNegocio.BD::CLI.Cliente\" \"CLI\""
			+ " WHERE \"IdCliente\" = :IdEntidad";
	
	String queryBuscaToken = "SELECT TOP 1 \"IdTokenSesion\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" where \"TokenValido\" = 'S' and \"IdUsuario\" = :IdUsuario and \"IdCanal\" = :IdCanal";
	
	String insertSesion = "insert into \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") \r\n"
			+ "values (:IdTokenSesion,:IdUsuario,'S',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,:IdCanal);";
	
	String updateSesion = "UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\""
			+ "SET \"FechaUltimoUso\"= :p_FechaUso, \"UsoToken\"= \"UsoToken\" + 1, \"TokenValido\" = :p_TokenValido"
			+ "		where \"IdTokenSesion\" = :p_Token;";
	
	String querySesion = "select \"IdUsuario\", \"FechaUltimoUso\" "
			+ "		 from \"BSCL.Plataforma.BD::ACC.Sesion\""
			+ "		where \"IdTokenSesion\" = :p_Token and \"TokenValido\" = 'S';";
	
	public CrearSesionSalida crearSesionUsuario(Connection conn,Long idEmpresa, String nombreUsuario, String claveUsuario,String idCanal) throws Exception {
		//tipoEntidad = "" equivale a error
		CrearSesionSalida c = new CrearSesionSalida();
		
		c.P_OUT_ADVERTENCIASCONTADOR = 0L;
		c.P_OUT_ADVERTENCIASDETALLE = new String[0];
		c.P_OUT_ENTIDAD="";
		c.P_OUT_ERRORESCONTADOR = 0L;
		c.P_OUT_ERRORESDETALLE = new ErrorDetalle[0];
		c.P_OUT_TOKEN="";
		
		ErrorDetalle defaultError = new ErrorDetalle();
		defaultError.Codigo = "USUARIO_CLAVE_INCORRECTA";
		defaultError.Descripcion = "";
		
		//empresa valida
		if (idEmpresa == null) {
			return c;
		}
		
		SQL _sql = new SQL();		
		HashMap<String,Object> p1 = new HashMap<>();
		p1.put("p_IdEmpresa", idEmpresa);
		p1.put("p_NombreUsuario", nombreUsuario.toUpperCase());
		List<UsuarioProceso> u = _sql.query(conn, queryUsuarios, p1 , UsuarioProceso.class);
		
		//usuario existe?
		if (u.isEmpty() || (u.size() == 1 && u.get(0).IdUsuario == null)) {
			return c;
		}
		
		//aun bloqueado temporalmente?
		if (u.get(0).FechaBloqueoTemporal.after(u.get(0).CurrentTimestamp)) {
			return c;
		}
		
		if (u.get(0).IdEstado.equals("BLO")) {
			return c;
		}
		
		//usuario autorizado?
		if (u.get(0).EsAutorizado.equals("P")) {
			return c;
		}
		
		//usuario vigente?
		if (!Fecha.toStringDate(u.get(0).FechaVigencia).equals("0001-01-01") && u.get(0).FechaVigencia.before(Fecha.toDate(u.get(0).Param_FechaSistema))) {
			return c;
		}
		
		//clave correcta?
		String claveHash = DigestUtils.sha256Hex(claveUsuario.getBytes()).toUpperCase();
		if (!claveHash.equals(u.get(0).Clave)) {
			
			List<UsuarioProceso> list = new ArrayList<>();
			UsuarioProceso u1 = new UsuarioProceso();
			u1.IdUsuario = u.get(0).IdUsuario;
			u1.IntentosConexion = u.get(0).IntentosConexion + 1;
			list.add(u1);
			_sql.batch(conn, updateIntentos, list , UsuarioProceso.class);
			
			//aplica bloqueo temporal
			if (u.get(0).Param_IntentosMaximo != null && u.get(0).IntentosConexion + 1 >= Integer.parseInt(u.get(0).Param_IntentosMaximo)) {
				List<UsuarioProceso> list2 = new ArrayList<>();
				UsuarioProceso u2 = new UsuarioProceso();
				u2.IdUsuario = u.get(0).IdUsuario;
				u2.Param_ClaveUsuarioTiempoBloqueo = u.get(0).Param_ClaveUsuarioTiempoBloqueo;
				list2.add(u2);
				_sql.batch(conn, updateBloqueoTemporal, list2 , UsuarioProceso.class);
			}
			return c;
		}
		
		
		
		if (u.get(0).TipoEntidad.equals("EMP") && ((idCanal.equals(CANAL_CORREDORES) && u.get(0).UsuarioCorredores.equals("S")) 
				|| (idCanal.equals(CANAL_NEGOCIOS) && u.get(0).UsuarioNegocios.equals("S"))
				|| (idCanal.equals(CANAL_WEB_SERVICES) && u.get(0).UsuarioServicios.equals("S")))) {
			
			HashMap<String,Object> hash1 = new HashMap<String,Object>();
			hash1.put("IdEntidad", u.get(0).IdEntidad);
			List<SocioNegocio> lst = _sql.query(conn, queryEmpleado, hash1, SocioNegocio.class);
			if (lst.isEmpty() || lst.get(0).IdEmpleado == null || !lst.get(0).IdEstado.equals("ACT")) {
				return c;
			}
			
			c.P_OUT_ENTIDAD = "EMP";
		}
		else if (u.get(0).TipoEntidad.equals("PER") && u.get(0).UsuarioClientes.equals("S") && idCanal.equals(CANAL_CLIENTES)) {
			HashMap<String,Object> hash1 = new HashMap<String,Object>();
			hash1.put("IdEntidad", u.get(0).IdEntidad);
			List<SocioNegocio> lst = _sql.query(conn, queryPersona, hash1, SocioNegocio.class);
			if (lst.isEmpty() || lst.get(0).IdPersona == null || !lst.get(0).IdEstado.equals("ACT")) {
				return c;
			}
			
			c.P_OUT_ENTIDAD = "PER";
		}
		else if (u.get(0).TipoEntidad.equals("CLI") && (
				(u.get(0).UsuarioServicios.equals("S") && idCanal.equals(CANAL_WEB_SERVICES)) 
				|| (u.get(0).UsuarioClientes.equals("S") && idCanal.equals(CANAL_CLIENTES))
						)) {
			HashMap<String,Object> hash1 = new HashMap<String,Object>();
			hash1.put("IdEntidad", u.get(0).IdEntidad);
			List<SocioNegocio> lst = _sql.query(conn, queryCliente, hash1, SocioNegocio.class);
			if (lst.isEmpty() || lst.get(0).IdCliente == null || !lst.get(0).IdEstado.equals("ACT") && !lst.get(0).IdEstado.equals("BLO")) {
				return c;
			}
			
			c.P_OUT_ENTIDAD = "CLI";
		}
		else {
			//si cae aca, el usuario no esta configurado para el canal indicado
			return c;
		}
		
		Long fechaActual = SesionUtils.ObtenerFechaHoraUTCTs(conn).getTime();
		String idleMaxTime = SesionUtils.GetParametro(conn, idCanal);

		String token = "";
		if (idCanal.equals(CANAL_WEB_SERVICES)) {
			HashMap<String,Object> hash1 = new HashMap<>();
			hash1.put("IdUsuario", u.get(0).IdUsuario);
			hash1.put("IdCanal", idCanal);
			//buscamos un token valido para el usuario
			List<UsuarioProceso> lst = _sql.query(conn, queryBuscaToken, hash1 , UsuarioProceso.class);
			
			if (lst.size() == 1 && lst.get(0).IdTokenSesion != null && !lst.get(0).IdTokenSesion.isEmpty()) {
				HashMap<String,Object> params = new HashMap<>();
				params.put("p_Token", lst.get(0).IdTokenSesion);
				List<Ses> ses = _sql.query(conn, querySesion, params , Ses.class);
				if(ses.size() > 0 && (fechaActual -  ses.get(0).FechaUltimoUso.getTime()) > Long.parseLong(idleMaxTime)) {
					SesionUpd sesionUpd = new SesionUpd();
					sesionUpd.p_FechaUso = new Date(fechaActual);
					sesionUpd.p_Token = lst.get(0).IdTokenSesion;
					sesionUpd.p_TokenValido = "N";

					_sql.batch(conn, updateSesion, Arrays.asList(sesionUpd) , SesionUpd.class);
					conn.commit();
				}else {
					token = lst.get(0).IdTokenSesion;
					c.P_OUT_TOKEN = token;
				}
				
			}
		}
		
		if (token.equals("") || !idCanal.equals(CANAL_WEB_SERVICES)) {
			
			token = Sesion.insertarSesion(conn, idEmpresa, nombreUsuario, u.get(0).IdUsuario, u.get(0).CurrentTimestamp, idCanal, u.get(0).Param_FechaSistema,"");
			
			c.P_OUT_TOKEN = token;
			
			agregarLogSesion(conn,u.get(0).IdUsuario,u.get(0).Param_FechaSistema,idCanal,idEmpresa,INICIO_SESION_EXITO,null,null);

		}

		if (u.get(0).IntentosConexion > 0) {
			List<UsuarioProceso> list = new ArrayList<>();
			UsuarioProceso u1 = new UsuarioProceso();
			u1.IdUsuario = u.get(0).IdUsuario;
			u1.IntentosConexion = 0;
			list.add(u1);
			_sql.batch(conn, updateIntentos, list , UsuarioProceso.class);
		}
		
		return c;
	}

	public static void agregarLogSesion(Connection conn, Long idUsuario,String fechaSistema, String idCanal, Long idEmpresa,String accion, 
			String campoAdicional, String valorAdicional) throws Exception {
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		AgregarLog.Log log = new AgregarLog.Log();
		log.IdTrabajo =idTrabajo;
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = "PLA";
		log.IdUsuario = idUsuario;
		log.IdEmpresa = idEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = idUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "SESION";
		log.EstadoRegistro = "";
		log.IdTipoOperacion = "";
		log.Folio = 0L;
		log.Cantidad = BigDecimal.ZERO;
		log.PrecioTasa = BigDecimal.ZERO;
		log.Monto = BigDecimal.ZERO;
		log.IdMonedaOrigen ="";
		log.IdMonedaLiquidacion = "";
		log.TipoCambio = "";
		log.Nemo = "";
		log.IdProceso = 0L;
		log.EstadoProceso = "";
		log.NombreProceso = "";						
		entradaAgregarLog.p_Log.add(log);
		entradaAgregarLog.p_LogDetalle = new ArrayList<>();
		entradaAgregarLog.p_IdTrabajo = idTrabajo;
		
		AgregarLog.DetalleLog detalleFecha = new AgregarLog.DetalleLog();			
		detalleFecha.IdCabeceraLog = 1L;			
		detalleFecha.SubRegistro = "SESION";
		detalleFecha.CampoRegistro = "FechaIngreso";
		detalleFecha.ValorAntiguo = ""; 
		detalleFecha.IdTrabajo = idTrabajo;
		detalleFecha.ValorNuevo = fechaSistema;	
		entradaAgregarLog.p_LogDetalle.add(detalleFecha);
		AgregarLog.DetalleLog detalleAccion = new AgregarLog.DetalleLog();			
		detalleAccion.IdCabeceraLog = 1L;			
		detalleAccion.SubRegistro = "SESION";
		detalleAccion.CampoRegistro = "ACCION";
		detalleAccion.ValorAntiguo = ""; 
		detalleAccion.ValorNuevo = accion;
		detalleAccion.IdTrabajo = idTrabajo;
		entradaAgregarLog.p_LogDetalle.add(detalleAccion);
		AgregarLog.DetalleLog detalleCanal = new AgregarLog.DetalleLog();			
		detalleCanal.IdCabeceraLog = 1L;			
		detalleCanal.SubRegistro = "SESION";
		detalleCanal.CampoRegistro = "CANAL";
		detalleCanal.ValorAntiguo = ""; 
		detalleCanal.ValorNuevo = idCanal;
		detalleCanal.IdTrabajo = idTrabajo;
		entradaAgregarLog.p_LogDetalle.add(detalleCanal);
		AgregarLog.DetalleLog detalleToken = new AgregarLog.DetalleLog();			
		detalleToken.IdCabeceraLog = 1L;			
		detalleToken.SubRegistro = "SESION";
		detalleToken.CampoRegistro = "TokenValido";
		detalleToken.ValorAntiguo = ""; 
		detalleToken.ValorNuevo = "S";
		detalleToken.IdTrabajo = idTrabajo;
		entradaAgregarLog.p_LogDetalle.add(detalleToken);
		
		if (campoAdicional != null) {
			AgregarLog.DetalleLog detalleAdicional = new AgregarLog.DetalleLog();			
			detalleAdicional.IdCabeceraLog = 1L;			
			detalleAdicional.SubRegistro = "SESION";
			detalleAdicional.CampoRegistro = campoAdicional;
			detalleAdicional.ValorAntiguo = ""; 
			detalleAdicional.ValorNuevo = valorAdicional;
			detalleAdicional.IdTrabajo = idTrabajo;
			entradaAgregarLog.p_LogDetalle.add(detalleAdicional);
				
		}
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);
	    
	}
	

	private void defaultError(HttpServletRequest request, HttpServletResponse response, String idCanal) throws IOException{
    	JsonObject errorOut = new JsonObject();
    	errorOut.addProperty("p_out_ErroresContador".toUpperCase(), 1);
    	JsonArray errorDetalleOut = new JsonArray();
    	
    	JsonObject obj = new JsonObject();
		obj.addProperty("CODIGO", "USUARIO_CLAVE_INCORRECTA");
		obj.addProperty("Descripcion", "Usuario o contrasena incorrecta.");
    	errorDetalleOut.add(obj);
    	errorOut.add("p_out_ErroresDetalle".toUpperCase(), errorDetalleOut);
    	
    	response.setStatus(401);
		response.getOutputStream().write(errorOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}
	
	/*public  static void main(String [] args) throws Exception {
		CrearSesion s = new CrearSesion();
		Connection conn = cl.bcs.plataforma.pla.utils.PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"); 
		CrearSesionSalida asdf = s.crearSesionUsuario(conn, 1L, "SISTEMA", "6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b", "COR");
		conn.commit();
		System.out.println(asdf);
	}*/
	
}
