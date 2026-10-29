package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.NetearCustodiaNegativa.EntradaNetearCustodiaNegativa;
import cl.bcs.custodia.NetearCustodiaNegativa.PortafolioInstrumento;
import cl.bcs.custodia.NetearCustodiaNegativa.SalidaNetearCustodiaNegativa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NetearClientes implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(NetearClientes.class);
	
	private final String SQL_SELECT_CARTERAS_NEGATIVAS = loadResourceToString("cl/bcs/custodia/sql/NetearClientes_SelectCarterasNegativas.sql");
	
	public static class EntradaNetearClientes extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public EntradaNetearClientes() {
		}
	}
	
	public static class CarteraNegativa extends BcsPojo {
		public Long IdPortafolio;
		public Long IdInstrumento;
	}
	
	public static class SalidaNetearClientes extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaNetearClientes() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaNetearClientes jin = gson.fromJson(in.toString(), EntradaNetearClientes.class);
		SalidaNetearClientes out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaNetearClientes logica(Connection con, EntradaNetearClientes entrada) throws Exception {
		logger.info("INICIO::NetearClientes");
		
		SalidaNetearClientes out = new SalidaNetearClientes();
		
		SQL sql = new SQL();
		List<CarteraNegativa> cneg = sql.query(con, SQL_SELECT_CARTERAS_NEGATIVAS, new HashMap<>(), CarteraNegativa.class);
		
		NetearCustodiaNegativa n = new NetearCustodiaNegativa();
		EntradaNetearCustodiaNegativa entradaCarteraNegativa = new EntradaNetearCustodiaNegativa();
		entradaCarteraNegativa.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCarteraNegativa.p_IdModulo = entrada.p_IdModulo;
		entradaCarteraNegativa.p_IdUsuario = entrada.p_IdUsuario;
		entradaCarteraNegativa.p_SimularOperacion = "N";
		entradaCarteraNegativa.p_UsaReglasNegocio = "S";
		for (CarteraNegativa c : cneg) {
		
			PortafolioInstrumento pp = new PortafolioInstrumento();
			
			pp.IdInstrumento = c.IdInstrumento;
			pp.IdPortafolio = c.IdPortafolio;
			
			entradaCarteraNegativa.p_PortafoliosInstrumento.add(pp );
		}
		
		
		SalidaNetearCustodiaNegativa salidaCarteraNegativa = n.logica(con, entradaCarteraNegativa );
		out.P_OUT_ERRORESCONTADOR = salidaCarteraNegativa.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaCarteraNegativa.P_OUT_MENSAJES;
		logger.debug(salidaCarteraNegativa.toString());
		
		logger.info("FIN::NetearClientes");
		return out;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\": 1, \"p_IdUsuario\": 9, \"p_IdModulo\": \"CUS\", \"p_UsaReglasNegocio\": \"S\", \"p_SimularOperacion\": \"N\", \"p_PortafoliosInstrumento\": [{\"IdPortafolio\": 1450057, \"IdInstrumento\": 262000, \"NemoSVS\": \"\"}]}";
				EntradaNetearClientes entrada = gson.fromJson(json, EntradaNetearClientes.class);
								
				NetearClientes demo = new NetearClientes();
				SalidaNetearClientes salida = demo.logica(con, entrada);
				System.out.println(salida);
				con.commit();
				
		    } catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

}
