package cl.bcs.tesoreria.pojo;

import java.math.BigDecimal;
import cl.bcs.utils.Utils;


public class IngresoEgreso {
	public Long					IdIngresoEgreso; 
	public Long					IdEmpresa; 
	public String				IdModulo; 
	public Long					IdUsuario; 
	public String				IdTipoMovimiento; 
	public String				IdConcepto; 
	public Long					IdCaja; 
	public String				IdMoneda; 
	public BigDecimal			Monto; 
	public java.sql.Date		FechaLiquidacion;
	public java.sql.Timestamp	FechaIngresoRegistro;   //Fecha/Hora del servidor
	public java.sql.Date		FechaMovimiento;        //Fecha de Sistema
	public String				IdTipoPersona; 
	public Long					IdPersona; 
	public String				NombrePersona; 
	public String				IdTipoDocumentoIdentidad; 
	public String				DocumentoIdentidad; 
	public String				IdTipoPersonaTitularPago; 
	public Long					IdPersonaTitularPago;
	public String				NombrePersonaTitularPago; 
	public Long					IdPortafolioTitularPago; 
	public String				SecuenciaAgrupacion; 
	public String				Glosa; 
	public Long					IdEmpleadoDigitador; 
	public Long					IdSucursalEmpleadoDigitador; 
	public String				IdTipoComprobanteOrigen; 
	public Long					IdComprobanteOrigen; 
	public java.sql.Timestamp	FechaComprobanteOrigen;
	public java.sql.Timestamp	FechaLiquidacionComprobanteOrigen;
	public String				IdTipoProducto; 
	public String				IdTipoOperacion; 
	public Long					IdOperacion; 
	public java.sql.Timestamp	FechaOperacion;
	public String				IdCondicionLiquidacion; 
	public String				IdBolsa; 
	public String				IdMonedaOperacion; 
	public BigDecimal			MontoOperacion; 
	public java.sql.Timestamp	FechaParidad;
	public String				IdTipoReajuste; 
	public BigDecimal			ValorParidad; 
	public Long					FolioCartera; 
	public Long					IdInstrumento; 
	public String				IdTipoInstrumento; 
	public String				IdClasificacionTipoInstrumento; 
	public String				EsBilateral; 
	public String				TitularPago;
	public String				EsIngresoAutomatico; 
	public String				AgrupaComprobante; 
	public Long					IdUsuarioBloqueo; 
	public java.sql.Timestamp	FechaHoraBloqueo;
	public Long					IdCarga;  
	public String				IdEstado; 
	public String				MueveCuentaInversion; 
	public Long					SecuenciaRelacionada; 
	public String				NeteaComprobante;
	public Long					IdUsuarioModificacion; 
	public java.sql.Timestamp	FechaHoraModificacion;
	public Long					IdPortafolio; 
	public Long					IdUsuarioEmision;  	
	public java.sql.Timestamp	FechaHoraEmision;  	
	public Long					IdUsuarioAnulacion; 
	public java.sql.Timestamp	FechaHoraAnulacion; 
	public String				IdTipoDocumentoIdentidadTitularPago; 
	public String				DocumentoIdentidadTitularPago;
	public String				EsTraspaso = "N";
	public String               IdTipoCanalIngreso;
	
	public String toString() {
		return Utils.printJson(this, false);
	}			
}
