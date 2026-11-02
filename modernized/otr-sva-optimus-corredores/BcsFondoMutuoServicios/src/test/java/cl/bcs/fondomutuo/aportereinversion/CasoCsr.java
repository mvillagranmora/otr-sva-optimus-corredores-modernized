package cl.bcs.fondomutuo.aportereinversion;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

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
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;

/**
 * Harness of F3 W3 T4-A: for a case under {@code src/test/resources/fondomutuo-csr/cases/<id>/} (case.json, seed.sql and the expected.json RECORDED from the unmodified legacy
 * {@code CrearSolicitudAporteReinversion} by legacy-oracle/record_csr.sh, copied by sync_csr.py; a golden over 1 MB is stored as expected.json.gz) it builds the same H2 database (the
 * schema and base seed of the oracle in {@code fondomutuo-3a/}, then {@code schema-csr.sql} and {@code seed/base-csr.sql} of {@code fondomutuo-csr/} through "esquemasExtra", then the
 * case seed), runs the NEW service through {@link ApiCsr} (plain Gson in, plain Gson out, as the legacy {@code call}) and returns the same dump the oracle writes: output, exception,
 * decision of {@code CallServlet}, tables, collaborator calls, sequences, session. A missing golden FAILS. The class has no lock (no DDL): the session section must stay empty.
 */
public final class CasoCsr {

    public static final String RELOJ = "2026-03-02 14:30:00";
    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /**
     * Gates of the run (they only add a 503 BEFORE the first write; the legacy has none): ABIERTAS = G1 (Tesoreria), G2 (Spot), G3 (SaldoFlujoCaja) and G-CUS (custody) open;
     * the others close exactly one; CERRADAS = the four shut (the shipped defaults).
     */
    public enum Perfil { ABIERTAS, G1_CERRADA, G2_CERRADA, G3_CERRADA, CUSTODIA_CERRADA, CERRADAS }

    private CasoCsr() {
    }

    public static Path recursos() {
        try {
            return Path.of(CasoCsr.class.getClassLoader().getResource("fondomutuo-csr/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("fixture directory fondomutuo-csr missing (run legacy-oracle/sync_csr.py)", e);
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

    public static JsonObject caso(String id) {
        return leer(recursos().resolve("cases").resolve(id).resolve("case.json"));
    }

    /** The "ref" of the case: the rule cards and defects it pins (its display name in the reports). */
    public static String ref(String id) {
        JsonObject c = caso(id);
        return c.has("ref") ? c.get("ref").getAsString() : "";
    }

    public static boolean tieneGolden(String id) {
        Path d = recursos().resolve("cases").resolve(id);
        return Files.exists(d.resolve("expected.json")) || Files.exists(d.resolve("expected.json.gz"));
    }

    public static JsonObject golden(String id) {
        Path d = recursos().resolve("cases").resolve(id);
        try {
            if (Files.exists(d.resolve("expected.json"))) return leer(d.resolve("expected.json"));
            if (Files.exists(d.resolve("expected.json.gz"))) {
                try (var in = new GZIPInputStream(Files.newInputStream(d.resolve("expected.json.gz")))) {
                    return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        throw new AssertionError("golden missing (record it with legacy-oracle/record_csr.sh): " + d.resolve("expected.json"));
    }

    public static Compuertas compuertas(Perfil perfil) {
        boolean g1 = perfil == Perfil.ABIERTAS || perfil == Perfil.G2_CERRADA || perfil == Perfil.G3_CERRADA || perfil == Perfil.CUSTODIA_CERRADA;
        boolean g2 = perfil == Perfil.ABIERTAS || perfil == Perfil.G1_CERRADA || perfil == Perfil.G3_CERRADA || perfil == Perfil.CUSTODIA_CERRADA;
        boolean g3 = perfil == Perfil.ABIERTAS || perfil == Perfil.G1_CERRADA || perfil == Perfil.G2_CERRADA || perfil == Perfil.CUSTODIA_CERRADA;
        boolean custodia = perfil == Perfil.ABIERTAS || perfil == Perfil.G1_CERRADA || perfil == Perfil.G2_CERRADA || perfil == Perfil.G3_CERRADA;
        return new Compuertas("", g1, g2, g3, false, custodia);
    }

    public static JsonObject ejecutar(String id, Perfil perfil) throws Exception {
        // -Dcsr.autocomprobacion=true: the rule tests read the RECORDED golden instead of running the production class (only for the PERFIL ABIERTAS runs of a recorded case): the proof that the
        // assertions of the tests describe what the legacy did, before any production code exists. Off by default; the equivalence tests never use it.
        if (Boolean.getBoolean("csr.autocomprobacion") && perfil == Perfil.ABIERTAS) return golden(id);
        return ejecutarReal(id, perfil);
    }

    /** Always runs the production class (the equivalence, determinism and gate tests use this one, never the golden-backed self-check of {@link #ejecutar(String, Perfil)}). */
    public static JsonObject ejecutarReal(String id, Perfil perfil) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id), perfil, null, null);
    }

    /** With {@code enProceso} the input is parsed into the TYPED production class, adjusted by the caller and given to the port as an in-process caller (Certificar) would. */
    public static JsonObject ejecutar(String id, Perfil perfil, Consumer<Object> enProceso) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id), perfil, enProceso, null);
    }

    /** The same, replacing the case input (an {@code entrada} object) by the given one: a variation of a recorded case, never compared with a golden. */
    public static JsonObject ejecutarConEntrada(String id, Perfil perfil, JsonObject entrada) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id), perfil, null, entrada);
    }

    /** Runs a case directory with the zone UTC and language en (the container settings; the goldens were recorded so) and H2 told to re-read the zone. */
    public static JsonObject ejecutar(Path dir, Perfil perfil, Consumer<Object> enProceso, JsonObject entradaAlterna) throws Exception {
        return ejecutar(dir, perfil, enProceso, entradaAlterna, false);
    }

    /** T4-C: the case goes through the in-process JSON adapter of Certificar ({@code AporteReinversionJsonAdapter}: JSON in, JSON out) instead of {@code ApiCsr.llamar}; the real port behind it is the same. */
    public static JsonObject ejecutarPorAdaptador(String id, Perfil perfil) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id), perfil, null, null, true);
    }

    private static JsonObject ejecutar(Path dir, Perfil perfil, Consumer<Object> enProceso, JsonObject entradaAlterna, boolean porAdaptador) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(dir, perfil, enProceso, entradaAlterna, porAdaptador);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static JsonObject correr(Path dir, Perfil perfil, Consumer<Object> enProceso, JsonObject entradaAlterna, boolean porAdaptador) throws Exception {
        Path base = recursos();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:csr" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = cl.bcs.fondomutuo.BdEfimeraH2.abrir(url)) {
            for (String f : ESQUEMA) script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
            if (caso.has("esquemasExtra")) {
                for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) script(raw, Files.readString(base.resolve(e.getAsString()), StandardCharsets.UTF_8));
            }
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ;
            SesionHana sesion = new SesionHana(raw, url, reloj);
            Connection con = sesion.conexion();
            JsonObject doubles = leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            Object puerto = SujetoCsr.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), con, guion, secuencias, compuertas(perfil));
            JsonObject entradaJson = (entradaAlterna != null ? entradaAlterna : caso.getAsJsonObject("entrada")).deepCopy();

            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject out;
                if (porAdaptador) {
                    out = cl.bcs.fondomutuo.certificacion.ApiT4c.llamarPorAdaptador(puerto, entradaJson);
                } else if (enProceso == null) {
                    out = ApiCsr.llamar(puerto, entradaJson);
                } else {
                    Object entrada = ApiCsr.entrada(entradaJson);
                    enProceso.accept(entrada);
                    out = JsonParser.parseString(JsonLegacy.plano().toJson(ApiCsr.logica(puerto, entrada))).getAsJsonObject();
                }
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
                boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                raw.rollback();
                if (t instanceof AssertionError && t.getMessage() != null && (t.getMessage().startsWith("CSR") || t.getMessage().startsWith("T4-C"))) throw (AssertionError) t;   // "not implemented" is never an observation
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
            res.add("colaboradores", guion.llamadas());
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
