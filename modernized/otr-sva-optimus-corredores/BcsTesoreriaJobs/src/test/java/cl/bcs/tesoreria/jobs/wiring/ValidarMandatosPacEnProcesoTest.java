package cl.bcs.tesoreria.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;
import cl.bcs.tesoreria.jobs.adapter.DespachadorEnProceso;
import cl.bcs.tesoreria.jobs.job.BcsJobProcesarDistribucion;
import cl.bcs.tesoreria.jobs.runtime.JobResultado;
import cl.bcs.tesoreria.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.Reloj;

/**
 * The job BcsJobProcesarDistribucion with the REAL in-process ValidarMandatosPAC (typed operation {@code cl.bcs.APV.ValidarMandatosPAC.logica}) on H2:
 * the LogMandatoPAC rows are updated as the legacy {@code logica(con, salida, idEmpresa, idUsuario)} did (inputs and tables of the APV goldens
 * APV-C-480..509, copied byte for byte under {@code enproceso/apv-C-nnn}), in the transaction of the job (committed with it, undone with it) and an
 * exception of the collaborator reaches the job's catch (rollback + rethrow), never swallowed.
 */
class ValidarMandatosPacEnProcesoTest extends EnProcesoBase {

    private static final String TABLA = "BSCL.Tesoreria.BD::PAC.LogMandatoPAC";
    private static final String OPERACION = "cl.bcs.APV.ValidarMandatosPAC.logica";

    @Autowired
    Colaboradores colaboradores;
    @Autowired
    DespachadorEnProceso despachador;
    @Autowired
    PlatformTransactionManager transacciones;
    @Autowired
    ConfiguracionSistema configuracion;
    @Autowired
    Reloj reloj;

    @TempDir
    Path base;

    @BeforeEach
    void prepararLog() throws Exception {
        jdbc.execute("DROP TABLE IF EXISTS \"" + TABLA + "\"");
        jdbc.execute("DROP TABLE IF EXISTS \"BSCL.Plataforma.BD::BAT.Programa\"");
        new ResourceDatabasePopulator(new ClassPathResource("/tesoreria-jobs/enproceso/schema-log-mandato-pac.sql")).execute(dataSource);
        jdbc.execute("CREATE TABLE \"BSCL.Plataforma.BD::BAT.Programa\" (\"IdPrograma\" BIGINT NOT NULL PRIMARY KEY, \"RutaSalida\" VARCHAR(1000))");
        jdbc.update("INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\" VALUES (900, NULL)");
        jdbc.update("DELETE FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.RutaBaseArchivos'");
        jdbc.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.RutaBaseArchivos', ?)", base.toString().replace('\\', '/'));
        Files.createDirectories(base.resolve("tes"));
    }

    private static final String CARPETA = "/tesoreria-jobs/enproceso/apv-C-";

    private static JsonObject caso(String n) {
        return JsonParser.parseString(recurso(CARPETA + n + "/case.json")).getAsJsonObject();
    }

    private static JsonObject esperado(String n) {
        return JsonParser.parseString(recurso(CARPETA + n + "/expected.json")).getAsJsonObject();
    }

    private void sembrar(String n) {
        new ResourceDatabasePopulator(new ByteArrayResource(recurso(CARPETA + n + "/seed.sql").getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .execute(dataSource);
    }

    /** The table as the golden dumps it: the listed columns plus the {@code expresiones} (FechaProceso as NULA / SIN_CAMBIO / ACTUALIZADA). */
    private JsonArray tabla(String n) {
        JsonObject t = caso(n).getAsJsonArray("tablas").get(0).getAsJsonObject();
        StringBuilder sql = new StringBuilder("SELECT ");
        for (JsonElement c : t.getAsJsonArray("columnas")) {
            sql.append('"').append(c.getAsString()).append("\", ");
        }
        for (var e : t.getAsJsonObject("expresiones").entrySet()) {
            sql.append(e.getValue().getAsString()).append(" AS \"").append(e.getKey()).append("\", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" FROM \"").append(TABLA).append("\" ORDER BY ").append(t.get("orden").getAsString());
        JsonArray filas = new JsonArray();
        for (Map<String, Object> f : jdbc.queryForList(sql.toString())) {
            JsonObject o = new JsonObject();
            f.forEach((k, v) -> {
                if (v instanceof Number num) {
                    o.addProperty(k, num);
                } else {
                    o.addProperty(k, v == null ? null : v.toString());
                }
            });
            filas.add(o);
        }
        return filas;
    }

    private JsonArray tablaEsperada(String n) {
        return esperado(n).getAsJsonObject("tablas").getAsJsonArray(TABLA);
    }

    /** What the job sends: the typed object under "salida" (the complete output of the distribution), plus idEmpresa and idUsuario. */
    private static JsonObject comoLoEnviaElJob(JsonObject entrada) {
        JsonObject o = new JsonObject();
        o.add("salida", entrada.get("entrada"));
        o.add("idEmpresa", entrada.get("idEmpresa"));
        o.add("idUsuario", entrada.get("idUsuario"));
        return o;
    }

    @ParameterizedTest(name = "APV-C-{0}")
    @ValueSource(strings = {"480", "481", "484", "487", "488"})
    void elPuertoDelJobActualizaLogMandatoPacComoElGoldenDelLegacy(String n) {
        for (boolean comoElJob : new boolean[]{false, true}) {
            jdbc.update("DELETE FROM \"" + TABLA + "\"");
            sembrar(n);
            JsonObject entrada = caso(n).getAsJsonObject("entrada");
            assertThat(colaboradores.invocar("ValidarMandatosPAC", comoElJob ? comoLoEnviaElJob(entrada) : entrada))
                    .as("legacy logica is void").isNull();
            assertThat(tabla(n)).as("shape: " + (comoElJob ? "salida (job)" : "entrada (golden)")).isEqualTo(tablaEsperada(n));
        }
        assertThat(PETICIONES).as("no HTTP").isEmpty();
    }

    @Test
    void elGoldenDeUsuarioNuloLanzaLaExcepcionDelLegacySinEscribir() {
        sembrar("494");
        assertThat(esperado("494").get("excepcion").getAsString()).startsWith("NullPointerException");
        assertThatThrownBy(() -> colaboradores.invocar("ValidarMandatosPAC", caso("494").getAsJsonObject("entrada")))
                .isInstanceOf(NullPointerException.class);
        assertThat(tabla("494")).isEqualTo(tablaEsperada("494"));
    }

    @Test
    void elDespachadorAceptaLaOperacionTipadaYRechazaLasDesconocidas() {
        assertThat(despachador.propietarioDe(OPERACION).orElseThrow().propietario()).isEqualTo("apv");
        assertThat(despachador.maneja("cl.bcs.APV.ValidarMandatosPAC")).as("the JavaCall call() stays served (null)").isTrue();
        assertThat(despachador.maneja("cl.bcs.APV.ValidarMandatosPAC.otra")).isFalse();
        assertThat(despachador.maneja("cl.bcs.APV.ValidarMandatosPAC.logica2")).isFalse();
        assertThatThrownBy(() -> despachador.despachar("cl.bcs.APV.NoExiste.logica", new JsonObject()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- the real job, REAL JobRunner transaction (REQUIRES_NEW) --------------------------------------------------------------------------

    /** The distribution answers the output object of golden APV-C-484 (1, 2, 3 to process; 2 and 4 rejected) plus the given extra members. */
    private JobResultado correrElJob(String miembrosExtra) throws Exception {
        JsonObject salida = caso("484").getAsJsonObject("entrada").getAsJsonObject("entrada").deepCopy();
        if (miembrosExtra != null) {
            JsonParser.parseString(miembrosExtra).getAsJsonObject().entrySet().forEach(e -> salida.add(e.getKey(), e.getValue()));
        }
        for (String lista : List.of("P_OUT_SOLICITUDES_PROCESADAS", "P_OUT_RESULTADO", "P_OUT_MENSAJES")) {
            if (!salida.has(lista)) {
                salida.add(lista, new JsonArray());
            }
        }
        salida.addProperty("P_OUT_ERRORESCONTADOR", 0);
        ProcesarDistribucionService distribucion = mock(ProcesarDistribucionService.class);
        when(distribucion.execute(any(JsonObject.class))).thenReturn(salida);
        BcsJobProcesarDistribucion job = new BcsJobProcesarDistribucion(distribucion, colaboradores, configuracion, reloj);
        JobRunner runner = new JobRunner(transacciones, jdbc, List.of(job), (id, error, mensaje) -> { });
        return runner.ejecutar(BcsJobProcesarDistribucion.CLASE, 900L, Map.of("IdEmpresa", "1", "IdUsuario", "21", "Simular", "N"));
    }

    @Test
    void elJobEscribeLogMandatoPacYLaEscrituraSeConfirmaConElJob() throws Exception {
        sembrar("484");
        JobResultado r = correrElJob(null);
        assertThat(r.desenlace()).as(String.valueOf(r.error())).isEqualTo(Desenlace.COMMIT);
        assertThat(tabla("484")).isEqualTo(tablaEsperada("484"));
        // the legacy rest of the job went on after the collaborator: CSV written, RutaSalida set, flag reset sent (the only HTTP request)
        assertThat(jdbc.queryForObject("SELECT \"RutaSalida\" FROM \"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = 900", String.class))
                .startsWith("tes/RendicionPAC");
        assertThat(PETICIONES).hasSize(1);
        assertThat(PETICIONES.get(0).get("class").getAsString()).isEqualTo("cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC");
    }

    @Test
    void siElJobFallaDespuesDeLaLlamadaLaEscrituraSeDeshaceConElJob() throws Exception {
        sembrar("484");
        JsonArray antes = tabla("484");
        assertThat(antes).isNotEqualTo(tablaEsperada("484"));
        // TES-J-DEF-15: a message with null tipo aborts the CSV, AFTER ValidarMandatosPAC wrote
        JobResultado r = correrElJob("{\"P_OUT_MENSAJES\":[{\"id\":1}]}");
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NullPointerException.class);
        assertThat(tabla("484")).as("original rows back").isEqualTo(antes);
        assertThat(PETICIONES).as("the flag reset never ran").isEmpty();
    }

    @Test
    void laExcepcionDelColaboradorLlegaAlJobSinTragarseYNoHayRollbackSilencioso() throws Exception {
        sembrar("484");
        JsonArray antes = tabla("484");
        // explicit null list: legacy NullPointerException in logica (APV-C-500); it must reach the job's catch (rollback + rethrow)
        JobResultado r = correrElJob("{\"P_OUT_SOLICITUDES_NO_PROCESADAS\":null}");
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).as("the original exception, not UnexpectedRollbackException").isInstanceOf(NullPointerException.class);
        assertThat(tabla("484")).isEqualTo(antes);
        assertThat(PETICIONES).isEmpty();
    }
}
