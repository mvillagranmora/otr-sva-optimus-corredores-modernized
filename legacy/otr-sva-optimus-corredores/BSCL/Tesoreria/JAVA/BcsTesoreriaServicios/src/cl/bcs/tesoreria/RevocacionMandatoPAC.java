package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.fondomutuo.RevocacionContratoCSC;
import cl.bcs.fondomutuo.RevocacionContratoCSC.EntradaRevocacionContrato;
import cl.bcs.fondomutuo.RevocacionContratoCSC.InContrato;
import cl.bcs.fondomutuo.RevocacionContratoCSC.SalidaRevocacionContrato;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RevocacionMandatoPAC implements JavaCall {
	
	
	private final Log log = new Log("FM.RevocacionMandatoPAC");
	
	public String SQL_MANDATO_UPDATE = loadResourceToString("cl/bcs/tesoreria/sql/Mandato_Revocacion_UpdateMandatoPAC.sql");
	public String SQL_SELECT_CONTRATO = loadResourceToString("cl/bcs/tesoreria/sql/MandatoPAC_Revocacion_SelectContratos.sql");
	public String SQL_SELECT_CONTRATO_SAAPV = loadResourceToString("cl/bcs/tesoreria/sql/MandatoPAC_Revocacion_SelectContratosSAAPV.sql");

	public static class InMandato{
		public Long 	IdMandato;
		public String 	Estado="";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class UpdMandato{
		public Long 	IdMandato;
		public String 	Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class ContratoCSCRelacionado {
        public Long		IdMandatoPAC;
        public Long		IdFolioCSC;
        public String	FolioCSC;
        public String	Estado="";
        
        public String toString() {
                return Utils.printJson(this, false);
        }
	}
	
	//  ENTRADA
	public static class EntradaRevocacionMandato {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InMandato>			    p_Mandato;
		public  String						p_DesdeCSC;
		public  String						p_DesdeSAAPV;

		public EntradaRevocacionMandato(){
			p_Mandato 					= new ArrayList<InMandato>();
			p_DesdeCSC					= "N";
			p_DesdeSAAPV				= "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	// SALIDA

		public static class SalidaRevocacionMandato{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
			public List <ResultadoSolicitud> P_OUT_RESULTADO;
			
			public SalidaRevocacionMandato(){
				this.P_OUT_ERRORESCONTADOR=0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
				this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
		
		@Override
		public JsonObject call(Connection conn, JsonObject in) throws Exception {
			Gson gson = new Gson();		
			EntradaRevocacionMandato jin = gson.fromJson(in.toString(), EntradaRevocacionMandato.class);
			SalidaRevocacionMandato out = logica(conn,jin);
			
			JsonParser parser = new JsonParser();
	        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		
		
		public SalidaRevocacionMandato logica(Connection con, EntradaRevocacionMandato entrada) throws Exception{
			
			log.write("INICIO SalidaRevocacionMandato");
			SalidaRevocacionMandato out = new SalidaRevocacionMandato();
			SQL sql=new SQL();
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
					
			if(entrada.p_Mandato.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (InMandato mov: entrada.p_Mandato) {
				if(mov.IdMandato.longValue() == 0L){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
				if (correlativos.contains(mov.IdMandato)) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(mov.IdMandato);
				}
			}
			
			List<Long> idMandato = new ArrayList<Long>();
			for(InMandato in : entrada.p_Mandato){
				if(!idMandato.contains(in.IdMandato)) idMandato.add(in.IdMandato);
			}
			
			if (!entrada.p_DesdeCSC.equals("S")) {
                
                RevocacionContratoCSC revContrato = new RevocacionContratoCSC();
                EntradaRevocacionContrato revContratoIn = new EntradaRevocacionContrato();
                revContratoIn.p_IdEmpresa = entrada.p_IdEmpresa;
                revContratoIn.p_IdUsuario = entrada.p_IdUsuario;
                revContratoIn.p_IdModulo = "FMM";
                revContratoIn.p_DesdePac ="S";
                
                List<Long> IdMandatoPAC = entrada.p_Mandato.stream().map(m -> m.IdMandato).collect(Collectors.toList());  
                HashMap<String,Object> params = new HashMap<String,Object>();
                params.put("IdMandatoPAC", IdMandatoPAC);
                List<ContratoCSCRelacionado> contratosRelacionados = sql.query(con, SQL_SELECT_CONTRATO, params, RevocacionMandatoPAC.ContratoCSCRelacionado.class);
                
                for (InMandato mov: entrada.p_Mandato) {
                        ContratoCSCRelacionado contrato = contratosRelacionados.stream().filter(m -> Objects.equals(m.IdMandatoPAC, mov.IdMandato)).findFirst().orElse(null);
                    	if(contrato.IdFolioCSC != null) {
                    		InContrato revocarContrato = new InContrato();
                    		revocarContrato.IdContrato = contrato.IdFolioCSC;
                    		revocarContrato.Estado = contrato.Estado;
                            revContratoIn.p_Contrato.add(revocarContrato);
                    	}
                }
                
                if(revContratoIn.p_Contrato.size() != 0) {
                	SalidaRevocacionContrato revContratoOut =  revContrato.logica(con, revContratoIn);
                    
                    if(revContratoOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
                            out.P_OUT_ERRORESCONTADOR = revContratoOut.P_OUT_ERRORESCONTADOR;
                            out.P_OUT_MENSAJES = revContratoOut.P_OUT_MENSAJES ;
                            return out;
                    }
                    
                    for(MensajeSalida mensajeMandato : revContratoOut.P_OUT_MENSAJES) {
                    	out.P_OUT_MENSAJES.add(mensajeMandato);
                    }
                }
			}
			
			List<UpdMandato> actualizarMandato = new ArrayList<UpdMandato>();
			//Procesar traspasos
			for(Long procesar : correlativos ){
				UpdMandato reg =new UpdMandato();
				reg.IdMandato=procesar;
				actualizarMandato.add(reg);	
			}
			
			if(actualizarMandato.size() > 0){
				sql.batch(con, SQL_MANDATO_UPDATE, actualizarMandato, UpdMandato.class);
				for (UpdMandato salida : actualizarMandato ) {
                    HashMap<String,String> valores = new HashMap<String,String>();
                    valores.put("IdContrato",salida.IdMandato.toString());
                    MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"REVOCACION_MANDATO_PAC_EXITOSA",valores,con,entrada.p_IdEmpresa);
                    out.P_OUT_MENSAJES.add(msg);
				}
				
			}
			
			//Revocar SAAPV
			if (!entrada.p_DesdeSAAPV.equals("S")) {
				CambiarEstadoContratosSAAPV revContratoSAAPV = new CambiarEstadoContratosSAAPV();
				ContratosSAAPV_Entrada revContratoSAAPVIn = new ContratosSAAPV_Entrada();
				
				revContratoSAAPVIn.p_IdEmpresa = entrada.p_IdEmpresa;
				revContratoSAAPVIn.p_IdUsuario = entrada.p_IdUsuario;
				revContratoSAAPVIn.p_IdModulo = entrada.p_IdModulo;
				revContratoSAAPVIn.p_Opcion = "REV";
				revContratoSAAPVIn.p_DesdePac ="S";
			    
	            List<Long> IdMandatoPAC = entrada.p_Mandato.stream().map(m -> m.IdMandato).collect(Collectors.toList());  
	            HashMap<String,Object> params = new HashMap<String,Object>();
	            params.put("IdMandatoPAC", IdMandatoPAC);
	            List<ContratosSAAPV> contratosSAAPVRelacionados = sql.query(con, SQL_SELECT_CONTRATO_SAAPV, params, ContratosSAAPV.class);
	            
	            for (ContratosSAAPV mov: contratosSAAPVRelacionados) {
	            	if(mov.IdFolioContratoSAAPV != null) {
		    			ContratosSAAPV revocarContratoSAAPV = new ContratosSAAPV();
		        		revocarContratoSAAPV.IdFolioContratoSAAPV = mov.IdFolioContratoSAAPV;
		        		revocarContratoSAAPV.IdEstado = "REV"; 
		        		revocarContratoSAAPV.IdPersona = mov.IdPersona;
		        		revocarContratoSAAPV.IdCliente = mov.IdCliente;
		        		revocarContratoSAAPV.IdEstadoSolicitud = "REV";
		
		        		revContratoSAAPVIn.p_ContratosSAAPV.add(revocarContratoSAAPV);
	            	}
	            }

				if(revContratoSAAPVIn.p_ContratosSAAPV.size() != 0) {
					ContratosSAAPV_Salida revContratoSAAPVOut = revContratoSAAPV.logica(con, revContratoSAAPVIn);
	                
	                if(revContratoSAAPVOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
	                	out.P_OUT_ERRORESCONTADOR = revContratoSAAPVOut.P_OUT_ERRORESCONTADOR;
	                	out.P_OUT_MENSAJES = revContratoSAAPVOut.P_OUT_MENSAJES ;
	                    return out;
	                }
	
	                for(MensajeSalida mensajeMandato : revContratoSAAPVOut.P_OUT_MENSAJES) {
	                	out.P_OUT_MENSAJES.add(mensajeMandato);
	                }	                	
                }
			}
			
			return out;
		}
		/********************
		 * Metodo Principal *
		 ********************/

		public static void main(String[] args) throws Exception {
			try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) {
				try {
					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
					
					// 1 SAAPV
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"TES\",\"p_Mandato\":[{\"IdMandato\":176744}]}"; //167351
					// 2 CSC
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"TES\",\"p_Mandato\":[{\"IdMandato\":177079},{\"IdMandato\":176021}]}";
					// 2 SAAPV
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"TES\",\"p_Mandato\":[{\"IdMandato\":176744},{\"IdMandato\":167351}]}";
					
					EntradaRevocacionMandato Objin_json = gson.fromJson(json, EntradaRevocacionMandato.class);
					
					RevocacionMandatoPAC demo = new RevocacionMandatoPAC();	
					
		 			System.out.println("ENTRADA");
		 			System.out.println(Objin_json.toString());
		 			System.out.println("SALIDA");

		 			try { 
		 				SalidaRevocacionMandato ObjOut = demo.logica(con, Objin_json);
		 				//con.commit();
		 				con.rollback();
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
