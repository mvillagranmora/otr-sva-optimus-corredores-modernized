package cl.bcs.fondomutuo.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Output model for cuota/monto calculation.
 * Migrated from: cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota
 */
public class CalcularMontoNumeroCuotaSalida {

    private List<ResultadoCuota> resultados = new ArrayList<>();

    public List<ResultadoCuota> getResultados() { return resultados; }
    public void setResultados(List<ResultadoCuota> v) { this.resultados = v; }

    public static class ResultadoCuota {
        private Long idInterno;
        private String tipoSolicitud;
        private BigDecimal monto;
        private BigDecimal numeroCuotas;
        private BigDecimal valorCuota;
        private BigDecimal montoDestino;
        private BigDecimal valorParidad;
        private BigDecimal valorFactorAjuste;
        private BigDecimal costoEnFactorAjuste;
        private String idMonedaSistema;
        private BigDecimal montoMonedaSistema;
        private String resultado;
        private String codigoMensaje;
        private BigDecimal porcentaje;
        private Long decimalesValorCuota;
        private Long decimalesNumeroCuotas;
        private String fechaValorCuota;

        public Long getIdInterno() { return idInterno; }
        public void setIdInterno(Long v) { this.idInterno = v; }
        public String getTipoSolicitud() { return tipoSolicitud; }
        public void setTipoSolicitud(String v) { this.tipoSolicitud = v; }
        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal v) { this.monto = v; }
        public BigDecimal getNumeroCuotas() { return numeroCuotas; }
        public void setNumeroCuotas(BigDecimal v) { this.numeroCuotas = v; }
        public BigDecimal getValorCuota() { return valorCuota; }
        public void setValorCuota(BigDecimal v) { this.valorCuota = v; }
        public BigDecimal getMontoDestino() { return montoDestino; }
        public void setMontoDestino(BigDecimal v) { this.montoDestino = v; }
        public BigDecimal getValorParidad() { return valorParidad; }
        public void setValorParidad(BigDecimal v) { this.valorParidad = v; }
        public BigDecimal getValorFactorAjuste() { return valorFactorAjuste; }
        public void setValorFactorAjuste(BigDecimal v) { this.valorFactorAjuste = v; }
        public BigDecimal getCostoEnFactorAjuste() { return costoEnFactorAjuste; }
        public void setCostoEnFactorAjuste(BigDecimal v) { this.costoEnFactorAjuste = v; }
        public String getIdMonedaSistema() { return idMonedaSistema; }
        public void setIdMonedaSistema(String v) { this.idMonedaSistema = v; }
        public BigDecimal getMontoMonedaSistema() { return montoMonedaSistema; }
        public void setMontoMonedaSistema(BigDecimal v) { this.montoMonedaSistema = v; }
        public String getResultado() { return resultado; }
        public void setResultado(String v) { this.resultado = v; }
        public String getCodigoMensaje() { return codigoMensaje; }
        public void setCodigoMensaje(String v) { this.codigoMensaje = v; }
        public BigDecimal getPorcentaje() { return porcentaje; }
        public void setPorcentaje(BigDecimal v) { this.porcentaje = v; }
        public Long getDecimalesValorCuota() { return decimalesValorCuota; }
        public void setDecimalesValorCuota(Long v) { this.decimalesValorCuota = v; }
        public Long getDecimalesNumeroCuotas() { return decimalesNumeroCuotas; }
        public void setDecimalesNumeroCuotas(Long v) { this.decimalesNumeroCuotas = v; }
        public String getFechaValorCuota() { return fechaValorCuota; }
        public void setFechaValorCuota(String v) { this.fechaValorCuota = v; }
    }
}
