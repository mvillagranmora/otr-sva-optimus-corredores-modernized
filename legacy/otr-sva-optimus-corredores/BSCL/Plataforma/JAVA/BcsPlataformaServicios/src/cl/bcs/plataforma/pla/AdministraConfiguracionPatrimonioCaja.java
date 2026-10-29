package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AdministraConfiguracionPatrimonioCaja implements JavaCall {
	static final Logger logger = LogManager.getLogger(AdministraConfiguracionPatrimonioCaja.class);
	private final String SQL_UPDATE_CONFIGURACION_PATRIMONIO_CAJA = loadResourceToString("cl/bcs/plataforma/pla/sql/AdministraConfiguracionPatrimonioCaja_UpdateConfiguracionPatrimonioCaja.sql");
	
	public static class EntradaAdministraConfiguracionPatrimonioCaja{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Item> p_Items;
		
		public EntradaAdministraConfiguracionPatrimonioCaja() {
			this.p_Items = new ArrayList<Item>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Item{
		public String CodigoItem;
		public String DescripcionItem;
		public String FuncionOperador;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAdministraConfiguracionPatrimonioCaja{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAdministraConfiguracionPatrimonioCaja() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAdministraConfiguracionPatrimonioCaja jin = gson.fromJson(in.toString(), EntradaAdministraConfiguracionPatrimonioCaja.class);
		SalidaAdministraConfiguracionPatrimonioCaja out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministraConfiguracionPatrimonioCaja logica(Connection con, EntradaAdministraConfiguracionPatrimonioCaja entrada) throws Exception {
		logger.info("INICIO::AdministraConfiguracionPatrimonioCaja");
		SalidaAdministraConfiguracionPatrimonioCaja out = new SalidaAdministraConfiguracionPatrimonioCaja();
		SQL sql=new SQL();
		
		if(entrada.p_Items.size() == 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ITEMS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		List<Item> invalidos = entrada.p_Items.stream().filter(item -> !"0".equals(item.FuncionOperador) && !"+".equals(item.FuncionOperador) && !"-".equals(item.FuncionOperador)).collect(Collectors.toList());
		
		if(invalidos.size() > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FUNCION_OPERADOR_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		sql.batch(con, SQL_UPDATE_CONFIGURACION_PATRIMONIO_CAJA, entrada.p_Items, Item.class);
		
		logger.info("FIN::AdministraConfiguracionPatrimonioCaja");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Items\":[{\"CodigoItem\":\"SaldoDisponible\",\"FuncionOperador\":\"+\"}]}";
		EntradaAdministraConfiguracionPatrimonioCaja Objin = gson.fromJson(jsonRF, EntradaAdministraConfiguracionPatrimonioCaja.class);
		AdministraConfiguracionPatrimonioCaja  demo=new AdministraConfiguracionPatrimonioCaja();				
		try {
			SalidaAdministraConfiguracionPatrimonioCaja ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
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
