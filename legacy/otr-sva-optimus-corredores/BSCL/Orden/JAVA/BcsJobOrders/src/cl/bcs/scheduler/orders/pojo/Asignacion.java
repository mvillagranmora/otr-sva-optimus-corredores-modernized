package cl.bcs.scheduler.orders.pojo;

public class Asignacion 
{
    public Asignacion(int idAsignacionPaso, String idEstadoFacturacion, int cantidadAsignada, String fechaAsignacion,
			int idTransaccionPaso, int idOrdenPaso, int montoTotal) {
		super();
		this.idAsignacionPaso = idAsignacionPaso;
		this.idEstadoFacturacion = idEstadoFacturacion;
		this.cantidadAsignada = cantidadAsignada;
		this.fechaAsignacion = fechaAsignacion;
		this.idTransaccionPaso = idTransaccionPaso;
		this.idOrdenPaso = idOrdenPaso;
		this.montoTotal = montoTotal;
	}
	int idAsignacionPaso;
    String idEstadoFacturacion;
    int cantidadAsignada;
    String fechaAsignacion;
    int idTransaccionPaso;
    int idOrdenPaso;
    int montoTotal;
	public int getMontoTotal() {
		return montoTotal;
	}
	public void setMontoTotal(int montoTotal) {
		this.montoTotal = montoTotal;
	}
	public Asignacion(int idAsignacionPaso, String idEstadoFacturacion, int cantidadAsignada, String fechaAsignacion,
			int idTransaccionPaso, int idOrdenPaso) {
 
		this.idAsignacionPaso = idAsignacionPaso;
		this.idEstadoFacturacion = idEstadoFacturacion;
		this.cantidadAsignada = cantidadAsignada;
		this.fechaAsignacion = fechaAsignacion;
		this.idTransaccionPaso = idTransaccionPaso;
		this.idOrdenPaso = idOrdenPaso;
	}
	public int getIdAsignacionPaso() {
		return idAsignacionPaso;
	}
	public void setIdAsignacionPaso(int idAsignacionPaso) {
		this.idAsignacionPaso = idAsignacionPaso;
	}
	public String getIdEstadoFacturacion() {
		return idEstadoFacturacion;
	}
	public void setIdEstadoFacturacion(String idEstadoFacturacion) {
		this.idEstadoFacturacion = idEstadoFacturacion;
	}
	public int getCantidadAsignada() {
		return cantidadAsignada;
	}
	public void setCantidadAsignada(int cantidadAsignada) {
		this.cantidadAsignada = cantidadAsignada;
	}
	public String getFechaAsignacion() {
		return fechaAsignacion;
	}
	public void setFechaAsignacion(String fechaAsignacion) {
		this.fechaAsignacion = fechaAsignacion;
	}
	public int getIdTransaccionPaso() {
		return idTransaccionPaso;
	}
	public void setIdTransaccionPaso(int idTransaccionPaso) {
		this.idTransaccionPaso = idTransaccionPaso;
	}
	public int getIdOrdenPaso() {
		return idOrdenPaso;
	}
	public void setIdOrdenPaso(int idOrdenPaso) {
		this.idOrdenPaso = idOrdenPaso;
	}
	public Asignacion()
	{
		
	}
    
}
