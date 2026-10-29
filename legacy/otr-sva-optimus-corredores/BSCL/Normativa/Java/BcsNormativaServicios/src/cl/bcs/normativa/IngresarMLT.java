package cl.bcs.normativa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarMLT implements JavaCall {
	

	public static class ObjDetalleParidad {
		   
	
	    
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ingresarMLTEntrada {
	
		//  "IdRegistroMandatos"                BIGINT          NOT NULL,
		//	"IdProcesoMandatos"                 BIGINT          NOT NULL,
		public Long p_IdSolicitudAporteRescate;
		public Long p_FolioMandato;
		public BigDecimal p_valor;//CostoUF o RentabilidadReal
		//"IdEstado" 							NVARCHAR(40) 	DEFAULT '',
		//"FechaIngreso"       				SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
	    
		
	}
		
	
	public static class RegistroMLT {
		
		//public Long IdRegistroMandatos;
		//public Long IdProcesoMandatos;
		public Long IdSolicitudAporteRescate;
		public Long FolioMandato;
		public BigDecimal Valor;//CostoUF o RentabilidadReal
		//"FechaIngreso"       				SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
		public Long IdUsuario;
		public Long IdEmpresa;
		public String IdRegimenTributario;
		
	}
	
	public static class  ingresarMLTSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();
	
	private static String SQL_UPDATE = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateRegistro.sql");
	private static String SQL_INSERT = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_InsertRegistro.sql");
	private static String SQL_SELECT_SOLICITUD = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_SelectSolicitud.sql");
	private static String SQL_UPDATE_SOLICITUD = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateSolicitud.sql");
	private static String SQL_UPDATE_TRASPASO = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateTraspaso.sql");
	
	private static String SQL_UPDATE_CARTERA_FM_APO = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateCarteraFM_APO.sql");
	private static String SQL_UPDATE_CARTERA_FM_RES = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateCarteraFM_RES.sql");
	
	
	private static String SQL_UPDATE_CARTERA_APV_APO = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateCarteraAPV_APO.sql");
	private static String SQL_UPDATE_CARTERA_APV_RES = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateCarteraAPV_RES.sql");
	private static String SQL_UPDATE_SOLICITUD_APV_RES = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateSolicitudAPV_RES.sql");
	
	private static String SQL_UPDATE_MLTULT_APV = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateUltAPV_MLT.sql");

	private static String SQL_UPDATE_PROCESO = loader.loadResourceToString("cl/bcs/normativa/sql/IngresarMLT_UpdateProceso.sql");
	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ingresarMLTEntrada in=gson.fromJson(jin.toString() ,ingresarMLTEntrada.class);
		 ingresarMLTSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  ingresarMLTSalida logica(Connection con, ingresarMLTEntrada entrada) throws Exception {
		 ingresarMLTSalida out = new  ingresarMLTSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
		// List<Long> ids = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::SIS.IdRegistroMandatos", 1L);
	     //   List<Long> idProc = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::SIS.IdProcesoMandatos", 1L);
	        
        try {
	        SQL sql = new SQL();
	        int i = 0;
	        String Regimen="";
	        
	        List<RegistroMLT> lstDetalleInicial = new ArrayList<RegistroMLT>();
	        RegistroMLT reg = new RegistroMLT();
	        reg.FolioMandato=entrada.p_FolioMandato;
	        reg.IdEmpresa=entrada.p_IdEmpresa;
	        //reg.IdRegistroMandatos=ids.get(i);
	        //reg.IdProcesoMandatos=idProc.get(i);
	        reg.IdSolicitudAporteRescate=entrada.p_IdSolicitudAporteRescate;
	        reg.IdUsuario=entrada.p_IdUsuario;
	        reg.Valor=entrada.p_valor;
	        
	        lstDetalleInicial.add(reg);

	    //actualizacion de registros  
	        HashMap<String,Object> params = new HashMap<String,Object>();
    		params.put("IdEmpresa", entrada.p_IdEmpresa);
    		params.put("IdSolicitudAporteRescate", entrada.p_IdSolicitudAporteRescate);
	            		
    		List<RegistroMLT> solicitud = sql.query(con, SQL_SELECT_SOLICITUD, params, RegistroMLT.class);
    		
    		if(solicitud.size() != 0){
    			
  	         
    			for(RegistroMLT in : solicitud){
    				 Regimen =  in.IdRegimenTributario;
    				
    			}
    			
    		}
	        
	    sql.batch(con, SQL_UPDATE, lstDetalleInicial, RegistroMLT.class);
	    sql.batch(con, SQL_INSERT, lstDetalleInicial, RegistroMLT.class);
	    sql.batch(con, SQL_UPDATE_PROCESO, lstDetalleInicial, RegistroMLT.class);
	    
	    
	    
	    ///Actualiza Solicitud de  Aporte
	    sql.batch(con, SQL_UPDATE_SOLICITUD, lstDetalleInicial, RegistroMLT.class);
	    
	    if ("18Q".equals(Regimen))
	    {
	    	///Actualiza traspaso
	    	sql.batch(con, SQL_UPDATE_TRASPASO, lstDetalleInicial, RegistroMLT.class);
	    	///Actualiza Carteras FM
	    	sql.batch(con, SQL_UPDATE_CARTERA_FM_APO, lstDetalleInicial, RegistroMLT.class);
	    	sql.batch(con, SQL_UPDATE_CARTERA_FM_RES, lstDetalleInicial, RegistroMLT.class);
	    	
	    }else{
	    	///Actualiza Carteras APV
	    	sql.batch(con, SQL_UPDATE_CARTERA_APV_APO, lstDetalleInicial, RegistroMLT.class);
	    	sql.batch(con, SQL_UPDATE_CARTERA_APV_RES, lstDetalleInicial, RegistroMLT.class);
	    	///Actualiza rescate relacionado
	    	sql.batch(con, SQL_UPDATE_SOLICITUD_APV_RES, lstDetalleInicial, RegistroMLT.class);
	    	
	    	sql.batch(con, SQL_UPDATE_MLTULT_APV, lstDetalleInicial, RegistroMLT.class);
	    	
	    };
	    
	    
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
		 
	}
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			//Connection con = PlataformaUtils.getQAConnection();
  			try{
  				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
  				 		
  				
  				String json = "{\"p_IdSolicitudAporteRescate\":661743,\"p_FolioMandato\":777,\"p_valor\":777,\"p_IdUsuario\":6,\"p_IdEmpresa\":1}";
  				ingresarMLTEntrada Objin_json = gson.fromJson(json, ingresarMLTEntrada.class);
  				
  				IngresarMLT demo = new IngresarMLT();	
  				ingresarMLTSalida ObjOut = demo.logica(con, Objin_json);  				
  				System.out.println(ObjOut.toString());
  				//con.rollback();
 				
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
}
