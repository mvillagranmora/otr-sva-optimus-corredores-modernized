package cl.bcs.APV;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CargaMasivaPAC.Banco;
import cl.bcs.APV.CrearRecaudacionCliente.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearRecaudacionEmpleador implements JavaCall   {

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_Recaudacion_Empleador = loadResourceToString("cl/bcs/APV/sql/DetalleEmpleador_Insert.sql");
	public String SQL_MODIFICAR_Recaudacion_Empleador = loadResourceToString("cl/bcs/APV/sql/DetalleEmpleador_Update.sql");
	public String SQL_DETALLE_CLIENTES_POR_DETALLE_EMPLEADOR = loadResourceToString("cl/bcs/APV/sql/ConsultarDetalleClientesPorDetalleEmpleador.sql");
		
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Empleador{

		public Long 		IdInterno; 
		public Long 		IdRecaudacionEmpleador;
		public Long 		IdRecaudacion;
		public String		CodigoArchivo;
		public Long 		IdEmpresa;
		public Long 		IdPersona;
		public String		FechaIngreso;
		public String 		PeriodoPago;
		public Long 		NumeroHojas;
		public String 		TipoAfiliado;
		public String 		TipoPago;
		public String 		Insistencias;
		public Long 		MontoDepositoConvenido;
		public Long 		MontoDepositoAPV;
		public Long 		Reajustes;
		public Long 		Intereses;
		public String 		Origen;
		public String 		Observaciones;
		public String 		IdEstado;
		
	    public Empleador() {
	    	IdInterno = 0L;
	    }
	}

	public static class Resultado {
		public  Long		IdInterno;
	    public 	Long		IdRecaudacion;
	    public 	Long		IdRecaudacionEmpleador;
		
	}
	
	public static class DetalleCliente{
		public Long	IdRecaudacionCliente;
	}
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long			p_IdEmpresa;
		public	Long			p_IdUsuario;
		public 	String			p_IdModulo;
		public	String			p_Tipo; //Ingreso o Modificación ("I" o "M")
		public	String			p_Origen;
		
		public List<Empleador>	p_Parametros;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<Empleador>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_IDRECEMP;
		public List<Resultado>		P_OUT_RESULTADO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
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
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		
		List<Long> idRecaudacionEmpleador = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::REC.IdRecaudacionEmpleador", (long)in.p_Parametros.size());
		int contadorIds = 0;
		for(CrearRecaudacionEmpleador.Empleador fila : in.p_Parametros){
			
			Long time = System.currentTimeMillis();
			java.sql.Date FechaSistema = new java.sql.Date(time);
			
			Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());
				
			//*** VALIDACION CONTRATOS SAAPV ***
			if(in.p_Tipo.compareTo("M")==0){
				
			}else{
				fila.IdRecaudacionEmpleador = idRecaudacionEmpleador.get(contadorIds);
			}
			
			SQL sql = new SQL();			
			Empleador Emp = new Empleador();

		    Emp.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
		  	Emp.IdRecaudacion = fila.IdRecaudacion;
		  	Emp.CodigoArchivo = fila.CodigoArchivo;
		    Emp.IdEmpresa = in.p_IdEmpresa; 			
		    Emp.IdPersona = fila.IdPersona; 						
			Emp.FechaIngreso = fila.FechaIngreso; 					
			Emp.PeriodoPago = fila.PeriodoPago; 				
			Emp.NumeroHojas = fila.NumeroHojas; 							
			Emp.TipoAfiliado = fila.TipoAfiliado;					
			Emp.TipoPago = fila.TipoPago;							
			Emp.Insistencias = fila.Insistencias; 							
			Emp.MontoDepositoConvenido = fila.MontoDepositoConvenido;							
			Emp.MontoDepositoAPV = fila.MontoDepositoAPV; 					
			Emp.Reajustes = fila.Reajustes;
			Emp.Intereses = fila.Intereses;
			Emp.Origen = fila.Origen;
			Emp.Observaciones = fila.Observaciones;	
			Emp.IdEstado = fila.IdEstado;		
			
			List<Empleador> ParamsInsertEmpleador = new ArrayList<Empleador>();
			
			ParamsInsertEmpleador.add(Emp);

			int[] res = sql.batch(con, in.p_Tipo.compareTo("M")==0 ? SQL_MODIFICAR_Recaudacion_Empleador : SQL_CREAR_Recaudacion_Empleador ,ParamsInsertEmpleador, Empleador.class);

			if(res!=null && res.length>0 && res[0]==1){
				msg.id = 1L;
				msg.tipo = "APV";
				msg.codigo = in.p_Tipo.compareTo("M")==0 ? "MODIFICACION_EMPLEADOR_OK" : "CREACION_EMPLEADOR_OK";
				msg.descripcion = in.p_Tipo.compareTo("M")==0 ? "Modificación de Detalle Empleador exitosa" :"Creacion de Detalle Empleador exitosa";
				Resultado resu = new Resultado();
				resu.IdInterno = fila.IdInterno;
				resu.IdRecaudacion = fila.IdRecaudacion;
				resu.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;

				salida.P_OUT_RESULTADO.add(resu);
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_IDRECEMP = fila.IdRecaudacionEmpleador;
				
				List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado>();

				CambiarEstadoRecaudacion.ParametrosRecaudacionEstado RecaudacionEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();
				
				HashMap<String, Object> param;
				param = new HashMap<>();
				param.put("IdRecaudacionEmpleador", fila.IdRecaudacionEmpleador);

				List<DetalleCliente> detalleClientes = sql.query(con, SQL_DETALLE_CLIENTES_POR_DETALLE_EMPLEADOR, param, DetalleCliente.class);
					
				RecaudacionEstado.FolioRecaudacion = fila.IdRecaudacion;
				RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
				RecaudacionEstado.IdRecaudacionCliente = 0L;
				RecaudacionEstado.IdEstado = "ING";
				paramsCambioEstado.add(RecaudacionEstado);
				
				for(DetalleCliente detCli : detalleClientes){
					CambiarEstadoRecaudacion.ParametrosRecaudacionEstado ClienteEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();

					ClienteEstado.FolioRecaudacion = 0L;
					ClienteEstado.IdRecaudacionEmpleador = 0L;
					ClienteEstado.IdRecaudacionCliente = detCli.IdRecaudacionCliente;
					ClienteEstado.IdEstado = "ING";
					paramsCambioEstado.add(ClienteEstado);
				}
				
				CambiarEstadoRecaudacion.ParametrosEntradaServicio paramsCambioEstado_Entrada = new CambiarEstadoRecaudacion.ParametrosEntradaServicio();

			    paramsCambioEstado_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			    paramsCambioEstado_Entrada.p_IdUsuario = in.p_IdUsuario;
			    paramsCambioEstado_Entrada.p_IdModulo = in.p_IdModulo;
			    paramsCambioEstado_Entrada.p_Parametros = paramsCambioEstado;
			    
			    CambiarEstadoRecaudacion ProcesoCambioEstado = new CambiarEstadoRecaudacion();

			    CambiarEstadoRecaudacion.ParametrosSalidaServicio OutCambioEstado = ProcesoCambioEstado.logica(con, paramsCambioEstado_Entrada); 
		
			}else{
				msg.id = 1L;
				msg.tipo = "APV";
				msg.codigo = "ERROR";
				msg.descripcion = in.p_Tipo.compareTo("M")==0 ? "Problemas al modificar Detalle Empleador" : "Problemas al crear Detalle Empleador";
				salida.P_OUT_MENSAJES.add(msg);
			}

			//FIN IF
			contadorIds ++;
		}
		return salida;
		
		
	}
	
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				
				Long time = System.currentTimeMillis();
				java.sql.Date FechaSistema = Fecha.toDate("1978-01-01T03:00:00.000Z");
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Origen\":\"MANUAL\",\"p_Parametros\":[{\"IdRecaudacionEmpleador\":132,\"IdRecaudacion\":329,\"IdEmpresa\":1,\"IdPersona\":1773691,\"FechaIngreso\":\"2018-05-16\",\"PeriodoPago\":\"2017-10\",\"NumeroHojas\":1,\"TipoAfiliado\":\"\",\"TipoPago\":\"ATRASADO\",\"Insistencias\":\"\",\"MontoDepositoConvenido\":0,\"MontoDepositoAPV\":195000,\"Reajustes\":0,\"Intereses\":0,\"Origen\":\"Manual\",\"Observaciones\":\"\",\"IdEstado\":\"ING\"}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
//				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
//				Empleador EMP = new Empleador();
//				
//				EMP.IdRecaudacionEmpleador = 0L;
//				EMP.IdRecaudacion = 1L;
//				EMP.IdEmpresa = 1L;
//				EMP.IdPersona = 1L;
//				EMP.FechaIngreso = "2017-11-06";
//				EMP.PeriodoPago = "2017-11";
//				EMP.NumeroHojas = 1L;
//				EMP.TipoAfiliado = "DEP";
//				EMP.TipoPago = "Normal";
//				EMP.Insistencias = "";
//				EMP.MontoDepositoConvenido = 0L;
//				EMP.MontoDepositoAPV = 0L;
//				EMP.Reajustes = 0L;
//				EMP.Intereses = 0L;
//				EMP.Origen = "Manual";
//				EMP.Observaciones = "";
//				EMP.IdEstado = "VIG";
//				List<Empleador> ParamsEmpleador = new ArrayList<Empleador>();
//				
//				ParamsEmpleador.add(EMP);
//				Objin.p_IdEmpresa 	= 1L;
//				Objin.p_IdUsuario 	= 2L;
//				Objin.p_IdModulo 	= "APV";
//				Objin.p_Tipo = "I";
//				Objin.p_Origen = "Manual";
//				
//				Objin.p_Parametros = ParamsEmpleador;
				 
				CrearRecaudacionEmpleador demo = new CrearRecaudacionEmpleador();	
				
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