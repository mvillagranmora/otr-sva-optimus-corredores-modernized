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
import cl.bcs.fondomutuo.AnulacionContratoCSC;
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


public class AnulacionMandatoPAC implements JavaCall {
	
	
	private final Log log = new Log("FM.AnulacionContratoCSC");
	
	public String SQL_MANDATO_UPDATE = loadResourceToString("cl/bcs/tesoreria/sql/MandatoPAC_Anulacion_UpdateContratoPAC.sql");
	public String SQL_SELECT_CONTRATO = loadResourceToString("cl/bcs/tesoreria/sql/MandatoPAC_Anulacion_SelectContratos.sql");
	public String SQL_SELECT_CONTRATO_SAAPV = loadResourceToString("cl/bcs/tesoreria/sql/MandatoPAC_Anulacion_SelectContratosSAAPV.sql");

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
	
	public static class ContratoCSCRelacionado {
		public Long 	IdMandatoPAC;
		public Long		IdFolioCSC;
		public String 	Estado="";

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
		public  String						p_DesdeCSC;
		public  String						p_DesdeSAAPV;		
		
		public EntradaAnulacionContrato(){
			p_Contrato 					= new ArrayList<InContrato>();
			p_DesdeCSC                  = "N";
			p_DesdeSAAPV				= "N";
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
			
			if(!entrada.p_DesdeCSC.equals("S")) {
				AnulacionContratoCSC anuContrato = new AnulacionContratoCSC();
				AnulacionContratoCSC.EntradaAnulacionContrato  anuContratoIn = new AnulacionContratoCSC.EntradaAnulacionContrato();
				anuContratoIn.p_IdEmpresa = entrada.p_IdEmpresa;
				anuContratoIn.p_IdUsuario = entrada.p_IdUsuario;
				anuContratoIn.p_IdModulo = "FMM";
				anuContratoIn.p_DesdePac ="S";
				
				List<Long> IdMandatoPAC = entrada.p_Contrato.stream().map(m -> m.IdContrato).collect(Collectors.toList());
                
                HashMap<String,Object> params = new HashMap<String,Object>();
                params.put("IdMandatoPAC", IdMandatoPAC);

                List<ContratoCSCRelacionado> contratosRelacionados = sql.query(con, SQL_SELECT_CONTRATO, params, AnulacionMandatoPAC.ContratoCSCRelacionado.class);
                
                for (InContrato mov: entrada.p_Contrato) {
                	ContratoCSCRelacionado contrato = contratosRelacionados.stream().filter(m -> Objects.equals(m.IdMandatoPAC, mov.IdContrato)).findFirst().orElse(null);
                	if(contrato.IdFolioCSC != null) {
                        AnulacionContratoCSC.InContrato AnularContrato = new AnulacionContratoCSC.InContrato();
                        AnularContrato.IdContrato = contrato.IdFolioCSC;
                        AnularContrato.Estado = contrato.Estado;
                        anuContratoIn.p_Contrato.add(AnularContrato);
                	}
                }				
				
                if(anuContratoIn.p_Contrato.size() != 0) {
                	AnulacionContratoCSC.SalidaAnulacionContrato anuContratOut = anuContrato.logica(con, anuContratoIn);
    				
    				if(anuContratOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
    					out.P_OUT_ERRORESCONTADOR = anuContratOut.P_OUT_ERRORESCONTADOR;
    					out.P_OUT_MENSAJES = anuContratOut.P_OUT_MENSAJES ;
    					return out;
    				}
    				
    				for(MensajeSalida mensajeContrato : anuContratOut.P_OUT_MENSAJES) {
                    	out.P_OUT_MENSAJES.add(mensajeContrato);
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
				sql.batch(con, SQL_MANDATO_UPDATE, actualizarContrato, UpdContrato.class);
				for (UpdContrato salida : actualizarContrato ) {
                    HashMap<String,String> valores = new HashMap<String,String>();
                    valores.put("IdContrato",salida.IdContrato.toString());
                    MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"ANULACION_MANDATO_PAC_EXITOSA",valores,con,entrada.p_IdEmpresa);
                    out.P_OUT_MENSAJES.add(msg);
				}
			}
			
			//Revocar SAAPV		
			if (!entrada.p_DesdeSAAPV.equals("S")) {
				CambiarEstadoContratosSAAPV anuContratoSAAPV = new CambiarEstadoContratosSAAPV();
				ContratosSAAPV_Entrada anuContratoSAAPVIn = new ContratosSAAPV_Entrada();
				
				anuContratoSAAPVIn.p_IdEmpresa = entrada.p_IdEmpresa;
				anuContratoSAAPVIn.p_IdUsuario = entrada.p_IdUsuario;
				anuContratoSAAPVIn.p_IdModulo = entrada.p_IdModulo;
				anuContratoSAAPVIn.p_Opcion = "ANU";
				anuContratoSAAPVIn.p_DesdePac ="S";
			    
	            List<Long> IdMandatoPAC = entrada.p_Contrato.stream().map(m -> m.IdContrato).collect(Collectors.toList());  
	            HashMap<String,Object> params = new HashMap<String,Object>();
	            params.put("IdMandatoPAC", IdMandatoPAC);
	            List<ContratosSAAPV> contratosSAAPVRelacionados = sql.query(con, SQL_SELECT_CONTRATO_SAAPV, params, ContratosSAAPV.class);
       
	            for (ContratosSAAPV mov: contratosSAAPVRelacionados) {
	    			ContratosSAAPV AnularContrato = new ContratosSAAPV();
	    			AnularContrato.IdFolioContratoSAAPV = mov.IdFolioContratoSAAPV;
	    			AnularContrato.IdEstado = "ANU";
	    			AnularContrato.IdPersona = mov.IdPersona;
	    			AnularContrato.IdCliente = mov.IdCliente;
	    			AnularContrato.IdEstadoSolicitud = "ANU";
	
	    			anuContratoSAAPVIn.p_ContratosSAAPV.add(AnularContrato);
	            }	
	            
	            if(anuContratoSAAPVIn.p_ContratosSAAPV.size() != 0) {
					ContratosSAAPV_Salida anuContratoSAAPVOut = anuContratoSAAPV.logica(con, anuContratoSAAPVIn);
	                
	                if(anuContratoSAAPVOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
	                	out.P_OUT_ERRORESCONTADOR = anuContratoSAAPVOut.P_OUT_ERRORESCONTADOR;
	                	out.P_OUT_MENSAJES = anuContratoSAAPVOut.P_OUT_MENSAJES ;
	                    return out;
	                }
	
	                for(MensajeSalida mensajeMandato : anuContratoSAAPVOut.P_OUT_MENSAJES) {
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
			try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
				try {
					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
					
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":176831}]}";
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":177079},{\"IdContrato\":177094}]}";
					// 2 SAAPV
					//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"IdContrato\":176744},{\"IdContrato\":167351}]}";
					
					EntradaAnulacionContrato Objin_json = gson.fromJson(json, EntradaAnulacionContrato.class);
					
					AnulacionMandatoPAC demo = new AnulacionMandatoPAC();	
					
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