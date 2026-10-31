package cl.bcs.fondomutuo.certificacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;

/**
 * JSON contracts (legacy class and field names, null keys written as the legacy did: the oracle's doubles record exactly this) of the collaborators that fronts F-C and F-D call:
 * {@code SaldoFlujoCaja}, {@code CalculaCustodiaRescate}, {@code ProcesarCustodiaFondoMutuo} and the part of {@code CalcularParidad} they read ({@code MontoLocal},
 * {@code MontoDestino}, {@code Resultado}). The arguments are serialised with {@link JsonLegacy#conNulos()} AT THE MOMENT OF THE CALL (an entry object that the legacy reuses
 * is written as many times as it was added, with its last values: D-F3B-10).
 */
public final class CertificarAporteRescateColaboradores {

    private CertificarAporteRescateColaboradores() {
    }

    // ---- SaldoFlujoCaja ------------------------------------------------------------------------------------------------------------------------------------------
    public static class FlujoCajaEntrada {
        public Long P_IDEMPRESA;
        public String P_FECHADESDE;
        public String P_FECHAHASTA;
        public String P_USO;
        public String P_DESPLEGARENUI = "N";
        public String P_AGRUPARPORORIGEN = "N";
        public Object p_BarraProgreso;
        public List<PortafolioAConsultar> P_PORTAFOLIOACONSULTAR = new ArrayList<>();
    }

    public static class PortafolioAConsultar {
        public Long IdPortafolio;
        public String IdMoneda;
    }

    public static class FlujoCajaSalida {
        public List<FlujoCaja> P_OUT_FLUJOCAJA = new ArrayList<>();
    }

    public static class FlujoCaja {
        public String FechaSaldo;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo = BigDecimal.ZERO;
    }

    static FlujoCajaSalida saldo(FlujoCajaPort puerto, FlujoCajaEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.saldo(json), FlujoCajaSalida.class);
    }

    // ---- CalculaCustodiaRescate ----------------------------------------------------------------------------------------------------------------------------------
    public static class CalculoCustodiaEntrada {
        public Long p_IdEmpresa;
        public List<CalculoCustodiaSolicitud> p_Solicitudes = new ArrayList<>();
    }

    /** Legacy {@code CalculaCustodiaRescate.InSolicitud}: {@code MontoDestino}, {@code ValorParidad} and {@code MontoMonedaSistema} are never set by the caller (null keys). */
    public static class CalculoCustodiaSolicitud {
        public Long IdSolicitudAporteRescate;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public String IdMoneda;
        public String IdMonedaFondo;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal MontoDestino;
        public BigDecimal ValorParidad;
        public String TipoSolicitud;
        public Long IdInstrumento;
        public Long IdCliente;
        public Long IdPortafolio;
        public String MontoMonedaSistema;
        public java.sql.Date FechaValorCuota;
        public String IdRegimenTributario;
        public Long IdOmnibus;
    }

    public static class CalculoCustodiaSalida {
        public List<ResultadoCalculoCustodia> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class ResultadoCalculoCustodia {
        public Long IdSolicitudAporteRescate;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal MontoDestino;
        public BigDecimal ValorParidad;
        public BigDecimal CantidadDisponible;
        public BigDecimal MontoMonedaSistema;
        public BigDecimal CostoEnFactorAjuste;
        public BigDecimal ValorFactorAjuste;
        public String Estado;
    }

    static CalculoCustodiaSalida calcular(CalculaCustodiaRescatePort puerto, CalculoCustodiaEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.calcular(json), CalculoCustodiaSalida.class);
    }

    // ---- ProcesarCustodiaFondoMutuo ------------------------------------------------------------------------------------------------------------------------------
    public static class CustodiaEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public List<MovimientoCustodia> p_Movimientos = new ArrayList<>();
    }

    /** Legacy {@code pojo.MovimientoCustodiaFondoMutuo} (field order = legacy; {@code IdCustodia} is never set by the caller). */
    public static class MovimientoCustodia {
        public Long IdGenerarMovimientoCustodia;
        public java.sql.Date FechaOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdTipoProducto;
        public String IdConcepto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public String IdTipoComprobante;
        public Long FolioComprobante;
        public java.sql.Date FechaComprobante;
        public String IdBolsa;
        public Long IdAgente;
        public java.sql.Date FechaLiquidacion;
        public Long IdUsuario;
        public String TipoCarteraNormativa;
        public java.sql.Date FechaCurse;
        public String TipoFactorAjuste;
        public BigDecimal ValorFactorAjusteaFechaCurse;
        public BigDecimal CostoEnFactorAjuste;
        public Long IdInstrumento;
        public String IdEmisor;
        public String IdMonedaInstrumento;
        public String IdBoveda;
        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public BigDecimal PrecioCompra;
        public BigDecimal MontoCompra;
        public BigDecimal MontoCompraMonedaInstrumento;
        public String IdTipoCanalIngreso;
        public String IdRegimenTributario;
        public Long IdCustodia;
        public java.sql.Date FechaOrigenCompra;
    }

    /** The part of {@code SalidaProcesarCustodiaFondoMutuo} that F-C and F-D read, with the defaults of the legacy constructor. */
    public static class CustodiaSalida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MovimientoSalida> P_OUT_MOVIMIENTOS = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class MovimientoSalida {
        public Long IdGenerarMovimientoCustodia;
        public Long IdCustodia;
        public BigDecimal CostoInversionEnFactorAjusteARetirar;
        public BigDecimal MontoCompraFactorARetirar;
        public BigDecimal MontoCompraFactorOriginal;
        /** What the reinversion reads (FM-CER-53): the quantity retired and the origin date of the purchase (text {@code yyyy-MM-dd}). */
        public BigDecimal Cantidad;
        public String FechaOrigenCompra;
    }

    static CustodiaSalida procesar(ProcesarCustodiaFondoMutuoPort puerto, CustodiaEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.procesar(json), CustodiaSalida.class);
    }

    // ---- CrearCargoAbono (cuenta de inversion; F-E) --------------------------------------------------------------------------------------------------------------
    /** Legacy {@code CrearCargoAbono.CrearCargoAbonoEntrada}. */
    public static class CargoAbonoEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaSistema;
        public String p_EsIngresoManual = "N";
        public List<ObjCargoAbono> p_CargoAbonoAGenerar = new ArrayList<>();
    }

    /** Legacy {@code CrearCargoAbono.ObjCargoAbonoAGenerar} (the dates are text, as in the legacy pojo). */
    public static class ObjCargoAbono {
        public Long NumeroCargoAbono;
        public Long IdPortafolio;
        public String IdMoneda;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public String FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoProducto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public String FechaOperacion;
        public String FechaLiquidacionOperacion;
        public String FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdContraparte;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public String FechaComprobante;
        public Long IdPersonaPagoCobro;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public BigDecimal Cantidad;
        public String Nemo;
        public String EsIndicadorBilateral;
        public String EsCargoAbonoPorTraspaso;
        public Long IdPortafolioTraspaso;
        public String GeneroContraparteEnTesoreria;
        public String CertificadoAutomatico;
    }

    /** The part of {@code CrearCargoAbonoSalida} that F-E reads, with the defaults of the legacy {@code mueveCuentaInversion}. */
    public static class CargoAbonoSalida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    static CargoAbonoSalida crearCargoAbono(CrearCargoAbonoPort puerto, CargoAbonoEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.crear(json), CargoAbonoSalida.class);
    }

    // ---- CalcularParidad (the part F-C and F-D read) -------------------------------------------------------------------------------------------------------------
    public static class ParidadSalida {
        public List<CalculoParidad> p_out_CalculosSalida = new ArrayList<>();
    }

    public static class CalculoParidad {
        public Long IdInterno;
        public BigDecimal MontoDestino;
        public BigDecimal MontoLocal;
        public String Resultado;
    }

    static ParidadSalida calcularParidad(ParidadPort puerto, CalcularParidadEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.calcular(json), ParidadSalida.class);
    }

    // ---- Facturacion: CrearMovimientoComprobante / CrearComprobante (F-G) ----------------------------------------------------------------------------------------------
    /** Legacy {@code CrearMovimientoComprobante_Entrada}: the two last flags are defaults of the legacy pojo and travel in the JSON. */
    public static class MovimientoComprobanteEntrada {
        public List<MovimientoComprobante> p_Movimientos = new ArrayList<>();
        public List<MovimientoFormaDePago> p_MovimientosFormaDePago = new ArrayList<>();
        public List<MovimientoCobro> p_MovimientosCobros = new ArrayList<>();
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion = "N";
        public String p_UsaReglasNegocio = "S";
        public String p_EsIngresoAutomatico = "N";
        public String p_EsCargaMasiva = "N";
        public boolean p_EsPauta = false;
    }

    /** Legacy {@code MovimientoComprobante_Entrada}: 107 keys, nulls included; the dates are text. */
    public static class MovimientoComprobante {
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
        public String IdMoneda;
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
        public String IdTipoLiquidacionComisionFactura = "";
        public Long IdTransaccion;
        public Long IdUsuario;
        public Long IdPersonaBroker;
        public Long IdPersonaBancoCustodio;
        public Long IdPersonaBancoLiquidador;
        public BigDecimal Intereses = BigDecimal.ZERO;
        public String LiquidaBancoCustodio = "N";
        public String MercadoSMV;
        public String ModalidadSMV;
        public String ModPrefFact = "";
        public BigDecimal Monto;
        public BigDecimal MontoRaiz;
        public BigDecimal MontoMovimientoComprobante;
        public BigDecimal MontoLiquidacion;
        public BigDecimal MontoDescuento;
        public BigDecimal MontoInteres;
        public BigDecimal MontoAPlazo;
        public Long NumeroMvtoCmbte;
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
        public String Contrapartida = "N";
    }

    /** Legacy {@code MovimientoComprobanteFormaDePago_Entrada}. */
    public static class MovimientoFormaDePago {
        public Long NumeroMvtoCmbte;
        public Long NumeroMvtoCmbteFormaDePago;
        public String IdMedioDePago;
        public String NumeroSerie;
        public Long NumeroDocumento;
        public Long IdCuentaBancaria;
        public String IdTipoCuentaBancaria;
        public String NumeroCuentaBancaria;
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
    }

    /** Legacy {@code MovimientoComprobanteCobro_Entrada}. */
    public static class MovimientoCobro {
        public Long NumeroMvtoCmbte;
        public Long NumeroMvtoCmbteCobro;
        public String IdCobro;
        public String IdMonedaCobro;
        public String IdMonedaLiquidacionCobro;
        public BigDecimal MontoCobro = BigDecimal.ZERO;
        public BigDecimal MontoMovimientoComprobanteCobro;
        public BigDecimal MontoLiquidacionCobro;
        public BigDecimal Porcentaje;
    }

    /** Legacy {@code CrearMovimientoComprobante_Salida} (the part F-G reads). The counter has NO default (the constructor of the legacy does not give one: a missing key is a NullPointerException). */
    public static class MovimientoComprobanteSalida {
        public List<MovimientoGenerado> p_out_Movimientos = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    public static class MovimientoGenerado {
        public Long IdMovimientoComprobante;
        public Long NumeroMvtoCmbte;
        public Long IdOperacion = 0L;
    }

    /** Legacy {@code CrearComprobante_Entrada}. */
    public static class ComprobanteEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_EsIngresoAutomatico = "N";
        public String p_GeneraComprobanteTesoreria = null;
        public String p_Glosa = "";
        public List<MovimientoPorFacturar> P_MOVIMIENTOFACTURAR = new ArrayList<>();
        public String p_ServicioAgrupador = "";
    }

    public static class MovimientoPorFacturar {
        public Long IdMovimientoComprobante;
        public String FechaComprobante;
        public String IdConcepto;
        public Long IdCliente;
        public Long IdPortafolio;
    }

    /** Legacy {@code CrearComprobante_Salida} (the part F-G reads; the counter defaults to 0 as in the legacy constructor). */
    public static class ComprobanteSalida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    static MovimientoComprobanteSalida crearMovimientoComprobante(FacturacionPort puerto, MovimientoComprobanteEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.crearMovimientoComprobante(json), MovimientoComprobanteSalida.class);
    }

    static ComprobanteSalida crearComprobante(FacturacionPort puerto, ComprobanteEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.crearComprobante(json), ComprobanteSalida.class);
    }

    // ---- CrearSolicitudAporteReinversion (AR, F-I) ---------------------------------------------------------------------------------------------------------------------
    /** Legacy {@code EntradaCrearSolicitudAporteReinversion}. */
    public static class AporteReinversionEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<AporteReinversion> p_SolicitudesAporte = new ArrayList<>();
    }

    /** Legacy {@code InSolicitudesAporte} with the defaults of its constructor; the dates are text. */
    public static class AporteReinversion {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondoOrigen;
        public String IdEmisorOrigen;
        public BigDecimal CostoUF = BigDecimal.ZERO;
        public String FechaOrigenInversion;
        public BigDecimal MontoOrigen;
        public String IndicadorMLT;
        public String IdSerieFondo;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMoneda;
        public String IdMedioDePago;
        public String Visado;
        public String SolicitudBatch;
        public String OrigenOperacion;
        public String IdRegimenTributario;
        public String Glosa;
        public Long IdAutorizadoOrigen;
        public Long FolioContrato;
        public Long IdSolicitudAporteRescateRelacionado;
        public String FechaEmision;
        public BigDecimal Porcentaje = new BigDecimal("100");
        public Long IdAgrupador = 0L;
        public String FechaCertificacionRescateOrigen = "0001-01-01";
        public String FechaLiquidacionRescateOrigen = "0001-01-01";
        public String FechaValorCuotaRescateOrigen = "0001-01-01";
        public String FechaCertificaSpotCompra;
        public String FechaLiquidaSpotCompra;
        public String Switch = "N";
        public String GeneraSpot = "N";
        public BigDecimal MontoPatrimonio = BigDecimal.ZERO;
        public String Canal;
        public String IngresoAutomatico = "N";
        public BigDecimal MontoTotalInversion = BigDecimal.ZERO;
        public java.util.Date FechaValorCuota;
        public String TipoReinversion = "";
    }

    /** Legacy {@code SalidaCrearSolicitudAporteReinversion} (the part F-I reads). */
    public static class AporteReinversionSalida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoAporte> P_OUT_RESULTADO = new ArrayList<>();
    }

    /** The part of the legacy {@code ResultadoSolicitud} that F-I reads; a missing {@code Switch} is a NullPointerException (D-F3B-30). */
    public static class ResultadoAporte {
        public Long IdInterno;
        public Long IdSolicitudAporteRescate;
        public String Switch;
        public String IdTipoCanalIngreso;
    }

    static AporteReinversionSalida crearAporteReinversion(CrearAporteReinversionPort puerto, AporteReinversionEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.crear(json), AporteReinversionSalida.class);
    }
}
