package cl.bcs.utils;

import java.sql.Connection;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProcesaLlamadaServletCall {
	//private static final Logger logger = LogManager.getLogger(ProcesaLlamadaServletCall.class);
	public static class SalidaProcesaLlamadaServletCall {

		public String p_out_PermiteUso;
		public long p_out_IdEmpresa;
		public long p_out_IdUsuario;
		public String p_out_NombreUsuario;
		public String p_out_TipoUsuario;
		public String p_out_NombreServicio;
		public String p_out_Extras;
		public String p_out_TipoServicio;
		public String p_out_ServicioTempIn;
		public String p_out_ServicioTempOut;
		public String p_out_Mensaje;
			
	}
	
	public static class EntradaProcesaLlamadaServletCall {

		public String p_Token;
		public int p_IdServicio;
		public String p_IdTransaccion;
		public String p_IdCanal = "COR";
		
	}
	
	public static class Servicio {
		public String RutaServicio;
		public String TipoServicio;
		public String TablasTempIN;
		public String TablasTempOUT;
		public String RegistraUso;
		public String Extras;
	}

	public static class Sesion {
		public Long IdUsuario;
		public java.sql.Timestamp FechaUltimoUso;
	}
	
	public static class Usuario {
		public Long IdEmpresa;
		public Date FechaVigencia;
		public String IdEstado;
		public String CodigoUsuario;
		public String TipoEntidad;
	}
	
	public static class SesionUpd{
		public String p_Token;
		public Date p_FechaUso;
		public String p_TokenValido;
	}
	
	public static class Parametro{
		public String Valor;
	}
	
	public static String queryUsuario = "select \"IdEmpresa\",\"FechaVigencia\",\"IdEstado\",\"CodigoUsuario\", \"TipoEntidad\""
			+ "		  from \"BSCL.Plataforma.BD::ACC.Usuario\""
			+ "		where \"IdUsuario\" = :p_IdUsuario";
	
	public static String querySesion = "select \"IdUsuario\", \"FechaUltimoUso\" "
			+ "		 from \"BSCL.Plataforma.BD::ACC.Sesion\""
			+ "		where \"IdTokenSesion\" = :p_Token and \"TokenValido\" = 'S' and \"IdCanal\" IN (:p_IdCanal, 'SSO');";
	
	public static String queryServicio = 
			"select srv.\"RutaServicio\",srv.\"TipoServicio\",srv.\"TablasTempIN\",srv.\"TablasTempOUT\",srv.\"RegistraUso\",coalesce(string_agg(srvextdet.\"RutaServicio\",','),'') as \"Extras\""
			+ "		from \"BSCL.Plataforma.BD::PLA.Servicio\" srv"
			+ "		left join \"BSCL.Plataforma.BD::PLA.ServicioExtra\" srvext on srv.\"IdServicio\" = srvext.\"IdServicio\" and srvext.\"IdCanal\" = :p_IdCanal "
			+ "		left join \"BSCL.Plataforma.BD::PLA.Servicio\" srvextdet on srvext.\"IdServicioExtra\" = srvextdet.\"IdServicio\" "
			+ "		where srv.\"IdServicio\" = :p_IdServicio "
			+ "		group by srv.\"RutaServicio\",srv.\"TipoServicio\",srv.\"TablasTempIN\",srv.\"TablasTempOUT\",srv.\"RegistraUso\"";
	
	public static String updateSesion = "UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\""
			+ "SET \"FechaUltimoUso\"= :p_FechaUso, \"UsoToken\"= \"UsoToken\" + 1, \"TokenValido\" = :p_TokenValido"
			+ "		where \"IdTokenSesion\" = :p_Token;";
	
	public static String queryParametro = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = :p_Parametro ";
	
	public SalidaProcesaLlamadaServletCall logica(Connection conn,Connection auditConn, EntradaProcesaLlamadaServletCall entrada) throws Exception {
		SalidaProcesaLlamadaServletCall salida = new SalidaProcesaLlamadaServletCall();
		
		if (entrada.p_IdCanal == null) {
			entrada.p_IdCanal = "COR";
		}
		
		Parametro idleMaxTime = new Parametro();
		idleMaxTime.Valor = SesionUtils.GetParametro(conn, entrada.p_IdCanal); //valor por defecto, siempre en milisegundos
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();		
		
		params.clear();
		params.put("p_IdServicio", entrada.p_IdServicio);
		params.put("p_IdCanal", entrada.p_IdCanal);
		List<Servicio> srv = _sql.query(conn, queryServicio, params , Servicio.class);
		if (srv.size() == 0) {
			salida.p_out_Mensaje = "SERVICIO INVALIDO";
			salida.p_out_PermiteUso = "N";
			return salida;
		}
		
		params.clear();
		params.put("p_Token", entrada.p_Token);
		params.put("p_IdCanal", entrada.p_IdCanal);
		List<Sesion> ses = _sql.query(conn, querySesion, params , Sesion.class);
		if (ses.size() == 0) {
			salida.p_out_Mensaje = "TOKEN INVALIDO";
			salida.p_out_PermiteUso = "N";
			return salida;
		}else {
			Long fechaActual = SesionUtils.ObtenerFechaHoraUTCTs(conn).getTime();
			if(!entrada.p_IdCanal.equalsIgnoreCase("COR") && (fechaActual -  ses.get(0).FechaUltimoUso.getTime()) > Long.parseLong(idleMaxTime.Valor)) {
				SesionUpd sesionUpd = new SesionUpd();
				sesionUpd.p_FechaUso = new Date(fechaActual);
				sesionUpd.p_Token = entrada.p_Token;
				sesionUpd.p_TokenValido = "N";

				_sql.batch(conn, updateSesion, Arrays.asList(sesionUpd) , SesionUpd.class);
				conn.commit();
				
				salida.p_out_Mensaje = "TOKEN INVALIDO";
				salida.p_out_PermiteUso = "N";
				return salida;
			}
		}
		
		params.clear();
		params.put("p_IdUsuario", ses.get(0).IdUsuario);
		List<Usuario> usuario = _sql.query(conn, queryUsuario, params , Usuario.class);
		if (ses.size() == 0) {
			salida.p_out_Mensaje = "TOKEN INVALIDO";
			salida.p_out_PermiteUso = "N";
			return salida;
		}
		
		params.clear();
		params.put("p_Parametro", "PLA.FechaSistema");
		List<Parametro> fec = _sql.query(conn, queryParametro, params , Parametro.class);
		if (fec.size() == 0) {
			salida.p_out_Mensaje = "FECHA SISTEMA NO ENCONTRADA";
			salida.p_out_PermiteUso = "N";
			return salida;
		}
		
		if (!usuario.get(0).FechaVigencia.toString().equalsIgnoreCase("0001-01-01")) {
			if (usuario.get(0).FechaVigencia.compareTo((Date.valueOf(fec.get(0).Valor))) == -1) {
				salida.p_out_Mensaje = "CUENTA BLOQUEADA O INACTIVA";
				salida.p_out_PermiteUso = "N";
				return salida;
			}
		}
		
		if (usuario.get(0).IdEstado.equalsIgnoreCase("BLO")) {
			salida.p_out_Mensaje = "CUENTA BLOQUEADA O INACTIVA";
			salida.p_out_PermiteUso = "N";
			return salida;
		}
	
		if(!entrada.p_IdCanal.equalsIgnoreCase("COR")) {
			SesionUpd sesionUpd = new SesionUpd();
			sesionUpd.p_FechaUso = new Date(SesionUtils.ObtenerFechaHoraUTCTs(conn).getTime());
			sesionUpd.p_Token = entrada.p_Token;
			sesionUpd.p_TokenValido = "S";
			
			_sql.batch(conn, updateSesion, Arrays.asList(sesionUpd) , SesionUpd.class);
			conn.commit();
		}


		Utils.registrarAuditoriaServicios(auditConn, entrada.p_IdServicio,srv.get(0).RutaServicio, ses.get(0).IdUsuario, 
				entrada.p_IdTransaccion, "INICIO", null, null, null, null);
		
		salida.p_out_PermiteUso = "S";
		salida.p_out_Mensaje = "OK++";
		salida.p_out_Extras = srv.get(0).Extras;
		salida.p_out_IdEmpresa = usuario.get(0).IdEmpresa;
		salida.p_out_IdUsuario = ses.get(0).IdUsuario;
		salida.p_out_NombreServicio = srv.get(0).RutaServicio;
		salida.p_out_NombreUsuario = usuario.get(0).CodigoUsuario;
		salida.p_out_ServicioTempIn = srv.get(0).TablasTempIN;
		salida.p_out_ServicioTempOut = srv.get(0).TablasTempOUT;
		salida.p_out_TipoServicio = srv.get(0).TipoServicio;
		salida.p_out_TipoUsuario = usuario.get(0).TipoEntidad;
		
		return salida;
		
	}
	
	/*public static void main(String [] args) throws Exception {
		ProcesaLlamadaServletCall p = new ProcesaLlamadaServletCall();
		Connection conn = cl.bcs.plataforma.pla.utils.PlataformaUtils.getPropertiesConnection("becbpro1", "connections.properties"); 
		
		EntradaProcesaLlamadaServletCall x = new EntradaProcesaLlamadaServletCall();
		x.p_IdCanal = "AGE";
		x.p_IdServicio = 3005;
		x.p_Token = "MTpzaXN0ZW1hOjIwMjYtMDgtMTcgMTU6MzU6NTIuODc3NTE0ZDYwNzEtMzJhYy00ZWU5LTgyOTAtMDg5Mjk5ZDE3MTEz";
		x.p_IdTransaccion = "INICIO";
		p.logica(conn, conn, x );
		
	}*/
	
}
