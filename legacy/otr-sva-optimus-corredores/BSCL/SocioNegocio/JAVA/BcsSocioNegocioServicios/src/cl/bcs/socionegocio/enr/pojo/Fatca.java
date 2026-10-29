package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

public class Fatca extends BcsPojo {
	public Long IdInterno;
	public Long IdEnrolamiento;
	public String IdClasificacionFatca;
	public String IdClasificacionEmpresaFatca;
	public String NumeroGIIN;
	public java.sql.Timestamp FechaFirmaFormulario;
	public String AutorizacionInformarOrganismo;
	public String ExcepcionResidente;
	public String ExcepcionNacimiento;
	public String ExcepcionDomicilio;
	public String ExcepcionTelefono;
	public String ExcepcionOrdenPermanente;
	public String ExcepcionFirma;
	public String ParticipacionSociedadesUSA;
	public String OrdenPermanente;
	public String NACEEUU;
	public String NACEEUUExplicacion;
	public String EntidadFinanciera;
	public String TipoInstitucionFinanciera;
	public String EntidadGestionada;
	public String IngresosPasivos;
	public String GenerarIngresosPasivos;
	public String CategoriaEntidad;
	public String GIINEntidad;
	public String RazonSocial;
	public java.sql.Date FechaLiquidacion;
	public String NombreMercado;
	public java.sql.Date FechaConstitucion;
	public String ObligacionesFiscales;
	public String ObligacionesFiscalesExplicacion;
	public String IndividuoControl;

	public Fatca(){
		this.IdInterno = 0L;
		this.IdClasificacionFatca = "PENDIENTE";
		this.NumeroGIIN = "";
		this.FechaFirmaFormulario = Fecha.toTimeStamp("0001-01-01 00:00:00");
		this.AutorizacionInformarOrganismo = "N";
		this.ExcepcionDomicilio = "N";
		this.ExcepcionTelefono = "N";
		this.ExcepcionOrdenPermanente = "N";
		this.ExcepcionFirma = "N";
		this.ParticipacionSociedadesUSA = "N";
		this.OrdenPermanente = "N";
		this.NACEEUU = "";
		this.NACEEUUExplicacion = "";
		this.EntidadFinanciera = "";
		this.TipoInstitucionFinanciera = "";
		this.EntidadGestionada = "";
		this.IngresosPasivos = "";
		this.GenerarIngresosPasivos = "";
		this.CategoriaEntidad = "";
		this.GIINEntidad = "";
		this.RazonSocial = "";
		this.FechaLiquidacion = Fecha.toDate("0001-01-01");
		this.NombreMercado = "";
		this.FechaConstitucion = Fecha.toDate("0001-01-01");;
		this.ObligacionesFiscales = "";
		this.ObligacionesFiscalesExplicacion = "";
		this.IndividuoControl = "";		
	}
}
