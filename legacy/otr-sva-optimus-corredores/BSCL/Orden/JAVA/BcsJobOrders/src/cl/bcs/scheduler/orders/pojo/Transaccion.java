package cl.bcs.scheduler.orders.pojo;

public class Transaccion {
 

	public int getIdTransaccionPaso() {
		return idTransaccionPaso;
	}
	public void setIdTransaccionPaso(int idTransaccionPaso) {
		this.idTransaccionPaso = idTransaccionPaso;
	}
	public String getIdClasificacionTipoInstrumento() {
		return idClasificacionTipoInstrumento;
	}
	public void setIdClasificacionTipoInstrumento(String idClasificacionTipoInstrumento) {
		this.idClasificacionTipoInstrumento = idClasificacionTipoInstrumento;
	}
	public String getIdTipoInstrumento() {
		return idTipoInstrumento;
	}
	public void setIdTipoInstrumento(String idTipoInstrumento) {
		this.idTipoInstrumento = idTipoInstrumento;
	}
	public String getIdBolsa() {
		return idBolsa;
	}
	public void setIdBolsa(String idBolsa) {
		this.idBolsa = idBolsa;
	}
	public String getIdTipoOperacion() {
		return idTipoOperacion;
	}
	public void setIdTipoOperacion(String idTipoOperacion) {
		this.idTipoOperacion = idTipoOperacion;
	}
	public String getIdCondicionDeLiquidacion() {
		return idCondicionDeLiquidacion;
	}
	public void setIdCondicionDeLiquidacion(String idCondicionDeLiquidacion) {
		this.idCondicionDeLiquidacion = idCondicionDeLiquidacion;
	}
	public double getPrecio() {
		return precio;
	}
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	public double getCantidadTotal() {
		return cantidadTotal;
	}
	public void setCantidadTotal(double cantidadTotal) {
		this.cantidadTotal = cantidadTotal;
	}
	public double getCantidadDisponible() {
		return cantidadDisponible;
	}
	public void setCantidadDisponible(double cantidadDisponible) {
		this.cantidadDisponible = cantidadDisponible;
	}
	public double getCantidadAsignada() {
		return cantidadAsignada;
	}
	public void setCantidadAsignada(double cantidadAsignada) {
		this.cantidadAsignada = cantidadAsignada;
	}
	public double getMonto() {
		return monto;
	}
	public void setMonto(double monto) {
		this.monto = monto;
	}
	public String getFechaIngresoRegistro() {
		return fechaIngresoRegistro;
	}
	public void setFechaIngresoRegistro(String fechaIngresoRegistro) {
		this.fechaIngresoRegistro = fechaIngresoRegistro;
	}
	public String getFechaOperacionTransaccion() {
		return fechaOperacionTransaccion;
	}
	public void setFechaOperacionTransaccion(String fechaOperacionTransaccion) {
		this.fechaOperacionTransaccion = fechaOperacionTransaccion;
	}
	public int getIdInstrumento() {
		return idInstrumento;
	}
	public void setIdInstrumento(int idInstrumento) {
		this.idInstrumento = idInstrumento;
	} 
	
	
	public Transaccion(int idTransaccionPaso, String idClasificacionTipoInstrumento, String idTipoInstrumento,
			String idBolsa, String idTipoOperacion, String idCondicionDeLiquidacion, double precio,
			double cantidadTotal, double cantidadDisponible, double cantidadAsignada, double monto,
			String fechaIngresoRegistro, String fechaOperacionTransaccion, int idInstrumento) {
		super();
		this.idTransaccionPaso = idTransaccionPaso;
		this.idClasificacionTipoInstrumento = idClasificacionTipoInstrumento;
		this.idTipoInstrumento = idTipoInstrumento;
		this.idBolsa = idBolsa;
		this.idTipoOperacion = idTipoOperacion;
		this.idCondicionDeLiquidacion = idCondicionDeLiquidacion;
		this.precio = precio;
		this.cantidadTotal = cantidadTotal;
		this.cantidadDisponible = cantidadDisponible;
		this.cantidadAsignada = cantidadAsignada;
		this.monto = monto;
		this.fechaIngresoRegistro = fechaIngresoRegistro;
		this.fechaOperacionTransaccion = fechaOperacionTransaccion;
		this.idInstrumento = idInstrumento;
	}
	int idTransaccionPaso;
	String idClasificacionTipoInstrumento;
	String idTipoInstrumento;
	String idBolsa;
	String idTipoOperacion;
	String idCondicionDeLiquidacion;
	double precio,cantidadTotal,cantidadDisponible,cantidadAsignada,monto;
	String fechaIngresoRegistro;
	String fechaOperacionTransaccion;
	int	   idInstrumento;
	
	

}
