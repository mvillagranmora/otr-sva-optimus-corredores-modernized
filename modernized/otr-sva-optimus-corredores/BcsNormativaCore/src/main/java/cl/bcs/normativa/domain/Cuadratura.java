package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Cuadratura de un documento normativo.
 * Migrado desde legacy cl.bcs.normativa.esquema.Cuadratura.
 */
public class Cuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    private String descripcion;
    private String nombreEsquema;
    private String tipo;
    private String versionEsquema;

    public String getDescripcion() { return descripcion; }
    public String getNombreEsquema() { return nombreEsquema; }
    public String getTipo() { return tipo; }
    public String getVersionEsquema() { return versionEsquema; }

    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "Cuadratura[tipo=" + tipo + "]";
    }
}
