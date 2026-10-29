package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoPorFechaEfectiva(
        Long idCuentaInversion,
        LocalDate fechaEfectiva,
        BigDecimal saldo,
        BigDecimal saldoEnGarantia,
        BigDecimal saldoAPlazoCompromisos,
        BigDecimal saldoAPlazoSimultaneas,
        BigDecimal prestamosEmprestitos,
        BigDecimal tesoreriaEfectivo,
        BigDecimal saldosRetenidos,
        BigDecimal saldosPendienteLiberacionDAP,
        BigDecimal montoCargoPendiente,
        BigDecimal montoAbonoPendiente
) {}
