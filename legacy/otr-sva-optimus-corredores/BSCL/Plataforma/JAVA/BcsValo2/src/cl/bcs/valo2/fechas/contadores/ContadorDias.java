package cl.bcs.valo2.fechas.contadores;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.fechas.Fecha;
import cl.bcs.valo2.repositorios.RepositorioFeriados;

public final class ContadorDias {
    private final Map<String, CalculadoraDias> mapCalculadoras;
    private final RepositorioFeriados repositorioFeriados;

    public ContadorDias(
            final ValorizadorContexto contexto) throws SQLException {
        this.repositorioFeriados = new RepositorioFeriados(contexto);
        this.mapCalculadoras = new ConcurrentHashMap<String, CalculadoraDias>();
        setCalculadoras();
    }

    public int calcularDiasCorridos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final String convencionDias = params.getConvencionDias();

        if ("BUS252".equalsIgnoreCase(convencionDias)
                || "BUS DAYS252".equalsIgnoreCase(convencionDias)
                || "BUS/252".equalsIgnoreCase(convencionDias)) {
            return calcularDiasHabiles(fechaDesde, fechaHasta, params);
        }

        final CalculadoraDias calculadora = mapCalculadoras.get(convencionDias.toUpperCase());
        if (null == calculadora) {
            throw new ContadorDiasException(
                    "Convencion de dias no implementada: " + convencionDias);
        }

        return calculadora.calcularDiasCorridos(fechaDesde, fechaHasta, params);
    }

    public int calcularDiasHabiles(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        int numBusDays = 0;
        double right = fechaHasta.getJuliano();
        double left = fechaDesde.getJuliano();

        if (left > right) {
            right = fechaDesde.getJuliano();
            left = fechaHasta.getJuliano();
        }

        while (left != right) {
            if (!esFeriado(new Fecha(left++), params.getCalendario())) {
                numBusDays++;
            }
        }

        return fechaDesde.getJuliano() > fechaHasta.getJuliano() ? -numBusDays : numBusDays;
    }

    public double calcularFraccionAno(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final ContadorDiasParams params) {
        final String convencionDias = params.getConvencionDias();

        if ("BUS252".equalsIgnoreCase(convencionDias)
                || "BUS DAYS252".equalsIgnoreCase(convencionDias)
                || "BUS/252".equalsIgnoreCase(convencionDias)) {
            return calcularDiasHabiles(fechaDesde, fechaHasta, params) / 252.;
        }

        final CalculadoraDias calculadora = mapCalculadoras.get(convencionDias.toUpperCase());
        if (null == calculadora) {
            throw new ContadorDiasException(
                    "Convencion de dias no implementada: " + convencionDias);
        }

        return calculadora.calcularFraccionAno(fechaDesde, fechaHasta, params);
    }

    public int contarFeriados(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final String calendario) {
        int numHolidays = 0;
        double right = fechaHasta.getJuliano();
        double left = fechaDesde.getJuliano();

        if (left > right) {
            right = fechaDesde.getJuliano();
            left = fechaHasta.getJuliano();
        }

        while (left != right) {
            if (esFeriado(new Fecha(left++), calendario)) {
                numHolidays++;
            }
        }

        return fechaDesde.getJuliano() > fechaHasta.getJuliano() ? -numHolidays : numHolidays;
    }

    public Fecha diaHabil(
            final Fecha fecha,
            final String calendario) {
        Fecha ret = Fecha.getInstance(fecha);
        while (esFeriado(ret, calendario)) {
            ret = ret.sumarDias(1);
        }

        return ret;
    }

    public boolean esFeriado(
            final Fecha fecha,
            final String calendario) {
        return repositorioFeriados.isFeriado(fecha, calendario);
    }

    public List<Double> getFeriados(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final String calendario) {
        final List<Double> lsHolidays = new ArrayList<Double>();

        double right = fechaHasta.getJuliano();
        double left = fechaDesde.getJuliano();

        if (left > right) {
            right = fechaDesde.getJuliano();
            left = fechaHasta.getJuliano();
        }

        while (left != right) {
            if (esFeriado(new Fecha(left++), calendario)) {
                lsHolidays.add(left - 1);
            }
        }

        return lsHolidays;
    }

    public Fecha restarDiasHabiles(
            final Fecha fecha,
            final int dias,
            final String calendario) {
        Fecha ret = Fecha.getInstance(fecha);
        double dbl = fecha.getJuliano();
        for (int i = 0; i < dias; i++) {
            do {
                ret = new Fecha(--dbl);
            } while (esFeriado(ret, calendario));
        }

        return ret;
    }

    private void setCalculadora(
            final CalculadoraDias dcfCalc) {
        for (final String nombre : dcfCalc.getNombres()) {
            mapCalculadoras.put(nombre.toUpperCase(), dcfCalc);
        }
    }

    private void setCalculadoras() {
        setCalculadora(new CalculadoraDias_30_360());
        setCalculadora(new CalculadoraDias_30_365());
        setCalculadora(new CalculadoraDias_30E_360());
        setCalculadora(new CalculadoraDias_Act_360());
        setCalculadora(new CalculadoraDias_Act_365());
        setCalculadora(new CalculadoraDias_Act_Act_ISDA());
        setCalculadora(new CalculadoraDias_NL_365());
    }

    public Fecha sumarDiasHabiles(
            final Fecha fecha,
            final int dias,
            final String calendario) {
        Fecha ret = Fecha.getInstance(fecha);
        for (int i = 0; i < dias; i++) {
            do {
                ret = ret.sumarDias(1);
            } while (esFeriado(ret, calendario));
        }

        return ret;
    }

}
