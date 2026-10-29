package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarConfiguracionBloqueoAutomaticoClientes implements JavaCall {	
	private final Log log = new Log("CLI_ModificarConfiguracionBloqueoAutomaticoClientes");

	//QUERIES
	private final String SQL_UPSERT_BLOQUEOAUTOMATICOCLIENTES = "upsert \"BSCL.SocioNegocio.BD::CLI.BloqueoAutomaticoClientes\""
			+ "(\"IdEmpresa\",\"IdEstado\",\"BloqueoCumplirMayoriaEdad\",\"BloqueoFichaDesactualizada\",\"BloqueoPortafolioSinOperaciones\","
			+ "\"FechaModificacion\")values(:IdEmpresa,:IdEstado,:BloqueoCumplirMayoriaEdad,:BloqueoFichaDesactualizada,:BloqueoPortafolioSinOperaciones,current_utctimestamp) where \"IdEmpresa\"=:IdEmpresa;";
	private final String SQL_UPSERT_DETALLEBLOQUEOAUTOMATICOCLIENTES = "upsert \"BSCL.SocioNegocio.BD::CLI.DetalleBloqueoAutomaticoClientes\""
			+ "(\"IdEmpresa\",\"IdTipoDocumento\",\"IdEstado\")values(:IdEmpresa,:IdTipoDocumento,:IdEstado) where \"IdEmpresa\"=:IdEmpresa and \"IdTipoDocumento\"=:IdTipoDocumento";
	/*******************
	 * pojos
	 *******************/
	public static class BloqueoAutomaticoClientes {
		public Long					IdEmpresa; 
		public String				IdEstado; 
		public Long					BloqueoCumplirMayoriaEdad; 
		public Long					BloqueoFichaDesactualizada; 
		public Long					BloqueoPortafolioSinOperaciones; 
		public java.sql.Timestamp	FechaCreacion; 
		public String				FechaCreacionStr; 
		public java.sql.Timestamp	FechaModificacion; 
		public String				FechaModificacionStr; 
	}
	public static class DetalleBloqueoAutomaticoClientes {
		public Long					IdEmpresa; 
		public String				IdTipoDocumento; 
		public String				IdEstado; 
	}
	/*******************
	 * clases in/out
	 *******************/
	public static class InModificarConfiguracionBloqueoAutomaticoClientes {
		public Long 		p_IdEmpresa;
		public String 		p_IdEstado;
		public Long 		p_BloqueoCumplirMayoriaEdad;
		public Long 		p_BloqueoFichaDesactualizada;
		public Long 		p_BloqueoPortafolioSinOperaciones;
		public List<Registro> p_Registros;
		public static class Registro{
			public String	IdTipoDocumento;
			public String	IdEstado;
		}
		public InModificarConfiguracionBloqueoAutomaticoClientes(){
			p_Registros = new ArrayList<Registro>();
		}
	}
	
	public static class OutModificarConfiguracionBloqueoAutomaticoClientes{
		public Long 		P_OUT_ERRORESCONTADOR;
		public String		P_OUT_DESCRIPCION;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InModificarConfiguracionBloqueoAutomaticoClientes in=gson.fromJson(jin.toString() ,InModificarConfiguracionBloqueoAutomaticoClientes.class);
		OutModificarConfiguracionBloqueoAutomaticoClientes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InModificarConfiguracionBloqueoAutomaticoClientes entrada = new InModificarConfiguracionBloqueoAutomaticoClientes();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdEstado="ACT";
			entrada.p_BloqueoCumplirMayoriaEdad=0L;
			entrada.p_BloqueoFichaDesactualizada=0L;
			entrada.p_BloqueoPortafolioSinOperaciones=0L;
			InModificarConfiguracionBloqueoAutomaticoClientes.Registro item1 = new InModificarConfiguracionBloqueoAutomaticoClientes.Registro();
			item1.IdEstado="ACT";
			item1.IdTipoDocumento="CONTRATOMERCADOFUTUROS";
			entrada.p_Registros.add(item1);
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ModificarConfiguracionBloqueoAutomaticoClientes demo=new ModificarConfiguracionBloqueoAutomaticoClientes();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e){e.printStackTrace();}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutModificarConfiguracionBloqueoAutomaticoClientes logica(Connection con, InModificarConfiguracionBloqueoAutomaticoClientes entrada) throws Exception {
		log.write("[INI]Ejecucion del SP");
		log.writeAsJson("Entrada SP",entrada);
		OutModificarConfiguracionBloqueoAutomaticoClientes salida = new OutModificarConfiguracionBloqueoAutomaticoClientes();
		salida.P_OUT_ERRORESCONTADOR=1L;
		SQL sql=new SQL();
		List<BloqueoAutomaticoClientes> listbac =new ArrayList<BloqueoAutomaticoClientes>();
		List<DetalleBloqueoAutomaticoClientes> listdbac =new ArrayList<DetalleBloqueoAutomaticoClientes>();
		BloqueoAutomaticoClientes itembac = new BloqueoAutomaticoClientes();
		itembac.IdEmpresa=entrada.p_IdEmpresa; 
		itembac.IdEstado=(entrada.p_IdEstado==null)?"ACT":entrada.p_IdEstado; 
		itembac.BloqueoCumplirMayoriaEdad=entrada.p_BloqueoCumplirMayoriaEdad; 
		itembac.BloqueoFichaDesactualizada=entrada.p_BloqueoFichaDesactualizada; 
		itembac.BloqueoPortafolioSinOperaciones=entrada.p_BloqueoPortafolioSinOperaciones; 
		listbac.add(itembac);
		for(InModificarConfiguracionBloqueoAutomaticoClientes.Registro item:entrada.p_Registros){
			DetalleBloqueoAutomaticoClientes itemdbac = new DetalleBloqueoAutomaticoClientes();
			itemdbac.IdEmpresa=entrada.p_IdEmpresa; 
			itemdbac.IdTipoDocumento=item.IdTipoDocumento; 
			itemdbac.IdEstado=item.IdEstado; 
			listdbac.add(itemdbac);
		}
		log.write("[EJE]Ejecución SQL Batch");
		sql.batch(con,SQL_UPSERT_BLOQUEOAUTOMATICOCLIENTES, listbac,BloqueoAutomaticoClientes.class);
		sql.batch(con,SQL_UPSERT_DETALLEBLOQUEOAUTOMATICOCLIENTES, listdbac,DetalleBloqueoAutomaticoClientes.class);
		salida.P_OUT_ERRORESCONTADOR=0L;
		log.writeAsJson("Salida SP",salida);
		log.write("[FIN]Finaliza la ejecución");
		return salida;
	}
	
}
