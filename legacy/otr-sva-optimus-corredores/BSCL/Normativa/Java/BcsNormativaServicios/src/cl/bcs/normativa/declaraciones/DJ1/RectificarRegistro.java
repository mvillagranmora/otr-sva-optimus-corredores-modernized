package cl.bcs.normativa.declaraciones.DJ1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class RectificarRegistro implements JavaCall {

	static final Logger log = LogManager.getLogger(RectificarRegistro.class);
	
	private final String SQL_SELECT_REGISTROS		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/RectificarRegistro_SelectRegistroRectificar.sql");	
	private final String SQL_UPDATE_REGISTROS		= loadResourceToString("cl/bcs/normativa/declaraciones/DJ1/sql/RectificarRegistro_UpdateRegistros.sql");

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarRegistros_Entrada 
	{		
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_UsaReglasNegocio;
		public String		p_FechaProceso;
		public String		p_Rectifica;
		public List<Registro> p_List;

		public ProcesarRegistros_Entrada()
		{
			this.p_List = new ArrayList<Registro>();	
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Registro
	{
		public Long			ID; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ProcesarRegistro 
	{
		public Long			ID;
		public String		KIdContrato;
		public String		Campo;
		public String		ValorAntiguo;
		public String		ValorNuevo;
	 	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarRegistros_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		
		public ProcesarRegistros_Salida()
		{	
			this.P_OUT_ERRORESCONTADOR 		= 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------
	
	public static class RegistrosInvalidos extends BcsPojo{
		public Long			Id;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		ProcesarRegistros_Entrada in = gson.fromJson(jin.toString(), ProcesarRegistros_Entrada.class);
		ProcesarRegistros_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void agregarLog(Connection con, ProcesarRegistros_Entrada entrada, List<ProcesarRegistro> cambioRegistro) throws Exception {
		log.debug("INICIO::RectificarRegistro.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogValor = cambioRegistro.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdCabeceraLog = solicitud.ID;
			detalle.SubRegistro = solicitud.KIdContrato;
			detalle.CampoRegistro = solicitud.Campo;
			detalle.ValorAntiguo = solicitud.ValorAntiguo;
			detalle.ValorNuevo = solicitud.ValorNuevo;
			return detalle;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogValor);
		
		entradaAgregarLog.p_Log = cambioRegistro.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = solicitud.ID;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "RECTIFICACION";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.NombreProceso = "Cambia estado Rectifica para registro SIID";
			log.IdRegistro = String.valueOf(solicitud.ID);
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		log.debug("FIN::RectificarRegistro.agregarLog", fin - inicio);
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ProcesarRegistros_Salida logica(Connection con, ProcesarRegistros_Entrada in) throws Exception {
		
		final Logger log = LogManager.getLogger(ProcesarRegistros_Salida.class);
		
		log.info("== INICIO PROCESAR Registros  ==");		
		log.info("ENTRADA > " + in.toString());
		
		SQL _sql = new SQL();
		ProcesarRegistros_Salida out = new ProcesarRegistros_Salida();
	
		HashMap<String,Object> params = new HashMap<>();
		
		
		//Si usa regla de negocio realiza validaciones basicas
		if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio))
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				log.info("NO CUMPLE REQUISITOS BASICOS");
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
						
			if (in.p_List.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CARGAS",null,con, in.p_IdEmpresa));
				return out;
			}
		}

		// --------------------------------------------------------------------------------
		// VALIDA LOS REGISTROS EXISTENTES
		List<Long> Ids = new ArrayList<>();
		for (Registro reg: in.p_List) {
			Ids.add(reg.ID);
		}
		params.clear();
		params.put("Id", Ids);
		params.put("ValorNuevo", in.p_Rectifica);
		
		List<ProcesarRegistro> ProcesarRegistros = new ArrayList<>();
		ProcesarRegistros = _sql.query(con, SQL_SELECT_REGISTROS, params, ProcesarRegistro.class);
		log.info("REGISTROS VALIDADOS > " + ProcesarRegistros);
		
		// --------------------------------------------------------------------------------
		// ACTUALIZA REGISTROS
		List<String> IdsRectifica = new ArrayList<>();
		for (ProcesarRegistro reg: ProcesarRegistros) {
			IdsRectifica.add(reg.KIdContrato);
		}
		params.clear();
		params.put("KIdContrtato", IdsRectifica);
		if (!params.isEmpty() && !ProcesarRegistros.isEmpty())
		{
			try {
				_sql.batch(con, SQL_UPDATE_REGISTROS, ProcesarRegistros, ProcesarRegistro.class);
				agregarLog(con, in, ProcesarRegistros);
			} catch (Exception er) {
				log.error("ERROR::RectificarRegistro", er);
			}
		}

		log.info("SALIDA > " + out);
		log.info("== TERMINO PROCESAR RECTIFICACION ==");
		
		return out;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TRI\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaProceso\":\"2023-11-22\",\"p_Rectifica\":\"S\",\"p_List\":[{\"ID\":13}]}";

			ProcesarRegistros_Entrada Objin = gson.fromJson(json, ProcesarRegistros_Entrada.class);
			RectificarRegistro demo = new RectificarRegistro();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				ProcesarRegistros_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
// 				con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			con.close(); 			
	    }		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
