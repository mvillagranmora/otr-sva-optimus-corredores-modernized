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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPlantillaLimite implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPlantillaLimite.class);

	private final String SQL_COUNT_EXISTE = loadResourceToString("cl/bcs/plataforma/lim/sql/CrearPlantillaLimite_Count_Existe.sql");
	private final String SQL_INSERT_PLANTILLA = loadResourceToString("cl/bcs/plataforma/lim/sql/CrearPlantillaLimite_Insert_Plantilla.sql");

	public static class EntradaCrearPlantillaLimite {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPlantillaLimite;
		public String p_DescripcionPlantillaLimite;
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

	public static class SalidaCrearPlantillaLimite {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPlantillaLimite() {
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

	public static class InsertPlantilla {
		public String IdPlantillaLimite;
		public String DescripcionPlantillaLimite;
		public String EsPerfilPersonalizado;
		public String FechaCreacion;
		public String FechaModificacion;
		public String FechaIngreso;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public String IdEstado;
		public String PorDefecto;
		public Long IdEmpresa;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaCrearPlantillaLimite logica(Connection conn, EntradaCrearPlantillaLimite in) throws Exception {
		logger.info("INIT CrearPlantillaLimite");
		SalidaCrearPlantillaLimite salida = new SalidaCrearPlantillaLimite();
		SQL sql = new SQL();

		try {
			String v_IdPlantillaLimite = in.p_IdPlantillaLimite.toUpperCase();
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_IdConcepto = "NCPLIM";
			String v_IdEstadoAutorizacion = "PEN";

			// Aplicar DEFAULT para IdEstado si viene null (igual que el SP)
			String v_IdEstado = (in.p_IdEstado != null && !in.p_IdEstado.trim().isEmpty()) ? in.p_IdEstado : "ACT";
			String v_PorDefecto = (in.p_PorDefecto != null && !in.p_PorDefecto.trim().isEmpty()) ? in.p_PorDefecto : "N";

			logger.debug("VALORES RECIBIDOS - IdEstado: " + in.p_IdEstado + " | PorDefecto: " + in.p_PorDefecto);
			logger.debug("VALORES A INSERTAR - IdEstado: " + v_IdEstado + " | PorDefecto: " + v_PorDefecto);

			// Validar que la plantilla no exista
			logger.debug("Validar que la plantilla no exista");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdPlantillaLimite", v_IdPlantillaLimite);

			List<CountExiste> existeList = sql.query(conn, SQL_COUNT_EXISTE, paramsExiste, CountExiste.class);
			if (!existeList.isEmpty() && existeList.get(0).countEntidad > 0) {
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PLANTILLA_EXISTE", "La plantilla límite ya existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Insertar plantilla límite
			logger.debug("INSERT plantilla límite");
			InsertPlantilla insertPlantilla = new InsertPlantilla();
			insertPlantilla.IdPlantillaLimite = v_IdPlantillaLimite;
			insertPlantilla.DescripcionPlantillaLimite = in.p_DescripcionPlantillaLimite.toUpperCase();
			insertPlantilla.EsPerfilPersonalizado = "N";
			insertPlantilla.FechaCreacion = v_FechaHoraSistema;
			insertPlantilla.FechaModificacion = v_FechaHoraSistema;
			insertPlantilla.FechaIngreso = v_FechaIngreso;
			insertPlantilla.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertPlantilla.IdUsuarioCreador = in.p_IdUsuario;
			insertPlantilla.IdEstado = v_IdEstado;  // Usar variable con DEFAULT
			insertPlantilla.PorDefecto = v_PorDefecto;  // Usar variable con DEFAULT
			insertPlantilla.IdEmpresa = in.p_IdEmpresa;

			sql.batch(conn, SQL_INSERT_PLANTILLA, java.util.Arrays.asList(insertPlantilla), InsertPlantilla.class);

			salida.P_OUT_REFID = 1L;

			// Consultar si requiere autorización
			logger.debug("Consultando si requiere autorización");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, v_IdConcepto);

			if ("S".equals(v_RequiereAutorizacion)) {
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
				evento.NumeroOperacionConcepto = v_IdPlantillaLimite;
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
				evento.Glosa = in.p_DescripcionPlantillaLimite;
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
				// Confirma la creación directamente
				logger.debug("Confirmar creación de plantilla límite");
				v_IdEstadoAutorizacion = "ING";

				ConfirmarCrearPlantillaLimite.EntradaConfirmarCrearPlantillaLimite confirmarIn = new ConfirmarCrearPlantillaLimite.EntradaConfirmarCrearPlantillaLimite();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdPlantillaLimite = v_IdPlantillaLimite;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearPlantillaLimite confirmarWrapper = new ConfirmarCrearPlantillaLimite();
				ConfirmarCrearPlantillaLimite.SalidaConfirmarCrearPlantillaLimite confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				if (confirmarOut.p_out_AdvertenciasDetalle != null) {
					for (ConfirmarCrearPlantillaLimite.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
						salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
					}
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				if (confirmarOut.p_out_ErroresDetalle != null) {
					for (ConfirmarCrearPlantillaLimite.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
					}
				}
			}

			// Agregar log
			logger.debug("Agregar log de ingreso");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "PLA";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaIngreso;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "PLANTILLA LIMITE";
			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = "INGRESO";
			detLog.CampoRegistro = "IdPlantillaLimite";
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = v_IdPlantillaLimite;
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearPlantillaLimite: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PLANTILLA_LIMITE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPlantillaLimite");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPlantillaLimite in = gson.fromJson(jin.toString(), EntradaCrearPlantillaLimite.class);
		SalidaCrearPlantillaLimite out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"LIM\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPlantillaLimite\":\"TEST12\",\"p_DescripcionPlantillaLimite\":\"TESTNADAMAS\",\"p_IdEstado\":\"ACT\",\"p_PorDefecto\":\"N\"}";
			EntradaCrearPlantillaLimite entrada = gson.fromJson(json, EntradaCrearPlantillaLimite.class);
			CrearPlantillaLimite demo = new CrearPlantillaLimite();
			SalidaCrearPlantillaLimite salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
