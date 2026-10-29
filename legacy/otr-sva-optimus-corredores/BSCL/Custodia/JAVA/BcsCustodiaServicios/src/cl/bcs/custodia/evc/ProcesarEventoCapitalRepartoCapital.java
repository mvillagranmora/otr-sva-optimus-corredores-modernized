package cl.bcs.custodia.evc;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente;
import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente;
import cl.bcs.custodia.evc.ProcesarEventoCapital.SalidaClientesConDerecho;

import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.actualizaEventoCapital;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnDinero.EventoCapitalFormaPagoModalidad;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;




public class ProcesarEventoCapitalRepartoCapital implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ProcesarEventoCapitalRepartoCapital.class);


	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_InsertEventoCapitalCliente.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_InsertEventoCapitalClienteFormaDePago.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL						= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_UpdateEventoCapital.sql");
	private final String SQL_SELECT_CLIENTE_OPERA_CUENTA_INVERSION		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_SelectClienteOperaCuentaInversion.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_GARANTIAS    		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_SelectEventoCapitalGarantias.sql");
	private final String SQL_SELECT_FORMA_PAGO_MODALIDAD 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_SelectFormaPagoModalidad.sql");
	private final String SQL_UPDATE_FORMA_PAGO_MODALIDAD 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalRepartoCapital_UpdateFormaPagoModalidad.sql");

	
	public static class ProcesarEventoCapitalRepartoCapitalEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalRepartoCapitalEntrada (){
			p_EventoCapital 			= new ArrayList<EventoCapital>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	
	public static class ProcesarEventoCapitalRepartoCapitalSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ProcesarEventoCapitalRepartoCapitalSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EventoCapitalFormaPago{
		public Long			IdEventoCapital;
		public Long			IdUsuarioIngreso;
		public Long			IdEmpresa;
		public String		DescuentaCuentaInv;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalIdEvtoGtia{
		public Long IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaCliente{
		public Long			IdCliente;
		public Long			IdSocioDeNegocio;
		public String		TipoSocioNegocio;
		public String		TipoDocumentoIdentidad;
		public String		DocumentoIdentidad;
		public String		NombreCompleto;
		public Long 		IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	// ---------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------
	public ProcesarEventoCapitalRepartoCapitalSalida logica(Connection con, ProcesarEventoCapitalRepartoCapitalEntrada in) throws Exception {
		ProcesarEventoCapitalRepartoCapitalSalida out = new ProcesarEventoCapitalRepartoCapitalSalida();

		logger.info(":: INICIO ProcesarEventoCapitalRepartoCapitalSalida ::");		
		SQL sql = new SQL();
		ProcesarEventoCapital ev = new ProcesarEventoCapital();
		
		
		// ----------------------------------------------------------------------------
		// VALIDACIONES INICIALES

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		
		// ----------------------------------------------------------------------------
		// PROCESAR EVENTO CAPITAL	
		
		// Consulta empresa
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		
		List<EventoCapitalIdEvtoGtia> IdEvenCapGtia = new ArrayList<EventoCapitalIdEvtoGtia>();		
		
		String DescuentaCuentaInv = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "EventoCapitalDescuentaCuentaInversion");
		
		for (EventoCapital recorreEventoCapital: in.p_EventoCapital){
			List<actualizaEventoCapital> idEventoCapital = new ArrayList<actualizaEventoCapital>();
			// Obtiene clientes con derecho
			SalidaClientesConDerecho CCD = ev.getClientesConDerecho(con, recorreEventoCapital, in.p_IdModulo, empresa);
			out.P_OUT_MENSAJES.addAll(CCD.P_OUT_MENSAJES);
			HashMap<String,String> params = new HashMap<String,String>();
			HashMap<String,Object> paramsGtia = new HashMap<String,Object>();
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 && CCD.P_OUT_MENSAJES.size()>0){
				continue;
			}
			
			
			// ----------------------------------------------------------------------------
			// INSERTA EVENTO CAPITAL CLIENTE
			
			
			List<Long> secuenciaEventoCapitalCliente= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(CCD.P_OUT_CONTRAPARTES.size()));
			
			for (EventoCapitalCliente recorreContrapartes : CCD.P_OUT_CONTRAPARTES) {	
				
				recorreContrapartes.IdEventoCapitalCliente 	= secuenciaEventoCapitalCliente.get(CCD.P_OUT_CONTRAPARTES.indexOf(recorreContrapartes));
				if( "S".equalsIgnoreCase(DescuentaCuentaInv) ) {
					ConsultaCliente consultaCliente = this.ConsultaCliente(con,sql,recorreContrapartes);
					if ( consultaCliente.DocumentoIdentidad != null ) {
						recorreContrapartes.IdPortafolio = consultaCliente.IdPortafolio;
						recorreContrapartes.IdCliente = consultaCliente.IdCliente;
						recorreContrapartes.TipoSocioNegocio = "CLI";
						recorreContrapartes.IdSocioNegocio = consultaCliente.IdSocioDeNegocio;
					}
				}
				
			}
			
			List<EventoCapitalCliente> insertEventoClienteFull = unionLists(CCD.P_OUT_CLIENTES, CCD.P_OUT_CONTRAPARTES);
			if (insertEventoClienteFull.size()>0 ){
				logger.info("Inicio Insert Eventos Capital Cliente");	

				//Calcula evento capital que estan en garantias/contraparte
				EventoCapitalIdEvtoGtia EvenCapGtia = new EventoCapitalIdEvtoGtia();
				EvenCapGtia.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				IdEvenCapGtia.add(EvenCapGtia);
									
				paramsGtia.put("IdEventoCapital", IdEvenCapGtia.get(0).IdEventoCapital.toString());
				List<EventoCapitalCliente> busquedaEventoCapitalGtia = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_GARANTIAS, paramsGtia, EventoCapitalCliente.class);
				
				List<EventoCapitalCliente> insertEventoClienteFullConGarantias = unionLists(insertEventoClienteFull, busquedaEventoCapitalGtia);
				
				if ( "RUE".equalsIgnoreCase(recorreEventoCapital.Origen) ) {
				
					BuscarMonedaPreferenciaPagoEventoCapitalCliente buscarMonedaPreferenciaPagoEventoCapitalCliente = new BuscarMonedaPreferenciaPagoEventoCapitalCliente();
					EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente = new EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente();
					
					entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdEmpresa = in.p_IdEmpresa;
					entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdModulo = in.p_IdModulo;
					entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdUsuario = in.p_IdUsuario;
					entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_EventoCapital = recorreEventoCapital;
					entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_EventoCapitalCliente = insertEventoClienteFullConGarantias;
					
					SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente = buscarMonedaPreferenciaPagoEventoCapitalCliente.logica(con, entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente);
					out.P_OUT_MENSAJES.addAll(salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente.P_OUT_MENSAJES);
					insertEventoClienteFullConGarantias = salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente.P_OUT_RESULTADO;

				}
				
				sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFullConGarantias , EventoCapitalCliente.class);
				
				logger.info("ProcesarEventoCapitalRepartoCapital - Fin Insert Eventos Capital Cliente");
				ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
																"PRO", recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFull.size(), recorreEventoCapital.Nemo);
				
				// ----------------------------------------------------------------------------
				// INSERTA MEDIO DE PAGO EVENTO CAPITAL
			
				logger.info("Inicio Insert Forma de Pago Eventos Capital Cliente");
				List<EventoCapitalFormaPago> insertEventoClienteFormaPago = new ArrayList<EventoCapitalFormaPago>();
				EventoCapitalFormaPago formaPago = new EventoCapitalFormaPago();
				formaPago.IdEventoCapital 	= recorreEventoCapital.IdEventoCapital;
				formaPago.IdEmpresa			= recorreEventoCapital.IdEmpresa;
				formaPago.IdUsuarioIngreso	= recorreEventoCapital.IdUsuario;
				insertEventoClienteFormaPago.add(formaPago);
				
				sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO, insertEventoClienteFormaPago, EventoCapitalFormaPago.class);				
				logger.info("Fin Insert Forma de Pago Eventos Capital Cliente");
				
				//Select para traer idEventoCapitalFormaDePago Asociado al eventoCapitalCliente
				
				List<EventoCapitalFormaPagoModalidad> busquedaFormaPagoModalidad = this.ConsultaEventoCapitalFormaPagoModalidad(con, recorreEventoCapital.IdEventoCapital);
				logger.info(busquedaFormaPagoModalidad);
				
     			
				//Update EventoCapitalCliente, pasando IdEventoCapitalClienteFormaDePago
				List<EventoCapitalCliente> updateEventoFormaPago= new ArrayList<EventoCapitalCliente>();
				sql.batch(con, SQL_UPDATE_FORMA_PAGO_MODALIDAD, busquedaFormaPagoModalidad, EventoCapitalFormaPagoModalidad.class);
				logger.info(updateEventoFormaPago);
					
			}
			
			
			// ----------------------------------------------------------------------------
			// ACTUALIZA ESTADO EVENTO CAPITAL
			
			logger.info("Inicio Actualizar Estado Eventos Capital");
			actualizaEventoCapital idEven = new actualizaEventoCapital();
			idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
			//idEven.IdEstado = vIdEstado;
			idEven.IdEstado = "PRO";
			idEventoCapital.add(idEven);
			
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, actualizaEventoCapital.class);
			logger.info("Fin Actualizar Estado Eventos Capital");	
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
			
		}

		
		logger.info(":: TERMINO ProcesarEventoCapitalRepartoCapitalSalida ::");
		return out;
	};		
	
	private ConsultaCliente ConsultaCliente(Connection con, SQL sql, EventoCapitalCliente contraparte) throws SQLException {
		logger.info("INICIO::ProcesarEventoCapitalRepartoCapital.ConsultaCliente");
		
		HashMap<String,Object> params 	= new HashMap<>(1);
		params.put("DocumentoIdentidad", contraparte.DocumentoIdentidad);
		List<ConsultaCliente> busquedaCliente = sql.query(con, SQL_SELECT_CLIENTE_OPERA_CUENTA_INVERSION, params , ConsultaCliente.class);

		logger.info("FIN::ProcesarEventoCapitalRepartoCapital.ConsultaCliente");

		return busquedaCliente.size() > 0 ? busquedaCliente.get(0) : new ConsultaCliente();
	}
	
	public <EventoCapitalCliente> List<EventoCapitalCliente> unionLists (	List<EventoCapitalCliente> list1, 
			List<EventoCapitalCliente> list2) {
		
		Set<EventoCapitalCliente> setNuevaLista = new HashSet<EventoCapitalCliente>();
		
		setNuevaLista.addAll(list1);
		setNuevaLista.addAll(list2);

		return new ArrayList<EventoCapitalCliente>(setNuevaLista);
	};
	
	private  List<EventoCapitalFormaPagoModalidad> ConsultaEventoCapitalFormaPagoModalidad(Connection con, Long evCapi) throws SQLException {
		logger.debug("INICIO::ProcesarEventoCapitalRepartoCapital.ConsultaEventoCapitalFormaPagoModalidad");
		long inicioMetodo=System.currentTimeMillis();
		HashMap<String,Object> params 	= new HashMap<>(1);
		params.put("IdEventoCapital", evCapi);
		SQL sql = new SQL();
		List<EventoCapitalFormaPagoModalidad> busquedaFormaPagoModalidad = new ArrayList<EventoCapitalFormaPagoModalidad>();
		busquedaFormaPagoModalidad = sql.query(con, SQL_SELECT_FORMA_PAGO_MODALIDAD, params , EventoCapitalFormaPagoModalidad.class);

		long finMetodo=System.currentTimeMillis();
		logger.debug("FIN::ProcesarEventoCapitalRepartoCapital.ConsultaEventoCapitalFormaPagoModalidad",finMetodo-inicioMetodo);
		return busquedaFormaPagoModalidad;
		
	}
    
	
	// ---------------------------------------------------------------
	// CALL 
	// ---------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {

		Gson gson = Utils.getGsonBuilder();	
		ProcesarEventoCapitalRepartoCapitalEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalRepartoCapitalEntrada.class);
		ProcesarEventoCapitalRepartoCapitalSalida out=logica(conn,jin);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	// ---------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_EventoCapital\":[{\"IdEventoCapital\":365125,\"IdEmpresa\":1,\"IdTipoEventoCapital\":\"DV\",\"Secuencia\":1,\"Descripcion\":\"PRUEBA JPZ INGEVEC EN DINERO\",\"EsInscrito\":\"S\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":1203,\"Nemo\":\"INGEVEC\",\"Factor\":0.08546550,\"Valor\":0E-8,\"IdMoneda\":\"CLP\",\"IdInstrumentoNuevo\":0,\"NemoNuevo\":\"0\",\"FactorNuevo\":1.00000000,\"FechaActualizacion\":\"2018-07-31 00:00:00.000\",\"FechaLimite\":\"2018-07-31 00:00:00.000\",\"FechaPago\":\"2018-08-07 00:00:00.000\",\"NumeroDividendo\":1101901,\"IdTipoDividendo\":\"P\",\"PrecioAnterior\":0E-8,\"FechaHoraIngresoRegistro\":\"2018-07-31 00:00:00.000\",\"FechaHoraUltimaActualizacion\":\"2018-08-07 00:00:00.000\",\"TipoIngreso\":\"C\",\"IdUsuario\":16,\"IdEstado\":\"VIG\",\"CuentaDepositanteSorteoLetras\":0,\"MonedaSorteoLetras\":0,\"GlosaMonedaSorteoLetras\":\"\",\"EmisorPagadorSorteoLetras\":0,\"EmisorSorteoLetras\":0,\"InstrumentoSorteoLetras\":0,\"GlosaInstrumentoSorteoLetras\":\"\",\"BloqueoSorteoLetras\":0,\"SorteadoSorteoLetras\":0.0000,\"APagarSorteoLetras\":0.0000,\"APagarPesosSorteoLetras\":0,\"IdEmisor\":\"INGEVEC\",\"IdMonedaLiquidacion\":\"CLP\",\"Paridad\":1.00000000,\"IdEmisorNuevo\":\"\",\"EstadoNotificacion\":\"N\",\"IdInstrumentoAnterior\":0,\"Indice\":0,\"Impuesto\":0E-8,\"CantidadCuotas\":null,\"Monto\":null}]}";
				ProcesarEventoCapitalRepartoCapitalEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalRepartoCapitalEntrada.class);
								
				ProcesarEventoCapitalRepartoCapital demo = new ProcesarEventoCapitalRepartoCapital();
				ProcesarEventoCapitalRepartoCapitalSalida Objout = demo.logica(con, Objin);
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};