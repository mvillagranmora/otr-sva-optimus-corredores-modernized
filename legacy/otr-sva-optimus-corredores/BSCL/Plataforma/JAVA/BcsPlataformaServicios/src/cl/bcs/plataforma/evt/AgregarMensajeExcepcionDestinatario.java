package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.pojo.MensajeExcepcionDestinatario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarMensajeExcepcionDestinatario implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AgregarMensajeExcepcionDestinatario.class);
	
	private String SQL_INSERT_MENSAJE_DESTINATARIO_EXCEPCION = loadResourceToString("cl/bcs/plataforma/evt/sql/AgregarMensajeExcepcionDestinatario_InsertMensajeExcepcionDestinatario.sql");

	
	public static class EntradaAgregarMensajeExcepcionDestinatario extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<MensajeExcepcionDestinatario> p_MensajesExcepcionDestinatario;
	}
	
	public static class SalidaAgregarMensajeExcepcionDestinatario extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaAgregarMensajeExcepcionDestinatario() {
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
		EntradaAgregarMensajeExcepcionDestinatario jin = gson.fromJson(in.toString(), EntradaAgregarMensajeExcepcionDestinatario.class);
		SalidaAgregarMensajeExcepcionDestinatario out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAgregarMensajeExcepcionDestinatario logica(Connection con, EntradaAgregarMensajeExcepcionDestinatario entrada) throws Exception {
		logger.info("INICIO::AgregarMensajeExcepcionDestinatario");
		long inicioServicio = System.currentTimeMillis();
		SalidaAgregarMensajeExcepcionDestinatario out = new SalidaAgregarMensajeExcepcionDestinatario();
		
		List<MensajeExcepcionDestinatario> excepciones = obtenerEntidadesExcepciones(con, entrada.p_MensajesExcepcionDestinatario);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		insertarMensajeExcepcionDestinatario(con, excepciones);
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(excepciones);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AgregarMensajeExcepcionDestinatario", finServicio - inicioServicio);
		return out;
	}
	
	private List<MensajeExcepcionDestinatario> obtenerEntidadesExcepciones(Connection con, List<MensajeExcepcionDestinatario> excepciones )  throws Exception{
		logger.trace("INICIO::AgregarMensajeExcepcionDestinatario.obtenerEntidadesExcepciones");
		long inicio = System.currentTimeMillis();
		
		List<MensajeExcepcionDestinatario> entidadesExcepciones = excepciones.parallelStream().map(entidadExcepcion->{
			MensajeExcepcionDestinatario excepcion = new MensajeExcepcionDestinatario();
			excepcion.IdInterno = entidadExcepcion.IdInterno;
			excepcion.IdMensaje = entidadExcepcion.IdMensaje;
			excepcion.IdPersonaDestinatario = entidadExcepcion.IdPersonaDestinatario;
					
			return excepcion;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarMensajeExcepcionDestinatario.obtenerEntidadesExcepciones", fin - inicio);
		return entidadesExcepciones;
	}
	
	
	private void insertarMensajeExcepcionDestinatario(Connection con, List<MensajeExcepcionDestinatario> excepciones) throws Exception {
		logger.debug("INICIO::AgregarMensajeExcepcionDestinatario.insertarMensajeExcepcionDestinatario");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		sql.batch(con, SQL_INSERT_MENSAJE_DESTINATARIO_EXCEPCION, excepciones, MensajeExcepcionDestinatario.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarMensajeExcepcionDestinatario.insertarMensajeExcepcionDestinatario", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<MensajeExcepcionDestinatario> excepciones){
		logger.trace("INICIO::AgregarMensajeExcepcionDestinatario.obtenerEntidadesInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = excepciones
				.parallelStream()
				.map(excepcion->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = excepcion.IdInterno;
					entidad.IdEntidad = excepcion.IdPersonaDestinatario;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarMensajeExcepcionDestinatario.obtenerEntidadesInsertados", fin - inicio);
		return entidades;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_MensajesExcepcionDestinatario\":[{\"IdInterno\":1, \"IdMensaje\": 5,\"IdPersonaDestinatario\":1773891 }]}";
			EntradaAgregarMensajeExcepcionDestinatario Objin = gson.fromJson(json, EntradaAgregarMensajeExcepcionDestinatario.class);

			AgregarMensajeExcepcionDestinatario demo = new AgregarMensajeExcepcionDestinatario();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaAgregarMensajeExcepcionDestinatario ObjOut = demo.logica(con, Objin);
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
