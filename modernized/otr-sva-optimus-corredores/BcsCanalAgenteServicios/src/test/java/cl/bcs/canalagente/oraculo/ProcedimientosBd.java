package cl.bcs.canalagente.oraculo;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;

/**
 * Runs, on H2, the final {@code <salida> = SELECT ... ;} of a HANA SQLScript procedure of BD/Servicios (one source file, byte for byte in the oracle and in the JUnit tests). The procedures
 * themselves (CREATE PROCEDURE, CALL of other procedures, table variables, RETURN) can only run on HANA; what H2 can run is the SELECT that builds the output table, once the table variable
 * {@code :v_xxx} it reads is replaced by a stand-in table seeded by the case. Nothing else of the text is interpreted. Java 11 syntax on purpose.
 */
public final class ProcedimientosBd {
    private static final Pattern COMENTARIOS = Pattern.compile("/\\*.*?\\*/|--[^\\n]*", Pattern.DOTALL);

    private ProcedimientosBd() {
    }

    /** the SELECT assigned to the output table variable {@code salida}, without the trailing ';' (comments removed) */
    public static String seleccionFinal(String texto, String salida) {
        String limpio = COMENTARIOS.matcher(texto).replaceAll(" ");
        Matcher m = Pattern.compile("(?<![\\w>])" + Pattern.quote(salida) + "\\s*=\\s*(SELECT\\b.*?);", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(limpio);
        if (!m.find()) throw new IllegalStateException("the procedure text has no '" + salida + " = SELECT ...;'");
        return m.group(1).trim();
    }

    /** the same SELECT with the table variable replaced by the (quoted) stand-in table */
    public static String paraH2(String texto, String salida, String variableTabla, String tablaH2) {
        return seleccionFinal(texto, salida).replace(":" + variableTabla, "\"" + tablaH2 + "\"");
    }

    /** the rows the SELECT returns on H2, ordered by {@code orden} (SQLScript gives no order) */
    public static JsonArray ejecutar(Connection conn, String texto, String salida, String variableTabla, String tablaH2, String orden) throws Exception {
        return VolcadoTablas.consulta(conn, "SELECT * FROM (" + paraH2(texto, salida, variableTabla, tablaH2) + ") ORDER BY " + orden);
    }
}
