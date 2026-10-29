package cl.bcs.normativa.registros;

import cl.bcs.normativa.data.Registro;

import java.math.BigDecimal;
import java.util.Date;
public class RegistroDJ1829 {

	
	
	
    /**
     * Declaracion de variables para la DJ
     */
    
	private String rutContraparte;
    private String taxIDContraparte;
    private String codigoPaisContraparte;
    private String tipoRelacionContraparte;
    private String modalidadContratacion;
    private String acuerdoMarcoTipo;
    private BigDecimal acuerdoMarcoNumero;
    private Date acuerdoMarcoFechaSuscripcion;
    private BigDecimal contratoNumero;
    private Date contratoFechaSuscripcion;
    private String contratoVencidoEjercicio;
    private String contratoEstado;
    private String contratoTipo;
    private String contratoNombreInstrumento;
    private String contratoModalidadCumplimiento;
    private String contratoPosicionDeclarante;
    private String activoSubyacenteTipo;    
    private String activoSubyacenteCodigo;
    private String activoSubyacenteOtros;
    private double activoSubyacenteTasaSpread;
    private String segundoActivoSubyacenteTipo;    
    private String segundoActivoSubyacenteCodigo;
    private String segundoActivoSubyacenteOtros;
    private double segundoActivoSubyacenteTasaSpread;
    private String precioFuturoCodigo;
    private BigDecimal precioFuturoPrecio;
    private String precioFuturoMoneda;
    private String unidad;
    private double montoContratado;
    private String segundaUnidad;
    private double segundoMontoNocional;
    private Date fechaVencimiento;
    private Date fechaLiquidacion;
    private String precioMercadoCodigo;
    private BigDecimal precioMercadoPrecio;
    private double valorJusto;
    private double resultadoEjercicio;
    private String cuentaContableResultadoEjercicio;
    private double efectoEnPatrimonio;
    private String cuentaContableRegistroPatrimonio;
    private double comisionPactada;
    private double primaTotal;
    private double inversionInicial;
    private double otrosGastosContrato;
    private double otrosIngresosContrato;
    private double pagosExteriorMonto;
    private String pagosExteriorModalidadPago;
    private double saldoGarantiasAlCierre;
    private double sumaResultadoEjercicio;
    private double sumaEfectoPatrimonio;
   
    public double getSaldoGarantiasAlCierre() {
		return saldoGarantiasAlCierre;
	}
	public void setSaldoGarantiasAlCierre(double saldoGarantiasAlCierre) {
		this.saldoGarantiasAlCierre = saldoGarantiasAlCierre;
	}
	//Obtener valor del registro
    public RegistroDJ1829(final Registro registro) {
    	
   	 this.rutContraparte = (String) registro.get("RutContraparte");
   	 this.taxIDContraparte = (String) registro.get("TaxIDContraparte");
   	 this.codigoPaisContraparte = (String) registro.get("CodigoPaisContraparte");
   	 this.tipoRelacionContraparte = (String) registro.get("TipoRelacionContraparte");
   	 this.modalidadContratacion = (String) registro.get("ModalidadContratacion");
   	 this.acuerdoMarcoTipo = (String) registro.get("AcuerdoMarcoTipo");
   	 this.acuerdoMarcoNumero = (BigDecimal) registro.get("AcuerdoMarcoNumero");
   	 this.acuerdoMarcoFechaSuscripcion = (Date) registro.get("AcuerdoMarcoFechaSuscripcion");
   	 this.contratoNumero = (BigDecimal) registro.get("ContratoNumero");
   	 this.contratoFechaSuscripcion = (Date) registro.get("ContratoFechaSuscripcion");
   	 this.contratoVencidoEjercicio = (String) registro.get("ContratoVencido");
   	 this.contratoEstado = (String) registro.get("ContratoEstadoConfirmacion");
   	 this.contratoTipo = (String) registro.get("ContratoTipo");
   	 this.contratoNombreInstrumento = (String) registro.get("ContratoNombreInstrumento");
   	 this.contratoModalidadCumplimiento = (String) registro.get("ContratoModalidadCumplimiento");
   	 this.contratoPosicionDeclarante = (String) registro.get("ContratoPosicionDeclarante");
   	 this.activoSubyacenteTipo = (String) registro.get("ActivoSubyacenteTipo");
   	 this.activoSubyacenteCodigo = (String) registro.get("ActivoSubyacenteCodigo");
   	 this.activoSubyacenteOtros = (String) registro.get("ActivoSubyacenteOtros");
   	 this.activoSubyacenteTasaSpread = (double) ((Number) registro.get("ActivoSubyacenteTasaFija")).doubleValue();
   	 this.segundoActivoSubyacenteTipo = (String) registro.get("SegundoActivoSubyacenteTipo"); 	
   	 this.segundoActivoSubyacenteCodigo = (String) registro.get("SegundoActivoSubyacenteCodigo");
   	 this.segundoActivoSubyacenteOtros = (String) registro.get("SegundoActivoSubyacenteOtros");
   	 this.segundoActivoSubyacenteTasaSpread = (double) ((Number) registro.get("SegundoActivoSubyacenteTasaFija")).doubleValue();
   	 this.precioFuturoCodigo = (String) registro.get("PrecioFuturoCodigoPrecio");
	 this.precioFuturoPrecio = (BigDecimal) registro.get("PrecioFuturoPrecio");
   	 this.precioFuturoMoneda = (String) registro.get("PrecioFuturoMoneda");
   	 this.unidad = (String) registro.get("Unidad");
   	 this.montoContratado = (double) ((Number) registro.get("MontoCantidadContratadoNocional")).doubleValue();
   	 this.segundaUnidad = (String) registro.get("SegundaUnidad");
   	 this.segundoMontoNocional = (double) ((Number) registro.get("SegundoMontoNocional")).doubleValue();
   	 this.fechaVencimiento = (Date) registro.get("FechaVencimiento");
   	 this.fechaLiquidacion = (Date) registro.get("FechaLiquidacionEjercicioOpcion");
   	 this.precioMercadoCodigo = (String) registro.get("PrecioMercadoCodigoPrecio");
   	 this.precioMercadoPrecio = (BigDecimal) registro.get("PrecioMercadoPrecio");
   	 this.valorJusto = (double) ((Number) registro.get("ValorJustoContratoConfirnacion")).doubleValue();
   	 this.resultadoEjercicio = (double) ((Number) registro.get("ResultadoEjercicio")).doubleValue();
   	 this.cuentaContableResultadoEjercicio = (String) registro.get("CuentaContableAsociadaResultadoEjercicio");
   	 this.efectoEnPatrimonio = (double) ((Number) registro.get("EfectoPatrimonio")).doubleValue();
   	 this.cuentaContableRegistroPatrimonio = (String) registro.get("CuentaContableAsociadaEfectoPatrimonio");
   	 this.comisionPactada = (double) ((Number) registro.get("ComisionPactada")).doubleValue();   	 
     this.primaTotal = (double) ((Number) registro.get("PrimaTotal")).doubleValue();
     this.inversionInicial = (double) ((Number) registro.get("InversionInicial")).doubleValue();
     this.otrosGastosContrato = (double) ((Number) registro.get("OtrosGastosAsociadosContrato")).doubleValue();
     this.otrosIngresosContrato = (double) ((Number) registro.get("OtrosIngresosAsociadosContrato")).doubleValue();
     this.pagosExteriorMonto = (double) ((Number) registro.get("PagosMontoPagadosAdeudados")).doubleValue();
     this.pagosExteriorModalidadPago = (String) registro.get("PagosExteriorModalidadPago");
     this.sumaResultadoEjercicio = (double) ((Number) registro.get("ResultadoEjercicio")).doubleValue();
     this.sumaEfectoPatrimonio = (double) ((Number) registro.get("EfectoPatrimonio")).doubleValue();
     this.saldoGarantiasAlCierre = (double) ((Number) registro.get("SaldoGarantiasCierre")).doubleValue();
    }
    public String getRutContraparte() {
		return rutContraparte;
	}

	public void setRutContraparte(String rutContraparte) {
		this.rutContraparte = rutContraparte;
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

	public String getTipoRelacionContraparte() {
		return tipoRelacionContraparte;
	}

	public void setTipoRelacionContraparte(String tipoRelacionContraparte) {
		this.tipoRelacionContraparte = tipoRelacionContraparte;
	}

	public String getModalidadContratacion() {
		return modalidadContratacion;
	}

	public void setModalidadContratacion(String modalidadContratacion) {
		this.modalidadContratacion = modalidadContratacion;
	}

	public String getAcuerdoMarcoTipo() {
		return acuerdoMarcoTipo;
	}

	public void setAcuerdoMarcoTipo(String acuerdoMarcoTipo) {
		this.acuerdoMarcoTipo = acuerdoMarcoTipo;
	}

	public BigDecimal getAcuerdoMarcoNumero() {
		return acuerdoMarcoNumero;
	}

	public void setAcuerdoMarcoNumero(BigDecimal acuerdoMarcoNumero) {
		this.acuerdoMarcoNumero = acuerdoMarcoNumero;
	}

	public Date getAcuerdoMarcoFechaSuscripcion() {
		return acuerdoMarcoFechaSuscripcion;
	}

	public void setAcuerdoMarcoFechaSuscripcion(Date acuerdoMarcoFechaSuscripcion) {
		this.acuerdoMarcoFechaSuscripcion = acuerdoMarcoFechaSuscripcion;
	}

	public BigDecimal getContratoNumero() {
		return contratoNumero;
	}

	public void setContratoNumero(BigDecimal contratoNumero) {
		this.contratoNumero = contratoNumero;
	}

	public Date getContratoFechaSuscripcion() {
		return contratoFechaSuscripcion;
	}

	public void setContratoFechaSuscripcion(Date contratoFechaSuscripcion) {
		this.contratoFechaSuscripcion = contratoFechaSuscripcion;
	}

	public String getContratoVencidoEjercicio() {
		return contratoVencidoEjercicio;
	}

	public void setContratoVencidoEjercicio(String contratoVencidoEjercicio) {
		this.contratoVencidoEjercicio = contratoVencidoEjercicio;
	}

	public String getContratoEstado() {
		return contratoEstado;
	}

	public void setContratoEstado(String contratoEstado) {
		this.contratoEstado = contratoEstado;
	}

	public String getContratoTipo() {
		return contratoTipo;
	}

	public void setContratoTipo(String contratoTipo) {
		this.contratoTipo = contratoTipo;
	}

	public String getContratoNombreInstrumento() {
		return contratoNombreInstrumento;
	}

	public void setContratoNombreInstrumento(String contratoNombreInstrumento) {
		this.contratoNombreInstrumento = contratoNombreInstrumento;
	}

	public String getContratoModalidadCumplimiento() {
		return contratoModalidadCumplimiento;
	}

	public void setContratoModalidadCumplimiento(String contratoModalidadCumplimiento) {
		this.contratoModalidadCumplimiento = contratoModalidadCumplimiento;
	}

	public String getContratoPosicionDeclarante() {
		return contratoPosicionDeclarante;
	}

	public void setContratoPosicionDeclarante(String contratoPosicionDeclarante) {
		this.contratoPosicionDeclarante = contratoPosicionDeclarante;
	}

	public String getActivoSubyacenteTipo() {
		return activoSubyacenteTipo;
	}

	public void setActivoSubyacenteTipo(String activoSubyacenteTipo) {
		this.activoSubyacenteTipo = activoSubyacenteTipo;
	}

	public String getActivoSubyacenteCodigo() {
		return activoSubyacenteCodigo;
	}

	public void setActivoSubyacenteCodigo(String activoSubyacenteCodigo) {
		this.activoSubyacenteCodigo = activoSubyacenteCodigo;
	}

	public String getActivoSubyacenteOtros() {
		return activoSubyacenteOtros;
	}

	public void setActivoSubyacenteOtros(String activoSubyacenteOtros) {
		this.activoSubyacenteOtros = activoSubyacenteOtros;
	}

	public double getActivoSubyacenteTasaSpread() {
		return activoSubyacenteTasaSpread;
	}

	public void setActivoSubyacenteTasaSpread(double activoSubyacenteTasaSpread) {
		this.activoSubyacenteTasaSpread = activoSubyacenteTasaSpread;
	}

	public String getSegundoActivoSubyacenteTipo() {
		return segundoActivoSubyacenteTipo;
	}

	public void setSegundoActivoSubyacenteTipo(String segundoActivoSubyacenteTipo) {
		this.segundoActivoSubyacenteTipo = segundoActivoSubyacenteTipo;
	}

	public String getSegundoActivoSubyacenteCodigo() {
		return segundoActivoSubyacenteCodigo;
	}

	public void setSegundoActivoSubyacenteCodigo(String segundoActivoSubyacenteCodigo) {
		this.segundoActivoSubyacenteCodigo = segundoActivoSubyacenteCodigo;
	}

	public String getSegundoActivoSubyacenteOtros() {
		return segundoActivoSubyacenteOtros;
	}

	public void setSegundoActivoSubyacenteOtros(String segundoActivoSubyacenteOtros) {
		this.segundoActivoSubyacenteOtros = segundoActivoSubyacenteOtros;
	}

	public double getSegundoActivoSubyacenteTasaSpread() {
		return segundoActivoSubyacenteTasaSpread;
	}

	public void setSegundoActivoSubyacenteTasaSpread(double segundoActivoSubyacenteTasaSpread) {
		this.segundoActivoSubyacenteTasaSpread = segundoActivoSubyacenteTasaSpread;
	}

	public String getPrecioFuturoCodigo() {
		return precioFuturoCodigo;
	}

	public void setPrecioFuturoCodigo(String precioFuturoCodigo) {
		this.precioFuturoCodigo = precioFuturoCodigo;
	}

	public BigDecimal getPrecioFuturoPrecio() {
		return this.precioFuturoPrecio;
	}

	public void setPrecioFuturoPrecio(BigDecimal precioFuturoPrecio) {
		this.precioFuturoPrecio = precioFuturoPrecio;
	}

	public String getPrecioFuturoMoneda() {
		return precioFuturoMoneda;
	}

	public void setPrecioFuturoMoneda(String precioFuturoMoneda) {
		this.precioFuturoMoneda = precioFuturoMoneda;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public double getMontoContratado() {
		return montoContratado;
	}

	public void setMontoContratado(double montoContratado) {
		this.montoContratado = montoContratado;
	}

	public String getSegundaUnidad() {
		return segundaUnidad;
	}

	public void setSegundaUnidad(String segundaUnidad) {
		this.segundaUnidad = segundaUnidad;
	}

	public double getSegundoMontoNocional() {
		return segundoMontoNocional;
	}

	public void setSegundoMontoNocional(double segundoMontoNocional) {
		this.segundoMontoNocional = segundoMontoNocional;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Date getFechaLiquidacion() {
		return fechaLiquidacion;
	}

	public void setFechaLiquidacion(Date fechaLiquidacion) {
		this.fechaLiquidacion = fechaLiquidacion;
	}

	public String getPrecioMercadoCodigo() {
		return precioMercadoCodigo;
	}

	public void setPrecioMercadoCodigo(String precioMercadoCodigo) {
		this.precioMercadoCodigo = precioMercadoCodigo;
	}

	public BigDecimal getPrecioMercadoPrecio() {
		return this.precioMercadoPrecio;
	}

	public void setPrecioMercadoPrecio(BigDecimal precioMercadoPrecio) {
    	this.precioMercadoPrecio = precioMercadoPrecio;
	}

	public double getValorJusto() {
		return valorJusto;
	}

	public void setValorJusto(double valorJusto) {
		this.valorJusto = valorJusto;
	}

	public double getResultadoEjercicio() {
		return resultadoEjercicio;
	}

	public void setResultadoEjercicio(double resultadoEjercicio) {
		this.resultadoEjercicio = resultadoEjercicio;
	}

	public String getCuentaContableResultadoEjercicio() {
		return cuentaContableResultadoEjercicio;
	}

	public void setCuentaContableResultadoEjercicio(String cuentaContableResultadoEjercicio) {
		this.cuentaContableResultadoEjercicio = cuentaContableResultadoEjercicio;
	}

	public double getEfectoEnPatrimonio() {
		return efectoEnPatrimonio;
	}

	public void setEfectoEnPatrimonio(double efectoEnPatrimonio) {
		this.efectoEnPatrimonio = efectoEnPatrimonio;
	}

	public String getCuentaContableRegistroPatrimonio() {
		return cuentaContableRegistroPatrimonio;
	}

	public void setCuentaContableRegistroPatrimonio(String cuentaContableRegistroPatrimonio) {
		this.cuentaContableRegistroPatrimonio = cuentaContableRegistroPatrimonio;
	}

	public double getComisionPactada() {
		return comisionPactada;
	}

	public void setComisionPactada(double comisionPactada) {
		this.comisionPactada = comisionPactada;
	}

	public double getPrimaTotal() {
		return primaTotal;
	}

	public void setPrimaTotal(double primaTotal) {
		this.primaTotal = primaTotal;
	}

	public double getInversionInicial() {
		return inversionInicial;
	}

	public void setInversionInicial(double inversionInicial) {
		this.inversionInicial = inversionInicial;
	}

	public double getOtrosGastosContrato() {
		return otrosGastosContrato;
	}

	public void setOtrosGastosContrato(double otrosGastosContrato) {
		this.otrosGastosContrato = otrosGastosContrato;
	}

	public double getOtrosIngresosContrato() {
		return otrosIngresosContrato;
	}

	public void setOtrosIngresosContrato(double otrosIngresosContrato) {
		this.otrosIngresosContrato = otrosIngresosContrato;
	}

	public double getPagosExteriorMonto() {
		return pagosExteriorMonto;
	}

	public void setPagosExteriorMonto(double pagosExteriorMonto) {
		this.pagosExteriorMonto = pagosExteriorMonto;
	}

	public String getPagosExteriorModalidadPago() {
		return pagosExteriorModalidadPago;
	}

	public void setPagosExteriorModalidadPago(String pagosExteriorModalidadPago) {
		this.pagosExteriorModalidadPago = pagosExteriorModalidadPago;
	}

	public double getSumaResultadoEjercicio() {
		return sumaResultadoEjercicio;
	}

	public void setSumaResultadoEjercicio(double sumaResultadoEjercicio) {
		this.sumaResultadoEjercicio = sumaResultadoEjercicio;
	}

	public double getSumaEfectoPatrimonio() {
		return sumaEfectoPatrimonio;
	}

	public void setSumaEfectoPatrimonio(double sumaEfectoPatrimonio) {
		this.sumaEfectoPatrimonio = sumaEfectoPatrimonio;
	}

	public RegistroDJ1829() {
    }
    public void acumular(final RegistroDJ1829 registroDJ1829) {        
        this.sumaResultadoEjercicio += registroDJ1829.getResultadoEjercicio();
        this.sumaEfectoPatrimonio += registroDJ1829.getEfectoEnPatrimonio();
    }
	
}
