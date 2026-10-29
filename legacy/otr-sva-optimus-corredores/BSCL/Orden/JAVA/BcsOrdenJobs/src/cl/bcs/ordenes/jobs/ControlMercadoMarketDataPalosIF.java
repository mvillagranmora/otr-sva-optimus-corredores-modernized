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
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionIFMarketData_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionIFMarketData;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosIF extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoMarketDataPalosIF.class);
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosIF_SelectTransacciones.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_SELECT_DATOS_RESTAURACION = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosIF_SelectBackupAsignaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosIF_SelectTransaccionesAnular.sql");
	private final String SQL_SELECT_MOVIMIENTO_FACTURAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosIF_SelectMovimientoFacturar.sql");
	private final String SQL_SELECT_ASIGNACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosIF_SelectAsignaciones.sql");
	private final String SQL_SELECT_ANULACION_MODIFICACION = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionAnulacionModificacion.sql");
	private final String SQL_SELECT_TRANSACCION_OPERADOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionOperador.sql");
	
	private static final String FLUJO_MODIFICACION_AUTO = "MODIFICACION_AUTO";
	private static final String COMPRA = "COM";
	private static final String VENTA = "VEN";
	
	public static class TransaccionIF_Proceso extends TransaccionIFMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public Long IdTransaccionActual;
		public ControlMercado registroControlMercado = new ControlMercado();
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
		public TransaccionIF_Proceso referencia;
		public Boolean TieneAsignacionAsociada = false;
		public Boolean TieneMovimientoAsociado = false;
		public String EsModificacionMarketData = "N";
	}
	
	public static class TransaccionAnulacionModificacion extends BcsPojo {
		public Long IdTransaccionHistorico;
		public String IdEstadoRegistro;
		public String Folio;
	}

	public static class Corredora extends BcsPojo {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
	}

	public static class Asignaciones extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;
	}

	public static class MovimientoFacturar extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public String IdEstadoFacturacion;
		
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
	    public BigDecimal TasaVentaCorta;
	    public BigDecimal TipoCambio;
	    public BigDecimal ValorRescate;
	    public String IdEstadoFacturacion;
	    public String IdTipoOperacion;
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	@Override
	public void onError(Throwable t) {}

	@Override
	public void pause() {}
	
	public boolean logica(Connection conn, boolean debug) throws Exception {
		try {
			logger.info("Inicio Ciclo MarketData IF");
			
			Long idEmpresa = 0L;
			if (debug) {
				idEmpresa = 1L;
			} else {
				 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			}
			
			SQL _sql = new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);		
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			
			HashMap<String,Object> paramsTransacciones = new HashMap<String,Object>();
			List<TransaccionIF_Proceso> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, paramsTransacciones, TransaccionIF_Proceso.class);
			
			logger.debug(transacciones.size() + " nuevos mensajes.");
			if (transacciones.size() == 0){
				return true;
			}
			
			for (TransaccionIF_Proceso tran : transacciones) {
				tran.registroControlMercado.Log = "";
				tran.registroControlMercado.Procesado = "N";
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
				
				if ("M".equals(tran.ACTION)) {
					tran.EsModificacionMarketData = "S";
				}
				
				if (tran.CODTICKER.equalsIgnoreCase("")) {
					tran.registroControlMercado.Procesado = "E";
					tran.registroControlMercado.Log = "ERROR, NO SE ENCONTRO TRANSACCION " + tran._IdMensaje1 + " - " + tran._IdMensaje2;
				}
			}
			
			String action = null;
			List<TransaccionIF_Proceso> transaccionesProcesar = new ArrayList<>();
			
			for (TransaccionIF_Proceso tx : transacciones) {
				if (action == null) {
					action = tx.ACTION;
				}
				
				if (action != null && !action.equalsIgnoreCase(tx.ACTION)) {
					break;					
				}
				
				transaccionesProcesar.add(tx);
			}
			
			if ("I".equalsIgnoreCase(action) || "S".equalsIgnoreCase(action)) {
				crearTransacciones(conn, transaccionesProcesar,  new HashMap<>(0), idEmpresa, idUsuario, action);
			} else if ("E".equalsIgnoreCase(action)) {
				anularTransacciones(conn, transaccionesProcesar, idEmpresa, idUsuario);
			} else if ("M".equalsIgnoreCase(action)) {
				modificarTransacciones(conn, transaccionesProcesar, idEmpresa, idUsuario, action);
			}

			List<ControlMercado> updateControlMercado = new ArrayList<>();
			for (TransaccionIF_Proceso tran : transacciones) {
				updateControlMercado.add(tran.registroControlMercado);
			}
			
			actualizarControlMercado(conn, updateControlMercado);
			
			logger.info("Fin Ciclo MarketData IF");
		}
		catch (Exception e) {
			logger.error("ERROR CRITICO EN MARKET DATA IF", e);
			throw e;
		}
		
		return true;
	}
	
	private void modificarTransacciones(Connection conn, List<TransaccionIF_Proceso> transacciones, Long idEmpresa, Long idUsuario, String action) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRF.modificarTransacciones");
		long inicio = System.currentTimeMillis();
		
		List<TransaccionParaAnular> transaccionesAnuladas = anularTransacciones(conn, transacciones, idEmpresa, idUsuario);
		HashMap<Long, Object> hashTransaccionesAnuladas = Utils.List2Hash(transaccionesAnuladas, "IdTransaccion");
		crearTransacciones(conn, transacciones, hashTransaccionesAnuladas, idEmpresa, idUsuario, action);
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
		for (TransaccionIF_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
    	}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRF.modificarTransacciones tiempo: " + (fin - inicio));
	}
	
	private void actualizarControlMercado (Connection conn, List<ControlMercado> updateControlMercado) throws SQLException {
		logger.debug("INICIO::ControlMercadoMarketDataPalosIF.actualizarControlMercado");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		
		if (!updateControlMercado.isEmpty()) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
    		conn.commit();
        }
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRF.actualizarControlMercado tiempo: " + (fin - inicio));
	}

	public void crearTransacciones(Connection conn, List<TransaccionIF_Proceso> transacciones, HashMap<Long, Object> hashTransaccionesAnuladas, Long idEmpresa, Long idUsuario, String action) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.crearTransacciones");
		long inicio = System.currentTimeMillis();

		SQL _sql = new SQL();
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");

		CrearTransaccionesMarketData_Entrada entradaCrearTransaccionMarketData = new CrearTransaccionesMarketData_Entrada();
		entradaCrearTransaccionMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionMarketData.p_IdUsuario = idUsuario;

		List<TransaccionIF_Proceso> transaccionesProcesar = transacciones.stream().filter(t -> !"E".equalsIgnoreCase(t.registroControlMercado.Procesado)).collect(Collectors.toList());

		List<String> foliosRevision = transaccionesProcesar.stream().map(t -> t.TRADEID).distinct().collect(Collectors.toList());
		HashMap<String, Object> paramsMod = new HashMap<String, Object>(1);
		paramsMod.put("Folio", foliosRevision);
		
		List<TransaccionAnulacionModificacion> listaAnulacionModificacion = _sql.query(conn, SQL_SELECT_ANULACION_MODIFICACION, paramsMod, TransaccionAnulacionModificacion.class);
		
		for (TransaccionIF_Proceso tran : transaccionesProcesar) {
			TransaccionIFMarketData_Entrada tx = new TransaccionIFMarketData_Entrada();
			Utils.copyPojo(tran, tx);
			
			TransaccionAnulacionModificacion transaccionModificacion = listaAnulacionModificacion.stream().filter(t -> t.Folio.equals(tran.TRADEID)).findFirst().orElse(null);
			
			if (transaccionModificacion != null) {
				tx.EsModificacionMarketData = "S";
				tx.EstadoModificacion = transaccionModificacion.IdEstadoRegistro;
				tx.IdTransaccionHistoricoAnulacion = transaccionModificacion.IdTransaccionHistorico;
			}
			
			entradaCrearTransaccionMarketData.transaccionesIF.add(tx);
		}

		if (entradaCrearTransaccionMarketData.transaccionesIF.isEmpty()) {
			logger.info("Operaciones de creacion omitidas. Las transacciones no cumplen las reglas de negocio.");
		} else {
			CrearTransaccionesMarketData serviceCrearTransaccionMarketData = new CrearTransaccionesMarketData();
			CrearTransaccionesMarketData_Salida salidaCrearTransaccionMarketData = serviceCrearTransaccionMarketData.logica(conn, entradaCrearTransaccionMarketData);

			List<Long> idsTransaccionesNuevas = new ArrayList<>();
			HashMap<String, Long> hashResultadoTransaccion = new HashMap<>();

			for (Resultado_Salida r : salidaCrearTransaccionMarketData.P_OUT_RESULTADO) {
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

			for (TransaccionIF_Proceso tx : transacciones) {
				String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.TRADETIME)));
				llavesRestauracion.add(tx.TRADEID + fechaSistema);
			}

			List<OrdenPostrera_Entrada> ordenesPorCrear = new ArrayList<>();
			List<Long> asignacionesPorFacturar = new ArrayList<>();
			HashMap<String, Object> hashAsignacionesCreadasAnteriormente = new HashMap<String, Object>();

			if (!llavesRestauracion.isEmpty()) {
				HashMap<String, Object> params = new HashMap<>(1);
				params.put("Llave", llavesRestauracion);

				List<DatosRestauracion> asignacionesCreadasAnteriormente = _sql.query(conn, SQL_SELECT_DATOS_RESTAURACION, params, DatosRestauracion.class);

				if (!asignacionesCreadasAnteriormente.isEmpty()) {
					hashAsignacionesCreadasAnteriormente = Utils.List2Hash(asignacionesCreadasAnteriormente, "KeyTransaccion");
				}
			}

			long idInternoContador = 1;

			for (TransaccionIF_Proceso tx : transaccionesProcesar) {
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
						
						if (hashListaTransaccionesOperador.containsKey(idNuevaTransaccion)) {
							continue;
						}
						
						TransaccionParaAnular transaccionAnulada = (TransaccionParaAnular) hashTransaccionesAnuladas.get(asignacionAnterior.IdTransaccion);
						
						if (transaccionAnulada == null || !transaccionAnulada.TieneAsignacionAsociada) {
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
						if (tx.AMOUNT != null)
							orden.Cantidad = tx.AMOUNT;
						if (tx.RESCUEVALUE != null)
							orden.ValorRescate = tx.RESCUEVALUE;
						orden.EsFacturaInmediata = asignacionAnterior.EsFacturaInmediata;
						orden.FlujoOrigen = FLUJO_MODIFICACION_AUTO;
						orden.FolioCartera = asignacionAnterior.FolioCartera;

						ordenesPorCrear.add(orden);
						
						if (transaccionAnulada.TieneMovimientoAsociado) {
							asignacionesPorFacturar.add(orden.IdInterno);
						}
					}
				} catch (Exception e) {
					logger.error("Error restaurando asignacion para Folio " + tx.TRADEID, e);
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

			for (Resultado_Salida res : salidaCrearTransaccionMarketData.P_OUT_RESULTADO) {
				hashSalidaResultado.computeIfAbsent(res.IdInterno, k -> new ArrayList<>()).add(res);
			}

			Map<Long, List<MensajeSalida>> hashSalidaMensajes = new HashMap<>();

			for (MensajeSalida msg : salidaCrearTransaccionMarketData.P_OUT_MENSAJES) {
				hashSalidaMensajes.computeIfAbsent(msg.id, k -> new ArrayList<>()).add(msg);
			}			

			for (TransaccionIF_Proceso tran : transaccionesProcesar) {
				List<Resultado_Salida> listaResultados = hashSalidaResultado.getOrDefault(tran.IdInterno, new ArrayList<>());
				List<MensajeSalida> listaMensajes = hashSalidaMensajes.getOrDefault(tran.IdInterno, new ArrayList<>());

				if (!"M".equalsIgnoreCase(action)) {
					if (!listaResultados.isEmpty()) {
						tran.registroControlMercado.Procesado = "S";
					} else if (!"E".equals(tran.registroControlMercado.Procesado)) {
						tran.registroControlMercado.Procesado = "E";
					}
				} else {
					boolean hayError = listaMensajes.stream().anyMatch(m -> MensajeSalida.MENSAJE_ERROR.equals(m.tipo));
					tran.registroControlMercado.Procesado = hayError ? "E" : "S";
				}

				String logMensajes = listaMensajes.stream().map(m -> m.descripcion).collect(Collectors.joining(" "));
				tran.registroControlMercado.Log += logMensajes + " ";
			}
		}
			
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.crearTransacciones tiempo: " + (fin - inicio));
	}
	
	private List<DatosRestauracion> obtenerAsignacionesAnteriores(TransaccionIF_Proceso tx, HashMap<String, Object> hashAsignacionesCreadasAnteriormente) throws Exception {
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.TRADETIME)));
		
		List<DatosRestauracion> datosRestauracion = new ArrayList<>();
		String keyCompra = tx.TRADEID + fechaSistema + COMPRA;
		String keyVenta = tx.TRADEID + fechaSistema + VENTA;

		DatosRestauracion asignacionCompra = (DatosRestauracion) hashAsignacionesCreadasAnteriormente.get(keyCompra);

		if (asignacionCompra != null)
			datosRestauracion.add(asignacionCompra);

		DatosRestauracion asignacionVenta = (DatosRestauracion) hashAsignacionesCreadasAnteriormente.get(keyVenta);

		if (asignacionVenta != null)
			datosRestauracion.add(asignacionVenta);

		return datosRestauracion;
	}

	public List<TransaccionParaAnular> anularTransacciones(Connection conn, List<TransaccionIF_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.anularTransacciones");
		long inicio = System.currentTimeMillis();

		SQL _sql = new SQL();
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");

		List<String> llavesAnulacion = new ArrayList<>();

		for (TransaccionIF_Proceso tx : transacciones) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.TRADETIME)));
			llavesAnulacion.add(tx.TRADEID + fechaSistema);
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesProcesar = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesPorAnular = new ArrayList<>();
		boolean found = false;
		List<Long> idsTransacciones = new ArrayList<>();

		for (TransaccionIF_Proceso tx : transacciones) {
			found = false;
			for (TransaccionParaAnular txa : transaccionesProcesar) {
				String fechaTransaccion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.TRADETIME)));
				String fechaBd = Fecha.toStringDate(txa.FechaSistemaCreacion);

				if (tx.TRADEID.equalsIgnoreCase(txa.Folio) && fechaTransaccion.equalsIgnoreCase(fechaBd)) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format(txa.FechaSistemaCreacion);
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = "ANULACION POR SISTEMA BURSATIL";
					e.Observaciones = "";
					e.EsModificacionMarketData = tx.EsModificacionMarketData;
					transaccionesPorAnular.add(e);
					txa.referencia = tx;
					txa.EsModificacionMarketData = tx.EsModificacionMarketData;
					tx.IdTransaccionActual = txa.IdTransaccion;
					found = true;
					idsTransacciones.add(txa.IdTransaccion);
				}
			}

			if (!found) {
				tx.registroControlMercado.Procesado = "E";
				tx.registroControlMercado.Log = "Transaccion a eliminar, no encontrada, tradeID:" + tx.TRADEID + " tradeTime:" + tx.TRADETIME;
			}
		}

		if (!idsTransacciones.isEmpty()) {
			HashMap<String, Object> paramsTransaccion = new HashMap<>(1);
			paramsTransaccion.put("IdTransaccion", idsTransacciones);

			List<MovimientoFacturar> movimientosPorAnular = _sql.query(conn, SQL_SELECT_MOVIMIENTO_FACTURAR, paramsTransaccion, MovimientoFacturar.class);
			List<Asignaciones> asignacionesPorAnular = _sql.query(conn, SQL_SELECT_ASIGNACIONES, paramsTransaccion, Asignaciones.class);

			Map<Long, Object> hashAsignaciones = Utils.List2Hash(asignacionesPorAnular, "IdTransaccion");
			Map<Long, Object> hashMovimientos = Utils.List2Hash(movimientosPorAnular, "IdTransaccion");

			for (Long idTransaccionActual : idsTransacciones) {
				Asignaciones asignacion = (Asignaciones) hashAsignaciones.get(idTransaccionActual);

				if (asignacion != null) {
					transaccionesProcesar.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneAsignacionAsociada = true);
				}
				
				MovimientoFacturar movimientoTransaccion = (MovimientoFacturar) hashMovimientos.get(idTransaccionActual);

				if (movimientoTransaccion != null) {
					transaccionesProcesar.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneMovimientoAsociado = true);
				}
			}

			if (!movimientosPorAnular.isEmpty()) {
				anularMovimientosFacturados(conn, movimientosPorAnular, idEmpresa, idUsuario);
			}

			if (!asignacionesPorAnular.isEmpty()) {
				anularAsignaciones(conn, asignacionesPorAnular, idEmpresa, idUsuario);
			}

			AnularTransaccionMasiva servicioAnularTransaccion = new AnularTransaccionMasiva();
			EntradaAnularTransaccionMasiva entradaAnularTransaccion = new EntradaAnularTransaccionMasiva();

			entradaAnularTransaccion.p_IdEmpresa = idEmpresa;
			entradaAnularTransaccion.p_IdUsuario = idUsuario;
			entradaAnularTransaccion.p_IdModulo = "ORD";
			entradaAnularTransaccion.p_Reproceso = "N";
			entradaAnularTransaccion.p_transacciones = transaccionesPorAnular;

			SalidaAnularTransaccionMasiva salidaAnularTransaccion = servicioAnularTransaccion.logica(conn, entradaAnularTransaccion);
			HashMap<Long, Object> hashSalidaMensajes = Utils.List2Hash(salidaAnularTransaccion.P_OUT_MENSAJES, "id");
			
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

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.anularTransacciones tiempo: " + (fin - inicio));
		return transaccionesProcesar;
	}

	private CrearOrdenesPostreras.CrearOrdenesPostreras_Salida crearOrdenPostrera(Connection conn, List<OrdenPostrera_Entrada> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.crearOrdenPostrera");
		
		CrearOrdenesPostreras_Entrada entrada = new CrearOrdenesPostreras_Entrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdModulo = "ORD";
		entrada.p_IdUsuario = idUsuario;
		entrada.p_Ordenes = asignaciones;
		CrearOrdenesPostreras service = new CrearOrdenesPostreras();
		CrearOrdenesPostreras_Salida salida = service.logica(conn, entrada);
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.crearOrdenPostrera");
		return salida;		
	}
	
	private void enviarAsignacionFacturar(Connection conn, List<Asignacion_Entrada> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.enviarAsignacionFacturar");
		
		EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
		EnviarAsignacionesFacturar_Entrada entradaEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
		
		entradaEnviarAsignacionesFacturar.p_IdEmpresa = idEmpresa;
		entradaEnviarAsignacionesFacturar.p_IdModulo = "ORD";
		entradaEnviarAsignacionesFacturar.p_IdUsuario = idUsuario;
		entradaEnviarAsignacionesFacturar.p_Asignaciones = asignaciones;
		
		serviceEnviarAsignacionesFacturar.logica(conn, entradaEnviarAsignacionesFacturar);
		
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.enviarAsignacionFacturar");
	}
	
	private void anularAsignaciones(Connection conn, List<Asignaciones> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.anularAsignacion");
		
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
		
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.anularAsignacion");
	}
	
	private void anularMovimientosFacturados(Connection conn, List<MovimientoFacturar> movimientos, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosIF.anularMovimientosFacturados");
		
		AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada entrada = new AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "ORD";
		entrada.p_UsaReglasNegocio = "S";
		entrada.p_simularOperacion = "N";
		entrada.p_EsIngresoAutomatico = "N";
		entrada.p_Glosa = "";
		entrada.p_MovimientoAAnular = movimientos.stream().map(mov -> {
			AnularMovimientoComprobante.Movimiento_Entrada m = new AnularMovimientoComprobante.Movimiento_Entrada();
			m.IdMovimientoComprobante = mov.IdMovimientoComprobante;
			return m;
		}).collect(Collectors.toList());
		new AnularMovimientoComprobante().logica(conn, entrada);
		
		logger.trace("FIN::ControlMercadoMarketDataPalosIF.anularMovimientosFacturados");
	}
	
	public static void main(String[] args) {
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa2","connections.properties");
            try {
            	ControlMercadoMarketDataPalosIF demo=new ControlMercadoMarketDataPalosIF();
            	/*SQL.exec(con, "UPDATE BSCL.\"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\"='N'" +
            			"	WHERE \"TipoMensaje\"='MARKET_DATA_PALO_IF' AND \"IdMensaje1\"='2025090215130004' AND \"IdMensaje2\"='161107'");
            	SQL.exec(con, "UPDATE BSCL.\"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\"='N'" +
            			"	WHERE \"TipoMensaje\"='MARKET_DATA_PALO_IF' AND \"IdMensaje1\"='2025090215130004' AND \"IdMensaje2\"='161109'");
            	SQL.exec(con, "UPDATE BSCL.\"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\"='N'" +
            			"	WHERE \"TipoMensaje\"='MARKET_DATA_PALO_IF' AND \"IdMensaje1\"='2025090217100014' AND \"IdMensaje2\"='161451'");*/
            	demo.logica(con,true);
                con.rollback();
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
