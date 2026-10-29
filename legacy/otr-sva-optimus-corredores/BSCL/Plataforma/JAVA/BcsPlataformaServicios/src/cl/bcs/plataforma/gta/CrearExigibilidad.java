package cl.bcs.plataforma.gta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

public class CrearExigibilidad implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearExigibilidad.class);
	private final String SQL_SelectExigibilidad = loadResourceToString( "cl/bcs/plataforma/gta/sql/CrearExigibilidad_SelectExigibilidadGarantia.sql");
	private final String SQL_InsertExigibilidad = loadResourceToString( "cl/bcs/plataforma/gta/sql/CrearExigibilidad_InsertExigibilidadGarantia.sql");
	private final String SQL_UpdateExigibilidad = loadResourceToString( "cl/bcs/plataforma/gta/sql/CrearExigibilidad_UpdateExigibilidadGarantia.sql");
	
	
	public static class CrearExigibilidadEntrada{
		public String p_IdEmpresa;
		public String p_IdUsuario;    
		public String p_IdModulo;   
		public String p_simularOperacion;	
		public String p_UsaReglasNegocio; 
		public List<ObjExigibilidad> p_TableExigibilidad;
		
		public CrearExigibilidadEntrada () {
			this.p_TableExigibilidad = new ArrayList<ObjExigibilidad>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearExigibilidadSalida{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CrearExigibilidadSalida() {
			this.P_OUT_MENSAJES= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ObjExigibilidad{
		public String  IdTipoInstrumento;
        public String  IdBolsa;
        public String  IdTipoInstrumentoSubyacente;
        public String  IdTipoClasificacionRiesgo;
        public String  Nemotecnico;
        public BigDecimal  ExigibilidadEntidad;
        public BigDecimal  ExigibilidadCorredora;
        public Long  IdExigibilidadGarantia;
        public Long  IdInstrumento;
        public String  IdEntidadClasificadoraRiesgo;
        public String  ClasificacionRiesgo;
        
        public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		CrearExigibilidadEntrada in=gson.fromJson(jin.toString() ,CrearExigibilidadEntrada.class);
		CrearExigibilidadSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private CrearExigibilidadSalida logica(Connection con, CrearExigibilidadEntrada in) throws Exception {		
		logger.info("INICIO::CrearExigibilidadSalida.logica");
		CrearExigibilidadSalida out = new CrearExigibilidadSalida();
		
		List<ObjExigibilidad> listaUpdateExigibilidad = obtenerExigibilidadUpdate(con,in.p_TableExigibilidad);
		List<ObjExigibilidad> listaInsertExigibilidad = obtenerExigibildiadInsert(listaUpdateExigibilidad,in.p_TableExigibilidad);
		
		if(!listaUpdateExigibilidad.isEmpty()) {
			this.updateExigibilidad(con,listaUpdateExigibilidad,out);
		}
		if(!listaInsertExigibilidad.isEmpty()) {
			//List<Long>  listaSecuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::ExigibilidadGarantia", (long) listaInsertExigibilidad.size());
			this.insertExigibilidad(con, listaInsertExigibilidad,out);
		}
		
		if(out.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_EXIGIBILIDA_EXITOSA","Exigibilidad actulaizada correctamente"));
		}
		
		logger.info("FIN::CrearExigibilidadSalida.logica");
		return out;
	}
	
	private void insertExigibilidad (Connection con,List<ObjExigibilidad> listaInsertExigibilidad,CrearExigibilidadSalida out){
		logger.debug("INICIO::CrearExigibilidadSalida.insertExigibilidad");
		SQL sql=new SQL();
		try {
			sql.batch(con,SQL_InsertExigibilidad,listaInsertExigibilidad, ObjExigibilidad.class);
		}catch(Exception e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AL_GRABAR","Error al momento de grabar exigibilidad"));
			out.P_OUT_ERRORESCONTADOR=1L;
		}
		logger.debug("FIN::CrearExigibilidadSalida.insertExigibilidad");
	}
	
	private void updateExigibilidad (Connection con,List<ObjExigibilidad> listaUpdateExigibilidad,CrearExigibilidadSalida out) throws Exception  {
		logger.debug("INICIO::CrearExigibilidadSalida.updateExigibilidad");
		SQL sql=new SQL();
		try {
			sql.batch(con,SQL_UpdateExigibilidad,listaUpdateExigibilidad, ObjExigibilidad.class);
		}catch(Exception e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AL_ACTUALIZAR","Error al momento de actualizar exigibilidad"));
			out.P_OUT_ERRORESCONTADOR=1L;
		}
		logger.debug("FIN::CrearExigibilidadSalida.updateExigibilidad");
	}
	
	private List<ObjExigibilidad> obtenerExigibilidadUpdate(Connection con,List<ObjExigibilidad> listaEntrada) throws SQLException{
		logger.debug("INICIO::CrearExigibilidadSalida.obtenerExigibilidadUpdate");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(); 
		List<Long> listaIdExigibilidad =   listaEntrada.stream().map(Id -> Id.IdExigibilidadGarantia).collect(Collectors.toList());
		params.put("ListaExigibilidad", listaIdExigibilidad);
		List<ObjExigibilidad> listaExigibilidad = sql.query(con, SQL_SelectExigibilidad, params, ObjExigibilidad.class);
		logger.debug("FIN::CrearExigibilidadSalida.obtenerExigibilidadUpdate");
		return listaExigibilidad;
	}
	
	private List<ObjExigibilidad> obtenerExigibildiadInsert(List<ObjExigibilidad> listaUpdate,List<ObjExigibilidad> listaEntrada) throws Exception{
		logger.debug("INICIO::CrearExigibilidadSalida.obtenerExigibildiadInsert");
		HashMap<Long,Object> HashUpdate = Utils.List2Hash(listaUpdate, "IdExigibilidadGarantia");
		List<ObjExigibilidad> listaInsert = listaEntrada.stream().filter(obj-> HashUpdate.get(obj.IdExigibilidadGarantia)== null).collect(Collectors.toList());
		logger.debug("FIN::CrearExigibilidadSalida.obtenerExigibildiadInsert");
		return listaInsert;
	}
	
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_TableExigibilidad\":[{\"IdTipoInstrumento\":\"DER\",\"IdBolsa\":\"BCS\",\"IdTipoInstrumentoSubyacente\":\"IOA\",\"IdTipoClasificacionRiesgo\":\"\",\"Nemotecnico\":\"AES20Q4960\",\"ExigibilidadEntidad\":20,\"ExigibilidadCorredora\":30,\"IdExigibilidadGarantia\":0,\"IdInstrumento\":476743,\"IdEntidadClasificadoraRiesgo\":\"BCS\",\"ClasificacionRiesgo\":\"\"}]}";
			CrearExigibilidadEntrada entrada = gson.fromJson(json, CrearExigibilidadEntrada.class);
			CrearExigibilidad demo = new CrearExigibilidad();
			
			System.out.println(demo.logica(con, entrada));
			con.rollback();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("==TERMINA==");
	}
	
	
	

}
