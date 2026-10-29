package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;
public class RegistroDJ1932 {

    /**
     * Declaracion de variables para la DJ
     */
    
	private long mes;
	private Date fechaOperacion;
    private String rutDepositanteInversionistaPrestamista;
    private String taxID;
    private String pais;
    private double depositosInversionesPrestamos;
    private double rescatesDevoluciones;
    private double interesesPercibidas;
    private double dividendosPercibidas;
    private double contratosPorDiferenciaPercibidas;
    private double otrasRentasPercibidas;
    private double interesesDevengadas;
    private double otrasRentasDevengadas;
	private String CodPaisRenta;
    private double sumaDepositosInversionesPrestamos;
    private double sumaRescatesDevoluciones;
    private double sumaInteresesPercibidas;
    private double sumaDividendosPercibidas;
    private double sumaContratosPorDiferenciaPercibidas;
    private double sumaOtrasRentasPercibidas;
    private double sumaInteresesDevengadas;
    private double sumaOtrasRentasDevengadas;
	//Obtener valor del registro
    
    public RegistroDJ1932(final Registro registro) {
    	
   	 this.mes = (Long) registro.get("Mes");
   	 this.fechaOperacion = (Date) registro.get("FechaOperacion");
   	 this.rutDepositanteInversionistaPrestamista = (String) registro.get("RutDepositanteInversionistaPrestamista");
   	 this.taxID = (String) registro.get("TaxID");
   	 this.pais = (String) registro.get("Pais");
   	 this.depositosInversionesPrestamos = (double) ((Number) registro.get("DepositosInversionesPrestamos")).doubleValue();
   	 this.rescatesDevoluciones = (double) ((Number) registro.get("RescatesDevoluciones")).doubleValue();
   	 this.interesesPercibidas = (double) ((Number) registro.get("InteresesPercibidas")).doubleValue();
   	 this.dividendosPercibidas = (double) ((Number) registro.get("DividendosPercibidas")).doubleValue();
   	 this.contratosPorDiferenciaPercibidas = (double) ((Number) registro.get("ContratosPorDiferenciaPercibidas")).doubleValue();
   	 this.otrasRentasPercibidas = (double) ((Number) registro.get("OtrasRentasPercibidas")).doubleValue();
   	 this.interesesDevengadas = (double) ((Number) registro.get("InteresesDevengadas")).doubleValue();
  	 this.otrasRentasDevengadas = (double) ((Number) registro.get("OtrasRentasDevengadas")).doubleValue();
	 this.CodPaisRenta = (String) registro.get("CodPaisRenta");
  	 this.sumaDepositosInversionesPrestamos = (double) ((Number) registro.get("DepositosInversionesPrestamos")).doubleValue();
  	 this.sumaRescatesDevoluciones = (double) ((Number) registro.get("RescatesDevoluciones")).doubleValue();
  	 this.sumaInteresesPercibidas = (double) ((Number) registro.get("InteresesPercibidas")).doubleValue();
  	 this.sumaDividendosPercibidas = (double) ((Number) registro.get("DividendosPercibidas")).doubleValue();
  	 this.sumaContratosPorDiferenciaPercibidas = (double) ((Number) registro.get("ContratosPorDiferenciaPercibidas")).doubleValue();
  	 this.sumaOtrasRentasPercibidas = (double) ((Number) registro.get("OtrasRentasPercibidas")).doubleValue();
  	 this.sumaInteresesDevengadas = (double) ((Number) registro.get("InteresesDevengadas")).doubleValue();
  	 this.sumaOtrasRentasDevengadas = (double) ((Number) registro.get("OtrasRentasDevengadas")).doubleValue();
  	 
    }
    
	public long getMes() {
		return mes;
	}
	
	public Date getFechaOperacion() {
        return fechaOperacion;
    }


	public void setMes(long mes) {
		this.mes = mes;
	}


	public String getRutDepositanteInversionistaPrestamista() {
		return rutDepositanteInversionistaPrestamista;
	}


	public void setRutDepositanteInversionistaPrestamista(String rutDepositanteInversionistaPrestamista) {
		this.rutDepositanteInversionistaPrestamista = rutDepositanteInversionistaPrestamista;
	}


	public String getTaxID() {
		return taxID;
	}


	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}


	public String getPais() {
		return pais;
	}


	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCodPaisRenta() {
		return CodPaisRenta;
	}


	public void setCodPaisRenta(String CodPaisRenta) {
		this.CodPaisRenta = CodPaisRenta;
	}


	public double getDepositosInversionesPrestamos() {
		return depositosInversionesPrestamos;
	}


	public void setDepositosInversionesPrestamos(double depositosInversionesPrestamos) {
		this.depositosInversionesPrestamos = depositosInversionesPrestamos;
	}


	public double getRescatesDevoluciones() {
		return rescatesDevoluciones;
	}


	public void setRescatesDevoluciones(double rescatesDevoluciones) {
		this.rescatesDevoluciones = rescatesDevoluciones;
	}


	public double getInteresesPercibidas() {
		return interesesPercibidas;
	}


	public void setInteresesPercibidas(double interesesPercibidas) {
		this.interesesPercibidas = interesesPercibidas;
	}


	public double getDividendosPercibidas() {
		return dividendosPercibidas;
	}


	public void setDividendosPercibidas(double dividendosPercibidas) {
		this.dividendosPercibidas = dividendosPercibidas;
	}


	public double getContratosPorDiferenciaPercibidas() {
		return contratosPorDiferenciaPercibidas;
	}


	public void setContratosPorDiferenciaPercibidas(double contratosPorDiferenciaPercibidas) {
		this.contratosPorDiferenciaPercibidas = contratosPorDiferenciaPercibidas;
	}


	public double getOtrasRentasPercibidas() {
		return otrasRentasPercibidas;
	}


	public void setOtrasRentasPercibidas(double otrasRentasPercibidas) {
		this.otrasRentasPercibidas = otrasRentasPercibidas;
	}


	public double getInteresesDevengadas() {
		return interesesDevengadas;
	}


	public void setInteresesDevengadas(double interesesDevengadas) {
		this.interesesDevengadas = interesesDevengadas;
	}


	public double getOtrasRentasDevengadas() {
		return otrasRentasDevengadas;
	}


	public void setOtrasRentasDevengadas(double otrasRentasDevengadas) {
		this.otrasRentasDevengadas = otrasRentasDevengadas;
	}


	public double getSumaDepositosInversionesPrestamos() {
		return sumaDepositosInversionesPrestamos;
	}


	public void setSumaDepositosInversionesPrestamos(double sumaDepositosInversionesPrestamos) {
		this.sumaDepositosInversionesPrestamos = sumaDepositosInversionesPrestamos;
	}


	public double getSumaRescatesDevoluciones() {
		return sumaRescatesDevoluciones;
	}


	public void setSumaRescatesDevoluciones(double sumaRescatesDevoluciones) {
		this.sumaRescatesDevoluciones = sumaRescatesDevoluciones;
	}


	public double getSumaInteresesPercibidas() {
		return sumaInteresesPercibidas;
	}


	public void setSumaInteresesPercibidas(double sumaInteresesPercibidas) {
		this.sumaInteresesPercibidas = sumaInteresesPercibidas;
	}


	public double getSumaDividendosPercibidas() {
		return sumaDividendosPercibidas;
	}


	public void setSumaDividendosPercibidas(double sumaDividendosPercibidas) {
		this.sumaDividendosPercibidas = sumaDividendosPercibidas;
	}


	public double getSumaContratosPorDiferenciaPercibidas() {
		return sumaContratosPorDiferenciaPercibidas;
	}


	public void setSumaContratosPorDiferenciaPercibidas(double sumaContratosPorDiferenciaPercibidas) {
		this.sumaContratosPorDiferenciaPercibidas = sumaContratosPorDiferenciaPercibidas;
	}


	public double getSumaOtrasRentasPercibidas() {
		return sumaOtrasRentasPercibidas;
	}


	public void setSumaOtrasRentasPercibidas(double sumaOtrasRentasPercibidas) {
		this.sumaOtrasRentasPercibidas = sumaOtrasRentasPercibidas;
	}


	public double getSumaInteresesDevengadas() {
		return sumaInteresesDevengadas;
	}


	public void setSumaInteresesDevengadas(double sumaInteresesDevengadas) {
		this.sumaInteresesDevengadas = sumaInteresesDevengadas;
	}


	public double getSumaOtrasRentasDevengadas() {
		return sumaOtrasRentasDevengadas;
	}


	public void setSumaOtrasRentasDevengadas(double sumaOtrasRentasDevengadas) {
		this.sumaOtrasRentasDevengadas = sumaOtrasRentasDevengadas;
	}


	public RegistroDJ1932() {
    }
	
    public void acumular(final RegistroDJ1932 registroDJ1932) {   
         this.sumaDepositosInversionesPrestamos += registroDJ1932.getSumaDepositosInversionesPrestamos();
     	 this.sumaRescatesDevoluciones += registroDJ1932.getSumaRescatesDevoluciones();
     	 this.sumaInteresesPercibidas += registroDJ1932.getSumaInteresesPercibidas();
    	 this.sumaDividendosPercibidas += registroDJ1932.getSumaDividendosPercibidas();
    	 this.sumaContratosPorDiferenciaPercibidas += registroDJ1932.getSumaContratosPorDiferenciaPercibidas();
     	 this.sumaOtrasRentasPercibidas += registroDJ1932.getSumaOtrasRentasPercibidas();
     	 this.sumaInteresesDevengadas += registroDJ1932.getSumaInteresesDevengadas();
    	 this.sumaOtrasRentasDevengadas += registroDJ1932.getSumaOtrasRentasDevengadas();
    }
	
}
