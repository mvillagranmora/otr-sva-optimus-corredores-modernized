package cl.bcs.sql.generator;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.sql.metadata.SqlColumn;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlBetween;
import cl.bcs.sql.statements.SqlDelete;
import cl.bcs.sql.statements.SqlInsert;
import cl.bcs.sql.statements.SqlOrderBy;
import cl.bcs.sql.statements.SqlOrderByCondition;
import cl.bcs.sql.statements.SqlSelect;
import cl.bcs.sql.statements.SqlUpdate;
import cl.bcs.sql.statements.SqlValue;
import cl.bcs.sql.statements.SqlValues;
import cl.bcs.sql.statements.SqlWhere;
import cl.bcs.sql.utils.SqlUtil;
import cl.bcs.utils.StringUtils;

/**
 * Implementa generador SQL para SAP Hana.
 *
 * @author fpino@email.com
 *
 */
public final class SapHanaGenerator implements SqlGenerator {

    @Override
    public String getSqlText(
            final List<SqlColumn> columns) {
        final List<String> names = new ArrayList<>();
        for (final SqlColumn column : columns) {
            names.add(getSqlText(column.getName()));
        }

        return StringUtils.join(",", names);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String getSqlText(
            final SqlBetween between) {
        final String columnName = between.getColumnName();
        final String name = getSqlText(columnName);
        final Comparable a = between.getLowValue();
        final Comparable b = between.getHighValue();

        if (a == null) {
            if (b == null) {
                // TODO IS NULL no funciona en SAP HANA.
                return name + " IS NULL";
            } else {
                return name + "<=" + SqlFormatter.format(b);
            }
        } else if (b == null) {
            return name + ">=" + SqlFormatter.format(a);
        } else if (a.compareTo(b) == 0) {
            return name + "=" + SqlFormatter.format(a);
        } else {
            return name + " BETWEEN " + SqlFormatter.format(a) + " AND " + SqlFormatter.format(b);
        }
    }

    @Override
    public String getSqlText(
            final SqlDelete statement) {
        final String ret = String.format("DELETE FROM %s %s", getSqlText(statement.getTable()),
                getSqlText(statement.getWhere()));
        return ret;
    }

    @Override
    public String getSqlText(
            final SqlInsert statement) {
        final SqlValues values = statement.getValues();
        final List<SqlValue> valueList = values.getValues();
        final List<String> list0 = new ArrayList<>();
        final List<String> list1 = new ArrayList<>();
        for (final SqlValue item : valueList) {
            list0.add(getSqlText(item.getColumnName()));
            list1.add(SqlFormatter.format(item.getValue()));
        }

        final String ret = String.format("INSERT INTO %s(%s) VALUES(%s)",
                getSqlText(statement.getTable()), StringUtils.join(",", list0),
                StringUtils.join(",", list1));
        return ret;
    }

    /**
     * Obtiene texto SQL para cla&uacute;sula ORDERY BY.
     *
     * @param orderBy
     *            La cla&uacute;sula ORDER BY.
     * @return Texto SQL para cla&uacute;sula ORDERY BY.
     */
    public Object getSqlText(
            final SqlOrderBy orderBy) {
        final List<SqlOrderByCondition> orderByList = orderBy.getConditions();
        if (orderByList == null || orderByList.isEmpty()) {
            return "";
        }

        final List<String> orders = new ArrayList<>();
        for (final SqlOrderByCondition pair : orderByList) {
            orders.add(getSqlText(pair.getColumnName()) + " " + pair.getOrderCondition());
        }

        return "ORDER BY " + StringUtils.join(",", orders);
    }

    @Override
    public String getSqlText(
            final SqlSelect statement) {
        final SqlTable table = statement.getTable();

        String columnList = "*";
        if (table.getColumns() != null) {
            columnList = getSqlText(table.getColumns());
        }

        return String.format("SELECT %s FROM %s %s %s", columnList, getSqlText(table),
                getSqlText(statement.getWhere()), getSqlText(statement.getOrderBy()));
    }

    @Override
    public String getSqlText(
            final SqlTable table) {
        String ret = "";

        final String catalog = table.getCatalog();
        if (catalog != null) {
            ret += getSqlText(catalog) + ".";
        }

        final String schema = table.getSchema();
        if (schema != null) {
            ret += getSqlText(schema) + ".";
        }

        ret += getSqlText(table.getName());
        return ret;
    }

    @Override
    public String getSqlText(
            final SqlUpdate statement) {
        final SqlValues values = statement.getValues();
        final List<SqlValue> valueList = values.getValues();
        final List<String> changes = new ArrayList<>();
        for (final SqlValue item : valueList) {
            final String change = getSqlText(item.getColumnName()) + "="
                    + SqlFormatter.format(item.getValue());
            changes.add(change);
        }

        final String ret = String.format("UPDATE %s SET %s %s", getSqlText(statement.getTable()),
                StringUtils.join(",", changes), getSqlText(statement.getWhere()));
        return ret;
    }

    @Override
    public String getSqlText(
            final SqlWhere where) {
        final List<SqlBetween> whereList = where.getConditions();
        if (whereList == null || whereList.isEmpty()) {
            return "";
        }

        final List<String> conditions = new ArrayList<>();
        for (final SqlBetween between : whereList) {
            conditions.add(getSqlText(between));
        }

        return "WHERE " + StringUtils.join(" AND ", conditions);
    }

    @Override
    public String getSqlText(
            final String name) {
        return SqlUtil.toQuotedString(name);
    }

}
