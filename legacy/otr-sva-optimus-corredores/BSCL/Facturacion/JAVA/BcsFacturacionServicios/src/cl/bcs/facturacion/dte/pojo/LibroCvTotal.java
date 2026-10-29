
package cl.bcs.facturacion.dte.pojo;

import java.math.BigDecimal;

public class LibroCvTotal {

    private Long tipoDocumento;
    private Integer cantidadDocumentos;
    private Integer cantidadAnulados;
    private BigDecimal totalMontoNeto;
    private BigDecimal totalMontoExento;
    private BigDecimal totalMontoIva;
    private BigDecimal totalMontoTotal;
    

       
    
    
    /**
     * 
     * @return
     *     The tipoDocumento
     */
    public Long getTipoDocumento() {
        return tipoDocumento;
    }

    /**
     * 
     * @param tipoDocumento
     *     The tipoDocumento
     */
    public void setTipoDocumento(Long tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    
    
    /**
     * 
     * @return
     *     The cantidadDocumentos
     */
    public Integer getCantidadDocumentos() {
        return cantidadDocumentos;
    }

    /**
     * 
     * @param cantidadDocumentos
     *     The cantidadDocumentos
     */
    public void setCantidadDocumentos(Integer cantidadDocumentos) {
        this.cantidadDocumentos = cantidadDocumentos;
    }
    
    
    /**
     * 
     * @return
     *     The cantidadAnulados
     */
    public Integer getCantidadAnulados() {
        return cantidadAnulados;
    }

    /**
     * 
     * @param cantidadAnulados
     *     The cantidadAnulados
     */
    public void setCantidadAnulados(Integer cantidadAnulados) {
        this.cantidadAnulados = cantidadAnulados;
    }
    
    
    /**
     * 
     * @return
     *     The totalMontoNeto
     */
    public BigDecimal getTotalMontoNeto() {
        return totalMontoNeto;
    }

    /**
     * 
     * @param totalMontoNeto
     *     The totalMontoNeto
     */
    public void setTotalMontoNeto(BigDecimal totalMontoNeto) {
        this.totalMontoNeto = totalMontoNeto;
    }
    
    
    /**
     * 
     * @return
     *     The totalMontoExento
     */
    public BigDecimal getTotalMontoExento() {
        return totalMontoExento;
    }

    /**
     * 
     * @param totalMontoExento
     *     The totalMontoExento
     */
    public void setTotalMontoExento(BigDecimal totalMontoExento) {
        this.totalMontoExento = totalMontoExento;
    }
    
    
    /**
     * 
     * @return
     *     The totalMontoIva
     */
    public BigDecimal getTotalMontoIva() {
        return totalMontoIva;
    }

    /**
     * 
     * @param totalMontoIva
     *     The totalMontoIva
     */
    public void setTotalMontoIva(BigDecimal totalMontoIva) {
        this.totalMontoIva = totalMontoIva;
    } 
    
    
    
    /**
     * 
     * @return
     *     The totalMontoTotal
     */
    public BigDecimal getTotalMontoTotal() {
        return totalMontoTotal;
    }

    /**
     * 
     * @param totalMontoTotal
     *     The totalMontoTotal
     */
    public void setTotalMontoTotal(BigDecimal totalMontoTotal) {
        this.totalMontoTotal = totalMontoTotal;
    } 
           
    
}
