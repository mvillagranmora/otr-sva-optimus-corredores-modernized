package cl.bcs.APV;

import java.sql.Connection;
import java.util.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ProcesarDistribucion.SalidaRecaudacionTraspaso;
import cl.bcs.tesoreria.pojo.Traspaso.RegistroRecaudacion;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;


public class ValidarMandatosPAC implements JavaCall {
	
	private final Log log 					= new Log("TES.ProcesarDistribucion");
	public String  SQL_UPDATE_LOGMANDATOS   = loadResourceToString("cl/bcs/APV/sql/ValidarMandatosPAC_INSERT_LogMandatoPAC.sql"); 

	
	public static class entrada_ValidarMandatosPAC{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <RegistroRecaudacion> P_OUT_SOLICITUDES_PROCESADAS;
		public List <RegistroRecaudacion> P_OUT_SOLICITUDES_NO_PROCESADAS;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public entrada_ValidarMandatosPAC(){
			this.P_OUT_ERRORESCONTADOR			=	0L;
			this.P_OUT_MENSAJES 				= 	new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO 				= 	new ArrayList<ResultadoSolicitud>();
			this.P_OUT_SOLICITUDES_PROCESADAS 	= 	new ArrayList<RegistroRecaudacion>();
			this.P_OUT_SOLICITUDES_NO_PROCESADAS = 	new ArrayList<RegistroRecaudacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LogValidarMandatosPAC
	{
		public Long			IdMandatoPAC;
		public String 		IdentServicio;
		public Date			FechaProceso;
		public Date			FechaSistema;
		public String		EstadoProcesamiento;
		public String		PreRendicion;
		public String 		Usuario;
		public String 		Glosa;
	}
	
	//----------------------------------------------------------------------------------------------------
	
	public void logica(Connection con, SalidaRecaudacionTraspaso entrada, Long IdEmpresa, Long IdUsuario) throws Exception{
		
		log.write("Inicio ValidarMandatosPAC");
		
		SQL sql 			= new SQL();	
		
		List<RegistroRecaudacion> solAGuardarOK		= new ArrayList<RegistroRecaudacion>(); 
		List<RegistroRecaudacion> solAGuardarNOK	= new ArrayList<RegistroRecaudacion>();
		Date fechaSistema 							= Fecha.toDate(ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		
		Map<Object, List<RegistroRecaudacion>> hashNoPro = entrada.P_OUT_SOLICITUDES_NO_PROCESADAS.parallelStream().collect(Collectors.groupingBy(tra -> tra.IdMandatoPAC));

		solAGuardarOK 	= entrada.P_OUT_SOLICITUDES_A_PROCESAR.parallelStream().filter(aP -> !hashNoPro.containsKey(aP.IdMandatoPAC)).collect(Collectors.toList());
		solAGuardarNOK 	= entrada.P_OUT_SOLICITUDES_NO_PROCESADAS.parallelStream().collect(Collectors.toList());
		

		
		/*****************************************************
		 ** 		INSERTAR MANDATOS PROCESADOS			**
		 *****************************************************/
		if(solAGuardarOK.size() > 0) {
			InsertarMandatosProcesados(con, sql, solAGuardarOK, fechaSistema, IdUsuario);
		}
		
		
		/*****************************************************
		 ** 		INSERTAR MANDATOS NO PROCESADOS			**
		 *****************************************************/
		if (solAGuardarNOK.size() > 0) {
			InsertarMandatosNoProcesados(con, sql, solAGuardarNOK, fechaSistema, IdUsuario);
		}
		
		log.write("Fin ValidarMandatosPAC");

	}
	
	private void InsertarMandatosProcesados(Connection con, SQL sql, List<RegistroRecaudacion> solAGuardarOK, Date fechaSistema, Long IdUsuario) throws SQLException {

		log.write("Inicio InsertarMandatosProcesados");
		
		List<LogValidarMandatosPAC> LogMandatos = new ArrayList<LogValidarMandatosPAC>();
		for (RegistroRecaudacion NoPro : solAGuardarOK) {
			
			LogValidarMandatosPAC np 	= new LogValidarMandatosPAC();
			
			np.IdentServicio		= NoPro.IdentServicio;
			np.IdMandatoPAC			= NoPro.IdMandatoPAC;
			np.FechaSistema			= fechaSistema;
			np.Usuario				= IdUsuario.toString();
			np.EstadoProcesamiento 	= "PRO";
			np.PreRendicion			= "S";
			np.Glosa				= "Procesado.";
			
			LogMandatos.add(np);
		}

		if (LogMandatos.size() > 0) {
			sql.batch(con, SQL_UPDATE_LOGMANDATOS, LogMandatos, LogValidarMandatosPAC.class);  
		}
		log.write("Fin InsertarMandatosProcesados");
	}

	private void InsertarMandatosNoProcesados(Connection con, SQL sql, List<RegistroRecaudacion> solAGuardarNOK, Date fechaSistema, Long IdUsuario) throws SQLException {
		
		log.write("Inicio InsertarMandatosNoProcesados");
		
		List<LogValidarMandatosPAC> LogMandatos = new ArrayList<LogValidarMandatosPAC>();
		for (RegistroRecaudacion NoPro : solAGuardarNOK) {
			
			LogValidarMandatosPAC np 	= new LogValidarMandatosPAC();
			
			np.IdentServicio		= NoPro.IdentServicio;
			np.IdMandatoPAC			= NoPro.IdMandatoPAC;
			np.FechaSistema			= fechaSistema;
			np.EstadoProcesamiento 	= "PEN";
			np.PreRendicion			= "S";
			np.Usuario				= IdUsuario.toString();
			np.Glosa				= NoPro.EstadoRendicion;
			
			LogMandatos.add(np);
		}

		if (LogMandatos.size() > 0) {
			sql.batch(con, SQL_UPDATE_LOGMANDATOS, LogMandatos, LogValidarMandatosPAC.class); //Cambiar por update
		}
		
		log.write("Fin InsertarMandatosNoProcesados");
		
	}

	
	//----------------------------------------------------------------------------------------------------


	public static void main(String[] args) {

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
