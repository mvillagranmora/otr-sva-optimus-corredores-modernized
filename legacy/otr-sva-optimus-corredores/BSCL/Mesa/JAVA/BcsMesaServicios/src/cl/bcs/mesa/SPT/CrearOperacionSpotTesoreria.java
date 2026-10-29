package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpotFacturacion.FormaPagoSpotFacturacionObjIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionSpotTesoreria implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearOperacionSpotTesoreria.class);
	
	private final String SQL_CONSULTAR_PERSONA_CLIENTE = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearOperacionSpotTesoreria_SelectClientesPersona.sql");
	private static final String SI = "S";
	
	public static class CrearOperacionSpotTesoreriaEntrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionSpot> p_DatosOperacion;
		public List<FormaPagoSpotFacturacionObjIn> p_MediosDePago;
		
		public CrearOperacionSpotTesoreriaEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionSpot>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoCuenta extends BcsPojo {
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String NombreBanco;
		public String NombreTipoCuentaBancaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionSpotTesoreriaSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public CrearOperacionSpotTesoreriaSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionSpotTesoreriaClientes {
		public Long	IdCliente;
		public Long	IdPersona;
		public Long IdEjecutivo;
		public Long IdSucursal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public CrearOperacionSpotTesoreriaSalida logica(Connection con, CrearOperacionSpotTesoreriaEntrada in) {
		CrearOperacionSpotTesoreriaSalida out = new CrearOperacionSpotTesoreriaSalida();
		
		if (in.p_DatosOperacion.size() == 0L) {
			return out;
		}
		
		try {
			SQL _sql = new SQL();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
			entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
			entradaCrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearIngresoEgreso.p_IdModulo = in.p_IdModulo;
			entradaCrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
			entradaCrearIngresoEgreso.p_SimularOperacion = in.p_SimularOperacion;
			entradaCrearIngresoEgreso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaCrearIngresoEgreso.p_FormaDePago = new ArrayList<>();
			entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = new ArrayList<>();
			entradaCrearIngresoEgreso.p_FechaMovimiento = in.p_DatosOperacion.get(0).FechaOperacion.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
			HashMap<Long, Object> hashMapClientes = new HashMap<>();
			
			{
				HashMap<String, Object> paramsClientes = new HashMap<>(1);
				paramsClientes.put("IdClientes", in.p_DatosOperacion.stream().map(op -> op.IdCliente).collect(Collectors.toList()));
				
				List<CrearOperacionSpotTesoreriaClientes> clientes = _sql.query(con, SQL_CONSULTAR_PERSONA_CLIENTE, paramsClientes, CrearOperacionSpotTesoreriaClientes.class);
				
				hashMapClientes = Utils.List2Hash(clientes, "IdCliente");
			}
			
			HashMap<Long, Object> operacionesMap = Utils.List2Hash(in.p_DatosOperacion, "IdOperacionSpot");
			
			AtomicInteger ati = new AtomicInteger();
			AtomicLong refFormaPago = new AtomicLong();
						
			List<ObjCargoAbonoAGenerar> listCargoAbono = new ArrayList<>();
			
			for (FormaPagoSpotFacturacionObjIn pag: in.p_MediosDePago) {
				OperacionSpot op = (OperacionSpot) operacionesMap.get(pag.IdOperacion);
				CrearOperacionSpotTesoreriaClientes cliente = (CrearOperacionSpotTesoreriaClientes) hashMapClientes.get(op.IdCliente);

				if (op != null && !SI.equalsIgnoreCase(op.OperacionFicticia)) {
					continue;
				}
				
				String IdConcepto= "COMSPT".equalsIgnoreCase(op.IdTipoOperacion) ? "CMMESP" : "VNMESP";
				String IdTipoOperacion = op.IdTipoOperacion;

				ObjCargoAbonoAGenerar cab = new ObjCargoAbonoAGenerar();
				
				cab.NumeroCargoAbono = (op.IdOperacionSpot + ati.getAndIncrement()) * -1;
				cab.IdPortafolio = op.IdPortafolio;
				cab.IdMoneda = pag.IdMoneda;
				
				if ("MOVING".equalsIgnoreCase(pag.IdTipoMovimiento)) {
					cab.IdTipoMovimiento = "MOVCAR";
				} else if ("MOVEGR".equalsIgnoreCase(pag.IdTipoMovimiento)) {
					cab.IdTipoMovimiento = "MOVABN";
				} else {
					cab.IdTipoMovimiento = "";
				}
				
				if("ARBINT".equalsIgnoreCase(op.Nemotecnico) || "ARBDIS".equalsIgnoreCase(op.Nemotecnico)){
					IdConcepto = "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimiento) ? "VNMESP" : "CMMESP";
					IdTipoOperacion = "MOVEGR".equalsIgnoreCase(pag.IdTipoMovimiento) ? "VENSPT" : "COMSPT";
				}
				
				cab.Descripcion = "";
				cab.IdCondicionDeLiquidacion = "";
				cab.FechaCondicionDeLiquidacion = pag.FechaLiquidacionComprobantePagoOperacion;
				cab.Monto = pag.Monto.abs();					
				cab.IdEjecutivo = cliente.IdEjecutivo;
				cab.IdSucursal = cliente.IdSucursal;
				cab.IdTipoProducto = op.IdTipoProducto;
				cab.IdTipoOperacion = IdTipoOperacion;
				cab.IdConcepto = IdConcepto;

				cab.IdOperacion = op.IdOperacionSpot;
				
				cab.FechaLiquidacionOperacion = pag.FechaLiquidacionComprobantePagoOperacion;
				cab.FechaVencimientoOperacion = "0001-01-01";
				cab.IdMonedaOperacion = pag.IdMoneda;
				cab.MontoOperacion = pag.Monto.abs();
				cab.TipoCambioOperacion = BigDecimal.ZERO;
				cab.IdContraparte = 0L;
				cab.IdTipoComprobante = "";
				cab.IdComprobante = 0L;
				cab.FolioComprobante = 0L+"";
				cab.IdComprobanteDetalle = 0L;
				cab.IdComprobanteFormaDePago = 0L;
				cab.IdTipoMovimientoAsociado = "";
				cab.IdMovimientoAsociado = 0L;
				cab.FechaComprobante = "0001-01-01";
				
				if (cab.IdTipoMovimientoAsociado == null) {
					cab.IdTipoMovimientoAsociado = "";
				}
				
				if (cab.IdMovimientoAsociado == null ) {
					cab.IdMovimientoAsociado = 0L;
				}
				
				cab.IdPersonaPagoCobro = 0L;
				cab.IdInstrumento = op.IdInstrumento;
				cab.IdTipoInstrumento = op.IdTipoInstrumento;
				cab.Nemo = op.Nemotecnico;
				cab.EsIndicadorBilateral = "N";
				cab.EsCargoAbonoPorTraspaso = "N";
				cab.IdPortafolioTraspaso = 0L;
				if ("".equalsIgnoreCase(cab.IdTipoMovimientoAsociado)) {
					cab.GeneroContraparteEnTesoreria = "N";
				}
				else {
					cab.GeneroContraparteEnTesoreria = "S";
				}
				cab.CertificadoAutomatico = "S";
				cab.Cantidad = BigDecimal.ZERO;
				
				listCargoAbono.add(cab);

				// TESORERIA
				if (!SI.equalsIgnoreCase(pag.AfectarTesoreria)) {
					continue;
				}

				IngresoEgreso_Entrada ingEgr = new IngresoEgreso_Entrada();
				

				ingEgr.NumeroReferencia = (op.IdOperacionSpot + ati.getAndIncrement()) * -1 ;
				ingEgr.IdTipoMovimiento = pag.IdTipoMovimiento;
				ingEgr.IdTipoPersona = "CLI";
				ingEgr.IdPersona = cliente.IdPersona;
				ingEgr.IdPortafolio = op.IdPortafolio;
				ingEgr.IdMoneda = pag.IdMoneda;
				ingEgr.Monto = pag.Monto;
				ingEgr.FechaLiquidacion = pag.FechaLiquidacionComprobantePagoOperacion;
				ingEgr.IdTipoPersonaTitularPago = "CLI";
				ingEgr.IdPersonaTitularPago = cliente.IdPersona;
				ingEgr.IdPortafolioTitularPago = op.IdPortafolio;
				ingEgr.IdCaja = 0L;
				ingEgr.SecuenciaAgrupacion = "";
				ingEgr.SecuenciaRelacionada = op.IdOperacionSpotOrigen;
				ingEgr.IdEmpleadoDigitador = 0L;
				ingEgr.IdSucursalEmpleadoDigitador = 0L;
				ingEgr.IdTipoProducto = op.IdTipoProducto;
				ingEgr.IdTipoOperacion = IdTipoOperacion;
				ingEgr.IdConcepto = IdConcepto;
				ingEgr.IdTipoComprobante = "MES";
				ingEgr.IdComprobante = op.IdOperacionSpotOrigen;
				ingEgr.FechaComprobante = fechaSistema;
				ingEgr.FechaLiquidacionComprobante = pag.FechaLiquidacionComprobantePagoOperacion;
				ingEgr.IdOperacion = op.IdOperacionSpot;
				ingEgr.FechaOperacion = Fecha.toStringDate(op.FechaOperacion);
				ingEgr.IdCondicionLiquidacion = pag.IdCondicionDeLiquidacion;
				ingEgr.IdBolsa = "";
				ingEgr.IdMonedaOperacion = "";
				ingEgr.MontoOperacion = BigDecimal.ZERO;
				ingEgr.IdTipoReajuste = "";
				ingEgr.Glosa = op.Observacion;
				ingEgr.FechaParidad = "0001-01-01";
				ingEgr.ValorParidad = BigDecimal.ZERO;
				ingEgr.FolioCartera = 0L;
				ingEgr.IdInstrumento = (op.IdInstrumento == null ? 0 : op.IdInstrumento);
				ingEgr.IdTipoInstrumento = "SPT";
				ingEgr.IdClasificacionTipoInstrumento = "SP";
				ingEgr.Bilateral = "N";
				ingEgr.GeneraComprobante = "N";
				ingEgr.AgruparEnComprobante = "S";
				ingEgr.MueveCuentaInversion = "S";
				ingEgr.TitularPago = "N";
				ingEgr.IdTipoCanalIngreso = op.IdTipoCanalIngreso != null ? op.IdTipoCanalIngreso : "MES" ; 
				
				entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.add(ingEgr);
				
				FormaDePago_Entrada fpag = new FormaDePago_Entrada();
								
				fpag.NumeroReferencia = ingEgr.NumeroReferencia;
				fpag.NumeroReferenciaFormaDePago = refFormaPago.getAndIncrement();
				fpag.IdMedioDePago = pag.IdMedioDePago;
				fpag.IdModalidadDePago = pag.IdModalidadPago != null ? pag.IdModalidadPago : "MODCAJ";
				fpag.IdMoneda = pag.IdMoneda;
				fpag.Monto = pag.Monto;
				fpag.IdCuentaBancariaCorredora = 0L;
				fpag.IdTarjetaBancariaCorredora = 0L;
				fpag.IdChequera = 0L;
				fpag.NumeroDocumentoCorredora = 0L;
				fpag.IdCuentaBancariaSocio = pag.IdCuentaBancaria;
				fpag.IdBancoSocio = pag.IdBanco;
				fpag.NombreCuentaBancariaSocio = "";
				fpag.NumeroCuentaBancariaSocio = pag.NumeroCuentaBancaria;
				fpag.IdTipoCuentaBancariaSocio = pag.IdTipoCuentaBancaria;
				fpag.NombreTipoCuentaBancariaSocio = "";
				fpag.IdTarjetaBancariaSocio = 0L;
				fpag.IdTipoTarjetaBancariaSocio = "";
				fpag.NombreTipoTarjetaBancariaSocio = "";
				fpag.NumeroTarjetaBancariaSocio = "";
				fpag.NumeroSerieSocio = "";
				fpag.NumeroDocumentoSocio = 0L;
				fpag.FechaDocumentoPagoSocio = pag.FechaLiquidacionComprobantePagoOperacion;
				
				if(esFormaPagoSpotCorredora(ingEgr.IdConcepto,pag.IdTipoMovimiento)) {
					fpag.IdCuentaBancariaCorredora = pag.IdCuentaBancaria;
					fpag.IdChequera = 0L;
					fpag.FechaDocumentoPagoCorredora =  pag.FechaLiquidacionComprobantePagoOperacion;
					fpag.IdCuentaBancariaSocio = 0L;
					fpag.IdBancoSocio = 0L;
					fpag.NombreCuentaBancariaSocio = "";
					fpag.NumeroCuentaBancariaSocio = "";
					fpag.IdTipoCuentaBancariaSocio = "";
					fpag.NombreTipoCuentaBancariaSocio = "";
					fpag.IdTarjetaBancariaSocio = 0L;
					fpag.IdTipoTarjetaBancariaSocio = "";
					fpag.NombreTipoTarjetaBancariaSocio = "";
					fpag.NumeroTarjetaBancariaSocio = "";
					fpag.NumeroSerieSocio = "";
					fpag.NumeroDocumentoSocio = 0L;
					fpag.FechaDocumentoPagoSocio = "0001-01-01";
				}
				else
				{
					fpag.IdCuentaBancariaCorredora = 0L;
					fpag.IdTarjetaBancariaCorredora = 0L;
					fpag.IdChequera = 0L;
					fpag.NumeroDocumentoCorredora = 0L;
					fpag.FechaDocumentoPagoCorredora = "0001-01-01";
				}
				entradaCrearIngresoEgreso.p_FormaDePago.add(fpag);
			}
			
			if (listCargoAbono.size() > 0) {
				CrearCargoAbono serviceCrearCargoAbono = new CrearCargoAbono();
				CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
				entradaCrearCargoAbono.p_CargoAbonoAGenerar = listCargoAbono;				
				entradaCrearCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearCargoAbono.p_IdModulo = in.p_IdModulo;
				entradaCrearCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaCrearCargoAbono.p_simularOperacion = in.p_SimularOperacion;
				entradaCrearCargoAbono.p_EsIngresoManual = "S";
				entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
				CrearCargoAbonoSalida salidaCrearCargoAbono = serviceCrearCargoAbono.logica(con, entradaCrearCargoAbono);
				out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR > 0L) {
					return out;
				}
			}

			if (entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size() > 0) {
				CrearIngresoEgreso ingresoEgresoService = new CrearIngresoEgreso();

				CrearIngresoEgreso_Salida salidaIngresoEgreso = ingresoEgresoService.logica(con, entradaCrearIngresoEgreso);
		
				out.P_OUT_ERRORESCONTADOR += salidaIngresoEgreso.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaIngresoEgreso.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR > 0L) {
					return out;
				}
			}
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR++;
			e.printStackTrace();
			logger.error(e);
		}
		
		return out;
	}
	
	
	public Boolean esFormaPagoSpotCorredora(String IdConcepto ,String IdTipoMovimientoSpot) {
		if(("CMMESP").equalsIgnoreCase(IdConcepto) && ("MOVING").equalsIgnoreCase(IdTipoMovimientoSpot) ||
			("VNMESP").equalsIgnoreCase(IdConcepto) && ("MOVING").equalsIgnoreCase(IdTipoMovimientoSpot)) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
