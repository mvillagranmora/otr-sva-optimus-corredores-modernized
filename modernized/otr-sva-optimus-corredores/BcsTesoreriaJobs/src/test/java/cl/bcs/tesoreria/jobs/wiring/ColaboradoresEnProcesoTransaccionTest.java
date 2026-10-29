package cl.bcs.tesoreria.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.JobResultado;
import cl.bcs.tesoreria.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Step 5, the same-transaction proof with the REAL {@code JobRunner} (it opens the transaction with REQUIRES_NEW on the application's
 * {@code PlatformTransactionManager}) and a REAL migrated collaborator that writes: APV {@code CambiarEstadoContratosSAAPV} (the use case updates
 * {@code SCS.ContratoSAAPV}), called through the job's {@code Colaboradores} port exactly as BcsJobProcesarDistribucion calls ValidarMandatosPAC.
 * If the collaborator used its own connection (as an HTTP hop does), its write would survive the rollback of the job and the tests below would fail.
 */
class ColaboradoresEnProcesoTransaccionTest extends EnProcesoBase {

    private static final String CONTRATO = "SELECT \"IdEstado\", \"IdEstadoSolicitud\", \"IdGlosa\", \"DescripcionGlosa\" "
            + "FROM \"BSCL.APV.BD::SCS.ContratoSAAPV\" WHERE \"FolioContratoSAAPV\" = 'F-K1'";

    @Autowired
    Colaboradores colaboradores;
    @Autowired
    PlatformTransactionManager transacciones;

    /** The VIG call of the golden APV-A-311, with the glosa text given. */
    private static JsonObject vigente(String glosa) {
        JsonObject entrada = caso().getAsJsonObject("entrada").deepCopy();
        entrada.getAsJsonArray("p_ContratosSAAPV").get(0).getAsJsonObject().addProperty("DescripcionGlosa", glosa);
        return entrada;
    }

    private Map<String, Object> contrato() {
        return jdbc.queryForMap(CONTRATO);
    }

    private JobResultado correr(Function<JobContext, Boolean> cuerpo) {
        TesoreriaJob job = new TesoreriaJob() {
            @Override
            public String clase() {
                return "cl.bcs.tesoreria.TestEnProceso";
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
    void laEscrituraDelColaboradorSeConfirmaCuandoElJobConfirma() {
        assertThat(contrato()).containsEntry("IdEstado", "PDI").containsEntry("IdGlosa", 0L);
        JobResultado r = correr(ctx -> {
            colaboradores.invocar("CambiarEstadoContratosSAAPV", vigente("MOTIVO"));
            return true;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(contrato()).containsEntry("IdEstado", "VIG").containsEntry("IdGlosa", 3L).containsEntry("DescripcionGlosa", "MOTIVO");
        assertThat(PETICIONES).as("only the AgregarLog of Plataforma went over HTTP").hasSize(1);
    }

    @Test
    void laEscrituraDelColaboradorSeDeshaceCuandoElJobHaceRollback() {
        JobResultado r = correr(ctx -> {
            colaboradores.invocar("CambiarEstadoContratosSAAPV", vigente("MOTIVO"));
            // the collaborator's own write is visible to the job itself (same connection) ...
            assertThat(contratoDesdeElJob()).containsEntry("IdEstado", "VIG");
            return false;
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ROLLBACK);
        // ... and gone after the rollback
        assertThat(contrato()).containsEntry("IdEstado", "PDI").containsEntry("IdGlosa", 0L).containsEntry("DescripcionGlosa", "");
    }

    @Test
    void laEscrituraDelColaboradorSeDeshaceCuandoElJobFallaDespuesDeLaLlamada() {
        JobResultado r = correr(ctx -> {
            colaboradores.invocar("CambiarEstadoContratosSAAPV", vigente("MOTIVO"));
            throw new IllegalStateException("the job fails after the collaborator wrote");
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(contrato()).containsEntry("IdEstado", "PDI").containsEntry("DescripcionGlosa", "");
    }

    @Test
    void unCommitIntermedioDelJobConfirmaLoDelColaboradorYLoPosteriorSeDeshace() {
        JobResultado r = correr(ctx -> {
            colaboradores.invocar("CambiarEstadoContratosSAAPV", vigente("MOTIVO"));
            ctx.commit(); // legacy con.commit() inside the job
            colaboradores.invocar("CambiarEstadoContratosSAAPV", vigente("OTRA"));
            throw new IllegalStateException("fails after the second call");
        });
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(contrato()).containsEntry("IdEstado", "VIG").containsEntry("DescripcionGlosa", "MOTIVO");
    }

    /** Reads inside the job through the transaction-bound connection of the JdbcTemplate (the one the runner opened). */
    private Map<String, Object> contratoDesdeElJob() {
        return jdbc.queryForMap(CONTRATO);
    }
}
