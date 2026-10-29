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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearGrupoActivoDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearGrupoActivoDetalle.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearGrupoActivoDetalle_Count_Existe.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearGrupoActivoDetalle_Insert_Detalle.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/com/sql/CrearGrupoActivoDetalle_Select_Glosa.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearGrupoActivoDetalle_Select_RequiereAutorizacion.sql");

	public static class EntradaCrearGrupoActivoDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdGrupoActivos;
		public Long p_IdInstrumento;
		public String p_IdEstado;

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

	public static class SalidaCrearGrupoActivoDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearGrupoActivoDetalle() {
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

	public static class CountExiste {
		public Long countEntidad;
	}

	public static class InsertDetalle {
		public Long IdDetalleGrupoActivos;
		public String IdGrupoActivos;
		public Long IdInstrumento;
		public String FechaCreacion;
		public String FechaIngreso;
		public String FechaModificacion;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public String IdEstado;
		public Long IdEmpresa;
	}

	public static class GlosaResult {
		public String DescripcionGrupoActivos;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaCrearGrupoActivoDetalle logica(Connection conn, EntradaCrearGrupoActivoDetalle in) throws Exception {
		logger.info("INIT CrearGrupoActivoDetalle");
		SalidaCrearGrupoActivoDetalle salida = new SalidaCrearGrupoActivoDetalle();
		SQL sql = new SQL();

		try {
			// Validar que no exista el detalle activo
			logger.debug("Verificar existencia de detalle grupo activos");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("pIdInstrumento", in.p_IdInstrumento);
			paramsCount.put("pIdGrupoActivos", in.p_IdGrupoActivos);

			List<CountExiste> countList = sql.query(conn, SQL_COUNT_EXISTE, paramsCount, CountExiste.class);
			if (!countList.isEmpty() && countList.get(0).countEntidad > 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("DETALLE_GRUPO_ACTIVO_EXISTE", "El detalle de grupo activos ya existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_IdConcepto = "NDGACT";
			String v_IdEstadoAutorizacion = "PEN";

			// Generar secuencia para ID
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::DetalleGrupoActivos", 1L);
			Long v_IdDetalleGrupoActivos = secuencia.get(0);
			salida.P_OUT_REFID = v_IdDetalleGrupoActivos;

			// Insertar detalle del grupo activos
			logger.debug("INSERT detalle grupo activos");
			InsertDetalle insertDetalle = new InsertDetalle();
			insertDetalle.IdDetalleGrupoActivos = v_IdDetalleGrupoActivos;
			insertDetalle.IdGrupoActivos = in.p_IdGrupoActivos;
			insertDetalle.IdInstrumento = in.p_IdInstrumento;
			insertDetalle.FechaCreacion = v_FechaHoraSistema;
			insertDetalle.FechaIngreso = v_FechaIngreso;
			insertDetalle.FechaModificacion = v_FechaHoraSistema;
			insertDetalle.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertDetalle.IdUsuarioCreador = in.p_IdUsuario;
			insertDetalle.IdEstado = in.p_IdEstado;
			insertDetalle.IdEmpresa = in.p_IdEmpresa;

			sql.batch(conn, SQL_INSERT_DETALLE, java.util.Arrays.asList(insertDetalle), InsertDetalle.class);

			// Consultar si requiere autorización
			HashMap<String, Object> paramsAuth = new HashMap<>();
			paramsAuth.put("IdEmpresa", in.p_IdEmpresa);
			paramsAuth.put("IdConcepto", v_IdConcepto);

			logger.debug("Consultando si requiere autorización");
			List<RequiereAutorizacion> reqAutList = sql.query(conn, SQL_SELECT_REQUIERE_AUTORIZACION, paramsAuth, RequiereAutorizacion.class);

			String v_RequiereAutorizacion = "N";
			if (!reqAutList.isEmpty()) {
				v_RequiereAutorizacion = reqAutList.get(0).RequiereAutorizacion;
			}

			if ("S".equals(v_RequiereAutorizacion)) {
				// Obtener glosa para el evento
				logger.debug("Obtener glosa del grupo activos");
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdGrupoActivos", in.p_IdGrupoActivos);

				List<GlosaResult> glosaList = sql.query(conn, SQL_SELECT_GLOSA, params, GlosaResult.class);
				String v_Glosa = (glosaList != null && !glosaList.isEmpty()) ? glosaList.get(0).DescripcionGrupoActivos : "";

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
				crearEvtIn.p_NumeroOperacionConcepto = String.valueOf(v_IdDetalleGrupoActivos);
				crearEvtIn.p_FechaOperacionConcepto = v_FechaIngreso;
				crearEvtIn.p_IdCliente = 0L;
				crearEvtIn.p_Glosa = v_Glosa;
				crearEvtIn.p_IdCanal = "OPT";

				CrearEventoAutorizacion crearEvtWrapper = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceSalida crearEvtOut = crearEvtWrapper.logica(conn, crearEvtIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.p_out_AdvertenciasContador.intValue();
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : crearEvtOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.p_out_ErroresContador.intValue();
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : crearEvtOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}

				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", "La operación requiere autorización"));
				salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();

			} else {
				// Confirmar creación sin autorización
				logger.debug("Confirmar creación sin autorización");
				v_IdEstadoAutorizacion = "ING";

				ConfirmarCrearGrupoActivoDetalle.EntradaConfirmarCrearGrupoActivoDetalle confirmarIn = new ConfirmarCrearGrupoActivoDetalle.EntradaConfirmarCrearGrupoActivoDetalle();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdDetalleGrupoActivos = v_IdDetalleGrupoActivos;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearGrupoActivoDetalle confirmar = new ConfirmarCrearGrupoActivoDetalle();
				ConfirmarCrearGrupoActivoDetalle.SalidaConfirmarCrearGrupoActivoDetalle confirmarOut = confirmar.logica(conn, confirmarIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.P_OUT_ADVERTENCIASCONTADOR;
				for (ConfirmarCrearGrupoActivoDetalle.AdvertenciaDetalle adv : confirmarOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}
				salida.P_OUT_ERRORESCONTADOR += confirmarOut.P_OUT_ERRORESCONTADOR;
				for (ConfirmarCrearGrupoActivoDetalle.ErrorDetalle err : confirmarOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

		} catch (Exception ex) {
			logger.error("Error en CrearGrupoActivoDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_GRUPO_ACTIVOS_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearGrupoActivoDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearGrupoActivoDetalle in = gson.fromJson(jin.toString(), EntradaCrearGrupoActivoDetalle.class);
		SalidaCrearGrupoActivoDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdGrupoActivos\":\"TEST\",\"p_IdInstrumento\":1,\"p_IdEstado\":\"ACT\"}";
			EntradaCrearGrupoActivoDetalle entrada = gson.fromJson(json, EntradaCrearGrupoActivoDetalle.class);
			CrearGrupoActivoDetalle demo = new CrearGrupoActivoDetalle();
			SalidaCrearGrupoActivoDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
