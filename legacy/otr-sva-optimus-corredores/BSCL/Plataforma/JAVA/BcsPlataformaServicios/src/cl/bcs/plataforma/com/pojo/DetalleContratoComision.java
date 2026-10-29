package cl.bcs.plataforma.com.pojo;

import java.math.BigDecimal;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DetalleContratoComision extends BcsPojo{
	
	public Long IdInterno;
	public Long IdDetalleContratoComision;
	public Long IdContratoComision;
	public Long IdCobroClasificacion;
	public String IdCobro;
	public String EsPorRangos;
	public String IdConcepto;
	public BigDecimal MontoMinimo; 
	public BigDecimal MontoMaximo; 
	public List<SubdetalleContratoComision> SubdetallesContratoComision;

}
