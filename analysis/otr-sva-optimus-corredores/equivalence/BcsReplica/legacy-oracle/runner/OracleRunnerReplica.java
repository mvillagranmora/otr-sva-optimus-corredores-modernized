import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import cl.bcs.oracle.Clock;
import cl.bcs.oracle.Doubles;
import cl.bcs.replica.EnviarTablaReplica;
import cl.bcs.replica.IniciarReplica;
import cl.bcs.replica.IniciarReplicaDelta;
import cl.bcs.replica.IniciarReplicaFull;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.replica.oraculo.ConexionRegistradora;
import cl.bcs.replica.oraculo.GuionColaboradores;
import cl.bcs.replica.oraculo.VolcadoTablas;

/**
 * Oracle of cl.bcs.replica (EnviarTablaReplica, ReplicaUtils, IniciarReplicaFull, IniciarReplicaDelta): executes the UNMODIFIED legacy classes (the only edit of the compiled copies is the
 * wall clock, see record.sh) on H2 MODE=LEGACY behind the recording connection {@link ConexionRegistradora}, with the collaborators outside the module answered by the case script.
 * Dump per case: salida / excepcion, the ordered SQL sent (sentencias), the collaborator calls (colaboradores), the tables the case names before and after (the session state when the
 * call ends: the commit or rollback belongs to the caller and is not part of the module).
 * Usage: java OracleRunnerReplica &lt;baseDir&gt; [caseNamePrefix]. Properties: oracle.cases (default &lt;baseDir&gt;/cases), oracle.repeat (N runs per case; an unstable case gets NO golden, exit 4),
 * oracle.report. Java 11 syntax on purpose. Nothing here touches HANA, a service or a credential.
 */
public class OracleRunnerReplica {
    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static final Gson NULOS = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    static final Gson SIN_NULOS = new GsonBuilder().disableHtmlEscaping().create();
    static final String RELOJ = "2026-03-02 14:30:00";
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
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
        cl.bcs.utils.Fecha.setLocalOffSet(0);
        JsonObject caso = read(dir.resolve("case.json"));
        String reloj = caso.has("reloj") ? caso.get("reloj").getAsString() : RELOJ;
        Clock.fijar(reloj);
        String url = "jdbc:h2:mem:rep" + (++dbCounter) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        Connection raw = DriverManager.getConnection(url, "sa", "");
        runScript(raw, base.resolve("schema-replica.sql"));
        if (caso.has("esquemasExtra")) for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) runScript(raw, base.resolve(e.getAsString()));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(raw, dir.resolve("seed.sql"));
        raw.commit();
        raw.setAutoCommit(false);
        ConexionRegistradora registro = new ConexionRegistradora(raw, caso.has("fallos") ? caso.getAsJsonArray("fallos") : null, reloj);
        Connection con = registro.conexion();
        Doubles.reset(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());

        JsonObject res = new JsonObject();
        res.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunnerReplica.java executing the legacy cl.bcs.replica." + caso.get("servicio").getAsString().split("\\.")[0]
                + " (real cl.bcs.utils.SQL / NamedParameterStatement / JavaCall / Fecha; the only edit of the compiled copies is the wall clock) on H2 MODE=LEGACY, JVM zone UTC, fixed clock " + reloj
                + ", behind the recording connection ConexionRegistradora; every collaborator outside the module = registered double answered by the case colaboradores. Do not edit by hand; re-record.");
        JsonArray tablas = caso.has("tablas") ? caso.getAsJsonArray("tablas") : null;
        res.add("tablasIniciales", VolcadoTablas.volcar(raw, tablas));
        try {
            JsonElement salida = invocar(caso.get("servicio").getAsString(), con, caso.getAsJsonObject("entrada"));
            if (salida != null) res.add("salida", GuionColaboradores.sinNulos(salida));
            else if (caso.get("servicio").getAsString().endsWith(".call")) res.addProperty("salidaNula", true);
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

    static Object valorJson(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) {
            BigDecimal b = p.getAsBigDecimal();
            return b.scale() <= 0 ? (Object) Long.valueOf(b.longValueExact()) : (Object) Double.valueOf(b.doubleValue());
        }
        return p.getAsString();
    }

    static String texto(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    static Long largo(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? Long.valueOf(o.get(k).getAsLong()) : null;
    }

    static JsonElement invocar(String servicio, Connection con, JsonObject e) throws Exception {
        switch (servicio) {
        case "EnviarTablaReplica.logica": {
            EnviarTablaReplica.Entrada_EnviarTablaReplica in = new EnviarTablaReplica.Entrada_EnviarTablaReplica();
            in.p_IdEmpresa = largo(e, "p_IdEmpresa");
            in.p_Proceso = texto(e, "p_Proceso");
            if (e.has("p_Parametros")) {
                if (e.get("p_Parametros").isJsonNull()) {
                    in.p_Parametros = null;
                } else {
                    in.p_Parametros = new HashMap<String, Object>();
                    for (Map.Entry<String, JsonElement> m : e.getAsJsonObject("p_Parametros").entrySet()) in.p_Parametros.put(m.getKey(), valorJson(m.getValue()));
                }
            }
            return NULOS.toJsonTree(new EnviarTablaReplica().logica(con, in));
        }
        case "EnviarTablaReplica.call": {
            JsonObject out = new EnviarTablaReplica().call(con, e);
            return out;
        }
        case "EnviarTablaReplica.respaldarDataDiaInhabil": {
            List<EnviarTablaReplica.Workstack> l = new EnviarTablaReplica().respaldarDataDiaInhabil(con, largo(e, "idEmpresa"), texto(e, "fechaOrigen"), texto(e, "tablaWorkstack"), texto(e, "parametrosWorkstack"));
            return SIN_NULOS.toJsonTree(l);
        }
        case "ReplicaUtils.procesarRespaldoSebra":
            new ReplicaUtils().procesarRespaldoSebra(con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.procesarRespaldoOptimus":
            new ReplicaUtils().procesarRespaldoOptimus(con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.procesarRespaldoDiarioCarterasSebra":
            new ReplicaUtils().procesarRespaldoDiarioCarterasSebra(con, texto(e, "fechaSistemaEjecucion"), largo(e, "idEmpresa"));
            return null;
        case "ReplicaUtils.guardarEnvioPostPauta":
            new ReplicaUtils().guardarEnvioPostPauta(con, texto(e, "tabla"), texto(e, "fechasistema"), texto(e, "fechaenvio"));
            return null;
        case "ReplicaUtils.call":
            return new ReplicaUtils().call(con, e);
        case "IniciarReplicaFull.logica":
            return NULOS.toJsonTree(new IniciarReplicaFull().logica(con, entradaIniciar(e)));
        case "IniciarReplicaDelta.logica":
            return NULOS.toJsonTree(new IniciarReplicaDelta().logica(con, entradaIniciar(e)));
        case "IniciarReplicaFull.call":
            return new IniciarReplicaFull().call(con, e);
        case "IniciarReplicaDelta.call":
            return new IniciarReplicaDelta().call(con, e);
        default:
            throw new IllegalArgumentException("unknown servicio " + servicio);
        }
    }

    static IniciarReplica.EntradaIniciarReplica entradaIniciar(JsonObject e) {
        IniciarReplica.EntradaIniciarReplica in = new IniciarReplica.EntradaIniciarReplica();
        in.p_IdEmpresa = largo(e, "p_IdEmpresa");
        in.p_IdUsuario = largo(e, "p_IdUsuario");
        return in;
    }
}
