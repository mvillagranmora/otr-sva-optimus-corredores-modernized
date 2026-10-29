package cl.bcs.fondomutuo.comisiones;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.fondomutuo.kit.JsonLegacy;

/** Legacy {@code pojo.TramoComision}: row of {@code Comunes_SelectTramoComision.sql} (fields by column label, as the legacy). */
public class TramoComision {

    public Long IdTramo;
    public Long IdComision;
    public Long DiasDesde;
    public Long DiasHasta;
    public BigDecimal PorcentajeComision;
    public String EstadoTramo;
    public String IdSerieFondo;
    public String Descripcion;
    public String TipoComision;
    public Date FechaVigenciaDesde;
    public Date FechaVigenciaHasta;
    public BigDecimal PorcentajeLiberado;
    public String EstadoComision;
    public Long IdEmpresa;

    public String toString() {
        return JsonLegacy.conNulos().toJson(this);
    }
}
