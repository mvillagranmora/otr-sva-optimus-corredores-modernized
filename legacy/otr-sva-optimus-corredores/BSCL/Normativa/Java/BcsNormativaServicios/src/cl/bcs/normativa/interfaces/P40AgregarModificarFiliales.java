package cl.bcs.normativa.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class P40AgregarModificarFiliales implements JavaCall{
		
	private final String SQL_UPSERT_FILIALESP40 = loadResourceToString("cl/bcs/normativa/interfaces/sql/P40AgregarModificarFiliales_UpsertFiliales.sql");
	
	public static class EntradaP40AgregarModificarFiliales{
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public List<FilialesP40>  p_FilialesP40;		
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaP40AgregarModificarFiliales{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaP40AgregarModificarFiliales() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FilialesP40{
		public Long IdSocioDeNegocio;
		public String Rut;
		public Long IdEmpresa;
		public String NombreFilial;
		public String CodigoTenedor;
		public String NombreZip;
		public String NombreArchivo;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaP40AgregarModificarFiliales in=gson.fromJson(jin.toString() , EntradaP40AgregarModificarFiliales.class);
		SalidaP40AgregarModificarFiliales out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public  SalidaP40AgregarModificarFiliales logica(Connection con, EntradaP40AgregarModificarFiliales entrada) throws Exception {
		SalidaP40AgregarModificarFiliales out = new SalidaP40AgregarModificarFiliales();
		SQL sql = new SQL();
		try {
			if(entrada.p_FilialesP40 != null && !entrada.p_FilialesP40.isEmpty()) {	
				sql.batch(con, SQL_UPSERT_FILIALESP40, entrada.p_FilialesP40, FilialesP40.class);
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"OK","Datos Ingresados Exitosamente"));
			}else {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_NO_HAY_DATOS","No hay Datos de entrada")); 
			}
		} catch(SQLException e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_AGREGAR","Error al Agregar Filial: " + e)); 
		}
		return out;
	}
		
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new Gson();			
			String jsonC = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"NOR\",\"p_FilialesP40\":[{\"Rut\":\"15443645-6\",\"IdEmpresa\":1,\"NombreFilial\":\"CRISTIAN FLORES ORELLANA ///\",\"IdSocioDeNegocio\":245883,\"CodigoTenedor\":\"345\",\"NombreZip\":\"FSDDF\",\"NombreArchivo\":\"SDF\"}]}";
			EntradaP40AgregarModificarFiliales Objin = gson.fromJson(jsonC, EntradaP40AgregarModificarFiliales.class);
			P40AgregarModificarFiliales demo=new P40AgregarModificarFiliales();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaP40AgregarModificarFiliales ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}