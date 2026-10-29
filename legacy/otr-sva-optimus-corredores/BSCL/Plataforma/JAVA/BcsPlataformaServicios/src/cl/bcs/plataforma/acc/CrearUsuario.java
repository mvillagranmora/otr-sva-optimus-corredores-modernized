package cl.bcs.plataforma.acc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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

import cl.bcs.plataforma.acc.ActualizarUsuario.ParamEnvioCorreo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearUsuario implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearUsuario.class);
	private static final String ID_CONCEPTO = "ENROLU";

	private final String SQL_SELECT_SOCIONEGOCIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_SocioNegocio.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_Usuario.sql");
	private final String SQL_SELECT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_RolUsuario.sql");
	private final String SQL_SELECT_HEREDAR_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_HeredarRolUsuario.sql");
	private final String SQL_INSERT_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Insert_Usuario.sql");
	private final String SQL_CONSULTA_EXISTE_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/Consulta_Existe_Usuario.sql");
	
	private final String SQL_INSERT_ROLES_HEREDADOS = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Insert_RolesHeredados.sql");
	private final String SQL_UPDATE_MENU_HEREDADOS = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Update_MenuUsuario.sql");
	private final String SQL_SELECT_USUARIOS_SEG = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_UsuariosSEG.sql");
	private final String SQL_SELECT_USUARIOS_EXTRA = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_UsuariosExtra.sql");
	private final String SQL_SELECT_IDPER_VALIDO = loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_IdPerValido.sql");
	private final String SQL_SELECT_PARAM_ENVIO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Param_Envio.sql");
	
	public static class EntradaCrearUsuario {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_CodigoUsuario;
		public String p_Clave;
		public String p_FechaIngreso;
		public String p_FechaVigencia;
		public String p_FechaUltimaConexion;
		public String p_Conectado;
		public String p_Hora;
		public String p_Terminal;
		public String p_IdEstado;
		public Integer p_IntentosConexion;
		public String p_FechaBloqueoTemporal;
		public String p_TipoEntidad;
		public Long p_IdEntidad;
		public Long p_HeredarRolesUsuario;
		public String p_Canal;
		
		public String p_UsuarioCorredores;
		public String p_UsuarioNegocios;
		public String p_UsuarioClientes;
		public String p_UsuarioServicios;
		public String p_EsUsuarioBolsa;
		
		public EntradaCrearUsuario() {
			this.p_Canal = "CORREDORES";
			this.p_UsuarioCorredores = "N";
			this.p_UsuarioNegocios = "N";
			this.p_UsuarioClientes = "N";
			this.p_UsuarioServicios = "N";
			this.p_EsUsuarioBolsa = "N";
					
			
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaCrearUsuario {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_NEW_USER_ID;

		public SalidaCrearUsuario() {
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
		// public Date FechaBloqueoTemporal;
		public String TipoEntidad;
		public Long IdEntidad;
		public String EsAutorizado;
		public String UsuarioCorredores;
		public String UsuarioNegocios;
		public String UsuarioClientes;
		public String UsuarioServicios;
		public String EsUsuarioBolsa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class HeredarUsuario{
		public Long IdUsuario;
		public Long IdUsuarioHeredar;
	}
	
	public static class CorreosSEG{
		public Long IdCorreo;
		public Long IdPersona;		
	}
	
	public static class CorreosExtra{
		public String IdCorreosJuntos;
	}
	
	public static class PrimeraPersonaValida{
		public Long IdPersona;
	}
	
	public boolean ContieneSoloNumeros(String texto) {
	    char[] chars = texto.toCharArray();

	    for (char c : chars) {
	        if(Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}	

	public SalidaCrearUsuario logica(Connection conn, EntradaCrearUsuario in) throws Exception {
		logger.info("INIT");
		SalidaCrearUsuario salida = new SalidaCrearUsuario();		
		SQL sql = new SQL();
		AtomicLong contador = new AtomicLong(0L);
		HashMap<String, String> mapMsg = new HashMap<>();
				
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("TipoEntidad", in.p_TipoEntidad);
		params.put("IdEntidad", in.p_IdEntidad);
		logger.debug("Evaluar si existe el socio de negocio");
		logger.debug("INICIO::SQL_SELECT_SOCIONEGOCIO");
		List<SocioNegocio> sociosDeNegocio = sql.query(conn, SQL_SELECT_SOCIONEGOCIO, params, SocioNegocio.class);
		logger.debug("FIN::SQL_SELECT_SOCIONEGOCIO");
		if(sociosDeNegocio.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_SOCIO_NEGOCIO", mapMsg, conn, in.p_IdEmpresa));
		}

		logger.debug("Evaluar si el codigo de usuario ya esta asociado a otro socio de negocio");
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("CodigoUsuario", in.p_CodigoUsuario);
		params.put("TipoEntidad", in.p_TipoEntidad);
		params.put("IdEntidad", in.p_IdEntidad);
		logger.debug("INICIO::SQL_SELECT_USUARIO");
		List<Usuario> usuarios = sql.query(conn, SQL_SELECT_USUARIO, params, Usuario.class);
		logger.debug("FIN::SQL_SELECT_USUARIO");
		if(!usuarios.isEmpty()) {
			String msjError = usuarios.stream().filter(us -> us.CodigoUsuario.equals(in.p_CodigoUsuario)).count() != 0 ? "USUARIO_YA_EXISTE" : "UI_SOCIO_ASOCIADO_A_USUARIO";
			salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, msjError, mapMsg, conn, in.p_IdEmpresa));
		}
		
		logger.debug("Evaluar si el usuario tiene el rol seguridad, el cual permite la creacion de usuarios.");
		Long idRolSeguridad = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "ACC.IdRolSeguridad");
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdUsuario", in.p_IdUsuario);
		params.put("IdRol", idRolSeguridad);
		logger.debug("INICIO::SQL_SELECT_ROL_USUARIO");
		usuarios = sql.query(conn, SQL_SELECT_ROL_USUARIO, params, Usuario.class);
		logger.debug("FIN::SQL_SELECT_ROL_USUARIO");
		if(usuarios.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_NO_EQUIPO_SEGURIDAD", mapMsg, conn, in.p_IdEmpresa));
		}
		
		logger.debug("Evaluar si existe el usuario del cual se quieren heredar roles.");
		if(!in.p_HeredarRolesUsuario.equals(-1L)) {
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdUsuario", in.p_IdUsuario);
			logger.debug("INICIO::SQL_SELECT_HEREDAR_ROL_USUARIO");
			List<Contador> contadores = sql.query(conn, SQL_SELECT_HEREDAR_ROL_USUARIO, params, Contador.class);
			logger.debug("FIN::SQL_SELECT_HEREDAR_ROL_USUARIO");
			if(contadores.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_HEREDAR_ROLES_ERROR", mapMsg, conn, in.p_IdEmpresa));
			}
		}
		logger.debug("Evaluar si hay errores y terminar el programa");
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if(salida.P_OUT_ERRORESCONTADOR>=1L) {
			return salida;
		}

		logger.debug("Validaciones OK, crear al usuario");
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
		String esEnrolado = ("S".equals(requiereAutorizacion)) ? "P" : "S";
		Long diasVencimientoClave = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.DiasCambioClave");
		LinkedList<Long> secuenciaUsuario = new LinkedList<Long>(Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Usuario", 1L));
		
		logger.debug("Realizar insert");
		Usuario usuario = new Usuario();
		usuario.IdUsuario = secuenciaUsuario.peek();
		usuario.IdEmpresa = in.p_IdEmpresa;
		usuario.CodigoUsuario = in.p_CodigoUsuario;
		// Crear Hash con los datos para el query batch
		in.p_Clave = Hashing.sha256().newHasher().putString(in.p_Clave, Charsets.UTF_8).hash().toString().toUpperCase();
		usuario.Clave = in.p_Clave;
		usuario.FechaIngreso = Fecha.toDate(in.p_FechaIngreso);
		usuario.FechaVigencia = Fecha.toDate(in.p_FechaVigencia);
		usuario.FechaUltimaConexion = Fecha.toDate(in.p_FechaVigencia);
		usuario.DiasVencimientoClave = BigDecimal.valueOf(diasVencimientoClave);
		usuario.Conectado = in.p_Conectado;
		usuario.Hora = Time.valueOf(LocalTime.now());
		usuario.Terminal = in.p_Terminal;
		usuario.IdEstado = in.p_IdEstado;
		usuario.IntentosConexion = in.p_IntentosConexion;
		usuario.TipoEntidad = in.p_TipoEntidad;
		usuario.IdEntidad = in.p_IdEntidad;
		usuario.EsAutorizado = esEnrolado;
		usuario.UsuarioCorredores = in.p_UsuarioCorredores;
		usuario.UsuarioClientes = in.p_UsuarioClientes;
		usuario.UsuarioNegocios = in.p_UsuarioNegocios;
		usuario.UsuarioServicios = in.p_UsuarioServicios;
		usuario.EsUsuarioBolsa = in.p_EsUsuarioBolsa;
		
		params.clear();
		params.put("IdEmpresa", usuario.IdEmpresa);
		params.put("CodigoUsuario", usuario.CodigoUsuario);
		List<Usuario> existe_usuario = sql.query(conn, SQL_CONSULTA_EXISTE_USUARIO, params, Usuario.class);
		if(!existe_usuario.isEmpty()) {
			MensajeSalida msj = new MensajeSalida();
			msj.codigo = "ERROR";
			msj.descripcion = "El Usuario ya Existe";
			msj.id = 0L;
			msj.tipo = "ERROR";
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(msj);
			
			return salida;
		}
		
		logger.debug("Insertar en la tabla!");
		logger.debug("INICIO::SQL_INSERT_USUARIO");
		int[] res = sql.batch(conn, SQL_INSERT_USUARIO, Arrays.asList(usuario), Usuario.class);
		logger.debug("FIN::SQL_INSERT_USUARIO");
		//1==usuario creado correctamente.
		if(res[0]==1) {
			salida.P_OUT_NEW_USER_ID = secuenciaUsuario.peek();
			
			logger.debug("Usuario creado correctamente");
			if (!in.p_HeredarRolesUsuario.equals(-1L)) {
				HeredarUsuario heredarUsuario = new HeredarUsuario();
				heredarUsuario.IdUsuario = secuenciaUsuario.peek();
				heredarUsuario.IdUsuarioHeredar = in.p_HeredarRolesUsuario;
				// heredar roles
				logger.debug("INICIO::SQL_INSERT_ROLES_HEREDADOS");
				sql.batch(conn, SQL_INSERT_ROLES_HEREDADOS, Arrays.asList(heredarUsuario), HeredarUsuario.class);
				logger.debug("FIN::SQL_INSERT_ROLES_HEREDADOS");
				// heredar menu
				logger.debug("INICIO::SQL_UPDATE_MENU_HEREDADOS");
				sql.batch(conn, SQL_UPDATE_MENU_HEREDADOS, Arrays.asList(heredarUsuario), HeredarUsuario.class);
				logger.debug("FIN::SQL_UPDATE_MENU_HEREDADOS");
				logger.debug("Roles heredados correctamente");
			}
			
			if("S".equals(requiereAutorizacion)) {
				logger.debug("Generar evento autorizacion.");
				CrearEventoAutorizacionMasivo.ServiceEntrada paramsIn = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				paramsIn.P_IDEMPRESA = in.p_IdEmpresa;
				paramsIn.P_IDMODULO = in.p_IdModulo;
				paramsIn.P_IDUSUARIO = in.p_IdUsuario;
				paramsIn.P_USAREGLASNEGOCIO = "N";
				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS eventoIn = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				eventoIn.Fecha = in.p_FechaIngreso;
				eventoIn.IdCliente = secuenciaUsuario.peek();
				eventoIn.IdModuloSolicita = in.p_IdModulo;
				eventoIn.IdUsuarioSolicita = in.p_IdUsuario;
				eventoIn.NumeroOperacionConcepto = secuenciaUsuario.peek().toString();
				eventoIn.IdTipoEvento = "ENROLU";
				paramsIn.P_TABLAEVENTOS.add(eventoIn);
				CrearEventoAutorizacionMasivo crearEventoAutorizacion = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceSalida paramsOut = crearEventoAutorizacion.logica(conn, paramsIn);

				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE err : paramsOut.P_OUT_ERRORESDETALLE) {
					// Error en crear evento autorizacion
					mapMsg.clear();
					mapMsg.put("NombreTipoEvento", "Enrolamiento Usuario");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, err.Codigo, mapMsg, conn, in.p_IdEmpresa));
				}
				for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE adv : paramsOut.P_OUT_ADVERTENCIASDETALLE) {
					// Error en crear evento autorizacion
					mapMsg.clear();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ADVERTENCIA, adv.Codigo, mapMsg, conn, in.p_IdEmpresa));
				}
			}
			
			List<ParamEnvioCorreo> paramEnvio = sql.query(conn, SQL_SELECT_PARAM_ENVIO, new HashMap<String,Object>(), ParamEnvioCorreo.class);
			
			if(!paramEnvio.isEmpty()){
				if(paramEnvio.get(0).Valor.equals("S") && in.p_UsuarioCorredores.equals("S") || paramEnvio.get(0).Valor.equals("S") && in.p_UsuarioNegocios.equals("S")){
					//notificacion creacion usuario
					String mensaje = "con fecha de " + in.p_FechaIngreso + " se ha creado un usuario de codigo: " + in.p_CodigoUsuario + " y tipo(s): ";
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
		                    evt.FechaCreacionSistema 	= in.p_FechaIngreso;            
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
		                
		                salida.P_OUT_MENSAJES.add(new MensajeSalida(new Long(notifIn.P_IDEMPRESA),
		    					MensajeSalida.MENSAJE_INFORMACION, notifOut.P_OUT_IDEVENTO.toString(),
		    					"Usuario creado exitosamente."));

		                logger.info("Finaliza Llamada a Servicio AgregarEvento");
		            }else{
		            	logger.info("P_EVENTOS vacio");
		            }
				}
			}	
			
		}else {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(contador.getAndIncrement(), MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CREAR_USUARIO", mapMsg, conn, in.p_IdEmpresa));
		}
		
		logger.info("END");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearUsuario in = gson.fromJson(jin.toString(), EntradaCrearUsuario.class);
		SalidaCrearUsuario out = logica(conn, in);
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
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"PLA\",\"p_CodigoUsuario\":\"CLIENT\",\"p_Clave\":\"6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b\",\"p_FechaIngreso\":\"2020-06-05\",\"p_FechaVigencia\":\"0001-01-01\",\"p_FechaUltimaConexion\":\"2020-06-05\",\"p_Conectado\":\"N\",\"p_Hora\":\"16:14:53\",\"p_Terminal\":\"Terminal Nro\",\"p_IdEstado\":\"ACT\",\"p_IntentosConexion\":0,\"p_FechaBLoqueoTempotal\":\"2020-07-03 20:14:53\",\"p_TipoEntidad\":\"CLI\",\"p_IdEntidad\":1419731,\"p_HeredarRolesUsuario\":1,\"p_UsuarioCorredores\":\"S\",\"p_UsuarioNegocios\":\"N\",\"p_UsuarioClientes\":\"N\",\"p_UsuarioServicios\":\"N\"}";

			// String json2 =
			// "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"PLA\",\"p_CodigoUsuario\":\"ROBOTECH\",\"p_Clave\":\"6b51d431df5d7f141cbececcf79edf3dd861c3b4069f0b11661a3eefacbba918\",\"p_FechaIngreso\":\"2019-01-04\",\"p_FechaVigencia\":\"0001-01-01\",\"p_FechaUltimaConexion\":\"2019-01-04\",\"p_Conectado\":\"N\",\"p_Hora\":\"12:55:41\",\"p_Terminal\":\"Terminal
			// Nro\",\"p_IdEstado\":\"ACT\",\"p_IntentosConexion\":0,\"p_FechaBLoqueoTempotal\":\"2019-05-22
			// 16:55:41\",\"p_TipoEntidad\":\"EMP\",\"p_IdEntidad\":231,\"p_HeredarRolesUsuario\":-1}";
			EntradaCrearUsuario objIn = gson.fromJson(json, EntradaCrearUsuario.class);
			// mientras pruebo hare el rollback
			CrearUsuario demo = new CrearUsuario();

			SalidaCrearUsuario ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			
			conn.rollback();
			//conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");

	};
}
