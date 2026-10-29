package cl.bcs.sql.statements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula una lista de {@link SqlValue}.
 *
 * @author fpino@email.com
 *
 */
public final class SqlValues implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Lista de {@link SqlValue}.
     */
    private final List<SqlValue> values;

    /**
     * Constructor est&aacute;ndar.
     */
    public SqlValues() {
        this.values = new ArrayList<>();
    }

    /**
     * Obtiene la lista de {@link SqlValue}.
     *
     * @return La lista de {@link SqlValue}.
     */
    public List<SqlValue> getValues() {
        return values;
    }

    /**
     * Asigna el valor asociado a la columna indicada.
     *
     * @param name
     *            El nombre de la columna.
     * @param value
     *            El valor.
     */
    public void put(final String name, final Comparable<?> value) {
        for (final SqlValue item : values) {
            if (item.getColumnName().equals(name)) {
                item.setValue(value);
                return;
            }
        }

        values.add(new SqlValue(name, value));
    }

}
