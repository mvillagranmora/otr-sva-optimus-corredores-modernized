package cl.bcs.sql.statements;

import java.io.Serializable;

/**
 * Condici&aacute;n BETWEEN.
 *
 * @author fpino@email.com
 *
 */
public final class SqlBetween implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre de la columna.
     */
    private final String columnName;

    /**
     * Valor mayor de la condici&oacute;n BETWEEN.
     */
    private Comparable<?> highValue;

    /**
     * Valor menor de la condici&oacute;n BETWEEN.
     */
    private Comparable<?> lowValue;

    /**
     * Constructor con valor simple.
     *
     * @param columnName
     *            El nombre de la columna.
     * @param value
     *            El valor de la condici&oacute;n.
     */
    public SqlBetween(final String columnName, final Comparable<?> value) {
        this(columnName, value, value);
    }

    /**
     * Constructor con par de valores.
     *
     * @param columnName
     *            El nombre de la columna.
     * @param lowValue
     *            El menor valor de la condici&oacute;n.
     * @param highValue
     *            El mayor valor de la condici&oacute;n.
     */
    public SqlBetween(final String columnName, final Comparable<?> lowValue,
            final Comparable<?> highValue) {
        this.columnName = columnName;
        setValues(lowValue, highValue);
    }

    /**
     * Obtiene el nombre de la columna.
     *
     * @return El nombre de la columna.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Obtiene el mayor valor de la condici&oacute;n.
     *
     * @return El mayor valor de la condici&oacute;n.
     */
    public Comparable<?> getHighValue() {
        return highValue;
    }

    /**
     * Obtiene el menor valor de la condici&oacute;n.
     *
     * @return El menor valor de la condici&oacute;n.
     */
    public Comparable<?> getLowValue() {
        return lowValue;
    }

    /**
     * Asigna el par de valores de la condici&oacute;n.
     *
     * @param lowValue
     *            El menor valor de la condici&oacute;n.
     * @param highValue
     *            El mayor valor de la condici&oacute;n.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setValues(final Comparable<?> lowValue, final Comparable<?> highValue) {
        if (lowValue != null && highValue != null) {
            if (!lowValue.getClass().equals(highValue.getClass())) {
                throw new IllegalArgumentException("Los valores no son del mismo tipo");
            }

            final Comparable comp0 = lowValue;
            final Comparable comp1 = highValue;
            if (comp0.compareTo(comp1) > 0) {
                throw new IllegalArgumentException("lowValue no puede ser mayor a highValue");
            }
        }

        this.lowValue = lowValue;
        this.highValue = highValue;
    }

}
