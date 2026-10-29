package cl.bcs.contabilidad.jobs.web;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.job.CentralizaMultiSeleccionJob;
import cl.bcs.contabilidad.jobs.job.CreaInterfazDemanda;
import cl.bcs.contabilidad.jobs.job.CreaInterfazDiaria;
import cl.bcs.contabilidad.jobs.job.GenerarInformeClienteRelacionadoBatch;
import cl.bcs.contabilidad.jobs.job.InterfazContable;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.support.FakeInterfazSapSource;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService;
import cl.bcs.contabilidad.service.GenerarInformeClienteRelacionadoService;
import cl.bcs.contabilidad.service.InterfazContableService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * {@code POST /internal/job-call} with body {@code {"clase": "<legacy FQN>", "idPrograma": n, "parametros": {...}}}: the
 * entry point the future Job Manager calls instead of instantiating the legacy BcsJob class by name. Binding decision
 * 2026-10-15: jobs run in-process, one transaction per run, BcsJob contract (see JobRunnerContractTest), no @Scheduled.
 *
 * <p>Response: 200 {@code {"clase","idPrograma","resultado":"COMMIT"|"ROLLBACK","error":false,"mensaje":""}}; a job
 * exception gives 500 with {@code "resultado":"ERROR","error":true,"mensaje":<message>}; a class this module does not
 * run gives 503 (the legacy job manager keeps it); a malformed request gives 400. The transaction rule is the BcsJob one
 * (return value / exception), NOT the JavaCall one of BcsContabilidadServicios (P_OUT_ERRORESCONTADOR == 0).
 */
class JobCallControllerTest {

    static final List<String> CLASES = List.of(
            "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob",
            "cl.bcs.contabilidad.jobs.CreaInterfazDiaria",
            "cl.bcs.contabilidad.jobs.CreaInterfazDemanda",
            "cl.bcs.contabilidad.jobs.InterfazContable",
            "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch");

    private final H2Jobs db = H2Jobs.nueva();
    private final CentralizaMultiSeleccionService centraliza = mock(CentralizaMultiSeleccionService.class);
    private final PlataformaPort plataformaDiaria = mock(PlataformaPort.class);
    private final PlataformaPort plataformaDemanda = mock(PlataformaPort.class);
    private final PlataformaPort plataformaInterfaz = mock(PlataformaPort.class);
    private final GenerarInformeClienteRelacionadoService informe = mock(GenerarInformeClienteRelacionadoService.class);
    private final AtomicInteger settingsDiaria = new AtomicInteger();
    private final AtomicInteger settingsDemanda = new AtomicInteger();

    private JobRunner runnerReal() {
        Clock reloj = Clock.systemDefaultZone();
        InterfazFileTransfer sftp = mock(InterfazFileTransfer.class);
        InterfazContadorSource contador = mock(InterfazContadorSource.class);
        // fake, same-shape SFTP settings; plataforma mocks return no base path, so the interface jobs stop with ROLLBACK
        SftpSettings fake = new SftpSettings("sftp.invalid", 22, "fake-sftp-user", "fake-sftp-pass-0000", Path.of("known_hosts"));
        List<ContabilidadJob> jobs = List.of(
                new CentralizaMultiSeleccionJob(centraliza),
                new CreaInterfazDiaria(plataformaDiaria, contador, new FakeInterfazSapSource(), sftp,
                        () -> { settingsDiaria.incrementAndGet(); return fake; }, reloj),
                new CreaInterfazDemanda(plataformaDemanda, contador, new FakeInterfazSapSource(), sftp,
                        () -> { settingsDemanda.incrementAndGet(); return fake; }, reloj),
                new InterfazContable(plataformaInterfaz, repo -> mock(InterfazContableService.class),
                        mock(InterfazContableRepository.class), reloj),
                new GenerarInformeClienteRelacionadoBatch(informe));
        return db.runner(jobs.toArray(new ContabilidadJob[0]));
    }

    private static MockMvc mvc(JobRunner runner) {
        return MockMvcBuilders.standaloneSetup(new JobCallController(runner)).build();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, String cuerpo) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse();
    }

    private static JsonObject json(MockHttpServletResponse r) throws Exception {
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @ParameterizedTest(name = "CONT-J-90: \"{0}\" routes to its own job")
    @ValueSource(strings = {
            "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob",
            "cl.bcs.contabilidad.jobs.CreaInterfazDiaria",
            "cl.bcs.contabilidad.jobs.CreaInterfazDemanda",
            "cl.bcs.contabilidad.jobs.InterfazContable",
            "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch"})
    void contJ90_rutea(String clase) throws Exception {
        when(centraliza.execute(any())).thenReturn(new JsonObject());
        when(informe.execute(any())).thenReturn(JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject());
        JobRunner runner = runnerReal();
        assertThat(runner.clases()).containsExactlyInAnyOrderElementsOf(CLASES);

        String params = "{\"IdEmpresa\":\"1\",\"IdUsuario\":\"21\",\"IdModulo\":\"CON\",\"FechaContable\":\"2026-04-30\",\"Ejercicio\":\"2026\","
                + "\"IdsTipoCentralizacion\":\"200\",\"SecCmbte\":\"MTAw\",\"Destino\":\"B\",\"FechaEjecucion\":\"2026-04-30\"}";
        MockHttpServletResponse r = llamar(mvc(runner), "{\"clase\":\"" + clase + "\",\"idPrograma\":12,\"parametros\":" + params + "}");
        assertThat(json(r).get("clase").getAsString()).isEqualTo(clase);

        switch (clase) {
            case "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob" -> verify(centraliza).execute(any());
            case "cl.bcs.contabilidad.jobs.CreaInterfazDiaria" -> {
                assertThat(settingsDiaria).hasValue(1);
                assertThat(settingsDemanda).hasValue(0);
            }
            case "cl.bcs.contabilidad.jobs.CreaInterfazDemanda" -> {
                assertThat(settingsDemanda).hasValue(1);
                assertThat(settingsDiaria).hasValue(0);
            }
            case "cl.bcs.contabilidad.jobs.InterfazContable" ->
                    verify(plataformaInterfaz).obtenerParametroConfiguracion(eq(1L), anyString(), eq("PLA.RutaBaseArchivos"), any());
            case "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch" -> verify(informe).execute(any());
            default -> throw new AssertionError(clase);
        }
        List<Object> otros = new ArrayList<>(List.of(centraliza, informe, plataformaInterfaz));
        if (clase.endsWith("CentralizaMultiSeleccionJob")) otros.remove(centraliza);
        if (clase.endsWith("GenerarInformeClienteRelacionadoBatch")) otros.remove(informe);
        if (clase.endsWith("InterfazContable")) otros.remove(plataformaInterfaz);
        verifyNoInteractions(otros.toArray());
        assertThat(db.estados().llamadas()).extracting(Estado::idPrograma).containsExactly(12L);
    }

    @ParameterizedTest(name = "CONT-J-91: \"{0}\" is not run here -> 503, no job, no status")
    @ValueSource(strings = {
            "cl.bcs.contabilidad.jobs.NoExiste",
            "cl.bcs.contabilidad.CentralizaMultiSeleccion",            // a JavaCall service, not a job
            "cl.bcs.contabilidad.jobs.CallerWSJournalVoucher",         // legacy helper class, not a BcsJob
            "cl.bcs.contabilidad.jobs.Respuesta",
            "CL.BCS.CONTABILIDAD.JOBS.INTERFAZCONTABLE"})               // class names are case-sensitive
    void contJ91_noMigrado503(String clase) throws Exception {
        MockHttpServletResponse r = llamar(mvc(runnerReal()), "{\"clase\":\"" + clase + "\",\"idPrograma\":12,\"parametros\":{}}");

        assertThat(r.getStatus()).isEqualTo(503);
        assertThat(json(r).get("clase").getAsString()).isEqualTo(clase);
        assertThat(db.estados().llamadas()).isEmpty();
        assertThat(db.transactionManager().inicios()).isZero();
        verifyNoInteractions(centraliza, informe, plataformaDiaria, plataformaDemanda, plataformaInterfaz);
    }

    /** Test job: writes a marker, then returns / throws according to the "modo" parameter. */
    private ContabilidadJob jobPrueba(AtomicReference<Map<String, Object>> visto) {
        return new ContabilidadJob() {
            @Override public String clase() { return "cl.bcs.contabilidad.jobs.Prueba"; }

            @Override
            public boolean transaction(JobContext ctx) throws Exception {
                visto.set(new java.util.HashMap<>(Map.of("id", ctx.idPrograma())));
                for (String k : List.of("modo", "Numero", "Booleano", "Nulo", "Texto")) {
                    if (ctx.getParam(k) != null) visto.get().put(k, ctx.getParam(k));
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
    @DisplayName("CONT-J-92: transaction rule over HTTP: true -> 200 COMMIT (persisted), false -> 200 ROLLBACK error=false, exception -> 500 ERROR")
    void contJ92_reglaTransaccional() throws Exception {
        AtomicReference<Map<String, Object>> visto = new AtomicReference<>();
        MockMvc mvc = mvc(db.runner(jobPrueba(visto)));

        MockHttpServletResponse ok = llamar(mvc, "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":7,\"parametros\":{\"modo\":\"true\"}}");
        assertThat(ok.getStatus()).isEqualTo(200);
        JsonObject o = json(ok);
        assertThat(o.get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(o.get("error").getAsBoolean()).isFalse();
        assertThat(o.get("mensaje").getAsString()).isEmpty();
        assertThat(o.get("idPrograma").getAsLong()).isEqualTo(7L);
        assertThat(db.marcas()).containsExactly("m");

        MockHttpServletResponse rb = llamar(mvc, "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":8,\"parametros\":{\"modo\":\"false\"}}");
        assertThat(rb.getStatus()).isEqualTo(200);
        assertThat(json(rb).get("resultado").getAsString()).isEqualTo("ROLLBACK");
        assertThat(json(rb).get("error").getAsBoolean()).isFalse();
        assertThat(db.marcas()).containsExactly("m");

        MockHttpServletResponse er = llamar(mvc, "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":9,\"parametros\":{\"modo\":\"x\"}}");
        assertThat(er.getStatus()).isEqualTo(500);
        assertThat(json(er).get("resultado").getAsString()).isEqualTo("ERROR");
        assertThat(json(er).get("error").getAsBoolean()).isTrue();
        assertThat(json(er).get("mensaje").getAsString()).isEqualTo("fallo del job");
        assertThat(db.marcas()).containsExactly("m");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(7L, false, ""), new Estado(8L, false, ""),
                new Estado(9L, true, "fallo del job"));
    }

    @Test
    @DisplayName("CONT-J-93: the JavaCall rule does not apply: CentralizaMultiSeleccionJob with P_OUT_ERRORESCONTADOR 2 -> 200 COMMIT")
    void contJ93_sinReglaJavaCall() throws Exception {
        // contrast: BcsContabilidadServicios JavaCallUnitOfWorkFilter rolls back /internal/java-call when the counter > 0
        when(centraliza.execute(any())).thenAnswer(inv -> {
            db.marcar("parcial");
            return JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":2}").getAsJsonObject();
        });
        String body = "{\"clase\":\"cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob\",\"idPrograma\":3,\"parametros\":"
                + "{\"IdEmpresa\":1,\"IdUsuario\":21,\"Ejercicio\":2026,\"IdsTipoCentralizacion\":\"200;201\",\"FechaContable\":\"2026-04-30\"}}";

        MockHttpServletResponse r = llamar(mvc(runnerReal()), body);

        assertThat(r.getStatus()).isEqualTo(200);
        assertThat(json(r).get("resultado").getAsString()).isEqualTo("COMMIT");
        assertThat(db.marcas()).containsExactly("parcial");
    }

    @Test
    @DisplayName("CONT-J-94: parametros: JSON numbers and booleans become their text, null values are dropped, a missing object is empty")
    void contJ94_parametrosATexto() throws Exception {
        AtomicReference<Map<String, Object>> visto = new AtomicReference<>();
        MockMvc mvc = mvc(db.runner(jobPrueba(visto)));

        llamar(mvc, "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":4711,"
                + "\"parametros\":{\"modo\":true,\"Numero\":12,\"Booleano\":false,\"Nulo\":null,\"Texto\":\"a b\"}}");
        assertThat(visto.get()).containsExactlyInAnyOrderEntriesOf(Map.of("id", 4711L, "modo", "true", "Numero", "12",
                "Booleano", "false", "Texto", "a b"));

        MockHttpServletResponse r = llamar(mvc, "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":5}");
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(visto.get()).containsExactlyInAnyOrderEntriesOf(Map.of("id", 5L));
    }

    @ParameterizedTest(name = "CONT-J-95: malformed request {0} -> 400, no job")
    @ValueSource(strings = {
            "no es json",
            "{\"idPrograma\":1}",
            "{\"clase\":\"\",\"idPrograma\":1}",
            "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\"}",
            "{\"clase\":\"cl.bcs.contabilidad.jobs.Prueba\",\"idPrograma\":\"doce\"}"})
    void contJ95_peticionInvalida(String cuerpo) throws Exception {
        AtomicReference<Map<String, Object>> visto = new AtomicReference<>();
        MockHttpServletResponse r = llamar(mvc(db.runner(jobPrueba(visto))), cuerpo);

        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(visto.get()).isNull();
        assertThat(db.estados().llamadas()).isEmpty();
    }

    @Test
    @DisplayName("CONT-J-96: an unknown class reaching the runner directly is a JobNoMigradoException (the controller maps it to 503)")
    void contJ96_runnerDirecto() {
        JobRunner runner = runnerReal();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> runner.ejecutar("cl.bcs.contabilidad.jobs.NoExiste", 1L, Map.of()))
                .isInstanceOf(cl.bcs.contabilidad.jobs.runtime.JobNoMigradoException.class);
        verifyNoInteractions(plataformaInterfaz);
    }
}
