package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.BcsPojo;

public class Enrolamiento extends BcsPojo{
	public Long IdEnrolamiento;
	public Long IdEmpresa;
	public Long IdUsuario;
	public String IdTipoEntidad;
	public Long IdEntidadEnrolada;
	public String IdTipoDocumentoIdentidad;
	public String IdTipoDocumentoIdentidadAsociado;
	public String DocumentoIdentidad;
	public String IdTipoDocumentoIdentidadAdicional;
	public String DocumentoIdentidadAdicional;
	public String Tipo;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String RazonSocial;
	public Long IdPaisNacimiento;
	public Long IdPaisResidencia;
	public java.sql.Date FechaNacimiento;
	public String Sexo;
	public String EstadoCivil;
	public Long IdCiudad;
	public Long IdDivisionPais;
	public String IdTipoTelefono;
	public String Telefono;
	public String Email;
	public String DireccionCalle;
	public String DireccionNumero;
	public String DireccionDepartamento;
	public java.sql.Timestamp FechaCreacion;
	public java.sql.Timestamp FechaModificacion;
	public Long IdPersonaConyuge;
	public String RegimenConyugal;
	public String CLI_IdCategoriaInversionista;
	public Long CLI_IdTipoClienteCMF;
	public String CLI_IdTipoCliente;
	public Long CLI_IdEmpleadoEjecutivo;
	public String CLI_CodigoAdicionalHolding;
	public String CLI_IdClaseCliente;
	public String POR_IdPerfilComercial;
	public String EMP_CodigoEmpleado;
	public String EMP_CodigoTipoCargoEmpleado;
	public Long EMP_IdEmpleadoSupervisor;
	public Long EMP_IdSucursal;
	public java.sql.Date EMP_FechaInicioCargo;
	public java.sql.Date EMP_FechaTerminoCargo;
	public String CNT_CodigoInscripcion;
	public String CNT_Emisor;
	public String CNT_Depositos;
	public String CNT_Bolsa;
	public String CNT_Corredora;
	public String CNT_CamaraDeCompensacion;
	public String CNT_Banco;
	public String CNT_Boveda;
	public String CNT_Otros;
	public Long PRO_IdAreaProveedor;
	public String EMP_CargoIndefinido;
	public String TipoEnrolamiento;
	public String CLI_AreaCliente;
	public String RespondioEncuestaInversionista;
	public String CodigoPropositoTransaccion;
	
	public Enrolamiento() {
		this.IdPersonaConyuge = 0L;
	}
}
