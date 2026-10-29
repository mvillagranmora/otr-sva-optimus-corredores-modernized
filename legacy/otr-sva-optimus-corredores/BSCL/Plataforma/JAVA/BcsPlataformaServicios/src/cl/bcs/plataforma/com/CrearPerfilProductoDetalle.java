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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPerfilProductoDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfilProductoDetalle.class);

	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProductoDetalle_Insert_Detalle.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProductoDetalle_Select_Glosa.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProductoDetalle_Select_RequiereAutorizacion.sql");

	public static class EntradaCrearPerfilProductoDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilProductos;
		public String p_IdTipoProducto;
		public String p_IdTipoOperacion;
		public String p_IdCanalIngreso;
		public String p_IdMoneda;
		public String p_IdGrupoActivos;
		public String p_IdEstado;
		public String p_PermiteRuteo;

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

	public static class SalidaCrearPerfilProductoDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfilProductoDetalle() {
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

	public static class InsertDetalle {
		public Long IdDetallePerfilProductos;
		public String IdPerfilProductos;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String IdMoneda;
		public String IdGrupoActivos;
		public String IdEstado;
		public String PermiteRuteo;
		public String FechaCreacion;
		public Long IdEmpresa;
		public String FechaIngreso;
		public String FechaModificacion;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
	}

	public static class GlosaResult {
		public String DescripcionPerfilProductos;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaCrearPerfilProductoDetalle logica(Connection conn, EntradaCrearPerfilProductoDetalle in) throws Exception {
		logger.info("INIT CrearPerfilProductoDetalle");
		SalidaCrearPerfilProductoDetalle salida = new SalidaCrearPerfilProductoDetalle();
		SQL sql = new SQL();

		try {
			// Obtener fecha del sistema
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_IdConcepto = "NDPPRO";
			String v_IdEstadoAutorizacion = "PEN";

			// Generar secuencia para ID
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::DetallePerfilProductos", 1L);
			Long v_IdDetallePerfilProductos = secuencia.get(0);

			salida.P_OUT_REFID = v_IdDetallePerfilProductos;

			// Insertar detalle del perfil
			logger.debug("INSERT detalle perfil productos");
			InsertDetalle insertDetalle = new InsertDetalle();
			insertDetalle.IdDetallePerfilProductos = v_IdDetallePerfilProductos;
			insertDetalle.IdPerfilProductos = in.p_IdPerfilProductos;
			insertDetalle.IdTipoProducto = in.p_IdTipoProducto;
			insertDetalle.IdTipoOperacion = in.p_IdTipoOperacion;
			insertDetalle.IdCanalIngreso = in.p_IdCanalIngreso;
			insertDetalle.IdMoneda = in.p_IdMoneda;
			insertDetalle.IdGrupoActivos = in.p_IdGrupoActivos;
			insertDetalle.IdEstado = in.p_IdEstado;
			insertDetalle.PermiteRuteo = in.p_PermiteRuteo;
			insertDetalle.FechaCreacion = v_FechaHoraSistema;
			insertDetalle.IdEmpresa = in.p_IdEmpresa;
			insertDetalle.FechaIngreso = v_FechaIngreso;
			insertDetalle.FechaModificacion = v_FechaHoraSistema;
			insertDetalle.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertDetalle.IdUsuarioCreador = in.p_IdUsuario;

			sql.batch(conn, SQL_INSERT_DETALLE, java.util.Arrays.asList(insertDetalle), InsertDetalle.class);

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
				// Obtener glosa para el evento
				logger.debug("Obtener glosa del perfil");
				java.util.HashMap<String, Object> params = new java.util.HashMap<>();
				params.put("IdPerfilProductos", in.p_IdPerfilProductos);
				params.put("IdTipoProducto", in.p_IdTipoProducto);

				List<GlosaResult> glosaList = sql.query(conn, SQL_SELECT_GLOSA, params, GlosaResult.class);
				String v_Glosa = (glosaList != null && !glosaList.isEmpty()) ? glosaList.get(0).DescripcionPerfilProductos : "";

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
				crearEvtIn.p_NumeroOperacionConcepto = String.valueOf(v_IdDetallePerfilProductos);
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

				// Si hay errores, retornar
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}

				// Agregar advertencia de autorización requerida
				String mensajeAutorizacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String, String>());
				salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
				salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();

			} else {
				// Confirma la creación directamente
				logger.debug("Confirmar creación de detalle");
				v_IdEstadoAutorizacion = "ING";

				ConfirmarCrearPerfilProductoDetalle.EntradaConfirmarCrearPerfilProductoDetalle confirmarIn = new ConfirmarCrearPerfilProductoDetalle.EntradaConfirmarCrearPerfilProductoDetalle();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdDetallePerfilProductos = v_IdDetallePerfilProductos;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearPerfilProductoDetalle confirmarWrapper = new ConfirmarCrearPerfilProductoDetalle();
				ConfirmarCrearPerfilProductoDetalle.SalidaConfirmarCrearPerfilProductoDetalle confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				for (ConfirmarCrearPerfilProductoDetalle.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				for (ConfirmarCrearPerfilProductoDetalle.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

		} catch (Exception ex) {
			logger.error("Error en CrearPerfilProductoDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL_PRODUCTO_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfilProductoDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfilProductoDetalle in = gson.fromJson(jin.toString(), EntradaCrearPerfilProductoDetalle.class);
		SalidaCrearPerfilProductoDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPerfilProductos\":\"PRUEBA01\",\"p_IdTipoProducto\":\"EVCA\",\"p_IdTipoOperacion\":\"0\",\"p_IdCanalIngreso\":\"CLI\",\"p_IdMoneda\":\"0\",\"p_IdGrupoActivos\":\"AGRESIVO\",\"p_IdEstado\":\"ACT\",\"p_PermiteRuteo\":\"N\"}";
			EntradaCrearPerfilProductoDetalle entrada = gson.fromJson(json, EntradaCrearPerfilProductoDetalle.class);
			CrearPerfilProductoDetalle demo = new CrearPerfilProductoDetalle();
			SalidaCrearPerfilProductoDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
