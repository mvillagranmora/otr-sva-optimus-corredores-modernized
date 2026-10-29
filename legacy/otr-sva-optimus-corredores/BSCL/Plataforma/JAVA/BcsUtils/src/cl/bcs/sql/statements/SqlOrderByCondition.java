package cl.bcs.sql.statements;

import java.io.Serializable;

/**
 * Representa una condici&oacute;n simple de ordenamiento (par nombre/orden).
 *
 * @author fpino@email.com
 *
 */
public class SqlOrderByCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre de la columna.
     */
    private final String columnName;

    /**
     * Sentido del ordenamiento.
     */
    private final String orderCondition;

    /**
     * Constructor a partir del par necesario.
     *
     * @param columnName
     *            El nombre de la columna.
     * @param orderCondition
     *            El sentido de ordenamiento.
     */
    public SqlOrderByCondition(final String columnName, final String orderCondition) {
        this.columnName = columnName;
        this.orderCondition = orderCondition;
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
     * Obtiene el sentido de ordenamiento.
     *
     * @return El sentido de ordenamiento.
     */
    public String getOrderCondition() {
        return orderCondition;
    }

    @Override
    public String toString() {
        return "SqlOrderByCondition [columnName=" + columnName + ", orderCondition="
                + orderCondition + "]";
    }

}
