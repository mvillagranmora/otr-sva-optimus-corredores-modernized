package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1944 {

    /**
     * Declaracion de variables para la DJ
     */
	private Date fechaOperacion;
	private long numeroCertificado;
	private String RutCliente;
    private String RutInstitucion;
    private double SalAhorroNetoEjerNEGT1;
    private double SalArrastreEjerSIGT1;
    private double SalAhorroNetoEjerT2;
    private double SalArrastreEjerSigNegT2;
    private double MtoRetiro57BisT2;
    private double SalAhorroNetoEjeNegT3;
    private double SalArrastreEjeSigNegT3;
    private double MtoRetiroAnoT3;
    private double MtoRenRetFFMMPOS;
    private double MtoRenRetFFMMNEG;
    private double MtoRenRetOTROSPOS;
    private double MtoRenRetOTROSNEG;
    /**
     * Declaracion de sumatorias para la DJ
     */
    private double sumSalAhorroNetoEjerNEGT1;
    private double sumSalArrastreEjerSIGT1;
    private double sumSalAhorroNetoEjerT2;
    private double sumSalArrastreEjerSigNegT2;
    private double sumMtoRetiro57BisT2;
    private double sumSalAhorroNetoEjeNegT3;
    private double sumSalArrastreEjeSigNegT3;
    private double sumMtoRetiroAnoT3;
    private double sumMtoRenRetFFMMPOS;
    private double sumMtoRenRetFFMMNEG;
    private double sumMtoRenRetOTROSPOS;
    private double sumMtoRenRetOTROSNEG;
    
    public RegistroDJ1944(final Registro registro) {
    	
   	 
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.numeroCertificado = (Long) registro.get("NumeroCertificado");
   	 this.RutInstitucion= (String) registro.get("RutInstitucion");
   	 this.RutCliente= (String) registro.get("RutCliente");
   	 this.SalAhorroNetoEjerNEGT1=(double) ((Number) registro.get("SalAhorroNetoEjerNEGT1")).doubleValue();
   	 this.SalArrastreEjerSIGT1=(double) ((Number) registro.get("SalArrastreEjerSIGT1")).doubleValue();
   	 this.SalAhorroNetoEjerT2=(double) ((Number) registro.get("SalAhorroNetoEjerT2")).doubleValue();
   	 this.SalArrastreEjerSigNegT2=(double) ((Number) registro.get("SalArrastreEjerSigNegT2")).doubleValue();
   	 this.MtoRetiro57BisT2=(double) ((Number) registro.get("MtoRetiro57BisT2")).doubleValue();
   	 this.SalAhorroNetoEjeNegT3=(double) ((Number) registro.get("SalAhorroNetoEjeNegT3")).doubleValue();
   	 this.SalArrastreEjeSigNegT3=(double) ((Number) registro.get("SalArrastreEjeSigNegT3")).doubleValue();
   	 this.MtoRetiroAnoT3=(double) ((Number) registro.get("MtoRetiroAnoT3")).doubleValue();
   	 this.MtoRenRetFFMMPOS=(double) ((Number) registro.get("MtoRenRetFFMMPOS")).doubleValue();
   	 this.MtoRenRetFFMMNEG=(double) ((Number) registro.get("MtoRenRetFFMMNEG")).doubleValue();
   	 this.MtoRenRetOTROSPOS=(double) ((Number) registro.get("MtoRenRetOTROSPOS")).doubleValue();
   	 this.MtoRenRetOTROSNEG=(double) ((Number) registro.get("MtoRenRetOTROSNEG")).doubleValue();
   	 
   	 this.sumSalAhorroNetoEjerNEGT1=(double) ((Number) registro.get("SalAhorroNetoEjerNEGT1")).doubleValue();
  	 this.sumSalArrastreEjerSIGT1=(double) ((Number) registro.get("SalArrastreEjerSIGT1")).doubleValue();
  	 this.sumSalAhorroNetoEjerT2=(double) ((Number) registro.get("SalAhorroNetoEjerT2")).doubleValue();
  	 this.sumSalArrastreEjerSigNegT2=(double) ((Number) registro.get("SalArrastreEjerSigNegT2")).doubleValue();
  	 this.sumMtoRetiro57BisT2=(double) ((Number) registro.get("MtoRetiro57BisT2")).doubleValue();
  	 this.sumSalAhorroNetoEjeNegT3=(double) ((Number) registro.get("SalAhorroNetoEjeNegT3")).doubleValue();
  	 this.sumSalArrastreEjeSigNegT3=(double) ((Number) registro.get("SalArrastreEjeSigNegT3")).doubleValue();
  	 this.sumMtoRetiroAnoT3=(double) ((Number) registro.get("MtoRetiroAnoT3")).doubleValue();
  	 this.sumMtoRenRetFFMMPOS=(double) ((Number) registro.get("MtoRenRetFFMMPOS")).doubleValue();
  	 this.sumMtoRenRetFFMMNEG=(double) ((Number) registro.get("MtoRenRetFFMMNEG")).doubleValue();
  	 this.sumMtoRenRetOTROSPOS=(double) ((Number) registro.get("MtoRenRetOTROSPOS")).doubleValue();
  	 this.sumMtoRenRetOTROSNEG=(double) ((Number) registro.get("MtoRenRetOTROSNEG")).doubleValue();
   	 
    }
    
	
	public Date getFechaOperacion() {
        return fechaOperacion;
    }
	
	public long getNumeroCertificado() {
		return numeroCertificado;
	}

	public void setNumeroCertificado(long numeroCertificado) {
		this.numeroCertificado = numeroCertificado;
	}

	public String getRutCliente() {
		return RutCliente;
	}
	
	public void setRutCliente(String RutCliente) {
		this.RutCliente=RutCliente;
	}
	
	public String getRutInstitucion() {
		return RutInstitucion;
	}
	
	public void setRutInstitucion(String RutInstitucion) {
		this.RutInstitucion=RutInstitucion;
	}
	
	public double getSalAhorroNetoEjerNEGT1() {
		return SalAhorroNetoEjerNEGT1;
	}
	
	public void setSalAhorroNetoEjerNEGT1(double SalAhorroNetoEjerNEGT1) {
		this.SalAhorroNetoEjerNEGT1=SalAhorroNetoEjerNEGT1;
	}
	
	public double getSalArrastreEjerSIGT1() {
		return SalArrastreEjerSIGT1;
	}
	
	public void setSalArrastreEjerSIGT1(double SalArrastreEjerSIGT1) {
		this.SalArrastreEjerSIGT1=SalArrastreEjerSIGT1;
	}
	
	public double getSalAhorroNetoEjerT2() {
		return SalAhorroNetoEjerT2;
	}
	
	public void setSalAhorroNetoEjerT2(double SalAhorroNetoEjerT2) {
		this.SalAhorroNetoEjerT2=SalAhorroNetoEjerT2;
	}
	
	public double getSalArrastreEjerSigNegT2() {
		return SalArrastreEjerSigNegT2;
	}
	
	public void setSalArrastreEjerSigNegT2(double SalArrastreEjerSigNegT2) {
		this.SalArrastreEjerSigNegT2=SalArrastreEjerSigNegT2;
	}
	
	public double getMtoRetiro57BisT2() {
		return MtoRetiro57BisT2;
	}
	
	public void setMtoRetiro57BisT2(double MtoRetiro57BisT2) {
		this.MtoRetiro57BisT2=MtoRetiro57BisT2;
	}
	
	public double getSalAhorroNetoEjeNegT3() {
		return SalAhorroNetoEjeNegT3;
	}
	
	public void setSalAhorroNetoEjeNegT3(double SalAhorroNetoEjeNegT3) {
		this.SalAhorroNetoEjeNegT3=SalAhorroNetoEjeNegT3;
	}
	
	public double getSalArrastreEjeSigNegT3() {
		return SalArrastreEjeSigNegT3;
	}
	
	public void setSalArrastreEjeSigNegT3(double SalArrastreEjeSigNegT3) {
		this.SalArrastreEjeSigNegT3=SalArrastreEjeSigNegT3;
	}
	
	public double getMtoRetiroAnoT3() {
		return MtoRetiroAnoT3;
	}
	
	public void setMtoRetiroAnoT3(double MtoRetiroAnoT3) {
		this.MtoRetiroAnoT3=MtoRetiroAnoT3;
	}
	
	public double getMtoRenRetFFMMPOS() {
		return MtoRenRetFFMMPOS;
	}
	
	public void setMtoRenRetFFMMPOS(double MtoRenRetFFMMPOS) {
		this.MtoRenRetFFMMPOS=MtoRenRetFFMMPOS;
	}
	
	public double getMtoRenRetFFMMNEG() {
		return MtoRenRetFFMMNEG;
	}
	
	public void setMtoRenRetFFMMNEG(double MtoRenRetFFMMNEG) {
		this.MtoRenRetFFMMNEG=MtoRenRetFFMMNEG;
	}
	
	public double getMtoRenRetOTROSPOS(){
		return MtoRenRetOTROSPOS;
	}
	
	public void setMtoRenRetOTROSPOS(double MtoRenRetOTROSPOS) {
		this.MtoRenRetOTROSPOS=MtoRenRetOTROSPOS;
	}
	
	public double getMtoRenRetOTROSNEG() {
		return MtoRenRetOTROSNEG;
	}
	
	public void setMtoRenRetOTROSNEG(double MtoRenRetOTROSNEG) {
		this.MtoRenRetOTROSNEG=MtoRenRetOTROSNEG;
	}
	
	public double getsumSalAhorroNetoEjerNEGT1() {
		return sumSalAhorroNetoEjerNEGT1;
	}
	
	public void setsumSalAhorroNetoEjerNEGT1(double sumSalAhorroNetoEjerNEGT1) {
		this.sumSalAhorroNetoEjerNEGT1=sumSalAhorroNetoEjerNEGT1;
	}
	
	public double getsumSalArrastreEjerSIGT1() {
		return sumSalArrastreEjerSIGT1;
	}
	
	public void setsumSalArrastreEjerSIGT1(double sumSalArrastreEjerSIGT1) {
		this.sumSalArrastreEjerSIGT1=sumSalArrastreEjerSIGT1;
	}
	
	public double getsumSalAhorroNetoEjerT2() {
		return sumSalAhorroNetoEjerT2;
	}
	
	public void setsumSalAhorroNetoEjerT2(double sumSalAhorroNetoEjerT2) {
		this.sumSalAhorroNetoEjerT2=sumSalAhorroNetoEjerT2;
	}
	
	public double getsumSalArrastreEjerSigNegT2() {
		return sumSalArrastreEjerSigNegT2;
	}
	
	public void setsumSalArrastreEjerSigNegT2(double sumSalArrastreEjerSigNegT2) {
		this.sumSalArrastreEjerSigNegT2=sumSalArrastreEjerSigNegT2;
	}
	
	public double getsumMtoRetiro57BisT2() {
		return sumMtoRetiro57BisT2;
	}
	
	public void setsumMtoRetiro57BisT2(double sumMtoRetiro57BisT2) {
		this.sumMtoRetiro57BisT2=sumMtoRetiro57BisT2;
	}
	
	public double getsumSalAhorroNetoEjeNegT3() {
		return sumSalAhorroNetoEjeNegT3;
	}
	
	public void setsumSalAhorroNetoEjeNegT3(double sumSalAhorroNetoEjeNegT3) {
		this.sumSalAhorroNetoEjeNegT3=sumSalAhorroNetoEjeNegT3;
	}
	
	public double getsumSalArrastreEjeSigNegT3() {
		return sumSalArrastreEjeSigNegT3;
	}
	
	public void setsumSalArrastreEjeSigNegT3(double sumSalArrastreEjeSigNegT3) {
		this.sumSalArrastreEjeSigNegT3=sumSalArrastreEjeSigNegT3;
	}
	
	public double getsumMtoRetiroAnoT3() {
		return sumMtoRetiroAnoT3;
	}
	
	public void setsumMtoRetiroAnoT3(double sumMtoRetiroAnoT3) {
		this.sumMtoRetiroAnoT3=sumMtoRetiroAnoT3;
	}
	
	public double getsumMtoRenRetFFMMPOS() {
		return sumMtoRenRetFFMMPOS;
	}
	
	public void setsumMtoRenRetFFMMPOS(double sumMtoRenRetFFMMPOS) {
		this.sumMtoRenRetFFMMPOS=sumMtoRenRetFFMMPOS;
	}
	
	public double getsumMtoRenRetFFMMNEG() {
		return sumMtoRenRetFFMMNEG;
	}
	
	public void setsumMtoRenRetFFMMNEG(double sumMtoRenRetFFMMNEG) {
		this.sumMtoRenRetFFMMNEG=sumMtoRenRetFFMMNEG;
	}
	
	public double getsumMtoRenRetOTROSPOS() {
		return sumMtoRenRetOTROSPOS;
	}
	
	public void setsumMtoRenRetOTROSPOS(double sumMtoRenRetOTROSPOS) {
		this.sumMtoRenRetOTROSPOS=sumMtoRenRetOTROSPOS;
	}
	
	public double getsumMtoRenRetOTROSNEG() {
		return sumMtoRenRetOTROSNEG;
	}
	
	public void setsumMtoRenRetOTROSNEG(double sumMtoRenRetOTROSNEG) {
		this.sumMtoRenRetOTROSNEG=sumMtoRenRetOTROSNEG;
	}


	public RegistroDJ1944() {
    }
	
    public void acumular(final RegistroDJ1944 registroDJ1944) {  

         this.sumSalAhorroNetoEjerNEGT1 += Math.round(registroDJ1944.getsumSalAhorroNetoEjerNEGT1());
      	 this.sumSalArrastreEjerSIGT1 += Math.round(registroDJ1944.getsumSalArrastreEjerSIGT1());
      	 this.sumSalAhorroNetoEjerT2 += Math.round(registroDJ1944.getsumSalAhorroNetoEjerT2());
      	 this.sumSalArrastreEjerSigNegT2 += Math.round(registroDJ1944.getsumSalArrastreEjerSigNegT2());
      	 this.sumMtoRetiro57BisT2 += Math.round(registroDJ1944.getsumMtoRetiro57BisT2());
      	 this.sumSalAhorroNetoEjeNegT3 += Math.round(registroDJ1944.getsumSalAhorroNetoEjeNegT3());
      	 this.sumSalArrastreEjeSigNegT3 += Math.round(registroDJ1944.getsumSalArrastreEjeSigNegT3());
      	 this.sumMtoRetiroAnoT3 += Math.round(registroDJ1944.getsumMtoRetiroAnoT3());
      	 this.sumMtoRenRetFFMMPOS += Math.round(registroDJ1944.getsumMtoRenRetFFMMPOS());
      	 this.sumMtoRenRetFFMMNEG += Math.round(registroDJ1944.getsumMtoRenRetFFMMNEG());
      	 this.sumMtoRenRetOTROSPOS += Math.round(registroDJ1944.getsumMtoRenRetOTROSPOS());
      	 this.sumMtoRenRetOTROSNEG += Math.round(registroDJ1944.getsumMtoRenRetOTROSNEG());
    }
	
}
