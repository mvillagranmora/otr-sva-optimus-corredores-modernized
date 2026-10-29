package cl.bcs.APV;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV;
import cl.bcs.APV.ValidaContratoSAAPV;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
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
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GrabarContratoTraspasoSAAPV implements JavaCall   {

	/*
	 * El log
	 */	
	static final Logger logger = LogManager.getLogger(GrabarContratoTraspasoSAAPV.class);

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
	public String SQL_CONSULTAR_DIRECCION_FISICA = loadResourceToString("cl/bcs/APV/sql/ConsultarDireccionFisica.sql");
	public String SQL_DIRECCION_PROPOSITO_APV 	 = loadResourceToString("cl/bcs/APV/sql/CreaDireccionPropositoAPV.sql");
	public String SQL_CAMBIAR_CARGO_PERSONA 	 = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaActividaCargo_Update.sql");
	public String SQL_CREAR_TRASPASO			 = loadResourceToString("cl/bcs/APV/sql/Traspaso_Insert.sql");
	public String SQL_CREAR_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/DetalleOrigenTraspaso_Insert.sql");

	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosContrato{
		
		public  Long		IdContratoSAAPV;
		public	Long		IdEmpresa;
		public  String		FolioContratoSAAPV;
		public  Long		IdPlan;
	    public 	String		NombrePlan;
	    public  String      TipoMovimiento;
	    public  String		RazonRetiro;
	    public  String		FechaSuscripcion; 		//Date
	    public  String		FechaCreacion; 			//Date
	    public  String		FechaAprobacion; 
	    public  String		FechaNotificacion; 		//Date
	    public  String		FechaRecepcion;			//Date 
	    public  String		Folio;
	    public  Long		IdPersona; 
	    public  Long		IdEmpleador; 
	    public  Long		IdCliente;
	    public  String      CodigoTipoSolicitud;
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
	    public	String		NombreEntidad;
	    public	String		DocumentoEntidad;
	    public  Long        IdPersonaOrigen;
	    public	String		TipoDeAhorro;
	    public	String		FondoDeOrigen;
	    public	String		TipoTraspaso;
	    public	double		MontoTraspaso;
	    public	double		UFTraspaso;
	    public 	double		PorcentajeTraspaso;
	    public  String 		Descripcion;
	    public  String		IdEstadoEnvioNotificacion;
	    public  String		IdEstadoSolicitud;
	    public 	String		IdEstado;
	    public  Long		IdCargaMasiva;
	    public 	Long		IdEmpleado;	
	    public  String      IdPropositoPortafolio;
	    public 	Long		IdMatrizAgrupadorAPV;
	    public  String      CodigoAgrupador;
	    public  String      Canal;
	};
	
	public static class ContratoSAAPV{
		
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
	    public  String      CodigoTipoSolicitud;
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
	    public 	String 		IdCategoriaInversionista;
	    public	String		IdPerfilComercial;
	    public  Long		IdCargaMasiva;
	};
	
	public static class DistribucionInversion {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public Long IdInstrumento;
		public String Nemotecnico;
		public double MontoPesos;
		public double Porcentaje;
		public String IdEstado;
	};

	public static class PortafoliosModificados {
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
	};
	
	public static class Traspaso{
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
	};
		
	public static class DetalleOrigenTraspaso{
		public Long   IdDetalleOrigenTraspaso;
		public Long   IdTraspaso;
		public String CodigoTipoAhorro;
		public String NombreEntidadOrigen;
		public String DocumentoEntidadOrigen;
		public String FondoOrigen;
		public double MontoTotalTraspaso;
		public double UFTotalTraspaso;
		public double PorcentajeTotalTraspaso;
		public String IdEstado;
	};
	

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	String						p_Tipo; //Individual Manual o Por Servicio
		//$scope.$root.SesionIdPaisSistema
		public List<ParametrosContrato>		p_Parametros;
		public List<DistribucionInversion> 	p_DistribucionInversion;
		//public List<PortafoliosModificados> p_PortafoliosModificados;
		public List<DetalleOrigenTraspaso> p_DetalleOrigenTraspaso;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<ParametrosContrato>();	
			p_DistribucionInversion = new ArrayList<DistribucionInversion>();	
			//p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();
			p_DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};	

	public static class AgrupadorPortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgrupador;
		public String NombreAgrupador;
		public String CodigoAgrupador;
	};
	
	public static class PortafolioContratoSAAPV{
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
	
	public static class ConsultaPortafolioDisponible {
		public  Long		IdPersona;
	    public 	String		Codigo;
	    public  Long		IdPortafolio;
	    public  Long		IdCliente;
	    public  String		NombreAgrupador;
	    public  Long		IdMatrizAgrupadorAPV;
	    public  String		CodigoAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class IndicadorEncargadoRRHH{
		public Long 		IdIndicador;	
		public Long			IdTipoIndicador; 
		public String 		ValorIndicador;
		public Date			FechaVencimiento;
	};	
	
	public static class SecuenciaIndicador{
		public Long			IdSecuencia;
	};
	
	public static class TipoIndicador{
		public Long			IdTipoIndicador;
		public String		CodigoTipoIndicador;
	};
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class  ConsultaDireccion{
		public Long IdDireccionFisica;
		
	}

	public static class ConsultaRelacionPersona{
		public Long IdRelacionPersona; 
		
	}
	
	public static class CambiarCargoPersona
	{
		public  Long	IdPersona;
		public  Long	IdTipoCargoPersona;
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
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
				
		SQL sql = new SQL();
		List<Long> idsContratoSAAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::SCS.IdContratoSAAPV", (long)in.p_Parametros.size());
		int contadorIds = 0;
		boolean ExisteFolioContrato;
		
		for(GrabarContratoTraspasoSAAPV.ParametrosContrato fila : in.p_Parametros){
			
			List<String> resultadoValidacion = null;
			fila.IdContratoSAAPV = idsContratoSAAPV.get(contadorIds);
			ExisteFolioContrato = false;

			String CLI_IdTipoClienteDefecto = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.ContratoCLI_IdTipoClienteDefecto");
			String FechaFinRelacionDefecto = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.ContratoFechaFinRelacionDefecto");
			//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
			
			Long time = System.currentTimeMillis();
			java.sql.Date FechaSistema = new java.sql.Date(time);
			
			Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
				
			//*** VALIDACION CONTRATOS SAAPV ***
			//valida si existe Persona, Cliente, Empleador y Portafolio
			
			ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada inValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
			ValidaContratoSAAPV.ValidaContratoSAAPV_Salida outValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Salida();
			List<ValidaContratoSAAPV.ContratosSAAPV> tablaValidaContrato = new ArrayList<ValidaContratoSAAPV.ContratosSAAPV>();
			
			//Este IF se puso para que se haga la validacion de contratos aunque no existan portafolios 
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

					if(!filaValidaContrato.FolioContratoSAAPV.equals("")){
						ExisteFolioContrato = true;
					}	
				}
				logger.debug("ValidaContratoSAAPV OK");
			}else{
			    MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_VALIDAR_PERSONA";
				msg.descripcion = "Error al validar persona contrato.";
				salida.P_OUT_MENSAJES.add(msg);				
			}
			
			if(fila.IdPersona == null) fila.IdPersona = 0L;
			if(fila.IdCliente == null) fila.IdCliente = 0L;
			if(fila.IdEmpleador == null) fila.IdEmpleador = 0L;
			if(fila.DocumentoIdentidadEmpleador == null) fila.DocumentoIdentidadEmpleador = "";
			
			/*En caso de que el FolioContrato Exista en el Ingreso se debe avisar que ya existe y continuar con el ciclo en el Caso de la Carga Masiva*/
			if(ExisteFolioContrato) {
				MensajeSalida msg = new MensajeSalida();
				msg.id = Long.parseLong(fila.FolioContratoSAAPV);
				msg.tipo = "ADVERTENCIA";
				msg.codigo = "FOLIO_CONTRATO_EXISTE";
				msg.descripcion = "El Folio Contrato SAAPV ya existe.";
				salida.P_OUT_MENSAJES.add(msg);				
				continue;
			}
			if(fila.TipoMovimiento.equals("INGRESO")){
				//*** VALIDACION CONTRATOS SAAPV - FIN ***
				if(fila.IdPersona == null || fila.IdPersona == 0){ 
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
					if(!fila.TelefonoLaboral.equals("")){				
						enr.IdTipoTelefono						=	"OTRO";
						enr.Telefono							= 	fila.TelefonoLaboral;
					}else if(!fila.TelefonoFijo.equals("") && fila.TelefonoLaboral.equals("")){
						enr.IdTipoTelefono						=	"FIJO";
						enr.Telefono							= 	fila.TelefonoFijo;	
					}else if(fila.TelefonoLaboral.equals("") && fila.TelefonoFijo.equals("")){
						enr.IdTipoTelefono					=	"MOVIL";
						enr.Telefono						= 	fila.TelefonoMovil;
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
					    
					    HashMap<String,Object> paramConsulta = new HashMap<>();
						paramConsulta.put("IdPersona", fila.IdPersona);
						List<ConsultaDireccion> ConsultaDireccion = sql.query(con, SQL_CONSULTAR_DIRECCION_FISICA,paramConsulta, ConsultaDireccion.class);
						//SQL_DIRECCION_PROPOSITO_APV
						//logger.debug(ConsultaDireccion.get(0));
						sql.batch(con, SQL_DIRECCION_PROPOSITO_APV,ConsultaDireccion, ConsultaDireccion.class);		   			  
						//logger.debug(resUpdate);
						
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
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "OK";
								msg.codigo = "CAMBIAR_CARGO_PERSONA_OK";
								msg.descripcion = "Modificacion de cargo persona exitosa.";
								logger.debug(msg);
							}else {
								msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = "CAMBIAR_CARGO_PERSONA_ERROR";
								msg.descripcion = "Modificacion de cargo persona con problemas.";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
						
						//MOVIL
						if(fila.TelefonoLaboral.equals("") == false)
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
										msg.id = idsContratoSAAPV.get(contadorIds);
										msg.tipo = "ERROR";
										msg.codigo = "ERROR_CREAR_TELEFONO";
										msg.descripcion = "Error al crear Telefono Particular";
										salida.P_OUT_MENSAJES.add(msg);
									}
								}	
						   }
					   }
					   
					   //PARTICULAR
					   if(fila.TelefonoFijo.equals("") == false)
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
										msg.id = idsContratoSAAPV.get(contadorIds);
										msg.tipo = "ERROR";
										msg.codigo = "ERROR_CREAR_TELEFONO";
										msg.descripcion = "Error al crear Telefono Particular";
										salida.P_OUT_MENSAJES.add(msg);
									}
								}	
						   }
					   	}
					   
					   	if(fila.TelefonoMovil.equals("") == false){
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
										msg.id = idsContratoSAAPV.get(contadorIds);
										msg.tipo = "ERROR";
										msg.codigo = "ERROR_CREAR_TELEFONO";
										msg.descripcion = "Error al crear Telefono Comercial";
										salida.P_OUT_MENSAJES.add(msg);
									}
								}	
						   }
					   	}
					   
					   
					   	if(fila.EmailCliente.equals("") == false){
						   
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
										msg.id = idsContratoSAAPV.get(contadorIds);
										msg.tipo = "ERROR";
										msg.codigo = "ERROR_CREAR_EMAIL";
										msg.descripcion = "Error al crear email Cliente";
										salida.P_OUT_MENSAJES.add(msg);
									}
								}	
						   }
					   	}
					    MensajeSalida msg = new MensajeSalida();
						msg.id = idsContratoSAAPV.get(contadorIds);
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
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "La Persona ya Existe.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
							    msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "Error al crear persona";
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
					if(!fila.TelefonoLaboral.equals("")){				
						enr.IdTipoTelefono						=	"OTRO";
						enr.Telefono							= 	fila.TelefonoLaboral;
					}else if(!fila.TelefonoFijo.equals("") && fila.TelefonoLaboral.equals("")){
						enr.IdTipoTelefono						=	"FIJO";
						enr.Telefono							= 	fila.TelefonoFijo;	
					}else if(fila.TelefonoLaboral.equals("") && fila.TelefonoFijo.equals("")){
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
						msg.id = fila.IdEmpleador;
						msg.tipo = "OK";
						msg.codigo = "CREACION_PERSONA_OK";
						msg.descripcion = "Creacion de persona exitosa";

						List<IndicadorEncargadoRRHH> lstEncargado = new ArrayList<IndicadorEncargadoRRHH>();
						HashMap<String, Object> params = new HashMap<>();
						params.put("IdTipoEntidad", "PERSONA");
						params.put("GrupoIndicador", "DATOS EMPLEADOR");
						List<TipoIndicador> lstTipoIndicador = sql.query(con, SQL_CONSULTAR_RELACIONADO_RRHH, params, TipoIndicador.class);
						boolean existeTipoIndicador = (lstTipoIndicador.isEmpty()) ? false : true;
						if(existeTipoIndicador){
							for(TipoIndicador tp :lstTipoIndicador){
								IndicadorEncargadoRRHH		EncargadoRRHH	= new IndicadorEncargadoRRHH();
								EncargadoRRHH.IdIndicador = 0L;
								EncargadoRRHH.IdTipoIndicador = tp.IdTipoIndicador;
								if(tp.CodigoTipoIndicador.compareTo("NOMBRE_ENCARGADO_RRHH") == 0)								
									EncargadoRRHH.ValorIndicador =	fila.NombreEncargado;
								else if(tp.CodigoTipoIndicador.compareTo("MAIL_ENCARGADO_RRHH") == 0)
									EncargadoRRHH.ValorIndicador =	fila.EmailEncargado;
								else if(tp.CodigoTipoIndicador.compareTo("TELEFONO_ENCARGADO_RRHH") == 0)
									EncargadoRRHH.ValorIndicador = 	fila.TelefonoEncargado;
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
								msg.descripcion = "Error al ingresar encargado de recursos humanos.";
								salida.P_OUT_MENSAJES.add(msg);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
								msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ADVERTENCIA_ENCARGADO_RRHH";
								msg.descripcion = "Error al ingresar encargado de recursos humanos.";
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
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "Persona Empleador ya existe.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
							    msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_CREAR_PERSONA";
								msg.descripcion = "Error al crear persona Empleador";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
					}
				}
				resultadoValidacion = ValidaCamposPersona("CLIENTE",fila,null);
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
						if(!fila.TelefonoMovil.equals("")) {
							enr.IdTipoTelefono						=	"MOVIL";
							enr.Telefono							= 	fila.TelefonoMovil;
						}else if(fila.TelefonoMovil.equals("") && !fila.TelefonoFijo.equals("")) {
							enr.IdTipoTelefono						=	"FIJO";
							enr.Telefono							= 	fila.TelefonoFijo;
						}else if(fila.TelefonoMovil.equals("") && fila.TelefonoFijo.equals("") && !fila.TelefonoLaboral.equals("")) {
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
							msg.id = idsContratoSAAPV.get(contadorIds);
							msg.tipo = "OK";
							msg.codigo = "CREACION_CLIENTE_OK";
							msg.descripcion = "Creacion de cliente exitosa";
							salida.P_OUT_MENSAJES.add(msg);
						}
						MensajeSalida msg;
						if(SalidaCliente.P_OUT_ERRORESCONTADOR > 0){
							for (ErrorDetalle lineaError : SalidaCliente.P_OUT_ERRORESDETALLE) {
								msg = new MensajeSalida();
								if (lineaError.Codigo.compareTo("CLIENTE_EXISTE") == 0){
									logger.debug("CLIENTE_EXISTE");
									msg.id = idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "ERROR_CREAR_CLIENTE";
									msg.descripcion = "CLIENTE_EXISTE";
									salida.P_OUT_MENSAJES.add(msg);
								}else if(lineaError.Codigo.compareTo("PERFIL_COMERCIAL_NO_EXISTE") == 0){
									logger.debug("PERFIL_COMERCIAL_NO_EXISTE");
									msg.id = idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ADVERTENCIA";
									msg.codigo = "ERROR_CREAR_PERFIL_CLIENTE";
									msg.descripcion = "PERFIL_COMERCIAL_NO_EXISTE";
									salida.P_OUT_MENSAJES.add(msg);
								}else{
									msg.id = idsContratoSAAPV.get(contadorIds);
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_CREAR_CLIENTE";
									msg.descripcion = "Error al crear cliente";
									salida.P_OUT_MENSAJES.add(msg);
								}
							}
						}
					}	
				}else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_DATOS_MINIMOS_CLIENTE";
					msg.descripcion = "Error al crear Cliente por que faltan datos.";
					salida.P_OUT_MENSAJES.add(msg);
					for(int i=0; i < resultadoValidacion.size(); i++){
						msg = new MensajeSalida();
						msg.id = idsContratoSAAPV.get(contadorIds);
						msg.tipo = "ADVERTENCIA";
						msg.codigo = "VALIDA_DATOS_MINIMOS_CLIENTE";
						msg.descripcion = " - " + resultadoValidacion.get(i);
						salida.P_OUT_MENSAJES.add(msg);
					}
				}
				
				/*Se ingresa Relacion Persona Cliente Empleador*/
				if((fila.IdPersona != null || fila.IdPersona != 0) && ((fila.IdEmpleador != null && fila.IdEmpleador != 0) || fila.IdEmpleador > 0)){
					
					//Valida la existencia de la relacion de la persona con el empleador
				    HashMap<String,Object> paramConsultaRelacion = new HashMap<>();
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
						
						Long IdRelacionPersona = 0L;
						HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_RELACION_PERSONA, paramsIn, paramsOut);
						if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
							IdRelacionPersona = (Long)resultSP.get("p_out_RefId"); //NO LO USAN EN NINGUN LADO DE QUE SIRVE ESTO!!!!
							logger.debug(IdRelacionPersona);
						}else{
							MensajeSalida msg;
							for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
								msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ERROR";
								msg.codigo = errorDetalle.Codigo;
								msg.descripcion = errorDetalle.Descripcion;
								salida.P_OUT_MENSAJES.add(msg);
								SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								msg = new MensajeSalida();
								msg.id = idsContratoSAAPV.get(contadorIds);
								msg.tipo = "ADVERTENCIA";
								msg.codigo = advertenciaDetalle.Codigo;
								msg.descripcion = advertenciaDetalle.Descripcion;
								salida.P_OUT_MENSAJES.add(msg);
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
							}
						}
					}
					
				}	
			}

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
		    PC.CodigoTipoSolicitud			= fila.CodigoTipoSolicitud;
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
		    PC.TelefonoComercialCliente		= fila.TelefonoMovil;
		    PC.TelefonoCelularCliente		= fila.TelefonoLaboral;
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
		    PC.NombreEntidadOrigen			= fila.NombreEntidad;
		    PC.RutEntidadOrigen				= fila.DocumentoEntidad;
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
		    
			List<ContratoSAAPV> ParamsContrato = new ArrayList<ContratoSAAPV>();			
			ParamsContrato.add(PC);	
						
			int[] res = sql.batch(con, SQL_CREAR_CONTRATOSAAPV,ParamsContrato, ContratoSAAPV.class);

			if(res != null && res.length > 0 && res[0] == 1){
			    MensajeSalida msgContrato = new MensajeSalida();
			    msgContrato.id = idsContratoSAAPV.get(contadorIds);
			    msgContrato.tipo = "OK";
			    msgContrato.codigo = "CREACION_CONTRATO_OK";
			    msgContrato.descripcion = "Creacion de contrato exitosa";
				salida.P_OUT_MENSAJES.add(msgContrato);
				
				List<Long> IdsContratoTraspasoSAAPV = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdTraspaso", 1L);
				
				Traspaso traspasoContrato = new Traspaso();
				traspasoContrato.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
				traspasoContrato.IdContratoSAAPV = fila.IdContratoSAAPV;
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
				traspasoContrato.NombreEntidad = fila.NombreEntidad;
				traspasoContrato.DocumentoEntidad = fila.DocumentoEntidad;
				traspasoContrato.IdEstado = fila.TipoMovimiento.equals("INGRESO") ? resultadoValidacion.size() == 0 ? "VIG" : "PDI" : "VIG";
				traspasoContrato.Canal = fila.Canal;
				List<Traspaso> lstTraspaso = new ArrayList<Traspaso>();
				lstTraspaso.add(traspasoContrato);
				int[] resultado = sql.batch(con, SQL_CREAR_TRASPASO,lstTraspaso, Traspaso.class);
				if(resultado != null && resultado.length > 0 && resultado[0] == 1){
					MensajeSalida msg = new MensajeSalida();
				    msg.id = IdsContratoTraspasoSAAPV.get(0);
				    msg.tipo = "OK";
				    msg.codigo = "CREACION_TRASPASO_OK";
				    msg.descripcion = "Creacion del traspaso exitosa";
					salida.P_OUT_MENSAJES.add(msg);
					
					DetalleOrigenTraspaso dtOrigenTraspaso = new DetalleOrigenTraspaso();
					List<DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
					for(DetalleOrigenTraspaso row : in.p_DetalleOrigenTraspaso){
						List<Long> IdsDetalleOrigenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso", 1L);
						dtOrigenTraspaso = new DetalleOrigenTraspaso();
						dtOrigenTraspaso.IdDetalleOrigenTraspaso = IdsDetalleOrigenTraspaso.get(0);
						dtOrigenTraspaso.IdTraspaso = IdsContratoTraspasoSAAPV.get(0);
						dtOrigenTraspaso.CodigoTipoAhorro = row.CodigoTipoAhorro;
						dtOrigenTraspaso.NombreEntidadOrigen = row.NombreEntidadOrigen;
						dtOrigenTraspaso.DocumentoEntidadOrigen = row.DocumentoEntidadOrigen;
						dtOrigenTraspaso.FondoOrigen = row.FondoOrigen;
						dtOrigenTraspaso.MontoTotalTraspaso = row.MontoTotalTraspaso;
						dtOrigenTraspaso.UFTotalTraspaso = row.UFTotalTraspaso;
						dtOrigenTraspaso.PorcentajeTotalTraspaso = row.PorcentajeTotalTraspaso;
						dtOrigenTraspaso.IdEstado = "VIG";
						lstOrigenTraspaso.add(dtOrigenTraspaso);
					};
					int[] result = sql.batch(con, SQL_CREAR_DETALLE_ORIGEN_TRASPASO,lstOrigenTraspaso, DetalleOrigenTraspaso.class);
					if(result != null && result.length > 0 && resultado[0] == 1){
						msg = new MensajeSalida();
					    msg.id = IdsContratoTraspasoSAAPV.get(0);
					    msg.tipo = "OK";
					    msg.codigo = "CREACION_ORIGEN_TRASPASO_OK";
					    msg.descripcion = "Creacion del Origen traspaso exitosa";
						salida.P_OUT_MENSAJES.add(msg);
					}
				}
				
				/*
				if(in.p_Tipo.compareTo("M") == 0){
					HashMap<String,Object> paramConsulta = new HashMap<>();
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
							    MensajeSalida msgEliContrato = new MensajeSalida();
							    msgEliContrato.id = 23L;
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
				*/
				
				/*Valido datos minimos del contrato para ingresarlo vig o pdi*/
				List<String> resultadoValidacionPersona;
				/*
				if(in.p_PortafoliosModificados.size() > 0 )
					resultadoValidacionPersona = ValidaCamposPersona("DATOS_MINIMOS",fila,in.p_PortafoliosModificados.get(0));
				else
				*/
					resultadoValidacionPersona = ValidaCamposPersona("DATOS_MINIMOS",fila,null);
				
				if(resultadoValidacionPersona.size() == 0){
					//BDH Se cambia estado a VIG
					CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada inAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada();
					List<CambiarEstadoContratosSAAPV.ContratosSAAPV> tablaContratos = new ArrayList<CambiarEstadoContratosSAAPV.ContratosSAAPV>();
					
					CambiarEstadoContratosSAAPV.ContratosSAAPV ContratoAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV();
					ContratoAutorizacion.IdPersona = fila.IdPersona;
					ContratoAutorizacion.IdCliente = fila.IdCliente;
					ContratoAutorizacion.IdEmpleador = fila.IdEmpleador;
					ContratoAutorizacion.IdFolioContratoSAAPV = fila.FolioContratoSAAPV;
					ContratoAutorizacion.FechaRevocacion = "1900-01-01";
					ContratoAutorizacion.IdEstadoEnvioNotificacion = fila.IdEstadoEnvioNotificacion;
					ContratoAutorizacion.IdEstadoSolicitud = "PDA";
					ContratoAutorizacion.IdEstado = fila.IdEstado;//debe ir vigente ya que la validacion de campos del contrato dio como resultado OK
					tablaContratos.add(ContratoAutorizacion);
				
					CambiarEstadoContratosSAAPV cambiarEstadoContratoService = new CambiarEstadoContratosSAAPV();
					inAutorizacion.p_IdEmpresa = in.p_IdEmpresa;
					inAutorizacion.p_IdUsuario = in.p_IdUsuario;
					inAutorizacion.p_IdModulo = in.p_IdModulo;
					inAutorizacion.p_Opcion = "VIG";
					inAutorizacion.p_ContratosSAAPV = tablaContratos;
					cambiarEstadoContratoService.logica(con, inAutorizacion);
					/*BDH Se cambia estado a VIG FIN */ 
				}else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
					msg.descripcion = "Contrato queda Pendiente de Ingreso por que faltan datos Minimos.";
					salida.P_OUT_MENSAJES.add(msg);
					for(int i=0; i < resultadoValidacionPersona.size(); i++){
						msg = new MensajeSalida();
						msg.id = idsContratoSAAPV.get(contadorIds);
						msg.tipo = "ADVERTENCIA";
						msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
						msg.descripcion = " - " + resultadoValidacionPersona.get(i);
						salida.P_OUT_MENSAJES.add(msg);
					}
				}
				
			}else{
			    MensajeSalida msg = new MensajeSalida();
				msg.id = idsContratoSAAPV.get(contadorIds);
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_CREAR_CONTRATO";
				msg.descripcion = "Problemas al crear contrato";
				salida.P_OUT_MENSAJES.add(msg);
			}
			
			if(in.p_DistribucionInversion.size() > 0){
				
				for(DistribucionInversion row : in.p_DistribucionInversion){
					row.IdContratoSAAPV = fila.IdContratoSAAPV;
				}
				
				int[] result = sql.batch(con, SQL_CREAR_DISTRIBUCION_INVERSION, in.p_DistribucionInversion, DistribucionInversion.class);
				if(result != null && result.length > 0 && result[0] == 1){
				    MensajeSalida msg = new MensajeSalida();
					msg.id = idsContratoSAAPV.get(contadorIds);
					msg.tipo = "OK";
					msg.codigo = "CREAR_DISTRIBUCION_OK";
					msg.descripcion = "Creacion Distribucion Inversion exitosa";
					salida.P_OUT_MENSAJES.add(msg);
				}else{
				    MensajeSalida msg = new MensajeSalida();
					msg.id = idsContratoSAAPV.get(contadorIds);
					msg.tipo = "ERROR";
					msg.codigo = "ERROR_CREAR_DISTRIBUCION";
					msg.descripcion = "ERROR al crear Distribucion";
					salida.P_OUT_MENSAJES.add(msg);
				}
			}
			contadorIds ++;
		}
		return salida;
	}
	
	//METODO VALIDA CUMPLE REQUISITOS MINIMOS PARA SER PERSONA,CLIENTE
	public List<String> ValidaCamposPersona(String TipoValidacion,ParametrosContrato Contrato,PortafoliosModificados Portafolio )
	{
		List<String> lstDatosFaltantes = new ArrayList<String>();
		if(TipoValidacion.equals("CLIENTE"))
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
			/*
			if(Contrato.NumeroDireccionCliente == null || Contrato.NumeroDireccionCliente.equals(""))
				lstDatosFaltantes.add("Numero Direccion Cliente."); 
			*/
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
			/*
			if(Contrato.IdTipoCliente == null ||Contrato.IdTipoCliente.equals(""))
				lstDatosFaltantes.add("Tipo Cliente.");
			*/ 
			if(Contrato.IdEmpleado == null ||Contrato.IdEmpleado == 0)
				lstDatosFaltantes.add("Ejecutivo Cliente.");

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
			/*
			if(Contrato.NumeroDireccionCliente == null || Contrato.NumeroDireccionCliente.equals(""))
				lstDatosFaltantes.add("Numero Direccion Cliente.");
				*/
			if(Contrato.IdPaisResidenciaCliente == null || Contrato.IdPaisResidenciaCliente == 0)
				lstDatosFaltantes.add("Id Pais.");
			if(Contrato.IdComunaCliente == null || Contrato.IdComunaCliente == 0)
				lstDatosFaltantes.add("Id Comuna.");
			if(Contrato.IdCiudadCliente == null || Contrato.IdCiudadCliente == 0)
				lstDatosFaltantes.add("Id Ciudad.");
			if(Contrato.TipoMovimiento.equals("INGRESO")) {
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
		}
		
		return lstDatosFaltantes;
	}
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":454549,\"IdPlan\":1,\"NombrePlan\":\"A. ABIERTO\",\"FechaSuscripcion\":\"2018-03-15\",\"FechaCreacion\":\"2018-03-15\",\"TipoMovimiento\":\"INGRESO\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"CodigoTipoSolicitud\":\"TRA\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdPersona\":0,\"IdCliente\":0,\"IdEmpleador\":0,\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"PRUEBA DE DATOS MINIMOS 25\",\"ApellidoPaternoCliente\":\"A\",\"ApellidoMaternoCliente\":\"B\",\"DocumentoIdentidadCliente\":\"7813510-7\",\"FechaNacimientoCliente\":\"2018-03-14\",\"EstadoCivil\":\"CC\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2931,\"IdComunaCliente\":327,\"DireccionCliente\":\"CALLEEEEE\",\"NumeroDireccionCliente\":\"567567\",\"IdTipoCargoPersona\":6,\"TelefonoFijo\":\"\",\"TelefonoMovil\":\"\",\"TelefonoLaboral\":\"+5625561527\",\"EmailCliente\":\"ADASDWERR@GMAIL.COM\",\"RazonSocialEmpleador\":\"FRUNA SA\",\"DocumentoIdentidadEmpleador\":\"54364384-K\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"CONDE DE MANO\",\"NumeroDireccionEmpleador\":\"670\",\"IdCiudadEmpleador\":2058,\"IdComunaEmpleador\":306,\"TelefonoEmpleador\":\"56223993861\",\"NombreEncargado\":\"MARIO SANTANDER\",\"EmailEncargado\":\"MSANTANDER@FRUNA.CL\",\"TelefonoEncargado\":\"+5692556487\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":5,\"CodigoPrevision\":\"1008\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"\",\"IdMedioRecaudacionFormaPago\":0,\"DiaDescuento\":0,\"Institucion\":0,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7297,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_DetalleOrigenTraspaso\":[{\"IdDetalleOrigenTraspaso\":0,\"IdTraspaso\":0,\"CodigoTipoAhorro\":\"APV\",\"NombreEntidadOrigen\":\"AFP PLANVITAL\",\"DocumentoEntidadOrigen\":\"98001200-K\",\"FondoOrigen\":\"A\",\"MontoTotalTraspaso\":0,\"UFTotalTraspaso\":0,\"PorcentajeTotalTraspaso\":50,\"IdEstado\":\"VIG\"},{\"IdDetalleOrigenTraspaso\":0,\"IdTraspaso\":0,\"CodigoTipoAhorro\":\"DC\",\"NombreEntidadOrigen\":\"AFP PLANVITAL\",\"DocumentoEntidadOrigen\":\"98001200-K\",\"FondoOrigen\":\"A\",\"MontoTotalTraspaso\":15000000,\"UFTotalTraspaso\":0,\"PorcentajeTotalTraspaso\":0,\"IdEstado\":\"VIG\"}]}";
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				GrabarContratoTraspasoSAAPV demo = new GrabarContratoTraspasoSAAPV();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.commit();
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