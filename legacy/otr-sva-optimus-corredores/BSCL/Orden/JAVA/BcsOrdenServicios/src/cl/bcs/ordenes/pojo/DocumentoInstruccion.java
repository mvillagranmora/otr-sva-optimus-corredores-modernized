package cl.bcs.ordenes.pojo;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

public class DocumentoInstruccion extends BcsPojo {
	public Long IdDocumentoInstruccion;
	public Long IdInstruccion;
	public String NombreDocumentoAdjunto;
	public String Descripcion;
	public String Adjunto;
	public java.sql.Timestamp FechaActualizacion; 
	public String Borrado;
	
	public DocumentoInstruccion () {
		this.IdDocumentoInstruccion = 0L;
		this.IdInstruccion = 0L;
		this.NombreDocumentoAdjunto = "";
		this.Descripcion = "";
		this.FechaActualizacion = Fecha.toTimeStamp("0001-01-01 00:00:00");
		this.Borrado = "N";
		
	}	
}
