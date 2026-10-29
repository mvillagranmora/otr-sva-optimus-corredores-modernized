package cl.bcs.plataforma.acc;

import java.sql.Connection;
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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarPerfilAcceso implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ActualizarPerfilAcceso.class);

	private final String SQL_SELECT_ESTADO_ANTIGUO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_EstadoAntiguo.sql");
	private final String SQL_UPDATE_PERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Update_Perfil.sql");
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Configuracion.sql");
	private final String SQL_SELECT_COMPARACION = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Comparacion.sql");
	private final String SQL_SELECT_COUNT_ACCESO_PERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Count_AccesoPerfil.sql");
	private final String SQL_SELECT_ID_ACCESO_PERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_IdAccesoPerfil.sql");
	private final String SQL_SELECT_DETALLE_LOG_ELIMINAR = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_DetalleLog_Eliminar.sql");
	private final String SQL_DELETE_PERFIL_ACCESO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Delete_PerfilAcceso.sql");
	private final String SQL_UPDATE_PERFIL_ACCESO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Update_PerfilAcceso.sql");
	private final String SQL_SELECT_COUNT_ACCESO_REPETIDO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Count_AccesoRepetido.sql");
	private final String SQL_INSERT_PERFIL_ACCESO = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Insert_PerfilAcceso.sql");
	private final String SQL_SELECT_DETALLE_LOG_INSERTAR = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_DetalleLog_Insertar.sql");
	
	public static class AccesoItem {
		public Long p_IdAcceso;
		public String p_Existe;
	}

	public static class EntradaActualizarPerfilAcceso {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Nombre;
		public String p_IdEstado;
		public Long p_IdPerfil;
		public List<AccesoItem> p_Accesos;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaActualizarPerfilAcceso {
		public String P_OUT_UPDATE;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaActualizarPerfilAcceso() {
			this.P_OUT_UPDATE = "";
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EstadoResult {
		public String IdEstado;
	}
	
	public static class Modificado {
		public String Modificado;
	}

	public static class ConfiguracionResult {
		public String Valor;
	}

	public static class ComparacionResult {
		public Long p_IdAcceso;
		public String p_Existe;
		public String Modificado;
	}

	public static class InsertPerfilAcceso {
		public Long IdAccesoPerfil;
		public Long IdEmpresa;
		public Long IdPerfil;
		public Long IdAcceso;
		public String IdEstado;
		public Long AsignoUsuario;
	}

	public static class UpdatePerfilAcceso {
		public String IdEstado;
		public Long AsignoUsuario;
		public Long IdAcceso;
		public Long IdPerfil;
		public Long IdEmpresa;
	}

	public static class DeletePerfilAcceso {
		public Long IdAccesoPerfil;
	}
	
	public static class Contador {
		public Long Contador;
	}
	
	public static class IdAccesoPerfilResult {
		public Long IdAccesoPerfil;
	}
	
	public static class DetalleLog {
		public String SubRegistro;
		public String AsignoUsuario;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	

	public SalidaActualizarPerfilAcceso logica(Connection conn, EntradaActualizarPerfilAcceso in) throws Exception {
		logger.info("INIT ActualizarPerfilAcceso");
		SalidaActualizarPerfilAcceso salida = new SalidaActualizarPerfilAcceso();
		SQL sql = new SQL();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		try {
			// Obtener estado antiguo del perfil
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdPerfil", in.p_IdPerfil);
			params.put("IdEmpresa", in.p_IdEmpresa);

			logger.debug("SELECT estado antiguo");
			List<EstadoResult> estadoAntiguo = sql.query(conn, SQL_SELECT_ESTADO_ANTIGUO, params, EstadoResult.class);
			String v_EstadoAntiguo = (estadoAntiguo != null && estadoAntiguo.size() > 0) ? estadoAntiguo.get(0).IdEstado : "";

			// Actualizar datos del perfil
			logger.debug("UPDATE perfil");
			List<UpdatePerfil> paramsUpdatePerfil = new ArrayList<>();
			UpdatePerfil updatePerfil = new UpdatePerfil();
			updatePerfil.Nombre = in.p_Nombre;
			updatePerfil.IdEstado = in.p_IdEstado;
			updatePerfil.IdPerfil = in.p_IdPerfil;
			updatePerfil.IdEmpresa = in.p_IdEmpresa;
			paramsUpdatePerfil.add(updatePerfil);
			
			sql.batch(conn, SQL_UPDATE_PERFIL, paramsUpdatePerfil, UpdatePerfil.class);

			// Obtener configuración de accesos perfiles repetidos
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			List<ConfiguracionResult> config = sql.query(conn, SQL_SELECT_CONFIGURACION, params, ConfiguracionResult.class);
			String v_accesosPerfilesRepetidos = (config != null && config.size() > 0) ? config.get(0).Valor : "S";

			// Comparar accesos
			logger.debug("Comparar accesos entrada vs existentes");
			List<ComparacionResult> comparacion = new ArrayList<>();
			for (AccesoItem acceso : in.p_Accesos) {
				params.clear();
				params.put("IdAcceso", acceso.p_IdAcceso);
				params.put("IdPerfil", in.p_IdPerfil);
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("p_Existe", acceso.p_Existe);
				params.put("p_IdEstado", in.p_IdEstado);

		
				List<Modificado> comp = sql.query(conn, SQL_SELECT_COMPARACION, params, Modificado.class);
				
				for (Modificado row : comp) {
					ComparacionResult cr = new ComparacionResult();
					cr.p_IdAcceso = acceso.p_IdAcceso;
					cr.p_Existe = acceso.p_Existe;
					cr.Modificado = row.Modificado;
					comparacion.add(cr);
				}
			}

			// Filtrar solo modificados
			List<ComparacionResult> soloModificados = new ArrayList<>();
			for (ComparacionResult cr : comparacion) {
				if ("S".equals(cr.Modificado)) {
					soloModificados.add(cr);
				}
			}

			// Validar si hay cambios
			if (soloModificados.size() == 0) {
				logger.debug("Sin cambios detectados");
			
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_SIN_CAMBIOS", "No se detectaron cambios para actualizar."));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Variables para log
			boolean needLog = false;
			boolean logEliminar = false;
			boolean logInsertar = false;
			List<DetalleLog> detalleLogEliminar = new ArrayList<>();
			List<DetalleLog> detalleLogInsertar = new ArrayList<>();

			// Procesar cada acceso modificado
			for (ComparacionResult modificado : soloModificados) {
				Long v_IdAcceso = modificado.p_IdAcceso;
				
				params.clear();
				params.put("IdAcceso", v_IdAcceso);
				params.put("IdPerfil", in.p_IdPerfil);
				params.put("IdEmpresa", in.p_IdEmpresa);

				List<Contador> countResult = sql.query(conn, SQL_SELECT_COUNT_ACCESO_PERFIL, params, Contador.class);
				Long v_countId = (countResult != null && countResult.size() > 0) ? countResult.get(0).Contador : 0L;

				
				if (v_countId > 0) {
					// Existe la relación acceso-perfil-empresa
					if ("N".equals(modificado.p_Existe)) {
						// Eliminar acceso
						logger.debug("Eliminar acceso IdAcceso: " + v_IdAcceso);
						

					List<IdAccesoPerfilResult> idResult = sql.query(conn, SQL_SELECT_ID_ACCESO_PERFIL, params, IdAccesoPerfilResult.class);
					Long v_IdAccesoPerfil = (idResult != null && idResult.size() > 0) ? idResult.get(0).IdAccesoPerfil : null;

						// Eliminar
						DeletePerfilAcceso deleteData = new DeletePerfilAcceso();
						deleteData.IdAccesoPerfil = v_IdAccesoPerfil;
						sql.batch(conn, SQL_DELETE_PERFIL_ACCESO, java.util.Arrays.asList(deleteData), DeletePerfilAcceso.class);

						needLog = true;
						logEliminar = true;
					} else {
						// Actualizar estado
						logger.debug("Actualizar estado acceso IdAcceso: " + v_IdAcceso);
						
						List<UpdatePerfilAcceso> paramsUpdatePerfilAcceso = new ArrayList<>();
						UpdatePerfilAcceso updateData = new UpdatePerfilAcceso();
						updateData.IdEstado = in.p_IdEstado;
						updateData.AsignoUsuario = in.p_IdUsuario;
						updateData.IdAcceso = v_IdAcceso;
						updateData.IdPerfil = in.p_IdPerfil;
						updateData.IdEmpresa = in.p_IdEmpresa;
						paramsUpdatePerfilAcceso.add(updateData);
						
						sql.batch(conn, SQL_UPDATE_PERFIL_ACCESO, paramsUpdatePerfilAcceso, UpdatePerfilAcceso.class);
					}
				} else if ("S".equals(modificado.p_Existe)) {
					// Insertar nuevo acceso
					v_countId = 0L;
					
					if ("N".equals(v_accesosPerfilesRepetidos)) {
						params.clear();
						params.put("IdAcceso", v_IdAcceso);
						params.put("IdEmpresa", in.p_IdEmpresa);
						
						List<Contador> countRepetido = sql.query(conn, SQL_SELECT_COUNT_ACCESO_REPETIDO, params, Contador.class);
						v_countId = (countRepetido != null && countRepetido.size() > 0) ? countRepetido.get(0).Contador : 0L;
					}

					if (v_countId == 0) {
						logger.debug("Insertar acceso IdAcceso: " + v_IdAcceso);
						
						List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PerfilAcceso", 1L);
						Long v_IdAccesoPerfil = secuencia.get(0);

						// Obtener detalle para log
						params.clear();
						params.put("IdAcceso", v_IdAcceso);
						params.put("IdAccesoPerfil", v_IdAccesoPerfil);
						
						List<DetalleLog> logDetail = sql.query(conn, SQL_SELECT_DETALLE_LOG_INSERTAR, params, DetalleLog.class);
						detalleLogInsertar.addAll(logDetail);

						// Insertar
						InsertPerfilAcceso insertData = new InsertPerfilAcceso();
						insertData.IdAccesoPerfil = v_IdAccesoPerfil;
						insertData.IdEmpresa = in.p_IdEmpresa;
						insertData.IdPerfil = in.p_IdPerfil;
						insertData.IdAcceso = v_IdAcceso;
						insertData.IdEstado = in.p_IdEstado;
						insertData.AsignoUsuario = in.p_IdUsuario;
						sql.batch(conn, SQL_INSERT_PERFIL_ACCESO, java.util.Arrays.asList(insertData), InsertPerfilAcceso.class);

						needLog = true;
						logInsertar = true;
					} else {
						// Acceso repetido
						logger.debug("Acceso repetido IdAcceso: " + v_IdAcceso);
						params.clear();
						params.put("IdAcceso", v_IdAcceso);
						


						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("UI_ACCESO_REPETIDO", "Se detecto acceso repetido"));
					}
				}
			}

			// Verificar si hay errores
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			// Agregar logs
			
			
			if (needLog) {
				if (logEliminar) {
					logger.debug("Agregar log eliminacion");
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = 159L;
					log.IdTipoLog = "ELIMINACION";
					log.IdModulo = "PLA";
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdTipoRegistro = "Perfil";
					entradaAgregarLog.p_Log.add(log);
					
					
					AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
					detLog.IdCabeceraLog = 159L;
					detLog.IdTrabajo= 0L;
					detLog.SubRegistro = null;
					detLog.CampoRegistro = "Eliminacion perfil acceso";
					detLog.ValorAntiguo = v_EstadoAntiguo;
					detLog.ValorNuevo = "";
					entradaAgregarLog.p_LogDetalle.add(detLog);

					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaAgregarLog);
				}

				if (logInsertar) {
					logger.debug("Agregar log insercion");
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = 160L;
					log.IdTipoLog = "INGRESO";
					log.IdModulo = "PLA";
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdTipoRegistro = "Perfil";
					entradaAgregarLog.p_Log.add(log);
					
					
					AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
					detLog.IdCabeceraLog = 160L;
					detLog.IdTrabajo= 0L;
					detLog.SubRegistro = null;
					detLog.CampoRegistro = "Ingreso perfil acceso";
					detLog.ValorAntiguo = v_EstadoAntiguo;
					detLog.ValorNuevo = "";
					entradaAgregarLog.p_LogDetalle.add(detLog);

					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaAgregarLog);
				}
			}

			// Log de cambio de estado
			if (!v_EstadoAntiguo.equals(in.p_IdEstado)) {
				logger.debug("Estado cambio, agregar log");				
				
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = 161L;
				log.IdTipoLog = "ACTUALIZACION";
				log.IdModulo = "PLA";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdTipoRegistro = "Perfil";
				entradaAgregarLog.p_Log.add(log);
				
				
				AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdCabeceraLog = 161L;
				detLog.IdTrabajo= 0L;
				detLog.SubRegistro = null;
				detLog.CampoRegistro = "CambioEstado";
				detLog.ValorAntiguo = v_EstadoAntiguo;
				detLog.ValorNuevo = in.p_IdEstado;
				entradaAgregarLog.p_LogDetalle.add(detLog);

				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
			}

			salida.P_OUT_UPDATE = "ok++";
			

		} catch (Exception ex) {
			logger.error("Error en ActualizarPerfilAcceso: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_ACTUALIZAR_PERFIL_ACCESO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ActualizarPerfilAcceso");
		return salida;
	}

	public static class UpdatePerfil {
		public String Nombre;
		public String IdEstado;
		public Long IdPerfil;
		public Long IdEmpresa;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarPerfilAcceso in = gson.fromJson(jin.toString(), EntradaActualizarPerfilAcceso.class);
		SalidaActualizarPerfilAcceso out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_Nombre\":\"TESTPERFIL\",\"p_IdEstado\":\"ACT\",\"p_IdPerfil\":39,\"p_Accesos\":[{\"p_IdAcceso\":1,\"p_Existe\":\"S\"}]}";
			EntradaActualizarPerfilAcceso entrada = gson.fromJson(json, EntradaActualizarPerfilAcceso.class);
			ActualizarPerfilAcceso demo = new ActualizarPerfilAcceso();
			SalidaActualizarPerfilAcceso salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
