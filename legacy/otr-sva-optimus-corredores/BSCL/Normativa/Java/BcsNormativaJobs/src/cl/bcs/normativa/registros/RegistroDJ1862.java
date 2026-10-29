package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1862 {

    /**
     * Declaracion de variables para la DJ
     */
    
	private String rutCliente;
    private String taxID;
    private String paisOtorganteIdentificador;
    private String nombre;
    private String codigoMoneda;
    private double montoEnPesos;
    private long numeroTransaccion;
    private String codigoModalidadOperacion;
    private String codigoConcepto;
    private Date fechaOperacion;
    private String nombreContraparte;
    private String taxIDContraparte;
    private String codigoPaisContraparte;
    private String bancoContraparte;
    private double sumaMontoEnPesosPositivo;
    private double sumaMontoEnPesosNegativo;
    
	public String getRutCliente() {
		return rutCliente;
	}

	public void setRutCliente(String rutCliente) {
		this.rutCliente = rutCliente;
	}

	public String getTaxID() {
		return taxID;
	}

	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}

	public String getPaisOtorganteIdentificador() {
		return paisOtorganteIdentificador;
	}

	public void setPaisOtorganteIdentificador(String paisOtorganteIdentificador) {
		this.paisOtorganteIdentificador = paisOtorganteIdentificador;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCodigoMoneda() {
		return codigoMoneda;
	}

	public void setCodigoMoneda(String codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
	}

	public double getMontoEnPesos() {
		return montoEnPesos;
	}

	public void setMontoEnPesos(double montoEnPesos) {
		this.montoEnPesos = montoEnPesos;
	}

	public long getNumeroTransaccion() {
		return numeroTransaccion;
	}

	public void setNumeroTransaccion(long numeroTransaccion) {
		this.numeroTransaccion = numeroTransaccion;
	}

	public String getCodigoModalidadOperacion() {
		return codigoModalidadOperacion;
	}

	public void setCodigoModalidadOperacion(String codigoModalidadOperacion) {
		this.codigoModalidadOperacion = codigoModalidadOperacion;
	}

	public String getCodigoConcepto() {
		return codigoConcepto;
	}

	public void setCodigoConcepto(String codigoConcepto) {
		this.codigoConcepto = codigoConcepto;
	}

	public Date getFechaOperacion() {
		return fechaOperacion;
	}

	public void setFechaOperacion(Date fechaOperacion) {
		this.fechaOperacion = fechaOperacion;
	}

	public String getNombreContraparte() {
		return nombreContraparte;
	}

	public void setNombreContraparte(String nombreContraparte) {
		this.nombreContraparte = nombreContraparte;
	}

	public String getTaxIDContraparte() {
		return taxIDContraparte;
	}

	public void setTaxIDContraparte(String taxIDContraparte) {
		this.taxIDContraparte = taxIDContraparte;
	}

	public String getCodigoPaisContraparte() {
		return codigoPaisContraparte;
	}

	public void setCodigoPaisContraparte(String codigoPaisContraparte) {
		this.codigoPaisContraparte = codigoPaisContraparte;
	}

	public String getBancoContraparte() {
		return bancoContraparte;
	}

	public void setBancoContraparte(String bancoContraparte) {
		this.bancoContraparte = bancoContraparte;
	}

	public double getSumaMontoEnPesosPositivo() {
		return sumaMontoEnPesosPositivo;
	}

	public void setSumaMontoEnPesosPositivo(double sumaMontoEnPesosPositivo) {
		this.sumaMontoEnPesosPositivo = sumaMontoEnPesosPositivo;
	}

	public double getSumaMontoEnPesosNegativo() {
		return sumaMontoEnPesosNegativo;
	}

	public void setSumaMontoEnPesosNegativo(double sumaMontoEnPesosNegativo) {
		this.sumaMontoEnPesosNegativo = sumaMontoEnPesosNegativo;
	} 

	//Obtener valor del registro
    public RegistroDJ1862(final Registro registro) {
    	
   	 this.rutCliente = (String) registro.get("RutCliente");
   	 this.taxID = (String) registro.get("TaxID");
   	 this.paisOtorganteIdentificador = (String) registro.get("PaisOtorganteIdentificador");
   	 this.nombre = (String) registro.get("Nombre");
   	 this.codigoMoneda = (String) registro.get("CodigoMoneda");
   	 this.montoEnPesos = (double) ((Number) registro.get("MontoEnPesos")).doubleValue();
   	 this.numeroTransaccion = (Long) registro.get("NumeroTransaccion");
   	 this.codigoModalidadOperacion = (String) registro.get("CodigoModalidadOperacion");
   	 this.codigoConcepto = (String) registro.get("CodigoConcepto");
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.nombreContraparte = (String) registro.get("NombreContraparte");
  	 this.taxIDContraparte = (String) registro.get("TaxIDContraparte");
  	 this.codigoPaisContraparte = (String) registro.get("CodigoPaisContraparte");
  	 this.bancoContraparte = (String) registro.get("BancoContraparte");
  	 this.sumaMontoEnPesosPositivo = (double) ((Number) registro.get("MontoEnPesos")).doubleValue();
  	 this.sumaMontoEnPesosNegativo = (double) ((Number) registro.get("MontoEnPesos")).doubleValue();
    }

	public RegistroDJ1862() {
    }
	
    public void acumular(final RegistroDJ1862 registroDJ1862) {        
        this.sumaMontoEnPesosPositivo += registroDJ1862.getSumaMontoEnPesosPositivo();
        this.sumaMontoEnPesosNegativo += registroDJ1862.getSumaMontoEnPesosNegativo();
    }
	
}
