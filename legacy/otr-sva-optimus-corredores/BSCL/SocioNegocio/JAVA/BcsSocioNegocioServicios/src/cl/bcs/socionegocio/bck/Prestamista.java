package cl.bcs.socionegocio.bck;
/**
 * @author Sebastian Hechenleitner
 */

public class Prestamista {
	private String rut;
	private String codCorr;
	private String cantidad;
	
	public Prestamista(String rut, String codCorr, String cantidad) {
		this.rut = rut;
		this.codCorr = codCorr;
		this.cantidad = cantidad;
	}
	
	public String getRut() {
		return rut;
	}
	public void setRut(String rut) {
		this.rut = rut;
	}
	public String getCodCorr() {
		return codCorr;
	}
	public void setCodCorr(String codCorr) {
		this.codCorr = codCorr;
	}
	public String getCantidad() {
		return cantidad;
	}
	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}
}