package cl.bcs.plataforma.mer.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;

public class DeterioroEmisor extends BcsPojo{
	public Long IdDeterioroEmisor;
	public String Emisor;
	public String FlagDeterioro;
	public BigDecimal PorcentajeDeterioro;
	public Integer Fase;
	public String Accion;
	public String IdEstado;
	
	public DeterioroEmisor() {
		this.IdDeterioroEmisor = 0L;
		this.Emisor = "";
		this.FlagDeterioro = "";
		this.PorcentajeDeterioro = BigDecimal.ZERO;
		this.Fase = 0;
		this.Accion = "";
		this.IdEstado = "ACT";
	}
	
	public String getEmisor() {
		return Emisor;
	}
}
