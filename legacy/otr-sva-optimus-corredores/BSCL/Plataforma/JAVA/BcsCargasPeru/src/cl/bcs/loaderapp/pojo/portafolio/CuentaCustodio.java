package cl.bcs.loaderapp.pojo.portafolio;

public class CuentaCustodio {
	
	private String TipoDocumentoIdentidad;
	private String DocumentoIdentidadCliente;
	private String CodigoPortafolio;
	private String IdBoveda;
	private String Codigo;
	private String IdEstado;
	private String IdModeloCustodia;
	private String IdTipoCustodia;
	private String IdTipoCuentaCustodia;
	private String DocumentoIdentidadPersonaBancoCustodio;
	private String DocumentoIdentidadPersonaBancoLiquidador;
	
	public String getTipoDocumentoIdentidad() {
		return TipoDocumentoIdentidad;
	}
	public void setTipoDocumentoIdentidad(String tipoDocumentoIdentidad) {
		TipoDocumentoIdentidad = tipoDocumentoIdentidad;
	}
	public String getDocumentoIdentidadCliente() {
		return DocumentoIdentidadCliente;
	}
	public void setDocumentoIdentidadCliente(String documentoIdentidadCliente) {
		DocumentoIdentidadCliente = documentoIdentidadCliente;
	}
	public String getCodigoPortafolio() {
		return CodigoPortafolio;
	}
	public void setCodigoPortafolio(String codigoPortafolio) {
		CodigoPortafolio = codigoPortafolio;
	}
	public String getIdBoveda() {
		return IdBoveda;
	}
	public void setIdBoveda(String idBoveda) {
		IdBoveda = idBoveda;
	}
	public String getCodigo() {
		return Codigo;
	}
	public void setCodigo(String codigo) {
		Codigo = codigo;
	}
	public String getIdEstado() {
		return IdEstado;
	}
	public void setIdEstado(String idEstado) {
		IdEstado = idEstado;
	}
	public String getIdModeloCustodia() {
		return IdModeloCustodia;
	}
	public void setIdModeloCustodia(String idModeloCustodia) {
		IdModeloCustodia = idModeloCustodia;
	}
	public String getIdTipoCustodia() {
		return IdTipoCustodia;
	}
	public void setIdTipoCustodia(String idTipoCustodia) {
		IdTipoCustodia = idTipoCustodia;
	}
	public String getIdTipoCuentaCustodia() {
		return IdTipoCuentaCustodia;
	}
	public void setIdTipoCuentaCustodia(String idTipoCuentaCustodia) {
		IdTipoCuentaCustodia = idTipoCuentaCustodia;
	}
	public String getDocumentoIdentidadPersonaBancoCustodio() {
		return DocumentoIdentidadPersonaBancoCustodio;
	}
	public void setDocumentoIdentidadPersonaBancoCustodio(String documentoIdentidadPersonaBancoCustodio) {
		DocumentoIdentidadPersonaBancoCustodio = documentoIdentidadPersonaBancoCustodio;
	}
	public String getDocumentoIdentidadPersonaBancoLiquidador() {
		return DocumentoIdentidadPersonaBancoLiquidador;
	}
	public void setDocumentoIdentidadPersonaBancoLiquidador(String documentoIdentidadPersonaBancoLiquidador) {
		DocumentoIdentidadPersonaBancoLiquidador = documentoIdentidadPersonaBancoLiquidador;
	}
	@Override
	public String toString() {
		return String.format(
				"CuentaCustodio [TipoDocumentoIdentidad=%s, DocumentoIdentidadCliente=%s, CodigoPortafolio=%s, IdBoveda=%s, Codigo=%s, IdEstado=%s, IdModeloCustodia=%s, IdTipoCustodia=%s, IdTipoCuentaCustodia=%s, DocumentoIdentidadPersonaBancoCustodio=%s, DocumentoIdentidadPersonaBancoLiquidador=%s]",
				TipoDocumentoIdentidad, DocumentoIdentidadCliente, CodigoPortafolio, IdBoveda, Codigo, IdEstado,
				IdModeloCustodia, IdTipoCustodia, IdTipoCuentaCustodia, DocumentoIdentidadPersonaBancoCustodio,
				DocumentoIdentidadPersonaBancoLiquidador);
	}
}
