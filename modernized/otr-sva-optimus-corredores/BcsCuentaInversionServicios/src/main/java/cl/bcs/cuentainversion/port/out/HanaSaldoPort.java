package cl.bcs.cuentainversion.port.out;

import cl.bcs.cuentainversion.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HanaSaldoPort {
    SaldoInicialFinal consultarSaldoInicialFinal(SaldoCuentaInversionEntrada entrada);
    List<SaldoPorFechaEfectiva> consultarSaldoPorFechaEfectiva(SaldoCuentaInversionEntrada entrada);
    List<Movimiento> consultarMovimientos(Long idCuentaInversion, LocalDate fechaDesde,
            LocalDate fechaHasta, boolean incluyeAnulados);
    List<Movimiento> consultarMovimientosHistoricos(Long idCuentaInversion, LocalDate fechaDesde,
            LocalDate fechaHasta, boolean incluyeAnulados);
    BigDecimal consultarSaldoPorFechaAnterior(Long idCuentaInversion, LocalDate fecha);

    /**
     * Movements of the current (post go-live) ledger for the given queries, one query per portfolio/currency (legacy
     * ConsultarMovimientoCuentaInversion_{Insert,Select,Delete}Consulta on the process table). Includes the running
     * {@code SaldoSimulado}.
     */
    default List<MovimientoCuentaInversion> consultarMovimientosPorConsulta(List<ConsultaMovimiento> consultas) {
        throw new UnsupportedOperationException("consultarMovimientosPorConsulta");
    }

    /** Same for the historic ledger ({@code CTA.CargoAbonoMovimientoHistorico}). */
    default List<MovimientoCuentaInversion> consultarMovimientosHistoricosPorConsulta(List<ConsultaMovimiento> consultas) {
        throw new UnsupportedOperationException("consultarMovimientosHistoricosPorConsulta");
    }
}
