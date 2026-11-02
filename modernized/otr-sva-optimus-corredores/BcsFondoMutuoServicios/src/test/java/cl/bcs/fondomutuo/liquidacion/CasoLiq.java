package cl.bcs.fondomutuo.liquidacion;

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

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.BdEfimeraH2;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.certificacion.CrearCargoAbonoPort;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida;

/**
 * Harness of F3 W3 T6-B (LiquidacionSolicitudAporteRescate). For a case under {@code src/test/resources/fondomutuo-liq/cases/<id>/} (case.json, seed.sql and the expected.json RECORDED from the unmodified
 * legacy by legacy-oracle/record_liq.sh, copied by sync_liq.py) it builds the same H2 as the oracle (the F1 schema and base seed of {@code fondomutuo-3a/}, {@code schema-liq.sql} and {@code seed/base-liq.sql} of
 * {@code fondomutuo-liq/}, the case seed), builds the PRODUCTION facade of Liquidacion ({@link ApiT6b#fachada}) over its REAL JDBC adapter ({@link ApiT6b#datosJdbc}), the REAL adapters of the parameters and the
 * dictionary, the sequences of H2, and FAKES at the port seam for the three collaborators outside the module (Tesoreria {@code CrearIngresoEgreso}, CuentaInversion {@code CrearCargoAbono}, Plataforma
 * {@code AgregarLog}) answered by the scripted collaborators of the case ({@link Guion}, the same semantics as the oracle's registered doubles). The WHOLE request goes through the production JavaCall handler
 * ({@link ApiT6b#llamarPorHandler}): JSON in, JSON out, with the recount of the errors of the legacy {@code call}.
 *
 * <p>What is observed, as the oracle observes it: the output, the table of the solicitudes before / after the call / after the host decides, EVERY call to the three collaborators in order with its
 * COMPLETE arguments and its answer, the sequences asked, the host decision. The host decision is emulated as the servlet does (CallServlet:441-456,495): counter missing or &gt; 0 => rollback, 0 => commit, an
 * exception => rollback. The H2 dialect bridge for the HANA UPDATE-FROM is {@link ConexionLiq}.
 *
 * <p>A missing golden FAILS (never skips); a missing production class FAILS with "T6-B not implemented". {@code -Dliq.autocomprobacion=true} makes {@link #ejecutar(String)} return the golden itself (no
 * production code is run): the proof that the assertions of the rule tests describe what the legacy did. Nothing here touches HANA or the network: H2 in memory only.
 */
public final class CasoLiq {

    public static final String RELOJ = "2026-03-02 14:30:00";
    public static final String SOL = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
    public static final String TESORERIA = "CrearIngresoEgreso";
    public static final String CUENTA_INVERSION = "CrearCargoAbono";
    public static final String LOG = "AgregarLog";

    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /** What one run leaves: {@code volcado} has the SAME shape as a golden of the oracle. */
    public static final class Corrida {
        public final JsonObject volcado;
        public final JsonObject salida;
        public final String excepcionTexto;
        public final Throwable excepcion;
        public final boolean delGolden;

        Corrida(JsonObject volcado, JsonObject salida, String excepcionTexto, Throwable excepcion, boolean delGolden) {
            this.volcado = volcado;
            this.salida = salida;
            this.excepcionTexto = excepcionTexto;
            this.excepcion = excepcion;
            this.delGolden = delGolden;
        }

        public boolean termino() {
            return excepcionTexto == null;
        }
    }

    private CasoLiq() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the cases
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** {@code src/test/resources/fondomutuo-liq}: the cases, the stand-in schema and the default data */
    public static Path recursosLiq() {
        try {
            return Path.of(CasoLiq.class.getClassLoader().getResource("fondomutuo-liq/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new AssertionError("fixture directory fondomutuo-liq missing (run legacy-oracle/sync_liq.py)", e);
        }
    }

    static Path recursos3a() {
        try {
            return Path.of(CasoLiq.class.getClassLoader().getResource("fondomutuo-3a/schema-types.sql").toURI()).getParent();
        } catch (Exception e) {
            throw new AssertionError("fixture directory fondomutuo-3a missing", e);
        }
    }

    static Path recursosCer() {
        try {
            return Path.of(CasoLiq.class.getClassLoader().getResource("fondomutuo-cer/seed/doubles-cer.json").toURI()).getParent().getParent();
        } catch (Exception e) {
            throw new AssertionError("fixture directory fondomutuo-cer missing", e);
        }
    }

    public static Path directorio() {
        return recursosLiq().resolve("cases");
    }

    public static List<String> casos() {
        List<String> ids = new ArrayList<>();
        Path dir = directorio();
        if (!Files.isDirectory(dir)) throw new AssertionError("fixture directory fondomutuo-liq/cases missing (run legacy-oracle/sync_liq.py)");
        try (var s = Files.list(dir)) {
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
        return leer(directorio().resolve(id).resolve("case.json"));
    }

    /** The recorded golden of the case; a case without golden FAILS the test (an unrecorded or unstable case must never pass by omission). */
    public static JsonObject golden(String id) {
        Path p = directorio().resolve(id).resolve("expected.json");
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_liq.sh): " + p);
        return leer(p);
    }

    /** The id of the case {@code FMM-LIQ-<nnn>_...} (the numbering is deterministic: make_cases_liq.py). */
    public static String id(int nnn) {
        String prefijo = String.format("FMM-LIQ-%03d_", nnn);
        return casos().stream().filter(c -> c.startsWith(prefijo)).findFirst().orElseThrow(() -> new AssertionError("no case " + prefijo + "* under fondomutuo-liq/cases"));
    }

    /** The id of the case with the given slug (the part after {@code FMM-LIQ-nnn_}); the rule tests name their cases by slug so that a renumbering never misleads them. */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> c.substring(c.indexOf('_') + 1).equals(slug)).findFirst().orElseThrow(() -> new AssertionError("no T6-B case with slug " + slug));
    }

    public static List<String> refs(String id) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : caso(id).getAsJsonArray("ref")) out.add(e.getAsString());
        return out;
    }

    public static boolean conTag(String id, String tag) {
        return refs(id).contains(tag);
    }

    /** The cases that carry a tag such as {@code LIQ-quirk:log-sin-asignar} (in the order of the directory). */
    public static List<String> conTag(String tag) {
        return casos().stream().filter(c -> conTag(c, tag)).toList();
    }

    public static boolean esExcepcion(String id) {
        return conTag(id, "LIQ-modo:excepcion");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // reading what a run left (the same accessors for the production run and for the golden)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static List<JsonObject> llamadas(Corrida c, String colaborador) {
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            JsonObject o = e.getAsJsonObject();
            if (colaborador.equals(o.get("colaborador").getAsString())) out.add(o);
        }
        return out;
    }

    /** the entry of the call number {@code n} (0 based) to the collaborator */
    public static JsonObject entradaDe(Corrida c, String colaborador, int n) {
        List<JsonObject> l = llamadas(c, colaborador);
        if (l.size() <= n) throw new AssertionError("the class made only " + l.size() + " call(s) to " + colaborador + ", not " + (n + 1));
        return l.get(n).getAsJsonArray("argumentos").get(0).getAsJsonObject();
    }

    /** {@code p_IngresoEgresoGenerar} of the (only) call to Tesoreria: the lines in the order the class built them */
    public static JsonArray lineasTesoreria(Corrida c) {
        return entradaDe(c, TESORERIA, 0).getAsJsonArray("p_IngresoEgresoGenerar");
    }

    public static JsonArray formasDePago(Corrida c) {
        return entradaDe(c, TESORERIA, 0).getAsJsonArray("p_FormaDePago");
    }

    /** the lines {@code p_CargoAbonoAGenerar} of the call number {@code n} to CuentaInversion */
    public static JsonArray cargosAbonos(Corrida c, int n) {
        return entradaDe(c, CUENTA_INVERSION, n).getAsJsonArray("p_CargoAbonoAGenerar");
    }

    public static JsonArray resultados(Corrida c) {
        return c.salida.getAsJsonArray("P_OUT_RESULTADO");
    }

    public static JsonArray mensajes(Corrida c) {
        return c.salida.getAsJsonArray("P_OUT_MENSAJES");
    }

    public static long contador(Corrida c) {
        return c.salida.get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    public static String decision(Corrida c) {
        return c.volcado.get("decisionServlet").getAsString();
    }

    /** the row of the table of the solicitudes AS THE CLASS LEFT IT (before the host decides) */
    public static JsonObject fila(Corrida c, long idSolicitud) {
        for (JsonElement e : c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(SOL)) {
            if (e.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong() == idSolicitud) return e.getAsJsonObject();
        }
        throw new AssertionError("no row " + idSolicitud + " in the table of the solicitudes");
    }

    /** the number of the folio of the carta the class wrote in the row (0 = never written) */
    public static long folio(Corrida c, long idSolicitud) {
        return fila(c, idSolicitud).get("FolioCartaLiquidacion").getAsLong();
    }

    public static String estadoLiquidacion(Corrida c, long idSolicitud) {
        return fila(c, idSolicitud).get("EstadoLiquidacion").getAsString();
    }

    /** the requests to the sequences, as {@code secuencia=cantidad} in order */
    public static List<String> secuencias(Corrida c) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : c.volcado.getAsJsonArray("secuenciasSolicitadas")) {
            JsonObject o = e.getAsJsonObject();
            out.add(o.get("secuencia").getAsString().replaceAll(".*::", "") + "=" + o.get("cantidad").getAsLong());
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // running
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** The run the rule tests use: the production facade, or the golden itself with {@code -Dliq.autocomprobacion=true}. */
    public static Corrida ejecutar(String id) throws Exception {
        if (Boolean.getBoolean("liq.autocomprobacion")) return desdeGolden(id);
        return ejecutarReal(id);
    }

    /** ALWAYS the production facade (the equivalence and determinism tests use this one, never the self-check of {@link #ejecutar(String)}). */
    public static Corrida ejecutarReal(String id) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(directorio().resolve(id));
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    static Corrida desdeGolden(String id) {
        JsonObject g = golden(id).deepCopy();
        JsonObject salida = g.has("salida") ? g.getAsJsonObject("salida") : null;
        return new Corrida(g, salida, g.has("excepcion") ? g.get("excepcion").getAsString() : null, null, true);
    }

    static void script(Connection conn, String texto) throws Exception {
        List<String> limpio = new ArrayList<>();
        for (String line : texto.split("\n")) {
            if (!line.strip().startsWith("--")) limpio.add(line.replaceAll(";\\s+--[^']*$", ";"));
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", limpio).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }

    private static void esquema(Connection raw, JsonObject caso) throws Exception {
        for (String f : ESQUEMA) script(raw, Files.readString(recursos3a().resolve(f), StandardCharsets.UTF_8));
        for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) script(raw, Files.readString(recursosLiq().resolve(e.getAsString()), StandardCharsets.UTF_8));
    }

    private static JsonObject tablas(Connection raw, JsonObject caso) throws Exception {
        JsonObject tablas = new JsonObject();
        for (JsonElement el : caso.getAsJsonArray("tablas")) {
            JsonObject t = el.getAsJsonObject();
            tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString(), t.has("columnas") ? t.getAsJsonArray("columnas") : null));
        }
        return tablas;
    }

    /** Same row rendering as the oracle: numbers normalised, dates as ISO text, NULL as JSON null; the columns the case names (all when it names none). */
    private static JsonArray volcar(Connection conn, String table, String orderBy, JsonArray columnas) throws Exception {
        String lista = "*";
        if (columnas != null) {
            List<String> cs = new ArrayList<>();
            for (JsonElement c : columnas) cs.add("\"" + c.getAsString() + "\"");
            lista = String.join(", ", cs);
        }
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT " + lista + " FROM \"" + table + "\" ORDER BY " + orderBy)) {
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

    static String clase(Throwable t) {
        Throwable causa = t instanceof AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        // a missing parameter is the legacy's plain Exception("Parametro X en modulo Y no encontrado"); the module's kit raises its own subclass with the same text; a failure scripted in a collaborator is a plain Exception
        return causa instanceof java.sql.SQLException ? "SQLException"
                : causa instanceof ParametroNoEncontradoException || causa.getClass() == RuntimeException.class ? "Exception" : causa.getClass().getSimpleName();
    }

    static String texto(Throwable t) {
        Throwable causa = t instanceof AccesoDatosException && t.getCause() != null ? t.getCause() : t;
        return clase(t) + ": " + causa.getMessage();
    }

    private static Corrida correr(Path dir) throws Exception {
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:liq" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = BdEfimeraH2.abrir(url)) {
            esquema(raw, caso);
            if (Files.exists(dir.resolve("seed.sql"))) script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ);
            Connection con = ConexionLiq.envolver(sesion.conexion());
            JsonObject doubles = leer(recursos3a().resolve("seed/doubles-base.json"));
            JsonObject benignos = leer(recursosCer().resolve("seed/doubles-cer.json"));
            for (String k : benignos.keySet()) doubles.add(k, benignos.get(k));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            ConexionUnidadDeTrabajo uow = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));

            // ---- the facade over the REAL adapters and the fakes of the three collaborators outside the module
            CrearIngresoEgresoPuerto tesoreria = entrada -> {
                JsonElement a = JsonLegacy.conNulos().toJsonTree(entrada);
                JsonObject resp = guion.responder(TESORERIA, a);
                return resp == null ? null : JsonLegacy.conNulos().fromJson(resp, CrearIngresoEgreso_Salida.class);
            };
            CrearCargoAbonoPort cuentaInversion = entrada -> guion.responder(CUENTA_INVERSION, entrada);
            LogPort log = entrada -> guion.responder(LOG, entrada);
            Object fachada = ApiT6b.fachada(ApiT6b.datosJdbc(uow), new ConfiguracionSistemaJdbc(uow), new DiccionarioJdbc(uow), new SecuenciaH2(con, secuencias), tesoreria, cuentaInversion, log);

            JsonObject res = new JsonObject();
            JsonObject inicial = tablas(raw, caso);
            Throwable fallo = null;
            JsonObject salida = null;
            try {
                salida = ApiT6b.llamarPorHandler(fachada, caso.getAsJsonObject("entrada").deepCopy());
            } catch (Throwable t) {
                if (t instanceof AssertionError && t.getMessage() != null && t.getMessage().startsWith("T6-B")) throw t;   // "not implemented" is never an observation of the legacy
                fallo = t;
            }
            JsonObject trasLlamada = tablas(raw, caso);
            String decision;
            if (fallo != null) {
                raw.rollback();
                decision = fallo instanceof ServicioNoServidoException ? "ROLLBACK_HTTP503" : "ROLLBACK_HTTP500";
                res.addProperty("excepcion", texto(fallo));
            } else {
                JsonElement cnt = salida.get("P_OUT_ERRORESCONTADOR");
                boolean confirma = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = confirma ? "COMMIT" : "ROLLBACK";
                if (confirma) raw.commit();
                else raw.rollback();
                res.add("salida", salida);
                res.addProperty("salidaCruda", salida.toString());
            }
            res.addProperty("decisionServlet", decision);
            res.add("tablasIniciales", inicial);
            res.add("tablasTrasLaLlamada", trasLlamada);
            res.add("tablas", tablas(raw, caso));
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", secuencias);
            res.add("previas", new JsonArray());
            JsonObject ses = new JsonObject();
            ses.add("tablasLocalesSinDrop", new JsonArray());
            ses.add("commitsAutonomos", new JsonArray());
            res.add("sesion", ses);
            return new Corrida(res, salida, fallo == null ? null : texto(fallo), fallo, false);
        }
    }
}
