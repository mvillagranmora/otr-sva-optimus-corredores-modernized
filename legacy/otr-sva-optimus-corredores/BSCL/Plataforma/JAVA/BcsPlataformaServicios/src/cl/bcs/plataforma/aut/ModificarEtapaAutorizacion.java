package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.pojo.Etapa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEtapaAutorizacion implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarEtapaAutorizacion.class);

	private final String SQL_SELECT_ESTADO = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEstado.sql");
	private final String SQL_SELECT_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_SelectEtapa.sql");
    private final String SQL_UPDATE_ETAPA_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/aut/sql/CrearEtapaAutorizacion_UpdateEtapaAutorizacion.sql");
    
	private static final List<String> TIPO_AUTORIZACION = Arrays.asList("T", "M");
	
	public static class EntradaModificarEtapaAutorizacion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Etapa> p_EtapaAutorizacion;
		
		public EntradaModificarEtapaAutorizacion() {
			this.p_EtapaAutorizacion = new ArrayList<Etapa>();	
		}
	}

	public static class SalidaModificarEtapaAutorizacion extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaModificarEtapaAutorizacion() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
    public static class Estado extends BcsPojo {
		public String IdEstado;
	}

	public SalidaModificarEtapaAutorizacion logica(Connection conn, EntradaModificarEtapaAutorizacion in) throws Exception {
		logger.info("INICIO::ModificarEtapaAutorizacion");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarEtapaAutorizacion salida = new SalidaModificarEtapaAutorizacion();	
		SQL sql = new SQL();
		HashMap<String, String> mapMsg = new HashMap<>(2);
						
		HashMap<String, Object> params = new HashMap<>(2);
		
		List<String> idsTipoEvento = in.p_EtapaAutorizacion.stream().map(x -> x.IdTipoEvento).distinct().collect(Collectors.toList());
		params.put("IdsTipoEvento", idsTipoEvento);
		params.put("IdEmpresa", in.p_IdEmpresa);
		
		logger.debug("INICIO::SQL_SELECT_ETAPA");
		List<Etapa> listEtapa = sql.query(conn, SQL_SELECT_ETAPA, params, Etapa.class);
		logger.debug("FIN::SQL_SELECT_ETAPA");

		logger.debug("INICIO::SQL_SELECT_ESTADO");
		List<Estado> listEstado = sql.query(conn, SQL_SELECT_ESTADO, new HashMap<>(0), Estado.class);
		logger.debug("FIN::SQL_SELECT_ESTADO");

		List<Etapa> listEtapaAutorizado = new ArrayList<Etapa>();
		
		AtomicLong contador = new AtomicLong();
		
		for (Etapa etapaAutorizadoModificado : in.p_EtapaAutorizacion) {
			long idInterno = contador.getAndIncrement();
			int minimoAutorizan = etapaAutorizadoModificado.MinimoAutorizan == null ? 0 : etapaAutorizadoModificado.MinimoAutorizan;
			etapaAutorizadoModificado.MinimoAutorizan = ("T").equals(etapaAutorizadoModificado.TipoAutorizacion) ? 0 : minimoAutorizan;
			etapaAutorizadoModificado.IdEmpresa = in.p_IdEmpresa;
			
			if (etapaAutorizadoModificado.Nombre == null || etapaAutorizadoModificado.Nombre.trim().isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_NOMBRE_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeNombre = listEtapa.stream().filter(e -> e.IdTipoEvento.equals(etapaAutorizadoModificado.IdTipoEvento) 
					&& e.IdEtapa.compareTo(etapaAutorizadoModificado.IdEtapa) != 0)
					.anyMatch(e -> e.Nombre.trim().equals(etapaAutorizadoModificado.Nombre.trim()));
			
			if (existeNombre) {
				mapMsg.put("nombreEtapa", etapaAutorizadoModificado.Nombre);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_NOMBRE_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);	
			}
			
			if (etapaAutorizadoModificado.Codigo == null || etapaAutorizadoModificado.Codigo.trim().isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_CODIGO_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeCodigo = listEtapa.stream().filter(e-> e.IdTipoEvento.equals(etapaAutorizadoModificado.IdTipoEvento)
					&& e.IdEtapa.compareTo(etapaAutorizadoModificado.IdEtapa) != 0)
					.anyMatch(e -> e.Codigo.trim().equals(etapaAutorizadoModificado.Codigo.trim()));
			
			if (existeCodigo) {
				mapMsg.clear();
				mapMsg.put("codigoEtapa", etapaAutorizadoModificado.Codigo);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_CODIGO_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (etapaAutorizadoModificado.Orden == null || etapaAutorizadoModificado.Orden.equals(0)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_ORDEN_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			boolean existeOrden = listEtapa.stream().filter(e-> e.IdTipoEvento.equals(etapaAutorizadoModificado.IdTipoEvento)
					&& e.IdEtapa.compareTo(etapaAutorizadoModificado.IdEtapa) != 0)
					.anyMatch(e -> e.Orden.equals(etapaAutorizadoModificado.Orden));
			
			if (existeOrden) {
				mapMsg.clear();
				mapMsg.put("ordenEtapa", etapaAutorizadoModificado.Orden.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ORDEN_ETAPA_EXISTE", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (etapaAutorizadoModificado.TipoAutorizacion == null || !TIPO_AUTORIZACION.contains(etapaAutorizadoModificado.TipoAutorizacion))  {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_TIPO_AUTORIZACION_NO_VALIDA", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (("M").equals(etapaAutorizadoModificado.TipoAutorizacion) && etapaAutorizadoModificado.MinimoAutorizan == 0)  {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_VALOR_MINIMO_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
						
		    boolean estadoValido = listEstado.stream().anyMatch(e -> e.IdEstado.equals(etapaAutorizadoModificado.IdEstado));

		    if (!estadoValido) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);			
			}
			
			if (salida.P_OUT_ERRORESCONTADOR == 0) {
				listEtapaAutorizado.add(etapaAutorizadoModificado);	
			}			
		}
		
		if (!listEtapaAutorizado.isEmpty()) {
			logger.debug("INICIO::SQL_UPDATE_ETAPA_AUTORIZACION");
			sql.batch(conn, SQL_UPDATE_ETAPA_AUTORIZACION, listEtapaAutorizado, Etapa.class);
			logger.debug("FIN::SQL_UPDATE_ETAPA_AUTORIZACION");
			
			String mensaje = listEtapaAutorizado.size() > 1 ? "AUTORIZADORES_MODIFICADOS" : "AUTORIZADOR_MODIFICADO";
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, mensaje, mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarEtapaAutorizacion", fin - inicio);
		
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarEtapaAutorizacion in = gson.fromJson(jin.toString(), EntradaModificarEtapaAutorizacion.class);
		SalidaModificarEtapaAutorizacion out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"AUT\",\"p_EtapaAutorizacion\":[{\"IdEtapa\":4831,\"Codigo\":\"ETAPA1\",\"Nombre\":\"ETAPA 2\",\"Orden\":21,\"TipoAutorizacion\":\"M\",\"MinimoAutorizan\":1,\"IdEstado\":\"VIG\",\"IdTipoEvento\":\"ANUCOM\"}]}";
			EntradaModificarEtapaAutorizacion objIn = gson.fromJson(json, EntradaModificarEtapaAutorizacion.class);
			ModificarEtapaAutorizacion demo = new ModificarEtapaAutorizacion();
			SalidaModificarEtapaAutorizacion ObjOut = demo.logica(conn, objIn);	

			System.out.println(ObjOut.toString());
			
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
	