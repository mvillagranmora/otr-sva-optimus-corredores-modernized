package cl.bcs.normativa.cartola;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarGrupoPortaforliosCartola implements JavaCall {
	
	/* IdServicio: 700.015	
	 *  This class is invoked from the file ConsultaController.js in order to identify 
	 * 	all the IdPortafolios by IdCliente (if IdPortafolio = 0)  and
	 * 	all the IdClientes and IdPortafolios by IdGrupo (if the IdGrupo > 0). 
	 */
	
	private final Logger logger = LogManager.getLogger(EntradaParametrosGrupoPortafolio.class);
	
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/normativa/cartola/sql/ConsultarGrupoPortaforliosCartola_SelectPortafolios.sql");
	private final String SQL_SELECT_GRUPO_PORTAFOLIOS = loadResourceToString("cl/bcs/normativa/cartola/sql/ConsultarGrupoPortaforliosCartola_SelectGrupoPortafolios.sql");
	
    public static class EntradaParametrosGrupoPortafolio{
    	public Long p_IdEmpresa;
    	public Long p_IdCliente;
     	public Long p_IdPortafolio;
     	public Long p_IdGrupo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	public static class DatosCliente{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    public static class SalidaProcesamiento{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <DatosCliente>  P_OUT_PORTAFOLIOS;
	
		public SalidaProcesamiento(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaParametrosGrupoPortafolio in = gson.fromJson(jin.toString(), EntradaParametrosGrupoPortafolio.class);
		
		SalidaProcesamiento out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	private SalidaProcesamiento logica(Connection conn, EntradaParametrosGrupoPortafolio entrada) throws Exception {
		logger.info("INICIO DEL PROCESO...");
		SalidaProcesamiento salida = new  SalidaProcesamiento();
		salida.P_OUT_ERRORESCONTADOR= 0L;
		try {
			List<EntradaParametrosGrupoPortafolio> paramGrupoPortafolio = new ArrayList<EntradaParametrosGrupoPortafolio>();					
			paramGrupoPortafolio.add(entrada);	// Get all the parameters			
			
			// Parameters validation
			if (paramGrupoPortafolio.get(0).p_IdEmpresa == null) {
				// IdEmpresa : null ->	Show the message
				salida.P_OUT_ERRORESCONTADOR= 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"EMPRESA_VACIA", "Ingrese el Id de la empresa"));
				
			} else if (paramGrupoPortafolio.get(0).p_IdCliente == null) {	
				// IdCliente : null ->	Show the message
				salida.P_OUT_ERRORESCONTADOR= 1L;
				String msg= Diccionario.getMensaje(conn, paramGrupoPortafolio.get(0).p_IdEmpresa,"CLIENTE_VACIO")
						.msg();				
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"CLIENTE_VACIO", msg));
				
			} else if (paramGrupoPortafolio.get(0).p_IdPortafolio == null)	{
				// IdPortafolio : null ->	Show the message
				salida.P_OUT_ERRORESCONTADOR= 1L;
				String msg= Diccionario.getMensaje(conn, paramGrupoPortafolio.get(0).p_IdEmpresa,"UI_DEBE_SELECCIONAR_PORTAFOLIO")
						.msg();						
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_DEBE_SELECCIONAR_PORTAFOLIO", msg));
				
			} 
			else {
				SQL sql = new SQL();
				HashMap<String,Object> hparam = new HashMap<String,Object>();
				// if p_IdGrupo > 0 then get all the IdPortafolios and IdClientes by IdGrupo
				if (paramGrupoPortafolio.get(0).p_IdGrupo > 0) {
					hparam.put("p_IdGrupo", entrada.p_IdGrupo);
					salida.P_OUT_PORTAFOLIOS = sql.query(conn, SQL_SELECT_GRUPO_PORTAFOLIOS, hparam, DatosCliente.class);
					
				} 
				// if p_IdPortafolio = 0 then get all the IdPortafolios by IdCliente
				else if (paramGrupoPortafolio.get(0).p_IdPortafolio == 0) {
					hparam.put("p_IdCliente", entrada.p_IdCliente);
					salida.P_OUT_PORTAFOLIOS = sql.query(conn, SQL_SELECT_PORTAFOLIOS, hparam, DatosCliente.class);
				}
			}
		}
				
		catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR= 1L;
			String msg= Diccionario.getMensaje(conn, entrada.p_IdEmpresa,"UI_PROCESO_EJECUTADO_ERROR")
			.msg();		
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", msg));
			e.printStackTrace();
		}
		logger.info("PROCESO CULIMINADO");
		return salida;
	}
	
	/******************************************************************************
	 * 							MAIN - Only for testing use
	 ******************************************************************************/
	public static void main(String[] args){
		
		try (Connection conn = PlataformaUtils.getDESAConnection())
		{
			System.out.println("== INICIO PRUEBA LOCAL ==");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdCliente\":524891,\"p_IdGrupo\":17,\"p_IdPortafolio\":0}";
			String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdClienteCartola\":1421445,\"p_IdCliente\":1421445,\"p_IdGrupo\":17,\"p_IdPortafolio\":0}";
			EntradaParametrosGrupoPortafolio entrada = gson.fromJson(jsonUniverso, EntradaParametrosGrupoPortafolio.class);
			ConsultarGrupoPortaforliosCartola demo = new ConsultarGrupoPortaforliosCartola();				
			SalidaProcesamiento salida = demo.logica(conn, entrada);
			
			System.out.println("Salida --> " + salida.toString());
			
		}catch(Exception e){ 
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}

