package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.replica.oraculo.ConexionRegistradora;
import cl.bcs.replica.oraculo.GuionColaboradores;
import cl.bcs.replica.oraculo.VolcadoTablas;

/**
 * Loads a case of {@code src/test/resources/replica-caracterizacion/cases/<id>/} (case.json: servicio, ref, entrada, colaboradores, fallos, tablas, reloj; seed.sql; expected.json = the golden
 * recorded from the unmodified legacy by analysis/.../equivalence/BcsReplica/legacy-oracle/record.sh), runs the production unit on H2 behind the same recording connection the oracle used
 * ({@link ConexionRegistradora}) and compares what it did with the golden: the entry point's output or exception, the SQL sent in order, the collaborator calls and the tables.
 */
final class CasoReplica {
    static final String RAIZ = "/replica-caracterizacion/";
    static final String RELOJ = "2026-03-02 14:30:00";
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger BASES = new AtomicInteger();

    private CasoReplica() {
    }

    static Path directorio() {
        try {
            return Paths.get(CasoReplica.class.getResource(RAIZ + "cases").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    static List<String> casos() {
        try (Stream<Path> s = Files.list(directorio())) {
            return s.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static JsonObject leer(Path p) {
        try {
            return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static JsonObject caso(String id) {
        return leer(directorio().resolve(id).resolve("case.json"));
    }

    static JsonObject golden(String id) {
        return leer(directorio().resolve(id).resolve("expected.json"));
    }

    static List<String> refs(String id) {
        List<String> r = new ArrayList<>();
        for (JsonElement e : caso(id).getAsJsonArray("ref")) r.add(e.getAsString());
        return r;
    }

    private static final java.util.Map<String, JsonObject> CORRIDAS = new java.util.concurrent.ConcurrentHashMap<>();

    /** the run of a case with every gate open and the collaborators scripted, once per JVM (the rule tests read the same runs the equivalence test compares) */
    static JsonObject corrida(String id) throws Exception {
        JsonObject c = CORRIDAS.get(id);
        if (c == null) {
            c = ejecutar(id);
            CORRIDAS.put(id, c);
        }
        return c;
    }

    /** the only case whose id contains every fragment (the number of a case may change when a generator grows; its slug does not) */
    static String id(String... fragmentos) {
        List<String> l = casos().stream().filter(c -> java.util.Arrays.stream(fragmentos).allMatch(c::contains)).collect(Collectors.toList());
        assertEquals(1, l.size(), "exactly one case must contain " + java.util.Arrays.toString(fragmentos) + ": " + l);
        return l.get(0);
    }

    static String idPorPrefijo(String prefijo) {
        List<String> l = casos().stream().filter(c -> c.startsWith(prefijo)).collect(Collectors.toList());
        assertEquals(1, l.size(), "exactly one case must start with " + prefijo + ": " + l);
        return l.get(0);
    }

    static String texto(String recurso) {
        try (var in = CasoReplica.class.getResourceAsStream(RAIZ + recurso)) {
            if (in == null) throw new IllegalStateException("missing test resource " + recurso);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void script(Connection conn, String sql) throws Exception {
        List<String> limpio = new ArrayList<>();
        for (String linea : sql.split("\n")) {
            if (!linea.strip().startsWith("--")) limpio.add(linea);
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", limpio).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }

    /** Runs the case of the directory {@code id} with the contexto adjusted by {@code ajuste} (null: every gate open, the collaborators scripted: what the legacy does). */
    static JsonObject ejecutar(String id, java.util.function.UnaryOperator<ApiReplica.Contexto> ajuste) throws Exception {
        JsonObject c = caso(id);
        Path semilla = directorio().resolve(id).resolve("seed.sql");
        return ejecutar(c, Files.exists(semilla) ? Files.readString(semilla, StandardCharsets.UTF_8) : null, ajuste);
    }

    static JsonObject ejecutar(String id) throws Exception {
        return ejecutar(id, null);
    }

    static JsonObject ejecutar(JsonObject caso, String semilla, java.util.function.UnaryOperator<ApiReplica.Contexto> ajuste) throws Exception {
        String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ;
        String url = "jdbc:h2:mem:repcar" + BASES.incrementAndGet() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        try {
            script(raw, texto("schema-replica.sql"));
            if (semilla != null) script(raw, semilla);
            raw.commit();
            raw.setAutoCommit(false);
            ConexionRegistradora registro = new ConexionRegistradora(raw, caso.has("fallos") ? caso.getAsJsonArray("fallos") : null, reloj);
            GuionColaboradores guion = new GuionColaboradores(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());
            ApiReplica.Contexto ctx = ApiReplica.Contexto.abierto(guion, reloj);
            if (ajuste != null) ctx = ajuste.apply(ctx);
            JsonArray tablas = caso.has("tablas") ? caso.getAsJsonArray("tablas") : null;
            JsonObject res = new JsonObject();
            res.add("tablasIniciales", VolcadoTablas.volcar(raw, tablas));
            String servicio = caso.get("servicio").getAsString();
            try {
                JsonElement salida = ApiReplica.ejecutar(servicio, registro.conexion(), caso.getAsJsonObject("entrada"), ctx);
                if (salida != null) res.add("salida", GuionColaboradores.sinNulos(salida));
                else if (servicio.endsWith(".call")) res.addProperty("salidaNula", true);
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable t) {
                res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + t.getMessage());
            }
            res.add("sentencias", registro.sentencias());
            res.add("colaboradores", guion.llamadas());
            res.add("tablas", VolcadoTablas.volcar(raw, tablas));
            return res;
        } finally {
            try (Statement st = raw.createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception e) {
                // the database is gone anyway
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------ comparison

    /** the class of the exception; the message only for a plain java.lang.Exception (the scripted collaborator errors and the "Empresa no existe" of the legacy pass through unchanged) */
    static String excepcionComparable(String excepcion) {
        int i = excepcion.indexOf(':');
        String clase = i > 0 ? excepcion.substring(0, i) : excepcion;
        return clase.equals("Exception") ? excepcion : clase;
    }

    static JsonElement canonico(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (var x : e.getAsJsonObject().entrySet()) m.put(x.getKey(), canonico(x.getValue()));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canonico(x));
            return a;
        }
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isNumber()) return new JsonPrimitive(new BigDecimal(new BigDecimal(p.getAsString()).stripTrailingZeros().toPlainString()));
        return p;
    }

    static String primeraDiferencia(JsonElement a, JsonElement b, String ruta) {
        if (a.equals(b)) return null;
        if (a.isJsonArray() && b.isJsonArray()) {
            JsonArray x = a.getAsJsonArray();
            JsonArray y = b.getAsJsonArray();
            for (int i = 0; i < Math.max(x.size(), y.size()); i++) {
                if (i >= x.size()) return ruta + "[" + i + "]: the golden has " + x.size() + " items, the unit produced an extra " + resumen(y.get(i));
                if (i >= y.size()) return ruta + "[" + i + "]: the unit produced " + y.size() + " items, the golden has an extra " + resumen(x.get(i));
                String d = primeraDiferencia(x.get(i), y.get(i), ruta + "[" + i + "]");
                if (d != null) return d;
            }
        }
        if (a.isJsonObject() && b.isJsonObject()) {
            for (String k : a.getAsJsonObject().keySet()) {
                if (!b.getAsJsonObject().has(k)) return ruta + "." + k + ": missing in the unit's output";
                String d = primeraDiferencia(a.getAsJsonObject().get(k), b.getAsJsonObject().get(k), ruta + "." + k);
                if (d != null) return d;
            }
            for (String k : b.getAsJsonObject().keySet()) {
                if (!a.getAsJsonObject().has(k)) return ruta + "." + k + ": not in the golden";
            }
        }
        return ruta + ": golden " + resumen(a) + " but the unit produced " + resumen(b);
    }

    private static String resumen(JsonElement e) {
        String s = e.toString();
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }

    static final String[] SECCIONES = { "excepcion", "salida", "salidaNula", "sentencias", "colaboradores", "tablasIniciales", "tablas" };

    /** the unit's dump must equal the golden in every section the golden has, and have no section the golden lacks */
    static void comparar(String id, JsonObject golden, JsonObject obtenido) {
        for (String sec : SECCIONES) {
            assertEquals(golden.has(sec), obtenido.has(sec), id + ": the section '" + sec + "' " + (golden.has(sec) ? "is in the golden but the unit did not produce it" : "was produced by the unit but is not in the golden")
                    + (obtenido.has("excepcion") ? " (the unit threw " + obtenido.get("excepcion").getAsString().split("\n")[0] + ")" : ""));
            if (!golden.has(sec)) continue;
            if (sec.equals("excepcion")) {
                assertEquals(excepcionComparable(golden.get(sec).getAsString()), excepcionComparable(obtenido.get(sec).getAsString()), id + ": exception");
                continue;
            }
            JsonElement g = canonico(golden.get(sec));
            JsonElement o = canonico(obtenido.get(sec));
            String d = primeraDiferencia(g, o, sec);
            assertTrue(d == null, id + ": " + d);
        }
    }

    static String pretty(JsonElement e) {
        return PRETTY.toJson(e);
    }
}
