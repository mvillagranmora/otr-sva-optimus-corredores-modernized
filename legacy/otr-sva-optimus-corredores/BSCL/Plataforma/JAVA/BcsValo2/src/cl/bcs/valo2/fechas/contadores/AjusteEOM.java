package cl.bcs.valo2.fechas.contadores;

import cl.bcs.valo2.fechas.Fecha;

class AjusteEOM {

    public static final AjusteEOM getInstance(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final Fecha fechaVencimiento,
            final boolean aplicarEOM) {
        final AjusteEOM dm = new AjusteEOM();

        if (!aplicarEOM) {
            return dm;
        }

        if (fechaDesde.esFinDeMes()) {
            dm.ajusteFechaAnterior = 30 - fechaDesde.getDia();
        }

        if (fechaHasta.esFinDeMes() && Fecha.FEBRERO != fechaHasta.getMes()
                && fechaVencimiento != fechaHasta) {
            dm.ajusteFechaPosterior = 30 - fechaHasta.getDia();
        }

        return dm;
    }

    public static final AjusteEOM getInstance_30_360(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final boolean aplicarEOM) {
        final AjusteEOM dm = new AjusteEOM();

        if (!aplicarEOM) {
            return dm;
        }

        if (Fecha.FEBRERO == fechaDesde.getMes() && fechaDesde.esFinDeMes()
                && Fecha.FEBRERO == fechaHasta.getMes() && fechaHasta.esFinDeMes()) {
            dm.ajusteFechaPosterior = 28 == Fecha.diasDelMes(fechaHasta.getAno(),
                    fechaHasta.getMes()) ? 2 : 1;
        }

        if (Fecha.FEBRERO == fechaDesde.getMes() && fechaDesde.esFinDeMes()) {
            dm.ajusteFechaAnterior = 28 == Fecha.diasDelMes(fechaDesde.getAno(),
                    fechaDesde.getMes()) ? 2 : 1;
        }

        if (31 == fechaHasta.getDia() + dm.ajusteFechaPosterior
                && (30 == fechaDesde.getDia() + dm.ajusteFechaAnterior
                        || 31 == fechaDesde.getDia() + dm.ajusteFechaAnterior)) {
            dm.ajusteFechaPosterior--;
        }

        if (31 == fechaDesde.getDia() + dm.ajusteFechaAnterior) {
            dm.ajusteFechaAnterior--;
        }

        return dm;
    }

    public static final AjusteEOM getInstance_30_365(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final boolean aplicarEOM) {
        final AjusteEOM dm = new AjusteEOM();

        if (!aplicarEOM) {
            return dm;
        }

        if (Fecha.FEBRERO == fechaDesde.getMes() && fechaDesde.esFinDeMes()
                && Fecha.FEBRERO == fechaHasta.getMes() && fechaHasta.esFinDeMes()) {
            dm.ajusteFechaPosterior = 28 == Fecha.diasDelMes(fechaHasta.getAno(),
                    fechaHasta.getMes()) ? 2 : 1;
        }

        if (Fecha.FEBRERO == fechaDesde.getMes() && fechaDesde.esFinDeMes()) {
            dm.ajusteFechaAnterior = 28 == Fecha.diasDelMes(fechaDesde.getAno(),
                    fechaDesde.getMes()) ? 2 : 1;
        }

        if (31 == fechaHasta.getDia() + dm.ajusteFechaPosterior
                && (30 == fechaDesde.getDia() + dm.ajusteFechaAnterior
                        || 31 == fechaDesde.getDia() + dm.ajusteFechaAnterior)) {
            dm.ajusteFechaPosterior--;
        }

        if (31 == fechaDesde.getDia() + dm.ajusteFechaAnterior) {
            dm.ajusteFechaAnterior--;
        }

        return dm;
    }

    private int ajusteFechaAnterior;
    private int ajusteFechaPosterior;

    public int getAjusteFechaAnterior() {
        return ajusteFechaAnterior;
    }

    public int getAjusteFechaPosterior() {
        return ajusteFechaPosterior;
    }
}
