package cl.bcs.tesoreria.jobs.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.ActivarMandatosArchivoCargaService;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService;
import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;
import cl.bcs.tesoreria.jobs.job.BcsJobCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.jobs.job.BcsJobProcesarDistribucion;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoCargos;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoPago;
import cl.bcs.tesoreria.jobs.job.port.CabecerasCarga;
import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.support.H2Jobs;
import cl.bcs.tesoreria.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.tesoreria.port.out.Secuencias;

/**
 * {@code POST /internal/job-call} with body {@code {"clase": "<legacy FQN>", "idPrograma": n, "parametros": {...}}}: the entry point the Job
 * Manager calls instead of instantiating the legacy BcsJob class by name (decision 2026-10-15: in-process, one transaction per run, BcsJob
 * contract, no @Scheduled). Response: 200 {@code {"clase","idPrograma","resultado":"COMMIT"|"ROLLBACK","error":false,"mensaje":""}}; a job exception
 * gives 500 with {@code "resultado":"ERROR","error":true,"mensaje":<message>}; a class this module does not run gives 503 (the legacy Job Manager
 * keeps it); a malformed request gives 400. The four legacy classes are registered under their legacy names.
 */
class JobCallControllerTest {

    static final List<String> CLASES = List.of(
            "cl.bcs.tesoreria.BcsJobProcesarDistribucion",
            "cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM",
            "cl.bcs.tesoreria.generarArchivoCargos.GenerarArchivoCargos",
            "cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPago");

    private final H2Jobs db = H2Jobs.nueva();

    private static List<TesoreriaJob> jobsReales() {
        Colaboradores colab = mock(Colaboradores.class);
        ConfiguracionSistema cfg = mock(ConfiguracionSistema.class);
        Reloj reloj = mock(Reloj.class);
        return List.of(
                new BcsJobProcesarDistribucion(mock(ProcesarDistribucionService.class), colab, cfg, reloj),
                new BcsJobCargaMasivaIngresoEgresoDAPFFMM(mock(CargaMasivaIngresoEgresoDAPFFMMService.class), mock(CabecerasCarga.class), colab, cfg,
                        mock(DiccionarioMensajes.class), reloj, new org.springframework.jdbc.core.JdbcTemplate()),
                new GenerarArchivoCargos(mock(ConsultarFormatoArchivoService.class), mock(ActivarMandatosArchivoCargaService.class),
                        mock(ProcedimientoArchivo.class), mock(Secuencias.class), new org.springframework.jdbc.core.JdbcTemplate(), Clock.systemUTC()),
                new GenerarArchivoPago(mock(ProcedimientoArchivo.class), cfg, new org.springframework.jdbc.core.JdbcTemplate(), Clock.systemUTC()));
    }

    private static MockMvc mvc(JobRunner runner) {
        return MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
    }

    private static JsonObject json(MockHttpServletResponse r) throws Exception {
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private TesoreriaJob jobPrueba(AtomicReference<Map<String, Object>> visto) {
        return new TesoreriaJob() {
            @Override
            public String clase() {
                return "cl.bcs.tesoreria.Prueba";
            }

            @Override
            public boolean transaction(JobContext ctx) {
                visto.set(new java.util.HashMap<>(Map.of("id", ctx.idPrograma())));
                for (String k : List.of("modo", "Numero", "Booleano", "Nulo", "Texto")) {
                    if (ctx.getParam(k) != null) {
                        visto.get().put(k, ctx.getParam(k));
                    }
                }
                db.marcar("m");
                return switch (String.valueOf(ctx.getParam("modo"))) {
                    case "true" -> true;
                    case "false" -> false;
                    default -> throw new IllegalStateException("fallo del job");
                };
            }
        };
    }

    @Test
    @DisplayName("TES-J-90: the four legacy BcsJob classes are registered under their legacy fully qualified names")
    void tesJ90_clasesRegistradas() {
        assertThat(db.runner(jobsReales().toArray(new TesoreriaJob[0])).clases()).containsExactlyInAnyOrderElementsOf(CLASES);
    }

    @ParameterizedTest(name = "TES-J-91: \"{0}\" is not run here -> 503, no job, no status")
    @ValueSource(strings = {
            "cl.bcs.tesoreria.NoExiste",
            "cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagos",              // JavaCall helper, not a BcsJob
            "cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagoBancoEstado",    // JavaCall helper, not a BcsJob
            "cl.bcs.tesoreria.ProcesarDistribucion",                                  // a JavaCall service of BcsTesoreriaServicios
            "CL.BCS.TESORERIA.BCSJOBPROCESARDISTRIBUCION"})                          // class names are case-sensitive
    void tesJ91_noMigrado503(String clase) throws Exception {
        MockHttpServletResponse r = llamar(mvc(db.runner(jobsReales().toArray(new TesoreriaJob[0]))),
                "{\"clase\":\"" + clase + "\",\"idPrograma\":12,\"parametros\":{}}");

        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(json(r).get("clase").getAsString()).isEqualTo(clase);
        assertThat(db.estados().llamadas()).isEmpty();
        assertThat(db.transactionManager().inicios()).isZero();
    }

    @Test
    @DisplayName("TES-J-92: transaction rule over HTTP: true -> 200 COMMIT (persisted), false -> 200 ROLLBACK error=false, exception -> 500 ERROR")
    void tesJ92_reglaTransaccional() throws Exception {
        MockMvc mvc = mvc(db.runner(jobPrueba(new AtomicReference<>())));

        MockHttpServletResponse ok = llamar(mvc, "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":7,\"parametros\":{\"modo\":\"true\"}}");
        assertThat(ok.getStatus()).isEqualTo(200);
        JsonObject o = json(ok);
        assertThat(o.get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(o.get("error").getAsBoolean()).isFalse();
        assertThat(o.get("mensaje").getAsString()).isEmpty();
        assertThat(o.get("idPrograma").getAsLong()).isEqualTo(7L);
        assertThat(db.marcas()).containsExactly("m");

        MockHttpServletResponse rb = llamar(mvc, "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":8,\"parametros\":{\"modo\":\"false\"}}");
        assertThat(rb.getStatus()).isEqualTo(200);
        assertThat(json(rb).get("resultado").getAsString()).isEqualTo("ROLLBACK");
        assertThat(json(rb).get("error").getAsBoolean()).isFalse();
        assertThat(db.marcas()).containsExactly("m");

        MockHttpServletResponse er = llamar(mvc, "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":9,\"parametros\":{\"modo\":\"x\"}}");
        assertThat(er.getStatus()).isEqualTo(500);
        assertThat(json(er).get("resultado").getAsString()).isEqualTo("ERROR");
        assertThat(json(er).get("error").getAsBoolean()).isTrue();
        assertThat(json(er).get("mensaje").getAsString()).isEqualTo("fallo del job");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(7L, false, ""), new Estado(8L, false, ""), new Estado(9L, true, "fallo del job"));
    }

    @Test
    @DisplayName("TES-J-94: parametros: JSON numbers and booleans become their text, null values are dropped, a missing object is empty")
    void tesJ94_parametrosATexto() throws Exception {
        AtomicReference<Map<String, Object>> visto = new AtomicReference<>();
        MockMvc mvc = mvc(db.runner(jobPrueba(visto)));

        llamar(mvc, "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":4711,"
                + "\"parametros\":{\"modo\":true,\"Numero\":12,\"Booleano\":false,\"Nulo\":null,\"Texto\":\"a b\"}}");
        assertThat(visto.get()).containsExactlyInAnyOrderEntriesOf(Map.of("id", 4711L, "modo", "true", "Numero", "12", "Booleano", "false", "Texto", "a b"));

        MockHttpServletResponse r = llamar(mvc, "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":5}");
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(visto.get()).containsExactlyInAnyOrderEntriesOf(Map.of("id", 5L));
    }

    @ParameterizedTest(name = "TES-J-95: malformed request {0} -> 400, no job")
    @ValueSource(strings = {
            "no es json",
            "{\"idPrograma\":1}",
            "{\"clase\":\"\",\"idPrograma\":1}",
            "{\"clase\":\"cl.bcs.tesoreria.Prueba\"}",
            "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":\"doce\"}",
            "{\"clase\":\"cl.bcs.tesoreria.Prueba\",\"idPrograma\":1,\"parametros\":{\"x\":[1]}}"})
    void tesJ95_peticionInvalida(String cuerpo) throws Exception {
        AtomicReference<Map<String, Object>> visto = new AtomicReference<>();
        MockHttpServletResponse r = llamar(mvc(db.runner(jobPrueba(visto))), cuerpo);

        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(visto.get()).isNull();
        assertThat(db.estados().llamadas()).isEmpty();
    }
}
