package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorRelacionPersona.EntradaValidarExistenciaBorradorRelacionPersona;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TipoRelacion;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModificarBorradorRelacionPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorTarjetaBancaria.class);

	private String SQL_UPDATE_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorRelacionPersona_UpdateRelacionPersona.sql");
	
	public static class EntradaModificarBorradorRelacionPersona extends EntradaAdministrarBorrador{
		public List<RelacionPersona> p_RelacionesPersona;
		
		public EntradaModificarBorradorRelacionPersona(){
			this.p_RelacionesPersona = new ArrayList<RelacionPersona>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorRelacionPersona jin = gson.fromJson(in.toString(), EntradaModificarBorradorRelacionPersona.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorRelacionPersona entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorRelacionPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararRelacionesEntrantes(con, entrada);

		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_RelacionesPersona, RelacionPersona.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_RelacionesPersona, "TarjetasBancarias", out);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		EntradaValidarExistenciaBorradorRelacionPersona entradaValidarExistenciaBorradorRelacionPersona = new EntradaValidarExistenciaBorradorRelacionPersona();
		entradaValidarExistenciaBorradorRelacionPersona.p_RelacionesPersonas = entrada.p_RelacionesPersona;
		entradaValidarExistenciaBorradorRelacionPersona.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorRelacionPersona validarExistenciaBorradorRelacionPersona = new ValidarExistenciaBorradorRelacionPersona();
		List<Validacion> validaciones = validarExistenciaBorradorRelacionPersona.logica(con, entradaValidarExistenciaBorradorRelacionPersona).validaciones;
		out.agregarValidacionesSalida(validaciones);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		actualizarRelacionesPersona(con, entrada.p_RelacionesPersona);

		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_RelacionesPersona);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorRelacionPersona", finServicio - inicioServicio);
		return out;
	}

	private void prepararRelacionesEntrantes(Connection con, EntradaModificarBorradorRelacionPersona entrada) throws SQLException {
		logger.trace("INICIO::ModificarBorradorRelacionPersona.prepararRelacionesEntrantes");
		long inicio = System.currentTimeMillis();

		List<String> tipoRelacionIds = entrada.p_RelacionesPersona.stream().filter(u -> u.IdTipoRelacion != null).map(u -> u.IdTipoRelacion).collect(Collectors.toList());
		Map<String, TipoRelacion> tipoRelacionPorIdTipoRelacion = EnrolamientoUtils.obtenerTipoRelacionPorIdTipoRelacion(con, tipoRelacionIds);

		for (RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			relacionPersona.EstaInsertada = "S";

			TipoRelacion tipoRelacion = tipoRelacionPorIdTipoRelacion.getOrDefault(relacionPersona.IdTipoRelacion, new TipoRelacion());
			relacionPersona.TipoRelacionUsaCondicionesRelacion = tipoRelacion.UsaCondicionesRelacion;
			relacionPersona.TipoRelacionEsIndefinida = tipoRelacion.EsIndefinida;
			relacionPersona.TipoRelacionUsaFinalidadRelacion = tipoRelacion.UsaFinalidadRelacion;
			relacionPersona.TipoRelacionUsaGradoVinculacionParental = tipoRelacion.UsaGradoVinculacionParental;

			boolean esAGYRLE = "AGYRLE".equals(tipoRelacion.IdTipoRelacion);
			boolean esPERDEC = "PERDEC".equals(tipoRelacion.IdTipoRelacion);
			boolean noUsaPorcentajeParticipacion = "N".equals(tipoRelacion.UsaPorcentajeParticipacion);
			relacionPersona.TipoRelacionUsaPorcentajeParticipacion = esAGYRLE || esPERDEC || noUsaPorcentajeParticipacion ? "N" : "S";
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarBorradorRelacionPersona.prepararRelacionesEntrantes {}", fin - inicio);
	}
	
	private void actualizarRelacionesPersona(Connection con, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::ModificarBorradorRelacionPersona.actualizarRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorRelacionPersona.actualizarRelacionesPersona", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<RelacionPersona> relacionesPersona){
		logger.trace("INICIO::ModificarBorradorRelacionPersona.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = relacionesPersona.parallelStream().map(relacionPersona->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = relacionPersona.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = relacionPersona.IdRelacionPersona;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorRelacionPersona.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"RELACIONES_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_RelacionesPersona\":[{\"IdInterno\":1,\"IdRelacionPersona\":273,\"IdEnrolamientoOrigen\":456,\"IdTipoRelacion\":\"\",\"IdPersonaDestino\":0,\"FechaInicioRelacion\":\"0001-01-01 00:00:00\",\"FechaFinRelacion\":\"0001-01-01 00:00:00\",\"PorcentajeParticipacion\":0,\"RetiraDocumentos\":\"N\",\"FinalidadRelacion\":\"\",\"CondicionesRelacion\":\"\",\"IdGradoVinculacionParental\":0,\"Ciudad\":\"ARICA\",\"Calle\":\"ASD\",\"Numero\":\"123\",\"Telefono\":\"+56 1 2345 6789\",\"Email\":\"ASD@ASD.COM\",\"Complemento\":\"ASDASD\"}]}";
			EntradaModificarBorradorRelacionPersona entrada = Utils.getGsonBuilder().fromJson(json, EntradaModificarBorradorRelacionPersona.class);
			System.out.println("ENTRADA");
			System.out.println(entrada);

			try {
				SalidaAdministrarBorrador salida = new ModificarBorradorRelacionPersona().logica(conn, entrada);
				System.out.println("SALIDA");
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
