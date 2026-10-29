package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Asignacion {
	public BigDecimal 	CantidadAsignada;
	public BigDecimal 	CantidadAsignadaRefact;
	public String 		CodigoIFRS;
	
	public String 		EsAPlazo;
	
	public Timestamp 	FechaAsignacion;
	public Timestamp 	FechaIngresoRegistro;
	public Date 		FechaParidad;
	public Date 		FechaTipoCambio;
	public Long 		FolioCartera;
	
	public Long 		IdAsignacion;
	public Long 		IdAsignacionRef;
	public Long 		IdAsignacionGrupo = 0L;
	public String 		IdEstadoAsignacion;
	public Long 		IdTransaccion;
	public Long 		IdOrden;
	public Long 		IdLibroOrdenes;
	public Long 		IdMovimientoComprobante;
	public Long 		IdComprobanteRefact;
	
	public BigDecimal 	MontoAsignado;
	public BigDecimal 	MontoAsignadoLocal;
	public BigDecimal 	MontoAsignadoLiquidacion;
	public BigDecimal 	MontoAsignadoInteres = BigDecimal.ZERO;
	
	public BigDecimal 	Paridad; //ORIGEN CAMBIO
	
	public BigDecimal 	TipoCambio;
	
	public BigDecimal 	ValorRescateAsignado;
	public BigDecimal 	ValorRescateAsignadoRefact;
	public Long 		IdAsignacionMasiva = 0L;
	public String 		IdTipoAsignacion = "";
	public String		AplicaComisionAsignacionMasiva = "";
			
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
