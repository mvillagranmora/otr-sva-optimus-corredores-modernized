package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class DocumentoIdentidad extends BcsPojo {
	public Long IdInterno;
	public Long IdDocumentoIdentidad;
	public Long IdEnrolamiento;
	public String DocumentoIdentidadAsociado;
	public String IdTipoDocumentoIdentidad;
	public String DocumentoIdentidad;
	public String NumeroSerie;
	public String FechaEmision;
	public String FechaVencimiento;
	public String IdTipoDocumentoIdentidadAsociado;
	public String TipoPersona;
	public String Predeterminado;
	public Long IdDocumentoIdentidadPersona;
	
	public DocumentoIdentidad() {
		this.IdDocumentoIdentidad = 0L;
		this.DocumentoIdentidadAsociado = "";
		this.Predeterminado = "N";
		this.IdTipoDocumentoIdentidadAsociado = "";
		this.IdDocumentoIdentidadPersona = 0L;
	}
}
