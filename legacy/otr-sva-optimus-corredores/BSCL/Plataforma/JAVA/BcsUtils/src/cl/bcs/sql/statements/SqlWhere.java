package cl.bcs.sql.statements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una cl&aacute;usula WHERE simple de condiciones BETWEEN unidas por
 * AND.
 *
 * @author fpino@email.com
 *
 */
public final class SqlWhere implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Lista de las condiciones BETWEEN.
     */
    private final List<SqlBetween> conditions;

    /**
     * Constructor est&aacute;ndar.
     */
    public SqlWhere() {
        this.conditions = new ArrayList<>();
    }

    /**
     * Obtiene lista de las condiciones.
     *
     * @return Lista de las condiciones.
     */
    public List<SqlBetween> getConditions() {
        return conditions;
    }

    /**
     * Asigna un valor simple a la condici&oacute;n sobre una columna.
     * <p>
     * En este caso, los l&iacute;mites del BETWEEN son iguales, por lo que
     * equivale a que la columna tenga el valor indicado.
     *
     * @param name
     *            Nombre de la columna.
     * @param value
     *            Valor de la condici&oacute;n.
     */
    public void put(final String name, final Comparable<?> value) {
        put(name, value, value);
    }

    /**
     * Asigna los l&iacute;mites de la condici&oacute;n BETWEEN.
     *
     * @param name
     *            Nombre de la columna.
     * @param lowValue
     *            Valor menor.
     * @param highValue
     *            Valor mayor.
     */
    public void put(final String name, final Comparable<?> lowValue,
            final Comparable<?> highValue) {
        for (final SqlBetween item : conditions) {
            if (item.getColumnName().equals(name)) {
                item.setValues(lowValue, highValue);
                return;
            }
        }

        conditions.add(new SqlBetween(name, lowValue, highValue));
    }

}
