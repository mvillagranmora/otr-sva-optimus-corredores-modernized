package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;

public class ConfiguracionCanalDistribucion {
	
	public Long IdEmpresa;
	public String IdTipoCanalDistribucion;
	public String IdTipoCanalIngreso;
	public String IdTipoInstrumento;
	public String IdBolsa;
	public String GeneraMovimientoFacturacionAutomatico;
	public String GeneraComprobanteFacturacionAutomatico;
	public BigDecimal MontoMaximoDistribucionOrden;
	
	
	@Override
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,false);		
	}
	
}
