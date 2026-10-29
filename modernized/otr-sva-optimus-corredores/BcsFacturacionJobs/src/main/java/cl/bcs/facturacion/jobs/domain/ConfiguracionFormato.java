package cl.bcs.facturacion.jobs.domain;

import java.util.List;

/**
 * Mutable container for a file-format configuration (mirrors legacy FormatoArchivo).
 *
 * <ul>
 *   <li>{@code rellenarCampo} — "S" to pad fields to their full width, "N" otherwise</li>
 *   <li>{@code separadorDecimales} — decimal separator character (e.g. "." or ",")</li>
 *   <li>{@code campos} — ordered list of field format descriptors</li>
 * </ul>
 */
public class ConfiguracionFormato {

    private String rellenarCampo;
    private String separadorDecimales;
    private List<CampoFormato> campos;

    public ConfiguracionFormato() {}

    public ConfiguracionFormato(String rellenarCampo, String separadorDecimales, List<CampoFormato> campos) {
        this.rellenarCampo = rellenarCampo;
        this.separadorDecimales = separadorDecimales;
        this.campos = campos;
    }

    public String getRellenarCampo() {
        return rellenarCampo;
    }

    public void setRellenarCampo(String rellenarCampo) {
        this.rellenarCampo = rellenarCampo;
    }

    public String getSeparadorDecimales() {
        return separadorDecimales;
    }

    public void setSeparadorDecimales(String separadorDecimales) {
        this.separadorDecimales = separadorDecimales;
    }

    public List<CampoFormato> getCampos() {
        return campos;
    }

    public void setCampos(List<CampoFormato> campos) {
        this.campos = campos;
    }
}
