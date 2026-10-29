package cl.bcs.sql.statements;

import java.io.Serializable;

import cl.bcs.sql.metadata.SqlTable;

/**
 * Sentencia SELECT con lista de condiciones simples.
 *
 * @author fpino@email.com
 *
 */
public final class SqlSelect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Cl&aacute;usula ORDER BY del resultado.
     */
    private final SqlOrderBy orderBy;

    /**
     * La tabla.
     */
    private final SqlTable table;

    /**
     * La condici&oacute;n WHERE.
     */
    private final SqlWhere where;

    /**
     * Constructor a partir de una table.
     *
     * @param table
     *            La tabla.
     */
    public SqlSelect(final SqlTable table) {
        this.table = table;
        this.where = new SqlWhere();
        this.orderBy = new SqlOrderBy();
    }

    /**
     * Obtiene la cl&aacute;usula ORDER BY.
     *
     * @return La cl&aacute;usula ORDER BY.
     */
    public SqlOrderBy getOrderBy() {
        return orderBy;
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
