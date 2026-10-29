
package cl.bcs.facturacion.dte.pojo.pe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//objeto base para dte peru
public class ComprobanteDTEPE {

	public Object id;
	public String fecha;
    public Long tipo;   
    public String serie;
    public String rucEmisor;
    public String tipoDocumentoEmisor;
    public String rutCavali; 
    public String razonSocialEmisor;     
    public String nombreComercialEmisor;
    public String direccionEmisor;
    public String urbanizacionEmisor;
    public String provinciaEmisor;
    public String departamentoEmisor;
    public String distritoEmisor;
    public String codigoUbigeoEmisor;
    public String codigoPaisEmisor;
    public String emailEmisor;
    public String telefonoEmisor;
    
    
    public String rucReceptor;
    public String tipoDocumentoReceptor;
    public String iup;
    public String razonSocialReceptor;
    public String documentoIdentidadAsociado;
    public String direccionReceptor;
    public String urbanizacionReceptor;
    public String provinciaReceptor;
    public String departamentoReceptor;
    public String distritoReceptor;
    public String ubigeoReceptor;
    public String codigoUbigeoReceptor;
    public String codigoPaisReceptor;
    public String emailReceptor;
    public String codigoTipoOperacion;
    public String codigoTipoTributario;
    public String nombreTipoTributario;
    public String codigoInternacionalTributo;
    public String moneda; //PARA NOTA CREDITO, MONEDA LIQUIDACION COMPROBANTE
    public String formaPago;
    public String fechaVencimiento;
    public Double subTotal;
    public Double anticipo;
    public Double valor;
    public Double montoNetoGravada;
    public Double montoNetoInafecta;
    public Double montoNetoExonerada;
    public Double sumatoriaIgv;
    public Double sumatoriaIsc;
    public Double sumatoriaOtrosTributos;
    public Double sumatoriaOtrosCargos;
    public String tipoDescuentoTotal;
    public Double descuentoTotal;
    public BigDecimal importeTotal;
    public String leyenda;
    public String claveOrigen;
    public String estadoSunat;
    public Boolean forzarFolio;
    
    public List<DetalleDocumentoPE> detalleDocumentos = null;
    public List<DetallePolizaPE> detallePolizas = null;
    public List<ReferenciaDocumentoPE> referenciaDocumentos = null;
    public List<ParametroDocumentoPE> parametroDocumentos = null;
    
    
    public String tipoIdentificadorReceptor;    
    public String codigoIdentificadorReceptor;
    public String nacionalidadReceptor;
    public String productoOperacion;
    public String tipoOperacion;    
    public String monedaTransaccion;
    public Long cantidadTotal;
    public BigDecimal precioPromedio;
    
    public BigDecimal arancelBVL;
    public BigDecimal retribucionCAVALI;
    public BigDecimal contribucionSMV;
    public BigDecimal fondoLiquidacion;
    public BigDecimal fondoGarantia;
    public BigDecimal comisionSAB;
    public BigDecimal otrasComisiones;
    public String  monedaLiquidacion;
    public BigDecimal  importeLiquidacion;
    public BigDecimal comisionLiquidacion;
    public BigDecimal igvLiquidacion;
    public BigDecimal vacLiquidacion;
    public BigDecimal interesLiquidacion;
    public BigDecimal  totalLiquidacion;
    public String boveda;
    public String tipoCustodia;
    public String bancoCustodia;
    public String bancoLiquidador;
    public String formaCustodia;
    public String codigoBoveda;
    
    public String  fechaLiquidacion;
    
    public BigDecimal tasa;
    public BigDecimal plazo;
    public BigDecimal tipoCambio;
    public String  monedaImporte;
    
    public BigDecimal  importeAPagar;
    public String  reemplazada;
    public List<ReferenciaPolizaPE> referenciaPolizas = null;
    public String  parametropolizas;
    
    public String observacion;
          
    
    //OTROS DOCS DISTINTO DE POLIZA
    public String tipoDocumento;
	public String codigoTipoDocumento;
	public String lenguaje;
}

