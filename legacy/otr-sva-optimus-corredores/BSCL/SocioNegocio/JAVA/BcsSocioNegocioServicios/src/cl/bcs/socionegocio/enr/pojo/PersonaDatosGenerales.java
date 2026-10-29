package cl.bcs.socionegocio.enr.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;

public class PersonaDatosGenerales extends BcsPojo {
	public Long IdInterno;
	public Long IdEnrolamiento;
	public String Nombres;
	public String ApellidoPaterno;
	public String ApellidoMaterno;
	public String RazonSocial;
	public Long IdGiroPrincipal;
	public Long IdPaisNacimiento;
	public Long IdPaisResidencia;
	public java.sql.Date FechaNacimiento;
	public String Sexo;
	public String EstadoCivil;
	public String IdTransparenciaFiscal;
	public String RegimenConyugal;
	public Long IdProfesion;
	public java.sql.Timestamp FechaInicioActividad;
	public Long IdPersonaConyuge;
	public Long IdPersonaRepresentante;
	public Long IdPaisNacionalidadPrincipal;
	public String GlobalLegalEntity;
	public Long IdPersonaEmpleador;
	public String CodigoTipoCargoEmpleado;
	public String DetalleCargo;
	public Long IdRangoPatrimonio;
	public Long IdGradoInstruccion;
	public String AgnosServicio;
	public BigDecimal IngresoPromedioMensual;
	public Long IdGiroCIIU;
	public Long IdPaisResidenciaSecundaria;
	public Long IdPaisNacionalidadSecundaria;
	public String EsExtranjero;
	public String ResidenciaTributariaExtranjera;
	public Long IdNombreFantasiaPrincipal;
	public String NombreFantasia;
	public String FechaDefuncion;
	public transient String EsBorrador;
	public String IdSituacionLaboral;
	public String IdTipoCanalIngresoCorredor;
	public String CodigoRangoRenta;
	public String CodigoRangoNivelVenta;

	public PersonaDatosGenerales() {
		EsBorrador = "S";
		IdSituacionLaboral = "";
		IdTipoCanalIngresoCorredor= "";
	}
}