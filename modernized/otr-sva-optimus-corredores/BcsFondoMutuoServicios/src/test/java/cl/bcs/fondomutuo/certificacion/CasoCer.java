package cl.bcs.fondomutuo.certificacion;

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
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.normal.SesionHana;

/**
 * Harness of F3 W3 T1 (CertificarSolicitudAporteRescate, fronts F-A and F-B): for a case under {@code src/test/resources/fondomutuo-cer/cases/<id>/} (case.json, seed.sql and the
 * expected.json RECORDED from the unmodified legacy by legacy-oracle/record_cer.sh, copied by sync_cer.py) it builds the same H2 (F1 schema and base seed of {@code fondomutuo-3a/},
 * {@code schema-cer.sql} and {@code seed/base-cer.sql} of {@code fondomutuo-cer/}, the case seed), runs the NEW facade {@link CertificarAporteRescatePort} with the REAL JDBC adapters
 * and returns what the oracle writes, in the same shape, PLUS the context that the facade handed to the next phases.
 *
 * <p>THE SEAM (see API_ASSUMED.md 0): the facade runs F-A and F-B for real and, when it gets to the aportes / rescates, calls the port
 * {@link CertificarAporteRescateFasesPosteriores}. Here that port is a double that RECORDS the {@link CertificarAporteRescateContexto} it receives and stops the run with
 * {@link ParadaT1}. A case whose legacy outcome is TERMINAL (it ends, or throws, inside F-A / F-B) is compared as a whole; a case that CONTINUES is compared up to the seam
 * ({@link CerCostura}). The mode of a case is written by the generator in its {@code ref} ({@code T1-costura:completo} / {@code T1-costura:parada}).
 * A missing golden FAILS (never skips). Nothing here touches HANA or the network: H2 in memory only.
 */
public final class CasoCer {

    public static final String RELOJ = "2026-03-02 14:30:00";
    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /**
     * Cases whose legacy outcome is a NullPointerException of {@code ConfiguracionSistema} (a company with NO configuration rows at all, or a null company). The kit port
     * ({@code ConfiguracionSistemaJdbc}) raises {@code ParametroNoEncontradoException} (or its own failure) instead (KNOWN KIT DEVIATION DESVIO-KIT-1, kept by decision of the user): for
     * these slugs, and ONLY for them, either outcome passes.
     */
    static final Set<String> DESVIO_KIT_EMPRESA_SIN_CONFIGURACION = Set.of("empresa-sin-configuracion", "empresa-inexistente", "contrato-empresa-nula");

    public enum Modo {
        TERMINAL, COMPLETO, PARADA
    }

    /** What one run of the new code leaves: the dump in the oracle's format and the context handed to the seam (null when the run ended before it). */
    public static final class Corrida {
        public final JsonObject volcado;
        public final CertificarAporteRescateContexto contexto;
        public final Throwable excepcion;
        /** COPIES of the solicitudes of the context with {@link CertificarAporteRescateReglas#formatearMontos} applied (FM-CER-23): what the legacy wrote and sent after its own formatting. */
        public final java.util.Map<Long, CertificarAporteRescateContrato.SolicitudProceso> formateadas;

        Corrida(JsonObject volcado, CertificarAporteRescateContexto contexto, Throwable excepcion, java.util.Map<Long, CertificarAporteRescateContrato.SolicitudProceso> formateadas) {
            this.volcado = volcado;
            this.contexto = contexto;
            this.excepcion = excepcion;
            this.formateadas = formateadas;
        }
    }

    /** A copy of every solicitud of the context, formatted by the rule under test (the context itself is left as the seam received it). */
    private static java.util.Map<Long, CertificarAporteRescateContrato.SolicitudProceso> formateadas(CertificarAporteRescateContexto ctx, ConexionUnidadDeTrabajo uow) throws Exception {
        com.google.gson.Gson gson = cl.bcs.fondomutuo.kit.JsonLegacy.conNulos();
        List<CertificarAporteRescateContrato.SolicitudProceso> copias = new ArrayList<>();
        for (CertificarAporteRescateContrato.SolicitudProceso s : ctx.solicitudes) {
            copias.add(gson.fromJson(gson.toJson(s), CertificarAporteRescateContrato.SolicitudProceso.class));
        }
        CertificarAporteRescateReglas.formatearMontos(SujetoCer.formato(uow), copias);
        java.util.Map<Long, CertificarAporteRescateContrato.SolicitudProceso> mapa = new java.util.LinkedHashMap<>();
        for (CertificarAporteRescateContrato.SolicitudProceso s : copias) mapa.put(s.IdSolicitudAporteRescate, s);
        return mapa;
    }

    private CasoCer() {
    }

    public static Path recursos() {
        try {
            return Path.of(CasoCer.class.getClassLoader().getResource("fondomutuo-cer/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("fixture directory fondomutuo-cer missing (run legacy-oracle/sync_cer.py)", e);
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
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_cer.sh): " + p);
        return leer(p);
    }

    public static JsonObject caso(String id) {
        return leer(recursos().resolve("cases").resolve(id).resolve("case.json"));
    }

    /** The first id of the cases whose directory is {@code FMM-CER-<nnn>_...} (the `-parada` variant has the same number). */
    public static String id(int nnn) {
        String prefijo = String.format("FMM-CER-%03d_", nnn);
        return casos().stream().filter(c -> c.startsWith(prefijo)).findFirst().orElseThrow(() -> new AssertionError("no case " + prefijo + "* under fondomutuo-cer/cases"));
    }

    /** The id of the case with the given slug (the part after {@code FMM-CER-nnn_}; the `-parada` variant is a different slug). */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> c.substring(c.indexOf('_') + 1).equals(slug)).findFirst().orElseThrow(() -> new AssertionError("no case with slug " + slug));
    }

    public static String slug(String id) {
        return id.substring(id.indexOf('_') + 1);
    }

    /** The slug of the case WITHOUT the {@code -parada} suffix (the two variants of a case share it). */
    public static String slugBase(String id) {
        String s = slug(id);
        return s.endsWith("-parada") ? s.substring(0, s.length() - "-parada".length()) : s;
    }

    public static Modo modo(String id) {
        JsonArray ref = caso(id).getAsJsonArray("ref");
        for (JsonElement e : ref) {
            if (e.getAsString().equals("T1-costura:parada")) return Modo.PARADA;
            if (e.getAsString().equals("T1-costura:completo")) return Modo.COMPLETO;
        }
        return Modo.TERMINAL;
    }

    public static List<String> refs(String id) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : caso(id).getAsJsonArray("ref")) out.add(e.getAsString());
        return out;
    }

    public static Corrida ejecutar(String id) throws Exception {
        return ejecutar(recursos().resolve("cases").resolve(id));
    }

    /** Runs a case directory with the zone UTC and language en, as the goldens were recorded. */
    public static Corrida ejecutar(Path dir) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(dir);
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

    static String clase(Throwable t) {
        Throwable causa = t instanceof AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        if (causa instanceof ParadaT1) return "Exception";
        // a missing parameter is the legacy's plain Exception("Parametro X en modulo Y no encontrado"); the module's kit raises its own subclass with the same text
        return causa instanceof java.sql.SQLException ? "SQLException"
                : causa instanceof ParametroNoEncontradoException || causa.getClass() == RuntimeException.class ? "Exception" : causa.getClass().getSimpleName();
    }

    static String texto(Throwable t) {
        Throwable causa = t instanceof AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        return clase(t) + ": " + causa.getMessage();
    }

    private static Corrida correr(Path dir) throws Exception {
        Path base = recursos();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cer" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            esquema(raw, caso);
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            JsonObject benignos = leer(base.resolve("seed/doubles-cer.json"));
            for (String k : benignos.keySet()) doubles.add(k, benignos.get(k));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            CertificarAporteRescateContexto[] visto = new CertificarAporteRescateContexto[1];
            CertificarAporteRescateFasesPosteriores posteriores = contexto -> {
                visto[0] = contexto;
                throw new ParadaT1();
            };
            ConexionUnidadDeTrabajo uow = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));
            CertificarAporteRescatePort servicio = SujetoCer.crear(uow, con, guion, secuencias, posteriores);
            JsonObject res = new JsonObject();
            JsonObject inicial = tablas(raw, caso);

            JsonArray previas = new JsonArray();
            if (caso.has("entradasPrevias")) {
                for (JsonElement ep : caso.getAsJsonArray("entradasPrevias")) {
                    JsonObject p = new JsonObject();
                    try {
                        JsonObject out = llamar(servicio, ep.getAsJsonObject().deepCopy());
                        p.addProperty("salidaCruda", out.toString());
                        boolean commit = confirma(out);
                        p.addProperty("decisionServlet", commit ? "COMMIT" : "ROLLBACK");
                        if (commit) raw.commit(); else raw.rollback();
                    } catch (Throwable t) {
                        raw.rollback();
                        p.addProperty("decisionServlet", "ROLLBACK_HTTP500");
                        p.addProperty("excepcion", texto(t));
                    }
                    previas.add(p);
                }
            }

            String decision;
            JsonObject trasLlamada;
            Throwable fallo = null;
            try {
                JsonObject out = llamar(servicio, caso.getAsJsonObject("entrada").deepCopy());
                res.add("salida", out);
                res.addProperty("salidaCruda", out.toString());
                trasLlamada = tablas(raw, caso);
                boolean commit = confirma(out);
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                fallo = t;
                trasLlamada = tablas(raw, caso);
                raw.rollback();
                decision = "ROLLBACK_HTTP500";
                res.addProperty("excepcion", texto(t));
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
            return new Corrida(res, visto[0], fallo, visto[0] == null ? null : formateadas(visto[0], uow));
        }
    }

    /** The JavaCall entry as the host sees it: decode with the plain Gson, run, encode with the plain Gson (what the legacy {@code call(Connection, JsonObject)} does). */
    static JsonObject llamar(CertificarAporteRescatePort servicio, JsonObject in) throws Exception {
        CertificarAporteRescateContrato.Entrada entrada = CertificarAporteRescateJson.gson().fromJson(in.toString(), CertificarAporteRescateContrato.Entrada.class);
        CertificarAporteRescateContrato.Salida salida = servicio.logica(entrada);
        return JsonParser.parseString(CertificarAporteRescateJson.gson().toJson(salida)).getAsJsonObject();
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
