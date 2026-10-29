package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class PreferenciaPagoPortafolio extends BcsPojo {
	public Long IdInterno;
	public String IdMedioPago;
	public String IdMoneda;
	public Long IdPreferenciaPagoPortafolio;
	public Long IdEnrolamiento;
	public String IdPreferenciaPago;
	public Long IdPersonaRelacionada;
	public Long IdCuentaBancaria;
	public Long IdEmpresa;
	public Long IdTarjetaBancaria;
	public String PorDefecto;
	public String IdEstado;
	public String RecibePagaPortafolio;
	public transient String EstaInsertada;
	public transient String EsBorrador;


	public PreferenciaPagoPortafolio() {
		this.IdPersonaRelacionada = 0L;
		this.EsBorrador = "S";
	}
}
