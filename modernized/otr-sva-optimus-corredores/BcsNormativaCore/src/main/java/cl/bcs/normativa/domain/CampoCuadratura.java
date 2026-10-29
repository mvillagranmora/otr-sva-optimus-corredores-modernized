package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Campo de una cuadratura normativa.
 * Migrado desde legacy cl.bcs.normativa.esquema.CampoCuadratura.
 */
public class CampoCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreCampo;
    private String nombreCuadratura;
    private String nombreEsquema;
    private int orden;
    private String versionEsquema;

    public String getNombreCampo() { return nombreCampo; }
    public String getNombreCuadratura() { return nombreCuadratura; }
    public String getNombreEsquema() { return nombreEsquema; }
    public int getOrden() { return orden; }
    public String getVersionEsquema() { return versionEsquema; }

    public void setNombreCampo(String nombreCampo) { this.nombreCampo = nombreCampo; }
    public void setNombreCuadratura(String nombreCuadratura) { this.nombreCuadratura = nombreCuadratura; }
    public void setNombreEsquema(String nombreEsquema) { this.nombreEsquema = nombreEsquema; }
    public void setOrden(int orden) { this.orden = orden; }
    public void setVersionEsquema(String versionEsquema) { this.versionEsquema = versionEsquema; }

    @Override
    public String toString() {
        return "CampoCuadratura[nombreCampo=" + nombreCampo + ", cuadratura=" + nombreCuadratura + "]";
    }
}
