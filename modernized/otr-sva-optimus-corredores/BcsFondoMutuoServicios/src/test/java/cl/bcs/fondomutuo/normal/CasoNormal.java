package cl.bcs.fondomutuo.normal;

import java.io.IOException;
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
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Harness of step 4A: for a case under {@code src/test/resources/fondomutuo-nor/cases/<id>/} (case.json, seed.sql and the expected.json RECORDED from the
 * unmodified legacy by legacy-oracle/record.sh, copied by sync4a.py) it builds the same H2 database (the schema and base seed of the oracle, in
 * {@code fondomutuo-3a/}, the case seed), runs the NEW Normal service through the JavaCall handler (plain Gson in, plain Gson out, as the controller) and returns
 * the same dump the oracle writes: output, exception, decision of {@code CallServlet}, tables, collaborator calls, sequences, session. A missing golden FAILS.
 */
public final class CasoNormal {

    public static final String RELOJ = "2026-03-02 14:30:00";
    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /**
     * Gates of the run: ABIERTAS = G3, G4 and G-CUS open (the legacy has no gates); G2 stays closed (Spot is F2 W2). F2 W1 lifted G1 (Tesoreria in process):
     * it is open in every profile except G1_CERRADA, which is ABIERTAS with {@code fondomutuo.tesoreria.habilitada=false} (the configured reversion).
     */
    public enum Perfil { ABIERTAS, CERRADAS, SOLO_G4_CERRADA, G1_CERRADA }

    /** Test hooks of Step 6 (residual risk of the lock DDL): off / null by default, so the goldens run exactly as recorded. */
    static volatile boolean commitEnDdl;
    static volatile java.util.function.Consumer<Connection> antesDelServicio;

    private CasoNormal() {
    }

    public static Path recursos() {
        try {
            return Path.of(CasoNormal.class.getClassLoader().getResource("fondomutuo-nor/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("fixture directory fondomutuo-nor missing (run legacy-oracle/sync4a.py)", e);
        }
    }

    public static List<String> casos() {
        List<String> ids = new ArrayList<>();
        try (var s = Files.list(recursos().resolve("cases"))) {
            s.sorted().forEach(d -> {
                if (Files.exists(d.resolve("case.json"))) ids.add(d.getFileName().toString());
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return ids;
    }

    public static JsonObject leer(Path p) {
        try {
            return JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static JsonObject golden(String id) {
        Path p = recursos().resolve("cases").resolve(id).resolve("expected.json");
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record.sh): " + p);
        return leer(p);
    }

    public static Compuertas compuertas(Perfil perfil) {
        boolean abiertas = perfil == Perfil.ABIERTAS || perfil == Perfil.G1_CERRADA;
        boolean g4 = abiertas;
        boolean g3YCustodia = perfil != Perfil.CERRADAS;
        boolean g1 = perfil != Perfil.G1_CERRADA;
        return new Compuertas("NORMAL", g1, SujetoNormal.spotF2 != null, g3YCustodia, g4, g3YCustodia);
    }

    /** Runs the case with the zone UTC and language en (the container settings; the goldens were recorded so) and H2 told to re-read the zone. */
    public static JsonObject ejecutar(String id, Perfil perfil) throws Exception {
        return ejecutar(id, perfil, null);
    }

    /** With {@code enProceso} the input is parsed, adjusted by the caller (literals: interned strings, D-NOR-4) and given to the service as an in-process caller would. */
    public static JsonObject ejecutar(String id, Perfil perfil, java.util.function.Consumer<NormalContrato.Entrada> enProceso) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(recursos().resolve("cases").resolve(id), perfil, enProceso);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static JsonObject correr(Path dir, Perfil perfil, java.util.function.Consumer<NormalContrato.Entrada> enProceso) throws Exception {
        Path base = recursos();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:nor" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : ESQUEMA) script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
            // F2 W1B: extra schema/seed of the layer-2 cases (Tesoreria objects), from fondomutuo-3a/ like the base; the aliases of Tesoreria's dialect bridge
            // name its family-A class, here the module's copy
            if (caso.has("esquemasExtra")) {
                for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) {
                    script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(e.getAsString()), StandardCharsets.UTF_8)
                            .replace("cl.bcs.tesoreria.familia_a.support.H2Hana", "cl.bcs.fondomutuo.tesoreria.H2Hana"));
                }
            }
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ;
            SesionHana sesion = new SesionHana(raw, url, reloj);
            sesion.commitEnDdl = commitEnDdl;
            Connection con = sesion.conexion();
            if (antesDelServicio != null) antesDelServicio.accept(raw);   // e.g. an uncommitted row, as the simulation pass of the Manejador leaves (D-NOR-6)
            JsonObject doubles = leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            boolean tesoreriaReal = caso.has("tesoreriaReal") && caso.get("tesoreriaReal").getAsBoolean();
            if (tesoreriaReal) sesion.tesoreria = guion;
            NormalPort normal = SujetoNormal.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), con, guion, secuencias, compuertas(perfil),
                    tesoreriaReal);
            NormalJavaCallHandler handler = new NormalJavaCallHandler(normal);

            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject out;
                if (enProceso == null) {
                    out = handler.call(con, caso.getAsJsonObject("entrada").deepCopy());
                } else {
                    NormalContrato.Entrada entrada = cl.bcs.fondomutuo.kit.JsonLegacy.plano().fromJson(caso.getAsJsonObject("entrada").toString(), NormalContrato.Entrada.class);
                    enProceso.accept(entrada);
                    out = JsonParser.parseString(cl.bcs.fondomutuo.kit.JsonLegacy.plano().toJson(normal.logica(entrada))).getAsJsonObject();
                }
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
                boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                raw.rollback();
                Throwable causa = t instanceof cl.bcs.fondomutuo.kit.AccesoDatosException && t.getCause() != null ? t.getCause() : t;
                decision = t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500";
                // the legacy SQL class rethrows what the driver throws as java.sql.SQLException; the H2 driver subclass is the same failure
                String clase = causa instanceof java.sql.SQLException ? "SQLException" : causa.getClass().getSimpleName();
                res.addProperty("excepcion", clase + ": " + causa.getMessage());
                if (t instanceof ServicioNoServidoException) res.addProperty("compuerta", ((ServicioNoServidoException) t).compuerta());
            }
            res.addProperty("decisionServlet", decision);
            JsonObject tablas = new JsonObject();
            if (caso.has("tablas")) {
                for (JsonElement el : caso.getAsJsonArray("tablas")) {
                    JsonObject t = el.getAsJsonObject();
                    tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
                }
            }
            res.add("tablas", tablas);
            if (antesDelServicio != null) {
                try (Statement st = raw.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM DUMMY WHERE DUMMY = 'Y'")) {
                    rs.next();
                    res.addProperty("filasDummyY", rs.getLong(1));
                }
            }
            res.add("colaboradores", guion.llamadas());
            for (JsonElement s : secuencias) s.getAsJsonObject().remove("tesoreria");   // marker of TesoreriaReal's recorder
            res.add("secuenciasSolicitadas", secuencias);
            JsonObject ses = new JsonObject();
            JsonArray locales = new JsonArray();
            sesion.tablasLocalesSinDrop().forEach(locales::add);
            ses.add("tablasLocalesSinDrop", locales);
            JsonArray au = new JsonArray();
            sesion.commitsAutonomos().forEach(au::add);
            ses.add("commitsAutonomos", au);
            res.add("sesion", ses);
            return res;
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

    static void script(Connection conn, String texto) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : texto.split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
