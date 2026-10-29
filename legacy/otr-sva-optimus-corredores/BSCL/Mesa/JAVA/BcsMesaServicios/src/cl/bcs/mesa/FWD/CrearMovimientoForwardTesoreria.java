package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.mesa.FWD.AnticiparOperacionForward.FormaDePagoAnticipoForward;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;

public class CrearMovimientoForwardTesoreria implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearMovimientoForwardTesoreria.class);
	
	private final String SQL_DELETE_TABLA_PASO = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_DeleteTablaPaso.sql");
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_InsertTablaPaso.sql");
	private final String SQL_SELECT_MOVIMIENTO_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectMovimientoForward.sql");
	private final String SQL_SELECT_MOVIMIENTO_FORWARD_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectMovimientoForwardTesoreria.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_SelectPreferenciaPagoTesoreria.sql");
	private final String SQL_UPDATE_CARTERA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_UpdateCartera.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_COMPENSADA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_ConsultarFormaDePagoCompensada.sql");
	private final String SQL_UPDATE_COMPROBANTE_PAGO_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearMovimientoForwardTesoreria_UpdateComprobantePagoOperacion.sql");
	
	public static class EntradaCrearMovimientoForwardTesoreria{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPais;
		
		public List<IngresoEgresoGenerar> p_IngresoEgresoGenerar;
		
		public EntradaCrearMovimientoForwardTesoreria() {
			this.p_IngresoEgresoGenerar = new ArrayList<IngresoEgresoGenerar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoEgresoGenerar{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdEstado;
		public BigDecimal MontoOperacion;
		public String IdMonedaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearMovimientoForwardTesoreria{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<IngresoEgresoGenerado> P_OUT_INGRESOEGRESOGENERADO;
		
		public SalidaCrearMovimientoForwardTesoreria() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoEgresoGenerado{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public Long NumeroIngresoEgreso;
		public Long IdIngresoEgreso;
		public Long NumeroCargoAbono;
		public Long IdCargoAbono;
		public String IdTipoMovimiento;
		public String IdMonedaLiquidacion;
		public BigDecimal MontoOperacion;
		public String FechaFixing;
		public String HoraFixing;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoForward{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdPortafolio;
		public Long FolioOperacion;
		public Long IdPersona;
		public String IngresoEgreso;
		public String TipoVencimiento;
		public BigDecimal MontoOperacion;
		public Long IdAgente;
		public Long IdSucursal;
		public String IdTipoOperacion;
		public Date FechaOperacion;
		public Date FechaVencimiento;
		public String IdMonedaLiquidacion;
		public Long IdCliente;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public BigDecimal TipoCambioOperacion;
		public String IdClasificacionTipoInstrumento;
		public Long IdOperacion;
		public String IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TesoreriaForward{
		public Long IdOperacion;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdEstado;
		public String IdModalidadPago;
		public String IdFormaDePago;
		public String IdTipoProducto;
		public BigDecimal MontoFormaDePagoForward;
		public String MonedaFormaDePagoForward;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal MontoOperacion;
		public String IdMonedaLiquidacion;
		public Long IdPortafolio;
		public String IdMedioDePago;
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoTesoreriaForward{
		public String Key;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdMoneda;
		public Long IdEmpresa;
		public String IdMedioDePago;
		public Long IdCuentaBancaria;
		public Long IdBancoCuentaBancaria;
		public String NombreBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdBancoTarjetaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NombreTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String PorDefecto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SumaTesoreria{
		public String Id;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdMonedaLiquidacion;
		public String IdEstado;
		public BigDecimal MontoTesoreria;
		
		public SumaTesoreria() {
			this.MontoTesoreria = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateComprobantePagoOperacion{
		public Date FechaAnticipo;
		public Long IdComprobantePagoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearMovimientoForwardTesoreria jin = gson.fromJson(in.toString(), EntradaCrearMovimientoForwardTesoreria.class);
		SalidaCrearMovimientoForwardTesoreria out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearMovimientoForwardTesoreria logica (Connection con, EntradaCrearMovimientoForwardTesoreria entrada) throws Exception {
		logger.info("INICIO::CrearMovimientoForwardTesoreria");
		
		SalidaCrearMovimientoForwardTesoreria out = new SalidaCrearMovimientoForwardTesoreria();
		
		SQL sql=new SQL();

		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
	    
	    String horaSistema = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
	    InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
	    inObtenerDiaAnterior.idPais = idPaisSistema;
	    inObtenerDiaAnterior.diaHabil = "S";
	    inObtenerDiaAnterior.soloCaeDiaHabil = "S";
	    InObtenerDiaAnterior.FechasIn fechaInicial = new InObtenerDiaAnterior.FechasIn();
	    fechaInicial.fechaInicial = fechaSistema;
	    fechaInicial.numeroDias = 1L;
	    inObtenerDiaAnterior.fechas.add(fechaInicial);
	    OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
	    String fechaHabilAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		
		SQL.exec(con, SQL_DELETE_TABLA_PASO);
		sql.batch(con, SQL_INSERT_TABLA_PASO, entrada.p_IngresoEgresoGenerar, IngresoEgresoGenerar.class);
		
		List<MovimientoForward> movimientosForward = sql.query(con, SQL_SELECT_MOVIMIENTO_FORWARD, new HashMap<String,Object>(), MovimientoForward.class);
		HashMap<Long,Object> hashMovimientoForward = Utils.List2Hash(movimientosForward, "FolioCartera");
		
		//CARGO - ABONO
		{
			CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			
			entradaCrearCargoAbono.p_CargoAbonoAGenerar = movimientosForward.stream().map(mov -> {
				ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
				obj.NumeroCargoAbono = mov.FolioCartera;
				obj.IdPortafolio = mov.IdPortafolio;
				obj.IdMoneda = mov.IdMonedaLiquidacion;
				obj.IdTipoMovimiento = "ING".equals(mov.IngresoEgreso) ? "MOVABN" : "MOVCAR";
				obj.IdConcepto = "ANTICIPADA".equals(mov.TipoVencimiento) ? "ANTFWD" : "VCDFWD";
				obj.Descripcion = "";
				obj.IdCondicionDeLiquidacion = "T0";
				obj.FechaCondicionDeLiquidacion = fechaSistema;
				obj.Monto = mov.MontoOperacion;
				obj.IdEjecutivo = mov.IdAgente;
				obj.IdSucursal = mov.IdSucursal;
				obj.IdTipoProducto = "MEFW";
				obj.IdTipoOperacion = mov.IdTipoOperacion;
				obj.IdOperacion = mov.FolioCartera;
				obj.FechaOperacion = Fecha.stringToSqlDate(mov.FechaOperacion);
				obj.FechaLiquidacionOperacion = fechaSistema;
				obj.FechaVencimientoOperacion = Fecha.stringToSqlDate(mov.FechaVencimiento);
				obj.IdMonedaOperacion = mov.IdMonedaLiquidacion;
				obj.MontoOperacion = mov.MontoOperacion;
				obj.TipoCambioOperacion = mov.TipoCambioOperacion;
				obj.IdContraparte = mov.IdCliente;
				obj.IdTipoComprobante = "MFW";
				obj.IdComprobante = mov.FolioCartera;
				obj.FolioComprobante = "";
				obj.IdComprobanteDetalle = 0L;
				obj.IdComprobanteFormaDePago = 0L;
				obj.IdTipoMovimientoAsociado = "";
				obj.IdMovimientoAsociado = 0L;
				obj.FechaComprobante = fechaSistema;
				obj.IdPersonaPagoCobro = 0L;
				obj.IdInstrumento = mov.IdInstrumento;
				obj.IdTipoInstrumento = mov.IdTipoInstrumento;
				obj.Nemo = mov.Nemotecnico;
				obj.EsIndicadorBilateral = "N";
				obj.EsCargoAbonoPorTraspaso = "N";
				obj.IdPortafolioTraspaso = 0L;
				obj.GeneroContraparteEnTesoreria = "N";
				obj.CertificadoAutomatico = "S";
				return obj;
			}).collect(Collectors.toList());
			
			entradaCrearCargoAbono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion = "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaCrearCargoAbono.P_OUT_MENSAJES);
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				return out;
			}
			
			out.P_OUT_INGRESOEGRESOGENERADO.addAll(salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO.stream().map(mov->{
				MovimientoForward movimientoForward = (MovimientoForward) hashMovimientoForward.get(mov.NumeroCargoAbono);
				IngresoEgresoGenerado ingresoEgresoGenerado = new IngresoEgresoGenerado();
				if(movimientoForward != null) {
					ingresoEgresoGenerado.FolioCartera = movimientoForward.FolioCartera;
					ingresoEgresoGenerado.IdCartera = movimientoForward.IdCartera;
					ingresoEgresoGenerado.IdCargoAbono = mov.IdCargoAbonoMovimiento;
					ingresoEgresoGenerado.IdTipoMovimiento = mov.IdTipoMovimiento;
					ingresoEgresoGenerado.NumeroCargoAbono = mov.NumeroCargoAbono;
					ingresoEgresoGenerado.IdOperacion = movimientoForward.IdOperacion;
					ingresoEgresoGenerado.IdMonedaLiquidacion = movimientoForward.IdMonedaLiquidacion;
					ingresoEgresoGenerado.MontoOperacion = movimientoForward.MontoOperacion;
					ingresoEgresoGenerado.FechaFixing = fechaHabilAnterior;
					ingresoEgresoGenerado.HoraFixing = horaSistema;
				}
				return ingresoEgresoGenerado;
				
			}).filter(mov-> mov.FolioCartera != null).collect(Collectors.toList()));
		}
		
		
		
		//CREAR INGRESO - EGRESO
		{
			HashMap<String,SumaTesoreria> sumatoriaTesoreria = new HashMap<String,SumaTesoreria>();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<TesoreriaForward> movimientosTesoreria = sql.query(con, SQL_SELECT_MOVIMIENTO_FORWARD_TESORERIA, params, TesoreriaForward.class);
			List<Long> listIdPortafolio = new ArrayList<Long>();
			for (TesoreriaForward mov : movimientosTesoreria) {
				if (!listIdPortafolio.contains(mov.IdPortafolio)) listIdPortafolio.add(mov.IdPortafolio);
			}
			params.clear();
			params.put("IdPortafolio", listIdPortafolio);
			List<FormaDePagoTesoreriaForward> lstPreferencias = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_TESORERIA, params, FormaDePagoTesoreriaForward.class);
			
			//Se quitan las llaves duplicadas, se prioriza "PorDefecto" = "S"
			HashMap<String, FormaDePagoTesoreriaForward> hashPreferencias = new HashMap<>();
			for (FormaDePagoTesoreriaForward obj : lstPreferencias) {
			    String key = obj.Key;
			    if (!hashPreferencias.containsKey(key)) {
			    	hashPreferencias.put(key, obj);
			    } else {
			    	if ("S".equals(obj.PorDefecto)) {
			    		hashPreferencias.put(key, obj);
			        }
			    }
			}
			
			if(movimientosTesoreria.size() > 0) {
				for(TesoreriaForward tesoreriaForward : movimientosTesoreria) {
					String id = tesoreriaForward.IdCartera + "_" + tesoreriaForward.FolioCartera + "_" + tesoreriaForward.IdMonedaLiquidacion;
					SumaTesoreria suma = sumatoriaTesoreria.get(id);
					if(suma == null) {
						suma = new SumaTesoreria();
						suma.Id = id;
						suma.IdCartera = tesoreriaForward.IdCartera;
						suma.FolioCartera = tesoreriaForward.FolioCartera;
						suma.IdMonedaLiquidacion = tesoreriaForward.IdMonedaLiquidacion;
						suma.IdEstado = tesoreriaForward.IdEstado;
					}
					
					BigDecimal montoTesoreria = tesoreriaForward.MontoFormaDePagoForward.divide(!"UF".equals(tesoreriaForward.IdMonedaPrincipal) ? tesoreriaForward.MontoMonedaPrincipal : tesoreriaForward.MontoMonedaSecundaria, MathContext.DECIMAL128).multiply(tesoreriaForward.MontoOperacion, MathContext.DECIMAL128);
					suma.MontoTesoreria = suma.MontoTesoreria.add(montoTesoreria);
					sumatoriaTesoreria.put(id, suma);
				}
				
				AtomicLong idFp = new AtomicLong(1L);
				
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
				List<Long> secuencia = Utils.getSecuencia(con, "BSCL.Tesoreria.BD.Secuencias::SecuenciaRelacionada", 1L);
				entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = movimientosForward.stream().map(mov -> {
					String id = mov.IdCartera + "_" + mov.FolioCartera + "_" + mov.IdMonedaLiquidacion;
					SumaTesoreria suma = sumatoriaTesoreria.get(id);
					IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
					if(suma != null) {
						ie.NumeroReferencia = mov.FolioCartera;
						ie.IdTipoMovimiento = "ING".equals(mov.IngresoEgreso) ? "MOVEGR" : "MOVING";
						ie.IdTipoPersona = "CLI";
						ie.IdPersona = mov.IdPersona;
						ie.IdPortafolio = mov.IdPortafolio;
						ie.IdMoneda = mov.IdMonedaLiquidacion;
						ie.Monto = suma.MontoTesoreria;
						ie.FechaLiquidacion = fechaSistema;
						ie.IdConcepto = "ANTICIPADA".equals(mov.TipoVencimiento) ? "ANTFWD" : "VCDFWD";
						ie.IdTipoPersonaTitularPago = "CLI";
						ie.IdPersonaTitularPago = mov.IdPersona;
						ie.IdPortafolioTitularPago = mov.IdPortafolio;
						ie.IdCaja = 0L;
						ie.SecuenciaAgrupacion = "";
						ie.SecuenciaRelacionada = secuencia.get(0);
						ie.Glosa = "";
						ie.IdEmpleadoDigitador = mov.IdAgente;
						ie.IdSucursalEmpleadoDigitador = 0L;
						ie.IdTipoProducto = "MEFW";
						ie.IdTipoOperacion = mov.IdTipoOperacion;
						ie.IdTipoComprobante = "MFW";
						ie.IdComprobante = mov.FolioCartera;
						ie.FechaComprobante = fechaSistema;
						ie.FechaLiquidacionComprobante = fechaSistema;
						ie.IdOperacion = mov.FolioOperacion;
						ie.FechaOperacion = Fecha.stringToSqlDate(mov.FechaOperacion);
						ie.IdCondicionLiquidacion = "T0";
						ie.IdBolsa = "BCS";
						ie.IdMonedaOperacion = mov.IdMonedaLiquidacion;
						ie.MontoOperacion = suma.MontoTesoreria;
						ie.IdTipoReajuste = "";
						ie.FechaParidad = "";
						ie.ValorParidad = BigDecimal.ZERO;
						ie.FolioCartera = 0L;
						ie.IdInstrumento = mov.IdInstrumento;
						ie.IdTipoInstrumento = mov.IdTipoInstrumento;
						ie.IdClasificacionTipoInstrumento = mov.IdClasificacionTipoInstrumento;
						ie.Bilateral = "N";
						ie.GeneraComprobante = "S";
						ie.AgruparEnComprobante = "N";
						ie.MueveCuentaInversion = "N";
						ie.TitularPago = "S";
						ie.IdTipoCanalIngreso = mov.IdTipoCanalIngreso;
						
					}
					return ie;
				}).filter(mov-> mov.NumeroReferencia != null).collect(Collectors.toList());
	
				entradaCrearIngresoEgreso.p_FormaDePago = movimientosTesoreria.stream().map(mov ->{
					BigDecimal montoTesoreria = mov.MontoFormaDePagoForward.divide(!"UF".equals(mov.IdMonedaPrincipal) ? mov.MontoMonedaPrincipal : mov.MontoMonedaSecundaria, MathContext.DECIMAL128).multiply(mov.MontoOperacion, MathContext.DECIMAL128);
					FormaDePago_Entrada fp = new FormaDePago_Entrada();
					
					FormaDePagoTesoreriaForward fpTf = (FormaDePagoTesoreriaForward) hashPreferencias.get(mov.IdPortafolio + "_" + mov.IdTipoOperacion + "_" + mov.IdMedioDePago + "_" + mov.IdMonedaLiquidacion);
					fp.NumeroReferencia = mov.FolioCartera;
					fp.NumeroReferenciaFormaDePago = idFp.getAndIncrement();
					fp.IdMedioDePago = mov.IdMedioDePago;
					fp.IdModalidadDePago = mov.IdModalidadPago;
					fp.IdMoneda = mov.IdMonedaLiquidacion;
					fp.Monto = montoTesoreria;
					fp.IdCuentaBancariaCorredora = 0L;
					fp.IdTarjetaBancariaCorredora = 0L;
					fp.IdChequera = 0L;
					fp.NumeroDocumentoCorredora = 0L;
					fp.FechaDocumentoPagoCorredora = "0001-01-01";
					if (fpTf != null) {
						fp.IdCuentaBancariaSocio = fpTf.IdCuentaBancaria;
						fp.IdBancoSocio = fpTf.IdBancoCuentaBancaria;
						fp.NombreCuentaBancariaSocio = fpTf.NombreBancoCuentaBancaria;
						fp.NumeroCuentaBancariaSocio = fpTf.NumeroCuentaBancaria;
						fp.IdTipoCuentaBancariaSocio = fpTf.IdTipoCuentaBancaria;
						fp.NombreTipoCuentaBancariaSocio = fpTf.NombreTipoCuentaBancaria;
						fp.IdTarjetaBancariaSocio = fpTf.IdBancoTarjetaBancaria;
						fp.IdTipoTarjetaBancariaSocio = fpTf.IdTipoTarjetaBancaria;
						fp.NombreTipoTarjetaBancariaSocio = fpTf.NombreTipoTarjetaBancaria;
						fp.NumeroTarjetaBancariaSocio = fpTf.NumeroTarjetaBancaria;
					} else {
						fp.IdCuentaBancariaSocio = 0L;
						fp.IdBancoSocio = 0L;
						fp.NombreCuentaBancariaSocio = "";
						fp.NumeroCuentaBancariaSocio = "";
						fp.IdTipoCuentaBancariaSocio = "";
						fp.NombreTipoCuentaBancariaSocio = "";
						fp.IdTarjetaBancariaSocio = 0L;
						fp.IdTipoTarjetaBancariaSocio = "";
						fp.NombreTipoTarjetaBancariaSocio = "";
						fp.NumeroTarjetaBancariaSocio = "";
					}
					fp.NumeroSerieSocio = "";
					fp.NumeroDocumentoSocio = 0L;
					fp.FechaDocumentoPagoSocio = "0001-01-01";
					return fp;
				}).collect(Collectors.toList());
				
				entradaCrearIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
				entradaCrearIngresoEgreso.p_SimularOperacion = "N";
				entradaCrearIngresoEgreso.p_UsaReglasNegocio = "S";		
				entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
				
				CrearIngresoEgreso_Salida salidaIngresoEgreso = crearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);
				HashMap<Long,Object> hashSalidaIngresoEgreso = Utils.List2Hash(salidaIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO, "NumeroReferencia");
				
				out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaIngresoEgreso.P_OUT_MENSAJES);
				
				if(out.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_MENSAJES.addAll(salidaIngresoEgreso.P_OUT_MENSAJES);
					return out;
				}
				
				out.P_OUT_INGRESOEGRESOGENERADO = out.P_OUT_INGRESOEGRESOGENERADO.stream().map(mov->{
					IngresoEgresoGenerado_Salida ingresoEgresoGenerado = (IngresoEgresoGenerado_Salida) hashSalidaIngresoEgreso.get(mov.FolioCartera);
					if(ingresoEgresoGenerado != null) {
						mov.IdTipoMovimiento = ingresoEgresoGenerado.IdTipoMovimiento;
						mov.NumeroIngresoEgreso = ingresoEgresoGenerado.NumeroReferencia;
						mov.IdIngresoEgreso = ingresoEgresoGenerado.IdIngresoEgreso;
					}
					return mov;
				}).collect(Collectors.toList());
			}
			
		}
		
		
		//CAMBIO ESTADO
		CambiarEstadoOperacionEntrada cambiarEstadoOperacionEntrada = new CambiarEstadoOperacionEntrada(entrada.p_IdEmpresa,entrada.p_IdUsuario,entrada.p_IdModulo,"S","S",entrada.p_IdPais);
		{
			CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
			cambiarEstadoOperacionEntrada = new CambiarEstadoOperacionEntrada(entrada.p_IdEmpresa,entrada.p_IdUsuario,entrada.p_IdModulo,"S","S",entrada.p_IdPais);
			List<ObjCambiarEstadoOperacionIN> p_DataCambiarEstado = out.P_OUT_INGRESOEGRESOGENERADO.stream().map(mov -> {
				ObjCambiarEstadoOperacionIN objCambiarEstadoOperacionIN = new ObjCambiarEstadoOperacionIN();
				objCambiarEstadoOperacionIN.IdOperacion = mov.IdOperacion;
				objCambiarEstadoOperacionIN.IdTipoProducto = "MEFW";
				objCambiarEstadoOperacionIN.IdNuevoEstado = "LIQ";
				objCambiarEstadoOperacionIN.IdReserva = 0L;
				objCambiarEstadoOperacionIN.FolioCarteraCustodia = mov.FolioCartera;
				objCambiarEstadoOperacionIN.MontoOperacionAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.MontoOperacionInicialAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.MontoOperacionFinalAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.TasaAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.MontoOperacionMonedaAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.MontoOperacionInicialMonedaAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.MontoOperacionFinalMonedaAnticipo = BigDecimal.ZERO;
				objCambiarEstadoOperacionIN.IdMonedaLiquidacionAnticipo = "";
				objCambiarEstadoOperacionIN.IdCondicionLiquidacionAnticipo = "";
				objCambiarEstadoOperacionIN.FechaCondicionLiquidacionAnticipo = Fecha.toDate("0001-01-01");
				return objCambiarEstadoOperacionIN;
			}).collect(Collectors.toList());
			
			cambiarEstadoOperacionEntrada.p_DataCambiarEstado.addAll(p_DataCambiarEstado);
			
			CambiarEstadoOperacionSalida cambiarEstadoOperacionSalida = cambiarEstadoOperacion.logica(con, cambiarEstadoOperacionEntrada);
			
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_MENSAJES.addAll(cambiarEstadoOperacionSalida.P_OUT_MENSAJES);
				return out;
			}
		}
		
		
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_LogDetalle = cambiarEstadoOperacionEntrada.p_DataCambiarEstado.stream().map(mov ->{
				AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
				detalle.IdCabeceraLog = 0L;
				detalle.SubRegistro = "ESTADO";
				detalle.CampoRegistro = "";
				detalle.ValorAntiguo = "VIG";
				detalle.ValorNuevo = mov.IdNuevoEstado;
				return detalle;
			}).collect(Collectors.toList());
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 0L;
			log.IdTipoLog = "CAMBIOEST";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdTipoRegistro = "FORWARD";
			log.NombreProceso = "Cambio de Estado Forward tesoreria";
			log.EstadoProceso = "VIG";
			entradaAgregarLog.p_Log.add(log);
			agregarLogService.logica(con, entradaAgregarLog);
		}
		
		sql.batch(con, SQL_UPDATE_CARTERA, out.P_OUT_INGRESOEGRESOGENERADO, IngresoEgresoGenerado.class);
		
		HashMap<String,Object> params = new HashMap<>();
		List<Long> idsCarteraParteAnticipada = new ArrayList<Long>();
		List<Long> idsOperacion = new ArrayList<Long>();
		List<Long> foliosCartera = new ArrayList<Long>();
		
		for(IngresoEgresoGenerar ope: entrada.p_IngresoEgresoGenerar){
			idsOperacion.add(ope.IdOperacion);
			foliosCartera.add(ope.FolioCartera);
			idsCarteraParteAnticipada.add(ope.IdCartera);
		}
		
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdOperacion", idsOperacion);
		params.put("FolioCartera", foliosCartera);
		params.put("IdCarteraParteAnticipada", idsCarteraParteAnticipada);
		
		List<FormaDePagoAnticipoForward> formaDePagoCompensada = sql.query(con, SQL_SELECT_FORMA_DE_PAGO_COMPENSADA, params, FormaDePagoAnticipoForward.class);
		
		if (formaDePagoCompensada.size() > 0 ) {
			List<UpdateComprobantePagoOperacion> updateFechaLiquidacion = new ArrayList<>();
			
			for(FormaDePagoAnticipoForward datosFormaPago: formaDePagoCompensada){
				UpdateComprobantePagoOperacion datosUpdateSQL = new UpdateComprobantePagoOperacion();
				datosUpdateSQL.FechaAnticipo = Fecha.toDate(fechaSistema);
				datosUpdateSQL.IdComprobantePagoOperacion = datosFormaPago.IdComprobantePagoOperacion;
		
				updateFechaLiquidacion.add(datosUpdateSQL);	
			}
			
			//Update fecha de liquidacion a la fecha de anticipo
			if (updateFechaLiquidacion.size() > 0) {	
				sql.batch(con, SQL_UPDATE_COMPROBANTE_PAGO_OPERACION, updateFechaLiquidacion, UpdateComprobantePagoOperacion.class);
			}
		}
		
		logger.info("FIN::CrearMovimientoForwardTesoreria");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonUF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MFW\",\"p_IdPais\":44,\"p_IngresoEgresoGenerar\":[{\"IdCartera\":83842430,\"FolioCartera\":1465932,\"IdOperacion\":194922,\"IdEstado\":\"VCD\",\"MontoOperacion\":4932000,\"IdMonedaLiquidacion\":\"CLP\"}]}";
			String jsonUSD = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MFW\",\"p_IdPais\":44,\"p_IngresoEgresoGenerar\":[{\"IdCartera\":83842432,\"FolioCartera\":1465934,\"IdOperacion\":194924,\"IdEstado\":\"VCD\",\"MontoOperacion\":47.05882352941177,\"IdMonedaLiquidacion\":\"USD\"}]}";
			EntradaCrearMovimientoForwardTesoreria Objin = gson.fromJson(jsonUF, EntradaCrearMovimientoForwardTesoreria.class);
			
			CrearMovimientoForwardTesoreria demo=new CrearMovimientoForwardTesoreria();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearMovimientoForwardTesoreria ObjOut= demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
 				er.printStackTrace();
				con.rollback();
			}
 			
	    }
		 	
		System.out.println("== END ==");
	}

}