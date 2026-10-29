package cl.bcs.fondomutuo.valorcuota;

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
import java.util.Set;
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
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;

/**
 * Harness of F3 W2 (CargarValorCuota, the only class of the wave and the only UI class that WRITES): for a case under
 * {@code src/test/resources/fondomutuo-cvc/cases/<id>/} (case.json, seed.sql and the expected.json RECORDED from the unmodified legacy by legacy-oracle/record_cvc.sh, copied by
 * sync_cvc.py) it builds the same H2 (F1 schema and base seed of {@code fondomutuo-3a/}, {@code schema-cvc.sql} and {@code seed/base-cvc.sql} of {@code fondomutuo-cvc/}, the case
 * seed), runs the NEW code through its JavaCall entry ({@link CargarValorCuotaJavaCallHandler}) and returns the dump the oracle writes:
 * <ul>
 * <li>{@code tablasIniciales}, {@code tablasTrasLaLlamada} (what the class left in the session BEFORE the host decides) and {@code tablas} (after the decision of
 *     {@code CallServlet.java:441-456,495}: counter missing or > 0 = rollback, 0 = commit, exception = rollback);</li>
 * <li>{@code colaboradores} (the AgregarLog calls, answered by {@link Guion}), {@code secuenciasSolicitadas}, {@code previas} (the requests of {@code entradasPrevias}, executed
 *     first in the same session, each with its own decision) and {@code sesion}.</li>
 * </ul>
 * A missing golden FAILS (never skips). Nothing here touches HANA or the network: H2 in memory only.
 */
public final class CasoCvc {

    public static final String RELOJ = "2026-03-02 14:30:00";
    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /**
     * Cases whose legacy outcome is a NullPointerException of {@code ConfiguracionSistema} (a company with NO configuration rows at all). The kit port
     * ({@code ConfiguracionSistemaJdbc}) raises {@code ParametroNoEncontradoException} instead (KNOWN KIT DEVIATION DESVIO-KIT-1, kept by decision of the user): for these slugs,
     * and ONLY for them, either class passes.
     */
    static final Set<String> DESVIO_KIT_EMPRESA_SIN_CONFIGURACION = Set.of("empresa-sin-configuracion");

    /** ABIERTA: the class is listed in {@code fondomutuo.solicitud.listas-servidas}. CERRADA: the shipped default (empty list). */
    public enum Perfil { ABIERTA, CERRADA }

    private CasoCvc() {
    }

    public static Path recursos() {
        try {
            return Path.of(CasoCvc.class.getClassLoader().getResource("fondomutuo-cvc/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("fixture directory fondomutuo-cvc missing (run legacy-oracle/sync_cvc.py)", e);
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

    /** The recorded golden of the case; a case without golden FAILS the test (an unrecorded or unstable case must never pass by omission). */
    public static JsonObject golden(String id) {
        Path p = recursos().resolve("cases").resolve(id).resolve("expected.json");
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_cvc.sh): " + p);
        return leer(p);
    }

    public static JsonObject caso(String id) {
        return leer(recursos().resolve("cases").resolve(id).resolve("case.json"));
    }

    /** The id of the case whose directory is {@code FMM-CVC-<nnn>_...}. */
    public static String id(int nnn) {
        String prefijo = String.format("FMM-CVC-%03d_", nnn);
        return casos().stream().filter(c -> c.startsWith(prefijo)).findFirst().orElseThrow(() -> new AssertionError("no case " + prefijo + "* under fondomutuo-cvc/cases"));
    }

    /** The id of the case with the given slug (the part after {@code FMM-CVC-nnn_}). */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> c.substring(c.indexOf('_') + 1).equals(slug)).findFirst()
                .orElseThrow(() -> new AssertionError("no case with slug " + slug));
    }

    public static String slug(String id) {
        return id.substring(id.indexOf('_') + 1);
    }

    static Compuertas compuertas(Perfil perfil) {
        return new Compuertas(perfil == Perfil.ABIERTA ? CargarValorCuotaConfiguracion.LISTA : "", false, false, false, false, false);
    }

    public static JsonObject ejecutar(String id, Perfil perfil) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id), perfil);
    }

    /** Runs a case directory with the zone UTC and language en, as the goldens were recorded. */
    public static JsonObject ejecutar(Path dir, Perfil perfil) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(dir, perfil);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static void esquema(Connection raw, JsonObject caso) throws Exception {
        Path base = recursos();
        for (String f : ESQUEMA) script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) script(raw, Files.readString(base.resolve(e.getAsString()), StandardCharsets.UTF_8));
    }

    private static JsonObject tablas(Connection raw, JsonObject caso) throws Exception {
        JsonObject tablas = new JsonObject();
        for (JsonElement el : caso.getAsJsonArray("tablas")) {
            JsonObject t = el.getAsJsonObject();
            tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
        }
        return tablas;
    }

    private static boolean confirma(JsonObject out) {
        JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
        return cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
    }

    private static String clase(Throwable t) {
        Throwable causa = t instanceof cl.bcs.fondomutuo.kit.AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        // a missing parameter is the legacy's plain Exception("Parametro X en modulo Y no encontrado"); the module's kit raises its own subclass with the same text
        return causa instanceof java.sql.SQLException ? "SQLException"
                : causa instanceof ParametroNoEncontradoException || causa.getClass() == RuntimeException.class ? "Exception" : causa.getClass().getSimpleName();
    }

    private static String texto(Throwable t) {
        Throwable causa = t instanceof cl.bcs.fondomutuo.kit.AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        return clase(t) + ": " + causa.getMessage();
    }

    private static JsonObject correr(Path dir, Perfil perfil) throws Exception {
        Path base = recursos();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cvc" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            esquema(raw, caso);
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            Compuertas compuertas = compuertas(perfil);
            CargarValorCuotaPort servicio = SujetoCvc.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), con, guion, secuencias);
            CargarValorCuotaJavaCallHandler handler = new CargarValorCuotaJavaCallHandler(servicio, compuertas);
            JsonObject res = new JsonObject();
            JsonObject inicial = tablas(raw, caso);

            JsonArray previas = new JsonArray();
            if (caso.has("entradasPrevias")) {
                for (JsonElement ep : caso.getAsJsonArray("entradasPrevias")) {
                    JsonObject p = new JsonObject();
                    try {
                        JsonObject out = handler.call(con, ep.getAsJsonObject().deepCopy());
                        p.addProperty("salidaCruda", out.toString());
                        boolean commit = confirma(out);
                        p.addProperty("decisionServlet", commit ? "COMMIT" : "ROLLBACK");
                        if (commit) raw.commit(); else raw.rollback();
                    } catch (Throwable t) {
                        raw.rollback();
                        p.addProperty("decisionServlet", t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500");
                        p.addProperty("excepcion", texto(t));
                    }
                    previas.add(p);
                }
            }

            String decision;
            JsonObject trasLlamada;
            try {
                JsonObject out = handler.call(con, caso.getAsJsonObject("entrada").deepCopy());
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                trasLlamada = tablas(raw, caso);
                boolean commit = confirma(out);
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                trasLlamada = tablas(raw, caso);
                raw.rollback();
                decision = t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500";
                res.addProperty("excepcion", texto(t));
                if (t instanceof ServicioNoServidoException) res.addProperty("compuerta", ((ServicioNoServidoException) t).compuerta());
            }
            res.addProperty("decisionServlet", decision);
            res.add("tablasIniciales", inicial);
            res.add("tablasTrasLaLlamada", trasLlamada);
            res.add("tablas", tablas(raw, caso));
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", secuencias);
            res.add("previas", previas);
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
