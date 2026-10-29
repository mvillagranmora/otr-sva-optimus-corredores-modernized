package cl.bcs.cuentainversion.domain;

import java.util.List;

public record CartolaSalida(SaldoInicialFinal saldo, List<Movimiento> movimientos) {}
