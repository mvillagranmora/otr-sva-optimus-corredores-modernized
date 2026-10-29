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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPerfilComercial implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarPerfilComercial.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarPerfilComercial_Count_Existe.sql");
	private final String SQL_SELECT_VALORES_ANTIGUOS = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarPerfilComercial_Select_ValoresAntiguos.sql");
	private final String SQL_UPDATE_PERFIL = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarPerfilComercial_Update_Perfil.sql");
	private final String SQL_UPDATE_POR_DEFECTO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarPerfilComercial_Update_PorDefecto.sql");

	public static class EntradaModificarPerfilComercial {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilComercialBase;
		public String p_DescripcionPerfilComercialBase;
		public String p_IdCategoriaInversionista;
		public String p_IdEstado;
		public String p_PorDefecto;
		public String p_IdPerfilProductos;
		public String p_IdPerfilCobros;
		public String p_IdPerfilCobrosPeru;
		public String p_IdPerfilLimites;
		public String p_IdPerfilCondicionesOperativas;
		public String p_IdModeloFacturacion;
		public String p_TipoEnrolamiento;  // DEFAULT 'FULL'

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

	public static class SalidaModificarPerfilComercial {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarPerfilComercial() {
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

	public static class ValorAntiguo {
		public String IdPerfilComercialBase;
		public String DescripcionPerfilComercialBase;
		public String IdCategoriaInversionista;
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public String IdPerfilCobrosPeru;
		public String IdPerfilLimites;
		public String IdPerfilCondicionesOperativas;
		public String IdModeloFacturacion;
		public java.sql.Date FechaModificacion;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
		public String TipoEnrolamiento;
	}

	public static class UpdatePerfil {
		public String DescripcionPerfilComercialBase;
		public String IdCategoriaInversionista;
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public String IdPerfilCobrosPeru;
		public String IdPerfilLimites;
		public String IdPerfilCondicionesOperativas;
		public String IdModeloFacturacion;
		public String FechaModificacion;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
		public String TipoEnrolamiento;
		public String IdPerfilComercialBase;
	}

	public static class UpdatePorDefecto {
		public String IdCategoriaInversionista;
		public String TipoEnrolamiento;
		public String IdPerfilComercialBase;
		public Long IdEmpresa;
	}

	private String obtenerValorAntiguo(ValorAntiguo valores, String campo) {
		if (valores == null || campo == null) return "";
		
		switch (campo) {
			case "DescripcionPerfilComercialBase":
				return valores.DescripcionPerfilComercialBase != null ? valores.DescripcionPerfilComercialBase : "";
			case "IdCategoriaInversionista":
				return valores.IdCategoriaInversionista != null ? valores.IdCategoriaInversionista : "";
			case "IdPerfilProductos":
				return valores.IdPerfilProductos != null ? valores.IdPerfilProductos : "";
			case "IdPerfilCobros":
				return valores.IdPerfilCobros != null ? valores.IdPerfilCobros : "";
			case "IdPerfilCobrosPeru":
				return valores.IdPerfilCobrosPeru != null ? valores.IdPerfilCobrosPeru : "";
			case "IdPerfilLimites":
				return valores.IdPerfilLimites != null ? valores.IdPerfilLimites : "";
			case "IdPerfilCondicionesOperativas":
				return valores.IdPerfilCondicionesOperativas != null ? valores.IdPerfilCondicionesOperativas : "";
			case "IdModeloFacturacion":
				return valores.IdModeloFacturacion != null ? valores.IdModeloFacturacion : "";
			case "IdEstado":
				return valores.IdEstado != null ? valores.IdEstado : "";
			case "PorDefecto":
				return valores.PorDefecto != null ? valores.PorDefecto : "";
			case "TipoEnrolamiento":
				return valores.TipoEnrolamiento != null ? valores.TipoEnrolamiento : "";
			default:
				return "";
		}
	}

	public SalidaModificarPerfilComercial logica(Connection conn, EntradaModificarPerfilComercial in) throws Exception {
		logger.info("INIT ModificarPerfilComercial");
		SalidaModificarPerfilComercial salida = new SalidaModificarPerfilComercial();
		SQL sql = new SQL();

		try {
			// Aplicar DEFAULT para TipoEnrolamiento
			String v_TipoEnrolamiento = (in.p_TipoEnrolamiento != null && !in.p_TipoEnrolamiento.trim().isEmpty()) 
				? in.p_TipoEnrolamiento 
				: "FULL";

			// Validar existencia del perfil
			logger.debug("Validar existencia del perfil comercial");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdPerfilComercialBase", in.p_IdPerfilComercialBase);

			List<CountExiste> existeList = sql.query(conn, SQL_COUNT_EXISTE, paramsExiste, CountExiste.class);
			if (existeList.isEmpty() || existeList.get(0).countEntidad == 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PERFIL_NO_EXISTE", "El perfil comercial no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Validar si es solo simulación
			if ("S".equals(in.p_simularOperacion)) {
				logger.debug("Operación simulada - saliendo sin cambios");
				return salida;
			}

			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());

			// Obtener valores antiguos para el log
			logger.debug("Obtener valores antiguos");
			HashMap<String, Object> paramsAntiguos = new HashMap<>();
			paramsAntiguos.put("IdPerfilComercialBase", in.p_IdPerfilComercialBase);

			List<ValorAntiguo> valoresAntiguos = sql.query(conn, SQL_SELECT_VALORES_ANTIGUOS, paramsAntiguos, ValorAntiguo.class);
			ValorAntiguo valorAntiguo = valoresAntiguos.isEmpty() ? new ValorAntiguo() : valoresAntiguos.get(0);

			// Actualizar perfil comercial
			logger.debug("UPDATE perfil comercial");
			UpdatePerfil updatePerfil = new UpdatePerfil();
			updatePerfil.DescripcionPerfilComercialBase = in.p_DescripcionPerfilComercialBase;
			updatePerfil.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
			updatePerfil.IdPerfilProductos = in.p_IdPerfilProductos;
			updatePerfil.IdPerfilCobros = in.p_IdPerfilCobros;
			updatePerfil.IdPerfilCobrosPeru = in.p_IdPerfilCobrosPeru;
			updatePerfil.IdPerfilLimites = in.p_IdPerfilLimites;
			updatePerfil.IdPerfilCondicionesOperativas = in.p_IdPerfilCondicionesOperativas;
			updatePerfil.IdModeloFacturacion = in.p_IdModeloFacturacion;
			updatePerfil.FechaModificacion = v_FechaHoraSistema;
			updatePerfil.IdEstado = in.p_IdEstado;
			updatePerfil.PorDefecto = in.p_PorDefecto;
			updatePerfil.IdEmpresa = in.p_IdEmpresa;
			updatePerfil.TipoEnrolamiento = v_TipoEnrolamiento;
			updatePerfil.IdPerfilComercialBase = in.p_IdPerfilComercialBase;

			sql.batch(conn, SQL_UPDATE_PERFIL, java.util.Arrays.asList(updatePerfil), UpdatePerfil.class);
			salida.P_OUT_REFID = 1L;

			// Si es por defecto, desactivar otros perfiles por defecto de la misma categoría y tipo enrolamiento
			if ("S".equals(in.p_PorDefecto)) {
				logger.debug("Actualizar otros perfiles por defecto a N");
				UpdatePorDefecto updatePorDefecto = new UpdatePorDefecto();
				updatePorDefecto.IdCategoriaInversionista = in.p_IdCategoriaInversionista;
				updatePorDefecto.TipoEnrolamiento = v_TipoEnrolamiento;
				updatePorDefecto.IdPerfilComercialBase = in.p_IdPerfilComercialBase;
				updatePorDefecto.IdEmpresa = in.p_IdEmpresa;

				sql.batch(conn, SQL_UPDATE_POR_DEFECTO, java.util.Arrays.asList(updatePorDefecto), UpdatePorDefecto.class);
			}

			// Agregar log de cambios
			logger.debug("Agregar log de modificación");
			
			// Obtener valores nuevos después del UPDATE
			List<ValorAntiguo> valoresNuevos = sql.query(conn, SQL_SELECT_VALORES_ANTIGUOS, paramsAntiguos, ValorAntiguo.class);
			ValorAntiguo valorNuevo = valoresNuevos.isEmpty() ? new ValorAntiguo() : valoresNuevos.get(0);

			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "ACTUALIZACION";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaHoraSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "MODELO COMERCIAL";
			entradaAgregarLog.p_Log.add(log);

			// Comparar y agregar detalles de cambios
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "DescripcionPerfilComercialBase", 
				obtenerValorAntiguo(valorAntiguo, "DescripcionPerfilComercialBase"),
				obtenerValorAntiguo(valorNuevo, "DescripcionPerfilComercialBase"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdCategoriaInversionista",
				obtenerValorAntiguo(valorAntiguo, "IdCategoriaInversionista"),
				obtenerValorAntiguo(valorNuevo, "IdCategoriaInversionista"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdPerfilProductos",
				obtenerValorAntiguo(valorAntiguo, "IdPerfilProductos"),
				obtenerValorAntiguo(valorNuevo, "IdPerfilProductos"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdPerfilCobros",
				obtenerValorAntiguo(valorAntiguo, "IdPerfilCobros"),
				obtenerValorAntiguo(valorNuevo, "IdPerfilCobros"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdPerfilCobrosPeru",
				obtenerValorAntiguo(valorAntiguo, "IdPerfilCobrosPeru"),
				obtenerValorAntiguo(valorNuevo, "IdPerfilCobrosPeru"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdPerfilLimites",
				obtenerValorAntiguo(valorAntiguo, "IdPerfilLimites"),
				obtenerValorAntiguo(valorNuevo, "IdPerfilLimites"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdPerfilCondicionesOperativas",
				obtenerValorAntiguo(valorAntiguo, "IdPerfilCondicionesOperativas"),
				obtenerValorAntiguo(valorNuevo, "IdPerfilCondicionesOperativas"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdModeloFacturacion",
				obtenerValorAntiguo(valorAntiguo, "IdModeloFacturacion"),
				obtenerValorAntiguo(valorNuevo, "IdModeloFacturacion"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "IdEstado",
				obtenerValorAntiguo(valorAntiguo, "IdEstado"),
				obtenerValorAntiguo(valorNuevo, "IdEstado"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "PorDefecto",
				obtenerValorAntiguo(valorAntiguo, "PorDefecto"),
				obtenerValorAntiguo(valorNuevo, "PorDefecto"));
			
			agregarDetalleLogSiCambio(entradaAgregarLog, in.p_IdPerfilComercialBase, "TipoEnrolamiento",
				obtenerValorAntiguo(valorAntiguo, "TipoEnrolamiento"),
				obtenerValorAntiguo(valorNuevo, "TipoEnrolamiento"));

			// Solo llamar a AgregarLog si hay cambios
			if (!entradaAgregarLog.p_LogDetalle.isEmpty()) {
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
			}

		} catch (Exception ex) {
			logger.error("Error en ModificarPerfilComercial: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_PERFIL_COMERCIAL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ModificarPerfilComercial");
		return salida;
	}

	private void agregarDetalleLogSiCambio(AgregarLog.InAgregarLog entradaLog, String subRegistro, String campo, String valorAntiguo, String valorNuevo) {
		// Solo agregar al log si hubo cambio
		if (valorAntiguo == null) valorAntiguo = "";
		if (valorNuevo == null) valorNuevo = "";
		
		if (!valorAntiguo.equals(valorNuevo)) {
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = subRegistro;
			detLog.CampoRegistro = campo;
			detLog.ValorAntiguo = valorAntiguo;
			detLog.ValorNuevo = valorNuevo;
			entradaLog.p_LogDetalle.add(detLog);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarPerfilComercial in = gson.fromJson(jin.toString(), EntradaModificarPerfilComercial.class);
		SalidaModificarPerfilComercial out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPerfilComercialBase\":\"TEST01\",\"p_DescripcionPerfilComercialBase\":\"TEST POST CAMBIO\",\"p_IdCategoriaInversionista\":\"ARRIESGADO\",\"p_IdEstado\":\"INA\",\"p_PorDefecto\":\"N\",\"p_IdPerfilProductos\":\"APV ARQ ABIERTA_ARRIESGADO\",\"p_IdPerfilCobros\":\"APV ARQ ABIERTA_CVA B_CONSERVADOR\",\"p_IdPerfilCobrosPeru\":\"\",\"p_IdPerfilLimites\":\"003\",\"p_IdModeloFacturacion\":\"0\",\"p_IdPerfilCondicionesOperativas\":\"0\",\"p_TipoEnrolamiento\":\"EXPRESS\"}";
			EntradaModificarPerfilComercial entrada = gson.fromJson(json, EntradaModificarPerfilComercial.class);
			ModificarPerfilComercial demo = new ModificarPerfilComercial();
			SalidaModificarPerfilComercial salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
