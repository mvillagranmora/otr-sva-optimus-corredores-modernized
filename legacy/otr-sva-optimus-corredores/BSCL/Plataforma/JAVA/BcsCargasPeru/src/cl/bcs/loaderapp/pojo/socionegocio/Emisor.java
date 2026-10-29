package cl.bcs.loaderapp.pojo.socionegocio;

import cl.bcs.utils.Utils;

public class Emisor {
	public String IdEmisor;
	public String CodigoEmisor;
	public String CodigoRegulador;
	public String EmiteIRF;
	public String IdEstado;
	public String TipoPersona;
	public String TipoDocumentoIdentidad;
	public String DocumentoIdentidad;
	public String TipoDocumentoIdentidadAsociado;
	public String DocumentoIdentidadAsociado;
	public String RazonSocial;
	public String Pais;
	public String Ciudad;
	public int Linea;

	public int getLinea() {
		return Linea;
	}
	public void setLinea(int linea) {
		Linea = linea;
	}
	public String getIdEmisor() {
		return IdEmisor;
	}
	public void setIdEmisor(String idEmisor) {
		IdEmisor = idEmisor;
	}
	public String getCodigoEmisor() {
		return CodigoEmisor;
	}
	public void setCodigoEmisor(String codigoEmisor) {
		CodigoEmisor = codigoEmisor;
	}
	public String getCodigoRegulador() {
		return CodigoRegulador;
	}
	public void setCodigoRegulador(String codigoRegulador) {
		CodigoRegulador = codigoRegulador;
	}
	public String getEmiteIRF() {
		return EmiteIRF;
	}
	public void setEmiteIRF(String emiteIRF) {
		EmiteIRF = emiteIRF;
	}
	public String getIdEstado() {
		return IdEstado;
	}
	public void setIdEstado(String idEstado) {
		IdEstado = idEstado;
	}
	public String getTipoPersona() {
		return TipoPersona;
	}
	public void setTipoPersona(String tipoPersona) {
		TipoPersona = tipoPersona;
	}
	public String getTipoDocumentoIdentidad() {
		return TipoDocumentoIdentidad;
	}
	public void setTipoDocumentoIdentidad(String tipoDocumentoIdentidad) {
		TipoDocumentoIdentidad = tipoDocumentoIdentidad;
	}
	public String getDocumentoIdentidad() {
		return DocumentoIdentidad;
	}
	public void setDocumentoIdentidad(String documentoIdentidad) {
		DocumentoIdentidad = documentoIdentidad;
	}
	public String getTipoDocumentoIdentidadAsociado() {
		return TipoDocumentoIdentidadAsociado;
	}
	public void setTipoDocumentoIdentidadAsociado(String tipoDocumentoIdentidadAsociado) {
		TipoDocumentoIdentidadAsociado = tipoDocumentoIdentidadAsociado;
	}
	public String getDocumentoIdentidadAsociado() {
		return DocumentoIdentidadAsociado;
	}
	public void setDocumentoIdentidadAsociado(String documentoIdentidadAsociado) {
		DocumentoIdentidadAsociado = documentoIdentidadAsociado;
	}
	public String getRazonSocial() {
		return RazonSocial;
	}
	public void setRazonSocial(String razonSocial) {
		RazonSocial = razonSocial;
	}
	public String getPais() {
		return Pais;
	}
	public void setPais(String pais) {
		Pais = pais;
	}
	public String getCiudad() {
		return Ciudad;
	}
	public void setCiudad(String ciudad) {
		Ciudad = ciudad;
	}
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
	
	
}
