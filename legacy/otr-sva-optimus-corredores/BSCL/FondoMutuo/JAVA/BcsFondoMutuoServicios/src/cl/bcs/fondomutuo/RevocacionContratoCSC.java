package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.RevocacionMandatoPAC;
import cl.bcs.tesoreria.RevocacionMandatoPAC.EntradaRevocacionMandato;
import cl.bcs.tesoreria.RevocacionMandatoPAC.InMandato;
import cl.bcs.tesoreria.RevocacionMandatoPAC.SalidaRevocacionMandato;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RevocacionContratoCSC implements JavaCall {
	private final Log log = new Log("FM.RevocacionContratoCSC");
	
	public String SQL_CONTRATO_UPDATE = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_UpdateContratoCSC.sql");
	public String SQL_CONTRATO_UPDATE_WS = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_UpdateContratoCSCWS.sql");
	public String SQL_SELECT_VALIDACIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_SelectValidaciones.sql");
	public String SQL_SELECT_MANDATOS = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Revocacion_SelectMandatos.sql");
	
	public static class InContrato{
		public Long 	IdContrato;
		public String 	Estado="";
		public String   FolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class UpdContrato{
		public Long 	IdContrato;
		public String 	Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public static class UpdContratoWS{
		public String 	FolioCSC;
		public String 	Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ResultadoValidaciones{
		
		public Long IdFolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MandatoPACRelacionado {
        public Long		IdMandatoPAC;
        public Long		IdFolioCSC;
        public String	FolioCSC;
        public String	Estado="";
        
        
        public String toString() {
                return Utils.printJson(this, false);
        }
	} 
	
	//  ENTRADA
	public static class EntradaRevocacionContrato {		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_Contrato;	
		public  String						p_DesdePac;
		
		public EntradaRevocacionContrato(){
			p_Contrato 					= new ArrayList<InContrato>();
			p_DesdePac					= "N";
		}	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA
	public static class SalidaRevocacionContrato{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoRevocacionCSC> P_OUT_RESULTADO;
			
		public SalidaRevocacionContrato(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoRevocacionCSC>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoRevocacionCSC{
		public Long IdFolioCSC;
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaRevocacionContrato jin = gson.fromJson(in.toString(), EntradaRevocacionContrato.class);
		SalidaRevocacionContrato out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
		
	public SalidaRevocacionContrato logica(Connection con, EntradaRevocacionContrato entrada) throws Exception{		
		log.write("INICIO SalidaRevocacionContrato");
		ResultadoRevocacionCSC resultado = new ResultadoRevocacionCSC();
		SalidaRevocacionContrato out = new SalidaRevocacionContrato();
		try {
			SQL sql=new SQL();
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES = salidaBasicos.p_out_Mensajes;
				return out;
			}
						
			if(entrada.p_Contrato.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MODIFICACIONES_DE_CONTRATO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			
			for (InContrato mov: entrada.p_Contrato) {				
				if (mov.IdContrato != null) correlativos.add(mov.IdContrato);									
			}
			
			List<Long> idContrato = new ArrayList<Long>();
			for(InContrato in : entrada.p_Contrato){
				if(!idContrato.contains(in.IdContrato)) idContrato.add(in.IdContrato);
			}
							
			List<UpdContratoWS> actualizarContratoWS = new ArrayList<UpdContratoWS>();
			List<UpdContrato> actualizarContrato = new ArrayList<UpdContrato>();
			
			Set<Long> agregadosActualizarContrato = new HashSet<>();
			
			for (InContrato mov: entrada.p_Contrato) {					
				HashMap<String,Object> params_ValidacionesContrato = new HashMap<String,Object>();
				params_ValidacionesContrato.put("FolioCSC", mov.FolioCSC);
				List<ResultadoValidaciones> contratosExistentesValidaciones = sql.query(con, SQL_SELECT_VALIDACIONES, params_ValidacionesContrato, ResultadoValidaciones.class);
				HashMap<Long,Object> hcontratosExistentesValidaciones = Utils.List2Hash(contratosExistentesValidaciones, "IdFolioCSC");
							
				if (mov.FolioCSC != null) {				
					if (hcontratosExistentesValidaciones.size() == 0) {
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRATO_NO_VALIDO",new HashMap<String,String>(), con, entrada.p_IdEmpresa));
						continue;						
					}
				UpdContratoWS contratoSuscripcionCuotasWS = new UpdContratoWS();
				contratoSuscripcionCuotasWS.FolioCSC = mov.FolioCSC;
				actualizarContratoWS.add(contratoSuscripcionCuotasWS);
				resultado.IdFolioCSC = contratosExistentesValidaciones.get(0).IdFolioCSC;
				out.P_OUT_RESULTADO.add(resultado);
				} else {
					for(Long procesar : correlativos ){
						if (agregadosActualizarContrato.add(procesar)) {  
			                UpdContrato reg = new UpdContrato();
			                reg.IdContrato = procesar;
			                actualizarContrato.add(reg);
			            }
					}
				}
			}
			
			SalidaRevocacionMandato revMandatoOut =  new SalidaRevocacionMandato();
			if(actualizarContratoWS.size() >0 || actualizarContrato.size() > 0 ) {
				if(!entrada.p_DesdePac.equals("S")) {
	                RevocacionMandatoPAC revMandato = new RevocacionMandatoPAC();
                    EntradaRevocacionMandato revMandatoIn = new EntradaRevocacionMandato();
                    revMandatoIn.p_IdEmpresa = entrada.p_IdEmpresa;
                    revMandatoIn.p_IdUsuario = entrada.p_IdUsuario;
                    revMandatoIn.p_IdModulo = "TES";
                    revMandatoIn.p_DesdeCSC ="S";
                    
	                List<Long> IdFolioCSC = entrada.p_Contrato.stream().map(m -> m.IdContrato).collect(Collectors.toList());
	                List<String> FolioCSC = entrada.p_Contrato.stream().map(m -> m.FolioCSC).collect(Collectors.toList());
	                
	                HashMap<String,Object> params = new HashMap<String,Object>();
	                params.put("IdFolioCSC", IdFolioCSC);
	                params.put("FolioCSC", FolioCSC);
	
	                List<MandatoPACRelacionado> mandatosRelacionados = sql.query(con, SQL_SELECT_MANDATOS, params, RevocacionContratoCSC.MandatoPACRelacionado.class);

	                for (InContrato mov : entrada.p_Contrato) {
	                	MandatoPACRelacionado mandato = null;
	                	if(mov.FolioCSC != null) {
	                		mandato = mandatosRelacionados.stream().filter(m -> Objects.equals(m.FolioCSC, mov.FolioCSC)).findFirst().orElse(null);
	                	}
	                	if(mov.IdContrato != null) {
	                		mandato = mandatosRelacionados.stream().filter(m -> Objects.equals(m.IdFolioCSC, mov.IdContrato)).findFirst().orElse(null);
	                	}	
	                	if(mandato != null) {
	                		InMandato revocarMandato = new InMandato();
	                		revocarMandato.IdMandato = mandato.IdMandatoPAC;
	                		revocarMandato.Estado = mandato.Estado;
	                		revMandatoIn.p_Mandato.add(revocarMandato);
	                	}
	                }
	                
	                if(revMandatoIn.p_Mandato.size() != 0) {
	                	revMandatoOut =  revMandato.logica(con, revMandatoIn);
    	                
		                if(revMandatoOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
	                        out.P_OUT_ERRORESCONTADOR = revMandatoOut.P_OUT_ERRORESCONTADOR;
	                        out.P_OUT_MENSAJES = revMandatoOut.P_OUT_MENSAJES ;
	                        return out;
		                }
		               
		                
		                for(MensajeSalida mensajeMandato : revMandatoOut.P_OUT_MENSAJES) {
		                	out.P_OUT_MENSAJES.add(mensajeMandato);
		                }	                	
	                }           
				}
			}
			
			if (actualizarContratoWS.size() >0) {
				sql.batch(con, SQL_CONTRATO_UPDATE_WS, actualizarContratoWS, UpdContratoWS.class);
				
				for (UpdContratoWS salida : actualizarContratoWS){
					HashMap<String,String> valores = new HashMap<String,String>();
                    valores.put("IdContrato",salida.FolioCSC.toString());
					MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"REVOCACION_CONTRATO_CSC_EXITOSA",valores,con,entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(msg);
				}
				
			}
			if(actualizarContrato.size() > 0){
				sql.batch(con, SQL_CONTRATO_UPDATE, actualizarContrato, UpdContrato.class);
				
				for (UpdContrato salida : actualizarContrato){
					HashMap<String,String> valores = new HashMap<String,String>();
                    valores.put("IdContrato",salida.IdContrato.toString());
					MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"REVOCACION_CONTRATO_CSC_EXITOSA",valores,con,entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(msg);
				}
			}
			
			return out;
			
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}
	}
		
	/********************
	 * Metodo Principal *
	 ********************/
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
					//String json4="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"FolioCSC\":W220684},{\"FolioCSC\":W220662}]}";
					//String json4="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":1787714},{\"IdContrato\":1560583}]}";
					//String json4="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"FolioCSC\":W217039},{\"IdContrato\":1560583}]}";
					String json4= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":1796708},{\"IdContrato\":1791711}]}"; 
					//String json4="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"FolioCSC\":W221341},{\"IdContrato\":1796708}]}";
					EntradaRevocacionContrato Objin_json = gson.fromJson(json4, EntradaRevocacionContrato.class);					
					RevocacionContratoCSC demo = new RevocacionContratoCSC();						
		 			try { 
		 				SalidaRevocacionContrato ObjOut = demo.logica(con, Objin_json);
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
