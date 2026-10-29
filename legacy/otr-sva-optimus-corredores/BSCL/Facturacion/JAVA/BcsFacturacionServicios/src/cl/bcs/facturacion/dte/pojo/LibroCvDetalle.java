
package cl.bcs.facturacion.dte.pojo;

import java.math.BigDecimal;

public class LibroCvDetalle {

    private Long tipoDocumento;
    private Long folioDocumento;
    private String fechaDocumento;
    private String rutDocumento;
    private String razonSocial;
    private BigDecimal montoNeto;
    private BigDecimal montoExento;
    private BigDecimal montoIva;
    private BigDecimal montoTotal;
    

    
    
    
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
     *     The folioDocumento
     */
    public Long getFolioDocumento() {
        return folioDocumento;
    }

    /**
     * 
     * @param folioDocumento
     *     The folioDocumento
     */
    public void setFolioDocumento(Long folioDocumento) {
        this.folioDocumento = folioDocumento;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The fechaDocumento
     */
    public String getFechaDocumento() {
        return fechaDocumento;
    }

    /**
     * 
     * @param fechaDocumento
     *     The fechaDocumento
     */
    public void setFechaDocumento(String fechaDocumento) {
        this.fechaDocumento = fechaDocumento;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The rutDocumento
     */
    public String getRutDocumento() {
        return rutDocumento;
    }

    /**
     * 
     * @param rutDocumento
     *     The rutDocumento
     */
    public void setRutDocumento(String rutDocumento) {
        this.rutDocumento = rutDocumento;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The razonSocial
     */
    public String getRazonSocial() {
        return razonSocial;
    }

    /**
     * 
     * @param razonSocial
     *     The razonSocial
     */
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The montoNeto
     */
    public BigDecimal getMontoNeto() {
        return montoNeto;
    }

    /**
     * 
     * @param montoNeto
     *     The montoNeto
     */
    public void setMontoNeto(BigDecimal montoNeto) {
        this.montoNeto = montoNeto;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The montoExento
     */
    public BigDecimal getMontoExento() {
        return montoExento;
    }

    /**
     * 
     * @param montoExento
     *     The montoExento
     */
    public void setMontoExento(BigDecimal montoExento) {
        this.montoExento = montoExento;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The montoIva
     */
    public BigDecimal getMontoIva() {
        return montoIva;
    }

    /**
     * 
     * @param montoIva
     *     The montoIva
     */
    public void setMontoIva(BigDecimal montoIva) {
        this.montoIva = montoIva;
    }
    
    
    
    /**
     * 
     * @return
     *     The montoTotal
     */
    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    /**
     * 
     * @param montoTotal
     *     The montoTotal
     */
    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

}
