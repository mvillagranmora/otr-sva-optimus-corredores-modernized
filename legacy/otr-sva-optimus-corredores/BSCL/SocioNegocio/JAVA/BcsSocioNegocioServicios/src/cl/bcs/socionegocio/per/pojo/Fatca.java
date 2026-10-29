package cl.bcs.socionegocio.per.pojo;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

public class Fatca extends BcsPojo {
	public Long IdInterno;
	public Long IdPersona;
	public String IdEstado;
	public String NombreCompleto;
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
	public String Borrado;
	public Long IdUsuarioCreador;
	public Long IdUsuarioModificador;
	public Long IdCarga;
	public String DeclaracionCRS;
	
	public Fatca FatcaPorDefecto() {
		Fatca fatca = new Fatca();
		fatca.IdInterno = 0L;
		fatca.IdClasificacionFatca = "PENDIENTE";
		fatca.NumeroGIIN = "";
		fatca.FechaFirmaFormulario = Fecha.toTimeStamp("0001-01-01 00:00:00");
		fatca.AutorizacionInformarOrganismo = "N";
		fatca.ExcepcionDomicilio = "N";
		fatca.ExcepcionTelefono = "N";
		fatca.ExcepcionOrdenPermanente = "N";
		fatca.ExcepcionFirma = "N";
		fatca.ParticipacionSociedadesUSA = "N";
		fatca.OrdenPermanente = "N";
		fatca.NACEEUU = "";
		fatca.NACEEUUExplicacion = "";
		fatca.EntidadFinanciera = "";
		fatca.TipoInstitucionFinanciera = "";
		fatca.EntidadGestionada = "";
		fatca.IngresosPasivos = "";
		fatca.GenerarIngresosPasivos = "";
		fatca.CategoriaEntidad = "";
		fatca.GIINEntidad = "";
		fatca.RazonSocial = "";
		fatca.FechaLiquidacion = Fecha.toDate("0001-01-01");
		fatca.NombreMercado = "";
		fatca.FechaConstitucion = Fecha.toDate("0001-01-01");
		fatca.ObligacionesFiscales = "";
		fatca.ObligacionesFiscalesExplicacion = "";
		fatca.IndividuoControl = "";
		fatca.DeclaracionCRS = "N";
		
		return fatca;
	}
	
}

