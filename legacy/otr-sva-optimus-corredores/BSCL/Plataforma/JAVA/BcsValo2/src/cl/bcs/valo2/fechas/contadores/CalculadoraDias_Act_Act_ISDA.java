package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class CalculadoraDias_Act_Act_ISDA implements CalculadoraDias {

    @Override
    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return fechaDesde.diasRestantes() - dm.getAjusteFechaAnterior()
                + fechaHasta.diasTranscurridos() + dm.getAjusteFechaPosterior()
                + fechaHasta.getAno() - fechaDesde.getAno() - (fechaDesde.esBisiesto() ? 366 : 365);
    }

    @Override
    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final AjusteEOM dm = AjusteEOM.getInstance(fechaDesde, fechaHasta,
                params.getFechaVencimiento(), params.isAjusteEOM());

        return (fechaDesde.diasRestantes() - dm.getAjusteFechaAnterior())
                / (fechaDesde.esBisiesto() ? 366. : 365.)
                + (fechaHasta.diasTranscurridos() + dm.getAjusteFechaPosterior())
                        / (fechaHasta.esBisiesto() ? 366. : 365.)
                + fechaHasta.getAno() - fechaDesde.getAno() - 1;
    }

    @Override
    public String[] getNombres() {
        return new String[] { "Actual/Actual ISDA", "Act/Act ISDA", "US:WIT Act/Act",
                "DCAct_Act_ISDA" };
    }

}
