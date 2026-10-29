
package cl.bcs.facturacion.dte.pojo;

import java.util.ArrayList;
import java.util.List;

public class EnvioDTEPEComprobante {

	public Object id;
    public String tipo;
    public String fecha;
    public String rucEmisor;
    public String razonSocialEmisor;
    public String direccionEmisor;
    public String urbanizacionEmisor;
    public String provinciaEmisor;
    public String departamentoEmisor;
    public String distritoEmisor;
    public String telefonoEmisor;
    public String emailEmisor;
    public String rucReceptor;
    public String razonSocialReceptor;
    public String tipoIdentificadorReceptor;
    public Integer codigoIdentificadorReceptor;
    
    public String direccionReceptor;
    public String urbanizacionReceptor;
    public String provinciaReceptor;
    public String departamentoReceptor;
    public String distritoReceptor;
    
    public String ubigeoReceptor;
    public String nacionalidadReceptor;
    public String emailReceptor;
    public String productoOperacion;
    public String tipoOperacion;
    public String claveOrigen;
    public String monedaTransaccion;
    public Double cantidadTotal;
    public Double precioPromedio;
    public Double importeTotal;
    public Double arancelBVL;
    public Double retribucionCAVALI;
    public Double contribucionSMV;
    public Double fondoLiquidacion;
    public Double fondoGarantia;
    public Double comisionSAB;
    public Double otrasComisiones;
    public String  monedaLiquidacion;
    public Double  importeLiquidacion;
    public Double comisionLiquidacion;
    public Double igvLiquidacion;
    public Double vacLiquidacion;
    public Double interesLiquidacion;
    public Double  totalLiquidacion;
    public String  fechaLiquidacion;
    public String  fechaVencimiento;
    public Double tasa;
    public Double plazo;
    public Double tipoCambio;
    public String  monedaImporte;
    public Double  importeAPagar;
    public String  reemplazada;
    public String  referenciapolizas;
    public String  parametropolizas;
    public Boolean forzarFolio;
    
    public List<AjusteDocumento> ajusteDocumentos = null;
    public List<DetallePoliza> detallePolizas = null;
    public List<ReferenciaDocumento> referenciaDocumentos = null;
    
	
   
    
}

