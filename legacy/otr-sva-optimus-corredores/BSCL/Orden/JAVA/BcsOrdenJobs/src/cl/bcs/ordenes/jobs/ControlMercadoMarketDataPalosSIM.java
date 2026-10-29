package cl.bcs.ordenes.jobs;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.AnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionSIMMarketData_Entrada;
import cl.bcs.ordenes.ModificarTransaccionMarketData;
import cl.bcs.ordenes.ModificarTransaccionMarketData.EntradaModificarTransaccionMarketData;
import cl.bcs.ordenes.ModificarTransaccionMarketData.SalidaModificarTransaccionMarketData;
import cl.bcs.ordenes.ModificarTransaccionMarketData.TransaccionSIM_ProcesoModificacion;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionSIMMarketData;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosSIM extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoMarketDataPalosSIM.class);
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosSIM_SelectTransacciones.sql");
	private final String SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosSIM_SelectTransaccionesOtrosCorredores.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosSIM_SelectTransaccionesAnular.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	
	public static class TransaccionParaAnular {
		public Long IdTransaccion;
		public String Folio;
		public Date FechaSistemaCreacion;
		public TransaccionSIM_Proceso referencia;
	}
	
	public static class TransaccionSIM_Proceso extends TransaccionSIMMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
	}
	
	public static class Usuario {
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean logica(Connection conn,boolean debug) throws Exception {
		
		logger.info("Inicio Ciclo MarketData SIM");
		
		Long idEmpresa = 0L;
		
        if (debug) {
        	idEmpresa = 1L;
        } else{
        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        }
        
		limpiezaTransaccionesNoEncontradas(conn);
        limpiezaTransaccionesOtrosCorredores(conn);
		
        //real start
        SQL _sql = new SQL();
        HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
		Long idUsuario = usuarioSistema.get(0).IdUsuario;
		
		List<TransaccionSIM_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String,Object>(), TransaccionSIM_Proceso.class);
		logger.debug(_transacciones.size() + " nuevos mensajes.");
		if (_transacciones.size() == 0){
			return true;
		}
		
		for (TransaccionSIM_Proceso tran : _transacciones) {
			
			tran.registroControlMercado.Log = "";
			tran.registroControlMercado.Procesado = "N";
			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			
		}
		
		String action = null;
		String folio = "";
		List<TransaccionSIM_Proceso> transaccionesProcesar = new ArrayList<>();
		for (TransaccionSIM_Proceso tx : _transacciones) {
			if (action == null) {
				action = tx.action;
			}
			
			if (action != null && !action.equalsIgnoreCase(tx.action)) {
				break;					
			}
			
			if (folio.equalsIgnoreCase(tx.folio)) {
				break;
			}
			
			folio = tx.folio;
			
			transaccionesProcesar.add(tx);
		}
		
		if ("S".equalsIgnoreCase(action) || "I".equalsIgnoreCase(action)) {
			crearTransacciones(conn,transaccionesProcesar,idEmpresa,idUsuario);
		} 
		else if ("E".equalsIgnoreCase(action)) {
			anularTransacciones(conn,transaccionesProcesar,idEmpresa,idUsuario);
		}
		else if("M".equalsIgnoreCase(action)) {
			modificarTransacciones(conn,transaccionesProcesar,idEmpresa,idUsuario);
		}
		else {
			limpiarTransaccionAccionNoEncontrada(conn, transaccionesProcesar, idEmpresa, idUsuario);
		}
		
		logger.info("Fin Ciclo MarketData SIM");
		
		return true;
	}
	
	public void limpiezaTransaccionesNoEncontradas(Connection conn) throws SQLException {
		//start
		SQL _sql = new SQL();
		List<TransaccionSIM_Proceso> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String,Object>(), TransaccionSIM_Proceso.class);
		if (transacciones.size() == 0){
			return;
		}
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
        for (TransaccionSIM_Proceso tran : transacciones) {
			
			
			if (tran.action.equalsIgnoreCase("")) {
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
	
	public void crearTransacciones(Connection conn, List<TransaccionSIM_Proceso> transacciones, Long idEmpresa,Long idUsuario) throws Exception {
		CrearTransaccionesMarketData_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketData_Entrada();
		SQL _sql = new SQL();
		
		entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
		for (TransaccionSIM_Proceso tran : transacciones) {
			
			TransaccionSIMMarketData_Entrada tx = new TransaccionSIMMarketData_Entrada();
			Utils.copyPojo(tran, tx);
		
			entradaCrearTransaccionesMarketData.transaccionesSIM.add(tx);
			
		}
		
		CrearTransaccionesMarketData serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketData();
		CrearTransaccionesMarketData_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);
		
		for (TransaccionSIM_Proceso tran : transacciones) {
			
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
					tran.registroControlMercado.Log+=msj.descripcion+"\n";
					logger.debug(msj.descripcion);
				}
				
			}
    	}
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		for (TransaccionSIM_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
		}
	}
	
	public void anularTransacciones(Connection conn, List<TransaccionSIM_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws Exception {

		
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		SQL _sql = new SQL();
		
		List<String> llavesAnulacion = new ArrayList<>();
		for (TransaccionSIM_Proceso tx : transacciones ) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.transactionDay)));
			llavesAnulacion.add(tx.folio+fechaSistema);
		}
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesXAnular = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesAnular = new ArrayList<>();
		boolean found = false;
		for (TransaccionSIM_Proceso tx : transacciones) {
			found = false;
			for (TransaccionParaAnular txa : transaccionesXAnular) {
				if (tx.folio.equalsIgnoreCase(txa.Folio) && sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.transactionDay))).equalsIgnoreCase(Fecha.toStringDate(txa.FechaSistemaCreacion))) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.transactionDay)));
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = "ANULACION POR SISTEMA BURSATIL";
					e.Observaciones = "";
					transaccionesAnular.add(e);
					txa.referencia = tx;
					found = true;
				}
			}
			
			if (!found) {
				//no encontrado
				tx.registroControlMercado.Procesado = "E";
				tx.registroControlMercado.Log = "Transaccion a eliminar, no encontrada, tradeID:" + tx.folio + " tradeTime:" + tx.transactionDay;
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
		for (MensajeSalida msj : salidaAnularTransacciones.P_OUT_MENSAJES) {
			for (TransaccionParaAnular txa : transaccionesXAnular) {
				if (txa.IdTransaccion.equals(msj.id)) {
					if (msj.tipo == MensajeSalida.MENSAJE_OK) {
						txa.referencia.registroControlMercado.Procesado = "S";
						txa.referencia.registroControlMercado.Log+= msj.descripcion + " " + msj.id+" ";
						
					}
					else {
						txa.referencia.registroControlMercado.Procesado = "E";
						txa.referencia.registroControlMercado.Log+= msj.descripcion + " " + msj.id+" ";
						
					}
					break;
				}
			}
		}
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		for (TransaccionSIM_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
		}
	}
	
	private void limpiezaTransaccionesOtrosCorredores(Connection conn) throws SQLException {
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		SQL _sql = new SQL();
		List<TransaccionSIM_Proceso> transaccionesOtrosCorredores = _sql.query(conn, SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES, new HashMap<String,Object>(), TransaccionSIM_Proceso.class);
		logger.debug(transaccionesOtrosCorredores.size() + " transacciones otros corredores detectadas.");
		for (TransaccionSIM_Proceso tran : transaccionesOtrosCorredores) {
			
			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			tran.registroControlMercado.Procesado = "E";
			if (tran.folio != null) {
				tran.registroControlMercado.Log = "Transaccion otros corredores Folio:" + tran.folio + " Fecha:" + tran.transactionDay+ " C:" + tran.buyExecutingFirm + " V:" + tran.sellExecutingFirm;	
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
	
	private void modificarTransacciones (Connection conn, List<TransaccionSIM_Proceso> transaccionesOriginales, Long idEmpresa, Long idUsuario) throws Exception {
		logger.trace("INICIO::ControlMercadoMarketDataPalosSIM.modificarTransacciones");
		long inicio = System.currentTimeMillis();
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
		EntradaModificarTransaccionMarketData entradaModificarTransaccionMarketData = new EntradaModificarTransaccionMarketData();
		entradaModificarTransaccionMarketData.p_IdEmpresa = idEmpresa;
		entradaModificarTransaccionMarketData.p_IdUsuario = idUsuario;
		copiarTransaccionAModificar(transaccionesOriginales, entradaModificarTransaccionMarketData.transaccionesSIM);
		
		ModificarTransaccionMarketData modificarTransaccionMarketData = new ModificarTransaccionMarketData();
		SalidaModificarTransaccionMarketData salidaModificarTransaccionMarketData = modificarTransaccionMarketData.logica(conn, entradaModificarTransaccionMarketData);
		
		for(TransaccionSIM_Proceso tran: transaccionesOriginales) {
			if(salidaModificarTransaccionMarketData.P_OUT_MENSAJES.size() > 0) {
				MensajeSalida mensaje = salidaModificarTransaccionMarketData.P_OUT_MENSAJES.stream().filter(t -> t.id.compareTo(tran.IdInterno) == 0).findFirst().orElse(null);
				if(mensaje != null) {
					if(MensajeSalida.MENSAJE_OK.equalsIgnoreCase(mensaje.tipo)) {
						tran.registroControlMercado.Procesado = "S";
						tran.registroControlMercado.Log+= mensaje.descripcion + " Folio: " +tran.folio + " Fecha:" + tran.transactionDay;
					}else {
						tran.registroControlMercado.Procesado = "E";
						tran.registroControlMercado.Log+= mensaje.descripcion;
					}
				}
				else {
					tran.registroControlMercado.Procesado = "E";
					tran.registroControlMercado.Log = "Transaccion a modificar, no encontrada, tradeID:" + tran.folio + " tradeTime:" + tran.transactionDay;
				}
			}
			else {
				tran.registroControlMercado.Procesado = "E";
				tran.registroControlMercado.Log = "Transaccion a modificar, no encontrada, tradeID:" + tran.folio + " tradeTime:" + tran.transactionDay;
			}
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		actualizarControlMercado(conn, updateControlMercado);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosSIM.modificarTransacciones tiempo: " + (fin - inicio));
		
	}
	
	private void limpiarTransaccionAccionNoEncontrada (Connection conn, List<TransaccionSIM_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws SQLException {
		logger.trace("INICIO::ControlMercadoMarketDataPalosSIM.limpiarTransaccionAccionNoEntrada");
		long inicio = System.currentTimeMillis();
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		
		for(TransaccionSIM_Proceso tran: transacciones) {
			
			tran.registroControlMercado.Procesado = "E";
			tran.registroControlMercado.Log = "Error tipo de accion no valida, accion:"  + tran.action + " - " + " Folio: " + tran.folio + " tradeTime: " + tran.transactionDay;
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		actualizarControlMercado(conn, updateControlMercado);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosSIM.limpiarTransaccionAccionNoEntrada tiempo: " + (fin - inicio));
	}
	
	private void actualizarControlMercado (Connection conn, List<ControlMercado> updateControlMercado) throws SQLException {
		logger.debug("INICIO::ControlMercadoMarketDataPalosSIM.actualizarControlMercado");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		
		if (!updateControlMercado.isEmpty()) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
    		conn.commit();
        }
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ControlMercadoMarketDataPalosSIM.actualizarControlMercado tiempo: " + (fin - inicio));
	}
	
	private void copiarTransaccionAModificar(List<TransaccionSIM_Proceso> transaccionesOriginales, List<TransaccionSIM_ProcesoModificacion> transaccionAModificar) {
		logger.trace("INICIO::ControlMercadoMarketDataPalosSIM.limpiarTransaccionAccionNoEntrada");
		long inicio = System.currentTimeMillis();
		
		for(TransaccionSIM_Proceso transaccion : transaccionesOriginales) {
			TransaccionSIM_ProcesoModificacion tranMod = new TransaccionSIM_ProcesoModificacion();
			
			tranMod._IdMensaje1			= transaccion._IdMensaje1;
			tranMod._IdMensaje2			= transaccion._IdMensaje2;
			tranMod._TipoMensaje		= transaccion._TipoMensaje;
			tranMod.action				= transaccion.action;
			tranMod.amount				= transaccion.amount;
			tranMod.bestStance			= transaccion.bestStance;
			tranMod.buyClientID			= transaccion.buyClientID;
			tranMod.buyEnteringFirm		= transaccion.buyEnteringFirm;
			tranMod.buyEnteringTrader	= transaccion.buyEnteringTrader;
			tranMod.buyExecutingFirm	= transaccion.buyExecutingFirm;
			tranMod.codIsin				= transaccion.codIsin;
			tranMod.divisible			= transaccion.divisible;
			tranMod.expirationDate		= transaccion.expirationDate;
			tranMod.folio				= transaccion.folio;
			tranMod.forwardPrice		= transaccion.forwardPrice;
			tranMod.IdInterno			= transaccion.IdInterno;
			tranMod.maxRate				= transaccion.maxRate;
			tranMod.modified			= transaccion.modified;
			tranMod.orderID				= transaccion.orderID;
			tranMod.prepaid				= transaccion.prepaid;
			tranMod.pubRate				= transaccion.pubRate;
			tranMod.qty					= transaccion.qty;
			tranMod.securityExchange	= transaccion.securityExchange;
			tranMod.securityType		= transaccion.securityType;
			tranMod.sellClientID		= transaccion.sellClientID;
			tranMod.sellEnteringFirm	= transaccion.sellEnteringFirm;
			tranMod.sellEnteringTrader	= transaccion.sellEnteringTrader;
			tranMod.sellExecutingFirm	= transaccion.sellExecutingFirm;
			tranMod.settlementType		= transaccion.settlementType;
			tranMod.spotPrice			= transaccion.spotPrice;
			tranMod.symbol				= transaccion.symbol;
			tranMod.transactionDay		= transaccion.transactionDay;
			tranMod.transactionTime		= transaccion.transactionTime;
			
			transaccionAModificar.add(tranMod);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ControlMercadoMarketDataPalosSIM.limpiarTransaccionAccionNoEntrada tiempo: " + (fin - inicio));
	}
	
	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
            // ---------------------------------
            try {
            	ControlMercadoMarketDataPalosSIM demo=new ControlMercadoMarketDataPalosSIM();
            	demo.logica(con,true);
                con.commit();
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
