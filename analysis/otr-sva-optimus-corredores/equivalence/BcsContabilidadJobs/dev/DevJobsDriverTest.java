package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.infrastructure.persistence.InterfazContableJdbcRepository;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Config;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Gateway;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.CasoEnvioLegacy;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer.Peticion;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer.Respuesta;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import cl.bcs.contabilidad.service.InterfazContableService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Transform driver (scratch copy, never in modernized/): development cases CONT-B-40..50. Runs the NEW InterfazContable job, wired exactly as
 * InterfazContableJobGoldenTest does, on every development case and writes four outputs per case; the legacy side is written
 * from the OracleRunner's expected.json in the same shape. No assertion: compare.py judges.
 * resultado.txt: outcome (true = COMMIT) and explicit commits; log.txt: the run-log bytes; estados.txt: every
 * PreComprobante Estado; sap.txt: the SAP request sequence (method + route; cookies excluded, approved B1SESSION
 * deviation); cuerpos.txt: every SAP request body, canonical (review H1); procedimientos.txt: the state-procedure
 * calls (review M1). RutaSalida is not compared: its timestamp varies (legacy wall clock vs fixed clock).
 */
class DevJobsDriverTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.InterfazContable";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2025-04-01T13:15:30Z"), ZoneId.of("America/Santiago"));

    @Test
    void escribirSalidas() throws Exception {
        Path out = Path.of(System.getProperty("dev.out"));
        Path casos = CasoEnvioLegacy.raiz().resolve("cases");
        List<String> nombres;
        try (var s = Files.list(casos)) {
            nombres = s.map(p -> p.getFileName().toString()).filter(n -> n.matches("CONT-B-(4[0-9]|50)_sap-.*")).sorted().toList();
        }
        for (String nombre : nombres) {
            String id = "B" + nombre.substring(7, 9);
            CasoEnvioLegacy caso = CasoEnvioLegacy.cargar(nombre);
            JsonObject esperado = caso.esperado();

            Path tmp = Files.createTempDirectory("fresh-job-");
            String base = tmp.resolve("out").toString() + File.separator;
            H2Jobs db = H2Jobs.nuevaLegacy();
            caso.sembrar(db);
            db.programa(5150L, null);
            db.jdbc().execute("CREATE TABLE \"TEST.EstadoContabilizado\" (\"Orden\" BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + " \"IdEmpresa\" BIGINT, \"IdUsuario\" BIGINT, \"IdPreComprobante\" BIGINT)");
            var plataforma = CasoEnvioLegacy.plataforma(caso.config(Map.of("PLATAFORMA/PLA.RutaBaseArchivos", base)));
            NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(db.dataSource());
            GenerarJsonAsientoSapService generador = new GenerarJsonAsientoSapService(jdbc, caso::planCuentas, plataforma);
            InterfazContableRepository repositorio = new InterfazContableJdbcRepository(jdbc) {
                @Override
                public void actualizarEstadoContabilizado(long idEmpresa, long idUsuario, long idPreComprobante) {
                    jdbc.getJdbcTemplate().update("INSERT INTO \"TEST.EstadoContabilizado\" (\"IdEmpresa\", \"IdUsuario\", \"IdPreComprobante\")"
                            + " VALUES (?, ?, ?)", idEmpresa, idUsuario, idPreComprobante);
                }
            };
            JobResultado r;
            List<Peticion> peticiones;
            try (FakeSapServiceLayer sl = new FakeSapServiceLayer()) {
                for (Map.Entry<String, JsonElement> e : caso.caso().getAsJsonObject("sap").entrySet()) {
                    List<Respuesta> rs = new ArrayList<>();
                    for (JsonElement x : e.getValue().getAsJsonArray()) {
                        JsonObject ro = x.getAsJsonObject();
                        List<String> cookies = new ArrayList<>();
                        if (ro.has("setCookies")) ro.getAsJsonArray("setCookies").forEach(c -> cookies.add(c.getAsString()));
                        rs.add(new Respuesta(ro.get("status").getAsInt(), ro.has("body") ? ro.get("body").getAsString() : null, cookies));
                    }
                    sl.cuando(e.getKey(), rs.toArray(new Respuesta[0]));
                }
                SapB1Gateway gateway = new SapB1Gateway(new SapB1Config(sl.url(), "fake-b1-user", "fake-b1-pass-0000", "FAKECO", 10, Duration.ZERO));
                // production wiring (JobsConfiguration): the run log is streamed by the service listener (review M3)
                InterfazContable job = new InterfazContable(plataforma,
                        InterfazContable.servicioConBitacora(generador, gateway, CasoEnvioLegacy.diccionario()), repositorio, RELOJ);
                Map<String, String> params = new LinkedHashMap<>();
                JsonObject entrada = caso.entrada();
                params.put("IdEmpresa", entrada.get("idEmpresa").getAsString());
                params.put("IdUsuario", entrada.get("idUsuario").getAsString());
                params.put("SecCmbte", esperado.get("secCmbteBase64").getAsString());
                params.put("Destino", entrada.get("destino").getAsString());
                r = db.runner(job).ejecutar(CLASE, 5150L, params);
                peticiones = sl.peticiones();
            }
            String ruta = db.rutaSalida(5150L);
            Path logNuevo = ruta == null ? null : Path.of(base + ruta);
            String logContenido = logNuevo != null && Files.exists(logNuevo) ? Archivos.leer(logNuevo) : "";

            Map<Long, String> estadosNuevos = new TreeMap<>();
            db.jdbc().query("SELECT \"IdPreComprobante\", \"Estado\" FROM \"BSCL.Contabilidad.BD::ITZ.PreComprobante\"",
                    rs -> { estadosNuevos.put(rs.getLong(1), rs.getString(2)); });
            List<String> sapNuevo = peticiones.stream().map(Peticion::clave).toList();
            List<String> cuerposNuevos = peticiones.stream().map(p -> cuerpo(p.clave(), p.cuerpo())).toList();
            List<String> procNuevos = db.jdbc().query("SELECT \"IdEmpresa\", \"IdUsuario\", \"IdPreComprobante\" FROM \"TEST.EstadoContabilizado\" ORDER BY \"Orden\"",
                    (rs, i) -> "IdEmpresa=" + rs.getLong(1) + " IdUsuario=" + rs.getLong(2) + " IdPreComprobante=" + rs.getLong(3));
            int commitsNuevos = db.transactionManager().commits() - (r.desenlace() == JobResultado.Desenlace.COMMIT ? 1 : 0);
            escribir(out.resolve("new").resolve(id), r.desenlace().name(), commitsNuevos,
                    logContenido, estadosNuevos, sapNuevo, cuerposNuevos, procNuevos);

            Map<Long, String> estadosLegacy = new TreeMap<>();
            esperado.getAsJsonObject("tablas").getAsJsonArray("PreComprobante").forEach(row -> estadosLegacy.put(
                    row.getAsJsonObject().get("IdPreComprobante").getAsLong(), row.getAsJsonObject().get("Estado").getAsString()));
            List<String> sapLegacy = new ArrayList<>();
            esperado.getAsJsonArray("peticiones").forEach(p -> sapLegacy.add(p.getAsJsonObject().get("metodo").getAsString()
                    + " " + p.getAsJsonObject().get("ruta").getAsString()));
            List<String> cuerposLegacy = new ArrayList<>();
            esperado.getAsJsonArray("peticiones").forEach(p -> {
                JsonObject po = p.getAsJsonObject();
                cuerposLegacy.add(cuerpo(po.get("metodo").getAsString() + " " + po.get("ruta").getAsString(),
                        po.get("cuerpo").isJsonNull() ? null : po.get("cuerpo").getAsString()));
            });
            List<String> procLegacy = new ArrayList<>();
            esperado.getAsJsonArray("procedimientos").forEach(p -> {
                JsonObject par = p.getAsJsonObject().getAsJsonObject("parametros");
                procLegacy.add("IdEmpresa=" + par.get("1").getAsLong() + " IdUsuario=" + par.get("2").getAsLong()
                        + " IdPreComprobante=" + par.get("5").getAsLong());
            });
            String logLegacy = esperado.get("logContenido").isJsonNull() ? "" : esperado.get("logContenido").getAsString();
            escribir(out.resolve("legacy").resolve(id), esperado.get("resultado").getAsBoolean() ? "COMMIT" : "ROLLBACK",
                    esperado.get("commits").getAsInt(), logLegacy, estadosLegacy, sapLegacy, cuerposLegacy, procLegacy);
            System.out.println("dev jobs driver wrote " + id);
        }
    }

    /**
     * One request body in canonical form (review H1): object keys sorted, JournalEntryLines compared as a multiset
     * (built from SQL without ORDER BY), the Login password masked; numbers keep their literal text.
     */
    static String cuerpo(String clave, String json) {
        if (json == null || json.isBlank()) return clave + " <sin cuerpo>";
        JsonElement e = com.google.gson.JsonParser.parseString(json);
        if (clave.endsWith("/Login") && e.isJsonObject() && e.getAsJsonObject().has("Password")) {
            e.getAsJsonObject().addProperty("Password", "****");
        }
        return clave + " " + canon(e, null);
    }

    static String canon(JsonElement e, String nombre) {
        if (e.isJsonObject()) {
            StringBuilder sb = new StringBuilder("{");
            new TreeMap<>(e.getAsJsonObject().asMap()).forEach((k, v) -> {
                if (sb.length() > 1) sb.append(',');
                sb.append('"').append(k).append("\":").append(canon(v, k));
            });
            return sb.append('}').toString();
        }
        if (e.isJsonArray()) {
            List<String> items = new ArrayList<>();
            e.getAsJsonArray().forEach(x -> items.add(canon(x, null)));
            if ("JournalEntryLines".equals(nombre)) items.sort(String::compareTo);
            return "[" + String.join(",", items) + "]";
        }
        return e.toString();
    }

    static void escribir(Path dir, String resultado, int commits, String log, Map<Long, String> estados, List<String> sap,
                         List<String> cuerpos, List<String> procedimientos) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("resultado.txt"), "resultado=" + resultado + "\ncommits=" + commits + "\n", StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("log.txt"), log, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        estados.forEach((k, v) -> sb.append(k).append('=').append(v).append('\n'));
        Files.writeString(dir.resolve("estados.txt"), sb.toString(), StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("sap.txt"), String.join("\n", sap) + "\n", StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("cuerpos.txt"), String.join("\n", cuerpos) + "\n", StandardCharsets.UTF_8);
        Files.writeString(dir.resolve("procedimientos.txt"), String.join("\n", procedimientos) + "\n", StandardCharsets.UTF_8);
    }
}
