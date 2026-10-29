package cl.bcs.plataforma.evt.pojo;

import cl.bcs.utils.Utils;

public class Mensaje {
	public Long 		IdInterno;
	public Long 		IdMensaje;
	public String		Titulo;
	public String 		IdTipoMensaje;
	public String 		IdTipoEntidadDestinatario;
	public String 		MensajeCorto;
	public String 		MensajeLargo;
	public String 		Habilitado;
	public String 		MuestraMensajeCorto;
	public String		UrlAviso;
	public String		IdTipoCanal;
	public java.sql.Date 	FechaVigenciaDesde;
	public java.sql.Date 	FechaVigenciaHasta;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
	
}
