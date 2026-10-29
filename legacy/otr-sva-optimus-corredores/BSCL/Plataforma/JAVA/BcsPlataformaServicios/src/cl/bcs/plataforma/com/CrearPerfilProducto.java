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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPerfilProducto implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPerfilProducto.class);

	private final String SQL_INSERT_PERFIL = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProducto_Insert_Perfil.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProducto_Select_RequiereAutorizacion.sql");
	private final String SQL_SELECT_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/CrearPerfilProducto_Select_Count_Existe.sql");

	public static class EntradaCrearPerfilProducto {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilProductos;
		public String p_DescripcionPerfilProductos;
		public String p_IdCategoriaInversionista;
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

	public static class SalidaCrearPerfilProducto {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPerfilProducto() {
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
		public String IdPerfilProductos;
		public String DescripcionPerfilProductos;
		public String IdCategoriaInversionista;
		public String EsPerfilPersonalizado;
		public String TipoEntidadReferencia;
		public Long IdReferencia;
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
	public static class Contador {
		public Long Contador;
	}
	public SalidaCrearPerfilProducto logica(Connection conn, EntradaCrearPerfilProducto in) throws Exception {
		logger.info("INIT CrearPerfilProducto");
		SalidaCrearPerfilProducto salida = new SalidaCrearPerfilProducto();
		SQL sql = new SQL();

		try {
			// Validar si el perfil de productos ya existe
			logger.debug("Validar si perfil de productos ya existe");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdPerfilProductos", in.p_IdPerfilProductos);

			List<Contador> countResult = sql.query(conn, SQL_SELECT_COUNT_EXISTE, paramsExiste, Contador.class);
			Long v_countExiste = (countResult != null && !countResult.isEmpty()) ? countResult.get(0).Contador : 0L;

			if (v_countExiste > 0) {
				logger.warn("Perfil de productos ya existe");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PERFIL_PRODUCTOS_EXISTE",
					"El perfil de productos '" + in.p_IdPerfilProductos + "' ya existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Obtener fecha del sistema
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_IdConcepto = "NCPPRO";
			String v_IdEstadoAutorizacion = "PEN";

			// Insertar perfil de productos
			logger.debug("INSERT perfil productos");
			InsertPerfil insertPerfil = new InsertPerfil();
			insertPerfil.IdPerfilProductos = in.p_IdPerfilProductos;
			insertPerfil.DescripcionPerfilProductos = in.p_DescripcionPerfilProductos;
			insertPerfil.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
			insertPerfil.EsPerfilPersonalizado = in.p_EsPerfilPersonalizado;
			insertPerfil.TipoEntidadReferencia = in.p_TipoEntidadReferencia != null ? in.p_TipoEntidadReferencia : "0";
			insertPerfil.IdReferencia = in.p_IdReferencia != null ? in.p_IdReferencia : 0L;
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
				crearEvtIn.p_NumeroOperacionConcepto = in.p_IdPerfilProductos;
				crearEvtIn.p_FechaOperacionConcepto = v_FechaIngreso;
				crearEvtIn.p_IdCliente = 0L;
				crearEvtIn.p_Glosa = in.p_DescripcionPerfilProductos;
				crearEvtIn.p_IdCanal = "OPT";

				CrearEventoAutorizacion crearEvtService = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceSalida crearEvtOut = crearEvtService.logica(conn, crearEvtIn);

				// Agregar advertencias y errores del evento
				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.p_out_AdvertenciasContador.intValue();
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : crearEvtOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.p_out_ErroresContador.intValue();
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : crearEvtOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

				// Advertir que el cambio quedó pendiente de autorización
				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					String mensajeAutorizacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String, String>());
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
					salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
				}

			} else {
				// Confirma la creación directamente
				logger.debug("Confirmar creación de perfil");
				v_IdEstadoAutorizacion = "ING";

				ConfirmarCrearPerfilProductoMasivo.Entrada confirmarIn = new ConfirmarCrearPerfilProductoMasivo.Entrada();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearPerfilProductoMasivo.PerfilProductoItem confirmarItem = new ConfirmarCrearPerfilProductoMasivo.PerfilProductoItem();
				confirmarItem.IdPerfilProductos = in.p_IdPerfilProductos;
				confirmarIn.p_PerfilProductos.add(confirmarItem);

				ConfirmarCrearPerfilProductoMasivo confirmarService = new ConfirmarCrearPerfilProductoMasivo();
				ConfirmarCrearPerfilProductoMasivo.Salida confirmarOut = confirmarService.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.P_OUT_ADVERTENCIASCONTADOR.intValue();
				for (ConfirmarCrearPerfilProductoMasivo.AdvertenciaDetalle adv : confirmarOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.P_OUT_ERRORESCONTADOR.intValue();
				for (ConfirmarCrearPerfilProductoMasivo.ErrorDetalle err : confirmarOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

			// Agregar log
			logger.debug("Agregar log de ingreso");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaIngreso;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "PERFIL PRODUCTO";
			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.SubRegistro = "INGRESO";
			detLog.CampoRegistro = "IdPerfilProductos";
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = in.p_IdPerfilProductos;
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearPerfilProducto: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERFIL_PRODUCTO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPerfilProducto");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPerfilProducto in = gson.fromJson(jin.toString(), EntradaCrearPerfilProducto.class);
		SalidaCrearPerfilProducto out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"COM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPerfilProductos\":\"PERFPROD001\",\"p_DescripcionPerfilProductos\":\"Perfil Test\",\"p_IdCategoriaInversionista\":\"CAT001\",\"p_EsPerfilPersonalizado\":\"N\",\"p_TipoEntidadReferencia\":\"0\",\"p_IdReferencia\":0,\"p_IdEstado\":\"ACT\",\"p_PorDefecto\":\"N\"}";
			EntradaCrearPerfilProducto entrada = gson.fromJson(json, EntradaCrearPerfilProducto.class);
			CrearPerfilProducto demo = new CrearPerfilProducto();
			SalidaCrearPerfilProducto salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
