package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ResultadoCertificacion {
	
	public Long IdSolicitudAporteRescate;
	public Long IdSolicitudAporteRescateRelacionado;
	public String IdSerieFondo;
	public String Codigo;
	public String Estado;
	public String NombreEstado;
	public String DescripcionCodigo;
	public String DocumentoIdentidad;
	public String CodigoPortafolio;
	
	 public ResultadoCertificacion(){
	 	this.DocumentoIdentidad = "";
    	this.CodigoPortafolio = "";
    	this.IdSolicitudAporteRescateRelacionado = 0L;
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
