package cl.bcs.mesajobs.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * B-MES-04/05: Lógica de formato de filas de archivo.
 * Réplica de GenerarArchivoAnexoIX:90-105 y equivalentes en los otros 13 jobs.
 */
@Service
public class RowFormatterService {

    /**
     * B-MES-05: Concatena columnas sin separador (formato TXT).
     * Legacy: datosTXT = datosTXT + rs.getString(i)
     */
    public static String formatearFilaTXT(List<String> columnas) {
        if (columnas == null || columnas.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String col : columnas) {
            sb.append(col == null ? "" : col);
        }
        return sb.toString();
    }

    /**
     * B-MES-04: Separador CSV.
     * Legacy: CSVWriter(fw, ';') — punto y coma, no coma.
     * CANARY: cambiar a ',' → B-MES-04 falla.
     */
    public static char separadorCsv() {
        return ';';
    }

    /**
     * B-MES-MISC: La comparación de formato usa compareTo exacto (case-sensitive).
     * Legacy: if (formato.compareTo("CSV") == 0)
     */
    public static boolean esCsv(String formato) {
        return "CSV".compareTo(formato) == 0;
    }

    public static boolean esTxt(String formato) {
        return "TXT".compareTo(formato) == 0;
    }
}
