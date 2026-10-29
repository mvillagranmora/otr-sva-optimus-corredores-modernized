package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

import java.math.BigDecimal;
import java.util.Date;
public class RegistroDJ1894 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
   

	private String idFondo;
	private String nemo;
	private String idEmisor;
	private String rutEmisor;
	private String rutParticipe;
	private String rutFondo;
	private String runFondo;
	private String tipoOperacion;
	private Date fechaInversion;
	private double montoNominalInversionActualizado;
	private double montoHistorico;
	private Date fechaRescate;
	private double montoNominalRescate;
	private double mayorValorActualizado;
	private double menorValorActualizado;
	private String rutAdminitradoraFondoReceptor;
	private double montoReinvertido;
	private long numeroCertificado;
	private double costoUf;
	private String tipoDeclarante;
    private double sumaInversionNominal;
    private double sumaRescateNominal;
    private double sumaMayorValor;
    private double sumaMenorValor;
    private double sumaMontoReinvertido;
    
    public double getSumaInversionNominal() {
		return sumaInversionNominal;
	}

	public void setSumaInversionNominal(double sumaInversionNominal) {
		this.sumaInversionNominal = sumaInversionNominal;
	}

	public double getSumaRescateNominal() {
		return sumaRescateNominal;
	}

	public void setSumaRescateNominal(double sumaRescateNominal) {
		this.sumaRescateNominal = sumaRescateNominal;
	}

	public double getSumaMayorValor() {
		return sumaMayorValor;
	}

	public void setSumaMayorValor(double sumaMayorValor) {
		this.sumaMayorValor = sumaMayorValor;
	}

	public double getSumaMenorValor() {
		return sumaMenorValor;
	}

	public void setSumaMenorValor(double sumaMenorValor) {
		this.sumaMenorValor = sumaMenorValor;
	}

	public double getSumaMontoReinvertido() {
		return sumaMontoReinvertido;
	}

	public void setSumaMontoReinvertido(double sumaMontoReinvertido) {
		this.sumaMontoReinvertido = sumaMontoReinvertido;
	}

//Get	
	
	public Date getFechaInversion() {
		return fechaInversion;
	}
	
	public void setFechaInversion(Date fechaInversion) {
		this.fechaInversion = fechaInversion;
	}
	
	public Date getFechaRescate() {
		return fechaRescate;
	}
	
	public void setFechaRescate(Date fechaRescate) {
		this.fechaRescate = fechaRescate;
	}

	public long getNumeroCertificado() {
		return numeroCertificado;
	}

	public void setNumeroCertificado(long numeroCertificado) {
		this.numeroCertificado = numeroCertificado;
	}	

	public String getNemo() {
		return nemo;
	}
	
	public void setNemo(String nemo) {
		this.nemo = nemo;
	}	

	public String getIdEmisor() {
		return idEmisor;
	}

	public void setIdEmisor(String idEmisor) {
		this.idEmisor = idEmisor;
	}	
	
	public String getRutEmisor() {
		return rutEmisor;
	}
	
	public void setRutEmisor(String rutEmisor) {
		this.rutEmisor = rutEmisor;
	}	
	
	public String getRutParticipe() {
		return rutParticipe;
	}
	
	public void setRutParticipe(String rutParticipe) {
		this.rutParticipe = rutParticipe;
	}	
	
	public String getRutFondo() {
		return rutFondo;
	}
	
	public void setRutFondo(String rutFondo) {
		this.rutFondo = rutFondo;
	}	

	public String getRunFondo() {
		return runFondo;
	}
	
	public void setRunFondo(String runFondo) {
		this.runFondo = runFondo;
	}		
	
	public String getTipoOperacion() {
		return tipoOperacion;
	}
	
	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}	
	
	public String getRutAdminitradoraFondoReceptor() {
		return rutAdminitradoraFondoReceptor;
	}
	
	public void setRutAdminitradoraFondoReceptor(String rutAdminitradoraFondoReceptor) {
		this.rutAdminitradoraFondoReceptor = rutAdminitradoraFondoReceptor;
	}	
	
	
	public double getCostoUf() {
		return costoUf;
	}	
		
	public void setCostoUf(double costoUf) {
		this.costoUf = costoUf;
	}	
	
		
	public double getMontoNominalInversionActualizado() {
		return montoNominalInversionActualizado;
	}	
		
	public void setMontoNominalInversionActualizado(double montoNominalInversionActualizado) {
		this.montoNominalInversionActualizado = montoNominalInversionActualizado;
	}		
		
	public double getMontoHistorico() {
		return montoHistorico;
	}
	
	public void setMontoHistorico(double montoHistorico) {
		this.montoHistorico = montoHistorico;
	}		
		
	public double getMontoNominalRescate() {
		return montoNominalRescate;
	}	
		
	public void setMontoNominalRescate(double montoNominalRescate) {
		this.montoNominalRescate = montoNominalRescate;
	}		
		
	public double getMayorValorActualizado() {
		return mayorValorActualizado;
	}
	
	public void setMayorValorActualizado(double mayorValorActualizado) {
		this.mayorValorActualizado = mayorValorActualizado;
	}		

	public double getMenorValorActualizado() {
		return menorValorActualizado;		
	}
	
	public void setMenorValorActualizado(double menorValorActualizado) {
		this.menorValorActualizado = menorValorActualizado;
	}
	
	public String getIdFondo() {
		return idFondo;
	}

	public void setIdFondo(String idFondo) {
		this.idFondo = idFondo;
	}
	
	public double getMontoReinvertido() {
		return montoReinvertido;
	}

	public void setMontoReinvertido(double montoReinvertido) {
		this.montoReinvertido = montoReinvertido;
	}

	public String getTipoDeclarante() {
		return tipoDeclarante;
	}

	public void setTipoDeclarante(String tipoDeclarante) {
		this.tipoDeclarante = tipoDeclarante;
	}
	
	
	public RegistroDJ1894() {
    }
    
    //Obtener valor del registro
    public RegistroDJ1894(final Registro registro) {
    	
    	this.idFondo = (String) registro.get("IdFondo");
    	this.nemo = (String) registro.get("Nemo");
    	this.idEmisor = (String) registro.get("IdEmisor");
    	this.rutEmisor = (String) registro.get("RutEmisor");
    	this.rutParticipe = (String) registro.get("RutParticipe");
    	this.rutFondo = (String) registro.get("RutFondo");
    	this.runFondo = (String) registro.get("RunFondo");
    	this.tipoOperacion= (String) registro.get("TipoOperacion");
    	this.fechaInversion = (Date) registro.get("FechaInversion");
    	this.montoNominalInversionActualizado = (double) ((Number) registro.get("MontoNominalInversionActualizado")).doubleValue();
    	this.montoHistorico = (double) ((Number) registro.get("MontoHistorico")).doubleValue();
    	this.fechaRescate = (Date) registro.get("FechaRescate");
    	this.montoNominalRescate = (double) ((Number) registro.get("MontoNominalRescate")).doubleValue();
    	this.mayorValorActualizado = (double) ((Number) registro.get("MayorValorActualizado")).doubleValue();
    	this.menorValorActualizado = (double) ((Number) registro.get("MenorValorActualizado")).doubleValue();    	
    	this.rutAdminitradoraFondoReceptor = (String) registro.get("RutAdminitradoraFondoReceptor");
    	this.montoReinvertido = (double) ((Number) registro.get("MontoReinvertido")).doubleValue();
    	this.numeroCertificado = (long) ((Number) registro.get("NumeroCertificado")).doubleValue();
    	this.costoUf = (double) ((Number) registro.get("CostoUf")).doubleValue();
    	this.tipoDeclarante = (String) registro.get("TipoDeclarante");
    }
 
    public void acumular(final RegistroDJ1894 registroDJ1894) {
    	this.sumaInversionNominal += registroDJ1894.getMontoNominalInversionActualizado();
        this.sumaRescateNominal += registroDJ1894.getMontoNominalRescate();
        this.sumaMayorValor += registroDJ1894.getMayorValorActualizado();
        this.sumaMenorValor += registroDJ1894.getMenorValorActualizado();
        this.sumaMontoReinvertido += registroDJ1894.getMontoReinvertido();
    }

}
