package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class EliminarMetadata implements JavaCall {
	
	private final Log log = new Log("CON.EliminarMetadata");
	
	private final String SQL_SELECT_METADATA_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/EliminarMetadata_SelectMetadataCuentaContable.sql");
	private final String SQL_DELETE_METADATA_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/EliminarMetadata_DeleteMetadataCuentaContable.sql");
	private final String SQL_DELETE_METADATA_FILTROS = loadResourceToString("cl/bcs/contabilidad/sql/EliminarMetadata_DeleteMetadataFiltros.sql");

	public static class InEliminarMetadata{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public List<Long> p_IdMetadataCuentaContable;
	}
	
	public static class OutEliminarMetadata{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
	}
	
	public static class Metadata{
		public Long IdMetadataCuentaContable;
	}
			
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InEliminarMetadata in = gson.fromJson(jin.toString() ,InEliminarMetadata.class);
		OutEliminarMetadata out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutEliminarMetadata logica(Connection con, InEliminarMetadata entrada) throws Exception {
		OutEliminarMetadata out = new OutEliminarMetadata();
		try{
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdMetadataCuentaContable", entrada.p_IdMetadataCuentaContable);
			List<Metadata> metadatas = sql.query(con,SQL_SELECT_METADATA_CUENTA_CONTABLE,params,Metadata.class);
			if(metadatas.size() != entrada.p_IdMetadataCuentaContable.size()){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJE = "UI_METADATA_NO_EXISTE";
			}else{
				sql.batch(con, SQL_DELETE_METADATA_CUENTA_CONTABLE, metadatas, Metadata.class);
				sql.batch(con, SQL_DELETE_METADATA_FILTROS, metadatas, Metadata.class);
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_MENSAJE = "UI_EXITO";
			}
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error",e);
		}
		return out;
	}
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getDESAConnection()){
  			InEliminarMetadata entrada= new InEliminarMetadata();
				entrada.p_IdEmpresa		= new Long(1);
				entrada.p_IdUsuario		= new Long(21);
				entrada.p_IdModulo 		= "CON";
				entrada.p_IdMetadataCuentaContable = new ArrayList<Long>();
				entrada.p_IdMetadataCuentaContable.add(3L);
				entrada.p_IdMetadataCuentaContable.add(1L);
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				EliminarMetadata demo= new EliminarMetadata();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}
}
