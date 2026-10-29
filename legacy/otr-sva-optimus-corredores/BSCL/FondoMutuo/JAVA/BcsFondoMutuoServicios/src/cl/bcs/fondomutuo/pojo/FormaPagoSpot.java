package cl.bcs.fondomutuo.pojo;

import java.math.BigDecimal;
import java.sql.Date;

import cl.bcs.utils.Utils;

public class FormaPagoSpot {
	
	
	public Long IdInternoPagoOperacion;
	public Long IdComprobantePagoOperacion;
	public String IdTipoMovimiento;
	public String IdFormaDePago;
	public String IdMoneda;
	public String IdMonedaLiquidacion;
	public Long IdOperacion;
	public String RecibePagaPortafolio;
	public BigDecimal MontoFormaDePago;
	public String AfectarTesoreria;
	public Date FechaLiquidacion;
	
    
    public FormaPagoSpot(){
    	this.IdInternoPagoOperacion = 0L;
    	this.IdComprobantePagoOperacion = 0L;
    	this.IdTipoMovimiento = "";
    	this.IdFormaDePago = "";
    	this.IdMoneda = "";
    	this.IdMonedaLiquidacion = "";
    	this.IdOperacion = 0L;
    	this.RecibePagaPortafolio = "";
    	this.MontoFormaDePago = BigDecimal.ZERO;
    	this.AfectarTesoreria = "";

    }
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
