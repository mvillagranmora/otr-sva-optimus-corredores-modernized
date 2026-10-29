package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.CargaInicialSaldosReporteAntiguedadService;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.CargaInicialSaldosReporteAntiguedadJob} (86 lines): initial load of the aging report balances.
 */
public final class CargaInicialSaldosReporteAntiguedadJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.CargaInicialSaldosReporteAntiguedadJob";

    private final CargaInicialSaldosReporteAntiguedadService cargaInicialSaldosReporteAntiguedadService;

    public CargaInicialSaldosReporteAntiguedadJob(CargaInicialSaldosReporteAntiguedadService cargaInicialSaldosReporteAntiguedadService) {
        this.cargaInicialSaldosReporteAntiguedadService = Objects.requireNonNull(cargaInicialSaldosReporteAntiguedadService, "cargaInicialSaldosReporteAntiguedadService");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // CargaInicialSaldosReporteAntiguedadJob.java:146-184 (logica, debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new CargaInicialSaldosReporteAntiguedadService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_FechaSistema = ctx.getParam("FechaSistema");
        var salida = Llamada.de(cargaInicialSaldosReporteAntiguedadService::execute, entrada, CargaInicialSaldosReporteAntiguedadService.Salida.class);
        // :173-178: no errors -> commit and true; otherwise rollback and false (CIJ-DEF-01)
        if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
            ctx.commit();
            return true;
        }
        ctx.rollback();
        return false;
    }
}
