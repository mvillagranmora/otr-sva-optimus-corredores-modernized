package cl.bcs.valo2.data.cargadores;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.valo2.data.archivos.ArchivoCupones;
import cl.bcs.valo2.entidades.Cupon;

public class GeneradorCargaCupones {

    private static String createInsertCupones(
            final long id,
            final Cupon cupon) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("INSERT INTO ").append(SqlUtil.toQuotedString("BSCL.Plataforma.BD::INS.Cupon"))
                .append('(').append(SqlUtil.toQuotedString("IdCupon")).append(",")
                .append(SqlUtil.toQuotedString("Nemotecnico")).append(",")
                .append(SqlUtil.toQuotedString("Numero")).append(",")
                .append(SqlUtil.toQuotedString("Fecha")).append(",")
                .append(SqlUtil.toQuotedString("Call")).append(",")
                .append(SqlUtil.toQuotedString("Intereses")).append(",")
                .append(SqlUtil.toQuotedString("Amortizacion")).append(",")
                .append(SqlUtil.toQuotedString("SaldoPorAmortizar")).append(") VALUES(").append(id)
                .append(",").append(toSingleQuotedString(cupon.getNemo())).append(",")
                .append(cupon.getNumero()).append(",").append(toSingleQuotedDate(cupon.getFecha()))
                .append(",").append(toSingleQuotedString(cupon.isCall() ? "S" : "N")).append(",")
                .append(cupon.getIntereses()).append(",").append(cupon.getAmortizacion())
                .append(",").append(cupon.getSaldoPorAmortizar()).append(");");
        return sb.toString();
    }

    public static void main(
            final String[] args)
                    throws FileNotFoundException {
        final ArchivoCupones archivoCupones = new ArchivoCupones("cupones.csv");
        final Map<String, List<Cupon>> cupones = archivoCupones.getCupones();
        long id = 1;
        try (final PrintStream out = new PrintStream("insert_cupones.sql")) {
            for (final String nemo : cupones.keySet()) {
                for (final Cupon cupon : cupones.get(nemo)) {
                    out.println(createInsertCupones(id++, cupon));
                }
            }
        }
    }

    private static String toSingleQuotedDate(
            final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return "'" + df.format(date) + "'";
    }

    private static String toSingleQuotedString(
            final String str) {
        return "'" + str + "'";
    }

}
