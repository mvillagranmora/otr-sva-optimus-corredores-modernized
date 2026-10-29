package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Input model for spread calculation on rescate operations.
 * Migrated from: cl.bcs.fondomutuo.CalcularSpreadRescate.EntradaCalcularSpreadRescate
 */
public class CalcularSpreadRescateEntrada {

    private Long idEmpresa;
    private List<DatoSpread> datos = new ArrayList<>();

    public Long getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Long v) { this.idEmpresa = v; }
    public List<DatoSpread> getDatos() { return datos; }
    public void setDatos(List<DatoSpread> v) { this.datos = v; }

    public static class DatoSpread {
        private Long numeroCalculoSpreadRescate;
        private String fecha;
        private BigDecimal valorFactorReajuste;
        private BigDecimal costoFactorReajusteRescate;
        private BigDecimal cantidadRescate;
        private BigDecimal valorCuota;
        private String idMonedaFondo;

        public Long getNumeroCalculoSpreadRescate() { return numeroCalculoSpreadRescate; }
        public void setNumeroCalculoSpreadRescate(Long v) { this.numeroCalculoSpreadRescate = v; }
        public String getFecha() { return fecha; }
        public void setFecha(String v) { this.fecha = v; }
        public BigDecimal getValorFactorReajuste() { return valorFactorReajuste; }
        public void setValorFactorReajuste(BigDecimal v) { this.valorFactorReajuste = v; }
        public BigDecimal getCostoFactorReajusteRescate() { return costoFactorReajusteRescate; }
        public void setCostoFactorReajusteRescate(BigDecimal v) { this.costoFactorReajusteRescate = v; }
        public BigDecimal getCantidadRescate() { return cantidadRescate; }
        public void setCantidadRescate(BigDecimal v) { this.cantidadRescate = v; }
        public BigDecimal getValorCuota() { return valorCuota; }
        public void setValorCuota(BigDecimal v) { this.valorCuota = v; }
        public String getIdMonedaFondo() { return idMonedaFondo; }
        public void setIdMonedaFondo(String v) { this.idMonedaFondo = v; }
    }
}
