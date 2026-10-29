package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class TarjetaBancaria extends BcsPojo{
	public Long IdInterno;
	public Long IdTarjetaBancaria;
	public Long IdEnrolamiento;
	public Long IdBanco;
	public String IdTipoTarjetaBancaria;
	public String NumeroTarjetaBancaria;
	public String FechaVencimiento;
	public Long CodigoVerificacion;
	public Long IdTarjetaBancariaPersona;
	
	public TarjetaBancaria() {
		this.IdTarjetaBancariaPersona = 0L;
	}
}
