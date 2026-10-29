package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.pojo.AutorizadoEtapa;
import cl.bcs.plataforma.aut.pojo.Etapa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearAutorizadoEtapa implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearAutorizadoEtapa.class);

	private final String SQL_SELECT_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectEtapa.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectUsuario.sql");
	private final String SQL_SELECT_AUTORIZADO_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectAutorizadoEtapa.sql");
	private final String SQL_SELECT_ROL = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRol.sql");
	private final String SQL_INSERT_AUTORIZADO_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_InsertAutorizadoEtapa.sql");
	private final String SQL_SELECT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRolUsuario.sql");
	
	public static class EntradaCrearAutorizadoEtapa extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<AutorizadoEtapa> p_AutorizadoEtapa;
		
		public EntradaCrearAutorizadoEtapa() {
			this.p_AutorizadoEtapa = new ArrayList<>();	
		}
	}

	public static class SalidaCrearAutorizadoEtapa extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaCrearAutorizadoEtapa() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class Usuario extends BcsPojo{
		public Long IdUsuario;
		public Long IdEmpresa;
		public String CodigoUsuario;
		public String IdEstado;
	}
	
	public static class Rol extends BcsPojo{
		public Long IdRol;	
		public Long IdEmpresa;
		public String Nombre;	
		public String IdEstado;
	}
	
	public static class RolUsuario extends BcsPojo{
        public Long IdUsuario;
        public Long IdRol;
        public String KeyRolUsuario;
    }

	public SalidaCrearAutorizadoEtapa logica(Connection conn, EntradaCrearAutorizadoEtapa in) throws Exception {
		logger.info("INICIO::CrearAutorizadoEtapa");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearAutorizadoEtapa salida = new SalidaCrearAutorizadoEtapa();	
		SQL sql = new SQL();
		AtomicLong contador = new AtomicLong(0L);
		HashMap<String, String> mapMsg = new HashMap<>(2);
		List<AutorizadoEtapa> listAutorizadosEtapaCorrectos = new ArrayList<>();
				
        List<Long> idsEtapas = obtenerIdsUnicos(in.p_AutorizadoEtapa, autorizado -> Stream.of(autorizado.IdEtapa));
        List<Long> idsUsuarios = obtenerIdsUnicos(in.p_AutorizadoEtapa, autorizado -> Stream.of(autorizado.IdUsuario, autorizado.IdUsuarioReemplaza));
        List<Long> idsRoles = obtenerIdsUnicos(in.p_AutorizadoEtapa, autorizado -> Stream.of(autorizado.IdRol));
        
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdsEtapas", idsEtapas);
		logger.debug("INICIO::SQL_SELECT_ETAPA");
		List<Etapa> listEtapas = sql.query(conn, SQL_SELECT_ETAPA, params, Etapa.class);
		logger.debug("FIN::SQL_SELECT_ETAPA");
		HashMap<Long,Object> hashEtapas = Utils.List2Hash(listEtapas, "IdEtapa");
		
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdsUsuarios", idsUsuarios);
		logger.debug("INICIO::SQL_SELECT_USUARIO");
		List<Usuario> listUsuarios = sql.query(conn, SQL_SELECT_USUARIO, params, Usuario.class);
		logger.debug("FIN::SQL_SELECT_USUARIO");
		HashMap<Long,Object> hashUsuarios = Utils.List2Hash(listUsuarios, "IdUsuario");

		params.clear();
		params.put("IdsEtapas", idsEtapas);
		logger.debug("INICIO::SQL_SELECT_AUTORIZADO_ETAPA");
		List<AutorizadoEtapa> listAutorizadoEtapaSistema = sql.query(conn, SQL_SELECT_AUTORIZADO_ETAPA, params, AutorizadoEtapa.class);
		logger.debug("FIN::SQL_SELECT_AUTORIZADO_ETAPA");
		HashMap<String,Object> hashAutorizadoEtapaSistema = Utils.List2Hash(listAutorizadoEtapaSistema, "KeyAutorizadoEtapa");
		
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdsRoles", idsRoles);
		logger.debug("INICIO::SQL_SELECT_ROL");
		List<Rol> listRol = sql.query(conn, SQL_SELECT_ROL, params, Rol.class);
		logger.debug("FIN::SQL_SELECT_ROL");
		HashMap<Long,Object> hashRoles = Utils.List2Hash(listRol, "IdRol");
		
		params.clear();
		params.put("IdsUsuarios", idsUsuarios);
		logger.debug("INICIO::SQL_SELECT_ROL_USUARIO");
        List<RolUsuario> listRolUsuario = sql.query(conn, SQL_SELECT_ROL_USUARIO, params, RolUsuario.class);
		logger.debug("FIN::SQL_SELECT_ROL_USUARIO");
		HashMap<String,Object> hashRolesUsuarios = Utils.List2Hash(listRolUsuario, "KeyRolUsuario");
		
		List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales", Long.valueOf(in.p_AutorizadoEtapa.size()));
		AtomicInteger index = new AtomicInteger(0);
		
		for(AutorizadoEtapa autorizadoEtapaNuevo : in.p_AutorizadoEtapa) {
			long idInterno = contador.getAndIncrement();
			//VALIDAR ETAPA
			Etapa etapa = (Etapa) hashEtapas.get(autorizadoEtapaNuevo.IdEtapa);
			if(etapa == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_ETAPA_TIPO_EVENTO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			if("INACTIVO".equals(etapa.NombreEstado)){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ETAPA_TIPO_EVENTO_INACTIVA", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			//VALIDAR EXISTENCIA AUTORIZADO
			String keyAutorizadoEtapa = autorizadoEtapaNuevo.IdEtapa.toString() + autorizadoEtapaNuevo.IdUsuario.toString() + autorizadoEtapaNuevo.IdRol.toString();
			AutorizadoEtapa autorizadoEtapaExistente = (AutorizadoEtapa) hashAutorizadoEtapaSistema.get(keyAutorizadoEtapa);	
			
			Usuario usuarioEtapa = (Usuario) hashUsuarios.get(autorizadoEtapaNuevo.IdUsuario);
			
			if("N".equals(autorizadoEtapaNuevo.EsAutorizacionPorRol)) {	
				if(usuarioEtapa == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				Set<Long> idsUsuariosAutorizadoresEtapa = listAutorizadoEtapaSistema.stream().filter(u -> u.IdUsuario != 0).map(u -> u.IdUsuario).collect(Collectors.toSet());
				boolean existeUsuarioAutorizador = idsUsuariosAutorizadoresEtapa.contains(autorizadoEtapaNuevo.IdUsuario);
				
				if ((autorizadoEtapaExistente != null && autorizadoEtapaExistente.IdUsuario.equals(autorizadoEtapaNuevo.IdUsuario)) || existeUsuarioAutorizador) {
					mapMsg.clear();
				    mapMsg.put("NombreUsuario", usuarioEtapa.CodigoUsuario);
				    mapMsg.put("NombreEtapa", etapa.Nombre);
				    salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_YA_ESTA_ASOCIADO_A_LA_ETAPA", mapMsg, conn, in.p_IdEmpresa));
				    salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
			}
			
			//VALIDAR ROL
			if(autorizadoEtapaNuevo.IdRol != 0L){
				Rol rol = (Rol) hashRoles.get(autorizadoEtapaNuevo.IdRol);
				
				String keyRolUsuario = autorizadoEtapaNuevo.IdUsuario.toString() + autorizadoEtapaNuevo.IdRol.toString();
				RolUsuario rolUsuario = (RolUsuario) hashRolesUsuarios.get(keyRolUsuario);
				
				if(rol == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_ROL", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(rolUsuario == null && ("N").equals(autorizadoEtapaNuevo.EsAutorizacionPorRol)) {
					mapMsg.clear();
					mapMsg.put("NombreUsuario", usuarioEtapa.CodigoUsuario);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ROL_NO_ASOCIADO_A_USUARIO", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(autorizadoEtapaExistente != null) {
					boolean esAutorizacionPorRol = ("S").equals(autorizadoEtapaNuevo.EsAutorizacionPorRol);				    
				    boolean rolExiste = autorizadoEtapaExistente.IdRol.equals(autorizadoEtapaNuevo.IdRol);
				    boolean usuarioExiste = autorizadoEtapaExistente.IdUsuario.equals(autorizadoEtapaNuevo.IdUsuario);

				    if ((esAutorizacionPorRol && rolExiste) || (!esAutorizacionPorRol && rolExiste && usuarioExiste)) {
				    	mapMsg.clear();
						mapMsg.put("NombreRol", rol.Nombre);
						mapMsg.put("NombreEtapa", etapa.Nombre);
						salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ROL_YA_ESTA_ASOCIADO_A_LA_ETAPA", mapMsg, conn, in.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					}
				}
			}
			
			//VALIDA USUARIO REEMPLAZO
			if(autorizadoEtapaNuevo.IdUsuarioReemplaza != 0) {
				Usuario usuarioReemplaza = (Usuario) hashUsuarios.get(autorizadoEtapaNuevo.IdUsuarioReemplaza);
				if(usuarioReemplaza == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_REEMPLAZA_NO_EXISTE", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(autorizadoEtapaNuevo.IdUsuarioReemplaza.equals(autorizadoEtapaNuevo.IdUsuario)) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_REEMPLAZA_IGUAL_A_USUARIO_AUTORIZA", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(autorizadoEtapaNuevo.IdUsuarioReemplaza.equals(autorizadoEtapaNuevo.IdUsuario)) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_REEMPLAZA_IGUAL_A_USUARIO_AUTORIZA", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if (autorizadoEtapaNuevo.ReemplazaDesde != null && autorizadoEtapaNuevo.ReemplazaHasta != null) {
					Date fechaMinima = Date.valueOf("0001-01-01");
					
				    if (!fechaMinima.equals(autorizadoEtapaNuevo.ReemplazaDesde) && !fechaMinima.equals(autorizadoEtapaNuevo.ReemplazaHasta)) {

				        if (autorizadoEtapaNuevo.ReemplazaDesde.compareTo(autorizadoEtapaNuevo.ReemplazaHasta) > 0) {
				            salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "FECHA_DESDE_DEBE_SER_MENOR_O_IGUAL_A_FECHA_HASTA", mapMsg, conn, in.p_IdEmpresa));
				            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				        }
				    }
				}
			}

			if(salida.P_OUT_ERRORESCONTADOR == 0) {
				autorizadoEtapaNuevo.IdAutorizadoEtapa = secuencias.get(index.getAndIncrement());
				listAutorizadosEtapaCorrectos.add(autorizadoEtapaNuevo);
			}
		}
		
		if(!listAutorizadosEtapaCorrectos.isEmpty()) {
			logger.debug("INICIO::SQL_INSERT_AUTORIZADO_ETAPA ");
			sql.batch(conn, SQL_INSERT_AUTORIZADO_ETAPA, listAutorizadosEtapaCorrectos, AutorizadoEtapa.class);
			logger.debug("FIN::SQL_INSERT_AUTORIZADO_ETAPA");
			String mensaje = listAutorizadosEtapaCorrectos.size() > 1 ? "AUTORIZADORES_CREADOS" : "AUTORIZADOR_CREADO";
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, mensaje, mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin= System.currentTimeMillis();
		logger.info("FIN::CrearAutorizadoEtapa", fin - inicio);

		return salida;
	}
	
	private <T> List<Long> obtenerIdsUnicos(List<AutorizadoEtapa> autorizados, Function<AutorizadoEtapa, Stream<Long>> mapper) {
        return autorizados.stream()
                .flatMap(mapper)
                .distinct()
                .collect(Collectors.toList());
    }
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearAutorizadoEtapa in = gson.fromJson(jin.toString(), EntradaCrearAutorizadoEtapa.class);
		SalidaCrearAutorizadoEtapa out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"PLA\",\"p_AutorizadoEtapa\":["
					+ "{\"IdEtapa\":4830,\"IdUsuario\":97,\"IdRol\":0,\"IdUsuarioReemplaza\":221,\"EsAutorizacionPorRol\":\"N\",\"IdEstado\":\"VIG\",\"ReemplazaDesde\":\"2025-04-30\",\"ReemplazaHasta\":\"2025-04-15\"},"
					+ "{\"IdEtapa\":4830,\"IdUsuario\":98,\"IdRol\":0,\"IdUsuarioReemplaza\":221,\"EsAutorizacionPorRol\":\"N\",\"IdEstado\":\"VIG\",\"ReemplazaDesde\":\"2025-04-30\",\"ReemplazaHasta\":\"2025-04-15\"}]}";

			EntradaCrearAutorizadoEtapa objIn = gson.fromJson(json, EntradaCrearAutorizadoEtapa.class);
			CrearAutorizadoEtapa demo = new CrearAutorizadoEtapa();
			SalidaCrearAutorizadoEtapa ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}