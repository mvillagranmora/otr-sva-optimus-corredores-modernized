package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

import java.math.BigDecimal;
import java.util.Date;
public class RegistroDJ1848 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
    
	private String inversionistaExtranjeroRut;
	private String inversionistaPaisResidencia;
    private String inversionistaExtranjeroModalidadActuacion;
    private long   transaccionesFormaDeclaracion;
    private String transaccionesTipoOperacionInversionista;
    private String transaccionesTipoInstrumentoContrato;
    private Date transaccionesFechaOperacionSituacionInversionista;
    private double transaccionesMontoOperacionPesos;
    private String transaccionesMonedaOriginal;
    private double transaccionesMontoOperacionMonedaOriginal;
    private double transaccionesGastos;
    private String cumplimientoTipoInversionista;
    private Date cumplimientoFechaContrato;
    private long cumplimientoTipoInstrumento;
    private Date cumplimientoFechaInstrumento;
    private long cumplimientoFolioDJ1848;
    private BigDecimal cumplimientoSaldoInversion;
    private BigDecimal cumplimientoReorganizacion;
    private String retencionesRutResponsaleImpuesto;
    private double retencionesBaseImponible;    
    private double retencionesTasaImpuesto;
    private double retencionesCreditoImpuesto;
    private double retencionesImpuesto;
    private String retencionesFolioF50;
    private	long   retencionesPeriodoTributarioF50;
    private String beneficiosNormaBeneficioTributario;
    private String beneficiosReferenciaLegalOtrosBeneficiosTributarios;
    private double beneficiosMontoExento;
    private double sumaRetencionesImpuesto;
    private double sumaBeneficiosMontoExento;
    
	public String getInversionistaExtranjeroRut() {
		return inversionistaExtranjeroRut;
	}

	public void setInversionistaExtranjeroRut(String inversionistaExtranjeroRut) {
		this.inversionistaExtranjeroRut = inversionistaExtranjeroRut;
	}
	
	public String getInversionistaPaisResidencia() {
		return inversionistaPaisResidencia;
	}

	public void setInversionistaPaisResidencia(String inversionistaPaisResidencia) {
		this.inversionistaPaisResidencia = inversionistaPaisResidencia;
	}

	public String getInversionistaExtranjeroModalidadActuacion() {
		return inversionistaExtranjeroModalidadActuacion;
	}

	public void setInversionistaExtranjeroModalidadActuacion(String inversionistaExtranjeroModalidadActuacion) {
		this.inversionistaExtranjeroModalidadActuacion = inversionistaExtranjeroModalidadActuacion;
	}
	
	public long getTransaccionesFormaDeclaracion() {
		return transaccionesFormaDeclaracion;
	}

	public void setTransaccionesFormaDeclaracion(long transaccionesFormaDeclaracion) {
		this.transaccionesFormaDeclaracion = transaccionesFormaDeclaracion;
	}

	public String getTransaccionesTipoOperacionInversionista() {
		return transaccionesTipoOperacionInversionista;
	}

	public void setTransaccionesTipoOperacionInversionista(String transaccionesTipoOperacionInversionista) {
		this.transaccionesTipoOperacionInversionista = transaccionesTipoOperacionInversionista;
	}

	public String getTransaccionesTipoInstrumentoContrato() {
		return transaccionesTipoInstrumentoContrato;
	}

	public void setTransaccionesTipoInstrumentoContrato(String transaccionesTipoInstrumentoContrato) {
		this.transaccionesTipoInstrumentoContrato = transaccionesTipoInstrumentoContrato;
	}

	public Date getTransaccionesFechaOperacionSituacionInversionista() {
		return transaccionesFechaOperacionSituacionInversionista;
	}

	public void setTransaccionesFechaOperacionSituacionInversionista(
			Date transaccionesFechaOperacionSituacionInversionista) {
		this.transaccionesFechaOperacionSituacionInversionista = transaccionesFechaOperacionSituacionInversionista;
	}

	public double getTransaccionesMontoOperacionPesos() {
		return transaccionesMontoOperacionPesos;
	}

	public void setTransaccionesMontoOperacionPesos(double transaccionesMontoOperacionPesos) {
		this.transaccionesMontoOperacionPesos = transaccionesMontoOperacionPesos;
	}

	public String getTransaccionesMonedaOriginal() {
		return transaccionesMonedaOriginal;
	}

	public void setTransaccionesMonedaOriginal(String transaccionesMonedaOriginal) {
		this.transaccionesMonedaOriginal = transaccionesMonedaOriginal;
	}

	public double getTransaccionesMontoOperacionMonedaOriginal() {
		return transaccionesMontoOperacionMonedaOriginal;
	}

	public void setTransaccionesMontoOperacionMonedaOriginal(double transaccionesMontoOperacionMonedaOriginal) {
		this.transaccionesMontoOperacionMonedaOriginal = transaccionesMontoOperacionMonedaOriginal;
	}

	public double getTransaccionesGastos() {
		return transaccionesGastos;
	}

	public void setTransaccionesGastos(double transaccionesGastos) {
		this.transaccionesGastos = transaccionesGastos;
	}

	public String getCumplimientoTipoInversionista() {
		return cumplimientoTipoInversionista;
	}

	public void setCumplimientoTipoInversionista(String cumplimientoTipoInversionista) {
		this.cumplimientoTipoInversionista = cumplimientoTipoInversionista;
	}

	public Date getCumplimientoFechaContrato() {
		return cumplimientoFechaContrato;
	}

	public void setCumplimientoFechaContrato(Date cumplimientoFechaContrato) {
		this.cumplimientoFechaContrato = cumplimientoFechaContrato;
	}

	public long getCumplimientoTipoInstrumento() {
		return cumplimientoTipoInstrumento;
	}

	public void setCumplimientoTipoInstrumento(long cumplimientoTipoInstrumento) {
		this.cumplimientoTipoInstrumento = cumplimientoTipoInstrumento;
	}

	public Date getCumplimientoFechaInstrumento() {
		return cumplimientoFechaInstrumento;
	}

	public void setCumplimientoFechaInstrumento(Date cumplimientoFechaInstrumento) {
		this.cumplimientoFechaInstrumento = cumplimientoFechaInstrumento;
	}

	public long getCumplimientoFolioDJ1848() {
		return cumplimientoFolioDJ1848;
	}

	public void setCumplimientoFolioDJ1848(long cumplimientoFolioDJ1848) {
		this.cumplimientoFolioDJ1848 = cumplimientoFolioDJ1848;
	}

	public BigDecimal getCumplimientoSaldoInversion() {
		return cumplimientoSaldoInversion;
	}

	public void setCumplimientoSaldoInversion(BigDecimal cumplimientoSaldoInversion) {
		this.cumplimientoSaldoInversion = cumplimientoSaldoInversion;
	}

	public BigDecimal getCumplimientoReorganizacion() {
		return cumplimientoReorganizacion;
	}

	public void setCumplimientoReorganizacion(BigDecimal cumplimientoReorganizacion) {
		this.cumplimientoReorganizacion = cumplimientoReorganizacion;
	}

	public String getRetencionesRutResponsaleImpuesto() {
		return retencionesRutResponsaleImpuesto;
	}

	public void setRetencionesRutResponsaleImpuesto(String retencionesRutResponsaleImpuesto) {
		this.retencionesRutResponsaleImpuesto = retencionesRutResponsaleImpuesto;
	}

	public double getRetencionesBaseImponible() {
		return retencionesBaseImponible;
	}

	public void setRetencionesBaseImponible(double retencionesBaseImponible) {
		this.retencionesBaseImponible = retencionesBaseImponible;
	}

	public double getRetencionesTasaImpuesto() {
		return retencionesTasaImpuesto;
	}

	public void setRetencionesTasaImpuesto(double retencionesTasaImpuesto) {
		this.retencionesTasaImpuesto = retencionesTasaImpuesto;
	}

	public double getRetencionesCreditoImpuesto() {
		return retencionesCreditoImpuesto;
	}

	public void setRetencionesCreditoImpuesto(double retencionesCreditoImpuesto) {
		this.retencionesCreditoImpuesto = retencionesCreditoImpuesto;
	}

	public double getRetencionesImpuesto() {
		return retencionesImpuesto;
	}

	public void setRetencionesImpuesto(double retencionesImpuesto) {
		this.retencionesImpuesto = retencionesImpuesto;
	}

	public String getRetencionesFolioF50() {
		return retencionesFolioF50;
	}

	public void setRetencionesFolioF50(String retencionesFolioF50) {
		this.retencionesFolioF50 = retencionesFolioF50;
	}
	
	public long getRetencionesPeriodoTributarioF50() {
		return retencionesPeriodoTributarioF50;
	}

	public void setRetencionesPeriodoTributarioF50(long retencionesPeriodoTributarioF50) {
		this.retencionesPeriodoTributarioF50 = retencionesPeriodoTributarioF50;
	}

	public String getBeneficiosNormaBeneficioTributario() {
		return beneficiosNormaBeneficioTributario;
	}

	public void setBeneficiosNormaBeneficioTributario(String beneficiosNormaBeneficioTributario) {
		this.beneficiosNormaBeneficioTributario = beneficiosNormaBeneficioTributario;
	}

	public String getBeneficiosReferenciaLegalOtrosBeneficiosTributarios() {
		return beneficiosReferenciaLegalOtrosBeneficiosTributarios;
	}

	public void setBeneficiosReferenciaLegalOtrosBeneficiosTributarios(
			String beneficiosReferenciaLegalOtrosBeneficiosTributarios) {
		this.beneficiosReferenciaLegalOtrosBeneficiosTributarios = beneficiosReferenciaLegalOtrosBeneficiosTributarios;
	}

	public double getBeneficiosMontoExento() {
		return beneficiosMontoExento;
	}

	public void setBeneficiosMontoExento(double beneficiosMontoExento) {
		this.beneficiosMontoExento = beneficiosMontoExento;
	}

	public double getSumaRetencionesImpuesto() {
		return sumaRetencionesImpuesto;
	}

	public void setSumaRetencionesImpuesto(double sumaRetencionesImpuesto) {
		this.sumaRetencionesImpuesto = sumaRetencionesImpuesto;
	}

	public double getSumaBeneficiosMontoExento() {
		return sumaBeneficiosMontoExento;
	}

	public void setSumaBeneficiosMontoExento(double sumaBeneficiosMontoExento) {
		this.sumaBeneficiosMontoExento = sumaBeneficiosMontoExento;
	}

	//Obtener valor del registro
    public RegistroDJ1848(final Registro registro) {
    	
   	 this.inversionistaExtranjeroRut = (String) registro.get("InversionistaExtranjeroRut");
   	 this.inversionistaPaisResidencia = (String) registro.get("InversionistaPaisResidencia");
   	 this.inversionistaExtranjeroModalidadActuacion = (String) registro.get("InversionistaExtranjeroModalidadActuacion");
   	 this.transaccionesFormaDeclaracion = (long) ((Number) registro.get("TransaccionesFormaDeclaracion"));
   	 this.transaccionesTipoOperacionInversionista = (String) registro.get("TransaccionesTipoOperacionInversionista");
   	 this.transaccionesTipoInstrumentoContrato = (String) registro.get("TransaccionesTipoInstrumentoContrato");
   	 this.transaccionesFechaOperacionSituacionInversionista = (Date) registro.get("TransaccionesFechaOperacionSituacionInversionista");
   	 this.transaccionesMontoOperacionPesos = (double) ((Number) registro.get("TransaccionesMontoOperacionPesos")).doubleValue();
   	 this.transaccionesMonedaOriginal = (String) registro.get("TransaccionesMonedaOriginal");
   	 this.transaccionesMontoOperacionMonedaOriginal = (double) ((Number) registro.get("TransaccionesMontoOperacionMonedaOriginal")).doubleValue();
   	 this.transaccionesGastos = (double) ((Number) registro.get("TransaccionesGastos")).doubleValue();
   	 this.cumplimientoTipoInversionista = (String) registro.get("CumplimientoTipoInversionista");
   	 this.cumplimientoFechaContrato = (Date) registro.get("CumplimientoFechaContrato");
   	 this.cumplimientoTipoInstrumento = (long) ((Number) registro.get("CumplimientoTipoInstrumento"));
     this.cumplimientoFechaInstrumento = (Date) registro.get("CumplimientoFechaInstrumento");
     this.cumplimientoFolioDJ1848 = (long) ((Number) registro.get("CumplimientoFolioDJ1848"));
     this.cumplimientoSaldoInversion = ((BigDecimal) registro.get("CumplimientoSaldoInversion"));
     this.cumplimientoReorganizacion = ((BigDecimal) registro.get("CumplimientoReorganizacion"));
   	 this.retencionesRutResponsaleImpuesto = (String) registro.get("RetencionesRutResponsaleImpuesto");
   	 this.retencionesBaseImponible = (double) ((Number) registro.get("RetencionesBaseImponible")).doubleValue();
   	 this.retencionesTasaImpuesto = (double) ((Number) registro.get("RetencionesTasaImpuesto")).doubleValue();
   	 this.retencionesCreditoImpuesto = (double) ((Number) registro.get("RetencionesCreditoImpuesto")).doubleValue();
   	 this.retencionesImpuesto = (double) ((Number) registro.get("RetencionesImpuesto")).doubleValue();
   	 this.retencionesFolioF50 = (String) registro.get("RetencionesFolioF50"); 	
   	 this.retencionesPeriodoTributarioF50 = (long) ((Number) registro.get("RetencionesPeriodoTributarioF50")); 	
   	 this.beneficiosNormaBeneficioTributario = (String) registro.get("BeneficiosNormaBeneficioTributario");
   	 this.beneficiosReferenciaLegalOtrosBeneficiosTributarios = (String) registro.get("BeneficiosReferenciaLegalOtrosBeneficiosTributarios");
   	 this.beneficiosMontoExento = (double) ((Number) registro.get("BeneficiosMontoExento")).doubleValue();
   	 this.sumaRetencionesImpuesto = (double) ((Number) registro.get("RetencionesImpuesto")).doubleValue();
   	 this.sumaBeneficiosMontoExento = (double) ((Number) registro.get("BeneficiosMontoExento")).doubleValue();
   	
    }

	public RegistroDJ1848() {
    }
	
    public void acumular(final RegistroDJ1848 registroDJ1848) {        
        this.sumaRetencionesImpuesto += registroDJ1848.getSumaRetencionesImpuesto();
        this.sumaBeneficiosMontoExento += registroDJ1848.getSumaBeneficiosMontoExento();
    }

}
