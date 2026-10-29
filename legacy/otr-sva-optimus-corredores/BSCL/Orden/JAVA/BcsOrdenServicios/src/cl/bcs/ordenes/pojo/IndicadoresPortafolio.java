package cl.bcs.ordenes.pojo;

public class IndicadoresPortafolio {
	public Long IdPortafolio;
	public String IdLibroOrdenes;
	public String IdLibroOrdenesAgente;
	public String IdLibroOrdenesRetail;
	public String IdDireccionNotificacion;
	public String OperaCustodiaRV;
	public String SobregiroRV;
	public String OperaCustodiaRFIF;
	public String SobregiroRF;
	public String SobregiroIF;
	public String PermiteVenderNominalRV;
	public String PermiteVenderNominalRFIF;
	public String OperaCuentaInversion;
	public String OperaLineaSobregiro;
	public String MecanismoCaptacion;
	public String CodigoIfrs;
	public String PermiteVentaDescubierta;
	public String ModificaTipoLiquidacion;
	public String PermiteRutear;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
