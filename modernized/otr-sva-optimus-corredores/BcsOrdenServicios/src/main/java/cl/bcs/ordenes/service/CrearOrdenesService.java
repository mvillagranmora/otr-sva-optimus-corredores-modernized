package cl.bcs.ordenes.service;

import cl.bcs.ordenes.domain.ConstantesOrdenes;
import cl.bcs.ordenes.domain.OrdenProceso;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * B-ORD-CRE-*: pure-logic business rules extracted from legacy
 * BcsOrdenServicios/src/cl/bcs/ordenes/CrearOrdenes.java (5 779 LOC, CCN=813).
 *
 * Todos los métodos son public static para habilitar testing puro sin contexto Spring.
 */
@Service
public class CrearOrdenesService {

    /** B-ORD-CRE-01: CrearOrdenes.java:1273 — clave de bloqueo de orden. */
    public static String buildIdBloqueaOrden(Long idPortafolio, Long idInstrumento,
            String idTipoOperacion, BigDecimal cantidad,
            BigDecimal precioLimite, String fechaSistema) {
        BigDecimal precio = precioLimite.stripTrailingZeros();
        return idPortafolio + "_" + idInstrumento + "_"
                + idTipoOperacion + "_" + cantidad + "_"
                + precio + "_" + fechaSistema;
    }

    /** B-ORD-CRE-02: CrearOrdenes.java:1572-1586 — routing GeneraSpot. */
    public static String resolveGeneraSpot(String entradaGeneraSpot, String accionOrden,
            String idMonedaOrden, String idMonedaLiquidacion) {
        if (!"S".equalsIgnoreCase(entradaGeneraSpot)) return "N";
        if (!ConstantesOrdenes.ACCION_COMPRA.equalsIgnoreCase(accionOrden)) return "N";
        if (ConstantesOrdenes.Dolar.equalsIgnoreCase(idMonedaOrden)) return "S";
        if ("CLP".equalsIgnoreCase(idMonedaOrden)
                && ConstantesOrdenes.Dolar.equalsIgnoreCase(idMonedaLiquidacion)) return "S";
        return "N";
    }

    /** B-ORD-CRE-03: CrearOrdenes.java:1588 — TipoLiquidacionFactura sólo cuando param = "ACTIVO". */
    public static String resolveTipoLiquidacionFactura(String paramLiquidacionComisionFact,
            String entradaTipoLiquidacionFactura) {
        boolean activo = "S".equals("ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N");
        return activo ? entradaTipoLiquidacionFactura : "";
    }

    /** B-ORD-CRE-04: CrearOrdenes.java:1782-1792 — derivación TipoCambio null. */
    public static BigDecimal resolveTipoCambio(BigDecimal entradaTipoCambio,
            String idMonedaOrden, String idMonedaLiquidacion, String monedaSistema) {
        if (entradaTipoCambio != null) return entradaTipoCambio;
        if (idMonedaOrden == null || idMonedaLiquidacion == null) return BigDecimal.ONE;
        if (idMonedaOrden.equals(idMonedaLiquidacion) && monedaSistema.equals(idMonedaOrden))
            return BigDecimal.ONE;
        return BigDecimal.ZERO;
    }

    /** B-ORD-CRE-05: CrearOrdenes.java:1738-1750 — derivación FechaVigencia. */
    public static Date resolveFechaVigencia(String idTipoVigencia, Date fechaSistemaCreacion,
            String entradaFechaVigencia) {
        if (idTipoVigencia == null || idTipoVigencia.isEmpty()
                || idTipoVigencia.equalsIgnoreCase(ConstantesOrdenes.TIPO_VIGENCIA_SOLO_HOY)) {
            return fechaSistemaCreacion;
        }
        if (idTipoVigencia.equalsIgnoreCase(ConstantesOrdenes.TIPO_VIGENCIA_SEMANAL)
                || idTipoVigencia.equalsIgnoreCase(ConstantesOrdenes.TIPO_VIGENCIA_FECHA_FIJA)
                || idTipoVigencia.equalsIgnoreCase(ConstantesOrdenes.TIPO_VIGENCIA_PERMANENTE)) {
            return Date.valueOf(entradaFechaVigencia);
        }
        return null;
    }

    /** B-ORD-CRE-06: CrearOrdenes.java:1764-1768 — EsFacturaInmediata null → "N". */
    public static String resolveEsFacturaInmediata(String esFacturaInmediata) {
        if (esFacturaInmediata == null || esFacturaInmediata.isEmpty()) return "N";
        return esFacturaInmediata;
    }

    /** B-ORD-CRE-07: CrearOrdenes.java:1771-1776 — EsVentaCorta null → "N". */
    public static String resolveEsVentaCorta(String esVentaCorta) {
        return esVentaCorta == null ? ConstantesOrdenes.NO : esVentaCorta;
    }

    /** B-ORD-CRE-08: CrearOrdenes.java:1795-1800 — IdCorredorCorresponsal null → 0L. */
    public static Long resolveIdCorredorCorresponsal(Long id) {
        return id == null ? 0L : id;
    }

    /** B-ORD-CRE-09: CrearOrdenes.java:1802-1807 — IdPersonaAutorizadoDarOrdenes null → 0L. */
    public static Long resolveIdPersonaAutorizadoDarOrdenes(Long id) {
        return id == null ? 0L : id;
    }

    /** B-ORD-CRE-10: CrearOrdenes.java:1757-1761 — IdBoveda "0" o null → null. */
    public static String resolveIdBoveda(String idBoveda) {
        if (idBoveda == null || "0".equalsIgnoreCase(idBoveda)) return null;
        return idBoveda;
    }

    /** B-ORD-CRE-11: CrearOrdenes.java:1824-1832 — routing IdTipoReajuste por clasificación. */
    public static String resolveIdTipoReajuste(String localizacion, String clasificacion,
            String idTipoReajusteInstrumento, String idTipoReajusteEntrada) {
        if ("CL".equalsIgnoreCase(localizacion)
                && ConstantesOrdenes.CLASIFICACION_RF.equalsIgnoreCase(clasificacion)) {
            return idTipoReajusteInstrumento;
        }
        if (ConstantesOrdenes.CLASIFICACION_IF.equalsIgnoreCase(clasificacion)) {
            return idTipoReajusteEntrada;
        }
        return "";
    }

    /** B-ORD-CRE-12: CrearOrdenes.java:1834-1852 — routing IdEmisor por clasificación y operación. */
    public static String resolveIdEmisor(String clasificacion, String tipoInstrumento,
            String tipoOperacion, String idEmisorEntrada, String idEmisorInstrumento,
            String idEmisorSubyacente) {
        if (ConstantesOrdenes.CLASIFICACION_IF.equalsIgnoreCase(clasificacion)) return idEmisorEntrada;
        if (ConstantesOrdenes.CLASIFICACION_RE.equalsIgnoreCase(clasificacion)) return idEmisorInstrumento;
        if (ConstantesOrdenes.CLASIFICACION_RV.equalsIgnoreCase(clasificacion)
                && ConstantesOrdenes.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(tipoInstrumento)
                && ConstantesOrdenes.TIPO_OPERACION_VENSIM.equalsIgnoreCase(tipoOperacion))
            return idEmisorSubyacente;
        if (ConstantesOrdenes.CLASIFICACION_RV.equalsIgnoreCase(clasificacion)
                && ConstantesOrdenes.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(tipoInstrumento)
                && ConstantesOrdenes.TIPO_OPERACION_COMSIM.equalsIgnoreCase(tipoOperacion))
            return "";
        return idEmisorInstrumento;
    }

    /** B-ORD-CRE-13: CrearOrdenes.java:1854-1862 — routing IdInstrumentoCustodia. */
    public static Long resolveIdInstrumentoCustodia(String tipoInstrumento, String clasificacion,
            Long idInstrumento, Long idInstrumentoSubyacente) {
        if (ConstantesOrdenes.TIPO_INSTRUMENTO_SIM.equalsIgnoreCase(tipoInstrumento))
            return idInstrumentoSubyacente;
        if (ConstantesOrdenes.CLASIFICACION_RE.equalsIgnoreCase(clasificacion))
            return idInstrumentoSubyacente;
        return idInstrumento;
    }

    /** B-ORD-CRE-14: CrearOrdenes.java:1902 — BEC bolsa → fuerza canal MAN. */
    public static String resolveIdTipoCanalDistribucionReal(String idBolsaReal,
            String idTipoCanalDistribucionReal) {
        return "BEC".equalsIgnoreCase(idBolsaReal) ? "MAN" : idTipoCanalDistribucionReal;
    }

    /** B-ORD-CRE-15: CrearOrdenes.java:1965-1975 — derivación EsBroker. */
    public static String resolveEsBroker(Long idPersonaBroker) {
        return idPersonaBroker != null ? "S" : "N";
    }

    /** B-ORD-CRE-16: CrearOrdenes.java:1343-1354 — VentaCorta sólo válida con canal MAN. */
    public static boolean isVentaCortaConCanalInvalido(String esVentaCorta,
            String idTipoCanalDistribucion) {
        return !"MAN".equalsIgnoreCase(idTipoCanalDistribucion)
                && "S".equalsIgnoreCase(esVentaCorta);
    }

    /**
     * Stub for callers inside deferred services (CargaArchivoOrdenesRF,
     * CargaArchivoOrdenesRV, CrearAsociaciones). The controller returns 503 for those
     * J-types so this method is never reached at runtime; it exists only to compile.
     */
    public JsonObject execute(JsonObject params) {
        return new JsonObject();
    }

    /**
     * B-ORD-CRE-17: CrearOrdenes.java:1086-1097 — limpiarOrdenes.
     * Mueve las órdenes con errores > 0 a la lista de errores y las elimina de la lista principal.
     */
    public static void limpiarOrdenes(List<OrdenProceso> ordenes, List<OrdenProceso> ordenesConErrores) {
        for (OrdenProceso ord : ordenes) {
            if (ord.errores > 0) ordenesConErrores.add(ord);
        }
        ordenesConErrores.forEach(ordenes::remove);
    }
}
