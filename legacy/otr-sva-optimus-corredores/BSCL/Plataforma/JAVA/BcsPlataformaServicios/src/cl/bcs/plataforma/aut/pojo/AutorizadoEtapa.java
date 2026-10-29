package cl.bcs.plataforma.aut.pojo;

import cl.bcs.utils.BcsPojo;

public class AutorizadoEtapa extends BcsPojo{
	public Long					IdAutorizadoEtapa; 
	public Long					IdEtapa; 
	public Long					IdUsuario; 
	public Long					IdRol; 
	public Long					IdUsuarioReemplaza; 
	public java.sql.Date		ReemplazaDesde; 
	public java.sql.Date		ReemplazaHasta; 
	public String				IdEstado; 
	public String 				EsAutorizacionPorRol;
	public String				SoloBloquear;
	public String 				KeyAutorizadoEtapa;
	
	public AutorizadoEtapa() {
		this.IdUsuario = 0L;
		this.IdRol = 0L; 
		this.IdUsuarioReemplaza = 0L;
		this.EsAutorizacionPorRol = "N";
		this.SoloBloquear = "N";
		this.KeyAutorizadoEtapa = "";
	}
}