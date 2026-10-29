package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.APV.GrabarTraspasos.TraspasoModificar;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.per.CrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.EntradaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.SalidaCrearDireccionElectronica;

import cl.bcs.socionegocio.per.CrearTelefono;
import cl.bcs.socionegocio.per.CrearTelefono.EntradaCrearTelefono;
import cl.bcs.socionegocio.per.CrearTelefono.SalidaCrearTelefono;

import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GrabarContratoSAAPV implements JavaCall   {

	/*
	 * El log
	 */	
	//private final Log log = new Log("Afiliacion.GrabarContraroSAAPV");
	static final Logger logger = LogManager.getLogger(GrabarContratoSAAPV.class);
	
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_PERSONA 				= loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_Insert.sql");
	public String SQL_CREAR_PORTAFOLIO_APV_Call 	= loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql");
	public String SQL_CONSULTAR_CONTRATOSAAPV 		= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta.sql");
	public String SQL_CREAR_CONTRATOSAAPV 			= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Insert.sql");
	public String SQL_ELIMINAR_CONTRATOSAAPV 		= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Delete.sql");
	public String SQL_CREAR_DISTRIBUCION_INVERSION 	= loadResourceToString("cl/bcs/APV/sql/AgregarDistribucionInversion.sql");
	public String SQL_ELIMINAR_DISTRIBUCION_CONTRATOSAAPV = loadResourceToString("cl/bcs/APV/sql/EliminarDistribucionInversion.sql");	
	public String SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV	  = loadResourceToString("cl/bcs/APV/sql/Crear_PortafolioContratoSAAPV.sql");
	public String SQL_CONSULTAR_RELACION_PERSONA	 = loadResourceToString("cl/bcs/APV/sql/ConsultarRelacionPersona.sql"); //Valida la existencia de la relacion entre la persona y el empleador ingresados
	public String SQL_CREAR_RELACION_PERSONA	 = loadResourceToString("cl/bcs/APV/sql/CrearRelacionPersona.sql");
	public String SQL_CREAR_RELACIONADO_RRHH	 = loadResourceToString("cl/bcs/APV/sql/Crear_IndicadoresPersonaRRHH_Call.sql");
	public String SQL_CONSULTAR_RELACIONADO_RRHH = loadResourceToString("cl/bcs/APV/sql/ConsultaTipoIndicadorPersona.sql");	
	//public String SQL_CAMBIO_PROPOSITO_DIRECCION = loadResourceToString("cl/bcs/APV/sql/CambioPropositoDireccion.sql");
	public String SQL_CONSULTAR_DIRECCION_FISICA = loadResourceToString("cl/bcs/APV/sql/ConsultarDireccionFisica.sql");
	public String SQL_DIRECCION_PROPOSITO_APV 	 = loadResourceToString("cl/bcs/APV/sql/CreaDireccionPropositoAPV.sql");
	public String SQL_CAMBIAR_CARGO_PERSONA 	 = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaActividaCargo_Update.sql");
	public String SQL_CREAR_TRASPASO			 = loadResourceToString("cl/bcs/APV/sql/Traspaso_Insert.sql");
	public String SQL_CREAR_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/DetalleOrigenTraspaso_Insert.sql");
	public String SQL_MODIFICA_TRASPASO = loadResourceToString("cl/bcs/APV/sql/Traspaso_Update.sql");
	public String SQL_CONSULTA_CONTRATOSAAPV_CONDICIONES_DUPLICADAS = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta_Condiciones_Duplicadas.sql");
	public String SQL_CERRAR_CONTRATOSAAPV_CONDICIONES_DUPLICADAS 	= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Condiciones_Duplicadas_Cerrar.sql");
	public String SQL_CONSULTA_CONTRATOSAAPV_PORTAFOLIOS = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta_PortafoliosPorIdContratoSAAPV.sql");
	public String SQL_CONSULTA_MEDIOS_RECAUDACION_FORMA_PAGO = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta_MedioRecaudacionPAC.sql");
	public String SQL_CERRAR_DISTRIBUCION_CONTRATOSAAPV_CONDICIONES_DUPLICADAS 	= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Distribucion_Condiciones_Duplicadas_Cerrar.sql");
	private String SQL_CREAR_ENTRADA_TEMPORAL = "create local temporary table #PortafoliosIngresados (\"IdPortafolio\" BIGINT , \"Producto\" nvarchar(100))";
	private String SQL_INSERT_ENTRADA_TEMPORAL = "insert into #PortafoliosIngresados values(:IdPortafolio,:Producto);";
	private String SQL_ELIMINAR_ENTRADA_TEMPORAL = "DROP TABLE #PortafoliosIngresados;";
	private String SQL_SELECT_ESTADO_CLIENTE 		= loadResourceToString("cl/bcs/APV/sql/GrabarContratoSAAPV_SelectEstadoCliente.sql");
	

	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosContrato extends BcsPojo {
		
		public  Long		IdContratoSAAPV;
		public	Long		IdEmpresa;
		public  String		FolioContratoSAAPV;
		public  Long		IdPlan;
	    public 	String		NombrePlan;
	    public  String		FechaSuscripcion; 		//Date
	    public  String		FechaCreacion; 			//Date
	    public  String		FechaAprobacion; 
	    public  String		FechaNotificacion; 		//Date
	    public  String		FechaRecepcion;			//Date 
	    public  String		Folio;
	    public  Long		IdPersona; 
	    public  Long		IdEmpleador; 
	    public  Long		IdCliente;

	    public 	String		IdCategoriaInversionista;
	    public  String		IdPerfilComercial;
	    public  String		NombreCliente;
	    public 	String		ApellidoPaternoCliente; 
	    public 	String		ApellidoMaternoCliente; 
	    public  String		DocumentoIdentidadCliente;
	    public  String		FechaNacimientoCliente;		//Date
	    public 	String		EstadoCivil;
	    public  String		IdTipoGenero;
	    public	Long		IdPaisResidenciaCliente;
	    public	Long		IdCiudadCliente;
	    public  Long		IdComunaCliente;
	    public  String		DireccionCliente;
	    public  String		NumeroDireccionCliente;
	    public  Long		IdTipoCargoPersona;
		public 	String 		TelefonoFijo;
	    public  String		TelefonoMovil;
	    public	String		TelefonoLaboral;
	    public	String		EmailCliente; 
	    public	String		RazonSocialEmpleador;
	    public	Long		IdMedioRecaudacionTrabajador;
	    public	Long		IdEntidadRecaudadora;
	    public 	String		DocumentoIdentidadEmpleador;
	    public 	Long		IdPaisEmpleador; 
	    public	String		DireccionComercialEmpleador;
	    public  String		NumeroDireccionEmpleador;
	    public	Long		IdCiudadEmpleador;
	    public	Long		IdComunaEmpleador;
	    public	String		TelefonoEmpleador; 
	    public	String		NombreEncargado; 
	    public	String		EmailEncargado;
	    public	String		TelefonoEncargado;	    
	    public 	Long		CodigoPrevision;
	    public	String		Pensionado;
	    public	String		Trabajador;
	    public	String		FormaDePago; 
	    public	Long		IdMedioRecaudacionFormaPago;
	    public	Long		DiaDescuento;
	    public	String		Institucion;
	    public	double		ComisionTransferencia;
	    
	    public  String		CodigoTipoSolicitud;
	    public  String      TipoMovimiento;
	    public  String		RazonRetiro;
	    public	String		NombreEntidadOrigen;
	    public	String		RutEntidadOrigen;
	    public  Long        IdPersonaOrigen;
	    public  String 		Descripcion;
	    public	String		TipoDeAhorro;
	    public	String		FondoDeOrigen;
	    public	String		TipoTraspaso;
	    public	double		MontoTraspaso;
	    public	double		UFTraspaso;
	    public 	double		PorcentajeTraspaso;
	    
	    public  String		IdEstadoEnvioNotificacion;
	    public  String		IdEstadoSolicitud;
	    public 	String		IdEstado;
	    public  Long		IdCargaMasiva;
	    public 	Long		IdEmpleado;	
	    public  String      IdPropositoPortafolio;
	    public 	Long		IdMatrizAgrupadorAPV;
	    public  String      CodigoAgrupador;
	    public	String		Canal;
	    public  String		EsAporteCuentaInversion;
	    
	    public Long IdInterno;
	    public ParametrosContrato() {
	    	IdInterno = 0L;
	    	Canal = "OPT";
	    	EsAporteCuentaInversion = "N";
	    }
	 }
	
	public static class ContratoSAAPV extends BcsPojo {
		
		public  Long		IdContratoSAAPV;
		public	Long		IdEmpresa;
		public  String		FolioContratoSAAPV;
		public  Long		IdPlan;
	    public 	String		NombrePlan;
	    public  String		FechaSuscripcion; 		//Date
	    public  String		FechaCreacion; 			//Date
	    public  String		FechaAprobacion; 
	    public  String		FechaNotificacion; 		//Date
	    public  String		FechaRecepcion;			//Date 
	    public  String		Folio;
	    public  Long		IdPersona; 
	    public  Long		IdEmpleador; 
	    public  Long		IdCliente;
	    public  String		NombreCliente;
	    public 	String		ApellidoPaternoCliente; 
	    public 	String		ApellidoMaternoCliente; 
	    public  String		DocumentoIdentidadCliente;
	    public  String		FechaNacimientoCliente;		//Date
	    public 	String		EstadoCivil;
	    public  String		IdTipoGenero;
	    public	Long		IdPaisResidenciaCliente;
	    public	Long		IdCiudadCliente;
	    public  Long		IdComunaCliente;
	    public  String		DireccionCliente;
	    public  Long		IdTipoCargoPersona;
		public 	String 		TelefonoCliente;
	    public  String		TelefonoComercialCliente;
	    public	String		TelefonoCelularCliente;
	    public	String		EmailCliente; 
	    public	String		RazonSocialEmpleador;
	    public	Long		IdMedioRecaudacionTrabajador;
	    public	Long		IdEntidadRecaudadora;
	    public 	String		DocumentoIdentidadEmpleador;
	    public	String		DireccionComercialEmpleador; 
	    public	Long		IdComunaEmpleador;
	    public	String		TelefonoEmpleador; 
	    public	String		NombreEncargado; 
	    public	String		EmailEncargado;
	    public	String		TelefonoEncargado;	    
	    public 	Long		CodigoPrevision;
	    public	String		Pensionado;
	    public	String		Trabajador;
	    public	String		FormaDePago; 
	    public	Long		IdMedioRecaudacionFormaPago;
	    public	Long		DiaDescuento;
	    public	String		Institucion;
	    public	double		ComisionTransferencia;
	    public  String      CodigoTipoSolicitud;
	    public	String		NombreEntidadOrigen;
	    public	String		RutEntidadOrigen;
	    public	String		TipoDeAhorro;
	    public	String		FondoDeOrigen;
	    public	String		TipoTraspaso;
	    public	double		MontoTraspaso;
	    public	double		UFTraspaso;
	    public 	double		PorcentajeTraspaso;
	    public  String		IdEstadoEnvioNotificacion;
	    public  String		IdEstadoSolicitud;
	    public 	String		IdEstado;
	    public  String		IdCategoriaInversionista;
	    public	String		IdPerfilComercial;
	    public  Long		IdCargaMasiva;
	    public	String		Canal;
	    
	    public ContratoSAAPV(){
	    	this.Canal = "OPT";
	    }
	}
	
	public static class DistribucionInversion extends BcsPojo {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public Long IdInstrumento;
		public String Nemotecnico;
		public double MontoPesos;
		public double Porcentaje;
		public String IdEstado;
		
		public Long IdInterno;
	    public DistribucionInversion() {
	    	IdInterno = 0L;
	    }
	}

	public static class PortafoliosModificados extends BcsPojo {

		public String 	CodigoAgrupador;
		public Long 	IdMatrizAgrupador;
		public String 	OrigenAhorro;
		public Long 	IdPortafolio;
		public String	IdPropositoPortafolio;
		public String 	MesInicio;
		public String 	MesTermino;
		public double 	MontoPesos;
		public double 	MontoUF;
		public String	Pago;
		public String	PagoNombre;
		public double 	PorcentajeRegimenImponible;
		public String 	Producto;
		public String 	Regimen;
		
		public Long IdInterno;
	    public PortafoliosModificados() {
	    	IdInterno = 0L;
	    } 
	}
	
	public static class Traspaso extends BcsPojo {
		public Long IdTraspaso;
		public Long IdContratoSAAPV;
		public Long IdEmpresa;
		public String TipoMovimiento;
		public String RazonRetiro;
		public String FechaIngreso;
		public String FechaEgreso;
		public String FechaModificacion;
		public String CodigoTipoTraspaso;
		public String Descripcion;
		public Long IdCliente;
		public Long IdPersona;
		public String CodigoTipoAhorro;	
		public String NombreEntidad;
		public String DocumentoEntidad;
		public String IdEstado;
		public String Canal;
		public String CierreSolicitud;
		public String FechaSuscripcion;
	};
		
	public static class DetalleOrigenTraspaso extends BcsPojo {
		public Long   IdDetalleOrigenTraspaso;
		public Long   IdTraspaso;
		public String CodigoTipoAhorro;
		public Long IdEntidadRecaudadora;
		public String NombreEntidad;
		public String DocumentoEntidad;
		public String Fondo;
		public double MontoTraspaso;
		public double UFTraspaso;
		public double PorcentajeTraspaso;
		public String IdEstado;
		public Long IdInstrumento;
		public String CodigoRegimenTributario;
		public Long IdPortafolio;
		public Long IdInterno;
	    public DetalleOrigenTraspaso() {
	    	IdInterno = 0L;
	    }
	};
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio extends BcsPojo {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	String						p_Tipo; //Individual Manual o Por Servicio
		//$scope.$root.SesionIdPaisSistema
		public List<ParametrosContrato>		p_Parametros;
		public List<DistribucionInversion> 	p_DistribucionInversion;
		public List<PortafoliosModificados> p_PortafoliosModificados;
		public List<DetalleOrigenTraspaso> 	p_DetalleOrigenTraspaso;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<ParametrosContrato>();	
			p_DistribucionInversion = new ArrayList<DistribucionInversion>();	
			p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();
			p_DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
		}
	}	

	public static class AgrupadorPortafolio extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgrupador;
		public String NombreAgrupador;
		public String CodigoAgrupador;
	};
	
	public static class PortafolioContratoSAAPV extends BcsPojo {
		public String OrigenAhorro;
		public Long IdPortafolio;
		public Long IdContratoSAAPV;
		public double MontoPesos;
		public double MontoUF;
		public double PorcentajeRegimenImponible;
		public String Pago;
		public String PeriodoInicio;
		public String PeriodoTermino;
		public String IdEstado;
	};
	
	public static class ConsultaPortafolioDisponible extends BcsPojo {
		public  Long		IdPersona;
	    public 	String		Codigo;
	    public  Long		IdPortafolio;
	    public  Long		IdCliente;
	    public  String		NombreAgrupador;
	    public  Long		IdMatrizAgrupadorAPV;
	    public  String		CodigoAgrupador;
		
	};
	
	public static class IndicadorEncargadoRRHH extends BcsPojo {
		public Long 		IdIndicador;	
		public Long			IdTipoIndicador; 
		public String 		ValorIndicador;
		public Date			FechaVencimiento;
	};	
	

	public static class clasePruebaApv extends BcsPojo {
		public String 		mensajeAPV;
	};	
	
	public static class SecuenciaIndicador extends BcsPojo {
		public Long			IdSecuencia;
	}
	
	public static class TipoIndicador extends BcsPojo {
		public Long			IdTipoIndicador;
		public String		CodigoTipoIndicador;
	}
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio extends BcsPojo {
		public Long 				P_REF_ID_PORTAFOLIO;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado extends BcsPojo {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
	}	
	
	public static class ContratoSAAPVID extends BcsPojo {
		public  Long		IdContratoSAAPV;
		public String		Pago;
		public String		FormaDePago;
		public String		PeriodoInicio;
		public String		PeriodoTermino;
		public Long			IdEntidadRecaudadora;
		public Long 		IdEmpleador;
		public Long			IdMedioRecaudacionFormaPago;
		
		public ContratoSAAPVID(){
			FormaDePago = "";
			PeriodoInicio = "";
			PeriodoTermino = "";
			IdEntidadRecaudadora = 0L;
			IdEmpleador = 0L;
			IdMedioRecaudacionFormaPago = 0L;
		}
	}	

	public static class PortafolioACerrar extends BcsPojo {
		public  Long		IdPortafolio;
	    public 	String		Producto;
		
	}	
	
	public static class  ConsultaDireccion extends BcsPojo {
		public Long IdDireccionFisica;
		
	}

	public static class ConsultaRelacionPersona extends BcsPojo {
		public Long IdRelacionPersona; 
		
	}
	
	public static class CambiarCargoPersona extends BcsPojo {
		public  Long	IdPersona;
		public  Long	IdTipoCargoPersona;
	}

	public static class DistribucionesSAAPV extends BcsPojo {
		public Long IdContratoSAAPV;
		public String Nemotecnico;
		public BigDecimal MontoPesos;
		public BigDecimal Porcentaje;
		
		
		public DistribucionesSAAPV() {
			this.IdContratoSAAPV = 0L;
			this.Nemotecnico = "";
			this.MontoPesos = BigDecimal.ZERO;
			this.Porcentaje = BigDecimal.ZERO;
		}
	}
	
	public static class PortafoliosSAAPV extends BcsPojo {
		public Long IdContratoSAAPV;
		public Long IdPortafolio;
		public String Regimen;
		
		public PortafoliosSAAPV() {
			this.IdContratoSAAPV = 0L;
			this.IdPortafolio = 0L;
			this.Regimen = "";
		}
	}
	
	public static class MedioRecaudacion extends BcsPojo {
		public Long IdMedioRecaudacion;
		public String CodigoMedioRecaudacion;
		public String TipoMedioRecaudacion;
		
		public MedioRecaudacion() {
			this.IdMedioRecaudacion = 0L;
			this.CodigoMedioRecaudacion = "";
			this.TipoMedioRecaudacion = "";
		}
	}
	
	public static class EstadoCliente extends BcsPojo {
		public Long		IdCliente;
		public String	IdEstado;
		public String	DocumentoIdentidad;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Metodos Basicos	*	
	 ************************/
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		logger.info("Inicia GrabarContratoSAAPV");
		CrearPersona 					per 				= new CrearPersona ();
		CrearTelefono					fono				= new CrearTelefono();
		Enrolamiento 					enr 				= new Enrolamiento();
		EntradaServicioCrearPersona 	EntradaPersona 		= new EntradaServicioCrearPersona();
		SalidaServicioCrearPersona 		ObjOut 				= new SalidaServicioCrearPersona ();
		EntradaCrearTelefono 			EntradaTelefonoCom 	= new EntradaCrearTelefono();
		EntradaCrearTelefono 			EntradaTelefonoPar 	= new EntradaCrearTelefono();
		SalidaCrearTelefono 			ObjOutFonoPar		= new SalidaCrearTelefono ();
		SalidaCrearTelefono 			ObjOutFonoCom 		= new SalidaCrearTelefono ();
		CrearDireccionElectronica		email				= new CrearDireccionElectronica();
		EntradaCrearDireccionElectronica	EntradaMail     = new EntradaCrearDireccionElectronica();
		SalidaCrearDireccionElectronica		ObjOutMail		= new SalidaCrearDireccionElectronica();
		
		
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		CrearCliente				cli				= new CrearCliente();
		EntradaServicioCrearCliente EntradaCliente  = new EntradaServicioCrearCliente();
		SalidaServicioCrearCliente  SalidaCliente   = new SalidaServicioCrearCliente();
		 
		long IdPortafolioAPV = 0;
		long IdRelacionPersona = 0;
		
		SQL sql = new SQL();
		List<Long> idsContratoSAAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV", (long)in.p_Parametros.size());
		String ApruebaTraspasoAutomatico = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "APV","APV.ApruebaTraspasoAutomatico");
		int contadorIds = 0;
		boolean ExisteFolioContrato;
		
		List<String> listaDocumentoIdentidad = new ArrayList<>();
		List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
		HashMap<String,Object> hashCliente = new HashMap<>();
		if(in.p_Parametros.size() > 0) {
			listaDocumentoIdentidad = in.p_Parametros.stream()
					.map(parametro-> parametro.DocumentoIdentidadCliente != null ? parametro.DocumentoIdentidadCliente: "")
					.distinct()
					.collect(Collectors.toList());
			
			if(listaDocumentoIdentidad.size() > 0) {
				listaEstadoCliente = obtenerEstadoCliente(con, sql, listaDocumentoIdentidad);
				
				if(listaEstadoCliente.size() > 0) {
					hashCliente = Utils.List2Hash(listaEstadoCliente, "DocumentoIdentidad");
				}
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		for(GrabarContratoSAAPV.ParametrosContrato fila : in.p_Parametros){
			
			if(hashCliente.size() > 0) {
				EstadoCliente estadoCliente = (EstadoCliente) hashCliente.get(fila.DocumentoIdentidadCliente);
				
				if(estadoCliente != null) {
					if("TRA".equalsIgnoreCase(fila.CodigoTipoSolicitud) && "INGRESO".equalsIgnoreCase(fila.TipoMovimiento)) {
						if("BLO".equalsIgnoreCase(estadoCliente.IdEstado)) {
							String DocumentoIdentidad = estadoCliente.DocumentoIdentidad;
							String codMsg = "CLIENTE_BLOQUEADO";
							String msg= Diccionario.getMensaje(con,in.p_IdEmpresa,codMsg)
									  .add("DocumentoIdentidad", DocumentoIdentidad)
									  .msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
							continue;
						}
					}
				}
			}
			
			Long IdContratoSAAPVBorrar = fila.IdContratoSAAPV;
			
			fila.IdContratoSAAPV = idsContratoSAAPV.get(contadorIds);
			ExisteFolioContrato = false;

			String CLI_IdTipoClienteDefecto = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.ContratoCLI_IdTipoClienteDefecto");
			String FechaFinRelacionDefecto = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.ContratoFechaFinRelacionDefecto");
			//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
			
			Long time = System.currentTimeMillis();
			java.sql.Date FechaSistema = new java.sql.Date(time);
			
			Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
			
			String FechaOptimus	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
				
			//*** VALIDACION CONTRATOS SAAPV ***
			//valida si existe Persona, Cliente, Empleador y Portafolio
			
			ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada inValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
			ValidaContratoSAAPV.ValidaContratoSAAPV_Salida outValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Salida();
			List<ValidaContratoSAAPV.ContratosSAAPV> tablaValidaContrato = new ArrayList<ValidaContratoSAAPV.ContratosSAAPV>();
			
			//Este IF se puso para que se haga la validacion de contratos aunque no existan portafolios 
			if(in.p_PortafoliosModificados.size() > 0 ) {
				for(PortafoliosModificados filaValidaPortafolio : in.p_PortafoliosModificados){
					ValidaContratoSAAPV.ContratosSAAPV ContratoValidaContrato;
			
					//if(fila.OrigenAhorroCV.compareTo("APV") == 0){
					ContratoValidaContrato = new ValidaContratoSAAPV.ContratosSAAPV();
					ContratoValidaContrato.IdPersona = null;
					ContratoValidaContrato.IdCliente = null;
					ContratoValidaContrato.IdEmpleador = null;
					ContratoValidaContrato.IdPortafolio = null;
					ContratoValidaContrato.DocumentoIdentidad = fila.DocumentoIdentidadCliente;
					ContratoValidaContrato.DocumentoIdentidadEmpleador = fila.DocumentoIdentidadEmpleador;
					ContratoValidaContrato.FolioContratoSAAPV = fila.FolioContratoSAAPV;
					ContratoValidaContrato.Codigo = filaValidaPortafolio.Regimen;
					ContratoValidaContrato.NombrePlan = fila.NombrePlan;
					ContratoValidaContrato.CodigoAgrupador = filaValidaPortafolio.CodigoAgrupador;
					tablaValidaContrato.add(ContratoValidaContrato);	
			 	}	
			}else {
				ValidaContratoSAAPV.ContratosSAAPV ContratoValidaContrato = new ValidaContratoSAAPV.ContratosSAAPV();
				ContratoValidaContrato.IdPersona = null;
				ContratoValidaContrato.IdCliente = null;
				ContratoValidaContrato.IdEmpleador = null;
				ContratoValidaContrato.IdPortafolio = null;
				ContratoValidaContrato.DocumentoIdentidad = fila.DocumentoIdentidadCliente;
				ContratoValidaContrato.DocumentoIdentidadEmpleador = fila.DocumentoIdentidadEmpleador;
				ContratoValidaContrato.FolioContratoSAAPV = fila.FolioContratoSAAPV;
				ContratoValidaContrato.Codigo = "";
				ContratoValidaContrato.NombrePlan = fila.NombrePlan;
				ContratoValidaContrato.CodigoAgrupador = "";
				tablaValidaContrato.add(ContratoValidaContrato);
			}

			ValidaContratoSAAPV ValidaContratoSAAPVService = new ValidaContratoSAAPV();
			inValidaContrato.p_IdEmpresa = in.p_IdEmpresa;
			inValidaContrato.p_IdUsuario = in.p_IdUsuario;
			inValidaContrato.p_IdModulo = in.p_IdModulo;
			inValidaContrato.p_Opcion = "I";
			inValidaContrato.p_ContratosSAAPV = tablaValidaContrato;
			outValidaContrato = ValidaContratoSAAPVService.logica(con, inValidaContrato);
			
			if(outValidaContrato.P_OUT_ERRORESCONTADOR == null){// && outValidaContrato.P_OUT_ERRORESCONTADOR == 0 ){

				for (ValidaContratoSAAPV.ModificaContratoSAAPV filaValidaContrato : outValidaContrato.P_IDSCONTRATO) {
					if(filaValidaContrato.IdPersona != null)
						fila.IdPersona = filaValidaContrato.IdPersona;
					
					if(filaValidaContrato.IdCliente != null)
						fila.IdCliente = filaValidaContrato.IdCliente;
					
					if(filaValidaContrato.IdEmpleador != null)
						fila.IdEmpleador = filaValidaContrato.IdEmpleador;
					
					if(filaValidaContrato.Codigo != null){
					 	for(PortafoliosModificados filaSeteaPortafolio : in.p_PortafoliosModificados){
					 		if(filaValidaContrato.Codigo.compareTo(filaSeteaPortafolio.Regimen)==0 && filaValidaContrato.IdPortafolio != null){
					 			filaSeteaPortafolio.IdPortafolio = filaValidaContrato.IdPortafolio;
					 		}
					 	}
					}
					if(!filaValidaContrato.FolioContratoSAAPV.equals("")){
						ExisteFolioContrato = true;
					}	
				}
				logger.debug("ValidaContratoSAAPV OK");
			}else{
			    MensajeSalida msg = new MensajeSalida();
				msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_VALIDAR_PERSONA";
				msg.descripcion = "No es posible validar persona contrato.";
				salida.P_OUT_MENSAJES.add(msg);				
			}
			
			if(fila.IdPersona == null) fila.IdPersona = 0L;
			if(fila.IdCliente == null) fila.IdCliente = 0L;
			if(fila.IdEmpleador == null) fila.IdEmpleador = 0L;
			if(fila.DocumentoIdentidadEmpleador == null) fila.DocumentoIdentidadEmpleador = "";
			
			/*En caso de que el FolioContrato Exista en el Ingreso se debe avisar que ya existe y continuar con el ciclo en el Caso de la Carga Masiva*/
			if(ExisteFolioContrato) {
				MensajeSalida msg = new MensajeSalida();
				msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
				if(fila.IdCargaMasiva != null) {
					msg.tipo = fila.IdCargaMasiva== 0 ?"ERROR" :"ADVERTENCIA";
				}
				else {
					msg.tipo = "ERROR";
				}
				msg.codigo = "FOLIO_CONTRATO_EXISTE";
				msg.descripcion = "El Folio Contrato SAAPV ya existe.";
				salida.P_OUT_MENSAJES.add(msg);	
				continue;
			}
			if(fila.CodigoTipoSolicitud.equals("") || (fila.CodigoTipoSolicitud.equals("TRA") && fila.TipoMovimiento.equals("INGRESO"))){
				//*** VALIDACION CONTRATOS SAAPV - FIN ***
				if(fila.IdPersona == null || fila.IdPersona == 0){ //.compareTo(null)!=0){	
					//CREACION DE PERSONA
					enr.IdEnrolamiento 						= 	(long) 0;
					enr.IdEmpresa							=	in.p_IdEmpresa;
					enr.IdUsuario							=	in.p_IdUsuario;
					enr.IdTipoEntidad						=	"PERSONA";
					enr.IdEntidadEnrolada 					= 	(long) 0;
					enr.IdTipoDocumentoIdentidad 			=	"R";
					enr.IdTipoDocumentoIdentidadAsociado	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
					enr.DocumentoIdentidad 			 		= 	fila.DocumentoIdentidadCliente;
					enr.IdTipoDocumentoIdentidadAdicional 	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
					enr.DocumentoIdentidadAdicional			=	"";
					enr.Tipo						 		=	"N";
					enr.RazonSocial							=	"";
					enr.Nombres 							= 	fila.NombreCliente;
					enr.ApellidoPaterno 					=	fila.ApellidoPaternoCliente;
					enr.ApellidoMaterno						=	fila.ApellidoMaternoCliente;
					enr.IdPaisNacimiento					=	fila.IdPaisResidenciaCliente;
					enr.FechaNacimiento						=	Fecha.toDate(fila.FechaNacimientoCliente);
					enr.Sexo								=	fila.IdTipoGenero;
					enr.EstadoCivil							=	fila.EstadoCivil;
					enr.IdCiudad							=	fila.IdCiudadCliente;
					enr.IdDivisionPais						=	fila.IdComunaCliente;//1L;
					enr.DireccionCalle						=	fila.DireccionCliente;
					enr.DireccionNumero						=	String.valueOf(fila.NumeroDireccionCliente);
					enr.DireccionDepartamento 				=	"";
					if(fila.TelefonoMovil != null && !fila.TelefonoMovil.equals("")){				
						enr.IdTipoTelefono					=	"MOVIL";
						enr.Telefono						= 	fila.TelefonoMovil;
					}else if(fila.TelefonoFijo != null && !fila.TelefonoFijo.equals("")){
						enr.IdTipoTelefono					=	"FIJO";
						enr.Telefono						= 	fila.TelefonoFijo;	
					}else{
						enr.IdTipoTelefono					=	"OTRO";
						enr.Telefono						= 	fila.TelefonoLaboral;
					}
					enr.Email								=	fila.EmailCliente;
					enr.FechaCreacion						=	Fechatime;
					enr.FechaModificacion					=	Fechatime;
					enr.IdPersonaConyuge					=	(long) 0;
					enr.RegimenConyugal						=	"";
					enr.CLI_IdCategoriaInversionista 		=	"";
					enr.CLI_IdTipoCliente					=	CLI_IdTipoClienteDefecto;
					enr.CLI_IdClaseCliente					=	"";
					enr.CLI_IdEmpleadoEjecutivo				=	(long) 0;
					enr.CLI_CodigoAdicionalHolding			=	"";
					enr.POR_IdPerfilComercial				=	"";
					enr.EMP_CodigoEmpleado					=	"";
					enr.EMP_CodigoTipoCargoEmpleado			=	"";
					enr.EMP_IdEmpleadoSupervisor			=	(long) 0;
					enr.EMP_IdSucursal						=	(long) 0;
					enr.EMP_FechaInicioCargo				=	(java.sql.Date) FechaSistema;
					enr.EMP_FechaTerminoCargo				=	(java.sql.Date) FechaSistema;
					enr.CNT_CodigoInscripcion				=	"";
					enr.CNT_Emisor							=	"";
					enr.CNT_Depositos						=	"";
					enr.CNT_Bolsa							=	"";
					enr.CNT_Corredora						=	"";
					enr.CNT_CamaraDeCompensacion			=	"";
					enr.CNT_Banco							=	"";
					enr.CNT_Boveda							=	"";
					enr.PRO_IdAreaProveedor					=	(long) 0;
					enr.EMP_CargoIndefinido					=	"";
				
					EntradaPersona.p_IdEmpresa	=	in.p_IdEmpresa;
					EntradaPersona.p_IdModulo	=	in.p_IdModulo;
					EntradaPersona.p_IdUsuario	=	in.p_IdUsuario;
					List<Enrolamiento> enrPersona = new ArrayList<Enrolamiento>();
					enrPersona.add(enr);
					EntradaPersona.p_ConsultarEnrolamiento = enrPersona;
					
					ObjOut = per.logica(con, EntradaPersona);
					//salida.P_OUT_ERRORESCONTADOR = ObjOut.P_OUT_ERRORESCONTADOR;
					if(ObjOut.P_OUT_ERRORESCONTADOR == 0){
					    fila.IdPersona	= ObjOut.P_OUT_REFID;
					    
					    HashMap<String,Object> paramConsulta = new HashMap<>(1);
						paramConsulta.put("IdPersona", fila.IdPersona);
						List<ConsultaDireccion> ConsultaDireccion = sql.query(con, SQL_CONSULTAR_DIRECCION_FISICA,paramConsulta, ConsultaDireccion.class);
						//SQL_DIRECCION_PROPOSITO_APV
						//logger.debug(ConsultaDireccion.get(0));
						int[] resUpdate = sql.batch(con, SQL_DIRECCION_PROPOSITO_APV,ConsultaDireccion, ConsultaDireccion.class);		   			  
						
						if(fila.IdTipoCargoPersona != 0) {
							CambiarCargoPersona cambiarCargoPersona = new CambiarCargoPersona();
							List<CambiarCargoPersona> listCambiarCargoPersona = new ArrayList<CambiarCargoPersona>();
							cambiarCargoPersona.IdPersona = fila.IdPersona;
							cambiarCargoPersona.IdTipoCargoPersona = fila.IdTipoCargoPersona;
							listCambiarCargoPersona.add(cambiarCargoPersona);
							int[] res = sql.batch(con,SQL_CAMBIAR_CARGO_PERSONA,listCambiarCargoPersona,CambiarCargoPersona.class);
							MensajeSalida msg;
							if(res != null && res.length > 0 && res[0] == 1){
							    msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "OK";
								msg.codigo = "CAMBIAR_CARGO_PERSONA_OK";
								msg.descripcion = "Modificacion de cargo persona exitosa.";
								logger.debug(msg);
							}else {
								msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = "CAMBIAR_CARGO_PERSONA_ERROR";
								msg.descripcion = "Modificacion de cargo persona con problemas.";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
						
						//LABORAL
						if(fila.TelefonoLaboral != null)
							if(!fila.TelefonoLaboral.equals(""))
							{	
							   EntradaTelefonoPar.p_IdEmpresa = fila.IdEmpresa;
							   EntradaTelefonoPar.p_IdUsuario = in.p_IdUsuario;
							   EntradaTelefonoPar.p_IdModulo = in.p_IdModulo;
							   EntradaTelefonoPar.p_UsaReglasNegocio = "N";
							   EntradaTelefonoPar.p_IdPais = fila.IdPaisResidenciaCliente;
							   EntradaTelefonoPar.p_IdTipoTelefono = "OTRO";
							   EntradaTelefonoPar.p_Numero = fila.TelefonoLaboral;
							   EntradaTelefonoPar.p_Anexo = "";
							   EntradaTelefonoPar.p_CodigoProposito = "PREV";
							   EntradaTelefonoPar.p_Comentario = "";
							   EntradaTelefonoPar.p_IdEstado = "ACT";
							   EntradaTelefonoPar.p_Tipo = "";
							   EntradaTelefonoPar.p_IdPersona = fila.IdPersona;
							   
							   ObjOutFonoPar = fono.logica(con, EntradaTelefonoPar);
			
							   if(ObjOutFonoPar.P_OUT_ERRORESCONTADOR != 0){
								   MensajeSalida msg;
									for (ErrorDetalle lineaErrorFono : ObjOutFonoPar.P_OUT_ERRORESDETALLE) {
										if (lineaErrorFono.Codigo.compareTo("UI_ERROR_AL_CREAR_TELEFONO") == 0) {
										logger.debug("Error al intentar crear el Telefono Particular");
										}else{
										    msg = new MensajeSalida();
											msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
											msg.tipo = "ERROR";
											msg.codigo = "ERROR_CREAR_TELEFONO";
											msg.descripcion = "No es posible crear Telefono Particular";
											salida.P_OUT_MENSAJES.add(msg);
										}
									}	
							   }
						   }
					   
						//PARTICULAR
						if(fila.TelefonoLaboral != null)
							if(!fila.TelefonoLaboral.equals(""))
							{
							   EntradaTelefonoPar.p_IdEmpresa = fila.IdEmpresa;
							   EntradaTelefonoPar.p_IdUsuario = in.p_IdUsuario;
							   EntradaTelefonoPar.p_IdModulo = in.p_IdModulo;
							   EntradaTelefonoPar.p_UsaReglasNegocio = "N";
							   EntradaTelefonoPar.p_IdPais = fila.IdPaisResidenciaCliente;
							   EntradaTelefonoPar.p_IdTipoTelefono = "FIJO";
							   EntradaTelefonoPar.p_Numero = fila.TelefonoFijo;
							   EntradaTelefonoPar.p_Anexo = "";
							   EntradaTelefonoPar.p_CodigoProposito = "PREV";
							   EntradaTelefonoPar.p_Comentario = "";
							   EntradaTelefonoPar.p_IdEstado = "ACT";
							   EntradaTelefonoPar.p_Tipo = "";
							   EntradaTelefonoPar.p_IdPersona = fila.IdPersona;
							   
							   ObjOutFonoPar = fono.logica(con, EntradaTelefonoPar);
			
							   if(ObjOutFonoPar.P_OUT_ERRORESCONTADOR != 0){
								   MensajeSalida msg;
									for (ErrorDetalle lineaErrorFono : ObjOutFonoPar.P_OUT_ERRORESDETALLE) {
										if (lineaErrorFono.Codigo.compareTo("UI_ERROR_AL_CREAR_TELEFONO") == 0) {
											logger.debug("Error al intentar crear el Telefono Particular");
										}else{
										    msg = new MensajeSalida();
											msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
											msg.tipo = "ERROR";
											msg.codigo = "ERROR_CREAR_TELEFONO";
											msg.descripcion = "No es posible crear Telefono Particular";
											salida.P_OUT_MENSAJES.add(msg);
										}
									}	
							   }
						   	}
					    //MOVIL
						if(fila.TelefonoMovil != null)
						   	if(!fila.TelefonoMovil.equals("")){
							   EntradaTelefonoCom.p_IdEmpresa = fila.IdEmpresa;
							   EntradaTelefonoCom.p_IdUsuario = in.p_IdUsuario;
							   EntradaTelefonoCom.p_IdModulo = in.p_IdModulo;
							   EntradaTelefonoCom.p_UsaReglasNegocio = "N";
							   EntradaTelefonoCom.p_IdPais = fila.IdPaisResidenciaCliente;
							   //COMERCIAL
							   EntradaTelefonoCom.p_IdTipoTelefono = "MOVIL";
							   EntradaTelefonoCom.p_Numero = fila.TelefonoMovil;
							   EntradaTelefonoCom.p_Anexo = "";
							   EntradaTelefonoCom.p_CodigoProposito = "PREV";
							   EntradaTelefonoCom.p_Comentario = "";
							   EntradaTelefonoCom.p_IdEstado = "ACT";
							   EntradaTelefonoCom.p_Tipo = "";
							   EntradaTelefonoCom.p_IdPersona = fila.IdPersona;
							   
							   ObjOutFonoCom = fono.logica(con, EntradaTelefonoCom);
			
							   if(ObjOutFonoCom.P_OUT_ERRORESCONTADOR != 0){
								   MensajeSalida msg;
									for (ErrorDetalle lineaErrorFono : ObjOutFonoCom.P_OUT_ERRORESDETALLE) {
										if (lineaErrorFono.Codigo.compareTo("UI_ERROR_AL_CREAR_TELEFONO") == 0) {
											logger.debug("Error al intentar crear el Telefono Comercial");
										}else{
										    msg = new MensajeSalida();
											msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
											msg.tipo = "ERROR";
											msg.codigo = "ERROR_CREAR_TELEFONO";
											msg.descripcion = "No es posible crear Telefono Comercial";
											salida.P_OUT_MENSAJES.add(msg);
										}
									}	
							   }
						   	}
					   
						if(fila.EmailCliente != null)
						   	if(!fila.EmailCliente.equals("")){
							   
							   EntradaMail.p_IdEmpresa 			= fila.IdEmpresa;
							   EntradaMail.p_IdUsuario 			= in.p_IdUsuario;
							   EntradaMail.p_IdModulo 			= in.p_IdModulo;
							   EntradaMail.p_UsaReglasNegocio 	= "N";
							   EntradaMail.p_Direccion 			= fila.EmailCliente;
							   EntradaMail.p_CodigoProposito 	= "PREV";
							   EntradaMail.p_Comentario 		= "";
							   EntradaMail.p_IdEstado 			= "ACT";
							   EntradaMail.p_IdPersona 			= fila.IdPersona;
							  				  
							   ObjOutMail = email.logica(con, EntradaMail);
							  	
							   if(ObjOutMail.P_OUT_ERRORESCONTADOR != 0){
									for (ErrorDetalle lineaErroremail : ObjOutMail.P_OUT_ERRORESDETALLE) {
										if (lineaErroremail.Codigo.compareTo("UI_ERROR_AL_CREAR_EMAIL") == 0) {
											logger.debug("Email Proposito APV");
										}else{
										    MensajeSalida msg = new MensajeSalida();
											msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
											msg.tipo = "ERROR";
											msg.codigo = "ERROR_CREAR_EMAIL";
											msg.descripcion = "No es posible crear email Cliente";
											salida.P_OUT_MENSAJES.add(msg);
										}
									}	
							   }
						   	}
						
					    MensajeSalida msg = new MensajeSalida();
						msg.id =fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
						msg.tipo = "OK";
						msg.codigo = "CREACION_PERSONA_OK";
						msg.descripcion = "Creacion de persona exitosa";
						salida.P_OUT_MENSAJES.add(msg);
					}else{
						MensajeSalida msg;
						for (ErrorDetalle lineaError : ObjOut.P_OUT_ERRORESDETALLE) {
							if (lineaError.Codigo.compareTo("PERSONA_EXISTE") == 0) {
								logger.debug("PERSONA_EXISTE");
							    msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "PERSONA_EXISTE";
								msg.descripcion = "La Persona ya Existe.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
							    msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "No es posible crear persona";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}	
					}
				}

				if((fila.IdEmpleador == null || fila.IdEmpleador == 0) && !fila.DocumentoIdentidadEmpleador.equals("")){ 	
					//Creacion Empleador
					enr.IdEnrolamiento 						= 	(long) 0;
					enr.IdEmpresa							=	in.p_IdEmpresa;
					enr.IdUsuario							=	in.p_IdUsuario;
					enr.IdTipoEntidad						=	"PERSONA";
					enr.IdEntidadEnrolada 					= 	(long) 0;
					enr.IdTipoDocumentoIdentidad 			=	"R";
					enr.IdTipoDocumentoIdentidadAsociado	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
					enr.DocumentoIdentidad 			 		= 	fila.DocumentoIdentidadEmpleador;
					enr.IdTipoDocumentoIdentidadAdicional 	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
					enr.DocumentoIdentidadAdicional			=	"";
					enr.Tipo						 		=	"J";
					enr.RazonSocial							=	fila.RazonSocialEmpleador;
					enr.Nombres 							= 	"";
					enr.ApellidoPaterno 					=	"";
					enr.ApellidoMaterno						=	"";
					enr.IdPaisNacimiento					=	fila.IdPaisEmpleador;
					enr.FechaNacimiento						=	FechaSistema;
					enr.Sexo								=	"";
					enr.EstadoCivil							=	"";
					enr.IdCiudad							=	fila.IdCiudadEmpleador;
					enr.IdDivisionPais						=	fila.IdComunaEmpleador;//0L;
					enr.DireccionCalle						=	fila.DireccionComercialEmpleador;
					enr.DireccionNumero						=	String.valueOf(fila.NumeroDireccionEmpleador);
					enr.DireccionDepartamento 				=	"";
					if(fila.TelefonoLaboral != null && !fila.TelefonoLaboral.equals("")){				
						enr.IdTipoTelefono						=	"OTRO";
						enr.Telefono							= 	fila.TelefonoLaboral;
					}else if(fila.TelefonoFijo != null && !fila.TelefonoFijo.equals("") && fila.TelefonoLaboral.equals("")){
						enr.IdTipoTelefono						=	"FIJO";
						enr.Telefono							= 	fila.TelefonoFijo;	
					}else if(fila.TelefonoMovil != null && fila.TelefonoLaboral.equals("") && fila.TelefonoFijo.equals("")){
						enr.IdTipoTelefono					=	"MOVIL";
						enr.Telefono						= 	fila.TelefonoMovil;
					}
					enr.Email								=	"";
					enr.FechaCreacion						=	Fechatime;
					enr.FechaModificacion					=	Fechatime;
					enr.IdPersonaConyuge					=	(long) 0;
					enr.RegimenConyugal						=	"";
					enr.CLI_IdCategoriaInversionista 		=	"";
					enr.CLI_IdTipoCliente					=	CLI_IdTipoClienteDefecto;
					enr.CLI_IdClaseCliente					=	"";
					enr.CLI_IdEmpleadoEjecutivo				=	(long) 0;
					enr.CLI_CodigoAdicionalHolding			=	"";
					enr.POR_IdPerfilComercial				=	"";
					enr.EMP_CodigoEmpleado					=	"";
					enr.EMP_CodigoTipoCargoEmpleado			=	"";
					enr.EMP_IdEmpleadoSupervisor			=	(long) 0;
					enr.EMP_IdSucursal						=	(long) 0;
					enr.EMP_FechaInicioCargo				=	(java.sql.Date) FechaSistema;
					enr.EMP_FechaTerminoCargo				=	(java.sql.Date) FechaSistema;
					enr.CNT_CodigoInscripcion				=	"";
					enr.CNT_Emisor							=	"";
					enr.CNT_Depositos						=	"";
					enr.CNT_Bolsa							=	"";
					enr.CNT_Corredora						=	"";
					enr.CNT_CamaraDeCompensacion			=	"";
					enr.CNT_Banco							=	"";
					enr.CNT_Boveda							=	"";
					enr.PRO_IdAreaProveedor					=	(long) 0;
					enr.EMP_CargoIndefinido					=	"";
				
					EntradaPersona.p_IdEmpresa	=	in.p_IdEmpresa;
					EntradaPersona.p_IdModulo	=	in.p_IdModulo;
					EntradaPersona.p_IdUsuario	=	in.p_IdUsuario;
					List<Enrolamiento> enrEmpleador = new ArrayList<Enrolamiento>();
					enrEmpleador.add(enr);
					EntradaPersona.p_ConsultarEnrolamiento = enrEmpleador;
				 			
					ObjOut = per.logica(con, EntradaPersona);
					//salida.P_OUT_ERRORESCONTADOR = ObjOut.P_OUT_ERRORESCONTADOR;
					if(ObjOut.P_OUT_ERRORESCONTADOR == 0){
					    MensajeSalida msg = new MensajeSalida();
					    fila.IdEmpleador = ObjOut.P_OUT_REFID;
						msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
						msg.tipo = "OK";
						msg.codigo = "CREACION_PERSONA_OK";
						msg.descripcion = "Creacion de persona Empleador exitosa";

						List<IndicadorEncargadoRRHH> lstEncargado = new ArrayList<IndicadorEncargadoRRHH>();
						HashMap<String, Object> params = new HashMap<>(2);
						params.put("IdTipoEntidad", "PERSONA");
						params.put("GrupoIndicador", "DATOS EMPLEADOR");
						List<TipoIndicador> lstTipoIndicador = sql.query(con, SQL_CONSULTAR_RELACIONADO_RRHH, params, TipoIndicador.class);
						boolean existeTipoIndicador = (lstTipoIndicador.isEmpty()) ? false : true;
						boolean indicadoresEmpleadoSonVacio = "".equalsIgnoreCase(fila.NombreEncargado) && "".equalsIgnoreCase(fila.EmailEncargado) && "".equalsIgnoreCase(fila.TelefonoEncargado) ? true:false;
						if(existeTipoIndicador){
							for(TipoIndicador tp :lstTipoIndicador){
								IndicadorEncargadoRRHH		EncargadoRRHH	= new IndicadorEncargadoRRHH();
								EncargadoRRHH.IdIndicador = 0L;
								EncargadoRRHH.IdTipoIndicador = tp.IdTipoIndicador;
								if(tp.CodigoTipoIndicador.compareTo("NOMBRE_ENCARGADO_RRHH") == 0)								
									EncargadoRRHH.ValorIndicador =	indicadoresEmpleadoSonVacio ? "N":fila.NombreEncargado;
								else if(tp.CodigoTipoIndicador.compareTo("MAIL_ENCARGADO_RRHH") == 0)
									EncargadoRRHH.ValorIndicador =	indicadoresEmpleadoSonVacio ? "N":fila.EmailEncargado;
								else if(tp.CodigoTipoIndicador.compareTo("TELEFONO_ENCARGADO_RRHH") == 0)
									EncargadoRRHH.ValorIndicador = 	indicadoresEmpleadoSonVacio ? "N":fila.TelefonoEncargado;
								EncargadoRRHH.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
								lstEncargado.add(EncargadoRRHH);
							}		
						}
						
						SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #RegistroAtributo(\"IdIndicador\"	 BIGINT,\"IdTipoIndicador\" BIGINT,\"ValorIndicador\" NVARCHAR(255),\"FechaVencimiento\" SECONDDATE)");
						
						if (lstEncargado.size() > 0)
						{
							//log.write("SOLICITA LIQUIDAR POR COMPROBANTE");
							sql.batch(con,
									"INSERT INTO #RegistroAtributo (\"IdIndicador\"	,\"IdTipoIndicador\" ,\"ValorIndicador\" ,\"FechaVencimiento\" ) VALUES(:IdIndicador, :IdTipoIndicador, :ValorIndicador, :FechaVencimiento)",
									lstEncargado, 
									IndicadorEncargadoRRHH.class);
						}
						
						HashMap<String,Object> paramsIn = new HashMap<String,Object>();
						paramsIn.put("IdEmpresa", in.p_IdEmpresa);
						paramsIn.put("IdUsuario", in.p_IdUsuario);
						paramsIn.put("IdModulo", in.p_IdModulo);
						paramsIn.put("IdEntidad", fila.IdEmpleador);	
						paramsIn.put("RegistroAtributo", lstEncargado);	
						
						//Salida HashMap
						HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
						paramsOut.put("p_out_IdSecuencias", SecuenciaIndicador.class);
						paramsOut.put("p_out_AdvertenciasContador", Long.class);
						paramsOut.put("p_out_ErroresContador", Long.class);
						paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
						paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
						
						HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_RELACIONADO_RRHH, paramsIn, paramsOut);
						
						if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
							//Ingreso de Indicador Exitoso.						
						}else{
							for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
								SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
								msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_ENCARGADO_RRHH";
								msg.descripcion = "No es posible Ingresar encargado de recursos humanos.";
								salida.P_OUT_MENSAJES.add(msg);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
								msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ADVERTENCIA_ENCARGADO_RRHH";
								msg.descripcion = "No es posible Ingresar encargado de recursos humanos.";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
						
						SQL.exec(con, "DROP TABLE #RegistroAtributo");
					}else{
						MensajeSalida msg;
						for (ErrorDetalle lineaError : ObjOut.P_OUT_ERRORESDETALLE) {
							msg = new MensajeSalida();
							if (lineaError.Codigo.compareTo("PERSONA_EXISTE") == 0) {
								logger.debug("PERSONA_EXISTE");
								msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "PERSONA_EMPLEADOR_EXISTE";
								msg.descripcion = "Persona Empleador ya existe.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
							    msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "No es posible crear persona Empleador";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
					}
				}
				List<String> resultadoValidacion = ValidaCamposPersona("CLIENTE",fila,null);
				if(resultadoValidacion.size() == 0){
					if(fila.IdCliente == null || fila.IdCliente == 0){
						//Creacion Cliente
						enr.IdEnrolamiento 						= 	0L;
						enr.IdEmpresa							=	in.p_IdEmpresa;
						enr.IdUsuario							=	in.p_IdUsuario;
						enr.IdTipoEntidad						=	"CLIENTE";
						enr.IdEntidadEnrolada 					= 	0L;
						enr.IdTipoDocumentoIdentidad 			=	"R";
						enr.IdTipoDocumentoIdentidadAsociado	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
						enr.DocumentoIdentidad 			 		= 	fila.DocumentoIdentidadCliente;
						enr.IdTipoDocumentoIdentidadAdicional 	=	"R"; // el ingreso por socio de negocio lo deja en R asi que se replica aca
						enr.DocumentoIdentidadAdicional			=	"";
						enr.Tipo						 		=	"N";
						enr.RazonSocial							=	"";
						enr.Nombres 							= 	fila.NombreCliente;
						enr.ApellidoPaterno 					=	fila.ApellidoPaternoCliente;
						enr.ApellidoMaterno						=	fila.ApellidoMaternoCliente;
						enr.IdPaisNacimiento					=	fila.IdPaisResidenciaCliente;
						enr.FechaNacimiento						=	FechaSistema;
						enr.Sexo								=	fila.IdTipoGenero;
						enr.EstadoCivil							=	fila.EstadoCivil;
						enr.IdCiudad							=	fila.IdCiudadCliente;
						enr.IdDivisionPais						=	fila.IdComunaCliente;//1L;
						enr.DireccionCalle						=	fila.DireccionCliente;
						enr.DireccionNumero						=	"";
						enr.DireccionDepartamento 				=	"";
						if(fila.TelefonoMovil != null && !fila.TelefonoMovil.equals("")) {
							enr.IdTipoTelefono						=	"MOVIL";
							enr.Telefono							= 	fila.TelefonoMovil;
						}else if(fila.TelefonoFijo != null && fila.TelefonoMovil.equals("") && !fila.TelefonoFijo.equals("")) {
							enr.IdTipoTelefono						=	"FIJO";
							enr.Telefono							= 	fila.TelefonoFijo;
						}else if(fila.TelefonoLaboral != null && fila.TelefonoMovil.equals("") && fila.TelefonoFijo.equals("") && !fila.TelefonoLaboral.equals("")) {
							enr.IdTipoTelefono						=	"OTRO";
							enr.Telefono							= 	fila.TelefonoLaboral;
						}
						enr.Email								=	fila.EmailCliente;
						enr.FechaCreacion						=	Fechatime;
						enr.FechaModificacion					=	Fechatime;
						enr.IdPersonaConyuge					=	0L;
						enr.RegimenConyugal						=	"";
						enr.CLI_IdCategoriaInversionista 		=	fila.IdCategoriaInversionista;
						enr.CLI_IdTipoCliente					=	CLI_IdTipoClienteDefecto;
						enr.CLI_IdClaseCliente					=	"";
						enr.CLI_IdEmpleadoEjecutivo				=	fila.IdEmpleado;
						enr.CLI_CodigoAdicionalHolding			=	"";
						enr.POR_IdPerfilComercial				=	fila.IdPerfilComercial;
						enr.EMP_CodigoEmpleado					=	"";
						enr.EMP_CodigoTipoCargoEmpleado			=	"";
						enr.EMP_IdEmpleadoSupervisor			=	0L;
						enr.EMP_IdSucursal						=	0L;
						enr.EMP_FechaInicioCargo				=	(java.sql.Date) FechaSistema;
						enr.EMP_FechaTerminoCargo				=	(java.sql.Date) FechaSistema;
						enr.CNT_CodigoInscripcion				=	"";
						enr.CNT_Emisor							=	"";
						enr.CNT_Depositos						=	"";
						enr.CNT_Bolsa							=	"";
						enr.CNT_Corredora						=	"";
						enr.CNT_CamaraDeCompensacion			=	"";
						enr.CNT_Banco							=	"";
						enr.CNT_Boveda							=	"";
						enr.PRO_IdAreaProveedor					=	0L;
						enr.EMP_CargoIndefinido					=	"";
						enr.CLI_ClienteFinal					=   "N";
						enr.POR_IdPersonaBrokerExtranjero		=   0L;

						EntradaCliente.p_IdEmpresa	=	in.p_IdEmpresa;
						EntradaCliente.p_IdModulo	=	in.p_IdModulo;
						EntradaCliente.p_IdUsuario	=	in.p_IdUsuario;
						List<Enrolamiento> enrCliente = new ArrayList<Enrolamiento>();
						enrCliente.add(enr);
						EntradaCliente.p_ConsultarEnrolamiento = enrCliente;
						
						SalidaCliente = cli.logica(con, EntradaCliente);
						//salida.P_OUT_ERRORESCONTADOR = ObjOut.P_OUT_ERRORESCONTADOR;
						if(SalidaCliente.P_OUT_REFIDCLIENTE != 0)
						{
							fila.IdCliente	= SalidaCliente.P_OUT_REFIDCLIENTE;
						    MensajeSalida msg = new MensajeSalida();
							msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
							msg.tipo = "OK";
							msg.codigo = "CREACION_CLIENTE_OK";
							msg.descripcion = "Creacion de cliente exitosa";
							salida.P_OUT_MENSAJES.add(msg);
						}
						MensajeSalida msg;
						if(SalidaCliente.P_OUT_ERRORESCONTADOR > 0 ){
							for (ErrorDetalle lineaError : SalidaCliente.P_OUT_ERRORESDETALLE) {
								msg = new MensajeSalida();
								if (lineaError.Codigo.equals("CLIENTE_EXISTE")){
									logger.debug("CLIENTE_EXISTE");
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "CLIENTE_EXISTE";
									msg.descripcion = "CLIENTE_EXISTE";
									salida.P_OUT_MENSAJES.add(msg);
								}else if(lineaError.Codigo.equals("PERFIL_COMERCIAL_NO_EXISTE")){
									logger.debug("PERFIL_COMERCIAL_NO_EXISTE");
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "PERFIL_NO_EXISTE";
									msg.descripcion = "PERFIL_COMERCIAL_NO_EXISTE";
									salida.P_OUT_MENSAJES.add(msg);
								}else if(lineaError.Codigo.equals("CLIENTE_NO_PUEDE_SER_ENROLADO")) {
									logger.debug("CLIENTE_NO_PUEDE_SER_ENROLADO");
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "CLIENTE_NO_PUEDE_CREAR_PORTAFOLIO";
									msg.descripcion = "CLIENTE_NO_PUEDE_CREAR_PORTAFOLIO";
								}else{
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_CREAR_CLIENTE";
									msg.descripcion = "No es posible crear cliente";
									salida.P_OUT_MENSAJES.add(msg);
								}
							}
						}
					}	
				}else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_DATOS_MINIMOS_CLIENTE";
					msg.descripcion = "No es posible completar la creacion del cliente por datos faltantes.";
					salida.P_OUT_MENSAJES.add(msg);
					/*
					for(int i=0; i < resultadoValidacion.size(); i++){
						msg = new MensajeSalida();
						msg.id = idsContratoSAAPV.get(contadorIds);
						msg.tipo = "ADVERTENCIA";
						msg.codigo = "VALIDA_DATOS_MINIMOS_CLIENTE";
						msg.descripcion = " - " + resultadoValidacion.get(i);
						salida.P_OUT_MENSAJES.add(msg);
					}
					*/
				}
				
				/*Se ingresa Relacion Persona Cliente Empleador*/
				if((fila.IdPersona != null || fila.IdPersona != 0) && ((fila.IdEmpleador != null && fila.IdEmpleador != 0) || fila.IdEmpleador > 0)){
					
					//Valida la existencia de la relacion de la persona con el empleador
				    HashMap<String,Object> paramConsultaRelacion = new HashMap<>(2);
					paramConsultaRelacion.put("IdPersona", fila.IdPersona);
					paramConsultaRelacion.put("IdEmpleador", fila.IdEmpleador);
					List<ConsultaRelacionPersona> ConsultaRelacion = sql.query(con, SQL_CONSULTAR_RELACION_PERSONA, paramConsultaRelacion, ConsultaRelacionPersona.class);

					boolean existeRelacionPersona = (ConsultaRelacion.isEmpty()) ? false : true;
					if(!existeRelacionPersona){
						//Si no existe relacion, debe crearla
						//Entrada HashMap
						HashMap<String,Object> paramsIn = new HashMap<String,Object>();
						paramsIn.put("IdEmpresa", in.p_IdEmpresa);
						paramsIn.put("IdUsuario", in.p_IdUsuario);
						paramsIn.put("IdModulo", in.p_IdModulo);			
						paramsIn.put("IdTipoRelacion", "EMPDOR");
						paramsIn.put("IdPersonaOrigen", fila.IdPersona);
						paramsIn.put("IdPersonaDestino", fila.IdEmpleador);
						paramsIn.put("IdEstado", "ACT");
						paramsIn.put("FechaInicioRelacion", FechaSistema);
						paramsIn.put("FechaFinRelacion", Fecha.toDate(FechaFinRelacionDefecto+" 00:00:00"));
						paramsIn.put("PorcentajeParticipacion", 0);
						
						//Salida HashMap
						HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
						paramsOut.put("p_out_RefId", Long.class);
						paramsOut.put("p_out_AdvertenciasContador", Long.class);
						paramsOut.put("p_out_ErroresContador", Long.class);
						paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
						paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
						
						HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_RELACION_PERSONA, paramsIn, paramsOut);
						if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
							IdRelacionPersona = (Long)resultSP.get("p_out_RefId");
						}else{
							MensajeSalida msg;
							for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
								msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = errorDetalle.Codigo;
								msg.descripcion = errorDetalle.Descripcion;
								salida.P_OUT_MENSAJES.add(msg);
								SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								msg = new MensajeSalida();
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = advertenciaDetalle.Codigo;
								msg.descripcion = advertenciaDetalle.Descripcion;
								salida.P_OUT_MENSAJES.add(msg);
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
							}
						}
					}
				}
				/*else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : Long.parseLong(fila.FolioContratoSAAPV);
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_RELACION_EMPLEADOR_TRABAJADOR";
					msg.descripcion = "No es posible Ingresar relacion entre Empleador y Trabajador por datos Pendientes de completar.";
					salida.P_OUT_MENSAJES.add(msg);
				}
				*/
	
			}
			if(fila.CodigoTipoSolicitud.equals("")){
				if(in.p_PortafoliosModificados.size() > 0 && fila.IdCliente != 0){
				 	for(PortafoliosModificados filaCreacionPortafolio : in.p_PortafoliosModificados){		
			 			if(fila.IdInterno == filaCreacionPortafolio.IdInterno) {
			 				HashMap<String,Object> paramsIn = new HashMap<String,Object>();
							paramsIn.put("IdEmpresa", in.p_IdEmpresa);
							paramsIn.put("IdUsuario", in.p_IdUsuario);
							paramsIn.put("IdModulo", in.p_IdModulo);
							paramsIn.put("PropositoPortafolio", filaCreacionPortafolio.IdPropositoPortafolio);
							paramsIn.put("IdCliente", fila.IdCliente);
							//paramsIn.put("IdPerfilComercial", "PERF");
							paramsIn.put("IdPerfilComercial", fila.IdPerfilComercial);
							paramsIn.put("CodigoPortafolio", filaCreacionPortafolio.Regimen);
							paramsIn.put("CodigoRegimen", filaCreacionPortafolio.Regimen);
							paramsIn.put("NombrePlan", fila.NombrePlan);
							paramsIn.put("CodigoAgrupador", filaCreacionPortafolio.CodigoAgrupador);				
								
							//Salida HashMap
							HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
							paramsOut.put("p_out_RefId", Long.class);
							paramsOut.put("p_out_AdvertenciasContador", Long.class);
							paramsOut.put("p_out_ErroresContador", Long.class);
							paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
							paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
							
							HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_PORTAFOLIO_APV_Call, paramsIn, paramsOut);
							
							if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0)) &&((Long) resultSP.get("p_out_AdvertenciasContador")).equals(new Long(0))){
								SalidaCliente.P_OUT_REFIDPORTAFOLIO = (Long)resultSP.get("p_out_RefId");
								salida.P_REF_ID_PORTAFOLIO = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							}else{
								MensajeSalida msg;
								for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
									SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
									msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									if(fila.IdCargaMasiva != null) {
										msg.tipo = fila.IdCargaMasiva== 0 ?"ERROR" :"ADVERTENCIA";
									}
									else {
										msg.tipo = "ERROR";
									}
									msg.codigo = errorDetalle.Codigo;
									msg.descripcion = errorDetalle.Descripcion;
									salida.P_OUT_MENSAJES.add(msg);
								}
								for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
									SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
									msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo ="ADVERTENCIA";
									msg.codigo = advertenciaDetalle.Codigo;
									msg.descripcion = advertenciaDetalle.Descripcion;
									salida.P_OUT_MENSAJES.add(msg);
								}
							}
							if(SalidaCliente.P_OUT_REFIDPORTAFOLIO != 0)
							{
							    MensajeSalida msg = new MensajeSalida();
							    filaCreacionPortafolio.IdPortafolio = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
								IdPortafolioAPV = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
								msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
								msg.tipo = "OK";
								msg.codigo = "CREACION_PORTAFOLIO_OK";
								msg.descripcion = "Creacion de portafolio exitosa";
								salida.P_OUT_MENSAJES.add(msg);
							}	
			 			}		
				 	}	
				}else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_DATOS_MINIMOS_CLIENTE";
					msg.descripcion = "No es posible Crear Plan de Ahorro (Portafolio) porque existen datos pendientes de Cliente.";
					salida.P_OUT_MENSAJES.add(msg);
				}
			}

			if(fila.CodigoTipoSolicitud.equals("") || (fila.CodigoTipoSolicitud.equals("TRA") && fila.TipoMovimiento.equals("INGRESO"))){
				ContratoSAAPV PC = new ContratoSAAPV();			
				PC.IdContratoSAAPV				= fila.IdContratoSAAPV;
				PC.IdEmpresa					= fila.IdEmpresa;
				PC.FolioContratoSAAPV 			= fila.FolioContratoSAAPV;
				PC.IdPlan						= fila.IdPlan;
				PC.NombrePlan					= fila.NombrePlan;
			    PC.FechaSuscripcion				= fila.FechaSuscripcion; 
			    PC.FechaCreacion				= fila.FechaCreacion;
			    PC.FechaAprobacion				= fila.FechaAprobacion;
			    PC.FechaNotificacion			= fila.FechaNotificacion;
			    PC.FechaRecepcion				= fila.FechaRecepcion;
			    PC.Folio						= fila.Folio;
			    PC.IdPersona					= fila.IdPersona;
			    PC.IdEmpleador					= fila.IdEmpleador;
			    PC.IdCliente					= fila.IdCliente;
			    PC.NombreCliente				= fila.NombreCliente;
			    PC.ApellidoPaternoCliente		= fila.ApellidoPaternoCliente;
			    PC.ApellidoMaternoCliente		= fila.ApellidoMaternoCliente; 
			    PC.DocumentoIdentidadCliente	= fila.DocumentoIdentidadCliente;
			    PC.FechaNacimientoCliente		= fila.FechaNacimientoCliente;
			    PC.EstadoCivil					= fila.EstadoCivil;
			    PC.IdTipoGenero					= fila.IdTipoGenero;
			    PC.IdPaisResidenciaCliente		= fila.IdPaisResidenciaCliente;
			    PC.IdCiudadCliente				= fila.IdCiudadCliente;
			    PC.IdComunaCliente				= fila.IdComunaCliente;
			    PC.DireccionCliente				= fila.DireccionCliente;
			    PC.IdTipoCargoPersona			= fila.IdTipoCargoPersona;
			    PC.TelefonoCliente				= fila.TelefonoFijo;
			    PC.TelefonoComercialCliente		= fila.TelefonoLaboral;
			    PC.TelefonoCelularCliente		= fila.TelefonoMovil;
			    PC.EmailCliente					= fila.EmailCliente;
			    PC.RazonSocialEmpleador			= fila.RazonSocialEmpleador;	    
			    PC.IdMedioRecaudacionTrabajador	= fila.IdMedioRecaudacionTrabajador;
			    PC.IdEntidadRecaudadora	        = fila.IdEntidadRecaudadora;	    
			    PC.DocumentoIdentidadEmpleador	= fila.DocumentoIdentidadEmpleador;
			    PC.DireccionComercialEmpleador	= fila.DireccionComercialEmpleador;
			    PC.IdComunaEmpleador			= fila.IdComunaEmpleador; 
			    PC.TelefonoEmpleador			= fila.TelefonoEmpleador; 
			    PC.NombreEncargado				= fila.NombreEncargado;
			    PC.EmailEncargado				= fila.EmailEncargado;
			    PC.TelefonoEncargado			= fila.TelefonoEncargado;
			    PC.CodigoPrevision				= fila.CodigoPrevision; 
			    PC.Pensionado					= fila.Pensionado;
			    PC.Trabajador					= fila.Trabajador;
			    PC.FormaDePago					= fila.FormaDePago;
			    PC.IdMedioRecaudacionFormaPago	= fila.IdMedioRecaudacionFormaPago;
			    PC.DiaDescuento					= fila.DiaDescuento;
			    PC.Institucion					= fila.Institucion;
			    PC.ComisionTransferencia		= fila.ComisionTransferencia;
			    PC.CodigoTipoSolicitud			= fila.CodigoTipoSolicitud;
			    PC.NombreEntidadOrigen			= fila.NombreEntidadOrigen;
			    PC.RutEntidadOrigen				= fila.RutEntidadOrigen;
			    PC.TipoDeAhorro					= fila.TipoDeAhorro;
			    PC.FondoDeOrigen				= fila.FondoDeOrigen;
			    PC.TipoTraspaso					= fila.TipoTraspaso;
			    PC.MontoTraspaso				= fila.MontoTraspaso;
			    PC.UFTraspaso					= fila.UFTraspaso;
			    PC.PorcentajeTraspaso			= fila.PorcentajeTraspaso;
			    PC.IdEstadoEnvioNotificacion 	= fila.IdEstadoEnvioNotificacion;
			    PC.IdEstadoSolicitud 			= fila.IdEstadoSolicitud;
			    PC.IdEstado						= fila.IdEstado;
			    PC.IdCategoriaInversionista		= fila.IdCategoriaInversionista;
			    PC.IdPerfilComercial			= fila.IdPerfilComercial;
			    PC.IdCargaMasiva                = fila.IdCargaMasiva;
			    PC.Canal						= fila.Canal;
			    
				List<ContratoSAAPV> ParamsContrato = new ArrayList<ContratoSAAPV>();			
				ParamsContrato.add(PC);
				
				int[] res;
				try {
					res = sql.batch(con, SQL_CREAR_CONTRATOSAAPV,ParamsContrato, ContratoSAAPV.class);
					AgregarLogSAAPV(con, in, "");
				} catch (Exception er) {
					String codigoError = er.getMessage().substring(er.getMessage().indexOf("[")+1,er.getMessage().indexOf("]"));
					if("301".equalsIgnoreCase(codigoError)) {
						MensajeSalida msg = new MensajeSalida();
						msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
						if(fila.IdCargaMasiva != null)
							msg.tipo = fila.IdCargaMasiva== 0 ?"ERROR" :"ADVERTENCIA";
						else
							msg.tipo = "ERROR";
						msg.codigo = "FOLIO_CONTRATO_EXISTE";
						msg.descripcion = "El Folio Contrato SAAPV ya existe.";
						salida.P_OUT_MENSAJES.add(msg);	
					}else {
						MensajeSalida msg = new MensajeSalida();
						msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
						msg.tipo = "ERROR";
						msg.codigo = "ERROR_CREAR_CONTRATO";
						msg.descripcion = "Problemas al crear contrato";
						salida.P_OUT_MENSAJES.add(msg);
					}
					return salida;
				}
				
				ContratosSAAPV_Salida salidaEstado = new ContratosSAAPV_Salida();
				if(res != null && res.length > 0 && res[0] == 1){
				    MensajeSalida msgContrato = new MensajeSalida();
				    String descripcion = "";
				    if(fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("")) {
				    	descripcion = "Creacion de contrato exitosa";
				    }else{
				    	descripcion = "Creacion de contrato exitosa Folio: " + fila.FolioContratoSAAPV; ;
				    }
				    msgContrato.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
				    msgContrato.tipo = "OK";
				    msgContrato.codigo = "CREACION_CONTRATO_OK";
				    msgContrato.descripcion = descripcion;
					salida.P_OUT_MENSAJES.add(msgContrato);
                   					
					if(in.p_Tipo.compareTo("M") == 0){

						HashMap<String,Object> paramConsulta = new HashMap<>(1);
						paramConsulta.put("IdContratoSAAPV", IdContratoSAAPVBorrar);
						List<ContratoConsultado> ContratoConsultado = sql.query(con, SQL_CONSULTAR_CONTRATOSAAPV,paramConsulta, ContratoConsultado.class);
						for (ContratoConsultado linea : ContratoConsultado) {
							if (linea.IdEstado.compareTo("PDI") == 0 || linea.IdEstado.compareTo("PEN") == 0){
								ContratoConsultado paramBorrar = new ContratoConsultado();
								paramBorrar.IdContratoSAAPV = IdContratoSAAPVBorrar;
								List<ContratoConsultado> p = new ArrayList<ContratoConsultado>();
								p.add(paramBorrar);
								int[] resDelete = sql.batch(con, SQL_ELIMINAR_CONTRATOSAAPV,p, ContratoConsultado.class);
								if(resDelete != null && resDelete.length > 0 && resDelete[0] == 1){
									AgregarLogSAAPV(con, in, "ELIMINAR");
								    MensajeSalida msgEliContrato = new MensajeSalida();
								    msgEliContrato.id = idsContratoSAAPV.get(contadorIds);
								    msgEliContrato.tipo = "OK";
								    msgEliContrato.codigo = "ELIMINA_CONTRATO_OK";
								    msgEliContrato.descripcion = "Se elimina el contrato anterior";
									
									int[] resDeleteDistrib = sql.batch(con, SQL_ELIMINAR_DISTRIBUCION_CONTRATOSAAPV,p, ContratoConsultado.class);
									if(resDeleteDistrib != null && resDeleteDistrib.length > 0 && resDeleteDistrib[0] == 1){
									    MensajeSalida msg = new MensajeSalida();
										msg.id = idsContratoSAAPV.get(contadorIds);
										msg.tipo = "OK";
										msg.codigo = "ELIMINA_DISTRIBUCION_CONTRATO_OK";
										msg.descripcion = "Se elimina el Distribucion Inversion anterior";
									}
								}
							}
						}
					}
					else {
						SQL.exec(con, SQL_CREAR_ENTRADA_TEMPORAL);
						
						List<PortafolioACerrar> PortafoliosACerrar = new ArrayList<>();

						for (PortafoliosModificados linea : in.p_PortafoliosModificados) {
							PortafolioACerrar PortafolioACerrar = new PortafolioACerrar();
							
							PortafolioACerrar.IdPortafolio = linea.IdPortafolio;
							PortafolioACerrar.Producto = linea.Producto;
							
							PortafoliosACerrar.add(PortafolioACerrar);
						}

						sql.batch(con, SQL_INSERT_ENTRADA_TEMPORAL, PortafoliosACerrar, PortafolioACerrar.class);
						
						HashMap<String,Object> params =  new HashMap<>(2);
						params.put("IdMedioRecaudacionFormaPago", fila.IdMedioRecaudacionFormaPago);
						params.put("FormaDePago", fila.FormaDePago);
						
						List<ContratoSAAPVID> contratosMismosRequisitos = sql.query(con, SQL_CONSULTA_CONTRATOSAAPV_CONDICIONES_DUPLICADAS, params, ContratoSAAPVID.class);
						
						HashMap<String,Object> paramsMediosRec =  new HashMap<>(0);
						MedioRecaudacion medioRecaudacionPAC = sql.query(con, SQL_CONSULTA_MEDIOS_RECAUDACION_FORMA_PAGO, paramsMediosRec, MedioRecaudacion.class).stream().findFirst().orElse(new MedioRecaudacion());
						
						List<ContratoSAAPVID> ListaEntradaCerrarContratos = new ArrayList<ContratoSAAPVID>();

						for(ContratoSAAPVID linea : contratosMismosRequisitos){
							if (linea.Pago.equalsIgnoreCase(in.p_PortafoliosModificados.get(0).Pago) ) {
								if("UNI".equalsIgnoreCase(linea.Pago) && linea.PeriodoInicio.equalsIgnoreCase(in.p_PortafoliosModificados.get(0).MesInicio)){
									if(debeCerrarContratoUnico(con, linea, in.p_DistribucionInversion, in.p_PortafoliosModificados, fila, medioRecaudacionPAC) ) {
										ContratoSAAPVID EntradaCerrarContratos = new ContratoSAAPVID();
										EntradaCerrarContratos.IdContratoSAAPV = linea.IdContratoSAAPV;
										ListaEntradaCerrarContratos.add(EntradaCerrarContratos);
									}	
								}else if(!"UNI".equalsIgnoreCase(linea.Pago) && !("IND").equalsIgnoreCase(linea.Pago)) {
									ContratoSAAPVID EntradaCerrarContratos = new ContratoSAAPVID();
									EntradaCerrarContratos.IdContratoSAAPV = linea.IdContratoSAAPV;
									ListaEntradaCerrarContratos.add(EntradaCerrarContratos);
								} else if (("IND").equalsIgnoreCase(linea.Pago)  && debeCerrarContratoIndefinido(con, linea, in.p_DistribucionInversion, in.p_PortafoliosModificados, fila)) {
									ContratoSAAPVID EntradaCerrarContratos = new ContratoSAAPVID();
									EntradaCerrarContratos.IdContratoSAAPV = linea.IdContratoSAAPV;
									ListaEntradaCerrarContratos.add(EntradaCerrarContratos);
								}
							}
						}
						
						int[] resCerrarContrato = sql.batch(con, SQL_CERRAR_CONTRATOSAAPV_CONDICIONES_DUPLICADAS,ListaEntradaCerrarContratos, ContratoSAAPVID.class);
						if(resCerrarContrato != null && resCerrarContrato.length > 0){
							AgregarLogSAAPV(con, in, "CERRAR");
						    MensajeSalida msgCerContrato = new MensajeSalida();
						    msgCerContrato.id = 1L;
						    msgCerContrato.tipo = "OK";
						    msgCerContrato.codigo = "CONTRATO_CERRADO_OK";
						    msgCerContrato.descripcion = "Se cierra el contrato anterior";
						    salida.P_OUT_MENSAJES.add(msgCerContrato);
							int[] resCerrarDistrib = sql.batch(con, SQL_CERRAR_DISTRIBUCION_CONTRATOSAAPV_CONDICIONES_DUPLICADAS,ListaEntradaCerrarContratos, ContratoSAAPVID.class);
							if(resCerrarDistrib != null && resCerrarDistrib.length > 0){
							    MensajeSalida msg = new MensajeSalida();
								msg.id = 1L;
								msg.tipo = "OK";
								msg.codigo = "CIERRA_DISTRIBUCION_CONTRATO_OK";
								msg.descripcion = "Se cierra la Distribucion Inversion anterior";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
						
						SQL.exec(con, SQL_ELIMINAR_ENTRADA_TEMPORAL);
					}
					
					/*Valido datos minimos del contrato para ingresarlo vig o pdi*/
					List<String> resultadoValidacionContrato;
					if(in.p_PortafoliosModificados.size() > 0 )
						resultadoValidacionContrato = ValidaCamposPersona("DATOS_MINIMOS",fila,in.p_PortafoliosModificados.get(0));
					else
						resultadoValidacionContrato = ValidaCamposPersona("DATOS_MINIMOS",fila,null);
					
					if(resultadoValidacionContrato.size() == 0 && !fila.CodigoTipoSolicitud.equals("TRA") && (in.p_DistribucionInversion.size() > 0)){
						//BDH Se cambia estado a VIG
						CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada inAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada();
						List<CambiarEstadoContratosSAAPV.ContratosSAAPV> tablaContratos = new ArrayList<CambiarEstadoContratosSAAPV.ContratosSAAPV>();
						
						CambiarEstadoContratosSAAPV.ContratosSAAPV ContratoAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV();
						ContratoAutorizacion.IdPersona = fila.IdPersona;
						ContratoAutorizacion.IdCliente = fila.IdCliente;
						ContratoAutorizacion.IdEmpleador = fila.IdEmpleador;
						ContratoAutorizacion.IdFolioContratoSAAPV = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : fila.FolioContratoSAAPV;
						ContratoAutorizacion.FechaRevocacion = "1900-01-01";
						ContratoAutorizacion.IdEstadoEnvioNotificacion = fila.IdEstadoEnvioNotificacion;
						ContratoAutorizacion.IdEstadoSolicitud = "PDA";
						ContratoAutorizacion.IdEstado = "VIG";//debe ir vigente ya que la validacion de campos del contrato dio como resultado OK
						tablaContratos.add(ContratoAutorizacion);
					
						CambiarEstadoContratosSAAPV cambiarEstadoContratoService = new CambiarEstadoContratosSAAPV();
						inAutorizacion.p_IdEmpresa = in.p_IdEmpresa;
						inAutorizacion.p_IdUsuario = in.p_IdUsuario;
						inAutorizacion.p_IdModulo = in.p_IdModulo;
						inAutorizacion.p_Opcion = "VIG";
						inAutorizacion.p_ContratosSAAPV = tablaContratos;
						salidaEstado = cambiarEstadoContratoService.logica(con, inAutorizacion);
						/*BDH Se cambia estado a VIG FIN */ 
					}else {
						MensajeSalida msg = new MensajeSalida();
						msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
						msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
						msg.tipo = "ADVERTENCIA";
						msg.descripcion = "Contrato Ingresado con datos Pendientes de completar.";
						
						salida.P_OUT_MENSAJES.add(msg);
						for(int i=0; i < resultadoValidacionContrato.size(); i++){
							if(resultadoValidacionContrato.get(i).equals("Razon Social.") ||
									resultadoValidacionContrato.get(i).equals("Documento Identidad Empleador.") || 
										resultadoValidacionContrato.get(i).equals("Direccion Comercial Empleador.") || 
											resultadoValidacionContrato.get(i).equals("Numero Direccion Empleador.")) {
									msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
									msg.descripcion = "Trabajador 'DEPENDIENTE' sin informar Empleador.";
									salida.P_OUT_MENSAJES.add(msg);
									break;
							}						
						}
						
						if( !fila.CodigoTipoSolicitud.equals("TRA") && (in.p_DistribucionInversion.size() ==0) && "N".equals(fila.EsAporteCuentaInversion)) {
							msg = new MensajeSalida();
							msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
							msg.tipo = "ADVERTENCIA";
							msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
							msg.descripcion = "No posee distribucion inversion.";
							salida.P_OUT_MENSAJES.add(msg);
							break;
						}
					}
					
				}else{
				    MensajeSalida msg = new MensajeSalida();
					msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ERROR";
					msg.codigo = "ERROR_CREAR_CONTRATO";
					msg.descripcion = "Problemas al crear contrato";
					salida.P_OUT_MENSAJES.add(msg);
				}				
				if(in.p_DistribucionInversion.size() > 0){
					DistribucionInversion DisInversion; 
					List<DistribucionInversion> lstInversion = new ArrayList<DistribucionInversion>();  
					for(DistribucionInversion row : in.p_DistribucionInversion){
						if(fila.IdInterno == row.IdInterno && row.IdInstrumento != 0) {
							DisInversion = new DistribucionInversion(); 
							DisInversion.IdContratoSAAPV = fila.IdContratoSAAPV;
							DisInversion.FolioContratoSAAPV = row.FolioContratoSAAPV;
							DisInversion.IdInstrumento = row.IdInstrumento;
							DisInversion.Nemotecnico = row.Nemotecnico;
							DisInversion.MontoPesos = row.MontoPesos;
							DisInversion.Porcentaje = row.Porcentaje;
							DisInversion.IdEstado = row.IdEstado;
							lstInversion.add(DisInversion);
						}
					}
					if(lstInversion.size() > 0) {
						List<List<String>> ValidoDistribucionContrato = new ArrayList<List<String>>();
						ValidoDistribucionContrato = ValidaDistribucion(lstInversion);
						List<DistribucionInversion> lstInversionOK = new ArrayList<DistribucionInversion>();  
						for(int x=0; x <= ValidoDistribucionContrato.size()-1;x++) {
							if(ValidoDistribucionContrato.get(x).get(0).equals("SI")) {
								DisInversion = new DistribucionInversion(); 
								DisInversion.IdContratoSAAPV = fila.IdContratoSAAPV;
								DisInversion.FolioContratoSAAPV = ValidoDistribucionContrato.get(x).get(2);
								DisInversion.IdInstrumento = Long.valueOf(ValidoDistribucionContrato.get(x).get(3));
								DisInversion.Nemotecnico = ValidoDistribucionContrato.get(x).get(4);
								DisInversion.MontoPesos = Double.valueOf(ValidoDistribucionContrato.get(x).get(5));
								DisInversion.Porcentaje = Double.valueOf(ValidoDistribucionContrato.get(x).get(6));
								DisInversion.IdEstado = ValidoDistribucionContrato.get(x).get(7);
								lstInversionOK.add(DisInversion);
							}	
						}
						
						int[] result = sql.batch(con, SQL_CREAR_DISTRIBUCION_INVERSION, lstInversionOK, DistribucionInversion.class);
						if(result != null && result.length > 0 && result[0] == 1){
						    MensajeSalida msg = new MensajeSalida();
							msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
							msg.tipo = "OK";
							msg.codigo = "CREAR_DISTRIBUCION_OK";
							msg.descripcion = "Creacion Distribucion Inversion exitosa";
							salida.P_OUT_MENSAJES.add(msg);
						}else{
						    MensajeSalida msg = new MensajeSalida();
							msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
							msg.tipo = "ERROR";
							msg.codigo = "ERROR_CREAR_DISTRIBUCION";
							msg.descripcion = "No es posible crear Distribucion";
							salida.P_OUT_MENSAJES.add(msg);
						}
					}
				}
				
				/*INICIO -  Se crea relacion entra el Contrato SAAPV y el Portafolio*/
				//BDH
				//JALA agrega "&& fila.IdCliente != 0" para evitar crear relacion de portfolio y contratos SAAPV de clientes que no pudieron ser creados
				if(in.p_PortafoliosModificados.size() > 0 && !fila.CodigoTipoSolicitud.equals("TRA") && fila.IdCliente != 0){
				 	for(PortafoliosModificados row : in.p_PortafoliosModificados){
				 		
				 		if(fila.IdInterno == row.IdInterno) {
				 			
				 			if(row.IdPortafolio !=0 ) {
					 			//==============================================================================================
					 			//Portafolio existe, solo vincular a nuevo SAAPV
					 			//==============================================================================================
								//Se crea relacion entre el Contrato SAAPV y el Portafolio
					 			//==============================================================================================
					 			//logger.debug(ParamsContrato.toString());
								PortafolioContratoSAAPV inPortCont = new PortafolioContratoSAAPV();
								List<PortafolioContratoSAAPV> ListPortCont = new ArrayList<PortafolioContratoSAAPV>();
									inPortCont.IdContratoSAAPV = fila.IdContratoSAAPV;
									inPortCont.OrigenAhorro = row.OrigenAhorro;
									inPortCont.IdPortafolio = row.IdPortafolio;
									inPortCont.MontoPesos = row.MontoPesos;
									inPortCont.MontoUF = row.MontoUF;
									inPortCont.PorcentajeRegimenImponible = row.PorcentajeRegimenImponible;
									inPortCont.Pago = row.Pago;
									inPortCont.PeriodoInicio = row.MesInicio;
									inPortCont.PeriodoTermino = row.MesTermino;
									inPortCont.IdEstado = "VIG";
								
								ListPortCont.add(inPortCont);
	
								int[] result = sql.batch(con, SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, ListPortCont, PortafolioContratoSAAPV.class);
								
								if(result != null && result.length > 0 && result[0] == 1){
								    MensajeSalida msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "OK";
									msg.codigo = "CREAR_PORTAFOLIO_CONTRATO_OK";
									msg.descripcion = "Se crea relacion entre portafolio con contrato SAAPV.";
									salida.P_OUT_MENSAJES.add(msg);
								}else{
								    MensajeSalida msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_CREAR_PORTAFOLIO_CONTRATO";
									msg.descripcion = "No es posible crear relacion entre portafolio con contrato SAAPV.";
									salida.P_OUT_MENSAJES.add(msg);
								}
				 			}else {
				 				
				 				 MensajeSalida msg = new MensajeSalida();
									msg.id = fila.FolioContratoSAAPV == null || fila.FolioContratoSAAPV.equals("") ? null : idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_CREAR_PORTAFOLIO_CONTRATO";
									msg.descripcion = "No es posible crear relacion entre portafolio con contrato SAAPV. Debe coincidir el plan de ahorro del contrato y portafolio del cliente.";
									salida.P_OUT_MENSAJES.add(msg);
				 				
				 			}
				 		}
				 	}
				}
				/*FIN -  Se crea relacion entra el Contrato SAAPV y el Portafolio*/
			}
			
			/*INICIO - CREACION DE TRASPASO*/
			if(fila.CodigoTipoSolicitud.equals("TRA")){
				fila.FechaCreacion = (fila.FechaCreacion == null || fila.FechaCreacion.equals("")) ? fechaSistema : fila.FechaCreacion;
				List<String> Validatraspaso;
				Validatraspaso = ValidaCamposPersona("TRASPASO",fila,null);
				List<Long> IdsContratoTraspasoSAAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdTraspaso", 1L);				
				Traspaso traspasoContrato = new Traspaso();
				traspasoContrato.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
				traspasoContrato.IdContratoSAAPV = fila.TipoMovimiento.equals("INGRESO") ? fila.IdContratoSAAPV : 0;
				traspasoContrato.IdEmpresa = fila.IdEmpresa;
				traspasoContrato.TipoMovimiento = fila.TipoMovimiento;
				traspasoContrato.RazonRetiro = fila.RazonRetiro;
				traspasoContrato.FechaIngreso = fila.FechaCreacion;
				traspasoContrato.FechaEgreso = fila.TipoMovimiento.equals("INGRESO") ? "0001-01-01" : fila.FechaCreacion;
				traspasoContrato.FechaModificacion = "0001-01-01";
				traspasoContrato.CodigoTipoTraspaso = fila.TipoTraspaso;
				traspasoContrato.Descripcion = fila.Descripcion;
				traspasoContrato.IdCliente = fila.IdCliente;
				traspasoContrato.IdPersona = fila.IdPersonaOrigen;
				traspasoContrato.CodigoTipoAhorro = fila.TipoDeAhorro;	
				traspasoContrato.NombreEntidad = fila.NombreEntidadOrigen;
				traspasoContrato.DocumentoEntidad = fila.RutEntidadOrigen;
				traspasoContrato.FechaSuscripcion= fila.FechaSuscripcion;
				traspasoContrato.IdEstado = Validatraspaso.size() == 0 ? "PDI" : "TPD";
				traspasoContrato.Canal = fila.Canal;
				traspasoContrato.CierreSolicitud = "0001-01-01";
				List<Traspaso> lstTraspaso = new ArrayList<Traspaso>();
				lstTraspaso.add(traspasoContrato);
				int[] resultado = sql.batch(con, SQL_CREAR_TRASPASO,lstTraspaso, Traspaso.class);
				if(resultado != null && resultado.length > 0 && resultado[0] == 1){
					MensajeSalida msg = new MensajeSalida();
				    msg.id = IdsContratoTraspasoSAAPV.get(0);
				    msg.tipo = "OK";
				    msg.codigo = "CREACION_TRASPASO_OK";
				    msg.descripcion = "Creacion del traspaso exitosa Folio: " + IdsContratoTraspasoSAAPV.get(0).toString();
					salida.P_OUT_MENSAJES.add(msg);
					
					if(fila.TipoMovimiento.equals("EGRESO")){
						if(ApruebaTraspasoAutomatico.equals("N")) {
							//Modifico estado de traspaso a Pendiente de Autorizacion
							List<TraspasoModificar> lstTraspasoMod = new ArrayList<TraspasoModificar>();
							TraspasoModificar tm = new TraspasoModificar();
							tm.IdEstado = "PDA";
							tm.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
							lstTraspasoMod.add(tm);
							
							int[] resultadoMod = sql.batch(con, SQL_MODIFICA_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
							if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
								msg = new MensajeSalida();
								msg.id = IdsContratoTraspasoSAAPV.get(0);
								msg.tipo = "OK";
								msg.codigo = "MODIFICA_ESTADO_OK";
								msg.descripcion = "Modifica estado del Traspaso Folio: " + IdsContratoTraspasoSAAPV.get(0).toString();
								salida.P_OUT_MENSAJES.add(msg);
							}else{
								msg = new MensajeSalida();
								msg.id = IdsContratoTraspasoSAAPV.get(0);
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_MODIFICA_ESTADO";
								msg.descripcion = "Problema al modificar estado del Traspaso.";
								salida.P_OUT_MENSAJES.add(msg);
							}					
							
							CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
							List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
							ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
							evento.IdTipoEvento = "INGTRA";
							evento.IdModuloSolicita = "APV";
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = FechaOptimus;
							evento.IdTipoOperacion = null;
							evento.IdConcepto = "INGTRA";
							evento.NumeroOperacionConcepto = IdsContratoTraspasoSAAPV.get(0).toString();
							evento.FechaOperacionConcepto = FechaOptimus;
							evento.IdCliente = fila.IdCliente;
							evento.IdFuncionario = 0L;
							evento.IdProveedor = 0L;
							evento.IdTipoProducto = null;
							evento.IdClasificacionTipoInstrumento = null;
							evento.IdTipoInstrumento = null;
							evento.IdInstrumento = null;
							evento.Nemo = null;
							evento.Cantidad = null;
							evento.Monto = null;
							evento.Precio = null;
							evento.IdMoneda = "CLP";
							evento.Glosa = "INGRESO DE TRASPASO APV Folio: " + IdsContratoTraspasoSAAPV.get(0).toString();
							tablaEventos.add(evento);
							
							if (tablaEventos.size() > 0) {
								CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
								inCrearAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
								inCrearAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
								inCrearAutorizacion.P_IDMODULO = in.p_IdModulo;
								inCrearAutorizacion.P_SIMULAROPERACION = "N";
								inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
								inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos;
								crearAutorizacionService.logica(con, inCrearAutorizacion);
							}
						}else {
							//Modifico estado de traspaso
							List<TraspasoModificar> lstTraspasoMod = new ArrayList<TraspasoModificar>();
							TraspasoModificar tm = new TraspasoModificar();
							tm.IdEstado = "ING";
							tm.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
							lstTraspasoMod.add(tm);
							
							int[] resultadoMod = sql.batch(con, SQL_MODIFICA_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
							if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
								msg = new MensajeSalida();
								msg.id = IdsContratoTraspasoSAAPV.get(0);
								msg.tipo = "OK";
								msg.codigo = "MODIFICA_ESTADO_OK";
								msg.descripcion = "Modifica estado del Traspaso Folio: " + IdsContratoTraspasoSAAPV.get(0).toString();
								salida.P_OUT_MENSAJES.add(msg);
							}else{
								msg = new MensajeSalida();
								msg.id = IdsContratoTraspasoSAAPV.get(0);
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_MODIFICA_ESTADO";
								msg.descripcion = "Problema al modificar estado del Traspaso.";
								salida.P_OUT_MENSAJES.add(msg);
							}					
						}
					}
					
					DetalleOrigenTraspaso dtOrigenTraspaso = new DetalleOrigenTraspaso();
					List<DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
					for(DetalleOrigenTraspaso row : in.p_DetalleOrigenTraspaso){
						if(fila.IdInterno == row.IdInterno && !row.DocumentoEntidad.equals("")) {
							List<Long> IdsDetalleOrigenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso", 1L);
							dtOrigenTraspaso = new DetalleOrigenTraspaso();
							dtOrigenTraspaso.IdDetalleOrigenTraspaso = IdsDetalleOrigenTraspaso.get(0);
							dtOrigenTraspaso.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
							dtOrigenTraspaso.CodigoTipoAhorro = row.CodigoTipoAhorro;
							dtOrigenTraspaso.IdEntidadRecaudadora = row.IdEntidadRecaudadora;
							dtOrigenTraspaso.NombreEntidad = row.NombreEntidad;
							dtOrigenTraspaso.DocumentoEntidad = row.DocumentoEntidad;
							dtOrigenTraspaso.Fondo = row.Fondo;
							dtOrigenTraspaso.MontoTraspaso = row.MontoTraspaso;
							dtOrigenTraspaso.UFTraspaso = row.UFTraspaso;
							dtOrigenTraspaso.PorcentajeTraspaso = row.PorcentajeTraspaso;
							dtOrigenTraspaso.IdEstado = "VIG";
							dtOrigenTraspaso.IdPortafolio = row.IdPortafolio;
							dtOrigenTraspaso.IdInstrumento = row.IdInstrumento;
							dtOrigenTraspaso.CodigoRegimenTributario = row.CodigoRegimenTributario;
							lstOrigenTraspaso.add(dtOrigenTraspaso);	
						}
					};
					if(lstOrigenTraspaso.size() > 0 ) {
						int[] result = sql.batch(con, SQL_CREAR_DETALLE_ORIGEN_TRASPASO,lstOrigenTraspaso, DetalleOrigenTraspaso.class);
						if(result != null && result.length > 0 && resultado[0] == 1){
							msg = new MensajeSalida();
						    msg.id = IdsContratoTraspasoSAAPV.get(0);
						    msg.tipo = "OK";
						    msg.codigo = "CREACION_ORIGEN_TRASPASO_OK";
						    msg.descripcion = "Creacion del Origen traspaso exitosa.";
							salida.P_OUT_MENSAJES.add(msg);
						}	
					}
				}	
			}
			/*FIN - CREACION DE TRASPASO*/

			contadorIds ++;
		}
		logger.info("Fin GrabarContratoSAAPV");
		return salida;
	}
	
	private Boolean debeCerrarContratoUnico(Connection con, ContratoSAAPVID linea, List<DistribucionInversion> p_DistribucionInversion, List<PortafoliosModificados> p_PortafoliosModificados, ParametrosContrato fila, MedioRecaudacion medioRecaudacionPAC) {
		logger.info("INICIO::ProcesarDistribucion.debeCerrarContratoUnico");
		Boolean debeCerrar = true;
		
		if("DIRECTA".equalsIgnoreCase(linea.FormaDePago) && linea.IdMedioRecaudacionFormaPago.equals(fila.IdMedioRecaudacionFormaPago) && !fila.IdMedioRecaudacionFormaPago.equals(medioRecaudacionPAC.IdMedioRecaudacion)) {
			debeCerrar = false;
		}
		
		logger.info("FIN::ProcesarDistribucion.debeCerrarContratoUnico");
		return debeCerrar;
	}

	private Boolean debeCerrarContratoIndefinido(Connection con, ContratoSAAPVID contratoExistente, List<DistribucionInversion> distribucionEntrada, List<PortafoliosModificados> portafolios, GrabarContratoSAAPV.ParametrosContrato fila) throws Exception {
		logger.info("INICIO::ProcesarDistribucion.debeCerrarContratoIndefinido");
		Boolean debeCerrar = false;
		
		SQL sql = new SQL();
		HashMap<String,Object> params =  new HashMap<>(1);
		params.put("IdContratoSAAPV", contratoExistente.IdContratoSAAPV);
		List<PortafoliosSAAPV> portafoliosPorContratoExistente = sql.query(con, SQL_CONSULTA_CONTRATOSAAPV_PORTAFOLIOS, params, PortafoliosSAAPV.class);
		
		if(contratosTienenMismosPortafolios(portafolios, portafoliosPorContratoExistente) && contratoExistente.FormaDePago.equals(fila.FormaDePago) && contratoExistente.IdEmpleador.equals(fila.IdEmpleador)) {
			debeCerrar = true;
		}
		
		logger.info("FIN::ProcesarDistribucion.debeCerrarContratoIndefinido");
		return debeCerrar;
	}

	private Boolean contratosTienenMismosPortafolios(List<PortafoliosModificados> portafolios, List<PortafoliosSAAPV> portafoliosPorContratoExistente) {
		logger.info("INICIO::ProcesarDistribucion.contratosTienenMismosPortafolios");
		Boolean mismosPortafolios = true;
		
		if(portafoliosPorContratoExistente.size() != portafolios.size()) {
			mismosPortafolios = !mismosPortafolios;
		} else {
			for(PortafoliosModificados portafolio : portafolios){
				if(portafoliosPorContratoExistente.stream().filter(pExiste -> portafolio.IdPortafolio.equals(pExiste.IdPortafolio) && portafolio.Regimen.equals(pExiste.Regimen)).count() == 0L) {
					mismosPortafolios = !mismosPortafolios;
					break;
				}
			}
		}
		
		logger.info("FIN::ProcesarDistribucion.contratosTienenMismosPortafolios");
		return mismosPortafolios;
	}

	public List<List<String>> ValidaDistribucion(List<DistribucionInversion> distriInstrumentos) {

		List<List<String>> InstrumentoValidos = new ArrayList<List<String>>();
		int contador=0;
		for(DistribucionInversion fila: distriInstrumentos){
			InstrumentoValidos.add(new ArrayList<String>());
			InstrumentoValidos.get(contador).add("SI");
			InstrumentoValidos.get(contador).add(fila.IdContratoSAAPV.toString());
			InstrumentoValidos.get(contador).add(fila.FolioContratoSAAPV);
			InstrumentoValidos.get(contador).add(fila.IdInstrumento.toString());
			InstrumentoValidos.get(contador).add(fila.Nemotecnico);
			InstrumentoValidos.get(contador).add(String.valueOf(fila.MontoPesos));
			InstrumentoValidos.get(contador).add(String.valueOf(fila.Porcentaje));
			InstrumentoValidos.get(contador).add(fila.IdEstado);
			contador++;
		}
		return InstrumentoValidos;
	}
	
	//METODO VALIDA CUMPLE REQUISITOS MINIMOS PARA SER PERSONA,CLIENTE
	public List<String> ValidaCamposPersona(String TipoValidacion,ParametrosContrato Contrato,PortafoliosModificados Portafolio )
	{
		List<String> lstDatosFaltantes = new ArrayList<String>();
		if(TipoValidacion.equals("CLIENTE") || TipoValidacion.equals("TRASPASO"))
		{	//Datos Minimos para Crear Cliente
			if(Contrato.DocumentoIdentidadCliente == null || Contrato.DocumentoIdentidadCliente.equals(""))
				lstDatosFaltantes.add("Documento Identidad Cliente.");
			if(Contrato.NombreCliente == null || Contrato.NombreCliente.equals(""))
				lstDatosFaltantes.add("Nombre Cliente.");				
			if(Contrato.ApellidoPaternoCliente == null || Contrato.ApellidoPaternoCliente.equals(""))
				lstDatosFaltantes.add("Apellido Paterno Cliente."); 
			if(Contrato.IdPaisResidenciaCliente == null || Contrato.IdPaisResidenciaCliente == 0)
				lstDatosFaltantes.add("Pais Residencia Cliente.");
			if(Contrato.EstadoCivil == null || Contrato.EstadoCivil.equals(""))
				lstDatosFaltantes.add("Estado Civil Cliente.");
			if(Contrato.FechaNacimientoCliente == null || Contrato.FechaNacimientoCliente.equals(""))
				lstDatosFaltantes.add("Fecha Nacimiento Cliente."); 
			if(Contrato.IdTipoGenero == null || Contrato.IdTipoGenero.equals(""))
				lstDatosFaltantes.add("Genero Cliente."); 
			if(Contrato.DireccionCliente == null || Contrato.DireccionCliente.equals(""))
				lstDatosFaltantes.add("Direccion Cliente."); 
			if(Contrato.NumeroDireccionCliente == null || Contrato.NumeroDireccionCliente.equals(""))
				lstDatosFaltantes.add("Numero Direccion Cliente."); 
			if((Contrato.TelefonoLaboral == null || Contrato.TelefonoLaboral.equals("")) && 
					(Contrato.TelefonoFijo == null || Contrato.TelefonoFijo.equals("")) &&
						(Contrato.TelefonoMovil == null || Contrato.TelefonoMovil.equals("")))
					lstDatosFaltantes.add("Telefono(s) Cliente.");
			if(Contrato.EmailCliente == null || Contrato.EmailCliente.equals(""))
				lstDatosFaltantes.add("Email Cliente."); 
			if(Contrato.IdCategoriaInversionista == null || Contrato.IdCategoriaInversionista.equals(""))
				lstDatosFaltantes.add("Categoria Inversionista Cliente.");
			if(Contrato.IdPerfilComercial == null || Contrato.IdPerfilComercial.equals(""))
				lstDatosFaltantes.add("Perfil Comercial Cliente.");
			if(Contrato.IdEmpleado == null || Contrato.IdEmpleado == 0)
				lstDatosFaltantes.add("Ejecutivo Cliente.");
			/*
			if(fila.IdTipoCliente != "")
				lstDatosFaltantes.add("Tipo Cliente.");
			*/ 
		}
		
		if(TipoValidacion.equals("DATOS_MINIMOS"))
		{
			if(Contrato.FolioContratoSAAPV == null || Contrato.FolioContratoSAAPV.equals(""))
				lstDatosFaltantes.add("Folio Contrato SAAPV.");
			if(Contrato.NombrePlan == null || Contrato.NombrePlan.equals(""))
				lstDatosFaltantes.add("Nombre de Plan SAAPV.");
			if(Contrato.FechaSuscripcion == null || Contrato.FechaSuscripcion.equals(""))
				lstDatosFaltantes.add("Fecha Suscripcion.");
			if(Contrato.NombreCliente == null || Contrato.NombreCliente.equals(""))
				lstDatosFaltantes.add("Nombre Cliente.");
			if(Contrato.ApellidoPaternoCliente == null || Contrato.ApellidoPaternoCliente.equals(""))
				lstDatosFaltantes.add("Apellido Paterno Cliente.");
			if(Contrato.DocumentoIdentidadCliente == null || Contrato.DocumentoIdentidadCliente.equals(""))
					lstDatosFaltantes.add("Documento Identidad Cliente.");
			if(Contrato.DireccionCliente == null || Contrato.DireccionCliente.equals(""))
				lstDatosFaltantes.add("Direccion Cliente.");
			if(Contrato.NumeroDireccionCliente == null || Contrato.NumeroDireccionCliente.equals(""))
				lstDatosFaltantes.add("Numero Direccion Cliente.");
			if(Contrato.IdPaisResidenciaCliente == null || Contrato.IdPaisResidenciaCliente == 0)
				lstDatosFaltantes.add("Id Pais.");
			if(Contrato.IdComunaCliente == null || Contrato.IdComunaCliente == 0 && Contrato.IdPaisResidenciaCliente == 44)
				lstDatosFaltantes.add("Id Comuna.");
			if(Contrato.IdCiudadCliente == null || Contrato.IdCiudadCliente == 0)
				lstDatosFaltantes.add("Id Ciudad.");
			if(Contrato.IdPerfilComercial == null || Contrato.IdPerfilComercial.equals(""))
				lstDatosFaltantes.add("Perfil Comercial.");
			
			if(Contrato.CodigoTipoSolicitud.equals("") || (Contrato.CodigoTipoSolicitud.equals("TRA") &&  Contrato.TipoMovimiento.equals("INGRESO"))) {
				if(Contrato.Trabajador == null || Contrato.Trabajador.equals("")) {
					lstDatosFaltantes.add("Tipo Trabajador.");
				}else if(Contrato.Trabajador.equals("DEPENDIENTE"))
				{
					if(Contrato.RazonSocialEmpleador == null || Contrato.RazonSocialEmpleador.equals(""))
						lstDatosFaltantes.add("Razon Social.");
					if(Contrato.DocumentoIdentidadEmpleador == null || Contrato.DocumentoIdentidadEmpleador.equals(""))
							lstDatosFaltantes.add("Documento Identidad Empleador.");
					if(Contrato.DireccionComercialEmpleador == null || Contrato.DireccionComercialEmpleador.equals(""))
						lstDatosFaltantes.add("Direccion Comercial Empleador.");
					if(Contrato.NumeroDireccionEmpleador == null || Contrato.NumeroDireccionEmpleador.equals(""))
						lstDatosFaltantes.add("Numero Direccion Empleador.");	
				}
				if(Contrato.Pensionado == null || Contrato.Pensionado.equals(""))
					lstDatosFaltantes.add("Pensionado.");	
			}
			if(!Contrato.CodigoTipoSolicitud.equals("TRA")) {
				if(Portafolio != null){
					if(Portafolio.OrigenAhorro == null || Portafolio.OrigenAhorro.equals(""))
						lstDatosFaltantes.add("Origen Ahorro.");
					if(Portafolio.PagoNombre == null || Portafolio.PagoNombre.equals(""))
						lstDatosFaltantes.add("Tipo de Pago.");
					if(Portafolio.Producto == null || Portafolio.Producto.equals(""))
						lstDatosFaltantes.add("Producto.");
					if(Portafolio.Regimen == null || Portafolio.Regimen.equals(""))
						lstDatosFaltantes.add("Regimen.");
					if(Portafolio.MontoPesos == 0 && Portafolio.MontoUF == 0)
						lstDatosFaltantes.add("Monto Portafolio.");
					if(Portafolio.MesInicio == null || Portafolio.MesInicio.equals(""))
						lstDatosFaltantes.add("Mes Inicio.");
				}
				else {
					lstDatosFaltantes.add("Portafolio.");
				}	
			}
		}
		
		return lstDatosFaltantes;
	}
	
	public void AgregarLogSAAPV(Connection con, ParametrosEntradaServicio in, String opcion) {
		try {
			
			String IdTipoLog;
			String NombreProceso;
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
			Long IdCliente = in.p_Parametros.get(0).IdCliente;
			String FolioContratoSAAPV = in.p_Parametros.get(0).FolioContratoSAAPV;
			String FechaCreacion = in.p_Parametros.get(0).FechaCreacion;
			String IdEstado = in.p_Parametros.get(0).IdEstado;
			Long IdPersona = in.p_Parametros.get(0).IdPersona;
			Long IdCabeceraLog = (IdCliente == 0) ? IdPersona : IdCliente;
			String IdEstadoEnvioNotificacion= in.p_Parametros.get(0).IdEstadoEnvioNotificacion;
			String DocumentoIdentidadEmpleador = in.p_Parametros.get(0).DocumentoIdentidadEmpleador;
			
			{
				//CABECERA
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
				log.IdCabeceraLog = IdCabeceraLog;
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = 0L;
				log.IdCliente = IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CONTRATO-SAAPV";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "ING";
				
				if (IdCliente == 0) {
					log.IdPersona = IdPersona;
				}
				
				// DETALLE DEL LOG
				if (opcion == "ELIMINAR") {
					IdTipoLog = "ELIMINACION";
					NombreProceso = "Eliminacion Contrato SAAPV";
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "FolioContratoSAAPV", FolioContratoSAAPV));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Descripcion", "Se elimina contrato anterior"));
					
				} else if (opcion == "CERRAR") {
					IdTipoLog = "MODIFICACION";
					NombreProceso = "Cambio Estado Contrato SAAPV";
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "FolioContratoSAAPV", FolioContratoSAAPV));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Descripcion", "Se cierra el contrato anterior"));
					
				} else {				
					IdTipoLog = "INGRESO";
					NombreProceso = "Ingresar Contrato SAAPV";
					
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "FolioContratoSAAPV", FolioContratoSAAPV));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "FechaCreacion", FechaCreacion));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdEstado", IdEstado));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdEstadoEnvioNotificacion", IdEstadoEnvioNotificacion));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "DocumentoIdentidadEmpleador", DocumentoIdentidadEmpleador));
				}
				
				log.IdTipoLog = IdTipoLog;
				log.NombreProceso = NombreProceso;
				entradaAgregarLog.p_Log.add(log);

				//REGISTRO
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public  InAgregarLog.DetalleLog agregarDetalleLogContratoSAAPV(Long IdCabeceraLog, String CampoRegistro, String ValorNuevo) {
		InAgregarLog.DetalleLog DetalleContratoSAAPV = new InAgregarLog.DetalleLog();
		DetalleContratoSAAPV.IdCabeceraLog = IdCabeceraLog; 
		DetalleContratoSAAPV.IdTrabajo = 0L;
		DetalleContratoSAAPV.SubRegistro = "CONTRATO-SAAPV"; 
		DetalleContratoSAAPV.CampoRegistro = CampoRegistro; 
		DetalleContratoSAAPV.ValorAntiguo = ""; 
		DetalleContratoSAAPV.ValorNuevo = ValorNuevo;
		return DetalleContratoSAAPV;
	}
	
	private List<EstadoCliente> obtenerEstadoCliente (Connection con, SQL sql, List<String> listaDocumentoIdentidad) throws SQLException{
		logger.debug("INICIO::GrabarContratoSAAPV.obtenerEstadoCliente");
		long inicio = System.currentTimeMillis();
		
		List<EstadoCliente> listaEstadoCliente = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>();
		params.put("documentoIdentidad", listaDocumentoIdentidad);
		listaEstadoCliente = sql.query(con, SQL_SELECT_ESTADO_CLIENTE, params,EstadoCliente.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GrabarContratoSAAPV.obtenerEstadoCliente", fin - inicio);
		
		return listaEstadoCliente;
	}
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
				//Long time = System.currentTimeMillis();
				//java.sql.Date FechaSistema = Fecha.toDate("1978-01-01T03:00:00.000Z");
				//Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());

				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":66,\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-21\",\"FechaCreacion\":\"2018-02-22\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdPersona\":null,\"IdCliente\":null,\"IdEmpleador\":0,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"JAIME ALEXIS\",\"ApellidoPaternoCliente\":\"ARAVENA\",\"ApellidoMaternoCliente\":\"GONZALEZ\",\"DocumentoIdentidadCliente\":\"113459198-6\",\"FechaNacimientoCliente\":\"1979-10-06\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdComunaCliente\":null,\"DireccionCliente\":null,\"NumeroDireccionCliente\":null,\"IdTipoCargoPersona\":null,\"TelefonoCliente\":null,\"TelefonoComercialCliente\":null,\"TelefonoCelularCliente\":null,\"EmailCliente\":\"PRUEBA_CORREO@GMAIL.COM\",\"RazonSocialEmpleador\":\"SAVORY\",\"DocumentoIdentidadEmpleador\":\"80537000-5\",\"IdPaisEmpleador\":0,\"DireccionComercialEmpleador\":null,\"NumeroDireccionEmpleador\":null,\"IdCiudadEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"562239968900\",\"NombreEncargado\":null,\"EmailEncargado\":null,\"TelefonoEncargado\":null,\"IdMedioRecaudacionTrabajador\":1,\"IdEntidadRecaudadora\":0,\"CodigoPrevision\":0,\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":1,\"Institucion\":0,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_ParametrosModificados\":0}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":150,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"\",\"IdPlan\":0,\"NombrePlan\":\"\",\"FechaSuscripcion\":\"2019-09-03\",\"FechaCreacion\":\"2019-09-03\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"CodigoTipoSolicitud\":\"TRA\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdPersona\":0,\"IdCliente\":0,\"IdEmpleador\":0,\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"PRUEBA\",\"ApellidoPaternoCliente\":\"4\",\"ApellidoMaternoCliente\":\"CICLO 2\",\"DocumentoIdentidadCliente\":\"11886711-4\",\"FechaNacimientoCliente\":\"1990-02-15\",\"EstadoCivil\":\"SO\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2931,\"IdComunaCliente\":299,\"DireccionCliente\":\"APOQUINDO\",\"NumeroDireccionCliente\":\"4820\",\"IdTipoCargoPersona\":1,\"TelefonoFijo\":\"+56229152324\",\"TelefonoMovil\":\"\",\"TelefonoLaboral\":\"\",\"EmailCliente\":\"PRUEBA4@SURA.CL\",\"RazonSocialEmpleador\":\"\",\"DocumentoIdentidadEmpleador\":\"\",\"IdPaisEmpleador\":0,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":0,\"IdCiudadEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"\",\"NombreEncargado\":\"\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":0,\"IdEntidadRecaudadora\":0,\"CodigoPrevision\":0,\"Pensionado\":\"\",\"Trabajador\":\"\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"INDIRECTA\",\"DiaDescuento\":0,\"Institucion\":0,\"ComisionTransferencia\":0,\"TipoMovimiento\":\"EGRESO\",\"RazonRetiro\":false,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":0,\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"Descripcion\":\"\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":55,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_ParametrosModificados\":0,\"p_DetalleOrigenTraspaso\":[]}";
				String jsonOPT = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":2025061803,\"IdPlan\":2,\"NombrePlan\":\"I. ACTIVO\",\"FechaSuscripcion\":\"2025-05-27\",\"FechaCreacion\":\"2025-05-27\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"CodigoTipoSolicitud\":\"\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"NO APV_OPCION 1_ARRIESGADO\",\"IdPersona\":0,\"IdCliente\":0,\"IdEmpleador\":0,\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"MACARENA SUSANA\",\"ApellidoPaternoCliente\":\"MANCILLA\",\"ApellidoMaternoCliente\":\"LOPEZ\",\"DocumentoIdentidadCliente\":\"16657034-4\",\"FechaNacimientoCliente\":\"1987-02-18\",\"EstadoCivil\":\"SO\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2931,\"IdComunaCliente\":327,\"DireccionCliente\":\"SANTA ISABEL\",\"NumeroDireccionCliente\":\"431\",\"IdTipoCargoPersona\":189,\"TelefonoFijo\":\"\",\"TelefonoMovil\":\"2131231273\",\"TelefonoLaboral\":\"\",\"EmailCliente\":\"CARLOS.SILVA@NUAMX.COM\",\"RazonSocialEmpleador\":\"CORREDORES DE BOLSA SURA S. A.\",\"DocumentoIdentidadEmpleador\":\"76011193-7\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"APOQUINDO\",\"NumeroDireccionEmpleador\":\"4820\",\"IdCiudadEmpleador\":2931,\"IdComunaEmpleador\":299,\"TelefonoEmpleador\":\"2222222\",\"NombreEncargado\":\"NOMBRE POR DEFINIR\",\"EmailEncargado\":\"MAIL POR DEFINIR\",\"TelefonoEncargado\":\"TELEFONO POR DEFINIR\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":827,\"CodigoPrevision\":\"827\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"APV\",\"MontoPesos\":10000,\"Pago\":\"IND\",\"PeriodoInicio\":\"2025-06\",\"PeriodoTermino\":\"9999-01\",\"RegimenTributario\":\"APV-A\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":2,\"DiaDescuento\":null,\"Institucion\":0,\"ComisionTransferencia\":0,\"TipoMovimiento\":\"\",\"RazonRetiro\":\"\",\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":0,\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"Descripcion\":\"\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":839,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdMatrizAgrupadorAPV\":8,\"CodigoAgrupador\":\"GRUPO 2\",\"Producto\":\"APV Acciones\"}],\"p_DistribucionInversion\":[{\"FolioContratoSAAPV\":2025061803,\"IdInstrumento\":910288,\"RazonSocial\":\"SURA Renta Bonos Chile D\",\"Nemotecnico\":\"00039DCLP\",\"MontoPesos\":10000,\"Porcentaje\":100,\"IdEstado\":\"VIG\"}],\"p_ParametrosModificados\":0,\"p_DetalleOrigenTraspaso\":[],\"Canal\":\"OPT\",\"p_PortafoliosModificados\":[{\"CodigoAgrupador\":\"GRUPO 2\",\"OrigenAhorro\":\"APV\",\"IdMatrizAgrupadorAPV\":8,\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2025-06\",\"MesTermino\":\"9999-01\",\"MontoPesos\":10000,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Regimen\":\"APV-A\",\"Producto\":\"APV Acciones\"}]}";
				ParametrosEntradaServicio Objin_json = gson.fromJson(jsonOPT, ParametrosEntradaServicio.class);
				
				GrabarContratoSAAPV demo = new GrabarContratoSAAPV();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					SQL.exec(con, "DROP TABLE #RegistroAtributo");
					con.rollback();
				}
	 			
		} catch (Exception e) {

			e.printStackTrace();			
		}	
			System.out.println("== END ==");
		};
	}

}