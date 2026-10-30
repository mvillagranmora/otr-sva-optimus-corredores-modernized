import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion;
import cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion.EntradaConsultarHistorialMovimientosCuentaInversion;
import cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion.SalidaConsultarHistorialMovimientosCuentaInversion;
import cl.bcs.canalagente.oraculo.ConexionRegistradora;
import cl.bcs.canalagente.oraculo.GuionColaboradores;
import cl.bcs.canalagente.oraculo.ProcedimientosBd;
import cl.bcs.canalagente.oraculo.VolcadoTablas;
import cl.bcs.oracle.Doubles;

/**
 * Oracle of cl.bcs.canalagente (ConsultarHistorialMovimientosCuentaInversion) and of the BD/Servicios procedure texts: executes the UNMODIFIED legacy class (real cl.bcs.utils.SQL /
 * NamedParameterStatement / JavaCall, real PlataformaUtils.MensajeSalida) on H2 MODE=LEGACY behind the recording connection {@link ConexionRegistradora}, with the one collaborator outside
 * the module (ConsultarCartolaDeMovimiento) answered by the case script. Dump per case: salida / excepcion, the ordered SQL sent (sentencias), the collaborator calls (colaboradores), the
 * tables the case names before and after (the session state when the call ends: the commit or rollback belongs to the caller and is not part of the module).
 * For the family PRC the "servicio" is a SELECT of a legacy procedure text (BD/Servicios/*.sql) executed on H2 against stand-in tables.
 * Usage: java OracleRunnerCanalAgente &lt;baseDir&gt; &lt;legacyBsclDir&gt; [caseNamePrefix]. Properties: oracle.cases (default &lt;baseDir&gt;/cases), oracle.repeat (N runs per case; an unstable case gets NO golden,
 * exit 4), oracle.report. Java 11 syntax on purpose. Nothing here touches HANA, a service or a credential.
 */
public class OracleRunnerCanalAgente {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static Path legacy;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        legacy = Path.of(args[1]);
        String prefix = args.length > 2 ? args[2] : "";
        Path casesRoot = System.getProperty("oracle.cases") != null ? Path.of(System.getProperty("oracle.cases")) : base.resolve("cases");
        List<Path> dirs;
        try (Stream<Path> s = Files.list(casesRoot)) {
            dirs = s.sorted().collect(Collectors.toList());
        }
        int n = 0;
        int stableRuns = 0;
        int unstableHard = 0;
        List<String> unstable = new ArrayList<>();
        for (Path dir : dirs) {
            if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix) || !Files.exists(dir.resolve("case.json"))) continue;
            int reps = Integer.getInteger("oracle.repeat", 1);
            JsonObject first = run(dir);
            String text = PRETTY.toJson(first) + "\n";
            if (reps > 1) {
                int differ = 0;
                String why = "";
                for (int r = 1; r < reps; r++) {
                    JsonObject again = run(dir);
                    if (!PRETTY.toJson(again).equals(PRETTY.toJson(first))) {
                        differ++;
                        if (why.isEmpty()) why = firstDifference(first, again);
                    }
                }
                if (differ > 0) {
                    String line = dir.getFileName() + " UNSTABLE: " + differ + " of " + (reps - 1) + " re-runs differ from run 1 (" + why + ")";
                    System.out.println("!! " + line);
                    unstable.add(line);
                    unstableHard++;
                    Files.deleteIfExists(dir.resolve("expected.json"));
                    continue;
                }
                stableRuns += reps;
            }
            Files.write(dir.resolve("expected.json"), text.getBytes(StandardCharsets.UTF_8));
            System.out.println("== " + dir.getFileName());
            n++;
        }
        System.out.println("cases executed: " + n + (stableRuns > 0 ? " (stable runs executed: " + stableRuns + ")" : ""));
        if (System.getProperty("oracle.report") != null) {
            List<String> rep = new ArrayList<>();
            rep.add("cases executed: " + n + ", repeats per case: " + Integer.getInteger("oracle.repeat", 1) + ", stable runs executed: " + stableRuns + ", unstable: " + unstable.size()
                    + ", hard: " + unstableHard);
            rep.addAll(unstable);
            Files.write(Path.of(System.getProperty("oracle.report")), (String.join("\n", rep) + "\n").getBytes(StandardCharsets.UTF_8));
        }
        if (n == 0) System.exit(2);
        if (unstableHard > 0) System.exit(4);
    }

    static String firstDifference(JsonObject a, JsonObject b) {
        for (String k : a.keySet()) {
            if (!PRETTY.toJson(a.get(k)).equals(PRETTY.toJson(b.get(k)))) return "section " + k + " differs";
        }
        return "keys differ";
    }

    static JsonObject read(Path p) throws Exception {
        return JsonParser.parseString(new String(Files.readAllBytes(p), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    static void runScript(Connection conn, Path file) throws Exception {
        List<String> clean = new ArrayList<>();
        for (String line : new String(Files.readAllBytes(file), StandardCharsets.UTF_8).split("\n")) {
            if (!line.strip().startsWith("--")) clean.add(line);
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : String.join("\n", clean).split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }

    static String clase(Throwable t) {
        return t.getClass().getSimpleName();
    }

    static String mensaje(Throwable t) {
        return String.valueOf(t.getMessage());
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = read(dir.resolve("case.json"));
        String servicio = caso.get("servicio").getAsString();
        String url = "jdbc:h2:mem:can" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        runScript(raw, base.resolve("schema-canalagente.sql"));
        if (caso.has("esquemasExtra")) for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) runScript(raw, base.resolve(e.getAsString()));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        ConexionRegistradora registro = new ConexionRegistradora(raw, caso.has("fallos") ? caso.getAsJsonArray("fallos") : null);
        Connection con = registro.conexion();
        Doubles.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());

        JsonObject res = new JsonObject();
        String que = servicio.startsWith("Procedimiento.") ? "the SELECT of the legacy procedure text BD/Servicios/" + caso.get("entrada").getAsJsonObject().get("procedimiento").getAsString()
                : "the legacy cl.bcs.canalagente.ConsultarHistorialMovimientosCuentaInversion." + servicio.split("\\.")[1] + " (real cl.bcs.utils.SQL / NamedParameterStatement / JavaCall, real PlataformaUtils.MensajeSalida; unmodified: no wall-clock edit needed)";
        res.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunnerCanalAgente.java executing " + que + " on H2 MODE=LEGACY, JVM zone UTC, behind the recording connection ConexionRegistradora;"
                + " the collaborator outside the module (ConsultarCartolaDeMovimiento) = registered double answered by the case colaboradores. Do not edit by hand; re-record.");
        JsonArray tablas = caso.has("tablas") ? caso.getAsJsonArray("tablas") : null;
        res.add("tablasIniciales", VolcadoTablas.volcar(raw, tablas));
        try {
            JsonElement salida = invocar(servicio, con, raw, caso.getAsJsonObject("entrada"));
            if (salida != null) res.add("salida", salida);
        } catch (Throwable t) {
            res.addProperty("excepcion", clase(t) + ": " + mensaje(t));
        }
        res.add("sentencias", registro.sentencias());
        res.add("colaboradores", Doubles.calls());
        res.add("tablas", VolcadoTablas.volcar(raw, tablas));
        try (Statement st = raw.createStatement()) {
            st.execute("SHUTDOWN");
        } catch (Exception e) {
            // the database is gone anyway
        }
        return res;
    }

    static String texto(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    static Long largo(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? Long.valueOf(o.get(k).getAsLong()) : null;
    }

    static JsonElement invocar(String servicio, Connection con, Connection raw, JsonObject e) throws Exception {
        switch (servicio) {
        case "ConsultarHistorial.logica": {
            EntradaConsultarHistorialMovimientosCuentaInversion in = new EntradaConsultarHistorialMovimientosCuentaInversion();
            in.p_IdEmpresa = largo(e, "p_IdEmpresa");
            in.p_IdUsuario = largo(e, "p_IdUsuario");
            in.p_IdModulo = texto(e, "p_IdModulo");
            in.p_IdPortafolio = largo(e, "p_IdPortafolio");
            in.p_FechaDesde = texto(e, "p_FechaDesde");
            in.p_FechaHasta = texto(e, "p_FechaHasta");
            in.p_IdMoneda = texto(e, "p_IdMoneda");
            in.p_IdMatrizAgrupador = largo(e, "p_IdMatrizAgrupador");
            in.p_EsADC = texto(e, "p_EsADC");
            SalidaConsultarHistorialMovimientosCuentaInversion out = new ConsultarHistorialMovimientosCuentaInversion().logica(con, in);
            return cl.bcs.utils.Utils.getGsonBuilder().toJsonTree(out);
        }
        case "ConsultarHistorial.call":
            return new ConsultarHistorialMovimientosCuentaInversion().call(con, e);
        case "Procedimiento.seleccionFinal": {
            String texto = new String(Files.readAllBytes(legacy.resolve("CanalAgente").resolve("BD").resolve("Servicios").resolve(e.get("procedimiento").getAsString())), StandardCharsets.UTF_8);
            return ProcedimientosBd.ejecutar(raw, texto, e.get("salida").getAsString(), e.get("variableTabla").getAsString(), e.get("tablaH2").getAsString(), e.get("orden").getAsString());
        }
        default:
            throw new IllegalArgumentException("unknown servicio " + servicio);
        }
    }
}
