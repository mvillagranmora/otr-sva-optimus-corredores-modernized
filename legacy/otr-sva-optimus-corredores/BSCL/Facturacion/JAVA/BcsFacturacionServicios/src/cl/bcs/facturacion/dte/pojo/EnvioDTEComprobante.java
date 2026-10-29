
package cl.bcs.facturacion.dte.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EnvioDTEComprobante {

    private Object id;
    

    private Long tipo;
    private Integer folio;
    private String fecha;
    private String rutEmisor;
    private String razonSocialEmisor;
    private String giroEmisor;
    private String direccionEmisor;
    private String comunaEmisor;
    private String ciudadEmisor;
    private String emailEmisor;
    private String condicionpago;
    private String codsiicondicionpago;
    private String rutReceptor;
    private String codigoSucursal;
    private String razonSocialReceptor;
    private String nombreReceptor;
    private String giroReceptor;
    private String direccionReceptor;
    private String comunaReceptor;
    private String ciudadReceptor;
    private String contactoReceptor;
    private String indBolsa;
    private String indMensaje_1;
    private String emailReceptor;
    private String medioPago;
    private String formaPago;
    private Boolean anulada; 
    private String fechaVencimiento;
    private String codigoVendedor;
    private String codigoReceptor;
    private BigDecimal montoNoFacturable;
    private BigDecimal montoNeto;
    private BigDecimal montoExento;
    private BigDecimal tasaIva;
    private BigDecimal montoIva;
    private BigDecimal montoTotal;
    private BigDecimal valorAPagar;
    private BigDecimal montoPeriodo;
    private Integer indServicio;
    private BigDecimal saldoAnterior;
    private Boolean forzarFolio;
    
    //Peru
    private String tipoPoliza;
    private String urbanizacionEmisor;
    private String provinciaEmisor;
    private String departamentoEmisor;
    private String distritoEmisor;
    private String telefonoEmisor;
    private String tipoIdentificadorReceptor;
    private Integer codigoIdentificadorReceptor;
    
    private String urbanizacionReceptor;
    private String provinciaReceptor;
    private String departamentoReceptor;
    private String distritoReceptor;
    
    private String ubigeoReceptor;
    private String nacionalidadReceptor;
    private String productoOperacion;
    private String tipoOperacion;
    private String claveOrigen;
    private String monedaTransaccion;
    private Double cantidadTotal;
    private Double precioPromedio;
    private Double importeTotal;
    private Integer arancelBVL;
    private Integer retribucionCAVALI;
    private Integer contribucionSMV;
    private Integer fondoLiquidacion;
    private Integer fondoGarantia;
    private Integer comisionSAB;
    private Integer otrasComisiones;
    private String  monedaLiquidacion;
    private Double  importeLiquidacion;
    private Integer comisionLiquidacion;
    private Integer igvLiquidacion;
    private Integer vacLiquidacion;
    private Integer interesLiquidacion;
    private Double  totalLiquidacion;
    private String  fechaLiquidacion;
    private Integer tasa;
    private Integer plazo;
    private Integer tipoCambio;
    private String  monedaImporte;
    private Double  importeAPagar;
    private String  reemplazada;
    private String  referenciapolizas;
    private String  parametropolizas;
    
    private List<AjusteDocumento> ajusteDocumentos = null;
    private List<DetalleDocumento> detalleDocumentos = null;
    private List<DetalleBoleta> detalleBoletas = null;
    private List<ReferenciaDocumento> referenciaDocumentos = null;
    private List<ReferenciaBoleta> referenciaBoletas = null;
    private List<ParametroDocumento> parametroDocumentos = null;
    private List<ParametroBoleta> parametroBoletas = null;

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
    public Long getTipo() {
        return tipo;
    }

    /**
     * 
     * @param tipo
     *     The tipo
     */
    public void setTipo(Long tipo) {
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
     *     The condicionpago
     */
    public String getCondicionPago() {
        return condicionpago;
    }

    /**
     * 
     * @param condicionpago
     *     The condicionpago
     */
    public void setCondicionPago(String condicionPago) {
        this.condicionpago = condicionPago;
    }
    
    
    
    
    /**
     * 
     * @return
     *     The codsiicondicionpago
     */
    public String getCodSIICondicionPago() {
        return codsiicondicionpago;
    }

    /**
     * 
     * @param codsiicondicionpago
     *     The codsiicondicionpago
     */
    public void setCodSIICondicionPago(String codSiiCondicionPago) {
        this.codsiicondicionpago = codSiiCondicionPago;
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
     *     The codigoSucursal
     */
    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    /**
     * 
     * @param 
     *     The codigoSucursal
     */
    public void setCodigoSucursal(String codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
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
     *     The nombreReceptor
     */
    public String getNombreReceptor() {
        return nombreReceptor;
    }

    /**
     * 
     * @param nombreReceptor
     *     The nombreReceptor
     */
    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
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
     *     The indBolsa
     */
    public String getIndBolsa() {
        return indBolsa;
    }

    /**
     * 
     * @param indBolsa
     *     The indBolsa
     */
    public void setIndBolsa(String indBolsa) {
        this.indBolsa = indBolsa;
    }
    
    
    /**
     * 
     * @return
     *     The indMensaje_1
     */
    public String getIndMensaje_1() {
        return indMensaje_1;
    }

    /**
     * 
     * @param indMensaje_1
     *     The indMensaje_1
     */
    public void setIndMensaje_1(String indMensaje_1) {
        this.indMensaje_1 = indMensaje_1;
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
     *     The anulada
     */
    public Boolean getAnulada() {
        return anulada;
    }

    /**
     * 
     * @param anulada
     *     The anulada
     */
    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
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
     *     The codigoReceptor
     */
    public String getCodigoReceptor() {
        return codigoReceptor;
    }

    /**
     * 
     * @param codigoReceptor
     *     The codigoReceptor
     */
    public void setCodigoReceptor(String codigoReceptor) {
        this.codigoReceptor = codigoReceptor;
    }
    
    
    

    /**
     * 
     * @return
     *     The montoNoFacturable
     */
    public BigDecimal getMontoNoFacturable() {
        return montoNoFacturable;
    }

    /**
     * 
     * @param montoNoFacturable
     *     The montoNoFacturable
     */
    public void setMontoNoFacturable(BigDecimal montoNoFacturable) {
        this.montoNoFacturable = montoNoFacturable;
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
     *     The tasaIva
     */
    public BigDecimal getTasaIva() {
        return tasaIva;
    }

    /**
     * 
     * @param tasaIva
     *     The tasaIva
     */
    public void setTasaIva(BigDecimal tasaIva) {
        this.tasaIva = tasaIva;
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

    /**
     * 
     * @return
     *     The valorAPagar
     */
    public BigDecimal getValorAPagar() {
        return valorAPagar;
    }

    /**
     * 
     * @param valorAPagar
     *     The valorAPagar
     */
    public void setValorAPagar(BigDecimal valorAPagar) {
        this.valorAPagar = valorAPagar;
    }

    /**
     * 
     * @return
     *     The montoPeriodo
     */
    public BigDecimal getMontoPeriodo() {
        return montoPeriodo;
    }

    /**
     * 
     * @param montoPeriodo
     *     The montoPeriodo
     */
    public void setMontoPeriodo(BigDecimal montoPeriodo) {
        this.montoPeriodo = montoPeriodo;
    }
    
    
    /**
     * 
     * @return
     *     The indServicio
     */
    public Integer getIndServicio() {
        return indServicio;
    }

    /**
     * 
     * @param indServicio
     *     The indServicio
     */
    public void setIndServicio(Integer indServicio) {
        this.indServicio = indServicio;
    }
    
    

    /**
     * 
     * @return
     *     The saldoAnterior
     */
    public BigDecimal getSaldoAnterior() {
        return saldoAnterior;
    }

    /**
     * 
     * @param saldoAnterior
     *     The saldoAnterior
     */
    public void setSaldoAnterior(BigDecimal saldoAnterior) {
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
    
    
    
    //Peru       
    /**
     * 
     * @return
     *     The tipoPoliza
     */
    public String getTipoPoliza() {
        return tipoPoliza;
    }

    /**
     * 
     * @param 
     *     The tipoPoliza
     */
    public void setTipoPoliza(String tipoPoliza) {
        this.tipoPoliza = tipoPoliza;
    }
    
    
    
    /**
     * 
     * @return
     *     The urbanizacionEmisor
     */
    public String getUrbanizacionEmisor() {
        return urbanizacionEmisor;
    }

    /**
     * 
     * @param urbanizacionEmisor
     *     The urbanizacionEmisor
     */
    public void setUrbanizacionEmisor(String urbanizacionEmisor) {
        this.urbanizacionEmisor = urbanizacionEmisor;
    }
    
    
    /**
     * 
     * @return
     *     The provinciaEmisor
     */
    public String getProvinciaEmisor() {
        return provinciaEmisor;
    }

    /**
     * 
     * @param provinciaEmisor
     *     The provinciaEmisor
     */
    public void setProvinciaEmisor(String provinciaEmisor) {
        this.provinciaEmisor = provinciaEmisor;
    }
    
    /**
     * 
     * @return
     *     The departamentoEmisor
     */
    public String getDepartamentoEmisor() {
        return departamentoEmisor;
    }

    /**
     * 
     * @param DepartamentoEmisor
     *     The DepartamentoEmisor
     */
    public void setDepartamentoEmisor(String departamentoEmisor) {
        this.departamentoEmisor = departamentoEmisor;
    }
    
    /**
     * 
     * @return
     *     The distritoEmisor
     */
    public String getDistritoEmisor() {
        return distritoEmisor;
    }

    /**
     * 
     * @param DistritoEmisor
     *     The DistritoEmisor
     */
    public void setDistritoEmisor(String distritoEmisor) {
        this.distritoEmisor = distritoEmisor;
    }
    
    /**
     * 
     * @return
     *     The telefonoEmisor
     */
    public String getTelefonoEmisor() {
        return telefonoEmisor;
    }

    /**
     * 
     * @param TelefonoEmisor
     *     The TelefonoEmisor
     */
    public void setTelefonoEmisor(String telefonoEmisor) {
        this.telefonoEmisor = telefonoEmisor;
    }
    
    /**
     * 
     * @return
     *     The TipoIdentificadorReceptor
     */
    public String getTipoIdentificadorReceptor() {
        return tipoIdentificadorReceptor;
    }

    /**
     * 
     * @param TipoIdentificadorReceptor
     *     The TipoIdentificadorReceptor
     */
    public void setTipoIdentificadorReceptor(String tipoIdentificadorReceptor) {
        this.tipoIdentificadorReceptor = tipoIdentificadorReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The CodigoIdentificadorReceptor
     */
    public Integer getCodigoIdentificadorReceptor() {
        return codigoIdentificadorReceptor;
    }

    /**
     * 
     * @param CodigoIdentificadorReceptor
     *     The CodigoIdentificadorReceptor
     */
    public void setCodigoIdentificadorReceptor(Integer codigoIdentificadorReceptor) {
        this.codigoIdentificadorReceptor = codigoIdentificadorReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The UrbanizacionReceptor
     */
    public String getUrbanizacionReceptor() {
        return urbanizacionReceptor;
    }

    /**
     * 
     * @param UrbanizacionReceptor
     *     The UrbanizacionReceptor
     */
    public void setUrbanizacionReceptor(String urbanizacionReceptor) {
        this.urbanizacionReceptor = urbanizacionReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The ProvinciaReceptor
     */
    public String getProvinciaReceptor() {
        return provinciaReceptor;
    }

    /**
     * 
     * @param ProvinciaReceptor
     *     The ProvinciaReceptor
     */
    public void setProvinciaReceptor(String provinciaReceptor) {
        this.provinciaReceptor = provinciaReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The DepartamentoReceptor
     */
    public String getDepartamentoReceptor() {
        return departamentoReceptor;
    }

    /**
     * 
     * @param DepartamentoReceptor
     *     The DepartamentoReceptor
     */
    public void setDepartamentoReceptor(String departamentoReceptor) {
        this.departamentoReceptor = departamentoReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The DistritoReceptor
     */
    public String getDistritoReceptor() {
        return distritoReceptor;
    }

    /**
     * 
     * @param DistritoReceptor
     *     The DistritoReceptor
     */
    public void setDistritoReceptor(String distritoReceptor) {
        this.distritoReceptor = distritoReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The UbigeoReceptor
     */
    public String getUbigeoReceptor() {
        return ubigeoReceptor;
    }

    /**
     * 
     * @param UbigeoReceptor
     *     The UbigeoReceptor
     */
    public void setUbigeoReceptor(String ubigeoReceptor) {
        this.ubigeoReceptor = ubigeoReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The NacionalidadReceptor
     */
    public String getNacionalidadReceptor() {
        return nacionalidadReceptor;
    }

    /**
     * 
     * @param NacionalidadReceptor
     *     The NacionalidadReceptor
     */
    public void setNacionalidadReceptor(String nacionalidadReceptor) {
        this.nacionalidadReceptor = nacionalidadReceptor;
    }
    
    
    /**
     * 
     * @return
     *     The ProductoOperacion
     */
    public String getProductoOperacion() {
        return productoOperacion;
    }

    /**
     * 
     * @param ProductoOperacion
     *     The ProductoOperacion
     */
    public void setProductoOperacion(String productoOperacion) {
        this.productoOperacion = productoOperacion;
    }
    

    /**
     * 
     * @return
     *     The TipoOperacion
     */
    public String getTipoOperacion() {
        return tipoOperacion;
    }

    /**
     * 
     * @param TipoOperacion
     *     The TipoOperacion
     */
    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }
    
    
    
    /**
     * 
     * @return
     *     The ClaveOrigen
     */
    public String getClaveOrigen() {
        return claveOrigen;
    }

    /**
     * 
     * @param ClaveOrigen
     *     The ClaveOrigen
     */
    public void setClaveOrigen(String claveOrigen) {
        this.claveOrigen = claveOrigen;
    }
    
    
    
    /**
     * 
     * @return
     *     The MonedaTransaccion
     */
    public String getMonedaTransaccion() {
        return monedaTransaccion;
    }

    /**
     * 
     * @param MonedaTransaccion
     *     The MonedaTransaccion
     */
    public void setMonedaTransaccion(String monedaTransaccion) {
        this.monedaTransaccion = monedaTransaccion;
    }
    
    
    /**
     * 
     * @return
     *     The CantidadTotal
     */
    public Double getCantidadTotal() {
        return cantidadTotal;
    }

    /**
     * 
     * @param CantidadTotal
     *     The CantidadTotal
     */
    public void setCantidadTotal(Double cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
    

    /**
     * 
     * @return
     *     The PrecioPromedio
     */
    public Double getPrecioPromedio() {
        return precioPromedio;
    }

    /**
     * 
     * @param PrecioPromedio
     *     The PrecioPromedio
     */
    public void setPrecioPromedio(Double precioPromedio) {
        this.precioPromedio = precioPromedio;
    }
    
    
    
    /**
     * 
     * @return
     *     The ImporteTotal
     */
    public Double getImporteTotal() {
        return importeTotal;
    }

    /**
     * 
     * @param ImporteTotal
     *     The ImporteTotal
     */
    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }
    
    
    /**
     * 
     * @return
     *     The ArancelBVL
     */
    public Integer getArancelBVL() {
        return arancelBVL;
    }

    /**
     * 
     * @param ArancelBVL
     *     The ArancelBVL
     */
    public void setArancelBVL(Integer arancelBVL) {
        this.arancelBVL = arancelBVL;
    }
    
    
    /**
     * 
     * @return
     *     The RetribucionCAVALI
     */
    public Integer getRetribucionCAVALI() {
        return retribucionCAVALI;
    }

    /**
     * 
     * @param RetribucionCAVALI
     *     The RetribucionCAVALI
     */
    public void setRetribucionCAVALI(Integer retribucionCAVALI) {
        this.retribucionCAVALI = retribucionCAVALI;
    }
    
    
    /**
     * 
     * @return
     *     The ContribucionSMV
     */
    public Integer getContribucionSMV() {
        return contribucionSMV;
    }

    /**
     * 
     * @param ContribucionSMV
     *     The ContribucionSMV
     */
    public void setContribucionSMV(Integer contribucionSMV) {
        this.contribucionSMV = contribucionSMV;
    }
    
    
    /**
     * 
     * @param FondoLiquidacion
     *     The FondoLiquidacion
     */    
    
    public Integer getFondoLiquidacion() {
		return fondoLiquidacion;
	}

	public void setFondoLiquidacion(Integer fondoLiquidacion) {
		this.fondoLiquidacion = fondoLiquidacion;
	}
	
	
	/**
     * 
     * @param FondoGarantia
     *     The FondoGarantia
     */
	
	 public Integer getFondoGarantia() {
		return fondoGarantia;
	 }

	 public void setFondoGarantia(Integer fondoGarantia) {
		this.fondoGarantia = fondoGarantia;
	 }
	 
	 
	 /**
	  * 
	  * @param ComisionSAB
	  *     The ComisionSAB
	  */
	 
	 public Integer getComisionSAB() {
			return comisionSAB;
		}

	 public void setComisionSAB(Integer comisionSAB) {
			this.comisionSAB = comisionSAB;
		} 
	
	 
	 /**
	  * 
	  * @param OtrasComisiones
	  *     The OtrasComisiones
	  */
	 
	 public Integer getOtrasComisiones() {
		return otrasComisiones;
	  }

	 public void setOtrasComisiones(Integer otrasComisiones) {
		this.otrasComisiones = otrasComisiones;
	  }
	 
	 /**
	  * 
	  * @param MonedaLiquidacion
	  *     The MonedaLiquidacion
	  */
	
	 public String getMonedaLiquidacion() {
			return monedaLiquidacion;
		}

	 public void setMonedaLiquidacion(String monedaLiquidacion) {
			this.monedaLiquidacion = monedaLiquidacion;
		}
	
	 /**
	  * 
	  * @param ImporteLiquidacion
	  *     The ImporteLiquidacion
	  */
	 public Double getImporteLiquidacion() {
			return importeLiquidacion;
	  }

	 public void setImporteLiquidacion(Double importeLiquidacion) {
			this.importeLiquidacion = importeLiquidacion;
	  }
	 
    
	 /**
	  * 
	  * @param ComisionLiquidacion
	  *     The ComisionLiquidacion
	  */
	 public Integer getComisionLiquidacion() {
			return comisionLiquidacion;
		}

	 public void setComisionLiquidacion(Integer comisionLiquidacion) {
			this.comisionLiquidacion = comisionLiquidacion;
		}
    
    
	 /**
	  * 
	  * @param IgvLiquidacion
	  *     The IgvLiquidacion
	  */ 
    
	 public Integer getIgvLiquidacion() {
			return igvLiquidacion;
		}

	 public void setIgvLiquidacion(Integer igvLiquidacion) {
			this.igvLiquidacion = igvLiquidacion;
		}
    
    
	 /**
	  * 
	  * @param VacLiquidacion
	  *     The VacLiquidacion
	  */
    
	 public Integer getVacLiquidacion() {
			return vacLiquidacion;
		}

	 public void setVacLiquidacion(Integer vacLiquidacion) {
			this.vacLiquidacion = vacLiquidacion;
		}
		
		
	 
	 /**
	  * 
	  * @param InteresLiquidacion
	  *     The InteresLiquidacion
	  */
	 public Integer getInteresLiquidacion() {
			return interesLiquidacion;
		}

	 public void setInteresLiquidacion(Integer interesLiquidacion) {
			this.interesLiquidacion = interesLiquidacion;
		}	
		
		
	
	 /**
	  * 
	  * @param InteresLiquidacion
	  *     The InteresLiquidacion
	  */
	 public Double getTotalLiquidacion() {
			return totalLiquidacion;
		}

	 public void setTotalLiquidacion(Double totalLiquidacion) {
			this.totalLiquidacion = totalLiquidacion;
		}
		
	 
	 /**
	  * 
	  * @param FechaLiquidacion
	  *     The FechaLiquidacion
	  */
	 public String getFechaLiquidacion() {
			return fechaLiquidacion;
		}

	 public void setFechaLiquidacion(String fechaLiquidacion) {
			this.fechaLiquidacion = fechaLiquidacion;
		} 
		
	
	 
	 /**
	  * 
	  * @param Tasa
	  *     The Tasa
	  */
	 public Integer getTasa() {
			return tasa;
		}

	 public void setTasa(Integer tasa) {
			this.tasa = tasa;
		}
	
	 
	 /**
	  * 
	  * @param Plazo
	  *     The Plazo
	  */
	 public Integer getPlazo() {
			return plazo;
		}

	 public void setPlazo(Integer plazo) {
			this.plazo = plazo;
		}
	 
	 
	 /**
	  * 
	  * @param TipoCambio
	  *     The TipoCambio
	  */
	 public Integer getTipoCambio() {
			return tipoCambio;
		}

	 public void setTipoCambio(Integer tipoCambio) {
			this.tipoCambio = tipoCambio;
		}	
    
	 
	 /**
	  * 
	  * @param MonedaImporte
	  *     The MonedaImporte
	  */
	 public String getMonedaImporte() {
			return monedaImporte;
		}

	 public void setMonedaImporte(String monedaImporte) {
			this.monedaImporte = monedaImporte;
		}
		
		
	
	 /**
	  * 
	  * @param ImporteAPagar
	  *     The ImporteAPagar
	  */
	 public Double getImporteAPagar() {
			return importeAPagar;
		}

	 public void setImporteAPagar(Double importeAPagar) {
			this.importeAPagar = importeAPagar;
		}
		
		
	 /**
	  * 
	  * @param Reemplazada
	  *     The Reemplazada
	  */
	 public String getReemplazada() {
			return reemplazada;
		}

	 public void setReemplazada(String reemplazada) {
			this.reemplazada = reemplazada;
		}
	 
	 
	
	 
	 /**
	  * 
	  * @param 
	  *     The referenciapolizas
	  */
	 public String getReferenciaPolizas() {
			return referenciapolizas;
		}

	 public void setReferenciaPolizas(String referenciapolizas) {
			this.referenciapolizas = referenciapolizas;
		}
	 
	 	 
	 
	 /**
	  * 
	  * @param 
	  *     The parametropolizas
	  */
	 public String getParametroPolizas() {
			return parametropolizas;
		}

	 public void setParametroPolizas(String parametropolizas) {
			this.parametropolizas = parametropolizas;
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
     *     The detalleBoletas
     */
    public List<DetalleBoleta> getDetalleBoletas() {
        return detalleBoletas;
    }

    /**
     * 
     * @param detalleDocumentos
     *     The detalleDocumentos
     */
    public void setDetalleBoletas(List<DetalleBoleta> detalleBoletas) {
        this.detalleBoletas = detalleBoletas;
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
     *     The referenciaDocumentos
     */
    public List<ReferenciaBoleta> getReferenciaBoletas() {
        return referenciaBoletas;
    }

    /**
     * 
     * @param referenciaDocumentos
     *     The referenciaDocumentos
     */
    public void setReferenciaBoletas(List<ReferenciaBoleta> referenciaBoletas) {
        this.referenciaBoletas = referenciaBoletas;
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
           
    public void addParametroDocumentos(ParametroDocumento pd) {
        this.parametroDocumentos.add(pd);
    }
    
    
    /**
     * 
     * @return
     *     The parametroBoletas
     */
    public List<ParametroBoleta> getParametroBoletas() {
        return parametroBoletas;
    }
    
    /**
     * 
     * @param parametroBoletas
     *     The parametroBoletas
     */
    public void setParametroBoletas(List<ParametroBoleta> parametroBoletas) {
        this.parametroBoletas = parametroBoletas;
    }
    
    public void addParametroBoletas(ParametroBoleta pd) {
        this.parametroBoletas.add(pd);
    }
    
}

