package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarValoresPorDefecto implements JavaCall {

	private final String SQL_CONSULTAR_VALORES_EMPRESA_SELECT = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarValoresDefecto_Select.sql");
	static final Logger logger = LogManager.getLogger(ConsultarValoresPorDefecto.class);
	
	public static class ValorDefecto {
		public String IdValorDefectoSistema;
		public String Nombre;
		public String Valor;
		public long IdEmpresa;
		public long IdUsuarioCreador;
		public String FechaCreacion;
		public String FechaModificacion;
		public String IdEstado;
		public String TipoEntidad;
		public String IdTipoCanalIngreso;
		@Override
		public String toString() {
			return String.format(
					"ValorDefecto [IdValorDefectoSistema=%s, Nombre=%s, Valor=%s, IdEmpresa=%s, IdUsuarioCreador=%s, FechaCreacion=%s, FechaModificacion=%s, IdEstado=%s, TipoEntidad=%s, IdTipoCanalIngreso=%s]",
					IdValorDefectoSistema, Nombre, Valor, IdEmpresa, IdUsuarioCreador, FechaCreacion, FechaModificacion,
					IdEstado, TipoEntidad, IdTipoCanalIngreso);
		}
	}
	
	public static class EntradaConsultarValoresPorDefecto extends BcsPojo {
		public Long p_IdEmpresa;
		public String p_TipoEntidad;
		public String p_IdTipoCanalIngreso;
		
		public EntradaConsultarValoresPorDefecto() {
			this.p_IdEmpresa = 1L;
			this.p_TipoEntidad = "";
			this.p_IdTipoCanalIngreso = "";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public HashMap<String, String> logica(Connection conn, EntradaConsultarValoresPorDefecto entrada) throws Exception {
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("TipoEntidad", entrada.p_TipoEntidad);
		params.put("IdTipoCanalIngreso", entrada.p_IdTipoCanalIngreso);
		logger.debug("INICIO::SQL_CONSULTAR_VALORES_EMPRESA_SELECT");
		List<ValorDefecto> valoresDefecto = _sql.query(conn, SQL_CONSULTAR_VALORES_EMPRESA_SELECT, params, ValorDefecto.class);
		logger.debug("FIN::SQL_CONSULTAR_VALORES_EMPRESA_SELECT");
		HashMap<String, String> result = new HashMap<>();
		for(ValorDefecto valor : valoresDefecto){
			result.put(valor.IdValorDefectoSistema, valor.Valor);
		}
		return result;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
			ConsultarValoresPorDefecto demo = new ConsultarValoresPorDefecto();
			System.out.println("ENTRADA");
			
			try {
				String json = "{\r\n"
						+ "  p_IdEmpresa: 1,\r\n"
						+ "  p_TipoEntidad: \"ORDENRAPIDA\",\r\n"
						+ "  p_IdTipoCanalIngreso: \"AGE\"\r\n"
						+ "}";
				Gson gson = Utils.getGsonBuilder();
				EntradaConsultarValoresPorDefecto entrada = gson.fromJson(json, EntradaConsultarValoresPorDefecto.class);
				HashMap<String, String> ObjOut = demo.logica(con, entrada);
				//con.commit();
				System.out.println(ObjOut.toString());
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