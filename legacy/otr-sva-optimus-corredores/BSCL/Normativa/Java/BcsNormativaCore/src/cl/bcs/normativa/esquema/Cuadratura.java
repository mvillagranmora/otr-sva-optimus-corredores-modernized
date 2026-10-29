package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Espec&iacute;fica cuadratura.
 *
 * @author fpino@email.com
 *
 */
public class Cuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Descripci&oacute;n de la cuadratura.
     */
    private String descripcion;

    /**
     * Nombre del esquema.
     */
    private String nombreEsquema;

    /**
     * Tipo de cuadratura.
     */
    private String tipo;

    /**
     * Versi&oacute;n del esquema.
     */
    private String versionEsquema;

    /**
     * Obtiene la descripci&oacute;n de la cuadratura.
     *
     * @return La descripci&oacute;n de la cuadratura.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el nombre del esquema.
     *
     * @return El nombre del esquema.
     */
    public String getNombreEsquema() {
        return nombreEsquema;
    }

    /**
     * Obtiene el tipo de cuadratura.
     *
     * @return El tipo de cuadratura.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Obtiene la versi&oacute;n del esquema.
     *
     * @return La versi&oacute;n del esquema.
     */
    public String getVersionEsquema() {
        return versionEsquema;
    }

    /**
     * Asigna la descripci&oacute;n de la cuadratura.
     *
     * @param descripcion
     *            La descripci&oacute;n de la cuadratura.
     */
    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Asigna el nombre del esquema.
     *
     * @param nombreEsquema
     *            El nombre del esquema.
     */
    public void setNombreEsquema(final String nombreEsquema) {
        this.nombreEsquema = nombreEsquema;
    }

    /**
     * Asigna el tipo de cuadratura.
     *
     * @param tipo
     *            El tipo de cuadratura.
     */
    public void setTipo(final String tipo) {
        this.tipo = tipo;
    }

    /**
     * Asigna la versi&oacute;n del esquema.
     *
     * @param versionEsquema
     *            La versi&oacute;n del esquema.
     */
    public void setVersionEsquema(final String versionEsquema) {
        this.versionEsquema = versionEsquema;
    }

    @Override
    public String toString() {
        return "Cuadratura [descripcion=" + descripcion + ", nombreEsquema=" + nombreEsquema
                + ", tipo=" + tipo + ", versionEsquema=" + versionEsquema + "]";
    }

}
