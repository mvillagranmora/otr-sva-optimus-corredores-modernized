package cl.bcs.fondomutuo.liquidacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The comparison of a T6-B case (OWN COPY of {@code CerCosturaPf}, which is NOT edited): the golden is the WHOLE legacy run of {@code LiquidacionSolicitudAporteRescate.call} and so is the new run (through the
 * production JavaCall handler), so EVERYTHING the harness can see is compared:
 * <ol>
 * <li>the seed (tablasIniciales) is the one of the golden;</li>
 * <li>the output of the request (counter RECOUNTED by the legacy call, messages, results in order), or, when the legacy threw, the exception CLASS (the message only for a plain {@code java.lang.Exception});</li>
 * <li>the host decision;</li>
 * <li>what the class left in the table of the solicitudes BEFORE the host decides and after it;</li>
 * <li>EVERY collaborator call in order, with its COMPLETE arguments and answer (Tesoreria: both lists, every field; CuentaInversion: every line; AgregarLog: the empty log);</li>
 * <li>the sequence requests and the session.</li>
 * </ol>
 * Numbers are compared by VALUE in the calls ({@code 1.50} = {@code 1.5}) and exactly in the tables. It does NOT restate the rules ({@code LiqReglasTest} pins them with literals).
 */
public final class CosturaLiq {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private CosturaLiq() {
    }

    public static void comparar(String id, JsonObject golden, CasoLiq.Corrida corrida) {
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            compararTodo(id, golden, corrida);
        } finally {
            TimeZone.setDefault(zona);
        }
    }

    private static void compararTodo(String id, JsonObject golden, CasoLiq.Corrida c) {
        JsonObject nuevo = c.volcado;
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales (the seed)");
        // the end of the run: an exception of the legacy is an exception of the new code (same class; the message for a plain Exception), otherwise the output is the same
        boolean desvio = CasoLiq.conTag(id, "LIQ-desvio:kit-1") && golden.has("excepcion") && golden.get("excepcion").getAsString().startsWith("NullPointerException:");
        if (golden.has("excepcion")) {
            assertNotNull(c.excepcionTexto, id + " :: the legacy threw " + golden.get("excepcion") + " and the new code finished");
            if (desvio) {
                // DESVIO-KIT-1 (kept by decision of the user, see CasoCer.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION): the legacy NPE of a company with no configuration rows, or a null company, against the kit's
                // "Parametro ... no encontrado"; for these cases, and ONLY these, either outcome passes (everything else of the run is still compared)
                assertTrue(c.excepcionTexto.startsWith("NullPointerException:") || c.excepcionTexto.startsWith("Exception: Parametro ") && c.excepcionTexto.endsWith(" no encontrado"),
                        id + " :: DESVIO-KIT-1 expects the kit's 'Parametro ... no encontrado' or the legacy NullPointerException: " + c.excepcionTexto);
            } else {
                compararExcepcion(id, golden.get("excepcion").getAsString(), c.excepcionTexto);
            }
        } else {
            assertTrue(c.excepcionTexto == null, id + " :: the legacy finished and the new code threw " + c.excepcionTexto);
            assertEquals(txtN(golden.get("salida")), txtN(nuevo.get("salida")), id + " :: output of the request (counter recounted by call(), messages, results)");
        }
        assertEquals(golden.get("decisionServlet").getAsString(), nuevo.get("decisionServlet").getAsString(), id + " :: decision of the host (COMMIT only with counter 0; an exception rolls back)");
        assertEquals(txt(golden.get("tablasTrasLaLlamada")), txt(nuevo.get("tablasTrasLaLlamada")), id + " :: table of the solicitudes as the class left it before the host decides");
        assertEquals(txt(golden.get("tablas")), txt(nuevo.get("tablas")), id + " :: table of the solicitudes after the decision of the host");
        assertEquals(txtN(proyectar(golden.getAsJsonArray("colaboradores"))), txtN(proyectar(nuevo.getAsJsonArray("colaboradores"))),
                id + " :: collaborator calls (name, COMPLETE arguments, answer, order)");
        assertEquals(txt(golden.get("secuenciasSolicitadas")), txt(nuevo.get("secuenciasSolicitadas")), id + " :: sequence requests (carta and comprobante of the liquidation)");
        assertEquals(txt(golden.get("sesion")), txt(nuevo.get("sesion")), id + " :: session (no local table without DROP, no autonomous commit)");
    }

    static void compararExcepcion(String id, String esperada, String real) {
        String ec = esperada.substring(0, esperada.indexOf(':'));
        String rc = real.substring(0, real.indexOf(':'));
        if (ec.equals("Exception")) {
            assertEquals(esperada.substring(esperada.indexOf(':') + 1), real.substring(real.indexOf(':') + 1), id + " :: exception message of the plain Exception");
        } else {
            assertEquals(ec, rc, id + " :: exception class (" + real + ")");
        }
    }

    /** {colaborador, argumentos, respuesta} of every call. */
    public static JsonArray proyectar(JsonArray colaboradores) {
        JsonArray n = new JsonArray();
        for (JsonElement x : colaboradores) {
            JsonObject o = x.getAsJsonObject();
            JsonObject m = new JsonObject();
            m.add("colaborador", o.get("colaborador"));
            m.add("argumentos", o.get("argumentos"));
            m.add("respuesta", o.get("respuesta"));
            n.add(m);
        }
        return n;
    }

    public static String txt(JsonElement e) {
        return PRETTY.toJson(canon(e, false));
    }

    /** like {@link #txt} but every number is rendered by VALUE ({@code 1.50} = {@code 1.5}). */
    public static String txtN(JsonElement e) {
        return PRETTY.toJson(canon(e, true));
    }

    static JsonElement canon(JsonElement e, boolean numeros) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x, numeros));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k), numeros));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        if (numeros && e.getAsJsonPrimitive().isNumber()) {
            return new JsonPrimitive("#" + new BigDecimal(e.getAsString()).stripTrailingZeros().toPlainString());
        }
        return e;
    }
}
