import java.io.File;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.contabilidad.EnvioContabilidad;
import cl.bcs.contabilidad.GenerarJsonAsientoSap;
import cl.bcs.contabilidad.ReversaPreComprobante;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer;
import cl.bcs.contabilidad.jobs.InterfazContable;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Utils;

/**
 * Records golden expectations by executing the UNMODIFIED legacy classes
 * (EnvioContabilidad, GenerarJsonAsientoSap, ReversaPreComprobante, InterfazContable + CallerWSJournalVoucher)
 * against H2 (MODE=LEGACY) seeded with each case, a fake SAP HANA OACT table and the fake Service Layer.
 *
 * Usage: java OracleRunner <envioResourcesDir> [caseNamePrefix]
 * Writes <case>/expected.json. See ../README.md ("Recording a new case").
 * No real SAP, HANA or credential is touched: all connection values below are fake.
 */
public class OracleRunner {

    static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    static Path base;
    static int dbCounter = 0;

    public static void main(String[] args) throws Exception {
        base = Path.of(args[0]);
        String prefix = args.length > 1 ? args[1] : "";
        Path cases = base.resolve("cases");
        try (var dirs = Files.list(cases)) {
            for (Path dir : dirs.sorted().toList()) {
                if (!Files.isDirectory(dir) || !dir.getFileName().toString().startsWith(prefix)) continue;
                System.out.println("== " + dir.getFileName());
                JsonObject expected = run(dir);
                Files.writeString(dir.resolve("expected.json"), PRETTY.toJson(expected) + "\n", StandardCharsets.UTF_8);
            }
        }
    }

    static JsonObject run(Path dir) throws Exception {
        JsonObject caso = JsonParser.parseString(Files.readString(dir.resolve("case.json"))).getAsJsonObject();
        String servicio = caso.get("servicio").getAsString();
        int n = ++dbCounter;
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:oracle" + n + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        runScript(conn, base.resolve("schema-envio.sql"));
        runScript(conn, base.resolve("seed/base-envio.sql"));
        if (Files.exists(dir.resolve("seed.sql"))) runScript(conn, dir.resolve("seed.sql"));

        // configuration
        ConfiguracionSistema.VALORES.clear();
        JsonObject cfg = JsonParser.parseString(Files.readString(base.resolve("seed/config.json"))).getAsJsonObject();
        if (caso.has("config")) {
            for (var e : caso.getAsJsonObject("config").entrySet()) {
                if (e.getValue().isJsonNull()) cfg.remove(e.getKey()); else cfg.add(e.getKey(), e.getValue());
            }
        }
        for (var e : cfg.entrySet()) ConfiguracionSistema.VALORES.put(e.getKey(), e.getValue().getAsString());

        // fake SAP HANA (OACT)
        String sapUrl = "jdbc:h2:mem:sap" + n + ";DB_CLOSE_DELAY=-1";
        try (Connection sap = DriverManager.getConnection(sapUrl, "sa", ""); Statement st = sap.createStatement()) {
            st.execute("CREATE SCHEMA FAKECO");
            st.execute("CREATE TABLE FAKECO.OACT (\"GroupMask\" SMALLINT, \"LocManTran\" VARCHAR(1), \"AcctCode\" VARCHAR(50), \"ActType\" VARCHAR(1))");
            JsonArray plan = caso.has("planCuentas") ? caso.getAsJsonArray("planCuentas")
                    : JsonParser.parseString(Files.readString(base.resolve("seed/plan-cuentas.json"))).getAsJsonArray();
            for (JsonElement el : plan) {
                JsonObject c = el.getAsJsonObject();
                try (var ps = sap.prepareStatement("INSERT INTO FAKECO.OACT VALUES (?,?,?,?)")) {
                    ps.setShort(1, c.get("GroupMask").getAsShort());
                    ps.setString(2, c.get("LocManTran").getAsString());
                    ps.setString(3, c.get("AcctCode").getAsString());
                    ps.setString(4, c.get("ActType").getAsString());
                    ps.executeUpdate();
                }
            }
        }
        Utils.SAP_CONNECTION = () -> {
            try { return DriverManager.getConnection(sapUrl, "sa", ""); } catch (Exception e) { throw new RuntimeException(e); }
        };
        Utils.SECUENCIAS.clear();
        Utils.SECUENCIA_LLAMADAS.clear();
        Utils.SECUENCIAS.put("BSCL.Contabilidad.BD.Secuencias::IdPreComprobante", 5000L);
        Utils.SECUENCIAS.put("BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento", 9000L);

        // fake configuration files (all values are fake)
        Path work = Path.of(System.getProperty("user.dir"));
        Path cfgDir = work.resolve("cfg" + n);
        Files.createDirectories(cfgDir.resolve("jobm"));
        Files.createDirectories(work.resolve("out" + n).resolve("ITZ"));
        Utils.CONFIGURACION.clear();
        Utils.CONFIGURACION.put("PLA.RutaBaseArchivos", work.resolve("out" + n).toString() + File.separator);
        boolean sinArchivo = caso.has("sinArchivoConfiguracion") && caso.get("sinArchivoConfiguracion").getAsBoolean();
        Utils.CONFIGURACION.put("PLA.RutaArchivosConfiguracion",
                (sinArchivo ? work.resolve("no-existe" + n) : cfgDir).toString() + File.separator);

        JsonObject expected = new JsonObject();
        expected.addProperty("_provenance", "Recorded by legacy-oracle/runner/OracleRunner.java executing unmodified legacy "
                + servicio + " on H2 MODE=LEGACY. Do not edit by hand; re-record.");

        try (FakeSapServiceLayer sl = new FakeSapServiceLayer()) {
            String props = "HANA_SERVER=localhost\nHANA_PORT=30015\nB1_COMPANY=FAKECO\nHANA_USER=FAKE_HANA_USER\n"
                    + "HANA_PASS=fake-hana-pass-0000\nB1_SERVICE_LAYER_URL=" + sl.url() + "\nB1_USER=fake-b1-user\n"
                    + "B1_PASS=fake-b1-pass-0000\n";
            Files.writeString(cfgDir.resolve("jobm/interfazSAP.properties"), props);
            Files.writeString(work.resolve("interfazSAP.properties"), props);

            switch (servicio) {
                case "EnvioContabilidad" -> {
                    JsonObject out = callOrException(() -> new EnvioContabilidad().call(conn, caso.getAsJsonObject("entrada")), expected);
                    if (out != null) expected.add("salida", out);
                    expected.add("tablas", dumpEnvio(conn));
                }
                case "ReversaPreComprobante" -> {
                    JsonObject out = callOrException(() -> new ReversaPreComprobante().call(conn, caso.getAsJsonObject("entrada")), expected);
                    if (out != null) expected.add("salida", out);
                    JsonObject t = dumpEnvio(conn);
                    t.add("PreComprobante", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobante", Set.of(), "\"IdPreComprobante\""));
                    t.add("PreComprobanteAsiento", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento", Set.of(), "\"IdPreComprobanteAsiento\""));
                    t.add("EventoPreComprobanteAsiento", dump(conn, "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento", Set.of(), "\"IdPreComprobanteAsiento\", \"IdEvento\""));
                    expected.add("tablas", t);
                    expected.add("secuenciasSolicitadas", PRETTY.toJsonTree(Utils.SECUENCIA_LLAMADAS));
                }
                case "GenerarJsonAsientoSap" -> {
                    GenerarJsonAsientoSap.GenerarJsonAsientoSap_Entrada in = new Gson().fromJson(caso.get("entrada"),
                            GenerarJsonAsientoSap.GenerarJsonAsientoSap_Entrada.class);
                    var salida = new GenerarJsonAsientoSap().logica(conn, in);
                    Gson wire = new Gson(); // CallerWSJournalVoucher.java:168 / :234 serialise with new Gson()
                    expected.add("socios", JsonParser.parseString(wire.toJson(salida.P_OUT_SOCIOS)));
                    expected.add("vouchers", JsonParser.parseString(wire.toJson(salida.P_OUT_VOUCHERS)));
                    JsonArray raw = new JsonArray();
                    for (var v : salida.P_OUT_VOUCHERS) raw.add(wire.toJson(v));
                    expected.add("vouchersRaw", raw);
                    expected.addProperty("javaCallCallDevuelveNull", new GenerarJsonAsientoSap().call(conn, new JsonObject()) == null);
                }
                case "InterfazContable" -> {
                    JsonObject sap = caso.getAsJsonObject("sap");
                    for (var e : sap.entrySet()) {
                        List<FakeSapServiceLayer.Respuesta> rs = new ArrayList<>();
                        for (JsonElement r : e.getValue().getAsJsonArray()) {
                            JsonObject ro = r.getAsJsonObject();
                            List<String> ck = new ArrayList<>();
                            if (ro.has("setCookies")) ro.getAsJsonArray("setCookies").forEach(c -> ck.add(c.getAsString()));
                            rs.add(new FakeSapServiceLayer.Respuesta(ro.get("status").getAsInt(),
                                    ro.has("body") ? ro.get("body").getAsString() : null, ck));
                        }
                        sl.cuando(e.getKey(), rs.toArray(new FakeSapServiceLayer.Respuesta[0]));
                    }
                    List<Map<String, Object>> llamadas = new ArrayList<>();
                    int[] commits = {0};
                    Connection rec = recording(conn, llamadas, commits);
                    InterfazContable job = new InterfazContable();
                    JsonObject entrada = caso.getAsJsonObject("entrada");
                    String b64 = Base64.getEncoder().encodeToString(entrada.get("secuencias").getAsString().getBytes(StandardCharsets.UTF_8));
                    job.params.put("IdEmpresa", entrada.get("idEmpresa").getAsString());
                    job.params.put("IdUsuario", entrada.get("idUsuario").getAsString());
                    job.params.put("SecCmbte", b64);
                    job.params.put("Destino", entrada.get("destino").getAsString());
                    expected.addProperty("secCmbteBase64", b64);
                    boolean ok = job.transaction(rec);
                    expected.addProperty("resultado", ok);
                    JsonArray peticiones = new JsonArray();
                    for (var p : sl.peticiones()) {
                        JsonObject po = new JsonObject();
                        po.addProperty("metodo", p.metodo());
                        po.addProperty("ruta", p.ruta());
                        po.addProperty("cookie", p.cookie());
                        po.addProperty("cuerpo", p.cuerpo());
                        peticiones.add(po);
                    }
                    expected.add("peticiones", peticiones);
                    Path log = work.resolve("out" + n).resolve(job.rutaSalida);
                    String contenido = Files.exists(log) ? Files.readString(log, StandardCharsets.UTF_8) : null;
                    expected.addProperty("logContenido", contenido);
                    expected.add("procedimientos", PRETTY.toJsonTree(llamadas));
                    expected.addProperty("commits", commits[0]);
                    JsonObject t = new JsonObject();
                    t.add("PreComprobante", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobante", Set.of(), "\"IdPreComprobante\""));
                    expected.add("tablas", t);
                }
                default -> throw new IllegalArgumentException("servicio desconocido " + servicio);
            }
        }
        conn.close();
        return expected;
    }

    interface Call { JsonObject run() throws Exception; }

    static JsonObject callOrException(Call c, JsonObject expected) {
        try {
            return c.run();
        } catch (Exception e) {
            expected.addProperty("excepcion", e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    static Connection recording(Connection real, List<Map<String, Object>> llamadas, int[] commits) {
        return (Connection) Proxy.newProxyInstance(OracleRunner.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
            if (m.getName().equals("commit")) { commits[0]++; return null; }
            if (m.getName().equals("prepareCall")) {
                Map<String, Object> call = new LinkedHashMap<>();
                call.put("sql", a[0]);
                Map<String, Object> params = new TreeMap<>();
                List<Integer> outs = new ArrayList<>();
                call.put("parametros", params);
                call.put("salidaRegistrada", outs);
                call.put("ejecutado", false);
                llamadas.add(call);
                return Proxy.newProxyInstance(OracleRunner.class.getClassLoader(), new Class<?>[]{CallableStatement.class}, (p2, m2, a2) -> {
                    switch (m2.getName()) {
                        case "setLong", "setString", "setInt" -> params.put(String.valueOf(a2[0]), a2[1]);
                        case "registerOutParameter" -> outs.add((Integer) a2[0]);
                        case "execute" -> { call.put("ejecutado", true); return false; }
                        case "close" -> { return null; }
                        default -> { }
                    }
                    return null;
                });
            }
            try {
                return m.invoke(real, a);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw ite.getCause();
            }
        });
    }

    static JsonObject dumpEnvio(Connection conn) throws Exception {
        JsonObject t = new JsonObject();
        t.add("PreComprobanteEnvioContabilidad", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobanteEnvioContabilidad", Set.of(), "\"JdtNum\""));
        t.add("PreComprobanteAsientoEnvioContabilidad", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsientoEnvioContabilidad",
                Set.of("IdPreComprobanteAsientoEnvioContabilidad"), "\"IdPreComprobanteAsientoEnvioContabilidad\""));
        t.add("PreComprobanteCentralizacionTipo", dump(conn, "BSCL.Contabilidad.BD::ITZ.PreComprobanteCentralizacionTipo", Set.of(), "\"IdPreComprobante\""));
        return t;
    }

    static JsonArray dump(Connection conn, String table, Set<String> exclude, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    String col = md.getColumnLabel(i);
                    if (exclude.contains(col)) continue;
                    Object v = rs.getObject(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number num) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(num.toString()).stripTrailingZeros().toPlainString())));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    static void runScript(Connection conn, Path file) throws Exception {
        String sql = Files.readString(file, StandardCharsets.UTF_8);
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            String l = line.strip();
            if (l.startsWith("--")) continue;
            clean.append(line).append('\n');
        }
        try (Statement st = conn.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
