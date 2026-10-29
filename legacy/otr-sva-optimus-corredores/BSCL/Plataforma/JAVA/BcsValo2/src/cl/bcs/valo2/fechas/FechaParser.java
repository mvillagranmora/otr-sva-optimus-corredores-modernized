package cl.bcs.valo2.fechas;

public abstract class FechaParser {

    public static Fecha parseFromDMY(
            final String str) {
        return parseFromDMY(str, "-");
    }

    public static Fecha parseFromDMY(
            final String str,
            final String delim) {
        if (null == str || str.isEmpty() || null == delim || delim.isEmpty()) {
            return null;
        }

        final String[] partes = str.split(delim);
        if (3 != partes.length) {
            return null;
        }

        final int dia = Integer.valueOf(partes[0]);
        final int mes = Integer.valueOf(partes[1]);
        final int ano = Integer.valueOf(partes[2]);

        return Fecha.getInstance(ano, mes, dia);
    }
}
