package cl.bcs.socionegocio.cli;

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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.AnotacionCliente;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarAnotacionesCliente implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarAnotacionesCliente.class);
	
	private final String SQL_SELECT_EXISTE_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarAnotacionesCliente_SelectExisteCliente.sql");
	private final String SQL_SELECT_ANOTACIONES_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarAnotacionesCliente_SelectAnotacionesCliente.sql");
	
	public static class EntradaConsultarAnotacionesCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Cliente> p_Clientes;
		
		public EntradaConsultarAnotacionesCliente () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "SOC";
			this.p_UsaReglasNegocio = "N";
			this.p_Clientes = new ArrayList<Cliente>();
		}
	}
	
	public static class Cliente extends BcsPojo {
		public Long IdCliente;
		
		public Cliente () {
			this.IdCliente = 0L;
		}
		public Cliente(Long idCliente) {
			this.IdCliente = idCliente;
		}
	}
	public static class SalidaConsultarAnotacionesCliente extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<AnotacionCliente> P_OUT_RESULTADO;
		
		public SalidaConsultarAnotacionesCliente() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<AnotacionCliente>();
		}
	}
	
	public SalidaConsultarAnotacionesCliente logica(Connection con, EntradaConsultarAnotacionesCliente entrada) throws Exception {
		logger.info("INICIO::ConsultarAnotacionesCliente");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarAnotacionesCliente salida = new SalidaConsultarAnotacionesCliente();
		
		if(entrada.p_Clientes.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENTRADA_SIN_CLIENTE", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(1);
        params.put("IdCliente", entrada.p_Clientes.get(0).IdCliente);
        
        List<Cliente> clienteExiste = sql.query(con, SQL_SELECT_EXISTE_CLIENTE, params, Cliente.class);
		
        if(clienteExiste.isEmpty()) {
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CLIENTE_NO_EXISTE", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
        }
        
        List<AnotacionCliente> anotaciones = sql.query(con, SQL_SELECT_ANOTACIONES_CLIENTE, params, AnotacionCliente.class);
        
        salida.P_OUT_RESULTADO.addAll(anotaciones);
        long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionesCliente", fin - inicio);
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarAnotacionesCliente jin=gson.fromJson(in.toString() ,EntradaConsultarAnotacionesCliente.class);
		SalidaConsultarAnotacionesCliente out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
																				//4	
//		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_ClienteCodigoPortafolio\":[{ \"DocumentoIdentidad\": \"10111045-1\",\"CodigoPortafolio\": \"0\"}]}";
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_Clientes\":[{ \"IdCliente\": 1252}]}";
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_ClienteCodigoPortafolio\":[]}";
		
		EntradaConsultarAnotacionesCliente Objin = gson.fromJson(json, EntradaConsultarAnotacionesCliente.class);																
		ConsultarAnotacionesCliente  demo= new ConsultarAnotacionesCliente();				
		try {
			SalidaConsultarAnotacionesCliente ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
		}
		System.out.println("== END ==");

	}

}
