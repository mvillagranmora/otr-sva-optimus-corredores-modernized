package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Espec&iacute;fica campos para cuadratura.
 *
 * @author fpino@email.com
 *
 */
public class CampoCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre del campo.
     */
    private String nombreCampo;

    /**
     * Nombre de la cuadratura.
     */
    private String nombreCuadratura;

    /**
     * Nombre del esquema.
     */
    private String nombreEsquema;

    /**
     * Orden del campo en la cuadratura.
     */
    private int orden;

    /**
     * Versi&oacute;n del esquema.
     */
    private String versionEsquema;

    /**
     * Obtiene el nombre del campo.
     *
     * @return El nombre del campo.
     */
    public String getNombreCampo() {
        return nombreCampo;
    }

    /**
     * Obtiene el nombre de la cuadratura.
     *
     * @return El nombre de la cuadratura.
     */
    public String getNombreCuadratura() {
        return nombreCuadratura;
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
     * Obtiene el orden del campo en la cuadratura.
     *
     * @return El orden del campo en la cuadratura.
     */
    public int getOrden() {
        return orden;
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
     * Asigna el nombre del campo.
     *
     * @param nombreCampo
     *            El nombre del campo.
     */
    public void setNombreCampo(final String nombreCampo) {
        this.nombreCampo = nombreCampo;
    }

    /**
     * Asigna el nombre de la cuadratura.
     *
     * @param nombreCuadratura
     *            El nombre de la cuadratura.
     */
    public void setNombreCuadratura(final String nombreCuadratura) {
        this.nombreCuadratura = nombreCuadratura;
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
     * Asigna el orden del campo en la cuadratura.
     *
     * @param orden
     *            El orden del campo en la cuadratura.
     */
    public void setOrden(final int orden) {
        this.orden = orden;
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
        return "CampoCuadratura [nombreCampo=" + nombreCampo + ", nombreCuadratura="
                + nombreCuadratura + ", nombreEsquema=" + nombreEsquema + ", orden=" + orden
                + ", versionEsquema=" + versionEsquema + "]";
    }

}
