package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

interface CalculadoraDias {

    int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params);

    double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params);

    String[] getNombres();

}
