package cl.bcs.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class SesionUtils {
	
	public static class Parametro{
		public String Valor;
	}
	
	public static String queryUsuario = "select \"IdEmpresa\",\"FechaVigencia\",\"IdEstado\",\"CodigoUsuario\", \"TipoEntidad\""
			+ "		  from \"BSCL.Plataforma.BD::ACC.Usuario\""
			+ "		where \"IdUsuario\" = :p_IdUsuario";
	
	public static String querySesion = "select \"IdUsuario\", \"FechaUltimoUso\" "
			+ "		 from \"BSCL.Plataforma.BD::ACC.Sesion\""
			+ "		where \"IdTokenSesion\" = :p_Token and \"TokenValido\" = 'S';";
	
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
		
	static Parametro parametroCLI = null;
	static Parametro parametroWS = null;
	static Parametro parametroAGE = null;

	
	public static String GetParametro(Connection conn, String canal) throws SQLException {
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL _sql = new SQL();
		String parametro = "";
		
		switch(canal) {
		case "WS":
			if (parametroWS == null) {
				params.put("p_Parametro", "PLA.IdleMaxTimeWS");
				parametroWS = _sql.query(conn, queryParametro, params , Parametro.class).get(0);
				parametro = parametroWS.Valor;
			}else {
				parametro = parametroWS.Valor;
			}
		  break;
		case "CLI":
			if (parametroCLI == null) {
				params.put("p_Parametro", "PLA.IdleMaxTimeCLI");
				parametroCLI = _sql.query(conn, queryParametro, params , Parametro.class).get(0);
				parametro = parametroCLI.Valor;
			}else {
				parametro = parametroCLI.Valor;
			}
			break;
		  case "AGE":
			  if (parametroAGE == null) {
				  params.put("p_Parametro", "PLA.IdleMaxTimeAGE");
				  parametroAGE = _sql.query(conn, queryParametro, params , Parametro.class).get(0);
				  parametro = parametroAGE.Valor;
			  }else {
				  parametro = parametroAGE.Valor;
			  }
			break;
		  default:
			  parametro = "1800000";
			  break;
		}

		return parametro;
	}
	
	private static Timestamp	cacheCurrentUTC=null;
	private static long 		ultTimeMillis=0;
	
	public static class TsUTC {

		public Timestamp UTC;
		
	}
	
	private static void updateCache(Connection con) throws Exception {
		if (System.currentTimeMillis()-ultTimeMillis>3600000) {
			cacheCurrentUTC=null;
			SQL sql = new SQL();
			List<TsUTC> tss = sql.query(con, "SELECT CURRENT_UTCTIMESTAMP as \"UTC\"  FROM DUMMY", new HashMap<>(), TsUTC.class);
			cacheCurrentUTC = tss.get(0).UTC;
			ultTimeMillis=System.currentTimeMillis();
		}
		if (cacheCurrentUTC==null) throw new Exception("No se puede obtener cache UTC");
	}
	
	public static Timestamp ObtenerFechaHoraUTCTs(Connection con) throws Exception {
		updateCache(con);
		int diffCache=(int)(System.currentTimeMillis()-ultTimeMillis);			
		return Fecha.addMilliseconds(cacheCurrentUTC, diffCache);
		
	}	
	
}