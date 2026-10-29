package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class CuentaBancaria extends BcsPojo {
	public String Accion;
	public Long IdInterno;
	public Long IdBanco;
	public String IdTipoCuentaBancaria;
	public String IdMoneda;
	public String NumeroCuentaBancaria;
	public Long IdPersona;
	public String IdEstado;
	public Long IdPlaza;
	public String CrearPreferenciaPago;
	public Long IdCliente;
	public String EsPac;
	// PERU
	public String IdTipoIdentificadorCuenta;
	public String IdentificadorCuenta;
	public Long IdPersonaRelacionada;
	public String CodigoCuentaInterbancario;
	public String CodigoAba;
	public String OtroCodigoBancoExtranjero;
	public String CodigoIban;
	public String OtroCodigoCuentaExtranjero;
	public Long IdCuentaBancaria;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public String Tipo;
	public String EsBorrador;
	
	public CuentaBancaria() {

		this.CodigoCuentaInterbancario = "";
		this.CodigoAba = "";
		this.OtroCodigoBancoExtranjero = "";
		this.CodigoIban = "";
		this.OtroCodigoCuentaExtranjero = "";
	}
	
}
