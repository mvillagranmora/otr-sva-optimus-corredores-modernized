package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaService;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GuardarPatrimonioCajaJob} (96 lines): saves the cash equity of a date.
 */
public final class GuardarPatrimonioCajaJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GuardarPatrimonioCajaJob";

    private final GuardarPatrimonioCajaService guardarPatrimonioCajaService;
    private final ConfiguracionJobs configuracion;

    public GuardarPatrimonioCajaJob(GuardarPatrimonioCajaService guardarPatrimonioCajaService, ConfiguracionJobs configuracion) {
        this.guardarPatrimonioCajaService = Objects.requireNonNull(guardarPatrimonioCajaService, "guardarPatrimonioCajaService");
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GuardarPatrimonioCajaJob.java:211-246 (logica, debug=false). The legacy catch only logged and rethrew.
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String fecha = ctx.getParam("Fecha");
        if (fecha == null || fecha.isEmpty()) {
            // :227-229 ConfiguracionSistema PLATAFORMA / PLA.FechaSistema
            fecha = Llamada.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        }
        var entrada = new GuardarPatrimonioCajaService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "PLA";
        entrada.p_Fecha = fecha;
        var salida = Llamada.de(guardarPatrimonioCajaService::execute, entrada, GuardarPatrimonioCajaService.Salida.class);
        // :237-239 (CIJ-DEF-01: false is a rollback that the framework reports like a commit)
        return salida.P_OUT_ERRORESCONTADOR <= 0;
    }
}
