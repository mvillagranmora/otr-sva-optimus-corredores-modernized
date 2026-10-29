package cl.bcs.loaderapp.pojo.posiciones;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class Posicion {
	public Long IdInterno;
	public String DocumentoIdentidad;
	public String Portafolio;
	public String CondicionLiquidacion;
	public String Boveda;
	public String CuentaCustodio;
	public String Concepto;
	public String Nemotecnico;
	public String Emisor;
	public String Reajuste;
	public String FechaVencimiento;
	public BigDecimal Cantidad;
	public BigDecimal Precio;
	public BigDecimal Tasa;
	public BigDecimal Monto;
	public BigDecimal ValorRescate;
	public String Glosa;
	public String NemoGarantizado;
	public String Moneda;
	public String Fungible;
	public String Bolsa;
	public Long CorrelativoReporte;
	public String TipoOperacion;
	public String ClasificacionInstrumento;
	public Long Plazo;
	public String FechaLiquidacion;
	public String FechaOperacion;
	public BigDecimal TipoCambio;
	public BigDecimal MontoContado;
	public BigDecimal MontoPlazo;
	public String EsNominal;
	public String ReferenciaReportado;
	public String Tipo;
	public String Clasificacion;
	public Long AsociadoCorelativo;
	
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
	
	
	
}
