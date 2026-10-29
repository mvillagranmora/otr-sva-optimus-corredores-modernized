package cl.bcs.plataforma.services.config.pojo;

public class RegistroConfiguracion {
	private Long IdEmpresa;
	private String CodigoParametro;
	private String Valor;
	private String Modulo;
	
	public RegistroConfiguracion() {
		
	}
	
	public RegistroConfiguracion(Long idEmpresa, String idModulo, String codigoParametro) {
		this.IdEmpresa = idEmpresa;
		this.Modulo = idModulo;
		this.CodigoParametro = codigoParametro;
	}
	public Long getIdEmpresa() {
		return IdEmpresa;
	}
	public void setIdEmpresa(Long idEmpresa) {
		IdEmpresa = idEmpresa;
	}
	public String getCodigoParametro() {
		return CodigoParametro;
	}
	public void setCodigoParametro(String codigoParametro) {
		CodigoParametro = codigoParametro;
	}
	public String getValor() {
		return Valor;
	}
	public void setValor(String valor) {
		Valor = valor;
	}
	
	public String getModulo() {
		return Modulo;
	}
	public void setModulo(String modulo) {
		Modulo = modulo;
	}
	
	@Override
	public String toString() {
		return this.IdEmpresa+"_"+this.Modulo+"_"+this.CodigoParametro;
	}
}
