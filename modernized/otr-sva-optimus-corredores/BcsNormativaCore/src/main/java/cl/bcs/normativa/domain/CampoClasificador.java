package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Campo de un clasificador normativo.
 * Migrado desde legacy cl.bcs.normativa.esquema.CampoClasificador.
 */
public class CampoClasificador implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreCampo;
    private String nombreEsquema;
    private String tipoClasificador;
    private Comparable<?> valorAsComparable;
    private String valorClasificador;
    private String versionEsquema;

    public String getNombreCampo() { return nombreCampo; }
    public String getNombreEsquema() { return nombreEsquema; }
    public String getTipoClasificador() { return tipoClasificador; }
    public Comparable<?> getValorAsComparable() { return valorAsComparable; }
    public String getValorClasificador() { return valorClasificador; }
    public String getVersionEsquema() { return versionEsquema; }

    public void setNombreCampo(String nombreCampo) { this.nombreCampo = nombreCampo; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setTipoClasificador(String tipoClasificador) { this.tipoClasificador = tipoClasificador; }
    public void setValorAsComparable(Comparable<?> valorAsComparable) { this.valorAsComparable = valorAsComparable; }
    public void setValorClasificador(String valorClasificador) { this.valorClasificador = valorClasificador; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "CampoClasificador[nombreCampo=" + nombreCampo + ", tipoClasificador=" + tipoClasificador + "]";
    }
}
