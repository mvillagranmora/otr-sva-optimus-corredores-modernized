package cl.bcs.apv.adapter.javacall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;

/**
 * Pins the equivalence recorded by the dual oracle ({@code analysis/.../equivalence/BcsAPVApi}) against the mappers of THIS module, byte for byte:
 * <ol>
 *   <li>default behaviour: {@code Entrada.porDefecto()} / {@code Resultado.porDefecto()} through the mapper of each of the 64 operations, serialised with the Gson flavour
 *       of the mapper, must be exactly the text the real legacy classes produced (goldens {@code legacy/<Caso>/*.json}) and the recorded new golden;</li>
 *   <li>fresh inputs ({@code fresh/input/<id>.json}: populated, limit values, partial/extra/null keys): legacy JSON in, record, legacy JSON out must equal the golden of
 *       the legacy Gson round trip ({@code fresh/legacy/<id>/out.json}).</li>
 * </ol>
 * Finding F-APV-01 (fixed 2026-10-30): 4 inputs of profile P2 that the legacy accepts used to be REJECTED by the mapper because a list of an opaque external
 * Tesoreria class ({@code JsonObject}) holds a {@code null} element; {@code MapeoJson.listaEscalar} now keeps it as a null element. The set of rejected inputs must stay
 * empty. Never skips: missing goldens fail.
 */
class ApvEquivalenciaGoldensTest {

    /** fresh ids the legacy accepts and the new mapper rejects: none (finding F-APV-01 fixed, see ORACLE_NOTES.md) */
    private static final List<String> RECHAZADAS_POR_EL_NUEVO = List.of();

    private final ApvJavaCallRegistro registro = new ApvJavaCallRegistro();

    private static Path equivalencia() {
        String env = System.getenv("APV_EQUIVALENCE_DIR");
        Path base = Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
        Path p = env != null && !env.isBlank() ? Path.of(env) : base.resolve("../../../analysis/otr-sva-optimus-corredores/equivalence/BcsAPVApi").normalize();
        if (!Files.isDirectory(p.resolve("legacy"))) {
            throw new AssertionError("equivalence goldens not found (the default behaviour cannot be left unpinned): " + p);
        }
        return p;
    }

    private static String leer(Path p) throws IOException {
        assertTrue(Files.isRegularFile(p), "missing golden " + p);
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    private static Sabor sabor(Object mapeo) throws Exception {
        Field f = mapeo.getClass().getDeclaredField("S");
        f.setAccessible(true);
        return (Sabor) f.get(null);
    }

    private static Object porDefecto(OperacionRegistrada reg, boolean entrada) throws Exception {
        for (Method m : reg.interfaz().getDeclaredMethods()) {
            if (m.getName().equals("ejecutar") && !m.isBridge() && !m.isSynthetic()) {
                return (entrada ? m.getParameterTypes()[0] : m.getReturnType()).getMethod("porDefecto").invoke(null);
            }
        }
        throw new AssertionError("ejecutar not found in " + reg.interfaz());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void equiv_01_porDefecto_por_el_mapeador_es_byte_a_byte_lo_que_produce_el_legacy_en_las_64_operaciones() throws Exception {
        Path eq = equivalencia();
        List<String> fallos = new ArrayList<>();
        int n = 0;
        for (OperacionRegistrada reg : registro.todas()) {
            if (!reg.invocable()) {
                continue;
            }
            MapeoOperacion mapeo = reg.mapeo();
            Sabor s = sabor(mapeo);
            String caso = reg.casoUso();
            String e = s.gson().toJson(mapeo.entradaAJson(porDefecto(reg, true)));
            String r = s.gson().toJson(mapeo.resultadoAJson(porDefecto(reg, false)));
            for (String[] par : new String[][] { { "entrada", e }, { "salida", r } }) {
                String legacy = leer(eq.resolve("legacy").resolve(caso).resolve(par[0] + ".json"));
                String nuevo = leer(eq.resolve("new").resolve(caso).resolve(par[0] + ".json"));
                n++;
                if (!legacy.equals(par[1])) {
                    fallos.add(caso + "." + par[0] + " differs from the legacy golden");
                }
                if (!nuevo.equals(par[1])) {
                    fallos.add(caso + "." + par[0] + " differs from the recorded new golden (re-run oracle/correr_nuevo.sh)");
                }
            }
        }
        assertEquals(128, n);
        assertEquals(List.of(), fallos);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void equiv_02_entradas_frescas_ida_y_vuelta_json_legacy_record_json_igualan_al_legacy_sin_rechazadas() throws Exception {
        Path eq = equivalencia();
        JsonObject index = JsonParser.parseString(leer(eq.resolve("fresh/input/index.json"))).getAsJsonObject();
        assertEquals(384, index.getAsJsonArray("entradas").size(), "64 operations x (Entrada, Salida) x profiles P1, P2, P3");
        List<String> fallos = new ArrayList<>();
        TreeSet<String> rechazadas = new TreeSet<>();
        int comparados = 0;
        for (JsonElement el : index.getAsJsonArray("entradas")) {
            JsonObject f = el.getAsJsonObject();
            String id = f.get("id").getAsString();
            OperacionRegistrada reg = registro.todas().stream().filter(o -> id.startsWith("F-" + o.casoUso() + "-") && o.invocable()
                    && id.substring(3 + o.casoUso().length()).matches("[ES]-P[123]")).findFirst().orElseThrow(() -> new AssertionError("no operation for " + id));
            MapeoOperacion mapeo = reg.mapeo();
            Sabor s = sabor(mapeo);
            JsonObject entrada = JsonParser.parseString(leer(eq.resolve("fresh/input/" + id + ".json"))).getAsJsonObject();
            String legacy = leer(eq.resolve("fresh/legacy/" + id + "/out.json"));
            assertFalse(legacy.equals("{}"), id + ": a fresh input must produce a non-empty legacy output");
            try {
                JsonObject out = f.get("lado").getAsString().equals("entrada") ? mapeo.entradaAJson(mapeo.entradaDesdeJson(entrada))
                        : mapeo.resultadoAJson(mapeo.resultadoDesdeJson(entrada));
                comparados++;
                if (!s.gson().toJson(out).equals(legacy)) {
                    fallos.add(id + " differs from the legacy round trip");
                }
            } catch (RuntimeException ex) {
                rechazadas.add(id);
            }
        }
        assertEquals(List.of(), fallos);
        assertEquals(RECHAZADAS_POR_EL_NUEVO, new ArrayList<>(rechazadas), "inputs the legacy accepts and the mapper rejects (finding F-APV-01 stays fixed)");
        assertEquals(384, comparados);
    }

    @Test
    void equiv_03_la_comparacion_registrada_de_entradas_frescas_dice_384_iguales_0_diferencias_0_faltantes() throws Exception {
        Path eq = equivalencia();
        JsonObject tot = JsonParser.parseString(leer(eq.resolve("FRESH_EQUIVALENCE.json"))).getAsJsonObject().getAsJsonObject("totals");
        assertEquals(384, tot.get("cases").getAsInt());
        assertEquals(384, tot.get("same").getAsInt(), tot.toString());
        assertEquals(0, tot.get("differs").getAsInt(), tot.toString());
        assertEquals(0, tot.get("missing").getAsInt(), tot.toString());
        assertEquals(0, tot.get("differsApproved").getAsInt(), tot.toString());
    }
}
