package cl.bcs.cuentainversion.jobs.job.pauta;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;
import cl.bcs.cuentainversion.jobs.runtime.SalidaPauta;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import java.util.Map;
import cl.bcs.cuentainversion.jobs.job.Llamada;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.PautaActualizarSaldoCargoAbono} (70 lines): pauta: recalculates every balance.
 */
public final class PautaActualizarSaldoCargoAbono implements ProcesoPauta {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.PautaActualizarSaldoCargoAbono";

    private final ConfiguracionJobs configuracion;
    private final ActualizarSaldoCargoAbonoService actualizarSaldoCargoAbonoService;

    public PautaActualizarSaldoCargoAbono(ConfiguracionJobs configuracion, ActualizarSaldoCargoAbonoService actualizarSaldoCargoAbonoService) {
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.actualizarSaldoCargoAbonoService = Objects.requireNonNull(actualizarSaldoCargoAbonoService, "actualizarSaldoCargoAbonoService");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public SalidaPauta transaction(Map<String, Object> parametros) throws Exception {
        // PautaActualizarSaldoCargoAbono.java:21-52. CIJ-DEF-11: (Long) casts; the step never commits (the PAUTA runner owns the transaction).
        String fechaSistema = Llamada.parametro(configuracion, (Long) parametros.get("IdEmpresa"), "PLATAFORMA", "PLA.FechaSistema");
        var entrada = new ActualizarSaldoCargoAbonoService.Entrada();
        entrada.p_IdEmpresa = (Long) parametros.get("IdEmpresa");
        entrada.p_IdUsuario = (Long) parametros.get("IdUsuario");
        entrada.p_FechaMovimiento = fechaSistema;
        entrada.p_ActualizarTodosLosSaldos = "S";
        var salidaServicio = Llamada.de(actualizarSaldoCargoAbonoService::execute, entrada, ActualizarSaldoCargoAbonoService.Salida.class);
        SalidaPauta salida = new SalidaPauta();
        salida.P_OUT_MENSAJE = InscribirProcesoBatch.concatenarMensajes(salidaServicio.P_OUT_MENSAJES);
        salida.P_OUT_ERRORESCONTADOR = salidaServicio.P_OUT_ERRORESCONTADOR;
        return salida;
    }
}
