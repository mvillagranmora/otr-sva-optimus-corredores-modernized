package cl.bcs.plataforma.lim;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPlantillaLimite implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarPlantillaLimite.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimite_Count_Existe.sql");
	private final String SQL_SELECT_VALORES_ANTIGUOS = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimite_Select_ValoresAntiguos.sql");
	private final String SQL_SELECT_TIPOS_DATO = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimite_Select_TiposDato.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimite_Select_Glosa.sql");

	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaModificarPlantillaLimite {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPlantillaLimite;
		public List<RegistroCambio> p_RegistroCambio;

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

	public static class SalidaModificarPlantillaLimite {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarPlantillaLimite() {
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
		public String DescripcionPlantillaLimite;
		public String IdEstado;
	}

	public static class TipoDato {
		public String CampoModificado;
		public String TipoDato;
	}

	public static class SelectGlosa {
		public String DescripcionPlantillaLimite;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	private String obtenerValorAntiguo(ValorAntiguo valores, String campo) {
		if (valores == null || campo == null) return "";
		
		switch (campo) {
			case "DescripcionPlantillaLimite":
				return valores.DescripcionPlantillaLimite != null ? valores.DescripcionPlantillaLimite : "";
			case "IdEstado":
				return valores.IdEstado != null ? valores.IdEstado : "";
			default:
				return "";
		}
	}

	public SalidaModificarPlantillaLimite logica(Connection conn, EntradaModificarPlantillaLimite in) throws Exception {
		logger.info("INIT ModificarPlantillaLimite");
		SalidaModificarPlantillaLimite salida = new SalidaModificarPlantillaLimite();
		SQL sql = new SQL();

		try {
			String v_IdEntidadOrigen = in.p_IdPlantillaLimite;
			String v_TipoEntidad = "PLANTILLALIMITE";
			String v_IdConcepto = "MCPLIM";
			String v_IdEstadoAutorizacion = "PEN";

			// Validar existencia
			logger.debug("Validar existencia de plantilla límite");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdPlantillaLimite", v_IdEntidadOrigen);

			List<CountExiste> existeList = sql.query(conn, SQL_COUNT_EXISTE, paramsExiste, CountExiste.class);
			if (existeList.isEmpty() || existeList.get(0).countEntidad == 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ENTIDAD_NO_EXISTE", "La plantilla límite no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			// Obtener secuencia para IdSolicitudCambio
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long v_IdSolicitudCambio = secuencia.get(0);
			salida.P_OUT_REFID = v_IdSolicitudCambio;

			// Obtener valores antiguos
			logger.debug("Obtener valores antiguos");
			HashMap<String, Object> paramsAntiguos = new HashMap<>();
			paramsAntiguos.put("IdPlantillaLimite", v_IdEntidadOrigen);

			List<ValorAntiguo> valoresAntiguos = sql.query(conn, SQL_SELECT_VALORES_ANTIGUOS, paramsAntiguos, ValorAntiguo.class);
			ValorAntiguo valorAntiguo = valoresAntiguos.isEmpty() ? new ValorAntiguo() : valoresAntiguos.get(0);

			// Obtener tipos de dato
			logger.debug("Obtener tipos de dato");
			List<TipoDato> tiposDato = sql.query(conn, SQL_SELECT_TIPOS_DATO, new HashMap<>(), TipoDato.class);
			HashMap<String, String> mapaTipos = new HashMap<>();
			for (TipoDato tipo : tiposDato) {
				mapaTipos.put(tipo.CampoModificado, tipo.TipoDato);
			}

			// Construir lista de SolicitudCambio
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registrosSolicitud = new ArrayList<>();
			
			for (RegistroCambio cambio : in.p_RegistroCambio) {
				CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registro.IdSolicitudCambio = v_IdSolicitudCambio;
				registro.IdTipoEntidad = v_TipoEntidad;
				registro.IdEntidadOrigen = v_IdEntidadOrigen;
				registro.CampoModificado = cambio.CampoModificado;
				registro.TipoDato = mapaTipos.get(cambio.CampoModificado);
				registro.ValorAntiguo = obtenerValorAntiguo(valorAntiguo, cambio.CampoModificado);
				registro.ValorNuevo = cambio.ValorNuevo;
				registro.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
				
				registrosSolicitud.add(registro);
			}

			// Llamar a CrearRegistroSolicitudCambio
			logger.debug("Crear registros de solicitud de cambio");
			CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio solicitudIn = new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
			solicitudIn.p_RegistroSolicitudCambio = registrosSolicitud;

			CrearRegistroSolicitudCambio solicitudService = new CrearRegistroSolicitudCambio();
			solicitudService.logica(conn, solicitudIn);

			// Consultar si requiere autorización
			logger.debug("Consultando si requiere autorización");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, v_IdConcepto);

			if ("S".equals(v_RequiereAutorizacion)) {
				// Obtener glosa
				HashMap<String, Object> paramsGlosa = new HashMap<>();
				paramsGlosa.put("IdPlantillaLimite", v_IdEntidadOrigen);

				List<SelectGlosa> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsGlosa, SelectGlosa.class);
				String v_Glosa = v_IdEntidadOrigen;
				if (!glosaList.isEmpty() && glosaList.get(0).DescripcionPlantillaLimite != null) {
					v_Glosa = glosaList.get(0).DescripcionPlantillaLimite;
				}

				// Generar evento de autorización MASIVO (igual que el SP)
				logger.debug("Crear evento de autorización masivo");
				
				CrearEventoAutorizacionMasivo.ServiceEntrada crearEvtIn = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				crearEvtIn.P_IDEMPRESA = in.p_IdEmpresa;
				crearEvtIn.P_IDUSUARIO = in.p_IdUsuario;
				crearEvtIn.P_IDMODULO = in.p_IdModulo;
				crearEvtIn.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
				crearEvtIn.P_SIMULAROPERACION = "N";
				
				// Crear el evento para la tabla
				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				evento.IdTipoEvento = v_IdConcepto;
				evento.IdModuloSolicita = in.p_IdModulo;
				evento.IdUsuarioSolicita = in.p_IdUsuario;
				evento.Fecha = v_FechaIngreso;
				evento.IdTipoOperacion = "";
				evento.IdConcepto = v_IdConcepto;
				evento.NumeroOperacionConcepto = v_IdEntidadOrigen;
				evento.FechaOperacionConcepto = v_FechaIngreso;
				evento.IdCliente = 0L;
				evento.IdFuncionario = 0L;
				evento.IdProveedor = 0L;
				evento.IdTipoProducto = "";
				evento.IdClasificacionTipoInstrumento = "";
				evento.IdTipoInstrumento = "";
				evento.IdInstrumento = 0L;
				evento.Nemo = "";
				evento.Cantidad = 0L;
				evento.Monto = java.math.BigDecimal.ZERO;
				evento.Precio = java.math.BigDecimal.ZERO;
				evento.Tasa = java.math.BigDecimal.ZERO;
				evento.IdMoneda = "";
				evento.Glosa = v_Glosa;
				evento.IdUnion = 0L;
				
				crearEvtIn.P_TABLAEVENTOS.add(evento);
				
				CrearEventoAutorizacionMasivo crearEvtWrapper = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceSalida crearEvtOut = crearEvtWrapper.logica(conn, crearEvtIn);

				// Agregar advertencias y errores del evento
				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.P_OUT_ADVERTENCIASCONTADOR.intValue();
				for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE adv : crearEvtOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.P_OUT_ERRORESCONTADOR.intValue();
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE err : crearEvtOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

			} else {
				// Confirmar modificación directamente
				logger.debug("Confirmar modificación de plantilla límite");
				
				ConfirmarModificarPlantillaLimite.EntradaConfirmarModificarPlantillaLimite confirmarIn = new ConfirmarModificarPlantillaLimite.EntradaConfirmarModificarPlantillaLimite();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdPlantillaLimite = v_IdEntidadOrigen;
				confirmarIn.p_IdEstadoAutorizacion = "ING";

				ConfirmarModificarPlantillaLimite confirmarWrapper = new ConfirmarModificarPlantillaLimite();
				ConfirmarModificarPlantillaLimite.SalidaConfirmarModificarPlantillaLimite confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				if (confirmarOut.p_out_AdvertenciasDetalle != null) {
					for (ConfirmarModificarPlantillaLimite.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
						salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
					}
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				if (confirmarOut.p_out_ErroresDetalle != null) {
					for (ConfirmarModificarPlantillaLimite.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
					}
				}
			}

			// Agregar log
			logger.debug("Agregar log de modificación");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaIngreso;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "PLANTILLA LIMITE";
			entradaAgregarLog.p_Log.add(log);

			for (RegistroCambio cambio : in.p_RegistroCambio) {
				AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdCabeceraLog = 1L;
				detLog.IdTrabajo = 0L;
				detLog.SubRegistro = "MODIFICACION";
				detLog.CampoRegistro = cambio.CampoModificado;
				detLog.ValorAntiguo = obtenerValorAntiguo(valorAntiguo, cambio.CampoModificado);
				detLog.ValorNuevo = cambio.ValorNuevo;
				entradaAgregarLog.p_LogDetalle.add(detLog);
			}

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en ModificarPlantillaLimite: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_PLANTILLA_LIMITE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ModificarPlantillaLimite");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarPlantillaLimite in = gson.fromJson(jin.toString(), EntradaModificarPlantillaLimite.class);
		SalidaModificarPlantillaLimite out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"LIM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPlantillaLimite\":\"TEST1\",\"p_RegistroCambio\":[{\"CampoModificado\":\"DescripcionPlantillaLimite\",\"ValorNuevo\":\"TESTEOS COMPLETOS\"}]}";
			EntradaModificarPlantillaLimite entrada = gson.fromJson(json, EntradaModificarPlantillaLimite.class);
			ModificarPlantillaLimite demo = new ModificarPlantillaLimite();
			SalidaModificarPlantillaLimite salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
