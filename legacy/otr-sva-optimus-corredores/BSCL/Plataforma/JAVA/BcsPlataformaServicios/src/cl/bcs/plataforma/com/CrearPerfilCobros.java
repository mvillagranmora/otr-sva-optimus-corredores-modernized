package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPerfilCobros implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfilCobros.class);

	private final String SQL_INSERT_PERFIL = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilCobros_Insert_Perfil.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilCobros_Select_RequiereAutorizacion.sql");

	public static class EntradaCrearPerfilCobros {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilCobros;
		public String p_DescripcionPerfilCobros;
		public String p_EsPerfilPersonalizado;
		public String p_TipoEntidadReferencia;
		public Long p_IdReferencia;
		public String p_IdEstado;
		public String p_PorDefecto;

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

	public static class SalidaCrearPerfilCobros {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfilCobros() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertPerfil {
		public String IdPerfilCobros;
		public String DescripcionPerfilCobros;
		public String EsPerfilPersonalizado;
		public String TipoEntidadReferencia;
		public Long IdReferencia;
		public String FechaCreacion;
		public String FechaModificacion;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
		public String FechaIngreso;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaCrearPerfilCobros logica(Connection conn, EntradaCrearPerfilCobros in) throws Exception {
		logger.info("INIT CrearPerfilCobros");
		SalidaCrearPerfilCobros salida = new SalidaCrearPerfilCobros();
		SQL sql = new SQL();

		try {
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_IdConcepto = "NCPCOB";
			String v_IdEstadoAutorizacion = "PEN";

			// Valores por defecto para parámetros opcionales (igual que en el SP)
			String v_TipoEntidadReferencia = (in.p_TipoEntidadReferencia != null) ? in.p_TipoEntidadReferencia : "";
			Long v_IdReferencia = (in.p_IdReferencia != null) ? in.p_IdReferencia : 0L;

			// Insertar perfil de cobros
			logger.debug("INSERT perfil cobros");
			InsertPerfil insertPerfil = new InsertPerfil();
			insertPerfil.IdPerfilCobros = in.p_IdPerfilCobros;
			insertPerfil.DescripcionPerfilCobros = in.p_DescripcionPerfilCobros;
			insertPerfil.EsPerfilPersonalizado = in.p_EsPerfilPersonalizado;
			insertPerfil.TipoEntidadReferencia = v_TipoEntidadReferencia;
			insertPerfil.IdReferencia = v_IdReferencia;
			insertPerfil.FechaCreacion = v_FechaHoraSistema;
			insertPerfil.FechaModificacion = v_FechaHoraSistema;
			insertPerfil.IdEstado = in.p_IdEstado;
			insertPerfil.PorDefecto = in.p_PorDefecto;
			insertPerfil.IdEmpresa = in.p_IdEmpresa;
			insertPerfil.FechaIngreso = v_FechaIngreso;
			insertPerfil.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertPerfil.IdUsuarioCreador = in.p_IdUsuario;

			sql.batch(conn, SQL_INSERT_PERFIL, java.util.Arrays.asList(insertPerfil), InsertPerfil.class);

			salida.P_OUT_REFID = 1L;

			// Consultar si requiere autorización
			HashMap<String, Object> paramsAuth = new HashMap<>();
			paramsAuth.put("IdEmpresa", in.p_IdEmpresa);
			
			logger.debug("Consultando si requiere autorización");
			List<RequiereAutorizacion> reqAutList = sql.query(conn, SQL_SELECT_REQUIERE_AUTORIZACION, paramsAuth, RequiereAutorizacion.class);
			
			String v_RequiereAutorizacion = "N";
			if (!reqAutList.isEmpty()) {
				v_RequiereAutorizacion = reqAutList.get(0).RequiereAutorizacion;
			}

			if ("S".equals(v_RequiereAutorizacion)) {
				// Generar evento de autorización
				logger.debug("Crear evento de autorización");
				CrearEventoAutorizacion.ServiceEntrada crearEvtIn = new CrearEventoAutorizacion.ServiceEntrada();
				crearEvtIn.p_IdEmpresa = in.p_IdEmpresa;
				crearEvtIn.p_IdUsuario = in.p_IdUsuario;
				crearEvtIn.p_IdModulo = in.p_IdModulo;
				crearEvtIn.p_UsaReglasNegocio = "N";
				crearEvtIn.p_IdTipoEvento = v_IdConcepto;
				crearEvtIn.p_IdModuloSolicita = in.p_IdModulo;
				crearEvtIn.p_IdUsuarioSolicita = in.p_IdUsuario;
				crearEvtIn.p_Fecha = v_FechaIngreso;
				crearEvtIn.p_IdConcepto = v_IdConcepto;
				crearEvtIn.p_NumeroOperacionConcepto = "0";
				crearEvtIn.p_FechaOperacionConcepto = v_FechaIngreso;
				crearEvtIn.p_IdCliente = 0L;
				crearEvtIn.p_Glosa = in.p_DescripcionPerfilCobros;
				crearEvtIn.p_IdCanal = "OPT";

				CrearEventoAutorizacion crearEvtWrapper = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceSalida crearEvtOut = crearEvtWrapper.logica(conn, crearEvtIn);

				// Agregar advertencias y errores del evento
				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.p_out_AdvertenciasContador.intValue();
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : crearEvtOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.p_out_ErroresContador.intValue();
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : crearEvtOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

			} else {
				// Confirma la creación directamente
				logger.debug("Confirmar creación de perfil");
				v_IdEstadoAutorizacion = "ING";
				
				ConfirmarCrearPerfilCobros.EntradaConfirmarCrearPerfilCobros confirmarIn = new ConfirmarCrearPerfilCobros.EntradaConfirmarCrearPerfilCobros();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdPerfilCobros = in.p_IdPerfilCobros;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearPerfilCobros confirmarWrapper = new ConfirmarCrearPerfilCobros();
				ConfirmarCrearPerfilCobros.SalidaConfirmarCrearPerfilCobros confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				for (ConfirmarCrearPerfilCobros.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				for (ConfirmarCrearPerfilCobros.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

			// Agregar log
			logger.debug("Agregar log de ingreso");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "COM";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaIngreso;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "PERFIL COBROS";
			entradaAgregarLog.p_Log.add(log);
			
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = "INGRESO";
			detLog.CampoRegistro = "IdPerfilCobros";
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = in.p_IdPerfilCobros;
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearPerfilCobros: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL_COBROS", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfilCobros");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfilCobros in = gson.fromJson(jin.toString(), EntradaCrearPerfilCobros.class);
		SalidaCrearPerfilCobros out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPerfilCobros\":\"PRUEBA4\",\"p_DescripcionPerfilCobros\":\"PRUEBA POST CAMBIOS\",\"p_EsPerfilPersonalizado\":\"N\",\"p_IdEstado\":\"ACT\",\"p_PorDefecto\":\"N\"}";
			EntradaCrearPerfilCobros entrada = gson.fromJson(json, EntradaCrearPerfilCobros.class);
			CrearPerfilCobros demo = new CrearPerfilCobros();
			SalidaCrearPerfilCobros salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
