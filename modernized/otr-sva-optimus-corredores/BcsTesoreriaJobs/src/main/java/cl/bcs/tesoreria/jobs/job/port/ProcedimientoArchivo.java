package cl.bcs.tesoreria.jobs.job.port;

import java.util.List;

/**
 * The HANA stored procedure that feeds a bank file (legacy: {@code CallableStatement} of
 * {@code GenerarArchivoCargos.getService} and {@code GenerarArchivoPago.logica}). Each result set is one record type;
 * the legacy reads every value with {@code getString} and the column names with {@code getColumnName}.
 *
 * <p>Test contract: one call of {@link #ejecutar} is one legacy {@code CallableStatement.execute()}. The legacy
 * GenerarArchivoCargos executes its procedure TWICE (legacy defect TES-J-DEF-02, pinned: two calls with the same
 * arguments), GenerarArchivoPago once.
 */
public interface ProcedimientoArchivo {

    /** One result set of the procedure. A {@code null} value is a SQL NULL. */
    record ResultadoProcedimiento(List<String> columnas, List<List<String>> filas) {
    }

    /**
     * @param llamada    the SQL text prepared by the legacy, for example {@code CALL BSCL."SP_X"(?,?,?,?,?)}
     * @param parametros the bound parameters, in order from index 1: {@link Long} ({@code setLong}) or {@link String}
     *                   ({@code setString})
     * @return the result sets in order; an EMPTY list when the procedure returned no result set ({@code execute()} false)
     */
    List<ResultadoProcedimiento> ejecutar(String llamada, List<Object> parametros) throws Exception;
}
