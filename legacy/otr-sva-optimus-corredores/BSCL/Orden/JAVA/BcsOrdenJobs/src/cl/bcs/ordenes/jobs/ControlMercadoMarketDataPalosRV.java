package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.ordenes.AnularAsignaciones;
import cl.bcs.ordenes.AnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearOrdenesPostreras;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRVMarketData_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionRVMarketData;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosRV extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoMarketDataPalosRV.class);

	private final String SQL_DELETE_HISTORICOS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_DeleteRegistrosHistoricos.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectTransaccionesAnular.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectTransaccionesOtrosCorredores.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_INSERT_HIS_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_Insert_His_Transacciones.sql");
	private final String SQL_SELECT_CORREDORAS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectCorredoras.sql");
	private final String SQL_SELECT_HIS_PALOS_RV = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectHisPalosRV.sql");
	private final String SQL_SELECT_ORDEN_ANTERIOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectOrdenAnterior.sql");
	//private final String SQL_SELECT_MOVIMIENTO_ANTERIOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectMovimientoAnterior.sql");
	private final String SQL_SELECT_ASIGNACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectAsignaciones.sql");
	private final String SQL_SELECT_MOVIMIENTO_FACTURAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRV_SelectMovimientoFacturar.sql");
	private final String SQL_SELECT_ANULACION_MODIFICACION = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionAnulacionModificacion.sql");
	private final String SQL_SELECT_TRANSACCION_OPERADOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionOperador.sql");

	private static final String FLUJO_MODIFICACION_AUTO = "MODIFICACION_AUTO";
	private static final String MOTIVO_ANULACION = "ANULACION POR SISTEMA BURSATIL";
	private static final String COMPRA = "COM";
	private static final String VENTA = "VEN";

	public static class TransaccionRV_Proceso extends TransaccionRVMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
		public Long IdTransaccionActual;
		public String EsModificacionMarketData = "N";

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Usuario extends BcsPojo {
		public Long IdUsuario;
	}

	public static class TransaccionParaAnular extends BcsPojo {
		public Long IdTransaccion;
		public String Folio;
		public Date FechaSistemaCreacion;
		public TransaccionRV_Proceso referencia;
		public Boolean TieneAsignacionAsociada = false;
		public Boolean TieneMovimientoAsociado = false;
		public String EsModificacionMarketData = "N";
	}

	public static class Corredora extends BcsPojo {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
	}

	public static class Asignaciones extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;

		public Asignaciones() {
			super();
		}

		public Asignaciones(Long idAsignacion, Long idTransaccion) {
			this.IdAsignacion = idAsignacion;
			this.IdTransaccion = idTransaccion;
		}

	}

	public static class MovimientoFacturar extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public Long IdPortafolio;
		public Long IdCliente;
		public String IdEstadoFacturacion;
	}
	
	public static class TransaccionAnulacionModificacion {
		public Long IdTransaccionHistorico;
		public String IdEstadoRegistro;
		public String Folio;
	}
	
	public static class TransaccionOperador extends BcsPojo{
		public Long IdTransaccion;
		public String TipoMensaje;
		public Date FechaSistemaCreacion;
	}

	public static class DatosRestauracion extends BcsPojo {
		public String KeyTransaccion;
		public java.math.BigDecimal Cantidad;
		public String CodigoIFRS;
		public String EsFacturaInmediata;
		public String FechaRecepcion;
		public Long FolioCartera;
		public String FolioTransaccion;
		public String Glosa;
		public String IdBoveda;
		public Long IdCuentaCustodioPortafolio;
		public String IdFormaCustodia;
		public Long IdInterno;
		public String IdMonedaLiquidacionOrden;
		public Long IdPortafolio;
		public Long IdTransaccion;
		public String IngresoDesde;
		public String PagoCobroCuentaInversion;
		public Long PlazoVentaCorta;
		public java.math.BigDecimal TasaVentaCorta;
		public java.math.BigDecimal TipoCambio;
		public java.math.BigDecimal ValorRescate;
		public String IdEstadoFacturacion;
		public String IdTipoOperacion;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	@Override
	public void onError(Throwable t) {}

	@Override
	public void pause() {}
	
	public boolean logica(Connection conn, boolean debug) throws Exception {
		try {
			logger.info("Inicio Ciclo MarketData RV");
			Long idEmpresa = 0L;

			if (debug) {
				idEmpresa = 1L;
			} else {
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			}

			limpiezaHistoricos(conn);
			limpiezaTransaccionesNoEncontradas(conn);
			limpiezaTransaccionesOtrosCorredores(conn);

			// real start
			SQL _sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);
			Long idUsuario = usuarioSistema.get(0).IdUsuario;

			List<TransaccionRV_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String, Object>(), TransaccionRV_Proceso.class);
		
			logger.debug("{} nuevos mensajes.", _transacciones.size());
			if (_transacciones.size() == 0) {
				return true;
			}
			
			//Modificaciones
			List<String> foliosModificacion = _transacciones.stream().collect(Collectors.groupingBy(t -> t.tradeID, Collectors.counting())).entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
			
			String action = null;
			String timeStamp = null;
			List<TransaccionRV_Proceso> transaccionesProcesar = new ArrayList<>();
			List<TransaccionRV_Proceso> transaccionesModificacionProcesar = new ArrayList<>();
			TransaccionRV_Proceso transaccionConModificacionPorIgnorar = null;

			long idInterno = 1;

			for (TransaccionRV_Proceso tx : _transacciones) {				
				tx.registroControlMercado.Log = "";
				tx.registroControlMercado.Procesado = "N";
				tx.registroControlMercado.IdMensaje1 = tx._IdMensaje1;
				tx.registroControlMercado.IdMensaje2 = tx._IdMensaje2;
				
				if (action == null) {
					action = tx.action;
				}

				if (timeStamp == null) {
					timeStamp = tx.timeStamp;
				}

				if ("E".equalsIgnoreCase(action)) {
					if (("I".equalsIgnoreCase(tx.action) || "S".equalsIgnoreCase(tx.action)) && timeStamp.equalsIgnoreCase(tx.timeStamp)) {
						transaccionConModificacionPorIgnorar = tx;
						tx.EsModificacionMarketData = "S";
						transaccionesModificacionProcesar.add(tx);
					}
				}

				if (action != null && !action.equalsIgnoreCase(tx.action)) {
					break;
				}

				tx.IdInterno = idInterno++;
				if(foliosModificacion.contains(tx.tradeID)) {
					tx.EsModificacionMarketData = "S";
					transaccionesModificacionProcesar.add(tx);
				} else {
					transaccionesProcesar.add(tx);
				}
				
			}

			HashMap<String, Corredora> corredoras = buscarCorredoras(conn);
			ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
			InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, idEmpresa);

			if (transaccionConModificacionPorIgnorar != null) {
				Corredora corredoraCompra = corredoras.get(transaccionConModificacionPorIgnorar.buyerBroker.toString());
				Corredora corredoraVenta = corredoras.get(transaccionConModificacionPorIgnorar.sellerBroker.toString());
				if (corredoraCompra != null && corredoraVenta != null) {
					if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora) && !corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
						TransaccionRVMarketData transaccion = buscarTransaccion(conn, transaccionConModificacionPorIgnorar.tradeTime, transaccionConModificacionPorIgnorar.tradeID);
						
						if (transaccion != null) {
							if (ignorarModificacion(transaccion, transaccionConModificacionPorIgnorar)) {
								actualizarControlMercadoTransaccionIgnorada(conn, transaccionConModificacionPorIgnorar);
								transaccionesModificacionProcesar = transaccionesModificacionProcesar.stream()
										.filter(tran -> !tran.tradeID.equalsIgnoreCase(transaccion.tradeID))
										.collect(Collectors.toList());
							}
						}
					}
				}
			}
			
			if (!transaccionesModificacionProcesar.isEmpty()) {
				modificarTransacciones(conn, transaccionesModificacionProcesar, idEmpresa, idUsuario);
			}

			if (!transaccionesProcesar.isEmpty()) {
				if (action.equalsIgnoreCase("E")) {
					anularTransacciones(conn, transaccionesProcesar, idEmpresa, idUsuario);
				}
				if (action.equalsIgnoreCase("S") || action.equalsIgnoreCase("I")) {
					crearTransacciones(conn, transaccionesProcesar, new ArrayList<>(), idEmpresa, idUsuario);
				}
			}

			logger.info("Fin Ciclo MarketData RV");
		} catch (Exception e) {
			throw e;
		}

		return true;
	}
	
	private void modificarTransacciones(Connection conn, List<TransaccionRV_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRV.modificarTransacciones");
		long inicio = System.currentTimeMillis();
		
		List<TransaccionRV_Proceso> transaccionesAnulacion = transacciones.stream().filter(t -> "E".equals(t.action)).collect(Collectors.toList());
		List<TransaccionParaAnular> transaccionesAnuladas = anularTransacciones(conn, transaccionesAnulacion, idEmpresa, idUsuario);
		
		List<TransaccionRV_Proceso> transaccionesCreacion = transacciones.stream().filter(t -> "I".equals(t.action) || "S".equals(t.action)).collect(Collectors.toList());
		crearTransacciones(conn, transaccionesCreacion, transaccionesAnuladas, idEmpresa, idUsuario);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRV.modificarTransacciones tiempo: " + (fin - inicio));
	}
	
	private void limpiezaHistoricos(Connection conn) throws SQLException {
		SQL.exec(conn, SQL_DELETE_HISTORICOS);
		conn.commit();
	}

	private void limpiezaTransaccionesOtrosCorredores(Connection conn) throws SQLException {
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		SQL _sql = new SQL();
		List<TransaccionRV_Proceso> transaccionesOtrosCorredores = _sql.query(conn,SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES, new HashMap<String, Object>(), TransaccionRV_Proceso.class);
		logger.debug("{} transacciones otros corredores detectadas.", transaccionesOtrosCorredores.size() );
		
		for (TransaccionRV_Proceso tran : transaccionesOtrosCorredores) {

			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			tran.registroControlMercado.Procesado = "E";
			if (tran.tradeID != null) {
				tran.registroControlMercado.Log = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp + " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;
			} else {
				tran.registroControlMercado.Log = "Registro sin datos";
			}

			updateControlMercado.add(tran.registroControlMercado);
		}

		// ACTUALIZAR CONTROL MERCADO OTROS CORREDORES
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
			logger.debug("{} transacciones otros corredores actualizadas.", transaccionesOtrosCorredores.size());
		}
	}

	public void limpiezaTransaccionesNoEncontradas(Connection conn) throws SQLException {
		SQL _sql = new SQL();
		List<TransaccionRV_Proceso> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String, Object>(), TransaccionRV_Proceso.class);
		
		if (transacciones.isEmpty()) {
			return;
		}
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();

		for (TransaccionRV_Proceso tran : transacciones) {

			if (tran.codTicker.equalsIgnoreCase("")) {
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
				tran.registroControlMercado.Procesado = "E";
				tran.registroControlMercado.Log = "ERROR, NO SE ENCONTRO TRANSACCION " + tran._IdMensaje1 + " - " + tran._IdMensaje2;
				updateControlMercado.add(tran.registroControlMercado);
			}
		}

		if (!updateControlMercado.isEmpty()) {
			logger.debug("{} registros invalidos.", updateControlMercado.size());
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
		}
	}

	public void crearTransacciones(Connection conn, List<TransaccionRV_Proceso> transacciones, List<TransaccionParaAnular> transaccionesAnuladas, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRV.crearTransacciones");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");

		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		
		CrearTransaccionesMarketData_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketData_Entrada();
		entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
		
		List<String> foliosRevision = transacciones.stream().map(t -> t.tradeID).distinct().collect(Collectors.toList());
		HashMap<String, Object> paramsMod = new HashMap<String, Object>(1);
		paramsMod.put("Folio", foliosRevision);
		
		List<TransaccionAnulacionModificacion> listaAnulacionModificacion = _sql.query(conn, SQL_SELECT_ANULACION_MODIFICACION, paramsMod, TransaccionAnulacionModificacion.class);
		
		for (TransaccionRV_Proceso tran : transacciones) {

			TransaccionRVMarketData_Entrada tx = new TransaccionRVMarketData_Entrada();
			Utils.copyPojo(tran, tx);

			if (localizacionSistema.equalsIgnoreCase("PE")) {
				tx.IdInterno = tx.IdInterno;
				tx.buyerBroker = "1";
				tx.sellerBroker = "1";
				tx.buyEnteringFirm = "1";
				tx.sellEnteringFirm = "1";
				tx.buyEnteringTrader = "000";
				tx.sellEnteringTrader = "000";
				tx.orderType = "";
				tx.shortSell = "NO";
				tx.settlementCondition = "";
				tx.priceBuyingPrice = BigDecimal.ZERO;
				tx.priceSellingPrice = BigDecimal.ZERO;
			}
			
			TransaccionAnulacionModificacion transaccionModificacion = listaAnulacionModificacion.stream().filter(t -> t.Folio.equals(tran.tradeID)).findFirst().orElse(null);
			
			if (transaccionModificacion != null) {
				tx.EsModificacionMarketData = "S";
				tx.EstadoModificacion = transaccionModificacion.IdEstadoRegistro;
				tx.IdTransaccionHistoricoAnulacion = transaccionModificacion.IdTransaccionHistorico;
			}
			
			entradaCrearTransaccionesMarketData.transaccionesRV.add(tx);
		}

		if (entradaCrearTransaccionesMarketData.transaccionesRV.isEmpty()) {
			logger.info("Operaciones de creacion omitidas. Las transacciones no cumplen las reglas de negocio.");
		} else {
			CrearTransaccionesMarketData serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketData();
			CrearTransaccionesMarketData_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);

			List<Long> idsTransaccionesNuevas = new ArrayList<>();
			HashMap<String, Long> hashResultadoTransaccion = new HashMap<>();

			for (Resultado_Salida r : salidaCrearTransaccionesMarketData.P_OUT_RESULTADO) {
				String key = r.IdInterno + "_" + r.IdTipoOperacion;
				hashResultadoTransaccion.put(key, r.IdTransaccion);
				idsTransaccionesNuevas.add(r.IdTransaccion);
			}
			
			String fechaSistemaCreacion = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			HashMap<String, Object> paramsOpe = new HashMap<String, Object>(2);
			paramsOpe.put("IdTransaccion", idsTransaccionesNuevas);
			paramsOpe.put("FechaSistemaCreacion", fechaSistemaCreacion);
			
			List<TransaccionOperador> listaTransaccionesOperador = _sql.query(conn, SQL_SELECT_TRANSACCION_OPERADOR, paramsOpe, TransaccionOperador.class);
			HashMap<Long, Object> hashListaTransaccionesOperador = Utils.List2Hash(listaTransaccionesOperador, "IdTransaccion");
			
			List<String> llavesRestauracion = new ArrayList<>();

			for (TransaccionRV_Proceso tx : transacciones) {
				String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
				llavesRestauracion.add(tx.tradeID + fechaSistema);
			}

			List<OrdenPostrera_Entrada> ordenesPorCrear = new ArrayList<>();
			List<Long> asignacionesPorFacturar = new ArrayList<>();
			HashMap<String, Object> hashAsignacionesCreadasAnteriormente = new HashMap<>();

			if (!llavesRestauracion.isEmpty()) {
				HashMap<String, Object> params = new HashMap<>(1);
				params.put("Llave", llavesRestauracion);
				
				List<DatosRestauracion> asignacionesCreadasAnteriormente = _sql.query(conn, SQL_SELECT_ORDEN_ANTERIOR, params, DatosRestauracion.class);
				
				if(!asignacionesCreadasAnteriormente.isEmpty()) {
					hashAsignacionesCreadasAnteriormente = Utils.List2Hash(asignacionesCreadasAnteriormente, "KeyTransaccion");
				}
			}

			long idInternoContador = 1;
			HashMap<Long, Object> hashTransaccionesAnuladas = Utils.List2Hash(transaccionesAnuladas, "IdTransaccion");

			for (TransaccionRV_Proceso tx : transacciones) {
				try {
					List<DatosRestauracion> asignacionesAnteriores = obtenerAsignacionesAnteriores(tx, hashAsignacionesCreadasAnteriormente);

					if (asignacionesAnteriores.isEmpty()) {
						continue;
					}

					for (DatosRestauracion asignacionAnterior : asignacionesAnteriores) {
						String key = tx.IdInterno + "_" + asignacionAnterior.IdTipoOperacion;
						Long idNuevaTransaccion = hashResultadoTransaccion.get(key);
						
						if (idNuevaTransaccion == null) {
							logger.warn("No se encontro transaccion nueva para IdInterno={}, TipoOperacion={}", tx.IdInterno, asignacionAnterior.IdTipoOperacion);
							continue;
						}
						
						TransaccionParaAnular transaccionAnulada = (TransaccionParaAnular) hashTransaccionesAnuladas.get(asignacionAnterior.IdTransaccion);
						
						if (transaccionAnulada == null || !transaccionAnulada.TieneAsignacionAsociada) {
			                continue;
			            }
						
						if (hashListaTransaccionesOperador.containsKey(idNuevaTransaccion)) {
							continue;
						}
						
						OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();

						orden.IdInterno = idInternoContador++;
						orden.IdTransaccion = idNuevaTransaccion;
						orden.IdPortafolio = asignacionAnterior.IdPortafolio;
						orden.IdBoveda = asignacionAnterior.IdBoveda;
						orden.IdFormaCustodia = asignacionAnterior.IdFormaCustodia;
						orden.IdCuentaCustodioPortafolio = asignacionAnterior.IdCuentaCustodioPortafolio;
						orden.CodigoIFRS = asignacionAnterior.CodigoIFRS;
						orden.PagoCobroCuentaInversion = asignacionAnterior.PagoCobroCuentaInversion;
						orden.Glosa = asignacionAnterior.Glosa;
						orden.IdMonedaLiquidacionOrden = asignacionAnterior.IdMonedaLiquidacionOrden;
						orden.FechaRecepcion = asignacionAnterior.FechaRecepcion;
						orden.PlazoVentaCorta = (asignacionAnterior.PlazoVentaCorta != null) ? asignacionAnterior.PlazoVentaCorta : 0L;
						orden.TasaVentaCorta = (asignacionAnterior.TasaVentaCorta != null) ? asignacionAnterior.TasaVentaCorta : BigDecimal.ZERO;
						orden.Cantidad = tx.quantity;
						orden.ValorRescate = asignacionAnterior.ValorRescate;
						orden.EsFacturaInmediata = asignacionAnterior.EsFacturaInmediata;
						orden.FlujoOrigen = FLUJO_MODIFICACION_AUTO;
						orden.FolioCartera = asignacionAnterior.FolioCartera;
							
						ordenesPorCrear.add(orden);

						if (transaccionAnulada.TieneMovimientoAsociado) {
							asignacionesPorFacturar.add(orden.IdInterno);
						}
					}			
				} catch (Exception e) {
					logger.error("Error restaurando asignacion para Folio {}", tx.tradeID, e);
				}
			}

			if (!ordenesPorCrear.isEmpty()) {
				CrearOrdenesPostreras.CrearOrdenesPostreras_Salida salidaPostrera = crearOrdenPostrera(conn, ordenesPorCrear, idEmpresa, idUsuario);

				if (salidaPostrera.P_OUT_ERRORESCONTADOR > 0) {
					logger.error("No se pudo crear la orden postrera");
				} else {
					List<Asignacion_Entrada> listaParaFacturar = salidaPostrera.P_OUT_RESULTADO.stream()
							.filter(r -> asignacionesPorFacturar.contains(r.IdInterno)).map(a -> {
								Asignacion_Entrada asig = new Asignacion_Entrada();
								asig.IdAsignacion = a.IdAsignacion;
								return asig;
							}).collect(Collectors.toList());

					if (!listaParaFacturar.isEmpty()) {
						enviarAsignacionFacturar(conn, listaParaFacturar, idEmpresa, idUsuario);
					}
				}
			}
			
			Map<Long, List<Resultado_Salida>> hashSalidaResultado = new HashMap<>();

			for (Resultado_Salida res : salidaCrearTransaccionesMarketData.P_OUT_RESULTADO) {
				hashSalidaResultado.computeIfAbsent(res.IdInterno, k -> new ArrayList<>()).add(res);
			}

			Map<Long, List<MensajeSalida>> hashSalidaMensajes = new HashMap<>();

			for (MensajeSalida msg : salidaCrearTransaccionesMarketData.P_OUT_MENSAJES) {
				hashSalidaMensajes.computeIfAbsent(msg.id, k -> new ArrayList<>()).add(msg);
			}			

			List<ControlMercado> updateControlMercado = new ArrayList<>();
			
			for (TransaccionRV_Proceso tran : transacciones) {
				List<Resultado_Salida> listaResultados = hashSalidaResultado.getOrDefault(tran.IdInterno, new ArrayList<>());
				List<MensajeSalida> listaMensajes = hashSalidaMensajes.getOrDefault(tran.IdInterno, new ArrayList<>());
				
				if ("N".equals(tran.EsModificacionMarketData)) {
					if (!listaResultados.isEmpty()) {
						tran.registroControlMercado.Procesado = "S";
						logger.debug("Registro {} - {} procesado OK", tran.registroControlMercado.IdMensaje1, tran.registroControlMercado.IdMensaje2);
					}

					if (!tran.registroControlMercado.Procesado.equalsIgnoreCase("S")) {
						tran.registroControlMercado.Procesado = "E";
						logger.debug("Registro {} - {} procesado ERROR", tran.registroControlMercado.IdMensaje1, tran.registroControlMercado.IdMensaje2);
					}
				} else {
					boolean hayError = listaMensajes.stream().anyMatch(m -> MensajeSalida.MENSAJE_ERROR.equals(m.tipo));
					tran.registroControlMercado.Procesado = hayError ? "E" : "S";
					//marcar como procesada la anulacion asociada
					for(TransaccionParaAnular anu : transaccionesAnuladas) {
						if(anu.Folio.equals(tran.tradeID)) {
							anu.referencia.registroControlMercado.Procesado =  hayError ? "E" : "S";
							updateControlMercado.add(anu.referencia.registroControlMercado);
						}
					}
				}
				
				String logMensajes = listaMensajes.stream().map(m -> m.descripcion).collect(Collectors.joining(" "));
				tran.registroControlMercado.Log += logMensajes + " ";
				updateControlMercado.add(tran.registroControlMercado);
			}

			// ACTUALIZAR CONTROL MERCADO
			if (updateControlMercado.size() > 0) {
				_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
				_sql.batch(conn, SQL_INSERT_HIS_TRANSACCIONES, transacciones, TransaccionRV_Proceso.class);
				conn.commit();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRV.crearTransacciones tiempo: " + (fin - inicio));
	}

	private List<DatosRestauracion> obtenerAsignacionesAnteriores(TransaccionRV_Proceso tx, HashMap<String, Object> hashAsignacionesCreadasAnteriormente)  throws Exception {
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
		
		List<DatosRestauracion> datosRestauracion = new ArrayList<>();
		String keyCompra = tx.tradeID + fechaSistema + COMPRA;
		String keyVenta = tx.tradeID + fechaSistema + VENTA;

		DatosRestauracion asignacionCompra = (DatosRestauracion) hashAsignacionesCreadasAnteriormente.get(keyCompra);

		if (asignacionCompra != null)
			datosRestauracion.add(asignacionCompra);

		DatosRestauracion asignacionVenta = (DatosRestauracion) hashAsignacionesCreadasAnteriormente.get(keyVenta);

		if (asignacionVenta != null)
			datosRestauracion.add(asignacionVenta);

		return datosRestauracion;
	}

	private CrearOrdenesPostreras.CrearOrdenesPostreras_Salida crearOrdenPostrera(Connection conn, List<OrdenPostrera_Entrada> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
		entradaCrearOrdenesPostreras.p_IdEmpresa = idEmpresa;
		entradaCrearOrdenesPostreras.p_IdModulo = "ORD";
		entradaCrearOrdenesPostreras.p_IdUsuario = idUsuario;
		entradaCrearOrdenesPostreras.p_Ordenes = asignaciones;
		CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
		CrearOrdenesPostreras_Salida salida = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
		return salida;
	}

	private void enviarAsignacionFacturar(Connection conn, List<Asignacion_Entrada> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
		EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();

		entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
		entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
		entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario;
		entradaEnviarAsignacionesFacturar.p_Asignaciones = asignaciones;

		serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);
	}

	private void anularAsignaciones(Connection conn, List<Asignaciones> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		AnularAsignaciones.AnularAsignaciones_Entrada entrada = new AnularAsignaciones.AnularAsignaciones_Entrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "ORD";

		entrada.asignacionesParaAnular = asignaciones.stream().map(a -> {
			AnularAsignaciones.Asignacion_Entrada asig = new AnularAsignaciones.Asignacion_Entrada();
			asig.IdAsignacion = a.IdAsignacion;
			return asig;
		}).collect(Collectors.toList());

		new AnularAsignaciones().logica(conn, entrada);
	}

	private void anularMovimientosFacturados(Connection conn, List<MovimientoFacturar> movimientosFacturados, Long idEmpresa, Long idUsuario) throws Exception {
		AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada entrada = new AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada();

		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "ORD";
		entrada.p_UsaReglasNegocio = "S";
		entrada.p_simularOperacion = "N";
		entrada.p_EsIngresoAutomatico = "N";
		entrada.p_Glosa = "";

		entrada.p_MovimientoAAnular = movimientosFacturados.stream().map(mov -> {
			AnularMovimientoComprobante.Movimiento_Entrada m = new AnularMovimientoComprobante.Movimiento_Entrada();
			m.IdMovimientoComprobante = mov.IdMovimientoComprobante;
			return m;
		}).collect(Collectors.toList());

		new AnularMovimientoComprobante().logica(conn, entrada);
	}

	public List<TransaccionParaAnular> anularTransacciones(Connection conn, List<TransaccionRV_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRV.anularTransacciones");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		
		List<String> llavesAnulacion = new ArrayList<>();
		
		for (TransaccionRV_Proceso tx : transacciones) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
			llavesAnulacion.add(tx.tradeID + fechaSistema);
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesProcesar = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesPorAnular = new ArrayList<>();
		boolean found = false;
		List<Long> idsTransacciones = new ArrayList<>();

		for (TransaccionRV_Proceso tx : transacciones) {
			found = false;
			for (TransaccionParaAnular txa : transaccionesProcesar) {
				String fechaTransaccion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
				String fechaBd = Fecha.toStringDate(txa.FechaSistemaCreacion);
				
				if (tx.tradeID.equalsIgnoreCase(txa.Folio) && fechaTransaccion.equalsIgnoreCase(fechaBd)) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = MOTIVO_ANULACION;
					e.Observaciones = "";
					e.EsModificacionMarketData = tx.EsModificacionMarketData;
					transaccionesPorAnular.add(e);
					txa.EsModificacionMarketData = tx.EsModificacionMarketData;
					txa.referencia = tx;
					tx.IdTransaccionActual = txa.IdTransaccion;
					found = true;
					idsTransacciones.add(txa.IdTransaccion);
				}
			}

			if (!found) {
				tx.registroControlMercado.Procesado = "E";
				tx.registroControlMercado.Log = "Transaccion a eliminar, no encontrada, tradeID:" + tx.tradeID + " tradeTime:" + tx.tradeTime;
			}
		}

		if (!idsTransacciones.isEmpty()) {
		
			HashMap<String, Object> paramsTransaccion = new HashMap<>();
			paramsTransaccion.put("IdTransaccion", idsTransacciones);

			List<MovimientoFacturar> movimientosPorAnular = _sql.query(conn, SQL_SELECT_MOVIMIENTO_FACTURAR, paramsTransaccion, MovimientoFacturar.class);
			List<Asignaciones> asignacionesPorAnular = _sql.query(conn, SQL_SELECT_ASIGNACIONES, paramsTransaccion, Asignaciones.class);

			Map<Long, Object> hashAsignaciones = Utils.List2Hash(asignacionesPorAnular, "IdTransaccion");
			Map<Long, Object> hashMovimientos = Utils.List2Hash(movimientosPorAnular, "IdTransaccion");

			for (Long idTransaccionActual : idsTransacciones) {
				Asignaciones asignacion = (Asignaciones) hashAsignaciones.get(idTransaccionActual);

				if(asignacion != null) {
					transaccionesProcesar.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneAsignacionAsociada = true);
				}

				MovimientoFacturar movimiento = (MovimientoFacturar) hashMovimientos.get(idTransaccionActual);

				if(movimiento != null) {
					transaccionesProcesar.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneMovimientoAsociado = true);
				}
			}

			if (!movimientosPorAnular.isEmpty()) {
				anularMovimientosFacturados(conn, movimientosPorAnular, idEmpresa, idUsuario);
			}

			if (!asignacionesPorAnular.isEmpty()) {
				anularAsignaciones(conn, asignacionesPorAnular, idEmpresa, idUsuario);
			}

			AnularTransaccionMasiva anularTransaccionService = new AnularTransaccionMasiva();
			EntradaAnularTransaccionMasiva entradaAnularTransaccion = new EntradaAnularTransaccionMasiva();

			entradaAnularTransaccion.p_IdEmpresa = idEmpresa;
			entradaAnularTransaccion.p_IdUsuario = idUsuario;
			entradaAnularTransaccion.p_IdModulo = "ORD";
			entradaAnularTransaccion.p_Reproceso = "N";
			entradaAnularTransaccion.p_transacciones = transaccionesPorAnular;

			SalidaAnularTransaccionMasiva salidaAnularTransacciones = anularTransaccionService.logica(conn, entradaAnularTransaccion);
			HashMap<Long, Object> hashSalidaMensajes = Utils.List2Hash(salidaAnularTransacciones.P_OUT_MENSAJES, "id");
			
			for (TransaccionParaAnular txa : transaccionesProcesar) {
				MensajeSalida msj = (MensajeSalida) hashSalidaMensajes.get(txa.IdTransaccion);
				
				if (msj == null) {
					msj = new MensajeSalida(txa.IdTransaccion, "", "", "");
				}
			
				if (MensajeSalida.MENSAJE_OK.equals(msj.tipo)) {
					txa.referencia.registroControlMercado.Procesado = "S";
				} else if ("S".equals(txa.EsModificacionMarketData)) {
					txa.referencia.registroControlMercado.Procesado = "N";
				} else {
					txa.referencia.registroControlMercado.Procesado = "E";
				}
				
				txa.referencia.registroControlMercado.Log += msj.descripcion + " : " + msj.id + " ";
			}
		}

		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
		
		for (TransaccionRV_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		

		// ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			_sql.batch(conn, SQL_INSERT_HIS_TRANSACCIONES, transacciones, TransaccionRV_Proceso.class);
			conn.commit();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRV.anularTransacciones tiempo: " + (fin - inicio));
		return transaccionesProcesar;
	}

	

	private HashMap<String,Corredora> buscarCorredoras (Connection con) throws SQLException{
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.buscarCorredoras");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params1 = new HashMap<>();
		List<Corredora> _corredoras = sql.query(con, SQL_SELECT_CORREDORAS, params1, Corredora.class);
		HashMap<String,Corredora> corredoras = new HashMap<>();
		for (Corredora corr : _corredoras) {
			corredoras.put(corr.CodigoCorredoraFix, corr);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.buscarCorredoras tiempo: {}", fin - inicio);

		return corredoras;
	}

	private TransaccionRVMarketData buscarTransaccion(Connection con, String tradeTime, String folio) throws SQLException {
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.buscarTransaccion");
		long inicio = System.currentTimeMillis();

		TransaccionRVMarketData transaccion = null;
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("TradeTime", tradeTime);
		params.put("Folio", folio);
		List<TransaccionRVMarketData> listaTransacciones = sql.query(con, SQL_SELECT_HIS_PALOS_RV, params,
				TransaccionRVMarketData.class);
		if (!listaTransacciones.isEmpty()) {
			transaccion = listaTransacciones.get(0);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.buscarTransaccion tiempo: {}", fin - inicio);
		return transaccion;
	}

	private boolean ignorarModificacion(TransaccionRVMarketData transaccionAnterior, TransaccionRV_Proceso transaccionActual) {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRV.ignorarModificacion");
		long inicio = System.currentTimeMillis();

		boolean soloVentaCorta = false;

		// Si todos los campos a excepcion de shortSell y timeStamp son iguales debe
		// ignorar la modificacion
		if (transaccionAnterior.codTicker.equalsIgnoreCase(transaccionActual.codTicker)
				&& transaccionAnterior.action.equalsIgnoreCase(transaccionActual.action)
				&& transaccionAnterior.tradeTime.equalsIgnoreCase(transaccionActual.tradeTime)
				&& transaccionAnterior.instrument.equalsIgnoreCase(transaccionActual.instrument)
				&& transaccionAnterior.market.equalsIgnoreCase(transaccionActual.market)
				&& transaccionAnterior.settlementCondition.equalsIgnoreCase(transaccionActual.settlementCondition)
				&& transaccionAnterior.orderType.equalsIgnoreCase(transaccionActual.orderType)
				&& transaccionAnterior.indivisible.equalsIgnoreCase(transaccionActual.indivisible)
				&& transaccionAnterior.quantity.compareTo(transaccionActual.quantity) == 0
				&& transaccionAnterior.price.compareTo(transaccionActual.price) == 0
				&& transaccionAnterior.amount.compareTo(transaccionActual.amount) == 0
				&& transaccionAnterior.closingPrice.compareTo(transaccionActual.closingPrice) == 0
				&& transaccionAnterior.buyerBroker.equalsIgnoreCase(transaccionActual.buyerBroker)
				&& transaccionAnterior.buyEnteringFirm.equalsIgnoreCase(transaccionActual.buyEnteringFirm)
				&& transaccionAnterior.buyEnteringTrader.equalsIgnoreCase(transaccionActual.buyEnteringTrader)
				&& transaccionAnterior.sellerBroker.equalsIgnoreCase(transaccionActual.sellerBroker)
				&& transaccionAnterior.sellEnteringFirm.equalsIgnoreCase(transaccionActual.sellEnteringFirm)
				&& transaccionAnterior.sellEnteringTrader.equalsIgnoreCase(transaccionActual.sellEnteringTrader)
				&& transaccionAnterior.priceBuyingQTY.compareTo(transaccionActual.priceBuyingQTY) == 0
				&& transaccionAnterior.priceBuyingPrice.compareTo(transaccionActual.priceBuyingPrice) == 0
				&& transaccionAnterior.priceSellingQty.compareTo(transaccionActual.priceSellingQty) == 0
				&& transaccionAnterior.priceSellingPrice.compareTo(transaccionActual.priceSellingPrice) == 0
				&& transaccionAnterior.codIsin.equalsIgnoreCase(transaccionActual.codIsin)
				&& transaccionAnterior.securityType.equalsIgnoreCase(transaccionActual.securityType)
				&& transaccionAnterior.timeInForce.equalsIgnoreCase(transaccionActual.timeInForce)
				&& transaccionAnterior.tradingSessionID.equalsIgnoreCase(transaccionActual.tradingSessionID)
				&& transaccionAnterior.entryType.equalsIgnoreCase(transaccionActual.entryType)
				&& transaccionAnterior.priceExchange.equalsIgnoreCase(transaccionActual.priceExchange)
				&& transaccionAnterior.priceTime.equalsIgnoreCase(transaccionActual.priceTime)) {
			soloVentaCorta = true;
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRV.ignorarModificacion tiempo: {}", fin - inicio);
		return soloVentaCorta;
	}

	private void actualizarControlMercadoTransaccionIgnorada(Connection con,
			TransaccionRV_Proceso transaccionConModificacionPorIgnorar) throws SQLException {
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.actualizarControlMercadoTransaccionIgnorada");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		transaccionConModificacionPorIgnorar.registroControlMercado.Procesado = "E";
		transaccionConModificacionPorIgnorar.registroControlMercado.Log = "Modificacion Transaccion Folio " + transaccionConModificacionPorIgnorar.tradeID + " Ignorada";
		List<ControlMercado> listaUpdate = new ArrayList<>();
		listaUpdate.add(transaccionConModificacionPorIgnorar.registroControlMercado);
		ControlMercado transaccionAnulacionPorIgnorar = new ControlMercado();
		transaccionAnulacionPorIgnorar.IdMensaje1 = transaccionConModificacionPorIgnorar.registroControlMercado.IdMensaje1;
		transaccionAnulacionPorIgnorar.IdMensaje2 = transaccionConModificacionPorIgnorar.tradeID + "E";
		transaccionAnulacionPorIgnorar.Procesado = "E";
		transaccionAnulacionPorIgnorar.Log = transaccionConModificacionPorIgnorar.registroControlMercado.Log;
		listaUpdate.add(transaccionAnulacionPorIgnorar);
		sql.batch(con, SQL_ACTUALIZA_CONTROL_MERCADO, listaUpdate, ControlMercado.class);
		con.commit();

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.actualizarControlMercadoTransaccionIgnorada tiempo: {}", fin - inicio);
	}

	public static void main(String[] args) {
		System.out.println("== START ==");
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("master", "connections.properties");
			ControlMercadoMarketDataPalosRV demo = new ControlMercadoMarketDataPalosRV();
			
			try {
				SQL.exec(con, "UPDATE \"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\" = 'N' " + "WHERE \"IdMensaje1\" = '2026031916550038' AND \"IdMensaje2\" = '215003I';");

				while (true) {
					demo.logica(con, true);
					con.commit();
				}
			} catch (Throwable er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}