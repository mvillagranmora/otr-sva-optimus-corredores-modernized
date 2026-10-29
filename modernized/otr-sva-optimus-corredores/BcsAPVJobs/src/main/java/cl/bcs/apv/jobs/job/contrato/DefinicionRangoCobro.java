package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;

import cl.bcs.apv.jobs.job.JsonLegacy;

/** Legacy {@code cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro} (ConsultarCobrosPeru.java:450-463, verbatim). */
public class DefinicionRangoCobro {
    public Long IdCobroRangoOrden;
    public Long IdCobroOrden;
    public Long IdOrden;
    public Long IdGrupoCobro;
    public Long IdEmpresa;
    public String CodigoDefinicionCobro;
    public BigDecimal Hasta;
    public BigDecimal Valor;

    public String toString() {
        return JsonLegacy.texto(this);
    }
}
