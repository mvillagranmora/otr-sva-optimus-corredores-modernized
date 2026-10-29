import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.oracle.Doubles;
import cl.bcs.oracle.SessionConnection;

/**
 * Focused oracle driver of step 3A (plan section 9): the PURE closure classes CalcularFechaSolicitudAporteRescate, CalcularMontoNumeroCuota,
 * CalcularComisiones and ObtenerPerfilFondoMutuo of cl.bcs.fondomutuo, executed UNMODIFIED (their logica(con, entrada) methods, exactly what
 * the legacy callers invoke) on H2 with the infrastructure of OracleRunner (schema, base seed, SessionConnection, registered doubles, static reset).
 * Run it through record3a.sh (record.sh compiles the real sources; ORACLE_MAIN selects this class). Java 11 syntax on purpose.
 *
 * Usage: java Oracle3ARunner baseDir [caseNamePrefix]    baseDir = equivalence/BcsFondoMutuoServicios ; cases in baseDir/cases3a/
 * oracle.out (system property): directory for dumps instead of goldens (mutation check of the goldens with LEGACY_OVERLAY).
 * Case: case.json {clase, ref, entrada, colaboradores?, reloj?} + seed.sql. Golden: expected.json, never edited by hand.
 * The dump is the Salida of logica() as JSON (dates yyyy-MM-dd, nulls written, BigDecimal as text: the scale counts), the post-run entrada
 * (CalcularComisiones mutates it), the exception (class and message) and every call to a registered double.
 */
public class Oracle3ARunner {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static final Gson OUT = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeNulls().disableHtmlEscaping().create();
    static final String CLOCK = "2026-03-02 14:30:00";
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        Path base = Path.of(args[0]);
        OracleRunner.base = base;
        String prefix = args.length > 1 ? args[1] : "";
        String outDir = System.getProperty("oracle.out");
        List<Path> dirs;
        try (Stream<Path> s = Files.list(base.resolve("cases3a"))) {
            dirs = s.sorted().collect(Collectors.toList());
        }
        int n = 0;
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            String text = PRETTY.toJson(run(base, dir)) + "\n";
            if (outDir != null) {
                Files.createDirectories(Path.of(outDir));
                Files.write(Path.of(outDir).resolve(dir.getFileName() + ".json"), text.getBytes(StandardCharsets.UTF_8));
            } else {
                Files.write(dir.resolve("expected.json"), text.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("== " + dir.getFileName());
            n++;
        }
        System.out.println("cases executed (3A legacy): " + n);
        if (n == 0) System.exit(2);
    }

    static JsonObject run(Path base, Path dir) throws Exception {
        OracleRunner.resetStatics();
        // ConfiguracionSistema keeps its parameters in a JVM-wide static map that is only added to (a parameter deleted later would still be "found"):
        // one case = one request from a fresh JVM, so it is emptied (limpiarParametros is the legacy's own reset)
        cl.bcs.plataforma.pla.utils.ConfiguracionSistema.limpiarParametros();
        // MonedaFormato keeps its table in a JVM-wide static (loaded once): every case starts from a fresh JVM state
        java.lang.reflect.Field f = Class.forName("cl.bcs.plataforma.pla.utils.MonedaFormato").getDeclaredField("formatos");
        f.setAccessible(true);
        f.set(null, null);
        JsonObject caso = OracleRunner.read(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:fmm3a" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        for (String s : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" }) {
            OracleRunner.runScript(raw, base.resolve(s));
        }
        if (Files.exists(dir.resolve("seed.sql"))) OracleRunner.runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        String clock = caso.has("reloj") ? caso.get("reloj").getAsString() : CLOCK;
        Connection con = SessionConnection.of(raw, url, clock).connection();
        JsonObject doubles = OracleRunner.read(base.resolve("seed/doubles-base.json"));
        if (caso.has("colaboradores")) {
            for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
        }
        Doubles.reset(doubles);

        String clase = caso.get("clase").getAsString();
        JsonObject res = new JsonObject();
        res.addProperty("_provenance", "Recorded by legacy-oracle/runner3a/Oracle3ARunner.java executing the unmodified legacy cl.bcs.fondomutuo." + clase
                + ".logica on H2 MODE=LEGACY, JVM zone UTC, fixed clock " + clock + ", collaborators outside the closure answered by registered doubles. Do not edit by hand; re-record.");
        Gson plano = new Gson();
        String entradaJson = caso.getAsJsonObject("entrada").toString();
        Object entrada = null;
        try {
            Object salida;
            switch (clase) {
                case "CalcularFechaSolicitudAporteRescate": {
                    cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate e =
                            plano.fromJson(entradaJson, cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate.class);
                    entrada = e;
                    salida = new cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate().logica(con, e);
                    break;
                }
                case "CalcularMontoNumeroCuota": {
                    cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota e =
                            plano.fromJson(entradaJson, cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota.class);
                    entrada = e;
                    salida = new cl.bcs.fondomutuo.CalcularMontoNumeroCuota().logica(con, e);
                    break;
                }
                case "CalcularComisiones": {
                    cl.bcs.fondomutuo.CalcularComisiones.EntradaCalcularComisiones e =
                            plano.fromJson(entradaJson, cl.bcs.fondomutuo.CalcularComisiones.EntradaCalcularComisiones.class);
                    entrada = e;
                    salida = new cl.bcs.fondomutuo.CalcularComisiones().logica(con, e);
                    break;
                }
                case "ObtenerPerfilFondoMutuo": {
                    cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo e =
                            plano.fromJson(entradaJson, cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo.class);
                    entrada = e;
                    salida = new cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo().logica(con, e);
                    break;
                }
                case "MonedaFormato": {
                    JsonObject e = caso.getAsJsonObject("entrada");
                    java.math.BigDecimal valor = e.get("valor").isJsonNull() ? null : e.get("valor").getAsBigDecimal();
                    String moneda = e.get("moneda").isJsonNull() ? null : e.get("moneda").getAsString();
                    String tipoInstrumento = e.has("idTipoInstrumento") ? e.get("idTipoInstrumento").getAsString() : "";
                    String tipoOperacion = e.has("idTipoOperacion") ? e.get("idTipoOperacion").getAsString() : "";
                    cl.bcs.plataforma.pla.utils.MonedaFormato.Resultado r = cl.bcs.plataforma.pla.utils.MonedaFormato.formatear(con, valor,
                            e.get("tipo").getAsString(), moneda, tipoInstrumento, tipoOperacion);
                    JsonObject o = new JsonObject();
                    o.addProperty("valor", r.valor);
                    salida = o;
                    break;
                }
                case "ObtenerDiaSiguiente": {
                    cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente e =
                            plano.fromJson(entradaJson, cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.class);
                    entrada = e;
                    salida = new cl.bcs.plataforma.pla.ObtenerDiaSiguiente().logica(con, e);
                    break;
                }
                case "ObtenerDiaAnterior": {
                    cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior e =
                            plano.fromJson(entradaJson, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.class);
                    entrada = e;
                    salida = new cl.bcs.plataforma.pla.ObtenerDiaAnterior().logica(con, e);
                    break;
                }
                case "ObtenerDiaHabilMultiplesPaises": {
                    cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises e =
                            plano.fromJson(entradaJson, cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises.class);
                    entrada = e;
                    salida = new cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises().logica(con, e);
                    break;
                }
                default:
                    throw new IllegalArgumentException("unknown clase " + clase);
            }
            res.add("salida", OUT.toJsonTree(salida));
        } catch (Throwable t) {
            raw.rollback();
            res.addProperty("excepcionClase", t.getClass().getSimpleName());
            res.addProperty("excepcionMensaje", String.valueOf(t.getMessage()));
        }
        if (entrada != null) res.add("entradaTrasEjecucion", OUT.toJsonTree(entrada));
        res.add("colaboradores", Doubles.calls());
        raw.rollback();
        raw.close();
        return res;
    }
}
