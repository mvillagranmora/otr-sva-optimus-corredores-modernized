package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BuscarMonedaPreferenciaPagoEventoCapitalCliente implements JavaCall {

	private static final Logger logger 											= LogManager.getLogger(BuscarMonedaPreferenciaPagoEventoCapitalCliente.class);
	private final String SQL_SELECT_CONFIGURACION_INSTRUMENTO_PAGO_DIVIDENDOS	= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectConfiguracionInstrumentoPagoDividendos.sql");
	private final String SQL_SELECT_MONEDA_PAGO_DIVIDENDOS						= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectMonedaPagoDividendos.sql");
	private final String SQL_SELECT_CLIENTE_MONEDA_PAGO_DIVIDENDOS				= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectClienteMonedaPagoDividendos.sql");
	private final String SQL_SELECT_FACTOR_OTRAS_MONEDAS_PAGO_DIVIDENDOS		= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectFactorOtrasMonedaPagoDividendos.sql");
	private final String SQL_SELECT_EMISOR					            		= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectEmisor.sql");
	private final String SQL_SELECT_BOVEDA					            		= loadResourceToString("cl/bcs/custodia/sql/BuscarMonedaPreferenciaPagoEventoCapitalCliente_SelectBoveda.sql");
	
	public static class EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente extends BcsPojo {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public EventoCapital				p_EventoCapital;
		public List<EventoCapitalCliente>	p_EventoCapitalCliente;
		
		public EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente (){
			p_EventoCapitalCliente	= new ArrayList<EventoCapitalCliente>();
		}
			
	};
	
	public static class SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente extends BcsPojo {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<EventoCapitalCliente>	P_OUT_RESULTADO;

		
		public SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<EventoCapitalCliente>();
		}
	
	}
	
	public static class PreferenciaMonedaPagoDividendos extends BcsPojo {
		
		public Long		IdPortafolio;
		public Long		IdCliente;
		public String	MonedaPeferenciaPago;

		public PreferenciaMonedaPagoDividendos() {
			this.MonedaPeferenciaPago = "CLP";
		}
		
	};
	
	public static class VariacionCapitalLog extends BcsPojo {
		
		public Timestamp HoraAuditoria;
		public BigDecimal Secuencia;
		public String IdMoneda;
		public String Nemotecnico;
		public BigDecimal Factor;
		
		public VariacionCapitalLog() {
			this.IdMoneda = "";
		}
		
	};
	
	public static class ConfiguracionInstrumentoPagoDividendosOtrasMonedas extends BcsPojo {
		
		public Long IdInstrumento;
		public String DividendoOtraMoneda;
		
	};
		
	public static class ConsultaEmisor extends BcsPojo {
		
		public String IdEmisor;
		public String DocumentoIdentidadEmisor;
		public String NombreClienteEmisor;
		
	};
	
	public static class ConsultarBoveda extends BcsPojo {
		
		public String IdBoveda;
		public String TipoSocioNegocio;
		public String DocumentoIdentidad;
		public String NombreCliente;
	};
	
	public static class MonedaPagoDividendos extends BcsPojo {
		
		public String IdMoneda;
		
		public MonedaPagoDividendos() {
			this.IdMoneda = "USD";
		}
	};
	
	public SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente logica(Connection con, EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente in) throws Exception {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.logica");
		SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente out 	= new SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente();
		RevisarRequisitosBasicos basicos 							= new RevisarRequisitosBasicos();
	    Long inicio 												= System.currentTimeMillis();
	    
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		if ( in.p_EventoCapitalCliente.size() == 0){
			return out;
		}
		
		ConfiguracionInstrumentoPagoDividendosOtrasMonedas configuracionInstrumentoPagoDividendoOtrasMonedas = obtenerConfiguracionInstrumentoPagoDividendoOtrasMonedas(con, in.p_EventoCapital.IdInstrumento);
		
		if( "N".equalsIgnoreCase(configuracionInstrumentoPagoDividendoOtrasMonedas.DividendoOtraMoneda) ) {
			out.P_OUT_RESULTADO = in.p_EventoCapitalCliente;
			return out;
		}
		
		List<VariacionCapitalLog> listadoFactoresPorMoneda = obtenerFactorPorMoneda(con, in.p_EventoCapital, obtenerMonedaPagoDividendos(con), configuracionInstrumentoPagoDividendoOtrasMonedas.DividendoOtraMoneda);
		
		if( listadoFactoresPorMoneda.size() == 0 ) {
			MensajeSalida msg = new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_NO_EXISTE_FACTOR_TABLA_VARIACIONCAPIALLOG", new HashMap<String,String>(), con, in.p_IdEmpresa);
			out.P_OUT_MENSAJES.add(msg);
			out.P_OUT_RESULTADO = in.p_EventoCapitalCliente;
			return out;
		}
		
		List<Long> idsPortafolios = in.p_EventoCapitalCliente
										.stream()
							            .collect(Collectors.toSet())
							            .stream()
										.map(item -> item.IdPortafolio)
										.collect(Collectors.toList());
		
		List<PreferenciaMonedaPagoDividendos> listadoPreferenciaMonedaPagoDividendosClientes = obtenerMonedaPreferenciaPagoDividendosClientes(con, idsPortafolios);

		List<EventoCapitalCliente> listadoContrapartes 				= new ArrayList<EventoCapitalCliente>();
		BigDecimal montoDescontarClpEmisor 							= new BigDecimal(0);
		BigDecimal cantidadDerechoDescontarEmisorClp 				= new BigDecimal(0);					
		HashMap<String, BigDecimal> montoIngresarPorMonedaEmisor 	= new HashMap<String, BigDecimal>();
		List<EventoCapitalCliente> listadoClientes 					= new ArrayList<EventoCapitalCliente>();
		
		for ( EventoCapitalCliente eventoCapitalCliente: in.p_EventoCapitalCliente ) {
			
			eventoCapitalCliente.FactorOtraMoneda = in.p_EventoCapital.Factor;
			
			if ( !eventoCapitalCliente.TipoSocioNegocio.equalsIgnoreCase("CNT") && "CUS".equalsIgnoreCase(eventoCapitalCliente.TipoCartera) && "DCV".equalsIgnoreCase(eventoCapitalCliente.IdBoveda) ){
				
				PreferenciaMonedaPagoDividendos buscarPreferenciaMonedaPagoDividendos = listadoPreferenciaMonedaPagoDividendosClientes
																								.stream()
																								.filter(item -> item.IdPortafolio.compareTo(eventoCapitalCliente.IdPortafolio) == 0 )
																								.findFirst()
																								.orElse(new PreferenciaMonedaPagoDividendos());
				VariacionCapitalLog nuevaMonedaCalculoFactor = listadoFactoresPorMoneda
																	.stream()
																	.filter(item -> item.IdMoneda.equalsIgnoreCase(buscarPreferenciaMonedaPagoDividendos.MonedaPeferenciaPago) )
																	.findFirst()
																	.orElse(new VariacionCapitalLog());
				
				if(  !("".equals(nuevaMonedaCalculoFactor.IdMoneda)) && 
					 !(in.p_EventoCapital.IdMoneda.equalsIgnoreCase(nuevaMonedaCalculoFactor.IdMoneda)) ) {
					
					eventoCapitalCliente.FactorOtraMoneda = nuevaMonedaCalculoFactor.Factor;
					montoDescontarClpEmisor = montoDescontarClpEmisor.add(eventoCapitalCliente.Total);
					cantidadDerechoDescontarEmisorClp = cantidadDerechoDescontarEmisorClp.add(eventoCapitalCliente.CantidadDerecho);
					
					eventoCapitalCliente.IdMoneda = buscarPreferenciaMonedaPagoDividendos.MonedaPeferenciaPago;
					eventoCapitalCliente.Total = BigDecimal.ZERO;						
					eventoCapitalCliente.Total = (eventoCapitalCliente.IdTipoEventoCapital.equals("DV") || eventoCapitalCliente.IdTipoEventoCapital.equals("RC"))
													? (eventoCapitalCliente.Total.add(eventoCapitalCliente.CantidadDerecho.multiply(in.p_EventoCapital.Paridad).multiply(nuevaMonedaCalculoFactor.Factor))) 
													: BigDecimal.ZERO;
					
					if( montoIngresarPorMonedaEmisor.get(eventoCapitalCliente.IdMoneda) != null ) {
						BigDecimal nuevoMontoemisor = montoIngresarPorMonedaEmisor.get(eventoCapitalCliente.IdMoneda);
						nuevoMontoemisor = nuevoMontoemisor.add(eventoCapitalCliente.Total);
						montoIngresarPorMonedaEmisor.put(eventoCapitalCliente.IdMoneda, nuevoMontoemisor);
					}
					else
						montoIngresarPorMonedaEmisor.put(eventoCapitalCliente.IdMoneda, eventoCapitalCliente.Total);
					
				}
				
				listadoClientes.add(eventoCapitalCliente);
				
			} else if( eventoCapitalCliente.TipoSocioNegocio.equalsIgnoreCase("CNT") ) {
				listadoContrapartes.add(eventoCapitalCliente);
			} else {
				listadoClientes.add(eventoCapitalCliente);
			}
		
		}				
				
		out.P_OUT_RESULTADO.addAll(listadoClientes);
		out.P_OUT_RESULTADO.addAll(obtenerContrapartesNuevosMontos(con, in, listadoContrapartes, montoDescontarClpEmisor, montoIngresarPorMonedaEmisor, cantidadDerechoDescontarEmisorClp));
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.logica", fin - inicio);
		return out;
		
	};
	
	
	private ConfiguracionInstrumentoPagoDividendosOtrasMonedas obtenerConfiguracionInstrumentoPagoDividendoOtrasMonedas(Connection con, Long p_IdInstrumento) throws SQLException {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerConfiguracionInstrumentoPagoDividendoOtrasMonedas");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		parametros.put("IdInstrumento", p_IdInstrumento);
		
		ConfiguracionInstrumentoPagoDividendosOtrasMonedas configuracionInstrumentoPagoDividendosOtrasMonedas = sql
					.query(con, SQL_SELECT_CONFIGURACION_INSTRUMENTO_PAGO_DIVIDENDOS, parametros, ConfiguracionInstrumentoPagoDividendosOtrasMonedas.class)
					.parallelStream()
					.findFirst()
					.orElse(new ConfiguracionInstrumentoPagoDividendosOtrasMonedas());
		
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerConfiguracionInstrumentoPagoDividendoOtrasMonedas");
		return configuracionInstrumentoPagoDividendosOtrasMonedas;
	}
	
	private List<MonedaPagoDividendos> obtenerMonedaPagoDividendos(Connection con) throws SQLException {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerMonedaPagoDividendos");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		
		
		List<MonedaPagoDividendos> listadoMonedaPagoDividendos = sql.query(con, SQL_SELECT_MONEDA_PAGO_DIVIDENDOS, parametros, MonedaPagoDividendos.class);
	
		
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerMonedaPagoDividendos");
		return listadoMonedaPagoDividendos;
	}
	
	private List<VariacionCapitalLog> obtenerFactorPorMoneda(Connection con, EventoCapital eventoCapital, List<MonedaPagoDividendos> listadoMonedas, String DividendoOtraMoneda ) throws SQLException {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerFactorPorMoneda");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		
		List<String> idMoneda = listadoMonedas
									.stream()
									.map(item -> item.IdMoneda)
									.collect(Collectors.toList());
		
		parametros.put("Secuencia", eventoCapital.Secuencia);
		parametros.put("Nemotecnico", eventoCapital.Nemo);
		parametros.put("IdMoneda", idMoneda);
		List<VariacionCapitalLog> obtenerMonedasConFactores = sql.query(con, SQL_SELECT_FACTOR_OTRAS_MONEDAS_PAGO_DIVIDENDOS, parametros, VariacionCapitalLog.class);
		
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerFactorPorMoneda");
		return obtenerMonedasConFactores;
	}

	private List<PreferenciaMonedaPagoDividendos> obtenerMonedaPreferenciaPagoDividendosClientes(Connection con, List<Long> idsPortafolios) throws SQLException {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerMonedaPreferenciaPagoDividendosClientes");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
	    parametros.put("IdsPortafolios", idsPortafolios);

		List<PreferenciaMonedaPagoDividendos> listadoPreferenciaMonedaPagoDividendosClientes = sql.query(con, SQL_SELECT_CLIENTE_MONEDA_PAGO_DIVIDENDOS, parametros, PreferenciaMonedaPagoDividendos.class);
		
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerMonedaPreferenciaPagoDividendosClientes");
		return listadoPreferenciaMonedaPagoDividendosClientes;
	}
	
	private List<EventoCapitalCliente> obtenerContrapartesNuevosMontos(Connection con, EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente in, List<EventoCapitalCliente> listadoContrapartes, BigDecimal montoDescontarClpEmisor, HashMap<String, BigDecimal> montoIngresarPorMonedaEmisor, BigDecimal cantidadDerechoDescontarEmisorClp) throws Exception {
		
		logger.info("INICIO::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerContrapartesNuevosMontos");
		SQL sql 							= new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		String parametroEmisor;
		
		if( montoDescontarClpEmisor.compareTo(BigDecimal.ZERO) == 0 ) {
			return listadoContrapartes;
		}
		
		try {
			parametroEmisor					= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.NombreEmisor");
		} catch(Exception e) {
			parametroEmisor	= "S";
		}
		
		parametros.put("IdInstrumento", in.p_EventoCapital.IdInstrumento);
		ConsultaEmisor emisor = sql.query(con, SQL_SELECT_EMISOR, parametros, ConsultaEmisor.class)
									.parallelStream()
									.findFirst()
									.orElse(new ConsultaEmisor());
		
		if( "N".equalsIgnoreCase(parametroEmisor) ) {
			
			parametros.clear();
			parametros.put("IdBoveda", "DCV");
			parametros.put("IdEmpresa", in.p_IdEmpresa);
			ConsultarBoveda boveda = sql.query(con, SQL_SELECT_BOVEDA, parametros, ConsultarBoveda.class)
										.parallelStream()
										.findFirst()
										.orElse(new ConsultarBoveda());
			emisor.DocumentoIdentidadEmisor = boveda.DocumentoIdentidad;
			emisor.NombreClienteEmisor = boveda.NombreCliente;
			
		}
		
		List<EventoCapitalCliente> contrapartesNuevosMontos = new ArrayList<EventoCapitalCliente>();

		for( EventoCapitalCliente contraparte: listadoContrapartes ) {
			
			if (  "CUS".equalsIgnoreCase(contraparte.TipoCartera) && contraparte.DocumentoIdentidad.equalsIgnoreCase(emisor.DocumentoIdentidadEmisor) ) {
				
				contraparte.Total = contraparte.Total.add(montoDescontarClpEmisor);
				contraparte.CantidadDerecho = contraparte.CantidadDerecho.add(cantidadDerechoDescontarEmisorClp);
				
				for( String moneda: montoIngresarPorMonedaEmisor.keySet() ) {
					
					EventoCapitalCliente contraparteEmisorOtraMoneda 		= new EventoCapitalCliente();
					List<Long> secuenciaEventoCapitalClienteContrapartes 	= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", 1L);
					contraparteEmisorOtraMoneda = Utils.copyPojo(contraparte, contraparteEmisorOtraMoneda);
					contraparteEmisorOtraMoneda.IdEventoCapitalCliente = secuenciaEventoCapitalClienteContrapartes.get(0);
					contraparteEmisorOtraMoneda.Total = new BigDecimal(0);
					contraparteEmisorOtraMoneda.IdMoneda = moneda;
					contraparteEmisorOtraMoneda.CantidadDerecho = cantidadDerechoDescontarEmisorClp.multiply(new BigDecimal(-1)).setScale(0, RoundingMode.DOWN);
					contraparteEmisorOtraMoneda.Total = MonedaFormato.formatear(con, montoIngresarPorMonedaEmisor.get(moneda).multiply(new BigDecimal(-1)) , MonedaFormato.MONTO, moneda).valor;
					contrapartesNuevosMontos.add(contraparteEmisorOtraMoneda);
				}
			}
		}
		
		listadoContrapartes.addAll(contrapartesNuevosMontos);
		
		logger.info("FIN::BuscarMonedaPreferenciaPagoEventoCapitalCliente.obtenerContrapartesNuevosMontos");
		return listadoContrapartes; 
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente jin=gson.fromJson(in.toString() ,EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.class);
		SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_EventoCapital\":[{\"IdEventoCapital\":365125,\"IdEmpresa\":1,\"IdTipoEventoCapital\":\"DV\",\"Secuencia\":1,\"Descripcion\":\"PRUEBA JPZ INGEVEC EN DINERO\",\"EsInscrito\":\"S\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":1203,\"Nemo\":\"INGEVEC\",\"Factor\":0.08546550,\"Valor\":0E-8,\"IdMoneda\":\"CLP\",\"IdInstrumentoNuevo\":0,\"NemoNuevo\":\"0\",\"FactorNuevo\":1.00000000,\"FechaActualizacion\":\"2018-07-31 00:00:00.000\",\"FechaLimite\":\"2018-07-31 00:00:00.000\",\"FechaPago\":\"2018-08-07 00:00:00.000\",\"NumeroDividendo\":1101901,\"IdTipoDividendo\":\"P\",\"PrecioAnterior\":0E-8,\"FechaHoraIngresoRegistro\":\"2018-07-31 00:00:00.000\",\"FechaHoraUltimaActualizacion\":\"2018-08-07 00:00:00.000\",\"TipoIngreso\":\"C\",\"IdUsuario\":16,\"IdEstado\":\"VIG\",\"CuentaDepositanteSorteoLetras\":0,\"MonedaSorteoLetras\":0,\"GlosaMonedaSorteoLetras\":\"\",\"EmisorPagadorSorteoLetras\":0,\"EmisorSorteoLetras\":0,\"InstrumentoSorteoLetras\":0,\"GlosaInstrumentoSorteoLetras\":\"\",\"BloqueoSorteoLetras\":0,\"SorteadoSorteoLetras\":0.0000,\"APagarSorteoLetras\":0.0000,\"APagarPesosSorteoLetras\":0,\"IdEmisor\":\"INGEVEC\",\"IdMonedaLiquidacion\":\"CLP\",\"Paridad\":1.00000000,\"IdEmisorNuevo\":\"\",\"EstadoNotificacion\":\"N\",\"IdInstrumentoAnterior\":0,\"Indice\":0,\"Impuesto\":0E-8,\"CantidadCuotas\":null,\"Monto\":null}]}";
				EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente Objin = gson.fromJson(json, EntradaBuscarMonedaPreferenciaPagoEventoCapitalCliente.class);
								
				BuscarMonedaPreferenciaPagoEventoCapitalCliente  kk = new BuscarMonedaPreferenciaPagoEventoCapitalCliente();
				SalidaBuscarMonedaPreferenciaPagoEventoCapitalCliente Objout = kk.logica(con, Objin);
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