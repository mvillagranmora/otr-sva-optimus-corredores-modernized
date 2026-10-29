package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMovimientoCuentaInversion implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarMovimientoCuentaInversion.class);
	
	private final String SQL_INSERT_CONSULTA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarMovimientoCuentaInversion_InsertConsulta.sql");
	private final String SQL_DELETE_CONSULTA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarMovimientoCuentaInversion_DeleteConsulta.sql");
	private final String SQL_SELECT_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarMovimientoCuentaInversion_SelectConsulta.sql");
	private final String SQL_SELECT_MOVIMIENTO_HISTORICO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarMovimientoCuentaInversion_SelectConsultaHistorico.sql");
	
	public static class EntradaConsultarMovimientoCuentaInversion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Consulta> p_Consulta;
	}
	
	public static class Consulta {
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdEstado;
		public BigDecimal SaldoInicial;
		public String FechaDesde;
		public String FechaHasta;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String SaldoPorFechaEfectiva;
		public String IncluyeSaldo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarMovimientoCuentaInversion{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Movimiento> P_OUT_MOVIMIENTOS;
		
		public SalidaConsultarMovimientoCuentaInversion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_MOVIMIENTOS = new ArrayList<Movimiento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento {
		public Long IdCargoAbonoMovimiento;
		public Date FechaSistema;
		public Timestamp FechaIngresoRegistro;
		public Long IdCuentaInversion;
		public String CodigoCuentaInversion;
		public String NombreCuentaInversion;
		public Long IdCliente;
		public String NombreCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public String CodigoMoneda;
		public String NombreMoneda;
		public String SimboloMoneda;
		public Integer DecimalesMoneda;
		public String IdTipoMovimiento;
		public String TipoMovimiento;
		public String NombreTipoMovimiento;
		public String IdConcepto;
		public String NombreConcepto;
		public String OrigenConcepto;
		public String Descripcion;
		public String IdCondicionDeLiquidacion;
		public String CodigoCondicionDeLiquidacion;
		public String NombreCondicionDeLiquidacion;
		public Integer PlazoCondicionDeLiquidacion;
		public Date FechaEfectivaMovimiento;
		public BigDecimal Monto;
		public BigDecimal SaldoPorFechaEfectiva;
		public BigDecimal SaldoPorFechaMovimiento;
		public String IdModulo;
		public String IdTipoProducto;
		public String CodigoTipoProducto;
		public String NombreTipoProducto;
		public String IdTipoOperacion;
		public String CodigoTipoOperacion;
		public String NombreTipoOperacion;
		public String AccionTipoOperacion;
		public Long IdOperacion;
		public Timestamp FechaOperacion;
		public Date FechaLiquidacionOperacion;
		public Date FechaVencimientoOperacion;
		public String IdMonedaOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal TipoCambioOperacion;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public String NemotecnicoOriginal;
		public String EsCargoAbonoPorTraspaso;
		public String EsCargoAbonoPorTraspasoTraducido;
		public Long IdCargoAbonoRelacionadoTraspaso;
		public String NombreClienteRelacionado;
		public String CodigoPortafolioRelacionado;
		public String IdTipoComprobante;
		public String CodigoTipoComprobante;
		public String NombreTipoComprobante;
		public Long IdComprobante;
		public String FolioComprobante;
		public String EsVentaCorta;
		public Timestamp FechaComprobante;
		public Long IdComprobanteDetalle;
		public Long IdPersonaPagoCobro;
		public String EsCertificadoAutomatico;
		public Long IdUsuarioCertificador;
		public Timestamp FechaCertificacion;
		public Long IdContraparte;
		public String EsIndicadorBilateral;
		public Long IdEmpresa;
		public String CodigoEmpresa;
		public String NombreEmpresa;
		public String EsIngresoManual;
		public Long IdEvento;
		public Long IdUsuario;
		public String NombreUsuario;
		public String IdEstado;
		public String NombreEstado;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public BigDecimal SaldoSimulado;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String NombreUsuarioCert;
		public Long IdCarga;
		public String IdEstadoSII;
		public Long IdRespuestaDTE;
		public BigDecimal MayorMenorValor;
		public BigDecimal MontoRetencionGananciaCap;
		
		public Movimiento() {
			this.IdEstado = "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarMovimientoCuentaInversion jin = gson.fromJson(in.toString(), EntradaConsultarMovimientoCuentaInversion.class);
		SalidaConsultarMovimientoCuentaInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarMovimientoCuentaInversion logica(Connection con, EntradaConsultarMovimientoCuentaInversion entrada) throws Exception {
		logger.info("INICIO::ConsultarMovimientoCuentaInversion");
		SalidaConsultarMovimientoCuentaInversion out = new SalidaConsultarMovimientoCuentaInversion();
		String consultarMovimientosHistorico = "0001-01-01";
		List<Movimiento> movimientos = null;
		List<Consulta> consultasMovHist = new ArrayList<Consulta>();
		List<Consulta> consultasMov = new ArrayList<Consulta>();
		Consulta cHist = new Consulta();
		boolean esHibrida = false;
		boolean incluyeSaldo = false;
		
		try {
			consultarMovimientosHistorico = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaGoLive");
			Fecha.toDate(consultarMovimientosHistorico);
		}
		catch(Exception ex) {
			consultarMovimientosHistorico = "0001-01-01";
		}
		
		try {
			for (Consulta c:entrada.p_Consulta) {
				if (c.IncluyeSaldo.equals("S")) incluyeSaldo = true; 
				if((Fecha.toDate(consultarMovimientosHistorico).after(Fecha.toDate(c.FechaDesde))) && (Fecha.toDate(consultarMovimientosHistorico).after(Fecha.toDate(c.FechaHasta)))){
					//fechaDesde < consultarMovimientosHistorico && fechaHasta < consultarMovimientosHistorico; traer historico
					consultasMovHist.add(c);
					insertarConsultaTablaTemporal(con, consultasMovHist);
					movimientos = obtenerMovimientosHistorico(con, incluyeSaldo);
					SQL.exec(con, SQL_DELETE_CONSULTA);
					out.P_OUT_MOVIMIENTOS.addAll(movimientos);
				}else if ((Fecha.toDate(consultarMovimientosHistorico).after(Fecha.toDate(c.FechaDesde))) && ((Fecha.toDate(c.FechaHasta).compareTo(Fecha.toDate(consultarMovimientosHistorico)))>=0)){
					//fechaDesde < consultarMovimientosHistorico && consultarMovimientosHistorico < fechaHasta; join
					Utils.copyPojo(c, cHist);
					
					c.FechaDesde = consultarMovimientosHistorico;
					consultasMov.add(c);
					
					consultarMovimientosHistorico = obtenerDiaAnterior(con, (Fecha.toDate(consultarMovimientosHistorico)));
					cHist.FechaHasta = consultarMovimientosHistorico;
					consultasMovHist.add(cHist);
					esHibrida = true;
					
					insertarConsultaTablaTemporal(con, consultasMovHist);
					movimientos = obtenerMovimientosHistorico(con, incluyeSaldo);
					SQL.exec(con, SQL_DELETE_CONSULTA);
					out.P_OUT_MOVIMIENTOS.addAll(movimientos);
					
					if (movimientos.size()>0) {
						for (Consulta cm:consultasMov) {
							cm.SaldoInicial = movimientos.get(1).SaldoSimulado; //saldo final historico 
						}
						if (out.P_OUT_MOVIMIENTOS.size() > 0) {
							out.P_OUT_MOVIMIENTOS.remove(1);
						}
					}
				}else {
					consultasMov.add(c);
				}
			}
			
			insertarConsultaTablaTemporal(con, consultasMov);
			movimientos = obtenerMovimientos(con);
			SQL.exec(con, SQL_DELETE_CONSULTA);
			out.P_OUT_MOVIMIENTOS.addAll(movimientos);
			
			
		}catch(Exception e) {
			logger.error("ERROR::ConsultarMovimientoCuentaInversion", e);
			throw e;
		}
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_EXITOSA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		logger.info("FIN::ConsultarMovimientoCuentaInversion");
		return out;
	}
	
	private void insertarConsultaTablaTemporal(Connection con, List<Consulta> consulta) throws SQLException {
		logger.info("INICIO::ConsultarMovimientoCuentaInversion.insertarConsultaTablaTemporal");
		long inicioInsertVariablesProceso = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CONSULTA, consulta, Consulta.class);
		long finInsertVariablesProceso = System.currentTimeMillis();
		logger.info("ConsultarMovimientoCuentaInversion.insertarConsultaTablaTemporal::Tiempo SQL_INSERT_VARIABLESPROCESO: " + (finInsertVariablesProceso - inicioInsertVariablesProceso));
		logger.info("FIN::ConsultarMovimientoCuentaInversion.insertarConsultaTablaTemporal");
	}
	
	private List<Movimiento> obtenerMovimientos(Connection con) throws Exception{
		logger.info("INICIO::ConsultarMovimientoCuentaInversion.obtenerMovimientos");
		SQL sql = new SQL();
		long inicioSelectCartolaDeMovimiento = System.currentTimeMillis();
		List<Movimiento> cartolaDeMovimiento = sql.query(con, SQL_SELECT_MOVIMIENTO, new HashMap<String,Object>(), Movimiento.class);
		long finSelectCartolaDeMovimiento = System.currentTimeMillis();
		logger.info("Tiempo SQL_SELECT_CARTOLADEMOVIMIENTO: " + (finSelectCartolaDeMovimiento - inicioSelectCartolaDeMovimiento));
		logger.info("FIN::ConsultarMovimientoCuentaInversion.obtenerMovimientos");
		return cartolaDeMovimiento;
	}
	
	private List<Movimiento> obtenerMovimientosHistorico(Connection con, boolean incluyeSaldo) throws Exception{
		logger.info("INICIO::ConsultarMovimientoCuentaInversion.obtenerMovimientosHistorico");
		SQL sql = new SQL();
		Movimiento row = new Movimiento();
		long inicioSelectCartolaDeMovimiento = System.currentTimeMillis();
		List<Movimiento> cartolaDeMovimientoHistorico = sql.query(con, SQL_SELECT_MOVIMIENTO_HISTORICO, new HashMap<String,Object>(), Movimiento.class);
		
		if (incluyeSaldo) {
			if (!cartolaDeMovimientoHistorico.isEmpty()) {
				row.Descripcion = "Saldo Final";
				row.SaldoSimulado = cartolaDeMovimientoHistorico.get(cartolaDeMovimientoHistorico.size()-1).SaldoSimulado;
				cartolaDeMovimientoHistorico.add(0, row);
				row = new Movimiento();
				row.Descripcion = "Saldo Inicial";
				row.SaldoSimulado = cartolaDeMovimientoHistorico.get(1).SaldoSimulado.add(cartolaDeMovimientoHistorico.get(1).Cargo.subtract(cartolaDeMovimientoHistorico.get(1).Abono));
				cartolaDeMovimientoHistorico.add(0, row);
			}
		}
		
		long finSelectCartolaDeMovimiento = System.currentTimeMillis();
		logger.info("Tiempo SQL_SELECT_CARTOLADEMOVIMIENTO_HISTORICO: " + (finSelectCartolaDeMovimiento - inicioSelectCartolaDeMovimiento));
		logger.info("FIN::ConsultarMovimientoCuentaInversion.obtenerMovimientosHistorico");
		return cartolaDeMovimientoHistorico;
	}
	
	private String obtenerDiaAnterior(Connection con, Date fechaOperacion) throws Exception {
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		entradaObtenerDiaAnterior.diaHabil = "N";

		InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
		fecha.fechaInicial = dateFormat.format(fechaOperacion);
		fecha.numeroDias = 1L;

		entradaObtenerDiaAnterior.fechas.add(fecha);
		OutObtenerDiaAnterior salidaObtenerDiaAnterior = obtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);
		return salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	}
	
}
