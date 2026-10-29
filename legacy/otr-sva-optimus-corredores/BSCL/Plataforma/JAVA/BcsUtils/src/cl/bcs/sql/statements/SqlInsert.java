package cl.bcs.sql.statements;

import java.io.Serializable;

import cl.bcs.sql.metadata.SqlTable;

/**
 * Sentencia INSERT.
 *
 * @author fpino@email.com
 *
 */
public final class SqlInsert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * La tabla.
     */
    private final SqlTable table;

    /**
     * Los valores del registro a insertar.
     */
    private final SqlValues values;

    /**
     * Constructor a partir de tabla.
     *
     * @param table
     *            La tabla.
     */
    public SqlInsert(final SqlTable table) {
        this.table = table;
        this.values = new SqlValues();
    }

    /**
     * Obtiene la tabla.
     *
     * @return La tabla.
     */
    public SqlTable getTable() {
        return table;
    }

    /**
     * Obtiene los valores del registro a insertar.
     *
     * @return Los valores del registro a insertar.
     */
    public SqlValues getValues() {
        return values;
    }

}
