package cl.bcs.tesoreria.jobs.job.archivo;

import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo.ResultadoProcedimiento;

import java.util.ArrayList;
import java.util.List;

/**
 * How the rows of the stored procedure become file lines. Legacy: {@code GenerarArchivoPago.generarArchivo_GENERICO} (:265-314) and
 * {@code generarArchivo_BCP_PEN_TBC_TELECREDITO} (:317-385). Defect TES-J-DEF-11 kept: for CSV the column
 * {@code TipoRegistroOptimus} must be the first one, otherwise {@code lineaCSV[i - 2]} is an
 * {@link ArrayIndexOutOfBoundsException}; an output format other than TXT / txt / DAT / CSV writes nothing (TES-J-DEF-10).
 */
public final class GeneradorPago {

    private GeneradorPago() {
    }

    private static boolean esTxt(String formatoSalida) {
        return (formatoSalida.compareTo("TXT") == 0 || formatoSalida.compareTo("txt") == 0) || formatoSalida.compareTo("DAT") == 0;
    }

    /** One line of the BCP file kept until all result sets are read. */
    private record Contenido(Integer idInterno, Integer tipoRegistro, String linea) {
    }

    /** :265-314 */
    public static void generico(List<ResultadoProcedimiento> resultados, FormatoPago formato, ArchivoPago archivo) {
        for (ResultadoProcedimiento resultado : resultados) {
            List<String> columnas = resultado.columnas();
            int numcols = columnas.size();
            for (List<String> fila : resultado.filas()) {
                String[] lineaCSV = new String[numcols];
                String lineaTXT = "";
                int tipoRegistroOptimus = 0;
                for (int i = 1; i <= numcols; ++i) {
                    String nombreColumna = columnas.get(i - 1);
                    if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus")) {
                        tipoRegistroOptimus = Integer.parseInt(fila.get(i - 1));
                    } else {
                        if (esTxt(formato.getFormatoSalida())) {
                            lineaTXT = lineaTXT + formato.formatear(tipoRegistroOptimus, nombreColumna, fila.get(i - 1))
                                    + formato.getSeparadorCampos();
                        } else if (formato.getFormatoSalida().compareTo("CSV") == 0) {
                            lineaCSV[i - 2] = formato.formatear(tipoRegistroOptimus, nombreColumna, fila.get(i - 1));
                        }
                    }
                }
                if (esTxt(formato.getFormatoSalida())) {
                    archivo.escribir(lineaTXT, formato.getTipoSaltoLinea());
                } else if (formato.getFormatoSalida().compareTo("CSV") == 0) {
                    archivo.escribir(lineaCSV);
                }
            }
        }
    }

    /** :317-385: records 1, 2 and 3 interleaved (a detail is followed by the control records with its IdInterno). */
    public static void bcpTelecredito(List<ResultadoProcedimiento> resultados, FormatoPago formato, ArchivoPago archivo) {
        List<Contenido> contenidoArchivo = new ArrayList<>();
        for (ResultadoProcedimiento resultado : resultados) {
            List<String> columnas = resultado.columnas();
            for (List<String> fila : resultado.filas()) {
                String lineaTXT = "";
                int tipoRegistroOptimus = 0;
                int idInterno = 0;
                for (int i = 1; i <= columnas.size(); ++i) {
                    String nombreColumna = columnas.get(i - 1);
                    if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus") || nombreColumna.equalsIgnoreCase("IdInterno")) {
                        if (nombreColumna.equalsIgnoreCase("TipoRegistroOptimus")) {
                            tipoRegistroOptimus = Integer.parseInt(fila.get(i - 1));
                        }
                        if (nombreColumna.equalsIgnoreCase("IdInterno")) {
                            idInterno = Integer.parseInt(fila.get(i - 1));
                        }
                    } else {
                        lineaTXT = lineaTXT + formato.formatear(tipoRegistroOptimus, nombreColumna, fila.get(i - 1))
                                + formato.getSeparadorCampos();
                    }
                }
                contenidoArchivo.add(new Contenido(idInterno, tipoRegistroOptimus, lineaTXT));
            }
        }
        if (contenidoArchivo.size() > 0) {
            for (Contenido contenido : contenidoArchivo) {
                if (contenido.tipoRegistro().compareTo(1) == 0) {
                    archivo.escribir(contenido.linea());
                }
                if (contenido.tipoRegistro().compareTo(2) == 0) {
                    archivo.escribir(contenido.linea());
                    for (Contenido contenidoL3 : contenidoArchivo) {
                        if (contenidoL3.tipoRegistro().compareTo(3) == 0
                                && contenido.idInterno().compareTo(contenidoL3.idInterno()) == 0) {
                            archivo.escribir(contenidoL3.linea());
                        }
                    }
                }
            }
        }
    }
}
