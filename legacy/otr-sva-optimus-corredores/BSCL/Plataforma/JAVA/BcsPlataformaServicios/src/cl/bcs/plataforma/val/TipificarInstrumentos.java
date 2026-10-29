package cl.bcs.plataforma.val;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TipificarInstrumentos implements JavaCall{	
	
	private final Log log = new Log("TipificarInstrumentos");
	
	//QUERIES
	//[no se utilizan archivos sql para evitar exceso de archivos]
	private final String SQL_Update_Instrumentos = "UPDATE \"BSCL.Plataforma.BD::INS.Instrumento\" "
			+ "SET \"TipoValorizacion\" = COALESCE(:TipoValorizacion,-1) WHERE \"Nemotecnico\" = :Nemotecnico";

	//CLASES
	public static class Nemotecnico {
		public String	Nemotecnico;
	}
	
	public static class InstrumentoUpdate {
		public String	Nemotecnico; 
		public Long		TipoValorizacion; 
	}

	//ENTRADA Y SALIDA DE LA CLASE
	public static class InTipificarInstrumentos{
		public Long 			idEmpresa; 			
	    public Long 			idUsuario;
	    public String 			idModulo;
	    public Long 			idPais;
	    public String			fechaTipificar; //java.sql.Date
	    public Long				cantidadTipificar;
	}
	public static class OutTipificarInstrumentos{
		public Long	 		erroresContador;
		public List<Instrumento> instrumentos;
		public static class Instrumento{
			public String	nemotecnico;
			public Long 	tipoValorizacion;
			public String 	codigoMensaje;
			public String 	descripcionMensaje;
		}
		public OutTipificarInstrumentos(){
			instrumentos = 	new ArrayList<Instrumento>();
		}
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InTipificarInstrumentos in=gson.fromJson(jin.toString() ,InTipificarInstrumentos.class);
		OutTipificarInstrumentos out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public OutTipificarInstrumentos logica(Connection con, InTipificarInstrumentos entrada) throws Exception {
		boolean debug = true;
		SQL sql=new SQL();
		OutTipificarInstrumentos salida = new OutTipificarInstrumentos();
		salida.erroresContador = 0L;
		HashMap<String,Object> parmsVacio = new HashMap<String,Object>();	
		String strfecsistema = "";
		if (entrada.fechaTipificar==null) {
			strfecsistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.idEmpresa,"PLATAFORMA","PLA.FechaSistema");
			entrada.fechaTipificar = strfecsistema;
		}
		//obtención de nemos
		String SQL_queryInstrumentos = "select top " + entrada.cantidadTipificar.intValue() + " i.\"Nemotecnico\" "
				+ "from \"BSCL.Plataforma.BD::INS.Instrumento\" i join \"BSCL.Plataforma.BD::INS.TipoInstrumento\" ti "
				+ "on (i.\"IdTipoInstrumento\" = ti.\"IdTipoInstrumento\") "
				+ "where ti.\"IdClasificacionTipoInstrumento\" in ('RF','IF') and i.\"TipoValorizacion\" is null or i.\"TipoValorizacion\" = '' "
				+ "group by i.\"Nemotecnico\"";
		List<Nemotecnico> listNemos = sql.query(con, SQL_queryInstrumentos,parmsVacio,Nemotecnico.class);
		if(listNemos.size()==0){
			log.write("No hay instrumentos para tipificar");
			return salida;
		}
		TipificarWs tip = new TipificarWs();
		TipificarWs.InTipificarWs intip = new TipificarWs.InTipificarWs();
		for(int i=0;i<listNemos.size();i++){
			ValoUtils.TareaValorizacion itemagregar = new ValoUtils.TareaValorizacion();
			itemagregar.IdTarea = new Long(i);
			itemagregar.GrupoValorizacion = 0L; 
			itemagregar.Nemo = listNemos.get(i).Nemotecnico;
			itemagregar.FechaVal = ValoUtils.stringToSqlDate(entrada.fechaTipificar);
			intip.instrumentos.add(itemagregar);
		}
		intip.idGrupo = 0L;
		intip.rutinaValorizador = "BCS";
		TipificarWs.OutTipificarWs outtip = tip.logica(con, intip);
		List<InstrumentoUpdate> paramsupdate = new ArrayList<InstrumentoUpdate>();
		for(ValoUtils.TareaValorizacion irestip : outtip.instrumentos){
			if(irestip.WsCallerCodSalida.equals("0")){
				InstrumentoUpdate item = new InstrumentoUpdate();
				item.Nemotecnico = irestip.Nemo;
				item.TipoValorizacion = irestip.TipoVal;
				paramsupdate.add(item);
			}
			OutTipificarInstrumentos.Instrumento insout = new OutTipificarInstrumentos.Instrumento();
			insout.nemotecnico = irestip.Nemo;
			insout.tipoValorizacion = irestip.TipoVal;
			
			if(!irestip.WsCallerCodSalida.equals("0")){
				insout.codigoMensaje = "ERROR_CALLER_WS";
				insout.descripcionMensaje = irestip.WsCallerDescSalida;
			}else if(!irestip.Estado.equals("0")){
				insout.codigoMensaje = "ERROR_WS";
				insout.descripcionMensaje = irestip.Glosa;
			}else{
				insout.codigoMensaje = "0";
				insout.descripcionMensaje = "OK";
			}
			insout.codigoMensaje = irestip.Estado;
			salida.instrumentos.add(insout);
		}
		if(paramsupdate.size()==0){
			log.write("Ningún instrumento tipificado con éxito.");
			return salida;
		}	
		sql.batch(con,SQL_Update_Instrumentos, paramsupdate,InstrumentoUpdate.class);
		salida.erroresContador = 0L;
		
		if(debug){
			log.writeAsJson("TipificarInstrumentos entrada",entrada);
			log.writeAsJson("TipificarInstrumentos salida",salida);
		}
		return salida;
	}
	
	public static void main(String[] args){
		Connection con=null;
		try{
			System.out.println(Fecha.toStringDate(Fecha.toDateTime(Utils.getCurrentTimeStamp())));
			con = PlataformaUtils.getDESAConnection();
			String fechaSistema = Utils.getFechaSistema(con, "BSCL", 1);
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"idEmpresa\":1,\"idUsuario\":1,\"idModulo\":\"PLA\",\"idPais\":44,\"fechaTipificar\":\""+fechaSistema+"\",\"cantidadTipificar\":5000}";
			InTipificarInstrumentos Objin = gson.fromJson(json, InTipificarInstrumentos.class);
	        System.out.println("=== JSON ENTRADA ===\n"+json+"\n====================");
	        TipificarInstrumentos demo=new TipificarInstrumentos();		
	    	System.out.println("=== JSON SALIDA ===\n"+Utils.printJson(demo.logica(con, Objin).toString(),false)+"\n====================");	
	    	con.commit();
	    	con.close();
		}catch(Exception e){
			e.printStackTrace();
			try{if(con!=null)con.rollback();}
			catch(Exception eb){ System.out.println("WTF!");}
		}
		
	}
	
}
