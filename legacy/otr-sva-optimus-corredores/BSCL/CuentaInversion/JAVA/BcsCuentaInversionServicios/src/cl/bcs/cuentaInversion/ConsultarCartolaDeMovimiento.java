package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion.EntradaConsultarMovimientoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion.SalidaConsultarMovimientoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.ErroresDetalle;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCartolaDeMovimiento implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConsultarCartolaDeMovimiento.class);
	private final String SQL_SELECT_PORTAFOLIOMONEDA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarCartolaDeMovimiento_SelectPortafolioMoneda.sql");
	private final String SQL_SELECT_NEMOSMOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarCartolaDeMovimiento_SelectNemosMovimiento.sql");
	
	public static class EntradaConsultarCartolaDeMovimiento {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdCuentaInversion;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdMoneda;
		public String p_IdEstado;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_Validaciones;
		public String p_SaldoPorFechaEfectiva;
		public Long p_IdProductoAgrupador;
		public Long p_IdMatrizAgrupador;
		public String p_EsADC;
		public String p_IncluyeSaldo;
		public String p_IncluyeAnulados;
		
		public EntradaConsultarCartolaDeMovimiento() {
			this.p_SaldoPorFechaEfectiva = "S";
			this.p_Validaciones = "S";
			this.p_IdProductoAgrupador = null;
			this.p_IdMatrizAgrupador = null;
			this.p_EsADC = null;
			this.p_IncluyeSaldo = "N";
			this.p_IncluyeAnulados = "S";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCartolaDeMovimiento {
		public List<CartolaDeMovimiento> P_OUT_CARTOLADEMOVIMIENTO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErroresDetalle> P_OUT_ERRORESDETALLE;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public SalidaConsultarCartolaDeMovimiento() {
			this.P_OUT_CARTOLADEMOVIMIENTO = new ArrayList<CartolaDeMovimiento>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErroresDetalle>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CartolaDeMovimiento {
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
		public String IdEstadoSII;
		public Long IdRespuestaDTE;
		public BigDecimal MayorMenorValor;
		public BigDecimal MontoRetencionGananciaCap;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class VariablesProceso {
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdEstado;
		public BigDecimal Saldo_Inicial;
		public String FechaDesde;
		public String FechaHasta;
		public Long IdMatrizAgrupador;
		public String EsADC;
		public String SaldoPorFechaEfectiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class NemosMovimientos{
		public Long IdComprobante;
		public String NemotecnicoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaConsultarCartolaDeMovimiento logica(Connection con, EntradaConsultarCartolaDeMovimiento entrada) throws Exception {
		logger.info("INICIO::ConsultarCartolaDeMovimiento");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarCartolaDeMovimiento salida = new SalidaConsultarCartolaDeMovimiento();
		SQL sql=new SQL();
		try {
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return salida;
			}
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("p_IdCuentaInversion", entrada.p_IdCuentaInversion);
			params.put("p_IdCliente", entrada.p_IdCliente);
			params.put("p_IdPortafolio", entrada.p_IdPortafolio);
			params.put("p_IdMoneda", entrada.p_IdMoneda);
			params.put("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			
			long inicioSelectPortafolioMoneda = System.currentTimeMillis();
			List<InCuentaInversionConsultar> inCuentaInversionConsultar = sql.query(con, SQL_SELECT_PORTAFOLIOMONEDA, params, InCuentaInversionConsultar.class);
			long finSelectPortafolioMoneda = System.currentTimeMillis();
			logger.info("Tiempo SQL_SELECT_PORTAFOLIOMONEDA: " + (finSelectPortafolioMoneda - inicioSelectPortafolioMoneda));
			
			if (inCuentaInversionConsultar.isEmpty()) {
				salida.P_OUT_ERRORESCONTADOR = 0L;
				return salida;
			}
			
			EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
			entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaSaldoCuentaInversion.p_FechaDesde = entrada.p_FechaDesde;
			entradaSaldoCuentaInversion.p_FechaHasta = entrada.p_FechaHasta;
			entradaSaldoCuentaInversion.p_SoloSaldoNegativo = "N";
			entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.addAll(inCuentaInversionConsultar);
			
			SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
			long inicioSaldoCuentaInversion = System.currentTimeMillis();
			SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
			long finSaldoCuentaInversion = System.currentTimeMillis();
			logger.info("Tiempo SaldoCuentaInversion: " + (finSaldoCuentaInversion - inicioSaldoCuentaInversion));
			
			BigDecimal saldoInicial;
			if ("S".equals(entrada.p_SaldoPorFechaEfectiva)) {
				saldoInicial = salidaSaldoCuentaInversion.P_OUT_SALDOINICIALFINAL.stream().max((i,j) -> i.SaldoEfectivoInicial.compareTo(j.SaldoEfectivoInicial)).get().SaldoEfectivoInicial;
			} else {
				saldoInicial = salidaSaldoCuentaInversion.P_OUT_SALDOINICIALFINAL.stream().max((i,j) -> i.SaldoMovimientoInicial.compareTo(j.SaldoMovimientoInicial)).get().SaldoMovimientoInicial;
			}
			
			ConsultarMovimientoCuentaInversion.Consulta consulta = new ConsultarMovimientoCuentaInversion.Consulta();
			consulta.IdPortafolio = entrada.p_IdPortafolio;
			consulta.IdMoneda = entrada.p_IdMoneda;
			consulta.IdEstado = entrada.p_IdEstado;
			consulta.SaldoInicial = saldoInicial;
			consulta.FechaDesde = entrada.p_FechaDesde;
			consulta.FechaHasta = entrada.p_FechaHasta;
			consulta.IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
			consulta.EsADC = entrada.p_EsADC;
			consulta.SaldoPorFechaEfectiva = entrada.p_SaldoPorFechaEfectiva;
			consulta.IncluyeSaldo = entrada.p_IncluyeSaldo;
			
			EntradaConsultarMovimientoCuentaInversion entradaConsultarMovimientoCuentaInversion = new EntradaConsultarMovimientoCuentaInversion();
			entradaConsultarMovimientoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarMovimientoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarMovimientoCuentaInversion.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarMovimientoCuentaInversion.p_Consulta = Arrays.asList(consulta);
			
			ConsultarMovimientoCuentaInversion consultarMovimientoCuentaInversion = new ConsultarMovimientoCuentaInversion();
			
			SalidaConsultarMovimientoCuentaInversion salidaConsultarMovimientoCuentaInversion = consultarMovimientoCuentaInversion.logica(con, entradaConsultarMovimientoCuentaInversion);
			List<Long> listIdsComprobante = new ArrayList<Long>();
			if("S".equalsIgnoreCase(entrada.p_IncluyeAnulados)) {
				listIdsComprobante = salidaConsultarMovimientoCuentaInversion
						.P_OUT_MOVIMIENTOS
						.parallelStream()
						.filter(movimientoCuentaInversion->esEstadoValido(entrada.p_IdEstado, movimientoCuentaInversion.IdEstado)
								&& !"ABNAPV".equalsIgnoreCase(movimientoCuentaInversion.IdConcepto))
						.map(movimientoCuentaInversion->{
							return movimientoCuentaInversion.IdComprobante;
				}).collect(Collectors.toList());
			}else {
				listIdsComprobante = salidaConsultarMovimientoCuentaInversion
						.P_OUT_MOVIMIENTOS
						.parallelStream()
						.filter(movimientoCuentaInversion->!"ANU".equals(movimientoCuentaInversion.IdEstado))
						.map(movimientoCuentaInversion->{
							return movimientoCuentaInversion.IdComprobante;
				}).collect(Collectors.toList());
			}
			params.clear();
			params.put("idsComprobante", listIdsComprobante);
			
			List<NemosMovimientos> nemosMovimientos = sql.query(con, SQL_SELECT_NEMOSMOVIMIENTO, params, NemosMovimientos.class);
			HashMap<Long, String> hashNemosMovimientos = new HashMap<>();
			
			for(Long idComprobante : listIdsComprobante) {
				String nemos = "";
				for(NemosMovimientos nemo : nemosMovimientos) {
					nemo.NemotecnicoInstrumento = nemo.NemotecnicoInstrumento == null ? "" : nemo.NemotecnicoInstrumento;
					if(idComprobante != null && nemo.IdComprobante.compareTo(idComprobante) == 0 && !nemos.contains(nemo.NemotecnicoInstrumento))
						nemos = "".equalsIgnoreCase(nemos) ? nemo.NemotecnicoInstrumento : nemos.concat(", " + nemo.NemotecnicoInstrumento);
				}
				hashNemosMovimientos.put(idComprobante, nemos);
			}
			List<CartolaDeMovimiento> cartolaDeMovimiento = new ArrayList<ConsultarCartolaDeMovimiento.CartolaDeMovimiento>();
			
			
			
			if("S".equalsIgnoreCase(entrada.p_IncluyeAnulados)) {
				cartolaDeMovimiento = salidaConsultarMovimientoCuentaInversion
						.P_OUT_MOVIMIENTOS
						.parallelStream()
						.filter(movimientoCuentaInversion->esEstadoValido(entrada.p_IdEstado, movimientoCuentaInversion.IdEstado))
						.map(movimientoCuentaInversion->{
							CartolaDeMovimiento movimientoCartola = new CartolaDeMovimiento();
						movimientoCartola.IdCargoAbonoMovimiento = movimientoCuentaInversion.IdCargoAbonoMovimiento;
						movimientoCartola.FechaSistema = movimientoCuentaInversion.FechaSistema;
						movimientoCartola.FechaIngresoRegistro = movimientoCuentaInversion.FechaIngresoRegistro;
						movimientoCartola.IdCuentaInversion = movimientoCuentaInversion.IdCuentaInversion;
						movimientoCartola.CodigoCuentaInversion = movimientoCuentaInversion.CodigoCuentaInversion;
						movimientoCartola.NombreCuentaInversion = movimientoCuentaInversion.NombreCuentaInversion;
						movimientoCartola.IdCliente = movimientoCuentaInversion.IdCliente;
						movimientoCartola.NombreCliente = movimientoCuentaInversion.NombreCliente;
						movimientoCartola.IdPortafolio = movimientoCuentaInversion.IdPortafolio;
						movimientoCartola.IdMoneda = movimientoCuentaInversion.IdMoneda;
						movimientoCartola.CodigoMoneda = movimientoCuentaInversion.CodigoMoneda;
						movimientoCartola.NombreMoneda = movimientoCuentaInversion.NombreMoneda;
						movimientoCartola.SimboloMoneda = movimientoCuentaInversion.SimboloMoneda;
						movimientoCartola.DecimalesMoneda = movimientoCuentaInversion.DecimalesMoneda;
						movimientoCartola.IdTipoMovimiento = movimientoCuentaInversion.IdTipoMovimiento;
						movimientoCartola.TipoMovimiento = movimientoCuentaInversion.TipoMovimiento;
						movimientoCartola.NombreTipoMovimiento = movimientoCuentaInversion.NombreTipoMovimiento;
						movimientoCartola.IdConcepto = movimientoCuentaInversion.IdConcepto;

						if(movimientoCuentaInversion.IdCarga == null) {
							movimientoCartola.NombreConcepto = movimientoCuentaInversion.NombreConcepto;
						}else {
							movimientoCartola.NombreConcepto = movimientoCuentaInversion.IdCarga > 0 ?  movimientoCuentaInversion.Descripcion:movimientoCuentaInversion.NombreConcepto;
						}
						movimientoCartola.OrigenConcepto = movimientoCuentaInversion.OrigenConcepto;
						movimientoCartola.Descripcion = movimientoCuentaInversion.Descripcion;
						movimientoCartola.IdCondicionDeLiquidacion = movimientoCuentaInversion.IdCondicionDeLiquidacion;
						movimientoCartola.CodigoCondicionDeLiquidacion = movimientoCuentaInversion.CodigoCondicionDeLiquidacion;
						movimientoCartola.NombreCondicionDeLiquidacion = movimientoCuentaInversion.NombreCondicionDeLiquidacion;
						movimientoCartola.PlazoCondicionDeLiquidacion = movimientoCuentaInversion.PlazoCondicionDeLiquidacion;
						movimientoCartola.FechaEfectivaMovimiento = movimientoCuentaInversion.FechaEfectivaMovimiento;
						movimientoCartola.Monto = movimientoCuentaInversion.Monto;
						movimientoCartola.SaldoPorFechaEfectiva = movimientoCuentaInversion.SaldoPorFechaEfectiva;
						movimientoCartola.SaldoPorFechaMovimiento = movimientoCuentaInversion.SaldoPorFechaMovimiento;
						movimientoCartola.IdModulo = movimientoCuentaInversion.IdModulo;
						movimientoCartola.IdTipoProducto = movimientoCuentaInversion.IdTipoProducto;
						movimientoCartola.CodigoTipoProducto = movimientoCuentaInversion.CodigoTipoProducto;
						movimientoCartola.NombreTipoProducto = movimientoCuentaInversion.NombreTipoProducto;
						movimientoCartola.IdTipoOperacion = movimientoCuentaInversion.IdTipoOperacion;
						movimientoCartola.CodigoTipoOperacion = movimientoCuentaInversion.CodigoTipoOperacion;
						movimientoCartola.NombreTipoOperacion = movimientoCuentaInversion.NombreTipoOperacion;
						movimientoCartola.AccionTipoOperacion = movimientoCuentaInversion.AccionTipoOperacion;
						movimientoCartola.IdOperacion = movimientoCuentaInversion.IdOperacion;
						movimientoCartola.FechaOperacion = movimientoCuentaInversion.FechaOperacion;
						movimientoCartola.FechaLiquidacionOperacion = movimientoCuentaInversion.FechaLiquidacionOperacion;
						movimientoCartola.FechaVencimientoOperacion = movimientoCuentaInversion.FechaVencimientoOperacion;
						movimientoCartola.IdMonedaOperacion = movimientoCuentaInversion.IdMonedaOperacion;
						movimientoCartola.MontoOperacion = movimientoCuentaInversion.MontoOperacion;
						movimientoCartola.TipoCambioOperacion = movimientoCuentaInversion.TipoCambioOperacion;
						movimientoCartola.IdTipoInstrumento = movimientoCuentaInversion.IdTipoInstrumento;
						movimientoCartola.NombreTipoInstrumento = movimientoCuentaInversion.NombreTipoInstrumento;
						movimientoCartola.IdClasificacionTipoInstrumento = movimientoCuentaInversion.IdClasificacionTipoInstrumento;
						movimientoCartola.NombreClasificacionTipoInstrumento = movimientoCuentaInversion.NombreClasificacionTipoInstrumento;
						movimientoCartola.IdInstrumento = movimientoCuentaInversion.IdInstrumento;
						movimientoCartola.Cantidad = movimientoCuentaInversion.Cantidad;
						if(movimientoCuentaInversion.IdModulo != null && movimientoCuentaInversion.IdModulo.equalsIgnoreCase("FAC")) {
							movimientoCartola.NemotecnicoOriginal = hashNemosMovimientos.get(movimientoCuentaInversion.IdComprobante) != null ? hashNemosMovimientos.get(movimientoCuentaInversion.IdComprobante) : "";
						}
						else {
							movimientoCartola.NemotecnicoOriginal = movimientoCuentaInversion.NemotecnicoOriginal != null ?  movimientoCuentaInversion.NemotecnicoOriginal : "";
						}
						movimientoCartola.EsCargoAbonoPorTraspaso = movimientoCuentaInversion.EsCargoAbonoPorTraspaso;
						movimientoCartola.EsCargoAbonoPorTraspasoTraducido = movimientoCuentaInversion.EsCargoAbonoPorTraspasoTraducido;
						movimientoCartola.IdCargoAbonoRelacionadoTraspaso = movimientoCuentaInversion.IdCargoAbonoRelacionadoTraspaso;
						movimientoCartola.NombreClienteRelacionado = movimientoCuentaInversion.NombreClienteRelacionado;
						movimientoCartola.CodigoPortafolioRelacionado = movimientoCuentaInversion.CodigoPortafolioRelacionado;
						movimientoCartola.IdTipoComprobante = movimientoCuentaInversion.IdTipoComprobante;
						movimientoCartola.CodigoTipoComprobante = movimientoCuentaInversion.CodigoTipoComprobante;
						movimientoCartola.NombreTipoComprobante = movimientoCuentaInversion.NombreTipoComprobante;
						movimientoCartola.IdComprobante = movimientoCuentaInversion.IdComprobante;
						movimientoCartola.FolioComprobante = movimientoCuentaInversion.FolioComprobante;
						movimientoCartola.EsVentaCorta = movimientoCuentaInversion.EsVentaCorta;
						movimientoCartola.FechaComprobante = movimientoCuentaInversion.FechaComprobante;
						movimientoCartola.IdComprobanteDetalle = movimientoCuentaInversion.IdComprobanteDetalle;
						movimientoCartola.IdPersonaPagoCobro = movimientoCuentaInversion.IdPersonaPagoCobro;
						movimientoCartola.EsCertificadoAutomatico = movimientoCuentaInversion.EsCertificadoAutomatico;
						movimientoCartola.IdUsuarioCertificador = movimientoCuentaInversion.IdUsuarioCertificador;
						movimientoCartola.FechaCertificacion = movimientoCuentaInversion.FechaCertificacion;
						movimientoCartola.IdContraparte = movimientoCuentaInversion.IdContraparte;
						movimientoCartola.EsIndicadorBilateral = movimientoCuentaInversion.EsIndicadorBilateral;
						movimientoCartola.IdEmpresa = movimientoCuentaInversion.IdEmpresa;
						movimientoCartola.CodigoEmpresa = movimientoCuentaInversion.CodigoEmpresa;
						movimientoCartola.NombreEmpresa = movimientoCuentaInversion.NombreEmpresa;
						movimientoCartola.EsIngresoManual = movimientoCuentaInversion.EsIngresoManual;
						movimientoCartola.IdEvento = movimientoCuentaInversion.IdEvento;
						movimientoCartola.IdUsuario = movimientoCuentaInversion.IdUsuario;
						movimientoCartola.NombreUsuario = movimientoCuentaInversion.NombreUsuario;
						movimientoCartola.IdEstado = movimientoCuentaInversion.IdEstado;
						movimientoCartola.NombreEstado = movimientoCuentaInversion.NombreEstado;
						movimientoCartola.Cargo = movimientoCuentaInversion.Cargo;
						movimientoCartola.Abono = movimientoCuentaInversion.Abono;
						movimientoCartola.SaldoSimulado = movimientoCuentaInversion.SaldoSimulado;
						movimientoCartola.IdMatrizAgrupador = movimientoCuentaInversion.IdMatrizAgrupador;
						movimientoCartola.EsADC = movimientoCuentaInversion.EsADC;
						movimientoCartola.NombreUsuarioCert = movimientoCuentaInversion.NombreUsuarioCert;
						movimientoCartola.IdEstadoSII = movimientoCuentaInversion.IdEstadoSII;
						movimientoCartola.IdRespuestaDTE = movimientoCuentaInversion.IdRespuestaDTE;
						movimientoCartola.MayorMenorValor = movimientoCuentaInversion.MayorMenorValor;
						movimientoCartola.MontoRetencionGananciaCap = movimientoCuentaInversion.MontoRetencionGananciaCap;
							return movimientoCartola;
				}).collect(Collectors.toList());
			}else {
				
				cartolaDeMovimiento = salidaConsultarMovimientoCuentaInversion
						.P_OUT_MOVIMIENTOS
						.parallelStream()
						.filter(movimientoCuentaInversion->esEstadoValido(entrada.p_IdEstado, movimientoCuentaInversion.IdEstado) && !movimientoCuentaInversion.IdEstado.equals("ANU"))
						.map(movimientoCuentaInversion->{
							CartolaDeMovimiento movimientoCartola = new CartolaDeMovimiento();
						movimientoCartola.IdCargoAbonoMovimiento = movimientoCuentaInversion.IdCargoAbonoMovimiento;
						movimientoCartola.FechaSistema = movimientoCuentaInversion.FechaSistema;
						movimientoCartola.FechaIngresoRegistro = movimientoCuentaInversion.FechaIngresoRegistro;
						movimientoCartola.IdCuentaInversion = movimientoCuentaInversion.IdCuentaInversion;
						movimientoCartola.CodigoCuentaInversion = movimientoCuentaInversion.CodigoCuentaInversion;
						movimientoCartola.NombreCuentaInversion = movimientoCuentaInversion.NombreCuentaInversion;
						movimientoCartola.IdCliente = movimientoCuentaInversion.IdCliente;
						movimientoCartola.NombreCliente = movimientoCuentaInversion.NombreCliente;
						movimientoCartola.IdPortafolio = movimientoCuentaInversion.IdPortafolio;
						movimientoCartola.IdMoneda = movimientoCuentaInversion.IdMoneda;
						movimientoCartola.CodigoMoneda = movimientoCuentaInversion.CodigoMoneda;
						movimientoCartola.NombreMoneda = movimientoCuentaInversion.NombreMoneda;
						movimientoCartola.SimboloMoneda = movimientoCuentaInversion.SimboloMoneda;
						movimientoCartola.DecimalesMoneda = movimientoCuentaInversion.DecimalesMoneda;
						movimientoCartola.IdTipoMovimiento = movimientoCuentaInversion.IdTipoMovimiento;
						movimientoCartola.TipoMovimiento = movimientoCuentaInversion.TipoMovimiento;
						movimientoCartola.NombreTipoMovimiento = movimientoCuentaInversion.NombreTipoMovimiento;
						movimientoCartola.IdConcepto = movimientoCuentaInversion.IdConcepto;

						if(movimientoCuentaInversion.IdCarga == null) {
							movimientoCartola.NombreConcepto = movimientoCuentaInversion.NombreConcepto;
						}else {
							movimientoCartola.NombreConcepto = movimientoCuentaInversion.IdCarga > 0 ?  movimientoCuentaInversion.Descripcion:movimientoCuentaInversion.NombreConcepto;
						}
						movimientoCartola.OrigenConcepto = movimientoCuentaInversion.OrigenConcepto;
						movimientoCartola.Descripcion = movimientoCuentaInversion.Descripcion;
						movimientoCartola.IdCondicionDeLiquidacion = movimientoCuentaInversion.IdCondicionDeLiquidacion;
						movimientoCartola.CodigoCondicionDeLiquidacion = movimientoCuentaInversion.CodigoCondicionDeLiquidacion;
						movimientoCartola.NombreCondicionDeLiquidacion = movimientoCuentaInversion.NombreCondicionDeLiquidacion;
						movimientoCartola.PlazoCondicionDeLiquidacion = movimientoCuentaInversion.PlazoCondicionDeLiquidacion;
						movimientoCartola.FechaEfectivaMovimiento = movimientoCuentaInversion.FechaEfectivaMovimiento;
						movimientoCartola.Monto = movimientoCuentaInversion.Monto;
						movimientoCartola.SaldoPorFechaEfectiva = movimientoCuentaInversion.SaldoPorFechaEfectiva;
						movimientoCartola.SaldoPorFechaMovimiento = movimientoCuentaInversion.SaldoPorFechaMovimiento;
						movimientoCartola.IdModulo = movimientoCuentaInversion.IdModulo;
						movimientoCartola.IdTipoProducto = movimientoCuentaInversion.IdTipoProducto;
						movimientoCartola.CodigoTipoProducto = movimientoCuentaInversion.CodigoTipoProducto;
						movimientoCartola.NombreTipoProducto = movimientoCuentaInversion.NombreTipoProducto;
						movimientoCartola.IdTipoOperacion = movimientoCuentaInversion.IdTipoOperacion;
						movimientoCartola.CodigoTipoOperacion = movimientoCuentaInversion.CodigoTipoOperacion;
						movimientoCartola.NombreTipoOperacion = movimientoCuentaInversion.NombreTipoOperacion;
						movimientoCartola.AccionTipoOperacion = movimientoCuentaInversion.AccionTipoOperacion;
						movimientoCartola.IdOperacion = movimientoCuentaInversion.IdOperacion;
						movimientoCartola.FechaOperacion = movimientoCuentaInversion.FechaOperacion;
						movimientoCartola.FechaLiquidacionOperacion = movimientoCuentaInversion.FechaLiquidacionOperacion;
						movimientoCartola.FechaVencimientoOperacion = movimientoCuentaInversion.FechaVencimientoOperacion;
						movimientoCartola.IdMonedaOperacion = movimientoCuentaInversion.IdMonedaOperacion;
						movimientoCartola.MontoOperacion = movimientoCuentaInversion.MontoOperacion;
						movimientoCartola.TipoCambioOperacion = movimientoCuentaInversion.TipoCambioOperacion;
						movimientoCartola.IdTipoInstrumento = movimientoCuentaInversion.IdTipoInstrumento;
						movimientoCartola.NombreTipoInstrumento = movimientoCuentaInversion.NombreTipoInstrumento;
						movimientoCartola.IdClasificacionTipoInstrumento = movimientoCuentaInversion.IdClasificacionTipoInstrumento;
						movimientoCartola.NombreClasificacionTipoInstrumento = movimientoCuentaInversion.NombreClasificacionTipoInstrumento;
						movimientoCartola.IdInstrumento = movimientoCuentaInversion.IdInstrumento;
						movimientoCartola.Cantidad = movimientoCuentaInversion.Cantidad;
						if(movimientoCuentaInversion.IdModulo != null && movimientoCuentaInversion.IdModulo.equalsIgnoreCase("FAC")) {
							movimientoCartola.NemotecnicoOriginal = hashNemosMovimientos.get(movimientoCuentaInversion.IdComprobante) != null ? hashNemosMovimientos.get(movimientoCuentaInversion.IdComprobante) : "";
						}
						else {
							movimientoCartola.NemotecnicoOriginal = movimientoCuentaInversion.NemotecnicoOriginal != null ?  movimientoCuentaInversion.NemotecnicoOriginal : "";
						}
						movimientoCartola.EsCargoAbonoPorTraspaso = movimientoCuentaInversion.EsCargoAbonoPorTraspaso;
						movimientoCartola.EsCargoAbonoPorTraspasoTraducido = movimientoCuentaInversion.EsCargoAbonoPorTraspasoTraducido;
						movimientoCartola.IdCargoAbonoRelacionadoTraspaso = movimientoCuentaInversion.IdCargoAbonoRelacionadoTraspaso;
						movimientoCartola.NombreClienteRelacionado = movimientoCuentaInversion.NombreClienteRelacionado;
						movimientoCartola.CodigoPortafolioRelacionado = movimientoCuentaInversion.CodigoPortafolioRelacionado;
						movimientoCartola.IdTipoComprobante = movimientoCuentaInversion.IdTipoComprobante;
						movimientoCartola.CodigoTipoComprobante = movimientoCuentaInversion.CodigoTipoComprobante;
						movimientoCartola.NombreTipoComprobante = movimientoCuentaInversion.NombreTipoComprobante;
						movimientoCartola.IdComprobante = movimientoCuentaInversion.IdComprobante;
						movimientoCartola.FolioComprobante = movimientoCuentaInversion.FolioComprobante;
						movimientoCartola.EsVentaCorta = movimientoCuentaInversion.EsVentaCorta;
						movimientoCartola.FechaComprobante = movimientoCuentaInversion.FechaComprobante;
						movimientoCartola.IdComprobanteDetalle = movimientoCuentaInversion.IdComprobanteDetalle;
						movimientoCartola.IdPersonaPagoCobro = movimientoCuentaInversion.IdPersonaPagoCobro;
						movimientoCartola.EsCertificadoAutomatico = movimientoCuentaInversion.EsCertificadoAutomatico;
						movimientoCartola.IdUsuarioCertificador = movimientoCuentaInversion.IdUsuarioCertificador;
						movimientoCartola.FechaCertificacion = movimientoCuentaInversion.FechaCertificacion;
						movimientoCartola.IdContraparte = movimientoCuentaInversion.IdContraparte;
						movimientoCartola.EsIndicadorBilateral = movimientoCuentaInversion.EsIndicadorBilateral;
						movimientoCartola.IdEmpresa = movimientoCuentaInversion.IdEmpresa;
						movimientoCartola.CodigoEmpresa = movimientoCuentaInversion.CodigoEmpresa;
						movimientoCartola.NombreEmpresa = movimientoCuentaInversion.NombreEmpresa;
						movimientoCartola.EsIngresoManual = movimientoCuentaInversion.EsIngresoManual;
						movimientoCartola.IdEvento = movimientoCuentaInversion.IdEvento;
						movimientoCartola.IdUsuario = movimientoCuentaInversion.IdUsuario;
						movimientoCartola.NombreUsuario = movimientoCuentaInversion.NombreUsuario;
						movimientoCartola.IdEstado = movimientoCuentaInversion.IdEstado;
						movimientoCartola.NombreEstado = movimientoCuentaInversion.NombreEstado;
						movimientoCartola.Cargo = movimientoCuentaInversion.Cargo;
						movimientoCartola.Abono = movimientoCuentaInversion.Abono;
						movimientoCartola.SaldoSimulado = movimientoCuentaInversion.SaldoSimulado;
						movimientoCartola.IdMatrizAgrupador = movimientoCuentaInversion.IdMatrizAgrupador;
						movimientoCartola.EsADC = movimientoCuentaInversion.EsADC;
						movimientoCartola.NombreUsuarioCert = movimientoCuentaInversion.NombreUsuarioCert;
						movimientoCartola.IdEstadoSII = movimientoCuentaInversion.IdEstadoSII;
						movimientoCartola.IdRespuestaDTE = movimientoCuentaInversion.IdRespuestaDTE;
						movimientoCartola.MayorMenorValor = movimientoCuentaInversion.MayorMenorValor;
						movimientoCartola.MontoRetencionGananciaCap = movimientoCuentaInversion.MontoRetencionGananciaCap;
						
							return movimientoCartola;
				}).collect(Collectors.toList());
			}
			
			salida.P_OUT_CARTOLADEMOVIMIENTO.addAll(cartolaDeMovimiento);
			salida.P_OUT_ERRORESCONTADOR = 0L;
		}catch(Exception e){
			logger.error("ERROR",e);
			throw e;
		}
		long finServicio = System.currentTimeMillis();
		logger.info("Tiempo ConsultarCartolaDeMovimiento: " + (finServicio - inicioServicio));
		logger.info("FIN::ConsultarCartolaDeMovimiento");
		return salida;
	}
	
	private Boolean esEstadoValido(String filtroEstado, String estadoMovimiento) {
		logger.trace("INICIO::ConsultarCartolaDeMovimiento.esEstadoValido");
		long inicio = System.currentTimeMillis();
		Boolean esEstadoValido = filtroEstado == null || filtroEstado.isEmpty() || filtroEstado.equals(estadoMovimiento);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarCartolaDeMovimiento.esEstadoValido", fin - inicio);
		return esEstadoValido;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarCartolaDeMovimiento jin = gson.fromJson(in.toString(), EntradaConsultarCartolaDeMovimiento.class);
		SalidaConsultarCartolaDeMovimiento out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
			Gson gson = Utils.getGsonBuilder();		
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1003,\"p_IdModulo\":\"CTA\",\"p_IdPortafolio\":51663,\"p_FechaDesde\":\"2018-01-01\",\"p_FechaHasta\":\"2020-01-01\",\"p_IdMoneda\":\"CLP\",\"p_IdMatrizAgrupador\":100,\"p_EsADC\":null}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1003,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCuentaInversion\":421553,\"p_IdCliente\":1555,\"p_IdPortafolio\":51663,\"p_IdMoneda\":\"USD\",\"p_IdEstado\":null,\"p_FechaDesde\":\"2018-01-01\",\"p_FechaHasta\":\"2020-01-01\",\"p_SaldoPorFechaEfectiva\":\"S\",\"p_Validaciones\":\"S\",\"p_IdProductoAgrupador\":null,\"p_IdMatrizAgrupador\":null,\"p_EsADC\":null}";
			EntradaConsultarCartolaDeMovimiento Objin = gson.fromJson(json, EntradaConsultarCartolaDeMovimiento.class);
			ConsultarCartolaDeMovimiento  demo=new ConsultarCartolaDeMovimiento();				
			try {
				demo.logica(con, Objin);
				//con.commit();
				//System.out.println(ObjOut.toString());
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
