package cl.bcs.tesoreria.javacall.liq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;

/**
 * Request, response and row types of the clearing-financiero query (legacy {@code ConsultarClearingFinanciero} nested classes). The field names are the
 * JavaCall contract (and the column labels the SQL resources return), so they stay as in the legacy; defaults are the legacy constructors' defaults
 * (Gson and the row mapper instantiate through the no-argument constructor).
 */
public final class ClearingFinanciero {

    private ClearingFinanciero() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaLiquidacion;
        public List<Camara> p_Camaras = new ArrayList<>();
    }

    public static class Salida {
        public List<Resumen> P_OUT_CLEARINGFINANCIERORESUMEN = new ArrayList<>();
        public List<Detalle> P_OUT_CLEARINGFINANCIERODETALLE = new ArrayList<>();
        public List<TransaccionDividendo> P_OUT_CLEARINGFINANCIERODIVIDENDOS = new ArrayList<>();
        public List<Liquidacion> P_OUT_CLEARINGFINANCIEROLIQUIDACIONES = new ArrayList<>();
        public List<SimultaneasDetalle> P_OUT_CLEARINGFINANCIEROSIMULTANEAS = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    /** Row of the simultaneous-operations advances queries. */
    public static class AnticiposSimultaneas {
        public String TipoOperacion;
        public String AccionOperacion;
        public Date FechaLiquidacion;
        public String FolioTransaccion;
        public String Nemotecnico;
        public BigDecimal Cantidad;
        public BigDecimal Monto;
        public Long CodigoGrupoCamara;
        public String NombreGrupoCamara;
        public String RutContraparte;
        public String RazonSocialContraparte;
        public Long IdCorredorContraparte;
        public String IdBolsa;
        public String IdClasificacionTipoInstrumento;
        public Long IdComprobanteTesoreria;
        public String IdMoneda;
    }

    public static class SimultaneasDetalle {
        public String TipoOperacion;
        public Date FechaLiquidacion;
        public String Nemotecnico;
        public BigDecimal Cantidad;
        public BigDecimal MontoCompra;
        public BigDecimal MontoVenta;
        public String NombreGrupoCamara;
        public String RutContraparte;
        public String IdClasificacionTipoInstrumento;
        public Long IdComprobanteTesoreria;
        public String IdBolsa;
    }

    /** Market transaction or dividend row (the same type serves both queries, the details and the summary source). */
    public static class TransaccionDividendo {
        public Long IdTransaccion;
        public String IdClasificacionTipoInstrumento;
        public Date FechaSistemaCreacion;
        public String IdTipoInstrumento;
        public String FolioTransaccion;
        public String IndicadorOrdenDirecta;
        public String IdBolsa;
        public BigDecimal MontoTransaccion;
        public String IdTipoOperacion;
        public String AccionTipoOperacion;
        public String IdCondicionDeLiquidacion;
        public String CodigoCondicionDeLiquidacion;
        public String Instrumento;
        public Long IdCorredorContraparte;
        public String CodigoOperadorContraparte;
        public String RutContraparte;
        public String RazonSocialContraparte;
        public String CodigoCorredorContraparte;
        public String CodigoOperadorCorredora;
        public Date FechaLiquidacion;
        public Date FechaVencimiento;
        public String EsAPlazo;
        public String EsBilateral;
        public String IdCamara;
        public Long IdComprobanteTesoreria;
        public BigDecimal CantidadTransaccion;
        public BigDecimal PrecioTransaccion;
        public BigDecimal Tasa;
        public Long IdEventoCapitalCliente;
        public Long IdEventoCapital;
        public String IdMonedaTransaccion;
        public BigDecimal MontoRetencion = BigDecimal.ZERO;
        public Long CodigoGrupoCamara;
        public String NombreGrupoCamara;
        public BigDecimal Total;
        public BigDecimal TotalVenta = BigDecimal.ZERO;
        public BigDecimal TotalCompra = BigDecimal.ZERO;
        public BigDecimal SaldoAcreedor = BigDecimal.ZERO;
        public BigDecimal SaldoDeudor = BigDecimal.ZERO;
        public String IdMoneda;
    }

    public static class ConfiguracionTesoreria {
        public Long IdEmpresa;
        public String CodigoParametro;
        public String Descripcion;
        public String Valor;
    }

    public static class Detalle {
        public Long IdTransaccion;
        public String IdBolsa;
        public Long CodigoGrupoCamara;
        public String NombreGrupoCamara;
        public String RutContraparte;
        public String AccionTipoOperacion;
        public BigDecimal TotalVenta = BigDecimal.ZERO;
        public BigDecimal TotalCompra = BigDecimal.ZERO;
        public String RazonSocialContraparte;
        public String IdClasificacionTipoInstrumento;
        public Date FechaSistemaCreacion;
        public String FolioTransaccion;
        public String IdCondicionDeLiquidacion;
        public String CodigoCondicionDeLiquidacion;
        public String IdMonedaTransaccion;
        public Long IdComprobanteTesoreria;
        public String Instrumento;
        public BigDecimal CantidadTransaccion;
        public BigDecimal PrecioTransaccion;
        public BigDecimal Tasa;
        public BigDecimal MontoRetencion = BigDecimal.ZERO;
        public Long IdEventoCapital;
        public Long IdEventoCapitalCliente;
        public String CompraVenta = "";
    }

    public static class Liquidacion {
        public Long IdMovimiento;
        public String Cliente;
        public Long IdPortafolio;
        public Date FechaMovimiento;
        public BigDecimal Abono;
        public BigDecimal Cargo;
        public String TipoMovimiento;
        public String DescripcionMovimiento;
        public String AccionTipoOperacion;
        public BigDecimal Monto;
        // summary data
        public String IdBolsa;
        public Long CodigoGrupoCamara;
        public String NombreGrupoCamara;
        public String IdClasificacionTipoInstrumento;
        public Long IdCorredorContraparte;
        public String RutContraparte;
        public String RazonSocialContraparte;
        public String IdMonedaTransaccion;
        public Long IdComprobanteTesoreria;
        public BigDecimal TotalVenta;
        public BigDecimal TotalCompra;
        public BigDecimal SaldoAcreedor;
        public BigDecimal SaldoDeudor;
        public BigDecimal MontoRetencion = BigDecimal.ZERO;
    }

    public static class Resumen {
        public String IdBolsa;
        public Long CodigoGrupoCamara;
        public String NombreGrupoCamara;
        public String IdClasificacionTipoInstrumento;
        public Long IdCorredorContraparte;
        public String RutContraparte;
        public String RazonSocialContraparte;
        public String IdMonedaTransaccion;
        public Long IdComprobanteTesoreria;
        public BigDecimal TotalVenta;
        public BigDecimal TotalCompra;
        public BigDecimal SaldoAcreedor;
        public BigDecimal SaldoDeudor;
        public BigDecimal MontoRetencion;
    }

    /** Row of the broker-data query (only its emptiness is used). */
    public static class DatosCorredor {
        public Long IdEmpresa;
        public String Codigo;
        public String Nombre;
        public String IdEstado;
        public Long IdPais;
        public Long IdSocioDeNegocio;
        public String NombreCorto;
        public Long IdContraparte;
        public Long IdPersona;
        public Date FechaIngreso;
        public Date FechaCreacion;
        public Date FechaModificacion;
        public Long IdDocumentoIdentidad;
        public String DocumentoIdentidad;
        public String TipoDocumentoIdentidad;
        public String DocumentoIdentidadAdicional;
        public String TipoDocumentoIdentidadAdicional;
        public String RazonSocial;
        public String CodigoContraparte;
        public String EsCorredora;
        public String DocumentoIdentidadRepresentante;
        public String DireccionElectronicaPrincipal;
        public String DireccionFisicaPrincipalCalle;
        public String DireccionFisicaPrincipalNumero;
        public String DireccionFisicaPrincipalDepartamento;
        public String DireccionFisicaPrincipalSector;
        public String DireccionFisicaPrincipalNombreCiudad;
        public String DireccionFisicaPrincipalNombreDivisionPais;
        public String DireccionFisicaPrincipalNombrePais;
        public String TelefonoPrincipal;
    }
}
