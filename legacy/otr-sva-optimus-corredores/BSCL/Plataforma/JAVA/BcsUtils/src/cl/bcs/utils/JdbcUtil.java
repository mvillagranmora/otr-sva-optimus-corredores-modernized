package cl.bcs.utils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * M&eacute;todos utiles para establecer/obtener datos relativos a sentencias
 * SQL.
 *
 * @author fpino@email.com
 *
 */
public final class JdbcUtil {

    /**
     * Obtiene {@link BigDecimal} desde {@link ResultSet}, seg&uacute;n nombre
     * de la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link BigDecimal} desde {@link ResultSet}, seg&uacute;n nombre
     *         de la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static BigDecimal getBigDecimal(final ResultSet rs, final String columnLabel)
            throws SQLException {
        return rs.getBigDecimal(columnLabel);
    }

    /**
     * Obtiene {@link Boolean} desde {@link ResultSet}, seg&uacute;n nombre de
     * la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Boolean} desde {@link ResultSet}, seg&uacute;n nombre de
     *         la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Boolean getBoolean(final ResultSet rs, final String columnLabel)
            throws SQLException {
        final Boolean ret = rs.getBoolean(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return ret;
    }

    /**
     * Obtiene bytes desde {@link ResultSet}, seg&uacute;n nombre de la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return bytes desde {@link ResultSet}, seg&uacute;n nombre de la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static byte[] getBytes(final ResultSet rs, final String columnLabel)
            throws SQLException {
        return rs.getBytes(columnLabel);
    }

    /**
     * Obtiene {@link Date} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Date} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Date getDate(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getDate(columnLabel);
    }

    /**
     * Obtiene {@link Double} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Double} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Double getDouble(final ResultSet rs, final String columnLabel)
            throws SQLException {
        final double val = rs.getDouble(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return val;
    }

    /**
     * Obtiene {@link Float} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Float} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Float getFloat(final ResultSet rs, final String columnLabel) throws SQLException {
        final float val = rs.getFloat(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return val;
    }

    /**
     * Obtiene {@link Integer} desde {@link ResultSet}, seg&uacute;n nombre de
     * la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Integer} desde {@link ResultSet}, seg&uacute;n nombre de
     *         la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Integer getInteger(final ResultSet rs, final String columnLabel)
            throws SQLException {
        final int val = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return val;
    }

    /**
     * Obtiene {@link Long} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Long} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Long getLong(final ResultSet rs, final String columnLabel) throws SQLException {
        final long val = rs.getLong(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return val;
    }

    /**
     * Obtiene {@link Object} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Object} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Object getObject(final ResultSet rs, final String columnLabel)
            throws SQLException {
        return rs.getObject(columnLabel);
    }

    /**
     * Obtiene {@link Integer} desde {@link ResultSet}, seg&uacute;n nombre de
     * la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Integer} desde {@link ResultSet}, seg&uacute;n nombre de
     *         la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Integer getShort(final ResultSet rs, final String columnLabel)
            throws SQLException {
        final int val = rs.getShort(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return val;
    }

    /**
     * Obtiene {@link String} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link String} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static String getString(final ResultSet rs, final String columnLabel)
            throws SQLException {
        return rs.getString(columnLabel);
    }

    /**
     * Obtiene boolean desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna, interpretando el valor String (valores S/N).
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return boolean desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna, interpretando el valor String (valores S/N).
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static boolean getStringAsBoolean(final ResultSet rs, final String columnLabel)
            throws SQLException {
        final String str = rs.getString(columnLabel);
        if (str == null) {
            return false;
        }

        return "S".equalsIgnoreCase(str);
    }

    /**
     * Obtiene {@link Time} desde {@link ResultSet}, seg&uacute;n nombre de la
     * columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Time} desde {@link ResultSet}, seg&uacute;n nombre de la
     *         columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Time getTime(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getTime(columnLabel);
    }

    /**
     * Obtiene {@link Timestamp} desde {@link ResultSet}, seg&uacute;n nombre de
     * la columna.
     *
     * @param rs
     *            El {@link ResultSet}.
     * @param columnLabel
     *            El nombre de la columna.
     * @return {@link Timestamp} desde {@link ResultSet}, seg&uacute;n nombre de
     *         la columna.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Timestamp getTimestamp(final ResultSet rs, final String columnLabel)
            throws SQLException {
        return rs.getTimestamp(columnLabel);
    }

    /**
     * Asigna par&aacute;metro {@link BigDecimal}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setBigDecimal(final PreparedStatement pstmt, final int index,
            final BigDecimal value) throws SQLException {
        pstmt.setBigDecimal(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Boolean}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setBoolean(final PreparedStatement pstmt, final int index,
            final Boolean value) throws SQLException {
        pstmt.setBoolean(index, value);
    }

    /**
     * Asigna par&aacute;metro bytes.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setBytes(final PreparedStatement pstmt, final int index, final byte[] value)
            throws SQLException {
        pstmt.setBytes(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Date}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setDate(final PreparedStatement pstmt, final int index, final Date value)
            throws SQLException {
        pstmt.setDate(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Double}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setDouble(final PreparedStatement pstmt, final int index, final Double value)
            throws SQLException {
        pstmt.setDouble(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Float}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setFloat(final PreparedStatement pstmt, final int index, final Float value)
            throws SQLException {
        pstmt.setFloat(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Integer}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setInteger(final PreparedStatement pstmt, final int index,
            final Integer value) throws SQLException {
        pstmt.setInt(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Long}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setLong(final PreparedStatement pstmt, final int index, final Long value)
            throws SQLException {
        pstmt.setLong(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Object}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setObject(final PreparedStatement pstmt, final int index, final Object value)
            throws SQLException {
        pstmt.setObject(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Short}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setShort(final PreparedStatement pstmt, final int index, final Short value)
            throws SQLException {
        pstmt.setShort(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link String}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setString(final PreparedStatement pstmt, final int index, final String value)
            throws SQLException {
        pstmt.setString(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Time}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setTime(final PreparedStatement pstmt, final int index, final Time value)
            throws SQLException {
        pstmt.setTime(index, value);
    }

    /**
     * Asigna par&aacute;metro {@link Timestamp}.
     *
     * @param pstmt
     *            La sentencia.
     * @param index
     *            El &iacute;ndice del par&aacute;metro (cuenta desde 1).
     * @param value
     *            El valor a asignar.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static void setTimestamp(final PreparedStatement pstmt, final int index,
            final Timestamp value) throws SQLException {
        pstmt.setTimestamp(index, value);
    }

    private JdbcUtil() {
    }

}
