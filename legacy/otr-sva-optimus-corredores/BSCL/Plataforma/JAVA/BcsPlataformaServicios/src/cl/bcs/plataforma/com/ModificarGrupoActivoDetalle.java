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
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarGrupoActivoDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarGrupoActivoDetalle.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Count_Existe.sql");
	private final String SQL_SELECT_ID_BASE = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Select_IdBase.sql");
	private final String SQL_SELECT_VALORES_ANTIGUOS = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Select_ValoresAntiguos.sql");
	private final String SQL_SELECT_TIPOS_DATO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Select_TiposDato.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Select_Glosa.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarGrupoActivoDetalle_Select_RequiereAutorizacion.sql");

	public static class EntradaModificarGrupoActivoDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDetalleGrupoActivos;
		public List<RegistroCambio> p_RegistroCambio;

		public EntradaModificarGrupoActivoDetalle() {
			this.p_RegistroCambio = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;
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

	public static class SalidaModificarGrupoActivoDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarGrupoActivoDetalle() {
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

	public static class Contador {
		public Long Contador;
	}

	public static class IdBase {
		public Long IdDetalleGrupoActivos;
	}

	public static class ValorAntiguo {
		public String IdInstrumento;
		public String IdEstado;
	}

	public static class TipoDato {
		public String DATA_TYPE_NAME;
		public String COLUMN_NAME;
		public String CampoModificado;
		public String ValorNuevo;
	}

	public static class GlosaResult {
		public String DescripcionGrupoActivos;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaModificarGrupoActivoDetalle logica(Connection conn, EntradaModificarGrupoActivoDetalle in) throws Exception {
		logger.info("INIT ModificarGrupoActivoDetalle");
		SalidaModificarGrupoActivoDetalle salida = new SalidaModificarGrupoActivoDetalle();
		SQL sql = new SQL();

		try {
			Long v_IdEntidadOrigen = in.p_IdDetalleGrupoActivos;
			Long v_IdEntidadBase = 0L;
			String v_tipoEntidad = "GRUPOACTIVODETALLE";
			String v_IdConcepto = "MDGACT";
			String v_estadoAutorizacion = "PEN";

			// Validar existencia
			logger.debug("Validar existencia de detalle");
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdDetalleGrupoActivos", v_IdEntidadOrigen);

			List<Contador> countList = sql.query(conn, SQL_COUNT_EXISTE, params, Contador.class);
			Long v_countEntidad = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;

			if (v_countEntidad == 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ENTIDAD_NO_EXISTE", "El detalle del grupo activos no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Obtener ID base
			logger.debug("Obtener IdDetalleGrupoActivos base");
			List<IdBase> idBaseList = sql.query(conn, SQL_SELECT_ID_BASE, params, IdBase.class);
			v_IdEntidadBase = (idBaseList != null && !idBaseList.isEmpty()) ? idBaseList.get(0).IdDetalleGrupoActivos : 0L;

			// Generar secuencia para solicitud de cambio
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long v_secuencia_IdSolicitudCambio = secuencia.get(0);
			salida.P_OUT_REFID = v_secuencia_IdSolicitudCambio;

			// Obtener valores antiguos y tipos de dato
			logger.debug("Obtener valores antiguos");
			List<ValorAntiguo> valoresAntiguos = sql.query(conn, SQL_SELECT_VALORES_ANTIGUOS, params, ValorAntiguo.class);
			ValorAntiguo valorAntiguo = (valoresAntiguos != null && !valoresAntiguos.isEmpty()) ? valoresAntiguos.get(0) : new ValorAntiguo();

			// Obtener tipos de dato para cada campo modificado
			logger.debug("Obtener tipos de dato");
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registrosSolicitudCambio = new ArrayList<>();

			for (RegistroCambio cambio : in.p_RegistroCambio) {
				HashMap<String, Object> paramsTipo = new HashMap<>();
				paramsTipo.put("CampoModificado", cambio.CampoModificado);

				List<TipoDato> tiposList = sql.query(conn, SQL_SELECT_TIPOS_DATO, paramsTipo, TipoDato.class);
				TipoDato tipoDato = (tiposList != null && !tiposList.isEmpty()) ? tiposList.get(0) : null;

				if (tipoDato != null) {
					String valorAnt = obtenerValorAntiguo(tipoDato.COLUMN_NAME, valorAntiguo);

					CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
					registro.IdSolicitudCambio = v_secuencia_IdSolicitudCambio;
					registro.IdTipoEntidad = v_tipoEntidad;
					registro.IdEntidadOrigen = String.valueOf(v_IdEntidadOrigen);
					registro.CampoModificado = cambio.CampoModificado;
					registro.TipoDato = tipoDato.DATA_TYPE_NAME;
					registro.ValorAntiguo = valorAnt;
					registro.ValorNuevo = cambio.ValorNuevo;
					registro.IdEstadoAutorizacion = v_estadoAutorizacion;

					registrosSolicitudCambio.add(registro);
				}
			}

			if (registrosSolicitudCambio.isEmpty()) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SIN_CAMBIOS", "No se detectaron cambios para modificar"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Crear registros de solicitud de cambio
			logger.debug("Crear registros de solicitud de cambio");
			CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio entradaCrearSolicitud = new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
			entradaCrearSolicitud.p_RegistroSolicitudCambio = registrosSolicitudCambio;

			CrearRegistroSolicitudCambio crearSolicitudService = new CrearRegistroSolicitudCambio();
			crearSolicitudService.logica(conn, entradaCrearSolicitud);

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
				HashMap<String, Object> paramsGlosa = new HashMap<>();
				paramsGlosa.put("IdDetalleGrupoActivos", v_IdEntidadBase);

				List<GlosaResult> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsGlosa, GlosaResult.class);
				String v_Glosa = (glosaList != null && !glosaList.isEmpty()) ? glosaList.get(0).DescripcionGrupoActivos : "";

				String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

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
				crearEvtIn.p_NumeroOperacionConcepto = String.valueOf(v_IdEntidadOrigen);
				crearEvtIn.p_FechaOperacionConcepto = v_FechaIngreso;
				crearEvtIn.p_IdCliente = v_IdEntidadBase;
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
				// Confirmar modificación sin autorización
				logger.debug("Confirmar modificación sin autorización");

				ConfirmarModificarGrupoActivoDetalle.EntradaConfirmarModificarGrupoActivoDetalle confirmarIn = new ConfirmarModificarGrupoActivoDetalle.EntradaConfirmarModificarGrupoActivoDetalle();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdDetalleGrupoActivos = in.p_IdDetalleGrupoActivos;
				confirmarIn.p_IdEstadoAutorizacion = "ING";

				ConfirmarModificarGrupoActivoDetalle confirmar = new ConfirmarModificarGrupoActivoDetalle();
				ConfirmarModificarGrupoActivoDetalle.SalidaConfirmarModificarGrupoActivoDetalle confirmarOut = confirmar.logica(conn, confirmarIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.P_OUT_ADVERTENCIASCONTADOR;
				for (ConfirmarModificarGrupoActivoDetalle.AdvertenciaDetalle adv : confirmarOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}
				salida.P_OUT_ERRORESCONTADOR += confirmarOut.P_OUT_ERRORESCONTADOR;
				for (ConfirmarModificarGrupoActivoDetalle.ErrorDetalle err : confirmarOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

		} catch (Exception ex) {
			logger.error("Error en ModificarGrupoActivoDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_GRUPO_ACTIVOS_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ModificarGrupoActivoDetalle");
		return salida;
	}

	private String obtenerValorAntiguo(String columnName, ValorAntiguo valorAntiguo) {
		if (valorAntiguo == null) {
			return "";
		}

		switch (columnName) {
			case "IdInstrumento":
				return valorAntiguo.IdInstrumento != null ? valorAntiguo.IdInstrumento : "";
			case "IdEstado":
				return valorAntiguo.IdEstado != null ? valorAntiguo.IdEstado : "";
			default:
				return "";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarGrupoActivoDetalle in = gson.fromJson(jin.toString(), EntradaModificarGrupoActivoDetalle.class);
		SalidaModificarGrupoActivoDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetalleGrupoActivos\":1054,\"p_RegistroCambio\":[{\"CampoModificado\":\"IdEstado\",\"ValorNuevo\":\"INA\"}]}";
			EntradaModificarGrupoActivoDetalle entrada = gson.fromJson(json, EntradaModificarGrupoActivoDetalle.class);
			ModificarGrupoActivoDetalle demo = new ModificarGrupoActivoDetalle();
			SalidaModificarGrupoActivoDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
