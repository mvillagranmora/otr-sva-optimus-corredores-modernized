package cl.bcs.fondomutuo.normal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.manejador.CasoMan;
import cl.bcs.fondomutuo.manejador.ManejadorOraculoTest;

/**
 * FMM-MAN-nn with the NEW Manejador over the REAL new Normal (step 4A) on the oracle H2: the golden was recorded by the UNMODIFIED legacy Manejador over
 * the legacy Normal. Same dump comparison as 4A's Normal tests: output text (key order, BigDecimal scale), the exception, the servlet decision, the tables
 * (requests, lock, AGF), every call to the collaborators OUTSIDE the destinations (name, arguments, answer), the sequences consumed by BOTH passes and the
 * session (local tables left, autonomous commits). The destinations (scripted) are compared by name, order and the fields the facade sets.
 */
class ManejadorSobreNormalRealTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    static Stream<String> casos() throws Exception {
        List<CasoMan> todos = CasoMan.todos();
        assertThat(todos.size()).isGreaterThanOrEqualTo(15);
        return todos.stream().map(c -> c.id).sorted();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void fmManNormalReal_equivalenteAlLegacy(String id) throws Exception {
        CasoMan c = CasoMan.todos().stream().filter(x -> x.id.equals(id)).findFirst().orElseThrow();
        JsonObject nuevo = ManejadorSobreNormal.correr(c);
        ManejadorOraculoTest.comparar(c, nuevo);
        JsonObject oro = c.esperado;
        for (String clave : new String[] {"tablas", "secuenciasSolicitadas", "sesion"}) {
            assertThat(texto(nuevo.get(clave))).as(id + " :: " + clave).isEqualTo(texto(oro.get(clave)));
        }
        assertThat(texto(fuera(nuevo.getAsJsonArray("colaboradores")))).as(id + " :: collaborators outside the destinations")
                .isEqualTo(texto(fuera(oro.getAsJsonArray("colaboradores"))));
    }

    /** The calls that are not to the seven destinations, reduced to (collaborator, arguments, answer) as 4A does. */
    static JsonArray fuera(JsonArray todas) {
        JsonArray r = new JsonArray();
        JsonArray dest = ManejadorOraculoTest.soloDestinos(todas);
        for (JsonElement e : todas) {
            if (dest.contains(e)) continue;
            JsonObject o = e.getAsJsonObject();
            JsonObject m = new JsonObject();
            m.add("colaborador", o.get("colaborador"));
            m.add("argumentos", o.get("argumentos"));
            m.add("respuesta", o.get("respuesta"));
            r.add(m);
        }
        return r;
    }

    static String texto(JsonElement e) {
        return e == null ? "<absent>" : PRETTY.toJson(canon(e));
    }

    static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k)));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        return e;
    }
}
