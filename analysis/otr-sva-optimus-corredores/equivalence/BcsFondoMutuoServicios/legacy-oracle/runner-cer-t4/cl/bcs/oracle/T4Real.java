package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.EntradaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.SalidaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversionReal;

/**
 * F3 W3 T4-B: the bridge of the generated double of {@code CrearSolicitudAporteReinversion} (AR) to the REAL class when the case says {@code "arReal": true}.
 * The double (generated from the legacy source) keeps every declaration of the original, so Certificar compiles unchanged against it; the real class is compiled
 * under the name {@code CrearSolicitudAporteReinversionReal} (a rename of the identifier, nothing else: see record_cer_t4.sh). The entry travels as JSON from the nested
 * type of the double to the nested type of the real class and the answer travels back; {@link T4Spy#ar} records the call (complete arguments and answer) and moves the calls
 * AR made to ITS collaborators (the registered doubles) out of the main list. The real AR runs on the SAME connection as Certificar, as in the legacy.
 */
public final class T4Real {

    private static final Gson OUT = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().disableHtmlEscaping().create();
    private static final Gson IN = cl.bcs.utils.Utils.getGsonBuilder();

    private T4Real() {
    }

    public static SalidaCrearSolicitudAporteReinversion ar(Connection con, EntradaCrearSolicitudAporteReinversion entrada) {
        return T4Spy.ar(new Object[] { con, entrada }, () -> {
            try {
                CrearSolicitudAporteReinversionReal.EntradaCrearSolicitudAporteReinversion real = IN.fromJson(OUT.toJson(entrada), CrearSolicitudAporteReinversionReal.EntradaCrearSolicitudAporteReinversion.class);
                CrearSolicitudAporteReinversionReal.SalidaCrearSolicitudAporteReinversion salida = new CrearSolicitudAporteReinversionReal().logica(con, real);
                return IN.fromJson(OUT.toJson(salida), SalidaCrearSolicitudAporteReinversion.class);
            } catch (Exception e) {
                T4Real.<RuntimeException>sneaky(e);
                return null;
            }
        });
    }

    /** The nested type of the real AR to the one of the double of AR (the double of ProcesarIngresoOperacionSpot declares its list with it): same JSON, same values. */
    public static cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.InSolicitudesAporte puente(Object real) {
        return IN.fromJson(OUT.toJson(real), cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.InSolicitudesAporte.class);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }
}
