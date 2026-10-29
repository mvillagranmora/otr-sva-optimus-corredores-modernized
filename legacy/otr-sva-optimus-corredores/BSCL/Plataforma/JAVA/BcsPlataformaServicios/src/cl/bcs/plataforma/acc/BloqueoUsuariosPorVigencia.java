package cl.bcs.plataforma.acc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BloqueoUsuariosPorVigencia implements JavaCall {
	private static final Logger logger = LogManager.getLogger(BloqueoUsuariosPorVigencia.class);
	
	private final String SQL_SELECT_USUARIO_A_BLOQUEAR = loadResourceToString("cl/bcs/plataforma/acc/sql/BloqueoUsuarioPorVigencia_Select_Usuario.sql");
	private final String SQL_UPDATE_BLOQUEO_USUARIO_POR_FECHA_VIGENCIA = loadResourceToString("cl/bcs/plataforma/acc/sql/BloqueoUsuariosPorVigencia_Update_Usuarios.sql");
    private final String SQL_SELECT_PARAM_TIPO_BLOQUEO = loadResourceToString("cl/bcs/plataforma/acc/sql/BloqueoUsuarioPorVigencia_Select_Param_Tipo_Bloqueo.sql");

    public static class BloqueoUsuariosPorVigenciaEntrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	}
    
	public static class ParametrosBloqueoUsuariosPorVigencia extends BcsPojo {
		public String IdEstado;
		public String IdTipoBloqueo;
		public String NombreTipoBloqueo;
		public String ComentarioBloqueo;
		public String FechaSistema;
	}
    
    public static class TipoBloqueo extends BcsPojo {
    	public String IdAccion;
    	public String NombreTipoBloqueo;
		public String IdTipoBloqueo;
        public String ComentarioPorDefecto;
	}
    
	public static class Usuario extends BcsPojo  {
		public Long IdUsuario;
		public Long IdEmpresa;
		public String CodigoUsuario;
		public String Clave;
		public Date FechaIngreso;
		public Date FechaVigencia;
		public Date FechaUltimaConexion;
		public BigDecimal DiasVencimientoClave;
		public String Conectado;
		public Time Hora;
		public String Terminal;
		public String IdEstado;
		public Integer IntentosConexion;
		public Timestamp FechaBloqueoTemporal;
		public String TipoEntidad;
		public Long IdEntidad;
		public String EsAutorizado;
		public String UsuarioCorredores;
		public String UsuarioNegocios;
		public String UsuarioClientes;
		public String UsuarioServicios;
		public String DebeCambiarClave;
		public String IdTipoBloqueo;
		public String ComentarioBloqueo;
		public String NombreTipoBloqueo;
	}

	public static class BloqueoUsuariosPorVigenciaSalida extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public BloqueoUsuariosPorVigenciaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	public BloqueoUsuariosPorVigenciaSalida logica(Connection conn, BloqueoUsuariosPorVigenciaEntrada entrada) throws Exception {
		logger.info("INICIO::BloqueoUsuariosPorVigencia");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.debug("Fecha del sistema: " + fechaSistema);

		BloqueoUsuariosPorVigenciaSalida salida = new BloqueoUsuariosPorVigenciaSalida();
		SQL sql = new SQL();

		try {
			HashMap<String, Object> parametrosUsuario = new HashMap<>();
			parametrosUsuario.put("FechaSistema", fechaSistema);
			
			logger.debug("INICIO::SQL_SELECT_USUARIO_A_BLOQUEAR");
			List<Usuario> usuarios = sql.query(conn, SQL_SELECT_USUARIO_A_BLOQUEAR, parametrosUsuario, Usuario.class);
			logger.debug("FIN::SQL_SELECT_USUARIO_A_BLOQUEAR");
			
			if(usuarios.isEmpty()) {
	            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "NO_HAY_USUARIO_POR_ACTUALIZAR", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
	            return salida;
			}
			
            HashMap<String, Object> parametrosTipoBloqueo = new HashMap<>();
            parametrosTipoBloqueo.put("IdTipoBloqueo", "BVIG");

            logger.debug("INICIO::SQL_SELECT_PARAM_TIPO_BLOQUEO");
		    List<TipoBloqueo> consultarTipoBloqueo = sql.query(conn, SQL_SELECT_PARAM_TIPO_BLOQUEO, parametrosTipoBloqueo, TipoBloqueo.class);
		    logger.debug("FIN::SQL_SELECT_PARAM_TIPO_BLOQUEO");

			ParametrosBloqueoUsuariosPorVigencia parametrosBloqueoUsuarioPorVigencia = new ParametrosBloqueoUsuariosPorVigencia();
			parametrosBloqueoUsuarioPorVigencia.IdEstado = consultarTipoBloqueo.get(0).IdAccion;
			parametrosBloqueoUsuarioPorVigencia.IdTipoBloqueo = consultarTipoBloqueo.get(0).IdTipoBloqueo;
			parametrosBloqueoUsuarioPorVigencia.NombreTipoBloqueo = consultarTipoBloqueo.get(0).NombreTipoBloqueo;
			parametrosBloqueoUsuarioPorVigencia.ComentarioBloqueo = consultarTipoBloqueo.get(0).ComentarioPorDefecto;
			parametrosBloqueoUsuarioPorVigencia.FechaSistema = fechaSistema;

			List<ParametrosBloqueoUsuariosPorVigencia> listaTipo = new ArrayList<ParametrosBloqueoUsuariosPorVigencia>();
			listaTipo.add(parametrosBloqueoUsuarioPorVigencia);

			logger.debug("INICIO::SQL_UPDATE_BLOQUEO_USUARIO_POR_FECHA_VIGENCIA");
			sql.batch(conn, SQL_UPDATE_BLOQUEO_USUARIO_POR_FECHA_VIGENCIA, listaTipo, ParametrosBloqueoUsuariosPorVigencia.class);
			logger.debug("FIN::SQL_UPDATE_BLOQUEO_USUARIO_POR_FECHA_VIGENCIA");
			logger.info("Usuarios bloqueados por vigencia: {}", usuarios.size());

            AgregarLog(conn, fechaSistema, usuarios, parametrosBloqueoUsuarioPorVigencia, entrada.p_IdUsuario);				
            
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "BLOQUEO_USUARIOS_POR_VIGENCIA_EXITOSO", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
		} catch (Exception e) {
			e.printStackTrace();
            salida.P_OUT_ERRORESCONTADOR++;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "BLOQUEO_USUARIOS_POR_VIGENCIA_ERROR", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
        }

        long fin = System.currentTimeMillis();
		logger.info("FIN::BloqueoUsuariosPorVigencia", fin - inicio);
		return salida;
	}
	
	public void AgregarLog(Connection conn, String p_FechaSistema, List<Usuario> usuarios, ParametrosBloqueoUsuariosPorVigencia in, Long p_IdUsuario) {
		logger.debug("INICIO::BloqueoUsuariosPorVigencia.agregarLogBloqueoUsuarios");
		long inicio = System.currentTimeMillis();

		try {
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (Usuario usuario : usuarios) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = usuario.IdUsuario;
				log.IdTipoLog = "ACTUALIZACION";
				log.IdModulo = "PLA";
				log.IdUsuario = p_IdUsuario;
				log.IdEmpresa = usuario.IdEmpresa;
				log.FechaSistemaLog = p_FechaSistema;
				log.IdUsuarioAtendido = usuario.IdUsuario;
				log.IdCliente = 0L;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "BLOQUEO_USUARIO_POR_VIGENCIA";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.IdPersona = 0L;
				log.NombreProceso = "Bloqueo de usuarios por fecha de vigencia";
				entradaAgregarLog.p_Log.add(log);

				AgregarLog.InAgregarLog.DetalleLog detLogEstado = new AgregarLog.InAgregarLog.DetalleLog();
				detLogEstado.IdTrabajo = 0L;
				detLogEstado.IdCabeceraLog = usuario.IdUsuario;
				detLogEstado.SubRegistro = usuario.CodigoUsuario.toUpperCase();
				detLogEstado.CampoRegistro = "Estado";
				detLogEstado.ValorAntiguo = usuario.IdEstado;
				detLogEstado.ValorNuevo = in.IdEstado;
				entradaAgregarLog.p_LogDetalle.add(detLogEstado);

				AgregarLog.InAgregarLog.DetalleLog detLogTipoBloqueo = new AgregarLog.InAgregarLog.DetalleLog();
				detLogTipoBloqueo.IdTrabajo = 0L;
				detLogTipoBloqueo.IdCabeceraLog = usuario.IdUsuario;
				detLogTipoBloqueo.SubRegistro = usuario.CodigoUsuario.toUpperCase();
				detLogTipoBloqueo.CampoRegistro = "TipoBloqueo";
				detLogTipoBloqueo.ValorAntiguo = !usuario.IdTipoBloqueo.isEmpty() ? "(" + usuario.IdTipoBloqueo + ") " + usuario.NombreTipoBloqueo : "";
				detLogTipoBloqueo.ValorNuevo = !in.IdTipoBloqueo.isEmpty() ? "(" + in.IdTipoBloqueo + ") " + in.NombreTipoBloqueo : "";
				entradaAgregarLog.p_LogDetalle.add(detLogTipoBloqueo);

				AgregarLog.InAgregarLog.DetalleLog detLogComentarioBloqueo = new AgregarLog.InAgregarLog.DetalleLog();
				detLogComentarioBloqueo.IdTrabajo = 0L;
				detLogComentarioBloqueo.IdCabeceraLog = usuario.IdUsuario;
				detLogComentarioBloqueo.SubRegistro = usuario.CodigoUsuario.toUpperCase();
				detLogComentarioBloqueo.CampoRegistro = "ComentarioBloqueo";
				detLogComentarioBloqueo.ValorAntiguo = usuario.ComentarioBloqueo != null ? usuario.ComentarioBloqueo : "";
				detLogComentarioBloqueo.ValorNuevo = in.ComentarioBloqueo;
				entradaAgregarLog.p_LogDetalle.add(detLogComentarioBloqueo);
			}
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

			long fin = System.currentTimeMillis();
			logger.debug("FIN::BloqueoUsuariosPorVigencia.agregarLogBloqueoUsuarios tiempo={}ms", (fin - inicio));

		} catch (Exception e) {
			logger.error("ERROR::BloqueoUsuariosPorVigencia.agregarLogBloqueoUsuarios", e);
			throw new RuntimeException("Error al registrar logs de bloqueo de usuarios", e);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		BloqueoUsuariosPorVigenciaEntrada in = gson.fromJson(jin.toString(), BloqueoUsuariosPorVigenciaEntrada.class);
		BloqueoUsuariosPorVigenciaSalida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public static void main(String[] args) throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
	        System.out.println("== BEGIN ==");
	        BloqueoUsuariosPorVigencia demo = new BloqueoUsuariosPorVigencia();
	        BloqueoUsuariosPorVigencia.BloqueoUsuariosPorVigenciaEntrada entrada = new BloqueoUsuariosPorVigencia.BloqueoUsuariosPorVigenciaEntrada();
	        BloqueoUsuariosPorVigencia.BloqueoUsuariosPorVigenciaSalida salida = demo.logica(conn, entrada);
			
	        System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);
	        for (PlataformaUtils.MensajeSalida msg : salida.P_OUT_MENSAJES) {
	            System.out.println(msg.descripcion);
	        }
	        conn.rollback();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    System.out.println("== END ==");
	}
}