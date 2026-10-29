package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioConfiguracion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioJavaCallHandler;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;

/**
 * Harness of F3 W1 front 9, part B (THE CHAIN): for a case under {@code src/test/resources/fondomutuo-cpfchain/cases-cpfchain/<id>/} (case.json, seed.sql and the expected.json RECORDED from the
 * unmodified legacy by record_cpfchain.sh, copied by sync_cpfchain.py) it builds ONE H2 with the tables of CPF AND the custody tables (F1 schema and base seed of {@code fondomutuo-3a/}, then
 * {@code schema-cpf.sql} + {@code seed/base-cpf.sql} (byte copies of the CPF front), {@code schema-cpfchain.sql} + {@code seed/base-cpfchain.sql}, then the case seed), runs the NEW CPF through its JavaCall
 * entry ({@link ConsultarCustodiaPortafolioJavaCallHandler}) WIRED to the NEW FFMM (the real {@link ConsultarCustodiaPortafolioFfmmAdaptador}, no double of the custody calculation) and returns the dump
 * the oracle writes. A missing golden FAILS (never skips).
 *
 * <p>The legacy oracle ran CPF and FFMM REAL and unmodified (plus {@code CalcularMontoNumeroCuota} and {@code ObtenerDiaSiguiente}); the collaborators outside the module were registered doubles
 * scripted per case: {@code CalcularParidad}, {@code EntregarPreciosTasas} and {@code ConsultarEmpresa} (called by FFMM first and by CPF later). Here {@link Guion} plays the same script behind the
 * same ports of the module (one Guion for both classes: the order of the recorded calls is part of the comparison).
 */
public final class CasoCadena {

    public static final String RELOJ = "2026-03-02 14:30:00";
    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    /** Same KNOWN KIT DEVIATION as CasoCpf / CasoFfmm (DESVIO-KIT-1); no chain case uses these slugs today, the set exists so that a future case does not fail for the known deviation only. */
    static final Set<String> DESVIO_KIT_EMPRESA_SIN_CONFIGURACION = Set.of("empresa-ausente", "empresa-nula", "empresa-sin-configuracion");

    private CasoCadena() {
    }

    public static Path recursos() {
        try {
            return Path.of(CasoCadena.class.getClassLoader().getResource("fondomutuo-cpfchain/cases-cpfchain").toURI()).getParent();
        } catch (Exception e) {
            throw new IllegalStateException("fixture directory fondomutuo-cpfchain missing (run legacy-oracle/sync_cpfchain.py)", e);
        }
    }

    public static Path raizCasos() {
        return recursos().resolve("cases-cpfchain");
    }

    public static List<String> casos() {
        List<String> ids = new ArrayList<>();
        try (var s = Files.list(raizCasos())) {
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
        Path p = raizCasos().resolve(id).resolve("expected.json");
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_cpfchain.sh): " + p);
        return leer(p);
    }

    public static JsonObject caso(String id) {
        return leer(raizCasos().resolve(id).resolve("case.json"));
    }

    public static String seed(String id) {
        try {
            return Files.readString(raizCasos().resolve(id).resolve("seed.sql"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The id of the case with the given slug (the part after {@code FM-CHAIN-nnn_}). */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> c.substring(c.indexOf('_') + 1).equals(slug)).findFirst()
                .orElseThrow(() -> new AssertionError("no chain case with slug " + slug));
    }

    public static String slug(String id) {
        return id.substring(id.indexOf('_') + 1);
    }

    static Compuertas compuertas() {
        return new Compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA, false, false, false, false, false);
    }

    public static JsonObject ejecutar(String id) throws Exception {
        return ejecutar(raizCasos().resolve(id));
    }

    /** Runs a case directory with the zone UTC and language en, as the goldens were recorded. */
    public static JsonObject ejecutar(Path dir) throws Exception {
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
        for (String f : ESQUEMA) CasoFfmm.script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) CasoFfmm.script(raw, Files.readString(base.resolve(e.getAsString()), StandardCharsets.UTF_8));
    }

    private static JsonObject correr(Path dir) throws Exception {
        Path base = recursos();
        JsonObject caso = leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cadena" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            esquema(raw, caso);
            if (Files.exists(dir.resolve("seed.sql"))) CasoFfmm.script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            Guion guion = new Guion(doubles);
            ConsultarCustodiaPortafolioPort servicio = SujetoCadena.crear(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true)), guion);
            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject entrada = caso.getAsJsonObject("entrada").deepCopy();
                JsonObject out = new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas()).call(con, entrada);
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
                String clase = causa instanceof java.sql.SQLException ? "SQLException"
                        : causa instanceof ParametroNoEncontradoException || causa.getClass() == RuntimeException.class ? "Exception" : causa.getClass().getSimpleName();
                res.addProperty("excepcion", clase + ": " + causa.getMessage());
                if (t instanceof ServicioNoServidoException) res.addProperty("compuerta", ((ServicioNoServidoException) t).compuerta());
            }
            res.addProperty("decisionServlet", decision);
            JsonObject tablas = new JsonObject();
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), CasoFfmm.volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
            }
            res.add("tablas", tablas);
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", new JsonArray());
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
}
