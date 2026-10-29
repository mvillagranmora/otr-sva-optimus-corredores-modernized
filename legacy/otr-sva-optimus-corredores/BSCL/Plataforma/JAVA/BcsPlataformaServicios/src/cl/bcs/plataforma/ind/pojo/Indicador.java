package cl.bcs.plataforma.ind.pojo;

public class Indicador {
	
	public Long IdIndicador;
	public Long IdTipoIndicador;
	public String NombreTipoIndicador;
	public String ClaseDato;
	public String CodigoTipoIndicador;
	public String ValorIndicador;
	public String IdEstadoAutorizacion;
	public String GrupoIndicador;
	public String IdUso;
	public Long CantidadDecimales;
	public Long LongitudMinima;
	public Long LongitudMaxima;
	public String IdTipoEntidad;
	public String IdEntidad;
	public String ModeloCliente;
	public String IdTipoPersona;
	
	public Long getIdIndicador() {
		return IdIndicador;
	}
	public void setIdIndicador(Long idIndicador) {
		IdIndicador = idIndicador;
	}
	public Long getIdTipoIndicador() {
		return IdTipoIndicador;
	}
	public void setIdTipoIndicador(Long idTipoIndicador) {
		IdTipoIndicador = idTipoIndicador;
	}
	public String getNombreTipoIndicador() {
		return NombreTipoIndicador;
	}
	public void setNombreTipoIndicador(String nombreTipoIndicador) {
		NombreTipoIndicador = nombreTipoIndicador;
	}
	public String getClaseDato() {
		return ClaseDato;
	}
	public void setClaseDato(String claseDato) {
		ClaseDato = claseDato;
	}
	public String getCodigoTipoIndicador() {
		return CodigoTipoIndicador;
	}
	public void setCodigoTipoIndicador(String codigoTipoIndicador) {
		CodigoTipoIndicador = codigoTipoIndicador;
	}
	public String getValorIndicador() {
		return ValorIndicador;
	}
	public void setValorIndicador(String valorIndicador) {
		ValorIndicador = valorIndicador;
	}
	public String getIdEstadoAutorizacion() {
		return IdEstadoAutorizacion;
	}
	public void setIdEstadoAutorizacion(String idEstadoAutorizacion) {
		IdEstadoAutorizacion = idEstadoAutorizacion;
	}
	public String getGrupoIndicador() {
		return GrupoIndicador;
	}
	public void setGrupoIndicador(String grupoIndicador) {
		GrupoIndicador = grupoIndicador;
	}
	public String getIdUso() {
		return IdUso;
	}
	public void setIdUso(String idUso) {
		IdUso = idUso;
	}
	public Long getCantidadDecimales() {
		return CantidadDecimales;
	}
	public void setCantidadDecimales(Long cantidadDecimales) {
		CantidadDecimales = cantidadDecimales;
	}
	public Long getLongitudMinima() {
		return LongitudMinima;
	}
	public void setLongitudMinima(Long longitudMinima) {
		LongitudMinima = longitudMinima;
	}
	public Long getLongitudMaxima() {
		return LongitudMaxima;
	}
	public void setLongitudMaxima(Long longitudMaxima) {
		LongitudMaxima = longitudMaxima;
	}
	public String getIdTipoEntidad() {
		return IdTipoEntidad;
	}
	public void setIdTipoEntidad(String idTipoEntidad) {
		IdTipoEntidad = idTipoEntidad;
	}
	public String getIdEntidad() {
		return IdEntidad;
	}
	public void setIdEntidad(String idEntidad) {
		IdEntidad = idEntidad;
	}
	public String getModeloCliente() {
		return ModeloCliente;
	}
	public void setModeloCliente(String modeloCliente) {
		ModeloCliente = modeloCliente;
	}
	public String getIdTipoPersona() {
		return IdTipoPersona;
	}
	public void setIdTipoPersona(String idTipoPersona) {
		IdTipoPersona = idTipoPersona;
	}
	@Override
	public String toString() {
		return "Indicador [IdIndicador=" + IdIndicador + ", IdTipoIndicador=" + IdTipoIndicador
				+ ", NombreTipoIndicador=" + NombreTipoIndicador + ", ClaseDato=" + ClaseDato + ", CodigoTipoIndicador="
				+ CodigoTipoIndicador + ", ValorIndicador=" + ValorIndicador + ", IdEstadoAutorizacion="
				+ IdEstadoAutorizacion + ", GrupoIndicador=" + GrupoIndicador + ", IdUso=" + IdUso
				+ ", CantidadDecimales=" + CantidadDecimales + ", LongitudMinima=" + LongitudMinima
				+ ", LongitudMaxima=" + LongitudMaxima + ", IdTipoEntidad=" + IdTipoEntidad + ", IdEntidad=" + IdEntidad
				+ ", ModeloCliente=" + ModeloCliente + ", IdTipoPersona=" + IdTipoPersona + "]";
	}
}
