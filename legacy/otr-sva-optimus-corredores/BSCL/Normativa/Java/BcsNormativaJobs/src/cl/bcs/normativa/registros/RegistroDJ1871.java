package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1871 {

    /**
     * Declaracion de variables para la DJ
     */
	private String rutCliente;
	private long anoAhorro;
    private double ahorrosTrabajador;
    private double ahorrosDeposito;
    private double ahorrosCotizaciones;
    private double retirosTrabajador;
    private double retirosDeposito;
    private double retirosCotizaciones;
    //private double sumaAhorrosTrabajador;
    //private double sumaAhorrosDeposito;
    //private double sumaAhorrosCotizaciones;
    //private double sumaRetirosTrabajador;
    //private double sumaRetirosDeposito;
    //private double sumaRetirosCotizaciones;
    private double sumaAhorros;
    private double sumaRetiros;
	//Obtener valor del registro
    
    public RegistroDJ1871(final Registro registro) {
    	
      	 this.rutCliente = (String) registro.get("RutCliente");
      	 this.anoAhorro = (Long) registro.get("AnoAhorro");
      	 this.ahorrosTrabajador = (double) ((Number) registro.get("AhorrosTrabajador")).doubleValue();
      	 this.ahorrosDeposito = (double) ((Number) registro.get("AhorrosDeposito")).doubleValue();
      	 this.ahorrosCotizaciones = (double) ((Number) registro.get("AhorrosCotizaciones")).doubleValue();
      	 this.retirosTrabajador = (double) ((Number) registro.get("RetirosTrabajador")).doubleValue();
      	 this.retirosDeposito = (double) ((Number) registro.get("RetirosDeposito")).doubleValue();
      	 this.retirosCotizaciones = (double) ((Number) registro.get("RetirosCotizaciones")).doubleValue();
      	 //this.sumaAhorrosTrabajador = (double) ((Number) registro.get("AhorrosTrabajador")).doubleValue();
      	 //this.sumaAhorrosDeposito = (double) ((Number) registro.get("AhorrosDeposito")).doubleValue();
      	 //this.sumaAhorrosCotizaciones = (double) ((Number) registro.get("AhorrosCotizaciones")).doubleValue();
      	 //this.sumaRetirosTrabajador = (double) ((Number) registro.get("RetirosTrabajador")).doubleValue();
      	 //this.sumaRetirosDeposito = (double) ((Number) registro.get("RetirosDeposito")).doubleValue();
      	 //this.sumaRetirosCotizaciones = (double) ((Number) registro.get("RetirosCotizaciones")).doubleValue();
      	this.sumaAhorros = (double) ((Number) registro.get("AhorrosTrabajador")).doubleValue();
      	this.sumaRetiros = (double) ((Number) registro.get("RetirosTrabajador")).doubleValue();
       }
    
    
    public String getRutCliente() {
		return rutCliente;
	}

	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}

	public long getAnoAhorro() {
		return anoAhorro;
	}

	public void setAnoAhorro(long anoAhorro) {
		this.anoAhorro = anoAhorro;
	}
	
	public double getAhorrosTrabajador() {
		return ahorrosTrabajador;
	}

	public void setAhorrosTrabajador(double ahorrosTrabajador) {
		this.ahorrosTrabajador = ahorrosTrabajador;
	}

	public double getAhorrosDeposito() {
		return ahorrosDeposito;
	}

	public void setAhorrosDeposito(double ahorrosDeposito) {
		this.ahorrosDeposito = ahorrosDeposito;
	}

	public double getAhorrosCotizaciones() {
		return ahorrosCotizaciones;
	}

	public void setAhorrosCotizaciones(double ahorrosCotizaciones) {
		this.ahorrosCotizaciones = ahorrosCotizaciones;
	}

	public double getRetirosTrabajador() {
		return retirosTrabajador;
	}

	public void setRetirosTrabajador(double retirosTrabajador) {
		this.retirosTrabajador = retirosTrabajador;
	}

	public double getRetirosDeposito() {
		return retirosDeposito;
	}

	public void setRetirosDeposito(double retirosDeposito) {
		this.retirosDeposito = retirosDeposito;
	}

	public double getRetirosCotizaciones() {
		return retirosCotizaciones;
	}

	public void setRetirosCotizaciones(double retirosCotizaciones) {
		this.retirosCotizaciones = retirosCotizaciones;
	}

	public double getSumaAhorros() {
		return sumaAhorros;
	}

	public void setSumaAhorros(double sumaAhorros) {
		this.sumaAhorros = sumaAhorros;
	}

	//public double getSumaAhorrosTrabajador() {
	//	return sumaAhorrosTrabajador;
	//}

	//public void setSumaAhorrosTrabajador(double sumaAhorrosTrabajador) {
	//	this.sumaAhorrosTrabajador = sumaAhorrosTrabajador;
	//}

	//public double getSumaAhorrosDeposito() {
	//	return sumaAhorrosDeposito;
	//}

	//public void setSumaAhorrosDeposito(double sumaAhorrosDeposito) {
	//	this.sumaAhorrosDeposito = sumaAhorrosDeposito;
	//}

	//public double getSumaAhorrosCotizaciones() {
	//	return sumaAhorrosCotizaciones;
	//}

	//public void setSumaAhorrosCotizaciones(double sumaAhorrosCotizaciones) {
	//	this.sumaAhorrosCotizaciones = sumaAhorrosCotizaciones;
	//}

	public double getSumaRetiros() {
		return sumaRetiros;
	}

	public void setSumaRetiros(double sumaRetiros) {
		this.sumaRetiros = sumaRetiros;
	}
	
	//public double getSumaRetirosTrabajador() {
	//return sumaRetirosTrabajador;
	//}

	//public void setSumaRetirosTrabajador(double sumaRetirosTrabajador) {
	//	this.sumaRetirosTrabajador = sumaRetirosTrabajador;
	//}

	//public double getSumaRetirosDeposito() {
	//	return sumaRetirosDeposito;
	//}

	//public void setSumaRetirosDeposito(double sumaRetirosDeposito) {
	//	this.sumaRetirosDeposito = sumaRetirosDeposito;
	//}

	//public double getSumaRetirosCotizaciones() {
	//	return sumaRetirosCotizaciones;
	//}

	//public void setSumaRetirosCotizaciones(double sumaRetirosCotizaciones) {
	//	this.sumaRetirosCotizaciones = sumaRetirosCotizaciones;
	//}   
    
    
	public RegistroDJ1871() {
    }
	
    public void acumular(final RegistroDJ1871 registroDJ1871) {   
    	//this.sumaAhorrosTrabajador = registroDJ1871.getSumaAhorrosTrabajador();
    	//this.sumaAhorrosDeposito = registroDJ1871.getSumaAhorrosDeposito();
    	//this.sumaAhorrosCotizaciones = registroDJ1871.getSumaAhorrosCotizaciones();
    	//this.sumaRetirosTrabajador = registroDJ1871.getSumaRetirosTrabajador();
    	//this.sumaRetirosDeposito = registroDJ1871.getSumaRetirosDeposito();
    	//this.sumaRetirosCotizaciones = registroDJ1871.getSumaRetirosCotizaciones();
    	this.sumaAhorros =	this.getSumaAhorros() + registroDJ1871.getAhorrosDeposito() + registroDJ1871.getAhorrosCotizaciones();
    	this.sumaRetiros =	this.getSumaRetiros() + registroDJ1871.getRetirosDeposito() + registroDJ1871.getRetirosCotizaciones();
    }
}
    