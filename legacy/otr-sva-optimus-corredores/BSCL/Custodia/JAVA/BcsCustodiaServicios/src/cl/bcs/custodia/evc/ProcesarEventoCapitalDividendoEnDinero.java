package cl.bcs.custodia.evc;

import java.math.BigDecimal;
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

import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA.UpdateEventoCapitalCliente;
import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente;
import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.evc.ProcesarEventoCapital.SalidaClientesConDerecho;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.actualizaEventoCapital;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalDividendoEnDinero implements JavaCall {

	private static final Logger logger 									= LogManager.getLogger(ProcesarEventoCapitalDividendoEnDinero.class);
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_InsertEventoCapitalCliente.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_InsertEventoCapitalClienteFormaDePago.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL						= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_UpdateEventoCapital.sql");
	private final String SQL_SELECT_CLIENTE_OPERA_CUENTA_INVERSION		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_SelectClienteOperaCuentaInversion.sql");
	private final String SQL_SELECT_FORMA_PAGO_MODALIDAD                = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_SelectFormaPagoModalidad.sql");
	private final String SQL_UPDATE_FORMA_PAGO_MODALIDAD                = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDividendoEnDinero_UpdateFormaPagoModalidad.sql");
	                                                                         
	public static class ConsultaBoveda{
		public String 			IdBoveda;
		public String			NumeroCuentaBoveda;
		public String			IdTipoCuentaCustodio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
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
	
	public static class ConsultaBovedas{
		public String	IdBoveda;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaBolsa{
		public String	IdBolsa;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String 	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaContraparte {
		public Long		IdInstrumento;
		public String	TipoCustodia;
		public String	IndicadorOD;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String 	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class actualzaEventoCapital{
		public Long		IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaEjecutivoPortafolio{
		public Long			IdPortafolio;
		public Long			IdEmpleadoEjecutivoPortafolio;
		public String		CodigoUsuarioAgente;
		public String		NombreAgente;
		public String		CodigoSucursal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class objConsultaCuentaCustodio{
		public Long		IdPortafolio;
		public String	IdBoveda;
		public String	CuentaCustodio;
		public String	IdTipoCuentaCustodia; 
		public String	NombreTipoCuentaCustodia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO: Clase de entrada
	public static class ProcesarEventoCapitalDividendoEnDineroEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalDividendoEnDineroEntrada (){
			p_EventoCapital 			= new ArrayList<EventoCapital>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class Obj_p_out_InformacionDetalle {
		public String	Codigo;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class Obj_p_out_ErroresDetalle {
		public String	Codigo;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Obj_p_out_AdvertenciasDetalle {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	public static class ProcesarEventoCapitalDividendoEnDineroSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ProcesarEventoCapitalDividendoEnDineroSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class listaInstrumentoProcesado {
		public Long 			IdReferencial;
		public Long 			IdInstrumento;
		public String 			Nemotecnico;
		public String 			NemoSVS;
		public String 			IdTipoInstrumento; // L = 3
		public BigDecimal		Cantidad = new BigDecimal(0L);
		//public BigDecimal 		MontoACompra = new BigDecimal(0L);
		//public BigDecimal 		MontoACompraMonedaLocal = new BigDecimal(0L);
		public BigDecimal 		ValorRescate = new BigDecimal(0L);
		public BigDecimal 		PrecioMercado = new BigDecimal(0L);
		public BigDecimal 		TasaMercado = new BigDecimal(0L);
		public BigDecimal 		MontoAMercadoMonedaSistema = new BigDecimal(0L);
		public BigDecimal 		MontoAMercadoMonedaInstrumento = new BigDecimal(0L);		
		//public BigDecimal 		PrecioVector = new BigDecimal(0L);
		//public BigDecimal 		MontoVector = new BigDecimal(0L);
		public String 			IdMonedaInstrumento;
		public java.sql.Date 	FechaLiquidacionInstrumento;
		public String 			IdEmisor; // L = 40
		public java.sql.Date 	FechaEmisionInstrumento;
		public java.sql.Date 	FechaVencimientoInstrumento;
		public Long 			PlazoEnDias;
		public String 			IdTipoReajuste; // L = 3
		public String 			EsFungible; // L = 1
		public String 			IdTipoInstrumentoGarantizado; // L = 3
		public String 			IdBolsa; // L = 3
		public Long 			PlazoEnDías; // L = 3
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosEjecutivo {
		public Long		IdPortafolio;
		public Long		IdEmpleadoEjecutivoPortafolio;
		public String	CodigoEmpleado;
		public String	NombreCompletoEjecutivo;
		public String	CodigoSucursal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosVendedorTransito{
		public Long		IdRegistro;
		public String	TipoCustodia;
		public String	IndicadorOD;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class DatosCuentaCustodio{
		public Long		IdPortafolio;
		public String	Codigo;
		public String	IdTipoCuentaCustodia;
		public String	LiquidaDividendos;
		public String	IdBoveda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosSocioDeNegocio {
		public Long		IdSocioDeNegocio;
		public Long		IdPersona;
		public Long		IdEmpresa;
		public Long		IdCliente;
		public Long		IdEmpleado;
		public Long		IdOperador;
		public Long		IdProveedor;
		public Long		IdContraparte;
		public String	Borrado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosCliente{
		public Long		IdCliente;
		public String	TipoDocumentoIdentidad;
		public String	DocumentoIdentidad;
		public String	NombreCompleto;
		public Long		IdPaisResidencia;
		public String	IdTipoPersona;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosOperacion{
		public Long		IdCartera;
		public String	IndicadorOD;
		public String	IndicadorCustodia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class DatosTipoCartera{
		public String		IdTipoCartera;
		public BigDecimal	CantidadDerecho;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ConsultaMedioPagoPorDefecto{
		public String		IdMedioDePago;
		public String		Nombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class EventoCapitalFormaPago{
		public Long			IdEventoCapital;
		public Long			IdUsuarioIngreso;
		public Long			IdEmpresa;
		public Long         IdEventoCapitalFormaPago;
		public String		DescuentaCuentaInv;
 //   	public String		NombreMedioDePago;
 //  	public String		IdModalidadPago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	////
	public static class EventoCapitalFormaPagoModalidad extends BcsPojo{
		public Long			IdEventoCapital;
		public Long         IdEventoCapitalCliente;
		public Long			IdEventoCapitalClienteFormaDePago;///
	}
		
	///
	public static class EventoCapitalIdEvtoGtia{
		public Long IdEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO: Logica
	public ProcesarEventoCapitalDividendoEnDineroSalida logica(Connection con, ProcesarEventoCapitalDividendoEnDineroEntrada in) throws Exception {
		ProcesarEventoCapitalDividendoEnDineroSalida out = new ProcesarEventoCapitalDividendoEnDineroSalida();
		SQL sql=new SQL();

		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		ProcesarEventoCapital ev = new ProcesarEventoCapital();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************			
		// Consulta empresa
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		List<ObjEventoCapital_Entrada> ListaIds = new ArrayList<ObjEventoCapital_Entrada>();
		List<EventoCapitalIdEvtoGtia> IdEvenCapGtia = new ArrayList<EventoCapitalIdEvtoGtia>();		
		List<ObjEventoCapitalCliente_Entrada> ListaCliIds = new ArrayList<ObjEventoCapitalCliente_Entrada>();
		
		String DescuentaCuentaInv = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "EventoCapitalDescuentaCuentaInversion");
		
		for (EventoCapital recorreEventoCapital: in.p_EventoCapital){
			String vIdEstado = new String();
			vIdEstado = "PRO";
			List<actualizaEventoCapital> idEventoCapital = new ArrayList<actualizaEventoCapital>();
			SalidaClientesConDerecho CCD = ev.getClientesConDerecho(con, recorreEventoCapital, in.p_IdModulo, empresa);
			out.P_OUT_MENSAJES.addAll(CCD.P_OUT_MENSAJES);
			HashMap<String,String> params = new HashMap<String,String>();
			HashMap<String,Object> paramsGtia = new HashMap<String,Object>();

			if (MensajeSalida.getNumeroErrores(CCD.P_OUT_MENSAJES) == 0){
				
				List<Long> secuenciaEventoCapitalClienteContrapartes 	= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(CCD.P_OUT_CONTRAPARTES.size()));
				
				for (EventoCapitalCliente recorreContrapartes : CCD.P_OUT_CONTRAPARTES) {	
					
					recorreContrapartes.IdEventoCapitalCliente 	= secuenciaEventoCapitalClienteContrapartes.get(CCD.P_OUT_CONTRAPARTES.indexOf(recorreContrapartes));
					
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
				
				//TODO: INSERT EVENTO CAPITAL CLIENTE
				List<EventoCapitalCliente> insertEventoClienteFull = unionLists(CCD.P_OUT_CLIENTES, CCD.P_OUT_CONTRAPARTES);
				
				if (insertEventoClienteFull.size()>0 ){
					for (EventoCapitalCliente recorreInsert: insertEventoClienteFull){
						if (!recorreInsert.TipoSocioNegocio.equalsIgnoreCase("CNT")){
							ObjEventoCapitalCliente_Entrada cli = new ObjEventoCapitalCliente_Entrada();
							cli.p_IdEventoCapitalCliente = recorreInsert.IdEventoCapitalCliente;
							ListaCliIds.add(cli);
						}
						if (recorreInsert.IdEstado.equals("PEN")){
							vIdEstado = "PEN";
						}
					}								
					
					if ( "RUE".equalsIgnoreCase(recorreEventoCapital.Origen) ) {
						
						BuscarMonedaPreferenciaPagoEventoCapitalCliente buscarMonedaPreferenciaPagoEventoCapitalCliente = new BuscarMonedaPreferenciaPagoEventoCapitalCliente();
						EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente = new EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente();
						
						entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdEmpresa = in.p_IdEmpresa;
						entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdModulo = in.p_IdModulo;
						entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_IdUsuario = in.p_IdUsuario;
						entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_EventoCapital = recorreEventoCapital;
						entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.p_EventoCapitalCliente = insertEventoClienteFull;
						
						SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente = buscarMonedaPreferenciaPagoEventoCapitalCliente.logica(con, entradaBuscarMonedaPreferenciaPagoEventoCapitalCliente);
						out.P_OUT_MENSAJES.addAll(salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente.P_OUT_MENSAJES);
						insertEventoClienteFull = salidaBuscarMonedaPreferenciaPagoEventoCapitalCliente.P_OUT_RESULTADO;
					}
					
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFull , EventoCapitalCliente.class);
					
					logger.info("ProcesarEventoCapitalDividendoEnDinero - Fin Insert Eventos Capital Cliente");	
					ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
																	vIdEstado, recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFull.size(), recorreEventoCapital.Nemo);
					
					//Medios de pago.
					List<EventoCapitalFormaPago> insertEventoClienteFormaPago = new ArrayList<EventoCapitalFormaPago>();
					EventoCapitalFormaPago formaPago = new EventoCapitalFormaPago();
					formaPago.IdEventoCapital 	= recorreEventoCapital.IdEventoCapital;
					formaPago.IdEmpresa			= recorreEventoCapital.IdEmpresa;
					formaPago.IdUsuarioIngreso	= recorreEventoCapital.IdUsuario;
					insertEventoClienteFormaPago.add(formaPago);
					
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO, insertEventoClienteFormaPago, EventoCapitalFormaPago.class);
					logger.info(insertEventoClienteFormaPago);
					logger.info("ProcesarEventoCapitalDividendoEnDinero - Fin Insert Formas de Pago Eventos Capital Cliente");
					
					
					//Select para traer idEventoCapitalFormaDePago Asociado al eventoCapitalCliente
					
					List<EventoCapitalFormaPagoModalidad> busquedaFormaPagoModalidad = this.ConsultaEventoCapitalFormaPagoModalidad(con, recorreEventoCapital.IdEventoCapital);
					logger.info(busquedaFormaPagoModalidad);
					
         			
					//Update EventoCapitalCliente, pasando IdEventoCapitalClienteFormaDePago
					List<EventoCapitalCliente> updateEventoFormaPago= new ArrayList<EventoCapitalCliente>();
					sql.batch(con, SQL_UPDATE_FORMA_PAGO_MODALIDAD, busquedaFormaPagoModalidad, EventoCapitalFormaPagoModalidad.class);
					logger.info(updateEventoFormaPago);
					
					
					//Envío de Notificación
					ObjEventoCapital_Entrada NotifIN = new ObjEventoCapital_Entrada();
					NotifIN.p_IdEventoCapital = recorreEventoCapital.IdEventoCapital;
					ListaIds.add(NotifIN);
						
				}
				actualizaEventoCapital idEven = new actualizaEventoCapital();
				idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				idEven.IdEstado = vIdEstado;
				idEventoCapital.add(idEven);
				
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, actualizaEventoCapital.class);
				logger.info("ProcesarEventoCapitalDividendoEnDinero - Fin Update Eventos Capital");	
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
				
			}else {
				params.clear();
				params.put("Nemo con error", recorreEventoCapital.Nemo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"Nemo con error: "+recorreEventoCapital.Nemo,params,con,in.p_IdEmpresa));
				System.out.println("pare");
			}
			
		}

		return out;
	};		

	private  List<EventoCapitalFormaPagoModalidad> ConsultaEventoCapitalFormaPagoModalidad(Connection con, Long evCapi) throws SQLException {
		logger.debug("INICIO::ProcesarEventoCapitalDividendoEnDinero.ConsultaEventoCapitalFormaPagoModalidad");
		long inicioMetodo=System.currentTimeMillis();
		HashMap<String,Object> params 	= new HashMap<>(1);
		params.put("IdEventoCapital", evCapi);
		SQL sql = new SQL();
		List<EventoCapitalFormaPagoModalidad> busquedaFormaPagoModalidad = new ArrayList<EventoCapitalFormaPagoModalidad>();
		busquedaFormaPagoModalidad = sql.query(con, SQL_SELECT_FORMA_PAGO_MODALIDAD, params , EventoCapitalFormaPagoModalidad.class);

		long finMetodo=System.currentTimeMillis();
		logger.debug("FIN::ProcesarEventoCapitalDividendoEnDinero.ConsultaEventoCapitalFormaPagoModalidad",finMetodo-inicioMetodo);
		return busquedaFormaPagoModalidad;
		
	}

	
	private ConsultaCliente ConsultaCliente(Connection con, SQL sql, EventoCapitalCliente contraparte) throws SQLException {
		logger.info("INICIO::ProcesarEventoCapitalDividendoEnDinero.ConsultaCliente");
		
		HashMap<String,Object> params 	= new HashMap<>(1);
		params.put("DocumentoIdentidad", contraparte.DocumentoIdentidad);
		List<ConsultaCliente> busquedaCliente = sql.query(con, SQL_SELECT_CLIENTE_OPERA_CUENTA_INVERSION, params , ConsultaCliente.class);

		logger.info("FIN::ProcesarEventoCapitalDividendoEnDinero.ConsultaCliente");

		return busquedaCliente.size() > 0 ? busquedaCliente.get(0) : new ConsultaCliente();
	}

	public <EventoCapitalCliente> List<EventoCapitalCliente> unionLists (	List<EventoCapitalCliente> list1, 
			List<EventoCapitalCliente> list2) {
		
		Set<EventoCapitalCliente> setNuevaLista = new HashSet<EventoCapitalCliente>();
		
		setNuevaLista.addAll(list1);
		setNuevaLista.addAll(list2);

		return new ArrayList<EventoCapitalCliente>(setNuevaLista);
	};
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ProcesarEventoCapitalDividendoEnDineroEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalDividendoEnDineroEntrada.class);
		ProcesarEventoCapitalDividendoEnDineroSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_EventoCapital\":[{\"IdEventoCapital\":6819145,\"IdEmpresa\":1,\"IdTipoEventoCapital\":\"DV\",\"Secuencia\":1,\"Descripcion\":\"PRUEBA JPZ INGEVEC EN DINERO\",\"EsInscrito\":\"S\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":1203,\"Nemo\":\"INGEVEC\",\"Factor\":0.08546550,\"Valor\":0E-8,\"IdMoneda\":\"CLP\",\"IdInstrumentoNuevo\":0,\"NemoNuevo\":\"0\",\"FactorNuevo\":1.00000000,\"FechaActualizacion\":\"2018-07-31 00:00:00.000\",\"FechaLimite\":\"2018-07-31 00:00:00.000\",\"FechaPago\":\"2018-08-07 00:00:00.000\",\"NumeroDividendo\":1101901,\"IdTipoDividendo\":\"P\",\"PrecioAnterior\":0E-8,\"FechaHoraIngresoRegistro\":\"2018-07-31 00:00:00.000\",\"FechaHoraUltimaActualizacion\":\"2018-08-07 00:00:00.000\",\"TipoIngreso\":\"C\",\"IdUsuario\":16,\"IdEstado\":\"VIG\",\"CuentaDepositanteSorteoLetras\":0,\"MonedaSorteoLetras\":0,\"GlosaMonedaSorteoLetras\":\"\",\"EmisorPagadorSorteoLetras\":0,\"EmisorSorteoLetras\":0,\"InstrumentoSorteoLetras\":0,\"GlosaInstrumentoSorteoLetras\":\"\",\"BloqueoSorteoLetras\":0,\"SorteadoSorteoLetras\":0.0000,\"APagarSorteoLetras\":0.0000,\"APagarPesosSorteoLetras\":0,\"IdEmisor\":\"INGEVEC\",\"IdMonedaLiquidacion\":\"CLP\",\"Paridad\":1.00000000,\"IdEmisorNuevo\":\"\",\"EstadoNotificacion\":\"N\",\"IdInstrumentoAnterior\":0,\"Indice\":0,\"Impuesto\":0E-8,\"CantidadCuotas\":null,\"Monto\":null}]}";
				ProcesarEventoCapitalDividendoEnDineroEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalDividendoEnDineroEntrada.class);
								
				ProcesarEventoCapitalDividendoEnDinero  kk = new ProcesarEventoCapitalDividendoEnDinero();
				ProcesarEventoCapitalDividendoEnDineroSalida Objout = kk.logica(con, Objin);
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