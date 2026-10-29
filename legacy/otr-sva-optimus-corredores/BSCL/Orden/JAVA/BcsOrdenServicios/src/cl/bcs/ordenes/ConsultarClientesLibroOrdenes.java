package cl.bcs.ordenes;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.math.BigDecimal;

import cl.bcs.ordenes.ConsultarOrdenes.Orden_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsultarClientesLibroOrdenes implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarClientesLibroOrdenes.class);
	//Declaraciones SQL 
	private final String SQL_SELECT_LIBRO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarClientesLibroOrdenes_SelectPorDocumentoIdentidad.sql");

	//Objeto Entrada Servicio
	public static class EntradaConsultarClientesLibroOrdenes {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo; 
		public String				p_UsaReglasNegocio;
		public List<Identidad> 			p_Identidades;
		
		public EntradaConsultarClientesLibroOrdenes() {
			this.p_Identidades 		= new ArrayList<Identidad>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//Objeto Salida Servicio
	public static class SalidaConsultarClientesLibroOrdenes {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ClienteLibroOrden> 	P_OUT_CLIENTESLIBRO;
		
		public SalidaConsultarClientesLibroOrdenes() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CLIENTESLIBRO = new ArrayList<ClienteLibroOrden>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//Objetos
	public static class Identidad {
		
		public String DocumentoIdentidad;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ClienteLibroOrden {
		public Long IdLibroOrdenes;
		public Long IdClient;
		public String IdEstado;
		public Long IdPersona;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String RazonSocial;
		public String NombreCompleto;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Proposito;
		public String IdEstadoPortafolio;
		public String DescripcionPortafolio;
		public Long IdPersonaBrokerExtranjero;
		public String NombreLibroOrdenes;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//Llamada
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarClientesLibroOrdenes jin = gson.fromJson(in.toString(), EntradaConsultarClientesLibroOrdenes.class);
		SalidaConsultarClientesLibroOrdenes jout = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(jout)).getAsJsonObject();
		return dataout;
	}
	//Logica
	public SalidaConsultarClientesLibroOrdenes logica(Connection con, EntradaConsultarClientesLibroOrdenes in) throws Exception {
		Long init = System.currentTimeMillis();
		Long startTime = 0L;
	
		logger.info("Inicio servicio ConsultarAsignacionesGrupo");
		SalidaConsultarClientesLibroOrdenes out = new SalidaConsultarClientesLibroOrdenes();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL _sql = new SQL();
		List<Object> param = new ArrayList<>();
		HashMap<String, Object> mapDocIden = new HashMap<String, Object>();
		try {
			for (Identidad obj: in.p_Identidades){
		    	param.add(obj.DocumentoIdentidad);
		    }
			mapDocIden.put("DocumentoIdentidad", param);
			logger.debug("Inicio SQL_SELECT_CLIENTES");
			startTime = System.nanoTime();
		    List<ClienteLibroOrden> clientesLibroOrden = _sql.query(con, SQL_SELECT_LIBRO_ORDENES, mapDocIden, ClienteLibroOrden.class);
		    logger.debug("clientesLibroOrden :");
		    logger.debug(clientesLibroOrden);
		    logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_LIBRO_ORDENES, COUNT: " +clientesLibroOrden.size());
		    logger.debug("INICIO - Ciclo agregar clientesLibroOrden");
			if(clientesLibroOrden != null && clientesLibroOrden.size() > 0) {
				for(ClienteLibroOrden orden : clientesLibroOrden) {
					out.P_OUT_CLIENTESLIBRO.add(orden);
				}
			}
			logger.debug("FIN - Ciclo agregar clientesLibroOrden");
			
		}catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			List<MensajeSalida> outFilter = new ArrayList<MensajeSalida>();
		    for(MensajeSalida   msj : out.P_OUT_MENSAJES) {
		    	if(msj.tipo.equals("ERROR")) {
		    		outFilter.add(msj);
		    	}
		    	
		    }
		    out.P_OUT_MENSAJES.clear();
		    out.P_OUT_MENSAJES.addAll(outFilter);
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		logger.info("Termino del servicio ConsultarAsignacionesGrupo, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	//Test
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//Datos ambiente DESA 15772424-K
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"ORD\",\"p_Identidades\":[{\"DocumentoIdentidad\":\"19080688-2\"},{\"DocumentoIdentidad\":\"78219990-0\"},{\"DocumentoIdentidad\":\"82148100-7\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"ORD\",\"p_Identidades\":[{\"DocumentoIdentidad\":\"15772424-K\"}]}";
			//Datos ambiente QA,
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"ORD\",\"p_Identidades\":[{\"DocumentoIdentidad\":\"11875775-0\"},{\"DocumentoIdentidad\":\"15345445-0\"}]}";

			EntradaConsultarClientesLibroOrdenes Objin = gson.fromJson(json, EntradaConsultarClientesLibroOrdenes.class);
			
			ConsultarClientesLibroOrdenes demo = new ConsultarClientesLibroOrdenes();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
	 			
 			try {
 				SalidaConsultarClientesLibroOrdenes ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println("SALIDA");
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
