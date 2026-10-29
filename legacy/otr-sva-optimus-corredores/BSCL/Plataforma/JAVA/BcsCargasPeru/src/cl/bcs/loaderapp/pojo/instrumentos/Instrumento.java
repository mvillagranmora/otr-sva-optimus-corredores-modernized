package cl.bcs.loaderapp.pojo.instrumentos;

import cl.bcs.utils.Utils;

public class Instrumento {
	
	private String Nemotecnico;
	private String ISIN;
	private String CorteMinimo;
	private String EstaInscrito;
	private String EsDivisible;
	private String RazonSocial;
	private String Lista;
	private String FechaEmision;
	private String FechaVencimiento;
	private String IdInstrumentoRelacionado;
	private String IdEmisor;
	private String Reajustable;
	private String IdMoneda;
	private String IdTipoInstrumento;
	private String IdEstado;
	private String DecimalesPrecio;
	private String Tamano;
	private String FechaInicioVigencia;
	private String IndArt104;
	private String TasaFiscal;
	private String IdEntidadClasificadoraRiesgo;
	private String ValorClasificacionRiesgo;
	private String TipoValorizacion;
	private String ValorizaPor;
	private String Precio;
	private String Tasa;
	private String TieneCupon;
	private String NumeroCuotasCirculacionMesActual;
	private String NumeroAccionesCirculacion;
	private String IndicadorLiquidaDCV;
	private String IdPaisEmisor;
	private String FrecuenciaCupon;
	private String Serie;
	private String PrecioReferencia;
	private String IndicadorBolson;
	private String FechaReferencia;
	private String NumeroCupones;
	private String CodigoRubro;

	public String FechaDevengo;
	public String PeriodoPagoCupon;
	public String TipoCalculoInteresesDevengado;
	public String TipoCalculoInteresesCupon;
	public String IndicadorCuponeraVariable;
	public String IndicadorValorizarendiaHabil;
	public String ConversionDias;
	public String AjustefindeMes;
	public String IndicadorMercadoLocal;
	public String TipodeReajuste;
	public String CUSIP;
	
	private int Linea;
	
	public void setLinea(int linea) {
		Linea = linea;
	}
	public int getLinea() {
		return Linea;
	}
	
	public String getNemotecnico() {
		return Nemotecnico;
	}
	public void setNemotecnico(String nemotecnico) {
		Nemotecnico = nemotecnico;
	}
	public String getISIN() {
		return ISIN;
	}
	public void setISIN(String iSIN) {
		ISIN = iSIN;
	}
	public String getCorteMinimo() {
		return CorteMinimo;
	}
	public void setCorteMinimo(String corteMinimo) {
		CorteMinimo = corteMinimo;
	}
	public String getEstaInscrito() {
		return EstaInscrito;
	}
	public void setEstaInscrito(String estaInscrito) {
		EstaInscrito = estaInscrito;
	}
	public String getEsDivisible() {
		return EsDivisible;
	}
	public void setEsDivisible(String esDivisible) {
		EsDivisible = esDivisible;
	}
	public String getRazonSocial() {
		return RazonSocial;
	}
	public void setRazonSocial(String razonSocial) {
		RazonSocial = razonSocial;
	}
	public String getLista() {
		return Lista;
	}
	public void setLista(String lista) {
		Lista = lista;
	}
	public String getFechaEmision() {
		return FechaEmision;
	}
	public void setFechaEmision(String fechaEmision) {
		FechaEmision = fechaEmision;
	}
	public String getFechaVencimiento() {
		return FechaVencimiento;
	}
	public void setFechaVencimiento(String fechaVencimiento) {
		FechaVencimiento = fechaVencimiento;
	}
	public String getIdInstrumentoRelacionado() {
		return IdInstrumentoRelacionado;
	}
	public void setIdInstrumentoRelacionado(String idInstrumentoRelacionado) {
		IdInstrumentoRelacionado = idInstrumentoRelacionado;
	}
	public String getIdEmisor() {
		return IdEmisor;
	}
	public void setIdEmisor(String idEmisor) {
		IdEmisor = idEmisor;
	}
	public String getReajustable() {
		return Reajustable;
	}
	public void setReajustable(String reajustable) {
		Reajustable = reajustable;
	}
	public String getIdMoneda() {
		return IdMoneda;
	}
	public void setIdMoneda(String idMoneda) {
		IdMoneda = idMoneda;
	}
	public String getIdTipoInstrumento() {
		return IdTipoInstrumento;
	}
	public void setIdTipoInstrumento(String idTipoInstrumento) {
		IdTipoInstrumento = idTipoInstrumento;
	}
	public String getIdEstado() {
		return IdEstado;
	}
	public void setIdEstado(String idEstado) {
		IdEstado = idEstado;
	}
	public String getDecimalesPrecio() {
		return DecimalesPrecio;
	}
	public void setDecimalesPrecio(String decimalesPrecio) {
		DecimalesPrecio = decimalesPrecio;
	}
	public String getTamano() {
		return Tamano;
	}
	public void setTamano(String tamano) {
		Tamano = tamano;
	}
	public String getFechaInicioVigencia() {
		return FechaInicioVigencia;
	}
	public void setFechaInicioVigencia(String fechaInicioVigencia) {
		FechaInicioVigencia = fechaInicioVigencia;
	}
	public String getIndArt104() {
		return IndArt104;
	}
	public void setIndArt104(String indArt104) {
		IndArt104 = indArt104;
	}
	public String getTasaFiscal() {
		return TasaFiscal;
	}
	public void setTasaFiscal(String tasaFiscal) {
		TasaFiscal = tasaFiscal;
	}
	public String getIdEntidadClasificadoraRiesgo() {
		return IdEntidadClasificadoraRiesgo;
	}
	public void setIdEntidadClasificadoraRiesgo(String idEntidadClasificadoraRiesgo) {
		IdEntidadClasificadoraRiesgo = idEntidadClasificadoraRiesgo;
	}
	public String getValorClasificacionRiesgo() {
		return ValorClasificacionRiesgo;
	}
	public void setValorClasificacionRiesgo(String valorClasificacionRiesgo) {
		ValorClasificacionRiesgo = valorClasificacionRiesgo;
	}
	public String getTipoValorizacion() {
		return TipoValorizacion;
	}
	public void setTipoValorizacion(String tipoValorizacion) {
		TipoValorizacion = tipoValorizacion;
	}
	public String getValorizaPor() {
		return ValorizaPor;
	}
	public void setValorizaPor(String valorizaPor) {
		ValorizaPor = valorizaPor;
	}
	public String getPrecio() {
		return Precio;
	}
	public void setPrecio(String precio) {
		Precio = precio;
	}
	public String getTasa() {
		return Tasa;
	}
	public void setTasa(String tasa) {
		Tasa = tasa;
	}
	public String getTieneCupon() {
		return TieneCupon;
	}
	public void setTieneCupon(String tieneCupon) {
		TieneCupon = tieneCupon;
	}
	public String getNumeroCuotasCirculacionMesActual() {
		return NumeroCuotasCirculacionMesActual;
	}
	public void setNumeroCuotasCirculacionMesActual(String numeroCuotasCirculacionMesActual) {
		NumeroCuotasCirculacionMesActual = numeroCuotasCirculacionMesActual;
	}
	public String getNumeroAccionesCirculacion() {
		return NumeroAccionesCirculacion;
	}
	public void setNumeroAccionesCirculacion(String numeroAccionesCirculacion) {
		NumeroAccionesCirculacion = numeroAccionesCirculacion;
	}
	public String getIndicadorLiquidaDCV() {
		return IndicadorLiquidaDCV;
	}
	public void setIndicadorLiquidaDCV(String indicadorLiquidaDCV) {
		IndicadorLiquidaDCV = indicadorLiquidaDCV;
	}
	public String getIdPaisEmisor() {
		return IdPaisEmisor;
	}
	public void setIdPaisEmisor(String idPaisEmisor) {
		IdPaisEmisor = idPaisEmisor;
	}
	public String getFrecuenciaCupon() {
		return FrecuenciaCupon;
	}
	public void setFrecuenciaCupon(String frecuenciaCupon) {
		FrecuenciaCupon = frecuenciaCupon;
	}
	public String getSerie() {
		return Serie;
	}
	public void setSerie(String serie) {
		Serie = serie;
	}
	public String getPrecioReferencia() {
		return PrecioReferencia;
	}
	public void setPrecioReferencia(String precioReferencia) {
		PrecioReferencia = precioReferencia;
	}
	public String getIndicadorBolson() {
		return IndicadorBolson;
	}
	public void setIndicadorBolson(String indicadorBolson) {
		IndicadorBolson = indicadorBolson;
	}
	public String getFechaReferencia() {
		return FechaReferencia;
	}
	public void setFechaReferencia(String fechaReferencia) {
		FechaReferencia = fechaReferencia;
	}
	public String getNumeroCupones() {
		return NumeroCupones;
	}
	public void setNumeroCupones(String numeroCupones) {
		NumeroCupones = numeroCupones;
	}
	public String getCodigoRubro() {
		return CodigoRubro;
	}
	public void setCodigoRubro(String codigoRubro) {
		CodigoRubro = codigoRubro;
	}
	
	
	public String getFechaDevengo() {
		return FechaDevengo;
	}
	public void setFechaDevengo(String fechaDevengo) {
		FechaDevengo = fechaDevengo;
	}
	public String getPeriodoPagoCupon() {
		return PeriodoPagoCupon;
	}
	public void setPeriodoPagoCupon(String periodoPagoCupon) {
		PeriodoPagoCupon = periodoPagoCupon;
	}
	public String getTipoCalculoInteresesDevengado() {
		return TipoCalculoInteresesDevengado;
	}
	public void setTipoCalculoInteresesDevengado(String tipoCalculoInteresesDevengado) {
		TipoCalculoInteresesDevengado = tipoCalculoInteresesDevengado;
	}
	public String getTipoCalculoInteresesCupon() {
		return TipoCalculoInteresesCupon;
	}
	public void setTipoCalculoInteresesCupon(String tipoCalculoInteresesCupon) {
		TipoCalculoInteresesCupon = tipoCalculoInteresesCupon;
	}
	public String getIndicadorCuponeraVariable() {
		return IndicadorCuponeraVariable;
	}
	public void setIndicadorCuponeraVariable(String indicadorCuponeraVariable) {
		IndicadorCuponeraVariable = indicadorCuponeraVariable;
	}
	public String getIndicadorValorizarendiaHabil() {
		return IndicadorValorizarendiaHabil;
	}
	public void setIndicadorValorizarendiaHabil(String indicadorValorizarendiaHabil) {
		IndicadorValorizarendiaHabil = indicadorValorizarendiaHabil;
	}
	public String getConversionDias() {
		return ConversionDias;
	}
	public void setConversionDias(String conversionDias) {
		ConversionDias = conversionDias;
	}
	public String getAjustefindeMes() {
		return AjustefindeMes;
	}
	public void setAjustefindeMes(String ajustefindeMes) {
		AjustefindeMes = ajustefindeMes;
	}
	public String getIndicadorMercadoLocal() {
		return IndicadorMercadoLocal;
	}
	public void setIndicadorMercadoLocal(String indicadorMercadoLocal) {
		IndicadorMercadoLocal = indicadorMercadoLocal;
	}
	public String getTipodeReajuste() {
		return TipodeReajuste;
	}
	public void setTipodeReajuste(String tipodeReajuste) {
		TipodeReajuste = tipodeReajuste;
	}
	public String getCUSIP() {
		return CUSIP;
	}
	public void setCUSIP(String cUSIP) {
		CUSIP = cUSIP;
	}
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
