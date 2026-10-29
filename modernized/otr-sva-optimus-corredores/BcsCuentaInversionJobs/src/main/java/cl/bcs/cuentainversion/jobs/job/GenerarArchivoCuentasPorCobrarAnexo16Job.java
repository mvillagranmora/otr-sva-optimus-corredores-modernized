package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarArchivoCuentasPorCobrarAnexo16Service;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarArchivoCuentasPorCobrarAnexo16Job} (120 lines): annex 16 receivables file; updates BAT.Programa.
 */
public final class GenerarArchivoCuentasPorCobrarAnexo16Job implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarArchivoCuentasPorCobrarAnexo16Job";

    private final GenerarArchivoCuentasPorCobrarAnexo16Service generarArchivoCuentasPorCobrarAnexo16Service;
    private final JdbcTemplate jdbc;

    public GenerarArchivoCuentasPorCobrarAnexo16Job(GenerarArchivoCuentasPorCobrarAnexo16Service generarArchivoCuentasPorCobrarAnexo16Service, JdbcTemplate jdbc) {
        this.generarArchivoCuentasPorCobrarAnexo16Service = Objects.requireNonNull(generarArchivoCuentasPorCobrarAnexo16Service, "generarArchivoCuentasPorCobrarAnexo16Service");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivoCuentasPorCobrarAnexo16Job.java: logica (debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new GenerarArchivoCuentasPorCobrarAnexo16Service.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_FechaHasta = ctx.getParam("FechaHasta");
        try {
            var salida = Llamada.de(generarArchivoCuentasPorCobrarAnexo16Service::execute, entrada,
                    GenerarArchivoCuentasPorCobrarAnexo16Service.Salida.class);
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
                Llamada.actualizar(jdbc, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, "IdPrograma", ctx.idPrograma(), "RutaArchivo", salida.P_OUT_RUTAARCHIVO);
                ctx.commit();
                return true;
            }
            return false; // CIJ-DEF-01
        } catch (Exception e) {
            ctx.rollback();
            throw e;
        }
    }

    /** Legacy resource {@code GenerarArchivoCuentasPorCobrarAnexo16Job_UpdateRutaSalidaProgramaBatch.sql}. */
    private static final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH =
            Llamada.sql("GenerarArchivoCuentasPorCobrarAnexo16Job_UpdateRutaSalidaProgramaBatch");
}
