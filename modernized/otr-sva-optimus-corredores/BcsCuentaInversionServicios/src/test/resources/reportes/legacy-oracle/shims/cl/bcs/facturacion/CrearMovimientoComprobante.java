package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.oracle.Colaboradores;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.facturacion.CrearMovimientoComprobante (BcsFacturacionServicios/.../CrearMovimientoComprobante.java,
 * 2032 lines: creates movements, vouchers, charges and cash movements). Belongs to the billing module, outside this suite.
 * The request/response data classes are copied VERBATIM from the legacy file (CrearMovimientoComprobante.java lines: 1495-1520 CrearMovimientoComprobante_Entrada; 1752-1882 MovimientoComprobante_Entrada; 1884-1910 MovimientoComprobanteFormaDePago_Entrada; 1912-1930 MovimientoComprobanteCobro_Entrada; 1932-1952 CrearMovimientoComprobante_Salida; 1954-1957 OUT_InformacionDetalle; 1959-1967 MovimientoComprobante_Salida; 1969-1976 Comprobante_Salida; 1978-1987 ResultadoProceso_Salida).
 * logica() records the whole request under llamadasColaboradores and answers with case.json colaboradores.crearMovimientoComprobante
 * (JSON of CrearMovimientoComprobante_Salida) or, by default, with one MovimientoComprobante_Salida per requested movement
 * (IdMovimientoComprobante = 900000 + position, NumeroMvtoCmbte = the requested NumeroMvtoCmbte) and no messages.
 */
public class CrearMovimientoComprobante implements JavaCall {

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        throw new UnsupportedOperationException("not part of the reportes oracle");
    }

    public CrearMovimientoComprobante_Salida logica(Connection conn, CrearMovimientoComprobante_Entrada in) throws Exception {
        Colaboradores.registrar("CrearMovimientoComprobante", in);
        JsonElement canned = Colaboradores.siguiente("crearMovimientoComprobante");
        if (canned != null) {
            CrearMovimientoComprobante_Salida s = Utils.getGsonBuilder().fromJson(canned, CrearMovimientoComprobante_Salida.class);
            if (s.P_OUT_ERRORESCONTADOR == null) s.P_OUT_ERRORESCONTADOR = 0L;
            // the real service builds tipo from the MENSAJE_* constants; getNumeroErrores compares them by reference
            for (PlataformaUtils.MensajeSalida m : s.P_OUT_MENSAJES) if (m.tipo != null) m.tipo = m.tipo.intern();
            return s;
        }
        CrearMovimientoComprobante_Salida s = new CrearMovimientoComprobante_Salida();
        s.P_OUT_ERRORESCONTADOR = 0L;
        long i = 0;
        for (MovimientoComprobante_Entrada m : in.p_Movimientos) {
            MovimientoComprobante_Salida o = new MovimientoComprobante_Salida();
            o.IdMovimientoComprobante = 900000L + i++;
            o.NumeroMvtoCmbte = m.NumeroMvtoCmbte;
            s.p_out_Movimientos.add(o);
        }
        return s;
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
}
