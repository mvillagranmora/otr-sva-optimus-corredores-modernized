package cl.bcs.sql.statements;

import java.io.Serializable;

/**
 * Representa una dupla columna/valor.
 *
 * @author fpino@email.com
 *
 */
public class SqlValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre de la columna.
     */
    private final String columnName;

    /**
     * Valor de la columna.
     */
    private Comparable<?> value;

    /**
     * Constructor con el par necesario.
     *
     * @param columnName
     *            El nombre de la columna.
     * @param value
     *            El valor de la columna.
     */
    public SqlValue(final String columnName, final Comparable<?> value) {
        this.columnName = columnName;
        this.value = value;
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
     * Obtiene el valor de la columna.
     *
     * @return El valor de la columna.
     */
    public Comparable<?> getValue() {
        return value;
    }

    /**
     * Asigna el valor de la columna.
     *
     * @param value
     *            El valor de la columna.
     */
    public void setValue(final Comparable<?> value) {
        this.value = value;
    }

}
