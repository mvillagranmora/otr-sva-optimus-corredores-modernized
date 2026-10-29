package cl.bcs.fondomutuo;

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


import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnulacionMandatoPAC;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnulacionContratoCSC implements JavaCall {
	
	
	private final Log log = new Log("FM.AnulacionContratoCSC");
	
	public String SQL_CONTRATO_UPDATE = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Anulacion_UpdateContratoCSC.sql");
	public String SQL_SELECT_MANDATOS = loadResourceToString("cl/bcs/fondomutuo/sql/ContratoCSC_Anulacion_SelectMandatos.sql");
	
	public static class InContrato{
		public Long 	IdContrato;
		public String 	Estado="";
		
		
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
	
	public static class MandatoPACRelacionado {
        public Long		IdMandatoPAC;
        public Long		IdFolioCSC;
        public String	Estado="";

        public String toString() {
                return Utils.printJson(this, false);
        }
	};
	
	//  ENTRADA
	public static class EntradaAnulacionContrato {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_Contrato;
		public  String                      p_DesdePac;
		
		
		public EntradaAnulacionContrato(){
			p_Contrato 					= new ArrayList<InContrato>();
			p_DesdePac                  = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA
		public static class SalidaAnulacionContrato{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
			public List <ResultadoSolicitud> P_OUT_RESULTADO;
			
			public SalidaAnulacionContrato(){
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
			EntradaAnulacionContrato jin = gson.fromJson(in.toString(), EntradaAnulacionContrato.class);
			SalidaAnulacionContrato out = logica(conn,jin);
			
			JsonParser parser = new JsonParser();
	        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		
		
		public SalidaAnulacionContrato logica(Connection con, EntradaAnulacionContrato entrada) throws Exception{
			
			log.write("INICIO SalidaAnulacionContrato");
			SalidaAnulacionContrato out = new SalidaAnulacionContrato();
			SQL sql=new SQL();
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			
			
			if(entrada.p_Contrato.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (InContrato mov: entrada.p_Contrato) {
				
				if(mov.IdContrato.longValue() == 0L){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				}
				if (correlativos.contains(mov.IdContrato)) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					return out;
				} else {
					correlativos.add(mov.IdContrato);
				}
			}
			
			List<Long> idContrato = new ArrayList<Long>();
			for(InContrato in : entrada.p_Contrato){
				if(!idContrato.contains(in.IdContrato)) idContrato.add(in.IdContrato);
			}
			
			if(!entrada.p_DesdePac.equals("S")) {
                AnulacionMandatoPAC anuMandato = new AnulacionMandatoPAC();
                AnulacionMandatoPAC.EntradaAnulacionContrato anuMandatoIn = new  AnulacionMandatoPAC.EntradaAnulacionContrato();
                anuMandatoIn.p_IdEmpresa = entrada.p_IdEmpresa;
                anuMandatoIn.p_IdUsuario = entrada.p_IdUsuario;
                anuMandatoIn.p_IdModulo = "TES";
                anuMandatoIn.p_DesdeCSC ="S";
                
                List<Long> IdFoliosCSC = entrada.p_Contrato.stream().map(m -> m.IdContrato).collect(Collectors.toList());
                
                HashMap<String,Object> params = new HashMap<String,Object>();
                params.put("FolioCSC", IdFoliosCSC);

                List<MandatoPACRelacionado> mandatosRelacionados = sql.query(con, SQL_SELECT_MANDATOS, params, AnulacionContratoCSC.MandatoPACRelacionado.class);
                               

                for (InContrato mov : entrada.p_Contrato) {
                    
                	MandatoPACRelacionado mandato = mandatosRelacionados.stream().filter(m -> Objects.equals(m.IdFolioCSC, mov.IdContrato)).findFirst().orElse(null);
                	
                	if(mandato != null) { 
                        AnulacionMandatoPAC.InContrato AnularMandato = new AnulacionMandatoPAC.InContrato();
                    	AnularMandato.IdContrato = mandato.IdMandatoPAC;
                    	AnularMandato.Estado = mandato.Estado;
                        anuMandatoIn.p_Contrato.add(AnularMandato);        
                	}
                }
                
                if(anuMandatoIn.p_Contrato.size() != 0) {
                	AnulacionMandatoPAC.SalidaAnulacionContrato anuMandatoOut = anuMandato.logica(con, anuMandatoIn);
                    
                    if(anuMandatoOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
                            out.P_OUT_ERRORESCONTADOR = anuMandatoOut.P_OUT_ERRORESCONTADOR;
                            out.P_OUT_MENSAJES = anuMandatoOut.P_OUT_MENSAJES ;
                            return out;
                    }
                   
                    for(MensajeSalida mensajeMandato : anuMandatoOut.P_OUT_MENSAJES) {
                    	out.P_OUT_MENSAJES.add(mensajeMandato);
                    }
                }
                          
			}
			
			List<UpdContrato> actualizarContrato = new ArrayList<UpdContrato>();
			//Procesar traspasos
			for(Long procesar : correlativos ){
				UpdContrato reg =new UpdContrato();
				reg.IdContrato=procesar;
				actualizarContrato.add(reg);	
			}
			
			if(actualizarContrato.size() > 0){
				sql.batch(con, SQL_CONTRATO_UPDATE, actualizarContrato, UpdContrato.class);
				for (UpdContrato salida : actualizarContrato ) {
                    HashMap<String,String> valores = new HashMap<String,String>();
                    valores.put("IdContrato",salida.IdContrato.toString());
                    MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"ANULACION_CONTRATO_CSC_EXITOSA",valores,con,entrada.p_IdEmpresa);
                    out.P_OUT_MENSAJES.add(msg);
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
					
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":1796708}]}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":1787714},{\"IdContrato\":1791711},{\"IdContrato\":1560583}]}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":1796708},{\"IdContrato\":1791711}]}";
					
					EntradaAnulacionContrato Objin_json = gson.fromJson(json, EntradaAnulacionContrato.class);
					
					AnulacionContratoCSC demo = new AnulacionContratoCSC();	
					
		 			System.out.println("ENTRADA");
		 			System.out.println(Objin_json.toString());
		 			System.out.println("SALIDA");

		 			try { 
		 				SalidaAnulacionContrato ObjOut = demo.logica(con, Objin_json);
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
