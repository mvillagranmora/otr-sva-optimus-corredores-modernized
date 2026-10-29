package cl.bcs.cuentaInversion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public  class ProcesaIndicesAtributtionAnalisys implements JavaCall
{
	private final String SQL_INSERT_CREA_INDICE              = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_InsertaIndice.sql");
	private final String SQL_SELECT_EXISTE_INDICE_SISTEMA    = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_ExisteIndiceSistema.sql");	
	private final String SQL_SELECT_EXISTE_RELACION_MERCADO  = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_ExisteRelacionMercadoSubmercado.sql");
	private final String SQL_UPDATE_MODIFICA_INDICE          = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_ModificaIndice.sql");
	private final String SQL_DELETE_ELIMINA_INDICE           = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_EliminaIndice.sql");
	private final String SQL_DELETE_ELIMINA_VALORES_INDICE   = loadResourceToString("cl/bcs/cuentaInversion/sql/ProcesaIndicesAtributtionAnalisys_EliminaValoresIndice.sql");
	private static final String TRANSACCION_INSERT	= "C";
	private static final String TRANSACCION_UPDATE 	= "M";
	private static final String TRANSACCION_DELETE 	= "E";
	
	/******************************************************************************
	 * 							CLASES
	 *******************************************************************************/
    public static class EntradaParametrosIndices{
    	    	
    	public Long p_IdEmpresa;
    	public String p_IdUsuario; 					
    	public String p_IdModulo;
    	public String p_simularOperacion;
    	public String p_UsaReglasNegocio;
    	public String p_Tarea;
    	public List<DatosIndice> p_Registros;
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
    
    public static class DatosIndice{
		public String p_IdIndiceFinanciero;
		public String p_Periodo;
		public String p_Nombre;
		public String p_Descripcion;
		public String p_IdMonedaParametro;
		public String p_IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class EliminaIndice{
	    public String	p_IdIndiceFinanciero;
	}
    
    public static class BuscaIndice{
	    public Long	Existe;
	}
    		
	public static class SalidaProcesamiento{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
		
			public SalidaProcesamiento(){
				this.P_OUT_ERRORESCONTADOR = 0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
	}	
	
		/******************************************************************************
		 * 						OBJETO DE TRANSPORTE
		 ******************************************************************************/
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaParametrosIndices in = gson.fromJson(jin.toString() ,EntradaParametrosIndices.class);
			
			//RecaudacionBCP_Entrada in = gson.fromJson(jin.toString(), RecaudacionBCP_Entrada.class);
			SalidaProcesamiento out = logica(conn, in);
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		/******************************************************************************
		 * 							LOGICA
		 * @throws Exception 
		 ******************************************************************************/
		
		@SuppressWarnings("unlikely-arg-type")
		public SalidaProcesamiento logica(Connection conn, EntradaParametrosIndices entrada) throws Exception {
			
			SQL sql = new SQL();
			SalidaProcesamiento Salida = new  SalidaProcesamiento();
			Salida.P_OUT_ERRORESCONTADOR= 0L;	
			try {						
				
				    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.FechaSistema");

					if (entrada.p_Tarea.equals(TRANSACCION_INSERT))
					{	
					 try {	
						HashMap<String,Object> parametro = new HashMap<String,Object>();
						parametro.put("p_IdIndiceFinanciero", entrada.p_Registros.get(0).p_IdIndiceFinanciero);
						List<BuscaIndice> ValorRetorno = sql.query(conn, SQL_SELECT_EXISTE_INDICE_SISTEMA, parametro, BuscaIndice.class);
						Long ExisteIndiceSistema = ValorRetorno.get(0).Existe;
						if (ExisteIndiceSistema>0)
						{
							String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_EXISTE_INDICE_SISTEMA")
										.add("IdIndiceFinanciero",entrada.p_Registros.get(0).p_IdIndiceFinanciero)
										.msg();
							Salida.P_OUT_ERRORESCONTADOR= 1L;
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
							return Salida;
						}
						
						System.out.println("CREARA INDICE");
						sql.batch(conn, SQL_INSERT_CREA_INDICE, entrada.p_Registros, DatosIndice.class);
						System.out.println("FIN DEL CREAR");
					   }
						catch (Exception e)
						{
							System.out.println(e);
							Salida.P_OUT_ERRORESCONTADOR= 1L;							
							String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_PROCESO_EJECUTADO_ERROR_INDENTIFICAR_INDICE")									
									.msg();
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
							return Salida;
						}	
						
						
					}else if (entrada.p_Tarea.equals(TRANSACCION_UPDATE))						
					{
						System.out.println("MODIFICARA INDICE");
						sql.batch(conn, SQL_UPDATE_MODIFICA_INDICE, entrada.p_Registros, DatosIndice.class);	
						System.out.println("FIN DEL MODIFICAR");
						
					}else if (entrada.p_Tarea.equals(TRANSACCION_DELETE))
					{	
						HashMap<String,Object> parametro = new HashMap<String,Object>();
						parametro.put("p_IdIndiceFinanciero", entrada.p_Registros.get(0).p_IdIndiceFinanciero);
						List<BuscaIndice> ValorRetorno = sql.query(conn, SQL_SELECT_EXISTE_RELACION_MERCADO, parametro, BuscaIndice.class);
						Long ExisteIndiceRelacion = ValorRetorno.get(0).Existe;
						if (ExisteIndiceRelacion>0)
						{
						
							String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_EXISTE_INDICE_RELACIONADO")
										.add("IdIndiceFinanciero",entrada.p_Registros.get(0).p_IdIndiceFinanciero)
										.msg();
							Salida.P_OUT_ERRORESCONTADOR= 1L;
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
							return Salida;
						}else
						{
							System.out.println("ELIMINARA INDICE");
							sql.batch(conn, SQL_DELETE_ELIMINA_VALORES_INDICE, entrada.p_Registros, DatosIndice.class);
							sql.batch(conn, SQL_DELETE_ELIMINA_INDICE,  entrada.p_Registros, DatosIndice.class);
							System.out.println("FIN DEL ELIMINAR: ");
						}	
					}				
			}
			
			catch (Exception e)
			{
				System.out.println(e);
				Salida.P_OUT_ERRORESCONTADOR= 1L;
				if (entrada.p_Tarea.equals(TRANSACCION_INSERT))
				 {					
					String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_ERROR_INGRESAR_INDICE")							
							.msg();				
				    Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));
					e.printStackTrace();
				 }
				else if (entrada.p_Tarea.equals(TRANSACCION_UPDATE))
				 {
					String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_ERROR_MODIFICAR_INDICE")							
							.msg();				
				    Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));					
					e.printStackTrace();
				 }
				else if (entrada.p_Tarea.equals(TRANSACCION_DELETE))
				 {
					String msg=Diccionario.getMensaje(conn,entrada.p_IdEmpresa,"UI_ERROR_ELIMINAR_INDICE")							
							.msg();				
				    Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR",msg));					
					e.printStackTrace();
				 }
			}
			
			return Salida;
		}


		
		/******************************************************************************
		 * 							MAIN
		 ******************************************************************************/
		public static void main(String[] args){
			
			try (Connection con = PlataformaUtils.getDESAConnection())
			{
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
//ELIMIAR       String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Tarea\":\"E\",\"p_Registros\":[{\"p_IdIndiceFinanciero\":\"YYZ\"},{\"p_IdIndiceFinanciero\":\"ACB\"}]}";
				String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Tarea\":\"C\",\"p_Registros\":[{\"p_IdIndiceFinanciero\":\"ICP\",\"p_Nombre\":\"ICP\",\"p_Descripcion\":\"ICP\",\"p_IdMonedaParametro\":\"\",\"p_IdMonedaValor\":\"\",\"p_Periodo\":\"DI\",\"p_Tarea\":\"C\"}]}";
				EntradaParametrosIndices entrada = gson.fromJson(jsonUniverso, EntradaParametrosIndices.class);
				ProcesaIndicesAtributtionAnalisys demo = new ProcesaIndicesAtributtionAnalisys();				
				SalidaProcesamiento salida = demo.logica(con, entrada);				
				System.out.println("salida --> "+salida.toString());
				//con.commit();
				//--rollback();
				
			}catch(Exception e){
				e.printStackTrace(); 
			}
			
			System.out.println("== FIN PRUEBA LOCAL ==");
		}

}


