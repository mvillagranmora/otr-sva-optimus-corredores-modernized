package cl.bcs.fondomutuo;

import java.math.BigDecimal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarFondoMutuoReinversion implements JavaCall{
	
	private final Log log = new Log("FM.ConsultarFondoMutuoReinversion");
	private final String SQL_SELECT_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarFondoMutuo_SelectReinversion.sql");
	
	public static class ConsultarFondoMutuoReinversion_Entrada {
		//Parametros Fijos
		public long		p_IdEmpresa;
		public long		p_IdUsuario;
		public String	p_IdModulo;
		
		//Parametros de filtro
		public String p_IdEstado;
		public String p_IdMoneda;
		public String p_FondoExtranjero;
		public String p_IdFamiliaFondo;
		public String p_IdEmisor;
		public String p_IdRegimenFondoMutuo;
		public String p_IdSerieFondoOrigen;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarFondoMutuoReinversion_Salida{
		public List<FiltroFondoMutuo> P_OUT_LISTA;
		
		public ConsultarFondoMutuoReinversion_Salida(){
			this.P_OUT_LISTA = new ArrayList<FiltroFondoMutuo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FiltroFondoMutuo{
		public String IdSerieFondo;
		public String NombreSerieFondo;
		public String IdMoneda;
		public String IdEmisor;
		public String MLT;
		public String IdRegimenFondoMutuo;
		public BigDecimal ValorCuotaInstrumento;
//		public BigDecimal PatrimonioMinimo;
		public BigDecimal MontoPatrimonioEmisor;
		public BigDecimal MontoMinimo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		ConsultarFondoMutuoReinversion_Entrada in=gson.fromJson(jin.toString(), ConsultarFondoMutuoReinversion_Entrada.class);
		ConsultarFondoMutuoReinversion_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}

	private ConsultarFondoMutuoReinversion_Salida logica(Connection conn, ConsultarFondoMutuoReinversion_Entrada in) throws Exception {
		log.write("INICIO ConsultarFondoMutuoReinversion");
		
		SQL sql=new SQL();
		
		ConsultarFondoMutuoReinversion_Salida out = new ConsultarFondoMutuoReinversion_Salida();
		
		HashMap<String,Object> params_query = new HashMap<>();
		params_query.put("IdEmpresa", in.p_IdEmpresa);
		params_query.put("IdEstado", in.p_IdEstado);
		params_query.put("IdMoneda", in.p_IdMoneda);
		params_query.put("FondoExtranjero", in.p_FondoExtranjero);
		params_query.put("IdFamiliaFondo", in.p_IdFamiliaFondo);
		params_query.put("IdEmisor", in.p_IdEmisor);
		params_query.put("IdRegimenFondoMutuo", in.p_IdRegimenFondoMutuo);
		params_query.put("IdSerieFondoOrigen", in.p_IdSerieFondoOrigen);
		
		List<FiltroFondoMutuo> list = sql.query(conn, SQL_SELECT_FONDO, params_query, FiltroFondoMutuo.class);
		
		for(FiltroFondoMutuo item : list) {
			FiltroFondoMutuo aux = new FiltroFondoMutuo();
			aux.IdSerieFondo = item.IdSerieFondo;
			aux.NombreSerieFondo = item.NombreSerieFondo;
			aux.IdMoneda = item.IdMoneda;
			aux.IdEmisor = item.IdEmisor;
			aux.MLT = item.MLT;
			aux.IdRegimenFondoMutuo = item.IdRegimenFondoMutuo;
			aux.ValorCuotaInstrumento = item.ValorCuotaInstrumento;
			//aux.PatrimonioMinimo = item.PatrimonioMinimo;
			aux.MontoPatrimonioEmisor = item.MontoPatrimonioEmisor;
			aux.MontoMinimo = item.MontoMinimo;
			out.P_OUT_LISTA.add(aux);
		}		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"FMM\",\"p_IdEstado\":\"ACT\",\"p_IdMoneda\":\"-\",\"p_IdFamiliaFondo\":\"-\",\"p_FondoExtranjero\":\"-\",\"p_IdEmisor\":\"-\",\"p_IdRegimenFondoMutuo\":\"18Q\",\"p_IdSerieFondoOrigen\":\"00212NUSD\"}";		
			ConsultarFondoMutuoReinversion_Entrada Objin = gson.fromJson(json, ConsultarFondoMutuoReinversion_Entrada.class);
			ConsultarFondoMutuoReinversion  demo=new ConsultarFondoMutuoReinversion();				
			try {
				
				ConsultarFondoMutuoReinversion_Salida ObjOut=demo.logica(con, Objin);
				//con.rollback();
				System.out.println(ObjOut.P_OUT_LISTA.size());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
