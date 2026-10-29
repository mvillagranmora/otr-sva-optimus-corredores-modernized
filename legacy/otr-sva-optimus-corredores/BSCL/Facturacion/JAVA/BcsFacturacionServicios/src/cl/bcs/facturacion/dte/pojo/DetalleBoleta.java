
package cl.bcs.facturacion.dte.pojo;

import java.math.BigDecimal;

public class DetalleBoleta {

    private Object id;
    private Integer numero;
    private String glosa;
    private String descripcion;

    private BigDecimal cantidad;
    private BigDecimal precio;
    private Double porcentajeAjuste;
    private Double montoAjuste;
    private BigDecimal monto;
    private BigDecimal precioOtraMoneda;
    private Object moneda;
    private Double factorConversion;
    private String boletaEditable;
    private Integer indicadorExento;
    private Object documentoEditable;
    private String itemMoneda;
    private String itemNemo;
    private String itemTransaccion;
    
    //Perú
    //private Double valor;
    //private Double importe;
    //private Integer polizaEditable;
    
    
    
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
     *     The descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * 
     * @param descripcion
     *     The descripcion
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
       
    

    /**
     * 
     * @return
     *     The cantidad
     */
    public BigDecimal getCantidad() {
        return cantidad;
    }

    /**
     * 
     * @param cantidad
     *     The cantidad
     */
    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * 
     * @return
     *     The precio
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * 
     * @param precio
     *     The precio
     */
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    /**
     * 
     * @return
     *     The porcentajeDescuento
     */
    public Double getPorcentajeAjuste() {
        return porcentajeAjuste;
    }

    /**
     * 
     * @param porcentajeDescuento
     *     The porcentajeDescuento
     */
    public void setPorcentajeAjuste(Double porcentajeAjuste) {
        this.porcentajeAjuste = porcentajeAjuste;
    }

    /**
     * 
     * @return
     *     The montoDescuento
     */
    public Double getMontoAjuste() {
        return montoAjuste;
    }

    /**
     * 
     * @param montoDescuento
     *     The montoDescuento
     */
    public void setMontoAjuste(Double montoAjuste) {
        this.montoAjuste = montoAjuste;
    }

    /**
     * 
     * @return
     *     The monto
     */
    public BigDecimal getMonto() {
        return monto;
    }

    /**
     * 
     * @param monto
     *     The monto
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    /**
     * 
     * @return
     *     The precioOtraMoneda
     */
    public BigDecimal getPrecioOtraMoneda() {
        return precioOtraMoneda;
    }

    /**
     * 
     * @param precioOtraMoneda
     *     The precioOtraMoneda
     */
    public void setPrecioOtraMoneda(BigDecimal precioOtraMoneda) {
        this.precioOtraMoneda = precioOtraMoneda;
    }

    /**
     * 
     * @return
     *     The moneda
     */
    public Object getMoneda() {
        return moneda;
    }

    /**
     * 
     * @param moneda
     *     The moneda
     */
    public void setMoneda(Object moneda) {
        this.moneda = moneda;
    }

    /**
     * 
     * @return
     *     The factorConversion
     */
    public Double getFactorConversion() {
        return factorConversion;
    }

    /**
     * 
     * @param factorConversion
     *     The factorConversion
     */
    public void setFactorConversion(Double factorConversion) {
        this.factorConversion = factorConversion;
    }
    
    
    
    
    
    /**
     * 
     * @return
     *     The boletaEditable
     */
    public String getBoletaEditable() {
        return boletaEditable;
    }

    /**
     * 
     * @param boletaEditable
     *     The boletaEditable
     */
    public void setBoletaEditable(String boletaEditable) {
        this.boletaEditable = boletaEditable;
    }
    
    

    /**
     * 
     * @return
     *     The indicadorExento
     */
    public Integer getIndicadorExento() {
        return indicadorExento;
    }

    /**
     * 
     * @param indicadorExento
     *     The indicadorExento
     */
    public void setIndicadorExento(Integer indicadorExento) {
        this.indicadorExento = indicadorExento;
    }
    
    
    /**
     * 
     * @param 
     *     The valor
     */
    /*public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}*/
	
	
	/**
     * 
     * @param 
     *     The importe
     */
	/*public Double getImporte() {
		return importe;
	}

	public void setImporte(Double importe) {
		this.importe = importe;
	}*/
	
    
	/**
     * 
     * @param 
     *     The polizaEditable
     */
	/*public Integer getPolizaEditable() {
		return polizaEditable;
	}

	public void setPolizaEditable(Integer polizaEditable) {
		this.polizaEditable = polizaEditable;
	}*/
      
    

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

    // CELBUHOPRO-959-pro
	public String getItemMoneda() {
	    return itemMoneda;
	}

	public void setItemMoneda(String itemMoneda) {
		this.itemMoneda = itemMoneda;
	}

    public String getItemNemo() {
	    return itemNemo;
	}
    
	public void setItemNemo(String itemNemo) {
		this.itemNemo = itemNemo;
	}

    public String getItemTransaccion() {
	    return itemTransaccion;
	}
    
	public void setItemTransaccion(String itemTransaccion) {
		this.itemTransaccion = itemTransaccion;
	}
}
