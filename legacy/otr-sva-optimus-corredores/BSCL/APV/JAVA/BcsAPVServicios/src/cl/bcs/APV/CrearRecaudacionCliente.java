package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearRecaudacionCliente implements JavaCall   {

	static final Logger logger = LogManager.getLogger(CrearRecaudacionCliente.class);

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_Recaudacion_Cliente = loadResourceToString("cl/bcs/APV/sql/DetalleCliente_Insert.sql");
	public String SQL_MODIFICAR_Recaudacion_Cliente = loadResourceToString("cl/bcs/APV/sql/DetalleCliente_Update.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Cliente extends BcsPojo{

		public Long 		IdInterno;
		public Long 		IdRecaudacionCliente;
		public Long 		IdRecaudacion;
		public Long 		IdRecaudacionEmpleador;
		public Long 		IdEmpresa;
		public Long 		IdCliente;
		public String		FechaIngreso;
		public String 		TipoAfiliado;
		public String 		IdRegimenTriburario;
		public BigDecimal			MontoDepositoConvenido;
		public BigDecimal 		MontoDepositoAPV;
		public String 		Origen;
		public String 		IdEstado;
		
		public Cliente(){
			IdInterno = 0L;
		}
	}
		
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio extends BcsPojo{
		
		public 	Long			p_IdEmpresa;
		public	Long			p_IdUsuario;
		public 	String			p_IdModulo;
		public	String			p_Tipo; //Ingreso o Modificación ("I" o "M")
		public	String			p_Origen;
		
		public List<Cliente>	p_Parametros;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<Cliente>();
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio extends BcsPojo{		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Resultado>		P_OUT_RESULTADO;
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
		}		
	};

	public static class Resultado extends BcsPojo{
		public  Long		IdInterno;
	    public 	Long		IdRecaudacionCliente;
		
	}

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
		logger.info("INICIO::CrearRecaudacionCliente");
		Long inicio = System.currentTimeMillis();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		
		List<Long> idRecaudacionCliente = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::REC.IdRecaudacionCliente", (long)in.p_Parametros.size());
		int contadorIds = 0;
		SQL sql = new SQL();
		
		for(CrearRecaudacionCliente.Cliente fila : in.p_Parametros){
			if(in.p_Tipo.compareTo("M")==0){
				
			}else{
				fila.IdRecaudacionCliente = idRecaudacionCliente.get(contadorIds);
			}
			
			//ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
			Cliente Cli = new Cliente();

		  	Cli.IdRecaudacionCliente 	= fila.IdRecaudacionCliente;
		  	Cli.IdRecaudacion 			= fila.IdRecaudacion;
		  	Cli.IdRecaudacionEmpleador	= fila.IdRecaudacionEmpleador;
		    Cli.IdEmpresa 				= in.p_IdEmpresa; 			
		    Cli.IdCliente 				= fila.IdCliente; 						
			Cli.FechaIngreso 			= fila.FechaIngreso; 					
			Cli.TipoAfiliado 			= fila.TipoAfiliado;					
			Cli.IdRegimenTriburario 	= fila.IdRegimenTriburario; 							
			Cli.MontoDepositoConvenido 	= fila.MontoDepositoConvenido;							
			Cli.MontoDepositoAPV 		= fila.MontoDepositoAPV; 					
			Cli.Origen 					= fila.Origen;
			Cli.IdEstado 				= fila.IdEstado;		
			
			List<Cliente> ParamsInsertCliente = new ArrayList<Cliente>();
			
			ParamsInsertCliente.add(Cli);

			int[] res = sql.batch(con, in.p_Tipo.compareTo("M")==0 ? SQL_MODIFICAR_Recaudacion_Cliente : SQL_CREAR_Recaudacion_Cliente ,ParamsInsertCliente, Cliente.class);

			ParametrosSalidaServicio salidaEstado = new ParametrosSalidaServicio();
			if(res!=null && res.length>0 && res[0]==1){
				msg.id = 1L;
				msg.tipo = "APV";
				msg.codigo = in.p_Tipo.compareTo("M")==0 ? "MODIFICACION_CLIENTE_OK" : "CREACION_CLIENTE_OK";
				msg.descripcion = in.p_Tipo.compareTo("M")==0 ? "Modificación de Detalle Cliente exitosa" :"Creacion de Detalle Cliente exitosa";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 0L;	
				Resultado resu = new Resultado();
				resu.IdInterno = fila.IdInterno;
				resu.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				
				salida.P_OUT_RESULTADO.add(resu);
				
				List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado>();

				CambiarEstadoRecaudacion.ParametrosRecaudacionEstado RecaudacionEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();
		
				if (fila.IdRecaudacionEmpleador == null) fila.IdRecaudacionEmpleador = 0L;
				RecaudacionEstado.FolioRecaudacion = fila.IdRecaudacion;
				RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
				RecaudacionEstado.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				RecaudacionEstado.IdEstado = "ING";
				paramsCambioEstado.add(RecaudacionEstado);
				
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
				msg.descripcion = in.p_Tipo.compareTo("M")==0 ? "Problemas al Modificar Detalle Cliente" : "Problemas al Crear Detalle Cliente";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}

			//FIN IF
			contadorIds ++;
		}
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::CrearRecaudacionCliente{}", fin - inicio);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		return salida;
		
		
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
			//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
				Long time = System.currentTimeMillis();
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"M\",\"p_Origen\":\"MANUAL\",\"p_Parametros\":[{\"IdRecaudacionCliente\":2643,\"IdRecaudacion\":468,\"IdRecaudacionEmpleador\":null,\"IdEmpresa\":1,\"IdCliente\":1423322,\"FechaIngreso\":\"2018-04-20\",\"TipoAfiliado\":\"IND\",\"IdRegimenTriburario\":\"APV-A\",\"MontoDepositoConvenido\":0,\"MontoDepositoAPV\":310,\"Origen\":\"Manual\",\"IdEstado\":\"ING\"}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				Cliente CLI = new Cliente();
				 
				CrearRecaudacionCliente demo = new CrearRecaudacionCliente();	
				
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