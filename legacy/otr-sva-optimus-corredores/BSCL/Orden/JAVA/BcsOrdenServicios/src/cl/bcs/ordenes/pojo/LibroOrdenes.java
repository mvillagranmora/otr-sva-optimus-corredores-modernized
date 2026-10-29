package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class LibroOrdenes {
	public Long IdLibroOrdenes;
	public Long IdEmpresa;
	public String NombreLibroOrdenes;
	public String IdTipoLibroOrden;
	public String IdEstado;
	public String IdTipoAsignacion;
	public Long Prioridad;
	public Date FechaCreacion;
	public Date FechaModificacion;
	public Timestamp UltimaFechaDeEjecucion;
	public String Borrado;
	public String LibroPorDefecto;
	public String AsignacionAutomatica;
	public Long IdInstrumento;
	public Long IdClienteOfertante;
	public BigDecimal Cantidad;
	public String IdPlantillaCobros;
	public Date FechaApertura;
	public Date FechaCierre;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
