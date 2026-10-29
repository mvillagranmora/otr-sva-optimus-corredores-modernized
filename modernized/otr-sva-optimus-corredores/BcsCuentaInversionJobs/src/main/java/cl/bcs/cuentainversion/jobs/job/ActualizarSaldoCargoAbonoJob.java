package cl.bcs.cuentainversion.jobs.job;

import java.util.ArrayList;
import java.util.List;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.ActualizarSaldoCargoAbonoJob} (170 lines): recalculates the balances of the selected accounts; marks BAT.TipoProgramaEjecucion EJEC / empty around the run.
 */
public final class ActualizarSaldoCargoAbonoJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.ActualizarSaldoCargoAbonoJob";

    private final ActualizarSaldoCargoAbonoService actualizarSaldoCargoAbonoService;
    private final JdbcTemplate jdbc;

    public ActualizarSaldoCargoAbonoJob(ActualizarSaldoCargoAbonoService actualizarSaldoCargoAbonoService, JdbcTemplate jdbc) {
        this.actualizarSaldoCargoAbonoService = Objects.requireNonNull(actualizarSaldoCargoAbonoService, "actualizarSaldoCargoAbonoService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // ActualizarSaldoCargoAbonoJob.java:41-127 (logica, debug=false). :58 runs OUTSIDE the try (CIJ-DEF-02).
        cambiarEstado("EJEC");
        try {
            Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
            Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
            String fechaSistema = ctx.getParam("FechaSistema");
            String cuentasInversion = ctx.getParam("CuentasInversion");
            List<ActualizarSaldoCargoAbonoService.CuentaInversion> cuentas = new ArrayList<>();
            for (String cuentaInversion : cuentasInversion.split(";")) { // :80-90
                var cuenta = new ActualizarSaldoCargoAbonoService.CuentaInversion();
                cuenta.IdCuentaInversion = Long.parseLong(cuentaInversion);
                cuentas.add(cuenta);
            }
            var entrada = new ActualizarSaldoCargoAbonoService.Entrada();
            entrada.p_IdEmpresa = idEmpresa;
            entrada.p_IdUsuario = idUsuario;
            entrada.p_FechaMovimiento = fechaSistema;
            entrada.p_CuentaInversion.addAll(cuentas);
            // :99 the real recalculation (execute), not the legacy call() stub (README CTA-A-08)
            var salida = Llamada.de(actualizarSaldoCargoAbonoService::execute, entrada, ActualizarSaldoCargoAbonoService.Salida.class);
            if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) { // :101-108
                ctx.commit();
                return true;
            }
            ctx.rollback();
            return false;
        } catch (Exception e) { // :110-114 (a java.lang.Error skips it and still reaches the finally: CIJ-DEF-02)
            ctx.rollback();
            throw e;
        } finally {
            // :115-124 frees the state row and COMMITS, whatever happened
            cambiarEstado("");
            ctx.commit();
        }
    }

    /** Legacy SQL_UPDATE_TIPO_PROGRAMA_EJECUCION, one row (:54-58 and :116-121). */
    private void cambiarEstado(String idEstado) {
        Llamada.actualizar(jdbc, SQL_UPDATE_TIPO_PROGRAMA_EJECUCION,
                "p_IdTipoPrograma", "ACTUALIZAR_SALDO_CARGO_ABONO", "p_IdEstado", idEstado);
    }

    private static final String SQL_UPDATE_TIPO_PROGRAMA_EJECUCION = Llamada.sql("ActualizarSaldoCargoAbonoJob_Update_TipoProgramaEjecucion");
}
