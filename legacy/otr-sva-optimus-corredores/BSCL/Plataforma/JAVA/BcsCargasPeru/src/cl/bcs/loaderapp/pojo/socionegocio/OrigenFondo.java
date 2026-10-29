package cl.bcs.loaderapp.pojo.socionegocio;

import cl.bcs.utils.Utils;

public class OrigenFondo {
	public String DocumentoIdentidad;
	public String TipoDocumentoIdentidad;
	public String TipoPersona;
	public String IdTipoOperacionOrigenFondo;
	public String FechaDeclaracion;
	public String Comentario;
	public String Remuneraciones;
	public String Ahorros;
	public String DesembolsoPrestamo;
	public String UtilidadesDividendosEmpresa;
	public String VentaMisAcciones;
	public String VentaInmueble;
	public String DescripcionOtros;
	public String Otros;
	
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
	public String getIdTipoOperacionOrigenFondo() {
		return IdTipoOperacionOrigenFondo;
	}
	public void setIdTipoOperacionOrigenFondo(String idTipoOperacionOrigenFondo) {
		IdTipoOperacionOrigenFondo = idTipoOperacionOrigenFondo;
	}
	public String getFechaDeclaracion() {
		return FechaDeclaracion;
	}
	public void setFechaDeclaracion(String fechaDeclaracion) {
		FechaDeclaracion = fechaDeclaracion;
	}
	public String getComentario() {
		return Comentario;
	}
	public void setComentario(String comentario) {
		Comentario = comentario;
	}
	public String getRemuneraciones() {
		return Remuneraciones;
	}
	public void setRemuneraciones(String remuneraciones) {
		Remuneraciones = remuneraciones;
	}
	public String getAhorros() {
		return Ahorros;
	}
	public void setAhorros(String ahorros) {
		Ahorros = ahorros;
	}
	public String getDesembolsoPrestamo() {
		return DesembolsoPrestamo;
	}
	public void setDesembolsoPrestamo(String desembolsoPrestamo) {
		DesembolsoPrestamo = desembolsoPrestamo;
	}
	public String getUtilidadesDividendosEmpresa() {
		return UtilidadesDividendosEmpresa;
	}
	public void setUtilidadesDividendosEmpresa(String utilidadesDividendosEmpresa) {
		UtilidadesDividendosEmpresa = utilidadesDividendosEmpresa;
	}
	public String getVentaMisAcciones() {
		return VentaMisAcciones;
	}
	public void setVentaMisAcciones(String ventaMisAcciones) {
		VentaMisAcciones = ventaMisAcciones;
	}
	public String getVentaInmueble() {
		return VentaInmueble;
	}
	public void setVentaInmueble(String ventaInmueble) {
		VentaInmueble = ventaInmueble;
	}
	public String getDescripcionOtros() {
		return DescripcionOtros;
	}
	public void setDescripcionOtros(String descripcionOtros) {
		DescripcionOtros = descripcionOtros;
	}
	public String getOtros() {
		return Otros;
	}
	public void setOtros(String otros) {
		Otros = otros;
	}
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
