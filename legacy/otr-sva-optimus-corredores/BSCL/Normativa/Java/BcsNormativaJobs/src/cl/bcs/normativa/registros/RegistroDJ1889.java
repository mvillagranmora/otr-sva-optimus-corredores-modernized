package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1889 {

    /**
     * Declaracion de variables para la DJ
     */
	private String rutCliente;
	private long tipoAhorro;
	private double montoNominal;
    private double rentabilidadPositiva;
    private double rentabilidadNegativa;
    private double rentabilidadActualizadaPositiva;
    private double rentabilidadActualizadaNegativa;
    private long numeroCertificado;
    private double sumaMontoNominal;
    private double sumaRentabilidadPositiva;
    private double sumaRentabilidadNegativa;
    private double sumaRentabilidadActualizadaPositiva;
    private double sumaRentabilidadActualizadaNegativa;
	
    //Obtener valor del registro
    public RegistroDJ1889(final Registro registro) {
    	
     	 this.rutCliente = (String) registro.get("RutCliente");
     	 this.tipoAhorro = (Long) registro.get("TipoAhorro");
     	 this.montoNominal = (double) ((Number) registro.get("MontoNominal")).doubleValue();
     	 this.rentabilidadPositiva = (double) ((Number) registro.get("RentabilidadPositiva")).doubleValue();
     	 this.rentabilidadNegativa = (double) ((Number) registro.get("RentabilidadNegativa")).doubleValue();
     	 this.rentabilidadActualizadaPositiva = (double) ((Number) registro.get("RentabilidadActualizadaPositiva")).doubleValue();
     	 this.rentabilidadActualizadaNegativa = (double) ((Number) registro.get("RentabilidadActualizadaNegativa")).doubleValue();
     	 this.numeroCertificado = (Long) registro.get("NumeroCertificado");
     	 this.sumaMontoNominal = (double) ((Number) registro.get("MontoNominal")).doubleValue();
     	 this.sumaRentabilidadPositiva = (double) ((Number) registro.get("RentabilidadPositiva")).doubleValue();
     	 this.sumaRentabilidadNegativa = (double) ((Number) registro.get("RentabilidadNegativa")).doubleValue();
     	 this.sumaRentabilidadActualizadaPositiva = (double) ((Number) registro.get("RentabilidadActualizadaPositiva")).doubleValue();
     	 this.sumaRentabilidadActualizadaNegativa = (double) ((Number) registro.get("RentabilidadActualizadaNegativa")).doubleValue();
      }
    
	public String getRutCliente() {
		return rutCliente;
	}

	
	
	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}




	public long getTipoAhorro() {
		return tipoAhorro;
	}




	public void setTipoAhorro(long tipoAhorro) {
		this.tipoAhorro = tipoAhorro;
	}




	public double getMontoNominal() {
		return montoNominal;
	}




	public void setMontoNominal(double montoNominal) {
		this.montoNominal = montoNominal;
	}




	public double getRentabilidadPositiva() {
		return rentabilidadPositiva;
	}




	public void setRentabilidadPositiva(double rentabilidadPositiva) {
		this.rentabilidadPositiva = rentabilidadPositiva;
	}




	public double getRentabilidadNegativa() {
		return rentabilidadNegativa;
	}




	public void setRentabilidadNegativa(double rentabilidadNegativa) {
		this.rentabilidadNegativa = rentabilidadNegativa;
	}




	public double getRentabilidadActualizadaPositiva() {
		return rentabilidadActualizadaPositiva;
	}




	public void setRentabilidadActualizadaPositiva(double rentabilidadActualizadaPositiva) {
		this.rentabilidadActualizadaPositiva = rentabilidadActualizadaPositiva;
	}




	public double getRentabilidadActualizadaNegativa() {
		return rentabilidadActualizadaNegativa;
	}




	public void setRentabilidadActualizadaNegativa(double rentabilidadActualizadaNegativa) {
		this.rentabilidadActualizadaNegativa = rentabilidadActualizadaNegativa;
	}




	public long getNumeroCertificado() {
		return numeroCertificado;
	}




	public void setNumeroCertificado(long numeroCertificado) {
		this.numeroCertificado = numeroCertificado;
	}




	public double getSumaMontoNominal() {
		return sumaMontoNominal;
	}




	public void setSumaMontoNominal(double sumaMontoNominal) {
		this.sumaMontoNominal = sumaMontoNominal;
	}




	public double getSumaRentabilidadPositiva() {
		return sumaRentabilidadPositiva;
	}




	public void setSumaRentabilidadPositiva(double sumaRentabilidadPositiva) {
		this.sumaRentabilidadPositiva = sumaRentabilidadPositiva;
	}




	public double getSumaRentabilidadNegativa() {
		return sumaRentabilidadNegativa;
	}




	public void setSumaRentabilidadNegativa(double sumaRentabilidadNegativa) {
		this.sumaRentabilidadNegativa = sumaRentabilidadNegativa;
	}




	public double getSumaRentabilidadActualizadaPositiva() {
		return sumaRentabilidadActualizadaPositiva;
	}




	public void setSumaRentabilidadActualizadaPositiva(double sumaRentabilidadActualizadaPositiva) {
		this.sumaRentabilidadActualizadaPositiva = sumaRentabilidadActualizadaPositiva;
	}




	public double getSumaRentabilidadActualizadaNegativa() {
		return sumaRentabilidadActualizadaNegativa;
	}

	public void setSumaRentabilidadActualizadaNegativa(double sumaRentabilidadActualizadaNegativa) {
		this.sumaRentabilidadActualizadaNegativa = sumaRentabilidadActualizadaNegativa;
	}
	
       
	public RegistroDJ1889() {
    }
	
    public void acumular(final RegistroDJ1889 registroDJ1889) {   
         this.sumaMontoNominal += registroDJ1889.getSumaMontoNominal();
         this.sumaRentabilidadPositiva += registroDJ1889.getSumaRentabilidadPositiva();
         this.sumaRentabilidadNegativa += registroDJ1889.getSumaRentabilidadNegativa();
         this.sumaRentabilidadActualizadaPositiva += registroDJ1889.getSumaRentabilidadActualizadaPositiva();
         this.sumaRentabilidadActualizadaNegativa += registroDJ1889.getSumaRentabilidadActualizadaNegativa();
    }
}
    