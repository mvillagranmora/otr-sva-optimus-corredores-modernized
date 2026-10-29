package cl.bcs.facturacion.pojo;

import cl.bcs.utils.Utils;
import java.math.BigDecimal;

public class SalidaCobrosSimulacion {
	
	public Long IdInterno; 
	public Long IdInternoCobro;
	public Long IdPortafolio; 
	public String IdBolsa;
	public String IdTipoProducto;
	public String IdTipoOperacion;
	public String IdTipoCanalIngreso;
	public String IdCobro;
	public BigDecimal ValorCobro; 
	public String FormaCobro; 
	public BigDecimal ValorTopeMinimoOrden; 
	public String IdMoneda; 
	public String CodigoCobroClasificacion;
	public String PermiteCambiarValor; 
	
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	}
}
