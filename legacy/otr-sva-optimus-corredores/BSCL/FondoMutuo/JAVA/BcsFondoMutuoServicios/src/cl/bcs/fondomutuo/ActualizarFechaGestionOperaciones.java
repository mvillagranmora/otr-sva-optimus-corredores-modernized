package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ConfirmarEjecutarFechaGestionOperaciones.EntradaConfirmarEjecutarFechaGestionOperaciones;
import cl.bcs.fondomutuo.ConfirmarEjecutarFechaGestionOperaciones.SalidaConfirmarEjecutarFechaGestionOperaciones;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.fondomutuo.ConfirmarEjecutarFechaGestionOperaciones.InEntidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarFechaGestionOperaciones implements JavaCall {

	static final Logger logger = LogManager.getLogger(ActualizarFechaGestionOperaciones.class);
	private final String SQL_SELECT_SOLICITUD_APORTERESCATES = loadResourceToString(
			"cl/bcs/fondomutuo/sql/ActualizarFechaGestionOperaciones_SelectFechas.sql");
	private final String IDCONCEPTO = "MODFEC";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD = "SOLICITUD-APORTE-RESCATE";


	public static class EntradaActualizarFechaGestionOperaciones extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<SolicitudesAporteRescate> p_IdSolicitudAporteRescate;
		public String p_FechaAceptacion;
		public String p_FechaCertificacion;
		public String p_FechaLiquidacion;
		public String p_FechaValorCuota;
		public String p_UsaReglasNegocio;

		public EntradaActualizarFechaGestionOperaciones() {
			this.p_IdSolicitudAporteRescate = new ArrayList<SolicitudesAporteRescate>();
		}

	}

	public static class SalidaActualizarFechaGestionOperaciones extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ERRORESCONTADOREVENTO;
		public List<MensajeSalida> P_OUT_ADVERTENCIAS;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaActualizarFechaGestionOperaciones() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOREVENTO = 0L;
			this.P_OUT_ADVERTENCIAS = new ArrayList<MensajeSalida>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}

	}

	public static class SolicitudesAporteRescate extends BcsPojo {
		public Long IdSolicitudAporteRescate;
		public Long FolioOmnibus;
		public Long IdCliente;
		public Long IdInstrumento;
		public Long IdEvento;
		public Long UsuarioAutorizador;
		public Long UsuarioModificador;
		public String Estado;
		public String EstadoLiquidacion;
		public String FechaAceptacion;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String FechaValorCuota;
		public String FechaAceptacionAnterior;
		public String FechaCertificacionAnterior;
		public String FechaLiquidacionAnterior;
		public String FechaValorCuotaAnterior;
		public String IdTipoMovimientoFondoMutuo;
;

		public SolicitudesAporteRescate() {
			this.FechaAceptacion = "0001-01-01";
			this.FechaCertificacion = "0001-01-01";
			this.FechaLiquidacion = "0001-01-01";
			this.FechaValorCuota = "0001-01-01";
			this.IdInstrumento = 0L;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaActualizarFechaGestionOperaciones jin = gson.fromJson(in.toString(),
				EntradaActualizarFechaGestionOperaciones.class);
		SalidaActualizarFechaGestionOperaciones out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaActualizarFechaGestionOperaciones logica(Connection con,
			EntradaActualizarFechaGestionOperaciones entrada) throws Exception {
		logger.debug("INICIO::ActualizarFechaGestionOperaciones");
		long inicio = System.currentTimeMillis();
		SalidaActualizarFechaGestionOperaciones out = new SalidaActualizarFechaGestionOperaciones();

		SQL sql = new SQL();

		HashMap<String, Object> params = new HashMap<String, Object>();
		List<Long> idSolicitudes = entrada.p_IdSolicitudAporteRescate.parallelStream().map(sol -> {
			return sol.IdSolicitudAporteRescate;
		}).collect(Collectors.toList());

		params.put("IdSolicitudAporteRescate", idSolicitudes);
	

		List<SolicitudesAporteRescate> listSolicitudesAporteRescates = sql.query(con,
				SQL_SELECT_SOLICITUD_APORTERESCATES, params, SolicitudesAporteRescate.class);
		List<SolicitudesAporteRescate> listSolicitudesAporteRescatesValidada = new ArrayList<SolicitudesAporteRescate>();
		List<Long> idSolicitudesValidacion = new ArrayList<Long>();
		boolean flagAceptacion = false, flagCertificacion = false, flagLiquidacion = false, flagValorCuota = false;
	
		if (listSolicitudesAporteRescates.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODIFICAR_FECHA",
					new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
	
		for (SolicitudesAporteRescate solicitud : listSolicitudesAporteRescates) {
			solicitud.FechaAceptacionAnterior = solicitud.FechaAceptacion;
			solicitud.FechaCertificacionAnterior = solicitud.FechaCertificacion;
			solicitud.FechaLiquidacionAnterior = solicitud.FechaLiquidacion;
			solicitud.FechaValorCuotaAnterior = solicitud.FechaValorCuota;
			solicitud.UsuarioModificador = entrada.p_IdUsuario;


			
				if (entrada.p_FechaAceptacion != null && entrada.p_FechaCertificacion != null
						&& entrada.p_FechaLiquidacion != null && entrada.p_FechaValorCuota != null) {
					if (entrada.p_FechaAceptacion.equals("0001-01-01")) {
						entrada.p_FechaAceptacion = null;
					} else {
						flagAceptacion = true;
					
					}

					if (entrada.p_FechaCertificacion.equals("0001-01-01")) {
						entrada.p_FechaCertificacion = null;
					} else {
						flagCertificacion = true;
				
					}

					if (entrada.p_FechaLiquidacion.equals("0001-01-01")) {
						entrada.p_FechaLiquidacion = null;
					} else {
						flagLiquidacion = true;
						
					}
					if (entrada.p_FechaValorCuota.equals("0001-01-01")) {
						entrada.p_FechaValorCuota = null;
					} else {
						flagValorCuota = true;
					
					}

				
				}
			if (solicitud.FolioOmnibus > 0) {
				MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
						MensajeSalida.MENSAJE_ADVERTENCIA, "UI_OPERACION_CON_OMNIBUS", new HashMap<String, String>(),
						con, entrada.p_IdEmpresa);
					out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}

			if (solicitud.IdTipoMovimientoFondoMutuo.equals("APOREINV")) {
					MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
							MensajeSalida.MENSAJE_ADVERTENCIA, "UI_OPERACION_REINVERSION", new HashMap<String, String>(),
							con, entrada.p_IdEmpresa);
					out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}
			if(flagAceptacion) {
				MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
						MensajeSalida.MENSAJE_ADVERTENCIA, "MODFEC_ACEPTACION", new HashMap<String, String>(),
						con, entrada.p_IdEmpresa);
				out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}
			if(flagCertificacion) {
				MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
						MensajeSalida.MENSAJE_ADVERTENCIA, "MODFEC_CERTIFICACION",
						new HashMap<String, String>(), con, entrada.p_IdEmpresa);
				out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}
			if(flagLiquidacion) {
				MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
						MensajeSalida.MENSAJE_ADVERTENCIA, "MODFEC_LIQUIDACION", new HashMap<String, String>(),
						con, entrada.p_IdEmpresa);
				out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}
			if(flagValorCuota) {
				MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
						MensajeSalida.MENSAJE_ADVERTENCIA, "MODFEC_VALORCUOTA", new HashMap<String, String>(), con,
						entrada.p_IdEmpresa);
				out.P_OUT_ADVERTENCIAS.add(mensajeSalida);
			}

			if ((solicitud.Estado.equals("ING") || solicitud.Estado.equals("PEN"))&& solicitud.EstadoLiquidacion.equals("ING")) {
				solicitud.FechaAceptacion = entrada.p_FechaAceptacion;
				solicitud.FechaCertificacion = entrada.p_FechaCertificacion;
				solicitud.FechaLiquidacion = entrada.p_FechaLiquidacion;
				solicitud.FechaValorCuota = entrada.p_FechaValorCuota;
			
				MensajeSalida msg = new MensajeSalida(solicitud.IdSolicitudAporteRescate, MensajeSalida.MENSAJE_OK,
						"UI_OK_CAMBIO_FECHA", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
				out.P_OUT_MENSAJES.add(msg);
			} else if (solicitud.Estado.equals("CRT") && solicitud.EstadoLiquidacion.equals("ING")) {
				solicitud.FechaAceptacion = null;
				solicitud.FechaCertificacion = null;
				solicitud.FechaLiquidacion = entrada.p_FechaLiquidacion;
				solicitud.FechaValorCuota = null;
				if (entrada.p_FechaAceptacion != null || entrada.p_FechaCertificacion != null
						|| entrada.p_FechaValorCuota != null) {
					MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
							MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_ESTADO_CRT_ING", new HashMap<String, String>(), con,
							entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeSalida);
					idSolicitudesValidacion.add(solicitud.IdSolicitudAporteRescate);
				} else if (solicitud.FechaLiquidacion != null) {
					MensajeSalida msg = new MensajeSalida(solicitud.IdSolicitudAporteRescate, MensajeSalida.MENSAJE_OK,
							"UI_OK_CAMBIO_FECHA", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(msg);
				}
			} else if (solicitud.Estado.equals("ING") && solicitud.EstadoLiquidacion.equals("LIQ")) {
				solicitud.FechaAceptacion = null;
				solicitud.FechaCertificacion = entrada.p_FechaCertificacion;
				solicitud.FechaLiquidacion = null;
				solicitud.FechaValorCuota = null;
				if (entrada.p_FechaAceptacion != null || entrada.p_FechaLiquidacion != null
						|| entrada.p_FechaValorCuota != null) {
					MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
							MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_ESTADO_ING_LIQ", new HashMap<String, String>(), con,
							entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeSalida);
					idSolicitudesValidacion.add(solicitud.IdSolicitudAporteRescate);
				} else if (solicitud.FechaCertificacion != null) {
					MensajeSalida msg = new MensajeSalida(solicitud.IdSolicitudAporteRescate, MensajeSalida.MENSAJE_OK,
							"UI_OK_CAMBIO_FECHA", new HashMap<String, String>(), con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(msg);
				}

			} else if (solicitud.Estado.equals("CRT") && solicitud.EstadoLiquidacion.equals("LIQ")) {
				solicitud.FechaAceptacion = null;
				solicitud.FechaCertificacion = null;
				solicitud.FechaLiquidacion = null;
				solicitud.FechaValorCuota = null;
				if (entrada.p_FechaAceptacion != null || entrada.p_FechaLiquidacion != null
						|| entrada.p_FechaValorCuota != null || entrada.p_FechaCertificacion != null) {
					MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
							MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_ESTADO_CRT_LIQ", new HashMap<String, String>(), con,
							entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeSalida);
					idSolicitudesValidacion.add(solicitud.IdSolicitudAporteRescate);
				}
			} else if (solicitud.Estado.equals("PDI") && solicitud.EstadoLiquidacion.equals("ING")) {
				solicitud.FechaAceptacion = null;
				solicitud.FechaCertificacion = null;
				solicitud.FechaLiquidacion = null;
				solicitud.FechaValorCuota = null;
				if (entrada.p_FechaAceptacion != null || entrada.p_FechaLiquidacion != null
						|| entrada.p_FechaValorCuota != null || entrada.p_FechaCertificacion != null) {
					MensajeSalida mensajeSalida = new MensajeSalida(solicitud.IdSolicitudAporteRescate,
							MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ERROR_ESTADO_PDI_ING", new HashMap<String, String>(), con,
							entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeSalida);
					idSolicitudesValidacion.add(solicitud.IdSolicitudAporteRescate);
				}
			}

		}
	
		out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream()
				.filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count();
		/*
		  if (out.P_OUT_ERRORESCONTADOR > 0){
		  
		  return out; }
		 */
		if(!idSolicitudesValidacion.isEmpty() && idSolicitudesValidacion != null) {
			for(SolicitudesAporteRescate solicitudesAntiguas : listSolicitudesAporteRescates) {
				if(!idSolicitudesValidacion.contains(solicitudesAntiguas.IdSolicitudAporteRescate)) {
						
					listSolicitudesAporteRescatesValidada.add(solicitudesAntiguas);
				}
				
				
			}
			
		}else {
			listSolicitudesAporteRescatesValidada.addAll(listSolicitudesAporteRescates);
		}
		
		for(SolicitudesAporteRescate solicitud : listSolicitudesAporteRescatesValidada) {
			
			CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
			EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();

			List<SolicitudesAporteRescate> solicitudesValoresAntiguos = obtenerSolicitudAporteRescate(con, entrada);
			
			List<SolicitudesAporteRescate> listSolicitud = new ArrayList<SolicitudesAporteRescate>();
			listSolicitud.add(solicitud);
		
			List<Long> idSolicitudesCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio",
					1L);
		
			Long idSolicitudCambio = idSolicitudesCambio.get(0);
	
			List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con,
					idSolicitudCambio, listSolicitud, entrada, solicitudesValoresAntiguos);

			if (registrosSolicitudesCambios.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"FALTA_ESPECIFICAR_REGISTRO_CAMBIO", null, con, entrada.p_IdEmpresa));
				return out;
			}

			inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;

			servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);

		}
		

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, IDCONCEPTO);
		SalidaActualizarFechaGestionOperaciones outAut = new SalidaActualizarFechaGestionOperaciones();
		if ("S".equals(requiereAutorizacion)) {
			outAut = crearEventoAutorizacion(con, entrada, listSolicitudesAporteRescatesValidada, out);
		} else {
			outAut = confirmarModificarFechaGestionOperaciones(con, entrada);
		}
		
		out.P_OUT_ADVERTENCIAS = outAut.P_OUT_ADVERTENCIAS;
		out.P_OUT_MENSAJES = outAut.P_OUT_MENSAJES;

		out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ADVERTENCIAS.stream().count();
		out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream()
				.filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count();

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarFechaGestionOperaciones", fin - inicio);
		return out;
	}

	private SalidaActualizarFechaGestionOperaciones confirmarModificarFechaGestionOperaciones(Connection con,
			EntradaActualizarFechaGestionOperaciones entrada) throws Exception {
		logger.debug("INICIO::ActualizarFechaGestionOperaciones.confirmarModificarFechaGestionOperaciones");
		long inicio = System.currentTimeMillis();
		EntradaConfirmarEjecutarFechaGestionOperaciones entradaConfirmarModificarFechaGestionOperaciones = new EntradaConfirmarEjecutarFechaGestionOperaciones();
		List<InEntidad> entidades = new ArrayList<InEntidad>();
		entrada.p_IdSolicitudAporteRescate.stream().forEach(sol -> {
			InEntidad entidad = new InEntidad();
			entidad.IdEntidad = sol.IdSolicitudAporteRescate;
			entidades.add(entidad);
		});

		entradaConfirmarModificarFechaGestionOperaciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarFechaGestionOperaciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarFechaGestionOperaciones.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarFechaGestionOperaciones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarFechaGestionOperaciones.p_Entidad.addAll(entidades);
		entradaConfirmarModificarFechaGestionOperaciones.p_IdEstadoAutorizacion = "MOD";

		ConfirmarModificarFechaGestionOperaciones confirmarModificarFechaGestionOperaciones = new ConfirmarModificarFechaGestionOperaciones();

		SalidaConfirmarEjecutarFechaGestionOperaciones salidaConfirmarModificarFechaGestionOperaciones = confirmarModificarFechaGestionOperaciones
				.logica(con, entradaConfirmarModificarFechaGestionOperaciones);

		SalidaActualizarFechaGestionOperaciones out = new SalidaActualizarFechaGestionOperaciones();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarFechaGestionOperaciones.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarFechaGestionOperaciones.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarFechaGestionOperaciones.confirmarModificarFechaGestionOperaciones", fin - inicio);
		return out;
	}

	private SalidaActualizarFechaGestionOperaciones crearEventoAutorizacion(Connection con,
			EntradaActualizarFechaGestionOperaciones entrada,
			List<SolicitudesAporteRescate> listSolicitudesAporteRescates, SalidaActualizarFechaGestionOperaciones out)
			throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGenerales.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		// Logica autorizacion
		java.sql.Date FechaIngreso = new java.sql.Date(System.currentTimeMillis());
		DateFormat PeriodoFecha = new SimpleDateFormat("yyyy-MM");
		String FechaSistema = PeriodoFecha.format(FechaIngreso);
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		for (SolicitudesAporteRescate solicitud : listSolicitudesAporteRescates) {

			String P_OUT_ADVERTENCIAS = out.P_OUT_ADVERTENCIAS.stream().filter(o -> o.id == solicitud.IdSolicitudAporteRescate).map(n -> String.valueOf(n.descripcion))
					.collect(Collectors.joining("-", "", ""));
			ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
			evento.IdTipoEvento = IDCONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = FechaSistema;// fecha de sistema
			evento.IdTipoOperacion = null;
			evento.IdConcepto = IDCONCEPTO;
			evento.NumeroOperacionConcepto = solicitud.IdSolicitudAporteRescate.toString();
			evento.FechaOperacionConcepto = FechaSistema;// fecha de sistema
			evento.IdCliente = solicitud.IdCliente;//
			evento.IdFuncionario = 0L;
			evento.IdProveedor = 0L;
			evento.IdTipoProducto = null;
			evento.IdClasificacionTipoInstrumento = null;
			evento.IdTipoInstrumento = null;
			evento.IdInstrumento = solicitud.IdInstrumento;
			evento.Nemo = null;
			evento.Cantidad = null;
			evento.Monto = null;
			evento.Precio = null;
			evento.IdMoneda = "CLP";
			evento.Glosa = "Secuencia: " + solicitud.IdSolicitudAporteRescate.toString() + "; " + P_OUT_ADVERTENCIAS;

			inCrearAutorizacion.P_TABLAEVENTOS.add(evento);
		}
		inCrearAutorizacion.P_IDEMPRESA = entrada.p_IdEmpresa;
		inCrearAutorizacion.P_IDMODULO = entrada.p_IdModulo;
		inCrearAutorizacion.P_IDUSUARIO = entrada.p_IdUsuario;
		inCrearAutorizacion.P_SIMULAROPERACION = "N";
		inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";

		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo
				.logica(con, inCrearAutorizacion);

		out.P_OUT_ERRORESCONTADOREVENTO = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;

		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE
				.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,
						advertencia.Codigo, advertencia.Descripcion))
				.collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream()
				.map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion))
				.collect(Collectors.toList());

		out.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		out.P_OUT_MENSAJES.addAll(mensajesErrores);

		if (out.P_OUT_ERRORESCONTADOREVENTO.equals(0L) && out.P_OUT_ERRORESCONTADOR.equals(0L) && listSolicitudesAporteRescates != null && !listSolicitudesAporteRescates.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION",
					new HashMap<String, String>(), con, entrada.p_IdEmpresa));
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGenerales.crearEventoAutorizacion", fin - inicio);
		return out;
	}

	private List<SolicitudesAporteRescate> obtenerSolicitudAporteRescate(Connection con,
			EntradaActualizarFechaGestionOperaciones entrada) throws Exception {
		logger.debug("INICIO::ActualizarFechaGestionOperaciones.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<Long> idSolicitudAporteRescate = entrada.p_IdSolicitudAporteRescate.stream()
				.map(detalle -> detalle.IdSolicitudAporteRescate).collect(Collectors.toList());
		params.put("IdSolicitudAporteRescate", idSolicitudAporteRescate);
		List<SolicitudesAporteRescate> solicitudAporteRescate = sql.query(con, SQL_SELECT_SOLICITUD_APORTERESCATES,
				params, SolicitudesAporteRescate.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarFechaGestionOperaciones.obtenerDatosPersona", fin - inicio);
		return solicitudAporteRescate;
	}

	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio,
			List<SolicitudesAporteRescate> solicitudesAporteRescate, EntradaActualizarFechaGestionOperaciones entrada,
			List<SolicitudesAporteRescate> solicitudesValoresAntiguos) throws Exception {
		logger.trace("INICIO::ActualizarFechaGestionOperaciones.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();

	for (SolicitudesAporteRescate solicitud : solicitudesAporteRescate) {
			SolicitudesAporteRescate solicitudAntigua = solicitudesValoresAntiguos.stream()
					.filter(sol -> sol.IdSolicitudAporteRescate.equals(solicitud.IdSolicitudAporteRescate)).findFirst()
					.orElse(new SolicitudesAporteRescate());
			if (entrada.p_FechaAceptacion != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.CampoModificado = "FechaAceptacion";
				registroSolicitudCambio.ValorNuevo = solicitud.FechaAceptacion;
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(solicitud.IdSolicitudAporteRescate);

				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(solicitudAntigua,
						SolicitudesAporteRescate.class, "FechaAceptacion");

				registroSolicitudCambio.TipoDato = "DATE";
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			if (entrada.p_FechaLiquidacion != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.CampoModificado = "FechaLiquidacion";
				registroSolicitudCambio.ValorNuevo = solicitud.FechaLiquidacion;
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(solicitud.IdSolicitudAporteRescate);

				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(solicitudAntigua,
						SolicitudesAporteRescate.class, "FechaLiquidacion");

				registroSolicitudCambio.TipoDato = "DATE";
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			if (entrada.p_FechaCertificacion != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.CampoModificado = "FechaCertificacion";
				registroSolicitudCambio.ValorNuevo = solicitud.FechaCertificacion;
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(solicitud.IdSolicitudAporteRescate);

				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(solicitudAntigua,
						SolicitudesAporteRescate.class, "FechaCertificacion");

				registroSolicitudCambio.TipoDato = "DATE";
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			if (entrada.p_FechaValorCuota != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.CampoModificado = "FechaValorCuota";
				registroSolicitudCambio.ValorNuevo = solicitud.FechaValorCuota;
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(solicitud.IdSolicitudAporteRescate);

				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(solicitudAntigua,
						SolicitudesAporteRescate.class, "FechaValorCuota");

				registroSolicitudCambio.TipoDato = "DATE";
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;

				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ActualizarFechaGestionOperaciones.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			ActualizarFechaGestionOperaciones actualiza = new ActualizarFechaGestionOperaciones();// 664266-406425
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_IdSolicitudAporteRescate\":[{\"IdSolicitudAporteRescate\":665799},{\"IdSolicitudAporteRescate\":665792},{\"IdSolicitudAporteRescate\":665791},{\"IdSolicitudAporteRescate\":665790}],\"p_FechaAceptacion\":\"0001-01-01\",\"p_FechaCertificacion\":\"0001-01-01\",\"p_FechaLiquidacion\":\"0001-01-01\",\"p_FechaValorCuota\":\"2022-04-06\"}";
			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FMM\",\"p_IdSolicitudAporteRescate\":[{\"IdSolicitudAporteRescate\":17405963}],\"p_FechaAceptacion\":\"2024-10-01\",\"p_FechaCertificacion\":\"2024-10-03\",\"p_FechaLiquidacion\":\"2024-10-07\",\"p_FechaValorCuota\":\"2024-10-02\"}";

			EntradaActualizarFechaGestionOperaciones Objin = gson.fromJson(json2,
					EntradaActualizarFechaGestionOperaciones.class);

			try {
				SalidaActualizarFechaGestionOperaciones salida = actualiza.logica(con, Objin);
				System.out.println(salida);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");

	}
}
