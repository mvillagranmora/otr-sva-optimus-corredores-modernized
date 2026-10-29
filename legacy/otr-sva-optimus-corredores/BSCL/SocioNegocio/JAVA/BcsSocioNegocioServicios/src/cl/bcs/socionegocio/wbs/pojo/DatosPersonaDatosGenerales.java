package cl.bcs.socionegocio.wbs.pojo;

import java.sql.Date;

import cl.bcs.plataforma.frm.ValidarContenidoCampos;
import cl.bcs.utils.BcsPojo;

public class DatosPersonaDatosGenerales extends BcsPojo {
	public Long IdPersona;
	public Long IdInterno;
	public java.sql.Timestamp FechaIngreso;
	public String IdEstado;
	public String Tipo;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String NombreCompleto;
	public String DocumentoIdentidad;
	public String RazonSocial;
	public Long IdGiroPrincipal;
	public Long IdPaisNacimiento;
	public Long IdPaisResidencia;
	public Date FechaNacimiento;
	public Date FechaDefuncion;
	public java.sql.Timestamp FechaInicioActividad;
	@ValidarContenidoCampos(idCampo = "IdTipoGenero", tipoDato = String.class, idCampoMensaje = "IdGenero", prefijo = "")
	public String Sexo;
	public String EsExtranjero;
	public String EstadoCivil;
	public String RegimenConyugal;
	public String CodigoTipoCargoEmpleado;
	public String Cargo;
	public String DetalleCargo;
	public Empleador Empleador;
	public Long IdProfesion;
	public Long IdUsuarioCreador;
	public Long IdPersonaConyuge;
	public Long IdPersonaRepresentante;
	public Long IdPaisNacionalidadPrincipal;
	public Long IdNombreFantasiaPrincipal;
	public String GlobalLegalEntity;
	public Long IdPersonaEmpleador;
	public Long IdRangoPatrimonio;
	public String IdTransparenciaFiscal;
	public String ResidenciaTributariaExtranjera;
	public String IdGiroCIIU;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public Long IdCliente;
	public String EsBorrador;
	public String IdSituacionLaboral;
	@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoRangoRenta", prefijo = "RANGO_RENTA")
	public String CodigoRangoRenta;
	@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoRangoNivelVenta", prefijo = "RANGO_VENTA")
	public String CodigoRangoNivelVenta;
	@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoPropositoTransaccion", prefijo = "PROPOSITO")
	public String CodigoPropositoTransaccion;
	
	public DatosPersonaDatosGenerales () {
		this.Empleador = new Empleador();
	}
	
}
