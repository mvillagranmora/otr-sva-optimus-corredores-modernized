package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Datos b&aacute;sicos de una regla de validaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public class Validacion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Indica si la regla est&acute; activa.
     */
    private boolean activa;
    /**
     * C&oacute;digo de la regla.
     */
    private String codigo;

    /**
     * Descripci&oacute;n de la regla.
     */
    private String descripcion;

    /**
     * Formula de la regla.
     */
    private String formula;

    /**
     * M&eacute;todo que implementa la validaci&oacute;n.
     */
    private Method metodo;

    /**
     * Nombre del esquema.
     */
    private String nombreEsquema;

    /**
     * Severidad de la regla.
     */
    private String severidad;

    /**
     * Tipo de la regla.
     */
    private String tipo;

    /**
     * Versi&oacute;n del esquema.
     */
    private String versionEsquema;

    /**
     * Obtiene el c&oacute;digo de la regla.
     *
     * @return El c&oacute;digo de la regla.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene la descripci&oacute;n de la regla.
     *
     * @return La descripci&oacute;n de la regla.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene la formula de la regla.
     *
     * @return La formula de la regla.
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Obtiene el m&eacute;todo que implementa la validaci&oacute;n.
     *
     * @return El m&eacute;todo que implementa la validaci&oacute;n.
     */
    public Method getMetodo() {
        return metodo;
    }

    /**
     * Obtiene el mombre del esquema.
     *
     * @return El mombre del esquema.
     */
    public String getNombreEsquema() {
        return nombreEsquema;
    }

    /**
     * Obtiene la severidad de la regla.
     *
     * @return La severidad de la regla.
     */
    public String getSeveridad() {
        return severidad;
    }

    /**
     * Obtiene el tipo de la regla.
     *
     * @return El tipo de la regla.
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
     * Indica si la regla est&acute; activa.
     *
     * @return <code>true</code> si la regla est&acute; activa,
     *         <code>false</code> en caso contrario.
     */
    public boolean isActiva() {
        return activa;
    }

    /**
     * Asigna si la regla est&acute; activa.
     *
     * @param activa
     *            Si la regla est&acute; activa.
     */
    public void setActiva(final boolean activa) {
        this.activa = activa;
    }

    /**
     * Asigna el c&oacute;digo de la regla.
     *
     * @param codigo
     *            El c&oacute;digo de la regla.
     */
    public void setCodigo(final String codigo) {
        this.codigo = codigo;
    }

    /**
     * Asigna la descripci&oacute;n de la regla.
     *
     * @param descripcion
     *            La descripci&oacute;n de la regla.
     */
    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Asigna la formula de la regla.
     *
     * @param formula
     *            La formula de la regla.
     */
    public void setFormula(final String formula) {
        this.formula = formula;
    }

    /**
     * Asigna el m&eacute;todo que implementa la validaci&oacute;n.
     *
     * @param metodo
     *            El m&eacute;todo que implementa la validaci&oacute;n.
     */
    public void setMetodo(final Method metodo) {
        this.metodo = metodo;
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
     * Asigna la severidad de la regla.
     *
     * @param severidad
     *            La severidad de la regla.
     */
    public void setSeveridad(final String severidad) {
        this.severidad = severidad;
    }

    /**
     * Asigna el tipo de la regla.
     *
     * @param tipo
     *            El tipo de la regla.
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
        return "Validacion [activa=" + activa + ", codigo=" + codigo + ", descripcion="
                + descripcion + ", formula=" + formula + ", metodo=" + metodo + ", nombreEsquema="
                + nombreEsquema + ", severidad=" + severidad + ", tipo=" + tipo
                + ", versionEsquema=" + versionEsquema + "]";
    }

}
