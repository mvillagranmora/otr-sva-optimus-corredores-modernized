package cl.bcs.mesa;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarRequisitosBasicosMRF implements JavaCall{
	
	
	private final String SQL_SELECT_PLANTILLA = loadResourceToString("cl/bcs/mesa/sql/select_validacion_plantilla.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/sql/select_validacion_portafolio.sql");
	
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	public static class EntradaValidacion{
		
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;		
		
		public String p_IdTipoOperacion;
		public Long   p_IdPortafolio;		
		public Long   p_IdEmpleado;
		public String p_IdTipoProducto;
	}
	
	public static class DetalleSalida{
		public Long IdInterno;
		public String IdOperacion;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public DetalleSalida (Long IdInterno, String IdOperacion,String Codigo,String Descripcion){
			this.IdInterno = IdInterno;
			this.IdOperacion= IdOperacion;
			this.Codigo= Codigo;
			this.Descripcion = Descripcion;
			
		}
	}
	
	
	public static class SalidaValidacion{
		
		
		public int p_out_AdvertenciasContador;
		public int p_out_ErroresContador;
		
		public List <DetalleSalida> p_out_AdvertenciaDetalle;
		public List <DetalleSalida> p_out_ErroresDetalle;
		
		public SalidaValidacion(){			
			p_out_AdvertenciasContador= 0;
			p_out_ErroresContador=0;
		}
		
		
	}
	public static class Resultado{
		public String resultado;
	}
	
	
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	
	public SalidaValidacion logica(Connection conn, EntradaValidacion entrada) throws Exception {
		SQL sql = new SQL();
		SalidaValidacion salida= new SalidaValidacion();
		
		
		HashMap<String,Object> paramsPlanilla = new HashMap<>();
		paramsPlanilla.put("p_IdEmpresa",entrada.p_IdEmpresa);
		paramsPlanilla.put("p_IdTipoOperacion",entrada.p_IdTipoOperacion);
		paramsPlanilla.put("p_IdEmpleado",entrada.p_IdEmpleado);
		
		HashMap<String,Object> paramsProducto = new HashMap<>();
		paramsProducto.put("p_IdEmpresa",entrada.p_IdEmpresa);
		paramsProducto.put("p_IdPortafolio",entrada.p_IdPortafolio);		
		paramsProducto.put("p_IdTipoProducto",entrada.p_IdTipoProducto);

		
		List<Resultado> EmpleadoTienePlantilla = sql.query(conn, SQL_SELECT_PLANTILLA,paramsPlanilla,Resultado.class);	
		List<Resultado> PortafolioTieneProducto = sql.query(conn, SQL_SELECT_PORTAFOLIO, paramsProducto,Resultado.class);
		
		
		String ResulPlantilla = EmpleadoTienePlantilla.get(0).resultado;
		String ResultProducto = PortafolioTieneProducto.get(0).resultado;
		
		salida.p_out_ErroresDetalle = new ArrayList<DetalleSalida>();
		
		if(ResulPlantilla.equals("N"))
		{				
			DetalleSalida detallePlanilla = new DetalleSalida (0L,"RECHAZO","OPERACION_NO_HABILITADA_PARA_EMPLEADO","UI_OPERACION_NO_HABILITADA_PARA_EMPLEADO");				
			salida.p_out_ErroresDetalle.add(detallePlanilla);
			salida.p_out_ErroresContador++;
		}
		
		if(ResultProducto.equals("N")){
			DetalleSalida detalleProducto = new DetalleSalida (0L,"RECHAZO","PRODUCTO_NO_HABILITADO","UI_PRODUCTO_NO_HABILITADO");			
			salida.p_out_ErroresDetalle.add(detalleProducto);
			salida.p_out_ErroresContador++;
		}
		
		return salida;
	}
	
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaValidacion in=gson.fromJson(jin.toString() ,EntradaValidacion.class);
		SalidaValidacion out=logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	
	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	
	public static void main(String[]args) throws Exception{
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
			System.out.println("== COMIENZA==");
			
			try{
				/*
				p_IdEmpleado:7889
				p_IdEmpresa:1
				p_IdModulo:"MRF"
				p_IdPortafolio:1450055
				p_IdTipoOperacion:"VENPAC"
				p_IdTipoProducto:"MERF"
				p_IdUsuario:21
				*/
			
				
				EntradaValidacion entrada =new EntradaValidacion();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "MRF";
				
				entrada.p_IdTipoOperacion   = "VENPAC";
				entrada.p_IdPortafolio = new Long(1450055);
				entrada.p_IdEmpleado = new Long(7889);
				entrada.p_IdTipoProducto = "MERF";
				
				JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        ConsultarRequisitosBasicosMRF demo=new ConsultarRequisitosBasicosMRF();
		    	System.out.println(demo.call(con, dataJsonIn).toString());	
		    	
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
