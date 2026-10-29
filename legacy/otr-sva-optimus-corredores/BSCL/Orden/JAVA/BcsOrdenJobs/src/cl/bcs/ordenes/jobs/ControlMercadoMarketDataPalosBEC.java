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
import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.InTransaccion;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.CrearTransaccionesMarketDataBEC_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.CrearTransaccionesMarketDataBEC_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.TransaccionBECMarketData_Entrada;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionBECMarketData;
import cl.bcs.ordenes.pojo.TransaccionRVMarketData;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosBEC extends BcsJob {

	private static final Logger logger = LogManager.getLogger(ControlMercadoMarketDataPalosBEC.class);

	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosBEC_SelectTransacciones.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES_ANULAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosBEC_SelectTransaccionesAnular.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercadoBEC.sql");
	private final String SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosBEC_SelectTransaccionesOtrosCorredores.sql");


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

	public static class TransaccionBEC_Proceso extends TransaccionBECMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Usuario {
		public Long IdUsuario;
	}

	public static class TransaccionParaAnular {
		public Long IdTransaccion;
		public String Folio;
		public Date FechaSistemaCreacion;
		public TransaccionBEC_Proceso referencia;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	
	private void limpiezaTransaccionesOtrosCorredores(Connection conn) throws SQLException {
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		SQL _sql = new SQL();
		List<TransaccionBEC_Proceso> transaccionesOtrosCorredores = _sql.query(conn, SQL_SELECT_TRANSACCIONES_OTROS_CORREDORES, new HashMap<String,Object>(), TransaccionBEC_Proceso.class);
		logger.debug(transaccionesOtrosCorredores.size() + " transacciones otros corredores detectadas.");
		for (TransaccionBEC_Proceso tran : transaccionesOtrosCorredores) {
			
			tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
			tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
			tran.registroControlMercado.Procesado = "E";
			if (tran.Folio > 0) {
				tran.registroControlMercado.Log = "Transaccion otros corredores Folio:" + tran.Folio + " Fecha:" + tran.FechaRegistro + " C:" + tran.BCSCodeComprador + " V:" + tran.BCSCodeVendedor;	
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

	public void crearTransacciones(Connection conn, List<TransaccionBEC_Proceso> transacciones, Long idEmpresa,
			Long idUsuario) throws Exception {
		CrearTransaccionesMarketDataBEC_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketDataBEC_Entrada();

		SQL _sql = new SQL();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");

		entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
		entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
		for (TransaccionBEC_Proceso tran : transacciones) {

			TransaccionBECMarketData_Entrada tx = new TransaccionBECMarketData_Entrada();

			tx.IdInterno = tran.IdInterno;
			tx.Accion = tran.Accion;
			tx.Anticipable = tran.Anticipable;
			tx.BCSCodeComprador = tran.BCSCodeComprador;
			tx.BCSCodeCompradorOpdir = tran.BCSCodeCompradorOpdir;
			tx.BCSCodeVendedor = tran.BCSCodeVendedor;
			tx.BCSCodeVendedorOpdir = tran.BCSCodeVendedorOpdir;
			tx.Board = tran.Board;
			tx.Cantidad = tran.Cantidad;
			tx.ClientIDCompra = tran.ClientIDCompra;
			tx.ClientIDVenta = tran.ClientIDVenta;
			tx.CompradorBCSTraderID = tran.CompradorBCSTraderID;
			tx.CompradorIdCorredor = tran.CompradorIdCorredor;
			tx.CompradorIdOpdir = tran.CompradorIdOpdir;
			tx.CompradorTraderID = tran.CompradorTraderID;
			tx.CompradorTraderIdOpdir = tran.CompradorTraderIdOpdir;
			tx.CompradorXStreamIdCorredor = tran.CompradorXStreamIdCorredor;
			tx.CompradorXStreamIdOpdir = tran.CompradorXStreamIdOpdir;
			tx.EmisorID = tran.EmisorID;
			tx.EmisorNombre = tran.EmisorNombre;
			tx.Fecha = tran.Fecha;
			tx.FechaLiquidacion = tran.FechaLiquidacion;
			tx.FechaRegistro = tran.FechaRegistro;
			tx.Folio = tran.Folio;
			tx.FolioRelacionado = tran.FolioRelacionado;
			tx.FolioXStream = tran.FolioXStream;
			tx.Generico = tran.Generico;
			tx.Grupo = tran.Grupo;
			tx.Hora = tran.Hora;
			tx.ID = tran.ID;
			tx.Instrumento = tran.Instrumento;
			tx.LugarPago = tran.LugarPago;
			tx.Mercado = tran.Mercado;
			tx.Moneda = tran.Moneda;
			tx.Monto = tran.Monto;
			tx.Plazo = tran.Plazo;
			tx.PlazoAAMM = tran.PlazoAAMM;
			tx.PlazoDias = tran.PlazoDias;
			tx.Precio = tran.Precio;
			tx.Reajustabilidad = tran.Reajustabilidad;
			tx.TipoInstrumentoRF = tran.TipoInstrumentoRF;
			tx.ValorPar = tran.ValorPar;
			tx.Variacion = tran.Variacion;
			tx.VendedorBCSTraderID = tran.VendedorBCSTraderID;
			tx.VendedorIdCorredor = tran.VendedorIdCorredor;
			tx.VendedorIdOpdir = tran.VendedorIdOpdir;
			tx.VendedorTraderID = tran.VendedorTraderID;
			tx.VendedorTraderIdOpdir = tran.VendedorTraderIdOpdir;
			tx.VendedorXStreamIdCorredor = tran.VendedorXStreamIdCorredor;
			tx.VendedorXStreamIdOpdir = tran.VendedorXStreamIdOpdir;
			tx.fondoCompra = tran.fondoCompra;
			tx.fondoVenta = tran.fondoVenta;
			tx.madurez = tran.madurez;
			tx.odAutomatica = tran.odAutomatica;
			tx.offshore = tran.offshore;
			tx.puntaCompraBolsa = tran.puntaCompraBolsa;
			tx.puntaCompraCantidad = tran.puntaCompraCantidad;
			tx.puntaCompraPrecio = tran.puntaCompraPrecio;
			tx.puntaVentaBolsa = tran.puntaVentaBolsa;
			tx.puntaVentaCantidad = tran.puntaVentaCantidad;
			tx.puntaVentaPrecio = tran.puntaVentaPrecio;
			tx.ventaCorta = tran.ventaCorta;

			entradaCrearTransaccionesMarketData.transacciones.add(tx);

		}

		CrearTransaccionesMarketDataBEC serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketDataBEC();
		CrearTransaccionesMarketDataBEC_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);

		for (TransaccionBEC_Proceso tran : transacciones) {

			for (cl.bcs.ordenes.CrearTransaccionesMarketDataBEC.Resultado_Salida resp : salidaCrearTransaccionesMarketData.P_OUT_RESULTADO) {

				if (tran.IdInterno.equals(resp.IdInterno) || (tran.FolioRelacionado != 0)) {
					
					tran.registroControlMercado.Procesado = "S";
					logger.debug("Registro " + tran.registroControlMercado.IdMensaje1 + " - "+ tran.registroControlMercado.IdMensaje2 + " procesado OK. ");
				}
			}
			
			for (TransaccionBEC_Proceso tran2 : transacciones) {
				if(tran.registroControlMercado.Procesado.equalsIgnoreCase("S") && tran.FolioRelacionado == tran2.Folio) {
					tran.registroControlMercado.Log = "Transaccion simultanea procesada OK: Folio:" + tran.Folio + ", Folio Relacionado: " + tran.FolioRelacionado;
					tran.registroControlMercado.Procesado = "S";
					logger.debug("Registro simultanea con Folio: " + tran.Folio + ", Folio Relacionado: " + tran.FolioRelacionado + " procesado OK. ");
				}
			}
			
			if (!tran.registroControlMercado.Procesado.equalsIgnoreCase("S")) {
				tran.registroControlMercado.Procesado = "E";
				logger.debug("Registro " + tran.registroControlMercado.IdMensaje1 + " - "
						+ tran.registroControlMercado.IdMensaje2 + " procesado ERROR");
			}

			for (MensajeSalida msj : salidaCrearTransaccionesMarketData.P_OUT_MENSAJES) {
				if (tran.IdInterno.equals(msj.id)) {
					tran.registroControlMercado.Log += msj.descripcion + "\n";
					logger.debug(msj.descripcion);
				}
			}
		}

		List<ControlMercado> updateControlMercado = new ArrayList<>();
		for (TransaccionBEC_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}

		 //ACTUALIZAR CONTROL MERCADO
		 if (updateControlMercado.size() > 0) {
			 _sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			 conn.commit();
		 }
	}
	
	public void anularTransacciones(Connection conn, List<TransaccionBEC_Proceso> transacciones, Long idEmpresa, Long idUsuario) throws Exception {

		
		SimpleDateFormat sdfTradeTimeIN = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdfTradeTimeOUT = new SimpleDateFormat("yyyy-MM-dd");
		SQL _sql = new SQL();
		
		List<String> llavesAnulacion = new ArrayList<>();
		for (TransaccionBEC_Proceso tx : transacciones ) {
			String fechaSistema = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.FechaRegistro)));
			llavesAnulacion.add(tx.Folio+fechaSistema);
		}
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("Llave", llavesAnulacion);
		List<TransaccionParaAnular> transaccionesXAnular = _sql.query(conn, SQL_SELECT_TRANSACCIONES_ANULAR, params, TransaccionParaAnular.class);

		List<InTransaccion> transaccionesAnular = new ArrayList<>();
		boolean found = false;
		for (TransaccionBEC_Proceso tx : transacciones) {
			found = false;
			
			if(tx.Mercado == 5 && tx.Plazo > 0) {
				tx.registroControlMercado.Procesado = "P";
				tx.registroControlMercado.Log = "Transaccion SIM a plazo descartada, solo se anula operacion a contado, tradeID:" + tx.Folio + " tradeTime:" + tx.FechaRegistro;
				continue;
			}
			
			for (TransaccionParaAnular txa : transaccionesXAnular) {
				if (String.valueOf(tx.Folio).equalsIgnoreCase(txa.Folio) && sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.FechaRegistro))).equalsIgnoreCase(Fecha.toStringDate(txa.FechaSistemaCreacion))) {
					InTransaccion e = new InTransaccion();
					e.FechaAnulacion = sdfTradeTimeOUT.format((sdfTradeTimeIN.parse(tx.FechaRegistro)));
					e.IdTransaccion = txa.IdTransaccion;
					e.Motivo = "ANULACION POR SISTEMA BEC";
					e.Observaciones = "";
					transaccionesAnular.add(e);
					txa.referencia = tx;
					found = true;
				}
			}
			
			if (!found) {
				//no encontrado
				tx.registroControlMercado.Procesado = "E";
				tx.registroControlMercado.Log = "Transaccion a eliminar, no encontrada, tradeID:" + tx.Folio + " tradeTime:" + tx.FechaRegistro;
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
		for (TransaccionBEC_Proceso tran : transacciones) {
			updateControlMercado.add(tran.registroControlMercado);
		}
		
		//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();
		}
	}

	public boolean logica(Connection conn, boolean debug) throws Exception {
		try {
			logger.info("Inicio Ciclo MarketData BEC");
			Long idEmpresa = 0L;

			if (debug) {
				idEmpresa = 1L;
			} else {
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			}

			// Limpiezas
//			limpiezaHistoricos(conn);
//	        limpiezaTransaccionesNoEncontradas(conn);
	        limpiezaTransaccionesOtrosCorredores(conn);
			
			SQL _sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			
			List<TransaccionBEC_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String, Object>(), TransaccionBEC_Proceso.class);
			logger.debug(_transacciones.size() + " nuevos mensajes.");
			if (_transacciones.size() == 0) {
				return true;
			}

			for (TransaccionBEC_Proceso tran : _transacciones) {
				
				tran.registroControlMercado.Log = "";
				tran.registroControlMercado.Procesado = "N";
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;

			}

			List<TransaccionBEC_Proceso> transaccionesProcesarIngreso = new ArrayList<>();
			List<TransaccionBEC_Proceso> transaccionesProcesarAnulacion = new ArrayList<>();
			
			for (TransaccionBEC_Proceso tx : _transacciones) {
				if (tx.Accion == 0) {
					transaccionesProcesarIngreso.add(tx);
				}

				if (tx.Accion == 1) {
					transaccionesProcesarAnulacion.add(tx);
				}
			}
	
			// Ingreso
			if (!transaccionesProcesarIngreso.isEmpty()) {
				crearTransacciones(conn, transaccionesProcesarIngreso, idEmpresa, idUsuario);
			}
			
			// Anulacion 
			if (!transaccionesProcesarAnulacion.isEmpty()) {
				anularTransacciones(conn, transaccionesProcesarAnulacion, idEmpresa, idUsuario);
			}
		
			logger.info("Fin Ciclo MarketData BEC");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return true;
	}

	public static void main(String[] args) {
		System.out.println("== START ==");
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("scotia","connections.properties");
			ControlMercadoMarketDataPalosBEC demo = new ControlMercadoMarketDataPalosBEC();
			SQL.exec(con, "UPDATE BSCL.\"BSCL.Orden.BD::ORD.ControlMercado\"\r\n"
					+ "	SET \"Procesado\"='N'\r\n"
					+ "	WHERE \"TipoMensaje\"='MARKET_DATA_PALO_BEC' AND \"IdMensaje1\"='2026-01-20 13:48:59.5250000' AND \"IdMensaje2\"='700598' ");

			// ---------------------------------
			try {
				demo.logica(con, true);
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