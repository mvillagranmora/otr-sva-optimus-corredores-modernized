// GENERATED DOUBLE of the legacy cl.bcs.facturacion.CrearComprobante by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.facturacion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import com.google.gson.JsonObject;



import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.facturacion.pojo.ComprobanteCobro;
import cl.bcs.facturacion.pojo.ComprobanteDetalle;
import cl.bcs.facturacion.pojo.ComprobanteFormaDePago;
import cl.bcs.facturacion.pojo.MovimientoComprobanteCobro;
import cl.bcs.facturacion.pojo.MovimientoComprobanteDetalle;
import cl.bcs.facturacion.pojo.MovimientoComprobanteFormaDePago;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearComprobante implements JavaCall,JavaCallMetadata {

	
	public final List<String> tiposFactura = Arrays.asList("FAC", "FAE", "BOE", "FOE","FOC", "FEE", "FOX","POL","BEE");
	public final List<String> tiposNotaCredito = Arrays.asList("NCE", "NCR", "NCO", "NEE", "NEO");
	public final List<String> tiposNotaCreditoEsp = Arrays.asList("NEO");
	public final List<String> tiposCobroFijo = Arrays.asList("COFI", "DEFI", "GAFI","IVA","GFFA","GFOD");
	public final List<String> tiposNotaDebito = Arrays.asList("NDE");
	public List<String> camposComprobanteDetalle = new ArrayList<>();
	public List<String> camposComprobanteDetalleVentaCorta = new ArrayList<>();
	public List<String> camposComprobanteDetallePrecioPromedioRV = new ArrayList<>();
	
	public List<String> camposComprobante = new ArrayList<>(); 
	
	List<MovimientoComprobante_Proceso> movimientos = new ArrayList<MovimientoComprobante_Proceso>();
	List<MovimientoComprobante_Proceso> movimientosError = new ArrayList<MovimientoComprobante_Proceso>();
	
	public void llenarLlaves(ConfiguracionFacturacion conf) { throw new UnsupportedOperationException("oracle double CrearComprobante.llenarLlaves: not part of the double"); }
	
	//ENTRADAS
	public static class CrearComprobante_Entrada extends BcsPojo {		
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;   
		public String							p_IdModulo;   
		public String							p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
		public String							p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
		public String							p_EsIngresoAutomatico = "N";	// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
	    

		public String         					p_GeneraComprobanteTesoreria = null;
		
		
		public String							p_Glosa = "";
	    public List<MovimientoComprobante_Entrada>	P_MOVIMIENTOFACTURAR;
		public String							p_ServicioAgrupador = "";
		
	    public CrearComprobante_Entrada (){
	    	P_MOVIMIENTOFACTURAR	= new ArrayList<MovimientoComprobante_Entrada>();
		}
	}
	
	public static class MovimientoComprobante_Entrada extends BcsPojo {
		public Long					IdMovimientoComprobante;
		public String				FechaComprobante;
		public String				IdConcepto;
		public Long 				IdCliente;
		public Long 				IdPortafolio;
	}
	
	//SALIDAS
	public static class ResultadoProceso_Salida extends BcsPojo {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
	}
	
	public static class CrearComprobante_Salida extends BcsPojo {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<MovimientoCtaInversion_Salida>	P_OUT_CARGOSABONOSCTAINVERSION;
		public List<MovimientoTesoreria_Salida>		P_OUT_INGRESOSEGRESOSTESORERIA;
		public List<MovimientoCustodia_Salida>		P_OUT_INGRESOSRETIROSCUSTODIA;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		
		
		public CrearComprobante_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_COMPROBANTE = new ArrayList<>();
			P_OUT_CARGOSABONOSCTAINVERSION = new ArrayList<>();
			P_OUT_INGRESOSEGRESOSTESORERIA = new ArrayList<>();
			P_OUT_INGRESOSRETIROSCUSTODIA = new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class MovimientoCtaInversion_Salida extends BcsPojo {
		public Long 	IdCargoAbonoMovimiento;
		public String 	IdTipoMovimiento;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	public static class MovimientoTesoreria_Salida extends BcsPojo {
		public Long 	IdIngresoEgreso;
		public String 	IdTipoMovimiento;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	public static class Comprobante_Salida extends BcsPojo {
		public Long	IdComprobante;
		public Long	IdMovimientoComprobante;
	}
	
	public static class MovimientoCustodia_Salida extends BcsPojo {
		public Long 	FolioCustodia;
		public Long		IdMovimientoComprobante;
		public Long		IdComprobante;
	}
	
	//PROCESOS
	public static class InfoMovimientoComprobante_Proceso extends BcsPojo{
		public String Accion;
		public String ActualizarCustodia;
		public String ActualizarTesoreria;
		public String ActualizarCuentaInversion;
		
		public String CodigoIFRS;
		public BigDecimal Cantidad;
		public String CobraPrimaAlInicio;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public BigDecimal CorteMinimo;
		
		public BigDecimal DiasPlazo;
		public String DireccionFacturacion;
		public String DocumentoIdentidad;
		
		public String EsAPlazo;
		public String EsComprobIndividual;
		public String EsVentaCorta;
		public String EsEnRueda;
		public String EsFungible;
		public String EsClienteFinal;
		
		public String FolioTransaccion;
		public java.sql.Timestamp FechaTransaccion;
		public Date FechaLiquidacion;
		public Long FolioComprobanteReferencia;
		public Date FechaComprobanteReferencia;
		public java.sql.Timestamp FechaEmision;
		public Date FechaVencimientoInstrumentoRelacionado;
		public Date FechaEmisionInstrumentoRelacionado;
		public java.sql.Timestamp FechaMovimientoComprobante;
		public Long FolioCartera;
		public java.sql.Timestamp FechaOrden;
		public java.sql.Timestamp FechaVencimiento;
		
		public Long IdComprobante;
		public Long IdCuentaCustodioPortafolio;
		public Long IdMovimientoComprobante;
		public Long IdMovimientoRelacionado;
		public String IdEstadoFacturacion;
		public String IndicadorOrdenDirecta;
		public Long IdTransaccion;
		public String IdTipoReajuste;
		public String IdTipoInstrumentoSubyacente;
		public String IdTipoInstrumentoRelacionado;
		public String IdTipoInstrumento;
		public Long IdSucursalEmpleadoEjecutivoPortafolio;
		public String IdMonedaSubyacente;
		public String IdMonedaMovimientoComprobante;
		public String IdMonedaLiquidacion;
		public String IdMoneda;
		public String IdMonedaSpot;
		public Long IdOperacion;
		public String IdMonedaEmision;
		public Long IdMesa;
		public String IdMonedaNegociacion;
		public Long IdInstrumentoSubyacente;
		public Long IdInstrumentoRelacionado;
		public Long IdInstrumentoPrestamo;
		public Long IdInstrumento;
		public String IdFormaCustodia;
		public Long IdEmpleadoEjecutivoPortafolio;
		public String IdConceptoMovimiento;
		public String IdClasificacionTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumento;
		public Long IdCliente;
		public String IdBolsa;
		public Long IdEmpresa;
		public String IdCondicionDeLiquidacion;
		public Long IdPortafolio;
		public String IdTipoComprobante;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdBoveda;
		public String IdTipoCanalIngreso;
		public Long IdComprobanteReferencia;
		public String IdTipoComprobanteReferencia;
		public String IdMonedaInstrumentoRelacionado;
		public String IdTipoReajusteRelacionado;
		public String IdEmisorRelacionado;
		public String IdEmisorSubyacente;
		public String IdTipoPrestamo;
		public String IdEmisor;
		public BigDecimal Intereses;
		public Long IdOrden;
		public Long IdPersonaBancoLiquidador;
		public Long IdPersonaBancoCustodio;
		public Long IdPersona;
		public Long IdPersonaBroker;
		public String IdTipoCliente;
		public String IdTipoLiquidacionComisionFactura;
		
		public String LiquidaBancoCustodio;
		
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoRaiz;
		public String ModPrefFact;
		public BigDecimal MontoAPlazo;
		public String ModalidadSMV;
		public String MercadoSMV;
		
		public String NombreTipoInstrumentoSubyacente;
		public String NombreTipoInstrumentoRelacionado;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumento;
		public String NemotecnicoSubyacente;
		public String NemotecnicoRelacionado;
		public String NemotecnicoInstrumento;
		public String NemoSVS;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreLargoCliente;
		public String NombreTipoCliente;
		
		public BigDecimal Precio;
		public BigDecimal PrecioRaiz;
		public BigDecimal PrecioAPlazo;
		public BigDecimal TasaEmision;
		
		public BigDecimal ReajustesVAC;
		
		public Long TamanoRelacionado;
		public String TipoDocumentoIdentidad;
		public String TipoComprobanteFacturacion;
		
		public BigDecimal Tasa;				//TASA RF IF SIM
		public BigDecimal TasaOperacion;	//PARA VENTA CORTA
		public BigDecimal ValorTipoCambio;
		public BigDecimal ValorRescate;
		public BigDecimal ValorParidad;
		public BigDecimal ValorCaptacionMonedaOrigen;
		
		public String     Glosa;
		public String     CodigoPortafolio;
		
		public String Referencia;
		public String TipoReferencia;
		
		public String Contrapartida;
		public Date FechaVencimientoInstrumento;
		public String EsExento;
		
		public String     DeatalleOperacion;
		
		public InfoMovimientoComprobante_Proceso() {
			this.IdTipoLiquidacionComisionFactura = "";
			this.ModPrefFact = "";
			this.Contrapartida = "N";
			this.EsExento = "N";
		}
	}
	
	public static class InfoMovimientoSpot_Proceso extends BcsPojo {
		public Long IdOperacionSpot;
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String IdTipoCanalIngreso;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String IdRegimenTributario;
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdPersonaEmisor;
		public Long IdCuentaBancariaCorredora;
	}
	
	public static class EstadoCajaSpot extends BcsPojo {
		public Long	 IdMovimientoComprobante;
		public String EstadoCaja;
		
		public EstadoCajaSpot() {
			this.EstadoCaja = "NRE";
		}
	}
	
	public static class InfoMovimientoSpotIntegracion_Proceso extends BcsPojo {
		public Long IdMovimientoComprobante;
		public Long  IdOperacionSpot;
		public String Estado;
	}
	
	public static class MovimientoComprobanteFormaDePago_Proceso extends MovimientoComprobanteFormaDePago{		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante_Proceso extends BcsPojo {
		public Long IdMovimientoComprobante;
		public String FechaComprobante;
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		public BigDecimal Monto;

		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
	}
	
	public static class MovimientoComprobanteCobroFijos extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long Cantidad;
	}
	
	public static class PorcentajeRetencionRegimenAPVB extends BcsPojo {
		public String RegimenTributario;
		public BigDecimal PorcentajeRetencion;
	}
	
	public static class MovimientoComprobanteExistente_Proceso extends BcsPojo {
		public String LlavePortafolioClasificacion;
	}
	
	public static class Cobro  extends BcsPojo {
        public String      IdCobro;
        public BigDecimal  ValorCobro;
    }

	public static class ComprobanteDetalle_Proceso extends ComprobanteDetalle {
		public Long IdCliente;
		public Long IdPortafolio;
		public String llaveComprobanteDetalle;
		public String Glosa;
		
		public List<MovimientoComprobanteDetalle_Proceso> movimientoComprobanteDetalles;
		
		public ComprobanteDetalle_Proceso() {
			movimientoComprobanteDetalles = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class ComprobanteFormaDePago_Proceso extends ComprobanteFormaDePago {
		public String Accion;
		public String IdTipoMovimientoSpot;
		public String AfectarTesoreria;
		public String AfectarCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteDetalle_Proceso extends MovimientoComprobanteDetalle {
		public MovimientoComprobante_Proceso movimientoComprobanteProceso;
		
		public String Accion;
		public String ActualizarTesoreria;
		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public BigDecimal CorteMinimo;
		public BigDecimal DiasPlazo;
		public String DireccionFacturacion;
		public String DocumentoIdentidad;
		public String EsClienteFinal;
		public String EsComprobIndividual;
		public String EsAPlazo;
		public String EsFungible;
		public java.sql.Timestamp FechaComprobante;
		public Date FechaComprobanteReferencia;
		public java.sql.Date FechaLiquidacion;
		public Timestamp FechaVencimiento;
		public Long FolioCartera; //para vender
		public Long FolioComprobanteReferencia;
		public String Glosa;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCliente;
		public Long IdComprobante;
		public Long IdComprobanteReferencia;
		public String IdCondicionDeLiquidacion;
		public Long IdCuentaCustodioPortafolio;
		public String IdEmisor;
		public String IdEmisorSubyacente;
		public Long IdEmpresa;
		public Long IdOrden;
		public Long IdPersona;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public Long IdPersonaBroker;
		public Long IdPortafolio;
		public String IdTipoCanalIngreso;
		public String IdTipoCliente;
		public String IdTipoComprobante;
		public String IdTipoComprobanteReferencia;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public BigDecimal Intereses;
		public String LiquidaBancoCustodio;
		public String MercadoSMV;
		public String ModalidadSMV;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreLargoCliente;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreTipoCliente;
		public BigDecimal ReajustesVAC;
		public String TipoDocumentoIdentidad;
		public String TipoComprobanteFacturacion;
		
		public Long IdOperacion;

		public BigDecimal MontoAPlazo;
		public BigDecimal PrecioAPlazo;
		public String CodigoPortafolio;
		
		public String Contrapartida;
		public Date FechaVencimientoInstrumento;
		public String EsExento;
		
		
		public MovimientoComprobanteDetalle_Proceso() {
			this.EsExento = "N";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobro_Proceso extends MovimientoComprobanteCobro {
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CodigoPortafolio extends BcsPojo {
		public String CodigoPortafolio;
	}
	
	public static class MovimientoFusionVC extends BcsPojo {
		public Long IdMovimientoComprobante;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal Precio;
	}

	
	public static class ConfiguracionFacturacion extends BcsPojo {
		public String SeparaPorEjecutivo;
		public String SeparaPorSucursal;
		public String SeparaPorBoveda;
	}
	
	public static class PreferenciaFacturacion extends BcsPojo {
		public Long IdPortafolio;
		public String FusionaComprobanteDetallePorPrecio;
		public String SeparaComprobantePorInstrumento;
		public String SeparaComprobanteDetallePorTransaccion;
		public String ModoFacturacion;
		public String FusionFacturaVentaCorta;
		public String FusionFacturaPrecioPromedioRV;
	}
	
	public static class SelectCondicionDeLiquidacion extends BcsPojo {
		public String	IdCondicionDeLiquidacion;
		public String	Codigo;
		public String	Nombre;
		public Long		PlazoEnDias;
	}
	
	public static class SelectTipoPrestamo extends BcsPojo {
		public Long		IdInstrumentoPrestamo;
		public String	IdTipoPrestamo;
		public String	TipoPrestamoSistemaExterno;
	}
	
	public static class PersonaTGR extends BcsPojo{
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		
		public PersonaTGR(){
			this.IdPersona = 0L;
			this.IdCuentaBancaria = 0L;
			this.IdBanco = 0L;
			this.IdTipoCuentaBancaria = "";
			this.IdMoneda = "";
			this.NumeroCuentaBancaria = "";
			this.NombreTipoCuentaBancaria = "";
		}
	}
	
	public static class MovimientoTesoreria_Proceso extends BcsPojo{
		public Long IdComprobanteOrigen;
		public Long Id;
		public String NombreTipoMovimiento;
		public String IdEstadoComprobante;
		public BigDecimal Monto;
		public String IdTipoMovimiento;
		public String IdConcepto;
		public String IdMoneda;
		public String IdTipoOperacion;
		public String IdTipoProducto;
	}
	
	public static class ComprobanteLiquidadoTesoreria extends BcsPojo{
		public String IdEstado;
		public BigDecimal Monto;
		public String IdTipoOperacion;
		public String IdMoneda;
		public Long IdComprobanteOrigen;
		public Long IdComprobante;
		public Long IdIngresoEgreso;
	}
	
	public static class MovimientoNoLiquidadoTesoreria extends BcsPojo{
		public Long IdComprobanteOrigen;
		public String IdEstadoComprobante;
		public BigDecimal Monto;
		public Long IdCliente;
		public String NombrePersona;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdMoneda;
		public String Nemotecnico;
		public String IdEstado;
		
	}
	
	public static class ComprobanteNoLiquidadoTesoreria extends BcsPojo{
		public Long IdComprobante;
		public String FechaComprobanteOrigen;
	}
	
	public static class ComprobanteCobro_Proceso extends ComprobanteCobro {
		
	}
	
	public static class Comprobante_Proceso extends Comprobante {
		
		public String llaveComprobante;
		public int advertencias;
		public int autorizaciones;
		public int errores;

		public String Accion;
		public String ActualizarTesoreria;
		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String EsComprobIndividual;
		public Long IdPersona;
		public String TipoComprobanteFacturacion;
		
		public List<ComprobanteDetalle_Proceso> comprobanteDetalles;
		public List<ComprobanteFormaDePago_Proceso> comprobanteFormasDePago;
		public HashMap<String,ComprobanteFormaDePago_Proceso> hashComprobanteFormasDePago = new HashMap<>(); //para simular el group by
		public List<ComprobanteCobro_Proceso> comprobanteCobros;
		public HashMap<String,ComprobanteCobro_Proceso> hashComprobanteCobros; //para simular el group by
		public List<MovimientoComprobanteDetalle_Proceso> movimientosComprobanteDetalle;
		public String EstadoCaja;
		public Comprobante_Proceso() {
			comprobanteDetalles = new ArrayList<>();
			comprobanteFormasDePago = new ArrayList<>();
			movimientosComprobanteDetalle = new ArrayList<>();
			comprobanteCobros = new ArrayList<>();
			hashComprobanteCobros = new HashMap<>();
			hashComprobanteFormasDePago = new HashMap<>();
			
			advertencias = 0;
			autorizaciones = 0;
			errores = 0;
			this.EstadoCaja = "NRE";
		}
		
		public void agregarComprobanteCobro(ComprobanteCobro_Proceso cobro) {
			
			String llaveCobro = cobro.IdCobro + cobro.IdComprobante + cobro.IdMonedaCobro + cobro.IdMonedaComprobanteCobro + cobro.IdMonedaLiquidacionCobro;
			
			ComprobanteCobro_Proceso cobroExistente = hashComprobanteCobros.get(llaveCobro);
			if (cobroExistente != null) {
				//SUMA MONTOS
				cobroExistente.MontoCobro = cobroExistente.MontoCobro.add(cobro.MontoCobro);				
				cobroExistente.MontoComprobanteCobro = cobroExistente.MontoComprobanteCobro.add(cobro.MontoComprobanteCobro);
				cobroExistente.MontoLiquidacionCobro = cobroExistente.MontoLiquidacionCobro.add(cobro.MontoLiquidacionCobro);
				hashComprobanteCobros.put(llaveCobro, cobroExistente);
			}
			else {
				comprobanteCobros.add(cobro);
				hashComprobanteCobros.put(llaveCobro, cobro);
			}
		}
		
		public void agregarComprobanteFormaDePago(ComprobanteFormaDePago_Proceso fpag) {
			String llaveFormaDePago = fpag.IdComprobante + this.IdTipoComprobante + this.Accion + fpag.IdMedioDePago + fpag.NumeroSerie + fpag.NumeroDocumento + fpag.IdCuentaBancaria
					+ fpag.IdTipoCuentaBancaria + fpag.NombreTipoCuentaBancaria + fpag.NumeroCuentaBancaria + fpag.IdTarjetaBancaria + fpag.IdTipoTarjetaBancaria + fpag.NombreTipoTarjetaBancaria 
					+ fpag.NumeroTarjetaBancaria + fpag.FechaVctoTarjetaBancaria.toString() + fpag.IdModalidadDePago + fpag.IdBanco + fpag.IdMoneda + fpag.FechaDocumentoPago.toString() 
					+ fpag.IdTipoMovimientoSpot + fpag.AfectarTesoreria + fpag.AfectarCuentaInversion;
			
			//TODO Para chile falta agregar IdTipoMovimientoSpot,AfectarTesoreria a la llave
			ComprobanteFormaDePago_Proceso fpagExistente = hashComprobanteFormasDePago.get(llaveFormaDePago);
			if (fpagExistente != null) {
				//SUMA MONTOS
				fpagExistente.MontoLiquidacionFormaDePago = fpagExistente.MontoLiquidacionFormaDePago.add(fpag.MontoLiquidacionFormaDePago);
				hashComprobanteFormasDePago.put(llaveFormaDePago, fpagExistente);
			}
			else {
				comprobanteFormasDePago.add(fpag);
				hashComprobanteFormasDePago.put(llaveFormaDePago, fpag);
			}
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GlosaTipoInstrumento extends BcsPojo {
		public String IdGlosa;
		public String IdTipoInstrumento;
		public String Nombre;
		public Long Prioridad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearComprobante", "call", new Object[]{conn, in}, JsonObject.class); }
	
	@Override
	public void setMetadata(Object in,Object out) { throw new UnsupportedOperationException("oracle double CrearComprobante.setMetadata: not part of the double"); }
	
	
	public String construirGlosaLarga(MovimientoComprobanteDetalle_Proceso movdet ,ComprobanteDetalle_Proceso compros, List<GlosaTipoInstrumento> listaGlosas ) { throw new UnsupportedOperationException("oracle double CrearComprobante.construirGlosaLarga: not part of the double"); }
	
	@Trace
	public CrearComprobante_Salida logica(Connection conn, CrearComprobante_Entrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearComprobante", "logica", new Object[]{conn, in}, CrearComprobante_Salida.class); };

	public static String generaLlave(List<Field> fields,List<String> campos,Object obj) { throw new UnsupportedOperationException("oracle double CrearComprobante.generaLlave: not part of the double"); }
	
	public void anulaMovimientosComprobantesCrobrosGFOD(List<MovimientoComprobanteCobro> listIdMovimientoComprobanteCobroGFOD,Connection conn) throws Exception { throw new UnsupportedOperationException("oracle double CrearComprobante.anulaMovimientosComprobantesCrobrosGFOD: not part of the double"); }
	
	public String calculaLLaveComprobante(PreferenciaFacturacion preferencia, MovimientoComprobanteDetalle_Proceso movdet) { throw new UnsupportedOperationException("oracle double CrearComprobante.calculaLLaveComprobante: not part of the double"); }
	
	public String calculaLLaveComprobanteDetalle(PreferenciaFacturacion preferencia,MovimientoComprobanteDetalle_Proceso movdet) { throw new UnsupportedOperationException("oracle double CrearComprobante.calculaLLaveComprobanteDetalle: not part of the double"); }
	
	public static java.sql.Date comparaValor(java.sql.Date origen,java.sql.Date destino) { throw new UnsupportedOperationException("oracle double CrearComprobante.comparaValor: not part of the double"); }
	
	public static java.sql.Timestamp comparaValor(java.sql.Timestamp origen,java.sql.Timestamp destino) { throw new UnsupportedOperationException("oracle double CrearComprobante.comparaValor: not part of the double"); }
	
	public static String comparaValor(String origen,String destino) { throw new UnsupportedOperationException("oracle double CrearComprobante.comparaValor: not part of the double"); }
	
	public static BigDecimal comparaValor(BigDecimal origen,BigDecimal destino) { throw new UnsupportedOperationException("oracle double CrearComprobante.comparaValor: not part of the double"); }
	
	public static Long comparaValor(Long origen,Long destino) { throw new UnsupportedOperationException("oracle double CrearComprobante.comparaValor: not part of the double"); }
	
	public void limpiaMovimientos(List<MovimientoComprobante_Proceso> movs,List<MovimientoComprobante_Proceso> movsError,List<ResultadoProceso_Salida> ress) { throw new UnsupportedOperationException("oracle double CrearComprobante.limpiaMovimientos: not part of the double"); }
	
	public Boolean esFormaPagoSpotCorredora(String IdConcepto ,String IdTipoMovimientoSpot) { throw new UnsupportedOperationException("oracle double CrearComprobante.esFormaPagoSpotCorredora: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CrearComprobante.main: not part of the double"); };

};