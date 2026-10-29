package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class CuentaBancaria extends BcsPojo {
	public String IdTipoBanco;
	public Long IdInterno;
	public Long IdCuentaBancaria;
    public Long IdBanco;
    public String IdTipoCuentaBancaria;
    public String IdMoneda;
    public String NumeroCuentaBancaria;
    public Long IdEnrolamiento;
    public Long IdPlaza;
    public String IdTipoIdentificadorCuenta;
    public String IdentificadorCuenta;
    public Long IdPersonaRelacionada;
    public String CodigoCuentaInterbancario;
    public String CodigoAba;
    public String OtroCodigoBancoExtranjero;
    public String CodigoIban;
    public String OtroCodigoCuentaExtranjero;
    public String EsPac;
    public String Predeterminado;
    public String CrearPreferenciaPago;
    public Long IdCuentaBancariaPersona;
    
    public CuentaBancaria() {
    	this.IdCuentaBancariaPersona = 0L;
    }
}