package cl.bcs.ordenes;

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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInstruccionMotivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarInstruccionMotivo.class);
	
	private final String SQL_SELECT_MOTIVOS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInstruccionMotivo_SelectMotivos.sql");
	
	public static class EntradaConsultarInstruccionMotivo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_IdEstado;
		public String p_Tipo;
		public String p_Motivo;
		public String p_IdTipoCanalIngreso;
		public String p_Activo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarInstruccionMotivo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Motivo> P_OUT_MOTIVOS;

		public SalidaConsultarInstruccionMotivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_MOTIVOS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Motivo{
		public Long IdMotivo;
		public String IdEstado;
		public String CodigoEstado;
		public String Estado;
		public String Motivo;
		public String Tipo;
		public String IdTipoCanalIngreso;
		public String Canal;
		public String Activo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarInstruccionMotivo in = gson.fromJson(jin.toString(), EntradaConsultarInstruccionMotivo.class);
		
		SalidaConsultarInstruccionMotivo out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public SalidaConsultarInstruccionMotivo logica(Connection conn, EntradaConsultarInstruccionMotivo entrada) throws Exception {
		SalidaConsultarInstruccionMotivo salida = new SalidaConsultarInstruccionMotivo();
		
		SQL _sql = new SQL();
		
		int varIdEstado = 0;
		int varMotivo = 0;
		int varTipo = 0;
		int varIdTipoCanalIngreso = 0;
		int varActivo = 0;
		
		if (entrada.p_IdEstado == null || ("0").equals(entrada.p_IdEstado) || ("").equals(entrada.p_IdEstado)) {
			entrada.p_IdEstado = "";
			varIdEstado = 1;
		}
		
		if (entrada.p_Motivo == null || ("0").equals(entrada.p_Motivo) || ("").equals(entrada.p_Motivo)) {
			entrada.p_Motivo = "";
			varMotivo = 1;
		}
		
		if (entrada.p_Tipo == null || ("0").equals(entrada.p_Tipo) || ("").equals(entrada.p_Tipo)) {
			entrada.p_Tipo = "";
			varTipo = 1;
		}
		
		if (entrada.p_IdTipoCanalIngreso == null || ("0").equals(entrada.p_IdTipoCanalIngreso) || ("").equals(entrada.p_IdTipoCanalIngreso)) {
			entrada.p_IdTipoCanalIngreso = "";
			varIdTipoCanalIngreso = 1;
		}
		
		if (entrada.p_Activo == null || ("0").equals(entrada.p_Activo) || ("").equals(entrada.p_Activo)) {
			entrada.p_Activo = "";
			varActivo = 1;
		}
		
		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdEstado", entrada.p_IdEstado);
		params.put("var_IdEstado", varIdEstado);
		
		params.put("p_Motivo", entrada.p_Motivo);
		params.put("var_Motivo", varMotivo);
		
		params.put("p_Tipo", entrada.p_Tipo);
		params.put("var_Tipo", varTipo);
		
		params.put("p_IdTipoCanalIngreso", entrada.p_IdTipoCanalIngreso);
		params.put("var_IdTipoCanalIngreso", varIdTipoCanalIngreso);
		
		params.put("p_Activo", entrada.p_Activo);
		params.put("var_Activo", varActivo);
		
		List<Motivo> motivos = null;
		logger.debug("INICIO - Select ObtenerMotivos");
		motivos = _sql.query(conn, SQL_SELECT_MOTIVOS, params, Motivo.class);
		logger.debug("FIN - Select ObtenerMotivos"); 
		
		logger.debug("INICIO - Ciclo agregar Motivos");
		if(motivos != null && motivos.size() > 0) {
			for(Motivo motivo : motivos) {
				salida.P_OUT_MOTIVOS.add(motivo);
			}
		}
		logger.debug("FIN - Ciclo agregar Ordenes");
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\""
				+ ",\"p_IdEstado\":\"0\""
				+ ",\"p_Tipo\":\"A\""
				+ ",\"p_Motivo\":\"\""
				+ ",\"p_IdTipoCanalIngreso\":0"
				+ ",\"p_Activo\":0}";

		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarInstruccionMotivo entrada = gson.fromJson(jsonRequest, EntradaConsultarInstruccionMotivo.class);
			System.out.println("Entrada");
			System.out.println(entrada.toString());

			ConsultarInstruccionMotivo consultarInstruccionMotivo = new ConsultarInstruccionMotivo();
			try {
				SalidaConsultarInstruccionMotivo salidaConsultarInstruccionMotivo = consultarInstruccionMotivo.logica(conn, entrada);
				System.out.println("Salida");
				System.out.println(salidaConsultarInstruccionMotivo.toString());
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
