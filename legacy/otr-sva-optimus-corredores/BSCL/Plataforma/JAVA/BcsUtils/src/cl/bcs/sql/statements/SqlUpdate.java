package cl.bcs.sql.statements;

import java.io.Serializable;

import cl.bcs.sql.metadata.SqlTable;

/**
 * Sentencia UPDATE con lista de condiciones simples.
 *
 * @author fpino@email.com
 *
 */
public final class SqlUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * La tabla.
     */
    private final SqlTable table;

    /**
     * Los pares nombre/valor de la asignaci&oacute;n de la sentencia UPDATE.
     */
    private final SqlValues values;

    /**
     * Cl&aacute;usula WHERE de la sentencia UPDATE.
     */
    private final SqlWhere where;

    /**
     * Constructor asociado a una tabla
     *
     * @param table
     *            La tabla.
     */
    public SqlUpdate(final SqlTable table) {
        this.table = table;
        this.values = new SqlValues();
        this.where = new SqlWhere();
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
     * Obtiene la lista de valores.
     *
     * @return La lista de valores.
     */
    public SqlValues getValues() {
        return values;
    }

    /**
     * Obtiene la cl&aacute;usula WHERE.
     *
     * @return La cl&aacute;usula WHERE.
     */
    public SqlWhere getWhere() {
        return where;
    }

}
