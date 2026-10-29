package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.RetirarCustodiaOSA;
import cl.bcs.custodia.RetirarCustodiaOSA.RetirarCustodiaOSA_Entrada;
import cl.bcs.custodia.RetirarCustodiaOSA.RetirarCustodiaOSA_Salida;
import cl.bcs.custodia.evc.CerrarEventoCapitalCambioDeNemo.CerrarEventoCapitalCambioDeNemoEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalCambioDeNemo.CerrarEventoCapitalCambioDeNemoSalida;
import cl.bcs.custodia.evc.CerrarEventoCapitalCanje.CerrarEventoCapitalCanje_Entrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalCanje.CerrarEventoCapitalCanje_Salida;
import cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon.CerrarEventoCapitalCorteCuponEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalCorteCupon.CerrarEventoCapitalCorteCuponSalida;
import cl.bcs.custodia.evc.CerrarEventoCapitalDA.EntradaCerrarEventoCapitalDA;
import cl.bcs.custodia.evc.CerrarEventoCapitalDA.SalidaCerrarEventoCapitalDA;
import cl.bcs.custodia.evc.CerrarEventoCapitalDV.CerrarEventoCapitalDVEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalDV.CerrarEventoCapitalDVSalida;
import cl.bcs.custodia.evc.CerrarEventoCapitalDVFFMM.Entrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalDVFFMM.Salida;
import cl.bcs.custodia.evc.CerrarEventoCapitalEmisionLiberada.CerrarEventoCapitalEmisionLiberadaEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalEmisionLiberada.CerrarEventoCapitalEmisionLiberadaSalida;
import cl.bcs.custodia.evc.CerrarEventoCapitalRC.CerrarEventoCapitalRCEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalRC.CerrarEventoCapitalRCSalida;
import cl.bcs.custodia.evc.CerrarEventoCapitalSorteoLetras.CerrarEventoCapitalSorteoLetrasEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalSorteoLetras.CerrarEventoCapitalSorteoLetrasSalida;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Entrada;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico.LiquidarMovimientoAutomatico_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalMasivo implements JavaCall {
	
	private static final Logger logger 							= LogManager.getLogger(LiquidarMovimientoAutomatico.class);
	private final String SQL_SELECT_CONSULTAR_EVENTO_CAPITAL	= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalMasivo_SelectEventoCapital.sql");
	
	public static class CerrarEventoCapitalEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_UsaReglasNegocio;
		public List<Long>					p_IdEventoCapital;
		
		public CerrarEventoCapitalEntrada (){
			this.p_IdEventoCapital 		= new ArrayList<Long>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CerrarEventoCapitalSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public CerrarEventoCapitalSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public CerrarEventoCapitalSalida logica(Connection con, CerrarEventoCapitalEntrada in) throws Exception {
		
		logger.info("INICIO::CerrarEventoCapitalMasivo");
		
		Long init 								= System.currentTimeMillis();
		CerrarEventoCapitalSalida out 			= new CerrarEventoCapitalSalida();
		HashMap<String,String> mapMsg 			= new HashMap<>();
		SQL sql 								= new SQL();
		Long Id 								= 0L;
		List<EventoCapital> eventoCapitalEP 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalDA 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalDV 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalCN 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalCC 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalSL 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalCA 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalDF 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalEL 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalRC 	= new ArrayList<>();
		List<EventoCapital> eventoCapitalOtros 	= new ArrayList<>();
		String idPaisSistema 					= ConfiguracionSistema
													.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		String fechaSistema 					= ConfiguracionSistema
													.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		
	
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		List<EventoCapital> eventoCapital = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CAPITAL, params, EventoCapital.class);
		
		
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();					
		RevisarRequisitosBasicosSalida salidaECidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaECidaBasicos.p_out_Mensajes);
		
		if (eventoCapital.size() != in.p_IdEventoCapital.size() ) {
			for( EventoCapital ev: eventoCapital ) {
				EventoCapital evIng = in.p_IdEventoCapital.contains(ev.IdEventoCapital) ? ev : null;
				if ( evIng == null ) //CREAR MENSAJE INDIVIDUAL PARA CADA EC QUE NO SE ENCUENTRE
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
			}
		}
		
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************	
		
		for (EventoCapital ev: eventoCapital) {
			switch (ev.IdTipoEventoCapital) {
				case "EP":     //EMISION PAGADA
					eventoCapitalEP.add(ev);
					break;
				case "DA":     //DIVIDENDO EN ACCIONES
					eventoCapitalDA.add(ev);
					break;
				case "CN":    //CAMBIO NEMO
					eventoCapitalCN.add(ev);
					break;
				case "DV":     //DIVIDENDO EN DINERO
					eventoCapitalDV.add(ev);
					break;     
				case "CC":     //CORTE CUPON
					eventoCapitalCC.add(ev);
					break;
				case "SL":     //SORTEO DE LETRAS
					eventoCapitalSL.add(ev);
					break;
				/*case "DS":     //DIVISION DE SOCIEDAD (ES UN SP)
					eventoCapitalDS.add(ev);
					break;*/
				case "CA":     //CANJE
					eventoCapitalCA.add(ev);
					break;
				/*case "SU":     //EMISION SUSCRIPCION  (ES UN SP)
					eventoCapitalSU.add(ev);
					break;*/
				case "DF":     //DIVIDENDO EN CUOTAS FFMM
					eventoCapitalDF.add(ev);
					break;					
				case "EL":     //EMISION LIBERADA
					eventoCapitalEL.add(ev);
					break;
				case "RC":     //REPARTO CAPITAL
					eventoCapitalRC.add(ev);
					break;
				default:
					eventoCapitalOtros.add(ev);
			}
		}
		
		if( eventoCapitalDA.size() > 0 ) { // DA = DIVIDENDO EN ACCIONES
			for (EventoCapital ev: eventoCapitalDA){
				CerrarEventoCapitalDA proc = new CerrarEventoCapitalDA();
				EntradaCerrarEventoCapitalDA entrada = new EntradaCerrarEventoCapitalDA();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				SalidaCerrarEventoCapitalDA salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalEL.size() > 0) { // EL = EMISION LIBERADA
			for (EventoCapital ev: eventoCapitalEL){
				CerrarEventoCapitalEmisionLiberada proc = new CerrarEventoCapitalEmisionLiberada();
				CerrarEventoCapitalEmisionLiberadaEntrada entrada = new CerrarEventoCapitalEmisionLiberadaEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				CerrarEventoCapitalEmisionLiberadaSalida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalEP.size() > 0 ) { // EP = EMISION PAGADA
			for (EventoCapital ev: eventoCapitalEP){
				RetirarCustodiaOSA proc = new RetirarCustodiaOSA();
				RetirarCustodiaOSA_Entrada entrada = new RetirarCustodiaOSA_Entrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdInstrumento = ev.IdInstrumento;
				entrada.p_IdPais = Long.parseLong(idPaisSistema);			
				RetirarCustodiaOSA_Salida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalDV.size() > 0 ) { // DV = DIVIDENDO EN DINERO
			for (EventoCapital ev: eventoCapitalDV){
				CerrarEventoCapitalDV proc = new CerrarEventoCapitalDV();
				CerrarEventoCapitalDVEntrada entrada = new CerrarEventoCapitalDVEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				CerrarEventoCapitalDVSalida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalCN.size() > 0 ) { // CN = CAMBIO DE NEMO
			for (EventoCapital ev: eventoCapitalCN){
				CerrarEventoCapitalCambioDeNemo proc = new CerrarEventoCapitalCambioDeNemo();
				CerrarEventoCapitalCambioDeNemoEntrada entrada = new CerrarEventoCapitalCambioDeNemoEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				CerrarEventoCapitalCambioDeNemoSalida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalSL.size() > 0 ) { // SL = Sorteo de letras	
			CerrarEventoCapitalSorteoLetras proc = new CerrarEventoCapitalSorteoLetras();
			CerrarEventoCapitalSorteoLetrasEntrada entrada = new CerrarEventoCapitalSorteoLetrasEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_EventoCapital = eventoCapitalSL; // Este servicio ya es masivo
			CerrarEventoCapitalSorteoLetrasSalida salidaEC = proc.logica(con, entrada);
			out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
			if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
				return out;
			}
		}
		if( eventoCapitalCA.size() > 0 ) { // CA = CANJE
			for (EventoCapital ev: eventoCapitalCA){
				CerrarEventoCapitalCanje proc = new CerrarEventoCapitalCanje();
				CerrarEventoCapitalCanje_Entrada entrada = new CerrarEventoCapitalCanje_Entrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdInstrumento = ev.IdInstrumento;
				entrada.p_IdPais = Long.parseLong(idPaisSistema);
				CerrarEventoCapitalCanje_Salida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalRC.size() > 0 ) { // RC = REPARTO CAPITAL
			for (EventoCapital ev: eventoCapitalRC){
				CerrarEventoCapitalRC proc = new CerrarEventoCapitalRC();
				CerrarEventoCapitalRCEntrada entrada = new CerrarEventoCapitalRCEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				CerrarEventoCapitalRCSalida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalCC.size() > 0 ) { // CC = CORTE CUPON
			for (EventoCapital ev: eventoCapitalCC){
				CerrarEventoCapitalCorteCupon proc = new CerrarEventoCapitalCorteCupon();
				CerrarEventoCapitalCorteCuponEntrada entrada = new CerrarEventoCapitalCorteCuponEntrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				entrada.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				CerrarEventoCapitalCorteCuponSalida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		if( eventoCapitalDF.size() > 0 ) { // DF = DIVIDENDO EN CUOTAS FFMM
			for (EventoCapital ev: eventoCapitalDF){
				CerrarEventoCapitalDVFFMM proc = new CerrarEventoCapitalDVFFMM();
				Entrada entrada = new Entrada();
				entrada.p_IdEmpresa = in.p_IdEmpresa; 
				entrada.p_IdUsuario = in.p_IdUsuario;
				entrada.p_IdModulo = "CUS";
				entrada.p_IdEventoCapital = ev.IdEventoCapital;
				Salida salidaEC = proc.logica(con, entrada);
				out.P_OUT_MENSAJES.addAll(salidaEC.P_OUT_MENSAJES);
				if ( MensajeSalida.getNumeroErrores(salidaEC.P_OUT_MENSAJES ) > 0) {
					return out;
				}
				agregarLogCerrarEventoCapital(con, ev, in, fechaSistema);
			}
		}
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CERRAR_EVENTOS_CAPITAL_MASIVO",new HashMap<String,String>(),con,in.p_IdEmpresa));

		logger.info("FIN::CerrarEventoCapitalMasivo, " + (System.currentTimeMillis() - init));
		return out;
		
	}
	
	public static void agregarLogCerrarEventoCapital(Connection con, EventoCapital ev, CerrarEventoCapitalEntrada in, String fechaSistema) {
		try {
			logger.debug("INICIO::CerrarEventoCapitalMasivo.agregarLogCerrarEventoCapital");
			long inicio = System.currentTimeMillis();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			
			log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			log.IdCabeceraLog	= ev.IdEventoCapital; 
			log.IdTipoLog		= "CERRAR"; 
			log.IdModulo		= in.p_IdModulo; 
			log.IdUsuario		= in.p_IdUsuario; 
			log.IdEmpresa		= in.p_IdEmpresa; 
			log.FechaSistemaLog	= fechaSistema; 
			log.IdUsuarioAtendido	= in.p_IdUsuario; 
			log.IdCliente			= 0L; 
			log.EstadoCliente		= ""; 
			log.NombreCliente		= ""; 
			log.IdTipoDocumentoIdentidad	= ""; 
			log.IdDocumentoIdentidad	= ""; 
			log.IdPortafolio			= 0L; 
			log.EstadoPortafolio		= ""; 
			log.IdTipoRegistro			= "CERRAREVENTOCAPITAL"; 
			log.IdRegistro				= ev.IdEventoCapital.toString(); 
			log.EstadoRegistro			= ""; 
			log.IdTipoOperacion			= ev.IdTipoEventoCapital; 
			log.Folio					= 0L; 
			log.Cantidad				= BigDecimal.ZERO;
			log.PrecioTasa				= BigDecimal.ZERO; 
			log.Monto					= BigDecimal.ZERO; 
			log.IdMonedaOrigen			= ""; 
			log.IdMonedaLiquidacion		= ""; 
			log.TipoCambio				= ""; 
			log.Nemo					= ev.Nemo; 
			log.IdProceso				= 0L; 
			log.EstadoProceso			= ""; 
			log.NombreProceso			= "cl.bcs.custodia.evc.CerrarEventoCapitalMasivo.java";
			
			entradaAgregarLog.p_Log.add(log);
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
			
			long fin = System.currentTimeMillis();
			logger.debug("FIN::CerrarEventoCapitalMasivo.agregarLogCerrarEventoCapital:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		CerrarEventoCapitalEntrada jin = gson.fromJson(in.toString(), CerrarEventoCapitalEntrada.class);
		CerrarEventoCapitalSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args)  throws Exception,SQLException {
	
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEventoCapital\":[40281]}";
			CerrarEventoCapitalEntrada Objin = gson.fromJson(json, CerrarEventoCapitalEntrada.class);
			CerrarEventoCapitalMasivo demo = new CerrarEventoCapitalMasivo();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try {
 				
 				CerrarEventoCapitalSalida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 				//con.commit();
 				
 			} catch(Exception er) {
 				
				er.printStackTrace();	
				//con.commit();
				
			}
 			
 			con.close();
 			
	    }
		catch (Exception e) {
			
			e.printStackTrace();	
			
		}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");

	};
}
