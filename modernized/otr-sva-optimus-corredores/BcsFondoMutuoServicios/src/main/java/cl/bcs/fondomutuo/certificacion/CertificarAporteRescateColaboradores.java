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
    }

    static CustodiaSalida procesar(ProcesarCustodiaFondoMutuoPort puerto, CustodiaEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.procesar(json), CustodiaSalida.class);
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
}
