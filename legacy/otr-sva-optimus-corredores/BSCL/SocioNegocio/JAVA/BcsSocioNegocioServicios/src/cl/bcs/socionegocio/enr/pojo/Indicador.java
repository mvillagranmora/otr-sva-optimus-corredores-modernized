package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class Indicador extends BcsPojo{
	public Long IdInterno;
	public Long IdIndicador;
	public Long IdEnrolamiento;
	public String IdTipoEntidad;
	public Long IdTipoIndicador;
	public String ModeloCliente;
	public Long IdEmpresa;
	public String ValorIndicador;
	public Long IdIndicadorPersona;
	public String CodigoTipoIndicador;
	public String IdEstado;
	public String IdTipoPersona;
	public String ClaseDato;
	
	public Indicador() {
		this.IdIndicadorPersona = 0L;
	}
}
