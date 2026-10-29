package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.utils.Utils;

public class MovimientoCustodiaFondoMutuo {
	
	public Long IdGenerarMovimientoCustodia;
	public Date FechaOperacion;
	public Long IdCliente;
	public Long IdPortafolio;
	public String IdTipoProducto;
	public String IdConcepto;
	public String IdTipoOperacion;
	public Long IdOperacion;
	public String IdTipoComprobante;
	public Long FolioComprobante;
	public Date FechaComprobante;
	public String IdBolsa;
	public Long IdAgente;
	public Date FechaLiquidacion;
	public Long IdUsuario;
	public String TipoCarteraNormativa;
	public Date FechaCurse;
	public String TipoFactorAjuste;
	public BigDecimal ValorFactorAjusteaFechaCurse;
	public BigDecimal CostoEnFactorAjuste;
	public Long IdInstrumento;
	public String IdEmisor;
	public String IdMonedaInstrumento;
	public String IdBoveda;
	public BigDecimal Cantidad;
	public BigDecimal ValorRescate;
	public BigDecimal PrecioCompra;
	public BigDecimal MontoCompra;
	public BigDecimal MontoCompraMonedaInstrumento;
	public String IdTipoCanalIngreso;
	public String IdRegimenTributario;
	public Long IdCustodia;
	public Date FechaOrigenCompra;
	public String toString() {
		return Utils.printJson(this, false);
	}
}
