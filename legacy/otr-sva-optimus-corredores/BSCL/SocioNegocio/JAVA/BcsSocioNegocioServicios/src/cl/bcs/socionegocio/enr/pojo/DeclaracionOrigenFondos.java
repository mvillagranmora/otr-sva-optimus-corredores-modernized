package cl.bcs.socionegocio.enr.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DeclaracionOrigenFondos extends BcsPojo {
	public Long IdInterno;
	public Long IdDeclaracionOrigenFondos;
	public Long IdEnrolamiento;
	public String IdTipoOperacionOrigenFondos;
	public java.sql.Date FechaDeclaracion;
	public String Comentario;
	public Long IdUsuarioCreador;
	public java.sql.Timestamp FechaIngreso;
	public Long IdEmpresa;
	public String OperacionInscripcion;
	public List<DetalleOrigenFondo> DetalleOrigenFondo;
	
	public DeclaracionOrigenFondos() {
		DetalleOrigenFondo = new ArrayList<>();
	}
}
