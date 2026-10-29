package cl.bcs.sql.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlDelete;
import cl.bcs.utils.DateUtil;

/**
 * M&eacute;todos utiles asociados a sentencias SQL.
 *
 * @author fpino@email.com
 *
 */
public final class SqlUtil {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * Ejecuta sentencia SQL de actualizaci&oacute;n.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param sql
     *            La sentencia SQL.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void executeUpdate(
            final Connection conn,
            final String sql)
                    throws SQLException {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    public static String format(
            final Date date) {
        if (date == null) {
            return null;
        }

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static Date getDate() {
        return new Date(new java.util.Date().getTime());
    }

    /**
     * Obtiene fecha a partir de los argumentos.
     *
     * @param year
     *            El a&ntilde;o.
     * @param month
     *            El mes
     * @param day
     *            El d&iacute;a.
     * @return La fecha a partir de los argumentos.
     */
    public static Date getDate(
            final int year,
            final int month,
            final int day) {
        return new Date(DateUtil.getDate(year, month, day).getTime());
    }

    public static Date getDate(
            final String val) {
        return new Date(DateUtil.getDate(val).getTime());
    }

    /**
     * Chequea si la conexi&oacute;n est&aacute; activa.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param sql
     *            La sentencia SQL.
     * @return <code>true</code> si la conexi&oacute;n est&aacute; activa,
     *         <code>false</code> en caso contrario.
     */
    public static boolean isConnectionAlive(
            final Connection conn,
            final String sql) {
        try {
            if (conn.isClosed()) {
                return false;
            }

            try (final Statement stmt = conn.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery(sql)) {
                    return true;
                }
            }
        } catch (final SQLException ex) {
            return false;
        }
    }

    /**
     * Genera nombre entre corchetes.
     *
     * @param name
     *            El nombre.
     * @return El nombre entre corchetes.
     */
    public static String toBracketedString(
            final String name) {
        return "[" + name + "]";
    }

    /**
     * Genera nombre entre comillas.
     *
     * @param name
     *            El nombre.
     * @return El nombre entre comillas.
     */
    public static String toQuotedString(
            final String name) {
        return "\"" + name + "\"";
    }

    /**
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param table
     *            La tabla.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void truncate(
            final Connection conn,
            final SqlTable table)
                    throws SQLException {
        final SqlDelete delete = new SqlDelete(table);
        executeUpdate(conn, GENERATOR.getSqlText(delete));
    }

    private SqlUtil() {
    }

}
