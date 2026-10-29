package cl.bcs.sql.statements;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una cl&aacute;usula ORDER BY de una sentencia SELECT.
 *
 * @author fpino@email.com
 *
 */
public class SqlOrderBy {

    /**
     * Lista de las condiciones de ordenamiento.
     */
    private final List<SqlOrderByCondition> conditions;

    /**
     * Constructor est&aacute;ndar.
     */
    public SqlOrderBy() {
        this.conditions = new ArrayList<SqlOrderByCondition>();
    }

    /**
     * Agrega una condici&oacute;n de ordenamiento (nombre columna m&aacute;s
     * sentido del orden).
     *
     * @param condition
     *            Par de la condici&oacute;n (nombre columna m&aacute;s sentido
     *            del orden).
     */
    public void add(final SqlOrderByCondition condition) {
        conditions.add(condition);
    }

    /**
     * Obtiene la lista de condiciones de ordenamiento.
     *
     * @return La lista de condiciones de ordenamiento.
     */
    public List<SqlOrderByCondition> getConditions() {
        return conditions;
    }

}
