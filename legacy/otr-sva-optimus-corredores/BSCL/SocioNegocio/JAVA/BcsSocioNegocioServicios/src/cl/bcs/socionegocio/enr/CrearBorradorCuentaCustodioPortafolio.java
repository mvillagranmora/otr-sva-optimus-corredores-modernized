package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorCuentaCustodioPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorCuentaCustodioPortafolio.class);
	
	private String SQL_INSERT_CUENTA_CUSTODIO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorCuentaCustodioPortafolio_InsertCuentaCustodioPortafolio.sql");
	
	public static class EntradaCrearBorradorCuentaCustodioPortafolio extends EntradaAdministrarBorrador{
		public List<CuentaCustodioPortafolio> p_CuentasCustodioPortafolio;
		
		public EntradaCrearBorradorCuentaCustodioPortafolio(){
			this.p_CuentasCustodioPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorCuentaCustodioPortafolio jin = gson.fromJson(in.toString(), EntradaCrearBorradorCuentaCustodioPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorCuentaCustodioPortafolio entrada) throws Exception {
		logger.info("INICIO::CrearBorradorCuentaCustodioPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararCuentasCustodioEntrantes(entrada);
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_CuentasCustodioPortafolio, CuentaCustodioPortafolio.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_CuentasCustodioPortafolio, "CuentasCustodioPortafolio", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<CuentaCustodioPortafolio> cuentasCustodioPortafolio = obtenerCuentasCustodioPortafolio(con, entrada.p_CuentasCustodioPortafolio);
		
		insertarCuentasCustodioPortafolio(con, cuentasCustodioPortafolio);
		
		out.P_OUT_REFID = obtenerIdsInsertados(cuentasCustodioPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorCuentaCustodioPortafolio", finServicio - inicioServicio);
		return out;
	}

	private void prepararCuentasCustodioEntrantes(EntradaCrearBorradorCuentaCustodioPortafolio entrada) {
		logger.trace("INICIO::CrearBorradorCuentaCustodioPortafolio.prepararCuentasCustodioEntrantes");
		long inicio = System.currentTimeMillis();

		for (CuentaCustodioPortafolio cuentaCustodio : entrada.p_CuentasCustodioPortafolio) {
			cuentaCustodio.EstaInsertada = "N";
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaCustodioPortafolio.prepararCuentasCustodioEntrantes {}", fin - inicio);
	}

	private List<CuentaCustodioPortafolio> obtenerCuentasCustodioPortafolio(Connection con, List<CuentaCustodioPortafolio> borradoresCuentasCustodioPortafolio) throws Exception{
		logger.trace("INICIO::CrearBorradorCuentaCustodioPortafolio.obtenerCuentasCustodioPortafolio");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorCuentaCustodioPortafolio", Long.valueOf(borradoresCuentasCustodioPortafolio.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<CuentaCustodioPortafolio> cuentasCustodioPortafolio = borradoresCuentasCustodioPortafolio.parallelStream().map(borradorCuentaCustodioPortafolio->{
			Long idCuentaCustodioPortafolio = secuencias.get(index.getAndIncrement());
			CuentaCustodioPortafolio cuentaCustodioPortafolio = obtenerEntidadCuentaCustodioPortafolio(borradorCuentaCustodioPortafolio, idCuentaCustodioPortafolio);
			return cuentaCustodioPortafolio;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaCustodioPortafolio.obtenerCuentasCustodioPortafolio", fin - inicio);
		return cuentasCustodioPortafolio;
	}
	
	private CuentaCustodioPortafolio obtenerEntidadCuentaCustodioPortafolio(CuentaCustodioPortafolio borradorCuentaCustodioPortafolio, Long idCuentaCustodioPortafolio) {
		logger.trace("INICIO::CrearBorradorCuentaCustodioPortafolio.obtenerCuentasCustodioPortafolio");
		long inicio = System.currentTimeMillis();
		
		CuentaCustodioPortafolio cuentaCustodioPortafolio = new CuentaCustodioPortafolio();
		cuentaCustodioPortafolio.IdInterno = borradorCuentaCustodioPortafolio.IdInterno;
		cuentaCustodioPortafolio.IdCuentaCustodioPortafolio = idCuentaCustodioPortafolio;
		cuentaCustodioPortafolio.IdEnrolamiento = borradorCuentaCustodioPortafolio.IdEnrolamiento;
		cuentaCustodioPortafolio.IdBoveda = borradorCuentaCustodioPortafolio.IdBoveda;
		cuentaCustodioPortafolio.Codigo = borradorCuentaCustodioPortafolio.Codigo;
		cuentaCustodioPortafolio.IdModeloCustodia = borradorCuentaCustodioPortafolio.IdModeloCustodia;
		cuentaCustodioPortafolio.IdTipoCustodia = borradorCuentaCustodioPortafolio.IdTipoCustodia;
		cuentaCustodioPortafolio.IdTipoCuentaCustodia = borradorCuentaCustodioPortafolio.IdTipoCuentaCustodia;
		cuentaCustodioPortafolio.LiquidaDividendos = borradorCuentaCustodioPortafolio.LiquidaDividendos;
		cuentaCustodioPortafolio.LiquidaCupones = borradorCuentaCustodioPortafolio.LiquidaCupones;
		cuentaCustodioPortafolio.LiquidaPrepagos = borradorCuentaCustodioPortafolio.LiquidaPrepagos;
		cuentaCustodioPortafolio.IdPersonaBancoCustodio = borradorCuentaCustodioPortafolio.IdPersonaBancoCustodio;
		cuentaCustodioPortafolio.IdPersonaBancoLiquidador = borradorCuentaCustodioPortafolio.IdPersonaBancoLiquidador;
		cuentaCustodioPortafolio.LiquidaVencimientos = borradorCuentaCustodioPortafolio.LiquidaVencimientos;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaCustodioPortafolio.obtenerCuentasCustodioPortafolio", fin - inicio);
		return cuentaCustodioPortafolio;
	}
	
	private void insertarCuentasCustodioPortafolio(Connection con, List<CuentaCustodioPortafolio> cuentasCustodioPortafolio) throws Exception {
		logger.debug("INICIO::CrearBorradorCuentaCustodioPortafolio.insertarCuentasCustodioPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CUENTA_CUSTODIO_PORTAFOLIO, cuentasCustodioPortafolio, CuentaCustodioPortafolio.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorCuentaCustodioPortafolio.insertarCuentasCustodioPortafolio", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<CuentaCustodioPortafolio> cuentasCustodioPortafolio){
		logger.trace("INICIO::CrearBorradorCuentaCustodioPortafolio.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = cuentasCustodioPortafolio.parallelStream().map(cuentaCustodioPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaCustodioPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaCustodioPortafolio.IdCuentaCustodioPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorCuentaCustodioPortafolio.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_CodigoFormulario\":\"CUENTA_CUSTODIO_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"S\",\"p_CuentasCustodioPortafolio\":[{\"IdEnrolamiento\":456,\"IdBoveda\":\"AAA\",\"Codigo\":\"ASDASD\",\"IdTipoCuentaCustodia\":\"DEPOSITANTE\",\"LiquidaDividendos\":\"N\",\"LiquidaCupones\":\"N\",\"LiquidaVencimientos\":\"N\",\"LiquidaPrepagos\":\"N\"}]}\n";
			EntradaCrearBorradorCuentaCustodioPortafolio entrada = Utils.getGsonBuilder().fromJson(json, EntradaCrearBorradorCuentaCustodioPortafolio.class);
			try {
				SalidaAdministrarBorrador salida = new CrearBorradorCuentaCustodioPortafolio().logica(conn, entrada);
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
