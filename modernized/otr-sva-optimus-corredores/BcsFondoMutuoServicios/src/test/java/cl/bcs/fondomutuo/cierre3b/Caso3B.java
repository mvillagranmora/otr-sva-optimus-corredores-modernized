package cl.bcs.fondomutuo.cierre3b;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.EmpresaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;

/**
 * Characterization harness of step 3B: for a case under {@code analysis/.../equivalence/BcsFondoMutuoServicios/cases3b/<id>/} (case.json, seed.sql and
 * the expected.json RECORDED from the unmodified legacy by legacy-oracle/record3b.sh) it builds the same H2 database (same schema scripts, base seed
 * and case seed), runs the NEW implementation through a {@link Sujeto} and compares: the JSON output (text, so BigDecimal scale counts), the exception
 * class, the dumped tables, the calls made to the collaborators (name, arguments, answer) and the sequences requested. A missing fixture FAILS.
 */
public final class Caso3B {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String RELOJ = "2026-03-02 14:30:00";

    /** What a service under test needs from the harness. */
    public static final class Contexto {
        public final ConexionUnidadDeTrabajo conexion;
        public final ConfiguracionSistemaJdbc configuracion;
        public final DiccionarioJdbc diccionario;
        public final EmpresaJdbc empresa;
        public final RevisarRequisitosBasicosJdbc revisar;
        public final CalendarioJdbc calendario;
        public final Guion guion;
        public final JsonArray secuencias = new JsonArray();
        public final Connection con;

        Contexto(Connection con, Guion guion) {
            this.con = con;
            this.guion = guion;
            this.conexion = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));
            this.configuracion = new ConfiguracionSistemaJdbc(conexion);
            this.diccionario = new DiccionarioJdbc(conexion);
            this.empresa = new EmpresaJdbc(conexion);
            this.revisar = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
            this.calendario = new CalendarioJdbc(conexion, configuracion);
        }
    }

    @FunctionalInterface
    public interface Sujeto {
        JsonObject llamar(Contexto ctx, JsonObject entrada) throws Exception;
    }

    private Caso3B() {
    }

    public static Path base() {
        String env = System.getenv("FM3B_BASE");   // only for a scratch copy of the module (the module directory is the default anchor)
        Path p = env != null ? Path.of(env)
                : Path.of("").toAbsolutePath().resolve("../../../analysis/otr-sva-optimus-corredores/equivalence/BcsFondoMutuoServicios").normalize();
        assertTrue(Files.isDirectory(p.resolve("cases3b")), "fixture directory missing: " + p.resolve("cases3b"));
        return p;
    }

    /** Ids of the cases of a service (directories whose case.json has that "servicio"), sorted. */
    public static List<String> casos(String servicio) {
        List<String> ids = new ArrayList<>();
        try (var s = Files.list(base().resolve("cases3b"))) {
            for (Path d : (Iterable<Path>) s.sorted()::iterator) {
                Path cj = d.resolve("case.json");
                if (Files.isDirectory(d) && Files.exists(cj) && leer(cj).get("servicio").getAsString().equals(servicio)) {
                    ids.add(d.getFileName().toString());
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        assertTrue(!ids.isEmpty(), "no case for " + servicio);
        return ids;
    }

    public static void verificar(String idCaso, Sujeto sujeto) throws Exception {
        Path dir = base().resolve("cases3b").resolve(idCaso);
        Path expectedPath = dir.resolve("expected.json");
        if (!Files.exists(expectedPath)) fail("golden missing (record it with legacy-oracle/record3b.sh): " + expectedPath);
        JsonObject expected = leer(expectedPath);
        // The goldens were recorded with the container settings (zone UTC, language en); the test must not depend on the host. The case runs with the
        // JVM zone UTC and language en, and H2 (which caches the JVM zone) is told to re-read it (org.h2.util.DateTimeUtils.resetCalendar), before and
        // after: dates of year 0001 stored by the legacy rules (sentinel dates) shift by a day in other zones.
        java.util.TimeZone zonaAnterior = java.util.TimeZone.getDefault();
        java.util.Locale idiomaAnterior = java.util.Locale.getDefault();
        JsonObject actual;
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            actual = ejecutar(dir, sujeto);
        } finally {
            java.util.TimeZone.setDefault(zonaAnterior);
            java.util.Locale.setDefault(idiomaAnterior);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
        for (String clave : new String[] { "salidaCruda", "resultado", "tablas", "colaboradores", "secuenciasSolicitadas" }) {
            assertEquals(texto(expected.get(clave)), texto(actual.get(clave)), idCaso + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), idCaso + " :: exception presence");
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), idCaso + " :: exception message");
            } else {
                assertEquals(ec, ac, idCaso + " :: exception class");
            }
        }
    }

    static JsonObject ejecutar(Path dir, Sujeto sujeto) throws Exception {
        Path base = base();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:n3b" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
                script(raw, base.resolve(f));
            }
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, dir.resolve("seed.sql"));
            raw.commit();
            raw.setAutoCommit(false);
            String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ;
            Connection con = new SesionH2(raw, reloj).conexion();

            JsonObject doubles = leer(base.resolve("seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            Contexto ctx = new Contexto(con, guion);

            JsonObject res = new JsonObject();
            try {
                JsonObject out = sujeto.llamar(ctx, caso.getAsJsonObject("entrada").deepCopy());
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                res.addProperty("resultado", "OK");
                raw.commit();
            } catch (Throwable t) {
                raw.rollback();
                res.addProperty("resultado", "EXCEPCION");
                res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + t.getMessage());
            }
            JsonObject tablas = new JsonObject();
            if (caso.has("tablas")) {
                for (JsonElement el : caso.getAsJsonArray("tablas")) {
                    JsonObject t = el.getAsJsonObject();
                    tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
                }
            }
            res.add("tablas", tablas);
            JsonArray colab = new JsonArray();
            for (JsonElement e : guion.llamadas()) {
                JsonObject o = e.getAsJsonObject();
                JsonObject n = new JsonObject();
                n.add("colaborador", o.get("colaborador"));
                n.add("argumentos", o.get("argumentos"));
                n.add("respuesta", o.get("respuesta"));
                colab.add(n);
            }
            res.add("colaboradores", colab);
            res.add("secuenciasSolicitadas", ctx.secuencias);
            return res;
        }
    }

    /** Expected side: keep only name, arguments and answer of each collaborator call (the legacy also records the Java method name). */
    private static String texto(JsonElement e) {
        if (e == null) return "<absent>";
        if (e.isJsonArray() && !e.getAsJsonArray().isEmpty() && e.getAsJsonArray().get(0).isJsonObject()
                && e.getAsJsonArray().get(0).getAsJsonObject().has("colaborador")) {
            JsonArray n = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) {
                JsonObject o = x.getAsJsonObject();
                JsonObject m = new JsonObject();
                m.add("colaborador", o.get("colaborador"));
                m.add("argumentos", o.get("argumentos"));
                m.add("respuesta", o.get("respuesta"));
                n.add(m);
            }
            return PRETTY.toJson(canon(n));
        }
        return PRETTY.toJson(canon(e));
    }

    /** Same content with the object keys sorted (the legacy writes fields in declaration order; the comparison is about content). */
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

    static JsonObject leer(Path p) {
        try {
            return JsonParser.parseString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JsonArray volcar(Connection conn, String table, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static void script(Connection conn, Path file) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
