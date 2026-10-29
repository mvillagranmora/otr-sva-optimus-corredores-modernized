package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.pojo.Mensaje;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearMensaje implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearMensaje.class);
	
	private String SQL_UPDATE_DESABILITA_MENSAJES_POR_TIPO = loadResourceToString("cl/bcs/plataforma/evt/sql/ComunesMensaje_UpdateDeshabilitaMensajesPorTipo.sql");
	private String SQL_INSERT_MENSAJE = loadResourceToString("cl/bcs/plataforma/evt/sql/CrearMensaje_InsertMensaje.sql");
	private final String BIENVENIDA = "BNVNDA";
	
	public static class EntradaCrearMensaje extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Mensaje> p_Mensajes;
	}
	
	public static class SalidaCrearMensaje extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearMensaje() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearMensaje jin = gson.fromJson(in.toString(), EntradaCrearMensaje.class);
		SalidaCrearMensaje out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearMensaje logica(Connection con, EntradaCrearMensaje entrada) throws Exception {
		logger.info("INICIO::CrearMensaje");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearMensaje out = new SalidaCrearMensaje();
		
		List<Mensaje> mensajes = obtenerEntidadesMensaje(con, entrada.p_Mensajes);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		if(mensajes.parallelStream().anyMatch( mensaje-> ("BNVNDA".equals(mensaje.IdTipoMensaje)) && ("S".equals(mensaje.Habilitado)) )) {
			deshabilitarBienvenidaPrevia(con);
		}
		
		insertarMensaje(con, mensajes);
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(mensajes);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearMensaje", finServicio - inicioServicio);
		return out;
	}
	
	private void deshabilitarBienvenidaPrevia(Connection con)  throws Exception {
		logger.trace("INICIO::CrearMensaje.deshabilitarBienvenidaPrevia");
		long inicio = System.currentTimeMillis();
		
		Mensaje mensaje = new Mensaje();
		mensaje.IdTipoMensaje = BIENVENIDA;
		
		List<Mensaje> parametros = Arrays.asList(mensaje);
		
		SQL sql = new SQL();
		
		sql.batch(con, SQL_UPDATE_DESABILITA_MENSAJES_POR_TIPO, parametros, Mensaje.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearMensaje.deshabilitarBienvenidaPrevia", fin - inicio);
	}
	
	private List<Mensaje> obtenerEntidadesMensaje(Connection con, List<Mensaje> mensajes )  throws Exception{
		logger.trace("INICIO::CrearMensaje.obtenerEntidadesMensaje");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Mensaje", Long.valueOf(mensajes.size()));
		AtomicInteger index = new AtomicInteger();
		List<Mensaje> entidadesMensajes = mensajes.parallelStream().map(entidadMensaje->{
			Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = entidadMensaje.IdInterno;
			mensaje.IdMensaje = secuencias.get(index.getAndIncrement());
			mensaje.IdTipoMensaje = entidadMensaje.IdTipoMensaje;
			mensaje.IdTipoEntidadDestinatario = entidadMensaje.IdTipoEntidadDestinatario;
			mensaje.Titulo =  entidadMensaje.Titulo;
			mensaje.MensajeCorto = entidadMensaje.MensajeCorto;
			mensaje.MensajeLargo = entidadMensaje.MensajeLargo;
			mensaje.FechaVigenciaDesde = entidadMensaje.FechaVigenciaDesde;
			mensaje.FechaVigenciaHasta = entidadMensaje.FechaVigenciaHasta;
			mensaje.Habilitado = entidadMensaje.Habilitado;
			mensaje.MuestraMensajeCorto = entidadMensaje.MuestraMensajeCorto;
			mensaje.UrlAviso = entidadMensaje.UrlAviso;
			mensaje.IdTipoCanal = entidadMensaje.IdTipoCanal;
			return mensaje;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearMensaje.obtenerEntidadesMensaje", fin - inicio);
		return entidadesMensajes;
	}
	
	
	private void insertarMensaje(Connection con, List<Mensaje> mensajes) throws Exception {
		logger.debug("INICIO::CrearMensaje.insertarMensaje");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		sql.batch(con, SQL_INSERT_MENSAJE, mensajes, Mensaje.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearMensaje.insertarMensaje", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<Mensaje> mensajes){
		logger.trace("INICIO::CrearMensaje.obtenerEntidadesInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = mensajes
				.parallelStream()
				.map(mensaje->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = mensaje.IdInterno;
					entidad.IdEntidad = mensaje.IdMensaje;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearMensaje.obtenerEntidadesInsertados", fin - inicio);
		return entidades;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Mensajes\":[{\"IdInterno\":1, \"FechaVigenciaDesde\": \"2021-01-01\",\"FechaVigenciaHasta\":\"2022-01-01\",\"Habilitado\": \"S\",\"IdTipoEntidadDestinatario\": \"CLI\",\"IdTipoMensaje\": \"BNVNDA\",\"Titulo\": \"NOMBRE_DEL_AVISO\",\"MensajeCorto\": \"ESTE ES UN MENSAJE CORTO\",\"MensajeLargo\": \"ESTE ES UN MENSAJE LARGO, QUE SE PUEDE EXTENDER HASTA 500 CARACTERES\",\"MuestraMensajeCorto\": \"N\",\"UrlAviso\": \"\",\"IdTipoCanal\": \"CLI\"}]}";
			EntradaCrearMensaje Objin = gson.fromJson(json, EntradaCrearMensaje.class);

			CrearMensaje demo = new CrearMensaje();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaCrearMensaje ObjOut = demo.logica(con, Objin);
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
