package cl.bcs.sql.metadata;

import java.io.Serializable;
import java.util.List;

/**
 * Metadata de un &iacute;ndice de una tabla.
 *
 * @author fpino@email.com
 *
 */
public final class SqlIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Lista de las columnas del &iacute;ndice.
     */
    private List<SqlIndexColumn> columns;

    /**
     * Nombre del &iacute;ndice.
     */
    private String name;

    /**
     * Indicador de unicidad de &iacute;ndice.
     */
    private String nonUnique;

    /**
     * Obtiene la lista de columnas del &iacute;ndice.
     *
     * @return La lista de columnas del &iacute;ndice.
     */
    public List<SqlIndexColumn> getColumns() {
        return this.columns;
    }

    /**
     * Obtiene el nombre del &iacute;ndice.
     *
     * @return El nombre del &iacute;ndice.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtiene el indicador de unicidad del &iacute;ndice.
     *
     * @return El indicador de unicidad del &iacute;ndice.
     */
    public String getNonUnique() {
        return this.nonUnique;
    }

    /**
     * Asigna la lista de columnas del &iacute;ndice.
     *
     * @param columns
     *            La lista de columnas del &iacute;ndice.
     */
    public void setColumns(final List<SqlIndexColumn> columns) {
        this.columns = columns;
    }

    /**
     * Asigna el nombre del &iacute;ndice.
     *
     * @param name
     *            El nombre del &iacute;ndice.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Asigna el indicador de unicidad del &iacute;ndice.
     *
     * @param nonUnique
     *            El indicador de unicidad del &iacute;ndice.
     */
    public void setNonUnique(final String nonUnique) {
        this.nonUnique = nonUnique;
    }

    @Override
    public String toString() {
        return "SqlIndex [columns=" + this.columns + ", name=" + this.name + ", nonUnique=" + this.nonUnique + "]";
    }

}
