package cl.bcs.oracle;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;

/**
 * F3 W3 T4-B: OBSERVATION of the two collaborators of CertificarSolicitudAporteRescate that T4 pins and that are NOT registered doubles of the oracle.
 *
 * <p>(1) {@link #hijo}: the legacy certifies the aportes of switch by CALLING ITSELF ({@code new CertificarSolicitudAporteRescate().logica(con, entradaSwitch)},
 * C:2764-2775). A class cannot be doubled by itself, so {@code record_cer_t4.sh} compiles a COPY of the legacy source (outside legacy/) in which that ONE call
 * is replaced by {@code cl.bcs.oracle.T4Spy.hijo(certificarSwitch, con, entradaSwitch)}. This method records the complete entry the legacy built for the child and the
 * answer the child returned, as an ordinary collaborator call named {@link #HIJO} (same shape as the doubles: {colaborador, metodo, argumentos, respuesta}), and moves the
 * calls the CHILD made to its own collaborators ({@code SaldoFlujoCaja}, custody, cuenta de inversion ...) out of the main list into the field {@code internas} of that record:
 * the new code does not run the child (the facade of T5 does), so the main list must hold only what the parent did. The child runs for real, on the same session, with the
 * same scripted doubles. Nothing is altered: the call is delegated untouched and the exception, if any, goes up untouched.
 *
 * <p>(2) {@link #ar}: {@code CrearSolicitudAporteReinversion} ("AR"). Cases whose JSON carries {@code "arReal": true} run the REAL class (the copy compiled as
 * {@code CrearSolicitudAporteReinversionReal}, a rename of the identifier only; the double generated from the legacy source delegates to it through JSON); every other case
 * answers from the script of the case like the other doubles. Either way the call is recorded with its COMPLETE arguments and the answer; with the real class the calls AR made
 * to ITS collaborators go to {@code internas} too.
 */
public final class T4Spy {

    public static final String HIJO = "CertificarSolicitudAporteRescate.hijo";
    public static final String AR = "CrearSolicitudAporteReinversion";

    private static final Gson OUT = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().disableHtmlEscaping().create();
    private static boolean arReal;

    private T4Spy() {
    }

    /** Called by the runner before every run with the case JSON. */
    public static void reiniciar(JsonObject caso) {
        arReal = caso.has("arReal") && caso.get("arReal").getAsBoolean();
    }

    public static boolean arReal() {
        return arReal;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    private static JsonArray internas(JsonArray calls, int desde) {
        JsonArray out = new JsonArray();
        while (calls.size() > desde) out.add(calls.remove(desde));
        return out;
    }

    public static SalidaCertificarSolicitudAporteRescate hijo(CertificarSolicitudAporteRescate certificador, Connection con, EntradaCertificarSolicitudAporteRescate entrada) throws Exception {
        JsonArray calls = Doubles.calls();
        int antes = calls.size();
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", HIJO);
        rec.addProperty("metodo", "logica");
        JsonArray args = new JsonArray();
        args.add(OUT.toJsonTree(entrada));
        rec.add("argumentos", args);
        SalidaCertificarSolicitudAporteRescate salida;
        try {
            salida = certificador.logica(con, entrada);
        } catch (Throwable t) {
            rec.addProperty("respuesta", "error: " + t.getClass().getSimpleName());
            rec.add("internas", internas(calls, antes));
            calls.add(rec);
            T4Spy.<RuntimeException>sneaky(t);
            return null;
        }
        rec.add("respuesta", OUT.toJsonTree(salida));
        rec.add("internas", internas(calls, antes));
        calls.add(rec);
        return salida;
    }

    /** Wraps the call of the real AR: records it as a collaborator call (answer included) and moves what AR itself called into {@code internas}. */
    public static <T> T ar(Object[] argumentos, java.util.function.Supplier<T> real) {
        JsonArray calls = Doubles.calls();
        int antes = calls.size();
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", AR);
        rec.addProperty("metodo", "logica");
        JsonArray args = new JsonArray();
        for (Object a : argumentos) {
            if (a instanceof Connection) continue;
            args.add(OUT.toJsonTree(a));
        }
        rec.add("argumentos", args);
        T salida;
        try {
            salida = real.get();
        } catch (Throwable t) {
            rec.addProperty("respuesta", "error: " + t.getClass().getSimpleName());
            rec.add("internas", internas(calls, antes));
            calls.add(rec);
            T4Spy.<RuntimeException>sneaky(t);
            return null;
        }
        rec.add("respuesta", OUT.toJsonTree(salida));
        rec.add("internas", internas(calls, antes));
        calls.add(rec);
        return salida;
    }
}
