package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class CuentaCustodioPortafolio extends BcsPojo {
	public Long IdInterno;
	public Long IdCuentaCustodioPortafolio;
	public Long IdEnrolamiento;
	public String IdBoveda;
	public String Codigo;
	public String IdModeloCustodia;
	public String IdTipoCustodia;
	public String IdTipoCuentaCustodia;
	public String LiquidaDividendos;
	public String LiquidaCupones;
	public String LiquidaPrepagos;
	public Long IdPersonaBancoCustodio;
	public Long IdPersonaBancoLiquidador;
	public String LiquidaVencimientos;
	public transient String EstaInsertada;
	
	public CuentaCustodioPortafolio() {
		this.IdTipoCuentaCustodia = "";
		this.IdTipoCustodia = "";
		this.IdModeloCustodia = "";
		this.IdPersonaBancoCustodio = 0L;
		this.IdPersonaBancoLiquidador = 0L;
	}
}
