package cl.bcs.loaderapp.pojo.socionegocio;

public class Anotacion {
	private String DocumentoIdentidad;
	private String TipoDocumentoIdentidad;
	private String TipoPersona;
	private String Titulo;
	private String Detalle;
	private String FechaAnotacion;
	private String IdMotivo;
	private String IdAmbito;
	private String IdRelevancia;
	private String FechaDesde;
	private String FechaHasta;
	public String getDocumentoIdentidad() {
		return DocumentoIdentidad;
	}
	public void setDocumentoIdentidad(String documentoIdentidad) {
		DocumentoIdentidad = documentoIdentidad;
	}
	public String getTipoDocumentoIdentidad() {
		return TipoDocumentoIdentidad;
	}
	public void setTipoDocumentoIdentidad(String tipoDocumentoIdentidad) {
		TipoDocumentoIdentidad = tipoDocumentoIdentidad;
	}
	public String getTipoPersona() {
		return TipoPersona;
	}
	public void setTipoPersona(String tipoPersona) {
		TipoPersona = tipoPersona;
	}
	public String getTitulo() {
		return Titulo;
	}
	public void setTitulo(String titulo) {
		Titulo = titulo;
	}
	public String getDetalle() {
		return Detalle;
	}
	public void setDetalle(String detalle) {
		Detalle = detalle;
	}
	public String getFechaAnotacion() {
		return FechaAnotacion;
	}
	public void setFechaAnotacion(String fechaAnotacion) {
		FechaAnotacion = fechaAnotacion;
	}
	public String getIdMotivo() {
		return IdMotivo;
	}
	public void setIdMotivo(String idMotivo) {
		IdMotivo = idMotivo;
	}
	public String getIdAmbito() {
		return IdAmbito;
	}
	public void setIdAmbito(String idAmbito) {
		IdAmbito = idAmbito;
	}
	public String getIdRelevancia() {
		return IdRelevancia;
	}
	public void setIdRelevancia(String idRelevancia) {
		IdRelevancia = idRelevancia;
	}
	public String getFechaDesde() {
		return FechaDesde;
	}
	public void setFechaDesde(String fechaDesde) {
		FechaDesde = fechaDesde;
	}
	public String getFechaHasta() {
		return FechaHasta;
	}
	public void setFechaHasta(String fechaHasta) {
		FechaHasta = fechaHasta;
	}
	@Override
	public String toString() {
		return String.format(
				"Anotacion [DocumentoIdentidad=%s, TipoDocumentoIdentidad=%s, TipoPersona=%s, Titulo=%s, Detalle=%s, FechaAnotacion=%s, IdMotivo=%s, IdAmbito=%s, IdRelevancia=%s, FechaDesde=%s, FechaHasta=%s]",
				DocumentoIdentidad, TipoDocumentoIdentidad, TipoPersona, Titulo, Detalle, FechaAnotacion, IdMotivo,
				IdAmbito, IdRelevancia, FechaDesde, FechaHasta);
	}
	
	
	
}
