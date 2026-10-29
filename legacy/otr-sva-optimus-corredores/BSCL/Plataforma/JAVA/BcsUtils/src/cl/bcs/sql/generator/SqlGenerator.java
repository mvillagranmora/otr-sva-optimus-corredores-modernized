package cl.bcs.sql.generator;

import java.util.List;

import cl.bcs.sql.metadata.SqlColumn;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlBetween;
import cl.bcs.sql.statements.SqlDelete;
import cl.bcs.sql.statements.SqlInsert;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.statements.SqlUpdate;
import cl.bcs.sql.statements.SqlWhere;

/**
 * Definici&oacute;n de los m&eacute;todos del generador SQL.
 *
 * @author fpino@email.com
 *
 */
public interface SqlGenerator {

    /**
     * Genera texto SQL para un lista de columnas.
     *
     * @param columns
     *            la lista de columnas.
     * @return Texto SQL para un lista de columnas.
     */
    String getSqlText(
            final List<SqlColumn> columns);

    /**
     * Genera texto SQL de BETWEEN.
     *
     * @param between
     *            El between.
     *
     * @return Texto SQL de BETWEEN.
     */
    String getSqlText(
            final SqlBetween between);

    /**
     * Genera texto SQL de DELETE.
     *
     * @param delete
     *            El delete.
     *
     * @return Texto SQL de DELETE.
     */
    String getSqlText(
            final SqlDelete delete);

    /**
     * Genera texto SQL de INSERT.
     *
     * @param insert
     *            El insert.
     *
     * @return Texto SQL de INSERT.
     */
    String getSqlText(
            final SqlInsert insert);

    /**
     * Genera texto SQL de SELECT.
     *
     * @param select
     *            El select.
     *
     * @return Texto SQL de SELECT.
     */
    String getSqlText(
            final SqlSelect select);

    /**
     * Genera texto SQL de la tabla.
     *
     * @param table
     *            La tabla.
     *
     * @return Texto SQL de la tabla.
     */
    String getSqlText(
            final SqlTable table);

    /**
     * Genera texto SQL de UPDATE.
     *
     * @param update
     *            El update.
     *
     * @return Texto SQL de UPDATE.
     */
    String getSqlText(
            final SqlUpdate update);

    /**
     * Genera texto SQL de WHERE.
     *
     * @param where
     *            El where.
     *
     * @return Texto SQL de WHERE.
     */
    String getSqlText(
            final SqlWhere where);

    /**
     * Genera texto SQL de un nombre.
     *
     * @param name
     *            El nombre.
     *
     * @return Texto SQL de un nombre.
     */
    String getSqlText(
            final String name);

}
