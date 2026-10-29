package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class BloqueoTemporalClientePorDesactualizacionDatos implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(BloqueoTemporalClientePorDesactualizacionDatos.class);
	
	private final String SQL_SELECT_CLIENTE_DESACTUALIZADOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/BloqueoTemporalClientePorDesactualizacionDatos_SelectClienteDesactualizados.sql");
	private final String SQL_UPDATE_CLIENTE_DESACTUALIZADOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/BloqueoTemporalClientePorDesactualizacionDatos_UpdateClienteDesactualizados.sql");
	
	private final Long NUMERO_DIAS_DEFECTO = 365L;
	
	public static class EntradaBloqueoTemporalClientePorDesactualizacionDatos extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	}
	
	public static class SalidaBloqueoTemporalClientePorDesactualizacionDatos extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaBloqueoTemporalClientePorDesactualizacionDatos() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Cliente extends BcsPojo{
		public Long IdCliente;
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		EntradaBloqueoTemporalClientePorDesactualizacionDatos in = gson.fromJson(jin.toString() ,EntradaBloqueoTemporalClientePorDesactualizacionDatos.class);
		SalidaBloqueoTemporalClientePorDesactualizacionDatos out = logica(con,in);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaBloqueoTemporalClientePorDesactualizacionDatos logica(Connection con, EntradaBloqueoTemporalClientePorDesactualizacionDatos entrada) throws Exception {
		logger.info("INICIO::BloqueoTemporalClientePorDesactualizacionDatos");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaBloqueoTemporalClientePorDesactualizacionDatos out = new SalidaBloqueoTemporalClientePorDesactualizacionDatos();
		
		String fechaLimiteActualizacionDatos = obtenerFechaLimiteActualizacionDatos(con, entrada.p_IdEmpresa);
		List<Cliente> clientesDesactualizados = obtenerClientesDesactualizados(con, fechaLimiteActualizacionDatos, entrada.p_IdEmpresa);
		bloquearClientesDesactualizados(con, clientesDesactualizados);
		
		MensajeSalida mensajeSalida = obtenerMensajeSalida(con, clientesDesactualizados, entrada.p_IdEmpresa);
		out.P_OUT_MENSAJES.add(mensajeSalida);
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::BloqueoTemporalClientePorDesactualizacionDatos", finServicio - inicioServicio);
		return out;
	}
	
	private String obtenerFechaLimiteActualizacionDatos(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.obtenerFechaLimiteActualizacionDatos");
		long inicio = System.currentTimeMillis();
		
		Long numeroDias = obtenerParametroNumeroDias(con, idEmpresa);
		java.sql.Date fechaSistema = obtenerFechaSistema(con, idEmpresa);
		java.sql.Date fechaCalculada = Fecha.addDays(fechaSistema, numeroDias.intValue() * -1);
		String fechaLimiteActualizacionDatos = Fecha.toStringDate(fechaCalculada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::BloqueoTemporalClientePorDesactualizacionDatos.obtenerFechaLimiteActualizacionDatos", fin - inicio);
		return fechaLimiteActualizacionDatos;
	}
	
	private Long obtenerParametroNumeroDias(Connection con, Long idEmpresa) {
		logger.trace("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.obtenerParametroNumeroDias");
		long inicio = System.currentTimeMillis();
		
		Long numeroDias = NUMERO_DIAS_DEFECTO;
		try {
			numeroDias = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.NumeroDiasClienteDesactualizados");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::BloqueoTemporalClientePorDesactualizacionDatos::Parametro PLA.NumeroDiasClienteDeasctualizados no existe, se tomara valor por defecto: " + NUMERO_DIAS_DEFECTO);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::BloqueoTemporalClientePorDesactualizacionDatos.obtenerParametroNumeroDias", fin - inicio);
		return numeroDias;
	}
	
	private java.sql.Date obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();
		
		String fechaSistemaConfigurada = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		java.sql.Date fechaSistema = Fecha.toDate(fechaSistemaConfigurada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::BloqueoTemporalClientePorDesactualizacionDatos.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}
	
	private List<Cliente> obtenerClientesDesactualizados(Connection con, String fechaLimiteActualizacionDatos, Long idEmpresa) throws Exception {
		logger.debug("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.obtenerClientesDesactualizados");
		long inicio = System.currentTimeMillis();
		
		String areaCliente = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.BloqueoArea");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("FechaLimiteActuliazacionDatos", fechaLimiteActualizacionDatos);
		parametros.put("AreaClienteParametro", areaCliente);
		
		SQL sql = new SQL();
		List<Cliente> clientesDesactualizados = sql.query(con, SQL_SELECT_CLIENTE_DESACTUALIZADOS, parametros, Cliente.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BloqueoTemporalClientePorDesactualizacionDatos.obtenerClientesDesactualizados", fin - inicio);
		return clientesDesactualizados;
	}
	
	private void bloquearClientesDesactualizados(Connection con, List<Cliente> clientesDesactualizados) throws Exception {
		logger.debug("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.bloquearClientesDesactualizados");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CLIENTE_DESACTUALIZADOS, clientesDesactualizados, Cliente.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::BloqueoTemporalClientePorDesactualizacionDatos.bloquearClientesDesactualizados", fin - inicio);
	}
	
	private MensajeSalida obtenerMensajeSalida(Connection con, List<Cliente> clientesDesactualizados, Long idEmpresa) throws Exception {
		logger.trace("INICIO::BloqueoTemporalClientePorDesactualizacionDatos.obtenerMensajeSalida");
		long inicio = System.currentTimeMillis();
		
		Integer cantidadClientes = clientesDesactualizados.size();
		HashMap<String, String> parametros = new HashMap<String, String>();
		parametros.put("Cantidad", String.valueOf(cantidadClientes));
		
		MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "CANTIDAD_CLIENTES_DESACTUALIZADOS_BLOQUEADOS_PAUTA", parametros, con, idEmpresa);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::BloqueoTemporalClientePorDesactualizacionDatos.obtenerMensajeSalida", fin - inicio);
		return mensajeSalida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaBloqueoTemporalClientePorDesactualizacionDatos entradaBloqueoTemporalClientePorDesactualizacionDatos = new EntradaBloqueoTemporalClientePorDesactualizacionDatos();
			entradaBloqueoTemporalClientePorDesactualizacionDatos.p_IdEmpresa = 1L;
			entradaBloqueoTemporalClientePorDesactualizacionDatos.p_IdUsuario = 21L;
			entradaBloqueoTemporalClientePorDesactualizacionDatos.p_IdModulo = "PLA";

			BloqueoTemporalClientePorDesactualizacionDatos bloqueoTemporalClientePorDesactualizacionDatos = new BloqueoTemporalClientePorDesactualizacionDatos();
			try {
				SalidaBloqueoTemporalClientePorDesactualizacionDatos salidaBloqueoTemporalClientePorDesactualizacionDatos = bloqueoTemporalClientePorDesactualizacionDatos.logica(con, entradaBloqueoTemporalClientePorDesactualizacionDatos);
				con.rollback();
				System.out.println(salidaBloqueoTemporalClientePorDesactualizacionDatos.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}
}
