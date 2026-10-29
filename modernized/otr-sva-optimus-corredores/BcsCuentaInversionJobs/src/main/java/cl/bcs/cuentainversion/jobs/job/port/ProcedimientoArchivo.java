package cl.bcs.cuentainversion.jobs.job.port;

import java.util.List;

/**
 * The HANA stored procedure that feeds a generated file (legacy: the {@code CallableStatement} of {@code GenerarArchivo.logica}, the
 * procedure named by {@code CTA.FormatoArchivo.Servicio}). Each result set is one record type; the legacy reads every value with
 * {@code getString} and the column names with {@code getColumnName}.
 *
 * <p>Test contract: one call of {@link #ejecutar} is one legacy {@code CallableStatement.execute()} (the generic job executes its
 * procedure once).
 */
public interface ProcedimientoArchivo {

    /** One result set of the procedure. A {@code null} value is a SQL NULL. */
    record ResultadoProcedimiento(List<String> columnas, List<List<String>> filas) {
    }

    /**
     * @param llamada    the SQL text prepared by the legacy, for example {@code CALL "SP_X"(?,?,?,?,?)}
     * @param parametros the bound parameters, in order from index 1: {@link Long} ({@code setLong}) or {@link String}
     *                   ({@code setString}); the legacy binds them by NAME ({@code p_IdEmpresa}, {@code p_IdUsuario},
     *                   {@code p_CodigoFormatoArchivo}, {@code p_IdPrograma}), in that order
     * @return the result sets in order; an EMPTY list when the procedure returned no result set ({@code execute()} false)
     */
    List<ResultadoProcedimiento> ejecutar(String llamada, List<Object> parametros) throws Exception;
}
