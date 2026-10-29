package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1899 {

    /**
     * Declaracion de variables para la DJ
     */
	//trabajador = cliente
	public String getRutCliente() {
		return rutCliente;
	}

	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}

	public String getActivoPensionado() {
		return activoPensionado;
	}

	public void setActivoPensionado(String activoPensionado) {
		this.activoPensionado = activoPensionado;
	}

	public String getRutEmpleador() {
		return rutEmpleador;
	}

	public void setRutEmpleador(String rutEmpleador) {
		this.rutEmpleador = rutEmpleador;
	}

	public double getDepositoEmpleador() {
		return depositoEmpleador;
	}

	public void setDepositoEmpleador(double depositoEmpleador) {
		this.depositoEmpleador = depositoEmpleador;
	}

	public double getAporteEmpleador() {
		return aporteEmpleador;
	}

	public void setAporteEmpleador(double aporteEmpleador) {
		this.aporteEmpleador = aporteEmpleador;
	}

	public double getTraspasoEmpleador() {
		return traspasoEmpleador;
	}

	public void setTraspasoEmpleador(double traspasoEmpleador) {
		this.traspasoEmpleador = traspasoEmpleador;
	}

	public double getRetiroEmpleadorActualizado() {
		return retiroEmpleadorActualizado;
	}

	public void setRetiroEmpleadorActualizado(double retiroEmpleadorActualizado) {
		this.retiroEmpleadorActualizado = retiroEmpleadorActualizado;
	}

	public double getMontoRetencionActualizado() {
		return montoRetencionActualizado;
	}

	public void setMontoRetencionActualizado(double montoRetencionActualizado) {
		this.montoRetencionActualizado = montoRetencionActualizado;
	}

	public double getAhorrosModalidadIndirectaEmpleador() {
		return ahorrosModalidadIndirectaEmpleador;
	}

	public void setAhorrosModalidadIndirectaEmpleador(double ahorrosModalidadIndirectaEmpleador) {
		this.ahorrosModalidadIndirectaEmpleador = ahorrosModalidadIndirectaEmpleador;
	}

	public double getAhorrosModalidadDirectaTrabajador() {
		return ahorrosModalidadDirectaTrabajador;
	}

	public void setAhorrosModalidadDirectaTrabajador(double ahorrosModalidadDirectaTrabajador) {
		this.ahorrosModalidadDirectaTrabajador = ahorrosModalidadDirectaTrabajador;
	}

	public double getCotizacionesModalidadIndirectaEmpleador() {
		return cotizacionesModalidadIndirectaEmpleador;
	}

	public void setCotizacionesModalidadIndirectaEmpleador(double cotizacionesModalidadIndirectaEmpleador) {
		this.cotizacionesModalidadIndirectaEmpleador = cotizacionesModalidadIndirectaEmpleador;
	}

	public double getCotizacionesModalidadDirectaTrabajador() {
		return cotizacionesModalidadDirectaTrabajador;
	}

	public void setCotizacionesModalidadDirectaTrabajador(double cotizacionesModalidadDirectaTrabajador) {
		this.cotizacionesModalidadDirectaTrabajador = cotizacionesModalidadDirectaTrabajador;
	}

	public double getCotizacionesTrabajadorIndependiente() {
		return cotizacionesTrabajadorIndependiente;
	}

	public void setCotizacionesTrabajadorIndependiente(double cotizacionesTrabajadorIndependiente) {
		this.cotizacionesTrabajadorIndependiente = cotizacionesTrabajadorIndependiente;
	}

	public double getMontoRetiroPorAhorrosIncisoPrimero() {
		return montoRetiroPorAhorrosIncisoPrimero;
	}

	public void setMontoRetiroPorAhorrosIncisoPrimero(double montoRetiroPorAhorrosIncisoPrimero) {
		this.montoRetiroPorAhorrosIncisoPrimero = montoRetiroPorAhorrosIncisoPrimero;
	}

	public double getRetencionImpuestoSobreRetiros() {
		return retencionImpuestoSobreRetiros;
	}

	public void setRetencionImpuestoSobreRetiros(double retencionImpuestoSobreRetiros) {
		this.retencionImpuestoSobreRetiros = retencionImpuestoSobreRetiros;
	}

	public double getMontoRetiroPorAhorrosIncisoSegundo() {
		return montoRetiroPorAhorrosIncisoSegundo;
	}

	public void setMontoRetiroPorAhorrosIncisoSegundo(double montoRetiroPorAhorrosIncisoSegundo) {
		this.montoRetiroPorAhorrosIncisoSegundo = montoRetiroPorAhorrosIncisoSegundo;
	}

		public long getNumeroCertificadoTrabajador() {
		return numeroCertificadoTrabajador;
	}

	public void setNumeroCertificadoTrabajador(long numeroCertificadoTrabajador) {
		this.numeroCertificadoTrabajador = numeroCertificadoTrabajador;
	}

	public long getNumeroCertificadoEmpleador() {
		return numeroCertificadoEmpleador;
	}

	public void setNumeroCertificadoEmpleador(long numeroCertificadoEmpleador) {
		this.numeroCertificadoEmpleador = numeroCertificadoEmpleador;
	}

	public double getSumaDepositoEmpleador() {
		return sumaDepositoEmpleador;
	}

	public void setSumaDepositoEmpleador(double sumaDepositoEmpleador) {
		this.sumaDepositoEmpleador = sumaDepositoEmpleador;
	}

	public double getSumaAporteEmpleador() {
		return sumaAporteEmpleador;
	}

	public void setSumaAporteEmpleador(double sumaAporteEmpleador) {
		this.sumaAporteEmpleador = sumaAporteEmpleador;
	}

	public double getSumaTraspasoEmpleador() {
		return sumaTraspasoEmpleador;
	}

	public void setSumaTraspasoEmpleador(double sumaTraspasoEmpleador) {
		this.sumaTraspasoEmpleador = sumaTraspasoEmpleador;
	}

	public double getSumaRetiroEmpleadorActualizado() {
		return sumaRetiroEmpleadorActualizado;
	}

	public void setSumaRetiroEmpleadorActualizado(double sumaRetiroEmpleadorActualizado) {
		this.sumaRetiroEmpleadorActualizado = sumaRetiroEmpleadorActualizado;
	}

	public double getSumaMontoRetencionActualizado() {
		return sumaMontoRetencionActualizado;
	}

	public void setSumaMontoRetencionActualizado(double sumaMontoRetencionActualizado) {
		this.sumaMontoRetencionActualizado = sumaMontoRetencionActualizado;
	}

	public double getSumaAhorrosModalidadIndirectaEmpleador() {
		return sumaAhorrosModalidadIndirectaEmpleador;
	}

	public void setSumaAhorrosModalidadIndirectaEmpleador(double sumaAhorrosModalidadIndirectaEmpleador) {
		this.sumaAhorrosModalidadIndirectaEmpleador = sumaAhorrosModalidadIndirectaEmpleador;
	}

	public double getSumaAhorrosModalidadDirectaTrabajador() {
		return sumaAhorrosModalidadDirectaTrabajador;
	}

	public void setSumaAhorrosModalidadDirectaTrabajador(double sumaAhorrosModalidadDirectaTrabajador) {
		this.sumaAhorrosModalidadDirectaTrabajador = sumaAhorrosModalidadDirectaTrabajador;
	}

	public double getSumaCotizacionesModalidadIndirectaEmpleador() {
		return sumaCotizacionesModalidadIndirectaEmpleador;
	}

	public void setSumaCotizacionesModalidadIndirectaEmpleador(double sumaCotizacionesModalidadIndirectaEmpleador) {
		this.sumaCotizacionesModalidadIndirectaEmpleador = sumaCotizacionesModalidadIndirectaEmpleador;
	}

	public double getSumaCotizacionesModalidadDirectaTrabajador() {
		return sumaCotizacionesModalidadDirectaTrabajador;
	}

	public void setSumaCotizacionesModalidadDirectaTrabajador(double sumaCotizacionesModalidadDirectaTrabajador) {
		this.sumaCotizacionesModalidadDirectaTrabajador = sumaCotizacionesModalidadDirectaTrabajador;
	}

	public double getSumaCotizacionesTrabajadorIndependiente() {
		return sumaCotizacionesTrabajadorIndependiente;
	}

	public void setSumaCotizacionesTrabajadorIndependiente(double sumaCotizacionesTrabajadorIndependiente) {
		this.sumaCotizacionesTrabajadorIndependiente = sumaCotizacionesTrabajadorIndependiente;
	}

	public double getSumaMontoRetiroPorAhorrosIncisoPrimero() {
		return sumaMontoRetiroPorAhorrosIncisoPrimero;
	}

	public void setSumaMontoRetiroPorAhorrosIncisoPrimero(double sumaMontoRetiroPorAhorrosIncisoPrimero) {
		this.sumaMontoRetiroPorAhorrosIncisoPrimero = sumaMontoRetiroPorAhorrosIncisoPrimero;
	}

	public double getSumaRetencionImpuestoSobreRetiros() {
		return sumaRetencionImpuestoSobreRetiros;
	}

	public void setSumaRetencionImpuestoSobreRetiros(double sumaRetencionImpuestoSobreRetiros) {
		this.sumaRetencionImpuestoSobreRetiros = sumaRetencionImpuestoSobreRetiros;
	}

	public double getSumaMontoRetiroPorAhorrosIncisoSegundo() {
		return sumaMontoRetiroPorAhorrosIncisoSegundo;
	}

	public void setSumaMontoRetiroPorAhorrosIncisoSegundo(double sumaMontoRetiroPorAhorrosIncisoSegundo) {
		this.sumaMontoRetiroPorAhorrosIncisoSegundo = sumaMontoRetiroPorAhorrosIncisoSegundo;
	} 

	//private String rutTrabajador;
	private String rutCliente;
	private String activoPensionado;
    private String rutEmpleador;
    private double depositoEmpleador;
    //Aporte Empleador
    private double aporteEmpleador;
    private double traspasoEmpleador;
    private double retiroEmpleadorActualizado;
    private double montoRetencionActualizado;
    //Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    private double ahorrosModalidadIndirectaEmpleador;
    private double ahorrosModalidadDirectaTrabajador;
    //DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    //del ART. 42 Nª1 de la LIR en UF
    private double cotizacionesModalidadIndirectaEmpleador;
    private double cotizacionesModalidadDirectaTrabajador;
    //DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    //del Art.42 Nª2 de la LIR en UF
    private double cotizacionesTrabajadorIndependiente;
    //Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    //del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    //los aportes del empleador, actualizados en $
    private double montoRetiroPorAhorrosIncisoPrimero;
    //Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    private double retencionImpuestoSobreRetiros;
    //Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    private double montoRetiroPorAhorrosIncisoSegundo;
  //Depósitos convenidos con el empleador en UF
    private double sumaDepositoEmpleador;
    //N° CERTIFICADO TRABAJADOR
    private long numeroCertificadoTrabajador;
    //N° CERTIFICADO EMPLEADOR
    private long numeroCertificadoEmpleador;
    //sumas
	    
	    //Ahorro Previsional voluntario Colectivo
	    private double sumaAporteEmpleador;
	    private double sumaTraspasoEmpleador;
	    private double sumaRetiroEmpleadorActualizado;
	    private double sumaMontoRetencionActualizado;
	    //Ahorros del trabajador acogidos al inciso primero Art.42
	    private double sumaAhorrosModalidadIndirectaEmpleador;
	    private double sumaAhorrosModalidadDirectaTrabajador;
	    //Depositos de ahorro Previsional Voluntario (DAPV) o Cotizacioes acogidos al Inciso primero del Art. 42 BIS de la LIR
	    //DAPV o ctizaciones Voluntarias enterado en calidad del trabajador Dependiente del Art 42 de la LIR en UF
	    private double sumaCotizacionesModalidadIndirectaEmpleador;
	    private double sumaCotizacionesModalidadDirectaTrabajador;
	    //DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
        //del Art.42 Nª2 de la LIR en UF
	    private double sumaCotizacionesTrabajadorIndependiente;
	    //Monto de los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados, Actualizados
	    private double sumaMontoRetiroPorAhorrosIncisoPrimero;
	    private double sumaRetencionImpuestoSobreRetiros;
	    private double sumaMontoRetiroPorAhorrosIncisoSegundo;
	    
    //Obtener valor del registro
    public RegistroDJ1899(final Registro registro) {
    	//this.rutTrabajador = (String) registro.get("RutTrabajador");
    	this.rutCliente = (String) registro.get("RutCliente");
    	this.activoPensionado =	(String) registro.get("ActivoPensionado");
    	this.rutEmpleador = (String) registro.get("RutEmpleador");
    	this.depositoEmpleador = (double) ((Number) registro.get("DepositoEmpleador")).doubleValue();
    	//Aporte Empleador
    	this.aporteEmpleador = (double) ((Number) registro.get("AporteEmpleador")).doubleValue();
    	this.traspasoEmpleador = (double) ((Number) registro.get("TraspasoEmpleador")).doubleValue();
    	this.retiroEmpleadorActualizado = (double) ((Number) registro.get("RetiroEmpleadorActualizado")).doubleValue();
    	this.montoRetencionActualizado = (double) ((Number) registro.get("MontoRetencionActualizado")).doubleValue();
    	//Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
    	this.ahorrosModalidadIndirectaEmpleador = (double) ((Number) registro.get("AhorrosModalidadIndirectaEmpleador")).doubleValue();
    	this.ahorrosModalidadDirectaTrabajador = (double) ((Number) registro.get("AhorrosModalidadDirectaTrabajador")).doubleValue();
    	//DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente
    	//del ART. 42 Nª1 de la LIR en UF
    	this.cotizacionesModalidadIndirectaEmpleador = (double) ((Number) registro.get("CotizacionesModalidadIndirectaEmpleador")).doubleValue();
    	this.cotizacionesModalidadDirectaTrabajador = (double) ((Number) registro.get("CotizacionesModalidadDirectaTrabajador")).doubleValue();
    	//DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
    	//del Art.42 Nª2 de la LIR en UF
    	this.cotizacionesTrabajadorIndependiente = (double) ((Number) registro.get("CotizacionesTrabajadorIndependiente")).doubleValue();
    	//Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
    	//del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
    	//los aportes del empleador, actualizados en $
    	this.montoRetiroPorAhorrosIncisoPrimero = (double) ((Number) registro.get("MontoRetiroPorAhorrosIncisoPrimero")).doubleValue();
    	//Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
    	this.retencionImpuestoSobreRetiros = (double) ((Number) registro.get("RetencionImpuestoSobreRetiros")).doubleValue();
    	//Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
    	this.montoRetiroPorAhorrosIncisoSegundo = (double) ((Number) registro.get("MontoRetiroPorAhorrosIncisoSegundo")).doubleValue();
    	//Depósitos convenidos con el empleador en UF
    	this.sumaDepositoEmpleador = (double) ((Number) registro.get("DepositoEmpleador")).doubleValue();
    	//N° CERTIFICADO TRABAJADOR
    	this.numeroCertificadoTrabajador = (Long) registro.get("NumeroCertificadoTrabajador");
    	//N° CERTIFICADO EMPLEADOR
    	this.numeroCertificadoEmpleador = (Long) registro.get("NumeroCertificadoEmpleador");
    	//sumas
		//Ahorro Previsional voluntario Colectivo
		this.sumaAporteEmpleador = (double) ((Number) registro.get("AporteEmpleador")).doubleValue();
		this.sumaTraspasoEmpleador = (double) ((Number) registro.get("TraspasoEmpleador")).doubleValue();
		this.sumaRetiroEmpleadorActualizado = (double) ((Number) registro.get("RetiroEmpleadorActualizado")).doubleValue();
		this.sumaMontoRetencionActualizado = (double) ((Number) registro.get("MontoRetencionActualizado")).doubleValue();
		//Ahorros del trabajador acogidos al inciso primero Art.42
		this.sumaAhorrosModalidadIndirectaEmpleador = (double) ((Number) registro.get("AhorrosModalidadIndirectaEmpleador")).doubleValue();
		this.sumaAhorrosModalidadDirectaTrabajador = (double) ((Number) registro.get("AhorrosModalidadDirectaTrabajador")).doubleValue();
		//Depositos de ahorro Previsional Voluntario (DAPV) o Cotizacioes acogidos al Inciso primero del Art. 42 BIS de la LIR
		//DAPV o ctizaciones Voluntarias enterado en calidad del trabajador Dependiente del Art 42 de la LIR en UF
		this.sumaCotizacionesModalidadIndirectaEmpleador = (double) ((Number) registro.get("CotizacionesModalidadIndirectaEmpleador")).doubleValue();
		this.sumaCotizacionesModalidadDirectaTrabajador = (double) ((Number) registro.get("CotizacionesModalidadDirectaTrabajador")).doubleValue();
		//DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente
		 //del Art.42 Nª2 de la LIR en UF
		this.sumaCotizacionesTrabajadorIndependiente = (double) ((Number) registro.get("CotizacionesTrabajadorIndependiente")).doubleValue();
		//Monto de los retiros efectuados con cargo a los ahorros previsionales voluntarios realizados, Actualizados
		this.sumaMontoRetiroPorAhorrosIncisoPrimero = (double) ((Number) registro.get("MontoRetiroPorAhorrosIncisoPrimero")).doubleValue();
		this.sumaRetencionImpuestoSobreRetiros = (double) ((Number) registro.get("RetencionImpuestoSobreRetiros")).doubleValue();
		this.sumaMontoRetiroPorAhorrosIncisoSegundo = (double) ((Number) registro.get("MontoRetiroPorAhorrosIncisoSegundo")).doubleValue(); 
      }
    
	public RegistroDJ1899() {
    }
	
    public void acumular(final RegistroDJ1899 registroDJ1899) {   
         this.sumaDepositoEmpleador += registroDJ1899.getSumaDepositoEmpleador();
         this.sumaAporteEmpleador += registroDJ1899.getSumaAporteEmpleador();
         this.sumaTraspasoEmpleador += registroDJ1899.getSumaTraspasoEmpleador();
         this.sumaRetiroEmpleadorActualizado += registroDJ1899.getSumaRetiroEmpleadorActualizado();
         this.sumaMontoRetencionActualizado += registroDJ1899.getSumaMontoRetencionActualizado();
         this.sumaAhorrosModalidadIndirectaEmpleador += registroDJ1899.getSumaAhorrosModalidadIndirectaEmpleador();
         this.sumaAhorrosModalidadDirectaTrabajador += registroDJ1899.getSumaAhorrosModalidadDirectaTrabajador();
         this.sumaCotizacionesModalidadIndirectaEmpleador += registroDJ1899.getSumaCotizacionesModalidadIndirectaEmpleador();
         this.sumaCotizacionesModalidadDirectaTrabajador += registroDJ1899.getSumaCotizacionesModalidadDirectaTrabajador();
         this.sumaCotizacionesTrabajadorIndependiente += registroDJ1899.getSumaCotizacionesTrabajadorIndependiente();
         this.sumaMontoRetiroPorAhorrosIncisoPrimero += registroDJ1899.getSumaMontoRetiroPorAhorrosIncisoPrimero();
         this.sumaRetencionImpuestoSobreRetiros += registroDJ1899.getSumaRetencionImpuestoSobreRetiros();
         this.sumaMontoRetiroPorAhorrosIncisoSegundo += registroDJ1899.getSumaMontoRetiroPorAhorrosIncisoSegundo(); 
    }
    
    
}