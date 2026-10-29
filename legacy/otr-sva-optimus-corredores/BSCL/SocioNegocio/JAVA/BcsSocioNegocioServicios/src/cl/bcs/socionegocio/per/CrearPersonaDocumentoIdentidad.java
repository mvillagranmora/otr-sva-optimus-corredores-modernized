package cl.bcs.socionegocio.per;

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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaDocumentosIdentidad.EntradaConfirmarCrearPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaDocumentosIdentidad.SalidaConfirmarCrearPersonaDocumentosIdentidad;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migracion de "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaDocumentoIdentidad" (IdServicio=72).
 * Reemplaza la llamada directa al SP (TipoServicio='P') para que PLA.Servicio despache a esta clase (TipoServicio='J').
 */
public class CrearPersonaDocumentoIdentidad implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearPersonaDocumentoIdentidad.class);

	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaDocumentoIdentidad_Select_DocumentoIdentidadExiste.sql");
	private final String SQL_SELECT_TIPO_DOCUMENTO_PERSONA_EXISTE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaDocumentoIdentidad_Select_TipoDocumentoPersonaExiste.sql");
	private final String SQL_SELECT_GLOSA_PERSONA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaDocumentoIdentidad_Select_GlosaPersona.sql");
	private final String SQL_INSERT_DOCUMENTO_IDENTIDAD = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaDocumentoIdentidad_Insert_DocumentoIdentidad.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaDocumentoIdentidad_Insert_LogGenerarContratos.sql");

	private final String ID_CONCEPTO = "NUEDOC";

	public static class EntradaCrearPersonaDocumentoIdentidad {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;

		public String p_IdTipoDocumentoIdentidad;
		public String p_DocumentoIdentidad;
		public String p_NumeroSerie;        // SP: DEFAULT ''
		public String p_FechaEmision;
		public String p_FechaVencimiento;   // SP: DEFAULT '0001-01-01'
	}

	public static class SalidaCrearPersonaDocumentoIdentidad extends SalidaEstandar {
		public Long p_out_RefId;

		public SalidaCrearPersonaDocumentoIdentidad() {
			this.p_out_RefId = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Contador {
		public Long Count;
	}

	public static class GlosaPersona {
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class InsertDocumentoIdentidad {
		public Long IdPersona;
		public Long IdDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
		public String NumeroSerie;
		public String FechaEmision;
		public String FechaVencimiento;
		public String FechaIngreso;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
	}

	public static class InsertLogGenerarContratos {
		public Long Idlog;
		public String CodigoFormulario;
		public String CampoBackend;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdUsuario;
		public String FechaSistema;
		public Long IdPrograma;
		public String Estado;
		public String Mensaje;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPersonaDocumentoIdentidad entrada = gson.fromJson(in.toString(), EntradaCrearPersonaDocumentoIdentidad.class);
		SalidaCrearPersonaDocumentoIdentidad salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(salida)).getAsJsonObject();
	}

	public SalidaCrearPersonaDocumentoIdentidad logica(Connection con, EntradaCrearPersonaDocumentoIdentidad in) throws Exception {
		logger.info("INICIO::CrearPersonaDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearPersonaDocumentoIdentidad salida = new SalidaCrearPersonaDocumentoIdentidad();
		SQL sql = new SQL();

		try {
			// Valores DEFAULT del SP original
			String numeroSerie = (in.p_NumeroSerie != null) ? in.p_NumeroSerie : "";
			String fechaVencimiento = soloFecha((in.p_FechaVencimiento != null) ? in.p_FechaVencimiento : "0001-01-01");
			String fechaEmision = soloFecha(in.p_FechaEmision);

			String fechaIngreso = Utils.getFechaSistema(con, Utils.getSchema(), in.p_IdEmpresa);

			// Validacion 1: el documento no debe estar registrado en otra entidad
			logger.debug("INICIO::SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTE");
			HashMap<String, Object> paramsExisteDocumento = new HashMap<String, Object>();
			paramsExisteDocumento.put("DocumentoIdentidad", in.p_DocumentoIdentidad);
			paramsExisteDocumento.put("IdTipoDocumentoIdentidad", in.p_IdTipoDocumentoIdentidad);
			List<Contador> existeDocumento = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTE, paramsExisteDocumento, Contador.class);
			logger.debug("FIN::SQL_SELECT_DOCUMENTO_IDENTIDAD_EXISTE");
			Long countDocumento = (!existeDocumento.isEmpty() && existeDocumento.get(0).Count != null) ? existeDocumento.get(0).Count : 0L;
			if (countDocumento > 0) {
				String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD_EXISTE", new HashMap<String, String>());
				salida.addError("UI_DOCUMENTO_IDENTIDAD_EXISTE", mensaje);
				return salida;
			}

			// Validacion 2: solo puede existir un tipo de documento de identidad vigente por persona
			logger.debug("INICIO::SQL_SELECT_TIPO_DOCUMENTO_PERSONA_EXISTE");
			HashMap<String, Object> paramsExisteTipo = new HashMap<String, Object>();
			paramsExisteTipo.put("IdPersona", in.p_IdPersona);
			paramsExisteTipo.put("IdTipoDocumentoIdentidad", in.p_IdTipoDocumentoIdentidad);
			List<Contador> existeTipo = sql.query(con, SQL_SELECT_TIPO_DOCUMENTO_PERSONA_EXISTE, paramsExisteTipo, Contador.class);
			logger.debug("FIN::SQL_SELECT_TIPO_DOCUMENTO_PERSONA_EXISTE");
			Long countTipo = (!existeTipo.isEmpty() && existeTipo.get(0).Count != null) ? existeTipo.get(0).Count : 0L;
			if (countTipo > 0) {
				String mensaje = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE", new HashMap<String, String>());
				salida.addError("UI_TIPO_DOCUMENTO_IDENTIDAD_EXISTE", mensaje);
				return salida;
			}

			Long idDocumentoIdentidad = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", 1L).get(0);
			salida.p_out_RefId = idDocumentoIdentidad;

			String requiereAutorizacionConcepto = Utils.getRequiereEventoAutorizacion(con, in.p_IdEmpresa, ID_CONCEPTO);
			Boolean requiereAutorizacion = "S".equals(requiereAutorizacionConcepto) && !"N".equals(in.p_UsaReglasNegocio);

			InsertDocumentoIdentidad documento = new InsertDocumentoIdentidad();
			documento.IdPersona = in.p_IdPersona;
			documento.IdDocumentoIdentidad = idDocumentoIdentidad;
			documento.DocumentoIdentidad = in.p_DocumentoIdentidad;
			documento.IdTipoDocumentoIdentidad = in.p_IdTipoDocumentoIdentidad;
			documento.NumeroSerie = numeroSerie;
			documento.FechaEmision = fechaEmision;
			documento.FechaVencimiento = fechaVencimiento;
			documento.FechaIngreso = fechaIngreso;
			documento.IdEstadoAutorizacion = requiereAutorizacion ? "PEN" : "ING";
			documento.IdUsuarioCreador = in.p_IdUsuario;

			List<InsertDocumentoIdentidad> documentos = new ArrayList<InsertDocumentoIdentidad>();
			documentos.add(documento);
			logger.debug("INICIO::SQL_INSERT_DOCUMENTO_IDENTIDAD");
			sql.batch(con, SQL_INSERT_DOCUMENTO_IDENTIDAD, documentos, InsertDocumentoIdentidad.class);
			logger.debug("FIN::SQL_INSERT_DOCUMENTO_IDENTIDAD");

			if (requiereAutorizacion) {
				logger.debug("INICIO::SQL_SELECT_GLOSA_PERSONA");
				HashMap<String, Object> paramsGlosa = new HashMap<String, Object>();
				paramsGlosa.put("IdPersona", in.p_IdPersona);
				List<GlosaPersona> personas = sql.query(con, SQL_SELECT_GLOSA_PERSONA, paramsGlosa, GlosaPersona.class);
				logger.debug("FIN::SQL_SELECT_GLOSA_PERSONA");
				String documentoIdentidadPersona = (!personas.isEmpty() && personas.get(0).DocumentoIdentidad != null) ? personas.get(0).DocumentoIdentidad : "";
				String nombreCompleto = (!personas.isEmpty() && personas.get(0).NombreCompleto != null) ? personas.get(0).NombreCompleto : "";
				String glosa = documentoIdentidadPersona + " / " + nombreCompleto + " / NuevoDoc: " + in.p_DocumentoIdentidad;

				CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
				entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
				entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
				entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
				entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";

				CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
				evento.IdTipoEvento = ID_CONCEPTO;
				evento.IdModuloSolicita = in.p_IdModulo;
				evento.IdUsuarioSolicita = in.p_IdUsuario;
				evento.Fecha = fechaIngreso;
				evento.IdConcepto = ID_CONCEPTO;
				evento.NumeroOperacionConcepto = String.valueOf(idDocumentoIdentidad);
				evento.FechaOperacionConcepto = fechaIngreso;
				evento.IdCliente = in.p_IdPersona;
				evento.Glosa = glosa;

				List<CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS> eventos = new ArrayList<CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS>();
				eventos.add(evento);
				entradaEvtAutorizacion.P_TABLAEVENTOS = eventos;

				logger.debug("INICIO::CrearEventoAutorizacionMasivo");
				CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(con, entradaEvtAutorizacion);
				logger.debug("FIN::CrearEventoAutorizacionMasivo");

				salida.P_OUT_ERRORESCONTADOR += salidaAut.P_OUT_ERRORESCONTADOR;
				for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE adv : salidaAut.P_OUT_ADVERTENCIASDETALLE) {
					salida.addAdvertencia(adv.Codigo, adv.Descripcion);
				}
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE err : salidaAut.P_OUT_ERRORESDETALLE) {
					salida.addError(err.Codigo, err.Descripcion);
				}

				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					String mensajeAutorizacion = Diccionario.getMensaje(con, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String, String>());
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
					salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
				}

			} else {
				logger.debug("INICIO::SQL_INSERT_LOG_GENERAR_CONTRATOS");
				InsertLogGenerarContratos log = new InsertLogGenerarContratos();
				log.Idlog = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L).get(0);
				log.CodigoFormulario = "DOCUMENTO_IDENTIDAD_";
				log.CampoBackend = "Creado";
				log.IdEmpresa = in.p_IdEmpresa;
				log.IdCliente = 0L;
				log.IdPersona = in.p_IdPersona;
				log.IdUsuario = in.p_IdUsuario;
				log.FechaSistema = fechaIngreso.substring(0, 10);
				log.IdPrograma = 0L;
				log.Estado = "PEN";
				log.Mensaje = "Cliente - CrearPersonaDocumentoIdentidad.sql";

				List<InsertLogGenerarContratos> logs = new ArrayList<InsertLogGenerarContratos>();
				logs.add(log);
				sql.batch(con, SQL_INSERT_LOG_GENERAR_CONTRATOS, logs, InsertLogGenerarContratos.class);
				logger.debug("FIN::SQL_INSERT_LOG_GENERAR_CONTRATOS");

				EntradaConfirmarCrearPersonaDocumentosIdentidad entradaConfirmar = new EntradaConfirmarCrearPersonaDocumentosIdentidad();
				entradaConfirmar.p_IdEmpresa = in.p_IdEmpresa;
				entradaConfirmar.p_IdUsuario = in.p_IdUsuario;
				entradaConfirmar.p_IdModulo = in.p_IdModulo;
				entradaConfirmar.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				entradaConfirmar.p_IdDocumentoIdentidad = idDocumentoIdentidad;
				entradaConfirmar.p_IdEstadoAutorizacion = "ING";

				logger.debug("INICIO::ConfirmarCrearPersonaDocumentosIdentidad");
				ConfirmarCrearPersonaDocumentosIdentidad confirmar = new ConfirmarCrearPersonaDocumentosIdentidad();
				SalidaConfirmarCrearPersonaDocumentosIdentidad salidaConfirmar = confirmar.logica(con, entradaConfirmar);
				logger.debug("FIN::ConfirmarCrearPersonaDocumentosIdentidad");

				salida.P_OUT_ERRORESCONTADOR += salidaConfirmar.P_OUT_ERRORESCONTADOR;
				for (MensajeSalida mensaje : salidaConfirmar.P_OUT_MENSAJES) {
					if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
						salida.addError(mensaje.codigo, mensaje.descripcion);
					} else if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
						salida.addAdvertencia(mensaje.codigo, mensaje.descripcion);
					}
				}
			}

		} catch (Exception ex) {
			logger.error("Error en CrearPersonaDocumentoIdentidad: " + ex.getMessage());
			salida.addError("ERROR_CREAR_PERSONA_DOCUMENTO_IDENTIDAD", ex.getMessage());
		}

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaDocumentoIdentidad", finServicio - inicioServicio);
		return salida;
	}

	/**
	 * Las columnas FechaEmision/FechaVencimiento son DAYDATE (solo fecha). La UI puede enviar
	 * timestamp ISO completo (ej. "2026-05-28T04:00:00.000Z"); se toma solo "YYYY-MM-DD" para
	 * evitar el error de conversion del driver JDBC hacia java.sql.Date.
	 */
	private String soloFecha(String valor) {
		if (valor == null) {
			return null;
		}
		return (valor.length() > 10) ? valor.substring(0, 10) : valor;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":41162,\"p_IdTipoDocumentoIdentidad\":\"P\",\"p_DocumentoIdentidad\":\"TEST-DOC-001\",\"p_NumeroSerie\":\"\",\"p_FechaEmision\":\"2020-01-01\",\"p_FechaVencimiento\":\"0001-01-01\"}";
			EntradaCrearPersonaDocumentoIdentidad entrada = gson.fromJson(json, EntradaCrearPersonaDocumentoIdentidad.class);

			CrearPersonaDocumentoIdentidad servicio = new CrearPersonaDocumentoIdentidad();
			SalidaCrearPersonaDocumentoIdentidad salida = servicio.logica(conn, entrada);
			System.out.println(salida.toString());

			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
