package cl.bcs.socionegocio.wbs.pojo;

import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DatosClienteDatosGenerales extends BcsPojo {
	public Long IdPersona;
	public String IdCategoriaInversionista;
	public String CodigoEjecutivoExterno;
	public String IdPerfilComercial;
	public String IdTipoCliente;
	public Long IdSegmentoCliente;
	public Long IdEmpleadoEjecutivo;
	public String AreaCliente;
	public Long IdSucursalPreferencia;
	public String ClienteFinal;
	public String Tipo;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public Long IdCliente;
	public List<DeclaracionOrigenFondo> DeclaracionOrigenFondo;
	
	public Long CLI_IdSegmentoCliente;
	public Long CLI_IdEmpleadoEjecutivo;
	public String EsBorrador;
}
