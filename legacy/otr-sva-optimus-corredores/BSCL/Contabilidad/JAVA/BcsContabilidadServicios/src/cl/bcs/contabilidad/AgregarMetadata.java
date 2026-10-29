package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class AgregarMetadata implements JavaCall {
	
	private final Log log = new Log("CON.AgregarMetadata");
	
	private final String SQL_SELECT_ID_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/AgregarMetadata_SelectMetadataCuentaContable.sql");
	private final String SQL_INSERT_METADATA_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/AgregarMetadata_InsertMetadataCuentaContable.sql");
	private final String SQL_INSERT_FILTROS_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/AgregarMetadata_InsertMetadataFiltros.sql");
	
	public static class InAgregarMetadata{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_CodigoMetadata;
		public String	p_DescripcionMetadata;
		public List<InFiltro> p_Filtros;
	}
	
	public static class InFiltro{
		public String	CuentaOptimus;
		public String	IdTipoDatoEvento;
		public String	Condicion;
		public String	Valor; 
	}
	
	public static class Id{
		public Long IdMetadataCuentaContable;
	}
	
	public static class OutAgregarMetadata{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public OutAgregarMetadata(){
			this.P_OUT_MENSAJE = "";
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
		
	public static class Metadata{
		public Long IdMetadataCuentaContable;
		public String CodigoMetadata;
		public String DescripcionMetadata;
	}
	
	public static class Filtro{
		public Long 	IdMetadataCuentaContable;
		public String	CuentaOptimus;
		public String	IdTipoDatoEvento;
		public String	Condicion;
		public String	Valor;
	}
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InAgregarMetadata in=gson.fromJson(jin.toString() ,InAgregarMetadata.class);
		OutAgregarMetadata out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutAgregarMetadata logica(Connection con, InAgregarMetadata entrada) throws Exception {
		OutAgregarMetadata out = new OutAgregarMetadata();
		try{
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<Id> id = sql.query(con,SQL_SELECT_ID_METADATA,params,Id.class);
			
			Metadata metadata = new Metadata();
			metadata.IdMetadataCuentaContable = id.get(0).IdMetadataCuentaContable;
			metadata.CodigoMetadata = entrada.p_CodigoMetadata;
			metadata.DescripcionMetadata = entrada.p_DescripcionMetadata;
			
			List<Metadata> metadatas = new ArrayList<Metadata>();
			metadatas.add(metadata);
			List<Filtro> filtros = new ArrayList<Filtro>();
			for(InFiltro p_filtro : entrada.p_Filtros){
				Filtro filtro = new Filtro();
				filtro.IdMetadataCuentaContable = metadata.IdMetadataCuentaContable;
				filtro.CuentaOptimus = p_filtro.CuentaOptimus;
				filtro.Condicion = p_filtro.Condicion;
				filtro.IdTipoDatoEvento = p_filtro.IdTipoDatoEvento;
				filtro.Valor = p_filtro.Valor;
				if("=".equals(filtro.Condicion) || ">".equals(filtro.Condicion) || "<".equals(filtro.Condicion) || "<>".equals(filtro.Condicion)){
					filtros.add(filtro);
				}else{
					HashMap<String,String> hcondicion = new HashMap<String,String>();
					hcondicion.put("Condicion", filtro.Condicion);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"CONDICION_NO_VALIDA_ASIENTO",hcondicion,con,entrada.p_IdEmpresa));
					log.write(filtro.Condicion + " no valido");
				}
				
			}
			
			sql.batch(con, SQL_INSERT_METADATA_CUENTA_CONTABLE, metadatas, Metadata.class);
			sql.batch(con, SQL_INSERT_FILTROS_METADATA, filtros, Filtro.class);
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJE = "UI_EXITO";
			
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error",e);
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_CodigoMetadata\":\"PRUEBA\",\"p_DescripcionMetadata\":\"DESCRIPCION PRUEBA\",\"p_Filtros\":[{\"CuentaOptimus\":\"110101001001\",\"IdTipoDatoEvento\":\"Agente\",\"Condicion\":\"=\",\"Valor\":\"WEWE\"}]}";
			InAgregarMetadata Objin = gson.fromJson(json, InAgregarMetadata.class);
			
			AgregarMetadata demo=new AgregarMetadata();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				OutAgregarMetadata ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
}
