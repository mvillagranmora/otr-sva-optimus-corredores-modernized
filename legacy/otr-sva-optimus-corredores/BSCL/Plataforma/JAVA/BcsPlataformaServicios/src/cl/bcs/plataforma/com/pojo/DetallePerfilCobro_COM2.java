package cl.bcs.plataforma.com.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.Utils;

public class DetallePerfilCobro_COM2 {
	public Long IdEmpresa;
	public Long IdDetallePerfilCobros;
	public String IdPerfilCobros;
	public String IdTipoProducto;
	public String IdTipoOperacion;
	public String IdBolsa;
	public String IdTipoCanalIngreso;
	public String CodigoDefinicionCobro;
	public String CodigoDefinicionCobroBase;
	public String FechaCreacion;
	public String IdEstadoAutorizacion;
	public Long IdUsuarioCreador;
	public Long IdUsuarioBorra;
	public String Borrado;
	public String IdEstado;
	public BigDecimal MontoFijo_MontoCobro; 
	public BigDecimal PorcentualFijo_ValorCobro; 
	public BigDecimal PorcentualTasaPlazo_ValorCobro; 
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
