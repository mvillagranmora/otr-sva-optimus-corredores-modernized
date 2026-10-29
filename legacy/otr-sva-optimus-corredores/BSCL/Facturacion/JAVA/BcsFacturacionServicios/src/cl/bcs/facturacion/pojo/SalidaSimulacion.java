package cl.bcs.facturacion.pojo;

import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class SalidaSimulacion {
	
	public BigDecimal  CantidadAsignacion;
	public String 	   CodigoPortafolio;
	public String 	   DocumentoIdentidad;
	public java.sql.Timestamp 	   FechaTransaccion;
	public Long 	   FolioCarteraAsignada;
	public String 	   FolioTransaccion;
	public String 	   Glosa;
	public String 	   IdBolsa;
	public String 	   IdBoveda;
	public Long 	   IdCliente;
	public String 	   IdFormaCustodia;
	public Long 	   IdInterno;
	public String 	   IdMonedaInstrumento;
	public Long 	   IdCuentaCustodioPortafolio;
	public String 	   CuentaCustodioPortafolio;
	public String 	   IdMonedaLiquidacionOrden; 		
	public Long 	   IdMovimiento;
	public Long 	   IdOrden;
	public Long 	   IdPreferenciaPagoPortafolio;
	public Long 	   IdPortafolio;
	public String 	   IdTipoInstrumento;
	public Long 	   IdTransaccion;
	public String 	   IdTipoCanalIngreso;
	public String 	   Instrumento;
	public String 	   NombreCompleto;
	public String 	   PagoCobroCuentaInversion;			
	public Long  	   PlazoVentaCorta;
	public BigDecimal  TasaVentaCorta;
	public BigDecimal  TipoCambio;
	public Long 	   ValorRescateAsignadoSimulacion;
	public String 	   IdTipoOperacion;
	public String 	   IdTipoProducto;
	public BigDecimal  PrecioPromedio;
	public String 	   InstrumentoNegociacionTran;
	
	public SalidaSimulacion() {
		this.CodigoPortafolio="";
		this.FechaTransaccion= new Timestamp(0L);
		this.FolioCarteraAsignada=0L;
		this.FolioTransaccion="0";
		this.Glosa="";
		this.IdBolsa="";
		this.IdBoveda="DCV";
		this.IdCliente=0L;
		this.IdFormaCustodia="";
		this.IdMonedaInstrumento="";
		this.IdCuentaCustodioPortafolio=null;
		this.CuentaCustodioPortafolio="";
		this.IdMonedaLiquidacionOrden=""; 	
		this.IdOrden=0L;
		this.IdPreferenciaPagoPortafolio=null;
		this.IdPortafolio=0L;
		this.IdTipoInstrumento="";
		this.IdTipoCanalIngreso="";
		this.NombreCompleto="";
		this.PagoCobroCuentaInversion="";		
		this.PlazoVentaCorta=0L;
		this.TasaVentaCorta=BigDecimal.ZERO;
		this.TipoCambio=BigDecimal.ZERO;
		this.ValorRescateAsignadoSimulacion=0L;
		this.PrecioPromedio=BigDecimal.ZERO;
		this.InstrumentoNegociacionTran="";
	}
	
	public SalidaSimulacion (Long IdInterno,Long IdTransaccion, Long IdMovimiento, BigDecimal CantidadAsignacion, Long IdPortafolio, String Instrumento,String DocumentoIdentidad){
		
		this.IdInterno= IdInterno;
		this.IdTransaccion=IdTransaccion;
		this.IdMovimiento =IdTransaccion;
		this.CantidadAsignacion=CantidadAsignacion;
		this.IdPortafolio=IdPortafolio;
		this.Instrumento = Instrumento;
		this.DocumentoIdentidad=DocumentoIdentidad;
		
		this.CodigoPortafolio="";
		this.FechaTransaccion= new Timestamp(0L);
		this.FolioCarteraAsignada=0L;
		this.FolioTransaccion="0";
		this.Glosa="";
		this.IdBolsa="";
		this.IdBoveda="DCV";
		this.IdCliente=0L;
		this.IdFormaCustodia="";
		this.IdMonedaInstrumento="";
		this.IdCuentaCustodioPortafolio=null;
		this.CuentaCustodioPortafolio="";
		this.IdMonedaLiquidacionOrden=""; 	
		this.IdOrden=0L;
		this.IdPreferenciaPagoPortafolio=null;
		this.IdPortafolio=0L;
		this.IdTipoInstrumento="";
		this.IdTipoCanalIngreso="";
		this.NombreCompleto="";
		this.PagoCobroCuentaInversion="";		
		this.PlazoVentaCorta=0L;
		this.TasaVentaCorta=BigDecimal.ZERO;
		this.TipoCambio=BigDecimal.ZERO;
		this.ValorRescateAsignadoSimulacion=0L;
		this.PrecioPromedio=BigDecimal.ZERO;
		this.InstrumentoNegociacionTran="";
	}

	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
