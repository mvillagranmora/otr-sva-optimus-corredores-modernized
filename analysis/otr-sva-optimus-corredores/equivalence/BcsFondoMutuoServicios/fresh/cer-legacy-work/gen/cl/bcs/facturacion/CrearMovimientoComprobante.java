// GENERATED DOUBLE of the legacy cl.bcs.facturacion.CrearMovimientoComprobante by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobanteCobro;
import cl.bcs.facturacion.pojo.MovimientoComprobanteFormaDePago;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearMovimientoComprobante implements JavaCall {
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearMovimientoComprobante", "call", new Object[]{con, jin}, JsonObject.class); }
	
	@Trace
	public CrearMovimientoComprobante_Salida logica(Connection conn, CrearMovimientoComprobante_Entrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearMovimientoComprobante", "logica", new Object[]{conn, in}, CrearMovimientoComprobante_Salida.class); }

	public static class SecuenciaAgrupacion_Proceso {
		public Long IdCliente;
		public Long IdPortafolio;
		public String SecuenciaAgrupacion;
		public Integer Cantidad;
	}
	
	public static class MovimientoComprobante_Proceso extends MovimientoComprobante {

		public String CrearComprobanteAutomatico;
		public int ok;
		public int advertencias;
		public int autorizaciones;
		public int errores;
		public Long NumeroMvtoCmbte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class MovimientoComprobanteCobro_Proceso extends MovimientoComprobanteCobro {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
	}
	
	public static class MovimientoComprobanteFormaDePago_Proceso extends MovimientoComprobanteFormaDePago {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
	}
	
	public static class Mesa_Proceso {
		public Long Prioridad;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdMesa;
		public String PorDefecto;
	}
	
	public static class MesaDefecto_Proceso {
		public Long IdMesa;
		public String PorDefecto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearMovimientoComprobante_Entrada {
	
		public List<MovimientoComprobante_Entrada> p_Movimientos;
		public List<MovimientoComprobanteFormaDePago_Entrada> p_MovimientosFormaDePago;
		public List<MovimientoComprobanteCobro_Entrada> p_MovimientosCobros;
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion = "N";
		public String p_UsaReglasNegocio = "S";
		public String p_EsIngresoAutomatico = "N";
		public String p_EsCargaMasiva = "N";
		public boolean p_EsPauta =false;

		public String toString() {
			return Utils.printJson(this, false);
		}
		public CrearMovimientoComprobante_Entrada() {
			
			p_EsIngresoAutomatico = "N";
			p_Movimientos = new ArrayList<MovimientoComprobante_Entrada>();
			p_MovimientosFormaDePago = new ArrayList<MovimientoComprobanteFormaDePago_Entrada>();
			p_MovimientosCobros = new ArrayList<MovimientoComprobanteCobro_Entrada>();
		}
	}
	
	public static class ClientePersona{
		public Long IdCliente;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String Estado;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		
		public ClientePersona() {
			this.IdCliente = 0L;
			this.Nombres = "";
			this.ApellidoPaterno = "";
			this.ApellidoMaterno = "";
			this.Estado = "";
			this.TipoDocumentoIdentidad = "";
			this.DocumentoIdentidad = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdCliente;
		public String EstadoPortafolio;
	
		public Portafolio() {
			this.IdPortafolio = 0L;
			this.IdCliente = 0L;
			this.EstadoPortafolio = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MedioDePago {
		public String IdMedioDePago;
		public String TipoMedioDePago;
		public String Nombre;
		
		public MedioDePago() {
			this.IdMedioDePago = "";
			this.TipoMedioDePago = "";
			this.Nombre = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro{
		public String IdCobro;
		public String NombreCobro;
		
		public Cobro(){
			this.IdCobro = "";
			this.NombreCobro = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InformacionMovimientoComprobante {
		
		public Long NumeroMvtoCmbte;
		/*public String IdConcepto;
		public String IdTipoComprobante;
		public String IdCondicionDeLiquidacion;
		public String IdTipoReajuste;
		public String IdTipoConvenio;
		public String IdEmisor;
		public String IdMonedaLiquidacion;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public Long IdEmpleadoDigitador;
		public Long IdInstrumentoSubyacente;
		public String IdTipoProducto;
		public String IdMoneda;
		public String IdMonedaSubyacente;
		public String IdTipoCanalIngreso;
		public String IdTipoOperacion;
		public String IdBoveda;
		public Long IdCorredoraContraparte;
		public String IdBolsa;
		public Long IdOrden;*/
		
     	public String IdTipoCanalIngreso;
		public String IdConceptoReal;
		public String IdTipoComprobanteReal;
		public String IdCondicionDeLiquidacionReal;
		public String IdTipoReajusteReal;
		public String IdTipoConvenioReal;
		public String IdEmisorReal;
		public String IdMonedaLiquidacionReal;
		public String IdEstadoMonedaLiquidacion;
		public Long IdInstrumentoReal;
		public Long IdInstrumentoRelacionadoReal;
		public Long IdInstrumentoSubyacenteReal;
		public Long IdEmpleadoDigitadorReal;
		public String IdTipoProductoReal;
		public String IdMonedaMovimientoComprobanteReal;
		public String IdEstadoMonedaMovimientoComprobante;
		public String IdMonedaSubyacenteReal;
		public String IdEstadoMonedaSubyacente;
		public String IdTipoCanalIngresoReal;
		public String IdTipoOperacionReal;
		public String IdBovedaReal;
		public Long IdCorredoraContraparteReal;
		public String IdBolsaReal;
		public Long IdOrdenReal;
		public String ISIN;
		public String ISINInstrumentoSubyacente;
		
		public BigDecimal CorteMinimo;
		
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		
		public String IdTipoInstrumentoInstrumentoRelacionado;
		public String NombreTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumentoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		
		public String IdTipoInstrumentoInstrumentoSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoInstrumentoSubyacente;
		
		public String ValorModoFacturacion;

		public Long IdEmpleadoPortafolio;
		public Long IdSucursalEmpleadoPortafolio;
		public String NombreSucursalEmpleadoPortafolio;
		public String NombreCompletoEmpleadoPortafolio;
		
		public Long IdEmpleadoDigitador;
		public Long IdSucursalEmpleadoDigitador;
		public String NombreSucursalEmpleadoDigitador;
		public String NombreCompletoEmpleadoDigitador;
		
		public String CodigoTipoCanalIngreso;
		
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String NombreCompleto;
		public String NombreTipoCliente;
		
		public String CodigoTipoProducto;
		public String CodigoTipoOperacion;
		public String CodigoBolsa;
		public String NombreCompletoEmisor;
		
		public Long IdMovimientoComprobante;
		public String NemotecnicoInstrumento;
		public String NemotecnicoInstrumentoRelacionado;
		public String NemotecnicoInstrumentoSubyacente;
		
		public Long IdComprobanteReferencia;
		public Long FolioComprobanteReferencia;
		public Timestamp FechaComprobanteReferencia; ///aqui
		public String IdTipoComprobanteReferencia;
		public String Contrapartida;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionMovimientoComprobanteFormaDePago {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
		
		/*public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdMoneda;
		public String IdTipoCuentaBancaria;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;*/
		
		public String IdMedioDePagoReal;
		public String IdModalidadDePagoReal;
		public String IdMonedaFormaDePagoReal;
		public String IdTipoCuentaBancariaReal;
		public Long IdTarjetaBancariaReal;
		public String IdTipoTarjetaBancariaReal;
		
		public Long IdMovimientoComprobanteFormaDePago;
		public String NombreMedioDePago;
		public String NombreTipoCuentaBancaria;
		public String NombreTipoTarjetaBancaria;
		public String NombreModalidadDePago;
		public String CodigoBanco;
		public String NombreBanco;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class InformacionMovimientoComprobanteCobro {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
		
		/*public String IdCobro;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;*/
		
		public String IdCobroReal;
		public String CodigoDefinicionCobroReal;
		public String IdMonedaReal;
		public String IdMonedaLiquidacionReal;
		
		public Long IdMovimientoComprobanteCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientoComprobante_Entrada extends BcsPojo{

		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String ActualizarTesoreria;
		public String ActualizarCustodiaRelacionado;
		public BigDecimal Cantidad;
		public String CrearComprobanteAutomatico;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		
		public BigDecimal DiasPlazo;
		
		public String EsAPlazo;
		public String EsComprobIndividual;
		public String EsCuentaPropia;
		public String EsEnRueda;
		public String EsFungible;
		public String EsPrecioPrima;
		public String EsRuteo;
		public String EsVentaCorta = "N";
		public String EsClienteFinal = "S";
		
		public String FechaEmision = "0001-01-01";
		public String FechaLiquidacion;
		public String FechaMovimientoComprobante;
		public String FechaOrden = "0001-01-01";
		public String FechaParidad = "0001-01-01";
		public String FechaTipoCambio = "0001-01-01";
		public String FechaTransaccion = "0001-01-01";
		public String FechaVencimiento = "0001-01-01";
		
		public String FechaHoraMovimientoComprobante = "0001-01-01 00:00:00";
		public Long FolioCartera;
		public String FolioTransaccion;
		
		public String Glosa;
		public String Referencia;
		public String TipoReferencia;
		
		public Long IdAsignacion;
		public Long IdAsignacionRef;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCliente;
		public String IdConcepto;
		public Long IdComprobanteReferencia;
		public String IdTipoComprobanteReferencia;
		public String IdCondicionLiquidacion;
		public Long IdCorredoraContraparte;
		public Long IdCuentaCustodioPortafolio;
		public String IdEmisor;
		public Long IdEmpleadoDigitador;                          
		public Long IdEmpresa;
		public String IdFormaCustodia;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public Long IdInstrumentoSubyacente;
		public Long IdMesa = 0L;
		public String IdMoneda; // IdMonedaInstrumento
		public String IdMonedaEmision = "";
		public String IdMonedaLiquidacion;
		public String IdMonedaSubyacente;
		public String IdMonedaSpot;
		public String IdMonedaNegociacion;
		public Long IdMovimientoRelacionado;
		public String IndicadorOrdenDirecta;
		public Long IdOrden;
		public Long IdPortafolio;
		public String IdTipoComprobante;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdTipoReajuste;
		public String IdTipoConvenio;
		public String IdTipoCanalIngreso;
		public String IdTipoCliente = "";
		public String IdTipoLiquidacionComisionFactura;
		public Long IdTransaccion;
		public Long IdUsuario;
		public Long IdPersonaBroker;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public BigDecimal Intereses = BigDecimal.ZERO;
		
		public String LiquidaBancoCustodio  = "N";
		
		public String MercadoSMV;
		public String ModalidadSMV;
		public String ModPrefFact;
		public BigDecimal Monto;
		public BigDecimal MontoRaiz;
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoDescuento;
		public BigDecimal MontoInteres;
		public BigDecimal MontoAPlazo;
		
		public Long NumeroMvtoCmbte;	//CORRELATIVO
		public String NemotecnicoInstrumento;
		public String NemoSVS = "";
		
		public BigDecimal Precio;
		public BigDecimal PrecioRaiz;
		public BigDecimal PrecioAPlazo;
		
		public BigDecimal ReajustesVAC = BigDecimal.ZERO;
		public String SecuenciaAgrupacion;
		public BigDecimal Spread;
		
		public BigDecimal Tasa = BigDecimal.ZERO;
		public BigDecimal TasaAgente;
		public BigDecimal TasaEmision;
		public BigDecimal TasaOperacion;
		
		public BigDecimal ValorRescate;
		public BigDecimal ValorParidad;
		public BigDecimal ValorTipoCambio;
		public BigDecimal ValorCaptacionMonedaOrigen;
		public String CodigoIFRS;
		
		public Long IdOperacion;
		
		public String Contrapartida;

		public MovimientoComprobante_Entrada() {
			this.IdTipoLiquidacionComisionFactura = "";
			this.ModPrefFact = "";
			this.Contrapartida = "N";
		}
	}
	
	public static class MovimientoComprobanteFormaDePago_Entrada {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
		
		public String IdMedioDePago;
		public String NumeroSerie;
		public Long NumeroDocumento;
		public Long IdCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String  NumeroCuentaBancaria;
	    public Long IdTarjetaBancaria;
	    public String IdTipoTarjetaBancaria;
	    public String NumeroTarjetaBancaria;
	    public String FechaVctoTarjetaBancaria;
	    public String IdModalidadDePago;
	    public Long IdBanco;
	    public String IdMoneda;
	    public String FechaDocumentoPago;
	    public BigDecimal MontoLiquidacionFormaDePago;
	    public String IdTipoMovimientoSpot = "";
	    public String AfectarTesoreria = "S";
	    public String AfectarCuentaInversion = "N";
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobro_Entrada {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
	    public String IdCobro;
	    public String IdMonedaCobro;
	    public String IdMonedaLiquidacionCobro;
	    public BigDecimal MontoCobro;
	    public BigDecimal MontoMovimientoComprobanteCobro;
	    public BigDecimal MontoLiquidacionCobro;
	    public BigDecimal Porcentaje;
	    
	    public MovimientoComprobanteCobro_Entrada() {
	    	this.MontoCobro = BigDecimal.ZERO;
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearMovimientoComprobante_Salida {
		
		public List<MovimientoComprobante_Salida> p_out_Movimientos;
		public List<Comprobante_Salida> p_out_Comprobantes;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> p_out_ResultadoProceso;
		public List<OUT_InformacionDetalle> 	p_out_InformacionDetalle;
		
		public CrearMovimientoComprobante_Salida() {
			this.p_out_Comprobantes = new ArrayList<>();
			this.p_out_Movimientos = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.p_out_InformacionDetalle = new ArrayList<OUT_InformacionDetalle>();
			this.p_out_ResultadoProceso = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OUT_InformacionDetalle {
		public String		Codigo; 
		public String		Descripcion; 
	}
	
	public static class MovimientoComprobante_Salida {
		public Long IdMovimientoComprobante;
		public Long NumeroMvtoCmbte;
		public Long IdOperacion = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Comprobante_Salida {
		public Long IdComprobante;
		public Long IdMovimientoComprobante;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoProceso_Salida {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoContrapartida extends BcsPojo{
		public MovimientoComprobante_Entrada Cargo;
		
		MovimientoContrapartida(){
			this.Cargo = new MovimientoComprobante_Entrada();
		}
	}
	
	public static class ConceptoIndicadorAbonoClpContrapartida extends BcsPojo {
		public String IdConcepto;
		public String Nombre;
		public String Servicio;
		public String Resultado;
	}
	
	public static class MovimientoComprobanteAnterior extends BcsPojo {
		public Long IdComprobanteReferencia;
		public BigDecimal Saldo;
	}
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CrearMovimientoComprobante.main: not part of the double"); };	
}

	