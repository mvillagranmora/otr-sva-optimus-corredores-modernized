package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_NL_365 implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (int) (fechaHasta.getJuliano() - fechaDesde.getJuliano()
                - Fecha.contarBisiestos(fechaDesde, fechaHasta, Fecha.RIGHT_INCLUDE)
                + dm.getAjusteFechaPosterior() - dm.getAjusteFechaAnterior());
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (fechaHasta.getJuliano() - fechaDesde.getJuliano()
                - Fecha.contarBisiestos(fechaDesde, fechaHasta, Fecha.RIGHT_INCLUDE)
                + dm.getAjusteFechaPosterior() - dm.getAjusteFechaAnterior()) / 365.;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "Actual/365 JGB", "Actual/365 JGB (NL)", "NL/365", "DCNL_365" };
    }

}
