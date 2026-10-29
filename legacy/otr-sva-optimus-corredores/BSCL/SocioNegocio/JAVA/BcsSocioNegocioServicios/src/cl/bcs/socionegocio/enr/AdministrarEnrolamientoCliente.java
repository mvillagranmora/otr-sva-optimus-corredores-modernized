package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.acc.CrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.EntradaCrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.SalidaCrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.Usuario;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.Cliente;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.CrearCliente.Canal;
import cl.bcs.socionegocio.cli.CrearCliente.EntidadAgregarLog;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.parametrosLog;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.sql.ResultSet;

public class AdministrarEnrolamientoCliente implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoCliente.class);
	
	private final String SQL_SELECT_EMPLEADO_EJECUTIVO 		= loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCliente_SelectEmpleadoEjecutivo.sql");
	private final String SQL_INSERT_CLIENTE 				= loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCliente_InsertCliente.sql");
	private final String SQL_UPDATE_SOCIO_NEGOCIO 			= loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoCliente_UpdateSocioNegocio.sql");
	public final  String SQL_CONSULTAR_CANAL 				= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectCanalIngreso.sql");
	public final String SQL_CREATE_TABLA_PARAMETROS 		= "CREATE LOCAL TEMPORARY TABLE  #tabla_Parametros (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))";
	public final String SQL_INSERT_TABLA_PARAMETROS			= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_ParametrosInsert.sql");
	public final String SQL_CALL_AGREGAR_PROGRAMA 			= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_CallAgregaPrograma.sql");
	public final String SQL_DROP_TABLA_PARAMETROS			= "DROP TABLE #tabla_Parametros";
	private final String SQL_CONSULTA_EXISTE_USUARIO		= loadResourceToString("cl/bcs/plataforma/acc/sql/Consulta_Existen_Usuarios.sql");
	
	private final String ID_CONCEPTO = "ENROLC";
	private final String ID_CONCEPTO_FULL = "ENCFUL";
	private final String ID_CONCEPTO_EXPRESS = "ENCEXP";

	private static String tipoEnrolamiento = "";
	
	public static class Configuracion extends BcsPojo {
		public String RequiereAutorizacion;
		public String RequiereAutorizacionEnrFull;
		public String RequiereAutorizacionEnrExpress;
		public String BloqueoPorDefecto;
		public String BloqueoPorDefectoEnrFull;
		public String BloqueoPorDefectoEnrExpress;
		public String CrearUsuarioAutomaticamente;

		public String requiereAutorizacion(String tipoEnrolamiento) {
			if ("EXPRESS".equals(tipoEnrolamiento)) {
				return RequiereAutorizacionEnrExpress;
			}

			if ("FULL".equals(tipoEnrolamiento)) {
				return RequiereAutorizacionEnrFull;
			}

			return RequiereAutorizacion;
		}

		public String bloquearPorDefecto(String tipoEnrolamiento) {
			if ("EXPRESS".equals(tipoEnrolamiento)) {
				return BloqueoPorDefectoEnrExpress;
			}

			if ("FULL".equals(tipoEnrolamiento)) {
				return BloqueoPorDefectoEnrFull;
			}

			return BloqueoPorDefecto;
		}
	}
	
	public static class EmpleadoEjecutivo extends BcsPojo{
		public Long IdEmpleadoEjecutivo;
		public Long IdPersona;
		
		public EmpleadoEjecutivo() {
			this.IdEmpleadoEjecutivo = 0L;
			this.IdPersona = 0L;
		}
	}
	
	public static class SocioNegocio extends BcsPojo {
		public Long IdSocioNegocio;
		public Long IdCliente;
	}
	
	public static class Canal {
		public String IdTipoCanalIngreso;
		public String CreaUsuarioCliente;
	}
	
	public static class Parametros extends BcsPojo{
		public String NombreParametro;
		public String Valor;
		Parametros(){
			this.NombreParametro ="";
			this.Valor ="";
		}
		Parametros(String NombreParametro,String Valor){
			this.NombreParametro =NombreParametro;
			this.Valor =Valor;
		}
	}
	
	public List<DatosClienteDatosGenerales> crearCliente(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoCliente.crearCliente");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::Cliente", Long.valueOf(borradoresCliente.size()));
		
		Configuracion configuracion = obtenerConfiguracion(con, entrada.p_IdEmpresa);
		Map<Long, EmpleadoEjecutivo> empleadosEjecutivoPorIdEmpleadoEjecutivo = obtenerEmpleadosEjecutivoPorIdEmpleadoEjecutivo(con, borradoresCliente);
		
		AtomicInteger index = new AtomicInteger();
		
		borradoresCliente.parallelStream().forEach(borradorCliente->{
			Long idCliente = secuencias.get(index.getAndIncrement());
			EmpleadoEjecutivo empleadoEjecutivo = obtenerEmpleadoEjecutivo(borradorCliente.Enrolamiento.CLI_IdEmpleadoEjecutivo, empleadosEjecutivoPorIdEmpleadoEjecutivo);
			borradorCliente.Cliente = obtenerEntidadCliente(borradorCliente, idCliente, configuracion, empleadoEjecutivo.IdPersona, entrada.p_IdUsuario);
		});
		
		insertarClientes(con, borradoresCliente,entrada.p_IdTipoCanalIngreso,entrada.p_FirmaAutomaticaDocumento,entrada.p_IdUsuario);
		actualizarSociosNegocio(con, borradoresCliente);

		List<DatosClienteDatosGenerales> borradoresConAutorizacion = borradoresCliente
				.stream()
				.filter(borradorCliente -> "S".equals(configuracion.requiereAutorizacion(borradorCliente.Cliente.TipoEnrolamiento)))
				.collect(Collectors.toList());
		crearEventoAutorizacion(con, entrada, borradoresConAutorizacion);
		
		
		SQL sql = new SQL();
		String tipoCanal =  entrada.p_IdTipoCanalIngreso.trim().isEmpty()?"WBS" : entrada.p_IdTipoCanalIngreso;
		String crearUsuarioAutomatico = "S";							
		
		HashMap<String,Object> paramscanal = new HashMap<>();
		paramscanal.put("IdTipoCanalIngreso", tipoCanal);
		List<Canal> canalingreso = sql.query(con, SQL_CONSULTAR_CANAL, paramscanal, Canal.class);
		if (canalingreso.size() == 0) {
			crearUsuarioAutomatico = "S";
		}
		else								{
			crearUsuarioAutomatico = canalingreso.get(0).CreaUsuarioCliente;
		}
		
		HashMap<String, Object> params_ = new HashMap<>();
		List<String> codigosUsuarios = borradoresCliente.stream()
			    .flatMap(cliente -> cliente.DocumentosIdentidades.stream())
			    .map(doc -> doc.DocumentoIdentidad)
			    .filter(Objects::nonNull)
			    .collect(Collectors.toList());
		
		params_.put("IdEmpresa", entrada.p_IdEmpresa);
		params_.put("CodigoUsuario",codigosUsuarios);
		List<Usuario> existe_usuario = sql.query(con, SQL_CONSULTA_EXISTE_USUARIO, params_, Usuario.class);
		
		if ("S".equals(crearUsuarioAutomatico) && existe_usuario.isEmpty()) {
			borradoresCliente = crearUsuario(con, entrada, borradoresCliente, tipoCanal);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.crearCliente", fin - inicio);
		return borradoresCliente;
	}
	
	private Configuracion obtenerConfiguracion(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::AdministrarEnrolamientoCliente.obtenerConfiguracion");
		long inicio = System.currentTimeMillis();
		
		Configuracion configuracion = new Configuracion();
		configuracion.RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO);
		configuracion.RequiereAutorizacionEnrFull = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO_FULL);
		configuracion.RequiereAutorizacionEnrExpress = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO_EXPRESS);

		configuracion.BloqueoPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "SOCIODENEGOCIO", "CLI.BloqueoPorDefecto");
		configuracion.BloqueoPorDefectoEnrFull = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "SOCIODENEGOCIO", "CLI.BloqueoPorDefectoEnrolamientoFull");
		configuracion.BloqueoPorDefectoEnrExpress = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "SOCIODENEGOCIO", "CLI.BloqueoPorDefectoEnrolamientoExpress");
		configuracion.CrearUsuarioAutomaticamente = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "SOCIODENEGOCIO", "CLI.CrearUsuarioAutomatico");
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCliente.obtenerConfiguracion", fin - inicio);
		return configuracion;
	}
	
	private Map<Long, EmpleadoEjecutivo> obtenerEmpleadosEjecutivoPorIdEmpleadoEjecutivo(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCliente.obtenerEmpleadosEjecutivo");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsEmpleadoEjecutivo = borradoresCliente
				.parallelStream()
				.map(borradorCliente->borradorCliente.Enrolamiento.CLI_IdEmpleadoEjecutivo)
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdEmpleadoEjecutivo", idsEmpleadoEjecutivo);
		
		SQL sql = new SQL();
		List<EmpleadoEjecutivo> empleadosEjecutivo = sql.query(con, SQL_SELECT_EMPLEADO_EJECUTIVO, parametros, EmpleadoEjecutivo.class);
		Map<Long, EmpleadoEjecutivo> empleadosEjecutivoPorIdEmpleadoEjecutivo = empleadosEjecutivo
				.parallelStream()
				.collect(Collectors.toMap(empleadoEjecutivo->empleadoEjecutivo.IdEmpleadoEjecutivo, empleadoEjecutivo->empleadoEjecutivo)); 
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.obtenerEmpleadosEjecutivo", fin - inicio);
		return empleadosEjecutivoPorIdEmpleadoEjecutivo;
	}
	
	private EmpleadoEjecutivo obtenerEmpleadoEjecutivo(Long idEmpleadoEjecutivo, Map<Long, EmpleadoEjecutivo> empleadosEjecutivosPorIdEmpleadoEjecutivo) {
		logger.trace("INICIO::AdministrarEnrolamientoCliente.obtenerEmpleadoEjecutivo");
		long inicio = System.currentTimeMillis();
		
		EmpleadoEjecutivo empleadoEjecutivo = empleadosEjecutivosPorIdEmpleadoEjecutivo.get(idEmpleadoEjecutivo);
		if(empleadoEjecutivo == null) {
			empleadoEjecutivo = new EmpleadoEjecutivo(); 
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCliente.obtenerEmpleadoEjecutivo", fin - inicio);
		return empleadoEjecutivo;
	}
	
	private Cliente obtenerEntidadCliente(DatosClienteDatosGenerales borradorCliente, Long idCliente, Configuracion configuracion, Long idPersonaEmpleadoEjecutivo, Long idUsuario) {
		logger.debug("INICIO::AdministrarEnrolamientoCliente.obtenerEntidadCliente");
		long inicio = System.currentTimeMillis();
		
		Cliente cliente = new Cliente();
		cliente.IdCliente = idCliente;
		cliente.IdPersona = borradorCliente.IdPersona;
		cliente.IdEmpresa = borradorCliente.Enrolamiento.IdEmpresa;
		cliente.IdUsuarioCreador = idUsuario;
		cliente.IdSegmentoCliente = borradorCliente.Enrolamiento.CLI_IdSegmentoCliente.equals(0L) ? null : borradorCliente.Enrolamiento.CLI_IdSegmentoCliente;
		cliente.IdPlantillaLimiteExtra = "CLI_" + cliente.IdCliente;
		cliente.IdPersonaEjecutivo = idPersonaEmpleadoEjecutivo;
		cliente.IdCategoriaInversionista = borradorCliente.Enrolamiento.CLI_IdCategoriaInversionista;
		cliente.IdTipoCliente = borradorCliente.Enrolamiento.CLI_IdTipoCliente;
		cliente.CodigoAdicionalHolding = borradorCliente.Enrolamiento.CLI_CodigoAdicionalHolding;
		cliente.CodigoEjecutivoExterno = borradorCliente.Enrolamiento.CLI_CodigoEjecutivoExterno;
		cliente.IdSucursalPreferencia = borradorCliente.Enrolamiento.CLI_IdSucursalPreferencia;
		cliente.IdClaseCliente = borradorCliente.Enrolamiento.CLI_IdClaseCliente;
		cliente.TipoEnrolamiento = borradorCliente.Enrolamiento.TipoEnrolamiento;
		cliente.EsClienteFinal = borradorCliente.Enrolamiento.CLI_EsClienteFinal;
		cliente.IdEstado = "S".equals(configuracion.bloquearPorDefecto(borradorCliente.Enrolamiento.TipoEnrolamiento)) ? "BLO" : "ACT";
		cliente.EsEnrolado = "S".equals(configuracion.requiereAutorizacion(tipoEnrolamiento)) ? "N" :"S";
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCliente.obtenerEntidadCliente", fin - inicio);
		return cliente;
	}
	
	private void insertarClientes(Connection con, List<DatosClienteDatosGenerales> borradoresCliente,String CanalIngreso, String p_FirmaAutomaticaDocumento, Long p_IdUsuario) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCliente.insertarClientes");
		long inicio = System.currentTimeMillis();
	
		List<Cliente> clientes = borradoresCliente
				.parallelStream()
				.map(borradorCliente->borradorCliente.Cliente)
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CLIENTE, clientes, Cliente.class);
			
		clientes.parallelStream().forEach(cliente->{
			
			//llama al job que crea todos los documentos que debe firmar el cliente
			List<Parametros> ParametrosX = new ArrayList<>() ;
			ParametrosX.add(new Parametros("p_IdEmpresa",String.valueOf(cliente.IdEmpresa))) ;
			ParametrosX.add(new Parametros("p_IdCliente",String.valueOf(cliente.IdCliente))) ;
			ParametrosX.add(new Parametros("p_IdTipoCanal",String.valueOf(CanalIngreso))) ;
			ParametrosX.add(new Parametros("p_IdUsuario",String.valueOf(p_IdUsuario))) ;
			ParametrosX.add(new Parametros("p_FirmaAutomaticaDocumento",p_FirmaAutomaticaDocumento));
			EnviaGenerarDocumentosAutomaticosJob(con,cliente.IdEmpresa,cliente.IdUsuarioCreador ,"GENERAR_DOCUMENTOS_AUTOMATICOS",ParametrosX);			
		
		
			// agrega log de categoria de cliente
			parametrosLog ParametrosL = new parametrosLog();
			ParametrosL.SubRegistro = "INGRESO";
			ParametrosL.CampoRegistro = "CategoriaInversionista";
			ParametrosL.ValorAntiguo = cliente.IdCategoriaInversionista;
			ParametrosL.ValorNuevo = cliente.IdCategoriaInversionista;
			RegistraLogCategoriaCliente(con, cliente,  ParametrosL,CanalIngreso);
			

		});
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.insertarClientes", fin - inicio);
	}
	
	private void actualizarSociosNegocio(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCliente.actualizarSocioNegocio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<SocioNegocio> sociosNegocio = borradoresCliente
				.parallelStream()
				.map(borradorCliente->{
						SocioNegocio socioNegocio = new SocioNegocio();
						socioNegocio.IdCliente = borradorCliente.Cliente.IdCliente;
						socioNegocio.IdSocioNegocio = borradorCliente.IdSocioNegocio;
						return socioNegocio;
					})
				.collect(Collectors.toList());
		
		sql.batch(con, SQL_UPDATE_SOCIO_NEGOCIO, sociosNegocio, SocioNegocio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.actualizarSocioNegocio", fin - inicio);
	}
	
	private List<DatosClienteDatosGenerales> crearEventoAutorizacion(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoCliente.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();

		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = borradoresCliente.parallelStream().map(borradorCliente->{
			
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			tipoEnrolamiento = EnrolamientoUtils.obtenerTipoEnrolamiento(borradorCliente.Enrolamiento.TipoEnrolamiento);
			evento.IdTipoEvento = tipoEnrolamiento;
			evento.IdConcepto = tipoEnrolamiento;
			
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.NumeroOperacionConcepto = String.valueOf(borradorCliente.Cliente.IdCliente);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = borradorCliente.Cliente.IdCliente;
			evento.Glosa = obtenerGlosa(borradorCliente);
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		if(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.add(new MensajeSalida(borradorCliente.IdInterno, MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.crearEventoAutorizacion", fin - inicio);
		return borradoresCliente;	
	}
	
	private String obtenerGlosa(DatosClienteDatosGenerales borradorCliente) {
		logger.trace("INICIO::AdministrarEnrolamientoCliente.obtenerGlosa");
		long inicio = System.currentTimeMillis();
		DocumentoIdentidad documentoIdentidad = EnrolamientoUtils.obtenerDocumentoIdentidadPorId(borradorCliente.DocumentosIdentidades, borradorCliente.Enrolamiento.IdDocumentoIdentidad);
		
		if("J".equals(borradorCliente.Enrolamiento.Tipo)) {
			String glosa = documentoIdentidad.DocumentoIdentidad + " / " + borradorCliente.Enrolamiento.RazonSocial;
			return glosa;
		}
		
		String glosa = documentoIdentidad.DocumentoIdentidad + " / " + borradorCliente.Enrolamiento.Nombres + " " + borradorCliente.Enrolamiento.ApellidoPaterno + " " + borradorCliente.Enrolamiento.ApellidoMaterno;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoCliente.obtenerGlosa", fin - inicio);
		return glosa;
	}
	
	private List<DatosClienteDatosGenerales> crearUsuario(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente, String tipoCanalIngreso) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoCliente.crearUsuario");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String claveUsuarioAleatoria = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.ClaveUsuarioAleatoria");
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			
			DocumentoIdentidad documentoIdentidad = EnrolamientoUtils.obtenerDocumentoIdentidadPorId(borradorCliente.DocumentosIdentidades, borradorCliente.Enrolamiento.IdDocumentoIdentidad);
			CrearUsuario crearUsuario = new CrearUsuario();
			EntradaCrearUsuario entradaCrearUsuario = new EntradaCrearUsuario();
			entradaCrearUsuario.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearUsuario.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearUsuario.p_IdModulo = entrada.p_IdModulo;
			entradaCrearUsuario.p_UsaReglasNegocio = "N";
			entradaCrearUsuario.p_CodigoUsuario = documentoIdentidad.DocumentoIdentidad;
			entradaCrearUsuario.p_Clave = obtenerClaveUsuario(claveUsuarioAleatoria, documentoIdentidad.DocumentoIdentidad);
			entradaCrearUsuario.p_FechaIngreso = fechaSistema;
			entradaCrearUsuario.p_FechaVigencia = "0001-01-01";
			entradaCrearUsuario.p_FechaUltimaConexion = "";
			entradaCrearUsuario.p_Conectado = "N";
			entradaCrearUsuario.p_Hora = "00:00:00";
			entradaCrearUsuario.p_Terminal = "";
			entradaCrearUsuario.p_IdEstado = "OWP".equalsIgnoreCase(tipoCanalIngreso) ? "BLO" : "ACT";
			entradaCrearUsuario.p_IntentosConexion = 0;
			entradaCrearUsuario.p_FechaBloqueoTemporal = "";
			entradaCrearUsuario.p_TipoEntidad = "CLI";
			entradaCrearUsuario.p_IdEntidad = borradorCliente.Cliente.IdCliente;
			entradaCrearUsuario.p_HeredarRolesUsuario = -1L;
			entradaCrearUsuario.p_UsuarioClientes = "S";
			SalidaCrearUsuario salidaCrearUsuario = crearUsuario.logica(con, entradaCrearUsuario);
			borradorCliente.MensajesSalida.addAll(salidaCrearUsuario.P_OUT_MENSAJES);
			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.crearUsuario", fin - inicio);
		return borradoresCliente;
	}
	
	private String obtenerClaveUsuario(String claveUsuarioAleatoria, String DocumentoIdentidad) {
		
		logger.trace("INICIO::AdministrarEnrolamientoCliente.crearUsuario");
		long inicio = System.currentTimeMillis();
		
		String claveUsuario = "";
		
		if("S".equalsIgnoreCase(claveUsuarioAleatoria)) {
			
			String claveRandom = String.valueOf(new Random().nextInt(100000));
			claveUsuario = Hashing.sha256().newHasher().putString(claveRandom, Charsets.UTF_8).hash().toString();
			
		} else {
			
			String[] splitDocumentoIdentidad = DocumentoIdentidad.split("-");
			claveUsuario = Hashing.sha256().newHasher().putString(splitDocumentoIdentidad[0], Charsets.UTF_8).hash().toString();
			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoCliente.crearUsuario", fin - inicio);
		
		return claveUsuario;
		
	}
	
	
	public void EnviaGenerarDocumentosAutomaticosJob(Connection con,Long IdEmpresa,Long IdUsuario,String IdTipoTarea,List<Parametros>TablaParametros) {
    	logger.debug("INICIO::CrearCliente.EnviaGenerarDocumentosAutomaticosJob");
		long inicio = System.currentTimeMillis();
		try {
			
			SQL sql = new SQL();
			SQL.exec(con, SQL_CREATE_TABLA_PARAMETROS);	
			sql.batch(con,SQL_INSERT_TABLA_PARAMETROS ,TablaParametros, Parametros.class);		
			
			HashMap<String,Object>paramIN = new HashMap<>();
			paramIN.put("IdEmpresa",IdEmpresa);
			paramIN.put("IdUsuario", IdUsuario);
			paramIN.put("IdTipoTarea", IdTipoTarea);
			
			HashMap<String,Class> paramOUT = new HashMap<>();
			
			HashMap<String, Object> resultadoSP = sql.call(con, SQL_CALL_AGREGAR_PROGRAMA, paramIN, paramOUT);
			
			SQL.exec(con, SQL_DROP_TABLA_PARAMETROS);	
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.EnviaGenerarDocumentosAutomaticosJob", fin - inicio);
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void RegistraLogCategoriaCliente(Connection con, Cliente in, parametrosLog TablaParametrosLog,String canalIngreso) {

		try {
			
			ModificarClienteDatosGenerales AgregaCambioLog = new ModificarClienteDatosGenerales();
			ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales entrada = new ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales();
			ModificarClienteDatosGenerales.RegistroCambio registroCambio = new ModificarClienteDatosGenerales.RegistroCambio();
			
			registroCambio.CampoModificado ="IdCategoriaInversionista";
			registroCambio.ValorNuevo =TablaParametrosLog.ValorNuevo;
			
			entrada.p_IdEmpresa        = in.IdEmpresa;
			entrada.p_IdUsuario        = in.IdUsuarioCreador;
			entrada.p_IdModulo         = canalIngreso;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdCliente        = in.IdCliente;
			entrada.p_IdCanal          = canalIngreso;
			entrada.p_DesdeEnrolamiento = true;
			entrada.p_RegistroCambio   = Arrays.asList(registroCambio); 			
			AgregaCambioLog.logica(con, entrada);
			
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
	}
	

	public class EntidadAgregarLog  {
	
		String  p_IdTipoLog;
		String  p_IdModulo;  
		Long  p_IdUsuario; 
		Long  p_IdEmpresa ;
		String  p_FechaSistemaLog; 
		Long  p_IdUsuarioAtendido; 
		String  p_IdTipoRegistro ;
		List <parametrosLog>  p_DetalleLog ;
		
		public	EntidadAgregarLog(){
			this.p_IdTipoLog = "";
			this.p_IdModulo = "";  
			this.p_IdUsuario = 0l; 
			this.p_IdEmpresa  = 0l;
			this.p_FechaSistemaLog = "";
			this.p_IdUsuarioAtendido = 0l; 
			this.p_IdTipoRegistro  = "";
			this.p_DetalleLog  = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class parametrosLog {
		String SubRegistro;
		String CampoRegistro;
		String ValorAntiguo;
		String ValorNuevo;

		public	parametrosLog() {
			this.SubRegistro = "";
			this.CampoRegistro = "";
			this.ValorAntiguo = "";
			this.ValorNuevo = "";

		}

	
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
