package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class AsignarDepositoEmpleador implements JavaCall   {
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_Asignar_Deposito_Empleador = loadResourceToString("cl/bcs/APV/sql/AsignarDepositoEmpleador.sql");
	public String SQL_Total_Deposito_Empleador = loadResourceToString("cl/bcs/APV/sql/TotalDepositoEmpleador.sql");

	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class DepositoEmpleador{
		
		public Long 	IdRecaudacion;
		public double 	MontoDeposito;
		public Long 	IdRecaudacionEmpleador;
		public double 	MontoEmpleador;
	}
	
	public static class TotalDepositoEmpleador{
		public BigDecimal	Total;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		
		public List<DepositoEmpleador>		p_Parametros;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<DepositoEmpleador>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();		
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		
		for(AsignarDepositoEmpleador.DepositoEmpleador fila : in.p_Parametros){
			
			SQL sql = new SQL();			
									
			HashMap<String,Object> paramsDepositoEmpleador = new HashMap<String,Object>();
			paramsDepositoEmpleador.put("IdRecaudacion", fila.IdRecaudacion);
			
			List<TotalDepositoEmpleador> totalDepositoEmpleador = sql.query(con, SQL_Total_Deposito_Empleador, paramsDepositoEmpleador, TotalDepositoEmpleador.class);
			
			BigDecimal total = totalDepositoEmpleador.get(0).Total;
				
			double totalDouble = total.doubleValue();
			
			if(totalDouble >= 0)
			{
				if(fila.MontoDeposito - totalDouble > 0){
					if(totalDouble + fila.MontoEmpleador <= fila.MontoDeposito)
					{
						DepositoEmpleador DE = new DepositoEmpleador();

						DE.IdRecaudacion 			= fila.IdRecaudacion;
						DE.IdRecaudacionEmpleador 	= fila.IdRecaudacionEmpleador;
						DE.MontoDeposito 			= fila.MontoDeposito;
						DE.MontoEmpleador 			= fila.MontoEmpleador;
						
						List<DepositoEmpleador> ParamsDepositoEmpleador = new ArrayList<DepositoEmpleador>();
						
						ParamsDepositoEmpleador.add(DE);
						
						int[] res = sql.batch(con, SQL_Asignar_Deposito_Empleador ,ParamsDepositoEmpleador, DepositoEmpleador.class);

						if(res!=null && res.length>0 && res[0]==1){
							msg.id = 1L;
							msg.tipo = "APV";
							msg.codigo = "OK";
							msg.descripcion = "Asignación exitosa";
							salida.P_OUT_MENSAJES.add(msg);
					
						}else{
							msg.id = 2L; 
							msg.tipo = "APV";
							msg.codigo = "ERROR";
							msg.descripcion = "Problemas al Conciliar";
							salida.P_OUT_MENSAJES.add(msg);
						}
					}else{
						msg.id = 3L;
						msg.tipo = "APV";
						msg.codigo = "ERROR";
						msg.descripcion = "Monto Empleador Excede el Monto Depósito Disponible";
						salida.P_OUT_MENSAJES.add(msg);
					}
				}else{
					msg.id = 4L;
					msg.tipo = "APV";
					msg.codigo = "ERROR";
					msg.descripcion = "Monto Empleador Excede el Monto Depósito Disponible";
					salida.P_OUT_MENSAJES.add(msg);
				}
			}
		}
		return salida;
	}
	
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {				
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				DepositoEmpleador DE = new DepositoEmpleador();
				
				//String dateSystem = Utils.getFechaSistema(con, Utils.getSchema(), 1);

				DE.IdRecaudacion = 92L;
				DE.IdRecaudacionEmpleador = 66L;
				DE.MontoDeposito = 20000;
				DE.MontoEmpleador = 5000;
				
				List<DepositoEmpleador> ParamsDepositoEmpleador = new ArrayList<DepositoEmpleador>();
				
				ParamsDepositoEmpleador.add(DE);
				Objin.p_IdEmpresa 	= 1L;
				Objin.p_IdUsuario 	= 23L;
				Objin.p_IdModulo 	= "APV";
				
				Objin.p_Parametros = ParamsDepositoEmpleador;
				
				AsignarDepositoEmpleador demo = new AsignarDepositoEmpleador();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}