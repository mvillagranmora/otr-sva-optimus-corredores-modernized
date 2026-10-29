package cl.bcs.plataforma.acc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.acc.CrearUsuario.CorreosExtra;
import cl.bcs.plataforma.acc.CrearUsuario.CorreosSEG;
import cl.bcs.plataforma.acc.CrearUsuario.PrimeraPersonaValida;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarUsuario implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ActualizarUsuario.class);

	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Usuario.sql");
	private final String SQL_SELECT_LOG_USUARIO_CLAVE = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Log_Usuario_Clave.sql");
	private final String SQL_UPDATE_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Usuario.sql");
	private final String SQL_UPDATE_INDICADOR = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Indicador.sql");
	private final String SQL_INSERT_HISTORIAL_CLAVE = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Insert_Historial_Clave.sql");
	private final String SQL_SELECT_COUNT_HEREDAR_ROLES = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Heredar_Roles.sql");
	private final String SQL_DELETE_ROLES_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Delete_Roles_Usuario.sql");
	private final String SQL_INSERT_ROLES_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Insert_Roles_Usuario.sql");
	private final String SQL_UPDATE_MENU_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Menu_Usuario.sql");
	
	private final String SQL_SELECT_USUARIOS_SEG = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_UsuariosSEG.sql");
	private final String SQL_SELECT_USUARIOS_EXTRA = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_UsuariosExtra.sql");
	private final String SQL_SELECT_IDPER_VALIDO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_IdPerValido.sql");
	private final String SQL_SELECT_PARAM_ENVIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Param_Envio.sql");
	
	public static class EntradaActualizarUsuario {		
		public Long p_IdEmpresa; 
		public Long p_IdUsuario; 
		public String p_IdModulo; 
		public String p_UsaReglasNegocio;
		public String p_CodigoUsuario;
		public String p_Clave;
		public String p_FechaVigencia;
		public String p_IdEstado;
		public Long p_HeredarRolesUsuario;
		public String p_IdTipoBloqueo;
		public String p_ComentarioBloqueo;
		public String p_NombreTipoBloqueo;
		
		public String p_UsuarioCorredores;
		public String p_UsuarioNegocios;
		public String p_UsuarioClientes;
		public String p_UsuarioServicios;
		
		public EntradaActualizarUsuario() {
			this.p_UsuarioCorredores = "N";
			this.p_UsuarioNegocios = "N";
			this.p_UsuarioClientes = "N";
			this.p_UsuarioServicios = "N";
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaActualizarUsuario {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_NEW_USER_ID;

		public SalidaActualizarUsuario() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_NEW_USER_ID = 0L;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contador{
		public Long Count;
	}
	
	public static class SocioNegocio{
		public Long IdSocioDeNegocio;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdEmpleado;		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Usuario {
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
		//public String UltimoCambioClave;
		public String DebeCambiarClave;
		public String IdTipoBloqueo;
		public String ComentarioBloqueo;
		public String NombreTipoBloqueo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class LogUsuarioClave{
		public String Clave;
		public Timestamp UltimoCambioDeClave;
	}
	
	public static class MensajeDiccionario{
		public String Mensaje;
	}
	
	public static class HeredarUsuario{
		public Long IdUsuario;
		public Long IdUsuarioHeredar;
	}
	
	public static class ContHeredaRolesUsuario{
		public Long Contador;
	}
	
	public static class HeredarRolesUsu{
		public Long IdUsuario;
		public Long HeredarRolesUsuario;
	}
	
	public static class Log{
		public String SubRegistro;
		public String CampoRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;				
	}
	
	public static class IdUsuario{
		public Long IdUsuario;
	}
	
	public static class HistorialClave{
		public Long IdUsuario;
		public String ClaveAnt;
		public Date UltimoCambioClave;			
	}
	
	public static class ActualizaIndicador{
		public String UsuarioClientes;
		public Long IdUsuario;
	}
	
	public static class User {
		public Long IdUsuario;
		public String Clave;
		public Date FechaVigencia;
		public String IdEstado;
		public String UsuarioCorredores;
		public String UsuarioNegocios;
		public String UsuarioClientes;
		public String UsuarioServicios;
		public Date UltimoCambioClave;
		public String DebeCambiarClave;
		public String IdTipoBloqueo;
		public String ComentarioBloqueo;
		public String NombreTipoBloqueo;
	};
	
	public boolean ContieneSoloNumeros(String texto) {
	    char[] chars = texto.toCharArray();

	    for (char c : chars) {
	        if(Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}
	
	public static class ParamEnvioCorreo{
		public String Valor;
	}

	public SalidaActualizarUsuario logica(Connection conn, EntradaActualizarUsuario in) throws Exception {
			
		Boolean cambioEnEstadoBloqueo = false;
		Boolean tipoUsuarioHabilitaEnvio = false;
		String claveAnt;
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		Date sysDate = new Date();
		String fechaFormateada = Fecha.toFormat(new Timestamp(sysDate.getTime()),"yyyy-MM-dd HH:mm:ss");
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 159L;
		log.IdTipoLog = "ACTUALIZACION";
		log.IdModulo = "PLA";
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdCliente = 0L;
		log.EstadoCliente = "";
		log.NombreCliente = "";
		log.IdTipoDocumentoIdentidad = "";
		log.IdDocumentoIdentidad = "";
		log.IdPortafolio = 0L;
		log.EstadoPortafolio = "";
		log.IdTipoRegistro = "USUARIO";
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
		log.NombreProceso = "";						
		entradaAgregarLog.p_Log.add(log);						
		
		//traer datos de usuario a actualizar
		logger.info("INIT");
		SalidaActualizarUsuario salida = new SalidaActualizarUsuario();		
		SQL sql = new SQL();
		AtomicLong contador = new AtomicLong(0L);
		HashMap<String, String> mapMsg = new HashMap<>();				
		HashMap<String, Object> params = new HashMap<>();
		params.put("CodigoUsuario", in.p_CodigoUsuario.toUpperCase());
		params.put("IdEmpresa", in.p_IdEmpresa);
		logger.debug("Evaluar si existe el socio de negocio");
		logger.debug("INICIO::SQL_SELECT_USUARIO");
		List<Usuario> usuario = sql.query(conn, SQL_SELECT_USUARIO, params, Usuario.class);
		logger.debug("FIN::SQL_SELECT_USUARIO");
		
		if(usuario.isEmpty()) {			
			salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_USUARIO", mapMsg, conn, in.p_IdEmpresa));
		}else{
			claveAnt = usuario.get(0).Clave;
			
			if(!in.p_IdEstado.equals(usuario.get(0).IdEstado)){
				cambioEnEstadoBloqueo = true;
			}
			
			log.IdUsuarioAtendido = usuario.get(0).IdUsuario != null ? usuario.get(0).IdUsuario : 0L;
			
			//detallelog cambio de estado
			if(!in.p_IdEstado.equals(usuario.get(0).IdEstado)){
				AgregarLog.InAgregarLog.DetalleLog detLogE = new AgregarLog.InAgregarLog.DetalleLog();
				detLogE.IdCabeceraLog = 159L;
				detLogE.IdTrabajo= 0L;
				detLogE.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogE.CampoRegistro = "CambioEstado";
				detLogE.ValorAntiguo = usuario.get(0).IdEstado != null? usuario.get(0).IdEstado : "";
				detLogE.ValorNuevo = in.p_IdEstado;
				entradaAgregarLog.p_LogDetalle.add(detLogE);
			}
			
			if(!in.p_IdTipoBloqueo.equals(usuario.get(0).IdTipoBloqueo)){
				AgregarLog.InAgregarLog.DetalleLog detLogTB = new AgregarLog.InAgregarLog.DetalleLog();
				detLogTB.IdCabeceraLog = 159L;
				detLogTB.IdTrabajo= 0L;
				detLogTB.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogTB.CampoRegistro = "TipoBloqueo";
				detLogTB.ValorAntiguo = !usuario.get(0).IdTipoBloqueo.isEmpty() ? "(" + usuario.get(0).IdTipoBloqueo + ") " + usuario.get(0).NombreTipoBloqueo : "";
				detLogTB.ValorNuevo = !in.p_IdTipoBloqueo.isEmpty() ? "(" + in.p_IdTipoBloqueo + ") " + in.p_NombreTipoBloqueo : "";
				entradaAgregarLog.p_LogDetalle.add(detLogTB);
			}
			
			if(!in.p_ComentarioBloqueo.equals(usuario.get(0).ComentarioBloqueo)){
				AgregarLog.InAgregarLog.DetalleLog detLogCB = new AgregarLog.InAgregarLog.DetalleLog();
				detLogCB.IdCabeceraLog = 159L;
				detLogCB.IdTrabajo= 0L;
				detLogCB.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogCB.CampoRegistro = "ComentarioBloqueo";
				detLogCB.ValorAntiguo = usuario.get(0).ComentarioBloqueo != null? usuario.get(0).ComentarioBloqueo : "";
				detLogCB.ValorNuevo = in.p_ComentarioBloqueo;
				entradaAgregarLog.p_LogDetalle.add(detLogCB);
			}
		
			//detallelogs si hubo cambios de acceso usuario clientes/corredores/negocios/servicios
			if(!in.p_UsuarioClientes.equals(usuario.get(0).UsuarioClientes)){
				AgregarLog.InAgregarLog.DetalleLog detLogC = new AgregarLog.InAgregarLog.DetalleLog();
				detLogC.IdCabeceraLog = 159L;
				detLogC.IdTrabajo= 0L;
				detLogC.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogC.CampoRegistro = "Usuario Clientes";
				detLogC.ValorAntiguo = usuario.get(0).UsuarioClientes != null? usuario.get(0).UsuarioClientes : "";
				detLogC.ValorNuevo = in.p_UsuarioClientes;
				entradaAgregarLog.p_LogDetalle.add(detLogC);
			}
			
			if(!in.p_UsuarioCorredores.equals(usuario.get(0).UsuarioCorredores)){
				AgregarLog.InAgregarLog.DetalleLog detLogCo = new AgregarLog.InAgregarLog.DetalleLog();
				detLogCo.IdCabeceraLog = 159L;
				detLogCo.IdTrabajo= 0L;
				detLogCo.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogCo.CampoRegistro = "Usuario Corredores";
				detLogCo.ValorAntiguo = usuario.get(0).UsuarioCorredores != null? usuario.get(0).UsuarioCorredores : "";
				detLogCo.ValorNuevo = in.p_UsuarioCorredores;
				entradaAgregarLog.p_LogDetalle.add(detLogCo);
			}
			
			if(!in.p_UsuarioNegocios.equals(usuario.get(0).UsuarioNegocios)){
				AgregarLog.InAgregarLog.DetalleLog detLogN = new AgregarLog.InAgregarLog.DetalleLog();
				detLogN.IdCabeceraLog = 159L;
				detLogN.IdTrabajo= 0L;
				detLogN.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogN.CampoRegistro = "Usuario Negocios";
				detLogN.ValorAntiguo = usuario.get(0).UsuarioNegocios != null? usuario.get(0).UsuarioNegocios : "";
				detLogN.ValorNuevo = in.p_UsuarioNegocios;
				entradaAgregarLog.p_LogDetalle.add(detLogN);
			}
			
			if(!in.p_UsuarioServicios.equals(usuario.get(0).UsuarioServicios)){
				AgregarLog.InAgregarLog.DetalleLog detLogS = new AgregarLog.InAgregarLog.DetalleLog();
				detLogS.IdCabeceraLog = 159L;
				detLogS.IdTrabajo= 0L;
				detLogS.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
				detLogS.CampoRegistro = "Usuario Servicios";
				detLogS.ValorAntiguo = usuario.get(0).UsuarioServicios != null? usuario.get(0).UsuarioServicios : "";
				detLogS.ValorNuevo = in.p_UsuarioServicios;
				entradaAgregarLog.p_LogDetalle.add(detLogS);
			}
								
			//dejar registro del nombre de usuario en el log
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 159L;
			detLog.IdTrabajo= 0L;
			detLog.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
			detLog.CampoRegistro = "FechaVigencia";
			detLog.ValorAntiguo = usuario.get(0).FechaVigencia.toString();
			detLog.ValorNuevo = in.p_FechaVigencia.toString();
			entradaAgregarLog.p_LogDetalle.add(detLog);
			
			if(!in.p_Clave.equals("")){
				in.p_Clave = Hashing.sha256().newHasher().putString(in.p_Clave, Charsets.UTF_8).hash().toString().toUpperCase();
				
				logger.debug("INICIO::SQL_SELECT_LOG_USUARIO_CLAVE");
				HashMap<String, Object> param = new HashMap<>();
				param.put("IdUsuario", usuario.get(0).IdUsuario);
				param.put("Clave", in.p_Clave);
				List<LogUsuarioClave> logUsuarioClave = sql.query(conn, SQL_SELECT_LOG_USUARIO_CLAVE, param, LogUsuarioClave.class);
				logger.debug("FIN::SQL_SELECT_LOG_USUARIO_CLAVE");
				
				if(in.p_Clave.equals(usuario.get(0).Clave) || logUsuarioClave.size() > 0){
					//claves iguales, error
					mapMsg.clear();
					mapMsg.put("Codigo", "Clave");
					mapMsg.put("Valor", "*****");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_CLAVES_IGUALES", mapMsg, conn, in.p_IdEmpresa));
				}else{
					//actualizar claves
					usuario.get(0).Clave = in.p_Clave;
					usuario.get(0).DebeCambiarClave = "S";
					
					//revisar si necesito agregar otra cabecera de log
					AgregarLog.InAgregarLog.DetalleLog detLog2 = new AgregarLog.InAgregarLog.DetalleLog();
					detLog2.IdCabeceraLog = 159L;
					detLog2.IdTrabajo= 0L;
					detLog2.SubRegistro = usuario.get(0).CodigoUsuario.toUpperCase();
					detLog2.CampoRegistro = "Clave";
					detLog2.ValorAntiguo = "*****";
					detLog2.ValorNuevo = "*****";
					entradaAgregarLog.p_LogDetalle.add(detLog2);
				}
			}else{
				if(usuario.get(0).EsAutorizado.equals("S")){
					usuario.get(0).DebeCambiarClave = "N";
				}
				
			}
			
			//actualizar usuario
			String updateUsuario = SQL_UPDATE_USUARIO;
			updateUsuario = updateUsuario.replace(":UltimoCambioClave", "'"+ fechaFormateada + "'");
			User user = new User();
			user.FechaVigencia =  Fecha.toDate(in.p_FechaVigencia);
			user.IdEstado = in.p_IdEstado;
			user.Clave = usuario.get(0).Clave;
			user.DebeCambiarClave = usuario.get(0).DebeCambiarClave;
			user.UsuarioCorredores = in.p_UsuarioCorredores;
			user.UsuarioNegocios = in.p_UsuarioNegocios;
			user.UsuarioClientes = in.p_UsuarioClientes;
			user.UsuarioServicios = in.p_UsuarioServicios;
			user.IdUsuario = usuario.get(0).IdUsuario;
			user.IdTipoBloqueo = in.p_IdTipoBloqueo;
			user.ComentarioBloqueo = in.p_ComentarioBloqueo;
					
			logger.debug("INICIO::SQL_UPDATE_USUARIO");
			sql.batch(conn, updateUsuario, Arrays.asList(user), User.class);
			logger.debug("FIN::SQL_UPDATE_USUARIO");
						
			//Se cambia el indicador que permite el acceso a optimus cliente
			ActualizaIndicador actInd = new ActualizaIndicador();
			actInd.UsuarioClientes = in.p_UsuarioClientes;
			actInd.IdUsuario = usuario.get(0).IdUsuario;
			logger.debug("INICIO::SQL_UPDATE_INDICADOR");
			sql.batch(conn, SQL_UPDATE_INDICADOR, Arrays.asList(actInd), ActualizaIndicador.class);
			logger.debug("FIN::SQL_UPDATE_INDICADOR");
			
			//guardar historial de claves
			String updateHist = SQL_INSERT_HISTORIAL_CLAVE;
			updateHist = updateHist.replace(":UltimoCambioClave", "'"+ fechaFormateada + "'");
			HistorialClave hist = new HistorialClave();
			hist.IdUsuario = usuario.get(0).IdUsuario;
			hist.ClaveAnt = claveAnt;
			logger.debug("INICIO::SQL_INSERT_HISTORIAL_CLAVE");
			sql.batch(conn, updateHist, Arrays.asList(hist), HistorialClave.class);
			logger.debug("FIN::SQL_INSERT_HISTORIAL_CLAVE");
			
			//Validar que se quiera heredarRoles para el usuario
			HashMap<String, Object> params5 = new HashMap<>();
			params5.put("HeredarRolesUsuario", in.p_HeredarRolesUsuario);
			params5.put("IdEmpresa", in.p_IdEmpresa);
			logger.debug("INICIO::SQL_SELECT_COUNT_HEREDAR_ROLES");
			List<ContHeredaRolesUsuario> contHereda = sql.query(conn, SQL_SELECT_COUNT_HEREDAR_ROLES, params5,ContHeredaRolesUsuario.class);
			logger.debug("FIN::SQL_SELECT_COUNT_HEREDAR_ROLES");
			if(contHereda.get(0).Contador == 0 && in.p_HeredarRolesUsuario != -1){
				mapMsg.clear();
				mapMsg.put("Codigo", "SOCIO");
				mapMsg.put("Valor", "");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_HEREDAR_ROLES_ERROR", mapMsg, conn, in.p_IdEmpresa));
			}
						
			if(contHereda.get(0).Contador > 0 && in.p_HeredarRolesUsuario != -1){
				//eliminar roles anteriores
				IdUsuario usu = new IdUsuario();
				usu.IdUsuario = usuario.get(0).IdUsuario;
				logger.debug("Borrar roles usuario");
				logger.debug("INICIO::SQL_DELETE_ROLES_USUARIO");
				sql.batch(conn, SQL_DELETE_ROLES_USUARIO, Arrays.asList(usu), IdUsuario.class);
				logger.debug("FIN::SQL_DELETE_ROLES_USUARIO");
				
				//Ingresar nuevos roles.
				HeredarRolesUsu her = new HeredarRolesUsu();
				her.IdUsuario = usuario.get(0).IdUsuario;
				her.HeredarRolesUsuario = in.p_HeredarRolesUsuario;
				
				logger.debug("Insertar roles nuevos usuario");
				logger.debug("INICIO::SQL_INSERT_ROLES_USUARIO");
				sql.batch(conn, SQL_INSERT_ROLES_USUARIO, Arrays.asList(her), HeredarRolesUsu.class);
				logger.debug("FIN::SQL_INSERT_ROLES_USUARIO");
				
				//Heredar menu de usuario
				logger.debug("Actualizar menu usuario");
				logger.debug("INICIO::SQL_UPDATE_MENU_USUARIO");
				sql.batch(conn, SQL_UPDATE_MENU_USUARIO, Arrays.asList(her), HeredarRolesUsu.class);
				logger.debug("FIN::SQL_UPDATE_MENU_USUARIO");
			}
	        
			//Ejecutar el programa de logeo.	
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
			List<ParamEnvioCorreo> paramEnvio = sql.query(conn, SQL_SELECT_PARAM_ENVIO, new HashMap<String,Object>(), ParamEnvioCorreo.class);
									
			if(!paramEnvio.isEmpty()){
				
				if(in.p_UsuarioCorredores.equals("S") || in.p_UsuarioNegocios.equals("S")){
					tipoUsuarioHabilitaEnvio = true;
				}
				
				if(MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) == 0 && cambioEnEstadoBloqueo && paramEnvio.get(0).Valor.equals("S") && tipoUsuarioHabilitaEnvio){
					//notificacion baja de usuario
					String diaActual = Fecha.toFormat(new Timestamp(sysDate.getTime()),"yyyy-MM-dd");
					String mensaje = "";
					
					if(in.p_IdEstado.equals("ACT")){
						mensaje = "con fecha de " + diaActual + " se ha desbloqueado un usuario de codigo: " + in.p_CodigoUsuario + " y tipo(s): ";	
					}else{
						mensaje = "con fecha de " + diaActual + " se ha bloqueado un usuario de codigo: " + in.p_CodigoUsuario + " y tipo(s): ";
					}
					
					List<String> listaTipos = new ArrayList<String>();
					
					if(in.p_UsuarioCorredores.equals("S")){
						listaTipos.add("corredor");
					}
					if(in.p_UsuarioClientes.equals("S")){
						listaTipos.add("cliente");
					}
					if(in.p_UsuarioNegocios.equals("S")){
						listaTipos.add("negocio");
					}
					if(in.p_UsuarioServicios.equals("S")){
						listaTipos.add("servicio");
					}
					
					StringJoiner joiner = new StringJoiner(",");
					for(String item : listaTipos){
						joiner.add(item);
					}
							
					mensaje += joiner.toString() + ".";
					
					List<CorreosSEG> listaDestinatariosCorreos = new ArrayList<CorreosSEG>();
					listaDestinatariosCorreos = sql.query(conn, SQL_SELECT_USUARIOS_SEG, new HashMap<String,Object>(), CorreosSEG.class);
					
					List<CorreosExtra> listaCorreosExtra = new ArrayList<CorreosExtra>();
					listaCorreosExtra = sql.query(conn, SQL_SELECT_USUARIOS_EXTRA, new HashMap<String,Object>(), CorreosExtra.class);
					
					List<PrimeraPersonaValida> listaPrimerId = new ArrayList<PrimeraPersonaValida>();
					listaPrimerId = sql.query(conn, SQL_SELECT_IDPER_VALIDO, new HashMap<String,Object>(), PrimeraPersonaValida.class);
					
					try {
						if(listaCorreosExtra.toString().length() > 0 && !listaCorreosExtra.isEmpty() && listaPrimerId.toString().length() > 0 && !listaPrimerId.isEmpty()){
							String[] listaCorreosExtraSeparada = new String[1];
							listaCorreosExtraSeparada = listaCorreosExtra.get(0).IdCorreosJuntos.split(",");
							
							for(String IdCorreo : listaCorreosExtraSeparada) {
								String IdCorreoSinEspacios = IdCorreo.trim();
								if(ContieneSoloNumeros(IdCorreoSinEspacios)){
									CorreosSEG correoSEG = new CorreosSEG();
									correoSEG.IdPersona = listaPrimerId.get(0).IdPersona;
									correoSEG.IdCorreo = Long.parseLong(IdCorreoSinEspacios);
									listaDestinatariosCorreos.add(correoSEG);
								}
							}
						}
					} catch (Exception e) {
						logger.info("Error en el agregado de correos extra segun parametria de sistema.");
					}
					
					AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
					notifIn.P_IDEMPRESA 		= in.p_IdEmpresa;
			        notifIn.P_IDUSUARIO 		= in.p_IdUsuario;
			        notifIn.P_IDMODULO 			= in.p_IdModulo;
			        notifIn.P_EVENTOS 			= new ArrayList<>();
			        notifIn.P_VALORES 			= new ArrayList<>();
			        notifIn.P_ARCHIVOSADJUNTOS 	= new ArrayList<>();
			        notifIn.P_IDDIRECCIONES 	= new ArrayList<>();
		            
		            long contadorNotificaciones = 0;
		            for(CorreosSEG destinatarios : listaDestinatariosCorreos) {
		            	//validar cliente posea un correo predeterminado
		            	if(destinatarios.IdCorreo != null) {
		            		// Evento de notificacion
		        	        AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
		                    evt.CodigoTipoEvento 		= "NOTIFICAR_CREACION_USUARIO";
		                    evt.IdSecuencia 			= contadorNotificaciones;
		                    evt.FechaCreacionSistema 	= diaActual;
		                    evt.IdPersonaRemitente 		= destinatarios.IdPersona; //mantener igual que el de abajo pero valido
		                    evt.IdPersonaDestinatario 	= destinatarios.IdPersona;
		                    evt.IdPortafolio 			= (long) 0;
		                    notifIn.P_EVENTOS.add(evt);
		                    logger.info("Evento notificacion cargado");
		                    
		        	      //Objetos y llenado para salida de datos Detalle        	       
		                    
		        	        notifIn.addValor(contadorNotificaciones, "MENSAJE", mensaje);
		        	        
		        	        
		        	        //Agrega correo electronico
		        	        AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
		    				ndir.IdSecuencia 		= contadorNotificaciones;
		    	            ndir.TipoDestinatario 	= "TO";
		    	            ndir.IdDireccion 		= destinatarios.IdCorreo;
		    	            notifIn.P_IDDIRECCIONES.add(ndir);
		    	            logger.info("agrega correo electronico");
		            	}
		            	contadorNotificaciones++;
		            }
		            
		            if (notifIn.P_EVENTOS.size() > 0) {
		            	
		            	logger.info("Hay "+ notifIn.P_EVENTOS.size() +" notificaciones para enviar");
		            	logger.info("Se Llama a Servicio AgregarEvento");
		            	
		                AgregarEvento notif = new AgregarEvento();
		                AgregarEvento.ServiceSalida notifOut = notif.logica(conn, notifIn);		                

		                logger.info("Finaliza Llamada a Servicio AgregarEvento");
		            }else{
		            	logger.info("P_EVENTOS vacio");
		            }
				}
			}
		}
								
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		logger.info("END");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarUsuario in = gson.fromJson(jin.toString(), EntradaActualizarUsuario.class);
		SalidaActualizarUsuario out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	public static void main(String[] args) throws Exception, SQLException {
		try (Connection conn = PlataformaUtils.getDESAConnection()){
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			// datos entrada
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 72273,\r\n" + 
					"  \"p_IdModulo\": \"PLA\",\r\n" + 
					"  \"p_CodigoUsuario\": \"PENVIO1\",\r\n" + 
					"  \"p_Clave\": \"\",\r\n" + 
					"  \"p_HeredarRolesUsuario\": -1,\r\n" + 
					"  \"p_FechaVigencia\": \"0001-01-01\",\r\n" + 
					"  \"p_IdEstado\": \"ACT\",\r\n" + 
					"  \"p_UsuarioCorredores\": \"S\",\r\n" + 
					"  \"p_UsuarioNegocios\": \"N\",\r\n" + 
					"  \"p_UsuarioClientes\": \"N\",\r\n" + 
					"  \"p_UsuarioServicios\": \"S\"\r\n" + 
					"}";
			
			EntradaActualizarUsuario objIn = gson.fromJson(json, EntradaActualizarUsuario.class);
			// mientras pruebo hare el rollback
			ActualizarUsuario demo = new ActualizarUsuario();

			SalidaActualizarUsuario ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			//conn.commit();
			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");

	};
}
