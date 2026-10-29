package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Output model for spread calculation on rescate operations.
 * Migrated from: cl.bcs.fondomutuo.CalcularSpreadRescate.SalidaCalcularSpreadRescate
 */
public class CalcularSpreadRescateSalida {

    private Long erroresContador = 0L;
    private List<String> mensajes = new ArrayList<>();
    private List<ResultadoSpread> resultados = new ArrayList<>();

    public Long getErroresContador() { return erroresContador; }
    public void setErroresContador(Long v) { this.erroresContador = v; }
    public List<String> getMensajes() { return mensajes; }
    public void setMensajes(List<String> v) { this.mensajes = v; }
    public List<ResultadoSpread> getResultados() { return resultados; }
    public void setResultados(List<ResultadoSpread> v) { this.resultados = v; }

    public static class ResultadoSpread {
        private Long numeroCalculoSpreadRescate;
        private BigDecimal valorFactorReajuste;
        private BigDecimal costoFactorReajusteRescate;
        private BigDecimal cantidadRescate;
        private BigDecimal valorCuota;
        private BigDecimal spread;

        public Long getNumeroCalculoSpreadRescate() { return numeroCalculoSpreadRescate; }
        public void setNumeroCalculoSpreadRescate(Long v) { this.numeroCalculoSpreadRescate = v; }
        public BigDecimal getValorFactorReajuste() { return valorFactorReajuste; }
        public void setValorFactorReajuste(BigDecimal v) { this.valorFactorReajuste = v; }
        public BigDecimal getCostoFactorReajusteRescate() { return costoFactorReajusteRescate; }
        public void setCostoFactorReajusteRescate(BigDecimal v) { this.costoFactorReajusteRescate = v; }
        public BigDecimal getCantidadRescate() { return cantidadRescate; }
        public void setCantidadRescate(BigDecimal v) { this.cantidadRescate = v; }
        public BigDecimal getValorCuota() { return valorCuota; }
        public void setValorCuota(BigDecimal v) { this.valorCuota = v; }
        public BigDecimal getSpread() { return spread; }
        public void setSpread(BigDecimal v) { this.spread = v; }
    }
}
