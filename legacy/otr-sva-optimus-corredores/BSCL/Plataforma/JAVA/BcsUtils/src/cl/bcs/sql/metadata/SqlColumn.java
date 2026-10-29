package cl.bcs.sql.metadata;

import java.io.Serializable;

/**
 * Metadata de una columna de una tabla.
 *
 * @author fpino@email.com
 *
 */
public final class SqlColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Cat&aacute;logo de la tabla.
     */
    private String catalog;

    /**
     * Valor por defecto de la columna.
     */
    private String defaultValue;

    /**
     * Nombre de la columna.
     */
    private String name;

    /**
     * Indicador de si la columna permite nulls.
     */
    private boolean nullable;

    /**
     * Secuencia de la columna en la tabla.
     */
    private int ordinalPosition;

    /**
     * Comentarios de la columna.
     */
    private String remarks;

    /**
     * Escala si el tipo de datos es num&eacute;rico.
     */
    private int scale;

    /**
     * Schema de la base de datos.
     */
    private String schema;

    /**
     * Largo del tipo de dato.
     */
    private int size;

    /**
     * Nombre de la tabla.
     */
    private String tableName;

    /**
     * Tipo de la tabla.
     */
    private String typeName;

    /**
     * Obtiene el cat&aacute;logo.
     *
     * @return El cat&aacute;logo.
     */
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * Obtiene el valor por defecto de la columna.
     *
     * @return El valor por defecto de la columna.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Obtiene el nombre de la columna.
     *
     * @return El nombre de la columna.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtiene la secuencia de la columna.
     *
     * @return La secuencia de la columna.
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * Obtiene los comentarios de la columna.
     *
     * @return Los comentarios de la columna.
     */
    public String getRemarks() {
        return this.remarks;
    }

    /**
     * Obtiene la escala para un tipo de dato num&eacute;rico.
     *
     * @return La escala para un tipo de dato num&eacute;rico.
     */
    public int getScale() {
        return this.scale;
    }

    /**
     * Obtiene schema de la base de datos.
     *
     * @return El schema de la base de datos.
     */
    public String getSchema() {
        return this.schema;
    }

    /**
     * Obtiene el largo del tipo de dato.
     *
     * @return El largo del tipo de dato.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Obtiene el nombre de la tabla.
     *
     * @return El nombre de la tabla.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Obtiene el nombre del tipo de dato.
     *
     * @return El nombre del tipo de dato.
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * Indica si la columna permite nulls.
     *
     * @return <code>true</code> si la columna permite nulls, <code>false</code>
     *         en caso contrario.
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * Asigna el cat&aacute;logo.
     *
     * @param catalog
     *            El cat&aacute;logo.
     */
    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Asigna el valor por defecto de la columna.
     *
     * @param defaultValue
     *            El valor por defecto de la columna.
     */
    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Asigna el nombre de la columna.
     *
     * @param name
     *            El nombre de la columna.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Asigna el indicador de si la columna permite nulls.
     *
     * @param nullable
     *            El indicador de si la columna permite nulls.
     */
    public void setNullable(final boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Asigna la secuencia de la columna.
     *
     * @param ordinalPosition
     *            La secuencia de la columna.
     */
    public void setOrdinalPosition(final int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    /**
     * Asigna los comentarios de la columna.
     *
     * @param remarks
     *            Los comentarios de la columna.
     */
    public void setRemarks(final String remarks) {
        this.remarks = remarks;
    }

    /**
     * Asigna la escala para un tipo de dato num&eacute;rico.
     *
     * @param scale
     *            La escala para un tipo de dato num&eacute;rico.
     */
    public void setScale(final int scale) {
        this.scale = scale;
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
     * Asigna el largo del tipo de dato.
     *
     * @param size
     *            El largo del tipo de dato.
     */
    public void setSize(final int size) {
        this.size = size;
    }

    /**
     * Asigna el nombre de la tabla.
     *
     * @param tableName
     *            El nombre de la tabla.
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    /**
     * Asigna el nombre del tipo de dato.
     *
     * @param typeName
     *            El nombre del tipo de dato.
     */
    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "SqlColumn [catalog=" + this.catalog + ", defaultValue=" + this.defaultValue + ", name=" + this.name
                + ", nullable=" + this.nullable + ", ordinalPosition=" + this.ordinalPosition + ", remarks="
                + this.remarks + ", scale=" + this.scale + ", schema=" + this.schema + ", size=" + this.size
                + ", tableName=" + this.tableName + ", typeName=" + this.typeName + "]";
    }

}
