package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.NombreFantasia;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaNombreFantasiaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearPersonaNombreFantasiaMasivo.class);

	private final String SQL_INSERT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasiaMasivo_InsertNombreFantasia.sql");
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasiaMasivo_SelectPortafolios.sql");
	private final String SQL_UPDATE_NOMBRE_FANTASIA_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasiaMasivo_UpdateNombreFantasiaTablaPersona.sql");
	private final String SQL_UPDATE_NOMBRE_FANTASIA_TABLA_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasiaMasivo_UpdateNombreFantasiaTablaPortafolio.sql");
	
	private final String ID_CONCEPTO = "NUENFA";
	
	public static class EntradaCrearPersonaNombreFantasiaMasivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<NombreFantasia> p_NombresFantasia;
		
		public EntradaCrearPersonaNombreFantasiaMasivo() {
			this.p_NombresFantasia = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearPersonaNombreFantasiaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearPersonaNombreFantasiaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class PortafolioUpdateNombreFantasia extends BcsPojo{
		public Long IdPortafolio;
		public Long IdNombreFantasia;
		
		public PortafolioUpdateNombreFantasia() {
			this.IdPortafolio = 0L;
			this.IdNombreFantasia = 0L;
		}
	}
	
	public static class PortafoliosCliente extends BcsPojo{
		public Long IdPortafolio;
		public Long IdPersona;
		
		public PortafoliosCliente() {
			this.IdPortafolio = 0L;
			this.IdPersona = 0L;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPersonaNombreFantasiaMasivo jin = gson.fromJson(in.toString(), EntradaCrearPersonaNombreFantasiaMasivo.class);
		SalidaCrearPersonaNombreFantasiaMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearPersonaNombreFantasiaMasivo logica(Connection con,EntradaCrearPersonaNombreFantasiaMasivo entrada) throws Exception {
		logger.info("INICIO::CrearPersonaNombreFantasiaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearPersonaNombreFantasiaMasivo out = new SalidaCrearPersonaNombreFantasiaMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<NombreFantasia> nombresFantasia = obtenerNombresFantasia(con, entrada.p_NombresFantasia, esOperacionConAutorizacion, entrada.p_IdEmpresa);

		insertarNombresFantasia(con, nombresFantasia);
		
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, nombresFantasia);
		} else {
			agregarLog(con, entrada, nombresFantasia);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(nombresFantasia);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaNombreFantasiaMasivo", finServicio - inicioServicio);
		return out;
	}

	private List<Entidad> obtenerEntidadesInsertados(List<NombreFantasia> nombresFantasia) {
		logger.trace("INICIO::CrearPersonaNombreFantasiaMasivo.obtenerEntidadesInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = nombresFantasia
				.parallelStream()
				.map(nombreFantasia->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = nombreFantasia.IdInterno;
					entidad.IdEntidad = nombreFantasia.IdNombreFantasia;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaNombreFantasiaMasivo.obtenerEntidadesInsertados", fin - inicio);
		return entidades;
	}

	private void agregarLog(Connection con, EntradaCrearPersonaNombreFantasiaMasivo entrada,List<NombreFantasia> nombresFantasia) throws Exception {
		logger.trace("INICIO::CrearPersonaNombreFantasiaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(NombreFantasia nombreFantasia : nombresFantasia) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(nombreFantasia.IdNombreFantasia, "DescripcionNombreFantasia", "", String.valueOf(nombreFantasia.DescripcionNombreFantasia)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(nombreFantasia.IdNombreFantasia, "IdEstado", "", nombreFantasia.IdEstado));

			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = nombreFantasia.IdNombreFantasia;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = nombreFantasia.IdPersona;
			log.IdTipoRegistro = "CREACION_NOMBRE_FANTASIA";
			log.EstadoRegistro = nombreFantasia.IdEstadoAutorizacion;
			log.EstadoProceso = nombreFantasia.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Nombre Fantasia";
			log.IdRegistro = String.valueOf(nombreFantasia.IdNombreFantasia);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaNombreFantasiaMasivo.agregarLog", fin - inicio);
		
	}

	private SalidaCrearPersonaNombreFantasiaMasivo crearEventoAutorizacion(Connection con, EntradaCrearPersonaNombreFantasiaMasivo entrada, List<NombreFantasia> nombresFantasia) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasiaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearPersonaNombreFantasiaMasivo salidaCrearPersonaNombreFantasiaMasivo = new SalidaCrearPersonaNombreFantasiaMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = nombresFantasia
				.parallelStream()
				.map(nombreFantasia->nombreFantasia.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = nombresFantasia.stream().map(nombreFantasia->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(nombreFantasia.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(nombreFantasia.IdNombreFantasia);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = nombreFantasia.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /NombreFantasia: " + nombreFantasia.DescripcionNombreFantasia;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearPersonaNombreFantasiaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaCrearPersonaNombreFantasiaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasiaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaCrearPersonaNombreFantasiaMasivo;	
	}

	private void insertarNombresFantasia(Connection con, List<NombreFantasia> nombresFantasia) throws SQLException {
		logger.trace("INICIO::CrearPersonaNombreFantasiaMasivo.insertarNombresFantasia");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_NOMBRE_FANTASIA, nombresFantasia, NombreFantasia.class);
		
		sql.batch(con, SQL_UPDATE_NOMBRE_FANTASIA_TABLA_PERSONA, nombresFantasia, NombreFantasia.class);
		
		List<PortafolioUpdateNombreFantasia> listaPortafolioUpdate = new ArrayList<>();
		List<PortafoliosCliente> listaPortafoliosCliente = new ArrayList<>();
		List<Long> listaIdsPersona = new ArrayList<>();
		HashMap<Long,Long> nombresFantasiaIds = new HashMap<>();
		
		if(nombresFantasia != null && !nombresFantasia.isEmpty()) {
			for(NombreFantasia idsNombreFantasia : nombresFantasia) {
				if (idsNombreFantasia != null && idsNombreFantasia.IdPersona != null && idsNombreFantasia.IdNombreFantasia != null) {
					listaIdsPersona.add(idsNombreFantasia.IdPersona);
					nombresFantasiaIds.put(idsNombreFantasia.IdPersona, idsNombreFantasia.IdNombreFantasia);
				}
			}
		}
		
		if(listaIdsPersona != null && !listaIdsPersona.isEmpty()) {
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("IdPersona", listaIdsPersona);
			
			listaPortafoliosCliente = sql.query(con, SQL_SELECT_PORTAFOLIOS, parametros, PortafoliosCliente.class);
		}
		
		if(listaPortafoliosCliente != null && !listaPortafoliosCliente.isEmpty()) {
			for(PortafoliosCliente portafolio : listaPortafoliosCliente) {
				if (portafolio != null && portafolio.IdPortafolio != null && portafolio.IdPersona != null) {
					PortafolioUpdateNombreFantasia portafolioUpdate = new PortafolioUpdateNombreFantasia();
					
					Long idPersona = portafolio.IdPersona;
					Long nombreFantasiaId = nombresFantasiaIds.get(idPersona);
					if (nombreFantasiaId != null) {
					    portafolioUpdate.IdNombreFantasia = nombreFantasiaId;
					}
					portafolioUpdate.IdPortafolio = portafolio.IdPortafolio;

					listaPortafolioUpdate.add(portafolioUpdate);
				}
			}
		}
		
		if(listaPortafolioUpdate != null && !listaPortafolioUpdate.isEmpty()) {
			sql.batch(con, SQL_UPDATE_NOMBRE_FANTASIA_TABLA_PORTAFOLIO, listaPortafolioUpdate, PortafolioUpdateNombreFantasia.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaNombreFantasiaMasivo.insertarNombresFantasia", fin - inicio);
	}

	private List<NombreFantasia> obtenerNombresFantasia(Connection con, List<NombreFantasia> entradaNombresFantasia,Boolean esOperacionConAutorizacion, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearPersonaNombreFantasiaMasivo.obtenerNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsNombreFantasia = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia", Long.valueOf(entradaNombresFantasia.size()));
		String fechaSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(con, idEmpresa);
		AtomicInteger index = new AtomicInteger();
		
		List<NombreFantasia> nombresFantasia = entradaNombresFantasia
				.stream()
				.map(entradaNombreFantasia->{
					Long idNombreFantasia = idsNombreFantasia.get(index.getAndIncrement());
					NombreFantasia nombreFantasia = obtenerEntidadNombreFantasia(entradaNombreFantasia, idNombreFantasia, esOperacionConAutorizacion, fechaSistemaUTC);
					return nombreFantasia;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaNombreFantasiaMasivo.obtenerNombresFantasia", fin - inicio);
		return nombresFantasia;
	}

	private NombreFantasia obtenerEntidadNombreFantasia(NombreFantasia entradaNombreFantasia, Long idNombreFantasia, Boolean esOperacionConAutorizacion, String fechaSistemaUTC) {
		logger.trace("INICIO::CrearPersonaNombreFantasiaMasivo.obtenerEntidadNombreFantasia");
		long inicio = System.currentTimeMillis();

		NombreFantasia nombreFantasia = new NombreFantasia();
		nombreFantasia.IdInterno = entradaNombreFantasia.IdInterno;
		nombreFantasia.IdNombreFantasia = idNombreFantasia;
		nombreFantasia.DescripcionNombreFantasia = entradaNombreFantasia.DescripcionNombreFantasia;
		nombreFantasia.FechaIngreso = Fecha.toTimeStamp(fechaSistemaUTC);
		nombreFantasia.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaNombreFantasia.IdEstado, "ACT");
		nombreFantasia.IdPersona = entradaNombreFantasia.IdPersona;
		nombreFantasia.IdUsuarioCreador = entradaNombreFantasia.IdUsuarioCreador;
		nombreFantasia.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaNombreFantasiaMasivo.obtenerEntidadNombreFantasia", fin - inicio);
		
		return nombreFantasia;
	}
	
	public static void main(String [] args) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "    \"p_NombresFantasia\": [{\r\n"
					+ "        \"IdInterno\": 0,\r\n"
					+ "        \"IdPersona\": 1759102,\r\n"
					+ "        \"DescripcionNombreFantasia\": \"PRUEBABABABABABA111111\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdInterno\": 1,\r\n"
					+ "        \"IdPersona\": 1759102,\r\n"
					+ "        \"DescripcionNombreFantasia\": \"PRUEBABABABABABA222222\"\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdModulo\": \"SOC\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaCrearPersonaNombreFantasiaMasivo entradaCrearPersonaNombreFantasiaMasivo = gson.fromJson(jsonEntrada, EntradaCrearPersonaNombreFantasiaMasivo.class);
			CrearPersonaNombreFantasiaMasivo crearPersonaNombreFantasiaMasivo = new CrearPersonaNombreFantasiaMasivo();				
			try {
			    SalidaCrearPersonaNombreFantasiaMasivo salida = crearPersonaNombreFantasiaMasivo.logica(con, entradaCrearPersonaNombreFantasiaMasivo);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
}
