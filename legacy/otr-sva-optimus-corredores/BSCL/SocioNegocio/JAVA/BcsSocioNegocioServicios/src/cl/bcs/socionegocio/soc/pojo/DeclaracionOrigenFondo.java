package cl.bcs.socionegocio.soc.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DeclaracionOrigenFondo extends BcsPojo{
	
	public String					Accion						= "";
	public Long						IdDeclaracionOrigenFondos	= 0L;
    public String					IdTipoOperacionOrigenFondos	= "";
    public String					FechaDeclaracion			= "";
    public String					Comentario					= "";
    public String					OperacionInscripcion		= "";
    public String					IdEstado					= "";
    public List<DetalleOrigenFondo>	DetalleOrigenFondo;
    
    public DeclaracionOrigenFondo()
	{
		DetalleOrigenFondo = new ArrayList<DetalleOrigenFondo>();
	}

}
