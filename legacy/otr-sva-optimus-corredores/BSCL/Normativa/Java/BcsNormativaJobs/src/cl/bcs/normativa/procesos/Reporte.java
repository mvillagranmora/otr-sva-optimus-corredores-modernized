package cl.bcs.normativa.procesos;

import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cl.bcs.normativa.dal.LocalizadorArchivo;

/**
 * Clase base de los reportes.
 *
 * @author fpino@email.com
 *
 */
public class Reporte {

    /**
     * M&eacute;todo conveniente para buscar la ruta del reporte.
     *
     * @param contexto
     *            El contexto de proceso.
     * @return La ruta del archivo del reporte.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String buscarRutaReporte(final ContextoProceso contexto) throws SQLException {
        return LocalizadorArchivo.buscarRutaArchivo(contexto.getConnection(), contexto.getSchema(),
                "reporte", contexto.getDocumento(), contexto.getIdProceso(), contexto.getEsquema(),
                contexto.getParametros());
    }

    /**
     * Salida del reporte.
     */
    private final PrintStream out;

    /**
     * Constructor.
     *
     * @param out
     *            La salida.
     */
    public Reporte(final PrintStream out) {
        this.out = out;
    }

    /**
     * Formatea fecha/hora.
     *
     * @param date
     *            La fecha/hora.
     * @return Fecha/hora formateada para el reporte.
     */
    public String format(final Date date) {
        final DateFormat df = new SimpleDateFormat("dd-MMM-yyy HH:mm:ss", new Locale("es"));
        return df.format(date);
    }

    /**
     * Avanza una l&iacute;nea en el reporte.
     */
    public void println() {
        out.println();
    }

    /**
     * Imprime una l&iacute;nea de texto del reporte.
     *
     * @param s
     *            El <code>String</code> a imprimir.
     */
    public void println(final String s) {
        out.println(s);
    }

}
