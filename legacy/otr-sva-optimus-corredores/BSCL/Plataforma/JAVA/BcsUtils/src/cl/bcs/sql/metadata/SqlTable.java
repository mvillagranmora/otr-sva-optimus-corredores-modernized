package cl.bcs.sql.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata de una tabla SQL.
 *
 * @author fpino@email.com
 *
 */
public final class SqlTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Cat&aacute;logo de la tabla.
     */
    private String catalog;

    /**
     * Lista de columnas de la tabla.
     */
    private List<SqlColumn> columns;

    /**
     * Lista de indices de la tabla.
     */
    private List<SqlIndex> indexes;

    /**
     * Nombre de la tabla.
     */
    private final String name;

    /**
     * Llave primaria de la tabla.
     */
    private SqlIndex primaryKey;

    /**
     * Comentarios de la tabla.
     */
    private String remarks;

    /**
     * Schema de la base de datos.
     */
    private String schema;

    /**
     * Tipo de la tabla.
     */
    private String type;

    /**
     * Constructor a partir del nombre de la tabla.
     *
     * @param name
     *            El nombre de la tabla.
     */
    public SqlTable(final String name) {
        this.name = name;
    }

    /**
     * Chequea si la tabla contiene una columna del nombre dado.
     *
     * @param columnName
     *            El nombre de la columna.
     * @return <code>true</code> si la tabla contiene una columna del nombre
     *         dado, <code>false</code> en caso contrario.
     */
    public boolean containsColumn(final String columnName) {
        for (final SqlColumn column : this.getColumns()) {
            if (column.getName().equals(columnName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene el catalogo de la tabla.
     *
     * @return El catalogo de la tabla.
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Obtiene la lista de columnas de la tabla.
     *
     * @return La lista de columnas de la tabla.
     */
    public List<SqlColumn> getColumns() {
        return columns;
    }

    /**
     * Obtiene la lista de columnas con valores por defecto de la tabla.
     *
     * @return La lista de columnas con valores por defecto de la tabla.
     */
    public List<SqlColumn> getColumnsWithDefault() {
        final List<SqlColumn> columnsWithDefault = new ArrayList<>();
        for (final SqlColumn column : this.columns) {
            if (column.getDefaultValue() != null) {
                columnsWithDefault.add(column);
            }
        }

        return columnsWithDefault;
    }

    /**
     * Obtiene la lista de indices de la tabla.
     *
     * @return La lista de indices de la tabla.
     */
    public List<SqlIndex> getIndexes() {
        return indexes;
    }

    /**
     * Obtiene el nombre de la tabla.
     *
     * @return El nombre de la tabla.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene la llave primaria de la tabla.
     *
     * @return La llave primaria de la tabla o, <code>null</code> si la tabla no
     *         tiene llave primaria.
     */
    public SqlIndex getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Obtiene los comentarios de la tabla.
     *
     * @return Los comentarios de la tabla.
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Obtiene el schema de la base de datos.
     *
     * @return El schema de la base de datos.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Obtiene el tipo de la tabla.
     *
     * @return El tipo de la tabla.
     */
    public String getType() {
        return type;
    }

    /**
     * Asigna el catalogo de la tabla.
     *
     * @param catalog
     *            El catalogo de la tabla.
     */
    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Asigna la lista de columnas de la tabla.
     *
     * @param columns
     *            La lista de columnas de la tabla.
     */
    public void setColumns(final List<SqlColumn> columns) {
        this.columns = columns;
    }

    /**
     * Asigna la lista de indices de la tabla.
     *
     * @param indexes
     *            La lista de indices de la tabla.
     */
    public void setIndexes(final List<SqlIndex> indexes) {
        this.indexes = indexes;
    }

    /**
     * Asigna la llave primaria de la tabla.
     *
     * @param primaryKey
     *            La llave primaria de la tabla.
     */
    public void setPrimaryKey(final SqlIndex primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Asigna los comentarios de la tabla.
     *
     * @param remarks
     *            Los comentarios de la tabla.
     */
    public void setRemarks(final String remarks) {
        this.remarks = remarks;
    }

    /**
     * Asigna el schema de la base de datos.
     *
     * @param schema
     *            El schema de la base de datos.
     */
    public void setSchema(final String schema) {
        this.schema = schema;
    }

    /**
     * Asigna el tipo de la tabla.
     *
     * @param type
     *            El tipo de la tabla.
     */
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SqlTable [catalog=" + catalog + ", columns=" + columns + ", indexes=" + indexes
                + ", name=" + name + ", primaryKey=" + primaryKey + ", remarks=" + remarks
                + ", schema=" + schema + ", type=" + type + "]";
    }

}
