package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.fondomutuo.RevocacionContratoCSC;
import cl.bcs.fondomutuo.RevocacionContratoCSC.InContrato;
import cl.bcs.fondomutuo.RevocacionContratoCSC.SalidaRevocacionContrato;
import cl.bcs.fondomutuo.RevocacionContratoCSC.EntradaRevocacionContrato;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class RevocacionContratoCSCManejador implements JavaCall {
	private final Log log = new Log("FM.RevocacionContratoCSCManejador");
	
	public static class InContratoManejador{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InContratoM> p_Contrato;
		
		public InContratoManejador() {
			this.p_Contrato = new ArrayList<InContratoM>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class InContratoM{
		public String FolioCSC;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
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
	
	
	//  ENTRADA
	public static class EntradaRevocacionContratoManejador {		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_Contrato;		
		
		public EntradaRevocacionContratoManejador(){
			p_Contrato 					= new ArrayList<InContrato>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}		
	
	// SALIDA
	public static class SalidaRevocacionContratoManejador{
		public Long P_OUT_ERRORESCONTADOR;
		public List<EntidadContrato> CONTRATO;
			
		public SalidaRevocacionContratoManejador(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.CONTRATO = new ArrayList<EntidadContrato>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
				
	public static class EntidadContrato{
		public String Folio;
		public Long	IdFolioCSC;
		public List<MensajesErrores> Mensaje;
	        
		public EntidadContrato(){
			Mensaje = new ArrayList<MensajesErrores>();
		}
			
		@Override
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
	public static class MensajesErrores{
		public String	IdInterno;
		public String	Tipo;
		public String	Codigo;
		public String	Descripcion;
			
		@Override
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaRevocacionContratoManejador jin = gson.fromJson(in.toString(), EntradaRevocacionContratoManejador.class);
		SalidaRevocacionContratoManejador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
	    JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
		
	public SalidaRevocacionContratoManejador logica(Connection con, EntradaRevocacionContratoManejador entrada) throws Exception{
		log.write("INICIO SalidaRevocacionContratoManejador");
		SalidaRevocacionContratoManejador out = new SalidaRevocacionContratoManejador();
		List<SalidaRevocacionContrato> salidas = new ArrayList<SalidaRevocacionContrato>();
		List<String> folios = new ArrayList<String>();
		try{	
			for (InContrato mov : entrada.p_Contrato) {
				InContrato inContrato = new InContrato();
				EntradaRevocacionContrato  entradaRevocacionContrato = new EntradaRevocacionContrato();
				
				inContrato.FolioCSC = mov.FolioCSC;
				entradaRevocacionContrato.p_Contrato.add(inContrato);
				entradaRevocacionContrato.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaRevocacionContrato.p_IdModulo = entrada.p_IdModulo;
				entradaRevocacionContrato.p_IdUsuario = entrada.p_IdUsuario;
			
				RevocacionContratoCSC revocacionContratoCSC = new RevocacionContratoCSC ();
				SalidaRevocacionContrato salidaRevocacionContrato = revocacionContratoCSC.logica(con, entradaRevocacionContrato);
				salidas.add(salidaRevocacionContrato);
				folios.add(mov.FolioCSC);
			}
			
			int i = 0;
			for(SalidaRevocacionContrato salida : salidas){
				EntidadContrato entidadContrato = new EntidadContrato();
				List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
			
				for (MensajeSalida mensajeSalida : salida.P_OUT_MENSAJES){
					MensajesErrores mensajesErrores = new MensajesErrores();
					mensajesErrores.IdInterno = folios.get(i);
					mensajesErrores.Tipo = mensajeSalida.tipo; 
					mensajesErrores.Codigo = mensajeSalida.codigo;
					mensajesErrores.Descripcion = mensajeSalida.descripcion;
					listMensajesErrores.add(mensajesErrores);
					entidadContrato.Mensaje= listMensajesErrores;
				}
				
				entidadContrato.Folio = folios.get(i);
				entidadContrato.IdFolioCSC = salida.P_OUT_RESULTADO.size() > 0 ? salida.P_OUT_RESULTADO.get(0).IdFolioCSC : 0;;
						
				out.CONTRATO.add(entidadContrato);
				if(salida.P_OUT_ERRORESCONTADOR > 0)
					out.P_OUT_ERRORESCONTADOR++;
				i++;
			}
		
			return out;
		}catch(Exception error){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}
			
	}
		/********************
		 * Metodo Principal *
		 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FMM\",\"p_Contrato\":[{\"FolioCSC\":\"110012\"}]}";
				EntradaRevocacionContratoManejador Objin_json = gson.fromJson(json, EntradaRevocacionContratoManejador.class);
					
				RevocacionContratoCSCManejador demo = new RevocacionContratoCSCManejador();	

		 		try { 
		 			SalidaRevocacionContratoManejador ObjOut = demo.logica(con, Objin_json);
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
		}
	}
}
