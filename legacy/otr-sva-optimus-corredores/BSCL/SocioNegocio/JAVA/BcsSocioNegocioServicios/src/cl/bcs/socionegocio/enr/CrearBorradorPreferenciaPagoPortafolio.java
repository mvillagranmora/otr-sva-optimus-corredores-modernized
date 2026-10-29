package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorPreferenciaPagoPortafolio implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorPreferenciaPagoPortafolio.class);
	
	private String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorPreferenciaPagoPortafolio_InsertPreferenciaPagoPortafolio.sql");
	
	public static class EntradaCrearBorradorPreferenciaPagoPortafolio extends EntradaAdministrarBorrador{
		public List<PreferenciaPagoPortafolio> p_PreferenciasPagoPortafolio;
		
		public EntradaCrearBorradorPreferenciaPagoPortafolio(){
			this.p_PreferenciasPagoPortafolio = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorPreferenciaPagoPortafolio jin = gson.fromJson(in.toString(), EntradaCrearBorradorPreferenciaPagoPortafolio.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorPreferenciaPagoPortafolio entrada) throws Exception {
		logger.info("INICIO::CrearBorradorPreferenciaPagoPortafolio");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararPreferenciasPagoEntrantes(entrada);
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_PreferenciasPagoPortafolio, PreferenciaPagoPortafolio.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_PreferenciasPagoPortafolio, "PreferenciasPagoPortafolio", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		out = validarCamposEntrada(con, entrada);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = obtenerPreferenciasPagoPortafolio(con, entrada.p_PreferenciasPagoPortafolio);
		
		insertarPreferenciasPagoPortafolio(con, preferenciasPagoPortafolio);
		
		out.P_OUT_REFID = obtenerIdsInsertados(preferenciasPagoPortafolio);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorPreferenciaPagoPortafolio", finServicio - inicioServicio);
		return out;
	}

	private void prepararPreferenciasPagoEntrantes(EntradaCrearBorradorPreferenciaPagoPortafolio entrada) {
		logger.trace("INICIO::CrearBorradorPreferenciaPagoPortafolio.prepararPreferenciasPagoEntrantes");
		long inicio = System.currentTimeMillis();

		for (PreferenciaPagoPortafolio preferenciaPago : entrada.p_PreferenciasPagoPortafolio) {
			preferenciaPago.EsBorrador = "S";
			preferenciaPago.EstaInsertada = "N";
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorPreferenciaPagoPortafolio.prepararPreferenciasPagoEntrantes {}", fin - inicio);
	}

	private List<PreferenciaPagoPortafolio> obtenerPreferenciasPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> borradoresPreferenciasPagoPortafolio) throws Exception{
		logger.trace("INICIO::CrearBorradorPreferenciaPagoPortafolio.obtenerPreferenciasPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorPreferenciaPagoPortafolio", Long.valueOf(borradoresPreferenciasPagoPortafolio.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = borradoresPreferenciasPagoPortafolio.parallelStream().map(borradorPreferenciaPagoPortafolio->{
			Long idPreferenciaPagoPortafolio = secuencias.get(index.getAndIncrement());
			PreferenciaPagoPortafolio preferenciaPagoPortafolio = obtenerEntidadPreferenciaPagoPortafolio(borradorPreferenciaPagoPortafolio, idPreferenciaPagoPortafolio);
			return preferenciaPagoPortafolio;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorPreferenciaPagoPortafolio.obtenerPreferenciasPagoPortafolio", fin - inicio);
		return preferenciasPagoPortafolio;
	}
	
	private PreferenciaPagoPortafolio obtenerEntidadPreferenciaPagoPortafolio(PreferenciaPagoPortafolio borradorCuentaCustodioPortafolio, Long idPreferenciaPagoPortafolio) {
		logger.trace("INICIO::CrearBorradorPreferenciaPagoPortafolio.obtenerEntidadPreferenciaPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		PreferenciaPagoPortafolio preferenciaPagoPortafolio = new PreferenciaPagoPortafolio();
		preferenciaPagoPortafolio.IdInterno = borradorCuentaCustodioPortafolio.IdInterno;
		preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio = idPreferenciaPagoPortafolio;
		preferenciaPagoPortafolio.IdEnrolamiento = borradorCuentaCustodioPortafolio.IdEnrolamiento;
		preferenciaPagoPortafolio.IdPreferenciaPago = borradorCuentaCustodioPortafolio.IdPreferenciaPago;
		preferenciaPagoPortafolio.IdPersonaRelacionada = borradorCuentaCustodioPortafolio.IdPersonaRelacionada;
		preferenciaPagoPortafolio.IdCuentaBancaria = borradorCuentaCustodioPortafolio.IdCuentaBancaria;
		preferenciaPagoPortafolio.IdEmpresa = borradorCuentaCustodioPortafolio.IdEmpresa;
		preferenciaPagoPortafolio.IdTarjetaBancaria = borradorCuentaCustodioPortafolio.IdTarjetaBancaria;
		preferenciaPagoPortafolio.PorDefecto = borradorCuentaCustodioPortafolio.PorDefecto;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorPreferenciaPagoPortafolio.obtenerEntidadPreferenciaPagoPortafolio", fin - inicio);
		return preferenciaPagoPortafolio;
	}
	
	private void insertarPreferenciasPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio) throws Exception {
		logger.debug("INICIO::CrearBorradorPreferenciaPagoPortafolio.insertarPreferenciasPagoPortafolio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasPagoPortafolio, PreferenciaPagoPortafolio.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorPreferenciaPagoPortafolio.insertarPreferenciasPagoPortafolio", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio){
		logger.trace("INICIO::CrearBorradorPreferenciaPagoPortafolio.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = preferenciasPagoPortafolio.parallelStream().map(preferenciaPagoPortafolio->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = preferenciaPagoPortafolio.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorPreferenciaPagoPortafolio.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	private SalidaAdministrarBorrador validarCamposEntrada(Connection con, EntradaCrearBorradorPreferenciaPagoPortafolio entrada) throws Exception {
		logger.trace("INICIO::CrearBorradorPreferenciaPagoPortafolio.validarCamposEntrada");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		for(PreferenciaPagoPortafolio preferencia : entrada.p_PreferenciasPagoPortafolio) {
			if(preferencia.IdPreferenciaPago == null || preferencia.IdPreferenciaPago.isEmpty()) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_PREFERENCIA_PAGO_NO_EXISTE", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorPreferenciaPagoPortafolio.validarCamposEntrada", fin - inicio);
		return out;
	}

	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_CodigoFormulario\":\"PREFERENCIAS_PAGO_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_UsaReglasNegocio\":\"S\",\"p_PreferenciasPagoPortafolio\":[{\"IdInterno\":1,\"IdEnrolamiento\":456,\"IdMedioPago\":\"CHE\",\"IdMoneda\":\"BRL\",\"IdPreferenciaPago\":\"VENFWD_CHE_BRL\",\"IdCuentaBancaria\":0,\"IdEmpresa\":1,\"IdTarjetaBancaria\":0,\"PorDefecto\":\"N\"}]}\n";
			EntradaCrearBorradorPreferenciaPagoPortafolio entrada = Utils.getGsonBuilder().fromJson(json, EntradaCrearBorradorPreferenciaPagoPortafolio.class);
			try {
				SalidaAdministrarBorrador salida = new CrearBorradorPreferenciaPagoPortafolio().logica(conn, entrada);
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
