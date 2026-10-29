package cl.bcs.socionegocio.wbs.pojo;



import cl.bcs.utils.BcsPojo;

public class Relacion extends BcsPojo {
    public String Accion;
    public Long IdRelacion;
	public Long IdPersonaRelacionada;
	public String IdTipoRelacion;
	public String TipoDocumentoIdentidad;
	public String DocumentoIdentidad; 
	public String Nombre;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public Long IdPersona;
	public String Tipo;
	public String IdEstado;
	public String CodigoFormulario;
	public Long IdCliente;
	public String TipoEnrolamiento;
	public String EsBorrador;
	
	public Relacion() {
	    super();
		IdEstado = "ACT";
	}	
}
