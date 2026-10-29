package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_Act_360 implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (int) (fechaHasta.getJuliano() - fechaDesde.getJuliano()
                + dm.getAjusteFechaPosterior() - dm.getAjusteFechaAnterior());
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (fechaHasta.getJuliano() - fechaDesde.getJuliano() + dm.getAjusteFechaPosterior()
                - dm.getAjusteFechaAnterior()) / 360.;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "Actual/360 Fixed", "Act/360", "A/360", "French", "US:WIB Act/360",
                "DCAct_360" };
    }

}
