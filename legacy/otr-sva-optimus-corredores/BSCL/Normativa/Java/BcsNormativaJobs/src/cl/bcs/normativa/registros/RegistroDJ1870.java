package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1870 {

    /**
     * Declaracion de variables para la DJ
     */
    
	private String datosPersonaRut;
    private String datosPersonaIndentificadorExtranjero;
    private String datosPersonaPaisOtorganteIdentificador;
    private String datosPersonaNombreRazonSocial;
    
    private String tipoTransaccionDJ;
    //private Double valoresTransados;

	private String valoresTransados;
    
    private String fechaTransaccionDia;
    
    private String fechaTransaccionMes;
    private String fechaTransaccionAnio;
     
    private String documentoTipoDocumento;
    
    private long documentoNumero;
    private String codigoMonedaTransada;
    private double montoCompras;
    private double montoVentas;
    private double tipoCambio;
    private double sumaMontoCompras;
    private double sumaMontoVentas;
  
	public String getDatosPersonaRut() {
		return datosPersonaRut;
	}


	public void setDatosPersonaRut(String datosPersonaRut) {
		this.datosPersonaRut = datosPersonaRut;
		/* this.datosPersonaRut = "15983577-4"; */
	}


	public String getDatosPersonaIndentificadorExtranjero() {
		return datosPersonaIndentificadorExtranjero;
	}


	public void setDatosPersonaIndentificadorExtranjero(String datosPersonaIndentificadorExtranjero) {
		this.datosPersonaIndentificadorExtranjero = datosPersonaIndentificadorExtranjero;
	}


	public String getDatosPersonaPaisOtorganteIdentificador() {
		return datosPersonaPaisOtorganteIdentificador;
	}


	public void setDatosPersonaPaisOtorganteIdentificador(String datosPersonaPaisOtorganteIdentificador) {
		this.datosPersonaPaisOtorganteIdentificador = datosPersonaPaisOtorganteIdentificador;
	}


	public String getDatosPersonaNombreRazonSocial() {
		return datosPersonaNombreRazonSocial;
	}


	public void setDatosPersonaNombreRazonSocial(String datosPersonaNombreRazonSocial) {
		this.datosPersonaNombreRazonSocial = datosPersonaNombreRazonSocial;
	}

	public String getTipoTransaccionDJ() {
		return tipoTransaccionDJ;
	}


	public void setTipoTransaccionDJ(String tipoTransaccionDJ) {
		this.tipoTransaccionDJ = tipoTransaccionDJ;
	}

	
	public String getValoresTransados() {
		return valoresTransados;
	}


	public void setValoresTransados(String valoresTransados) {
		this.valoresTransados = valoresTransados;
	}
	
	
	public String getFechaTransaccionDia() {
		return fechaTransaccionDia;
	}


	public void setFechaTransaccionDia(String fechaTransaccionDia) {
		this.fechaTransaccionDia = fechaTransaccionDia;
	}
	

	public String getFechaTransaccionMes() {
		return fechaTransaccionMes;
	}


	public void setFechaTransaccionMes(String fechaTransaccionMes) {
		this.fechaTransaccionMes = fechaTransaccionMes;
	}


	public String getFechaTransaccionAnio() {
		return fechaTransaccionAnio;
	}


	public void setFechaTransaccionAnio(String fechaTransaccionAnio) {
		this.fechaTransaccionAnio = fechaTransaccionAnio;
	}

	
	public String getDocumentoTipoDocumento() {
		return documentoTipoDocumento;
	}


	public void setDocumentoTipoDocumento(String documentoTipoDocumento) {
		this.documentoTipoDocumento = documentoTipoDocumento;
	}

	
	public long getDocumentoNumero() {
		return documentoNumero;
	}


	public void setDocumentoNumero(long documentoNumero) {
		this.documentoNumero = documentoNumero;
	}


	public String getCodigoMonedaTransada() {
		return codigoMonedaTransada;
	}


	public void setCodigoMonedaTransada(String codigoMonedaTransada) {
		this.codigoMonedaTransada = codigoMonedaTransada;
	}


	public double getMontoCompras() {
		return montoCompras;
	}


	public void setMontoCompras(double montoCompras) {
		this.montoCompras = montoCompras;
	}


	public double getMontoVentas() {
		return montoVentas;
	}


	public void setMontoVentas(double montoVentas) {
		this.montoVentas = montoVentas;
	}


	public double getTipoCambio() {
		return tipoCambio;
	}


	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}


	public double getSumaMontoCompras() {
		return sumaMontoCompras;
	}


	public void setSumaMontoCompras(double sumaMontoCompras) {
		this.sumaMontoCompras = sumaMontoCompras;
	}


	public double getSumaMontoVentas() {
		return sumaMontoVentas;
	}


	public void setSumaMontoVentas(double sumaMontoVentas) {
		this.sumaMontoVentas = sumaMontoVentas;
	}

	//Obtener valor del registro
    public RegistroDJ1870(final Registro registro) {
    	
   	 this.datosPersonaRut = (String) registro.get("DatosPersonaRut");
   	 this.datosPersonaIndentificadorExtranjero = (String) registro.get("DatosPersonaIndentificadorExtranjero");
   	 this.datosPersonaPaisOtorganteIdentificador = (String) registro.get("DatosPersonaPaisOtorganteIdentificador");
   	 this.datosPersonaNombreRazonSocial = (String) registro.get("DatosPersonaNombreRazonSocial");
   	 this.tipoTransaccionDJ = (String) registro.get("TipoTransaccionDJ");
   	 //this.valoresTransados = (Double) registro.get("valoresTransados");
	 //this.valoresTransados = (double) ((Number) registro.get("ValoresTransados")).doubleValue();
	 this.valoresTransados = (String) registro.get("ValoresTransados");


	 this.fechaTransaccionDia = (String) registro.get("FechaTransaccionDia");
   	 this.fechaTransaccionMes = (String) registro.get("FechaTransaccionMes");
   	 this.fechaTransaccionAnio = (String) registro.get("FechaTransaccionAnio");
   	 this.documentoTipoDocumento = (String) registro.get("DocumentoTipoDocumento");
   	 this.documentoNumero = (Long) registro.get("DocumentoNumero");
   	 this.codigoMonedaTransada = (String) registro.get("CodigoMonedaTransada");
	 this.montoCompras = (double) ((Number) registro.get("MontoCompras")).doubleValue();
  	 this.montoVentas = (double) ((Number) registro.get("MontoVentas")).doubleValue();
  	 this.tipoCambio = (double) ((Number) registro.get("TipoCambio")).doubleValue();
  	 this.sumaMontoCompras = (double) ((Number) registro.get("MontoCompras")).doubleValue();
  	 this.sumaMontoVentas = (double) ((Number) registro.get("MontoVentas")).doubleValue();
  	
    }
    
    
	public RegistroDJ1870() {
    }
	
    public void acumular(final RegistroDJ1870 registroDJ1870) {        
        this.sumaMontoCompras += Math.round(registroDJ1870.getSumaMontoCompras());
        this.sumaMontoVentas += Math.round(registroDJ1870.getSumaMontoVentas());
    }
	
}
