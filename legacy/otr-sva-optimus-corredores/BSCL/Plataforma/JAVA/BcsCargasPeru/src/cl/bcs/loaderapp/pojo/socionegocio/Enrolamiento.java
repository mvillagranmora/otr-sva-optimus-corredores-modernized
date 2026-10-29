package cl.bcs.loaderapp.pojo.socionegocio;

public class Enrolamiento {
	public Long IdPersona;
	public String IdEstado;
	
	public String Tipo;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String RazonSocial;
	public Long IdPaisNacimiento;
	public Long IdPaisNacionalidadPrincipal;
	public Long IdPaisResidencia;
	public String FechaNacimiento;
	public String Sexo;
	public String EstadoCivil;
	
	public String FechaIngreso;
	
	public String EsEnrolado;
	public Long IdUsuarioCreador;
	public Long IdCiudad;
	public Long IdSocioNegocio;
	public Long IdEmpresa;
	public String FechaHoraSistema;
	public Long Distrito;
	
	@Override
	public String toString() {
		return String.format(
				"EnrolamientoPersona [IdPersona=%s, IdEstado=%s, Tipo=%s, Nombres=%s, ApellidoPaterno=%s, ApellidoMaterno=%s, RazonSocial=%s, IdPaisNacimiento=%s, IdPaisNacionalidadPrincipal=%s, IdPaisResidencia=%s, FechaNacimiento=%s, Sexo=%s, EstadoCivil=%s, FechaIngreso=%s, EsEnrolado=%s, IdUsuarioCreador=%s, IdCiudad=%s]",
				IdPersona, IdEstado, Tipo, Nombres, ApellidoPaterno, ApellidoMaterno, RazonSocial, IdPaisNacimiento,
				IdPaisNacionalidadPrincipal, IdPaisResidencia, FechaNacimiento, Sexo, EstadoCivil, FechaIngreso,
				EsEnrolado, IdUsuarioCreador, IdCiudad);
	}
}
