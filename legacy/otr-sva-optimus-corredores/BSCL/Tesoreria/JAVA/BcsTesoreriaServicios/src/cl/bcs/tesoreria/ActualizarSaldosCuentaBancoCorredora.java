package cl.bcs.tesoreria;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.CuentaBanco_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarSaldosCuentaBancoCorredora implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ActualizarSaldosCuentaBancoCorredora.class);
	
	private final String SQL_SELECT_CUENTAS_BANCARIAS = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBancoCorredora_SelectCuentasBancarias.sql");
	
	public static class EntradaActualizarSaldosCuentaBancoCorredora extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
	}
	
	public static class SalidaActualizarSaldosCuentaBancoCorredora extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public SalidaActualizarSaldosCuentaBancoCorredora() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarSaldosCuentaBancoCorredora in = gson.fromJson(jin.toString(), EntradaActualizarSaldosCuentaBancoCorredora.class);
		SalidaActualizarSaldosCuentaBancoCorredora out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarSaldosCuentaBancoCorredora logica(Connection con, EntradaActualizarSaldosCuentaBancoCorredora entrada) throws Exception {
		logger.info("INICIO::ActualizarSaldosCuentaBancoCorredora");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaActualizarSaldosCuentaBancoCorredora out = new SalidaActualizarSaldosCuentaBancoCorredora();
		
		List<CuentaBanco_Entrada> cuentasBancarias = obtenerCuentasBancariasCorredora(con);
		
		if(cuentasBancarias.size() > 0) {
			ActualizarSaldosCuentaBanco_Entrada entradaActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco_Entrada();	
			ActualizarSaldosCuentaBanco actualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco();
			
			entradaActualizarSaldosCuentaBanco.p_FechaMovimiento = entrada.p_FechaMovimiento;
			entradaActualizarSaldosCuentaBanco.p_CuentaBanco = cuentasBancarias;
			
			ActualizarSaldosCuentaBanco_Salida salidaActualizarSaldosCuentaBanco = actualizarSaldosCuentaBanco.logica(con, entradaActualizarSaldosCuentaBanco);
			
			out.P_OUT_MENSAJES.addAll(salidaActualizarSaldosCuentaBanco.P_OUT_MENSAJES);			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "SALDO_CUENTA_BANCO_CORREDORA_ACTUALIZADOS", new HashMap<String, String>(1), con, entrada.p_IdEmpresa));
			}
		} 
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ActualizarSaldosCuentaBancoCorredora", finServicio - inicioServicio);
		return out;
	}
	
	private List<CuentaBanco_Entrada> obtenerCuentasBancariasCorredora(Connection con) throws Exception {
		logger.debug("INICIO::ActualizarSaldosCuentaBancoCorredora.obtenerCuentasBancariasCorredora");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		
		SQL sql = new SQL();
		List<CuentaBanco_Entrada> cuentasBancarias = sql.query(con, SQL_SELECT_CUENTAS_BANCARIAS, parametros, CuentaBanco_Entrada.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarSaldosCuentaBancoCorredora.obtenerCuentasBancariasCorredora", fin - inicio);
		return cuentasBancarias;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EntradaActualizarSaldosCuentaBancoCorredora entrada = new EntradaActualizarSaldosCuentaBancoCorredora();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_FechaMovimiento = "2025-03-25";
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			ActualizarSaldosCuentaBancoCorredora demo = new ActualizarSaldosCuentaBancoCorredora();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.rollback();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
