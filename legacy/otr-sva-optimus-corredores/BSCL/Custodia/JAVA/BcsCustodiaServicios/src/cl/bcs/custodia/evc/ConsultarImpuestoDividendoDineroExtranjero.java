package cl.bcs.custodia.evc;

import java.math.BigDecimal;
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

public class ConsultarImpuestoDividendoDineroExtranjero implements JavaCall {
	private final static Logger logger = LogManager.getLogger(ConsultarImpuestoDividendoDineroExtranjero.class);
	private final String SQL_CONSULTA_IMPUESTO_DV_EXTRANJERO = loadResourceToString("cl/bcs/custodia/sql/ConsultarImpuestoDividendoDineroExtranjero_selectImpuestoDvExtranjero.sql");

	public static class ConsultarImpuestoDividendoDineroExtranjero_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<Emisor>					p_Emisores;
	}
	public static class ConsultarImpuestoDividendoDineroExtranjero_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<PaisImpuestoDividendoExtranjero> P_OUT_CONSULTA_IMPUESTO;
		
		public ConsultarImpuestoDividendoDineroExtranjero_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Emisor {
		String IdEmisor;
	}
	public static class PaisImpuestoDividendoExtranjero {
		public String AplicaImpuesto;
		public String AplicaGastoTransferencia;
		public Long IdPaisResidencia;
		public String IdEmisor;
		public BigDecimal PorcentajeImpuestoDefecto;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarImpuestoDividendoDineroExtranjero_Salida logica(Connection con, ConsultarImpuestoDividendoDineroExtranjero_Entrada in) throws Exception{
		logger.debug("INICIO::ConsultarImpuestoDividendoDineroExtranjero");
		ConsultarImpuestoDividendoDineroExtranjero_Salida out = new ConsultarImpuestoDividendoDineroExtranjero_Salida();
		SQL sql = new SQL();
		if (in.p_Emisores.size() > 0) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			List<String> listaIds = new ArrayList<String>();
			for(Emisor e: in.p_Emisores){
				listaIds.add(e.IdEmisor);
			}
			params.put("ids",listaIds);
			logger.debug("INICIO::SQL_CONSULTA_IMPUESTO_DV_EXTRANJERO");
			List<PaisImpuestoDividendoExtranjero> impuestosDV  = sql.query(con, SQL_CONSULTA_IMPUESTO_DV_EXTRANJERO, params, PaisImpuestoDividendoExtranjero.class);
			logger.debug("FIN::SQL_CONSULTA_IMPUESTO_DV_EXTRANJERO");
			out.P_OUT_CONSULTA_IMPUESTO = impuestosDV;
		}
		logger.debug("FIN::ConsultarImpuestoDividendoDineroExtranjero");
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultarImpuestoDividendoDineroExtranjero_Entrada in = gson.fromJson(jin.toString(), ConsultarImpuestoDividendoDineroExtranjero_Entrada.class);
		ConsultarImpuestoDividendoDineroExtranjero_Salida out = logica(conn, in);
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
						+ "  \"p_Emisores\": [\r\n"
						+ "    {\r\n"
						+ "      \"IdEmisor\": \"AENZAC1\"\r\n"
						+ "    }\r\n"
						+ "  ]\r\n"
						+ "}";
				ConsultarImpuestoDividendoDineroExtranjero_Entrada Objin = gson.fromJson(json, ConsultarImpuestoDividendoDineroExtranjero_Entrada.class);
				ConsultarImpuestoDividendoDineroExtranjero  consulta = new ConsultarImpuestoDividendoDineroExtranjero();
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
