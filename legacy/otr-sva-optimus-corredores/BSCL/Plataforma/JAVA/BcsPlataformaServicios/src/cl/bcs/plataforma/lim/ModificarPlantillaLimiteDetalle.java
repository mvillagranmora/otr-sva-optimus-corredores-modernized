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

public class ModificarPlantillaLimiteDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarPlantillaLimiteDetalle.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Count_Existe.sql");
	private final String SQL_SELECT_ID_BASE = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_IdBase.sql");
	private final String SQL_SELECT_VALORES_ANTIGUOS = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_ValoresAntiguos.sql");
	private final String SQL_SELECT_TIPOS_DATO = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_TiposDato.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/lim/sql/ModificarPlantillaLimiteDetalle_Select_Glosa.sql");

	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaModificarPlantillaLimiteDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdLimite;
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

	public static class SalidaModificarPlantillaLimiteDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaModificarPlantillaLimiteDetalle() {
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

	public static class IdBase {
		public String IdPlantillaLimite;
	}

	public static class ValorAntiguo {
		public String Nombre;
		public String IdMesa;
		public String IdCanalIngreso;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdInstrumento;
		public String UnidadPlazo;
		public String Plazo;
		public String IdTipoReajuste;
		public String CodigoOperador;
		public String MontoTotal;
		public String MontoIntraday;
		public String MontoOvernight;
		public String IdMoneda;
		public String IdMonedaCalculo;
		public String ClasificacionRiesgoMinima;
		public String IdEstado;
		public String EsPersonalizada;
		public String EsTotalizador;
		public String IdAgente;
		public String IdClasificacionTipoInstrumento;
		public String Porcentaje;
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
			case "Nombre": return valores.Nombre != null ? valores.Nombre : "";
			case "IdMesa": return valores.IdMesa != null ? valores.IdMesa : "";
			case "IdCanalIngreso": return valores.IdCanalIngreso != null ? valores.IdCanalIngreso : "";
			case "IdTipoInstrumento": return valores.IdTipoInstrumento != null ? valores.IdTipoInstrumento : "";
			case "IdTipoOperacion": return valores.IdTipoOperacion != null ? valores.IdTipoOperacion : "";
			case "IdTipoProducto": return valores.IdTipoProducto != null ? valores.IdTipoProducto : "";
			case "IdInstrumento": return valores.IdInstrumento != null ? valores.IdInstrumento : "";
			case "UnidadPlazo": return valores.UnidadPlazo != null ? valores.UnidadPlazo : "";
			case "Plazo": return valores.Plazo != null ? valores.Plazo : "";
			case "IdTipoReajuste": return valores.IdTipoReajuste != null ? valores.IdTipoReajuste : "";
			case "CodigoOperador": return valores.CodigoOperador != null ? valores.CodigoOperador : "";
			case "MontoTotal": return valores.MontoTotal != null ? valores.MontoTotal : "";
			case "MontoIntraday": return valores.MontoIntraday != null ? valores.MontoIntraday : "";
			case "MontoOvernight": return valores.MontoOvernight != null ? valores.MontoOvernight : "";
			case "IdMoneda": return valores.IdMoneda != null ? valores.IdMoneda : "";
			case "IdMonedaCalculo": return valores.IdMonedaCalculo != null ? valores.IdMonedaCalculo : "";
			case "ClasificacionRiesgoMinima": return valores.ClasificacionRiesgoMinima != null ? valores.ClasificacionRiesgoMinima : "";
			case "IdEstado": return valores.IdEstado != null ? valores.IdEstado : "";
			case "EsPersonalizada": return valores.EsPersonalizada != null ? valores.EsPersonalizada : "";
			case "EsTotalizador": return valores.EsTotalizador != null ? valores.EsTotalizador : "";
			case "IdAgente": return valores.IdAgente != null ? valores.IdAgente : "";
			case "IdClasificacionTipoInstrumento": return valores.IdClasificacionTipoInstrumento != null ? valores.IdClasificacionTipoInstrumento : "";
			case "Porcentaje": return valores.Porcentaje != null ? valores.Porcentaje : "";
			default: return "";
		}
	}

	public SalidaModificarPlantillaLimiteDetalle logica(Connection conn, EntradaModificarPlantillaLimiteDetalle in) throws Exception {
		logger.info("INIT ModificarPlantillaLimiteDetalle");
		SalidaModificarPlantillaLimiteDetalle salida = new SalidaModificarPlantillaLimiteDetalle();
		SQL sql = new SQL();

		try {
			Long v_IdEntidadOrigen = in.p_IdLimite;
			String v_TipoEntidad = "PLANTILLALIMITEDETALLE";
			String v_IdConcepto = "MDPLIM";
			String v_IdEstadoAutorizacion = "PEN";

			// Validar existencia
			logger.debug("Validar existencia de detalle límite");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdLimite", v_IdEntidadOrigen);

			List<CountExiste> existeList = sql.query(conn, SQL_COUNT_EXISTE, paramsExiste, CountExiste.class);
			if (existeList.isEmpty() || existeList.get(0).countEntidad == 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ENTIDAD_NO_EXISTE", "El detalle límite no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			// Obtener secuencia para IdSolicitudCambio
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long v_IdSolicitudCambio = secuencia.get(0);
			salida.P_OUT_REFID = v_IdSolicitudCambio;

			// Obtener IdBase (IdPlantillaLimite) para el evento
			logger.debug("Obtener IdBase");
			HashMap<String, Object> paramsBase = new HashMap<>();
			paramsBase.put("IdLimite", v_IdEntidadOrigen);

			List<IdBase> idBaseList = sql.query(conn, SQL_SELECT_ID_BASE, paramsBase, IdBase.class);
			String v_IdEntidadBase = idBaseList.isEmpty() ? "" : idBaseList.get(0).IdPlantillaLimite;

			// Obtener valores antiguos
			logger.debug("Obtener valores antiguos");
			HashMap<String, Object> paramsAntiguos = new HashMap<>();
			paramsAntiguos.put("IdLimite", v_IdEntidadOrigen);

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
				registro.IdEntidadOrigen = String.valueOf(v_IdEntidadOrigen);
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
				paramsGlosa.put("IdLimite", v_IdEntidadOrigen);

				List<SelectGlosa> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsGlosa, SelectGlosa.class);
				String v_Glosa = String.valueOf(v_IdEntidadOrigen);
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
				
				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				evento.IdTipoEvento = v_IdConcepto;
				evento.IdModuloSolicita = in.p_IdModulo;
				evento.IdUsuarioSolicita = in.p_IdUsuario;
				evento.Fecha = v_FechaIngreso;
				evento.IdTipoOperacion = "";
				evento.IdConcepto = v_IdConcepto;
				evento.NumeroOperacionConcepto = v_IdEntidadBase;
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
				logger.debug("Confirmar modificación de detalle límite");
				
				ConfirmarModificarPlantillaLimiteDetalle.EntradaConfirmarModificarPlantillaLimiteDetalle confirmarIn = new ConfirmarModificarPlantillaLimiteDetalle.EntradaConfirmarModificarPlantillaLimiteDetalle();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdLimite = v_IdEntidadOrigen;
				confirmarIn.p_IdEstadoAutorizacion = "ING";

				ConfirmarModificarPlantillaLimiteDetalle confirmarWrapper = new ConfirmarModificarPlantillaLimiteDetalle();
				ConfirmarModificarPlantillaLimiteDetalle.SalidaConfirmarModificarPlantillaLimiteDetalle confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				if (confirmarOut.p_out_AdvertenciasDetalle != null) {
					for (ConfirmarModificarPlantillaLimiteDetalle.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
						salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
					}
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				if (confirmarOut.p_out_ErroresDetalle != null) {
					for (ConfirmarModificarPlantillaLimiteDetalle.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
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
			log.IdTipoRegistro = "PLANTILLA LIMITE DETALLE";
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
			logger.error("Error en ModificarPlantillaLimiteDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_MODIFICAR_PLANTILLA_LIMITE_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ModificarPlantillaLimiteDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarPlantillaLimiteDetalle in = gson.fromJson(jin.toString(), EntradaModificarPlantillaLimiteDetalle.class);
		SalidaModificarPlantillaLimiteDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"LIM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdLimite\":12285,\"p_RegistroCambio\":[{\"CampoModificado\":\"IdClasificacionTipoInstrumento\",\"ValorNuevo\":\"FM\"},{\"CampoModificado\":\"IdMoneda\",\"ValorNuevo\":\"USD\"}]}";
			EntradaModificarPlantillaLimiteDetalle entrada = gson.fromJson(json, EntradaModificarPlantillaLimiteDetalle.class);
			ModificarPlantillaLimiteDetalle demo = new ModificarPlantillaLimiteDetalle();
			SalidaModificarPlantillaLimiteDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
