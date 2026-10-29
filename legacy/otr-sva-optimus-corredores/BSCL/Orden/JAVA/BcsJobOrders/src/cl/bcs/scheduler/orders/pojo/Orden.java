package cl.bcs.scheduler.orders.pojo;

public class Orden {
	
	long idJob;
	int idOrdenPaso;
	String idClasificacionTipoInstrumento;
	String idTipoInstrumento;
	String idBolsa;
	String idTipoOperacion;
	String idCondicionDeLiquidacion;
	double precio;
	double cantidadDisponible;
	double cantidadAsignada;
	double monto;
	String tipoRuteo;
	String fechaIngreso;
	String fechaOperacion;
	int idLibroOrdenes;
	String idCodigoTipoPrecio;
	String especificaBolsa;
	int idInstrumento;
	
	public Orden(long idJob, int idOrdenPaso, String idClasificacionTipoInstrumento, String idTipoInstrumento,
			String idBolsa, String idTipoOperacion, String idCondicionDeLiquidacion, double precio,
			double cantidadDisponible, double cantidadAsignada, double monto, String tipoRuteo, String fechaIngreso,
			String fechaOperacion, int idLibroOrdenes, String idCodigoTipoPrecio,String especificaBolsa, int idInstrumento) {
		super();
		this.idJob = idJob;
		this.idOrdenPaso = idOrdenPaso;
		this.idClasificacionTipoInstrumento = idClasificacionTipoInstrumento;
		this.idTipoInstrumento = idTipoInstrumento;
		this.idBolsa = idBolsa;
		this.idTipoOperacion = idTipoOperacion;
		this.idCondicionDeLiquidacion = idCondicionDeLiquidacion;
		this.precio = precio;
		this.cantidadDisponible = cantidadDisponible;
		this.monto = monto;
		this.tipoRuteo = tipoRuteo;
		this.fechaIngreso = fechaIngreso;
		this.fechaOperacion = fechaOperacion;
		this.idLibroOrdenes = idLibroOrdenes;
		this.idCodigoTipoPrecio = idCodigoTipoPrecio;
		this.especificaBolsa = especificaBolsa;
		this.idInstrumento = idInstrumento;
	}
	public int getIdInstrumento() {
		return idInstrumento;
	}
	public void setIdInstrumento(int idInstrumento) {
		this.idInstrumento = idInstrumento;
	}
	public String getEspecificaBolsa() {
		return especificaBolsa;
	}
	public void setEspecificaBolsa(String especificaBolsa) {
		this.especificaBolsa = especificaBolsa;
	}
	public long getIdJob() {
		return idJob;
	}
	public void setIdJob(long idJob) {
		this.idJob = idJob;
	}
	public int getIdOrdenPaso() {
		return idOrdenPaso;
	}
	public void setIdOrdenPaso(int idOrdenPaso) {
		this.idOrdenPaso = idOrdenPaso;
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
	public String getTipoRuteo() {
		return tipoRuteo;
	}
	public void setTipoRuteo(String tipoRuteo) {
		this.tipoRuteo = tipoRuteo;
	}
	public String getFechaIngreso() {
		return fechaIngreso;
	}
	public void setFechaIngreso(String fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}
	public String getFechaOperacion() {
		return fechaOperacion;
	}
	public void setFechaOperacion(String fechaOperacion) {
		this.fechaOperacion = fechaOperacion;
	}
	public int getIdLibroOrdenes() {
		return idLibroOrdenes;
	}
	public void setIdLibroOrdenes(int idLibroOrdenes) {
		this.idLibroOrdenes = idLibroOrdenes;
	}
	public String getIdCodigoTipoPrecio() {
		return idCodigoTipoPrecio;
	}
	public void setIdCodigoTipoPrecio(String idCodigoTipoPrecio) {
		this.idCodigoTipoPrecio = idCodigoTipoPrecio;
	}
}
	
	
