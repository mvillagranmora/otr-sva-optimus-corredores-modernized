package cl.bcs.sql.statements;

import java.io.Serializable;

import cl.bcs.sql.metadata.SqlTable;

/**
 * Sentencia DELETE con lista de condiciones simples.
 *
 * @author fpino@email.com
 *
 */
public final class SqlDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * La tabla.
     */
    private final SqlTable table;

    /**
     * Cl&aacute;usula WHERE.
     */
    private final SqlWhere where;

    /**
     * Constructor a partir de tabla.
     *
     * @param table
     *            La tabla.
     */
    public SqlDelete(final SqlTable table) {
        this.table = table;
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
     * Obtiene la cl&aacute;usula WHERE.
     *
     * @return La cl&aacute;usula WHERE.
     */
    public SqlWhere getWhere() {
        return where;
    }

}
