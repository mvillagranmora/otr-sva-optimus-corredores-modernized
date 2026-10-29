package cl.bcs.cuentainversion.domain;

import java.math.BigDecimal;

public record ConsultarSaldoResultado(
        Long idCuentaInversion,
        Long idPortafolio,
        String idMoneda,
        BigDecimal saldo,
        BigDecimal montoMoneyMarket,
        BigDecimal totalLiquidez,
        SaldoInicialFinal saldoInicialFinal
) {}
