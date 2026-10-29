package cl.bcs.valo2.fechas;

public abstract class FechaFormatter {

    private static String prepad(
            final int i) {
        if (i > 9) {
            return "" + i;
        }

        return "0" + i;
    }

    public static String toDDMMYYYY(
            final Fecha fecha) {
        return toDDMMYYYY(fecha, "-");
    }

    public static String toDDMMYYYY(
            final Fecha fecha,
            final String delim) {
        final String strDelim = null == delim ? "" : delim;

        return prepad(fecha.getDia()) + strDelim + prepad(fecha.getMes()) + strDelim
                + fecha.getAno();
    }

    public static String toYYYYMMDD(
            final Fecha fecha) {
        return toYYYYMMDD(fecha, "-");
    }

    public static String toYYYYMMDD(
            final Fecha fecha,
            final String delim) {
        final String strDelim = null == delim ? "" : delim;

        return prepad(fecha.getAno()) + strDelim + prepad(fecha.getMes()) + strDelim
                + prepad(fecha.getDia());
    }
}
