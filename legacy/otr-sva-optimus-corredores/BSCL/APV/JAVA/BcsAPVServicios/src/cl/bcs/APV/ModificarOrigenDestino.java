package cl.bcs.APV;

import java.sql.Connection;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarOrigenDestino implements JavaCall{	
	
	private final String SQL_UPDATE_ORIGENDESTINO = loadResourceToString("cl/bcs/APV/sql/ModificarOrigenDestino.sql");
	
	public static class OrigenDestino {
		public Long		IdOrigenDestinoTransicion;
		public String	Operacion;
		public Long		OrigenCtaBco;
		public Long		SocioNegocioCtaBco;
		public Long		DestinoCtaBco;
		public String	FechaModificacion;
		public Long 	IdUsuarioModificador;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class OrigenDestinoEntrada {
		public Long		IdOrigenDestinoTransicion;
		public String	Operacion;
		public Long		OrigenCtaBco;
		public Long		SocioNegocioCtaBco;
		public Long		DestinoCtaBco;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ModificarBonificacion_Entrada {
	    public Long					p_IdEmpresa;
	    public Long					p_IdUsuario;
	    public String				p_IdModulo;
	    public List<OrigenDestinoEntrada>	p_Parametros;
	    
	    public ModificarBonificacion_Entrada(){
	    	p_Parametros = new ArrayList<OrigenDestinoEntrada>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ModificarOrigenDestino_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ModificarOrigenDestino_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ModificarBonificacion_Entrada jin = gson.fromJson(in.toString() ,ModificarBonificacion_Entrada.class);
		ModificarOrigenDestino_Salida out = logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ModificarOrigenDestino_Salida logica(Connection con, ModificarBonificacion_Entrada in) throws Exception {
		ModificarOrigenDestino_Salida salida = new ModificarOrigenDestino_Salida();
		SQL sql = new SQL();
		MensajeSalida	msg	= new MensajeSalida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		
		for(OrigenDestinoEntrada fila : in.p_Parametros){
			
			OrigenDestino OD = new OrigenDestino();
			
			Date date = new Date();
			DateFormat Formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		  	OD.IdOrigenDestinoTransicion 	= fila.IdOrigenDestinoTransicion;
		  	OD.Operacion					= fila.Operacion;
		  	OD.OrigenCtaBco					= fila.OrigenCtaBco;
		  	OD.SocioNegocioCtaBco			= fila.SocioNegocioCtaBco;
		  	OD.DestinoCtaBco				= fila.DestinoCtaBco;
		  	OD.FechaModificacion			= Formato.format(date);
		  	OD.IdUsuarioModificador			= in.p_IdUsuario;
		  	OD.IdEstado						= fila.IdEstado;
		 		  			
			List<OrigenDestino> ParamsOD = new ArrayList<OrigenDestino>();
			
			ParamsOD.add(OD);

			int[] res = sql.batch(con, SQL_UPDATE_ORIGENDESTINO ,ParamsOD, OrigenDestino.class);

			if(res!=null && res.length>0 && res[0]==1){
				msg.id = 1L;
				msg.tipo = "INFORMACION";
				msg.codigo = "UI_DATOS_MODIFICADOS";
				msg.descripcion = "Datos Modificados";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 0L;	
			}else{
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "UI_ERROR_MODIFICACION";
				msg.descripcion = "Error al Modificar los Datos.";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}
		}
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_RelacionLaboral\":[]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdOrigenDestinoTransicion\":1,\"Operacion\":\"DISTRIBUCION\",\"OrigenCtaBco\":0,\"SocioNegocioCtaBco\":100,\"DestinoCtaBco\":109,\"IdEstado\":\"ACT\"}]}";
				ModificarBonificacion_Entrada Objin = gson.fromJson(json, ModificarBonificacion_Entrada.class);
				
				ModificarOrigenDestino demo = new ModificarOrigenDestino();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				ModificarOrigenDestino_Salida ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
					//er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}