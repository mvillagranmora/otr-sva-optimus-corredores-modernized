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

public class CrearPlantillaLimiteDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearPlantillaLimiteDetalle.class);

	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/plataforma/lim/sql/CrearPlantillaLimiteDetalle_Insert_Detalle.sql");
	private final String SQL_SELECT_GLOSA = loadResourceToString("cl/bcs/plataforma/lim/sql/CrearPlantillaLimiteDetalle_Select_Glosa.sql");

	public static class EntradaCrearPlantillaLimiteDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPlantillaLimite;
		public String p_IdTipoLimite;
		public String p_Nombre;
		public Long p_IdMesa;
		public String p_IdCanalIngreso;
		public String p_IdTipoInstrumento;
		public String p_IdTipoOperacion;
		public String p_IdTipoProducto;
		public Long p_IdInstrumento;
		public String p_UnidadPlazo;
		public Integer p_Plazo;
		public String p_IdTipoReajuste;
		public String p_CodigoOperador;
		public Double p_MontoTotal;
		public Double p_MontoIntraday;
		public Double p_MontoOvernight;
		public String p_IdMoneda;
		public String p_IdMonedaCalculo;
		public String p_ClasificacionRiesgoMinima;
		public String p_IdEstado;
		public String p_EsPersonalizada;
		public String p_EsTotalizador;
		public Long p_IdAgente;
		public String p_IdClasificacionTipoInstrumento;
		public Double p_Porcentaje;

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

	public static class SalidaCrearPlantillaLimiteDetalle {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPlantillaLimiteDetalle() {
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
		public Long IdLimite;
		public String IdPlantillaLimite;
		public String IdTipoLimite;
		public String Nombre;
		public Long IdMesa;
		public String IdCanalIngreso;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String UnidadPlazo;
		public Integer Plazo;
		public String IdTipoReajuste;
		public String CodigoOperador;
		public Double MontoTotal;
		public Double MontoIntraday;
		public Double MontoOvernight;
		public String IdMoneda;
		public String IdMonedaCalculo;
		public String ClasificacionRiesgoMinima;
		public String IdEstado;
		public String EsPersonalizada;
		public String EsTotalizador;
		public String FechaIngreso;
		public String FechaCreacion;
		public String FechaModificacion;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public String UI_Limite;
		public Long IdAgente;
		public String IdClasificacionTipoInstrumento;
		public Double Porcentaje;
	}

	public static class SelectGlosa {
		public String DescripcionPlantillaLimite;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public SalidaCrearPlantillaLimiteDetalle logica(Connection conn, EntradaCrearPlantillaLimiteDetalle in) throws Exception {
		logger.info("INIT CrearPlantillaLimiteDetalle");
		SalidaCrearPlantillaLimiteDetalle salida = new SalidaCrearPlantillaLimiteDetalle();
		SQL sql = new SQL();

		try {
			String v_FechaIngreso = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String v_FechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa.intValue());
			String v_IdConcepto = "NDPLIM";
			String v_IdEstadoAutorizacion = "PEN";

			// Obtener secuencia para IdLimite
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::LIM.IdLimite", 1L);
			Long v_IdLimite = secuencia.get(0);
			salida.P_OUT_REFID = v_IdLimite;

			// Insertar detalle de límite
			logger.debug("INSERT detalle límite");
			InsertDetalle insertDetalle = new InsertDetalle();
			insertDetalle.IdLimite = v_IdLimite;
			insertDetalle.IdPlantillaLimite = in.p_IdPlantillaLimite;
			insertDetalle.IdTipoLimite = in.p_IdTipoLimite != null ? in.p_IdTipoLimite : "";
			insertDetalle.Nombre = in.p_Nombre != null ? in.p_Nombre : "";
			insertDetalle.IdMesa = in.p_IdMesa != null ? in.p_IdMesa : 0L;
			insertDetalle.IdCanalIngreso = in.p_IdCanalIngreso != null ? in.p_IdCanalIngreso : "";
			insertDetalle.IdTipoInstrumento = in.p_IdTipoInstrumento != null ? in.p_IdTipoInstrumento : "";
			insertDetalle.IdTipoOperacion = in.p_IdTipoOperacion != null ? in.p_IdTipoOperacion : "";
			insertDetalle.IdTipoProducto = in.p_IdTipoProducto != null ? in.p_IdTipoProducto : "";
			insertDetalle.IdInstrumento = in.p_IdInstrumento != null ? in.p_IdInstrumento : 0L;
			insertDetalle.UnidadPlazo = in.p_UnidadPlazo != null ? in.p_UnidadPlazo : "D";
			insertDetalle.Plazo = in.p_Plazo != null ? in.p_Plazo : 0;
			insertDetalle.IdTipoReajuste = in.p_IdTipoReajuste != null ? in.p_IdTipoReajuste : "";
			insertDetalle.CodigoOperador = in.p_CodigoOperador != null ? in.p_CodigoOperador : "";
			insertDetalle.MontoTotal = in.p_MontoTotal != null ? in.p_MontoTotal : 0.0;
			insertDetalle.MontoIntraday = in.p_MontoIntraday != null ? in.p_MontoIntraday : 0.0;
			insertDetalle.MontoOvernight = in.p_MontoOvernight != null ? in.p_MontoOvernight : 0.0;
			insertDetalle.IdMoneda = in.p_IdMoneda != null ? in.p_IdMoneda : "";
			insertDetalle.IdMonedaCalculo = in.p_IdMonedaCalculo != null ? in.p_IdMonedaCalculo : "";
			insertDetalle.ClasificacionRiesgoMinima = in.p_ClasificacionRiesgoMinima != null ? in.p_ClasificacionRiesgoMinima : "";
			insertDetalle.IdEstado = in.p_IdEstado != null ? in.p_IdEstado : "ACT";
			insertDetalle.EsPersonalizada = in.p_EsPersonalizada != null ? in.p_EsPersonalizada : "N";
			insertDetalle.EsTotalizador = in.p_EsTotalizador != null ? in.p_EsTotalizador : "N";
			insertDetalle.FechaIngreso = v_FechaIngreso;
			insertDetalle.FechaCreacion = v_FechaHoraSistema;
			insertDetalle.FechaModificacion = v_FechaHoraSistema;
			insertDetalle.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertDetalle.IdUsuarioCreador = in.p_IdUsuario;
			insertDetalle.UI_Limite = "PLANTILLA";
			insertDetalle.IdAgente = in.p_IdAgente != null ? in.p_IdAgente : 0L;
			insertDetalle.IdClasificacionTipoInstrumento = in.p_IdClasificacionTipoInstrumento != null ? in.p_IdClasificacionTipoInstrumento : "";
			insertDetalle.Porcentaje = in.p_Porcentaje != null ? in.p_Porcentaje : 0.0;

			sql.batch(conn, SQL_INSERT_DETALLE, java.util.Arrays.asList(insertDetalle), InsertDetalle.class);

			// Consultar si requiere autorización
			logger.debug("Consultando si requiere autorización");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, v_IdConcepto);

			if ("S".equals(v_RequiereAutorizacion)) {
				// Obtener glosa para el evento
				HashMap<String, Object> paramsGlosa = new HashMap<>();
				paramsGlosa.put("IdPlantillaLimite", in.p_IdPlantillaLimite);

				List<SelectGlosa> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsGlosa, SelectGlosa.class);
				String v_Glosa = in.p_IdPlantillaLimite;
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
				evento.NumeroOperacionConcepto = String.valueOf(v_IdLimite);
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
				// Confirma la creación directamente
				logger.debug("Confirmar creación de detalle límite");
				v_IdEstadoAutorizacion = "ING";

				ConfirmarCrearPlantillaLimiteDetalle.EntradaConfirmarCrearPlantillaLimiteDetalle confirmarIn = new ConfirmarCrearPlantillaLimiteDetalle.EntradaConfirmarCrearPlantillaLimiteDetalle();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdLimite = v_IdLimite;
				confirmarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;

				ConfirmarCrearPlantillaLimiteDetalle confirmarWrapper = new ConfirmarCrearPlantillaLimiteDetalle();
				ConfirmarCrearPlantillaLimiteDetalle.SalidaConfirmarCrearPlantillaLimiteDetalle confirmarOut = confirmarWrapper.logica(conn, confirmarIn);

				// Agregar advertencias y errores de la confirmación
				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.p_out_AdvertenciasContador.intValue();
				if (confirmarOut.p_out_AdvertenciasDetalle != null) {
					for (ConfirmarCrearPlantillaLimiteDetalle.AdvertenciaDetalle adv : confirmarOut.p_out_AdvertenciasDetalle) {
						salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
					}
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.p_out_ErroresContador.intValue();
				if (confirmarOut.p_out_ErroresDetalle != null) {
					for (ConfirmarCrearPlantillaLimiteDetalle.ErrorDetalle err : confirmarOut.p_out_ErroresDetalle) {
						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
					}
				}
			}

			// Agregar log (AgregarLogMasivo con múltiples detalles)
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
			log.IdTipoRegistro = "PLANTILLA LIMITE DETALLE";
			entradaAgregarLog.p_Log.add(log);

			// Detalles del log
			agregarDetalleLog(entradaAgregarLog, "IdLimite", "", String.valueOf(v_IdLimite));
			agregarDetalleLog(entradaAgregarLog, "NombrePlantilla", "", in.p_IdPlantillaLimite);
			agregarDetalleLog(entradaAgregarLog, "NombreLimite", "", in.p_Nombre != null ? in.p_Nombre : "");
			agregarDetalleLog(entradaAgregarLog, "Mercado", "", in.p_IdClasificacionTipoInstrumento != null ? in.p_IdClasificacionTipoInstrumento : "");
			agregarDetalleLog(entradaAgregarLog, "Canal", "", in.p_IdCanalIngreso != null ? in.p_IdCanalIngreso : "");
			agregarDetalleLog(entradaAgregarLog, "Moneda", "", in.p_IdMoneda != null ? in.p_IdMoneda : "");
			agregarDetalleLog(entradaAgregarLog, "Porcentaje", "", in.p_Porcentaje != null ? String.valueOf(in.p_Porcentaje) : "0");
			agregarDetalleLog(entradaAgregarLog, "LineaAsignada", "", in.p_MontoTotal != null ? String.valueOf(in.p_MontoTotal) : "0");
			agregarDetalleLog(entradaAgregarLog, "Estado", "", in.p_IdEstado != null ? in.p_IdEstado : "");

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearPlantillaLimiteDetalle: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PLANTILLA_LIMITE_DETALLE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPlantillaLimiteDetalle");
		return salida;
	}

	private void agregarDetalleLog(AgregarLog.InAgregarLog entradaLog, String campo, String valorAntiguo, String valorNuevo) {
		AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = 1L;
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = "INGRESO";
		detLog.CampoRegistro = campo;
		detLog.ValorAntiguo = valorAntiguo;
		detLog.ValorNuevo = valorNuevo;
		entradaLog.p_LogDetalle.add(detLog);
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPlantillaLimiteDetalle in = gson.fromJson(jin.toString(), EntradaCrearPlantillaLimiteDetalle.class);
		SalidaCrearPlantillaLimiteDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPlantillaLimite\":\"PLANTILLA001\",\"p_IdTipoLimite\":\"TIPO1\",\"p_Nombre\":\"Limite Test\",\"p_IdMesa\":0,\"p_IdCanalIngreso\":\"OPT\",\"p_IdTipoInstrumento\":\"\",\"p_IdTipoOperacion\":\"\",\"p_IdTipoProducto\":\"\",\"p_IdInstrumento\":0,\"p_UnidadPlazo\":\"D\",\"p_Plazo\":0,\"p_IdTipoReajuste\":\"\",\"p_CodigoOperador\":\"\",\"p_MontoTotal\":1000000.0,\"p_MontoIntraday\":0.0,\"p_MontoOvernight\":0.0,\"p_IdMoneda\":\"CLP\",\"p_IdMonedaCalculo\":\"CLP\",\"p_ClasificacionRiesgoMinima\":\"\",\"p_IdEstado\":\"ACT\",\"p_EsPersonalizada\":\"N\",\"p_EsTotalizador\":\"N\",\"p_IdAgente\":0,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_Porcentaje\":0.0}";
			EntradaCrearPlantillaLimiteDetalle entrada = gson.fromJson(json, EntradaCrearPlantillaLimiteDetalle.class);
			CrearPlantillaLimiteDetalle demo = new CrearPlantillaLimiteDetalle();
			SalidaCrearPlantillaLimiteDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
