package cl.bcs.tesoreria.generarArchivoCargos;

public class FormatoArchivoDetalle {
	private String 	CodigoFormatoArchivo;
	private Long 	IdFormatoArchivoDetalle;
	private Integer TipoRegistro;	
	private String 	Campo;	
	private String 	TipoDato;	
	private Integer Largo;	
	private Integer Decimales;	
	private String 	ValorDefecto;	
	private Integer Orden;	
	private String 	Descripcion;
	private String 	Formato;
 
	public String getCodigoFormatoArchivo() {
		return CodigoFormatoArchivo;
	}
	
	public Long getIdFormatoArchivoDetalle() {
		return IdFormatoArchivoDetalle;
	}
	public Integer getTipoRegistro() {
		return TipoRegistro;
	}
	public String getCampo() {
		return Campo;
	}
	public String getTipoDato() {
		return TipoDato;
	}
	public Integer getLargo() {
		return Largo;
	}
	public Integer getDecimales() {
		return Decimales;
	}
	public String getValorDefecto() {
		return ValorDefecto;
	}
	public Integer getOrden() {
		return Orden;
	}
	public String getDescripcion() {
		return Descripcion;
	}
	public String getFormato() {
		return Formato;
	}

	public void setCodigoFormatoArchivo(String codigoFormatoArchivo) {
		CodigoFormatoArchivo = codigoFormatoArchivo;
	}	
	
	public void setIdFormatoArchivoDetalle(Long idFormatoArchivoDetalle) {
		IdFormatoArchivoDetalle = idFormatoArchivoDetalle;
	}
	public void setTipoRegistro(Integer tipoRegistro) {
		TipoRegistro = tipoRegistro;
	}
	public void setCampo(String campo) {
		Campo = campo;
	}
	public void setTipoDato(String tipoDato) {
		TipoDato = tipoDato;
	}
	public void setLargo(Integer largo) {
		Largo = largo;
	}
	public void setDecimales(Integer decimales) {
		Decimales = decimales;
	}
	public void setValorDefecto(String valorDefecto) {
		ValorDefecto = valorDefecto;
	}
	public void setOrden(Integer orden) {
		Orden = orden;
	}
	public void setDescripcion(String descripcion) {
		Descripcion = descripcion;
	}	
	public void setFormato(String formato) {
		Formato = formato;
	}	
	public FormatoArchivoDetalle() {
	}	
		
}
