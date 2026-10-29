
package cl.bcs.facturacion.dte.pojo;

import java.math.BigDecimal;

public class AjusteDocumento {

    private Object id;

    private Integer numero;
    private String tipo;
    private String glosa;
    private String tipoValor;
    private BigDecimal valor;
    
    private Object documentoEditable;


    /**
     * 
     * @return
     *     The id
     */
    public Object getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(Object id) {
        this.id = id;
    }

    
    /**
     * 
     * @return
     *     The numero
     */
    public Integer getNumero() {
        return numero;
    }

    /**
     * 
     * @param numero
     *     The numero
     */
    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    /**
     * 
     * @return
     *     The tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * 
     * @param tipo
     *     The tipo
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * 
     * @return
     *     The glosa
     */
    public String getGlosa() {
        return glosa;
    }

    /**
     * 
     * @param glosa
     *     The glosa
     */
    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    /**
     * 
     * @return
     *     The tipoValor
     */
    public String getTipoValor() {
        return tipoValor;
    }

    /**
     * 
     * @param tipoValor
     *     The tipoValor
     */
    public void setTipoValor(String tipoValor) {
        this.tipoValor = tipoValor;
    }

    /**
     * 
     * @return
     *     The valor
     */
    public BigDecimal getValor() {
        return valor;
    }

    /**
     * 
     * @param valor
     *     The valor
     */
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    
    
    /**
     * 
     * @return
     *     The documentoEditable
     */
    public Object getDocumentoEditable() {
        return documentoEditable;
    }

    /**
     * 
     * @param documentoEditable
     *     The documentoEditable
     */
    public void setDocumentoEditable(Object documentoEditable) {
        this.documentoEditable = documentoEditable;
    }

}
