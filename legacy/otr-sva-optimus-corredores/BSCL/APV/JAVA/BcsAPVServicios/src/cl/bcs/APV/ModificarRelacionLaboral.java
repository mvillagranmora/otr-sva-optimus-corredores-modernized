package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarRelacionLaboral implements JavaCall{	
	
	private final Log log = new Log("RelacionLaboral");
	private final String SQL_INSERT_RELACION_LABORAL = loadResourceToString("cl/bcs/APV/sql/AgregarRelacionLaboral_Insert.sql");
	
	public static class RelacionLaboral {
		public Long					IdPersona;
		public Long					IdEmpleador;
		public String				CodigoTipoCargoEmpleado;
		public BigDecimal			IngresoMensual;
		public String				FechaIngresoCargo; 
		public String				FechaRetiroCargo;
		public String				IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class ModificarRelacionLaboral_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public List<RelacionLaboral>		p_RelacionLaboral;
	    
	    public ModificarRelacionLaboral_Entrada(){
	    	p_RelacionLaboral = new ArrayList<RelacionLaboral>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ModificarRelacionLaboral_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ModificarRelacionLaboral_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
		
	public ModificarRelacionLaboral_Salida logica(Connection con, ModificarRelacionLaboral_Entrada in) throws Exception {
		ModificarRelacionLaboral_Salida out = new ModificarRelacionLaboral_Salida();
		SQL sql = new SQL();
		
		try {
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			};
		
			if (out.P_OUT_MENSAJES.size() > 0){
				return out;
			};
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			sql.batch(con,SQL_INSERT_RELACION_LABORAL,in.p_RelacionLaboral, RelacionLaboral.class);
			/*
			for (MensajeSalida  AdveError: resultSP.P_OUT_MENSAJES){
				out.P_OUT_MENSAJES.add(AdveError);
			};								
			out.P_OUT_ERRORESCONTADOR 	= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);			
			*/
			log.write(out.P_OUT_MENSAJES);

		}
		finally {
			//algo
		}		
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ModificarRelacionLaboral_Entrada jin = gson.fromJson(in.toString() ,ModificarRelacionLaboral_Entrada.class);
		ModificarRelacionLaboral_Salida out = logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_RelacionLaboral\":[]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_RelacionLaboral\":[{\"IdPersona\":179580,\"IdEmpleador\":1759213,\"CodigoTipoCargoEmpleado\":\"JEFCUS\",\"IngresoMensual\":0,\"FechaIngresoCargo\":\"0001-01-01\",\"FechaRetiroCargo\":\"0001-01-01\",\"IdEstado\":\"VIG\"}]}";
				
				ModificarRelacionLaboral_Entrada Objin = gson.fromJson(json, ModificarRelacionLaboral_Entrada.class);
				
				ModificarRelacionLaboral demo = new ModificarRelacionLaboral();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				ModificarRelacionLaboral_Salida ObjOut = demo.logica(con, Objin);
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