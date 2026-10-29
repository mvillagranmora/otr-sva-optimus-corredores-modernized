package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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

public class ModificarAutorizadoEtapa implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarAutorizadoEtapa.class);

	private final String SQL_SELECT_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectEtapa.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectUsuario.sql");
	private final String SQL_SELECT_AUTORIZADO_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectAutorizadoEtapa.sql");
	private final String SQL_SELECT_ROL = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRol.sql");
	private final String SQL_SELECT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_SelectRolUsuario.sql");
	private final String SQL_UPDATE_AUTORIZADO_ETAPA = loadResourceToString("cl/bcs/plataforma/aut/sql/AutorizadoEtapa_UpdateAutorizadoEtapa.sql");
	
	private final String ID_ESTADO_BLOQUEO = "BLO";
	private final String NOMBRE_ESTADO_INACTIVO = "INACTIVO";
	
	public static class EntradaModificarAutorizadoEtapa extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<AutorizadoEtapa> p_AutorizadoEtapa;
		
		public EntradaModificarAutorizadoEtapa() {
			this.p_AutorizadoEtapa = new ArrayList<>();	
		}
	}

	public static class SalidaModificarAutorizadoEtapa extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaModificarAutorizadoEtapa() {
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

	public SalidaModificarAutorizadoEtapa logica(Connection conn, EntradaModificarAutorizadoEtapa in) throws Exception {
		logger.info("INICIO::ModificarAutorizadoEtapa");
		long inicio = System.currentTimeMillis();
		
		SalidaModificarAutorizadoEtapa salida = new SalidaModificarAutorizadoEtapa();	
		SQL sql = new SQL();
		AtomicLong contador = new AtomicLong(0L);
		HashMap<String, String> mapMsg = new HashMap<>(2);
		List<AutorizadoEtapa> listAutorizadosEtapaModificados = new ArrayList<>();
				
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
		
		for(AutorizadoEtapa autorizadoEtapaModificado : in.p_AutorizadoEtapa) {
			
			if(("S").equals(autorizadoEtapaModificado.SoloBloquear)) {
				autorizadoEtapaModificado.IdEstado = ID_ESTADO_BLOQUEO;
				listAutorizadosEtapaModificados.add(autorizadoEtapaModificado);
				continue;
			}
			
			long idInterno = contador.getAndIncrement();
			//VALIDAR ETAPA
			Etapa etapa = (Etapa) hashEtapas.get(autorizadoEtapaModificado.IdEtapa);
			if(etapa == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_ETAPA_TIPO_EVENTO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			if(NOMBRE_ESTADO_INACTIVO.equals(etapa.NombreEstado)){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ETAPA_TIPO_EVENTO_INACTIVA", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			//COMPARAR CON AUTORIZADOS EXISTENTES
			List<AutorizadoEtapa> listAutorizadoEtapaSistemaFiltrado = listAutorizadoEtapaSistema.stream().filter(aut -> !Objects.equals(aut.IdAutorizadoEtapa, autorizadoEtapaModificado.IdAutorizadoEtapa)).collect(Collectors.toList());
			Usuario usuarioEtapa = (Usuario) hashUsuarios.get(autorizadoEtapaModificado.IdUsuario);
			
			if("N".equals(autorizadoEtapaModificado.EsAutorizacionPorRol)) {				
				if(usuarioEtapa == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_ETAPA_NO_VALIDO", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(!listAutorizadoEtapaSistemaFiltrado.isEmpty()) {
					boolean existeUsuario = listAutorizadoEtapaSistemaFiltrado.stream().anyMatch(existente ->  Objects.equals(existente.IdUsuario, autorizadoEtapaModificado.IdUsuario));
	
					if(existeUsuario) {
						mapMsg.clear();
						mapMsg.put("NombreUsuario", usuarioEtapa.CodigoUsuario);
						mapMsg.put("NombreEtapa", etapa.Nombre);
						salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_YA_ESTA_ASOCIADO_A_LA_ETAPA", mapMsg, conn, in.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					}
				}
			}
			
			//COMPARA CON ROLES EXISTENTES
			if(autorizadoEtapaModificado.IdRol != 0){
				Rol rol = (Rol) hashRoles.get(autorizadoEtapaModificado.IdRol);
				
				String keyRolUsuario = autorizadoEtapaModificado.IdUsuario.toString() + autorizadoEtapaModificado.IdRol.toString();
				RolUsuario rolUsuario = (RolUsuario) hashRolesUsuarios.get(keyRolUsuario);
				
				if(rol == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_ROL", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if (rolUsuario == null && ("N").equals(autorizadoEtapaModificado.EsAutorizacionPorRol)) {
					mapMsg.clear();
					mapMsg.put("NombreUsuario", usuarioEtapa.CodigoUsuario);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ROL_NO_ASOCIADO_A_USUARIO", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(!listAutorizadoEtapaSistemaFiltrado.isEmpty()) {
					boolean comparaUsuario = ("N").equals(autorizadoEtapaModificado.EsAutorizacionPorRol);

				    boolean existeRol = listAutorizadoEtapaSistemaFiltrado.stream()
				            .anyMatch(existente -> (Objects.equals(existente.IdRol, autorizadoEtapaModificado.IdRol) && existente.IdUsuario.equals(0L)) &&
				                    (!comparaUsuario || Objects.equals(existente.IdUsuario, autorizadoEtapaModificado.IdUsuario)));
					
					if(existeRol) {
						mapMsg.clear();
						mapMsg.put("NombreRol", rol.Nombre);
						mapMsg.put("NombreEtapa", etapa.Nombre);
						salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "ROL_YA_ESTA_ASOCIADO_A_LA_ETAPA", mapMsg, conn, in.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					}
				}
			}
			
			//VALIDA USUARIO REEMPLAZO
			if(autorizadoEtapaModificado.IdUsuarioReemplaza != 0) {
				Usuario usuarioReemplaza = (Usuario) hashUsuarios.get(autorizadoEtapaModificado.IdUsuarioReemplaza);
				if(usuarioReemplaza == null) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_REEMPLAZA_NO_EXISTE", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if(autorizadoEtapaModificado.IdUsuarioReemplaza.equals(autorizadoEtapaModificado.IdUsuario)) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "USUARIO_REEMPLAZA_IGUAL_A_USUARIO_AUTORIZA", mapMsg, conn, in.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				}
				
				if (autorizadoEtapaModificado.ReemplazaDesde != null && autorizadoEtapaModificado.ReemplazaHasta != null) {
					Date fechaMinima = Date.valueOf("0001-01-01");
					
				    if (!fechaMinima.equals(autorizadoEtapaModificado.ReemplazaDesde) && !fechaMinima.equals(autorizadoEtapaModificado.ReemplazaHasta)) {

				        if (autorizadoEtapaModificado.ReemplazaDesde.compareTo(autorizadoEtapaModificado.ReemplazaHasta) > 0) {
				            salida.P_OUT_MENSAJES.add(new MensajeSalida(idInterno, MensajeSalida.MENSAJE_ERROR, "FECHA_DESDE_DEBE_SER_MENOR_O_IGUAL_A_FECHA_HASTA", mapMsg, conn, in.p_IdEmpresa));
				            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				        }
				    }
				}
			}
			
			if(salida.P_OUT_ERRORESCONTADOR == 0) {
				listAutorizadosEtapaModificados.add(autorizadoEtapaModificado);
			}
		}
		
		if(!listAutorizadosEtapaModificados.isEmpty()) {
			logger.debug("INICIO::SQL_UPDATE_AUTORIZADO_ETAPA ");
			sql.batch(conn, SQL_UPDATE_AUTORIZADO_ETAPA, listAutorizadosEtapaModificados, AutorizadoEtapa.class);
			logger.debug("FIN::SQL_UPDATE_AUTORIZADO_ETAPA");
			String mensaje = listAutorizadosEtapaModificados.size() > 1 ? "AUTORIZADORES_MODIFICADOS" : "AUTORIZADOR_MODIFICADO";
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, mensaje, mapMsg, conn, in.p_IdEmpresa));
		}
		
		long fin= System.currentTimeMillis();
		logger.info("FIN::ModificarAutorizadoEtapa", fin - inicio);
		
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
		EntradaModificarAutorizadoEtapa in = gson.fromJson(jin.toString(), EntradaModificarAutorizadoEtapa.class);
		SalidaModificarAutorizadoEtapa out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("qa2", "connections.properties")){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"PLA\",\"p_AutorizadoEtapa\":[{\"IdAutorizadoEtapa\":248476,\"IdEtapa\":247344,\"IdUsuario\":297,\"IdRol\":0,\"IdUsuarioReemplaza\":0,\"EsAutorizacionPorRol\":\"N\",\"IdEstado\":\"VIG\",\"ReemplazaDesde\":\"0001-01-01\",\"ReemplazaHasta\":\"0001-01-01\"}]}";

			EntradaModificarAutorizadoEtapa objIn = gson.fromJson(json, EntradaModificarAutorizadoEtapa.class);
			ModificarAutorizadoEtapa demo = new ModificarAutorizadoEtapa();
			SalidaModificarAutorizadoEtapa ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}