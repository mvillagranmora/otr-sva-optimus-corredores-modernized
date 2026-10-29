package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_30_360 implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance_30_360(fechaDesde, fechaHasta,
                params.isAjusteEOM());

        return 360 * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30 * (fechaHasta.getMes() - fechaDesde.getMes() + dm.getAjusteFechaPosterior()
                        - dm.getAjusteFechaAnterior())
                + fechaHasta.getDia() - fechaDesde.getDia();
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance_30_360(fechaDesde, fechaHasta,
                params.isAjusteEOM());

        return (360. * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30. * (fechaHasta.getMes() - fechaDesde.getMes() + dm.getAjusteFechaPosterior()
                        - dm.getAjusteFechaAnterior())
                + (fechaHasta.getDia() - fechaDesde.getDia())) / 360.;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "30/360", "30U/360", "Bond basis", "30/360 US", "US MUNI: 30/360",
                "MUNI30/360", "ISDA30/360", "DC30_360" };
    }

}
