package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.api.casouso.CasosUso;

/**
 * Pins the DEFAULT behaviour of the 64 operations recorded by the dual oracle ({@code analysis/.../equivalence/BcsAPVApi}): the legacy Entrada / Salida
 * classes instantiated with their constructors and serialised by the Gson of {@code call()} (goldens {@code legacy/<Caso>/entrada.json, salida.json}), against
 * the {@code porDefecto()} of the records of this module.
 *
 * <p>The adapter that turns a record into the legacy JSON lives in another module (BcsApvJavaCallAdapter has its own byte comparison). Here, without the mapper,
 * the record and the golden must agree on STRUCTURE: for every operation and side, the number of JSON keys of the legacy golden (recursively, list elements
 * included) is exactly what the record default produces (all components for the {@code Utils.getGsonBuilder()} flavour, only non-null ones for plain
 * {@code new Gson()}). A wrong default (a list that should be {@code [null]}, a counter that should be 0, a nested object that should exist) changes the count.
 *
 * <p>It never skips: goldens that are not where the layout says, or a recorded comparison that is not exactly 128 same / 0 differences, is a failure
 * (to refresh: oracle/compilar_legacy.sh, correr_legacy.sh, correr_nuevo.sh, make_cases.py and compare.py, see ORACLE_NOTES.md).
 */
class ApvApiEquivalenciaDefectoGoldensTest {

    static Path equivalencia() {
        String env = System.getenv("APV_EQUIVALENCE_DIR");
        Path p = env != null && !env.isBlank() ? Path.of(env)
                : LegacyApv.modulo().resolve("../../../analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi").normalize();
        if (!Files.isDirectory(p.resolve("legacy"))) {
            throw new AssertionError("equivalence goldens not found (the default behaviour cannot be left unpinned): " + p);
        }
        return p;
    }

    private static JsonObject json(Path p) throws IOException {
        assertTrue(Files.isRegularFile(p), "missing golden " + p);
        String t = Files.readString(p, StandardCharsets.UTF_8);
        assertFalse(t.isBlank(), "empty golden " + p);
        return JsonParser.parseString(t).getAsJsonObject();
    }

    @Test
    void equiv_defecto_01_hay_golden_legacy_y_nuevo_para_las_64_operaciones_y_ambos_lados() throws Exception {
        Path eq = equivalencia();
        assertEquals(64, CasosUso.TODOS.size());
        int n = 0;
        for (Class<?> c : CasosUso.TODOS) {
            for (String lado : List.of("entrada", "salida")) {
                for (String d : List.of("legacy", "new")) {
                    json(eq.resolve(d).resolve(c.getSimpleName()).resolve(lado + ".json"));
                    n++;
                }
            }
        }
        assertEquals(256, n);
        JsonObject idx = json(eq.resolve("legacy/index.json"));
        assertEquals(64, idx.getAsJsonArray("operaciones").size());
    }

    @Test
    void equiv_defecto_02_la_comparacion_registrada_es_128_iguales_0_diferencias_0_faltantes() throws Exception {
        Path eq = equivalencia();
        JsonObject cases = json(eq.resolve("cases.json"));
        assertEquals(128, cases.getAsJsonArray("cases").size(), "64 operations x (Entrada, Salida)");
        JsonObject tot = json(eq.resolve("EQUIVALENCE.json")).getAsJsonObject("totals");
        assertEquals(128, tot.get("cases").getAsInt());
        assertEquals(128, tot.get("same").getAsInt(), "same: " + tot);
        assertEquals(0, tot.get("differs").getAsInt(), "differs: " + tot);
        assertEquals(0, tot.get("missing").getAsInt(), "missing: " + tot);
        assertEquals(0, tot.get("differsApproved").getAsInt(), "no difference may be approved away silently: " + tot);
    }

    @Test
    void equiv_defecto_03_porDefecto_de_cada_record_tiene_la_misma_estructura_que_el_golden_del_legacy() throws Exception {
        Path eq = equivalencia();
        Map<String, String> sabor = new java.util.HashMap<>();
        for (JsonElement e : json(eq.resolve("legacy/index.json")).getAsJsonArray("operaciones")) {
            sabor.put(e.getAsJsonObject().get("caso").getAsString(), e.getAsJsonObject().get("gson").getAsString());
        }
        List<String> fallos = new ArrayList<>();
        int comparados = 0;
        for (Class<?> c : CasosUso.TODOS) {
            String caso = c.getSimpleName();
            assertTrue(sabor.containsKey(caso), "no legacy index entry for " + caso);
            boolean nulos = sabor.get(caso).equals("builder");
            String pkg = "cl.bcs.apv.api.dto." + caso.toLowerCase() + ".";
            for (String lado : List.of("Entrada", "Resultado")) {
                Class<?> rec = Class.forName(pkg + caso + lado);
                Object def = rec.getMethod("porDefecto").invoke(null);
                JsonObject g = json(eq.resolve("legacy").resolve(caso).resolve(lado.equals("Entrada") ? "entrada.json" : "salida.json"));
                int esperado = claves(g);
                int real = claves(def, nulos);
                comparados++;
                if (esperado != real) {
                    fallos.add(caso + lado + ": legacy golden has " + esperado + " keys, porDefecto() gives " + real + " (" + sabor.get(caso) + ")");
                }
            }
        }
        assertEquals(128, comparados);
        assertEquals(List.of(), fallos, "defaults of the records that do not match the legacy constructors");
    }

    /** recursive number of keys of a JSON object (list elements that are objects included) */
    static int claves(JsonElement e) {
        int n = 0;
        if (e.isJsonObject()) {
            for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
                n += 1 + claves(en.getValue());
            }
        } else if (e.isJsonArray()) {
            for (JsonElement x : (JsonArray) e) {
                n += claves(x);
            }
        }
        return n;
    }

    /** same count computed from a record default: components (all, or non-null ones), nested records and list elements recursively */
    static int claves(Object v, boolean nulos) throws Exception {
        if (v == null) {
            return 0;
        }
        if (v instanceof List<?> l) {
            int n = 0;
            for (Object x : l) {
                n += claves(x, nulos);
            }
            return n;
        }
        if (v instanceof JsonObject jo) {
            return claves(jo);
        }
        if (!v.getClass().isRecord()) {
            return 0;
        }
        int n = 0;
        for (RecordComponent rc : v.getClass().getRecordComponents()) {
            Method m = rc.getAccessor();
            Object x = m.invoke(v);
            if (x == null && !nulos) {
                continue;
            }
            n += 1 + claves(x, nulos);
        }
        return n;
    }
}
