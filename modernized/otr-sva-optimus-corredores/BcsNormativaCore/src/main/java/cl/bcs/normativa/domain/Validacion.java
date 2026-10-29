package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Datos basicos de una regla de validacion.
 * Migrado desde legacy cl.bcs.normativa.esquema.Validacion.
 */
public class Validacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean activa;
    private String codigo;
    private String descripcion;
    private String formula;
    private String nombreEsquema;
    private String severidad;
    private String tipo;
    private String versionEsquema;

    public boolean isActiva() { return activa; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public String getFormula() { return formula; }
    public String getNombreEsquema() { return nombreEsquema; }
    public String getSeveridad() { return severidad; }
    public String getTipo() { return tipo; }
    public String getVersionEsquema() { return versionEsquema; }

    public void setActiva(boolean activa) { this.activa = activa; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFormula(String formula) { this.formula = formula; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "Validacion[codigo=" + codigo + ", tipo=" + tipo + "]";
    }
}
