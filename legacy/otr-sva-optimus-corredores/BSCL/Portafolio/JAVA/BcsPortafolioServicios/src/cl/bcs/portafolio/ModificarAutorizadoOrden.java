package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.ModificaPreferenciaPagoPortafolio.ModificaPreferenciaPagoPortafolio_Entrada;
import cl.bcs.portafolio.ModificaPreferenciaPagoPortafolio.ModificaPreferenciaPagoPortafolio_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarAutorizadoOrden implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarAutorizadoOrden.class);
	
	private final String SQL_ACTUALIZAR_AUTORIZACION_ORDEN = loadResourceToString("cl/bcs/portafolio/sql/ModificarAutorizadoOrden_ActualizarAutorizacionOrden.sql");
	
	public static class EntradaModificarAutorizadoOrden{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ModificaAutorizados> p_ListaAutorizados;
		
		public EntradaModificarAutorizadoOrden() {
			this.p_ListaAutorizados = new ArrayList<ModificaAutorizados>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificaAutorizados{
		public Long IdAutorizado;
		public Boolean FirmaOptimusCliente;
		public String IdTipoAutorizado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String FirmaOptimusClienteSN;

	    public Boolean getFirmaOptimusCliente() {
	        return FirmaOptimusCliente;
	    }
	    
	    public void setFirmaOptimusCliente(Boolean FirmaOptimusCliente) {
	        this.FirmaOptimusCliente = FirmaOptimusCliente;
	    }

	    public String getFirmaOptimusClienteSN() {
	        return FirmaOptimusClienteSN;
	    }

	    public void setFirmaOptimusClienteSN(String FirmaOptimusClienteSN) {
	        this.FirmaOptimusClienteSN = FirmaOptimusClienteSN;
	    }
	}
	
	public static class SalidaModificarAutorizadoOrden{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarAutorizadoOrden() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarAutorizadoOrden jin = gson.fromJson(in.toString(), EntradaModificarAutorizadoOrden.class);
		SalidaModificarAutorizadoOrden out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	public SalidaModificarAutorizadoOrden logica(Connection con, EntradaModificarAutorizadoOrden entrada) throws Exception {
		logger.info("INICIO::ModificarAutorizadoOrden");
		SalidaModificarAutorizadoOrden out = new SalidaModificarAutorizadoOrden();
		
		SQL sql=new SQL();
		
		for (ModificaAutorizados a : entrada.p_ListaAutorizados) {

		    if (a.getFirmaOptimusCliente() == null) {
		        a.setFirmaOptimusClienteSN("N");
		    } else {
		        a.setFirmaOptimusClienteSN(
		            a.getFirmaOptimusCliente() ? "S" : "N"
		        );
		    }
		}
		
		sql.batch(con, SQL_ACTUALIZAR_AUTORIZACION_ORDEN, entrada.p_ListaAutorizados , ModificaAutorizados.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CREAR_AUTORIZADO_ORDEN_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		
		logger.info("FIN::ModificarAutorizadoOrden");
		return out;
	}
	/**************************************************
	 * 					MAIN 
	 * ************************************************/
	public static void main(String[] args) { 
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String jsonCL = "{\r\n" + 
					"	\"p_IdEmpresa\": 1,\r\n" + 
					"	\"p_IdUsuario\": 97,\r\n" + 
					"	\"p_IdModulo\": \"PLA\",\r\n" + 
					"	\"p_ListaAutorizados\": [\r\n" + 
					"	{\r\n" + 
					"		\"IdAutorizado\": 11435,\r\n" + 
					"		\"IdTipoAutorizado\": \"ICLIENTE-OPERAR\",\r\n" + 
					"		\"FirmaOptimusCliente\": true\r\n" + 
					"		}\r\n" + 
					"	]\r\n" + 
					"}"; 
					
			
			EntradaModificarAutorizadoOrden Objin = gson.fromJson(jsonCL, EntradaModificarAutorizadoOrden.class);

			ModificarAutorizadoOrden demo = new ModificarAutorizadoOrden();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaModificarAutorizadoOrden ObjOut = demo.logica(con, Objin);
				//con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

} 
