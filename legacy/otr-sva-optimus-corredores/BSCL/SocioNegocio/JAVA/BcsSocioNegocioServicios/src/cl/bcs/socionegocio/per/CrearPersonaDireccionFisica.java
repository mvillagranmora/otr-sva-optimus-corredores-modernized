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

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: PER.CrearPersonaDireccionFisica
 * Crea la direccion fisica de una persona. Si el concepto NUEDIF requiere autorizacion
 * (y p_UsaReglasNegocio no es "N"), genera el evento de autorizacion; si no, inserta el
 * log de regeneracion de contratos y confirma de inmediato invocando ConfirmarCrearPersonaDireccionFisica.
 */
public class CrearPersonaDireccionFisica implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearPersonaDireccionFisica.class);

	private final String SQL_INSERT_DIRECCION_FISICA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/CrearPersonaDireccionFisica_Insert.sql");
	private final String SQL_SELECT_GLOSA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/CrearPersonaDireccionFisica_Select_Glosa.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/CrearPersonaDireccionFisica_Insert_LogGenerarContratos.sql");

	public static class EntradaCrearPersonaDireccionFisica {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Calle;
		public String p_Numero;
		public String p_Departamento;
		public String p_Sector;
		public String p_CodigoPostal;
		public Long p_IdCiudad;
		public Long p_IdDivisionPais;
		public String p_CodigoProposito;
		public String p_Comentario;
		public String p_IdEstado;
		public Long p_IdPersona;
		public String p_CodigoCompuesto;

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

	public static class SalidaCrearPersonaDireccionFisica {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearPersonaDireccionFisica() {
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

	public static class InsertDireccionFisica {
		public Long IdDireccionFisica;
		public String Calle;
		public String Numero;
		public String Departamento;
		public String Sector;
		public String CodigoPostal;
		public Long IdCiudad;
		public Long IdDivisionPais;
		public String CodigoProposito;
		public String Comentario;
		public String IdEstado;
		public String FechaIngreso;
		public Long IdPersona;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public String CodigoCompuesto;
	}

	public static class GlosaResult {
		public String Glosa;
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

	public SalidaCrearPersonaDireccionFisica logica(Connection conn, EntradaCrearPersonaDireccionFisica in) throws Exception {
		logger.info("INIT CrearPersonaDireccionFisica");
		SalidaCrearPersonaDireccionFisica salida = new SalidaCrearPersonaDireccionFisica();
		SQL sql = new SQL();

		try {
			String v_IdConcepto = "NUEDIF";
			String v_IdEstadoAutorizacion = "PEN";
			Long v_IdDivisionPais = in.p_IdDivisionPais != null ? in.p_IdDivisionPais : 0L;
			String v_CodigoCompuesto = in.p_CodigoCompuesto != null ? in.p_CodigoCompuesto : "";

			// Obtener fecha del sistema
			String v_FechaIngreso = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			// Generar secuencia para el Id
			List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica", 1L);
			Long v_IdDireccionFisica = secuencia.get(0);

			// El SP deja asignado el RefId apenas se genera el Id, independiente del flujo posterior
			salida.P_OUT_REFID = v_IdDireccionFisica;

			// Insertar direccion fisica
			logger.debug("INSERT direccion fisica");
			InsertDireccionFisica insertDireccion = new InsertDireccionFisica();
			insertDireccion.IdDireccionFisica = v_IdDireccionFisica;
			insertDireccion.Calle = in.p_Calle;
			insertDireccion.Numero = in.p_Numero;
			insertDireccion.Departamento = in.p_Departamento;
			insertDireccion.Sector = in.p_Sector;
			insertDireccion.CodigoPostal = in.p_CodigoPostal;
			insertDireccion.IdCiudad = in.p_IdCiudad;
			insertDireccion.IdDivisionPais = v_IdDivisionPais;
			insertDireccion.CodigoProposito = in.p_CodigoProposito;
			insertDireccion.Comentario = in.p_Comentario;
			insertDireccion.IdEstado = in.p_IdEstado;
			insertDireccion.FechaIngreso = v_FechaIngreso;
			insertDireccion.IdPersona = in.p_IdPersona;
			insertDireccion.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			insertDireccion.IdUsuarioCreador = in.p_IdUsuario;
			insertDireccion.CodigoCompuesto = v_CodigoCompuesto;

			List<InsertDireccionFisica> insertList = new ArrayList<>();
			insertList.add(insertDireccion);
			sql.batch(conn, SQL_INSERT_DIRECCION_FISICA, insertList, InsertDireccionFisica.class);

			// Consultar si requiere autorizacion
			logger.debug("Consultando si requiere autorización");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, v_IdConcepto);

			if ("S".equals(v_RequiereAutorizacion) && !"N".equals(in.p_UsaReglasNegocio)) {
				// Obtener glosa para el evento
				logger.debug("Obtener glosa de la persona");
				HashMap<String, Object> paramsGlosa = new HashMap<>();
				paramsGlosa.put("IdPersona", in.p_IdPersona);
				paramsGlosa.put("Calle", in.p_Calle);
				paramsGlosa.put("Numero", in.p_Numero);

				List<GlosaResult> glosaList = sql.query(conn, SQL_SELECT_GLOSA, paramsGlosa, GlosaResult.class);
				String v_Glosa = (glosaList != null && !glosaList.isEmpty()) ? glosaList.get(0).Glosa : "";

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
				crearEvtIn.p_Fecha = v_FechaIngreso.substring(0, 10);
				crearEvtIn.p_IdConcepto = v_IdConcepto;
				crearEvtIn.p_NumeroOperacionConcepto = String.valueOf(v_IdDireccionFisica);
				crearEvtIn.p_FechaOperacionConcepto = v_FechaIngreso.substring(0, 10);
				crearEvtIn.p_IdCliente = in.p_IdPersona;
				crearEvtIn.p_Glosa = v_Glosa;
				crearEvtIn.p_IdCanal = "OPT";

				CrearEventoAutorizacion crearEvtService = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceSalida crearEvtOut = crearEvtService.logica(conn, crearEvtIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += crearEvtOut.p_out_AdvertenciasContador.intValue();
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : crearEvtOut.p_out_AdvertenciasDetalle) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += crearEvtOut.p_out_ErroresContador.intValue();
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : crearEvtOut.p_out_ErroresDetalle) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}

				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					String mensajeAutorizacion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<String, String>());
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", mensajeAutorizacion));
					salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
				}

			} else {
				// No requiere autorizacion: registrar log de regeneracion de contratos y confirmar directo
				logger.debug("Insertar log de regeneración de contratos");
				List<Long> secuenciaLog = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);

				InsertLogGenerarContratos logContratos = new InsertLogGenerarContratos();
				logContratos.Idlog = secuenciaLog.get(0);
				logContratos.CodigoFormulario = "DIRECCION_FISICA_";
				logContratos.CampoBackend = "Creado";
				logContratos.IdEmpresa = in.p_IdEmpresa;
				logContratos.IdCliente = 0L;
				logContratos.IdPersona = in.p_IdPersona;
				logContratos.IdUsuario = in.p_IdUsuario;
				logContratos.FechaSistema = v_FechaIngreso.substring(0, 10);
				logContratos.IdPrograma = 0L;
				logContratos.Estado = "PEN";
				logContratos.Mensaje = "Persona - CrearPersonaDireccionFisica.sql";

				List<InsertLogGenerarContratos> logList = new ArrayList<>();
				logList.add(logContratos);
				sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logList, InsertLogGenerarContratos.class);

				logger.debug("Confirmar creación de dirección física");
				ConfirmarCrearPersonaDireccionFisica.EntradaConfirmarCrearPersonaDireccionFisica confirmarIn =
					new ConfirmarCrearPersonaDireccionFisica.EntradaConfirmarCrearPersonaDireccionFisica();
				confirmarIn.p_IdEmpresa = in.p_IdEmpresa;
				confirmarIn.p_IdUsuario = in.p_IdUsuario;
				confirmarIn.p_IdModulo = in.p_IdModulo;
				confirmarIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				confirmarIn.p_IdDireccionFisica = v_IdDireccionFisica;
				confirmarIn.p_IdEstadoAutorizacion = "ING";

				ConfirmarCrearPersonaDireccionFisica confirmarService = new ConfirmarCrearPersonaDireccionFisica();
				ConfirmarCrearPersonaDireccionFisica.SalidaConfirmarCrearPersonaDireccionFisica confirmarOut = confirmarService.logica(conn, confirmarIn);

				salida.P_OUT_ADVERTENCIASCONTADOR += confirmarOut.P_OUT_ADVERTENCIASCONTADOR;
				for (ConfirmarCrearPersonaDireccionFisica.AdvertenciaDetalle adv : confirmarOut.P_OUT_ADVERTENCIASDETALLE) {
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(adv.Codigo, adv.RequiereAutorizacion, adv.Descripcion));
				}

				salida.P_OUT_ERRORESCONTADOR += confirmarOut.P_OUT_ERRORESCONTADOR;
				for (ConfirmarCrearPersonaDireccionFisica.ErrorDetalle err : confirmarOut.P_OUT_ERRORESDETALLE) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(err.Codigo, err.Descripcion));
				}
			}

		} catch (Exception ex) {
			logger.error("Error en CrearPersonaDireccionFisica: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_PERSONA_DIRECCION_FISICA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearPersonaDireccionFisica");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaCrearPersonaDireccionFisica.class);
		SalidaCrearPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Calle\":\"Calle Falsa\",\"p_Numero\":\"123\",\"p_Departamento\":\"\",\"p_Sector\":\"Centro\",\"p_CodigoPostal\":\"1234567\",\"p_IdCiudad\":1,\"p_IdDivisionPais\":0,\"p_CodigoProposito\":\"PART\",\"p_Comentario\":\"Prueba\",\"p_IdEstado\":\"ACT\",\"p_IdPersona\":35994,\"p_CodigoCompuesto\":\"\"}";
			EntradaCrearPersonaDireccionFisica entrada = gson.fromJson(json, EntradaCrearPersonaDireccionFisica.class);
			CrearPersonaDireccionFisica demo = new CrearPersonaDireccionFisica();
			SalidaCrearPersonaDireccionFisica salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
