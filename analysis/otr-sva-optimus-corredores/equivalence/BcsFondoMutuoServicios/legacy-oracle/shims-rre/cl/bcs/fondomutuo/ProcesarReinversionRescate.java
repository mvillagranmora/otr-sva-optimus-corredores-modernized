package cl.bcs.fondomutuo;

/**
 * ORACLE COMPILE-ONLY SHIM (record_rre.sh only; never executed). The legacy CrearSolicitudRescateReinversion.java:36-42 imports seven nested types of the
 * 2.504-LOC legacy ProcesarReinversionRescate and uses none of them: it declares its own BloqueoReinversion / DesbloquearReinversion, which shadow the
 * imports (C-W0A-4). The real class is not compiled into the oracle (its closure is far larger than the subject); these empty types only let the
 * unmodified legacy source compile. Any use of them at run time would be a NoSuchMethodError / missing field, i.e. visible.
 */
public class ProcesarReinversionRescate {
    public static class BloqueoReinversion { }
    public static class DesbloquearReinversion { }
    public static class InEntradaReinversion { }
    public static class InEntradaReinversionAPV { }
    public static class MensajesErrores { }
    public static class ResultadoOperacion { }
    public static class SalidaProcesarReinversionRescate { }
}
