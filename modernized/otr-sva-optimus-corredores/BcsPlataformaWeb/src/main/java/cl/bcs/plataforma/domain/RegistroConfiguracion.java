package cl.bcs.plataforma.domain;

/**
 * Configuration registry entry — used by ConfiguracionController.
 * Source: cl.bcs.plataforma.services.config.pojo.RegistroConfiguracion
 */
public class RegistroConfiguracion {

    private Long   idEmpresa;
    private String codigoParametro;
    private String valor;
    private String modulo;

    public RegistroConfiguracion() {}

    public RegistroConfiguracion(Long idEmpresa, String modulo, String codigoParametro) {
        this.idEmpresa       = idEmpresa;
        this.modulo          = modulo;
        this.codigoParametro = codigoParametro;
    }

    /** Cache key: idEmpresa_modulo_codigoParametro */
    @Override
    public String toString() {
        return idEmpresa + "_" + modulo + "_" + codigoParametro;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getIdEmpresa()                    { return idEmpresa; }
    public void setIdEmpresa(Long v)              { this.idEmpresa = v; }

    public String getCodigoParametro()            { return codigoParametro; }
    public void   setCodigoParametro(String v)    { this.codigoParametro = v; }

    public String getValor()                      { return valor; }
    public void   setValor(String v)              { this.valor = v; }

    public String getModulo()                     { return modulo; }
    public void   setModulo(String v)             { this.modulo = v; }
}
