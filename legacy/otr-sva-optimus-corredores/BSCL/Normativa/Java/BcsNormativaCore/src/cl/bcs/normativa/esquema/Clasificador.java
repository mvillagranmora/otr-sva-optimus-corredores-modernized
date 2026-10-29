package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.List;

/**
 * Espec&iacute;fica clasificador para cuadratura.
 *
 * @author fpino@email.com
 *
 */
public class Clasificador implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Lista de campos clasificadores.
     */
    private List<CampoClasificador> camposClasificador;

    /**
     * Descripci&oacute;n del clasificador.
     */
    private String descripcion;

    /**
     * Nombre del esquema.
     */
    private String nombreEsquema;

    /**
     * Nombre del clasificador.
     */
    private String tipo;

    /**
     * Versi&oacute;n del esquema.
     */
    private String versionEsquema;

    /**
     * Obtiene los campos del clasificador.
     *
     * @return Los campos del clasificador.
     */
    public List<CampoClasificador> getCamposClasificador() {
        return camposClasificador;
    }

    /**
     * Obtiene la descripci&oacute;n del clasificador.
     *
     * @return La descripci&oacute;n del clasificador.
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
     * Obtiene el tipo de clasificador.
     *
     * @return El tipo de clasificador.
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
     * Asigna los campos del clasificador.
     *
     * @param camposClasificador
     *            Los campos del clasificador.
     */
    public void setCamposClasificador(final List<CampoClasificador> camposClasificador) {
        this.camposClasificador = camposClasificador;
    }

    /**
     * Asigna la descripci&oacute;n del clasificador.
     *
     * @param descripcion
     *            La descripci&oacute;n del clasificador.
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
     * Asigna el tipo de clasificador.
     *
     * @param tipo
     *            El tipo de clasificador.
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
        return "Clasificador [camposClasificador=" + camposClasificador + ", descripcion="
                + descripcion + ", nombreEsquema=" + nombreEsquema + ", tipo=" + tipo
                + ", versionEsquema=" + versionEsquema + "]";
    }

}
