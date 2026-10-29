package cl.bcs.plataforma.val;


import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.plataforma.val.util.ValorizarWsBCS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;

public class TipificarWs implements JavaCall{	
	private final Log log = new Log("TipificarWs");
	private static ValorizarWsBCS valobcs = null;
	//QUERIES
	
	//ENTRADA/SALIDA SP
	public static class InTipificarWs{
		public Long idGrupo; // idGrupo > 0: Es llamado desde el job debe realizar queries.
		public String rutinaValorizador;
		public List<ValoUtils.TareaValorizacion> instrumentos;
		public InTipificarWs(){
			instrumentos = new ArrayList<ValoUtils.TareaValorizacion>();
			idGrupo=new Long(0);
		};
	}
	public static class OutTipificarWs {
		public Long erroresContador;
		public String descripcion;
		public List<ValoUtils.TareaValorizacion> instrumentos;
		public OutTipificarWs(){
			instrumentos = new ArrayList<ValoUtils.TareaValorizacion>();
		};
	}

	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InTipificarWs in=gson.fromJson(jin.toString() ,InTipificarWs.class);
		OutTipificarWs out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public OutTipificarWs logica(Connection con, InTipificarWs entrada) throws Exception {
		boolean debug = true;
		OutTipificarWs salida = new OutTipificarWs();
		String msjaux = "";
		log.write("Nueva Valorización, IdGrupo: "+entrada.idGrupo);
		List<ValoUtils.TareaValorizacion> instrumentos = null;
		salida.erroresContador = new Long(0);
		instrumentos = entrada.instrumentos;
		if(instrumentos.size()==0){
			salida.erroresContador++;
			salida.descripcion = "No se han enviado instrumentos";
			return salida;
		}log.write("Datos obtenidos");
		if(entrada.rutinaValorizador.equals("BCS")){
			inicializaTipificarWsBCS();
			//Tipificar
			log.write("Rutina BCS - Inicia Tipificar (instrumentos no tipificados)");
			List<ValorizarWsBCS.ValorizarWsBCSObj> instip = new ArrayList<ValorizarWsBCS.ValorizarWsBCSObj>();
			for(ValoUtils.TareaValorizacion item : instrumentos){
				item.IdEstado = "INI";
				if(item.TipoVal==null || item.TipoVal.equals(-1)){
					ValorizarWsBCS.ValorizarWsBCSObj itemnuevo = new ValorizarWsBCS.ValorizarWsBCSObj();
					itemnuevo.nemo = item.Nemo;
					itemnuevo.fechaVal = item.FechaVal;
					boolean itemexiste = false;
					for(ValorizarWsBCS.ValorizarWsBCSObj check : instip){
						if (check.nemo.equals(itemnuevo.nemo)){
							itemexiste = true;
							break;
						}
					} if(!itemexiste) instip.add(itemnuevo);
				}
			}
			valobcs.obtenerTipoVal(instip);
			log.write("Rutina BCS - Procesa la tipificación realizada desde el WebService");
			for(ValoUtils.TareaValorizacion item : instrumentos){
				for(ValorizarWsBCS.ValorizarWsBCSObj itemtip : instip){
					if(item.Nemo.equals(itemtip.nemo)){
						item.FechaVal = itemtip.fechaVal;
						item.Estado = itemtip.estado;
						item.Glosa = itemtip.glosa;
						item.Hora = itemtip.hora;
						item.TipoVal = itemtip.tipoVal;
						item.WsCallerCodSalida = itemtip.wsCallerCodSalida;
						item.WsCallerDescSalida = itemtip.wsCallerDescSalida;
						if(!itemtip.wsCallerCodSalida.equals("0")) item.IdEstado = "ERR";
					}
				}
			}
			salida.instrumentos = instrumentos;
			log.write("Rutina BCS - Fin de la rutina");
		}
		// else if(entrada.rutinaValorizador.equals("OTRA_RUTINA")){}
		else{
			msjaux = "Rutina "+entrada.rutinaValorizador+" no implementada";
			log.write(msjaux);
			salida.erroresContador++;
			salida.descripcion = msjaux;
			return salida;
		}
		if(debug){
			log.writeAsJson("Entrada TipificarWs",entrada);
			log.writeAsJson("Salida TipificarWs",salida);
		}
		return salida;
	}
	
	/**********************
	 * métodos de la clase
	 **********************/
	private static void inicializaTipificarWsBCS() throws Exception{ 
		Log log = new Log("TipificarInstrumentos");
		if(valobcs == null){
    		Properties prop = new Properties();
            InputStream input = new FileInputStream("valorizador.properties");
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=("Y".equalsIgnoreCase(prop.getProperty("DEPURATION"))?true:false); //Y|N
            int ConnTimeOut=Integer.valueOf(prop.getProperty("CONNTIMEOUT"));
            int ReadTimeOut=Integer.valueOf(prop.getProperty("READTIMEOUT"));
            input.close();
            log.write("configuracion valorizador, server: "+Server+",user: "+User);
            valobcs=new ValorizarWsBCS(Server,User,Pass,Depuracion,ConnTimeOut,ReadTimeOut);
    	}
    }
}
