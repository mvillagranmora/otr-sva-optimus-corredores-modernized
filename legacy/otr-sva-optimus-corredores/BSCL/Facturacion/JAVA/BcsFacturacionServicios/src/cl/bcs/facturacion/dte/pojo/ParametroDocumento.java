
package cl.bcs.facturacion.dte.pojo;


public class ParametroDocumento {

    private String nombre;
    private String valor;
    
    /**
	 * @param nombre
	 * @param valor
	 */
	public ParametroDocumento(String nombre, String valor) {
		this.nombre = nombre;
		this.valor = valor;
	}
	
	public ParametroDocumento() {}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	


}
