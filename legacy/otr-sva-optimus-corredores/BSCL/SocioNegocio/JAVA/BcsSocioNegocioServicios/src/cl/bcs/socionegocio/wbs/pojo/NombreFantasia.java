package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class NombreFantasia extends BcsPojo {
	public Long IdInterno;
	public Long IdNombreFantasia;
	public String NombreFantasia;
	public String DescripcionNombreFantasia;
	public String FechaIngreso;
	public String IdEstado;
	public String IdEstadoAutorizacion;
	public Long IdPersona;
	public Long IdCliente;
	public String TipoEnrolamiento;
	public String TipoPersona;
	public String CodigoFormulario;
	public String Accion;
	public String EsBorrador;

	
	public NombreFantasia() {
	    super();
	    Accion = "";
	    NombreFantasia= "";
	    IdEstado = "";
	    IdPersona = 0L;
	    CodigoFormulario = "";
	    IdCliente = 0L;    
	}
}
