package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.facturacion.AnularMovimientoComprobante;
import cl.bcs.ordenes.AnularAsignaciones;
import cl.bcs.ordenes.AnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRVMarketData_Entrada;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionDropcopy;
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

public class ControlMercadoPalosRVMME extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoPalosRVMME.class);
	
	private final String SIDE_COMPRA = "1";
	private final String SIDE_VENTA = "2";
	private final String SIDE_VC = "5";
	private final String ACTION_ANULACION = "7";
	
	private final String SQL_DELETE_HISTORICOS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_DeleteRegistrosHistoricos.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_BEC = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectTransaccionesBEC.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectTransaccionesAnular.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectTransaccionesOtrosCorredores.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_INSERT_HIS_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_Insert_His_Transacciones.sql");
	private final String SQL_SELECT_CORREDORAS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectCorredoras.sql");
	private final String SQL_SELECT_HIS_PALOS_RV = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectHisPalosRV.sql");
	//private final String SQL_SELECT_ORDEN_ANTERIOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRVMME_SelectOrdenAnterior.sql");
	//private final String SQL_SELECT_MOVIMIENTO_ANTERIOR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRVMME_SelectMovimientoAnterior.sql");
	private final String SQL_SELECT_ASIGNACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRVMME_SelectAsignaciones.sql");
	private final String SQL_SELECT_MOVIMIENTO_FACTURAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosRVMME_SelectMovimientoFacturar.sql");
	private final String SQL_SELECT_INSTRUMENTO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoPalosRVMME_SelectInstrumento.sql");
	
	public static class TransaccionDropcopyProceso extends TransaccionDropcopy{
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
	}
	
	public static class BusquedaInstrumento extends BcsPojo{
		public String NemoNegociable;
		public String NemoRaiz;
		public Long IdInstrumentoRaiz;
		public String IdCondicionLiquidacion;
		public String IdMoneda;
	}
	
	public static class TransaccionRV_Proceso extends TransaccionRVMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
		
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
	}
	
	public static class Corredora extends BcsPojo{
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
	}

	public static class Asignacion extends BcsPojo {
		public Long IdAsignacion;
		public Long IdTransaccion;
	}

	public static class MovimientoFacturar extends BcsPojo  {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public Long IdPortafolio;
		public Long IdCliente;
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	@Override
	public void onError(Throwable t) {}

	@Override
	public void pause() {}
	
	private void limpiezaHistoricos(Connection conn) throws SQLException {
		SQL.exec(conn, SQL_DELETE_HISTORICOS);
		conn.commit();
	}
	
	private void limpiezaTransaccionesOtrosCorredores(Connection conn) throws SQLException {
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		SQL _sql = new SQL();
		List<TransaccionRV_Proceso> transaccionesOtrosCorredores = _sql.query(conn, SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES, new HashMap<String,Object>(), TransaccionRV_Proceso.class);
		logger.debug(transaccionesOtrosCorredores.size() + " transacciones otros corredores detectadas.");
		for (TransaccionRV_Proceso tran : transaccionesOtrosCorredores) {
			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			tran.registroControlMercado.Procesado = "E";
			if (tran.tradeID != null) {
				tran.registroControlMercado.Log = "Transaccion otros corredores Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp+ " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;	
			}
			else {
				tran.registroControlMercado.Log = "Registro sin datos";
			}
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		//ACTUALIZAR CONTROL MERCADO OTROS CORREDORES
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
			logger.debug(transaccionesOtrosCorredores.size() + " transacciones otros corredores actualizadas.");
		}
	}
	
	public void limpiezaTransaccionesNoEncontradas(Connection conn) throws SQLException {
		SQL _sql = new SQL();
		List<TransaccionDropcopyProceso> transaccionesDrop = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String,Object>(), TransaccionDropcopyProceso.class);
		List<TransaccionRV_Proceso> transacciones = new ArrayList<TransaccionRV_Proceso>();
		transacciones.addAll(HomologacionDropcopy(conn, transaccionesDrop));
		if (transacciones.size() == 0){
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
        	logger.debug(updateControlMercado.size() + " registros invalidos.");
        	_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
    		conn.commit();
        }
	}
		
	private void limpiezaTransaccionesBEC(Connection conn) throws SQLException {
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		SQL _sql = new SQL();
		List<TransaccionRV_Proceso> transaccionesOtrosCorredores = _sql.query(conn, SQL_SELECT_TRANSACCIONES_BEC, new HashMap<String,Object>(), TransaccionRV_Proceso.class);
		logger.debug(transaccionesOtrosCorredores.size() + " transacciones BEC detectadas.");
		for (TransaccionRV_Proceso tran : transaccionesOtrosCorredores) {
			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			tran.registroControlMercado.Procesado = "E";
			if (tran.tradeID != null) {
				tran.registroControlMercado.Log = "Transaccion BEC Folio:" + tran.tradeID + " Fecha:" + tran.timeStamp+ " C:" + tran.buyerBroker + " V:" + tran.sellerBroker;	
			}
			else {
				tran.registroControlMercado.Log = "Registro sin datos";
			}
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		//ACTUALIZAR CONTROL MERCADO BEC
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
			logger.debug(transaccionesOtrosCorredores.size() + " transacciones BEC actualizadas.");
		}
	}
	
	public void crearTransacciones(Connection conn, List<TransaccionRV_Proceso> transacciones, List<TransaccionDropcopyProceso> transaccionDrop,Long idEmpresa,Long idUsuario) throws Exception {
		CrearTransaccionesMarketData_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketData_Entrada();
		SQL _sql = new SQL();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
		for (TransaccionRV_Proceso tran : transacciones) {
			TransaccionRVMarketData_Entrada tx = new TransaccionRVMarketData_Entrada();
			Utils.copyPojo(tran, tx);
			if (localizacionSistema.equalsIgnoreCase("PE")) {
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
			entradaCrearTransaccionesMarketData.transaccionesRV.add(tx);
		}
		
		CrearTransaccionesMarketData serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketData();
		CrearTransaccionesMarketData_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);
		
		for (TransaccionRV_Proceso tran : transacciones) {
			for (Resultado_Salida resp : salidaCrearTransaccionesMarketData.P_OUT_RESULTADO) {
				if (tran.IdInterno.equals(resp.IdInterno)) {
					tran.registroControlMercado.Procesado = "S";
					logger.debug("Registro " + tran.registroControlMercado.IdMensaje1 + " - " + tran.registroControlMercado.IdMensaje2 + " procesado OK");
				}
			}
			
			if (!tran.registroControlMercado.Procesado.equalsIgnoreCase("S")) {
				tran.registroControlMercado.Procesado = "E";
				logger.debug("Registro " + tran.registroControlMercado.IdMensaje1 + " - " + tran.registroControlMercado.IdMensaje2 + " procesado ERROR");
			}
			
			for (MensajeSalida msj : salidaCrearTransaccionesMarketData.P_OUT_MENSAJES) {
				if (tran.IdInterno.equals(msj.id)) {
					tran.registroControlMercado.Log += msj.descripcion + " ";
					logger.debug(msj.descripcion);
				}
			}
    	}
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		for (TransaccionRV_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		updateControlMercado.addAll(marcarRegistrosAdicionales(updateControlMercado,transaccionDrop,false));
		
		//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			_sql.batch(conn, SQL_INSERT_HIS_TRANSACCIONES, transaccionDrop, TransaccionDropcopyProceso.class);
			conn.commit();
		}
	}
	
	private List<ControlMercado> marcarRegistrosAdicionales(List<ControlMercado> updateControlMercado, List<TransaccionDropcopyProceso> transaccionDrop, boolean esAnulacion){
		List<ControlMercado> updatesAdicionales = new ArrayList<ControlMercado>();
		HashMap<String, ControlMercado> hashControlMercado = new HashMap<String, ControlMercado>();
		for (ControlMercado cm : updateControlMercado) {
			String llave = cm.IdMensaje2+cm.IdMensaje1;
			hashControlMercado.put(llave, cm);
		}
		for (TransaccionDropcopyProceso drop : transaccionDrop) {
			String action = drop.TRADEREPORTTYPE.toString();
			boolean noMarcar = (esAnulacion && !action.equalsIgnoreCase(ACTION_ANULACION) || !esAnulacion && action.equalsIgnoreCase(ACTION_ANULACION));
			if (noMarcar) continue;
			String llave = drop._IdMensaje2+drop._IdMensaje1;
			ControlMercado registrado = hashControlMercado.get(llave);
			if (registrado == null) {
				ControlMercado registroAdicional = new ControlMercado();
				registroAdicional.IdMensaje1 = drop._IdMensaje1;
				registroAdicional.IdMensaje2 = drop._IdMensaje2;
				registroAdicional.Procesado = "E";
				registroAdicional.Log = "Se descarta registro auxiliar";
				updatesAdicionales.add(registroAdicional);
			}
		}
		return updatesAdicionales;
	}

	private void anularAsignaciones(Connection conn, List<Asignacion> asignaciones, Long idEmpresa, Long idUsuario) throws Exception {
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
	
	public void anularTransacciones(Connection conn, List<TransaccionRV_Proceso> transacciones, List<TransaccionDropcopyProceso> transaccionesDrop, Long idEmpresa, Long idUsuario) throws Exception {
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		SQL _sql = new SQL();
		
		List<String> llavesAnulacion = new ArrayList<>();
		for (TransaccionRV_Proceso tx : transacciones ) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
			llavesAnulacion.add(tx.tradeID+fechaSistema);
		}
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesPorAnular = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesAnular = new ArrayList<>();
		boolean found = false;
		List<Long> idsTransacciones = new ArrayList<>();

		for (TransaccionRV_Proceso tx : transacciones) {
			found = false;
			for (TransaccionParaAnular txa : transaccionesPorAnular) {
				if (tx.tradeID.equalsIgnoreCase(txa.Folio) && sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime))).equalsIgnoreCase(Fecha.toStringDate(txa.FechaSistemaCreacion))) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.tradeTime)));
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = "ANULACION POR SISTEMA BURSATIL";
					e.Observaciones = "";
					transaccionesAnular.add(e);
					txa.referencia = tx;
					found = true;
					idsTransacciones.add(txa.IdTransaccion);
				}
			}
			
			if (!found) {
				//no encontrado
				tx.registroControlMercado.Procesado = "E";
				tx.registroControlMercado.Log = "Transaccion a eliminar, no encontrada, tradeID:" + tx.tradeID + " tradeTime:" + tx.tradeTime;
			}
		}
		
		if (!idsTransacciones.isEmpty()) {
			HashMap<String, Object> paramsTransaccion = new HashMap<>();
			paramsTransaccion.put("IdTransaccion", idsTransacciones);

		    List<MovimientoFacturar> movimientosFacturados = _sql.query(conn, SQL_SELECT_MOVIMIENTO_FACTURAR, paramsTransaccion, MovimientoFacturar.class);
		    List<Asignacion> asignacionesAsignadas = _sql.query(conn, SQL_SELECT_ASIGNACIONES, paramsTransaccion,Asignacion.class);
		    
		    if(!movimientosFacturados.isEmpty()) {
		    	anularMovimientosFacturados(conn, movimientosFacturados, idEmpresa, idUsuario);
		    }
		    
		    if (!asignacionesAsignadas.isEmpty()) {
		        anularAsignaciones(conn, asignacionesAsignadas, idEmpresa, idUsuario);
		    }
		}
		
		AnularTransaccionMasiva anularTransaccionesService = new AnularTransaccionMasiva();
		EntradaAnularTransaccionMasiva entradaAnularTransacciones = new EntradaAnularTransaccionMasiva();
		
		entradaAnularTransacciones.p_IdEmpresa = idEmpresa;
		entradaAnularTransacciones.p_IdUsuario = idUsuario;
		entradaAnularTransacciones.p_IdModulo = "ORD";
		entradaAnularTransacciones.p_Reproceso = "N";
		entradaAnularTransacciones.p_transacciones = transaccionesAnular;
		
		SalidaAnularTransaccionMasiva salidaAnularTransacciones = anularTransaccionesService.logica(conn, entradaAnularTransacciones);
		HashMap<Long, Object> hashSalidaMensajes = Utils.List2Hash(salidaAnularTransacciones.P_OUT_MENSAJES, "id");
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
		for (TransaccionParaAnular txa : transaccionesPorAnular) {
			MensajeSalida msj = (MensajeSalida) hashSalidaMensajes.get(txa.IdTransaccion);
		
			if (msj == null) {
				msj = new MensajeSalida(txa.IdTransaccion, "", "", "");
			}

			if (MensajeSalida.MENSAJE_OK.equals(msj.tipo)) {
				txa.referencia.registroControlMercado.Procesado = "S";
			} else {
				txa.referencia.registroControlMercado.Procesado = "E";
			}

			txa.referencia.registroControlMercado.Log += msj.descripcion + " : " + msj.id + " ";
		}
		
		
		for (TransaccionRV_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		updateControlMercado.addAll(marcarRegistrosAdicionales(updateControlMercado, transaccionesDrop, true));
		
		//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			_sql.batch(conn, SQL_INSERT_HIS_TRANSACCIONES, transaccionesDrop, TransaccionDropcopyProceso.class);
			conn.commit();
		}
	}
	
	public boolean logica(Connection conn,boolean debug) throws Exception {
		try {
			logger.info("Inicio Ciclo MarketData RV");
			Long idEmpresa = 0L;
	        
	        if (debug) {
	        	idEmpresa = 1L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	        }
       		
	        limpiezaHistoricos(conn);
	        limpiezaTransaccionesNoEncontradas(conn);
	        limpiezaTransaccionesOtrosCorredores(conn);
	        //TODO: Se debe realizar la implementacion de transacciones BEC mediante dropcopyrv mme
	        limpiezaTransaccionesBEC(conn);
	        
	        SQL _sql = new SQL();
	        HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			
			List<TransaccionDropcopyProceso> transaccionDrop = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String,Object>(), TransaccionDropcopyProceso.class);
			List<TransaccionRV_Proceso> transacciones = new ArrayList<TransaccionRV_Proceso>();
			transacciones.addAll(HomologacionDropcopy(conn, transaccionDrop));
			logger.debug(transacciones.size() + " nuevos mensajes.");
			if (transacciones.size() == 0){
				return true;
			}
			
			for (TransaccionRV_Proceso tran : transacciones) {
				tran.registroControlMercado.Log = "";
				tran.registroControlMercado.Procesado = "N";
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			}
			
			String action = null;
			String timeStamp = null;
			List<TransaccionRV_Proceso> transaccionesProcesar = new ArrayList<>();
			List<TransaccionRV_Proceso> transaccionesIngreso = new ArrayList<>();
			List<TransaccionRV_Proceso> transaccionesAnulacion = new ArrayList<>();
			TransaccionRV_Proceso transaccionConModificacionPorIgnorar = null;
			for (TransaccionRV_Proceso tx : transacciones) {
				if (action == null) {
					action = tx.action;
				}
				if(timeStamp == null) {
					timeStamp = tx.timeStamp;
				}
				if("E".equalsIgnoreCase(action)) {
					if(("I".equalsIgnoreCase(tx.action) || "S".equalsIgnoreCase(tx.action)) && timeStamp.equalsIgnoreCase(tx.timeStamp)) {
						transaccionConModificacionPorIgnorar = tx;
					}
				}
				if (action != null && !action.equalsIgnoreCase(tx.action)) {
					break;					
				}
				transaccionesProcesar.add(tx);
			}
			
			for (TransaccionRV_Proceso tx : transacciones) {
				if(tx.action.equalsIgnoreCase("E")) {					
					transaccionesAnulacion.add(tx);
				} else if(tx.action.equalsIgnoreCase("I") || tx.action.equalsIgnoreCase("S")) {
					transaccionesIngreso.add(tx);
				}
			}
			
			HashMap<String,Corredora> corredoras = buscarCorredoras(conn);
			ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
			InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, idEmpresa);
			
			if(transaccionConModificacionPorIgnorar != null) {
				Corredora corredoraCompra = corredoras.get(transaccionConModificacionPorIgnorar.buyerBroker.toString());
				Corredora corredoraVenta = corredoras.get(transaccionConModificacionPorIgnorar.sellerBroker.toString());
				if(corredoraCompra != null && corredoraVenta !=null) {
					if(corredoraCompra.IdCorredora.equals(empresa.IdCorredora) && !corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
						TransaccionRVMarketData transaccion = buscarTransaccion(conn, transaccionConModificacionPorIgnorar.tradeTime, transaccionConModificacionPorIgnorar.tradeID);
						if(transaccion != null) {
							if(ignorarModificacion(transaccion, transaccionConModificacionPorIgnorar)) {
								actualizarControlMercadoTransaccionIgnorada(conn, transaccionConModificacionPorIgnorar);
								transaccionesProcesar = transaccionesProcesar.stream()
										.filter(tran -> !tran.tradeID.equalsIgnoreCase(transaccion.tradeID))
										.collect(Collectors.toList());
							}
						}
					}
				}
			}
			
			if(!transaccionesIngreso.isEmpty()) {
				crearTransacciones(conn,transaccionesIngreso,transaccionDrop,idEmpresa,idUsuario);
			}
			if(!transaccionesAnulacion.isEmpty()) {
				anularTransacciones(conn,transaccionesAnulacion,transaccionDrop,idEmpresa,idUsuario);
			}
			logger.info("Fin Ciclo MarketData RV");
		}
		catch (Exception e) {
			throw e;
		}		
		return true;
	}
	
	private List<TransaccionRV_Proceso> HomologacionDropcopy(Connection conn, List<TransaccionDropcopyProceso> transaccionesDrop) throws SQLException{
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.HomologacionDropcopy");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		HashMap<String,TransaccionDropcopy> hashCompraVenta = new HashMap<String,TransaccionDropcopy>();
		HashMap<String,Object> paramsIns = new HashMap<String,Object>();
		Set<String> listaInstrumentos = new HashSet<String>();
		for (TransaccionDropcopyProceso tdrop : transaccionesDrop) {
			hashCompraVenta.put(tdrop.TRDMATCHID+"-"+tdrop.SIDE, tdrop);
			listaInstrumentos.add(tdrop.SYMBOL);
		}
		paramsIns.put("nemos", new ArrayList<>(listaInstrumentos));
		List<BusquedaInstrumento> instrumentos = _sql.query(conn, SQL_SELECT_INSTRUMENTO, paramsIns, BusquedaInstrumento.class);
		HashMap<String,BusquedaInstrumento> infoInstrumentos = new HashMap<String,BusquedaInstrumento>();
		for (BusquedaInstrumento ins : instrumentos) {
			BusquedaInstrumento info = new BusquedaInstrumento();
			info.NemoNegociable = ins.NemoNegociable;
			info.NemoRaiz = ins.NemoRaiz;
			info.IdInstrumentoRaiz = ins.IdInstrumentoRaiz;
			info.IdCondicionLiquidacion = ins.IdCondicionLiquidacion;
			info.IdMoneda = ins.IdMoneda;
			infoInstrumentos.put(ins.NemoNegociable, info);
		}
		logger.debug(transaccionesDrop.size() + " nuevos mensajes.");
		
		HashMap<String, TransaccionRV_Proceso> hashTransaccionesDrop = new HashMap<String, TransaccionRV_Proceso>();
		for (TransaccionDropcopyProceso tdrop : transaccionesDrop) {
			TransaccionDropcopy compra = hashCompraVenta.get(tdrop.TRDMATCHID+"-"+SIDE_COMPRA);
			TransaccionDropcopy venta = hashCompraVenta.get(tdrop.TRDMATCHID+"-"+SIDE_VC) == null ? hashCompraVenta.get(tdrop.TRDMATCHID+"-"+SIDE_VENTA) : hashCompraVenta.get(tdrop.TRDMATCHID+"-"+SIDE_VC);
						
			String action = "";
			if (tdrop.TRADEREPORTTYPE.equals(7L) && tdrop.EXECTYPE.equalsIgnoreCase("H")) {					
				action = "E";
			}
			else {					
				action = "I";
			}
			
			String instrumento = "";
			if (infoInstrumentos.get(tdrop.SYMBOL) == null) {
				String mensaje = "Instrumento "+tdrop.SYMBOL+" no encontrado";
				logger.error(mensaje);
			}else if (infoInstrumentos.get(tdrop.SYMBOL).NemoRaiz == null) {
				instrumento = tdrop.SYMBOL;
			}else {
				instrumento = infoInstrumentos.get(tdrop.SYMBOL).NemoRaiz;
			}
										
			TransaccionRV_Proceso tr = new TransaccionRV_Proceso();
			tr.IdInterno = tdrop.IdInterno;
			tr._TipoMensaje = tdrop._TipoMensaje;
			tr._IdMensaje1 = tdrop._IdMensaje1;
			tr._IdMensaje2 = tdrop._IdMensaje2;
			tr.codTicker = "TRADE";
			tr.timeStamp = tdrop.TIMESTAMP.replace("-", "").replace(":", "").replace("T", "").replace("Z", "").replace(".", "").replace(" ", "");
			tr.action = action;
			tr.tradeTime = tdrop.TRANSACTTIME.replace("-", "").replace(":", "").replace("T", "").replace("Z", "").replace(".", "").replace(" ", "").substring(0, 17);
			tr.tradeID = tdrop.SIDETRADEID;
			tr.instrument = instrumento;
			tr.market = "";
			tr.settlementCondition = infoInstrumentos.get(tdrop.SYMBOL) == null ? "" : infoInstrumentos.get(tdrop.SYMBOL).IdCondicionLiquidacion;
			tr.orderType = (compra != null && venta != null && compra.EXECUTINGFIRM.equalsIgnoreCase(venta.EXECUTINGFIRM)) ? "" : "OC";
			tr.indivisible = "";
			tr.shortSell = hashCompraVenta.get(tdrop.TRDMATCHID+"-"+SIDE_VC) != null ? "YES" : "NO";
			tr.quantity = tdrop.LASTQTY;
			tr.price = tdrop.LASTPX;
			tr.amount = tdrop.LASTQTY.multiply(tdrop.LASTPX);
			tr.closingPrice = new BigDecimal(0);
			tr.buyFundManager = compra != null ? compra.FONDO : "";
			tr.buyerBroker = tdrop.SIDE.equalsIgnoreCase(SIDE_COMPRA) 
					? tdrop.EXECUTINGFIRM.substring(tdrop.EXECUTINGFIRM.length()-3,tdrop.EXECUTINGFIRM.length()) 
					: tdrop.CONTRAFIRM.substring(tdrop.CONTRAFIRM.length()-3,tdrop.CONTRAFIRM.length());
			if(!tr.buyFundManager.equals("")) {
				tr.buyEnteringFirm = compra != null ? compra.CODIGOOPERADORASOCIADO : "001";
			}else {
				tr.buyEnteringFirm = tdrop.SIDE.equalsIgnoreCase(SIDE_COMPRA) 
						? tdrop.EXECUTINGFIRM.substring(tdrop.EXECUTINGFIRM.length()-3,tdrop.EXECUTINGFIRM.length()) 
								: tdrop.CONTRAFIRM.substring(tdrop.CONTRAFIRM.length()-3,tdrop.CONTRAFIRM.length()) ;			
			}
			tr.buyEnteringTrader = compra != null ? compra.EXECUTINGTRADER.substring(compra.EXECUTINGTRADER.length()-3,compra.EXECUTINGTRADER.length()) : "001";
			tr.buyClientID = compra != null ? compra.TEXT : "";

			tr.sellFundManager = venta != null ? venta.FONDO : "";
			tr.sellerBroker = tdrop.SIDE.equalsIgnoreCase(SIDE_VENTA) || tdrop.SIDE.equalsIgnoreCase(SIDE_VC)
					? tdrop.EXECUTINGFIRM.substring(tdrop.EXECUTINGFIRM.length()-3,tdrop.EXECUTINGFIRM.length()) 
					: tdrop.CONTRAFIRM.substring(tdrop.CONTRAFIRM.length()-3,tdrop.CONTRAFIRM.length());
			
			if(!tr.sellFundManager.equalsIgnoreCase("")) {
				tr.sellEnteringFirm = venta != null ? venta.CODIGOOPERADORASOCIADO: "001";
			}else {				
				tr.sellEnteringFirm = tdrop.SIDE.equalsIgnoreCase(SIDE_VENTA) || tdrop.SIDE.equalsIgnoreCase(SIDE_VC)
						? tdrop.EXECUTINGFIRM.substring(tdrop.EXECUTINGFIRM.length()-3,tdrop.EXECUTINGFIRM.length()) 
								: tdrop.CONTRAFIRM.substring(tdrop.CONTRAFIRM.length()-3,tdrop.CONTRAFIRM.length());
			}
			
			tr.sellEnteringTrader = venta != null ? venta.EXECUTINGTRADER.substring(venta.EXECUTINGTRADER.length()-3,venta.EXECUTINGTRADER.length()) : "001";
			tr.sellClientID = venta != null ? venta.TEXT : "";

			tr.priceBuyingQTY = new BigDecimal(0);
			tr.priceBuyingPrice = new BigDecimal(0);
			tr.priceSellingQty = new BigDecimal(0);
			tr.priceSellingPrice = new BigDecimal(0);
			tr.codIsin = "";
			tr.securityType = "";
			tr.timeInForce = "";
			tr.tradingSessionID = "";
			tr.entryType = "";
			tr.priceExchange = "";
			tr.priceTime = "";
			tr.tradeCurrency = infoInstrumentos.get(tdrop.SYMBOL) == null ? "" : infoInstrumentos.get(tdrop.SYMBOL).IdMoneda;
			tr.orderBookName = tdrop.SYMBOL;
			hashTransaccionesDrop.put(tdrop.TRDMATCHID+tdrop.EXECUTINGFIRM+tdrop.TRADEREPORTTYPE, tr);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.HomologacionDropcopy tiempo: " + (fin - inicio));
		return new ArrayList<TransaccionRV_Proceso>(hashTransaccionesDrop.values());
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
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.buscarCorredoras tiempo: " + (fin - inicio));
		return corredoras;
	}

	private List<TransaccionRVMarketData> ConvertirATransaccionRVMarketData(List<TransaccionRV_Proceso> transaccionesRV){
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.ConvertirATransaccionRVMarketData");
		long inicio = System.currentTimeMillis();
		List<TransaccionRVMarketData> lista = new ArrayList<TransaccionRVMarketData>();
		for (TransaccionRV_Proceso tr : transaccionesRV) {
			TransaccionRV_Proceso tran = new TransaccionRV_Proceso();
			tran.codTicker = tr.codTicker;
			tran.timeStamp = tr.timeStamp;
			tran.action = tr.action;
			tran.tradeTime = tr.tradeTime;
			tran.tradeID = tr.tradeID;
			tran.instrument = tr.instrument;
			tran.market = tr.market;
			tran.settlementCondition = tr.settlementCondition;
			tran.orderType = tr.orderType;
			tran.indivisible = tr.indivisible;
			tran.shortSell = tr.shortSell;
			tran.quantity = tr.quantity;
			tran.price = tr.price;
			tran.amount = tr.amount;
			tran.closingPrice = tr.closingPrice;
			tran.buyerBroker = tr.buyerBroker;
			tran.buyEnteringFirm = tr.buyEnteringFirm;
			tran.buyEnteringTrader = tr.buyEnteringTrader;
			tran.sellerBroker = tr.sellerBroker;
			tran.sellEnteringFirm = tr.sellEnteringFirm;
			tran.sellEnteringTrader = tr.sellEnteringTrader;
			tran.priceBuyingQTY = tr.priceBuyingQTY;
			tran.priceBuyingPrice = tr.priceBuyingPrice;
			tran.priceSellingQty = tr.priceSellingQty;
			tran.priceSellingPrice = tr.priceSellingPrice;
			tran.priceExchange = tr.priceExchange;
			tran.timeInForce = tr.timeInForce;
			tran.entryType = tr.entryType;
			tran.tradingSessionID = tr.tradingSessionID;
			tran.codIsin = tr.codIsin;
			tran.securityType = tr.securityType;
			tran.priceTime = tr.priceTime;
			tran.buyClientID = tr.buyClientID;
			tran.buyFundManager = tr.buyFundManager;
			tran.sellClientID = tr.sellClientID; 
			tran.sellFundManager = tr.sellFundManager; 
			tran.tradeCurrency = tr.tradeCurrency;
			tran.orderBookName = tr.orderBookName;
			lista.add(tran);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.ConvertirATransaccionRVMarketData tiempo: " + (fin - inicio));
		return lista;
	}
	
	private TransaccionRVMarketData buscarTransaccion (Connection con, String tradeTime, String folio) throws SQLException{
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.buscarTransaccion");
		long inicio = System.currentTimeMillis();
		
		TransaccionRVMarketData transaccion = null;
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("TradeTime", tradeTime);
		params.put("Folio", folio);
		List<TransaccionDropcopyProceso> listaTransaccionesDropcopy = sql.query(con, SQL_SELECT_HIS_PALOS_RV, params, TransaccionDropcopyProceso.class);
		List<TransaccionRV_Proceso> listaTransaccionesRV = HomologacionDropcopy(con, listaTransaccionesDropcopy);
		List<TransaccionRVMarketData> listaTransacciones = ConvertirATransaccionRVMarketData(listaTransaccionesRV);
		if(!listaTransacciones.isEmpty()) {
			transaccion = listaTransacciones.get(0);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.buscarTransaccion tiempo: " + (fin - inicio));
		return transaccion;
	}
	
	private boolean ignorarModificacion (TransaccionRVMarketData transaccionAnterior, TransaccionRV_Proceso transaccionActual){
		logger.trace("INICIO::ControlMercadoMarketDataPalosRV.ignorarModificacion");
		long inicio = System.currentTimeMillis();
		
		boolean soloVentaCorta = false;
		
		//Si todos los campos a excepcion de shortSell y timeStamp son iguales debe ignorar la modificacion
		if(transaccionAnterior.codTicker.equalsIgnoreCase(transaccionActual.codTicker) && transaccionAnterior.action.equalsIgnoreCase(transaccionActual.action)
			&& transaccionAnterior.tradeTime.equalsIgnoreCase(transaccionActual.tradeTime) && transaccionAnterior.instrument.equalsIgnoreCase(transaccionActual.instrument)
			&& transaccionAnterior.market.equalsIgnoreCase(transaccionActual.market) && transaccionAnterior.settlementCondition.equalsIgnoreCase(transaccionActual.settlementCondition)
			&& transaccionAnterior.orderType.equalsIgnoreCase(transaccionActual.orderType) && transaccionAnterior.indivisible.equalsIgnoreCase(transaccionActual.indivisible)
			&& transaccionAnterior.quantity.compareTo(transaccionActual.quantity) == 0 && transaccionAnterior.price.compareTo(transaccionActual.price) == 0
			&& transaccionAnterior.amount.compareTo(transaccionActual.amount) == 0 && transaccionAnterior.closingPrice.compareTo(transaccionActual.closingPrice) == 0
			&& transaccionAnterior.buyerBroker.equalsIgnoreCase(transaccionActual.buyerBroker) && transaccionAnterior.buyEnteringFirm.equalsIgnoreCase(transaccionActual.buyEnteringFirm)
			&& transaccionAnterior.buyEnteringTrader.equalsIgnoreCase(transaccionActual.buyEnteringTrader) && transaccionAnterior.sellerBroker.equalsIgnoreCase(transaccionActual.sellerBroker)
			&& transaccionAnterior.sellEnteringFirm.equalsIgnoreCase(transaccionActual.sellEnteringFirm) && transaccionAnterior.sellEnteringTrader.equalsIgnoreCase(transaccionActual.sellEnteringTrader)
			&& transaccionAnterior.priceBuyingQTY.compareTo(transaccionActual.priceBuyingQTY) == 0 && transaccionAnterior.priceBuyingPrice.compareTo(transaccionActual.priceBuyingPrice) == 0
			&& transaccionAnterior.priceSellingQty.compareTo(transaccionActual.priceSellingQty) == 0 && transaccionAnterior.priceSellingPrice.compareTo(transaccionActual.priceSellingPrice) == 0
			&& transaccionAnterior.codIsin.equalsIgnoreCase(transaccionActual.codIsin) && transaccionAnterior.securityType.equalsIgnoreCase(transaccionActual.securityType)
			&& transaccionAnterior.timeInForce.equalsIgnoreCase(transaccionActual.timeInForce) && transaccionAnterior.tradingSessionID.equalsIgnoreCase(transaccionActual.tradingSessionID)
			&& transaccionAnterior.entryType.equalsIgnoreCase(transaccionActual.entryType) && transaccionAnterior.priceExchange.equalsIgnoreCase(transaccionActual.priceExchange)
			&& transaccionAnterior.priceTime.equalsIgnoreCase(transaccionActual.priceTime)
			) {
			soloVentaCorta = true;
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosRV.ignorarModificacion tiempo: " + (fin - inicio));
		return soloVentaCorta;
	}
	
	private void actualizarControlMercadoTransaccionIgnorada (Connection con, TransaccionRV_Proceso transaccionConModificacionPorIgnorar) throws SQLException{
		logger.debug("INICIO::ControlMercadoMarketDataPalosRV.actualizarControlMercadoTransaccionIgnorada");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		transaccionConModificacionPorIgnorar.registroControlMercado.Procesado = "E";
		transaccionConModificacionPorIgnorar.registroControlMercado.Log = "Modificacion Transaccion Folio " + transaccionConModificacionPorIgnorar.tradeID +  " Ignorada";
		List<ControlMercado> listaUpdate = new ArrayList<>();
		listaUpdate.add(transaccionConModificacionPorIgnorar.registroControlMercado);
		ControlMercado transaccionAnulacionPorIgnorar = new ControlMercado();
		transaccionAnulacionPorIgnorar.IdMensaje1 = transaccionConModificacionPorIgnorar.registroControlMercado.IdMensaje1;
		transaccionAnulacionPorIgnorar.IdMensaje2 = transaccionConModificacionPorIgnorar.tradeID + "E";
		transaccionAnulacionPorIgnorar.Procesado = "E";
		transaccionAnulacionPorIgnorar.Log = transaccionConModificacionPorIgnorar.registroControlMercado.Log;
		listaUpdate.add(transaccionAnulacionPorIgnorar);
		sql.batch(con, SQL_ACTUALIZA_CONTROL_MERCADO,listaUpdate, ControlMercado.class);
		con.commit();

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosRV.actualizarControlMercadoTransaccionIgnorada tiempo: " + (fin - inicio));
	}
	
	public static void main(String[] args) {
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties");
            ControlMercadoPalosRVMME demo=new ControlMercadoPalosRVMME();
            do{
            	
                try {
//    				SQL.exec(con, "UPDATE \"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\" = 'N' WHERE \"IdMensaje1\" = '2026-06-30 17:15:19.4370000' AND \"IdMensaje2\" = '2002750';");

    				demo.logica(con,true);
                	con.commit();
                } catch (Throwable er) {
                    er.printStackTrace();
                    con.rollback();
                }
                Thread.sleep(20000);
            } while (true) ;

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
}