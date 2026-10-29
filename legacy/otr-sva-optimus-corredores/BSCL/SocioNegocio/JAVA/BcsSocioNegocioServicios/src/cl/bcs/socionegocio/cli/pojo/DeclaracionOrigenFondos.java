package cl.bcs.socionegocio.cli.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DeclaracionOrigenFondos extends BcsPojo{
	public Long IdDeclaracionOrigenFondos;
	public Long IdCliente;
	public String IdTipoOperacionOrigenFondos;
	public java.sql.Timestamp FechaDeclaracion;
	public String Comentario;
	public Long IdUsuarioCreador;
	public java.sql.Timestamp FechaIngreso;
	public Long IdEmpresa;
	public String IdEstado;
	public String IdEstadoAutorizacion;
	public String OperacionInscripcion;
	public List<DetalleOrigenFondo> DetalleOrigenFondo;
	
	public DeclaracionOrigenFondos() {
		DetalleOrigenFondo = new ArrayList<>();
	}
}
