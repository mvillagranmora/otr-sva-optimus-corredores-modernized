package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarReporteAcreedoresService;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarReporteAcreedoresJob} (137 lines): creditors report; updates BAT.Programa.
 */
public final class GenerarReporteAcreedoresJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarReporteAcreedoresJob";

    private final GenerarReporteAcreedoresService generarReporteAcreedoresService;
    private final JdbcTemplate jdbc;

    public GenerarReporteAcreedoresJob(GenerarReporteAcreedoresService generarReporteAcreedoresService, JdbcTemplate jdbc) {
        this.generarReporteAcreedoresService = Objects.requireNonNull(generarReporteAcreedoresService, "generarReporteAcreedoresService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarReporteAcreedoresJob.java:logica. The parameters are read OUTSIDE the try, in this order (CIJ-DEF-06).
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new GenerarReporteAcreedoresService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "CTA";
        entrada.p_FechaSolicitud = ctx.getParam("P_FechaSolicitud");
        entrada.p_IdMoneda = ctx.getParam("P_Moneda");
        entrada.p_Consolidar = ctx.getParam("P_Consolidar");
        entrada.p_NombreInternoArchivo = ctx.getParam("NombreInternoArchivo");
        try {
            var salida = Llamada.de(generarReporteAcreedoresService::execute, entrada, ReporteXlsxCuentas.Salida.class);
            Llamada.guardarRutaReporte(jdbc, SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH, salida, ctx.idPrograma());
            return true; // CIJ-DEF-03: also with errors in the answer; the service work stays committed
        } catch (Exception e) {
            ctx.rollback();
            throw e;
        }
    }

    /** Legacy resource {@code GenerarReporteAcreedoresJob_UpdateRutaSalidaProgramaBatch.sql}. */
    private static final String SQL_UPDATE_RUTA_SALIDA_PROGRAMA_BATCH =
            Llamada.sql("GenerarReporteAcreedoresJob_UpdateRutaSalidaProgramaBatch");
}
