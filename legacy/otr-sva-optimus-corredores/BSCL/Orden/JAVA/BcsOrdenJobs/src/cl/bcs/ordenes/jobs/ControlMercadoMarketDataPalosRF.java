package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.AnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearOrdenesPostreras;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRFMarketData_Entrada;
import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.facturacion.AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada;
import cl.bcs.facturacion.AnularMovimientoComprobante.Movimiento_Entrada;
import cl.bcs.ordenes.AnularAsignaciones;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionRFMarketData;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosRF extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoMarketDataPalosRF.class);
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRF_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRF_SelectTransaccionesAnular.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_SELECT_ASIGNACIONES_MOVIMIENTOS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRF_SelectAsignacionesMovimientos.sql");
	private final String SQL_SELECT_DATOS_RESTAURACION = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRF_SelectBackupAsignaciones.sql");
	private final String SQL_SELECT_ANULACION_MODIFICACION = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionAnulacionModificacion.sql");
	private final String SQL_SELECT_TRANSACCION_OPERADOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalos_SelectTransaccionOperador.sql");
	
	private static final String FLUJO_MODIFICACION_AUTO = "MODIFICACION_AUTO";
	private static final String COMPRA = "COM";
	private static final String VENTA = "VEN";
	
	public static class TransaccionRF_Proceso extends TransaccionRFMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
		public Long IdTransaccionGenerada;
		public Long IdTransaccionActual;
		public String EsModificacionMarketData = "N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Usuario extends BcsPojo{
		public Long IdUsuario;
	}
	
	public static class TransaccionParaAnular extends BcsPojo {
		public Long IdTransaccion;
		public String Folio;
		public Date FechaSistemaCreacion;
		public TransaccionRF_Proceso referencia;
		public Boolean TieneAsignacionAsociada = false;
		public Boolean TieneMovimientoAsociado = false;
		public String EsModificacionMarketData = "N";
	}
	
	public static class TransaccionAnulacionModificacion extends BcsPojo {
		public Long IdTransaccionHistorico;
		public String IdEstadoRegistro;
		public String Folio;
	}
	
	public static class TransaccionOperador extends BcsPojo{
		public Long IdTransaccion;
		public String TipoMensaje;
		public Date FechaSistemaCreacion;
	}
	
	public static class AsignacionMovimiento extends BcsPojo{
		public Long IdTransaccion;
	    public Long IdAsignacion;
	    public Long IdMovimientoComprobante;
	    public String IdEstadoFacturacion;
	}
	
	public static class DatosRestauracion extends BcsPojo {
		public String KeyTransaccion;
	    public BigDecimal Cantidad;
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
	
	public boolean logica(Connection conn,boolean debug) {
		try {
			logger.info("Inicio Ciclo MarketData RF");
			Long idEmpresa = 0L;
			
	        if (debug) {
	        	idEmpresa = 1L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	        }
			
	    	SQL _sql = new SQL();
	        HashMap<String, Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
	        
			HashMap<String,Object> paramsTransacciones = new HashMap<String,Object>();
			paramsTransacciones.put("Procesado", "N");
			List<TransaccionRF_Proceso> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, paramsTransacciones , TransaccionRF_Proceso.class);
			
			logger.debug(transacciones.size() + " nuevos mensajes.");
			if (transacciones.size() == 0){
				return true;
			}
			
			for (TransaccionRF_Proceso tran : transacciones) {
				tran.registroControlMercado.Log = "";
				tran.registroControlMercado.Procesado = "N";
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
				
				if ("M".equals(tran.action)) {
					tran.EsModificacionMarketData = "S";
				}
				
				if (tran.codTicker.equalsIgnoreCase("")) {
					tran.registroControlMercado.Procesado = "E";
					tran.registroControlMercado.Log = "ERROR, NO SE ENCONTRO TRANSACCION " + tran._IdMensaje1 + " - " + tran._IdMensaje2;
					logger.debug(tran.registroControlMercado.Log);
				}
				
			}
			
			String action = null;
			List<TransaccionRF_Proceso> transaccionesProcesar = new ArrayList<>();
			
			for (TransaccionRF_Proceso tx : transacciones) {
				if (action == null) {
					action = tx.action;
				}
				
				if (action != null && !action.equalsIgnoreCase(tx.action)) {
					break;					
				}
				
				transaccionesProcesar.add(tx);
			}
			
			if ("I".equalsIgnoreCase(action) || "S".equalsIgnoreCase(action)) {
				crearTransacciones(conn, transaccionesProcesar, new HashMap<>(0), idEmpresa, idUsuario, action);
			} else if ("E".equalsIgnoreCase(action)) {
				anularTransacciones(conn, transaccionesProcesar, idEmpresa, idUsuario, action);
			} else if ("M".equalsIgnoreCase(action)) {
				modificarTransacciones(conn, transaccionesProcesar, idEmpresa, idUsuario, action);
			}
			
			List<ControlMercado> updateControlMercado = new ArrayList<>();
			for (TransaccionRF_Proceso tran : transacciones) {
				updateControlMercado.add(tran.registroControlMercado);
			}

			actualizarControlMercado(conn, updateControlMercado);
			
			logger.info("Fin Ciclo MarketData RF");
		}
		catch (Exception e) {
			logger.error("ERROR AL CREAR TRANSACCION");
			logger.error(e);
		}
		
		return true;
	}
	
	public void crearTransacciones(Connection conn, List<TransaccionRF_Proceso> transacciones, HashMap<Long, Object> hashTransaccionesAnuladas, Long idEmpresa, Long idUsuario, String action) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRF.crearTransacciones");
		long inicio = System.currentTimeMillis();

		SQL _sql = new SQL();
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");

		CrearTransaccionesMarketData_Entrada entradaCrearTransaccionMarketData = new CrearTransaccionesMarketData_Entrada();
		entradaCrearTransaccionMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionMarketData.p_IdUsuario = idUsuario;
		
		List<TransaccionRF_Proceso> transaccionesProcesar = transacciones.stream().filter(t -> !"E".equalsIgnoreCase(t.registroControlMercado.Procesado)).collect(Collectors.toList());
		
		List<String> foliosRevision = transaccionesProcesar.stream().map(t -> t.tradeID).distinct().collect(Collectors.toList());
		HashMap<String, Object> paramsMod = new HashMap<String, Object>(1);
		paramsMod.put("Folio", foliosRevision);
		
		List<TransaccionAnulacionModificacion> listaAnulacionModificacion = _sql.query(conn, SQL_SELECT_ANULACION_MODIFICACION, paramsMod, TransaccionAnulacionModificacion.class);

		for (TransaccionRF_Proceso tran : transaccionesProcesar) {
			TransaccionRFMarketData_Entrada tx = new TransaccionRFMarketData_Entrada();
			Utils.copyPojo(tran, tx);
			
			TransaccionAnulacionModificacion transaccionModificacion = listaAnulacionModificacion.stream().filter(t -> t.Folio.equals(tran.tradeID)).findFirst().orElse(null);
		
			if (transaccionModificacion != null) {
				tx.EsModificacionMarketData = "S";
				tx.EstadoModificacion = transaccionModificacion.IdEstadoRegistro;
				tx.IdTransaccionHistoricoAnulacion = transaccionModificacion.IdTransaccionHistorico;
			}
			
			entradaCrearTransaccionMarketData.transaccionesRF.add(tx);
		}
		
		if (entradaCrearTransaccionMarketData.transaccionesRF.isEmpty()) {
			logger.info("Operaciones de creacion omitidas. Las transacciones no cumplen las reglas de negocio.");
		} else {
			CrearTransaccionesMarketData servicioCrearTransaccionMarketData = new CrearTransaccionesMarketData();
			CrearTransaccionesMarketData_Salida salidaCrearTransaccionMarketData = servicioCrearTransaccionMarketData.logica(conn, entradaCrearTransaccionMarketData);
			
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
	
			for (TransaccionRF_Proceso tx : transacciones) {
				String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
				llavesRestauracion.add(tx.tradeID + fechaSistema);
			}
	
			CrearOrdenesPostreras servicioPostreras = new CrearOrdenesPostreras();
			EnviarAsignacionesFacturar servicioFacturar = new EnviarAsignacionesFacturar();
			List<OrdenPostrera_Entrada> ordenesPorCrear = new ArrayList<>();
			List<Long> asignacionesPorFacturar = new ArrayList<>();
			HashMap<String, Object> hashAsignacionesCreadasAnteriormente = new HashMap<>();
	
			if (!llavesRestauracion.isEmpty()) {
				HashMap<String, Object> params = new HashMap<>(1);
				params.put("Llave", llavesRestauracion);
	
				List<DatosRestauracion> asignacionesCreadasAnteriormente = _sql.query(conn,SQL_SELECT_DATOS_RESTAURACION, params, DatosRestauracion.class);
	
				if (!asignacionesCreadasAnteriormente.isEmpty()) {
					hashAsignacionesCreadasAnteriormente = Utils.List2Hash(asignacionesCreadasAnteriormente, "KeyTransaccion");
				}
			}
	
			long idInternoContador = 1;
	
			for (TransaccionRF_Proceso tx : transaccionesProcesar) {
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
					logger.error("Error restaurando asignacion para Folio " + tx.tradeID, e);
				}
			}
	
			if (!ordenesPorCrear.isEmpty()) {
				CrearOrdenesPostreras_Entrada entradaPostrera = new CrearOrdenesPostreras_Entrada();
				entradaPostrera.p_IdEmpresa = idEmpresa;
				entradaPostrera.p_IdUsuario = idUsuario;
				entradaPostrera.p_IdModulo = "ORD";
				entradaPostrera.p_OrigenPost = "MARKETDATA_MODIFICACION";
				entradaPostrera.p_Ordenes = ordenesPorCrear;
	
				CrearOrdenesPostreras.CrearOrdenesPostreras_Salida salidaPostrera = servicioPostreras.logica(conn, entradaPostrera);
	
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
						EnviarAsignacionesFacturar_Entrada entradaFact = new EnviarAsignacionesFacturar_Entrada();
						entradaFact.p_IdEmpresa = idEmpresa;
						entradaFact.p_IdUsuario = idUsuario;
						entradaFact.p_IdModulo = "ORD";
						entradaFact.p_Asignaciones = listaParaFacturar;
						servicioFacturar.logica(conn, entradaFact);
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

			for (TransaccionRF_Proceso tran : transaccionesProcesar) {
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
		logger.trace("FIN::ControlMercadoMarketDataPalosRF.crearTransacciones tiempo: " + (fin - inicio));
	}
	
	private List<DatosRestauracion> obtenerAsignacionesAnteriores(TransaccionRF_Proceso tx, HashMap<String, Object> hashAsignacionesCreadasAnteriormente) throws Exception {
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
	
	public List<TransaccionParaAnular> anularTransacciones(Connection conn, List<TransaccionRF_Proceso> transacciones, Long idEmpresa, Long idUsuario, String action) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRF.anularTransacciones");
		long inicio = System.currentTimeMillis();

		SQL _sql = new SQL();
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");

		List<String> llavesAnulacion = new ArrayList<>();

		for (TransaccionRF_Proceso tx : transacciones) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
			llavesAnulacion.add(tx.tradeID + fechaSistema);
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesPorAnular = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesAnular = new ArrayList<>();
		boolean found = false;
		List<Long> idsTransacciones = new ArrayList<>();

		for (TransaccionRF_Proceso tx : transacciones) {
			found = false;
			for (TransaccionParaAnular txa : transaccionesPorAnular) {
				String fechaTransaccion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
				String fechaBd = Fecha.toStringDate(txa.FechaSistemaCreacion);

				if (tx.tradeID.equalsIgnoreCase(txa.Folio) && fechaTransaccion.equalsIgnoreCase(fechaBd)) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format((txa.FechaSistemaCreacion));
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = "ANULACION POR SISTEMA BURSATIL";
					e.Observaciones = "";
					e.EsModificacionMarketData = tx.EsModificacionMarketData;
					transaccionesAnular.add(e);
					txa.referencia = tx;
					txa.EsModificacionMarketData = tx.EsModificacionMarketData;
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

		if (!transaccionesAnular.isEmpty()) {
			List<Movimiento_Entrada> movimientosPorAnular = new ArrayList<>();
			List<AnularAsignaciones.Asignacion_Entrada> asignacionesPorAnular = new ArrayList<>();

			HashMap<String, Object> paramsTransaccion = new HashMap<>(1);
			paramsTransaccion.put("IdTransaccion", idsTransacciones);

			List<AsignacionMovimiento> dependencias = _sql.query(conn, SQL_SELECT_ASIGNACIONES_MOVIMIENTOS, paramsTransaccion, AsignacionMovimiento.class);

			Map<Long, List<AsignacionMovimiento>> hashDependencias = dependencias.stream().collect(Collectors.groupingBy(d -> d.IdTransaccion));

			for (Long idTransaccionActual : idsTransacciones) {
				List<AsignacionMovimiento> dependenciasTransaccion = hashDependencias.getOrDefault(idTransaccionActual, Collections.emptyList());
				
				if (dependenciasTransaccion != null) {
					Movimiento_Entrada movimiento = dependenciasTransaccion.stream()
							.filter(dt -> dt.IdMovimientoComprobante != null && dt.IdMovimientoComprobante > 0)
							.map(m -> {
								Movimiento_Entrada mov = new Movimiento_Entrada();
								mov.IdMovimientoComprobante = m.IdMovimientoComprobante;
								return mov;
							}).distinct().findFirst().orElse(null);

					if (movimiento != null) {
						movimientosPorAnular.add(movimiento);
						transaccionesPorAnular.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneMovimientoAsociado = true);
					}

					AnularAsignaciones.Asignacion_Entrada asignacion = dependenciasTransaccion.stream()
							.filter(dt -> dt.IdAsignacion != null && dt.IdAsignacion > 0).map(a -> {
								AnularAsignaciones.Asignacion_Entrada asig = new AnularAsignaciones.Asignacion_Entrada();
								asig.IdAsignacion = a.IdAsignacion;
								return asig;
							}).distinct().findFirst().orElse(null);

					if (asignacion != null) {
						asignacionesPorAnular.add(asignacion);
						transaccionesPorAnular.stream().filter(t -> t.IdTransaccion.equals(idTransaccionActual)).forEach(o -> o.TieneAsignacionAsociada = true);
					}
				}
			}

			if (!movimientosPorAnular.isEmpty()) {
				AnularMovimientoComprobante servicioAnularMovimiento = new AnularMovimientoComprobante();
				AnularMovimientoComprobante_Entrada entradaAnularMovimiento = new AnularMovimientoComprobante_Entrada();
				entradaAnularMovimiento.p_IdEmpresa = idEmpresa;
				entradaAnularMovimiento.p_IdUsuario = idUsuario;
				entradaAnularMovimiento.p_IdModulo = "ORD";
				entradaAnularMovimiento.p_simularOperacion = "N";
				entradaAnularMovimiento.p_UsaReglasNegocio = "S";
				entradaAnularMovimiento.p_Glosa = "Anulacion Automatica MarketData";
				entradaAnularMovimiento.p_MovimientoAAnular = movimientosPorAnular;
				servicioAnularMovimiento.logica(conn, entradaAnularMovimiento);
			}

			if (!asignacionesPorAnular.isEmpty()) {
				AnularAsignaciones servicioAnularAsignacion = new AnularAsignaciones();
				AnularAsignaciones.AnularAsignaciones_Entrada entradaAnularAsignacion = new AnularAsignaciones.AnularAsignaciones_Entrada();
				entradaAnularAsignacion.p_IdEmpresa = idEmpresa;
				entradaAnularAsignacion.p_IdUsuario = idUsuario;
				entradaAnularAsignacion.p_IdModulo = "ORD";
				entradaAnularAsignacion.asignacionesParaAnular = asignacionesPorAnular;
				servicioAnularAsignacion.logica(conn, entradaAnularAsignacion);
			}

			AnularTransaccionMasiva servicioAnularTransaccion = new AnularTransaccionMasiva();
			EntradaAnularTransaccionMasiva entradaAnularTransaccion = new EntradaAnularTransaccionMasiva();
			entradaAnularTransaccion.p_IdEmpresa = idEmpresa;
			entradaAnularTransaccion.p_IdUsuario = idUsuario;
			entradaAnularTransaccion.p_IdModulo = "ORD";
			entradaAnularTransaccion.p_Reproceso = "N";
			entradaAnularTransaccion.p_transacciones = transaccionesAnular;

			SalidaAnularTransaccionMasiva salidaAnularTransaccion = servicioAnularTransaccion.logica(conn, entradaAnularTransaccion);
			HashMap<Long, Object> hashSalidaMensajes = Utils.List2Hash(salidaAnularTransaccion.P_OUT_MENSAJES, "id");
			
			for (TransaccionParaAnular txa : transaccionesPorAnular) {
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
		logger.trace("FIN::ControlMercadoMarketDataPalosRF.anularTransacciones tiempo: " + (fin - inicio));
		return transaccionesPorAnular;
	}
	
	private void modificarTransacciones(Connection conn, List<TransaccionRF_Proceso> transacciones, Long idEmpresa, Long idUsuario, String action) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosRF.modificarTransacciones");
		long inicio = System.currentTimeMillis();
		
		List<TransaccionParaAnular> transaccionesAnuladas = anularTransacciones(conn, transacciones, idEmpresa, idUsuario, action);
		HashMap<Long, Object> hashTransaccionesAnuladas = Utils.List2Hash(transaccionesAnuladas, "IdTransaccion");
	    crearTransacciones(conn, transacciones, hashTransaccionesAnuladas, idEmpresa, idUsuario, action);
	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRF.modificarTransacciones tiempo: " + (fin - inicio));
	}
	
	private void actualizarControlMercado (Connection conn, List<ControlMercado> updateControlMercado) throws SQLException {
		logger.debug("INICIO::ControlMercadoMarketDataPalosRF.actualizarControlMercado");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		
		if (!updateControlMercado.isEmpty()) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
    		conn.commit();
        }
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRF.actualizarControlMercado tiempo: " + (fin - inicio));
	}
	
	public static void main(String[] args) {
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties");

            try {
            	ControlMercadoMarketDataPalosRF demo=new ControlMercadoMarketDataPalosRF();
            	/*SQL.exec(con, "UPDATE BSCL.\"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\"='N'" + " WHERE \"TipoMensaje\"='MARKET_DATA_PALO_RF' AND \"IdMensaje1\"='2025090314550002' AND \"IdMensaje2\"='180528'");*/
            	         		
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
