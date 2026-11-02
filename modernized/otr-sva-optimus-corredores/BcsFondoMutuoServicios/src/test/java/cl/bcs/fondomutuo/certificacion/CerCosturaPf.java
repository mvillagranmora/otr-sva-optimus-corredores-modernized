package cl.bcs.fondomutuo.certificacion;

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
 * The comparison of a T6-A case (OWN COPY of CerCosturaT5, which is NOT edited): the golden is the WHOLE legacy run of CertificarPorFiltros (and of the real Certificar it calls) and so is the new run, so
 * EVERYTHING the harness can see is compared:
 * <ol>
 * <li>the seed (tablasIniciales) is the one of the golden;</li>
 * <li>the output of the request (counter, messages, results as Certificar answered them), or, when the legacy threw, the exception CLASS (the message only for a plain {@code java.lang.Exception});</li>
 * <li>what the class left in the session BEFORE the host decides and after it (the select writes nothing; Certificar writes);</li>
 * <li>EVERY collaborator call in order, with its COMPLETE arguments and answer: the first and only record of the class itself is {@code CertificarPorFiltros.certificar} (the complete entry it built for
 *     Certificar: header, p_Solicitudes = the rows of every filter, in the order of the filters, duplicates included) and everything Certificar did is inside its field {@code internas};</li>
 * <li>the sequence requests, the decision of the host and the session.</li>
 * </ol>
 * Numbers are compared by VALUE in the calls ({@code 1.50} = {@code 1.5}) and exactly in the tables. The ONLY normalisation is the one of T3 / T4 / T5 (the key {@code p_DatosOrigenInversion} of the Spot
 * entry when it is an empty list). It does NOT restate the rules ({@code CerPfReglasTest} pins them with literals).
 */
public final class CerCosturaPf {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private CerCosturaPf() {
    }

    public static void comparar(String id, JsonObject golden, CasoCerPf.Corrida corrida) {
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            compararTodo(id, golden, corrida);
        } finally {
            TimeZone.setDefault(zona);
        }
    }

    private static void compararTodo(String id, JsonObject golden, CasoCerPf.Corrida c) {
        JsonObject nuevo = c.volcado;
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales (the seed)");
        // the end of the run: an exception of the legacy is an exception of the new code (same class; the message for a plain Exception), otherwise the output is the same
        if (golden.has("excepcion")) {
            assertNotNull(c.excepcionTexto, id + " :: the legacy threw " + golden.get("excepcion") + " and the new code finished");
            if (CasoCerPf.conTag(id, "PF-desvio:kit-1") && golden.get("excepcion").getAsString().startsWith("NullPointerException:")) {
                // DESVIO-KIT-1 (kept by decision of the user, see CasoCer.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION): the legacy NPE of a company with no configuration rows, or a null company, against the kit's
                // "Parametro ... no encontrado"; for these cases, and ONLY these, either outcome passes (everything else of the run is still compared)
                assertTrue(c.excepcionTexto.startsWith("NullPointerException:") || c.excepcionTexto.startsWith("Exception: Parametro ") && c.excepcionTexto.endsWith(" no encontrado"),
                        id + " :: DESVIO-KIT-1 expects the kit's 'Parametro ... no encontrado' or the legacy NullPointerException: " + c.excepcionTexto);
            } else {
                compararExcepcion(id, golden.get("excepcion").getAsString(), c.excepcionTexto);
            }
        } else {
            assertTrue(c.excepcionTexto == null, id + " :: the legacy finished and the new code threw " + c.excepcionTexto);
            assertEquals(txtN(golden.get("salida")), txtN(nuevo.get("salida")), id + " :: output of the request (counter, messages, results)");
        }
        assertEquals(golden.get("decisionServlet").getAsString(), nuevo.get("decisionServlet").getAsString(), id + " :: decision of the host (COMMIT only with counter 0; an exception rolls back)");
        assertEquals(txt(golden.get("tablasTrasLaLlamada")), txt(nuevo.get("tablasTrasLaLlamada")), id + " :: tables as the class left them before the host decides");
        assertEquals(txt(golden.get("tablas")), txt(nuevo.get("tablas")), id + " :: tables after the decision of the host");
        boolean desvio = CasoCerPf.conTag(id, "PF-desvio:kit-1") && golden.has("excepcion") && golden.get("excepcion").getAsString().startsWith("NullPointerException:");
        assertEquals(txtN(proyectar(desvio ? sinElNombreDeLaExcepcion(golden.getAsJsonArray("colaboradores")) : golden.getAsJsonArray("colaboradores"))),
                txtN(proyectar(desvio ? sinElNombreDeLaExcepcion(nuevo.getAsJsonArray("colaboradores")) : nuevo.getAsJsonArray("colaboradores"))),
                id + " :: collaborator calls (name, COMPLETE arguments, answer, order; the child inside its record)");
        assertEquals(txt(golden.get("secuenciasSolicitadas")), txt(nuevo.get("secuenciasSolicitadas")), id + " :: sequence requests (ids of the log of certification)");
        assertEquals(txt(golden.get("sesion")), txt(nuevo.get("sesion")), id + " :: session (no local table without DROP, no autonomous commit)");
    }

    /** DESVIO-KIT-1: the record of the call to Certificar says "error: NullPointerException" (legacy) or "error: Exception" (kit); for the tagged cases the class name of the error is not compared. */
    static JsonArray sinElNombreDeLaExcepcion(JsonArray colaboradores) {
        JsonArray copia = colaboradores.deepCopy();
        for (JsonElement e : copia) {
            JsonObject o = e.getAsJsonObject();
            if (CasoCerPf.PF.equals(o.get("colaborador").getAsString()) && o.has("respuesta") && o.get("respuesta").isJsonPrimitive() && o.get("respuesta").getAsString().startsWith("error: ")) {
                o.addProperty("respuesta", "error: DESVIO-KIT-1");
            }
        }
        return copia;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    static void compararExcepcion(String id, String esperada, String real) {
        String ec = esperada.substring(0, esperada.indexOf(':'));
        String rc = real.substring(0, real.indexOf(':'));
        if (ec.equals("Exception")) {
            assertEquals(esperada.substring(esperada.indexOf(':') + 1), real.substring(real.indexOf(':') + 1), id + " :: exception message of the plain Exception");
        } else {
            assertEquals(ec, rc, id + " :: exception class (" + real + ")");
        }
    }

    /** {colaborador, argumentos, respuesta [, internas]} of every call; the nested records of the child are projected too. */
    public static JsonArray proyectar(JsonArray colaboradores) {
        JsonArray n = new JsonArray();
        for (JsonElement x : colaboradores) {
            JsonObject o = normalizar(x.getAsJsonObject());
            JsonObject m = new JsonObject();
            m.add("colaborador", o.get("colaborador"));
            m.add("argumentos", o.get("argumentos"));
            m.add("respuesta", o.get("respuesta"));
            if (o.has("internas")) m.add("internas", proyectar(o.getAsJsonArray("internas")));
            n.add(m);
        }
        return n;
    }

    /** The Spot entry key the typed class of the module does not declare: removed when it is an empty list, in the golden and in the new code (as T3 / T4). */
    private static JsonObject normalizar(JsonObject llamada) {
        if (!"ProcesarIngresoOperacionSpot".equals(llamada.get("colaborador").getAsString())) return llamada;
        JsonObject copia = llamada.deepCopy();
        for (JsonElement a : copia.getAsJsonArray("argumentos")) {
            if (a.isJsonObject()) {
                JsonObject o = a.getAsJsonObject();
                if (o.has("p_DatosOrigenInversion") && o.get("p_DatosOrigenInversion").isJsonArray() && o.getAsJsonArray("p_DatosOrigenInversion").isEmpty()) o.remove("p_DatosOrigenInversion");
            }
        }
        return copia;
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
