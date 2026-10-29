package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.ActualizarSaldosReporteAntiguedadService;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.ActualizarSaldosReporteAntiguedadJob} (100 lines): updates the aging report balances of one client.
 */
public final class ActualizarSaldosReporteAntiguedadJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.ActualizarSaldosReporteAntiguedadJob";

    private final ActualizarSaldosReporteAntiguedadService actualizarSaldosReporteAntiguedadService;

    public ActualizarSaldosReporteAntiguedadJob(ActualizarSaldosReporteAntiguedadService actualizarSaldosReporteAntiguedadService) {
        this.actualizarSaldosReporteAntiguedadService = Objects.requireNonNull(actualizarSaldosReporteAntiguedadService, "actualizarSaldosReporteAntiguedadService");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // ActualizarSaldosReporteAntiguedadJob.java:46-98 (logica, debug=false; the debug branch is RULE-076, not ported)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new ActualizarSaldosReporteAntiguedadService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_Rut = ctx.getParam("Rut");
        entrada.p_CodigoPortafolio = ctx.getParam("CodigoPortafolio");
        entrada.p_Moneda = ctx.getParam("IdMoneda");
        entrada.p_FechaMovimiento = ctx.getParam("FechaMovimiento");
        var salida = Llamada.de(actualizarSaldosReporteAntiguedadService::execute, entrada, ActualizarSaldosReporteAntiguedadService.Salida.class);
        // :86-91: no errors -> commit and true; otherwise rollback and false (CIJ-DEF-01: the framework reports both alike)
        if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
            ctx.commit();
            return true;
        }
        ctx.rollback();
        return false;
    }
}
