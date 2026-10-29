package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.cli.ConfirmarModificarClienteDatosGeneralesMasivo.EntradaConfirmarModificarClienteDatosGeneralesMasivo;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.SalidaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.EntradaModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.SalidaModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.pojo.DatosPersonaDatosGenerales;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;

public class ConfirmarModificarPersonaDatosGeneralesMasivo implements JavaCall, ConfirmarEjecutarCambiosPersona  {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDatosGeneralesMasivo.class);
	
	private String SQL_UPDATE_PORTAFOLIOS = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarRazonSocialPortafolios.sql");
	
	private String SQL_SELECT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ComunesFatcaPaisResidencia_SelectFatcaPaisResidencia.sql");
	private String SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO = loadResourceToString("cl/bcs/socionegocio/per/sql/ComunesFatca_UpdateFatcaExcepcionNacimiento.sql");
	private String SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ComunesFatca_UpdateFatcaExcepcionResidencia.sql");

	private String SQL_UPDATE_DATOS_GENERALES = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_UpdateDatosGenerales.sql");

	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDatosGenerales_SelectPersonaDatosGenerales.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");
	private String SQL_SELECT_TIPOPERSONA =  loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectTipoPersona.sql");
	private String SQL_SELECT_PAIS = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectPais.sql");
	private String SQL_SELECT_SITUACIONLABORAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectSituacionLaboral.sql");
	private String SQL_SELECT_ESTADOCIVIL = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectEstadoCivil.sql");
	private String SQL_SELECT_PROFESION = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectProfesion.sql");
	private String SQL_SELECT_RANGOPATRIMONIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectRangoPatrimonio.sql");
	private String SQL_SELECT_GIRO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectGiro.sql");
	private String SQL_SELECT_GENERO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectGenero.sql");
	private String SQL_SELECT_REGIMENCONYUGAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectRegimenConyugal.sql");
	private String SQL_SELECT_CARGOEMPLEADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDatosGeneralesMasivo_SelectTipoCargoPersona.sql");
	private final String ID_TIPO_ENTIDAD = "PERSONA";
	
	public static class PersonaDatosGeneralesActualizar extends BcsPojo {
		public Long IdPersona;
		public String IdEstadoAutorizacion;
	}
	
	public static class RegistroCambio extends BcsPojo {
		public String CampoModificado;
		public String ValorNuevo;
	}
		
	public static class ExcepcionResidencia extends BcsPojo{
		public Long IdPersona;
		public String ExcepcionResidencia;
	}
	
	public static class ExcepcionNacimiento extends BcsPojo{
		public Long IdPersona;
		public String ExcepcionNacimiento;
	}
	
	public static class TipoPersona{
		public String IdTipoPersona;
		public String Descripcion;	
	}
	
	public static class Pais{
		public Long IdPais;
		public String NombrePais;
	}
	
	public static class SituacionLaboral{
		public String IdSituacionLaboroal;
		public String CodigoSituacionLaboral;
		public String SituacionLaboral;
	}
	
	public static class EstadoCivil{
		public String IdTipoEstadoCivil;
		public String Nombre;
	}
	
	public static class Profesion{
		public Long IdProfesion;
		public String Nombre;
	}
	
	public static class RangoPatrimonio{
		public Long IdRangoPatrimonio;
		public String DescripcionRango;
	}
	
	public static class Giro{
		public Long IdGiro;
		public String Nombre;
	}
	
	public static class Genero{
		public String IdTipoGenero;
		public String Descripcion;
	}
	
	public static class RegimenConyugal{
		public String  IdTipoRegimenConyugal;
		public String Nombre;
	}
	
	public static class TipoCargoEmpleado{
		public String CodigoTipoCargoEmpleado;
		public String NombreTipoCargoEmpleado;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaConfirmarEjecutarCambiosPersona.class);
		SalidaConfirmarEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();

		if (entrada.p_Entidad.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_PERSONA",null,con,entrada.p_IdEmpresa));
	    	out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
	    	out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}

		insertarCambiosEnTablaLogGenerarContratos(solicitudesCambios, con, entrada);
		List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales = obtenerEntidadesDatosGenerales(solicitudesCambios,con);
		
		modificarCampoDatosGenerales(con, datosPersonaDatosGenerales);		
		
		out = administrarFatca(con, datosPersonaDatosGenerales, entrada.p_IdEmpresa,entrada.p_IdUsuario);
		
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		agregarLog(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ObtenerSolicitudesDeCambiosPersona.obtenerSolicitudesDeCambios");
		long inicio = System.currentTimeMillis();

		ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudesDeCambios = new ObtenerSolicitudesDeCambiosPersonaMasivo();
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios.logica(con, entrada, ID_TIPO_ENTIDAD);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ObtenerSolicitudesDeCambiosPersona.obtenerSolicitudesDeCambios", fin - inicio);
		
		return solicitudesCambios;
	}


	private void insertarCambiosEnTablaLogGenerarContratos(List<SolicitudCambio> solicitudesCambios, Connection con, EntradaConfirmarEjecutarCambiosPersona entrada)  throws Exception{
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.insertarCambiosEnTablaLogGenerarContratos");
		long inicio = System.currentTimeMillis();
		
		List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();
    	List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) solicitudesCambios.size());
    	String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
    	for(SolicitudCambio solicitud: solicitudesCambios) {
			if(!solicitud.ValorNuevo.equals(solicitud.ValorAntiguo)) {
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.remove(0);
				logGenerarContrato.CodigoFormulario = "DATOS_GENERALES_PERSONA_";
				logGenerarContrato.CampoBackend = solicitud.CampoModificado;
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = 0L;
				logGenerarContrato.IdPersona = solicitud.IdEntidadOrigenLong;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = fechaSistema;
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Cliente - ConfirmarModificarClienteDatosGeneralesMasivo.java";
				params.add(logGenerarContrato);
			}
		}						
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.insertarCambiosEnTablaLogGenerarContratos", fin - inicio);
    }	
	
	private List<DatosPersonaDatosGenerales> obtenerEntidadesDatosGenerales(List<SolicitudCambio> solicitudesCambios,Connection con) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerEntidadesDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<DatosPersonaDatosGenerales> datosAActualizar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			DatosPersonaDatosGenerales datosPersonaDatosGenerales = new DatosPersonaDatosGenerales();
			List<SolicitudCambio> cambiosPersona = persona.getValue();
			datosPersonaDatosGenerales.IdPersona = persona.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosPersona) {
				datosPersonaDatosGenerales = ValorAntiguo.asignarValorNuevo(datosPersonaDatosGenerales, solicitudCambio.CampoModificado, solicitudCambio.ValorNuevo);
			}
			String esPersonaNatural = armaRazonSocial(con,solicitudesCambios);
			if(esPersonaNatural != null) {
				datosPersonaDatosGenerales = ValorAntiguo.asignarValorNuevo(datosPersonaDatosGenerales, "RazonSocial", esPersonaNatural );
			}
			datosAActualizar.add(datosPersonaDatosGenerales);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerEntidadesDatosGenerales", fin - inicio);
		return datosAActualizar;
	}

	private String armaRazonSocial(Connection con, List<SolicitudCambio> solicitudesCambios)throws Exception {
		
		String ApellidoPaterno = "";
		String ApellidoMaterno = "";
		String Nombres = "";
		Boolean FlagApellidoPaterno = false;
		Boolean  FlagApellidoMaterno = false;
		Boolean  FlagNombres = false;
		String RazonSocial = "";
		String Tipo = "";
		String OriginalApellidoPaterno = "";
		String OriginalApellidoMaterno = "";
		String OriginalNombres = "";
		
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		//se recupera lista de datos modificados en la UI
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			DatosPersonaDatosGenerales datosPersonaDatosGenerales = new DatosPersonaDatosGenerales();
			List<SolicitudCambio> cambiosPersona = persona.getValue();
			datosPersonaDatosGenerales.IdPersona = persona.getKey();
			
			//Consulta los datos originales de la persona por id de persona
			params.put("IdPersona", datosPersonaDatosGenerales.IdPersona);	
			List<DatosPersonaDatosGenerales> datosOriginales = sql.query(con, SQL_SELECT_PERSONA, params, DatosPersonaDatosGenerales.class);
			for (DatosPersonaDatosGenerales p: datosOriginales)
			{
				Tipo = p.Tipo;
				OriginalApellidoPaterno = p.ApellidoPaterno;
				OriginalApellidoMaterno = p.ApellidoMaterno;
				OriginalNombres = p.Nombres;
			}
			//VALIDA QUE LA PERSONA CONSULTADA SEA SOLO PERSONA NATURAL (TIPO = 'N')
			if(Tipo.equals("N"))
			{
				for(SolicitudCambio solicitudCambio : cambiosPersona) //REVISA TODOS LOS CAMBIOS QUE VIENEN EN EL JSON DESDE LA UI
				{
					//valida que en la solicitud de cambios se  modifiquen apellidos o nombres
					if(solicitudCambio.CampoModificado.equals("ApellidoPaterno") ||
							solicitudCambio.CampoModificado.equals("ApellidoMaterno") ||
							solicitudCambio.CampoModificado.equals("Nombres")) 
					{
						//ARMA EL APELLIDO PATERNO
						//valida si hubieron cambios en el apellido paterno
						if(solicitudCambio.CampoModificado.equals("ApellidoPaterno") && solicitudCambio.ValorNuevo != null) {
							if(solicitudCambio.ValorNuevo != "" || solicitudCambio.ValorNuevo != null)
							{
								ApellidoPaterno = solicitudCambio.ValorNuevo + " ";
								FlagApellidoPaterno = true;//marca que se modifico este campo
							}
						}
						else //si no hay cambios, asigna el dato existente en la base de datos
						{
							if(OriginalApellidoPaterno != "" || OriginalApellidoPaterno != null)
							{
								if(FlagApellidoPaterno == false)//valida que no se haya modificado antes el campo
								{
									ApellidoPaterno = OriginalApellidoPaterno + " ";
								}
							}
						}
						//ARMA EL APELLIDO MATERNO 
						//valida si hubieron cambios en el apellido materno
						if(solicitudCambio.CampoModificado.equals("ApellidoMaterno") && solicitudCambio.ValorNuevo != null) {
							if(solicitudCambio.ValorNuevo != "" || solicitudCambio.ValorNuevo != null) 
							{
								ApellidoMaterno = solicitudCambio.ValorNuevo + " ";
								FlagApellidoMaterno = true;//marca que se modifico este campo
							}
						}
						else//si no hay cambios, asigna el dato existente en la base de datos
						{
							if(OriginalApellidoMaterno != "" || OriginalApellidoMaterno != null) 
							{
								if(FlagApellidoMaterno == false) //valida que no se haya modificado antes el campo
								{
									ApellidoMaterno = OriginalApellidoMaterno + " ";
								}
							}
						}
						//ARMA EL NOMBRE
						//valida si hubieron cambios en el nombre
						if(solicitudCambio.CampoModificado.equals("Nombres") && solicitudCambio.ValorNuevo != null) {
							Nombres = solicitudCambio.ValorNuevo;
							FlagNombres = true;
						}
						else 
						{//si no hay cambios asigna el valor existente en base de datos
							if(FlagNombres == false) //valida que no se haya modificado antes el campo
							{
								Nombres = OriginalNombres;
							}
							
						}
					}
					else
					{
						if(FlagApellidoMaterno == false && FlagApellidoPaterno == false && FlagNombres == false) {
							return null; //SI NO MODIFICA APELLIDOS O NOMBRES DEVOLVER NULL
						}
						
					}
					
				}//FIN CICLO FOR
			}
			else
			{
				return null; //SI NO ES PERSONA NATURAL RETORNA NULO
			}
			
		}
		RazonSocial = ApellidoPaterno + ApellidoMaterno + Nombres; // arma razon social
		
		
		return RazonSocial;
	}
	
	private void modificarCampoDatosGenerales(Connection con, List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarCampoDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		if( datosPersonaDatosGenerales.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_DATOS_GENERALES, datosPersonaDatosGenerales, DatosPersonaDatosGenerales.class);	
			
			for(int i=0;i<datosPersonaDatosGenerales.toArray().length;i++) {
				if(datosPersonaDatosGenerales.get(i).RazonSocial != null && datosPersonaDatosGenerales.get(i).IdPersona != null) {
					sql.batch(con, SQL_UPDATE_PORTAFOLIOS, datosPersonaDatosGenerales, DatosPersonaDatosGenerales.class);
					break;
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarCampoDatosGenerales", fin - inicio);
	}
	
	private SalidaConfirmarEjecutarCambiosPersona administrarFatca(Connection con, List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales, Long idEmpresa, Long idUsuario)  throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarCampoDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarEjecutarCambiosPersona = new SalidaConfirmarEjecutarCambiosPersona();
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		List<DatosPersonaDatosGenerales> datosPersonasConPaisResidencia = datosPersonaDatosGenerales
				.parallelStream()
				.filter(datoPersonaDatosGenerales->datoPersonaDatosGenerales.IdPaisResidencia != null)
				.collect(Collectors.toList());
		
		List<DatosPersonaDatosGenerales> datosPersonasConPaisResidenciaDistintoPaisSistema = datosPersonasConPaisResidencia
				.parallelStream()
				.filter(datoPersonaDatosGenerales->datoPersonaDatosGenerales.IdPaisResidencia != idPaisSistema)
				.collect(Collectors.toList());
		
		List<DatosPersonaDatosGenerales> datosPersonasConPaisNacimiento = datosPersonaDatosGenerales
				.parallelStream()
				.filter(datoPersonaDatosGenerales->datoPersonaDatosGenerales.IdPaisNacimiento != null)
				.collect(Collectors.toList());
		
		List<DatosPersonaDatosGenerales> datosPersonasConDistintaResidenciaTributaria = datosPersonaDatosGenerales
				.parallelStream()
				.filter(datoPersonaDatosGenerales->datoPersonaDatosGenerales.ResidenciaTributariaExtranjera != null && datoPersonaDatosGenerales.IdPaisResidencia == null)
				.collect(Collectors.toList());
		
		actualizarExcepcionResidenciaFatca(con, datosPersonasConPaisResidencia);
		if(datosPersonasConDistintaResidenciaTributaria.size() <0) {
			salidaConfirmarEjecutarCambiosPersona = administrarFatcaPaisResidencia(con, idEmpresa,idUsuario, datosPersonasConPaisResidenciaDistintoPaisSistema);
		}
		actualizarExcepcionNacimientoFatca(con, datosPersonasConPaisNacimiento);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarCampoDatosGenerales", fin - inicio);
		return salidaConfirmarEjecutarCambiosPersona;
	}
	
	private SalidaConfirmarEjecutarCambiosPersona administrarFatcaPaisResidencia(Connection con, Long idEmpresa,Long idUsuario, List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.administrarFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaConfirmarEjecutarCambiosPersona out = new SalidaConfirmarEjecutarCambiosPersona();
		SQL sql = new SQL();
		
		List<Long> idsPersona = datosPersonaDatosGenerales.parallelStream().map(persona->persona.IdPersona).collect(Collectors.toList());
		
		HashMap<String,Object> paramsSelect = new HashMap<String,Object>();
		paramsSelect.put("IdPersona", idsPersona);	
	
		List<FatcaPaisResidencia> fatcasPaisResidenciaPrincipal = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA, paramsSelect, FatcaPaisResidencia.class);
		
		List<FatcaPaisResidencia> fatcasPaisResidenciaCreacion = new ArrayList<>();
		List<FatcaPaisResidencia> fatcasPaisResidenciaModificacion = new ArrayList<>();

		for(DatosPersonaDatosGenerales datoPersonaDatosGenerales : datosPersonaDatosGenerales) {
			FatcaPaisResidencia fatcaPaisResidenciaPrincipal = fatcasPaisResidenciaPrincipal
					.parallelStream()
					.filter(fatca->fatca.IdPersona.equals(datoPersonaDatosGenerales.IdPersona))
					.findFirst()
					.orElse(null);
			if(fatcaPaisResidenciaPrincipal == null) {
				FatcaPaisResidencia fatcaPaisResidencia = obtenerFatcaResidencia(datoPersonaDatosGenerales.IdPersona, datoPersonaDatosGenerales.IdPaisResidencia);
				fatcasPaisResidenciaCreacion.add(fatcaPaisResidencia);
			} else if(fatcaPaisResidenciaPrincipal.IdPaisResidencia != datoPersonaDatosGenerales.IdPaisResidencia) {
				FatcaPaisResidencia fatcaPaisResidencia = obtenerFatcaResidencia(datoPersonaDatosGenerales.IdPersona, datoPersonaDatosGenerales.IdPaisResidencia);
				fatcasPaisResidenciaModificacion.add(fatcaPaisResidencia);
			}
		}
		
		if(fatcasPaisResidenciaCreacion.size() > 0) {
			SalidaCrearFatcaPaisResidencia salidaCrearFatcaPaisResidencia = crearFatcaPaisResidencia(con, idEmpresa,idUsuario, fatcasPaisResidenciaCreacion);			
			if(salidaCrearFatcaPaisResidencia.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_ERRORESCONTADOR += salidaCrearFatcaPaisResidencia.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaCrearFatcaPaisResidencia.P_OUT_MENSAJES);
			}
		}
		
		if(fatcasPaisResidenciaModificacion.size() > 0){		
			SalidaModificarFatcaPaisResidencia salidModificarFatcaPaisResidencia = modificarFatcaPaisResidencia(con, idEmpresa,idUsuario, fatcasPaisResidenciaModificacion);
			if(salidModificarFatcaPaisResidencia.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_ERRORESCONTADOR += salidModificarFatcaPaisResidencia.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidModificarFatcaPaisResidencia.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.administrarFatcaPaisResidencia", fin - inicio);
		return out;	
	}
	
	private FatcaPaisResidencia obtenerFatcaResidencia(Long idPersona, Long idPaisResidencia) {
		logger.trace("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.administrarFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		
		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
		fatcaPaisResidencia.IdPersona = idPersona;
		fatcaPaisResidencia.IdPaisResidencia = idPaisResidencia;
		fatcaPaisResidencia.TIN = "";
		fatcaPaisResidencia.IdCausaNoTin = "OTRA";
		fatcaPaisResidencia.Observacion = "Pendiente de ingreso";
		fatcaPaisResidencia.PaisResidenciaPrincipal = "S";
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.administrarFatcaPaisResidencia", fin - inicio);
		return fatcaPaisResidencia;
	}
	
	private SalidaCrearFatcaPaisResidencia crearFatcaPaisResidencia(Connection con, Long idEmpresa,Long idUsuario, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.crearFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaCrearFatcaPaisResidencia salidaCrearFatcaPaisResidencia = new SalidaCrearFatcaPaisResidencia();
		CrearFatcaPaisResidencia crearFatcaPaisResidencia = new CrearFatcaPaisResidencia();
		EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia = new EntradaCrearFatcaPaisResidencia();
		
		entradaCrearFatcaPaisResidencia.p_IdEmpresa = idEmpresa;
		entradaCrearFatcaPaisResidencia.p_IdUsuario = idUsuario;
		entradaCrearFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;
		entradaCrearFatcaPaisResidencia.p_IdUsuario = idUsuario;
		salidaCrearFatcaPaisResidencia = crearFatcaPaisResidencia.logica(con, entradaCrearFatcaPaisResidencia);		
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.crearFatcaPaisResidencia", fin - inicio);
		return salidaCrearFatcaPaisResidencia;
	}
	
	private SalidaModificarFatcaPaisResidencia modificarFatcaPaisResidencia(Connection con, Long idEmpresa,Long idUsuario, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaModificarFatcaPaisResidencia salidaModificarFatcaPaisResidencia = new SalidaModificarFatcaPaisResidencia();
		ModificarFatcaPaisResidencia modificarFatcaPaisResidencia = new ModificarFatcaPaisResidencia();
		EntradaModificarFatcaPaisResidencia entradaModificarFatcaPaisResidencia = new EntradaModificarFatcaPaisResidencia();
		
		entradaModificarFatcaPaisResidencia.p_IdEmpresa = idEmpresa;	
		entradaModificarFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;
		entradaModificarFatcaPaisResidencia.p_IdUsuario = idUsuario;
		
		salidaModificarFatcaPaisResidencia = modificarFatcaPaisResidencia.logica(con, entradaModificarFatcaPaisResidencia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarFatcaPaisResidencia", fin - inicio);
		return salidaModificarFatcaPaisResidencia;
	}
	
	private void actualizarExcepcionNacimientoFatca(Connection con, List<DatosPersonaDatosGenerales> datosGeneralesPersona) throws SQLException {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.actualizarExcepcionNacimientoFatca");
		long inicio = System.currentTimeMillis();
		
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		
		List<ExcepcionNacimiento> excepciones = datosGeneralesPersona
				.parallelStream()
				.map(datoGeneralesPersona->{
					ExcepcionNacimiento excepcionNacimiento = new ExcepcionNacimiento();
					excepcionNacimiento.IdPersona = datoGeneralesPersona.IdPersona; 
					excepcionNacimiento.ExcepcionNacimiento = datoGeneralesPersona.IdPaisNacimiento.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
					return excepcionNacimiento;
				})
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO, excepciones, ExcepcionNacimiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.actualizarExcepcionNacimientoFatca", fin - inicio);
		
	}

	private void actualizarExcepcionResidenciaFatca(Connection con, List<DatosPersonaDatosGenerales> datosGeneralesPersona) throws SQLException {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.actualizarExcepcionResidenciaFatca");
		long inicio = System.currentTimeMillis();
		
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		
		List<ExcepcionResidencia> excepciones = datosGeneralesPersona
				.parallelStream()
				.map(datoGeneralesPersona->{
					ExcepcionResidencia excepcionResidencia = new ExcepcionResidencia();
					excepcionResidencia.IdPersona = datoGeneralesPersona.IdPersona; 
					excepcionResidencia.ExcepcionResidencia = datoGeneralesPersona.IdPaisResidencia.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
					return excepcionResidencia;
				})
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA, excepciones, ExcepcionResidencia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.actualizarExcepcionResidenciaFatca", fin - inicio);	
	}
	
	private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio = new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdTipoEntidad="PERSONA";
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEntidadOrigen= String.valueOf(entrada.p_Entidad.get(0).IdEntidad );
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
    }
	
	private void agregarLog(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		SQL _sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Long> IdsPersona= new ArrayList<Long>();
		List<String> IdsTipoPersona = new ArrayList<String>();
		List<Long> IdPais = new ArrayList<>(); 
		List<String> IdSituacionLaboral = new ArrayList<String>(); 
		List<String> IdsEstadoCivil  = new ArrayList<String>();
		List<Long> IdsProfesion = new ArrayList<Long>();
		List<Long> IdsRangoPatrimonio = new ArrayList<Long>();
		List<Long> IdGiroPrincipal = new ArrayList<Long>();
		List<String> IdSexo = new ArrayList<String>();
		List<String> IdsRegiemnConyugal = new ArrayList<String>();
		List<String> IdsTipoEmpleado = new ArrayList<String>();
		List<String> IdsTransparenciaFiscal = new ArrayList<String>(); 
		
		for(SolicitudCambio soliciCambio: solicitudCambio) {
			if(soliciCambio.CampoModificado==null)continue;
			try {
				if(!(IdsPersona.contains(soliciCambio.IdEntidadOrigenLong))) IdsPersona.add(soliciCambio.IdEntidadOrigenLong);
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdPersonaEmpleador") == 0 || soliciCambio.CampoModificado.compareToIgnoreCase("IdPersonaConyuge")==0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.compareToIgnoreCase("0")==0)) IdsPersona.add(Long.parseLong(soliciCambio.ValorAntiguo));
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.compareToIgnoreCase("0")==0)) IdsPersona.add(Long.parseLong(soliciCambio.ValorNuevo));
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("Tipo")==0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsTipoPersona.contains(soliciCambio.ValorAntiguo)) ) IdsTipoPersona.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsTipoPersona.contains(soliciCambio.ValorNuevo))) IdsTipoPersona.add(soliciCambio.ValorNuevo);
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdSituacionLaboral") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdSituacionLaboral.contains(soliciCambio.ValorAntiguo)) ) IdSituacionLaboral.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdSituacionLaboral.contains(soliciCambio.ValorNuevo))) IdSituacionLaboral.add(soliciCambio.ValorNuevo);
				}	
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdPaisResidencia") == 0 || soliciCambio.CampoModificado.compareToIgnoreCase("IdPaisNacionalidadPrincipal") == 0 || soliciCambio.CampoModificado.compareToIgnoreCase("IdPaisNacimiento") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && (!IdPais.contains(Long.parseLong(soliciCambio.ValorAntiguo)))) IdPais.add(Long.parseLong(soliciCambio.ValorAntiguo));
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && (!IdPais.contains(Long.parseLong(soliciCambio.ValorNuevo)))) IdPais.add(Long.parseLong(soliciCambio.ValorNuevo));
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdTransparenciaFiscal") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsTransparenciaFiscal.contains(soliciCambio.ValorAntiguo))) IdsTransparenciaFiscal.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsTransparenciaFiscal.contains(soliciCambio.ValorNuevo))) IdsTransparenciaFiscal.add(soliciCambio.ValorNuevo);
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("EstadoCivil") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsEstadoCivil.contains(soliciCambio.ValorAntiguo))) IdsEstadoCivil.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsEstadoCivil.contains(soliciCambio.ValorNuevo))) IdsEstadoCivil.add(soliciCambio.ValorNuevo);
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdProfesion") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsProfesion.contains(Long.parseLong(soliciCambio.ValorAntiguo)))) IdsProfesion.add( Long.parseLong(soliciCambio.ValorAntiguo));
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsProfesion.contains(Long.parseLong(soliciCambio.ValorNuevo)))) IdsProfesion.add(Long.parseLong(soliciCambio.ValorNuevo));
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdRangoPatrimonio") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsRangoPatrimonio.contains(Long.parseLong(soliciCambio.ValorAntiguo)))) IdsRangoPatrimonio.add( Long.parseLong(soliciCambio.ValorAntiguo));
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsRangoPatrimonio.contains(Long.parseLong(soliciCambio.ValorNuevo)))) IdsRangoPatrimonio.add(Long.parseLong(soliciCambio.ValorNuevo));
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("IdGiroPrincipal") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdGiroPrincipal.contains(Long.parseLong(soliciCambio.ValorAntiguo)))) IdGiroPrincipal.add( Long.parseLong(soliciCambio.ValorAntiguo));
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdGiroPrincipal.contains(Long.parseLong(soliciCambio.ValorNuevo)))) IdGiroPrincipal.add(Long.parseLong(soliciCambio.ValorNuevo));
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("Sexo") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdSexo.contains(soliciCambio.ValorAntiguo))) IdSexo.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdSexo.contains(soliciCambio.ValorNuevo))) IdSexo.add(soliciCambio.ValorNuevo);
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("RegimenConyugal") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsRegiemnConyugal.contains(soliciCambio.ValorAntiguo))) IdsRegiemnConyugal.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsRegiemnConyugal.contains(soliciCambio.ValorNuevo))) IdsRegiemnConyugal.add(soliciCambio.ValorNuevo);
				}
				if(soliciCambio.CampoModificado.compareToIgnoreCase("CodigoTipoCargoEmpleado") == 0) {
					if(soliciCambio.ValorAntiguo!=null && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorAntiguo.trim().compareToIgnoreCase("")==0) && !(IdsTipoEmpleado.contains(soliciCambio.ValorAntiguo))) IdsTipoEmpleado.add(soliciCambio.ValorAntiguo);
					if(soliciCambio.ValorNuevo!=null && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("null")==0) && !(soliciCambio.ValorNuevo.trim().compareToIgnoreCase("")==0) && !(IdsTipoEmpleado.contains(soliciCambio.ValorNuevo))) IdsTipoEmpleado.add(soliciCambio.ValorNuevo);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		HashMap<String, Object> hPais = obtenerHashPaises(con, _sql, IdPais);
		HashMap<String, Object> hSituacionLaboral = obtenerHashSituacionLaboral(con, _sql, IdSituacionLaboral);
		HashMap<String, Object> hEstadoCivil = obtenerHashEstadoCivil(con, _sql, IdsEstadoCivil);
		HashMap<String, Object> hProfesion = obtenerHashProfesion(con, _sql, IdsProfesion);
		HashMap<String, Object> hRangoPatrimonio = obtenerHashRangoPatrimonio(con, _sql, IdsRangoPatrimonio);
		HashMap<String, Object> hGiro = obtenerHashGiro(con, _sql, IdGiroPrincipal);
		HashMap<String, Object> hGenero = obtenerHashGenero(con, _sql, IdSexo);
		HashMap<String, Object> hRegimenConyugal = obtenerHashRegimenConyugal(con, _sql, IdsRegiemnConyugal);
		HashMap<String, Object> hCargoEmpleado = obtenerHashCargoEmpleado(con, _sql, IdsTipoEmpleado);
		HashMap<String, Object> hTipoPersona = obtenerHashTipoPersona(con, _sql, IdsTipoPersona);

		//PREPARA LOG
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdPersona", IdsPersona);
		
		List<DatosPersonaDatosGenerales> listadoPersona = new ArrayList<DatosPersonaDatosGenerales>();
		try {
			listadoPersona = _sql.query(con, SQL_SELECT_PERSONA, params, DatosPersonaDatosGenerales.class);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(!(listadoPersona.isEmpty())) {
			HashMap<String, Object> hPersona = Utils.List2Hash(listadoPersona, "IdPersona");
			if(!(hPersona.isEmpty())) {
				//CABECERA LOG
				Long cont = 0L;
				Long IdTrabajo = (long)(Math.random()*10000);
				entradaAgregarLog.p_IdTrabajo = IdTrabajo;
				for(SolicitudCambio cambio : solicitudCambio) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					//CABECERA LOG
					log.IdCabeceraLog = cambio.IdEntidadOrigenLong;
					if(verificarExistenciaCabeceraLog(log, entradaAgregarLog.p_Log)==false) {
						log.IdTrabajo = IdTrabajo;
						log.IdTipoLog = "MODIFICACION";
						log.IdModulo = entrada.p_IdModulo;
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdTipoRegistro = (cambio.IdTipoEntidad!=null && !(cambio.IdTipoEntidad.compareToIgnoreCase("")==0))? cambio.IdTipoEntidad : "" ;
						log.IdPersona = cambio.IdEntidadOrigenLong;
						if(hPersona.containsKey(cambio.IdEntidadOrigenLong)) {
							DatosPersonaDatosGenerales persona = (DatosPersonaDatosGenerales) hPersona.get(cambio.IdEntidadOrigenLong);
							log.NombreCliente = (persona.NombreCompleto!=null && !(persona.NombreCompleto.compareToIgnoreCase("")==0))? persona.NombreCompleto :"";
							log.IdDocumentoIdentidad = (persona.DocumentoIdentidad!=null && !(persona.DocumentoIdentidad.compareTo("")==0) )? persona.DocumentoIdentidad: "";
						}
						log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
						log.NombreProceso = "Confirmar Solicitud Cambio Datos Generales Persona";
						entradaAgregarLog.p_Log.add(log);
					}
					if(cambio.CampoModificado.compareTo("FechaDefuncion")==0) {
						if(cambio.ValorAntiguo!=null && (cambio.ValorNuevo.toString().trim().compareToIgnoreCase("0001-01-01")==0)) {
							continue;
						}
					}
					//DETALLE LOG
					agregarLogDetalle(log, IdTrabajo, cambio, entradaAgregarLog.p_LogDetalle, hPersona, hSituacionLaboral, hPais, hEstadoCivil,hProfesion, hRangoPatrimonio, hGenero, hGiro, hRegimenConyugal, hCargoEmpleado);
				}
			}
			try {
				if(!(entradaAgregarLog.p_Log.isEmpty())) {
					agregarLogService.logica(con, entradaAgregarLog);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.agregarLog", fin - inicio);
	}
	
	private boolean verificarExistenciaCabeceraLog(AgregarLog.InAgregarLog.Log log, List<AgregarLog.InAgregarLog.Log> ListaDetalleLog) {
		boolean resultado = false;
		for(AgregarLog.InAgregarLog.Log cabeceraLog : ListaDetalleLog) {
			if( log.IdCabeceraLog.compareTo(cabeceraLog.IdCabeceraLog)==0) {
				resultado = true; 
				break;
			}
		}
		return resultado;
	}
	
	private void agregarLogDetalle(AgregarLog.InAgregarLog.Log log, Long idTrabajo, SolicitudCambio cambio,  List<AgregarLog.InAgregarLog.DetalleLog> ListaDetalleLog, HashMap<String, Object> hPersona,
									HashMap<String, Object> hashSituacionLaboral, HashMap<String,Object>hashPais, HashMap<String, Object> hashEstadoCivil, HashMap<String,Object> hashProfesion,
									HashMap<String, Object> hashRangoPatrimonio, HashMap<String, Object> hashGenero, HashMap<String, Object> hGiro,HashMap<String, Object> hRegimenConyugal, HashMap<String, Object> hCargoEmpleado) {
		
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.agregarLogDetalle");
		long inicio = System.currentTimeMillis();
		
		AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
		detalleLog.IdTrabajo = idTrabajo;
		detalleLog.IdCabeceraLog = log.IdCabeceraLog;
		detalleLog.SubRegistro = "PERSONA";
		if(cambio.CampoModificado!=null && !(cambio.CampoModificado.compareToIgnoreCase("null")==0) && !(cambio.CampoModificado.compareToIgnoreCase("")==0) ) {
			detalleLog.CampoRegistro = cambio.CampoModificado;
			try {
				if(cambio.CampoModificado.compareToIgnoreCase("IdSituacionLaboral") == 0) {
					SituacionLaboral situacionLaboral;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashSituacionLaboral.containsKey(cambio.ValorAntiguo)){
						situacionLaboral = (SituacionLaboral) hashSituacionLaboral.get(cambio.ValorAntiguo);
						detalleLog.ValorAntiguo = (situacionLaboral.SituacionLaboral!=null) ? situacionLaboral.SituacionLaboral : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashSituacionLaboral.containsKey(cambio.ValorNuevo)){
						situacionLaboral = (SituacionLaboral) hashSituacionLaboral.get(cambio.ValorNuevo);
						detalleLog.ValorNuevo = (situacionLaboral!=null) ? situacionLaboral.SituacionLaboral : cambio.ValorNuevo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("IdPaisResidencia") == 0 || cambio.CampoModificado.compareToIgnoreCase("IdPaisNacimiento") == 0 || cambio.CampoModificado.compareToIgnoreCase("IdPaisNacionalidadPrincipal") == 0) {
					Pais pais;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareTo("null") == 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashPais.containsKey(Long.parseLong(cambio.ValorAntiguo))){
						pais = (Pais) hashPais.get(Long.parseLong(cambio.ValorAntiguo));
						detalleLog.ValorAntiguo = (pais.IdPais!=null) ? pais.NombrePais : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareTo("null") == 0) && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashPais.containsKey(Long.parseLong(cambio.ValorNuevo))){
						pais = (Pais) hashPais.get(Long.parseLong(cambio.ValorNuevo));
						detalleLog.ValorNuevo = (pais.IdPais!=null) ? pais.NombrePais : cambio.ValorNuevo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("EstadoCivil") == 0) {
					EstadoCivil estadoCivil;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashEstadoCivil.containsKey(cambio.ValorAntiguo)){
						estadoCivil = (EstadoCivil) hashEstadoCivil.get(cambio.ValorAntiguo);
						detalleLog.ValorAntiguo = (estadoCivil.IdTipoEstadoCivil!=null) ? estadoCivil.Nombre : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashEstadoCivil.containsKey(cambio.ValorNuevo)){
						estadoCivil = (EstadoCivil) hashEstadoCivil.get(cambio.ValorNuevo);
						detalleLog.ValorNuevo = (estadoCivil.IdTipoEstadoCivil!=null) ? estadoCivil.Nombre : cambio.ValorAntiguo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("IdProfesion") == 0) {
					Profesion profesion;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashProfesion.containsKey(Long.parseLong(cambio.ValorAntiguo))){
						profesion = (Profesion) hashProfesion.get(Long.parseLong(cambio.ValorAntiguo));
						detalleLog.ValorAntiguo = (profesion.IdProfesion!=null) ? profesion.Nombre : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")==0) &&!(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashProfesion.containsKey(Long.parseLong(cambio.ValorNuevo))){
						profesion = (Profesion) hashProfesion.get(Long.parseLong(cambio.ValorNuevo));
						detalleLog.ValorNuevo = (profesion.IdProfesion!=null) ? profesion.Nombre : cambio.ValorAntiguo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("IdRangoPatrimonio") == 0) {
					RangoPatrimonio rangoPatrimonio;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashRangoPatrimonio.containsKey(Long.parseLong(cambio.ValorAntiguo))){
						rangoPatrimonio = (RangoPatrimonio) hashRangoPatrimonio.get(Long.parseLong(cambio.ValorAntiguo));
						detalleLog.ValorAntiguo = (rangoPatrimonio.IdRangoPatrimonio!=null) ? rangoPatrimonio.DescripcionRango : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")==0) && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashRangoPatrimonio.containsKey(Long.parseLong(cambio.ValorNuevo))){
						rangoPatrimonio = (RangoPatrimonio) hashRangoPatrimonio.get(Long.parseLong(cambio.ValorNuevo));
						detalleLog.ValorNuevo = (rangoPatrimonio.IdRangoPatrimonio!=null) ? rangoPatrimonio.DescripcionRango : cambio.ValorAntiguo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("Sexo") == 0) {
					Genero genero;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hashGenero.containsKey(cambio.ValorAntiguo)){
						genero = (Genero) hashGenero.get(cambio.ValorAntiguo);
						detalleLog.ValorAntiguo = (genero.IdTipoGenero!=null) ? genero.Descripcion : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hashGenero.containsKey(cambio.ValorNuevo)){
						genero = (Genero) hashGenero.get(cambio.ValorNuevo);
						detalleLog.ValorNuevo = (genero.IdTipoGenero!=null) ? genero.Descripcion : cambio.ValorAntiguo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("IdGiroPrincipal") == 0) {
					Giro giro;
					if((cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0)) && hGiro.containsKey(Long.parseLong(cambio.ValorAntiguo))){
						giro = (Giro) hGiro.get(Long.parseLong(cambio.ValorAntiguo));
						detalleLog.ValorAntiguo = (giro.IdGiro!=null) ? giro.Nombre : cambio.ValorAntiguo;
					}
					if((cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")==0) && !(cambio.ValorNuevo.compareToIgnoreCase("")==0)) && hGiro.containsKey(Long.parseLong(cambio.ValorNuevo))){
						giro = (Giro) hGiro.get(Long.parseLong(cambio.ValorNuevo));
						detalleLog.ValorNuevo = (giro.IdGiro!=null) ? giro.Nombre : cambio.ValorAntiguo;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("FechaNacimiento") == 0 || cambio.CampoModificado.compareToIgnoreCase("FechaDefuncion")==0) {
					detalleLog.ValorAntiguo = (cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")==0) && !(cambio.ValorAntiguo.trim().compareToIgnoreCase("0001-01-01")==0)) ? cambio.ValorAntiguo : "";
					detalleLog.ValorNuevo = (cambio.ValorNuevo!=null)? cambio.ValorNuevo : "";
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("IdPersonaConyuge") == 0 || cambio.CampoModificado.compareToIgnoreCase("IdPersonaEmpleador")==0) {
					DatosPersonaDatosGenerales persona;
					if( cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("0")==0) && hPersona.containsKey(Long.parseLong(cambio.ValorAntiguo))){
						persona = (DatosPersonaDatosGenerales) hPersona.get(Long.parseLong(cambio.ValorAntiguo));
						detalleLog.ValorAntiguo = (persona.Nombres!=null) ? persona.NombreCompleto : cambio.ValorAntiguo;
					}
					if( cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")== 0) && !(cambio.ValorNuevo.compareToIgnoreCase("0")==0) && hPersona.containsKey(Long.parseLong(cambio.ValorNuevo)) ) {
						persona = (DatosPersonaDatosGenerales) hPersona.get(Long.parseLong(cambio.ValorNuevo));
						detalleLog.ValorNuevo = (persona.Nombres!=null) ? persona.NombreCompleto : "";
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("RegimenConyugal") == 0) {
					RegimenConyugal regimConyugal;
					if( cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0) && hRegimenConyugal.containsKey(cambio.ValorAntiguo)) {
						regimConyugal = (RegimenConyugal) hRegimenConyugal.get(cambio.ValorAntiguo);
						detalleLog.ValorAntiguo = (regimConyugal.Nombre!=null)? regimConyugal.Nombre : "" ;
					}
					if( cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")== 0) && !(cambio.ValorNuevo.compareToIgnoreCase("")==0) && hRegimenConyugal.containsKey(cambio.ValorNuevo)) {
						regimConyugal = (RegimenConyugal) hRegimenConyugal.get(cambio.ValorNuevo);
						detalleLog.ValorNuevo = (regimConyugal.Nombre!=null)? regimConyugal.Nombre : "" ;
					}
				}
				else if(cambio.CampoModificado.compareToIgnoreCase("CodigoTipoCargoEmpleado") == 0) {
					TipoCargoEmpleado cargoEmpleado;
					if( cambio.ValorAntiguo!=null && !(cambio.ValorAntiguo.compareToIgnoreCase("null")== 0) && !(cambio.ValorAntiguo.compareToIgnoreCase("")==0) && hCargoEmpleado.containsKey(cambio.ValorAntiguo)) {
						cargoEmpleado = (TipoCargoEmpleado) hCargoEmpleado.get(cambio.ValorAntiguo);
						detalleLog.ValorAntiguo = (cargoEmpleado.NombreTipoCargoEmpleado!=null)? cargoEmpleado.NombreTipoCargoEmpleado: "" ;
					}
					if( cambio.ValorNuevo!=null && !(cambio.ValorNuevo.compareToIgnoreCase("null")== 0) && !(cambio.ValorNuevo.compareToIgnoreCase("")==0) && hCargoEmpleado.containsKey(cambio.ValorNuevo)) {
						cargoEmpleado = (TipoCargoEmpleado) hCargoEmpleado.get(cambio.ValorNuevo);
						detalleLog.ValorNuevo = (cargoEmpleado.NombreTipoCargoEmpleado!=null)? cargoEmpleado.NombreTipoCargoEmpleado: "" ;
					}
				}
				else {
					detalleLog.ValorAntiguo = (cambio.ValorAntiguo!=null)? cambio.ValorAntiguo: "" ;
					detalleLog.ValorNuevo = (cambio.ValorNuevo!=null)? cambio.ValorNuevo: "" ;
				}
				ListaDetalleLog.add(detalleLog);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.agregarLogDetalle", fin - inicio);
	}
	
	private HashMap<String, Object> obtenerHashTipoPersona(Connection con, SQL sql, List<String> idsTipoPersona){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashTipoPersona");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsTipoPersona.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdTipoPersona", idsTipoPersona); 
			try {
				List<TipoPersona> listado = sql.query(con, SQL_SELECT_TIPOPERSONA, params, TipoPersona.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdTipoPersona");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashTipoPersona", fin - inicio);
		
		return hashLista;
	}
	
	
	private HashMap<String, Object> obtenerHashPaises(Connection con, SQL sql, List<Long> idsPais){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashPaises");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsPais.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdPais", idsPais);
			try {
				List<Pais> listado = sql.query(con, SQL_SELECT_PAIS, params, Pais.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdPais");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashPaises", fin - inicio);
		
		return hashLista;
	}
	
	private HashMap<String, Object> obtenerHashSituacionLaboral(Connection con, SQL sql, List<String> idsSiutacionLaboral){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashSituacionLaboral");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsSiutacionLaboral.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("CodigoSituacionLaboral", idsSiutacionLaboral);
			try {
				List<SituacionLaboral> listado = sql.query(con, SQL_SELECT_SITUACIONLABORAL, params, SituacionLaboral.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "CodigoSituacionLaboral");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashSituacionLaboral", fin - inicio);
		
		return hashLista;
	}
	
	private HashMap<String, Object> obtenerHashEstadoCivil(Connection con, SQL sql, List<String> idsEstadoCivil){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashEstadoCivil");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsEstadoCivil.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdTipoEstadoCivil", idsEstadoCivil);
			try {
				List<EstadoCivil> listado = sql.query(con, SQL_SELECT_ESTADOCIVIL, params, EstadoCivil.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdTipoEstadoCivil");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashEstadoCivil", fin - inicio);
		
		return hashLista;
	}
	
	
	private HashMap<String, Object> obtenerHashProfesion(Connection con, SQL sql, List<Long> idsProfesion){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashProfesion");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsProfesion.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdProfesion", idsProfesion);
			try {
				List<Profesion> listado = sql.query(con, SQL_SELECT_PROFESION, params, Profesion.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdProfesion");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashProfesion", fin - inicio);
		
		return hashLista;
	}
	
	private HashMap<String, Object> obtenerHashRangoPatrimonio(Connection con, SQL sql, List<Long> idsRangoPatrimonio){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashRangoPatrimonio");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsRangoPatrimonio.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdRangoPatrimonio", idsRangoPatrimonio);
			try {
				List<RangoPatrimonio> listado = sql.query(con, SQL_SELECT_RANGOPATRIMONIO, params, RangoPatrimonio.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdRangoPatrimonio");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashRangoPatrimonio", fin - inicio);
		
		return hashLista;
	}
	
	private HashMap<String, Object> obtenerHashGiro(Connection con, SQL sql, List<Long> idsGiro){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashGiro");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsGiro.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdGiro", idsGiro);
			try {
				List<Giro> listado = sql.query(con, SQL_SELECT_GIRO, params, Giro.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdGiro");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashGiro", fin - inicio);
		
		return hashLista;
	}
	
	
	private HashMap<String, Object> obtenerHashGenero(Connection con, SQL sql, List<String> idsGenero){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashGenero");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsGenero.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdTipoGenero", idsGenero);
			try {
				List<Genero> listado = sql.query(con, SQL_SELECT_GENERO, params, Genero.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdTipoGenero");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashGenero", fin - inicio);
		
		return hashLista;
	}
	
	public HashMap<String, Object> obtenerHashRegimenConyugal(Connection con, SQL sql, List<String> idsRegimenConyugal){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashRegimenConyugal");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsRegimenConyugal.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdTipoRegimenConyugal", idsRegimenConyugal);
			try {
				List<RegimenConyugal> listado = sql.query(con, SQL_SELECT_REGIMENCONYUGAL, params, RegimenConyugal.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdTipoRegimenConyugal");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashRegimenConyugal", fin - inicio);
		
		return hashLista;
	}
	
	public HashMap<String, Object> obtenerHashCargoEmpleado(Connection con, SQL sql, List<String> idTipoCargo){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashCargoEmpleado");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idTipoCargo.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("CodigoTipoCargoEmpleado", idTipoCargo);
			try {
				List<TipoCargoEmpleado> listado = sql.query(con, SQL_SELECT_CARGOEMPLEADO , params, TipoCargoEmpleado.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "CodigoTipoCargoEmpleado");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerHashCargoEmpleado", fin - inicio);
		
		return hashLista;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_PersonaDatosGenerales: [{IdPersona:1790725}],p_IdEstadoAutorizacion: \"AUT\"}";
			EntradaConfirmarEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaConfirmarEjecutarCambiosPersona.class);
			
			ConfirmarModificarPersonaDatosGeneralesMasivo demo = new ConfirmarModificarPersonaDatosGeneralesMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaConfirmarEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}	
