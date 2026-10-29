package cl.bcs.plataforma.bat;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarAgenda implements JavaCall {
	
	public String CALL_AGREGAR_AGENDA = loadResourceToString("cl/bcs/plataforma/bat/sql/AgregarAgrenda_Call.sql");
	public String CREATE_PARAMETROS_AGENDA = "CREATE LOCAL TEMPORARY TABLE #PARAMETROS_AGENDA(\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(5000))";
	public String DROP_PARAMETROS_AGENDA = "DROP TABLE #PARAMETROS_AGENDA;";
	public String INSERT_PARAMETROS_AGENDA = "INSERT INTO #PARAMETROS_AGENDA VALUES(:NombreParametro, :Valor);";
	
	public static class TablaParametros{
		public String NombreParametro;
		public String Valor;
		public InputStream ValorBlob;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaAgregarAgenda{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
        
		public String p_IdTipoTarea;
		public String p_EsDemonio;   
		public String p_DiaDeMes;
		public String p_DiaDeSemana;
		public String p_Mes;
		public String p_Hora;
		public String p_Minuto;
		public String p_SoloDiasHabiales;
		public String p_SoloPrincipioMes;
		public String p_SoloFinDeMes;
		public String p_habilitado;
		public List<TablaParametros> 	p_TablaParametros;
		
		public EntradaAgregarAgenda() {
			this.p_TablaParametros =  new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAgregarAgenda extends SalidaServicios{
		public Long p_IdAgenda;
		public Long p_IdPrograma;
		
		public SalidaAgregarAgenda(){
			super();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaAgregarAgenda jin=gson.fromJson(in.toString() ,EntradaAgregarAgenda.class);
		SalidaAgregarAgenda out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public SalidaAgregarAgenda logica(Connection conn, EntradaAgregarAgenda in) {
		SalidaAgregarAgenda out = new SalidaAgregarAgenda();
		SQL sql = new SQL();
		
		try {
			SQL.exec(conn, CREATE_PARAMETROS_AGENDA);
			
			sql.batch(conn, INSERT_PARAMETROS_AGENDA, in.p_TablaParametros, TablaParametros.class);
			
			HashMap<String, Object> paramsIn = new HashMap<>();
			paramsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			paramsIn.put("p_IdUsuario", in.p_IdUsuario);
			paramsIn.put("p_IdModulo", in.p_IdModulo);
			
			paramsIn.put("p_IdTipoTarea", in.p_IdTipoTarea);
			paramsIn.put("p_EsDemonio", in.p_EsDemonio);
			paramsIn.put("p_DiaDeMes", in.p_DiaDeMes);
			paramsIn.put("p_DiaDeSemana", in.p_DiaDeSemana);
			paramsIn.put("p_Mes", in.p_Mes);
			paramsIn.put("p_Hora", in.p_Hora);
			paramsIn.put("p_Minuto", in.p_Minuto);
			paramsIn.put("p_SoloDiasHabiales", in.p_SoloDiasHabiales);
			paramsIn.put("p_SoloPrincipioMes", in.p_SoloPrincipioMes);
			paramsIn.put("p_SoloFinDeMes", in.p_SoloFinDeMes);
			paramsIn.put("p_habilitado", in.p_habilitado);
					
			HashMap<String, Class> paramsOut = new HashMap<>();
			paramsOut.put("p_IdAgenda", Long.class);
			paramsOut.put("p_IdPrograma", Long.class);
			paramsOut.put("p_out_AdvertenciasContador", Long.class);
			paramsOut.put("p_out_AdvertenciasDetalle", SalidaAgregarAgenda.AdvertenciaDetalle.class);
			paramsOut.put("p_out_ErroresContador", Long.class);
			paramsOut.put("p_out_ErroresDetalle", SalidaAgregarAgenda.ErrorDetalle.class);
		
			HashMap<String,Object> resultSP = sql.call(conn, CALL_AGREGAR_AGENDA, paramsIn, paramsOut);
			if(resultSP.containsKey("p_out_ErroresContador")) {
				out.p_IdAgenda = (Long) resultSP.get("p_IdAgenda");
				out.p_IdPrograma = (Long) resultSP.get("p_IdPrograma");
				out.P_OUT_ADVERTENCIASCONTADOR = (long) resultSP.get("p_out_AdvertenciasContador");
				out.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) resultSP.get("p_out_AdvertenciasDetalle");
				out.P_OUT_ERRORESCONTADOR = (long) resultSP.get("p_out_ErroresContador");
				out.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle");
				
				System.out.println(out.P_OUT_ERRORESCONTADOR);
				System.out.println(out.P_OUT_ERRORESDETALLE);
			}
			
		} catch (SQLException e) {
			out.addError("UI_ERROR", e.getMessage());
			e.printStackTrace();
		
		}finally {
			try {
				SQL.exec(conn, DROP_PARAMETROS_AGENDA);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{}";

				EntradaAgregarAgenda Objin = gson.fromJson(json, EntradaAgregarAgenda.class);								
				AgregarAgenda  kk = new AgregarAgenda();
				System.out.println(kk.logica(con, Objin));
				con.commit();
				
				System.out.println("---- FIN Agregar Agenda ------");
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

}
