package cl.bcs.socionegocio.wbs.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

public class DeclaracionOrigenFondo extends BcsPojo {
	public String Accion;
	public Long IdCliente;
	public Long IdPersona;
	public Long IdDeclaracionOrigenFondos;
	public String IdTipoOperacionOrigenFondos;
	public java.sql.Date FechaDeclaracion;
	public String Comentario;
	public String OperacionInscripcion;
	public String IdEstado;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public String Tipo;
	public String EsBorrador;
	public List<DetalleOrigenFondo> DetalleOrigenFondo;

	public DeclaracionOrigenFondo() {
		this.Accion = "";
		this.IdCliente = 0L;
		this.IdPersona = 0L;
		this.IdDeclaracionOrigenFondos = 0L;
		this.IdTipoOperacionOrigenFondos = "";
		this.FechaDeclaracion = Fecha.toDate("0001-01-01");
		this.Comentario = "";
		this.OperacionInscripcion = "";
		this.IdEstado = "";
		this.DetalleOrigenFondo = new ArrayList<>();
	}
}
