package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Input model for cuota/monto calculation.
 * Migrated from: cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota
 */
public class CalcularMontoNumeroCuotaEntrada {

    private Long idEmpresa;
    private List<SerieSolicitud> series = new ArrayList<>();

    public CalcularMontoNumeroCuotaEntrada() {}

    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long v) { this.idEmpresa = v; }
    public List<SerieSolicitud> getSeries() { return series; }
    public void setSeries(List<SerieSolicitud> v) { this.series = v; }

    /**
     * Per-series calculation request.
     */
    public static class SerieSolicitud {
        private Long idInterno;
        private String fecha;
        private String idSerieFondo;
        private String calculoPorMontoCuota;
        private BigDecimal monto;
        private BigDecimal numeroCuotas;
        private String idMonedaFondo;
        private String idMonedaInversion;
        private String tipoSolicitud;
        private String tipoFactorAjuste;
        private Long idEmpresa;
        private BigDecimal porcentaje;
        private String idTipoMovimientoFondoMutuo = "";

        public Long getIdInterno() { return idInterno; }
        public void setIdInterno(Long v) { this.idInterno = v; }
        public String getFecha() { return fecha; }
        public void setFecha(String v) { this.fecha = v; }
        public String getIdSerieFondo() { return idSerieFondo; }
        public void setIdSerieFondo(String v) { this.idSerieFondo = v; }
        public String getCalculoPorMontoCuota() { return calculoPorMontoCuota; }
        public void setCalculoPorMontoCuota(String v) { this.calculoPorMontoCuota = v; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal v) { this.monto = v; }
        public BigDecimal getNumeroCuotas() { return numeroCuotas; }
        public void setNumeroCuotas(BigDecimal v) { this.numeroCuotas = v; }
        public String getIdMonedaFondo() { return idMonedaFondo; }
        public void setIdMonedaFondo(String v) { this.idMonedaFondo = v; }
        public String getIdMonedaInversion() { return idMonedaInversion; }
        public void setIdMonedaInversion(String v) { this.idMonedaInversion = v; }
        public String getTipoSolicitud() { return tipoSolicitud; }
        public void setTipoSolicitud(String v) { this.tipoSolicitud = v; }
        public String getTipoFactorAjuste() { return tipoFactorAjuste; }
        public void setTipoFactorAjuste(String v) { this.tipoFactorAjuste = v; }
        public Long getIdEmpresa() { return idEmpresa; }
        public void setIdEmpresa(Long v) { this.idEmpresa = v; }
        public BigDecimal getPorcentaje() { return porcentaje; }
        public void setPorcentaje(BigDecimal v) { this.porcentaje = v; }
        public String getIdTipoMovimientoFondoMutuo() { return idTipoMovimientoFondoMutuo; }
        public void setIdTipoMovimientoFondoMutuo(String v) { this.idTipoMovimientoFondoMutuo = v; }
    }
}
