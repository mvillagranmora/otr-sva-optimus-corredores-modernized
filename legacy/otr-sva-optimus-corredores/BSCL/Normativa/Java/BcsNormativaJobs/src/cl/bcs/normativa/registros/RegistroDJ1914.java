package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1914 {

    /**
     * Declaracion de variables para la DJ
     */
    
    private String rutCliente;
    private String tipoInstrumento;
    private String numeroIdentificadorInversion;
    private Date fechaInversionOriginal;    
    private double retiroInversion54BISMontoNominal;
    private double retiroInversion54BISInteresPositivo;
    private double retiroInversion54BISInteresNegativo;
    private double sumaRetiroInversion54BISMontoNominal;
    private double sumaRetiroInversion54BISInteresPositivo;
    private double sumaRetiroInversion54BISInteresNegativo;
    private long numeroCertificado;
   
    //Obtener valor del registro
    public RegistroDJ1914(final Registro registro) {
    	
   	 this.rutCliente = (String) registro.get("RutCliente");
   	 this.tipoInstrumento = (String) registro.get("TipoInstrumento");
   	 this.numeroIdentificadorInversion = (String) registro.get("NumeroIdentificadorInversion");
   	 this.fechaInversionOriginal = (Date) registro.get("FechaInversionOriginal"); 	
   	 this.retiroInversion54BISMontoNominal = (double) ((Number) registro.get("RetiroInversion54BISMontoNominal")).doubleValue();
   	 this.retiroInversion54BISInteresPositivo = (double) ((Number) registro.get("RetiroInversion54BISInteresPositivo")).doubleValue();
   	 this.retiroInversion54BISInteresNegativo = (double) ((Number) registro.get("RetiroInversion54BISInteresNegativo")).doubleValue();
   	 this.sumaRetiroInversion54BISMontoNominal = (double) ((Number) registro.get("RetiroInversion54BISMontoNominal")).doubleValue();
   	 this.sumaRetiroInversion54BISInteresPositivo = (double) ((Number) registro.get("RetiroInversion54BISInteresPositivo")).doubleValue();
   	 this.sumaRetiroInversion54BISInteresNegativo = (double) ((Number) registro.get("RetiroInversion54BISInteresNegativo")).doubleValue();
   	 this.numeroCertificado = (Long) registro.get("NumeroCertificado");
   	}

	public String getRutCliente() {
		return rutCliente;
	}

	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}

	public String getTipoInstrumento() {
		return tipoInstrumento;
	}

	public void setTipoInstrumento(String tipoInstrumento) {
		this.tipoInstrumento = tipoInstrumento;
	}

	public String getNumeroIdentificadorInversion() {
		return numeroIdentificadorInversion;
	}

	public void setNumeroIdentificadorInversion(String numeroIdentificadorInversion) {
		this.numeroIdentificadorInversion = numeroIdentificadorInversion;
	}

	public Date getFechaInversionOriginal() {
		return fechaInversionOriginal;
	}

	public void setFechaInversionOriginal(Date fechaInversionOriginal) {
		this.fechaInversionOriginal = fechaInversionOriginal;
	}

	public double getRetiroInversion54BISMontoNominal() {
		return retiroInversion54BISMontoNominal;
	}

	public void setRetiroInversion54BISMontoNominal(double retiroInversion54BISMontoNominal) {
		this.retiroInversion54BISMontoNominal = retiroInversion54BISMontoNominal;
	}

	public double getRetiroInversion54BISInteresPositivo() {
		return retiroInversion54BISInteresPositivo;
	}

	public void setRetiroInversion54BISInteresPositivo(double retiroInversion54BISInteresPositivo) {
		this.retiroInversion54BISInteresPositivo = retiroInversion54BISInteresPositivo;
	}

	public double getRetiroInversion54BISInteresNegativo() {
		return retiroInversion54BISInteresNegativo;
	}

	public void setRetiroInversion54BISInteresNegativo(double retiroInversion54BISInteresNegativo) {
		this.retiroInversion54BISInteresNegativo = retiroInversion54BISInteresNegativo;
	}

	public double getSumaRetiroInversion54BISMontoNominal() {
		return sumaRetiroInversion54BISMontoNominal;
	}

	public void setSumaRetiroInversion54BISMontoNominal(double sumaRetiroInversion54BISMontoNominal) {
		this.sumaRetiroInversion54BISMontoNominal = sumaRetiroInversion54BISMontoNominal;
	}

	public double getSumaRetiroInversion54BISInteresPositivo() {
		return sumaRetiroInversion54BISInteresPositivo;
	}

	public void setSumaRetiroInversion54BISInteresPositivo(double sumaRetiroInversion54BISInteresPositivo) {
		this.sumaRetiroInversion54BISInteresPositivo = sumaRetiroInversion54BISInteresPositivo;
	}

	public double getSumaRetiroInversion54BISInteresNegativo() {
		return sumaRetiroInversion54BISInteresNegativo;
	}

	public void setSumaRetiroInversion54BISInteresNegativo(double sumaRetiroInversion54BISInteresNegativo) {
		this.sumaRetiroInversion54BISInteresNegativo = sumaRetiroInversion54BISInteresNegativo;
	}

	public long getNumeroCertificado() {
		return numeroCertificado;
	}

	public void setNumeroCertificado(long numeroCertificado) {
		this.numeroCertificado = numeroCertificado;
	}
    
	public RegistroDJ1914() {
    }
	
    public void acumular(final RegistroDJ1914 registroDJ1914) {        

     	this.sumaRetiroInversion54BISMontoNominal = registroDJ1914.getRetiroInversion54BISMontoNominal();
     	this.sumaRetiroInversion54BISInteresPositivo = registroDJ1914.getRetiroInversion54BISInteresPositivo();
     	this.sumaRetiroInversion54BISInteresNegativo = registroDJ1914.getRetiroInversion54BISInteresNegativo(); 
    }
    
	
}
