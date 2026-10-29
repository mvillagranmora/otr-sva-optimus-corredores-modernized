package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/**
 * Pure rules extracted from {@code CrearSolicitudRescateReinversion.logica} WITHOUT changing the order of the checks (the only refactor allowed, plan 5.2):
 * each method is the literal legacy expression, including the points where it throws (a null series, a null field): same exception, same place in the flow.
 */
final class RreReglas {

    private RreReglas() {
    }

    /** Legacy nested {@code KeyAporteMonto}: hashCode {@code 31 * IdAgrupador.hashCode() + IdSerieFondo.hashCode()} (null fields: NullPointerException). */
    static final class KeyAporteMonto {
        Long IdAgrupador;
        String IdSerieFondo;

        @Override
        public int hashCode() {
            return 31 * this.IdAgrupador.hashCode() + this.IdSerieFondo.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            KeyAporteMonto llave = (KeyAporteMonto) obj;
            return this.IdAgrupador.equals(llave.IdAgrupador) && this.IdSerieFondo.equals(llave.IdSerieFondo);
        }
    }

    /** {@code FondoMutuoUtils.esCanalIngresoWebServices}: WEB or EJE ({@code equals}, case sensitive; null is not). */
    static boolean esCanalIngresoWebServices(String canal) {
        return RreConstantes.CANAL_WEB.equals(canal) || RreConstantes.CANAL_EJECUTIVO.equals(canal);
    }

    /**
     * FM-REI-07 (RR:449-458): the four independent {@code if} of the type of reinversion of a rescue against the contribution with its IdInterno; the
     * last one that holds wins. Returns the new {@code SonMonedasDiferentes} ("S"/"N") or {@code null} when the third rule did not apply (unchanged).
     */
    static String tipoReinversionRescate(InSolicitudesRescate rescate, SerieFondoSolicitudes serieRescate, SerieFondoSolicitudes serieAporte) {
        String monedas = null;
        if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S") && serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
            rescate.TipoReinversion = RreConstantes.SWITCH;
        if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("S") && !serieRescate.IdEmisor.equalsIgnoreCase(serieAporte.IdEmisor))
            rescate.TipoReinversion = RreConstantes.INTERNACIONAL;
        if (serieRescate.FondoExtranjero.equalsIgnoreCase("S") && serieAporte.FondoExtranjero.equalsIgnoreCase("N")) {
            rescate.TipoReinversion = RreConstantes.NACIONAL;
            monedas = !serieRescate.IdMoneda.equalsIgnoreCase(serieAporte.IdMoneda) ? RreConstantes.SI : RreConstantes.NO;
        }
        if (serieRescate.FondoExtranjero.equalsIgnoreCase("N"))
            rescate.TipoReinversion = RreConstantes.NACIONAL;
        return monedas;
    }

    /** FM-REI-12 (RR:1439-1446): the same four rules for a contribution against its origin series (no currency flag). */
    static void tipoReinversionAporte(InSolicitudesAporte solicitud, SerieFondoSolicitudes serieOrigen, SerieFondoSolicitudes serieDestino) {
        if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S") && serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
            solicitud.TipoReinversion = RreConstantes.SWITCH;
        if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("S") && !serieOrigen.IdEmisor.equalsIgnoreCase(serieDestino.IdEmisor))
            solicitud.TipoReinversion = RreConstantes.INTERNACIONAL;
        if (serieOrigen.FondoExtranjero.equalsIgnoreCase("S") && serieDestino.FondoExtranjero.equalsIgnoreCase("N"))
            solicitud.TipoReinversion = RreConstantes.NACIONAL;
        if (serieOrigen.FondoExtranjero.equalsIgnoreCase("N"))
            solicitud.TipoReinversion = RreConstantes.NACIONAL;
    }

    /** SWITCH (2), INTERNACIONAL (4), else the days of the series (RR:487, :1475). */
    static Long retencion(String tipo, Long diasSerie) {
        return tipo.equalsIgnoreCase(RreConstantes.SWITCH) ? 2 : (tipo.equalsIgnoreCase(RreConstantes.INTERNACIONAL) ? 4 : diasSerie);
    }

    static boolean switchOInternacional(String tipo) {
        return tipo.equalsIgnoreCase(RreConstantes.SWITCH) || tipo.equalsIgnoreCase(RreConstantes.INTERNACIONAL);
    }

    /**
     * FM-REI-09 (RR:512-519): the lock id by POSITION (rescue i and contribution i of the lists, D-F2-10), with {@code String +}: a BigDecimal is its
     * {@code toString()} (scale counts), a null amount is the text "null".
     */
    static String idBloqueo(InSolicitudesRescate rescate, InSolicitudesAporte aportePosicion, String fechaSistema) {
        String regimenTributario = rescate.IdRegimenTributario != null ? rescate.IdRegimenTributario : "";
        BigDecimal montoCuotas = "C".equalsIgnoreCase(rescate.CalculoPorMontoCuota) ? rescate.NumeroCuotas : rescate.Monto;
        return rescate.IdPortafolio.toString() + '_' + regimenTributario + '_' + aportePosicion.IdSerieFondo + '_' + montoCuotas + '_' + fechaSistema.toString();
    }

    /** FM-REI-18 (RR:814-830): the taxation by regime group ({@code switch} on a String: a null group is a NullPointerException). */
    static String tributacion(String grupoRegimen) {
        switch (grupoRegimen) {
        case "APV":
            return RreConstantes.TRIBUTACION_APV;
        case "ART108":
            return RreConstantes.TRIBUTACION_ART108;
        case "ART107":
            return RreConstantes.TRIBUTACION_ART107;
        case "57BIS":
            return RreConstantes.TRIBUTACION_57BIS;
        default:
            return RreConstantes.TRIBUTACION_DEFECTO;
        }
    }
}
