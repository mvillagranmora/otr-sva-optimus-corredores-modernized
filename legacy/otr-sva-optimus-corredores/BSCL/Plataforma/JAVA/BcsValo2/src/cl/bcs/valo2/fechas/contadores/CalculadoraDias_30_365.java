package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_30_365 implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance_30_365(fechaDesde, fechaHasta,
                params.isAjusteEOM());

        return 365 * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30 * (fechaHasta.getMes() - fechaDesde.getMes() + dm.getAjusteFechaPosterior()
                        - dm.getAjusteFechaAnterior())
                + fechaHasta.getDia() - fechaDesde.getDia();
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance_30_365(fechaDesde, fechaHasta,
                params.isAjusteEOM());

        return (365. * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30. * (fechaHasta.getMes() - fechaDesde.getMes() + dm.getAjusteFechaPosterior()
                        - dm.getAjusteFechaAnterior())
                + (fechaHasta.getDia() - fechaDesde.getDia())) / 365.;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "30/365", "ISMA 30/365", "ISDA SWAPS:30/365", "ISDA30/365",
                "ISDA 30E/365", "DC30_365" };
    }

}
