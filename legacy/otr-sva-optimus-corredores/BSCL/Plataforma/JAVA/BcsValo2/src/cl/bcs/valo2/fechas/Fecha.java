package cl.bcs.valo2.fechas;

import java.util.Calendar;
import java.util.Date;

import cl.bcs.utils.DateUtil;

public class Fecha implements Comparable<Fecha> {
    private static final double HALFSECOND = 0.5;
    private static final int JGREG = 15 + 31 * (10 + 12 * 1582);

    public static final int LEFT_INCLUDE = 1;
    public static final int RIGHT_INCLUDE = 2;

    public static final int ENERO = 1;
    public static final int FEBRERO = 2;
    public static final int MARZO = 3;
    public static final int ABRIL = 4;
    public static final int MAYO = 5;
    public static final int JUNIO = 6;
    public static final int JULIO = 7;
    public static final int AGOSTO = 8;
    public static final int SEPTIEMBRE = 9;
    public static final int OCTUBRE = 10;
    public static final int NOVIEMBRE = 11;
    public static final int DICIEMBRE = 12;

    public static final int LUNES = 0;
    public static final int MARTES = 1;
    public static final int MIERCOLES = 2;
    public static final int JUEVES = 3;
    public static final int VIERNES = 4;
    public static final int SABADO = 5;
    public static final int DOMINGO = 6;

    public static final int contarBisiestos(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final int includeSide) {
        int numFeb29 = 0;
        boolean loop = true;
        Fecha left = fechaDesde;

        while (loop) {
            double d = left.juliano + 365;
            if (d > fechaHasta.juliano) {
                d = fechaHasta.juliano;
                loop = false;
            }

            final Fecha right = new Fecha(d);
            if (contiene29Feb(left, right, includeSide)) {
                numFeb29++;
            }

            left = right;
        }

        return numFeb29;
    }

    public static final boolean contiene29Feb(
            final Fecha fechaDesde,
            final Fecha fechaHasta,
            final int includeSide) {
        if (fechaDesde.juliano >= fechaHasta.juliano) {
            return false;
        }

        double left = fechaDesde.juliano;
        if (0 == (includeSide & LEFT_INCLUDE)) {
            left++;
        }

        double right = fechaHasta.juliano;
        if (0 == (includeSide & RIGHT_INCLUDE)) {
            right--;
        }

        for (double dbl = left; dbl <= right; dbl++) {
            final Fecha jd = new Fecha(dbl);
            if (FEBRERO == jd.mes && 29 == jd.dia) {
                return true;
            }
        }

        return false;
    }

    public static final int diasDelMes(
            final int ano,
            final int mes) {
        switch (mes) {
        case ENERO:
            return 31;
        case FEBRERO:
            return esBisiesto(ano) ? 29 : 28;
        case MARZO:
            return 31;
        case ABRIL:
            return 30;
        case MAYO:
            return 31;
        case JUNIO:
            return 30;
        case JULIO:
            return 31;
        case AGOSTO:
            return 31;
        case SEPTIEMBRE:
            return 30;
        case OCTUBRE:
            return 31;
        case NOVIEMBRE:
            return 30;
        case DICIEMBRE:
            return 31;
        default:
            throw new FechaException("Mes invalido: " + mes);
        }
    }

    public static final boolean esBisiesto(
            final int ano) {
        return 0 == ano % 4 && 0 != ano % 100 || 0 == ano % 400;
    }

    public static Fecha getInstance(
            final Date date) {
        if (date == null) {
            return null;
        }

        return getInstance(DateUtil.getYear(date), DateUtil.getMonth(date) + 1,
                DateUtil.getDay(date));
    }

    public static Fecha getInstance(
            final Fecha fecha) {
        if (fecha == null) {
            return null;
        }

        return new Fecha(fecha.juliano);
    }

    public static final Fecha getInstance(
            final int ano,
            final int mes,
            final int dia) {
        return new Fecha(juliano(ano, mes, dia));
    }

    public static final Fecha hoy() {
        return getInstance(new Date());
    }

    public static double juliano(
            final int ano,
            final int mes,
            final int dia) {
        if (0 > ano) {
            throw new FechaException("Ano invalido en fecha");
        }

        if (0 > mes) {
            throw new FechaException("Mes invalido en fecha");
        }

        if (0 > dia) {
            throw new FechaException("Dia invalido en fecha");
        }

        int julianYear = ano;
        int julianMonth = mes;

        if (ano < 0) {
            julianYear++;
        }

        if (mes > 2) {
            julianMonth++;
        } else {
            julianYear--;
            julianMonth += 13;
        }

        double dbl = Math.floor(365.25 * julianYear) + Math.floor(30.6001 * julianMonth) + dia
                + 1720995.0;

        if (dia + 31 * (mes + 12 * ano) >= JGREG) {
            final int a = (int) (0.01 * julianYear);
            dbl += 2 - a + 0.25 * a;
        }

        return Math.floor(dbl);
    }

    private final int ano;
    private final int dia;
    private final double juliano;
    private final int mes;

    public Fecha(
            final double juliano) {
        this.juliano = juliano;

        int a = (int) (juliano + HALFSECOND / 86400.);
        if (a >= JGREG) {
            final int alpha = (int) ((a - 1867216 - 0.25) / 36524.25);
            a = a + 1 + alpha - alpha / 4;
        }

        final int b = a + 1524;
        final int c = (int) (6680.0 + (b - 2439870 - 122.1) / 365.25);
        final int d = 365 * c + c / 4;
        final int e = (int) ((b - d) / 30.6001);

        this.dia = b - d - (int) (30.6001 * e);

        int m = e - 1;
        if (m > 12) {
            m -= 12;
        }

        this.mes = m;

        int y = c - 4715;

        if (m > 2) {
            y--;
        }

        if (y <= 0) {
            y--;
        }

        this.ano = y;
    }

    @Override
    public int compareTo(
            final Fecha other) {
        if (juliano > other.juliano) {
            return 1;
        }

        if (juliano < other.juliano) {
            return -1;
        }

        return 0;
    }

    public int diaSemana() {
        return (int) juliano % 7;
    }

    public int diasRestantes() {
        return (int) (juliano(ano, DICIEMBRE, 31) - juliano);
    }

    public int diasTranscurridos() {
        return (int) (juliano - juliano(ano, ENERO, 1));
    }

    public int diferenciaEnDias(
            final Fecha fecha) {
        return (int) (juliano - fecha.juliano);
    }

    @Override
    public boolean equals(
            final Object o) {
        if (!(o instanceof Fecha)) {
            return false;
        }

        return (int) juliano == (int) ((Fecha) o).juliano;
    }

    public boolean esBisiesto() {
        return esBisiesto(ano);
    }

    public boolean esFinDeMes() {
        return dia == diasDelMes(ano, mes);
    }

    public boolean esFinDeSemana() {
        final int dow = diaSemana();
        return dow == SABADO || dow == DOMINGO;
    }

    public int getAno() {
        return ano;
    }

    public int getDia() {
        return dia;
    }

    public double getJuliano() {
        return juliano;
    }

    public int getMes() {
        return mes;
    }

    @Override
    public int hashCode() {
        final long bits = Double.doubleToLongBits((int) juliano);
        return (int) (bits ^ bits >>> 32);
    }

    public Fecha restaMeses(
            final int months) {

        // Advertencia!!!
        //
        // Copiado desde implementacion original BCS.
        // No sirve para fechas con dia > 28.

        int y = ano;
        int m = mes;
        int d = dia;

        for (int i = 0; i < months; i++) {
            m--;
            if (m < ENERO) {
                m = DICIEMBRE;
                y--;
            }
        }

        final int daysInMonth = diasDelMes(y, m);
        if (d > daysInMonth) {
            d = daysInMonth;
        }

        return getInstance(y, m, d);
    }

    public Fecha sumarAnos(
            final int anos) {
        return getInstance(ano + anos, mes, dia);
    }

    public Fecha sumarDias(
            final int dias) {
        return new Fecha(juliano + dias);
    }

    public Fecha sumarMeses(
            final int meses) {
        int d = dia;
        int m = mes;
        int y = ano;

        if (12 < (m += meses)) {
            while (12 < m) {
                y++;
                m -= 12;
            }
        } else if (0 >= m) {
            y--;
            m += 12;
        }

        while (d > diasDelMes(y, m)) {
            d--;
        }

        return getInstance(y, m, d);
    }

    public Fecha sumarTenor(
            final String tenor) {
        if (null == tenor || tenor.isEmpty()) {
            return null;
        }

        final int timeUnit = Integer.valueOf(tenor.substring(0, tenor.length() - 1));
        final char chTenor = tenor.toUpperCase().charAt(tenor.length() - 1);

        switch (chTenor) {
        case 'D':
            return sumarDias(timeUnit);
        case 'W':
            return sumarDias(timeUnit * 7);
        case 'L':
            return sumarDias(timeUnit * 28);
        case 'M':
            return sumarMeses(timeUnit);
        case 'Y':
            return sumarAnos(timeUnit);
        default:
            throw new FechaException("Formato desconocido: " + tenor);
        }
    }

    public Date toDate() {
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, ano);
        cal.set(Calendar.MONTH, mes - 1);
        cal.set(Calendar.DAY_OF_MONTH, dia);
        return cal.getTime();
    }

    @Override
    public String toString() {
        return FechaFormatter.toDDMMYYYY(this);
    }

}
