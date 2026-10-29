package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1919 {

    /**
     * Declaracion de variables para la DJ
     */
    
	private String rutEmisor;
    private String taxID;
    private String rutCliente;
    private String codigoPaisConstitucion;
    private String codigoPaisResidencia;
    private long porcentajePropiedadDirectaIndirecta;
    private long participacionDirectaIndirectaControl;
    private String nemotecnicoSVS;
    private Date fechaOperacion;
    private String numeroCuentaDCV;
    private String moneda;
    private double valorEmision;
    private double valorColocacion;
    private double valorAdquisicion;
    private double valorAdquisicionEnPesos;
    private double valorEnajenacion;
    private double valorEnajenacionEnPesos;
    private double mayorValorEnajenacion;
    private double mayorValorEnajenacionEnPesos;
    private double sumaMayorValorEnajenacion;
    private double sumaMayorValorEnajenacionEnPesos;

	public String getRutEmisor() {
		return rutEmisor;
	}


	public void setRutEmisor(String rutEmisor) {
		this.rutEmisor = rutEmisor;
	}


	public String getTaxID() {
		return taxID;
	}


	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}


	public String getRutCliente() {
		return rutCliente;
	}


	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}


	public String getCodigoPaisConstitucion() {
		return codigoPaisConstitucion;
	}


	public void setCodigoPaisConstitucion(String codigoPaisConstitucion) {
		this.codigoPaisConstitucion = codigoPaisConstitucion;
	}


	public String getCodigoPaisResidencia() {
		return codigoPaisResidencia;
	}


	public void setCodigoPaisResidencia(String codigoPaisResidencia) {
		this.codigoPaisResidencia = codigoPaisResidencia;
	}


	public long getPorcentajePropiedadDirectaIndirecta() {
		return porcentajePropiedadDirectaIndirecta;
	}


	public void setPorcentajePropiedadDirectaIndirecta(long porcentajePropiedadDirectaIndirecta) {
		this.porcentajePropiedadDirectaIndirecta = porcentajePropiedadDirectaIndirecta;
	}


	public long getParticipacionDirectaIndirectaControl() {
		return participacionDirectaIndirectaControl;
	}


	public void setParticipacionDirectaIndirectaControl(long participacionDirectaIndirectaControl) {
		this.participacionDirectaIndirectaControl = participacionDirectaIndirectaControl;
	}


	public String getNemotecnicoSVS() {
		return nemotecnicoSVS;
	}


	public void setNemotecnicoSVS(String nemotecnicoSVS) {
		this.nemotecnicoSVS = nemotecnicoSVS;
	}


	public Date getFechaOperacion() {
		return fechaOperacion;
	}


	public void setFechaOperacion(Date fechaOperacion) {
		this.fechaOperacion = fechaOperacion;
	}


	public String getNumeroCuentaDCV() {
		return numeroCuentaDCV;
	}


	public void setNumeroCuentaDCV(String numeroCuentaDCV) {
		this.numeroCuentaDCV = numeroCuentaDCV;
	}


	public String getMoneda() {
		return moneda;
	}


	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}


	public double getValorEmision() {
		return valorEmision;
	}


	public void setValorEmision(double valorEmision) {
		this.valorEmision = valorEmision;
	}


	public double getValorColocacion() {
		return valorColocacion;
	}


	public void setValorColocacion(double valorColocacion) {
		this.valorColocacion = valorColocacion;
	}


	public double getValorAdquisicion() {
		return valorAdquisicion;
	}


	public void setValorAdquisicion(double valorAdquisicion) {
		this.valorAdquisicion = valorAdquisicion;
	}


	public double getValorAdquisicionEnPesos() {
		return valorAdquisicionEnPesos;
	}


	public void setValorAdquisicionEnPesos(double valorAdquisicionEnPesos) {
		this.valorAdquisicionEnPesos = valorAdquisicionEnPesos;
	}


	public double getValorEnajenacion() {
		return valorEnajenacion;
	}


	public void setValorEnajenacion(double valorEnajenacion) {
		this.valorEnajenacion = valorEnajenacion;
	}


	public double getValorEnajenacionEnPesos() {
		return valorEnajenacionEnPesos;
	}


	public void setValorEnajenacionEnPesos(double valorEnajenacionEnPesos) {
		this.valorEnajenacionEnPesos = valorEnajenacionEnPesos;
	}


	public double getMayorValorEnajenacion() {
		return mayorValorEnajenacion;
	}


	public void setMayorValorEnajenacion(double mayorValorEnajenacion) {
		this.mayorValorEnajenacion = mayorValorEnajenacion;
	}


	public double getMayorValorEnajenacionEnPesos() {
		return mayorValorEnajenacionEnPesos;
	}


	public void setMayorValorEnajenacionEnPesos(double mayorValorEnajenacionEnPesos) {
		this.mayorValorEnajenacionEnPesos = mayorValorEnajenacionEnPesos;
	}


	public double getSumaMayorValorEnajenacion() {
		return sumaMayorValorEnajenacion;
	}


	public void setSumaMayorValorEnajenacion(double sumaMayorValorEnajenacion) {
		this.sumaMayorValorEnajenacion = sumaMayorValorEnajenacion;
	}


	public double getSumaMayorValorEnajenacionEnPesos() {
		return sumaMayorValorEnajenacionEnPesos;
	}


	public void setSumaMayorValorEnajenacionEnPesos(double sumaMayorValorEnajenacionEnPesos) {
		this.sumaMayorValorEnajenacionEnPesos = sumaMayorValorEnajenacionEnPesos;
	}


	//Obtener valor del registro
    public RegistroDJ1919(final Registro registro) {
    	
   	 this.rutEmisor = (String) registro.get("RutEmisor");
   	 this.taxID = (String) registro.get("TaxID");
   	 this.rutCliente = (String) registro.get("RutCliente");
   	 this.codigoPaisConstitucion = (String) registro.get("CodigoPaisConstitucion");
   	 this.codigoPaisResidencia = (String) registro.get("CodigoPaisResidencia");
     this.porcentajePropiedadDirectaIndirecta = (Long) registro.get("PorcentajePropiedadDirectaIndirecta");
   	 this.participacionDirectaIndirectaControl = (Long) registro.get("ParticipacionDirectaIndirectaControl");
   	 this.nemotecnicoSVS = (String) registro.get("NemotecnicoSVS");
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.numeroCuentaDCV = (String) registro.get("NumeroCuentaDCV");
   	 this.moneda = (String) registro.get("Moneda");
  	 this.valorEmision = (double) ((Number) registro.get("ValorEmision")).doubleValue();
  	 this.valorColocacion = (double) ((Number) registro.get("ValorColocacion")).doubleValue();
  	 this.valorAdquisicion = (double) ((Number) registro.get("ValorAdquisicion")).doubleValue();
  	 this.valorAdquisicionEnPesos = (double) ((Number) registro.get("ValorAdquisicionEnPesos")).doubleValue();
  	 this.valorEnajenacion = (double) ((Number) registro.get("ValorEnajenacion")).doubleValue();
  	 this.valorEnajenacionEnPesos = (double) ((Number) registro.get("ValorEnajenacionEnPesos")).doubleValue();
  	 this.mayorValorEnajenacion = (double) ((Number) registro.get("MayorValorEnajenacion")).doubleValue();
  	 this.mayorValorEnajenacionEnPesos = (double) ((Number) registro.get("MayorValorEnajenacionEnPesos")).doubleValue();
  	 this.sumaMayorValorEnajenacion = (double) ((Number) registro.get("MayorValorEnajenacion")).doubleValue();
  	 this.sumaMayorValorEnajenacionEnPesos = (double) ((Number) registro.get("MayorValorEnajenacionEnPesos")).doubleValue();
 	 
    }
    
    
	public RegistroDJ1919() {
    }
	
    public void acumular(final RegistroDJ1919 registroDJ1919) {   
         this.sumaMayorValorEnajenacion = registroDJ1919.getSumaMayorValorEnajenacion();
     	 this.sumaMayorValorEnajenacionEnPesos = registroDJ1919.getSumaMayorValorEnajenacionEnPesos();
    }
	
}
