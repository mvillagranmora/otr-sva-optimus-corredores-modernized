package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorPreferenciaPagoPortafolio.EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorPreferenciaPagoPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorPreferenciaPagoPortafolio.class);

	private String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorPreferenciaPagoPortafolio_UpdatePreferenciaPagoPortafolio.sql");
	private String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorPreferenciaPagoPortafolio_UpdatePreferenciaPagoPortafolioNoPredeterminados.sql");
	
	public static class EntradaModificarBorradorPreferenciaPagoPortafolio extends EntradaAdministrarBorrador{
		public List<PreferenciaPagoPortafolio> p_PreferenciasPagoPortafolio;
		
		public EntradaModificarBorradorPreferenciaPagoPortafolio(){
			this.p_PreferenciasPagoPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorPreferenciaPagoPortafolio jin = gson.fromJson(in.toString(), EntradaModificarBorradorPreferenciaPagoPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorPreferenciaPagoPortafolio entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorPreferenciaPagoPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararPreferenciasPagoEntrantes(entrada);

		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_PreferenciasPagoPortafolio, PreferenciaPagoPortafolio.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_PreferenciasPagoPortafolio, "PreferenciasPagoPortafolio", out);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio entradaValidarExistenciaBorradorPreferenciaPagoPortafolio = new EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio();
		entradaValidarExistenciaBorradorPreferenciaPagoPortafolio.p_PreferenciasPagoPortafolio = entrada.p_PreferenciasPagoPortafolio;
		entradaValidarExistenciaBorradorPreferenciaPagoPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorPreferenciaPagoPortafolio validarExistenciaBorradorPreferenciaPagoPortafolio = new ValidarExistenciaBorradorPreferenciaPagoPortafolio();
		List<Validacion> validaciones = validarExistenciaBorradorPreferenciaPagoPortafolio.logica(con, entradaValidarExistenciaBorradorPreferenciaPagoPortafolio).validaciones;
		out.agregarValidacionesSalida(validaciones);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		actualizarPreferenciasPagoPortafolio(con, entrada.p_PreferenciasPagoPortafolio);
		actualizarPreferenciasPagoPortafolioNoPredeterminadas(con, entrada.p_PreferenciasPagoPortafolio);

		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_PreferenciasPagoPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorPreferenciaPagoPortafolio", finServicio - inicioServicio);
		return out;
	}

	private void prepararPreferenciasPagoEntrantes(EntradaModificarBorradorPreferenciaPagoPortafolio entrada) {
		logger.trace("INICIO::ModificarBorradorPreferenciaPagoPortafolio.prepararPreferenciasPagoEntrantes");
		long inicio = System.currentTimeMillis();

		for (PreferenciaPagoPortafolio preferenciaPago : entrada.p_PreferenciasPagoPortafolio) {
			preferenciaPago.EsBorrador = "S";
			preferenciaPago.EstaInsertada = "S";
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarBorradorPreferenciaPagoPortafolio.prepararPreferenciasPagoEntrantes {}", fin - inicio);
	}

	private void actualizarPreferenciasPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.debug("INICIO::ModificarBorradorPreferenciaPagoPortafolio.actualizarPreferenciasPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasPagoPortafolio, PreferenciaPagoPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorPreferenciaPagoPortafolio.actualizarPreferenciasPagoPortafolio", fin - inicio);
	}
	
	private void actualizarPreferenciasPagoPortafolioNoPredeterminadas(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.debug("INICIO::ModificarBorradorPreferenciaPagoPortafolio.actualizarPreferenciasPagoPortafolioNoPredeterminadas");
		long inicio = System.currentTimeMillis();
		
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolioPredeterminadas = preferenciasPagoPortafolio.parallelStream().filter(preferenciaPagoPortafolio->"S".equals(preferenciaPagoPortafolio.PorDefecto)).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_NO_PREDETERMINADOS, preferenciasPagoPortafolioPredeterminadas, PreferenciaPagoPortafolio.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorPreferenciaPagoPortafolio.actualizarPreferenciasPagoPortafolioNoPredeterminadas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio){
		logger.trace("INICIO::ModificarBorradorPreferenciaPagoPortafolio.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsActualizados = preferenciasPagoPortafolio.parallelStream().map(preferenciaPagoPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = preferenciaPagoPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorPreferenciaPagoPortafolio.obtenerIdsActualizados", fin - inicio);
		return idsActualizados;
	}

	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_CodigoFormulario\":\"PREFERENCIAS_PAGO_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_UsaReglasNegocio\":\"S\",\"p_PreferenciasPagoPortafolio\":[{\"IdInterno\":1,\"IdPreferenciaPagoPortafolio\":3319,\"IdEnrolamiento\":456,\"IdMedioPago\":\"CHE\",\"IdMoneda\":\"BRL\",\"IdPreferenciaPago\":\"VENSPT_CHE_BRL\",\"IdCuentaBancaria\":0,\"IdEmpresa\":1,\"IdTarjetaBancaria\":0,\"PorDefecto\":\"N\"}]}\n";
			EntradaModificarBorradorPreferenciaPagoPortafolio entrada = Utils.getGsonBuilder().fromJson(json, EntradaModificarBorradorPreferenciaPagoPortafolio.class);
			try {
				SalidaAdministrarBorrador salida = new ModificarBorradorPreferenciaPagoPortafolio().logica(conn, entrada);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}
				System.out.println(salida);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
