package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaBolsaCTAService;
import cl.bcs.cuentainversion.reportes.ReporteXlsxCuentas;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarReporteAntiguedadDeudaBolsaCTAJob} (140 lines): stock exchange debt aging report; updates BAT.Programa.
 */
public final class GenerarReporteAntiguedadDeudaBolsaCTAJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarReporteAntiguedadDeudaBolsaCTAJob";

    private final GenerarReporteAntiguedadDeudaBolsaCTAService generarReporteAntiguedadDeudaBolsaCTAService;
    private final JdbcTemplate jdbc;

    public GenerarReporteAntiguedadDeudaBolsaCTAJob(GenerarReporteAntiguedadDeudaBolsaCTAService generarReporteAntiguedadDeudaBolsaCTAService, JdbcTemplate jdbc) {
        this.generarReporteAntiguedadDeudaBolsaCTAService = Objects.requireNonNull(generarReporteAntiguedadDeudaBolsaCTAService, "generarReporteAntiguedadDeudaBolsaCTAService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarReporteAntiguedadDeudaBolsaCTAJob.java:logica (debug=false). Parameters read OUTSIDE the try (CIJ-DEF-06).
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new GenerarReporteAntiguedadDeudaBolsaCTAService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "CTA";
        entrada.p_FechaSolicitud = ctx.getParam("P_FechaSolicitud");
        entrada.p_IdMoneda = ctx.getParam("P_Moneda");
        entrada.p_SeparaIntermediacion = ctx.getParam("P_SeparaIntermediacion");
        entrada.p_AgrupaPortafolios = ctx.getParam("P_AgrupaPortafolios");
        entrada.p_Consolidar = ctx.getParam("P_Consolidar");
        entrada.p_NombreInternoArchivo = ctx.getParam("NombreInternoArchivo");
        try {
            var salida = Llamada.de(generarReporteAntiguedadDeudaBolsaCTAService::execute, entrada, ReporteXlsxCuentas.Salida.class);
            Llamada.guardarRutaReporte(jdbc, Llamada.SQL_RUTA_SALIDA_COMENTARIO, salida, ctx.idPrograma());
            return true; // CIJ-DEF-03
        } catch (Exception e) {
            ctx.rollback();
            throw e;
        }
    }
}
