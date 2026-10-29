package cl.bcs.sql.metadata;

import java.io.Serializable;

/**
 * Metadata de la columna de un &iacute;ndice de una tabla.
 *
 * @author fpino@email.com
 *
 */
public final class SqlIndexColumn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre de la columna del &iacute;ndice.
     */
    private String name;

    /**
     * Forma de ordenamiento de la columna.
     */
    private String ordering;

    /**
     * Secuencia de la columna en el &iacute;ndice.
     */
    private int ordinalPosition;

    /**
     * Obtiene el nombre de la columna del &iacute;ndice.
     *
     * @return El nombre de la columna del &iacute;ndice.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene la forma de ordenamiento de la columna.
     *
     * @return La forma de ordenamiento de la columna.
     */
    public String getOrdering() {
        return ordering;
    }

    /**
     * Obtiene la secuencia de la columna en el &iacute;ndice.
     *
     * @return La secuencia de la columna en el &iacute;ndice.
     */
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    /**
     * Asigna el nombre de la columna del &iacute;ndice.
     *
     * @param name
     *            El nombre de la columna del &iacute;ndice.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Asigna la forma de ordenamiento de la columna.
     *
     * @param ordering
     *            La forma de ordenamiento de la columna.
     */
    public void setOrdering(final String ordering) {
        this.ordering = ordering;
    }

    /**
     * Asigna la secuencia de la columna en el &iacute;ndice.
     *
     * @param ordinalPosition
     *            La secuencia de la columna en el &iacute;ndice.
     */
    public void setOrdinalPosition(final int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public String toString() {
        return "SqlIndexColumn [name=" + name + ", ordering=" + ordering + ", ordinalPosition="
                + ordinalPosition + "]";
    }

}
