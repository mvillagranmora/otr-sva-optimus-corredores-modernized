package cl.bcs.ordenes;

import java.sql.Connection;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

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

public class ConsultarInformacionPortafolioOrdenes implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarInformacionPortafolioOrdenes.class);

	private final String SQL_SELECT_PORTAFOLIO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarInformacionPortafolioOrdenes_SelectPorIdPortafolio.sql");

	//Objeto Entrada Servicio
		public static class EntradaConsultarInformacionPortafolioOrdenes {
			public Long 				p_IdEmpresa;
			public Long 				p_IdUsuario;
			public String 				p_IdModulo; 
			public List<Long> 			p_Portafolios;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		//Objeto Salida Servicio
		public static class SalidaConsultarInformacionPortafolioOrdenes {		
			public List<MensajeSalida>		P_OUT_MENSAJES;
			public Long						P_OUT_ERRORESCONTADOR;
			public List<PortafolioOrdenes> 	P_OUT_PORTAFOLIOSORDENES;
			
			public SalidaConsultarInformacionPortafolioOrdenes() {
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
				this.P_OUT_PORTAFOLIOSORDENES = new ArrayList<PortafolioOrdenes>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		public static class PortafolioOrdenes {
			public Long IdPortafolio;
	        public String IdDireccionNotificacion;
	        public String OperaCustodiaRV;
	        public String OperaCustodiaRFIF;
	        public String PermiteVenderNominalRV;
	        public String PermiteVenderNominalRFIF;
	        public String OperaCuentaInversion;
	        public String OperaLineaSobregiro;
	        public String MecanismoCaptacion;
	        public Integer IdLibroOrdenes;
	        public String EsClienteFinal;
	        public Long IdPersonaBrokerExtranjero;
	        public String CodigoIfrs;
	        
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		//Llamada
		@Override
		public JsonObject call(Connection conn, JsonObject in) throws Exception {
			Gson gson = Utils.getGsonBuilder();
			EntradaConsultarInformacionPortafolioOrdenes jin = gson.fromJson(in.toString(), EntradaConsultarInformacionPortafolioOrdenes.class);
			SalidaConsultarInformacionPortafolioOrdenes jout = logica(conn,jin);
			JsonParser parser = new JsonParser();
	        JsonObject dataout	= parser.parse(gson.toJson(jout)).getAsJsonObject();
			return dataout;
		}
		//Logica
		public SalidaConsultarInformacionPortafolioOrdenes logica(Connection con, EntradaConsultarInformacionPortafolioOrdenes in) throws Exception {
			Long init = System.currentTimeMillis();
			Long startTime = 0L;
		
			logger.info("Inicio servicio ConsultarInformacionPortafolioOrdenes");
			SalidaConsultarInformacionPortafolioOrdenes out = new SalidaConsultarInformacionPortafolioOrdenes();
			out.P_OUT_ERRORESCONTADOR = 0L;
			SQL _sql = new SQL();
			HashMap<String, Object> mapIdPortafolio = new HashMap<String, Object>();
			try {
				mapIdPortafolio.put("IdPortafolio", in.p_Portafolios);
				logger.debug("Inicio SQL_SELECT_PORTAFOLIO_ORDENES");
				startTime = System.nanoTime();
			    List<PortafolioOrdenes> portafoliosOrdenes = _sql.query(con, SQL_SELECT_PORTAFOLIO_ORDENES, mapIdPortafolio, PortafolioOrdenes.class);
			    logger.debug("portafoliosOrdenes :");
			    logger.debug(portafoliosOrdenes);
			    logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_PORTAFOLIO_ORDENES, COUNT: " +portafoliosOrdenes.size());
			    logger.debug("INICIO - Ciclo agregar portafoliosOrdenes");
				if(portafoliosOrdenes != null && portafoliosOrdenes.size() > 0) {
					for(PortafolioOrdenes portafolio : portafoliosOrdenes) {
						out.P_OUT_PORTAFOLIOSORDENES.add(portafolio);
					}
				}
				logger.debug("FIN - Ciclo agregar portafoliosOrdenes");
				
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
			logger.info("Termino del servicio ConsultarInformacionPortafolioOrdenes, tiempo:" + (System.currentTimeMillis() - init));
			return out;
		}
		//Test
		public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = Utils.getGsonBuilder();
				//Datos ambiente DESA
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"ORD\",\"p_Portafolios\":[1450146,1451213,1451210]}";
				//Datos ambiente QA,
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"ORD\",\"p_Portafolios\":[{\"IdPortafolio\":100994},{\"IdPortafolio\":100836},{\"IdPortafolio\":100550}]}";

				EntradaConsultarInformacionPortafolioOrdenes Objin = gson.fromJson(json, EntradaConsultarInformacionPortafolioOrdenes.class);
				
				ConsultarInformacionPortafolioOrdenes demo = new ConsultarInformacionPortafolioOrdenes();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			
		 			
	 			try {
	 				SalidaConsultarInformacionPortafolioOrdenes ObjOut = demo.logica(con, Objin);
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
