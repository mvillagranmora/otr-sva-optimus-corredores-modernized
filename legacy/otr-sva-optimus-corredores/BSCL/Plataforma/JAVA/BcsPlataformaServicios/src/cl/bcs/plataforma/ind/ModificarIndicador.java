package cl.bcs.plataforma.ind;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.ind.ConfirmarModificarIndicador.EntradaConfirmarModificarIndicador;
import cl.bcs.plataforma.ind.ConfirmarModificarIndicador.SalidaConfirmarModificarIndicador;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarIndicador implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarIndicador.class);
	
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_InsertTablaPaso.sql");
	private final String SQL_DELETE_TABLA_PASO = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_DeleteTablaPaso.sql");
	private final String SQL_SELECT_INDICADORES_NUEVOS = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresNuevos.sql");
	private final String SQL_SELECT_INDICADORES_EXISTENTES = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresExistentes.sql");
	private final String SQL_INSERT_INDICADORES = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_InsertIndicadores.sql");
	private final String SQL_SELECT_INDICADORES_NUEVOS_NORMATIVOS_CLIENTE = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresNuevosNormativosCliente.sql");
	
	public static class EntradaModificarIndicador extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoEntidad;
		public Long p_IdEntidad;
		public String p_ModeloCliente;
		public List<RegistroAtributo> p_RegistroAtributo;
		
		public EntradaModificarIndicador() {
			this.p_ModeloCliente = "";
			this.p_RegistroAtributo = new ArrayList<RegistroAtributo>();
		}
	}
	
	public static class RegistroAtributo extends BcsPojo{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String ValorIndicador;
		public java.sql.Timestamp FechaVencimiento;
	}
	
	public static class RegistroAtributoTablaPaso extends RegistroAtributo{
		public Long IdTrabajo;
		public Long IdEntidad;
		public String IdTipoEntidad;
	}
	
	public static class Indicador extends BcsPojo{
		public Long IdIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public String ModeloCliente;
		public Long IdEmpresa;
		public String ValorIndicador;
		public String ValorAntiguo;
		public String RequiereAutorizacion;
		public java.sql.Timestamp FechaVencimiento;
		public java.sql.Timestamp FechaVencimientoAntiguo;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstadoAutorizacion;
	}
	
	public static class SalidaModificarIndicador extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Secuencia> P_OUT_IDSECUENCIAS;
		
		public SalidaModificarIndicador() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDSECUENCIAS = new ArrayList<Secuencia>();
		}
	}
	
	public static class Secuencia extends BcsPojo{
		public Long IdSecuencia;
	}
	
	public static class SalidaCrearEventoAutorizacionModificarIndicador extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearEventoAutorizacionModificarIndicador() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarIndicador jin = gson.fromJson(in.toString(), EntradaModificarIndicador.class);
		SalidaModificarIndicador out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarIndicador logica(Connection con, EntradaModificarIndicador entrada) throws Exception {
		logger.info("INICIO::ModificarIndicador");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarIndicador out = new SalidaModificarIndicador();
		Long idTrabajo = 0L;
		try {
			idTrabajo = insertarTablaPaso(con, entrada);
			
			List<Indicador> indicadoresNuevos = buscarIndicadoresNuevos(con, idTrabajo);
			List<Indicador> indicadoresExistentes = buscarIndicadoresExistentes(con, idTrabajo);
			
			insertarIndicadoresNuevos(con,entrada,indicadoresNuevos);
			insertarIndicadoresNuevosNormativosCliente(con,entrada,idTrabajo);
			
			List<Indicador> indicadoresSinAutorizacion = obtenerIndicadoresSinAutorizacion(indicadoresNuevos, indicadoresExistentes);
			List<Indicador> indicadoresConAutorizacion = obtenerIndicadoresConAutorizacion(indicadoresNuevos, indicadoresExistentes);
			
			Long idSecuenciaCambiosSinAutorizacion = crearSolicitudCambio(con, indicadoresSinAutorizacion);
			Long idSecuenciaCambiosConAutorizacion = crearSolicitudCambio(con, indicadoresConAutorizacion);
			
			SalidaConfirmarModificarIndicador salidaConfirmarModificarIndicadorSinAutorizacion = confirmarModificarIndicadorSinAutorizacion(con, entrada, idSecuenciaCambiosSinAutorizacion);
			out.P_OUT_ERRORESCONTADOR += salidaConfirmarModificarIndicadorSinAutorizacion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaConfirmarModificarIndicadorSinAutorizacion.P_OUT_MENSAJES);
			
			SalidaCrearEventoAutorizacionModificarIndicador salidaCrearEventoAutorizacionModificarIndicador = crearEventoAutorizacion(con, entrada, idSecuenciaCambiosConAutorizacion);
			out.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionModificarIndicador.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaCrearEventoAutorizacionModificarIndicador.P_OUT_MENSAJES);
			
			out.P_OUT_IDSECUENCIAS = obtenerSecuenciasSalida(idSecuenciaCambiosSinAutorizacion, idSecuenciaCambiosConAutorizacion);
		}catch(Exception e) {
			logger.error("ERROR::ModificarIndicador");
			throw e;
		}finally {
			borrarTablaPaso(con, idTrabajo);
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIndicador", finServicio - inicioServicio);
		return out;
		
	}
	
	private Long insertarTablaPaso(Connection con, EntradaModificarIndicador entrada) throws Exception {
		logger.debug("INICIO::ModificarIndicador.insertarTablaPaso");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		List<RegistroAtributoTablaPaso> registrosTablaPaso = entrada.p_RegistroAtributo
		.parallelStream()
		.map(registro->{
			RegistroAtributoTablaPaso registroTablaPaso = new RegistroAtributoTablaPaso();
			registroTablaPaso.IdTrabajo = idTrabajo;
			registroTablaPaso.IdEntidad = entrada.p_IdEntidad;
			registroTablaPaso.IdTipoEntidad = entrada.p_IdTipoEntidad;
			registroTablaPaso.IdIndicador = registro.IdIndicador;
			registroTablaPaso.IdTipoIndicador = registro.IdTipoIndicador;
			registroTablaPaso.ValorIndicador = registro.ValorIndicador;
			registroTablaPaso.FechaVencimiento = registro.FechaVencimiento;
			return registroTablaPaso;
		}).collect(Collectors.toList());
		sql.batch(con, SQL_INSERT_TABLA_PASO, registrosTablaPaso, RegistroAtributoTablaPaso.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.insertarTablaPaso", fin - inicio);
		return idTrabajo;
	}
	
	private List<Indicador> buscarIndicadoresNuevos(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ModificarIndicador.buscarIndicadoresNuevos");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		SQL sql = new SQL();
		List<Indicador> indicadoresNuevos = sql.query(con, SQL_SELECT_INDICADORES_NUEVOS, parametros, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.buscarIndicadoresNuevos", fin - inicio);
		return indicadoresNuevos;
	}
	
	private List<Indicador> buscarIndicadoresExistentes(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ModificarIndicador.buscarIndicadoresExistentes");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		SQL sql = new SQL();
		List<Indicador> indicadoresNuevos = sql.query(con, SQL_SELECT_INDICADORES_EXISTENTES, parametros, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.buscarIndicadoresExistentes", fin - inicio);
		return indicadoresNuevos;
	}
	
	private void insertarIndicadoresNuevos(Connection con, EntradaModificarIndicador entrada, List<Indicador> indicadoreNuevos) throws Exception {
		logger.debug("INICIO::ModificarIndicador.insertarIndicadoresNuevos");
		long inicio = System.currentTimeMillis();
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		List<Indicador> indicadores = indicadoreNuevos.parallelStream().map(indicador->{
			Indicador indicadorNuevo = new Indicador();
			indicadorNuevo.IdIndicador = indicador.IdIndicador;
			indicadorNuevo.IdEntidad = indicador.IdEntidad;
			indicadorNuevo.IdTipoEntidad = indicador.IdTipoEntidad;
			indicadorNuevo.IdTipoIndicador = indicador.IdTipoIndicador;
			indicadorNuevo.ModeloCliente = entrada.p_ModeloCliente;
			indicadorNuevo.IdEmpresa = entrada.p_IdEmpresa;
			indicadorNuevo.ValorIndicador = "";
			indicadorNuevo.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
			indicadorNuevo.IdEstadoAutorizacion = "PEN";
			return indicadorNuevo;
		}).collect(Collectors.toList());
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_INDICADORES, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.insertarIndicadoresNuevos", fin - inicio);
	}
	
	private void insertarIndicadoresNuevosNormativosCliente(Connection con, EntradaModificarIndicador entrada, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ModificarIndicador.insertarIndicadoresNuevosNormativosCliente");
		long inicio = System.currentTimeMillis();
		if("PERSONA".equals(entrada.p_IdTipoEntidad)) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(1);
			parametros.put("IdTrabajo", idTrabajo);
			SQL sql = new SQL();
			List<Indicador> indicadoresClienteNuevos = sql.query(con, SQL_SELECT_INDICADORES_NUEVOS_NORMATIVOS_CLIENTE, parametros, Indicador.class);
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
			List<Indicador> indicadores = indicadoresClienteNuevos.parallelStream().map(indicador->{
				Indicador indicadorNuevo = new Indicador();
				indicadorNuevo.IdIndicador = indicador.IdIndicador;
				indicadorNuevo.IdEntidad = indicador.IdEntidad;
				indicadorNuevo.IdTipoEntidad = "CLIENTE";
				indicadorNuevo.IdTipoIndicador = indicador.IdTipoIndicador;
				indicadorNuevo.ModeloCliente = entrada.p_ModeloCliente;
				indicadorNuevo.IdEmpresa = entrada.p_IdEmpresa;
				indicadorNuevo.ValorIndicador = "";
				indicadorNuevo.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
				indicadorNuevo.IdEstadoAutorizacion = "ING";
				return indicadorNuevo;
			}).collect(Collectors.toList());
			sql.batch(con, SQL_INSERT_INDICADORES, indicadores, Indicador.class);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.insertarIndicadoresNuevosNormativosCliente", fin - inicio);
	}
	
	private List<Indicador> obtenerIndicadoresSinAutorizacion(List<Indicador> indicadoresNuevos, List<Indicador> indicadoresExistentes){
		logger.debug("INICIO::ModificarIndicador.obtenerIndicadoresSinAutorizacion");
		long inicio = System.currentTimeMillis();
		List<Indicador> indicadoresSinAutorizacion = new ArrayList<Indicador>();
		indicadoresSinAutorizacion.addAll(indicadoresNuevos.parallelStream().filter(indicador->"N".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		indicadoresSinAutorizacion.addAll(indicadoresExistentes.parallelStream().filter(indicador->"N".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.obtenerIndicadoresSinAutorizacion", fin - inicio);
		return indicadoresSinAutorizacion;
	}
	
	private List<Indicador> obtenerIndicadoresConAutorizacion(List<Indicador> indicadoresNuevos, List<Indicador> indicadoresExistentes){
		logger.debug("INICIO::ModificarIndicador.obtenerIndicadoresSinAutorizacion");
		long inicio = System.currentTimeMillis();
		List<Indicador> indicadoresSinAutorizacion = new ArrayList<Indicador>();
		indicadoresSinAutorizacion.addAll(indicadoresNuevos.parallelStream().filter(indicador->"S".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		indicadoresSinAutorizacion.addAll(indicadoresExistentes.parallelStream().filter(indicador->"S".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.obtenerIndicadoresSinAutorizacion", fin - inicio);
		return indicadoresSinAutorizacion;
	}
	
	private Long crearSolicitudCambio(Connection con, List<Indicador> indicadores) throws Exception {
		logger.debug("INICIO::ModificarIndicador.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		Long idSolicitudCambioSalida = null;
		if(indicadores.size() > 0) {
			List<Long> secuenciasSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long idSolicitudCambio = secuenciasSolicitudCambio.get(0);
			EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
			idSolicitudCambioSalida = idSolicitudCambio;
			
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> solicitudesValorIndicador = indicadores.parallelStream().map(indicador->{
				CrearRegistroSolicitudCambio.RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = "INDICADOR";
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(indicador.IdIndicador);
				registroSolicitudCambio.CampoModificado = "ValorIndicador";
				registroSolicitudCambio.TipoDato = "NVARCHAR";
				registroSolicitudCambio.ValorAntiguo = indicador.ValorAntiguo;
				registroSolicitudCambio.ValorNuevo = indicador.ValorIndicador;
				registroSolicitudCambio.IdEstadoAutorizacion = "PEN";
				return registroSolicitudCambio;
			}).collect(Collectors.toList());
			
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> solicitudesFechaVencimiento = indicadores.parallelStream().map(indicador->{
				CrearRegistroSolicitudCambio.RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = "INDICADOR";
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(indicador.IdIndicador);
				registroSolicitudCambio.CampoModificado = "FechaVencimiento";
				registroSolicitudCambio.TipoDato = "SECONDDATE";
				registroSolicitudCambio.ValorAntiguo = Fecha.toStringDatetime(indicador.FechaVencimientoAntiguo);
				registroSolicitudCambio.ValorNuevo = Fecha.toStringDatetime(indicador.FechaVencimiento);
				registroSolicitudCambio.IdEstadoAutorizacion = "PEN";
				return registroSolicitudCambio;
			}).collect(Collectors.toList());
			entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio.addAll(solicitudesValorIndicador);
			entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio.addAll(solicitudesFechaVencimiento);
			CrearRegistroSolicitudCambio crearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
			crearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.crearSolicitudCambio", fin - inicio);
		return idSolicitudCambioSalida;
	}
	
	private SalidaConfirmarModificarIndicador confirmarModificarIndicadorSinAutorizacion(Connection con, EntradaModificarIndicador entrada, Long idSolicitudCambio) throws Exception {
		logger.debug("INICIO::ModificarIndicador.confirmarModificarIndicadorSinAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaConfirmarModificarIndicador salidaConfirmarModificarIndicador = new SalidaConfirmarModificarIndicador();
		
		if(idSolicitudCambio != null) {
			EntradaConfirmarModificarIndicador entradaConfirmarModificarIndicador = new EntradaConfirmarModificarIndicador();
			entradaConfirmarModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmarModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmarModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmarModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaConfirmarModificarIndicador.p_IdSolicitudCambio = idSolicitudCambio;
			entradaConfirmarModificarIndicador.p_IdEstadoAutorizacion = "ING";
			entradaConfirmarModificarIndicador.p_IdTipoEntidad = entrada.p_IdTipoEntidad;
			entradaConfirmarModificarIndicador.p_IdEntidad = 0L;
			ConfirmarModificarIndicador confirmarModificarIndicador = new ConfirmarModificarIndicador();
			salidaConfirmarModificarIndicador = confirmarModificarIndicador.logica(con, entradaConfirmarModificarIndicador);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.confirmarModificarIndicadorSinAutorizacion", fin - inicio);
		return salidaConfirmarModificarIndicador;
	}
	
	private SalidaCrearEventoAutorizacionModificarIndicador crearEventoAutorizacion(Connection con, EntradaModificarIndicador entrada, Long idSolicitudCambio) throws Exception {
		logger.debug("INICIO::ModificarIndicador.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearEventoAutorizacionModificarIndicador salidaCrearEventoAutorizacionModificarIndicador = new SalidaCrearEventoAutorizacionModificarIndicador();
		if(idSolicitudCambio != null) {
			CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
			entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
			entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
			entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
			entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
			entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
			
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = "MODIND";
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = "MODIND";
			evento.NumeroOperacionConcepto = String.valueOf(idSolicitudCambio);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = entrada.p_IdEntidad;
			evento.Glosa = entrada.p_IdTipoEntidad;
			
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
			
			CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
			
			salidaCrearEventoAutorizacionModificarIndicador.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
			List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
			List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
			
			salidaCrearEventoAutorizacionModificarIndicador.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
			salidaCrearEventoAutorizacionModificarIndicador.P_OUT_MENSAJES.addAll(mensajesErrores);
			
			if(salidaCrearEventoAutorizacionModificarIndicador.P_OUT_ERRORESCONTADOR.equals(0L)) {
				salidaCrearEventoAutorizacionModificarIndicador.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.crearEventoAutorizacion", fin - inicio);
		return salidaCrearEventoAutorizacionModificarIndicador;	
	}
	
	private List<Secuencia> obtenerSecuenciasSalida(Long idSecuenciaCambiosSinAutorizacion, Long idSecuenciaCambiosConAutorizacion){
		logger.debug("INICIO::ModificarIndicador.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		List<Secuencia> secuencias = new ArrayList<Secuencia>();
		if(idSecuenciaCambiosSinAutorizacion != null) {
			Secuencia secuencia = new Secuencia();
			secuencia.IdSecuencia = idSecuenciaCambiosSinAutorizacion;
			secuencias.add(secuencia);
		}
		if(idSecuenciaCambiosConAutorizacion != null) {
			Secuencia secuencia = new Secuencia();
			secuencia.IdSecuencia = idSecuenciaCambiosConAutorizacion;
			secuencias.add(secuencia);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.crearEventoAutorizacion", fin - inicio);
		return secuencias;
	}
	
	private void borrarTablaPaso(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ModificarIndicador.borrarTablaPaso");
		long inicio = System.currentTimeMillis();
		RegistroAtributoTablaPaso registroTablaPaso = new RegistroAtributoTablaPaso();
		registroTablaPaso.IdTrabajo = idTrabajo;
		List<RegistroAtributoTablaPaso> registrosTablaPaso = new ArrayList<RegistroAtributoTablaPaso>();
		registrosTablaPaso.add(registroTablaPaso);
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_TABLA_PASO, registrosTablaPaso, RegistroAtributoTablaPaso.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarIndicador.borrarTablaPaso", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		EntradaModificarIndicador Objin = new EntradaModificarIndicador();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdUsuario = 2L;
		Objin.p_IdModulo = "PLA";
		Objin.p_UsaReglasNegocio = "N";
		Objin.p_IdTipoEntidad = "PERSONA";
		Objin.p_IdEntidad = 621869L;
		Objin.p_ModeloCliente = "";
		RegistroAtributo registroAtributo = new RegistroAtributo();
		registroAtributo.IdIndicador = 4051907L;
		registroAtributo.IdTipoIndicador = 5L;
		registroAtributo.ValorIndicador = "N";
		registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
		Objin.p_RegistroAtributo.add(registroAtributo);
		RegistroAtributo registroAtributo2 = new RegistroAtributo();
		registroAtributo2.IdIndicador = 0L;
		registroAtributo2.IdTipoIndicador = 10042L;
		registroAtributo2.ValorIndicador = "803";
		registroAtributo2.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
		Objin.p_RegistroAtributo.add(registroAtributo2);
		ModificarIndicador demo=new ModificarIndicador();				
		try {
			SalidaModificarIndicador ObjOut=demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.commit();
			}else {
				con.rollback();
			}
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
