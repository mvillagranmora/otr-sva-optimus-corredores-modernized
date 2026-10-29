package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;

public record SaldoInicialFinal(
        BigDecimal saldoEfectivoInicial,
        BigDecimal saldoEfectivoFinal,
        BigDecimal saldoMovimientoInicial,
        BigDecimal saldoMovimientoFinal,
        BigDecimal montoCargoPendiente,
        BigDecimal montoAbonoPendiente
) {}
