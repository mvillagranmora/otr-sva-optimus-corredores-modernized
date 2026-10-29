package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class TipoDocumento extends BcsPojo {
	public String Accion;
	public Long IdDocumentoIdentidad;
	public String TipoDocumento;
	public String NumeroDocumento;
	public String NumeroSerie;
	public String FechaVencimiento;
	public String FechaEmision;
	public String IdEstado;
	public String Predeterminado;
	public String IdTipoDocumentoIdentidadAsociado;
	public String TipoPersona;
	public String DocumentoIdentidadAsociado;
	public Long IdUsuarioCreador;

	
	public Long IdPersona;
	public String TipoEnrolamiento;
	public String Tipo;
	public String CodigoFormulario;
	public Long IdCliente;
	public String EsBorrador;
	
	public TipoDocumento() {
	    super();
	    Accion = "";
	    IdDocumentoIdentidad = 0L;
	    NumeroSerie = "";
	    FechaVencimiento = "";
	    FechaEmision = "";
	    IdEstado = "";
	    IdPersona = 0L;
	    CodigoFormulario = "";
	    IdCliente = 0L;
	    Predeterminado = "N";
	    IdTipoDocumentoIdentidadAsociado = "";
	    TipoPersona="";
	    DocumentoIdentidadAsociado="";
	    IdUsuarioCreador=0L;
	    
	}
	
	
}
