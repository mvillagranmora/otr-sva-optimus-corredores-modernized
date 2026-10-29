package cl.bcs.plataforma.lim;

import cl.bcs.plataforma.lim.pojo.Limite;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsultarLimitesCuentaPropia implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarLimitesCuentaPropia.class);
	
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_LIMITE_CUENTA_PROPIA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_CreateLocalTempTableLimiteCuentaPropia.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_LIMITE_CUENTA_PROPIA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_DropLocalTempTableLimiteCuentaPropia.sql");
	private final String SQL_INSERT_TEMP_LIMITE_CUENTA_PROPIA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_InsertTempLimiteCuentaPropia.sql");
	private final String SQL_SELECT_LIMITE_CUENTA_PROPIA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarLimitesCuentaPropia_SelectLimiteCuentaPropia.sql");
	
	public static class EntradaConsultarLimitesCuentaPropia extends BcsPojo {
		public Long p_IdEmpresa;
		public List<ClasificacionLimite> Clasificaciones;
		
		public EntradaConsultarLimitesCuentaPropia() {
			Clasificaciones = new ArrayList<>();
		}
	}
	
	public static class SalidaConsultarLimitesCuentaPropia extends BcsPojo {
		public Map<Long, List<Limite>> LimitesPorIdInterno;
		
		public SalidaConsultarLimitesCuentaPropia() {
			LimitesPorIdInterno = new HashMap<>();
		}
	}
	
	public static class ClasificacionLimite extends BcsPojo {
		public Long IdInterno;
		public Long IdInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoLimite;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String CodigoOperador;
		public String IdMonedaOperacion;
		public String IdEmisor;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaConsultarLimitesCuentaPropia logica(Connection conn, EntradaConsultarLimitesCuentaPropia entrada) throws SQLException {
		logger.info("INICIO::ConsultarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarLimitesCuentaPropia salida = new SalidaConsultarLimitesCuentaPropia();
		
		crearTablaTemporal(conn);
		insertarClasificacionesSolicitadas(conn, entrada.Clasificaciones);
		List<Limite> limitesSolicitados = obtenerLimitesCuentaPropia(conn,entrada);
		salida.LimitesPorIdInterno = obtenerLimitesPorIdInterno(limitesSolicitados);
		eliminarTablaTemporal(conn);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarLimitesCuentaPropia {}", fin - inicio);
		
		return salida;
	}
	
	private void crearTablaTemporal(Connection conn) throws SQLException {
		logger.debug("INICIO::ConsultarLimitesCuentaPropia.crearTablaTemporal");
		long inicio = System.currentTimeMillis();
		
		SQL.exec(conn, SQL_CREATE_LOCAL_TEMP_TABLE_LIMITE_CUENTA_PROPIA);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarLimitesCuentaPropia.crearTablaTemporal {}", fin - inicio);
	}
	
	private void eliminarTablaTemporal(Connection conn) throws SQLException {
		logger.debug("INICIO::ConsultarLimitesCuentaPropia.eliminarTablaTemporal");
		long inicio = System.currentTimeMillis();
		
		SQL.exec(conn, SQL_DROP_LOCAL_TEMP_TABLE_LIMITE_CUENTA_PROPIA);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarLimitesCuentaPropia.eliminarTablaTemporal {}", fin - inicio);
	}
	
	private void insertarClasificacionesSolicitadas(Connection conn, List<ClasificacionLimite> clasificaciones) throws SQLException {
		logger.debug("INICIO::ConsultarLimitesCuentaPropia.insertarClasificacionesSolicitadas");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(conn, SQL_INSERT_TEMP_LIMITE_CUENTA_PROPIA, clasificaciones, ClasificacionLimite.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarLimitesCuentaPropia.insertarClasificacionesSolicitadas {}", fin - inicio);
	}
	
	private List<Limite> obtenerLimitesCuentaPropia(Connection conn, EntradaConsultarLimitesCuentaPropia entrada) throws SQLException {
		logger.debug("INICIO::ConsultarLimitesCuentaPropia.obtenerLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> params = new HashMap<>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<Limite> limites = new SQL().query(conn, SQL_SELECT_LIMITE_CUENTA_PROPIA, params, Limite.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarLimitesCuentaPropia.obtenerLimitesCuentaPropia {}", fin - inicio);
		
		return limites;
	}
	
	private Map<Long, List<Limite>> obtenerLimitesPorIdInterno(List<Limite> limites) {
		logger.trace("INICIO::ConsultarLimitesCuentaPropia.obtenerLimitesPorIdInterno");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<Limite>> limitesPorId = limites.parallelStream().collect(Collectors.groupingBy((u) -> u.IdInterno));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarLimitesCuentaPropia.obtenerLimitesPorIdInterno {}", fin - inicio);
		
		return limitesPorId;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"Clasificaciones\":[{\"IdInterno\":1,\"IdInstrumento\":null,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":null,\"IdTipoLimite\":null,\"IdTipoOperacion\":null,\"IdTipoCanalIngreso\":null,\"CodigoOperador\":null,\"IdMonedaOperacion\":null}]}";
				EntradaConsultarLimitesCuentaPropia entrada = Utils.getGsonBuilder().fromJson(json, EntradaConsultarLimitesCuentaPropia.class);
				ConsultarLimitesCuentaPropia consultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia();
				SalidaConsultarLimitesCuentaPropia salidaConsultarLimitesCuentaPropia = consultarLimitesCuentaPropia.logica(conn, entrada);
				System.out.println(salidaConsultarLimitesCuentaPropia);
			} finally {
				conn.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
