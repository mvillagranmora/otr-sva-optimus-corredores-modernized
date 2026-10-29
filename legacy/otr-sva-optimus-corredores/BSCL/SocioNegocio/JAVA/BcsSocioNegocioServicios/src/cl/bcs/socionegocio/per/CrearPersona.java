package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.SalidaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatca.EntradaCrearFatca;
import cl.bcs.socionegocio.per.pojo.Fatca;
import cl.bcs.socionegocio.per.CrearFatca.SalidaCrearFatca;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.EntradaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.SalidaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.EntradaCrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.SalidaCrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearTelefono.EntradaCrearTelefono;
import cl.bcs.socionegocio.per.CrearTelefono.SalidaCrearTelefono;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaValidarConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.SalidaValidarConyuge;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.socionegocio.utils.ValidacionesSocioNegocio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;
import cl.bcs.utils.Fecha;


public class CrearPersona implements JavaCall{
	
	private Logger logger = LogManager.getLogger(CrearPersona.class);
	
	public String SQL_ConsultarExistenciaEmpleadoUsuario = "SELECT COUNT(1) AS \"Count\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" as USU  INNER JOIN \"BSCL.SocioNegocio.BD::EMP.Empleado\" as EMP ON (EMP.\"IdEmpleado\" = USU.\"IdEntidad\") WHERE USU.\"TipoEntidad\" = 'EMP' and USU.\"IdUsuario\" = :p_IdUsuario and USU.\"IdEmpresa\" = :p_IdEmpresa;";
	public String SQL_ConsultarExistenciaPersona = "SELECT CP.\"IdPersona\", SOC.\"IdSocioDeNegocio\" FROM \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" CP INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" SOC ON SOC.\"IdPersona\" = CP.\"IdPersona\" WHERE CP.\"DocumentoIdentidad\" = :DocumentoIdentidad AND CP.\"TipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CP.\"Tipo\" = (CASE WHEN :CodigoPais='PE' THEN CP.\"Tipo\" ELSE :Tipo END)";
	public String SQL_ConsultarIdPaisCiudad= "SELECT \"IdPais\" AS \"Id\" FROM \"BSCL.Plataforma.BD.Vistas::REG.ConsultarCiudad\" CIU WHERE CIU.\"IdCiudad\" = :IdCiudad ";
	public String SQL_ConsultarPersona = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_SelectPersona.sql");
	public String SQL_ConsultarCiudad = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_SelectCiudad.sql");
	public String SQL_CREAR_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_Insert.sql");
	public String SQL_CREAR_PERSONA_UPDATE = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_Update.sql");
	public String SQL_CREAR_PERSONA_ENROLAMIENTO_UPDATE = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersona_Enrolamiento_Update.sql");
	public String SQL_CREAR_SOCIO_NEGOCIO = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_Insert.sql");
	
	private static final String ID_CONCEPTO = "ENRPER";
	
	public static class EntradaServicioCrearPersona {
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio = "N";
		public List<Enrolamiento> p_ConsultarEnrolamiento;
		public String p_IdTipoCanal;
		
		public EntradaServicioCrearPersona() {
			this.p_IdTipoCanal = "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaExistePersona{
		public long IdPersona;
		public long IdSocioDeNegocio;
	}
	
	public static class Ids{
		public long Id;
	}

	public static class Contador{
		public long Count;
		@Override
		public String toString() {
			return String.format("Contador [Count=%s]", Count);
		}
	}
	
	public static class PersonaLog{
		public Long IdPersona;
		public String TipoPersona;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String PaisNacimiento;
		public String EstadoCivil;
		public String Genero;
		public String PaisResidencia;
		public String RegimenConyugal;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Ciudad{
		public Long IdCiudad;
		public String NombreCiudad;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadPersona extends Enrolamiento{
		public EntidadPersona(){
			super();
		}
		public Long IdPersona;		
		public String IdEstado;
		public String FechaIngreso;
		public String EsEnrolado;
		public Long IdUsuarioCreador;
		
		public Long IdSocioNegocio;
		public String FechaHoraSistema;
		
		public void setFromEnrolamiento(Enrolamiento enr){
			this.IdEnrolamiento  = enr.IdEnrolamiento ;
			this.IdEmpresa  = enr.IdEmpresa ;
			this.IdUsuario  = enr.IdUsuario ;
			this.IdTipoEntidad  = enr.IdTipoEntidad ;
			this.IdEntidadEnrolada  = enr.IdEntidadEnrolada ;
			this.IdTipoDocumentoIdentidad  = enr.IdTipoDocumentoIdentidad ;
			this.IdTipoDocumentoIdentidadAsociado  = enr.IdTipoDocumentoIdentidadAsociado ;
			this.DocumentoIdentidad  = enr.DocumentoIdentidad ;
			this.IdTipoDocumentoIdentidadAdicional  = enr.IdTipoDocumentoIdentidadAdicional ;
			this.DocumentoIdentidadAdicional  = enr.DocumentoIdentidadAdicional ;
			this.Tipo  = enr.Tipo ;
			this.Nombres  = (enr.Nombres!=null) ? enr.Nombres : "" ;
			this.ApellidoPaterno  = (enr.ApellidoPaterno!=null) ? enr.ApellidoPaterno : "" ;
			this.ApellidoMaterno  = (enr.ApellidoMaterno!=null) ? enr.ApellidoMaterno :  "" ;
			this.RazonSocial  = (enr.RazonSocial!=null) ? enr.RazonSocial : "" ;
			this.IdPaisNacimiento  = enr.IdPaisNacimiento ;
			this.IdPaisNacionalidadPrincipal  = Objects.nonNull(enr.IdPaisNacionalidadPrincipal) && enr.IdPaisNacionalidadPrincipal > 0L ? enr.IdPaisNacionalidadPrincipal : enr.IdPaisNacimiento ;
			this.IdPaisResidencia = Objects.nonNull(enr.IdPaisResidencia) ? enr.IdPaisResidencia : 0L;
			this.ResidenciaTributariaExtranjera = enr.ResidenciaTributariaExtranjera;
			this.FechaNacimiento  = enr.FechaNacimiento ;
			this.Sexo  = enr.Sexo ;
			this.EstadoCivil  = enr.EstadoCivil ;
			this.IdCiudad  = enr.IdCiudad ;
			this.IdDivisionPais  = enr.IdDivisionPais ;
			this.IdTipoTelefono  = enr.IdTipoTelefono ;
			this.Telefono  = enr.Telefono ;
			this.Email  = enr.Email ;
			this.DireccionCalle  = enr.DireccionCalle ;
			this.DireccionNumero  = enr.DireccionNumero ;
			this.DireccionDepartamento  = enr.DireccionDepartamento ;
			this.FechaCreacion  = enr.FechaCreacion ;
			this.FechaCreacionStr  = enr.FechaCreacionStr ;
			this.FechaModificacion  = enr.FechaModificacion ;
			this.FechaModificacionStr  = enr.FechaModificacionStr ;
			this.IdPersonaConyuge  = enr.IdPersonaConyuge ;
			this.RegimenConyugal  = enr.RegimenConyugal ;
			this.CLI_IdCategoriaInversionista  = enr.CLI_IdCategoriaInversionista ;
			this.CLI_IdTipoCliente  = enr.CLI_IdTipoCliente ;
			this.CLI_IdEmpleadoEjecutivo  = enr.CLI_IdEmpleadoEjecutivo ;
			this.CLI_CodigoAdicionalHolding  = enr.CLI_CodigoAdicionalHolding ;
			this.CLI_IdClaseCliente  = enr.CLI_IdClaseCliente ;
			this.POR_IdPerfilComercial  = enr.POR_IdPerfilComercial ;
			this.EMP_CodigoEmpleado  = enr.EMP_CodigoEmpleado ;
			this.EMP_CodigoTipoCargoEmpleado  = enr.EMP_CodigoTipoCargoEmpleado ;
			this.EMP_IdEmpleadoSupervisor  = enr.EMP_IdEmpleadoSupervisor ;
			this.EMP_IdSucursal  = enr.EMP_IdSucursal ;
			this.EMP_FechaInicioCargo  = enr.EMP_FechaInicioCargo ;
			this.EMP_FechaTerminoCargo  = enr.EMP_FechaTerminoCargo ;
			this.CNT_CodigoInscripcion  = enr.CNT_CodigoInscripcion ;
			this.CNT_Emisor  = enr.CNT_Emisor ;
			this.CNT_Depositos  = enr.CNT_Depositos ;
			this.CNT_Bolsa  = enr.CNT_Bolsa ;
			this.CNT_Corredora  = enr.CNT_Corredora ;
			this.CNT_CamaraDeCompensacion  = enr.CNT_CamaraDeCompensacion ;
			this.CNT_Banco  = enr.CNT_Banco ;
			this.CNT_Boveda  = enr.CNT_Boveda ;
			this.PRO_IdAreaProveedor  = enr.PRO_IdAreaProveedor ;
			this.EMP_CargoIndefinido = enr.EMP_CargoIndefinido;
			this.EsRutInterno = enr.EsRutInterno;
			this.IdCarga = enr.IdCarga;
			this.IdProfesion = enr.IdProfesion;
			this.IdTipoCanalIngresoCorredor = enr.IdTipoCanalIngresoCorredor;
			this.IdSituacionLaboral = enr.IdSituacionLaboral;
			this.IdGiro = enr.IdGiro;
			this.CodigoRangoRenta = "N".equalsIgnoreCase(enr.Tipo) ? enr.CodigoRangoRenta  : "";
			this.CodigoRangoNivelVenta = "J".equalsIgnoreCase(enr.Tipo) ? enr.CodigoRangoNivelVenta  : "";
			this.CodigoPropositoTransaccion = "N".equalsIgnoreCase(enr.Tipo) ? enr.CodigoPropositoTransaccion : "";

		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCrearPersonaUpdate{
		public Long p_IdDocumentoIdentidad;
		public Long p_IdDocumentoIdentidadAdicional;
		public Long p_IdDireccionElectronicaPrincipal;
		public Long p_IdDireccionFisicaPrincipal;
		public Long p_IdTelefonoPrincipal;
		public Long p_IdPersona;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCrearPersonaUpdateSocioNegocio{
		public Long p_IdPersona;
		public Long p_IdEnrolamiento;
	}
	
	public static class SalidaServicioCrearPersona extends SalidaEstandar{
		public SalidaServicioCrearPersona(){
			super();
			this.P_OUT_REFID = 0;
			this.P_OUT_IDSOCIONEGOCIO = 0;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
		}
		public long P_OUT_REFID;
		public long P_OUT_IDSOCIONEGOCIO;
		public long P_OUT_IDENROLAMIENTO;
		public String P_OUT_DOCUMENTOIDENTIDAD="";
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioCrearPersona in = gson.fromJson(jin.toString(), EntradaServicioCrearPersona.class);
		SalidaServicioCrearPersona out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public SalidaServicioCrearPersona logica(Connection conn, EntradaServicioCrearPersona in) throws Exception{
		logger.info("INICIO Servicio crear personas");
		SalidaServicioCrearPersona salida = new SalidaServicioCrearPersona();
		ValidacionesSocioNegocio validaciones = new ValidacionesSocioNegocio();
		String BloqueoPorDefecto;
		if(in.p_IdTipoCanal.isEmpty() || in.p_IdTipoCanal.contains("APP") || in.p_IdTipoCanal.contains("WBS") || in.p_IdTipoCanal.contains("NUEVA WEB SURA")){
			BloqueoPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "PER.BloqueoPorDefectoWS");
		}else {
			BloqueoPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "PER.BloqueoPorDefecto");
		}
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		SQL sql = new SQL();
		if(in.p_ConsultarEnrolamiento.size()>0){
			logger.debug("Hay personas que enrolar en el request");
			
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
			String fechaHoraSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
			String horaSistema = FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa);
			int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
	        Fecha.setLocalOffSet(segundosZonaHoraria);
			Timestamp fechaHoraSistemaUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema, horaSistema);
			
			List<Long> listaIdCiudad = new ArrayList<Long>();
			for(Enrolamiento persona : in.p_ConsultarEnrolamiento) {
				if(persona.IdCiudad!=null && !(listaIdCiudad.contains(persona.IdCiudad))) listaIdCiudad.add(persona.IdCiudad);
			}
			
			HashMap <String, Object> paramCiudad = new HashMap <String, Object> ();
			paramCiudad.put("IdCiudad", listaIdCiudad);
			List<Ciudad> selectCiudad = sql.query(conn, SQL_ConsultarCiudad, paramCiudad, Ciudad.class);
			HashMap<String,Object> hCiudad = Utils.List2Hash(selectCiudad, "IdCiudad");

			for(Enrolamiento persona : in.p_ConsultarEnrolamiento){
				logger.debug("Crear persona: " + persona.DocumentoIdentidad);
				if(persona.IdTipoEntidad!=null && Arrays.asList("PERSONA", "CLIENTE", "EMPLEADO", "CONTRAPARTE", "PROVEEDOR").contains(persona.IdTipoEntidad.toUpperCase())){
					logger.debug("Es una entidad valida para enrolar");
					//Consultar usuario tiene asociado un empleado
					if(validaciones.existeEmpleado(conn, in.p_IdUsuario, in.p_IdEmpresa)){
						logger.debug("Existe el empleado en la empresa");
						List<MensajeSalida> validacionConyuge = validarConyuge(conn, in.p_IdEmpresa, in.p_IdUsuario, persona.IdPersonaConyuge);
						List<AdvertenciaDetalle> advertenciasValidacionConyuge = obtenerAdvertenciaMensajeSalida(validacionConyuge);
						List<ErrorDetalle> erroresValidacionConyuge = obtenerErrorMensajeSalida(validacionConyuge);
						salida.P_OUT_ADVERTENCIASDETALLE.addAll(advertenciasValidacionConyuge);
						salida.P_OUT_ERRORESDETALLE.addAll(erroresValidacionConyuge);
						salida.P_OUT_ADVERTENCIASCONTADOR += advertenciasValidacionConyuge.size();
						salida.P_OUT_ERRORESCONTADOR += erroresValidacionConyuge.size();
						if(salida.P_OUT_ERRORESCONTADOR > 0) {
							return salida;
						}
						// Validar si el cliente existe => Error
						ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
						SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, in.p_IdEmpresa, persona.IdTipoEntidad, persona.IdTipoDocumentoIdentidad, persona.DocumentoIdentidad, persona.Tipo);
						if(documentoValido.Valido.equals("S")){
							logger.debug("Documento de identidad valido");
							
							logger.debug("Consultar si requiere autorizacion");
							String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
							String esEnrolado = (requiereAutorizacion.equals("S")) ? "P" : "S";
							
							logger.debug("Identificar si el documento de identidad esta asociado a una persona.");
							HashMap<String, Object> params = new HashMap<>();
							params.put("DocumentoIdentidad", persona.DocumentoIdentidad);
							params.put("TipoDocumentoIdentidad", persona.IdTipoDocumentoIdentidad);
							params.put("Tipo", persona.Tipo);
							params.put("CodigoPais", documentoValido.CodigoPais);
							logger.debug("INICIO::SQL_ConsultarExistenciaPersona");
							List<ConsultaExistePersona> contadorPersona = sql.query(conn, SQL_ConsultarExistenciaPersona, params, ConsultaExistePersona.class);
							logger.debug("FIN::SQL_ConsultarExistenciaPersona");
							boolean existePersona = (contadorPersona.isEmpty()) ? false : true;

							if(existePersona){
								logger.debug("salir porque la persona ya existe!!");
								salida.P_OUT_REFID = contadorPersona.get(0).IdPersona;
								salida.P_OUT_IDSOCIONEGOCIO = contadorPersona.get(0).IdSocioDeNegocio;
								salida.P_OUT_IDENROLAMIENTO = 0L;
								//errores por si se llamo directo al servicio de creacion de persona
								if("PERSONA".equalsIgnoreCase(persona.IdTipoEntidad)) {
									salida.P_OUT_ERRORESCONTADOR++;
									salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(persona.IdTipoEntidad+"_EXISTE",persona.IdTipoEntidad+"_EXISTE"));	
								}
								return salida;
							}
							
							logger.debug("Persona no existe, crear");	
							//ids para cada entidad
							Long idSocioNegocio = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio", (long) 1).get(0);
							Long idPersona = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Persona", (long) 1).get(0);
							Long idDocumentoIdentidad = null;
							
							Long idDireccionFisica = null;
							Long idDireccionElectronica = null;
							Long idTelefono = null;
							
							EntidadPersona entidadPersona = new EntidadPersona();
							entidadPersona.setFromEnrolamiento(persona);
							entidadPersona.IdPersona = idPersona;
							
							entidadPersona.IdEstado = (BloqueoPorDefecto.equals("S"))? "BLO":"ACT";
							entidadPersona.FechaIngreso = fechaHoraSistemaUTC.toString();
							entidadPersona.EsEnrolado = esEnrolado;
							entidadPersona.IdUsuario = in.p_IdUsuario;
							logger.debug("INICIO::SQL_CREAR_PERSONA");
							int[] res = sql.batch(conn, SQL_CREAR_PERSONA, Arrays.asList(entidadPersona), EntidadPersona.class);
							logger.debug("FIN::SQL_CREAR_PERSONA");
							
							List<Ids> idsPaisCiudad = new ArrayList<Ids>();
							
							if(persona.IdCiudad!=null && persona.IdCiudad!=0){
								params = new HashMap<>();
								params.put("IdCiudad", persona.IdCiudad);
								logger.debug("INICIO::SQL_ConsultarIdPaisCiudad");
								idsPaisCiudad  = sql.query(conn, SQL_ConsultarIdPaisCiudad, params, Ids.class);
								logger.debug("FIN::SQL_ConsultarIdPaisCiudad");
								entidadPersona.IdPaisResidencia = idsPaisCiudad.get(0).Id;
							}
							
							if(res!=null && res.length>0 && res[0]==1){
								System.out.println("RES PER: " + idPersona);
								
								logger.debug("se creo la persona, crear socio de negocio.");
								entidadPersona.IdSocioNegocio = idSocioNegocio;
								logger.debug("INICIO::SQL_CREAR_SOCIO_NEGOCIO");
								res = sql.batch(conn, SQL_CREAR_SOCIO_NEGOCIO, Arrays.asList(entidadPersona), EntidadPersona.class);
								logger.debug("FIN::SQL_CREAR_SOCIO_NEGOCIO");
								System.out.println("RES SOC: " + ((res!=null && res.length>0 && res[0]==1) ? idSocioNegocio : false ));
								logger.debug("Crear fatca");
								entidadPersona.FechaHoraSistema = fechaHoraSistema;

								SalidaCrearFatca salidaCrearFatca = crearFatcaDefecto(conn, in, entidadPersona);
								if(salidaCrearFatca.P_OUT_ERRORESCONTADOR > 0) {
									salida.P_OUT_ERRORESCONTADOR += salidaCrearFatca.P_OUT_ERRORESCONTADOR;
									salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_ERROR_CREAR_FATCA","UI_ERROR_CREAR_FATCA"));
								}

								if(entidadPersona.IdPaisResidencia != 0 && entidadPersona.IdPaisResidencia.compareTo(idPaisSistema) != 0){									
									SalidaCrearFatcaPaisResidencia salidaCrearFatcaPaisResidencia = crearFatcaPaisResidencia(conn, in.p_IdEmpresa, in.p_IdUsuario, entidadPersona);									
									if(salidaCrearFatcaPaisResidencia.P_OUT_ERRORESCONTADOR > 0) {
										salida.P_OUT_ERRORESCONTADOR += salidaCrearFatcaPaisResidencia.P_OUT_ERRORESCONTADOR;
										salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_ERROR_CREAR_FATCA_PAIS_RESIDENCIA","UI_ERROR_CREAR_FATCA_PAIS_RESIDENCIA"));
									}
								}
								
								logger.debug("Ingresar documento identidad");
								CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
								EntradaCrearDocumentoIdentidad entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
								entradaDocumentoIdentidad.p_IdUsuario = in.p_IdUsuario;
								entradaDocumentoIdentidad.p_IdEmpresa = in.p_IdEmpresa;
								entradaDocumentoIdentidad.p_IdModulo = in.p_IdModulo;
								entradaDocumentoIdentidad.p_UsaReglasNegocio = "N";
								
								entradaDocumentoIdentidad.p_IdPersona = idPersona;
								entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidad = persona.IdTipoDocumentoIdentidad;
								entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = persona.IdTipoDocumentoIdentidadAsociado;
								entradaDocumentoIdentidad.p_DocumentoIdentidad = persona.DocumentoIdentidad;
								entradaDocumentoIdentidad.p_NumeroSerie = persona.NumeroSerie;
								entradaDocumentoIdentidad.p_FechaEmision = persona.FechaEmision;
								entradaDocumentoIdentidad.p_FechaVencimiento = persona.FechaVencimiento;
								entradaDocumentoIdentidad.p_TipoPersona = persona.Tipo;
								
								SalidaCrearDocumentoIdentidad salidaDocumentoIdentidad = crearDocumentoIdentidad.logica(conn, entradaDocumentoIdentidad);
								if(salidaDocumentoIdentidad.P_OUT_ERRORESCONTADOR==0){
									logger.debug("Documento de identidad creado");
									idDocumentoIdentidad = salidaDocumentoIdentidad.p_out_RefId;
									salida.P_OUT_DOCUMENTOIDENTIDAD = entradaDocumentoIdentidad.p_DocumentoIdentidad;
									System.out.println("RES DOC: " + idDocumentoIdentidad);
								}else{
									for (ErrorDetalle error : salidaDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
										salida.addError(error.Codigo, error.Descripcion);
									}
									return salida;
								}

								//ingresar direccion fisica
								if ((persona.IdDivisionPais != null && persona.IdDivisionPais != 0 && persona.IdCiudad != null && persona.IdCiudad != 0) || (Long.valueOf(0).equals(persona.IdDivisionPais) && persona.IdCiudad != 0)) {
									logger.debug("Ingresar direccion fisica");
									CrearDireccionFisica crearDireccionFisica = new CrearDireccionFisica();
									EntradaCrearDireccionFisica entradaDireccionFisica = new EntradaCrearDireccionFisica();
									entradaDireccionFisica.p_IdUsuario = in.p_IdUsuario;
									entradaDireccionFisica.p_IdEmpresa = in.p_IdEmpresa;
									entradaDireccionFisica.p_IdModulo = in.p_IdModulo;
									entradaDireccionFisica.p_UsaReglasNegocio = "N";
									
									entradaDireccionFisica.p_Calle = persona.DireccionCalle;
									entradaDireccionFisica.p_Numero = persona.DireccionNumero;
									entradaDireccionFisica.p_Departamento = persona.DireccionDepartamento;
									entradaDireccionFisica.p_Sector = "";
									entradaDireccionFisica.p_CodigoPostal = "";
									entradaDireccionFisica.p_IdCiudad = persona.IdCiudad;
									entradaDireccionFisica.p_IdDivisionPais = persona.IdDivisionPais;
									entradaDireccionFisica.p_Tipo = persona.Tipo;
									entradaDireccionFisica.p_Comentario = "";
									entradaDireccionFisica.p_IdEstado = "ACT";
									entradaDireccionFisica.p_IdPersona = idPersona;
									entradaDireccionFisica.p_Predeterminado = "S";
									
									SalidaCrearDireccionFisica salidaDireccionFisica = crearDireccionFisica.logica(conn, entradaDireccionFisica);
									if(salidaDireccionFisica.P_OUT_ADVERTENCIASCONTADOR==0 && salidaDireccionFisica.P_OUT_ERRORESCONTADOR==0){
										logger.debug("direccion fisica creada");
										idDireccionFisica = salidaDireccionFisica.p_out_RefId;
										System.out.println("RES DIR FIS: " + idDireccionFisica);
									}else{
										for (ErrorDetalle error : salidaDireccionFisica.P_OUT_ERRORESDETALLE) {
											salida.addError(error.Codigo, error.Descripcion);
										}
										return salida;
									}
								}
								
								//ingresar telefono
								if(persona.Telefono!=null && !persona.Telefono.equals("") && persona.IdTipoTelefono!=null && !persona.IdTipoTelefono.equals("")){
									logger.debug("Ingresar telefono");
									if(!idsPaisCiudad.isEmpty()){
										CrearTelefono crearTelefono = new CrearTelefono();
										EntradaCrearTelefono entradadCrearTelefono = new EntradaCrearTelefono();
										entradadCrearTelefono.p_IdUsuario = in.p_IdUsuario;
										entradadCrearTelefono.p_IdEmpresa = in.p_IdEmpresa;
										entradadCrearTelefono.p_IdModulo = in.p_IdModulo;
										entradadCrearTelefono.p_UsaReglasNegocio = "N";
										
										entradadCrearTelefono.p_IdPais = persona.IdPaisResidencia;
										entradadCrearTelefono.p_IdTipoTelefono = persona.IdTipoTelefono;
										entradadCrearTelefono.p_Numero = persona.Telefono;
										entradadCrearTelefono.p_Anexo = "";
										entradadCrearTelefono.p_Tipo = persona.Tipo;
										entradadCrearTelefono.p_IdPersona = idPersona;
										entradadCrearTelefono.p_Predeterminado = "S";
										
										SalidaCrearTelefono salidaCrearTelefono = crearTelefono.logica(conn, entradadCrearTelefono);
										if(salidaCrearTelefono.P_OUT_ADVERTENCIASCONTADOR==0 && salidaCrearTelefono.P_OUT_ERRORESCONTADOR==0){
											logger.debug("Telefono creado");
											idTelefono = salidaCrearTelefono.p_out_RefId;
											System.out.println("RES TEL: " + idTelefono);
										}else{
											for (ErrorDetalle error : salidaCrearTelefono.P_OUT_ERRORESDETALLE) {
												salida.addError(error.Codigo, error.Descripcion);
											}
											return salida;
										}
									}
								}
								
								//ingresar direccion electronica
								if(persona.Email!=null && !persona.Email.equals("")){
									logger.debug("Ingresar direccion electronica");
									CrearDireccionElectronica crearDireccionelectronica = new CrearDireccionElectronica();
									EntradaCrearDireccionElectronica entradaDireccionElectronica = new EntradaCrearDireccionElectronica();
									entradaDireccionElectronica.p_IdUsuario = in.p_IdUsuario;
									entradaDireccionElectronica.p_IdEmpresa = in.p_IdEmpresa;
									entradaDireccionElectronica.p_IdModulo = in.p_IdModulo;
									entradaDireccionElectronica.p_UsaReglasNegocio = "N";
									
									entradaDireccionElectronica.p_Comentario = "";
									entradaDireccionElectronica.p_Direccion = persona.Email;
									entradaDireccionElectronica.p_IdPersona = idPersona;
									entradaDireccionElectronica.p_Predeterminado = "S";
									
									SalidaCrearDireccionElectronica salidaCrearDireccionElectronica = crearDireccionelectronica.logica(conn, entradaDireccionElectronica);
									if(salidaCrearDireccionElectronica.P_OUT_ADVERTENCIASCONTADOR==0 && salidaCrearDireccionElectronica.P_OUT_ERRORESCONTADOR==0){
										logger.debug("direccion electronica creada.");
										idDireccionElectronica = salidaCrearDireccionElectronica.p_out_RefId;
										System.out.println("RES DIR ELE: " + idDireccionElectronica);
									}else{
										for (ErrorDetalle error : salidaCrearDireccionElectronica.P_OUT_ERRORESDETALLE) {
											salida.addError(error.Codigo, error.Descripcion);
										}
										return salida;
									}
								}
								
								logger.debug("actualizar los ids de la persona con cada entidad que fue ingresada");
								EntidadCrearPersonaUpdate updatePersona = new EntidadCrearPersonaUpdate();
								updatePersona.p_IdDireccionElectronicaPrincipal = idDireccionElectronica;
								updatePersona.p_IdDireccionFisicaPrincipal = idDireccionFisica;
								updatePersona.p_IdDocumentoIdentidad = idDocumentoIdentidad;
								updatePersona.p_IdPersona = idPersona;
								updatePersona.p_IdTelefonoPrincipal = idTelefono;
								logger.debug("INICIO::SQL_CREAR_PERSONA_UPDATE");
								int[] resUpdatePersona = sql.batch(conn, SQL_CREAR_PERSONA_UPDATE, Arrays.asList(updatePersona), EntidadCrearPersonaUpdate.class);
								logger.debug("FIN::SQL_CREAR_PERSONA_UPDATE");
								if(resUpdatePersona!=null && resUpdatePersona.length>0 && resUpdatePersona[0]==1){
									logger.debug("ids de la persona actualizados");
									
									registarLog(conn, sql, entidadPersona, in, fechaSistema, hCiudad);
									
									salida.P_OUT_REFID = idPersona;
								}else{
									salida.addError("UI_ERROR_CREAR_PERSONA", "UI_ERROR_CREAR_PERSONA");
								}
								
								salida.P_OUT_IDSOCIONEGOCIO = idSocioNegocio;
								
								if(requiereAutorizacion.equals("S") ){
									logger.debug("Crear evento de autorizacion.");	
									
									CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
									CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
									entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
									entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
									entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
									entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
									entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
									
									CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
									evento.IdTipoEvento = ID_CONCEPTO;
									evento.IdModuloSolicita = in.p_IdModulo;
									evento.IdUsuarioSolicita = in.p_IdUsuario;
									evento.Fecha = fechaSistema;
									evento.IdConcepto = ID_CONCEPTO;
									evento.NumeroOperacionConcepto = String.valueOf(idPersona);
									evento.FechaOperacionConcepto = fechaSistema;
									evento.IdCliente = idPersona;
									evento.Glosa = (persona.Tipo.equals("J")) ? persona.DocumentoIdentidad + " / " + persona.RazonSocial : persona.DocumentoIdentidad + " / " + persona.Nombres + " " + persona.ApellidoPaterno + " " + persona.ApellidoMaterno;
									
									entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
									
									CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn, entradaEvtAutorizacion);
									salida.addAdvertencia("REQUIERE_AUTORIZACION_PERSONA", Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION_PERSONA", new HashMap<>()));
									
									for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
										salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
									}
									for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
										salida.addError(error.Codigo, error.Descripcion);
									}
								}
							}else{
								salida.P_OUT_ERRORESCONTADOR++;
								salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PERSONA_NO_PUEDE_SER_ENROLADO","PERSONA_NO_PUEDE_SER_ENROLADO"));
								logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
							}
							
						}else{
							salida.P_OUT_ERRORESCONTADOR++;
							salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(persona.IdTipoEntidad+"_EXISTE",persona.IdTipoEntidad+"_EXISTE"));
							logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
						}
					}else{
						salida.P_OUT_ERRORESCONTADOR++;
						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("USUARIO_SIN_EMPLEADO_ASOCIADO","USUARIO_SIN_EMPLEADO_ASOCIADO"));
						logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
					}
				}else{
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("TIPO_SOCIO_NEGOCIO_INVALIDO","TIPO_SOCIO_NEGOCIO_INVALIDO"));
					logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
				}
			}
		}else{
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PERSONA_NO_PUEDE_SER_ENROLADO","PERSONA_NO_PUEDE_SER_ENROLADO"));
			logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
		}
		
		logger.info("TERMINO Servicio crear personas");	 
		return salida;
	}
	
	
	private void registarLog(Connection conn, SQL sql, EntidadPersona persona, EntradaServicioCrearPersona in, String fechaSistema, HashMap<String,Object> ciudades) {
		logger.debug("INICIO::CrearPersona.RegistrarLog");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = new ArrayList<Long>();
		idsPersona.add(persona.IdPersona);
		if(persona.IdPersonaConyuge!=null && persona.IdPersonaConyuge.compareTo(0L) != 0) idsPersona.add(persona.IdPersonaConyuge);
		HashMap<String,Object> paramsPersona = new HashMap<>();

		paramsPersona.put("IdPersona", idsPersona);
		
		try {
			List<PersonaLog> selectPersona = sql.query(conn, SQL_ConsultarPersona, paramsPersona, PersonaLog.class);
			HashMap<String,Object> hPersona = Utils.List2Hash(selectPersona, "IdPersona");
			
			AgregarLog agregarLogService = new AgregarLog();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			Long IdTrabajo = (long)(Math.random()*10000);

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			entradaAgregarLog.p_IdTrabajo = IdTrabajo;
			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = persona.IdPersona;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = in.p_IdModulo;
			log.IdTipoRegistro = "PERSONA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			String nombreCompleto = (persona.Nombres!=null && persona.Nombres!="") ? persona.Nombres+" " : "";
			nombreCompleto += (persona.ApellidoPaterno!=null && persona.ApellidoPaterno!="") ? persona.ApellidoPaterno+" " : "";
			nombreCompleto += (persona.ApellidoPaterno!=null && persona.ApellidoMaterno!="") ? persona.ApellidoMaterno : "";
			log.NombreCliente = nombreCompleto;
			log.IdTipoDocumentoIdentidad = persona.IdTipoDocumentoIdentidad;
			log.IdDocumentoIdentidad = persona.DocumentoIdentidad;
			log.Folio = persona.IdPersona;
			log.NombreProceso = "Ingreso Persona";
			log.EstadoProceso = "Ing";
			if(selectPersona != null && !selectPersona.isEmpty() && selectPersona.get(0) != null) {
				log.IdPersona = selectPersona.get(0).IdPersona != null ? selectPersona.get(0).IdPersona : 0L;
			}
			entradaAgregarLog.p_Log.add(log);
			
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			
			try {
				PersonaLog personaCreada = (PersonaLog) hPersona.get(persona.IdPersona);
				Ciudad ciudad = (Ciudad) ciudades.get(persona.IdCiudad);
				agregarLogDetalle("Ingreso Persona", log, "Nombre Completo", nombreCompleto , entradaAgregarLog.p_LogDetalle);
				agregarLogDetalle("Ingreso Persona", log, "Documento Identidad", persona.IdTipoDocumentoIdentidad+" "+persona.DocumentoIdentidad, entradaAgregarLog.p_LogDetalle);
				if(personaCreada!=null) {
					agregarLogDetalle("Ingreso Persona", log, "Tipo Persona", personaCreada.TipoPersona, entradaAgregarLog.p_LogDetalle);
					agregarLogDetalle("Ingreso Persona", log, "Pais Nacimiento", personaCreada.PaisNacimiento, entradaAgregarLog.p_LogDetalle);
					agregarLogDetalle("Ingreso Persona", log, "Pais Residencia", personaCreada.PaisResidencia, entradaAgregarLog.p_LogDetalle);
					agregarLogDetalle("Ingreso Persona", log, "Genero", personaCreada.Genero, entradaAgregarLog.p_LogDetalle);
				}
				if(ciudad!=null) {
					agregarLogDetalle("Ingreso Persona", log, "Ciudad", ciudad.NombreCiudad, entradaAgregarLog.p_LogDetalle);
				}
				if(persona.FechaNacimiento!=null&&!(persona.FechaNacimiento.toString().compareToIgnoreCase("0001-01-01") == 0)) {
					agregarLogDetalle("Ingreso Persona", log, "Fecha Nacimiento", persona.FechaNacimiento.toString(), entradaAgregarLog.p_LogDetalle);
				}
				String direccion = (persona.DireccionCalle!=null && persona.DireccionCalle!="")? persona.DireccionCalle+" ": "";
				direccion += (persona.DireccionNumero!=null && persona.DireccionNumero!="")? persona.DireccionNumero+" ": "";
				direccion += (persona.DireccionDepartamento!=null && persona.DireccionDepartamento!="") ? persona.DireccionDepartamento : "";
				agregarLogDetalle("Ingreso Persona", log, "Direccion", direccion, entradaAgregarLog.p_LogDetalle);
				String telefono = (persona.IdTipoTelefono!=null && persona.IdTipoTelefono!="")? persona.IdTipoTelefono+" " : "";
				telefono += (persona.Telefono!=null && persona.Telefono!="")? persona.Telefono: "";
				agregarLogDetalle("Ingreso Persona", log, "Telefono", telefono ,entradaAgregarLog.p_LogDetalle);
				agregarLogDetalle("Ingreso Persona", log, "Email", persona.Email ,entradaAgregarLog.p_LogDetalle);
				if(persona.IdPersonaConyuge!=null && persona.IdPersonaConyuge.compareTo(0L)!=0) {
					PersonaLog personaConyuge = (PersonaLog) hPersona.get(persona.IdPersonaConyuge);
					if(personaConyuge!=null) {
						String estadoCivil = (personaCreada!=null && personaCreada.EstadoCivil!=null && personaCreada.EstadoCivil!="")? personaCreada.EstadoCivil+" "+"CON"+" ": "";  
						estadoCivil += (personaConyuge.NombreCompleto!=null && personaConyuge.NombreCompleto!="") ? personaConyuge.NombreCompleto+" "+"EN"+" " : "" ;
						estadoCivil += (personaCreada.RegimenConyugal!=null && personaCreada.RegimenConyugal!="")?personaCreada.RegimenConyugal : "";
						agregarLogDetalle("Ingreso Persona", log,"Estado Civil", estadoCivil, entradaAgregarLog.p_LogDetalle);
					}
				}else {
					agregarLogDetalle("Ingreso Persona", log,"Estado Civil", personaCreada.EstadoCivil, entradaAgregarLog.p_LogDetalle);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			agregarLogService.logica(conn, entradaAgregarLog);
		}catch(Exception e) {
			e.printStackTrace();
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.RegistrarLog", fin - inicio);
	}
	
	
	 private void agregarLogDetalle(String Movimiento, AgregarLog.InAgregarLog.Log log, String nombreCampo, String campo, List<AgregarLog.InAgregarLog.DetalleLog> ListaDetalleLog) {
		AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
		detalleLog.IdTrabajo = log.IdTrabajo;
		detalleLog.IdCabeceraLog = log.IdCabeceraLog;
		detalleLog.SubRegistro = Movimiento.trim();
		if(campo != null) {
			if(campo.trim().length() > 0 && !(campo.trim() == "")) {
				detalleLog.CampoRegistro = nombreCampo.trim();
				detalleLog.ValorAntiguo = "";
				detalleLog.ValorNuevo = campo.trim();
				ListaDetalleLog.add(detalleLog);
			}
		}
	}
	
	private SalidaCrearFatca crearFatcaDefecto(Connection con, EntradaServicioCrearPersona entrada, EntidadPersona entidadPersona) throws Exception{
		logger.debug("INICIO::CrearPersona.crearFatcaDefecto");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearFatca salidaCrearFatca = new SalidaCrearFatca();
		
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		
		List<Fatca> fatcas = new ArrayList<Fatca>();
		Fatca fatca = new Fatca();
		fatca = fatca.FatcaPorDefecto();
		fatca.IdPersona = entidadPersona.IdPersona;
		fatca.IdClasificacionEmpresaFatca = "J".equals(entidadPersona.TipoEnrolamiento) ? "PENDIENTE" : "";
		fatca.ExcepcionResidente = paisUsa.IdPais.equals(entidadPersona.IdPaisResidencia) ? "S" : "N";
		fatca.ExcepcionNacimiento = paisUsa.IdPais.equals(entidadPersona.IdPaisNacimiento) ? "S" : "N";

		fatcas.add(fatca);
		
		CrearFatca crearFatca = new CrearFatca();
		EntradaCrearFatca entradaCrearFatca = new EntradaCrearFatca();
		entradaCrearFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearFatca.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearFatca.p_Fatcas = fatcas;
		
		salidaCrearFatca = crearFatca.logica(con, entradaCrearFatca);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.crearFatcaDefecto", fin - inicio);
		return salidaCrearFatca;
	}
	
	private SalidaCrearFatcaPaisResidencia crearFatcaPaisResidencia(Connection con, Long idEmpresa, Long idUsuario, EntidadPersona entidadPersona) throws Exception {
		logger.debug("INICIO::CrearPersona.crearFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaCrearFatcaPaisResidencia salidaCrearFatcaPaisResidencia = new SalidaCrearFatcaPaisResidencia();
		
		List<FatcaPaisResidencia> fatcasPaisResidencia = new ArrayList<FatcaPaisResidencia>();	
		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
		fatcaPaisResidencia.IdPersona = entidadPersona.IdPersona;
		fatcaPaisResidencia.IdPaisResidencia = entidadPersona.IdPaisResidencia;
		fatcaPaisResidencia.TIN = "";
		fatcaPaisResidencia.IdCausaNoTin = "OTRA";
		fatcaPaisResidencia.Observacion = "Pendiente de ingreso";
		fatcaPaisResidencia.PaisResidenciaPrincipal = "S";
		fatcasPaisResidencia.add(fatcaPaisResidencia);		
				
		CrearFatcaPaisResidencia crearFatcaPaisResidencia = new CrearFatcaPaisResidencia();
		EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia = new EntradaCrearFatcaPaisResidencia();
			
		entradaCrearFatcaPaisResidencia.p_IdEmpresa = idEmpresa;
		entradaCrearFatcaPaisResidencia.p_IdUsuario = idUsuario;
		entradaCrearFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;

		salidaCrearFatcaPaisResidencia = crearFatcaPaisResidencia.logica(con, entradaCrearFatcaPaisResidencia);			
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.crearFatcaPaisResidencia", fin - inicio);
		return salidaCrearFatcaPaisResidencia;
	}
	
	private List<MensajeSalida> validarConyuge(Connection con, Long idEmpresa, Long idUsuario, Long idPersonaConyuge) throws Exception{
		logger.debug("INICIO::CrearPersona.validarConyuge");
		long inicio = System.currentTimeMillis();
		
		EntradaValidarConyuge entradaValidarConyuge = new EntradaValidarConyuge();
		entradaValidarConyuge.p_IdEmpresa = idEmpresa;
		entradaValidarConyuge.p_IdUsuario = idUsuario;
		entradaValidarConyuge.p_IdModulo = "SOC";
		entradaValidarConyuge.p_UsaReglasNegocio = "S";
		
		EntradaConyuge entradaConyuge = new EntradaConyuge();
		entradaConyuge.IdInterno = 1L;
		entradaConyuge.IdPersona = 0L;
		entradaConyuge.IdPersonaConyuge = idPersonaConyuge;
		
		entradaValidarConyuge.p_Conyuges = Arrays.asList(entradaConyuge);
		
		ValidarConyuge validarConyuge = new ValidarConyuge();
		SalidaValidarConyuge salidaValidarConyuge = validarConyuge.logica(con, entradaValidarConyuge);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.validarConyuge", fin - inicio);
		return salidaValidarConyuge.P_OUT_MENSAJES;
	}
	
	public List<AdvertenciaDetalle> obtenerAdvertenciaMensajeSalida(List<MensajeSalida> mensajesSalida){
		logger.debug("INICIO::CrearPersona.obtenerAdvertenciaMensajeSalida");
		long inicio = System.currentTimeMillis();
		List<AdvertenciaDetalle> advertencias = mensajesSalida
				.stream()
				.filter(mensajeSalida->MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensajeSalida.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensajeSalida.tipo))
				.map(mensajeSalida -> {
					AdvertenciaDetalle advertenciaDetalle = new AdvertenciaDetalle();
					advertenciaDetalle.Codigo = mensajeSalida.codigo;
					advertenciaDetalle.Descripcion = mensajeSalida.descripcion;
					advertenciaDetalle.RequiereAutorizacion = MensajeSalida.MENSAJE_AUTORIZACION.equals(mensajeSalida.tipo) ? "S" : "N";
					return advertenciaDetalle;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.obtenerAdvertenciaMensajeSalida", fin - inicio);
		return advertencias;
	}
	
	public List<ErrorDetalle> obtenerErrorMensajeSalida(List<MensajeSalida> mensajesSalida){
		logger.debug("INICIO::CrearPersona.obtenerErrorMensajeSalida");
		long inicio = System.currentTimeMillis();
		List<ErrorDetalle> errores = mensajesSalida
				.stream()
				.filter(mensajeSalida->MensajeSalida.MENSAJE_ERROR.equals(mensajeSalida.tipo))
				.map(mensajeSalida -> {
					ErrorDetalle errorDetalle = new ErrorDetalle();
					errorDetalle.Codigo = mensajeSalida.codigo;
					errorDetalle.Descripcion = mensajeSalida.descripcion;
					return errorDetalle;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersona.obtenerErrorMensajeSalida", fin - inicio);
		return errores;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":21,\"IdTipoEntidad\":\"PERSONA\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"23897602-2\",\"IdTipoDocumentoIdentidadAdicional\":\"\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"PRUEBA 3\",\"ApellidoPaterno\":\"R\",\"ApellidoMaterno\":\"R\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":223,\"FechaNacimiento\":\"1986-11-08\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":54,\"IdDivisionPais\":0,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"+56982136987\",\"Email\":\"FER@GMAIL.COM\",\"DireccionCalle\":\"123\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2021-02-03T15:15:09.397Z\",\"FechaModificacion\":\"2021-02-03T15:15:09.397Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"\",\"CLI_IdTipoCliente\":\"\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":0,\"CLI_CodigoAdicionalHolding\":\"\",\"POR_IdPerfilComercial\":\"\",\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\"}";
			EntradaServicioCrearPersona Objin = gson.fromJson(json, EntradaServicioCrearPersona.class);

			CrearPersona demo = new CrearPersona();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioCrearPersona ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
