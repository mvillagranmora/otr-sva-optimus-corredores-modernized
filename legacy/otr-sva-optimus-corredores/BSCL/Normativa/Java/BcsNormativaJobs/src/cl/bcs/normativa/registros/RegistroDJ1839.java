package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1839 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
    
    private String rutParteA;
    private String taxIDParteA;
    private String RutParteB;
    private String taxIDParteB;
    private String codigoPaisParteA;
    private String codigoPaisParteB;
    private long acuerdoMarcoTipo;
    private long acuerdoMarcoNumero;
    private Date acuerdoMarcoFecha;
    private long contratoNumero;
    private Date contratoFechaSuscripcion;
    private long contratoTipo;
    private String contratoNombreInstrumento;
    
	public RegistroDJ1839() {
    }
    
    public String getRutParteA() {
		return rutParteA;
	}

	public void setRutParteA(String rutParteA) {
		this.rutParteA = rutParteA;
	}

	public String getTaxIDParteA() {
		return taxIDParteA;
	}

	public void setTaxIDParteA(String taxIDParteA) {
		this.taxIDParteA = taxIDParteA;
	}

	public String getRutParteB() {
		return RutParteB;
	}

	public void setRutParteB(String rutParteB) {
		RutParteB = rutParteB;
	}

	public String getTaxIDParteB() {
		return taxIDParteB;
	}

	public void setTaxIDParteB(String taxIDParteB) {
		this.taxIDParteB = taxIDParteB;
	}

	public String getCodigoPaisParteA() {
		return codigoPaisParteA;
	}

	public void setCodigoPaisParteA(String codigoPaisParteA) {
		this.codigoPaisParteA = codigoPaisParteA;
	}

	public String getCodigoPaisParteB() {
		return codigoPaisParteB;
	}

	public void setCodigoPaisParteB(String codigoPaisParteB) {
		this.codigoPaisParteB = codigoPaisParteB;
	}

	public long getAcuerdoMarcoTipo() {
		return acuerdoMarcoTipo;
	}

	public void setAcuerdoMarcoTipo(long acuerdoMarcoTipo) {
		this.acuerdoMarcoTipo = acuerdoMarcoTipo;
	}

	public long getAcuerdoMarcoNumero() {
		return acuerdoMarcoNumero;
	}

	public void setAcuerdoMarcoNumero(long acuerdoMarcoNumero) {
		this.acuerdoMarcoNumero = acuerdoMarcoNumero;
	}

	public Date getAcuerdoMarcoFecha() {
		return acuerdoMarcoFecha;
	}

	public void setAcuerdoMarcoFecha(Date acuerdoMarcoFecha) {
		this.acuerdoMarcoFecha = acuerdoMarcoFecha;
	}

	public long getContratoNumero() {
		return contratoNumero;
	}

	public void setContratoNumero(long contratoNumero) {
		this.contratoNumero = contratoNumero;
	}

	public Date getContratoFechaSuscripcion() {
		return contratoFechaSuscripcion;
	}

	public void setContratoFechaSuscripcion(Date contratoFechaSuscripcion) {
		this.contratoFechaSuscripcion = contratoFechaSuscripcion;
	}

	public long getContratoTipo() {
		return contratoTipo;
	}

	public void setContratoTipo(long contratoTipo) {
		this.contratoTipo = contratoTipo;
	}

	public String getContratoNombreInstrumento() {
		return contratoNombreInstrumento;
	}

	public void setContratoNombreInstrumento(String contratoNombreInstrumento) {
		this.contratoNombreInstrumento = contratoNombreInstrumento;
	}

	//Obtener valor del registro
    public RegistroDJ1839(final Registro registro) {
   	
    	this.rutParteA = (String) registro.get("RutCliente");
   	    this.taxIDParteA = (String) registro.get("TaxID");
   	    this.RutParteB = (String) registro.get("RutClienteB");
   	    this.taxIDParteB = (String) registro.get("TaxIDB");
   	    this.codigoPaisParteA = (String) registro.get("CodigoPaisA");
   	    this.codigoPaisParteB = (String) registro.get("CodigoPaisB");
   	    this.acuerdoMarcoTipo = (Long) registro.get("AcuerdoMarcoTipo");
   	    this.acuerdoMarcoNumero = (Long) registro.get("AcuerdoMarcoNumero");
   	    this.acuerdoMarcoFecha = (Date) registro.get("AcuerdoMarcoFechaSuscripcion");
   	    this.contratoNumero = (Long) registro.get("ContratoNumero");
  	    this.contratoFechaSuscripcion = (Date) registro.get("ContratoFechaSuscripcion");
  	    this.contratoTipo = (Long) registro.get("ContratoTipo");
  	    this.contratoNombreInstrumento = (String) registro.get("ContratoNombreInstrumento");
    }
 
    public void acumular(final RegistroDJ1839 registroDJ1839) {
    }
	
}
