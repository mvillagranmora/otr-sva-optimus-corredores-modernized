package cl.bcs.portafolio;

import static cl.bcs.plataforma.pla.utils.PlataformaUtils.loadResourceToString;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceEntrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV;
import cl.bcs.APV.ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio para la creación de cuenta custodio.
 * Migración completa de CrearCuentaCustodio.sql
 * 
 * Autor: Migración Java
 * Fecha: Mayo 2026
 */
public class CrearCuentaCustodio implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearCuentaCustodio.class);

	private final SQL sql = new SQL();

	// Constantes
	private final String ID_CONCEPTO = "NPCCUS";
	private final String PROPOSITO_APV = "PROPOSITOAPV";

	// SQLs para operaciones
	private final String SQL_INSERT_CUENTA_CUSTODIO = loadResourceToString(
			"cl/bcs/portafolio/sql/CrearCuentaCustodio_InsertCuentaCustodio.sql");

	private final String SQL_UPDATE_ESTADO_AUTORIZACION = loadResourceToString(
			"cl/bcs/portafolio/sql/CrearCuentaCustodio_UpdateEstadoAutorizacion.sql");

	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString(
			"cl/bcs/portafolio/sql/CrearCuentaCustodio_SelectPortafolio.sql");

	// Clases internas para entrada y salida
	public static class EntradaCrearCuentaCustodio {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPortafolio;
		public String p_IdBoveda;
		public String p_Codigo;
		public String p_IdEstado;
		public String p_IdTipoCuentaCustodia;
		public String p_IdTipoCustodia;
		public String p_IdModeloCustodia;
		public Long p_IdPersonaBancoCustodio;
		public Long p_IdPersonaBancoLiquidador;
		public String p_LiquidaDividendos;
		public String p_LiquidaCupones;
		public String p_LiquidaVencimientos;
		public String p_LiquidaPrepagos;
	}

	public static class SalidaCrearCuentaCustodio {
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public SalidaCrearCuentaCustodio() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new java.util.ArrayList<>();
		}
	}

	public static class CuentaCustodioInsert {
		public Long IdCuentaCustodioPortafolio;
		public Long IdPortafolio;
		public String IdBoveda;
		public String Codigo;
		public String FechaIngreso;
		public String FechaCreacion;
		public String FechaModificacion;
		public Long IdUsuarioCreador;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public String IdTipoCuentaCustodia;
		public String IdTipoCustodia;
		public String IdModeloCustodia;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public String LiquidaDividendos;
		public String LiquidaCupones;
		public String LiquidaVencimientos;
		public String LiquidaPrepagos;
	}

	public static class PortafolioInfo {
		public Long IdPortafolio;
		public String Proposito;
	}

	public static class UpdateEstadoAutorizacion {
		public Long IdCuentaCustodioPortafolio;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearCuentaCustodio entrada = gson.fromJson(in.toString(), EntradaCrearCuentaCustodio.class);
		SalidaCrearCuentaCustodio out = logica(conn, entrada);

		JsonParser jsonParser = new JsonParser();
		return jsonParser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaCrearCuentaCustodio logica(Connection conn, EntradaCrearCuentaCustodio in) throws Exception {
		logger.info("INICIO::CrearCuentaCustodio");
		long inicio = System.currentTimeMillis();

		SalidaCrearCuentaCustodio out = new SalidaCrearCuentaCustodio();

		// 1. Verificar si requiere autorización
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);

		// 2. Determinar IdEstadoAutorizacion
		String idEstadoAutorizacion = "S".equals(requiereAutorizacion) ? "PEN" : "AUT";

		// 3. Obtener secuencia para IdCuentaCustodioPortafolio
		List<Long> secuencia = Utils.getSecuencia(conn, "BSCL.Portafolio.BD.Secuencias::CuentaCustodioPortafolio", 1L);
		Long idCuentaCustodioPortafolio = secuencia.get(0);
		out.P_OUT_REFID = idCuentaCustodioPortafolio;

		// 4. Obtener fecha del sistema
		String fechaIngreso = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);

		// 5. Preparar objeto para INSERT
		CuentaCustodioInsert insert = new CuentaCustodioInsert();
		insert.IdCuentaCustodioPortafolio = idCuentaCustodioPortafolio;
		insert.IdPortafolio = in.p_IdPortafolio;
		insert.IdBoveda = in.p_IdBoveda;
		insert.Codigo = in.p_Codigo;
		insert.FechaIngreso = fechaIngreso;
		insert.FechaCreacion = fechaIngreso;
		insert.FechaModificacion = fechaIngreso;
		insert.IdUsuarioCreador = in.p_IdUsuario;
		insert.IdEstado = in.p_IdEstado;
		insert.IdEstadoAutorizacion = idEstadoAutorizacion;
		
		// Aplicar valores DEFAULT
		insert.IdTipoCuentaCustodia = (in.p_IdTipoCuentaCustodia != null) ? in.p_IdTipoCuentaCustodia : "";
		insert.IdTipoCustodia = (in.p_IdTipoCustodia != null) ? in.p_IdTipoCustodia : "";
		insert.IdModeloCustodia = (in.p_IdModeloCustodia != null) ? in.p_IdModeloCustodia : "";
		insert.IdPersonaBancoCustodio = (in.p_IdPersonaBancoCustodio != null) ? in.p_IdPersonaBancoCustodio : 0L;
		insert.IdPersonaBancoLiquidador = (in.p_IdPersonaBancoLiquidador != null) ? in.p_IdPersonaBancoLiquidador : 0L;
		insert.LiquidaDividendos = (in.p_LiquidaDividendos != null) ? in.p_LiquidaDividendos : "N";
		insert.LiquidaCupones = (in.p_LiquidaCupones != null) ? in.p_LiquidaCupones : "N";
		insert.LiquidaVencimientos = (in.p_LiquidaVencimientos != null) ? in.p_LiquidaVencimientos : "N";
		insert.LiquidaPrepagos = (in.p_LiquidaPrepagos != null) ? in.p_LiquidaPrepagos : "N";

		// 6. Ejecutar INSERT
		sql.batch(conn, SQL_INSERT_CUENTA_CUSTODIO, java.util.Arrays.asList(insert), CuentaCustodioInsert.class);

		// 7. Flujo según requiera autorización
		if ("S".equals(requiereAutorizacion)) {
			// Crear evento de autorización
			CrearEventoAutorizacion servicioEvento = new CrearEventoAutorizacion();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			ServiceEntrada entradaEvento = new ServiceEntrada();
			entradaEvento.p_IdEmpresa = in.p_IdEmpresa;
			entradaEvento.p_IdUsuario = in.p_IdUsuario;
			entradaEvento.p_IdModulo = in.p_IdModulo;
			entradaEvento.p_UsaReglasNegocio = "N";
			entradaEvento.p_IdTipoEvento = ID_CONCEPTO;
			entradaEvento.p_IdModuloSolicita = in.p_IdModulo;
			entradaEvento.p_IdUsuarioSolicita = in.p_IdUsuario;
			entradaEvento.p_Fecha = fechaSistema;
			entradaEvento.p_IdConcepto = ID_CONCEPTO;
			entradaEvento.p_NumeroOperacionConcepto = String.valueOf(idCuentaCustodioPortafolio);
			entradaEvento.p_FechaOperacionConcepto = fechaSistema;
			entradaEvento.p_IdCliente = 0L;
			entradaEvento.p_Glosa = "Cuenta Custodio: " + in.p_Codigo;

			ServiceSalida salidaEvento = servicioEvento.logica(conn, entradaEvento);

			// Propagar errores y advertencias
			out.P_OUT_ERRORESCONTADOR = salidaEvento.p_out_ErroresContador;
			
			// Convertir advertencias a mensajes
			for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : salidaEvento.p_out_AdvertenciasDetalle) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, adv.Codigo, adv.Descripcion));
			}
			
			// Convertir errores a mensajes
			for (CrearEventoAutorizacion.ObjERRORESDETALLE err : salidaEvento.p_out_ErroresDetalle) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
			}

			// Si no hay errores, agregar mensaje de autorización requerida
			if (out.P_OUT_ERRORESCONTADOR == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<>(), conn, in.p_IdEmpresa));
			}

		} else {
			// No requiere autorización - Confirmar directamente
			
			// Actualizar estado a AUT (ya está en AUT desde el INSERT)
			UpdateEstadoAutorizacion update = new UpdateEstadoAutorizacion();
			update.IdCuentaCustodioPortafolio = idCuentaCustodioPortafolio;
			update.IdEstadoAutorizacion = "AUT";
			sql.batch(conn, SQL_UPDATE_ESTADO_AUTORIZACION, java.util.Arrays.asList(update), UpdateEstadoAutorizacion.class);

			// Verificar si es portafolio APV para replicar
			HashMap<String, Object> paramsPortafolio = new HashMap<>();
			paramsPortafolio.put("IdPortafolio", in.p_IdPortafolio);
			
			List<PortafolioInfo> portafolios = sql.query(conn, SQL_SELECT_PORTAFOLIO, paramsPortafolio, PortafolioInfo.class);
			
			if (!portafolios.isEmpty() && PROPOSITO_APV.equals(portafolios.get(0).Proposito)) {
				// Llamar a ReplicaPortafolioAPV
				ReplicaPortafolioAPV replicaService = new ReplicaPortafolioAPV();
				
				EntradaReplicaPortafolioAPV entradaReplica = new EntradaReplicaPortafolioAPV();
				entradaReplica.p_IdEmpresa = in.p_IdEmpresa;
				entradaReplica.p_IdUsuario = in.p_IdUsuario;
				entradaReplica.p_IdModulo = in.p_IdModulo;
				entradaReplica.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				entradaReplica.p_Portafolio = in.p_IdPortafolio;
				entradaReplica.p_IdEntidad = idCuentaCustodioPortafolio;
				entradaReplica.p_TipoEntidad = "CUENTACUSTODIO";
				entradaReplica.p_TipoOperacion = "I";

				SalidaReplicaPortafolioAPV salidaReplica = replicaService.logica(conn, entradaReplica);

				// Propagar errores si los hay
				if (salidaReplica.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_ERRORESCONTADOR += salidaReplica.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES.addAll(salidaReplica.P_OUT_MENSAJES);
					logger.warn("Errores en réplica APV para cuenta custodio {}: {}", 
							idCuentaCustodioPortafolio, salidaReplica.P_OUT_ERRORESCONTADOR);
				}
			}
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCuentaCustodio {}", fin - inicio);

		return out;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST CrearCuentaCustodio ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				// Test crear cuenta custodio
				System.out.println("\n=== TEST CREAR CUENTA CUSTODIO ===");
				String jsonEntrada = "{"
						+ "\"p_IdEmpresa\":1,"
						+ "\"p_IdUsuario\":4,"
						+ "\"p_IdModulo\":\"POR\","
						+ "\"p_UsaReglasNegocio\":\"N\","
						+ "\"p_IdPortafolio\":209343,"
						+ "\"p_IdBoveda\":\"COR\","
						+ "\"p_Codigo\":\"TEST-CC-JAVA-001\","
						+ "\"p_IdEstado\":\"ACT\","
						+ "\"p_LiquidaDividendos\":\"S\","
						+ "\"p_LiquidaCupones\":\"S\","
						+ "\"p_LiquidaVencimientos\":\"S\","
						+ "\"p_LiquidaPrepagos\":\"S\""
						+ "}";

				Gson gson = Utils.getGsonBuilder();
				EntradaCrearCuentaCustodio entrada = gson.fromJson(jsonEntrada, EntradaCrearCuentaCustodio.class);

				CrearCuentaCustodio servicio = new CrearCuentaCustodio();
				SalidaCrearCuentaCustodio salida = servicio.logica(conn, entrada);

				System.out.println("RefId: " + salida.P_OUT_REFID);
				System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);
				System.out.println("Mensajes: " + salida.P_OUT_MENSAJES.size());

				if (!salida.P_OUT_MENSAJES.isEmpty()) {
					System.out.println("\nMensajes:");
					for (MensajeSalida msg : salida.P_OUT_MENSAJES) {
						System.out.println("  - " + msg.codigo + " [" + msg.tipo + "]: " + msg.descripcion);
					}
				}

				conn.rollback();

			} catch (Exception e) {
				e.printStackTrace();
				conn.rollback();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n== FIN TEST ==");
	}
}
