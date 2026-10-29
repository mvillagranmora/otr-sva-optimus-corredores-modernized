
package cl.bcs.facturacion.dte.pojo;

import java.util.List;

public class DocumentoDTE {

    private Object id;
    private Integer tipo;
    private Integer folio;
    private String fecha;
    private String rutEmisor;
    private String razonSocialEmisor;
    private String giroEmisor;
    private String direccionEmisor;
    private String comunaEmisor;
    private String ciudadEmisor;
    private String emailEmisor;
    private String rutReceptor;
    private String razonSocialReceptor;
    private String giroReceptor;
    private String direccionReceptor;
    private String comunaReceptor;
    private String ciudadReceptor;
    private String contactoReceptor;
    private String emailReceptor;
    private String medioPago;
    private String formaPago;
    private String fechaVencimiento;
    private String codigoVendedor;
    private Integer diasPago;
    private Double montoNoFacturable;
    private Double montoNeto;
    private Double montoExento;
    private Integer tasaIva;
    private Double montoIva;
    private Double montoTotal;
    private Double valorAPagar;
    private Integer montoPeriodo;
    private Integer saldoAnterior;
    private Boolean forzarFolio;
    private List<AjusteDocumento> ajusteDocumentos = null;
    private List<DetalleDocumento> detalleDocumentos = null;
    private List<ReferenciaDocumento> referenciaDocumentos = null;
    private List<ParametroDocumento> parametroDocumentos = null;

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
     *     The tipo
     */
    public Integer getTipo() {
        return tipo;
    }

    /**
     * 
     * @param tipo
     *     The tipo
     */
    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    /**
     * 
     * @return
     *     The folio
     */
    public Integer getFolio() {
        return folio;
    }

    /**
     * 
     * @param folio
     *     The folio
     */
    public void setFolio(Integer folio) {
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
     *     The rutEmisor
     */
    public String getRutEmisor() {
        return rutEmisor;
    }

    /**
     * 
     * @param rutEmisor
     *     The rutEmisor
     */
    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }

    /**
     * 
     * @return
     *     The razonSocialEmisor
     */
    public String getRazonSocialEmisor() {
        return razonSocialEmisor;
    }

    /**
     * 
     * @param razonSocialEmisor
     *     The razonSocialEmisor
     */
    public void setRazonSocialEmisor(String razonSocialEmisor) {
        this.razonSocialEmisor = razonSocialEmisor;
    }

    /**
     * 
     * @return
     *     The giroEmisor
     */
    public String getGiroEmisor() {
        return giroEmisor;
    }

    /**
     * 
     * @param giroEmisor
     *     The giroEmisor
     */
    public void setGiroEmisor(String giroEmisor) {
        this.giroEmisor = giroEmisor;
    }

    /**
     * 
     * @return
     *     The direccionEmisor
     */
    public String getDireccionEmisor() {
        return direccionEmisor;
    }

    /**
     * 
     * @param direccionEmisor
     *     The direccionEmisor
     */
    public void setDireccionEmisor(String direccionEmisor) {
        this.direccionEmisor = direccionEmisor;
    }

    /**
     * 
     * @return
     *     The comunaEmisor
     */
    public String getComunaEmisor() {
        return comunaEmisor;
    }

    /**
     * 
     * @param comunaEmisor
     *     The comunaEmisor
     */
    public void setComunaEmisor(String comunaEmisor) {
        this.comunaEmisor = comunaEmisor;
    }

    /**
     * 
     * @return
     *     The ciudadEmisor
     */
    public String getCiudadEmisor() {
        return ciudadEmisor;
    }

    /**
     * 
     * @param ciudadEmisor
     *     The ciudadEmisor
     */
    public void setCiudadEmisor(String ciudadEmisor) {
        this.ciudadEmisor = ciudadEmisor;
    }

    /**
     * 
     * @return
     *     The emailEmisor
     */
    public String getEmailEmisor() {
        return emailEmisor;
    }

    /**
     * 
     * @param emailEmisor
     *     The emailEmisor
     */
    public void setEmailEmisor(String emailEmisor) {
        this.emailEmisor = emailEmisor;
    }

    /**
     * 
     * @return
     *     The rutReceptor
     */
    public String getRutReceptor() {
        return rutReceptor;
    }

    /**
     * 
     * @param rutReceptor
     *     The rutReceptor
     */
    public void setRutReceptor(String rutReceptor) {
        this.rutReceptor = rutReceptor;
    }

    /**
     * 
     * @return
     *     The razonSocialReceptor
     */
    public String getRazonSocialReceptor() {
        return razonSocialReceptor;
    }

    /**
     * 
     * @param razonSocialReceptor
     *     The razonSocialReceptor
     */
    public void setRazonSocialReceptor(String razonSocialReceptor) {
        this.razonSocialReceptor = razonSocialReceptor;
    }

    /**
     * 
     * @return
     *     The giroReceptor
     */
    public String getGiroReceptor() {
        return giroReceptor;
    }

    /**
     * 
     * @param giroReceptor
     *     The giroReceptor
     */
    public void setGiroReceptor(String giroReceptor) {
        this.giroReceptor = giroReceptor;
    }

    /**
     * 
     * @return
     *     The direccionReceptor
     */
    public String getDireccionReceptor() {
        return direccionReceptor;
    }

    /**
     * 
     * @param direccionReceptor
     *     The direccionReceptor
     */
    public void setDireccionReceptor(String direccionReceptor) {
        this.direccionReceptor = direccionReceptor;
    }

    /**
     * 
     * @return
     *     The comunaReceptor
     */
    public String getComunaReceptor() {
        return comunaReceptor;
    }

    /**
     * 
     * @param comunaReceptor
     *     The comunaReceptor
     */
    public void setComunaReceptor(String comunaReceptor) {
        this.comunaReceptor = comunaReceptor;
    }

    /**
     * 
     * @return
     *     The ciudadReceptor
     */
    public String getCiudadReceptor() {
        return ciudadReceptor;
    }

    /**
     * 
     * @param ciudadReceptor
     *     The ciudadReceptor
     */
    public void setCiudadReceptor(String ciudadReceptor) {
        this.ciudadReceptor = ciudadReceptor;
    }

    /**
     * 
     * @return
     *     The contactoReceptor
     */
    public String getContactoReceptor() {
        return contactoReceptor;
    }

    /**
     * 
     * @param contactoReceptor
     *     The contactoReceptor
     */
    public void setContactoReceptor(String contactoReceptor) {
        this.contactoReceptor = contactoReceptor;
    }

    /**
     * 
     * @return
     *     The emailReceptor
     */
    public String getEmailReceptor() {
        return emailReceptor;
    }

    /**
     * 
     * @param emailReceptor
     *     The emailReceptor
     */
    public void setEmailReceptor(String emailReceptor) {
        this.emailReceptor = emailReceptor;
    }

    /**
     * 
     * @return
     *     The medioPago
     */
    public String getMedioPago() {
        return medioPago;
    }

    /**
     * 
     * @param medioPago
     *     The medioPago
     */
    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    /**
     * 
     * @return
     *     The formaPago
     */
    public String getFormaPago() {
        return formaPago;
    }

    /**
     * 
     * @param formaPago
     *     The formaPago
     */
    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    /**
     * 
     * @return
     *     The fechaVencimiento
     */
    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    /**
     * 
     * @param fechaVencimiento
     *     The fechaVencimiento
     */
    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * 
     * @return
     *     The codigoVendedor
     */
    public String getCodigoVendedor() {
        return codigoVendedor;
    }

    /**
     * 
     * @param codigoVendedor
     *     The codigoVendedor
     */
    public void setCodigoVendedor(String codigoVendedor) {
        this.codigoVendedor = codigoVendedor;
    }

    /**
     * 
     * @return
     *     The diasPago
     */
    public Integer getDiasPago() {
        return diasPago;
    }

    /**
     * 
     * @param diasPago
     *     The diasPago
     */
    public void setDiasPago(Integer diasPago) {
        this.diasPago = diasPago;
    }

    /**
     * 
     * @return
     *     The montoNoFacturable
     */
    public Double getMontoNoFacturable() {
        return montoNoFacturable;
    }

    /**
     * 
     * @param montoNoFacturable
     *     The montoNoFacturable
     */
    public void setMontoNoFacturable(Double montoNoFacturable) {
        this.montoNoFacturable = montoNoFacturable;
    }

    /**
     * 
     * @return
     *     The montoNeto
     */
    public Double getMontoNeto() {
        return montoNeto;
    }

    /**
     * 
     * @param montoNeto
     *     The montoNeto
     */
    public void setMontoNeto(Double montoNeto) {
        this.montoNeto = montoNeto;
    }

    /**
     * 
     * @return
     *     The montoExento
     */
    public Double getMontoExento() {
        return montoExento;
    }

    /**
     * 
     * @param montoExento
     *     The montoExento
     */
    public void setMontoExento(Double montoExento) {
        this.montoExento = montoExento;
    }

    /**
     * 
     * @return
     *     The tasaIva
     */
    public Integer getTasaIva() {
        return tasaIva;
    }

    /**
     * 
     * @param tasaIva
     *     The tasaIva
     */
    public void setTasaIva(Integer tasaIva) {
        this.tasaIva = tasaIva;
    }

    /**
     * 
     * @return
     *     The montoIva
     */
    public Double getMontoIva() {
        return montoIva;
    }

    /**
     * 
     * @param montoIva
     *     The montoIva
     */
    public void setMontoIva(Double montoIva) {
        this.montoIva = montoIva;
    }

    /**
     * 
     * @return
     *     The montoTotal
     */
    public Double getMontoTotal() {
        return montoTotal;
    }

    /**
     * 
     * @param montoTotal
     *     The montoTotal
     */
    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    /**
     * 
     * @return
     *     The valorAPagar
     */
    public Double getValorAPagar() {
        return valorAPagar;
    }

    /**
     * 
     * @param valorAPagar
     *     The valorAPagar
     */
    public void setValorAPagar(Double valorAPagar) {
        this.valorAPagar = valorAPagar;
    }

    /**
     * 
     * @return
     *     The montoPeriodo
     */
    public Integer getMontoPeriodo() {
        return montoPeriodo;
    }

    /**
     * 
     * @param montoPeriodo
     *     The montoPeriodo
     */
    public void setMontoPeriodo(Integer montoPeriodo) {
        this.montoPeriodo = montoPeriodo;
    }

    /**
     * 
     * @return
     *     The saldoAnterior
     */
    public Integer getSaldoAnterior() {
        return saldoAnterior;
    }

    /**
     * 
     * @param saldoAnterior
     *     The saldoAnterior
     */
    public void setSaldoAnterior(Integer saldoAnterior) {
        this.saldoAnterior = saldoAnterior;
    }

    /**
     * 
     * @return
     *     The forzarFolio
     */
    public Boolean getForzarFolio() {
        return forzarFolio;
    }

    /**
     * 
     * @param forzarFolio
     *     The forzarFolio
     */
    public void setForzarFolio(Boolean forzarFolio) {
        this.forzarFolio = forzarFolio;
    }

    /**
     * 
     * @return
     *     The ajusteDocumentos
     */
    public List<AjusteDocumento> getAjusteDocumentos() {
        return ajusteDocumentos;
    }

    /**
     * 
     * @param ajusteDocumentos
     *     The ajusteDocumentos
     */
    public void setAjusteDocumentos(List<AjusteDocumento> ajusteDocumentos) {
        this.ajusteDocumentos = ajusteDocumentos;
    }

    /**
     * 
     * @return
     *     The detalleDocumentos
     */
    public List<DetalleDocumento> getDetalleDocumentos() {
        return detalleDocumentos;
    }

    /**
     * 
     * @param detalleDocumentos
     *     The detalleDocumentos
     */
    public void setDetalleDocumentos(List<DetalleDocumento> detalleDocumentos) {
        this.detalleDocumentos = detalleDocumentos;
    }

    /**
     * 
     * @return
     *     The referenciaDocumentos
     */
    public List<ReferenciaDocumento> getReferenciaDocumentos() {
        return referenciaDocumentos;
    }

    /**
     * 
     * @param referenciaDocumentos
     *     The referenciaDocumentos
     */
    public void setReferenciaDocumentos(List<ReferenciaDocumento> referenciaDocumentos) {
        this.referenciaDocumentos = referenciaDocumentos;
    }

    /**
     * 
     * @return
     *     The parametroDocumentos
     */
    public List<ParametroDocumento> getParametroDocumentos() {
        return parametroDocumentos;
    }

    /**
     * 
     * @param parametroDocumentos
     *     The parametroDocumentos
     */
    public void setParametroDocumentos(List<ParametroDocumento> parametroDocumentos) {
        this.parametroDocumentos = parametroDocumentos;
    }

}
