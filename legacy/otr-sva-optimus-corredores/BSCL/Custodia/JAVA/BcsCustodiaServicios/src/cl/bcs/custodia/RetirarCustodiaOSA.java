package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.CerrarEventoCapitalDA;
import cl.bcs.custodia.evc.CerrarEventoCapitalDA.EntradaCerrarEventoCapitalDA;
import cl.bcs.custodia.evc.CerrarEventoCapitalDA.SalidaCerrarEventoCapitalDA;
import cl.bcs.custodia.evc.NotificarEventoCapital;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RetirarCustodiaOSA implements JavaCall{
	
	private final Log log = new Log("CUS.RetirarCustodiaOSA");
	private final String SQL_SELECT_NEMOTECNICO_RELACIONADO			= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_SelectInstrumentoRelacionado.sql");
	private final String SQL_SELECT_CARTERA_GTIA_RETIRO			 	= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_SelectCarteraGarantiaRetiro.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 					= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_UpdateEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_SU 				= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_SelectEventoCapitalSU.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_SU 				= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_UpdateEventoCapitalSU.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 			= loadResourceToString("cl/bcs/custodia/sql/RetirarCustodiaOSA_UpdateEventoCapitalCliente.sql");
	
	public static class RetirarCustodiaOSA_Entrada{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public Long			p_IdPais;
		public Long 		p_IdEventoCapital;
		public Long 		p_IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarEvento{
		public Long IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarEventoSU{
		public Long IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class RetirarCustodiaOSA_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public RetirarCustodiaOSA_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarterasARetirar{
		public Long			IdGenerarMovimientoCustodia;
		public Long			IdPais;
		public Long			IdEmpresa;
		public String		FechaOperacion;
		public String		EsNominalIngreso;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdMonedaSistema;
		public String		IdConcepto;
		public String		IdTipoOperacion;
		public String		IdBolsa;
		public String		IdCondicionLiquidacion;
		public String		Glosa;
		public Long			IdUsuario;
		public String		IdModulo;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public Long			IdInstrumento;
		public String		Nemotecnico;
		public String		IdEmisor;
		public String		IdBoveda;
		public BigDecimal	Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class _CarterasARetirar{
		public Long			IdGenerarMovimientoCustodia;
		public String		TablaAfectada;
		public String      IdOrigenGarantia;
		public String		EsNominalIngreso;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		IdMonedaSistema;
		public String		IdConcepto;
		public String		IdTipoOperacion;
		public String		IdBolsa;
		public String		IdCondicionLiquidacion;
		public String		Glosa;
		public String		IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public Long			IdInstrumento;
		public String		Nemotecnico;
		public String		IdEmisor;
		public String		IdBoveda;
		public BigDecimal	Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoRelacionado{
		public Long 	IdInstrumentoRelacionado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	// -----------------------------------------------------------
	//  LOGICA
	// -----------------------------------------------------------

	public RetirarCustodiaOSA_Salida logica(Connection con, RetirarCustodiaOSA_Entrada entrada) throws Exception {
		log.write("INICIO");
		RetirarCustodiaOSA_Salida out = new RetirarCustodiaOSA_Salida();
		SQL sql=new SQL();
		String cierreOK = "";
		String fechaSistema  = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		
		
		// ------------------------------------------------
		// BUSCA INSTRUMENTO RELACIONADO
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdInstrumento", entrada.p_IdInstrumento);
		List<InstrumentoRelacionado> idInstrumentoRelacionado = sql.query(con, SQL_SELECT_NEMOTECNICO_RELACIONADO, params, InstrumentoRelacionado.class);
		HashMap<Long,Long> transitosGenerados = new HashMap<Long, Long>();
		

		if (idInstrumentoRelacionado.size() > 0 ){
			List<RetiroCustodia.ObjDatosEntrada> registroCarterasGarantiaRetirar = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			// PREPARAMOS REGISTROS QUE SE RETIRARAN DESDE CARTERA Y QUE PASARAN DESDE LA GARANTIA A LA CARTERA.
			registroCarterasGarantiaRetirar = CarterasGarantiasARetirar (entrada, con, idInstrumentoRelacionado, fechaSistema);
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
			
			if (registroCarterasGarantiaRetirar.size()> 0){
				
				// ----------------------------------------------------------------------------------
				// GENERA MOVIMIENTO EN TRANSITO POR RETIRO OSA
				List<ObjDatosEntrada> MovimientosTransito  = new ArrayList<ObjDatosEntrada>();
				
				for (RetiroCustodia.ObjDatosEntrada registroCarterasARetirar : registroCarterasGarantiaRetirar){			
					ObjDatosEntrada arg0 = new ObjDatosEntrada();
					arg0.IdGenerarMovimientoCustodia		= registroCarterasARetirar.IdGenerarMovimientoCustodia;
					arg0.TablaAfectada						= "TRANSITO";
					arg0.AccionOperacion					= "RETIRO";
					arg0.EsRegistroNuevo					= "S";
					arg0.FolioCarteraOrigen					= 0L;	//Para caso de retiro desde cartera					
					arg0.IdCliente							= registroCarterasARetirar.IdCliente;
					arg0.IdPortafolio						= registroCarterasARetirar.IdPortafolio;
					arg0.IdCuentaCustodioPortafolio			= 0L;
					arg0.IdClienteRelacionado				= null;
					arg0.IdPortafolioRelacionado			= null;
					arg0.FechaOperacion						= Fecha.toDate(fechaSistema);
					arg0.FechaVencimientoOperacion			= Fecha.toDate("0001-01-01");
					arg0.PlazoEnDiasOperacion				= 0L;
					arg0.IdTipoOperacion					= "RETOSA";   // Retiro de Osa
					arg0.IdConcepto							= "VCTOSA";   // Retiro por Vencimiento de OSA	
					arg0.IdCondicionLiquidacion				= "T0"; 
					arg0.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					arg0.IdTipoCanalIngreso					= "SIS";
					arg0.FolioExternoReferencia				= "";
					arg0.IdBolsa							= "BC";	
					arg0.IdUsuario							= entrada.p_IdUsuario;
					arg0.IdSucursal							= 0L; 
					arg0.IdAgente							= 0L; 
					arg0.Glosa								= registroCarterasARetirar.Glosa;
					arg0.IdInstrumento						= registroCarterasARetirar.IdInstrumento;
					arg0.IdMonedaInstrumento				= registroCarterasARetirar.IdMonedaInstrumento;
					arg0.IdTipoReajusteInstrumento			= registroCarterasARetirar.IdMonedaInstrumento;
					arg0.IdEmisor							= registroCarterasARetirar.IdEmisor;
					arg0.EsFungible							= "N";
					arg0.IdBoveda							= registroCarterasARetirar.IdBoveda;
					arg0.CuentaBoveda						= "";
					arg0.EsNominal							= "N";			
					arg0.Cantidad							= registroCarterasARetirar.Cantidad; 
					arg0.CorteMinimo						= null; 
					arg0.ValorRescate						= null; 
					arg0.PrecioCompra						= BigDecimal.ZERO; 
					arg0.TasaCompra							= BigDecimal.ZERO;
					arg0.MontoCompra						= BigDecimal.ZERO; 
					arg0.MontoCompraMonedaInstrumento		= BigDecimal.ZERO;
					arg0.IdOperacion						= registroCarterasARetirar.IdOperacion;
					MovimientosTransito.add(arg0);
				}
				
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;		
				paramInSP.p_IdModulo			= entrada.p_IdModulo;
				paramInSP.p_SimularOperacion	= "N";
				paramInSP.p_TicketAtencion 		= 0L;
				paramInSP.p_DatosEntrada		= MovimientosTransito;				
				IngresoCustodia.IngresoCustodiaSalida resultIngresoRetiroTransito = ingresoCustodia.logica(con, paramInSP);
				
				if (!resultIngresoRetiroTransito.P_OUT_ERRORESCONTADOR.equals(0L)){
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida msg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
						if (msg.RegistroConProblemas.equals("N")) continue;
						out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
					};					
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;									
				}
				else
				{
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida reg : resultIngresoRetiroTransito.P_OUT_DATOSSALIDA){
						if (reg.RegistroConProblemas.equals("S")) continue;
						
						GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
						MOV.IdMovimientoCarteraEnTransito = reg.IdCustodia;
						MOV.EsCertificacion = "S";
						MOV.IdUsuario = entrada.p_IdUsuario;
						p_Transitos.add(MOV);					
						
						transitosGenerados.put(reg.IdGenerarMovimientoCustodia,reg.IdCustodia);
					};				
				}

				//Se actualizan movimientos en transito
				if (p_Transitos.size() > 0 )
				{
					GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
				}
				

				
				// ----------------------------------------------------------------------------------
				// SE GENERA RETIRO DE CUSTODIA POR OSAS
				
				List<ObjMovimiento> logCustodia = new ArrayList<ObjMovimiento>();
				for (RetiroCustodia.ObjDatosEntrada registroCarterasARetirar : registroCarterasGarantiaRetirar){
					if(transitosGenerados.get(registroCarterasARetirar.IdGenerarMovimientoCustodia) != null) {
						registroCarterasARetirar.IdMovimientoCarteraEnTransitoOrigen = transitosGenerados.get(registroCarterasARetirar.IdGenerarMovimientoCustodia);
					}else {
						registroCarterasARetirar.IdMovimientoCarteraEnTransitoOrigen = 0L;
					}
					ObjMovimiento registrologCustodia = new ObjMovimiento();
					registrologCustodia.IdGenerarMovimientoCustodia = registroCarterasARetirar.IdGenerarMovimientoCustodia;
					registrologCustodia.IdClienteRetiro				= registroCarterasARetirar.IdCliente;
					registrologCustodia.IdPortafolioRetiro			= registroCarterasARetirar.IdPortafolio;
					registrologCustodia.IdInstrumento				= registroCarterasARetirar.IdInstrumento;
					logCustodia.add(registrologCustodia);
				}
				Long TicketAtencion = LogCustodia.logCustodia(con, logCustodia);
				
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramRetCartera = new RetiroCustodia.RetiroCustodiaEntrada();
				paramRetCartera.p_IdEmpresa			= entrada.p_IdEmpresa;
				paramRetCartera.p_SimularOperacion	= "N";
				paramRetCartera.p_TicketAtencion	= TicketAtencion;
				paramRetCartera.p_DatosEntrada		= registroCarterasGarantiaRetirar;
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramRetCartera);
				
				if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){						
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetiros.P_OUT_DATOSSALIDA){
						if (msg.RegistroConProblemas.equals("N")) continue;
						out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
					};						
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;												
				}
				else
				{
					
					// -------------------------------------------------------------------------------
					// RETIRAMOS CARTERAS QUE PROVIENEN DEL RETIRO DE GARANTIAS REALIZADO RECIEN.
					List<RetiroCustodia.ObjDatosEntrada> registroCarterasxGarantiaRetirar = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
					registroCarterasxGarantiaRetirar = CarterasGarantiasARetirar (entrada, con, idInstrumentoRelacionado, fechaSistema);
					if (registroCarterasxGarantiaRetirar.size() > 0){
						List<ObjMovimiento> logCustodiaG = new ArrayList<ObjMovimiento>();
						for (RetiroCustodia.ObjDatosEntrada registroCarterasARetirar : registroCarterasxGarantiaRetirar){
							ObjMovimiento registrologCustodia = new ObjMovimiento();
							registrologCustodia.IdGenerarMovimientoCustodia = registroCarterasARetirar.IdGenerarMovimientoCustodia;
							registrologCustodia.IdClienteRetiro				= registroCarterasARetirar.IdCliente;
							registrologCustodia.IdPortafolioRetiro			= registroCarterasARetirar.IdPortafolio;
							registrologCustodia.IdInstrumento				= registroCarterasARetirar.IdInstrumento;
							logCustodia.add(registrologCustodia);
						}
						Long TicketAtencionG = LogCustodia.logCustodia(con, logCustodiaG);
						
						RetiroCustodia retiroCustodiaG = new RetiroCustodia();
						RetiroCustodia.RetiroCustodiaEntrada paramRetCarteraG = new RetiroCustodia.RetiroCustodiaEntrada();
						paramRetCarteraG.p_IdEmpresa		= entrada.p_IdEmpresa;
						paramRetCarteraG.p_SimularOperacion	= "N";
						paramRetCarteraG.p_TicketAtencion	= TicketAtencionG;
						paramRetCarteraG.p_DatosEntrada		= registroCarterasxGarantiaRetirar;
						RetiroCustodia.RetiroCustodiaSalida resultRetirosG = retiroCustodiaG.logica(con, paramRetCarteraG);
						
						if (!resultRetirosG.P_OUT_ERRORESCONTADOR.equals(0L)){						
							for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetirosG.P_OUT_DATOSSALIDA){
								if (msg.RegistroConProblemas.equals("N")) continue;
								out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,entrada.p_IdEmpresa));
							};						
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;												
						}else{
							// ACTUALIZAMOS EVENTOS DE CAPITAL.
							cierreOK = ActualizarEventoCapital(entrada, con);
						}
					}else{
						// ACTUALIZAMOS EVENTOS DE CAPITAL.
						cierreOK = ActualizarEventoCapital(entrada, con);
					}
					if (cierreOK == "OK"){
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_CERRAR_OSA_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					}else{
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_OPERACION_CERRAR_OSA_ERROR", new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					}
				};
			
			}else {
				cierreOK = ActualizarEventoCapital(entrada, con);
				if (cierreOK == "OK"){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_OPERACION_CERRAR_OSA_EXITO_SIN_CARTERAS_A_RETIRAR",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}else{
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_OPERACION_CERRAR_OSA_ERROR", new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				
			};
		}else{
			//Instrumento relacionado no encontrado
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUMENTO_RELACIONADO_NO_ENCONTRADO", new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
				
		if(cierreOK=="OK") {
			
			NotificarEventoCapital notificarEventoCierre = new NotificarEventoCapital();	
			NotificarEventoCapital_Entrada NotifIN = new NotificarEventoCapital_Entrada();
			NotifIN.p_IdUsuario = entrada.p_IdUsuario;
			NotifIN.p_IdEmpresa = entrada.p_IdEmpresa;
			NotifIN.p_IdModulo = entrada.p_IdModulo;
			NotifIN.p_EstadoEventoCapital = "CER";
			ObjEventoCapital_Entrada objIdsCapital = new ObjEventoCapital_Entrada();
			objIdsCapital.p_IdEventoCapital=entrada.p_IdEventoCapital;
			NotifIN.p_IdsEventoCapital.add(objIdsCapital);
			
			try {
				NotificarEventoCapital_Salida ObjOut= notificarEventoCierre.logica(con, NotifIN);
				List<MensajeSalida> mensajeAdvertencia = ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				if(ObjOut.P_OUT_ERRORESCONTADOR>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,entrada.p_IdEmpresa) );								
				}else if(mensajeAdvertencia.size()>0) {
					out.P_OUT_MENSAJES=ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				}
			}
			catch(Exception e) {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,entrada.p_IdEmpresa) );				
			}
			
		}
		return out;
	}

	
	
	// Actualiza eventos de capital
	public String ActualizarEventoCapital(	 RetirarCustodiaOSA_Entrada entrada
											,Connection con) throws SQLException{
		SQL sql=new SQL();	
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdEventoCapital", entrada.p_IdEventoCapital);
		
		
		List<EventoCapital> eventoCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_SU, params, EventoCapital.class);
		List<ActualizarEventoSU> actualizarSU = new ArrayList<ActualizarEventoSU>();
		for (EventoCapital ev: eventoCapital) {
			if (ev.IdTipoEventoCapital.equalsIgnoreCase("SU")) {
				ActualizarEventoSU eventoSU = new ActualizarEventoSU();
				eventoSU.IdEventoCapital = ev.IdEventoCapital;
				actualizarSU.add(eventoSU);
			}
		}
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_SU, actualizarSU, ActualizarEventoSU.class);
		
		ActualizarEvento evento = new ActualizarEvento();
		evento.IdEventoCapital = entrada.p_IdEventoCapital;
		List<ActualizarEvento> actualizar = new ArrayList<ActualizarEvento>();
		actualizar.add(evento);
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, ActualizarEvento.class);
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizar, ActualizarEvento.class);
		return "OK";
	}
	
	
	
	
	

	public List<RetiroCustodia.ObjDatosEntrada> CarterasGarantiasARetirar (	 RetirarCustodiaOSA_Entrada entrada
																			,Connection con
																			,List<InstrumentoRelacionado> idInstrumentoRelacionado
																			,String fechaSistema) throws Exception{
		SQL sql=new SQL();
		List<RetiroCustodia.ObjDatosEntrada> ARetirar = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		HashMap<String,Object> paramsCarteras = new HashMap<String,Object>();
		List<Long> paramInstru = new ArrayList<Long>();
		for (InstrumentoRelacionado recorreInsRel: idInstrumentoRelacionado){
			Long regisInsRela= recorreInsRel.IdInstrumentoRelacionado;
			paramInstru.add(regisInsRela);
		}
		paramsCarteras.put("IdInstrumentoRelacionado", paramInstru);
		List<_CarterasARetirar> carterasParaRetirar = sql.query(con, SQL_SELECT_CARTERA_GTIA_RETIRO, paramsCarteras, _CarterasARetirar.class);
		
		if (carterasParaRetirar.size()> 0) {
			for (_CarterasARetirar recorreCarterasParaRetirar: carterasParaRetirar){
				RetiroCustodia.ObjDatosEntrada registroCarterasParaRetirar = new RetiroCustodia.ObjDatosEntrada();
				
				registroCarterasParaRetirar.IdGenerarMovimientoCustodia 	= recorreCarterasParaRetirar.IdGenerarMovimientoCustodia;
				registroCarterasParaRetirar.IdCustodia 						= recorreCarterasParaRetirar.IdGenerarMovimientoCustodia;
				registroCarterasParaRetirar.IdOrigenGarantia                = recorreCarterasParaRetirar.IdOrigenGarantia;
				registroCarterasParaRetirar.FechaOperacion 					= Fecha.toDate(fechaSistema);
				registroCarterasParaRetirar.EsNominal 						= recorreCarterasParaRetirar.EsNominalIngreso;
				registroCarterasParaRetirar.IdCliente 						= recorreCarterasParaRetirar.IdCliente;
				registroCarterasParaRetirar.IdPortafolio 					= recorreCarterasParaRetirar.IdPortafolio;
				registroCarterasParaRetirar.IdConcepto						= recorreCarterasParaRetirar.IdConcepto;
				registroCarterasParaRetirar.IdTipoOperacion					= recorreCarterasParaRetirar.IdTipoOperacion;
				registroCarterasParaRetirar.IdCondicionLiquidacion			= recorreCarterasParaRetirar.IdCondicionLiquidacion;
				registroCarterasParaRetirar.Glosa							= recorreCarterasParaRetirar.Glosa;
				registroCarterasParaRetirar.IdUsuario 						= entrada.p_IdUsuario;
				registroCarterasParaRetirar.IdModulo 						= entrada.p_IdModulo;
				registroCarterasParaRetirar.IdInstrumento					= recorreCarterasParaRetirar.IdInstrumento;
				registroCarterasParaRetirar.IdEmisor						= recorreCarterasParaRetirar.IdEmisor;
				registroCarterasParaRetirar.IdBoveda						= recorreCarterasParaRetirar.IdBoveda;
				registroCarterasParaRetirar.AccionOperacion					= "RETIRO";
				registroCarterasParaRetirar.ValorRescate					= BigDecimal.ZERO;
				registroCarterasParaRetirar.TablaAfectada					= recorreCarterasParaRetirar.TablaAfectada;
				registroCarterasParaRetirar.EsRetiroNegocio					= "S";
				registroCarterasParaRetirar.Cantidad						= recorreCarterasParaRetirar.Cantidad;
				registroCarterasParaRetirar.IdOperacion						= entrada.p_IdEventoCapital;
				

				registroCarterasParaRetirar.FechaLiquidacion                = Fecha.toDate(fechaSistema);
				
				ARetirar.add(registroCarterasParaRetirar);
			}
		}
		return ARetirar;
	}
	
	// -----------------------------------------------------------
	//  CALL
	// -----------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		RetirarCustodiaOSA_Entrada in=gson.fromJson(jin.toString() ,RetirarCustodiaOSA_Entrada.class);
		RetirarCustodiaOSA_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	
	
	// -----------------------------------------------------------
	//  METODO PARA PRUEBAS LOCALES
	// -----------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":16,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":364788,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":2203}";
			RetirarCustodiaOSA_Entrada Objin = gson.fromJson(json, RetirarCustodiaOSA_Entrada.class);
			
			RetirarCustodiaOSA demo=new RetirarCustodiaOSA();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				RetirarCustodiaOSA_Salida ObjOut= demo.logica(con, Objin);
 				//con.rollback();
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	

}
