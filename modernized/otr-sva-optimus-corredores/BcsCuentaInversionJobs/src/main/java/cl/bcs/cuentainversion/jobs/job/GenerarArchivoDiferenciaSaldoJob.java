package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.ConsultaComparacionSaldoService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarArchivoDiferenciaSaldoJob} (150 lines): balance difference file; updates BAT.Programa.
 */
public final class GenerarArchivoDiferenciaSaldoJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarArchivoDiferenciaSaldoJob";

    private final ConsultaComparacionSaldoService consultaComparacionSaldoService;
    private final JdbcTemplate jdbc;

    public GenerarArchivoDiferenciaSaldoJob(ConsultaComparacionSaldoService consultaComparacionSaldoService, JdbcTemplate jdbc) {
        this.consultaComparacionSaldoService = Objects.requireNonNull(consultaComparacionSaldoService, "consultaComparacionSaldoService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivoDiferenciaSaldoJob.java: logica (debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new ConsultaComparacionSaldoService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = ctx.getParam("IdModulo");
        entrada.p_FechaDesde = ctx.getParam("FechaDesde");
        entrada.p_FechaHasta = ctx.getParam("FechaHasta");
        try {
            var salida = Llamada.de(consultaComparacionSaldoService::execute, entrada, ConsultaComparacionSaldoService.Salida.class);
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
                Llamada.actualizar(jdbc, Llamada.SQL_RUTA_SALIDA, "IdPrograma", ctx.idPrograma(), "RutaArchivo", salida.P_OUT_RUTAARCHIVO);
                ctx.commit();
                return true;
            }
            return false; // CIJ-DEF-01
        } catch (Exception e) {
            ctx.rollback();
            throw e;
        }
    }
}
