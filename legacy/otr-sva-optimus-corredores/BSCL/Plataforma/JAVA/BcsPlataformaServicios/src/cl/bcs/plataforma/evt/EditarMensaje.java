package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.ValidarExistenciaMensaje.EntradaValidarExistenciaMensaje;
import cl.bcs.plataforma.evt.pojo.Mensaje;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarMensaje implements JavaCall {

	static final Logger logger = LogManager.getLogger(EditarMensaje.class);
	
	private String SQL_UPDATE_DESABILITA_MENSAJES_POR_TIPO = loadResourceToString("cl/bcs/plataforma/evt/sql/ComunesMensaje_UpdateDeshabilitaMensajesPorTipo.sql");
	private String SQL_UPDATE_MENSAJE = loadResourceToString("cl/bcs/plataforma/evt/sql/EditarMensaje_UpdateMensaje.sql");
	private final String BIENVENIDA = "BNVNDA";
	
	public static class EntradaEditarMensaje extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Mensaje> p_Mensajes;
	}
	
	public static class SalidaEditarMensaje extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaEditarMensaje() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID = new ArrayList<>();
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if(validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}
		
		public void agregarErroresMensajesSalida(SalidaEditarMensaje salidaEditarMensaje) {
			this.P_OUT_ERRORESCONTADOR += salidaEditarMensaje.P_OUT_ERRORESCONTADOR;
			this.P_OUT_MENSAJES.addAll(salidaEditarMensaje.P_OUT_MENSAJES);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEditarMensaje jin = gson.fromJson(in.toString(), EntradaEditarMensaje.class);
		SalidaEditarMensaje out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEditarMensaje logica(Connection con, EntradaEditarMensaje entrada) throws Exception {
		logger.info("INICIO::EditarMensaje");
		long inicioServicio = System.currentTimeMillis();
		SalidaEditarMensaje out = new SalidaEditarMensaje();
		
		ValidarExistenciaMensaje validarExistenciaMensaje = new ValidarExistenciaMensaje();
		EntradaValidarExistenciaMensaje entradaValidarExistenciaMensaje = new EntradaValidarExistenciaMensaje();
		entradaValidarExistenciaMensaje.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaMensaje.p_Mensajes = entrada.p_Mensajes;
		
		List<Validacion> validacionesExistenciaMensajes = validarExistenciaMensaje.logica(con, entradaValidarExistenciaMensaje).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaMensajes);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Mensaje> mensajes = obtenerEntidadesMensaje(con, entrada.p_Mensajes);
		List<Mensaje> bienvenidasHabilitadas = mensajes.parallelStream().filter( mensaje-> ( "BNVNDA".equals(mensaje.IdTipoMensaje) && "S".equals(mensaje.Habilitado) ) ).collect(Collectors.toList());
		logger.info(bienvenidasHabilitadas);
		logger.info(mensajes.parallelStream().anyMatch( mensaje-> ( "BNVNDA".equals(mensaje.IdTipoMensaje) && "S".equals(mensaje.Habilitado) ) ));
		
		if(mensajes.parallelStream().anyMatch( mensaje-> ("BNVNDA".equals(mensaje.IdTipoMensaje)) && ("S".equals(mensaje.Habilitado)) )) {
			deshabilitarBienvenidaPrevia(con);
		}
		
		actualizarMensaje(con, mensajes);
		
		out.P_OUT_REFID = obtenerEntidadesModificados(mensajes);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EditarMensaje", finServicio - inicioServicio);
		return out;
	}
	
	private List<Mensaje> obtenerEntidadesMensaje(Connection con, List<Mensaje> mensajes )  throws Exception{
		logger.trace("INICIO::EditarMensaje.obtenerEntidadesMensaje");
		long inicio = System.currentTimeMillis();
	
		List<Mensaje> entidadesMensajes = mensajes.parallelStream().map(entidadMensaje->{
			Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = entidadMensaje.IdInterno;
			mensaje.IdMensaje = entidadMensaje.IdMensaje;
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
		logger.trace("FIN::EditarMensaje.obtenerEntidadesMensaje", fin - inicio);
		return entidadesMensajes;
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
	
	private void actualizarMensaje(Connection con, List<Mensaje> mensajes) throws Exception {
		logger.debug("INICIO::EditarMensaje.actualizarMensaje");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		sql.batch(con, SQL_UPDATE_MENSAJE, mensajes, Mensaje.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EditarMensaje.actualizarMensaje", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesModificados(List<Mensaje> mensajes){
		logger.trace("INICIO::EditarMensaje.obtenerEntidadesModificados");
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
		logger.trace("FIN::EditarMensaje.obtenerEntidadesModificados", fin - inicio);
		return entidades;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_Mensajes\":[{\"IdInterno\":1, \"IdMensaje\":8, \"FechaVigenciaDesde\": \"2021-01-01\",\"FechaVigenciaHasta\":\"2022-01-01\",\"Habilitado\": \"N\",\"IdTipoEntidadDestinatario\": \"CLI\",\"IdTipoMensaje\": \"AVISO\",\"Titulo\": \"NOMBRE_DEL_AVISO\",\"MensajeCorto\": \"ESTE ES UN TEXTO\",\"MensajeLargo\": \"ESTE ES UN TEXTO\",\"MuestraMensajeCorto\": \"N\",\"UrlAviso\": \"\",\"IdTipoCanal\": \"CLI\"},{\"IdInterno\":1, \"IdMensaje\":7, \"FechaVigenciaDesde\": \"2021-01-01\",\"FechaVigenciaHasta\":\"2022-01-01\",\"Habilitado\": \"S\",\"IdTipoEntidadDestinatario\": \"CLI\",\"IdTipoMensaje\": \"BNVNDA\",\"Titulo\": \"NOMBRE_DEL_AVISO\",\"MensajeCorto\": \"ESTE ES UN TEXTO\",\"MensajeLargo\": \"ESTE ES UN TEXTO\",\"MuestraMensajeCorto\": \"N\",\"UrlAviso\": \"\",\"IdTipoCanal\": \"CLI\"}]}";
			EntradaEditarMensaje Objin = gson.fromJson(json, EntradaEditarMensaje.class);

			EditarMensaje demo = new EditarMensaje();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaEditarMensaje ObjOut = demo.logica(con, Objin);
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
