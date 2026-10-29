package cl.bcs.custodia.pojo;

import java.sql.Time;
import java.sql.Timestamp;

import cl.bcs.utils.Utils;

public class PreferenciaPago {
	
	public Long 	IdPreferenciaPagoPortafolio;
    public String 	IdModalidadPago;
    public String 	IdMedioDePago;
    public String 	NombreMedioDePago;
    public Long 	IdBanco;
    public String 	NombreBanco;
    public String	IdTipoCuentaBancaria;
    public String	NombreTipoCuentaBancaria;
    public Long		IdCuentaBancaria;
    public String	NombreCuentaBancaria;
    public String	NumeroCuentaBancaria;
    public String	IdTipoTarjetaBancaria;
    public String	NumeroTarjetaBancaria;
    
    public Long 	IdPortafolio;
    public String 	IdTipoOperacion;
    public String 	IdMoneda;    
    public String 	NumeroMedioDePago;
    public Timestamp 	FechaHoraIngresoRegistro;
    public String 	IdEstado;
	
    public String toString() {
		return Utils.printJson(this, false);
	};
}
