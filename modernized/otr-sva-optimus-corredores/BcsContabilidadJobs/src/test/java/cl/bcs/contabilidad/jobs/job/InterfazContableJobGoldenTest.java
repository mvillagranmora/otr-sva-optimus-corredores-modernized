package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.infrastructure.persistence.InterfazContableJdbcRepository;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Config;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Gateway;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.CasoEnvioLegacy;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer.Peticion;
import cl.bcs.contabilidad.jobs.support.FakeSapServiceLayer.Respuesta;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import cl.bcs.contabilidad.service.InterfazContableService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Golden equivalence of the InterfazContable JOB SHELL against outputs recorded from the unmodified legacy
 * {@code cl.bcs.contabilidad.jobs.InterfazContable} (cases CONT-B-40..50, recorded by the BcsContabilidadServicios legacy
 * oracle and read in place, see {@link CasoEnvioLegacy}).
 *
 * <p>Wiring, everything local and fake: the case database on H2 (MODE=LEGACY); REAL {@code GenerarJsonAsientoSapService},
 * REAL {@code SapB1Gateway} against {@link FakeSapServiceLayer} replaying the case's SAP script, REAL
 * {@code InterfazContableService}, REAL {@code InterfazContableJdbcRepository} except the state procedure
 * {@code ITZ.ActualizarEstadoPreComprobante}, which H2 cannot host and is recorded in {@code TEST.EstadoContabilizado}
 * (the oracle also only recorded it). The job runs through the {@code JobRunner} like the Job Manager would.
 *
 * <p>Compared with the golden, per case: {@code resultado} (true = COMMIT), {@code logContenido} = the exact bytes of
 * {@code <PLA.RutaBaseArchivos>} + RutaSalida, {@code commits} = explicit commits of the job (one per accepted voucher,
 * InterfazContable.java:244; plus the final BcsJob commit), the Estado of every PreComprobante row, and the sequence of
 * SAP requests. Also pinned: RutaSalida = {@code ITZ/<yyyyMMddHHmmss>_InterfazContable.log} and the approved deviations
 * (B1SESSION cookie sent; the SAP password never in the log file nor on stdout/stderr).
 */
@ExtendWith(OutputCaptureExtension.class)
class InterfazContableJobGoldenTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.InterfazContable";
    /** Fake, same-shape SAP B1 credentials (the values the oracle used); real ones come from the environment. */
    static final String USUARIO_SAP = "fake-b1-user";
    static final String CLAVE_SAP = "fake-b1-pass-0000";
    static final long ID_PROGRAMA = 5150L;
    static final Clock RELOJ = Clock.fixed(Instant.parse("2025-04-01T13:15:30Z"), ZoneId.of("America/Santiago"));
    static final String RUTA_SALIDA = "ITZ/20250401101530_InterfazContable.log";

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    @TempDir
    Path tmp;

    record Corrida(JobResultado resultado, H2Jobs db, List<Peticion> peticiones, String base) {
    }

    private Corrida correr(String nombre, boolean romperRutaSalida) throws Exception {
        CasoEnvioLegacy caso = CasoEnvioLegacy.cargar(nombre);
        JsonObject entrada = caso.entrada();
        H2Jobs db = H2Jobs.nuevaLegacy();
        caso.sembrar(db);
        db.programa(ID_PROGRAMA, null);
        db.jdbc().execute("CREATE TABLE \"TEST.EstadoContabilizado\" (\"Orden\" BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + " \"IdEmpresa\" BIGINT, \"IdUsuario\" BIGINT, \"IdPreComprobante\" BIGINT)");
        if (romperRutaSalida) db.jdbc().execute("DROP TABLE \"" + H2Jobs.TABLA_PROGRAMA + "\"");

        String base = tmp.resolve("out").toString() + File.separator;
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

        try (FakeSapServiceLayer sl = new FakeSapServiceLayer()) {
            for (Map.Entry<String, JsonElement> e : caso.caso().getAsJsonObject("sap").entrySet()) {
                List<Respuesta> rs = new ArrayList<>();
                for (JsonElement r : e.getValue().getAsJsonArray()) {
                    JsonObject ro = r.getAsJsonObject();
                    List<String> cookies = new ArrayList<>();
                    if (ro.has("setCookies")) ro.getAsJsonArray("setCookies").forEach(c -> cookies.add(c.getAsString()));
                    rs.add(new Respuesta(ro.get("status").getAsInt(), ro.has("body") ? ro.get("body").getAsString() : null, cookies));
                }
                sl.cuando(e.getKey(), rs.toArray(new Respuesta[0]));
            }
            SapB1Gateway gateway = new SapB1Gateway(new SapB1Config(sl.url(), USUARIO_SAP, CLAVE_SAP, "FAKECO", 10, Duration.ZERO));
            InterfazContable job = new InterfazContable(plataforma,
                    repo -> new InterfazContableService(generador, gateway, repo, CasoEnvioLegacy.diccionario()), repositorio, RELOJ);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("IdEmpresa", entrada.get("idEmpresa").getAsString());
            params.put("IdUsuario", entrada.get("idUsuario").getAsString());
            params.put("SecCmbte", caso.esperado().get("secCmbteBase64").getAsString());
            params.put("Destino", entrada.get("destino").getAsString());
            JobResultado r = db.runner(job).ejecutar(CLASE, ID_PROGRAMA, params);
            return new Corrida(r, db, sl.peticiones(), base);
        }
    }

    private static Map<Long, String> estados(H2Jobs db) {
        Map<Long, String> m = new LinkedHashMap<>();
        db.jdbc().query("SELECT \"IdPreComprobante\", \"Estado\" FROM \"BSCL.Contabilidad.BD::ITZ.PreComprobante\" ORDER BY \"IdPreComprobante\"",
                rs -> { m.put(rs.getLong(1), rs.getString(2)); });
        return m;
    }

    private static Map<Long, String> estadosEsperados(JsonObject esperado) {
        Map<Long, String> m = new LinkedHashMap<>();
        esperado.getAsJsonObject("tablas").getAsJsonArray("PreComprobante").forEach(row ->
                m.put(row.getAsJsonObject().get("IdPreComprobante").getAsLong(), row.getAsJsonObject().get("Estado").getAsString()));
        return m;
    }

    @ParameterizedTest(name = "{0}: job outcome, log file, RutaSalida, commits and PreComprobante.Estado equal the legacy recording")
    @ValueSource(strings = {
            "CONT-B-40_sap-envio-ok-socio-nuevo",
            "CONT-B-41_sap-socio-existente",
            "CONT-B-42_sap-error-400-cuenta-5002",
            "CONT-B-43_sap-error-400-campo-1000",
            "CONT-B-44_sap-error-400-sin-homologar",
            "CONT-B-45_sap-lote-ok-y-error",
            "CONT-B-46_sap-error-crear-socio",
            "CONT-B-47_sap-error-verificar-socio",
            "CONT-B-48_sap-reintentos-5xx-asiento",
            "CONT-B-49_sap-login-fallido",
            "CONT-B-50_sap-precomprobante-sin-vista"})
    void contJ77_golden(String nombre, CapturedOutput salida) throws Exception {
        // CONT-J-77. InterfazContable.java:84-85 (file), :108/:123/:160/:164/:169 (lines), :175 (batch E), :178 (RutaSalida),
        // :185 (true), :244 (commit per accepted voucher).
        JsonObject esperado = CasoEnvioLegacy.cargar(nombre).esperado();
        Corrida c = correr(nombre, false);

        assertThat(c.resultado().desenlace()).as(nombre + " resultado").isEqualTo(esperado.get("resultado").getAsBoolean() ? Desenlace.COMMIT : Desenlace.ROLLBACK);
        assertThat(c.db().estados().llamadas()).containsExactly(new Estado(ID_PROGRAMA, false, ""));
        assertThat(c.db().rutaSalida(ID_PROGRAMA)).isEqualTo(RUTA_SALIDA);
        assertThat(Archivos.leer(Path.of(c.base() + RUTA_SALIDA))).as(nombre + " logContenido").isEqualTo(esperado.get("logContenido").getAsString());

        int commitsJob = esperado.get("commits").getAsInt();
        assertThat(c.db().jdbc().queryForObject("SELECT COUNT(*) FROM \"TEST.EstadoContabilizado\"", Integer.class))
                .as(nombre + " state-procedure calls").isEqualTo(commitsJob);
        assertThat(c.db().transactionManager().commits()).as(nombre + " commits: one per accepted voucher + the final BcsJob commit")
                .isEqualTo(commitsJob + 1);
        assertThat(estados(c.db())).as(nombre + " PreComprobante.Estado").isEqualTo(estadosEsperados(esperado));

        List<String> rutas = new ArrayList<>();
        esperado.getAsJsonArray("peticiones").forEach(p -> rutas.add(p.getAsJsonObject().get("metodo").getAsString() + " "
                + p.getAsJsonObject().get("ruta").getAsString()));
        assertThat(c.peticiones()).extracting(Peticion::clave).as(nombre + " SAP requests").isEqualTo(rutas);
        // Approved deviation: modern sends the B1SESSION cookie (legacy sent only the last Set-Cookie, ROUTEID).
        c.peticiones().stream().filter(p -> !p.clave().equals("POST /b1s/v1/Login"))
                .forEach(p -> assertThat(p.cookie()).as("session cookie").contains("B1SESSION=fake-session-0001"));
        // Approved deviation: the SAP password is never logged (legacy printed the login JSON to stdout).
        assertThat(salida.getAll()).doesNotContain(CLAVE_SAP);
        assertThat(Archivos.leer(Path.of(c.base() + RUTA_SALIDA))).doesNotContain(CLAVE_SAP);
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @DisplayName("CONT-J-78: CONT-B-45 with a failing RutaSalida update -> ERROR; voucher 100 stays committed (:244), the 'E' of 200 is rolled back")
    void contJ78_fallaTrasEnvio_conservaLoComprometido() throws Exception {
        // Legacy: con.commit() after each 204 (InterfazContable.java:244); the batch 'E' (:175) and updateRutaSalida (:178)
        // are only committed by BcsJob (:133-135), so an exception at :178 rolls them back (BcsJob.java:140-144).
        String nombre = "CONT-B-45_sap-lote-ok-y-error";
        JsonObject esperado = CasoEnvioLegacy.cargar(nombre).esperado();
        Corrida c = correr(nombre, true);

        assertThat(c.resultado().desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(c.db().estados().llamadas()).singleElement().extracting(Estado::error).isEqualTo(true);
        assertThat(c.db().jdbc().queryForList("SELECT \"IdPreComprobante\" FROM \"TEST.EstadoContabilizado\"", Long.class))
                .containsExactly(100L);
        Map<Long, String> estados = estados(c.db());
        assertThat(estadosEsperados(esperado)).containsEntry(200L, "E");
        assertThat(estados).as("the 'E' batch is rolled back with the job").containsEntry(200L, "V").containsEntry(100L, "V");
        assertThat(Archivos.leer(Path.of(c.base() + RUTA_SALIDA))).as("the file is not transactional")
                .isEqualTo(esperado.get("logContenido").getAsString());
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void reportar() {
        System.out.println("equivalence cases executed (InterfazContable job shell): " + EJECUTADOS.get() + "/12");
        assertThat(EJECUTADOS.get()).as("equivalence cases executed: 0 is NOT proven").isPositive();
    }
}
