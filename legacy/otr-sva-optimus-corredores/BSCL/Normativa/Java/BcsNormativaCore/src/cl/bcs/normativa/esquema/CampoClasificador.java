package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Espec&iacute;fica campos para clasificador.
 *
 * @author fpino@email.com
 *
 */
public class CampoClasificador implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nombre del campo.
     */
    private String nombreCampo;

    /**
     * Nombre del esquema.
     */
    private String nombreEsquema;

    /**
     * Tipo del clasificador.
     */
    private String tipoClasificador;

    /**
     * Valor del clasificador como objeto comparable.
     */
    private Comparable<?> valorAsComparable;

    /**
     * Valor del clasificador.
     */
    private String valorClasificador;

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
     * Obtiene el nombre del esquema.
     *
     * @return El nombre del esquema.
     */
    public String getNombreEsquema() {
        return nombreEsquema;
    }

    /**
     * Obtiene el tipo del clasificador.
     *
     * @return El tipo del clasificador.
     */
    public String getTipoClasificador() {
        return tipoClasificador;
    }

    /**
     * Obtiene el valor del clasificador como objeto comparable.
     *
     * @return El valor del clasificador como objeto comparable.
     */
    public Comparable<?> getValorAsComparable() {
        return valorAsComparable;
    }

    /**
     * Obtiene el valor del clasificador.
     *
     * @return El valor del clasificador.
     */
    public String getValorClasificador() {
        return valorClasificador;
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
     * Asigna el nombre del esquema.
     *
     * @param nombreEsquema
     *            El nombre del esquema.
     */
    public void setNombreEsquema(final String nombreEsquema) {
        this.nombreEsquema = nombreEsquema;
    }

    /**
     * Asigna el tipo del clasificador.
     *
     * @param tipoClasificador
     *            El tipo del clasificador.
     */
    public void setTipoClasificador(final String tipoClasificador) {
        this.tipoClasificador = tipoClasificador;
    }

    /**
     * Asigna el valor del clasificador como objeto comparable.
     *
     * @param valorAsComparable
     *            El valor del clasificador como objeto comparable.
     */
    public void setValorAsComparable(final Comparable<?> valorAsComparable) {
        this.valorAsComparable = valorAsComparable;
    }

    /**
     * Asigna el valor del clasificador.
     *
     * @param valorClasificador
     *            El valor del clasificador.
     */
    public void setValorClasificador(final String valorClasificador) {
        this.valorClasificador = valorClasificador;
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
        return "CampoClasificador [nombreCampo=" + nombreCampo + ", nombreEsquema=" + nombreEsquema
                + ", tipoClasificador=" + tipoClasificador + ", valorAsComparable="
                + valorAsComparable + ", valorClasificador=" + valorClasificador
                + ", versionEsquema=" + versionEsquema + "]";
    }

}
