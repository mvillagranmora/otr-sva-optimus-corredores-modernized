package cl.bcs.loaderapp.pojo.socionegocio;

public class Documento {
	private String TipoDocumento;
	private String NombreDocumento;
	private String NumeroFirmantesEmpresa;
	private String NumeroFirmantesCliente;
	private String Clasificacion;
	private String ValidarFechaVigencia;
	private String CodigoTipoIndicador;
	private String TipoPersona;
	
	public String getTipoDocumento() {
		return TipoDocumento;
	}
	public void setTipoDocumento(String tipoDocumento) {
		TipoDocumento = tipoDocumento;
	}
	public String getNombreDocumento() {
		return NombreDocumento;
	}
	public void setNombreDocumento(String nombreDocumento) {
		NombreDocumento = nombreDocumento;
	}
	public String getNumeroFirmantesEmpresa() {
		return NumeroFirmantesEmpresa;
	}
	public void setNumeroFirmantesEmpresa(String numeroFirmantesEmpresa) {
		NumeroFirmantesEmpresa = numeroFirmantesEmpresa;
	}
	public String getNumeroFirmantesCliente() {
		return NumeroFirmantesCliente;
	}
	public void setNumeroFirmantesCliente(String numeroFirmantesCliente) {
		NumeroFirmantesCliente = numeroFirmantesCliente;
	}
	public String getClasificacion() {
		return Clasificacion;
	}
	public void setClasificacion(String clasificacion) {
		Clasificacion = clasificacion;
	}
	public String getValidarFechaVigencia() {
		return ValidarFechaVigencia;
	}
	public void setValidarFechaVigencia(String validarFechaVigencia) {
		ValidarFechaVigencia = validarFechaVigencia;
	}
	public String getCodigoTipoIndicador() {
		return CodigoTipoIndicador;
	}
	public void setCodigoTipoIndicador(String codigoTipoIndicador) {
		CodigoTipoIndicador = codigoTipoIndicador;
	}
	public String getTipoPersona() {
		return TipoPersona;
	}
	public void setTipoPersona(String tipoPersona) {
		TipoPersona = tipoPersona;
	}
	@Override
	public String toString() {
		return String.format(
				"Documento [TipoDocumento=%s, NombreDocumento=%s, NumeroFirmantesEmpresa=%s, NumeroFirmantesCliente=%s, Clasificacion=%s, ValidarFechaVigencia=%s, CodigoTipoIndicador=%s, TipoPersona=%s]",
				TipoDocumento, NombreDocumento, NumeroFirmantesEmpresa, NumeroFirmantesCliente, Clasificacion,
				ValidarFechaVigencia, CodigoTipoIndicador, TipoPersona);
	}
	
	
}
