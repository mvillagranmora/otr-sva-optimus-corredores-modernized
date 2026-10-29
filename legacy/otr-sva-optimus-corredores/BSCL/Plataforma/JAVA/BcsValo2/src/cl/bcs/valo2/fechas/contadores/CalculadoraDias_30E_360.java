package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_30E_360 implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return 360 * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30 * (fechaHasta.getMes() - fechaDesde.getMes()) + fechaHasta.getDia()
                - fechaDesde.getDia() + dm.getAjusteFechaPosterior()
                - dm.getAjusteFechaAnterior();
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (360. * (fechaHasta.getAno() - fechaDesde.getAno())
                + 30. * (fechaHasta.getMes() - fechaDesde.getMes())
                + (fechaHasta.getDia() - fechaDesde.getDia()) + dm.getAjusteFechaPosterior()
                - dm.getAjusteFechaAnterior()) / 360.;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "30E/360", "30/360 ICMA", "30S/360", "Eurobond basis",
                "Eurobond basis (ISDA 2006)", "Special German", "ISMA 30/360", "30E/360 ISDA",
                "Eurobond basis (ISDA 2000)", "German", "German:30/360", "Ger:30/360",
                "ISDA SWAPS:30/360", "ISDA 30E/360", "DC30E_360" };
    }

}
