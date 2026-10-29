package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.pojo.Etapa;
import cl.bcs.plataforma.aut.pojo.TipoEvento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEtapaAutorizacion implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearEtapaAutorizacion.class);

	private final String SQL_SELECT_TIPO_EVENTO = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectTipoEvento.sql");
	private final String SQL_SELECT_ESTADO = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEstado.sql");
	private final String SQL_SELECT_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEtapa.sql");
	private final String SQL_INSERT_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_InsertEtapa.sql");
	
	private static final List<String> TIPO_AUTORIZACION = Arrays.asList("T", "M");

	public static class EntradaCrearEtapaAutorizacion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Etapa> p_EtapaAutorizacion;
		
		public EntradaCrearEtapaAutorizacion() {
			this.p_EtapaAutorizacion = new ArrayList<Etapa>();	
		}
	}

	public static class SalidaCrearEtapaAutorizacion extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaCrearEtapaAutorizacion() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class Estado extends BcsPojo {
		public String IdEstado;
	}

	public SalidaCrearEtapaAutorizacion logica(Connection conn, EntradaCrearEtapaAutorizacion in) throws Exception {
		logger.info("INICIO::CrearEtapaAutorizacion");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearEtapaAutorizacion salida = new SalidaCrearEtapaAutorizacion();	
		SQL sql = new SQL();
		HashMap<String, String> mapMsg = new HashMap<>(2);
		
		HashMap<String, Object> params = new HashMap<>(2);
		List<String> idsTipoEvento = in.p_EtapaAutorizacion.stream().map(x -> x.IdTipoEvento).distinct().collect(Collectors.toList());
		params.put("IdsTipoEvento", idsTipoEvento);
		params.put("IdEmpresa", in.p_IdEmpresa);
		logger.debug("INICIO::SQL_SELECT_TIPO_EVENTO");
		List<TipoEvento> listTipoEvento = sql.query(conn, SQL_SELECT_TIPO_EVENTO, params, TipoEvento.class);
		logger.debug("FIN::SQL_SELECT_TIPO_EVENTO");
		HashMap<String, Object> hashTiposEventos = Utils.List2Hash(listTipoEvento, "IdTipoEvento");
		
		logger.debug("INICIO::SQL_SELECT_ETAPA");
		List<Etapa> listEtapa = sql.query(conn, SQL_SELECT_ETAPA, params, Etapa.class);
		logger.debug("FIN::SQL_SELECT_ETAPA");

		logger.debug("INICIO::SQL_SELECT_ESTADO");
		List<Estado> listEstado = sql.query(conn, SQL_SELECT_ESTADO, new HashMap<>(0), Estado.class);
		logger.debug("FIN::SQL_SELECT_ESTADO");

		List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales", Long.valueOf(in.p_EtapaAutorizacion.size()));
		List<Etapa> listaEtapasCorrectas = new ArrayList<Etapa>();
		
		AtomicLong contador = new AtomicLong();
		AtomicInteger index = new AtomicInteger(0);
		for (Etapa etapaNueva : in.p_EtapaAutorizacion) {
			long idInterno = contador.getAndIncrement();
			int minimoAutorizan = etapaNueva.MinimoAutorizan == null ? 0 : etapaNueva.MinimoAutorizan;
			etapaNueva.MinimoAutorizan = ("T").equals(etapaNueva.TipoAutorizacion) ? 0 : minimoAutorizan;
			etapaNueva.IdEmpresa = in.p_IdEmpresa;
			
			TipoEvento tipoEvento = (TipoEvento) hashTiposEventos.get(etapaNueva.IdTipoEvento);

			if (tipoEvento == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_TIPO_EVENTO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			if (!("VIG").equals(tipoEvento.IdEstado)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_NO_VIGENTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			if (etapaNueva.Nombre == null || etapaNueva.Nombre.trim().isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_NOMBRE_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeNombre = listEtapa.stream().filter(e -> e.IdTipoEvento.equals(etapaNueva.IdTipoEvento))
					.anyMatch(e -> e.Nombre.trim().equals(etapaNueva.Nombre.trim()));
			
			if (existeNombre) {
				mapMsg.put("nombreEtapa", etapaNueva.Nombre);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_NOMBRE_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);	
			}
			
			if (etapaNueva.Codigo == null || etapaNueva.Codigo.trim().isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_CODIGO_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeCodigo = listEtapa.stream().filter(e-> e.IdTipoEvento.equals(etapaNueva.IdTipoEvento))
					.anyMatch(e -> e.Codigo.trim().equals(etapaNueva.Codigo.trim()));
			
			if (existeCodigo) {
				mapMsg.clear();
				mapMsg.put("codigoEtapa", etapaNueva.Codigo);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_CODIGO_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (etapaNueva.Orden == null || etapaNueva.Orden.equals(0)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_ORDEN_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeOrden = listEtapa.stream().filter(e-> e.IdTipoEvento.equals(etapaNueva.IdTipoEvento))
					.anyMatch(e -> e.Orden.equals(etapaNueva.Orden));
			
			if (existeOrden) {
				mapMsg.clear();
				mapMsg.put("ordenEtapa", etapaNueva.Orden.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ORDEN_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (etapaNueva.TipoAutorizacion == null || !TIPO_AUTORIZACION.contains(etapaNueva.TipoAutorizacion))  {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_TIPO_AUTORIZACION_NO_VALIDA", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (("M").equals(etapaNueva.TipoAutorizacion) && etapaNueva.MinimoAutorizan == 0)  {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_VALOR_MINIMO_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
						
		    boolean estadoValido = listEstado.stream().anyMatch(e -> e.IdEstado.equals(etapaNueva.IdEstado));

		    if (!estadoValido) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (salida.P_OUT_ERRORESCONTADOR == 0) {
				etapaNueva.IdEtapa = secuencias.get((index.getAndIncrement()));
				listaEtapasCorrectas.add(etapaNueva);	
			}
			
		}
		
		if (!listaEtapasCorrectas.isEmpty()) {
			mapMsg.clear();
			logger.debug("INICIO::SQL_INSERT_ETAPA");
			sql.batch(conn, SQL_INSERT_ETAPA, listaEtapasCorrectas, Etapa.class);
			logger.debug("FIN::SQL_INSERT_ETAPA");

			String mensaje = listaEtapasCorrectas.size() > 1 ? "UI_ETAPAS_CREADAS" : "UI_ETAPA_CREADA";
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, mensaje, mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearEtapaAutorizacion", fin - inicio);
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearEtapaAutorizacion in = gson.fromJson(jin.toString(), EntradaCrearEtapaAutorizacion.class);
		SalidaCrearEtapaAutorizacion out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"AUT\",\"p_EtapaAutorizacion\":[{\"IdTipoEvento\":\"ANUCOM\",\"Codigo\":\"KI\",\"Nombre\":\"ETAPA 3\",\"Orden\":3,\"TipoAutorizacion\":\"M\",\"MinimoAutorizan\":1,\"IdEstado\":\"VIG\"}]}";

			EntradaCrearEtapaAutorizacion objIn = gson.fromJson(json, EntradaCrearEtapaAutorizacion.class);
			CrearEtapaAutorizacion demo = new CrearEtapaAutorizacion();
			SalidaCrearEtapaAutorizacion ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
