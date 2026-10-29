package cl.bcs.cuentaInversion.webservices;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion.EntradaConsultarMovimientoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion.SalidaConsultarMovimientoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMovimientos implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarMovimientos.class);
	
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/webservices/sql/ConsultarMovimientos_SelectCuentaInversion.sql");
	
	public static class EntradaConsultarMovimientos{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdCliente;
		public String p_FechaInicioConsulta;
		public String p_FechaTerminoConsulta;
		public String p_IdMoneda;
		public String p_IdEstado;
		public String p_Origen;
		public String p_Instrumento;
		public String p_TipoOperacion;
		public Long p_IdMatrizAgrupador;
		public String p_EsADC;
		public List<Long> p_IdPortafolios;
		public String p_TipoFechaConsulta; // Valores posibles: "ADQ" o "LIQ" o ""
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarMovimientos{
		public List<Movimiento> P_OUT_RESULTADO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConsultarMovimientos() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Movimiento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento{
		public String FechaEfectiva;
		public String FechaMovimiento;
		public String Concepto;
		public String EsTransferencia;
		public String Moneda;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public String TipoComprobante;
		public String SecuenciaComprobante;
		public String FechaComprobante;
		public String TipoProducto;
		public String TipoOperacion;
		public String FechaOperacion;
		public String FechaVencimiento;
		public Long SecuenciaOperacion;
		public BigDecimal MontoOperacion;
		public Long SecuenciaMovimiento;
		public Long SecuenciaMovimientoTransferencia;
		public String NombreClienteRelacionado;
		public String CodigoPortafolioRelacionado;
		public String IngresadoPor;
		public String FechaDeCertificacion;
		public String CertificadoPor;
		public String Descripcion;
		public String Estado;
		public String MotivoAnulacion;
		public String Autorizacion;
		public String EstadoAutorizacion;
		public String FechaLiquidacion;
		public String FolioComprobante;
		public BigDecimal Cantidad;
		public BigDecimal Saldo;
		public Long IdInstrumento;
		public String NombreCliente;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdConcepto;
		public String IdEstado;
		public String NombreTipoMovimiento;
		public BigDecimal SaldoSimulado;
		public String CodigoPortafolio;
		public Long IdComprobante;
		public Long IdRespuestaDTE;
		public String RespuestaPdfDTE;
		public String DocumentoIdentidad;
		public String Origen;
		public Long IdMatrizAgrupador;
		public String NemotecnicoOriginal;
		public String EsADC;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MatrizAgrupador{
		public Long IdMatrizAgrupadorAPV;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaInversion {
		public Long IdCuentaInversion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdEmpleadoEjecutivo;
		public String IdMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	// ---------------------------------------------------------------------
	// CALL
	// ---------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarMovimientos jin = gson.fromJson(in.toString(), EntradaConsultarMovimientos.class);
		SalidaConsultarMovimientos out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	// ---------------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------------
	public SalidaConsultarMovimientos logica(Connection con, EntradaConsultarMovimientos entrada) throws Exception {
		logger.info("INICIO::SalidaConsultarMovimientos");
		SalidaConsultarMovimientos out = new SalidaConsultarMovimientos();
		List<CuentaInversion> cuentasInversion = obtenerCuentaInversion(con,entrada.p_IdPortafolios,entrada.p_IdMoneda);
		SalidaSaldoCuentaInversion saldosCuentaInversion = obtenerSaldoCuentaInversion(con,entrada,cuentasInversion);
		SalidaConsultarMovimientoCuentaInversion salidaConsultaMovimientoCuentaInversion = obtenerMovimientosCuentaInversion(con,entrada,
				cuentasInversion,saldosCuentaInversion
				,entrada.p_TipoFechaConsulta);
		
		int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
		Fecha.setLocalOffSet(SegundosZonaHoraria);
		
		out.P_OUT_RESULTADO = obtenerSalida(con,entrada,salidaConsultaMovimientoCuentaInversion.P_OUT_MOVIMIENTOS,cuentasInversion);
		
		
		logger.info("FIN::SalidaConsultarMovimientos");
		return out;
	}
	
	
	
	private List<CuentaInversion> obtenerCuentaInversion(Connection con, List<Long> portafolios, String idMoneda) throws Exception{
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerCuentaInversion");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", portafolios);
		params.put("IdMoneda", idMoneda);
		List<CuentaInversion> cuentaInversion = sql.query(con, SQL_SELECT_CUENTA_INVERSION, params, CuentaInversion.class);
		logger.info("FIN::SalidaConsultarMovimientos.obtenerCuentaInversion");
		return cuentaInversion;
	}
	
	
	
	private SalidaSaldoCuentaInversion obtenerSaldoCuentaInversion(Connection con, EntradaConsultarMovimientos entrada, List<CuentaInversion> cuentasInversion) throws Exception {
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerSaldoCuentaInversion");
		EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
		entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaSaldoCuentaInversion.p_FechaDesde = entrada.p_FechaInicioConsulta;
		entradaSaldoCuentaInversion.p_FechaHasta = entrada.p_FechaTerminoConsulta;
		entradaSaldoCuentaInversion.p_SoloSaldoNegativo = "N";
		entradaSaldoCuentaInversion.p_CuentaInversionAConsultar = cuentasInversion.parallelStream().map(cuentaInversion->{
			InCuentaInversionConsultar inCuentaInversion = new InCuentaInversionConsultar();
			inCuentaInversion.IdPortafolio = cuentaInversion.IdPortafolio;
			inCuentaInversion.IdMoneda = cuentaInversion.IdMoneda;
			return inCuentaInversion;
		}).collect(Collectors.toList());
		SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
		SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
		logger.info("FIN::SalidaConsultarMovimientos.obtenerSaldoCuentaInversion");
		return salidaSaldoCuentaInversion;
	}
	
	
	
	private SalidaConsultarMovimientoCuentaInversion obtenerMovimientosCuentaInversion(Connection con, EntradaConsultarMovimientos entrada, List<CuentaInversion> cuentasInversion, 
			SalidaSaldoCuentaInversion saldosCuentaInversion, String p_TipoFechaConsulta) throws Exception {
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerMovimientosCuentaInversion");
		Map<Long,SaldoInicialFinal> saldosInicialesFinales = saldosCuentaInversion.P_OUT_SALDOINICIALFINAL.parallelStream().collect(Collectors.toMap(saldo->saldo.IdCuentaInversion, saldo->saldo));
		EntradaConsultarMovimientoCuentaInversion entradaConsultarMovimientoCuentaInversion = new EntradaConsultarMovimientoCuentaInversion();
		entradaConsultarMovimientoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarMovimientoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarMovimientoCuentaInversion.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarMovimientoCuentaInversion.p_Consulta = cuentasInversion.parallelStream().map(cuentaInversion->{
			SaldoInicialFinal saldoFinal = saldosInicialesFinales.get(cuentaInversion.IdCuentaInversion);
			ConsultarMovimientoCuentaInversion.Consulta consulta = new ConsultarMovimientoCuentaInversion.Consulta();
			consulta.IdPortafolio = cuentaInversion.IdPortafolio;
			consulta.IdMoneda = cuentaInversion.IdMoneda;
			consulta.IdEstado = entrada.p_IdEstado;
			consulta.SaldoInicial = saldoFinal != null ? saldoFinal.SaldoEfectivoFinal : BigDecimal.ZERO;
			consulta.FechaDesde = entrada.p_FechaInicioConsulta;
			consulta.FechaHasta = entrada.p_FechaTerminoConsulta;
			consulta.IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
			consulta.EsADC = entrada.p_EsADC;
			
		
			consulta.SaldoPorFechaEfectiva = p_TipoFechaConsulta != null &&  p_TipoFechaConsulta.trim().equalsIgnoreCase("")  && p_TipoFechaConsulta.equalsIgnoreCase("LIQ") ? "S": "N";
			
			
			consulta.IncluyeSaldo = "N";
			
			return consulta;
		}).collect(Collectors.toList());
		
		ConsultarMovimientoCuentaInversion consultarMovimientoCuentaInversion = new ConsultarMovimientoCuentaInversion();
		SalidaConsultarMovimientoCuentaInversion salidaConsultarMovimientoCuentaInversion = consultarMovimientoCuentaInversion.logica(con, entradaConsultarMovimientoCuentaInversion);
		logger.info("FIN::SalidaConsultarMovimientos.obtenerMovimientosCuentaInversion");
		return salidaConsultarMovimientoCuentaInversion;
	}
	
	private List<Movimiento> obtenerSalida(Connection con, EntradaConsultarMovimientos entrada, List<ConsultarMovimientoCuentaInversion.Movimiento> movimientos, List<CuentaInversion> cuentasInversion){
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerSalida");							
		Map<Long,CuentaInversion> mapCuentaInversionCodigoPortafolio= cuentasInversion.parallelStream().collect(Collectors.toMap(cuentaInversion->cuentaInversion.IdCuentaInversion, cuentaInversion->cuentaInversion));
		List<Movimiento> movimientosFiltrados = movimientos.parallelStream()
				.map(movimientoCuentaInversion->{
					CuentaInversion cuentaInversion = mapCuentaInversionCodigoPortafolio.get(movimientoCuentaInversion.IdCuentaInversion);
					Movimiento movimiento = new Movimiento();
					movimiento.FechaEfectiva = movimientoCuentaInversion.FechaEfectivaMovimiento != null ?Fecha.stringToSqlDate(movimientoCuentaInversion.FechaEfectivaMovimiento):"0001-01-01" ;
					// La FechaMovimiento debe corresponder a la fecha de adquisicion
					Integer zonaHoraria = 0;
					try {
						zonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);	
					} catch (Exception e1) {
						zonaHoraria = 0;
					}		
					String horaMovimiento = "";
					movimientoCuentaInversion.FechaCertificacion = Fecha.addSeconds(movimientoCuentaInversion.FechaCertificacion, (zonaHoraria) );
					try
					{					
						horaMovimiento = Fecha.toStringTime(Fecha.toTime(Fecha.toFormat(movimientoCuentaInversion.FechaCertificacion,"HH:mm:ss").toString()));
					}
					catch(Exception e)
					{
						horaMovimiento = "00:00:00";
					}
					
					movimiento.FechaMovimiento = movimientoCuentaInversion.FechaSistema != null ? Fecha.toStringDatetime(Fecha.toTimeStamp(movimientoCuentaInversion.FechaSistema.toString()+" "+ horaMovimiento)):"0001-01-01" ;
					

					
					
					
					movimiento.Concepto = movimientoCuentaInversion.NombreConcepto;
					movimiento.EsTransferencia = movimientoCuentaInversion.EsCargoAbonoPorTraspaso;
					movimiento.Moneda = movimientoCuentaInversion.IdMoneda;
					movimiento.Cargo = movimientoCuentaInversion.Cargo;
					movimiento.Abono = movimientoCuentaInversion.Abono;
					movimiento.TipoComprobante = movimientoCuentaInversion.NombreTipoComprobante;
					movimiento.SecuenciaComprobante = movimientoCuentaInversion.FolioComprobante;
					movimiento.FechaComprobante = movimientoCuentaInversion.FechaComprobante != null ?Fecha.toFormat(movimientoCuentaInversion.FechaComprobante,"yyyy-MM-dd HH:mm:ss"): "0001-01-01" ;
					movimiento.TipoProducto = movimientoCuentaInversion.NombreTipoProducto;
					movimiento.TipoOperacion = movimientoCuentaInversion.NombreTipoOperacion;
					movimiento.FechaOperacion = movimientoCuentaInversion.FechaOperacion != null ? Fecha.toFormat(movimientoCuentaInversion.FechaOperacion,"yyyy-MM-dd HH:mm:ss"):"0001-01-01" ;
					movimiento.FechaVencimiento = movimientoCuentaInversion.FechaVencimientoOperacion != null ? Fecha.stringToSqlDate(movimientoCuentaInversion.FechaVencimientoOperacion): "0001-01-01" ;
					movimiento.SecuenciaOperacion = movimientoCuentaInversion.IdOperacion;
					movimiento.MontoOperacion = movimientoCuentaInversion.MontoOperacion;
					movimiento.SecuenciaMovimiento = movimientoCuentaInversion.IdCargoAbonoMovimiento;
					movimiento.SecuenciaMovimientoTransferencia = movimientoCuentaInversion.IdCargoAbonoRelacionadoTraspaso;
					movimiento.NombreClienteRelacionado = movimientoCuentaInversion.NombreClienteRelacionado;
					movimiento.CodigoPortafolioRelacionado = movimientoCuentaInversion.CodigoPortafolioRelacionado;
					movimiento.IngresadoPor = movimientoCuentaInversion.NombreUsuario;
					movimiento.FechaDeCertificacion = movimientoCuentaInversion.FechaCertificacion != null ?Fecha.toFormat(movimientoCuentaInversion.FechaCertificacion,"yyyy-MM-dd HH:mm:ss"): "0001-01-01";
					movimiento.CertificadoPor = movimientoCuentaInversion.NombreUsuarioCert;
					movimiento.Descripcion = movimientoCuentaInversion.Descripcion;
					movimiento.Estado = movimientoCuentaInversion.NombreEstado;
					movimiento.MotivoAnulacion = "";
					movimiento.Autorizacion = "";
					movimiento.EstadoAutorizacion = "";
					movimiento.FechaLiquidacion = movimientoCuentaInversion.FechaLiquidacionOperacion != null ? Fecha.stringToSqlDate(movimientoCuentaInversion.FechaLiquidacionOperacion): "0001-01-01";
					movimiento.FolioComprobante = movimientoCuentaInversion.FolioComprobante;
					movimiento.Cantidad = movimientoCuentaInversion.Cantidad;
					movimiento.Saldo = movimientoCuentaInversion.SaldoPorFechaEfectiva;
					movimiento.IdInstrumento = movimientoCuentaInversion.IdInstrumento;
					movimiento.NombreCliente = movimientoCuentaInversion.NombreCliente;
					movimiento.IdCliente = movimientoCuentaInversion.IdCliente;
					movimiento.IdPortafolio = movimientoCuentaInversion.IdPortafolio;
					movimiento.IdConcepto = movimientoCuentaInversion.IdConcepto;
					movimiento.IdEstado = movimientoCuentaInversion.IdEstado;
					movimiento.NombreTipoMovimiento = movimientoCuentaInversion.NombreTipoMovimiento;
					movimiento.SaldoSimulado = movimientoCuentaInversion.SaldoSimulado;
					movimiento.IdComprobante = movimientoCuentaInversion.IdComprobante;
					movimiento.IdRespuestaDTE = 0L;
					movimiento.RespuestaPdfDTE = "";
					movimiento.IdMatrizAgrupador = movimientoCuentaInversion.IdMatrizAgrupador;
					movimiento.NemotecnicoOriginal = movimientoCuentaInversion.NemotecnicoOriginal;
					movimiento.EsADC = movimientoCuentaInversion.EsADC;
					if(cuentaInversion != null) {
						movimiento.CodigoPortafolio = cuentaInversion.CodigoPortafolio;
						movimiento.DocumentoIdentidad = cuentaInversion.DocumentoIdentidad;
						movimiento.Origen = obtenerOrigen(cuentaInversion);
					}else {
						movimiento.CodigoPortafolio = "";
						movimiento.DocumentoIdentidad = "";
						movimiento.Origen = "";
					}
					return movimiento;
				}).collect(Collectors.toList());
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerSalida");
		return movimientosFiltrados;
	}
	
	private String obtenerOrigen(CuentaInversion cuentaInversion) {
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerOrigen");
		String codigoPortafolio = cuentaInversion != null ? cuentaInversion.CodigoPortafolio : "";
		String codigoPortafolioSplit = codigoPortafolio.split("-")[0];
		String origen = codigoPortafolioSplit.equals("APV") || codigoPortafolioSplit.equals("DC") ? "APV" : "NOAPV";
		logger.info("INICIO::SalidaConsultarMovimientos.obtenerOrigen");
		return origen;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("becbpro1","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();		
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1646\",\"p_IdModulo\":\"CTA\",\"p_IdCliente\":134979,\"p_FechaInicioConsulta\":\"2026-07-01\",\"p_FechaTerminoConsulta\":\"2026-07-31\",\"p_IdMoneda\":\"CLP\",\"p_IdEstado\":\"CRT\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_Origen\":null,\"p_Instrumento\":null,\"p_TipoOperacion\":null,\"p_IdMatrizAgrupador\":0,\"p_EsADC\":null,\"p_IdPortafolios\":[117199]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1646\",\"p_IdModulo\":\"CTA\",\"p_IdCliente\":134979,\"p_FechaInicioConsulta\":\"2026-08-01\",\"p_FechaTerminoConsulta\":\"2026-08-31\",\"p_IdMoneda\":\"CLP\",\"p_IdEstado\":\"CRT\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_Origen\":null,\"p_Instrumento\":null,\"p_TipoOperacion\":null,\"p_IdMatrizAgrupador\":0,\"p_EsADC\":null,\"p_IdPortafolios\":[117199]}";
			EntradaConsultarMovimientos Objin = gson.fromJson(json, EntradaConsultarMovimientos.class);
			ConsultarMovimientos  demo=new ConsultarMovimientos();				
			try {
				System.out.println(demo.logica(con, Objin));
			}
			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
    	}
		System.out.println("== END ==");
	}
}
