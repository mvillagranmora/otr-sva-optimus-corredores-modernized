package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.jobs.adapter.DespachadorEnProceso;
import cl.bcs.contabilidad.jobs.adapter.EntradaEnProceso;
import cl.bcs.contabilidad.jobs.adapter.PlataformaEnProceso;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.javacall.cloper.ActualizarTransaccionService;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 5, the same-transaction proof with the REAL {@code JobRunner} (it opens the transaction with REQUIRES_NEW on the application's
 * {@code PlatformTransactionManager}) and a REAL Tesoreria collaborator that writes: {@code ActualizarTransaccionService} (legacy
 * {@code cl.bcs.tesoreria.cloper.ActualizarTransaccion}, an UPDATE of {@code ORD.Transaccion}), reached through the real in-process entry point of
 * Tesoreria ({@code JavaCallDispatcher}), exactly as the clearing is. The service is a test bean here because the production wiring only scans the
 * clearing (the one group M collaborator Contabilidad reaches), which only reads. If the collaborator used its own connection (as an HTTP hop does),
 * its write would survive the rollback of the job and the tests below would fail.
 */
class ColaboradoresEnProcesoTransaccionTest extends EnProcesoBase {

    private static final String ACTUALIZAR = "cl.bcs.tesoreria.cloper.ActualizarTransaccion";
    private static final String TRANSACCION = "SELECT \"IdCamara\", \"EsBilateral\" FROM \"BSCL.Orden.BD::ORD.Transaccion\" WHERE \"IdTransaccion\" = ";

    /** A transactional (REQUIRED) service that always throws. */
    static class ServicioQueFalla {
        @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
        public void falla() {
            throw new IllegalStateException("tesoreria service failed");
        }
    }

    /** The CLOPER web service is not part of the proof: a stub answers the two trades of the date. */
    @TestConfiguration(proxyBeanMethods = false)
    static class ColaboradoresDePrueba {

        @Bean
        DiccionarioMensajes diccionarioDePrueba() {
            return (idEmpresa, codigo, valores) -> codigo;
        }

        @Bean
        Colaboradores colaboradoresTesoreriaDePrueba() {
            return (servicio, entrada) -> {
                String palos = "{\"valor\":[" + palo("F1") + "," + palo("F2") + "]}";
                return JsonParser.parseString(palos).getAsJsonObject();
            };
        }

        @Bean
        ActualizarTransaccionService actualizarTransaccionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
                                                                  DiccionarioMensajes diccionario, Colaboradores colaboradores) {
            return new ActualizarTransaccionService(jdbc, configuracion, diccionario, colaboradores);
        }

        @Bean
        ServicioQueFalla servicioQueFalla() {
            return new ServicioQueFalla();
        }

        private static String palo(String folio) {
            return "{\"folio\":\"" + folio + "\",\"nemo\":\"NEMO\",\"fecha\":\"2025-04-01\",\"cantidad\":10,\"monto\":1000,"
                    + "\"camara\":\"RV\",\"tipo_liquida\":\"B\",\"estado\":\"E\"}";
        }
    }

    @Autowired
    ServicioQueFalla servicioQueFalla;
    @Autowired
    PlatformTransactionManager transacciones;
    @Autowired
    JavaCallDispatcher dispatcherTesoreria;
    @Autowired
    DespachadorEnProceso despachador;
    @Autowired
    PlataformaPort plataforma;

    @BeforeEach
    void prepararBase() {
        crearTabla("BSCL.Orden.BD::ORD.Transaccion", "\"IdTransaccion\" BIGINT, \"IdEmpresa\" BIGINT, \"Folio\" VARCHAR(50), "
                + "\"Nemotecnico\" VARCHAR(50), \"Cantidad\" DECIMAL(26,8), \"Precio\" DECIMAL(26,8), \"Monto\" DECIMAL(26,8), "
                + "\"FechaSistemaCreacion\" DATE, \"FechaLiquidacion\" DATE, \"IdCamara\" VARCHAR(10), \"EsBilateral\" VARCHAR(1)");
        crearTabla("BSCL.Plataforma.BD::PLA.Configuracion", "\"IdEmpresa\" BIGINT, \"CodigoParametro\" VARCHAR(200), \"Valor\" VARCHAR(500)");
        crearTabla("BSCL.Plataforma.BD::PLA.Empresa", "\"IdEmpresa\" BIGINT, \"IdSocioDeNegocio\" BIGINT");
        crearTabla("BSCL.SocioNegocio.BD::SOC.SocioDeNegocio", "\"IdSocioDeNegocio\" BIGINT, \"IdEmpresa\" BIGINT, \"IdPersona\" BIGINT, \"Borrado\" VARCHAR(1)");
        crearTabla("BSCL.Plataforma.BD::INS.Corredora", "\"IdPersona\" BIGINT, \"IdCorredora\" BIGINT, \"NombreCorto\" VARCHAR(100)");
        crearTabla("BSCL.Plataforma.BD::INS.CorredoraCodigos", "\"IdCorredora\" BIGINT, \"CodigoCorredora\" VARCHAR(50), \"IdBolsa\" VARCHAR(10)");
        for (String parametro : List.of("PLA.CloperWsBSC.Password", "PLA.CloperWsBSC.Profile", "PLA.CloperWsBSC.Wsdl")) {
            jdbc.update("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, ?, 'x')", parametro);
        }
        for (int id = 1; id <= 2; id++) {
            jdbc.update("INSERT INTO \"BSCL.Orden.BD::ORD.Transaccion\" VALUES (?, 1, ?, 'NEMO', 10, 100, 1000, DATE '2025-04-01', DATE '2025-04-02', '', 'N')",
                    id, "F" + id);
        }
    }

    private static JsonObject paraFolio(String folio) {
        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", 1L);
        entrada.addProperty("p_FechaTransaccion", "2025-04-01");
        entrada.addProperty("p_FolioTransaccion", folio);
        return entrada;
    }

    private Map<String, Object> transaccion(int id) {
        return jdbc.queryForMap(TRANSACCION + id);
    }

    private JsonObject colaborador(String folio) {
        return dispatcherTesoreria.maneja(ACTUALIZAR) ? despacharComoLaAplicacion(folio) : null;
    }

    private JsonObject despacharComoLaAplicacion(String folio) {
        try {
            return dispatcherTesoreria.despachar(ACTUALIZAR, paraFolio(folio));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private JobResultado correr(Function<JobContext, Boolean> cuerpo) {
        ContabilidadJob job = new ContabilidadJob() {
            @Override
            public String clase() {
                return "cl.bcs.contabilidad.TestEnProceso";
            }

            @Override
            public boolean transaction(JobContext ctx) {
                return cuerpo.apply(ctx);
            }
        };
        JobRunner runner = new JobRunner(transacciones, jdbc, List.of(job), (id, error, mensaje) -> { });
        return runner.ejecutar(job.clase(), 1L, Map.of());
    }

    @Test
    @DisplayName("S5-T01: the collaborator's write is committed when the job commits (and is written by the real service: 0 errors)")
    void laEscrituraDelColaboradorSeConfirmaCuandoElJobConfirma() {
        assertThat(transaccion(1)).containsEntry("IdCamara", "").containsEntry("EsBilateral", "N");
        JobResultado r = correr(ctx -> {
            JsonObject salida = colaborador("F1");
            assertThat(salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
            return true;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(transaccion(1)).containsEntry("IdCamara", "RV").containsEntry("EsBilateral", "S");
        assertThat(transaccion(2)).containsEntry("IdCamara", "").containsEntry("EsBilateral", "N");
        assertThat(PETICIONES).as("no HTTP").isEmpty();
    }

    @Test
    @DisplayName("S5-T02: the write is visible to the job itself (same connection) and undone when the job returns false")
    void laEscrituraSeDeshaceCuandoElJobHaceRollback() {
        JobResultado r = correr(ctx -> {
            colaborador("F1");
            assertThat(transaccion(1)).containsEntry("IdCamara", "RV");
            return false;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(transaccion(1)).containsEntry("IdCamara", "").containsEntry("EsBilateral", "N");
    }

    @Test
    @DisplayName("S5-T03: the write is undone when the job fails after the call")
    void laEscrituraSeDeshaceCuandoElJobFallaDespuesDeLaLlamada() {
        JobResultado r = correr(ctx -> {
            colaborador("F1");
            throw new IllegalStateException("the job fails after the collaborator wrote");
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(transaccion(1)).containsEntry("IdCamara", "").containsEntry("EsBilateral", "N");
    }

    @Test
    @DisplayName("S5-T04: an intermediate ctx.commit() keeps the first write; a later failure undoes only the second")
    void unCommitIntermedioConfirmaLoDelColaboradorYLoPosteriorSeDeshace() {
        JobResultado r = correr(ctx -> {
            colaborador("F1");
            ctx.commit(); // legacy con.commit() inside the job
            colaborador("F2");
            throw new IllegalStateException("fails after the second call");
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(transaccion(1)).containsEntry("IdCamara", "RV");
        assertThat(transaccion(2)).containsEntry("IdCamara", "");
    }

    /** Stand-in for a Tesoreria service that throws (legacy NPE TES-B-45): a real @Transactional (REQUIRED) bean, so its proxy joins the job transaction. */
    private EntradaEnProceso tesoreriaQueFalla() {
        return EntradaEnProceso.de("tesoreria", PlataformaEnProceso.CLEARING_FINANCIERO::equals, (clase, entrada) -> {
            servicioQueFalla.falla();
            return null;
        });
    }

    @Test
    @DisplayName("S5-T06: the clearing fails and the caller swallows it (CentralizaMultiSeleccion, per type): the earlier write still commits, the run is COMMIT")
    void unFalloDelClearingTragadoPorElLlamadorNoEnvenenaLaTransaccionDelJob() {
        PlataformaEnProceso puerto = new PlataformaEnProceso(plataforma, new DespachadorEnProceso(List.of(tesoreriaQueFalla())), transacciones);
        JobResultado r = correr(ctx -> {
            colaborador("F1");
            try {
                puerto.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01");
                throw new AssertionError("the clearing must fail");
            } catch (IllegalStateException esperado) {
                assertThat(esperado).hasMessage("tesoreria service failed"); // reaches the caller unchanged
            }
            return true;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(transaccion(1)).containsEntry("IdCamara", "RV");
    }

    @Test
    @DisplayName("S5-T07: why the savepoint: the same failure without it (bare dispatcher) poisons the run (UnexpectedRollbackException, all undone)")
    void sinPuntoDeGuardaElMismoFalloEnvenenaLaTransaccion() {
        DespachadorEnProceso sinPuntoDeGuarda = new DespachadorEnProceso(List.of(tesoreriaQueFalla()));
        JobResultado r = correr(ctx -> {
            colaborador("F1");
            try {
                sinPuntoDeGuarda.despachar(PlataformaEnProceso.CLEARING_FINANCIERO, paraFolio("F1"));
            } catch (RuntimeException tragada) {
                // swallowed
            }
            return true;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(org.springframework.transaction.UnexpectedRollbackException.class);
        assertThat(transaccion(1)).containsEntry("IdCamara", "");
    }

    @Test
    @DisplayName("S5-T05: the application's Tesoreria entry point is the same dispatcher that serves the clearing, and this writer is not claimed in production")
    void elPuntoDeEntradaEsElMismoYElEscritorDePruebaNoSeReclama() {
        assertThat(despachador.maneja(ACTUALIZAR)).as("production wiring claims only the clearing").isFalse();
        assertThat(dispatcherTesoreria.maneja(ACTUALIZAR)).isTrue();
    }
}
