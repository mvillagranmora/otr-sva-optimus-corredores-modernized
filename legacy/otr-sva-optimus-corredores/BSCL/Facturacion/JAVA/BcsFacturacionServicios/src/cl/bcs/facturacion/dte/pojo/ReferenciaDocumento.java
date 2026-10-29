
package cl.bcs.facturacion.dte.pojo;


public class ReferenciaDocumento {

    private Object id;
    private Long numero;
    private String tipo;

    private String folio;
    private String fecha;
    private String razon;
    private Long codigo;
    private Object documentoEditable;
    
    
    //Perú
    private String referenciaPolizas;
    private String parametroPolizas;
    

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
    public Long getNumero() {
        return numero;
    }

    /**
     * 
     * @param numero
     *     The numero
     */
    public void setNumero(Long numero) {
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
     *     The folio
     */
    public String getFolio() {
        return folio;
    }

    /**
     * 
     * @param folio
     *     The folio
     */
    public void setFolio(Long folio) {
        this.folio = folio == null? "": folio.toString();
    }

    public void setFolio(String folio) {
    	this.folio = folio;
    }

    /**
     * 
     * @return
     *     The fecha
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * 
     * @param fecha
     *     The fecha
     */
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    /**
     * 
     * @return
     *     The razon
     */
    public String getRazon() {
        return razon;
    }

    /**
     * 
     * @param razon
     *     The razon
     */
    public void setRazon(String razon) {
        this.razon = razon;
    }

    /**
     * 
     * @return
     *     The codigo
     */
    public Long getCodigo() {
        return codigo;
    }

    /**
     * 
     * @param codigo
     *     The codigo
     */
    public void setCodigo(Long codigo) {
        this.codigo = codigo;
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

	
	
    
    /**
     * 
     * @param
     *     The referenciaPoliza
     */
	
	public String getReferenciaPolizas() {
		return referenciaPolizas;
	}

	public void setReferenciaPolizas(String referenciaPolizas) {
		this.referenciaPolizas = referenciaPolizas;
	}
	
	
	/**
     * 
     * @param
     *     The parametroPolizas
     */
	public String getParametroPolizas() {
		return parametroPolizas;
	}

	public void setParametroPolizas(String parametroPolizas) {
		this.parametroPolizas = parametroPolizas;
	}

}
