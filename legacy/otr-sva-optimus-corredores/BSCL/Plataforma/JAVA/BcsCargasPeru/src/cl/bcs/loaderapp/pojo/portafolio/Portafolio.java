package cl.bcs.loaderapp.pojo.portafolio;

public class Portafolio {
	public String CodigoPortafolio;
	public String Proposito;
	public String TipoDocumentoIdentidadCliente;
	public String DocumentoIdentidadCliente;
	public String IdEstado;
	public String FechaBloqueo;
	public String ComentarioBloqueo;
	public String IdTipoBloqueo;
	public String IdPerfilComercialBaseOrigen;
	public String IdPerfilProductos;
	public String IdPerfilCobros;
	public String DescripcionPortafolio;
	public String IdPerfilLimites;
	public String NumeroFicha;
	public String IdNombreFantasia;
	public String Alias;
	public String TipoDocumentoIdentidadBrokerExtranjero;
	public String DocumentoIdentidadBrokerExtranjero;

	public String getCodigoPortafolio() {
		return CodigoPortafolio;
	}
	public void setCodigoPortafolio(String codigoPortafolio) {
		CodigoPortafolio = codigoPortafolio;
	}
	public String getProposito() {
		return Proposito;
	}
	public void setProposito(String proposito) {
		Proposito = proposito;
	}
	public String getTipoDocumentoIdentidadCliente() {
		return TipoDocumentoIdentidadCliente;
	}
	public void setTipoDocumentoIdentidadCliente(String tipoDocumentoIdentidadCliente) {
		TipoDocumentoIdentidadCliente = tipoDocumentoIdentidadCliente;
	}
	public String getDocumentoIdentidadCliente() {
		return DocumentoIdentidadCliente;
	}
	public void setDocumentoIdentidadCliente(String documentoIdentidadCliente) {
		DocumentoIdentidadCliente = documentoIdentidadCliente;
	}
	public String getIdEstado() {
		return IdEstado;
	}
	public void setIdEstado(String idEstado) {
		IdEstado = idEstado;
	}
	public String getFechaBloqueo() {
		return FechaBloqueo;
	}
	public void setFechaBloqueo(String fechaBloqueo) {
		FechaBloqueo = fechaBloqueo;
	}
	public String getComentarioBloqueo() {
		return ComentarioBloqueo;
	}
	public void setComentarioBloqueo(String comentarioBloqueo) {
		ComentarioBloqueo = comentarioBloqueo;
	}
	public String getIdTipoBloqueo() {
		return IdTipoBloqueo;
	}
	public void setIdTipoBloqueo(String idTipoBloqueo) {
		IdTipoBloqueo = idTipoBloqueo;
	}
	public String getIdPerfilComercialBaseOrigen() {
		return IdPerfilComercialBaseOrigen;
	}
	public void setIdPerfilComercialBaseOrigen(String idPerfilComercialBaseOrigen) {
		IdPerfilComercialBaseOrigen = idPerfilComercialBaseOrigen;
	}
	public String getIdPerfilProductos() {
		return IdPerfilProductos;
	}
	public void setIdPerfilProductos(String idPerfilProductos) {
		IdPerfilProductos = idPerfilProductos;
	}
	public String getIdPerfilCobros() {
		return IdPerfilCobros;
	}
	public void setIdPerfilCobros(String idPerfilCobros) {
		IdPerfilCobros = idPerfilCobros;
	}
	public String getDescripcionPortafolio() {
		return DescripcionPortafolio;
	}
	public void setDescripcionPortafolio(String descripcionPortafolio) {
		DescripcionPortafolio = descripcionPortafolio;
	}
	public String getIdPerfilLimites() {
		return IdPerfilLimites;
	}
	public void setIdPerfilLimites(String idPerfilLimites) {
		IdPerfilLimites = idPerfilLimites;
	}
	public String getNumeroFicha() {
		return NumeroFicha;
	}
	public void setNumeroFicha(String numeroFicha) {
		NumeroFicha = numeroFicha;
	}
	public String getIdNombreFantasia() {
		return IdNombreFantasia;
	}
	public void setIdNombreFantasia(String idNombreFantasia) {
		IdNombreFantasia = idNombreFantasia;
	}
	public String getAlias() {
		return Alias;
	}
	public void setAlias(String alias) {
		Alias = alias;
	}
	public String getTipoDocumentoIdentidadBrokerExtranjero() {
		return TipoDocumentoIdentidadBrokerExtranjero;
	}
	public void setTipoDocumentoIdentidadBrokerExtranjero(String tipoDocumentoIdentidadBrokerExtranjero) {
		TipoDocumentoIdentidadBrokerExtranjero = tipoDocumentoIdentidadBrokerExtranjero;
	}
	public String getDocumentoIdentidadBrokerExtranjero() {
		return DocumentoIdentidadBrokerExtranjero;
	}
	public void setDocumentoIdentidadBrokerExtranjero(String documentoIdentidadBrokerExtranjero) {
		DocumentoIdentidadBrokerExtranjero = documentoIdentidadBrokerExtranjero;
	}
	
	@Override
	public String toString() {
		return String.format(
				"Portafolio [CodigoPortafolio=%s, Proposito=%s, TipoDocumentoIdentidadCliente=%s, DocumentoIdentidadCliente=%s, IdEstado=%s, FechaBloqueo=%s, ComentarioBloqueo=%s, IdTipoBloqueo=%s, IdPerfilComercialBaseOrigen=%s, IdPerfilProductos=%s, IdPerfilCobros=%s, DescripcionPortafolio=%s, IdPerfilLimites=%s, NumeroFicha=%s, IdNombreFantasia=%s, Alias=%s, TipoDocumentoIdentidadBrokerExtranjero=%s, DocumentoIdentidadBrokerExtranjero=%s]",
				CodigoPortafolio, Proposito, TipoDocumentoIdentidadCliente, DocumentoIdentidadCliente, IdEstado,
				FechaBloqueo, ComentarioBloqueo, IdTipoBloqueo, IdPerfilComercialBaseOrigen, IdPerfilProductos,
				IdPerfilCobros, DescripcionPortafolio, IdPerfilLimites, NumeroFicha, IdNombreFantasia, Alias,
				TipoDocumentoIdentidadBrokerExtranjero, DocumentoIdentidadBrokerExtranjero);
	}
}
