package cl.bcs.custodia.evc;

import java.sql.Connection;
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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarImpuestoGastoEventoCapitalClienteDV implements JavaCall{
	private final static Logger logger = LogManager.getLogger(ConsultarImpuestoGastoEventoCapitalClienteDV.class);
	private final String SQL_CONSULTA_IMPUESTO_GASTO_DV = loadResourceToString("cl/bcs/custodia/sql/ConsultarImpuestoGastoEventoCapitalClienteDV_select.sql");

	public static class ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public Long							p_IdInstrumento;
		public Long							p_IdCliente;
	}

	public static class ConsultarImpuestoGastoEventoCapitalClienteDV_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ImpuestoGastoInstrumentoEventoDV> P_OUT_CONSULTA;
		
		public ConsultarImpuestoGastoEventoCapitalClienteDV_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ImpuestoGastoInstrumentoEventoDV {
		public String AplicaGasto;
		public String AplicaImpuesto;
		public Long IdInstrumento;
		public Long IdCliente;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarImpuestoGastoEventoCapitalClienteDV_Salida logica(Connection con, ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada in) throws Exception{
		logger.debug("INICIO::ConsultarGastoEventoCapitalDV");
		ConsultarImpuestoGastoEventoCapitalClienteDV_Salida out = new ConsultarImpuestoGastoEventoCapitalClienteDV_Salida();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdInstrumento",in.p_IdInstrumento);
		params.put("IdCliente",in.p_IdCliente);
		logger.debug("INICIO::SQL_CONSULTA_GASTO_DV");
		List<ImpuestoGastoInstrumentoEventoDV> res  = sql.query(con, SQL_CONSULTA_IMPUESTO_GASTO_DV, params, ImpuestoGastoInstrumentoEventoDV.class);
		logger.debug("FIN::SQL_CONSULTA_GASTO_DV");
		out.P_OUT_CONSULTA = res;
		logger.debug("FIN::ConsultarGastoEventoCapitalDV");
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada in = gson.fromJson(jin.toString(), ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada.class);
		ConsultarImpuestoGastoEventoCapitalClienteDV_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception  {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\r\n"
						+ "  \"p_IdEmpresa\": 1,\r\n"
						+ "  \"p_IdUsuario\": 97,\r\n"
						+ "  \"p_IdModulo\": \"CUS\",\r\n"
						+ "  \"p_IdInstrumento\": 260866,\r\n"
						+ "  \"p_IdCliente\": 6405\r\n"
						+ "}";
				ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada Objin = gson.fromJson(json,ConsultarImpuestoGastoEventoCapitalClienteDV_Entrada.class);
				ConsultarImpuestoGastoEventoCapitalClienteDV  consulta = new ConsultarImpuestoGastoEventoCapitalClienteDV();
				System.out.println(consulta.logica(con, Objin));
				//con.rollback();
				con.commit();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}
