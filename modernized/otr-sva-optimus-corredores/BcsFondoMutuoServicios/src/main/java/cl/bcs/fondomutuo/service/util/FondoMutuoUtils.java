package cl.bcs.fondomutuo.service.util;

import cl.bcs.fondomutuo.domain.ConstantesFondoMutuo;

import java.util.Objects;

/**
 * Pure-logic utility methods for FondoMutuo domain.
 *
 * Migrated from: cl.bcs.fondomutuo.utils.FondoMutuoUtils
 * All methods are static for testability without Spring context.
 *
 * B-FM-UTIL-01: esFlujoCustodiaParaSolicitudProceso
 *   flujo matches solicitud when portafolio+instrumento match and
 *   either regimen is blank OR regimen matches.
 *
 * B-FM-UTIL-02: esRegimenTributarioVacio — null or empty string
 *
 * B-FM-UTIL-03: esCanalIngresoWebServices — WEB or EJE
 *
 * B-FM-UTIL-04: esCanalIngresoDistintoAWebServices — not WEB, not EJE
 */
public final class FondoMutuoUtils {

    private FondoMutuoUtils() {}

    // -------------------------------------------------------------------------
    // B-FM-UTIL-01
    // Source: FondoMutuoUtils.java — esFlujoCustodiaParaSolicitudProceso
    // -------------------------------------------------------------------------
    public static boolean esFlujoCustodiaParaSolicitudProceso(
            Long flujoIdPortafolio,
            Long flujoIdInstrumento,
            String flujoIdRegimenTributario,
            Long solicitudIdPortafolio,
            Long solicitudIdInstrumento,
            String solicitudIdRegimenTributario) {
        Objects.requireNonNull(flujoIdPortafolio, "flujoIdPortafolio");
        Objects.requireNonNull(flujoIdInstrumento, "flujoIdInstrumento");
        Objects.requireNonNull(solicitudIdPortafolio, "solicitudIdPortafolio");
        Objects.requireNonNull(solicitudIdInstrumento, "solicitudIdInstrumento");

        return flujoIdPortafolio.equals(solicitudIdPortafolio)
               && flujoIdInstrumento.equals(solicitudIdInstrumento)
               && (esRegimenTributarioVacio(solicitudIdRegimenTributario)
                   || solicitudIdRegimenTributario.equals(flujoIdRegimenTributario));
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-02
    // Source: FondoMutuoUtils.java — esRegimenTributarioVacio
    // -------------------------------------------------------------------------
    public static boolean esRegimenTributarioVacio(String idRegimenTributario) {
        return idRegimenTributario == null || idRegimenTributario.isEmpty();
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-03
    // Source: FondoMutuoUtils.java — esCanalIngresoWebServices
    // -------------------------------------------------------------------------
    public static boolean esCanalIngresoWebServices(String idTipoCanalIngreso) {
        return ConstantesFondoMutuo.CANAL_WEB.equals(idTipoCanalIngreso)
               || ConstantesFondoMutuo.CANAL_EJECUTIVO.equals(idTipoCanalIngreso);
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-04
    // Source: FondoMutuoUtils.java — esCanalIngresoDistintoAWebServices
    // -------------------------------------------------------------------------
    public static boolean esCanalIngresoDistintoAWebServices(String idTipoCanalIngreso) {
        return !esCanalIngresoWebServices(idTipoCanalIngreso);
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-05: Determine estado from certAuto flag (derived from ConstantesFondoMutuo)
    // Source: common pattern across solicitud services
    // CRT when certAuto='S' and no autorizaciones; PEN otherwise
    // -------------------------------------------------------------------------
    public static String deriveEstadoCertificacion(String certAuto, int cantidadAutorizaciones) {
        Objects.requireNonNull(certAuto, "certAuto");
        boolean autoCertifica = ConstantesFondoMutuo.SI.equalsIgnoreCase(certAuto);
        boolean sinAutorizaciones = cantidadAutorizaciones == 0;
        return (autoCertifica && sinAutorizaciones) ? ConstantesFondoMutuo.ESTADO_CERTIFICADO
                                                    : ConstantesFondoMutuo.ESTADO_PENDIENTE;
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-06: Is reinversion movement type
    // Source: CalcularMontoNumeroCuota.java — "RESREINV" check
    // -------------------------------------------------------------------------
    public static boolean esMovimientoReinversion(String idTipoMovimientoFondoMutuo) {
        return ConstantesFondoMutuo.REINVERSION.equalsIgnoreCase(idTipoMovimientoFondoMutuo);
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-07: Is solicitud of type Aporte
    // -------------------------------------------------------------------------
    public static boolean esTipoSolicitudAporte(String tipoSolicitud) {
        return ConstantesFondoMutuo.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(tipoSolicitud);
    }

    // -------------------------------------------------------------------------
    // B-FM-UTIL-08: Is solicitud of type Rescate
    // -------------------------------------------------------------------------
    public static boolean esTipoSolicitudRescate(String tipoSolicitud) {
        return ConstantesFondoMutuo.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(tipoSolicitud);
    }
}
