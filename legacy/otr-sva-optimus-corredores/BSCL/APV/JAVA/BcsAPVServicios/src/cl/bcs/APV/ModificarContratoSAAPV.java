package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
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

import cl.bcs.APV.GrabarContratoSAAPV.ContratoSAAPV;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosContrato;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosEntradaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.PortafolioContratoSAAPV;
import cl.bcs.APV.GrabarContratoSAAPV.Traspaso;
import cl.bcs.APV.GrabarTraspasos.TraspasoModificar;
import cl.bcs.APV.CambiarEstadoContratosSAAPV;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarContratoSAAPV implements JavaCall   {
	
	static final Logger logger = LogManager.getLogger(ModificarContratoSAAPV.class);
	/*==================================================================================================*/
	/* Queries que van a ser utilizadas por el servicio. */ 
	/*==================================================================================================*/
	public String SQL_CREAR_PORTAFOLIO_APV_Call 	= loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql"); //Para los nuevos portafolios
	public String SQL_CONSULTAR_CONTRATOSAAPV 		= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta.sql");  // Se utiliza para determinar el estado del contrato en linea
	public String SQL_MODIFICAR_CONTRATOSAAPV 		= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Update.sql");  // Se modifica el contrato, debe permanecer el vinculo con los Portafolios existentes y nuevos
	public String SQL_ELIMINAR_CONTRATOSAAPV 		= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Delete.sql");  // Cuando el estado del contrato es "PDI" o "PEN", este se elimina y se deja solo el nuevo, vinculado a los portafolios que tenga ese SAAPV
	public String SQL_CREAR_DISTRIBUCION_INVERSION 	= loadResourceToString("cl/bcs/APV/sql/AgregarDistribucionInversion.sql");  // Se utiliza para crear la distribucion que viene ingresando
	public String SQL_ELIMINAR_DISTRIBUCION_CONTRATOSAAPV = loadResourceToString("cl/bcs/APV/sql/EliminarDistribucionInversion.sql");  // elimina el Distribucion Inversion anterior
	public String SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV	  = loadResourceToString("cl/bcs/APV/sql/Crear_PortafolioContratoSAAPV.sql"); // crea relacion entre portafolio con contrato SAAPV
	
	public String SQL_ELIMINAR_PORTAFOLIOS_CONTRATOS_SAAPV	  = loadResourceToString("cl/bcs/APV/sql/Eliminar_PortafolioContratoSAAPV.sql"); // elimina relacion entre portafolio con contrato SAAPV
	/*TRASPASOS*/
	public String SQL_ELIMINAR_DETALLE_ORIGEN_TRASPASO_EGRESO = loadResourceToString("cl/bcs/APV/sql/Eliminar_DetalleOrigenTraspasoEgreso.sql"); //Elimina traspaso de egresos
	public String SQL_CONSULTAR_TRASPASO_EGRESOS = loadResourceToString("cl/bcs/APV/sql/ConsultarTraspasosEgresos.sql"); // Consulta los traspasos de egresos
	public String SQL_MODIFICAR_TRASPASO_EGRESOS = loadResourceToString("cl/bcs/APV/sql/ModificarTraspasosEgresos.sql"); // modifica los traspasos de egresos
	public String SQL_ELIMINAR_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/Eliminar_DetalleOrigenTraspaso.sql"); // Elimina origen traspaso
	public String SQL_ELIMINAR_TRASPASO	= loadResourceToString("cl/bcs/APV/sql/Eliminar_Traspaso.sql"); // Elimina traspaso
	public String SQL_CREAR_TRASPASO			 = loadResourceToString("cl/bcs/APV/sql/Traspaso_Insert.sql"); //Crea Traspaso
	public String SQL_CREAR_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/DetalleOrigenTraspaso_Insert.sql"); //Crea Detalle Origen Traspaso
	public String SQL_MODIFICAR_DETTALETRASPASO_IDCONTRATO = "UPDATE \"BSCL.APV.BD::TRA.DetalleTraspaso\" SET \"IdTraspaso\" = :IdTraspasoNuevo WHERE \"IdTraspaso\" = :IdTraspaso";// Se modifica IdTraspaso DetalleTraspaso
	public String SQL_CONSULTAR_TRASPASO = "SELECT \"IdTraspaso\" FROM \"BSCL.APV.BD::TRA.Traspaso\" WHERE \"IdContratoSAAPV\" = :IdContratoSAAPV";
	public String SQL_MODIFICAR_PORTAFOLIOCONTRATO = "UPDATE \"BSCL.APV.BD::SCS.PortafolioContratoSAAPV\" SET \"IdContratoSAAPV\" = :IdContratoSAAPVNuevo WHERE \"IdContratoSAAPV\" = :IdContratoSAAPV";
	public String SQL_MODIFICAR_ESTADOCONTRATO = "UPDATE \"BSCL.APV.BD::SCS.ContratoSAAPV\" SET \"IdEstado\" = 'CER' WHERE \"IdContratoSAAPV\" = :IdContratoSAAPV";
	
	public String SQL_MODIFICAR_TRASPASO = loadResourceToString("cl/bcs/APV/sql/Traspaso_Update.sql");
	/********************/
	/* Clases IN/OUT	*/
	/********************/	
	public static class DistribucionInversion {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public Long IdInstrumento;
		public String Nemotecnico;
		public double MontoPesos;
		public double Porcentaje;
		public String IdEstado;
	}
	
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
		public double 	Porcentaje;
		public String 	Producto;
		public String 	Regimen;
		public Long 	IdContratoSAAPVOrigen = 0L;
	}
	
	public static class DetalleOrigenTraspaso{
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
		public Long IdContratoSAAPVOrigen = 0L;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	String						p_Tipo;
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
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class AgrupadorPortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgrupador;
		public String NombreAgrupador;
		public String CodigoAgrupador;
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

	public static class clasePruebaApv{
		public String 		mensajeAPV;
	};	
	
	public static class SecuenciaIndicador{
		public Long			IdSecuencia;
	}
	
	public static class TipoIndicador{
		public Long			IdTipoIndicador;
		public String		CodigoTipoIndicador;
	}
	
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

	public static class PortafoliosSAAPV{
		public long			IdPortafolios;
		public String		CodigoPortafolio;
		public String		Proposito;
		public long			IdCliente;
		public String		IdEstado;
		public String		FechaBloqueo;
		public String		ComentarioBloqueo;
		public String		IdTipoBloqueo;
		public String		FechaIngreso;
		public String		FechaCreacion;
		public String		FechaModificacion;
		public long			IdCarga;
		public String		IdPerfilComercialBaseOrigen;
		public String		IdPerfilProductos;
		public String		IdPerfilCobros;
		public String		IdEstadoAutorizacion;
		public String		DescripcionPortafolio;
		public String		IdPerfilLimites;
		public String		IdPerfilCondicionesOperativas;
		public String		IdPerfilProductosExtra;
		public String		IdPerfilCobrosExtra;
		public String		IdPerfilLimitesExtra;
		public String		IdPerfilCondicionesOperativasExtra;
		public String		IdTipoDesbloqueo;
		public String		FechaDesbloqueo;
		public String		ComentarioDesbloqueo;
		public long			IdDireccionEnvioCorrespondencia;
		public long			IdTelefonoContacto;
		public long			IdEmpleadoEjecutivo;
		public String		EsEjecutivoExclusivo;
		public String		NumeroFicha;
		public long			IdNombreFantasia;
		public String		Alias;
		public long			IdPersonaBrokerExtranjero;
		public String		IdPerfilCobrosPeru;
		public String		IdPerfilCobrosPeruExtra;
		public long			IdMatrizAgrupadorAPV;
	}

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TraspasoEgresosConsultado{
		public Long IdTraspaso;
		public String IdEstado;
		public String FechaModificacion;
		public String NombreEntidad;
		public String DocumentoEntidad;
		public String TipoTraspaso;
		public double PorcentajeTraspaso;
		public double MontoTraspaso;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class  ConsultaDireccion{
		public Long IdDireccionFisica;
		
	}
	
	public static class TraspasoConsultado {
		public Long IdTraspaso;
		public Long IdTraspasoNuevo;
	}
	
	public static class TraspasoConsultadoEgresos{
		public Long IdDetalleOrigenTraspaso;
	}

	
	public static class ModificarPortafolioContrato{
		public Long IdContratoSAAPV;
		public Long IdContratoSAAPVNuevo;
	}
	
	public static class ModificarEstadoContrato{
		public Long IdContratoSAAPV;
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();

		SalidaServicioCrearCliente  SalidaCliente   = new SalidaServicioCrearCliente();
		
		SQL sql = new SQL();
		
		String ApruebaTraspasoAutomatico = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "APV","APV.ApruebaTraspasoAutomatico");
		
		for(ParametrosContrato fila : in.p_Parametros){
			Long IdContratoSAAPVBorrar = fila.IdContratoSAAPV;
			
			//*** VALIDACION CONTRATOS SAAPV ***
			ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada inValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
			ValidaContratoSAAPV.ValidaContratoSAAPV_Salida outValidaContrato = new ValidaContratoSAAPV.ValidaContratoSAAPV_Salida();
			List<ValidaContratoSAAPV.ContratosSAAPV> tablaValidaContrato = new ArrayList<ValidaContratoSAAPV.ContratosSAAPV>();
			
		 	for(PortafoliosModificados filaValidaPortafolio : in.p_PortafoliosModificados){
		 		if (IdContratoSAAPVBorrar.longValue() != filaValidaPortafolio.IdContratoSAAPVOrigen.longValue()) continue;
		 		
				ValidaContratoSAAPV.ContratosSAAPV ContratoValidaContrato;
		
				//Se envia solo IdPortafolio NULL para que SOLO valide el portafolio
				if(filaValidaPortafolio.IdPortafolio == 0L){
					ContratoValidaContrato = new ValidaContratoSAAPV.ContratosSAAPV();
					ContratoValidaContrato.IdPersona = fila.IdPersona;
					ContratoValidaContrato.IdCliente = fila.IdCliente;
					ContratoValidaContrato.IdEmpleador = fila.IdEmpleador;
					ContratoValidaContrato.IdPortafolio = null;
					ContratoValidaContrato.DocumentoIdentidad = fila.DocumentoIdentidadCliente;
					ContratoValidaContrato.DocumentoIdentidadEmpleador = fila.DocumentoIdentidadEmpleador;
					ContratoValidaContrato.FolioContratoSAAPV = null;
					ContratoValidaContrato.Codigo = filaValidaPortafolio.Regimen;
					ContratoValidaContrato.NombrePlan = fila.NombrePlan;
					ContratoValidaContrato.CodigoAgrupador = filaValidaPortafolio.CodigoAgrupador;
					tablaValidaContrato.add(ContratoValidaContrato);	
				}
		 	}

			ValidaContratoSAAPV ValidaContratoSAAPVService = new ValidaContratoSAAPV();
			inValidaContrato.p_IdEmpresa = in.p_IdEmpresa;
			inValidaContrato.p_IdUsuario = in.p_IdUsuario;
			inValidaContrato.p_IdModulo = in.p_IdModulo;
			inValidaContrato.p_Opcion = "M";
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
					 		if (IdContratoSAAPVBorrar.longValue() != filaSeteaPortafolio.IdContratoSAAPVOrigen.longValue()) continue;
					 		
					 		if(filaValidaContrato.Codigo.compareTo(filaSeteaPortafolio.Regimen)==0 && filaValidaContrato.IdPortafolio != null){
					 			filaSeteaPortafolio.IdPortafolio = filaValidaContrato.IdPortafolio;
					 		}
					 	}
					}	
				}
				logger.debug("ValidaContratoSAAPV OK");
			}
			if(fila.IdMedioRecaudacionFormaPago == null && !fila.TipoMovimiento.equals("EGRESO")) {
				MensajeSalida msgError = new MensajeSalida();
				msgError.tipo = "ERROR";
				msgError.codigo = "ERROR_FORMA_PAGO_NO_EXISTE";
				msgError.descripcion = "No existe Forma de pago de medio recaudación.";
				salida.P_OUT_MENSAJES.add(msgError);
				return salida;
			}
			//*** VALIDACION CONTRATOS SAAPV - FIN ***
			if("".equalsIgnoreCase(fila.FolioContratoSAAPV))
				fila.FolioContratoSAAPV = fila.Folio;
			else if("".equalsIgnoreCase(fila.Folio))
				fila.Folio = fila.FolioContratoSAAPV;
			ContratoSAAPV PC = new ContratoSAAPV();			
			PC.IdContratoSAAPV				= fila.IdContratoSAAPV;
			PC.IdEmpresa					= fila.IdEmpresa;
			PC.FolioContratoSAAPV 			= fila.FolioContratoSAAPV;
			PC.IdPlan						= fila.IdPlan;
			PC.NombrePlan					= fila.NombrePlan;
			PC.FechaSuscripcion				= fila.FechaSuscripcion.isEmpty()?"1900-01-01":fila.FechaSuscripcion; 
		    PC.FechaCreacion				= fila.FechaCreacion.isEmpty()?"1900-01-01":fila.FechaCreacion;
		    PC.FechaAprobacion				= fila.FechaAprobacion.isEmpty()?"1900-01-01":fila.FechaAprobacion; 
		    PC.FechaNotificacion			= fila.FechaNotificacion.isEmpty()?"1900-01-01":fila.FechaNotificacion; 
		    PC.FechaRecepcion				= fila.FechaRecepcion.isEmpty()?"1900-01-01":fila.FechaRecepcion; 
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
			//===============================================================================================
			// Se obtiene estado del contrato en linea previo a la modificacion para evaluacion respecto de que hacer con el
			//===============================================================================================
			HashMap<String,Object> paramConsulta = new HashMap<>();
			paramConsulta.put("IdContratoSAAPV", IdContratoSAAPVBorrar);
			List<ContratoConsultado> ContratoConsultado = sql.query(con, SQL_CONSULTAR_CONTRATOSAAPV,paramConsulta, ContratoConsultado.class);
			//boolean EliminoContrato = false;
			
			//Se Modifica el contrato SAAPV con los nuevos parametros
			List<ContratoSAAPV> ParamsContrato = new ArrayList<ContratoSAAPV>();			
			ParamsContrato.add(PC);	

			sql.batch(con, SQL_MODIFICAR_CONTRATOSAAPV,ParamsContrato, ContratoSAAPV.class);
			if(!in.p_Parametros.isEmpty()) {
				if(in.p_Parametros.get(0).CodigoTipoSolicitud != null && in.p_Parametros.get(0).TipoMovimiento != null && !in.p_Parametros.get(0).CodigoTipoSolicitud.equals("TRA") && !in.p_Parametros.get(0).TipoMovimiento.equals("EGRESO")) {
					AgregarLogSAAPV(con, in);
				}
			}
			
			msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "OK";
			msg.codigo = "MODIFICACION_CONTRATO_OK";
			msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Modificacion de contrato exitosa";
			salida.P_OUT_MENSAJES.add(msg);
			logger.debug("Modifica ContratoSAAPV OK");
		
			for (ContratoConsultado linea : ContratoConsultado) {
				if (linea.IdEstado.compareTo("PDI") == 0 || linea.IdEstado.compareTo("PEN") == 0){
					
					ContratoConsultado paramBorrar = new ContratoConsultado();
					paramBorrar.IdContratoSAAPV = IdContratoSAAPVBorrar;
					List<ContratoConsultado> p = new ArrayList<ContratoConsultado>();
					p.add(paramBorrar);
					/*INICIO TRASPASO*/
					//=============================================================================================================
					// MODIFICA TRASPASO
					//=============================================================================================================
					if(fila.CodigoTipoSolicitud.equals("TRA")){
						//===============================================================================================
						// Se obtiene IdTraspaso Antiguo antes de borrarlo
						//===============================================================================================
						HashMap<String,Object> paramConsultaTraspaso = new HashMap<>();
						paramConsultaTraspaso.put("IdContratoSAAPV", IdContratoSAAPVBorrar);
						Long IdTraspasoBorrar = 0L;
						List<TraspasoConsultado> TraspasoConsultado = sql.query(con, SQL_CONSULTAR_TRASPASO,paramConsultaTraspaso, TraspasoConsultado.class);
						for(TraspasoConsultado traspasoLinea : TraspasoConsultado) {
							IdTraspasoBorrar = traspasoLinea.IdTraspaso;
						}
						int[] resDetalleOrigen = sql.batch(con, SQL_ELIMINAR_ORIGEN_TRASPASO,p, ContratoConsultado.class);
						if(resDetalleOrigen != null && resDetalleOrigen.length > 0 && resDetalleOrigen[0] == 1){
							msg = new MensajeSalida();
							msg.id = 3L;
							msg.tipo = "OK";
							msg.codigo = "OK_ELIMINAR_DETALLE_ORIGEN_TRASPASO";
							msg.descripcion = "Se elimina el Detalle origen traspaso.";
							logger.debug("Elimina Detalle Origen Traspaso OK");	
						}
						int[] resTraspaso = sql.batch(con, SQL_ELIMINAR_TRASPASO,p, ContratoConsultado.class);
						if(resTraspaso != null && resTraspaso.length > 0 && resTraspaso[0] == 1){
							msg = new MensajeSalida();
							msg.id = 3L;
							msg.tipo = "OK";
							msg.codigo = "OK_ELIMINAR_TRASPASO";
							msg.descripcion = "Se elimina el traspaso.";
							logger.debug("Eliminar Traspaso OK");
						}
						/*INICIO - CREACION DE TRASPASO*/
						if(fila.CodigoTipoSolicitud.equals("TRA")){
							List<Long> IdsTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdTraspaso", 1L);				
							Traspaso traspasoContrato = new Traspaso();
							traspasoContrato.IdTraspaso = IdsTraspaso.get(0);
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
							traspasoContrato.NombreEntidad = fila.NombreEntidadOrigen;
							traspasoContrato.DocumentoEntidad = fila.RutEntidadOrigen;
							traspasoContrato.IdEstado = "PDI";
							traspasoContrato.Canal = fila.Canal;
							traspasoContrato.FechaSuscripcion = fila.FechaSuscripcion;
							List<Traspaso> lstTraspaso = new ArrayList<Traspaso>();
							lstTraspaso.add(traspasoContrato);
							int[] resultado = sql.batch(con, SQL_CREAR_TRASPASO,lstTraspaso, Traspaso.class);
							if(resultado != null && resultado.length > 0 && resultado[0] == 1){
								msg = new MensajeSalida();
							    msg.id = IdsTraspaso.get(0);
							    msg.tipo = "OK";
							    msg.codigo = "CREACION_TRASPASO_OK";
							    msg.descripcion = "Creacion del traspaso exitosa";
								salida.P_OUT_MENSAJES.add(msg);
								logger.debug("Crea Contrato Traspaso OK");
								
								DetalleOrigenTraspaso dtOrigenTraspaso = new DetalleOrigenTraspaso();
								List<DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
								for(DetalleOrigenTraspaso row : in.p_DetalleOrigenTraspaso){
									if (IdContratoSAAPVBorrar.longValue() != row.IdContratoSAAPVOrigen.longValue()) continue;
									
									List<Long> IdsDetalleOrigenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso", 1L);
									dtOrigenTraspaso = new DetalleOrigenTraspaso();
									dtOrigenTraspaso.IdDetalleOrigenTraspaso = IdsDetalleOrigenTraspaso.get(0);
									dtOrigenTraspaso.IdTraspaso = IdsTraspaso.get(0);
									dtOrigenTraspaso.CodigoTipoAhorro = row.CodigoTipoAhorro;
									dtOrigenTraspaso.IdEntidadRecaudadora = row.IdEntidadRecaudadora;
									dtOrigenTraspaso.NombreEntidad = row.NombreEntidad;
									dtOrigenTraspaso.DocumentoEntidad = row.DocumentoEntidad;
									dtOrigenTraspaso.Fondo = row.Fondo;
									dtOrigenTraspaso.MontoTraspaso = row.MontoTraspaso;
									dtOrigenTraspaso.UFTraspaso = row.UFTraspaso;
									dtOrigenTraspaso.PorcentajeTraspaso = row.PorcentajeTraspaso;
									dtOrigenTraspaso.IdEstado = "VIG";
									dtOrigenTraspaso.IdInstrumento = row.IdInstrumento;
									dtOrigenTraspaso.IdPortafolio = row.IdPortafolio;
									dtOrigenTraspaso.CodigoRegimenTributario = row.CodigoRegimenTributario;
									lstOrigenTraspaso.add(dtOrigenTraspaso);
								};
								
								if (lstOrigenTraspaso.size() > 0) {
									int[] result = sql.batch(con, SQL_CREAR_DETALLE_ORIGEN_TRASPASO, lstOrigenTraspaso, DetalleOrigenTraspaso.class);
									if(result != null && result.length > 0 && result[0] == 1){
										msg = new MensajeSalida();
									    msg.id = IdsTraspaso.get(0);
									    msg.tipo = "OK";
									    msg.codigo = "CREACION_ORIGEN_TRASPASO_OK";
									    msg.descripcion = "Creacion del Origen traspaso exitosa";
										salida.P_OUT_MENSAJES.add(msg);
										logger.debug("Crea Detalle Origen Traspaso OK");
									}
								}
								
								/*Modifico el IdTraspaso Nuevo del DetalleTraspaso, ya que el contrato viejo se borro*/
								List<TraspasoConsultado> lstTraspasoModificar = new ArrayList<TraspasoConsultado>();
								TraspasoConsultado tpTraspaso = new TraspasoConsultado();
								tpTraspaso.IdTraspaso = IdTraspasoBorrar;
								tpTraspaso.IdTraspasoNuevo = IdsTraspaso.get(0);
								lstTraspasoModificar.add(tpTraspaso);
								int[] resultModificaDtTraspaso = sql.batch(con, SQL_MODIFICAR_DETTALETRASPASO_IDCONTRATO,lstTraspasoModificar, TraspasoConsultado.class);
								if(resultModificaDtTraspaso != null && resultModificaDtTraspaso.length > 0 && resultModificaDtTraspaso[0] == 1){
									msg = new MensajeSalida();
								    msg.id = IdsTraspaso.get(0);
								    msg.tipo = "OK";
								    msg.codigo = "MODIFICACION_DETALLETRASPASO_OK";
								    msg.descripcion = "Se modifica DetalleTraspaso por el Contrato Traspaso Nuevo.";
									salida.P_OUT_MENSAJES.add(msg);
									logger.debug("Se modifica DetalleTraspaso por el Contrato Traspaso Nuevo.");
								}
								/*Modifico IdContratoSAAPv PortafolioContratoSAAPV*/
								List<ModificarPortafolioContrato> lstModificarPortafolioContrato = new ArrayList<ModificarPortafolioContrato>();
								ModificarPortafolioContrato modModificarPortafolioContrato = new ModificarPortafolioContrato();
								modModificarPortafolioContrato.IdContratoSAAPV = IdContratoSAAPVBorrar;
								modModificarPortafolioContrato.IdContratoSAAPVNuevo = fila.IdContratoSAAPV;
								lstModificarPortafolioContrato.add(modModificarPortafolioContrato);
								int[] resultModificaPortafolioContrato = sql.batch(con, SQL_MODIFICAR_PORTAFOLIOCONTRATO,lstModificarPortafolioContrato, ModificarPortafolioContrato.class);
								if(resultModificaPortafolioContrato != null && resultModificaPortafolioContrato.length > 0 && resultModificaPortafolioContrato[0] == 1){
									msg = new MensajeSalida();
								    msg.id = IdsTraspaso.get(0);
								    msg.tipo = "OK";
								    msg.codigo = "MODIFICACION_PORTAFOLIOCONTRATO_OK";
								    msg.descripcion = "Se modifica PortafolioContrato por el Contrato Traspaso Nuevo.";
									salida.P_OUT_MENSAJES.add(msg);
									logger.debug("Se modifica PortafolioContrato por el Contrato Traspaso Nuevo.");
									
									if(ApruebaTraspasoAutomatico.equals("N")) {
										//Modifico estado de traspaso a Pendiente de Autorizacion
										List<TraspasoModificar> lstTraspasoMod = new ArrayList<TraspasoModificar>();
										TraspasoModificar tm = new TraspasoModificar();
										tm.IdEstado = "PDA";
										tm.IdTraspaso = IdsTraspaso.get(0);
										lstTraspasoMod.add(tm);
										
										int[] resultadoMod = sql.batch(con, SQL_MODIFICAR_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
										if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
											msg = new MensajeSalida();
											msg.id =  IdsTraspaso.get(0);
											msg.tipo = "OK";
											msg.codigo = "MODIFICA_ESTADO_OK";
											msg.descripcion = "Modifica estado del Traspaso.";
											salida.P_OUT_MENSAJES.add(msg);
										}else{
											msg = new MensajeSalida();
											msg.id =  IdsTraspaso.get(0);
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
										evento.Fecha = fila.FechaCreacion;
										evento.IdTipoOperacion = null;
										evento.IdConcepto = "INGTRA";
										evento.NumeroOperacionConcepto = IdsTraspaso.get(0).toString();
										evento.FechaOperacionConcepto = fila.FechaCreacion;
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
										evento.Glosa = "INGRESO DE TRASPASO APV Folio : " + IdsTraspaso.get(0).toString();
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
										tm.IdTraspaso = IdsTraspaso.get(0);
										lstTraspasoMod.add(tm);
										
										int[] resultadoMod = sql.batch(con, SQL_MODIFICAR_TRASPASO, lstTraspasoMod, TraspasoModificar.class);				
										if(resultadoMod != null && resultadoMod.length > 0 && resultadoMod[0] == 1){
											msg = new MensajeSalida();
											msg.id =  IdsTraspaso.get(0);
											msg.tipo = "OK";
											msg.codigo = "MODIFICA_ESTADO_OK";
											msg.descripcion = "Modifica estado del Traspaso.";
											salida.P_OUT_MENSAJES.add(msg);
										}else{
											msg = new MensajeSalida();
											msg.id =  IdsTraspaso.get(0);
											msg.tipo = "ERROR";
											msg.codigo = "ERROR_MODIFICA_ESTADO";
											msg.descripcion = "Problema al modificar estado del Traspaso.";
											salida.P_OUT_MENSAJES.add(msg);
										}					
									}
								}
							}
						}
						/*FIN - CREACION DE TRASPASO*/
					}
					/*FIN TRASPASO*/
					//=============================================================================================================
					// ELIMINA DISTRIBUCON
					//=============================================================================================================
					int[] resDeleteDistrib = sql.batch(con, SQL_ELIMINAR_DISTRIBUCION_CONTRATOSAAPV,p, ContratoConsultado.class);
					if(resDeleteDistrib != null && resDeleteDistrib.length > 0 && resDeleteDistrib[0] > 0){
						msg = new MensajeSalida();
						msg.id = 3L;
						msg.tipo = "OK";
						msg.codigo = "ELIMINAR_DISTRIBUCION_OK";
						msg.descripcion = "Se elimina el Distribucion Inversion anterior";
					}
				}
				else {
					ContratoConsultado paramBorrar = new ContratoConsultado();
					paramBorrar.IdContratoSAAPV = IdContratoSAAPVBorrar;
					List<ContratoConsultado> p = new ArrayList<ContratoConsultado>();
					p.add(paramBorrar);
					sql.batch(con, SQL_ELIMINAR_DISTRIBUCION_CONTRATOSAAPV,p, ContratoConsultado.class);
					msg = new MensajeSalida();
					msg.id = 3L;
					msg.tipo = "OK";
					msg.codigo = "ELIMINAR_DISTRIBUCION_OK";
					msg.descripcion = "Se elimina el Distribucion Inversion anterior";
					
				}
			}
			if(!fila.CodigoTipoSolicitud.equals("TRA")) {
				/*Valido datos minimos del contrato para ingresarlo vigente o pendiente de ingreso*/
				List<String> resultadoValidacionPersona = new ArrayList<String>();
				if(in.p_PortafoliosModificados.size() > 0) {
					for(PortafoliosModificados portafolio : in.p_PortafoliosModificados){
				 		if (IdContratoSAAPVBorrar.longValue() != portafolio.IdContratoSAAPVOrigen.longValue()) continue;
				 		
						resultadoValidacionPersona = ValidaCamposPersona("DATOS_MINIMOS", fila, portafolio);
				 	}
				}	
				else
					resultadoValidacionPersona = ValidaCamposPersona("DATOS_MINIMOS",fila,null);
				
				CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada inAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada();
				List<CambiarEstadoContratosSAAPV.ContratosSAAPV> tablaContratos = new ArrayList<CambiarEstadoContratosSAAPV.ContratosSAAPV>();

				CambiarEstadoContratosSAAPV.ContratosSAAPV ContratoAutorizacion = new CambiarEstadoContratosSAAPV.ContratosSAAPV();
				
				if(resultadoValidacionPersona.size() == 0){
					//=========================================================================================================================================
					//BDH Se cambia estado a VIG
					//=============================================================================================================
					
					//  R CORRECTITUD
					ContratoAutorizacion.IdPersona = fila.IdPersona;
					ContratoAutorizacion.IdCliente = fila.IdCliente;
					ContratoAutorizacion.IdEmpleador = fila.IdEmpleador;
					ContratoAutorizacion.IdFolioContratoSAAPV = fila.FolioContratoSAAPV;
					ContratoAutorizacion.FechaRevocacion = "1900-01-01";
					ContratoAutorizacion.IdEstadoEnvioNotificacion = fila.IdEstadoEnvioNotificacion;
					ContratoAutorizacion.IdEstadoSolicitud = "PDA";
					ContratoAutorizacion.IdEstado = "VIG";
					tablaContratos.add(ContratoAutorizacion);
					CambiarEstadoContratosSAAPV cambiarEstadoContratoService = new CambiarEstadoContratosSAAPV();
					inAutorizacion.p_IdEmpresa = in.p_IdEmpresa;
					inAutorizacion.p_IdUsuario = in.p_IdUsuario;
					inAutorizacion.p_IdModulo = in.p_IdModulo;
					inAutorizacion.p_Opcion = "VIG";
					inAutorizacion.p_ContratosSAAPV = tablaContratos;
					cambiarEstadoContratoService.logica(con, inAutorizacion);
				}else {
					msg = new MensajeSalida();
					msg.id = 4L;
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
					msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Contrato queda Pendiente de Ingreso por que faltan datos Minimos.";
					salida.P_OUT_MENSAJES.add(msg);
					for(int i=0; i < resultadoValidacionPersona.size(); i++){
						msg = new MensajeSalida();
						msg.id = 4L;
						msg.tipo = "ADVERTENCIA";
						msg.codigo = "VALIDA_DATOS_MINIMOS_CONTRATO";
						msg.descripcion = " - " + resultadoValidacionPersona.get(i);
						salida.P_OUT_MENSAJES.add(msg);
					}
				}
				
			}
			
			//=========================================================================================================================================
			/* Se crean distribuciones asociadas a la modificacion, VALIDAR Si se deben crear siempre??????????? */ 
			//=========================================================================================================================================
			if(in.p_DistribucionInversion.size() > 0){
				List<DistribucionInversion> distribucionInversionList = new ArrayList<DistribucionInversion>();
			 	for(DistribucionInversion row : in.p_DistribucionInversion){
			 		if(	row.IdContratoSAAPV == null)row.IdContratoSAAPV = fila.IdContratoSAAPV;

			 		if (IdContratoSAAPVBorrar.longValue() != row.IdContratoSAAPV.longValue()) continue;
			 		
		 			DistribucionInversion distribucionInversion = new DistribucionInversion();
		 			distribucionInversion.IdContratoSAAPV = fila.IdContratoSAAPV;
					distribucionInversion.FolioContratoSAAPV = row.FolioContratoSAAPV;
					distribucionInversion.IdInstrumento = row.IdInstrumento;
					distribucionInversion.Nemotecnico = row.Nemotecnico;
					distribucionInversion.MontoPesos = row.MontoPesos;
					distribucionInversion.Porcentaje = row.Porcentaje;
					distribucionInversion.IdEstado = row.IdEstado;
					distribucionInversionList.add(distribucionInversion);
				}
				
				int[] result = sql.batch(con, SQL_CREAR_DISTRIBUCION_INVERSION, distribucionInversionList.size() > 0 ? distribucionInversionList : in.p_DistribucionInversion, DistribucionInversion.class);
				if(result != null && result.length > 0 && result[0] == 1){
					msg = new MensajeSalida();
					msg.id = 1L;
					msg.tipo = "OK";
					msg.codigo = "CREAR_DISTRIBUCION_OK";
					msg.descripcion = "Creacion Distribucion Inversion exitosa";
					salida.P_OUT_MENSAJES.add(msg);
				}else{
					msg = new MensajeSalida();
					msg.id = 2L;
					msg.tipo = "ERROR";
					msg.codigo = "ERROR_CREAR_DISTRIBUCION";
					msg.descripcion = "Problema al crear Distribucion";
					salida.P_OUT_MENSAJES.add(msg);
				}
			}
			//=========================================================================================================================================
			// Evalua los Portafolios que vienen ingresando para determinar acciones
			//=========================================================================================================================================
			if(in.p_PortafoliosModificados.size() > 0 && !fila.CodigoTipoSolicitud.equals("TRA")){
				PortafolioContratoSAAPV PortAEliminar = new PortafolioContratoSAAPV();
				List<PortafolioContratoSAAPV> ListPortAEliminar = new ArrayList<PortafolioContratoSAAPV>();
				PortAEliminar.IdContratoSAAPV = fila.IdContratoSAAPV;
				ListPortAEliminar.add(PortAEliminar);

				sql.batch(con, SQL_ELIMINAR_PORTAFOLIOS_CONTRATOS_SAAPV, ListPortAEliminar, PortafolioContratoSAAPV.class);
			 	for(PortafoliosModificados row : in.p_PortafoliosModificados){
			 		//if (IdContratoSAAPVBorrar.longValue() != row.IdContratoSAAPVOrigen.longValue()) continue;
			 		
			 		if(row.IdPortafolio != null && row.IdPortafolio > 0) {
			 			//==============================================================================================
			 			//Portafolio existe, solo vincular a nuevo SAAPV
			 			//==============================================================================================
						//Se crea relacion entre el Contrato SAAPV y el Portafolio
			 			//==============================================================================================
			 			//logger.debug(ParamsContrato.toString());
			 			if(row.IdContratoSAAPVOrigen.equals(fila.IdContratoSAAPV)) {
							PortafolioContratoSAAPV inPortCont = new PortafolioContratoSAAPV();
							List<PortafolioContratoSAAPV> ListPortCont = new ArrayList<PortafolioContratoSAAPV>();
							inPortCont.IdContratoSAAPV = fila.IdContratoSAAPV;
							inPortCont.OrigenAhorro = row.OrigenAhorro;
							inPortCont.IdPortafolio = row.IdPortafolio;
							inPortCont.MontoPesos =  row.MontoPesos;
							inPortCont.MontoUF = row.MontoUF;
							inPortCont.PorcentajeRegimenImponible = row.Porcentaje;
							inPortCont.Pago = row.Pago;
							inPortCont.PeriodoInicio = row.MesInicio;
							inPortCont.PeriodoTermino = row.MesTermino;
							inPortCont.IdEstado = "VIG";
							ListPortCont.add(inPortCont);
	
							int[] result = sql.batch(con, SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, ListPortCont, PortafolioContratoSAAPV.class);
							
							if(result != null && result.length > 0 && result[0] == 1){
								msg = new MensajeSalida();
								msg.id = 5L;
								msg.tipo = "OK";
								msg.codigo = "CREAR_PORTAFOLIO_CONTRATO_OK";
								msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Se crea relacion entre portafolio con contrato SAAPV.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
								msg = new MensajeSalida();
								msg.id = 6L;
								msg.tipo = "OK";
								msg.codigo = "ERROR_CREAR_PORTAFOLIO_CONTRATO";
								msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Problema al crear relacion entre portafolio con contrato SAAPV.";
								salida.P_OUT_MENSAJES.add(msg);
							}
			 			}
			 			//logger.debug(row.IdPortafolio);
			 		}else{
			 			//Nuevo Portafolio, Crear y vincular.
						HashMap<String,Object> paramsIn = new HashMap<String,Object>();
						
						paramsIn.put("IdEmpresa", in.p_IdEmpresa);
						paramsIn.put("IdUsuario", in.p_IdUsuario);
						paramsIn.put("IdModulo", in.p_IdModulo);
						
						paramsIn.put("IdCliente", fila.IdCliente);
						paramsIn.put("IdPerfilComercial", fila.IdPerfilComercial);
						paramsIn.put("PropositoPortafolio", row.IdPropositoPortafolio);
						paramsIn.put("CodigoPortafolio", row.Regimen);
						paramsIn.put("CodigoRegimen", row.Regimen);
						paramsIn.put("NombrePlan", fila.NombrePlan);
						paramsIn.put("CodigoAgrupador", row.CodigoAgrupador);
						
						//Salida HashMap
						HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
						paramsOut.put("p_out_RefId", Long.class);
						paramsOut.put("p_out_AdvertenciasContador", Long.class);
						paramsOut.put("p_out_ErroresContador", Long.class);
						paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
						paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);

						HashMap<String,Object> resultSP = sql.call(con, SQL_CREAR_PORTAFOLIO_APV_Call, paramsIn, paramsOut);
								
						if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
							SalidaCliente.P_OUT_REFIDPORTAFOLIO = (Long)resultSP.get("p_out_RefId");
						}else{
							for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
								SalidaCliente.addError(errorDetalle.Codigo, errorDetalle.Descripcion);
							}
							for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){
								SalidaCliente.addAdvertencia(advertenciaDetalle.Codigo, advertenciaDetalle.Descripcion);
							}
						}
						if(SalidaCliente.P_OUT_REFIDPORTAFOLIO != 0)
						{
							
							row.IdPortafolio = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg = new MensajeSalida();
							msg.id = SalidaCliente.P_OUT_REFIDPORTAFOLIO;
							msg.tipo = "OK";
							msg.codigo = "CREACION_PORTAFOLIO_OK";
							msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Creacion de portafolio exitosa";
							salida.P_OUT_MENSAJES.add(msg);
							
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
							inPortCont.PorcentajeRegimenImponible = row.Porcentaje;
							inPortCont.Pago = row.Pago;
							inPortCont.PeriodoInicio = row.MesInicio;
							inPortCont.PeriodoTermino = row.MesTermino;
							inPortCont.IdEstado = "VIG";
							ListPortCont.add(inPortCont);

							int[] result = sql.batch(con, SQL_CREAR_PORTAFOLIOS_CONTRATOS_SAAPV, ListPortCont, PortafolioContratoSAAPV.class);
							
							if(result != null && result.length > 0 && result[0] == 1){
								msg = new MensajeSalida();
								msg.id = 5L;
								msg.tipo = "OK";
								msg.codigo = "CREAR_PORTAFOLIO_CONTRATO_OK";
								msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Se crea relacion entre portafolio con contrato SAAPV.";
								salida.P_OUT_MENSAJES.add(msg);
							}else{
								msg = new MensajeSalida();
								msg.id = 6L;
								msg.tipo = "OK";
								msg.codigo = "ERROR_CREAR_PORTAFOLIO_CONTRATO";
								msg.descripcion = "CONTRATO " + fila.FolioContratoSAAPV + ": Problema al crear relacion entre portafolio con contrato SAAPV.";
								salida.P_OUT_MENSAJES.add(msg);
							}
						}
					}
				}
			}else if(fila.TipoMovimiento.equals("EGRESO")){
				HashMap<String,Object> paramConsultaTraspasoEgresos = new HashMap<>();
				List<DetalleOrigenTraspaso> TraspasosNuevos = in.p_DetalleOrigenTraspaso;
				paramConsultaTraspasoEgresos.put("IdTraspaso", in.p_DetalleOrigenTraspaso.get(0).IdTraspaso);
				List<Long> IdDetalleTraspaso = new ArrayList<>();
				DetalleOrigenTraspaso dtOrigenTraspaso = new DetalleOrigenTraspaso();
				List<DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
				
				List<TraspasoConsultadoEgresos> p = new ArrayList<TraspasoConsultadoEgresos>(); //DEBETENER SOLO IDS TRASPASOS
				
				List<TraspasoConsultadoEgresos> TraspasoConsultado = sql.query(con,SQL_CONSULTAR_TRASPASO_EGRESOS,paramConsultaTraspasoEgresos, TraspasoConsultadoEgresos.class);
				AgregarLogSAAPV(con, in);
				//consulta sobre los traspasos ya existentes y eliminar registro
				for(TraspasoConsultadoEgresos traspasoEgreso : TraspasoConsultado) {
					//IdDetalleTraspaso.add(traspasoEgreso.IdTraspaso);
					p.add(traspasoEgreso);
				}
				int[] resDetalleOrigen = sql.batch(con, SQL_ELIMINAR_DETALLE_ORIGEN_TRASPASO_EGRESO,p, TraspasoConsultadoEgresos.class);
				if(resDetalleOrigen != null && resDetalleOrigen.length > 0 && resDetalleOrigen[0] == 1){
					msg = new MensajeSalida();
					msg.id = 3L;
					msg.tipo = "OK";
					msg.codigo = "OK_ELIMINAR_DETALLE_ORIGEN_TRASPASO";
					msg.descripcion = "Se elimina el Detalle origen traspaso.";
					logger.debug("Elimina Detalle Origen Traspaso OK");	
				}
				
				//insertar nuevo detalle de traspasos de egresos
				for(DetalleOrigenTraspaso TraspsosEgreso: TraspasosNuevos) {
					List<Long> IdsDetalleOrigenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso", 1L);
					dtOrigenTraspaso = new DetalleOrigenTraspaso();
					dtOrigenTraspaso.IdDetalleOrigenTraspaso = IdsDetalleOrigenTraspaso.get(0);
					dtOrigenTraspaso.IdTraspaso = TraspsosEgreso.IdTraspaso;
					dtOrigenTraspaso.CodigoTipoAhorro = TraspsosEgreso.CodigoTipoAhorro;
					dtOrigenTraspaso.IdEntidadRecaudadora = TraspsosEgreso.IdEntidadRecaudadora;
					dtOrigenTraspaso.NombreEntidad = TraspsosEgreso.NombreEntidad;
					dtOrigenTraspaso.DocumentoEntidad = TraspsosEgreso.DocumentoEntidad;
					dtOrigenTraspaso.Fondo = TraspsosEgreso.Fondo;
					dtOrigenTraspaso.MontoTraspaso = TraspsosEgreso.MontoTraspaso;
					dtOrigenTraspaso.UFTraspaso = TraspsosEgreso.UFTraspaso;
					dtOrigenTraspaso.PorcentajeTraspaso = TraspsosEgreso.PorcentajeTraspaso;
					dtOrigenTraspaso.IdEstado = "VIG";
					dtOrigenTraspaso.IdInstrumento = TraspsosEgreso.IdInstrumento;
					dtOrigenTraspaso.IdPortafolio = TraspsosEgreso.IdPortafolio;
					dtOrigenTraspaso.CodigoRegimenTributario = TraspsosEgreso.CodigoRegimenTributario;
					lstOrigenTraspaso.add(dtOrigenTraspaso);
				}
				if (lstOrigenTraspaso.size() > 0) {
					int[] result = sql.batch(con, SQL_CREAR_DETALLE_ORIGEN_TRASPASO, lstOrigenTraspaso, DetalleOrigenTraspaso.class);
					if(result != null && result.length > 0 && result[0] == 1){
						msg = new MensajeSalida();
					    msg.id = lstOrigenTraspaso.get(0).IdTraspaso;
					    msg.tipo = "OK";
					    msg.codigo = "CREACION_ORIGEN_TRASPASO_OK";
					    msg.descripcion = "Creacion del Origen traspaso exitosa";
						salida.P_OUT_MENSAJES.add(msg);
						logger.debug("Crea Detalle Origen Traspaso OK");
					}
				}
				String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
				HashMap<String,Object> parms = new HashMap<>();
				parms.put("FechaModificacion",FechaSistema);
				parms.put("IdTraspaso",in.p_DetalleOrigenTraspaso.get(0).IdTraspaso);
				
				List<TraspasoEgresosConsultado> lstTraspasoModificar = new ArrayList<TraspasoEgresosConsultado>();
				TraspasoEgresosConsultado tpTraspaso = new TraspasoEgresosConsultado();
				tpTraspaso.FechaModificacion = FechaSistema;
				tpTraspaso.NombreEntidad =in.p_Parametros.get(0).NombreEntidadOrigen;
				tpTraspaso.DocumentoEntidad =in.p_Parametros.get(0).RutEntidadOrigen;
				tpTraspaso.TipoTraspaso =in.p_Parametros.get(0).TipoTraspaso;
				//tpTraspaso.PorcentajeTraspaso =in.p_Parametros.get(0).PorcentajeTraspaso;
				//tpTraspaso.MontoTraspaso =in.p_Parametros.get(0).MontoTraspaso;
						
				tpTraspaso.IdTraspaso = in.p_DetalleOrigenTraspaso.get(0).IdTraspaso;
				lstTraspasoModificar.add(tpTraspaso);
				
				int[] result = sql.batch(con, SQL_MODIFICAR_TRASPASO_EGRESOS, lstTraspasoModificar, TraspasoEgresosConsultado.class);
				if(result != null && result.length > 0 && result[0] == 1){
					msg = new MensajeSalida();
				    msg.id = lstOrigenTraspaso.get(0).IdTraspaso;
				    msg.tipo = "OK";
				    msg.codigo = "MODIFICACION_TRASPASO_OK";
				    msg.descripcion = "Creacion del traspaso exitosa";
					salida.P_OUT_MENSAJES.add(msg);
					logger.debug("Crea Traspaso OK");
				}
				//in.p_DetalleOrigenTraspaso tiene los nuevos cosos para agregar
			}
		}
		return salida;
	}
	
	//METODO VALIDA CUMPLE REQUISITOS MINIMOS PARA SER PERSONA,CLIENTE
		public List<String> ValidaCamposPersona(String TipoValidacion,ParametrosContrato Contrato,PortafoliosModificados Portafolio )
		{
			List<String> lstDatosFaltantes = new ArrayList<String>();
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
				if(Contrato.IdComunaCliente == null || Contrato.IdComunaCliente == 0)
					lstDatosFaltantes.add("Id Comuna.");
				if(Contrato.IdCiudadCliente == null || Contrato.IdCiudadCliente == 0)
					lstDatosFaltantes.add("Id Ciudad.");
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
			
			return lstDatosFaltantes;
		}
	
		public void AgregarLogSAAPV(Connection con, ParametrosEntradaServicio in) {
			try {
				
				String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
				Long IdCliente = in.p_Parametros.get(0).IdCliente;
				Long IdPersona = in.p_Parametros.get(0).IdPersona;
				Long IdCabeceraLog = (IdCliente == 0) ? IdPersona : IdCliente;
				String FolioContratoSAAPV = in.p_Parametros.get(0).FolioContratoSAAPV;
				String FechaSuscripcion = in.p_Parametros.get(0).FechaSuscripcion;
				String NombreEntidad = in.p_Parametros.get(0).NombreEntidadOrigen;
				String DocumentoEntidad = in.p_Parametros.get(0).RutEntidadOrigen;
				Long IdTraspaso = in.p_DetalleOrigenTraspaso.get(0).IdTraspaso!=null?in.p_DetalleOrigenTraspaso.get(0).IdTraspaso:0;
				
				{
					//CABECERA
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					
					log.IdCabeceraLog = IdCabeceraLog;
					log.IdTipoLog = "MODIFICACION";
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
					log.NombreProceso = "Modificación Contrato SAAPV";
					
					if (IdCliente == 0) {
						log.IdPersona = IdPersona;
					}
					
					// DETALLE DEL LOG
					if (in.p_Parametros.get(0).TipoMovimiento.equals("EGRESO")) {
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "FolioTraspaso", IdTraspaso.toString()));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "DocumentoEntidad", DocumentoEntidad));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "NombreEntidad", NombreEntidad));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "FechaSuscripcion", FechaSuscripcion));
					}else {
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "FolioContratoSAAPV", FolioContratoSAAPV));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Contrato-SAAPV", "FechaSuscripcion", FechaSuscripcion));
					}
					
					for (int i = 0; i < in.p_DistribucionInversion.size(); i++) {
						String IdInstrumento = Long.toString(in.p_DistribucionInversion.get(i).IdInstrumento);
						String IdEstado = in.p_DistribucionInversion.get(i).IdEstado;
						String MontoPesos = Double.toString(in.p_DistribucionInversion.get(i).MontoPesos);
						String Porcentaje = Double.toString(in.p_DistribucionInversion.get(i).IdInstrumento);
						
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Distribucion-Inversion", "IdInstrumento", IdInstrumento));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Distribucion-Inversion", "IdEstado", IdEstado));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Distribucion-Inversion", "MontoPesos", MontoPesos));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Distribucion-Inversion", "Porcentaje", Porcentaje));
					}
					
					for (int i = 0; i < in.p_PortafoliosModificados.size(); i++) {
						String IdPortafolio = Long.toString(in.p_PortafoliosModificados.get(i).IdPortafolio);
						String Producto = in.p_PortafoliosModificados.get(i).Producto;
						String MesInicio = in.p_PortafoliosModificados.get(i).MesInicio;
						String MesTermino = in.p_PortafoliosModificados.get(i).MesTermino; 
						String MontoPesos = Double.toString(in.p_PortafoliosModificados.get(i).MontoPesos);
						String MontoUF = Double.toString(in.p_PortafoliosModificados.get(i).MontoUF);
						
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "IdPortafolio", IdPortafolio));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "Producto", Producto));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "MesInicio", MesInicio));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "MesTermino", MesTermino));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "MontoPesos", MontoPesos));
						entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "Portafolio-SAAPV", "MontoUF", MontoUF));
					}
						
					entradaAgregarLog.p_Log.add(log);
					
					//REGISTRO
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(con, entradaAgregarLog);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		public  InAgregarLog.DetalleLog agregarDetalleLogContratoSAAPV(Long IdCabeceraLog, String SubRegistro, String CampoRegistro, String ValorNuevo) {
			InAgregarLog.DetalleLog DetalleContratoSAAPV = new InAgregarLog.DetalleLog();
			DetalleContratoSAAPV.IdCabeceraLog = IdCabeceraLog; 
			DetalleContratoSAAPV.IdTrabajo = 0L;
			DetalleContratoSAAPV.SubRegistro = SubRegistro; 
			DetalleContratoSAAPV.CampoRegistro = CampoRegistro; 
			DetalleContratoSAAPV.ValorAntiguo = ""; 
			DetalleContratoSAAPV.ValorNuevo = ValorNuevo;
			return DetalleContratoSAAPV;
		}
		
	/********************
	 * Metodo Principal *
	 ********************/

		public static void main(String[] args) throws Exception {
			try (Connection con = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")) {
				try {
					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
					
				//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
					//Long time = System.currentTimeMillis();
					//java.sql.Date FechaSistema = Fecha.toDate("1978-01-01T03:00:00.000Z");
					//Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
					
					//pruebas BDH
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Parametros\":[{\"IdContratoSAAPV\":1006959,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"10009669100\",\"IdPlan\":2,\"NombrePlan\":\"I. ACTIVO\",\"FechaSuscripcion\":\"2022-12-07\",\"FechaCreacion\":\"2024-05-30\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"CodigoTipoSolicitud\":\"TRA\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"MODERADO\",\"IdPerfilComercial\":\"APV INV ACTIVA_MODERADO\",\"IdPersona\":114346,\"IdCliente\":82850,\"IdEmpleador\":0,\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"ALEJANDRO ENRIQUE\",\"ApellidoPaternoCliente\":\"ASTUDILLO\",\"ApellidoMaternoCliente\":\"MARTINEZ\",\"DocumentoIdentidadCliente\":\"10927111-K\",\"FechaNacimientoCliente\":\"1978-08-29\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2931,\"IdComunaCliente\":299,\"DireccionCliente\":\"Quilvo\",\"NumeroDireccionCliente\":\"108\",\"IdTipoCargoPersona\":124,\"TelefonoFijo\":\"\",\"TelefonoMovil\":\"+56990893822\",\"TelefonoLaboral\":\"\",\"EmailCliente\":\"aastu004@gmail.com\",\"RazonSocialEmpleador\":\"\",\"DocumentoIdentidadEmpleador\":\"\",\"IdPaisEmpleador\":0,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":\"\",\"IdCiudadEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"\",\"NombreEncargado\":\"\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":831,\"CodigoPrevision\":\"831\",\"Pensionado\":\"NO\",\"Trabajador\":\"INDEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":1,\"DiaDescuento\":0,\"Institucion\":0,\"ComisionTransferencia\":0,\"TipoMovimiento\":\"INGRESO\",\"RazonRetiro\":\"\",\"NombreEntidadOrigen\":\"AFP CUPRUM S.A\",\"RutEntidadOrigen\":\"76240079-0\",\"IdPersonaOrigen\":61704,\"TipoDeAhorro\":0,\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"TOT\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"Descripcion\":\"\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":335,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[{\"FolioContratoSAAPV\":\"10009669100\",\"IdInstrumento\":1021327,\"RazonSocial\":\"SURA ESTRUCTURADO UF V CHILE C\",\"Nemotecnico\":\"00391CCLP\",\"MontoPesos\":0,\"Porcentaje\":100,\"IdEstado\":\"VIG\"}],\"p_ParametrosModificados\":0,\"p_DetalleOrigenTraspaso\":[{\"IdDetalleOrigenTraspaso\":0,\"IdContratoSAAPVOrigen\":1006959,\"IdEmpresa\":1,\"CodigoTipoAhorro\":0,\"IdEntidadRecaudadora\":831,\"NombreEntidad\":\"AFP CUPRUM S.A\",\"DocumentoEntidad\":\"76240079-0\",\"Fondo\":\"\",\"MontoTraspaso\":5000000,\"UFTraspaso\":0,\"PorcentajeTraspaso\":100,\"IdEstado\":\"VIG\",\"IdInstrumento\":0,\"IdPortafolio\":0,\"CodigoRegimenTributario\":0}],\"Canal\":\"OPT\",\"p_PortafoliosModificados\":[{\"CodigoAgrupador\":\"GRUPO 2\",\"IdMatrizAgrupadorAPV\":8,\"OrigenAhorro\":\"APV\",\"IdPortafolio\":480902,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2022-12\",\"MesTermino\":\"2022-12\",\"MontoPesos\":130115,\"MontoUF\":0,\"Pago\":\"UNI\",\"PagoNombre\":\"UNICO\",\"PorcentajeRegimenImponible\":0,\"Regimen\":\"APV-A\",\"Producto\":\"APV Acciones\",\"IdContratoSAAPVOrigen\":1006959},{\"CodigoAgrupador\":\"GRUPO 2\",\"IdMatrizAgrupadorAPV\":12,\"OrigenAhorro\":\"DC\",\"IdPortafolio\":480903,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2022-12\",\"MesTermino\":\"2022-12\",\"MontoPesos\":10894460,\"MontoUF\":0,\"Pago\":\"UNI\",\"PagoNombre\":\"UNICO\",\"PorcentajeRegimenImponible\":0,\"Regimen\":\"DC-R\",\"Producto\":\"APV Acciones\",\"IdContratoSAAPVOrigen\":1006959},{\"CodigoAgrupador\":\"GRUPO 2\",\"IdMatrizAgrupadorAPV\":9,\"OrigenAhorro\":\"APV\",\"IdPortafolio\":480299,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2022-12\",\"MesTermino\":\"2022-12\",\"MontoPesos\":5414505,\"MontoUF\":0,\"Pago\":\"UNI\",\"PagoNombre\":\"UNICO\",\"PorcentajeRegimenImponible\":0,\"Regimen\":\"APV-B\",\"Producto\":\"APV Acciones\",\"IdContratoSAAPVOrigen\":1006959}]}";				
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Parametros\":[{\"IdContratoSAAPV\":436,\"IdEmpresa\":1,\"FolioContratoSAAPV\":988888,\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-09\",\"FechaCreacion\":\"2018-02-10\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"9876\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdPersona\":1774228,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"HANS GERD\",\"ApellidoPaternoCliente\":\"MULLER\",\"ApellidoMaternoCliente\":\"RAMIREZ\",\"DocumentoIdentidadCliente\":\"12578993-5\",\"FechaNacimientoCliente\":\"1974-07-06\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":717,\"IdComunaCliente\":284,\"DireccionCliente\":\"LOS LAGOS\",\"NumeroDireccionCliente\":\"24\",\"IdTipoCargoPersona\":140,\"TelefonoCliente\":\"+56111111111\",\"TelefonoComercialCliente\":\"+56222222222\",\"TelefonoCelularCliente\":\"+56333333333\",\"EmailCliente\":\"HMULLER@PRONETIT.CL\",\"RazonSocialEmpleador\":\"INGENIERIA PRONET IT SPA\",\"DocumentoIdentidadEmpleador\":\"76508113-0\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":\"\",\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"+56 9 90999534\",\"NombreEncargado\":\"HANS MULLER\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":2,\"CodigoPrevision\":\"1003\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorroCV\":\"\",\"OrigenAhorroDC\":\"\",\"MontoAhorroCV\":0,\"UFAhorroCV\":0,\"PorcentajeRegimenImponibleCV\":0,\"MontoAhorroDC\":0,\"UFAhorroDC\":0,\"PorcentajeRegimenImponibleDC\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":3,\"Institucion\":0,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_PortafoliosModificados\":[{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"IdPortafolio\":1450621,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2018-02\",\"MesTermino\":\"9999-01\",\"MontoPeso\":10200,\"MontoUF\":1.5,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Porcentaje\":100,\"Regimen\":\"APV-A\"}],\"ICliente\":1423499}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Parametros\":[{\"IdContratoSAAPV\":436,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"9875   \",\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-09\",\"FechaCreacion\":\"2018-02-09\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"9876   \",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS                \",\"IdPersona\":1774228,\"IdEmpleador\":1773691,\"IdCliente\":1423499,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"HANS GERD\",\"ApellidoPaternoCliente\":\"MULLER\",\"ApellidoMaternoCliente\":\"RAMIREZ\",\"DocumentoIdentidadCliente\":\"12578993-5\",\"FechaNacimientoCliente\":\"1974-07-06\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":717 ,\"IdComunaCliente\":284,\"DireccionCliente\":\"LOS LAGOS\",\"NumeroDireccionCliente\":24,\"IdTipoCargoPersona\":140,\"TelefonoCliente\":\"+56111111111\",\"TelefonoComercialCliente\":\"+56222222222\",\"TelefonoCelularCliente\":\"+56333333333\",\"EmailCliente\":\"HMULLER@PRONETIT.CL\",\"RazonSocialEmpleador\":\"INGENIERIA PRONET IT SPA\",\"DocumentoIdentidadEmpleador\":\"76508113-0\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"+56 9 90999534\",\"NombreEncargado\":\"HANS MULLER\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":2,\"CodigoPrevision\":\"1003\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorroCV\":\"APV\",\"OrigenAhorroDC\":\"\",\"MontoAhorroCV\":10200,\"UFAhorroCV   \":1.5                 ,\"PorcentajeRegimenImponibleCV\":100,\"MontoAhorroDC\":0,\"UFAhorroDC\":0,\"PorcentajeRegimenImponibleDC\":100,\"Pago\":\"IND\",\"PeriodoInicio\":\"2018-02\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"APV-A\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":3,\"DiaDescuento\":5,\"Institucion\":0,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_ParametrosModificados\":[{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2018-03\",\"MesTermino\":\"9999-01\",\"MontoPeso\":140000,\"MontoUF\":5.26,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Porcentaje\":100,\"Regimen\":\"APV-A\"},{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2018-03\",\"MesTermino\":\"9999-01\",\"MontoPeso\":140000,\"MontoUF\":5.26,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Porcentaje\":100,\"Regimen\":\"APV-A\"},{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2018-03\",\"MesTermino\":\"9999-01\",\"MontoPeso\":140000,\"MontoUF\":5.26,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Porcentaje\":100,\"Regimen\":\"APV-A\"}]}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Parametros\":[{\"IdContratoSAAPV\":431,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"1111111\",\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-08\",\"FechaCreacion\":\"2018-02-09\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"1111111\",\"IdCategoriaInversionista\":\"BALANCEADO\",\"IdPerfilComercial\":\"PERFIL_COMERCIAL_MIGRACION_BALANCEADO\",\"IdPersona\":1774220,\"IdEmpleador\":0      ,\"IdCliente\":1423493,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"WWW      \",\"ApellidoPaternoCliente\":\"W     \",\"ApellidoMaternoCliente\":\"W      \",\"DocumentoIdentidadCliente\":\"15091971-1\",\"FechaNacimientoCliente\":\"1956-10-10\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2635,\"IdComunaCliente\":346,\"DireccionCliente\":\"CALLE    \",\"NumeroDireccionCliente\":65,\"IdTipoCargoPersona\":6  ,\"TelefonoCliente\":\"            \",\"TelefonoComercialCliente\":\"+56111111111\",\"TelefonoCelularCliente\":\"+56999999999\",\"EmailCliente\":\"                   \",\"RazonSocialEmpleador\":\"                        \",\"DocumentoIdentidadEmpleador\":null          ,\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"              \",\"NombreEncargado\":\"           \",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":3,\"CodigoPrevision\":\"1005\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorroCV\":\"APV\",\"OrigenAhorroDC\":\"\",\"MontoAhorroCV\":10000,\"MontoAhorroDC\":0  ,\"UFAhorroDC\":0,\"PorcentajeRegimenImponibleDC\":0  ,\"Pago\":\"IND\",\"PeriodoInicio\":\"2018-02\",\"PeriodoTermino\":\"9999-01\",\"RegimenTributario\":\"APV-A\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":1,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdMatrizAgrupadorAPV\":2,\"CodigoAgrupador\":\"GRUPO 1\"}],\"p_DistribucionInversion\":[],\"p_PortafoliosModificados\":[{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupador\":2,\"IdPortafolio\":1450612,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2018-02\",\"MesTermino\":\"2018-02\",\"MontoPesos\":92000,\"MontoUF\":3.46,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"Porcentaje\":100,\"Producto\":\"APV FONDOS MUTUOS\",\"Regimen\":\"APV-A\"}]}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Parametros\":[{\"IdContratoSAAPV\":384,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"1111111\",\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-08\",\"FechaCreacion\":\"2018-02-09\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"1111111\",\"IdCategoriaInversionista\":\"BALANCEADO\",\"IdPerfilComercial\":\"PERFIL_COMERCIAL_MIGRACION_BALANCEADO\",\"IdPersona\":null,\"IdEmpleador\":0,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"WWW\",\"ApellidoPaternoCliente\":\"W\",\"ApellidoMaternoCliente\":\"W\",\"DocumentoIdentidadCliente\":\"15091971-1\",\"FechaNacimientoCliente\":\"1956-10-10\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2635,\"IdComunaCliente\":346,\"DireccionCliente\":\"CALLE\",\"NumeroDireccionCliente\":65,\"IdTipoCargoPersona\":6,\"TelefonoCliente\":\"\",\"TelefonoComercialCliente\":\"+56111111111\",\"TelefonoCelularCliente\":\"+56999999999\",\"EmailCliente\":\"\",\"RazonSocialEmpleador\":\"\",\"DocumentoIdentidadEmpleador\":null,\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"\",\"NombreEncargado\":\"\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":2,\"IdEntidadRecaudadora\":3,\"CodigoPrevision\":\"1005\",\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorroCV\":\"APV\",\"OrigenAhorroDC\":\"\",\"MontoAhorroCV\":10000,\"MontoAhorroDC\":0,\"UFAhorroDC\":0,\"PorcentajeRegimenImponibleDC\":0,\"Pago\":\"IND\",\"PeriodoInicio\":\"2018-02\",\"PeriodoTermino\":\"9999-01\",\"RegimenTributario\":\"APV-A\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":1,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdMatrizAgrupadorAPV\":2,\"CodigoAgrupador\":\"GRUPO 1\"}],\"p_DistribucionInversion\":[]}";
					
					ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
					
					ModificarContratoSAAPV demo = new ModificarContratoSAAPV();	
					
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