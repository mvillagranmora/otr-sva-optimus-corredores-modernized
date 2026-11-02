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
 * The comparison of a T5 case: the golden is the WHOLE legacy run (A..J) and so is the new run, so EVERYTHING the harness can see is compared, never "up to a seam":
 * <ol>
 * <li>the seed (tablasIniciales) is the one of the golden;</li>
 * <li>the output of the request: the JSON of {@code SalidaCertificarSolicitudAporteRescate} (counter, messages, one result per solicitud with state, code, description, the rows of the child
 *     FIRST), or, when the legacy threw, the exception CLASS (the message only for a plain {@code java.lang.Exception}: the parameters of the legacy and the scripted errors);</li>
 * <li>what the class left in the session BEFORE the host decides ({@code tablasTrasLaLlamada}: the UPDATE to CRT, FM-CER-64, the log of certification, ALSO when the legacy threw: the order of
 *     effects) and after the decision of the host ({@code tablas}: COMMIT only with counter 0; an exception rolls back);</li>
 * <li>EVERY collaborator call, with its COMPLETE arguments and the answer it got, in the order of the legacy: the calls of the child certification are inside the record
 *     {@code CertificarSolicitudAporteRescate.hijo} (field {@code internas}), recursively (nested records are compared too);</li>
 * <li>the sequence requests (the ids of the log of certification, parent and child), the decision of the host and the session (no local table without DROP, no autonomous commit).</li>
 * </ol>
 * Numbers are compared by VALUE in the calls ({@code 1.50} = {@code 1.5}: the scale of a BigDecimal of the arguments is not a rule) and exactly in the tables (the dump normalises them). The ONLY
 * normalisation is the one of T3 / T4 (the key {@code p_DatosOrigenInversion} of the Spot entry when it is an empty list: the typed class of the module does not declare it).
 * It does NOT restate the rules ({@code CerT5ReglasTest} pins them with literals read in the golden).
 *
 * <p>The ONE intended difference with the legacy: a recursion deeper than 1 FAILS CLOSED ({@link #compararProfundidad}).
 */
final class CerCosturaT5 {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private CerCosturaT5() {
    }

    static void comparar(String id, JsonObject golden, CasoCerT5.Corrida corrida) {
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            if (CasoCerT5.conTag(id, "T5-profundidad")) compararProfundidad(id, golden, corrida);
            else compararTodo(id, golden, corrida);
        } finally {
            TimeZone.setDefault(zona);
        }
    }

    private static void compararTodo(String id, JsonObject golden, CasoCerT5.Corrida c) {
        JsonObject nuevo = c.volcado;
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales (the seed)");
        // the end of the run: an exception of the legacy is an exception of the new code (same class; the message for a plain Exception), otherwise the output is the same
        if (golden.has("excepcion")) {
            assertNotNull(c.excepcionTexto, id + " :: the legacy threw " + golden.get("excepcion") + " and the new code finished");
            compararExcepcion(id, golden.get("excepcion").getAsString(), c.excepcionTexto);
        } else {
            assertTrue(c.excepcionTexto == null, id + " :: the legacy finished and the new code threw " + c.excepcionTexto);
            assertEquals(txtN(golden.get("salida")), txtN(nuevo.get("salida")), id + " :: output of the request (counter, messages, results)");
        }
        assertEquals(golden.get("decisionServlet").getAsString(), nuevo.get("decisionServlet").getAsString(), id + " :: decision of the host (COMMIT only with counter 0; an exception rolls back)");
        assertEquals(txt(golden.get("tablasTrasLaLlamada")), txt(nuevo.get("tablasTrasLaLlamada")), id + " :: tables as the class left them before the host decides");
        assertEquals(txt(golden.get("tablas")), txt(nuevo.get("tablas")), id + " :: tables after the decision of the host");
        assertEquals(txtN(proyectar(golden.getAsJsonArray("colaboradores"))), txtN(proyectar(nuevo.getAsJsonArray("colaboradores"))),
                id + " :: collaborator calls (name, COMPLETE arguments, answer, order; the child inside its record)");
        assertEquals(txt(golden.get("secuenciasSolicitadas")), txt(nuevo.get("secuenciasSolicitadas")), id + " :: sequence requests (ids of the log of certification)");
        assertEquals(txt(golden.get("sesion")), txt(nuevo.get("sesion")), id + " :: session (no local table without DROP, no autonomous commit)");
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the depth guard (D-F3B-50, review M2): the legacy has NO guard, the new facade FAILS CLOSED when the recursion is deeper than 1
    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void compararProfundidad(String id, JsonObject golden, CasoCerT5.Corrida c) {
        JsonObject nuevo = c.volcado;
        assertEquals(txt(golden.get("tablasIniciales")), txt(nuevo.get("tablasIniciales")), id + " :: tablasIniciales (the seed)");
        assertTrue(!golden.has("excepcion"), id + " :: the legacy ran the three levels to the end (it has no guard)");
        assertNotNull(c.excepcionTexto, id + " :: the new facade must FAIL CLOSED when the recursion is deeper than 1; it finished");
        assertTrue(c.excepcion instanceof cl.bcs.fondomutuo.kit.ServicioNoServidoException || c.delGolden,
                id + " :: the depth guard raises ServicioNoServidoException (503: the router repeats the request in TomEE, which has no limit); it raised " + c.excepcion);
        assertTrue(c.excepcionTexto.startsWith("ServicioNoServidoException"), id + " :: " + c.excepcionTexto);
        assertEquals("ROLLBACK_HTTP503", nuevo.get("decisionServlet").getAsString(), id + " :: decision");
        assertEquals(txt(nuevo.get("tablasIniciales")), txt(nuevo.get("tablas")), id + " :: nothing survives the rollback");
        // the calls: exactly the legacy ones up to the grandchild, whose ENTRY is the legacy one and whose answer is the refusal
        assertEquals(txtN(proyectar(truncarEnLaProfundidad(golden.getAsJsonArray("colaboradores"), 0))), txtN(proyectar(nuevo.getAsJsonArray("colaboradores"))),
                id + " :: collaborator calls up to the refusal of the grandchild (its entry is the legacy one)");
    }

    /** The golden calls up to the first record of the child at the given depth; at depth 1 the record of the grandchild becomes the refusal and everything after it is cut. */
    static JsonArray truncarEnLaProfundidad(JsonArray calls, int nivel) {
        JsonArray out = new JsonArray();
        for (JsonElement e : calls) {
            JsonObject c = e.getAsJsonObject();
            if (!CasoCerT5.HIJO.equals(c.get("colaborador").getAsString())) {
                out.add(c);
                continue;
            }
            JsonObject copia = c.deepCopy();
            if (nivel == 0) {
                copia.addProperty("respuesta", "error: ServicioNoServidoException");
                copia.add("internas", truncarEnLaProfundidad(c.getAsJsonArray("internas"), 1));
            } else {
                copia.addProperty("respuesta", "error: ServicioNoServidoException");
                copia.add("internas", new JsonArray());
            }
            out.add(copia);
            return out;
        }
        return out;
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
    static JsonArray proyectar(JsonArray colaboradores) {
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

    static String txt(JsonElement e) {
        return PRETTY.toJson(canon(e, false));
    }

    /** like {@link #txt} but every number is rendered by VALUE ({@code 1.50} = {@code 1.5}). */
    static String txtN(JsonElement e) {
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
