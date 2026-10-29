package cl.bcs.APV.pojo;

import cl.bcs.utils.BcsPojo;

public class DatosRecaudaciones extends BcsPojo{
	public Long IdRecaudacionCliente;
	public String RutCliente;
	public Long MontoRecaudacion;
	public String TipoRegimen;
	public String Regimen;
	public String RutEmpleador;
	public Long IdContratoSAAPV;
	public Long MontoPesos;
	public Long FolioRecaudacion;
	public String FolioContratoSAAPV;
	public String CodigoMedioRecaudacion;
	
	public DatosRecaudaciones() {
		this.IdRecaudacionCliente = 0L;
		this.RutCliente = "";
		this.TipoRegimen = "";
		this.Regimen = "";
		this.MontoRecaudacion = 0L;
		this.RutEmpleador = "";
		this.IdContratoSAAPV = 0L;
		this.MontoPesos = 0L;
		this.FolioRecaudacion=0L;
		this.FolioContratoSAAPV="";
		this.CodigoMedioRecaudacion = "";
	}
}
