package cl.bcs.plataforma.lim.pojo;

import cl.bcs.utils.BcsPojo;

import java.math.BigDecimal;

public class Limite extends BcsPojo {
	public Long IdInterno;
	
	public Long IdLimite;
	public Long IdInstrumento;
	public Long IdEmpresa;
	public BigDecimal MontoTotal;
	public BigDecimal MontoIntraday;
	public BigDecimal MontoOvernight;
	public BigDecimal Porcentaje;
	public Integer Plazo;
	public String IdTipoLimite;
	public String IdCanalIngreso;
	public String IdTipoOperacion;
	public String UnidadPlazo;
	public String CodigoOperador;
	public String IdMoneda;
	public String IdTipoInstrumento;
	public String IdClasificacionTipoInstrumento;
	public String IdEmisor;
	public String IdTipoComportamiento;
	public String UsaPlazo;
	public String EsCuentaPropia;
	public String IdEntidadClasificadoraRiesgo;
	public String ValorClasificacionRiesgo;
	public BigDecimal MargenInferior;
	public BigDecimal MargenSuperior;
	public Long PrioridadEvaluacion;
}
