package cl.bcs.cuentainversion.jobs.job.pauta;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;
import cl.bcs.cuentainversion.jobs.runtime.SalidaPauta;
import cl.bcs.cuentainversion.service.GuardarSaldosReporteAntiguedadService;
import java.util.Map;
import cl.bcs.cuentainversion.jobs.job.Llamada;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.PautaGuardarSaldosReporteAntiguedad} (43 lines): pauta: saves the aging report balances.
 */
public final class PautaGuardarSaldosReporteAntiguedad implements ProcesoPauta {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.PautaGuardarSaldosReporteAntiguedad";

    private final ConfiguracionJobs configuracion;
    private final GuardarSaldosReporteAntiguedadService guardarSaldosReporteAntiguedadService;

    public PautaGuardarSaldosReporteAntiguedad(ConfiguracionJobs configuracion, GuardarSaldosReporteAntiguedadService guardarSaldosReporteAntiguedadService) {
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.guardarSaldosReporteAntiguedadService = Objects.requireNonNull(guardarSaldosReporteAntiguedadService, "guardarSaldosReporteAntiguedadService");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public SalidaPauta transaction(Map<String, Object> parametros) throws Exception {
        // PautaGuardarSaldosReporteAntiguedad.java:22-38. CIJ-DEF-11: (Long) casts; no commit.
        String fechaSistema = Llamada.parametro(configuracion, (Long) parametros.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
        var entrada = new GuardarSaldosReporteAntiguedadService.Entrada();
        entrada.p_IdEmpresa = (Long) parametros.get("IdEmpresa");
        entrada.p_IdUsuario = (Long) parametros.get("IdUsuario");
        entrada.p_FechaSistema = fechaSistema;
        var salidaServicio = Llamada.de(guardarSaldosReporteAntiguedadService::execute, entrada, GuardarSaldosReporteAntiguedadService.Salida.class);
        SalidaPauta salida = new SalidaPauta();
        salida.P_OUT_MENSAJE = InscribirProcesoBatch.concatenarMensajesReporte(salidaServicio.P_OUT_MENSAJES);
        salida.P_OUT_ERRORESCONTADOR = salidaServicio.P_OUT_ERRORESCONTADOR;
        return salida;
    }
}
