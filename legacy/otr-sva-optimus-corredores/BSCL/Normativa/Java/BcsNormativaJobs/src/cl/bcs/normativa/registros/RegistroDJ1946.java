package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;
import java.util.Date;

public class RegistroDJ1946 {

    /**
    * Declaracion de variables para la DJ
    */


    private long operacion;
    private long tipoRenta;
    private Double rentaBruta;
    private Double tasaDeRetencion;
    private Double impuesto;
    private Double rentaLiquida;
    private Double creditoIDPC;
    private Double otrosCreditos;
    private long f50Codigo;
    private long f50Folio;
    private Date fechaPago;
    private String monedaPago;
    private double tipoCambioPago;
    private long pagadorTipoJuridico;
    private String pagadorRazonSocial;
    private String pagadorNombre;
    private String pagadorRut;
    private String pagadorDomicilio;
    private String pagadorCiudad;
    private long pagadorRelacion;
    private String pagadorHolding;
    private long perceptorTipoJuridico;
    private String perceptorPaisResidencia;
    private String perceptorRazonSocial;
    private String perceptorNombre;
    private long perceptorIdentificacionTributaria;
    private String perceptorRut;
    private Date perceptorFechaNacimiento;
    private long perceptorTipoDomicilio;
    private String perceptorDomicilio;
    private String perceptorCiudad;
    private String perceptorPais;
    private long intermediarioTipoJuridico;
    private String intermediarioPaisResidencia;
    private String intermediarioRazonSocial;
    private String intermediarioNombre;
    private long intermediarioIdentificacionTributaria;
	private String NumPerIdenTri_C51;
	private String NumInterIdenTri_C52;



    /* Operacion */
    public long getOperacion() {
		return operacion;
	}

	public void setOperacion(long operacion) {
		this.operacion = operacion;
	}


    /* TipoRenta */
    public long getTipoRenta() {
		return tipoRenta;
	}

	public void setTipoRenta(long tipoRenta) {
		this.tipoRenta = tipoRenta;
	}


    /* RentaBruta */
    public Double getRentaBruta() {
		return rentaBruta;
	}

	public void setRentaBruta(Double rentaBruta) {
		this.rentaBruta = rentaBruta;
	}


    /* TasaDeRetencion */
    public Double getTasaDeRetencion() {
		return tasaDeRetencion;
	}

	public void setTasaDeRetencion(Double tasaDeRetencion) {
		this.tasaDeRetencion = tasaDeRetencion;
	}


    /* Impuesto */
    public Double getImpuesto() {
		return impuesto;
	}

	public void setImpuesto(Double impuesto) {
		this.impuesto = impuesto;
	}


    /* RentaLiquida */
    public Double getRentaLiquida() {
		return rentaLiquida;
	}

	public void setRentaLiquida(Double rentaLiquida) {
		this.rentaLiquida = rentaLiquida;
	}


    /* CreditoIDPC */
    public Double getCreditoIDPC() {
		return creditoIDPC;
	}

	public void setCreditoIDPC(Double creditoIDPC) {
		this.creditoIDPC = creditoIDPC;
	}


    /* OtrosCreditos */
    public Double getOtrosCreditos() {
		return otrosCreditos;
	}

	public void setOtrosCreditos(Double otrosCreditos) {
		this.otrosCreditos = otrosCreditos;
	}


    /* F50Codigo */
    public long getF50Codigo() {
		return f50Codigo;
	}

	public void setF50Codigo(long f50Codigo) {
		this.f50Codigo = f50Codigo;
	}


    /* F50Folio */
    public long getF50Folio() {
		return f50Folio;
	}

	public void setF50Folio(long f50Folio) {
		this.f50Folio = f50Folio;
	}


    /* FechaPago */
    public Date getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(Date fechaPago) {
		this.fechaPago = fechaPago;
	}


    /* MonedaPago */
    public String getMonedaPago() {
		return monedaPago;
	}

	public void setMonedaPago(String monedaPago) {
		this.monedaPago = monedaPago;
	}


    /* TipoCambioPago */
    public double getTipoCambioPago() {
		return tipoCambioPago;
	}

	public void setTipoCambioPago(double tipoCambioPago) {
		this.tipoCambioPago = tipoCambioPago;
	}


    /* PagadorTipoJuridico */
    public long getPagadorTipoJuridico() {
		return pagadorTipoJuridico;
	}

	public void setPagadorTipoJuridico(long pagadorTipoJuridico) {
		this.pagadorTipoJuridico = pagadorTipoJuridico;
	}


    /* PagadorRazonSocial */
    public String getPagadorRazonSocial() {
		return pagadorRazonSocial;
	}

	public void setPagadorRazonSocial(String pagadorRazonSocial) {
		this.pagadorRazonSocial = pagadorRazonSocial;
	}


    /* PagadorNombre */
    public String getPagadorNombre() {
		return pagadorNombre;
	}

	public void setPagadorNombre(String pagadorNombre) {
		this.pagadorNombre = pagadorNombre;
	}


    /* PagadorRut */
    public String getPagadorRut() {
		return pagadorRut;
	}

	public void setPagadorRut(String pagadorRut) {
		this.pagadorRut = pagadorRut;
	}


    /* PagadorDomicilio */
    public String getPagadorDomicilio() {
		return pagadorDomicilio;
	}

	public void setPagadorDomicilio(String pagadorDomicilio) {
		this.pagadorDomicilio = pagadorDomicilio;
	}


    /* PagadorCiudad */
    public String getPagadorCiudad() {
		return pagadorCiudad;
	}

	public void setPagadorCiudad(String pagadorCiudad) {
		this.pagadorCiudad = pagadorCiudad;
	}

    
    /* PagadorRelacion */
    public long getPagadorRelacion() {
		return pagadorRelacion;
	}

	public void setPagadorRelacion(long pagadorRelacion) {
		this.pagadorRelacion = pagadorRelacion;
	}


    /* PagadorHolding */
    public String getPagadorHolding() {
		return pagadorHolding;
	}

	public void setPagadorHolding(String pagadorHolding) {
		this.pagadorHolding = pagadorHolding;
	}


    /* PerceptorTipoJuridico */
    public long getPerceptorTipoJuridico() {
		return perceptorTipoJuridico;
	}

	public void setPerceptorTipoJuridico(long perceptorTipoJuridico) {
		this.perceptorTipoJuridico = perceptorTipoJuridico;
	}


    /* PerceptorPaisResidencia */
    public String getPerceptorPaisResidencia() {
		return perceptorPaisResidencia;
	}

	public void setPerceptorPaisResidencia(String perceptorPaisResidencia) {
		this.perceptorPaisResidencia = perceptorPaisResidencia;
	}


    /* PerceptorRazonSocial */
    public String getPerceptorRazonSocial() {
		return perceptorRazonSocial;
	}

	public void setPerceptorRazonSocial(String perceptorRazonSocial) {
		this.perceptorRazonSocial = perceptorRazonSocial;
	}


    /* PerceptorNombre */
    public String getPerceptorNombre() {
		return perceptorNombre;
	}

	public void setPerceptorNombre(String perceptorNombre) {
		this.perceptorNombre = perceptorNombre;
	}


    /* PerceptorIdentificacionTributaria */
    public long getPerceptorIdentificacionTributaria() {
		return perceptorIdentificacionTributaria;
	}

	public void setPerceptorIdentificacionTributaria(long perceptorIdentificacionTributaria) {
		this.perceptorIdentificacionTributaria = perceptorIdentificacionTributaria;
	}


    /* PerceptorRut */
    public String getPerceptorRut() {
		return perceptorRut;
	}

	public void setPerceptorRut(String perceptorRut) {
		this.perceptorRut = perceptorRut;
	}


    /* PerceptorFechaNacimiento */
    public Date getPerceptorFechaNacimiento() {
		return perceptorFechaNacimiento;
	}

	public void setPerceptorFechaNacimiento(Date perceptorFechaNacimiento) {
		this.perceptorFechaNacimiento = perceptorFechaNacimiento;
	}


    /* PerceptorTipoDomicilio */
    public long getPerceptorTipoDomicilio() {
		return perceptorTipoDomicilio;
	}

	public void setPerceptorTipoDomicilio(long perceptorTipoDomicilio) {
		this.perceptorTipoDomicilio = perceptorTipoDomicilio;
	}


    /* PerceptorDomicilio */
    public String getPerceptorDomicilio() {
		return perceptorDomicilio;
	}

	public void setPerceptorDomicilio(String perceptorDomicilio) {
		this.perceptorDomicilio = perceptorDomicilio;
	}


    /* PerceptorCiudad */
    public String getPerceptorCiudad() {
		return perceptorCiudad;
	}

	public void setPerceptorCiudad(String perceptorCiudad) {
		this.perceptorCiudad = perceptorCiudad;
	}


    /* PerceptorPais */
    public String getPerceptorPais() {
		return perceptorPais;
	}

	public void setPerceptorPais(String perceptorPais) {
		this.perceptorPais = perceptorPais;
	}


    /* IntermediarioTipoJuridico */
    public long getIntermediarioTipoJuridico() {
		return intermediarioTipoJuridico;
	}

	public void setIntermediarioTipoJuridico(long intermediarioTipoJuridico) {
		this.intermediarioTipoJuridico = intermediarioTipoJuridico;
	}


    /* IntermediarioPaisResidencia */
    public String getIntermediarioPaisResidencia() {
		return intermediarioPaisResidencia;
	}

	public void setIntermediarioPaisResidencia(String intermediarioPaisResidencia) {
		this.intermediarioPaisResidencia = intermediarioPaisResidencia;
	}


    /* IntermediarioRazonSocial */
    public String getIntermediarioRazonSocial() {
		return intermediarioRazonSocial;
	}

	public void setIntermediarioRazonSocial(String intermediarioRazonSocial) {
		this.intermediarioRazonSocial = intermediarioRazonSocial;
	}


    /* IntermediarioNombre */
    public String getIntermediarioNombre() {
		return intermediarioNombre;
	}

	public void setIntermediarioNombre(String intermediarioNombre) {
		this.intermediarioNombre = intermediarioNombre;
	}


    /* IntermediarioIdentificacionTributaria */
    public long getIntermediarioIdentificacionTributaria() {
		return intermediarioIdentificacionTributaria;
	}

	public void setIntermediarioIdentificacionTributaria(long intermediarioIdentificacionTributaria) {
		this.intermediarioIdentificacionTributaria = intermediarioIdentificacionTributaria;
	}

	/* NumPerIdenTri_C51 */

	 public String getNumPerIdenTri_C51() {
		return NumPerIdenTri_C51;
	}

	public void setNumPerIdenTri_C51(String NumPerIdenTri_C51) {
		this.NumPerIdenTri_C51 = NumPerIdenTri_C51;
	}

	/* NumInterIdenTri_C52 */

	 public String getNumInterIdenTri_C52() {
		return NumInterIdenTri_C52;
	}

	public void setNumInterIdenTri_C52(String NumInterIdenTri_C52) {
		this.NumInterIdenTri_C52 = NumInterIdenTri_C52;
	}


    //Obtener valor del registro
    public RegistroDJ1946(final Registro registro) {
    	
        this.operacion = (Long) registro.get("Operacion");
        this.tipoRenta = (Long) registro.get("TipoRenta");
        this.rentaBruta = (double) ((Number) registro.get("RentaBruta")).doubleValue();
        this.tasaDeRetencion = (double) ((Number) registro.get("TasaDeRetencion")).doubleValue();
        this.impuesto = (double) ((Number) registro.get("Impuesto")).doubleValue();
        this.rentaLiquida = (double) ((Number) registro.get("RentaLiquida")).doubleValue();
        this.creditoIDPC = (double) ((Number) registro.get("CreditoIDPC")).doubleValue();
        this.otrosCreditos = (double) ((Number) registro.get("OtrosCreditos")).doubleValue();
        this.f50Codigo = (Long) registro.get("F50Codigo");
        this.f50Folio = (Long) registro.get("F50Folio");
        this.fechaPago = (Date) registro.get("FechaPago");
        this.monedaPago = (String) registro.get("MonedaPago");
        this.tipoCambioPago = (double) ((Number) registro.get("TipoCambioPago")).doubleValue();
        this.pagadorTipoJuridico = (Long) registro.get("PagadorTipoJuridico");
        this.pagadorRazonSocial = (String) registro.get("PagadorRazonSocial");
        this.pagadorNombre = (String) registro.get("PagadorNombre");
        this.pagadorRut = (String) registro.get("PagadorRut");
        this.pagadorDomicilio = (String) registro.get("PagadorDomicilio");
        this.pagadorCiudad = (String) registro.get("PagadorCiudad");
        this.pagadorRelacion = (Long) registro.get("PagadorRelacion");
        this.pagadorHolding = (String) registro.get("PagadorHolding");
        this.perceptorTipoJuridico = (Long) registro.get("PerceptorTipoJuridico");
        this.perceptorPaisResidencia = (String) registro.get("PerceptorPaisResidencia");
        this.perceptorRazonSocial = (String) registro.get("PerceptorRazonSocial");
        this.perceptorNombre = (String) registro.get("PerceptorNombre");
        this.perceptorIdentificacionTributaria = (Long) registro.get("PerceptorIdentificacionTributaria");
        this.perceptorRut = (String) registro.get("PerceptorRut");
        this.perceptorFechaNacimiento = (Date) registro.get("PerceptorFechaNacimiento");
        this.perceptorTipoDomicilio = (Long) registro.get("PerceptorTipoDomicilio");
        this.perceptorDomicilio = (String) registro.get("PerceptorDomicilio");
        this.perceptorCiudad = (String) registro.get("PerceptorCiudad");
        this.perceptorPais = (String) registro.get("PerceptorPais");
        this.intermediarioTipoJuridico = (Long) registro.get("IntermediarioTipoJuridico");
        this.intermediarioPaisResidencia = (String) registro.get("IntermediarioPaisResidencia");
        this.intermediarioRazonSocial = (String) registro.get("IntermediarioRazonSocial");
        this.intermediarioNombre = (String) registro.get("IntermediarioNombre");
        this.intermediarioIdentificacionTributaria = (Long) registro.get("IntermediarioIdentificacionTributaria");
		this.NumPerIdenTri_C51 = (String) registro.get("NumPerIdenTri_C51");
		this.NumInterIdenTri_C52 = (String) registro.get("NumInterIdenTri_C52");
  	
    }

    public RegistroDJ1946() {
    }

    public void acumular(final RegistroDJ1946 registroDJ1946) {
        //this.sumaRentaBruta += registroDJ1946.getSumaRentaBruta();
        //this.sumaRentaBruta += Math.round(registroDJ1946.getRentaBruta());
    }
}
