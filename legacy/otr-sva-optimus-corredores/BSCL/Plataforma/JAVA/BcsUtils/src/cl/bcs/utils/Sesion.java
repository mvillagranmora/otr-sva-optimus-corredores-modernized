package cl.bcs.utils;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;

public class Sesion {

	public static class SesionData {
		public Long IdEmpresa;
		public String IdTokenSesion;
		public Long IdUsuario;
		public String NombreEmpresa;
		public String NombreUsuario;
		public String CodigoMenuUsuario;
		public Long IdSocioDeNegocio;
		public Long IdPersonaCorredora;
		public String IdMonedaSistema;
		public Long IdPaisSistema;
		public String FechaSistema;
		public String BolsaDefectoSistema;
		public List<ValorDefectoSistema> ValoresDefectoSistema;
		public Long IdEmpleado;
		public Long IdCliente;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Mensaje> P_OUT_MENSAJES;
		public Long IdCorredoraSistema;
		public String IdEntidadSSO;
		public String SSORedirectURL;
		
		public SesionData() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		

	}
	
	static class Mensaje {
		public Mensaje(String string, String string2) {
			Codigo = string;
			Descripcion = string2;
		}
		String Codigo;
		String Descripcion;
	}
	
	static class ValorDefectoSistema {
		public String IdValorDefectoSistema;
		public String Nombre;
		public String Valor;
		public Long IdEmpresa;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		

	}
	
	static class InformacionUsuario {
		public String IdTokenUsuario;
		public Long IdUsuario;
		public Long IdEmpresa;
		public java.sql.Date FechaVigencia;
		public String IdEstado;
		public String CodigoUsuario;
		public Long IdEntidad;
		public String CodigoMenu;
		public Long IdEmpleado;
		public Long IdCliente;
		public String IdEntidadSSO;
		public String RedirectURL;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	static class Configuracion {
		public String CodigoParametro;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	static class Empresa {
		public Long IdPais;
		public String Nombre;
		public Long IdSocioDeNegocio;
		public Long IdPersona;
		public Long IdCorredora;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//query sesion y usuario
	static String queryUsuario = 
			"SELECT U.\"IdUsuario\",U.\"IdEmpresa\",\"FechaVigencia\",S.\"IdEntidadSSO\",SSO.\"RedirectURL\",U.\"IdEstado\",U.\"CodigoUsuario\",U.\"IdEntidad\",U.\"CodigoMenu\",EMP.\"IdEmpleado\",CLI.\"IdCliente\" " +
			" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" S " +
			" INNER JOIN \"BSCL.Plataforma.BD::ACC.Usuario\" U on U.\"IdUsuario\" = S.\"IdUsuario\" "+ 
			" LEFT JOIN \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" SSO on SSO.\"IdEntidad\" = S.\"IdEntidadSSO\" "+ 
			" LEFT JOIN \"BSCL.SocioNegocio.BD::EMP.Empleado\" EMP ON U.\"TipoEntidad\" = 'EMP' AND U.\"IdEntidad\" = EMP.\"IdEmpleado\" " +
			" LEFT JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" CLI ON U.\"TipoEntidad\" = 'CLI' AND U.\"IdEntidad\" = CLI.\"IdCliente\" " +
			" WHERE \"IdTokenSesion\" = :IdTokenSesion " +
			" AND \"TokenValido\" = 'S' ";
	
	//query parametros
	static String queryParametros = 
			"SELECT \"CodigoParametro\",\"Valor\" " + 
			" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" p " +
			" WHERE \"IdEmpresa\" = :IdEmpresa " +
			" AND \"CodigoParametro\" IN (:Parametros) ";
	
	//query empresa
	static String queryEmpresa = 
			"SELECT e.\"IdPais\",e.\"Nombre\",e.\"IdSocioDeNegocio\",soc.\"IdPersona\",c.\"IdCorredora\" " +
			" FROM \"BSCL.Plataforma.BD::PLA.Empresa\" e " +
			" LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" soc on e.\"IdSocioDeNegocio\" = soc.\"IdSocioDeNegocio\" " +
			" LEFT JOIN \"BSCL.Plataforma.BD::INS.Corredora\" c ON c.\"IdPersona\" = soc.\"IdPersona\" " +
			" WHERE e.\"IdEmpresa\" = :IdEmpresa";	

	
	//query valores por defecto
	static String queryValoresPorDefecto = 
			"SELECT \"IdValorDefectoSistema\",\"Nombre\",\"Valor\",\"IdEmpresa\",\"IdEstado\" " +
			" FROM \"BSCL.Plataforma.BD::PLA.ValorDefectoSistema\"" +
			" WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdEstado\" = 'ACT'";
	
	static String updateUsuario = 
			"UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" " + 
			" SET \"FechaUltimaConexion\" = CURRENT_UTCTIMESTAMP,\"Conectado\" = 'S',\"Hora\" = CURRENT_UTCTIMESTAMP" + 
			" WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdUsuario\" = :IdUsuario;"; 
	
	static String updateSesion = 
			"UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" " + 
			" SET \"FechaUltimoUso\" = CURRENT_UTCTIMESTAMP,\"UsoToken\" = \"UsoToken\" + 1" + 
			" WHERE \"IdTokenSesion\" = :IdTokenUsuario;"; 
	
	static String insertSesion = "insert into \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\",\"IdEntidadSSO\") \r\n"
			+ "values (:IdTokenSesion,:IdUsuario,'S',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,:IdCanal,:IdEntidadSSO);";
	
	static String closeSesion = 
			"UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" " + 
			" SET \"TokenValido\" = 'N'" + 
			" WHERE \"IdUsuario\" = :IdUsuario AND \"IdCanal\" = :IdCanal AND \"TokenValido\" = 'S'" +
			"  AND ( " +
		    "      SELECT \"Valor\" " +
		    "      FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
		    "      WHERE \"CodigoParametro\" = 'PLA.SesionUnicaUsuario' " +
		    "  ) = 'S'" +
		    "   AND EXISTS ( " +
	        "        SELECT 1 " +
	        "        FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" c " +
	        "        WHERE c.\"CodigoParametro\" = 'PLA.CanalesSesionUnica' " +
	        "          AND ';' || c.\"Valor\" || ';' LIKE '%;' || \"IdCanal\" || ';%' " +
	        "   )";

	
	public static class UsuarioProceso {
		public Long IdUsuario;
		public String IdTokenSesion;
		public String IdCanal;
		public String IdEntidadSSO;
	}
	
	public final static String insertarSesion(Connection conn,Long idEmpresa, String nombreUsuario,Long idUsuario,
			Timestamp CurrentTimestamp,String idCanal,String fechaSistema, String idEntidadSSO) throws Exception {
		
		SQL _sql = new SQL();
		String datatoken = idEmpresa + ":" + nombreUsuario + ":" + CurrentTimestamp + UUID.randomUUID().toString();
		String token = new String(Base64.encodeBase64(datatoken.getBytes()));
		
		List<UsuarioProceso> ins = new ArrayList<>();
		UsuarioProceso p = new UsuarioProceso();
		p.IdUsuario = idUsuario;
		p.IdTokenSesion = token;
		p.IdCanal = idCanal;
		p.IdEntidadSSO = idEntidadSSO;
		ins.add(p);
		_sql.batch(conn, closeSesion, ins , UsuarioProceso.class);
		_sql.batch(conn, insertSesion, ins , UsuarioProceso.class);
		
		
		return token;
		
	}
	
	
	
	public final static SesionData getSesionData(Connection conn,String pTokenSession) throws Exception {
		
		SesionData s = new SesionData(); 
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdTokenSesion", pTokenSession);
		List<InformacionUsuario> informacionUsuario  = _sql.query(conn, queryUsuario, params, InformacionUsuario.class);
		
		if (informacionUsuario.size() == 0) {
			Mensaje m = new Mensaje("SESION_INVALIDA","Sesion invalida");
			s.P_OUT_MENSAJES.add(m);
			s.P_OUT_ERRORESCONTADOR = 1L;
			return s;
		}
		
		informacionUsuario.get(0).IdTokenUsuario = pTokenSession;
		
		//validacion usuario bloqueado
		if (!informacionUsuario.get(0).IdEstado.equalsIgnoreCase("ACT")) {
			Mensaje m = new Mensaje("USUARIO_BLOQUEADO","El usuario " + informacionUsuario.get(0).CodigoUsuario + " se encuentra bloqueado.");
			s.P_OUT_MENSAJES.add(m);
			s.P_OUT_ERRORESCONTADOR = 1L;
			return s;
		}
		
		HashMap<String, Object> parametros = new HashMap<>();
		{
			List<String> parametrosLectura = new ArrayList<>();
			parametrosLectura.add("PLA.ZonaHoraria");
			parametrosLectura.add("PLA.FechaSistema");
			parametrosLectura.add("PLA.MonedaSistema");
			parametrosLectura.add("PLA.BolsaDefectoSistema");
			
			params.clear();
			params.put("IdEmpresa",informacionUsuario.get(0).IdEmpresa);
			params.put("Parametros",parametrosLectura);
			List<Configuracion> _parametros = _sql.query(conn, queryParametros, params, Configuracion.class);
			parametros = Utils.List2Hash(_parametros, "CodigoParametro");
		}
		
		
		//validacion usuario vigencia
		if (!Fecha.toStringDate(informacionUsuario.get(0).FechaVigencia).equalsIgnoreCase("0001-01-01") && 
				informacionUsuario.get(0).FechaVigencia.getTime() < Fecha.toDate(((Configuracion)parametros.get("PLA.FechaSistema")).Valor).getTime()) {
			Mensaje m = new Mensaje("USUARIO_BLOQUEADO","El usuario " + informacionUsuario.get(0).CodigoUsuario + " se encuentra vencido.");
			s.P_OUT_MENSAJES.add(m);
			s.P_OUT_ERRORESCONTADOR = 1L;
			return s;
		}

		//validacion usuario valido
		if (informacionUsuario.get(0).IdEmpleado == 0 && informacionUsuario.get(0).IdCliente == 0) {
			Mensaje m = new Mensaje("USUARIO_NO_VALIDO","El usuario " + informacionUsuario.get(0).CodigoUsuario + " no es valido.");
			s.P_OUT_MENSAJES.add(m);
			s.P_OUT_ERRORESCONTADOR = 1L;
			return s;
		}
		
		params.clear();
		params.put("IdEmpresa",informacionUsuario.get(0).IdEmpresa);
		List<Empresa> informacionEmpresa = _sql.query(conn, queryEmpresa, params, Empresa.class);
		
		if (informacionEmpresa.size() != 1) {
			Mensaje m = new Mensaje("EMPRESA_MAL_CONFIGURADA","La empresa no se encuentra configurada correctamente.");
			s.P_OUT_MENSAJES.add(m);
			s.P_OUT_ERRORESCONTADOR = 1L;
			return s;
		}
		
		params.clear();
		params.put("IdEmpresa", informacionUsuario.get(0).IdEmpresa);
		List<ValorDefectoSistema> valoresDefecto = _sql.query(conn, queryValoresPorDefecto, params, ValorDefectoSistema.class);
		
		s.BolsaDefectoSistema = ((Configuracion) parametros.get("PLA.BolsaDefectoSistema")).Valor;
		s.CodigoMenuUsuario = informacionUsuario.get(0).CodigoMenu;
		s.FechaSistema  = ((Configuracion) parametros.get("PLA.FechaSistema")).Valor;
		s.IdCliente = informacionUsuario.get(0).IdCliente;
		s.IdEmpleado = informacionUsuario.get(0).IdEmpleado;
		s.IdEmpresa = informacionUsuario.get(0).IdEmpresa;
		s.IdMonedaSistema = ((Configuracion) parametros.get("PLA.MonedaSistema")).Valor;
		s.IdPaisSistema = informacionEmpresa.get(0).IdPais;
		s.IdPersonaCorredora = informacionEmpresa.get(0).IdPersona;
		s.IdSocioDeNegocio = informacionEmpresa.get(0).IdSocioDeNegocio;
		s.IdUsuario = informacionUsuario.get(0).IdUsuario;
		s.NombreEmpresa = informacionEmpresa.get(0).Nombre;
		s.NombreUsuario = informacionUsuario.get(0).CodigoUsuario;
		s.IdCorredoraSistema = informacionEmpresa.get(0).IdCorredora;
		s.IdEntidadSSO = informacionUsuario.get(0).IdEntidadSSO;
		s.SSORedirectURL = informacionUsuario.get(0).RedirectURL;
		s.ValoresDefectoSistema = valoresDefecto;
		
		//update sesion
		
		_sql.batch(conn,updateSesion, informacionUsuario, InformacionUsuario.class);
		_sql.batch(conn,updateUsuario, informacionUsuario, InformacionUsuario.class);
		
		return s;
	}
	public static void main(String [] args) throws Exception {
		
		String dbURL  = "jdbc:sap://161.238.0.14:30215/?";
		String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "DESARROLLO";
        String pass   = "Bolsa123";	
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
		SQL.exec(conn, "set schema BSCL");
		SQL.exec(conn, "SET TRANSACTION AUTOCOMMIT DDL OFF");
		
		SesionData asdf = Sesion.getSesionData(conn, "MTMxMzp0ZXN0MToyMDE3LTExLTMwIDE3OjAzOjE2LjAyNzAwMDA=");
		System.out.println(asdf);
	}
}
