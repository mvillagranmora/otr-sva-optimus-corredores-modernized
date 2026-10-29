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

public class P40EliminarFiliales implements JavaCall{
		
	private final String SQL_DELETE_FILIALESP40 = loadResourceToString("cl/bcs/normativa/interfaces/sql/P40EliminarFiliales_DeleteFiliales.sql");
	
	public static class EntradaP40EliminarFiliales{
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public Long  p_IdSocioDeNegocio;		
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaP40EliminarFiliales{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaP40EliminarFiliales() {
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
		public Long IdEmpresa;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaP40EliminarFiliales in=gson.fromJson(jin.toString() , EntradaP40EliminarFiliales.class);
		SalidaP40EliminarFiliales out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public  SalidaP40EliminarFiliales logica(Connection con, EntradaP40EliminarFiliales entrada) throws Exception {
		SalidaP40EliminarFiliales out = new SalidaP40EliminarFiliales();
		SQL sql = new SQL();
		try {
			if(entrada.p_IdSocioDeNegocio != null && entrada.p_IdSocioDeNegocio != 0L) {	
				List<FilialesP40> listFilial = new ArrayList<>();
				FilialesP40 filial = new FilialesP40();
				filial.IdEmpresa = entrada.p_IdEmpresa;
				filial.IdSocioDeNegocio = entrada.p_IdSocioDeNegocio;
				listFilial.add(filial);
				
				sql.batch(con, SQL_DELETE_FILIALESP40, listFilial, FilialesP40.class);
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"OK","Datos Eliminados Exitosamente"));
			}else {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_NO_HAY_DATOS","No hay Datos de entrada")); 
			}
		}catch(SQLException e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_FILIAL_NO_EXISTE","Filial no existe")); 
		}
		return out;
	}
		
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			EntradaP40EliminarFiliales entrada= new EntradaP40EliminarFiliales();
			entrada.p_IdEmpresa		= new Long(1);
			entrada.p_IdModulo = "NOR";
			entrada.p_IdSocioDeNegocio = new Long(245883);
			JsonParser parser 		= new JsonParser();
			JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			P40EliminarFiliales demo= new P40EliminarFiliales();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}