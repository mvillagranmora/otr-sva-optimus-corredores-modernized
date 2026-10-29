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

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorCuentaCustodioPortafolio.EntradaValidarExistenciaBorradorCuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorCuentaCustodioPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorTarjetaBancaria.class);

	private String SQL_UPDATE_CUENTA_CUSTODIO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorCuentaCustodioPortafolio_UpdateCuentaCustodioPortafolio.sql");
	
	public static class EntradaModificarBorradorCuentaCustodioPortafolio extends EntradaAdministrarBorrador{
		public List<CuentaCustodioPortafolio> p_CuentasCustodioPortafolio;
		
		public EntradaModificarBorradorCuentaCustodioPortafolio(){
			this.p_CuentasCustodioPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorCuentaCustodioPortafolio jin = gson.fromJson(in.toString(), EntradaModificarBorradorCuentaCustodioPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorCuentaCustodioPortafolio entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorCuentaCustodioPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararCuentasCustodioEntrantes(entrada);

		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_CuentasCustodioPortafolio, CuentaCustodioPortafolio.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_CuentasCustodioPortafolio, "CuentasCustodioPortafolio", out);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		EntradaValidarExistenciaBorradorCuentaCustodioPortafolio entradaValidarExistenciaBorradorCuentaCustodioPortafolio = new EntradaValidarExistenciaBorradorCuentaCustodioPortafolio();
		entradaValidarExistenciaBorradorCuentaCustodioPortafolio.p_CuentasCustodioPortafolio = entrada.p_CuentasCustodioPortafolio;
		entradaValidarExistenciaBorradorCuentaCustodioPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorCuentaCustodioPortafolio validarExistenciaBorradorCuentaCustodioPortafolio = new ValidarExistenciaBorradorCuentaCustodioPortafolio();
		List<Validacion> validaciones = validarExistenciaBorradorCuentaCustodioPortafolio.logica(con, entradaValidarExistenciaBorradorCuentaCustodioPortafolio).validaciones;
		out.agregarValidacionesSalida(validaciones);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		actualizarCuentasCustodioPortafolio(con, entrada.p_CuentasCustodioPortafolio);

		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_CuentasCustodioPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorCuentaCustodioPortafolio", finServicio - inicioServicio);
		return out;
	}

	private void prepararCuentasCustodioEntrantes(EntradaModificarBorradorCuentaCustodioPortafolio entrada) {
		logger.info("INICIO::ModificarBorradorCuentaCustodioPortafolio.prepararCuentasCustodioEntrantes");
		long inicio = System.currentTimeMillis();

		for (CuentaCustodioPortafolio cuentaCustodio : entrada.p_CuentasCustodioPortafolio) {
			cuentaCustodio.EstaInsertada = "S";
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorCuentaCustodioPortafolio.prepararCuentasCustodioEntrantes {}", fin - inicio);
	}

	private void actualizarCuentasCustodioPortafolio(Connection con, List<CuentaCustodioPortafolio> cuentasCustodioPortafolio) throws Exception {
		logger.debug("INICIO::ModificarBorradorCuentaCustodioPortafolio.actualizarCuentasCustodioPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CUENTA_CUSTODIO_PORTAFOLIO, cuentasCustodioPortafolio, CuentaCustodioPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorCuentaCustodioPortafolio.actualizarCuentasCustodioPortafolio", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<CuentaCustodioPortafolio> cuentasCustodioPortafolio){
		logger.trace("INICIO::ModificarBorradorCuentaCustodioPortafolio.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsActualizados = cuentasCustodioPortafolio.parallelStream().map(cuentaCustodioPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaCustodioPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaCustodioPortafolio.IdCuentaCustodioPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorCuentaCustodioPortafolio.obtenerIdsActualizados", fin - inicio);
		return idsActualizados;
	}

	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_CodigoFormulario\":\"CUENTA_CUSTODIO_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_CuentasCustodioPortafolio\":[{\"IdEnrolamiento\":456,\"IdCuentaCustodioPortafolio\":340,\"IdBoveda\":\"AAA\",\"Codigo\":\"ASDASD\",\"IdTipoCuentaCustodia\":\"DEPOSITANTE\",\"LiquidaDividendos\":\"N\",\"LiquidaCupones\":\"N\",\"LiquidaVencimientos\":\"N\",\"LiquidaPrepagos\":\"N\"}]}\n";
			EntradaModificarBorradorCuentaCustodioPortafolio entrada = Utils.getGsonBuilder().fromJson(json, EntradaModificarBorradorCuentaCustodioPortafolio.class);
			try {
				SalidaAdministrarBorrador salida = new ModificarBorradorCuentaCustodioPortafolio().logica(conn, entrada);
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
