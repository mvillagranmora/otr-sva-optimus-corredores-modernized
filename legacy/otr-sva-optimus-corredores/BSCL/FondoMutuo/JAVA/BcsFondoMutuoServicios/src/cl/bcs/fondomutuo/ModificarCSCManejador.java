package cl.bcs.fondomutuo;

import java.util.ArrayList;
import java.sql.Connection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ModificarCSC.EntradaModificarCSC;
import cl.bcs.fondomutuo.ModificarCSC.InModificarCSC;
import cl.bcs.fondomutuo.ModificarCSC.SalidaModificarCSC;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ModificarCSCManejador implements JavaCall{
	private final Log log = new Log("FM.ModificarCSCManejador");
	
	public static class EntradaModificarCSCManejador{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InModificarCSC> p_ModificarCSC;	

		public EntradaModificarCSCManejador() {
			this.p_ModificarCSC = new ArrayList<InModificarCSC>();
	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarCSCManejador{
		public Long P_OUT_ERRORESCONTADOR;
		public List<EntidadContrato>	CONTRATO;

		public SalidaModificarCSCManejador(){
			CONTRATO = new ArrayList<EntidadContrato>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadContrato{
		public String					Folio;
		public Long						IdFolioCSC;
		public List<MensajesErrores>	Mensajes;
        
		public EntidadContrato(){
			Mensajes = new ArrayList<MensajesErrores>();
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajesErrores{
		public Long	IdInterno;
		public String Tipo;
		public String Codigo;
		public String Descripcion;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarCSCManejador jin = gson.fromJson(in.toString(), EntradaModificarCSCManejador.class);
		SalidaModificarCSCManejador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//map
	public SalidaModificarCSCManejador logica (Connection con, EntradaModificarCSCManejador entrada) throws Exception{		
		log.write("INICIO ModificarCSCManejador");
		SalidaModificarCSCManejador out = new SalidaModificarCSCManejador();
		List<SalidaModificarCSC> salidas = new ArrayList<SalidaModificarCSC>();
		List<String> folios = new ArrayList<String>();

		try {
			for (InModificarCSC mov : entrada.p_ModificarCSC) {
				InModificarCSC inContrato = new InModificarCSC();				
				EntradaModificarCSC entradaModificarCSC = new EntradaModificarCSC();
				
				inContrato.FolioCSC = mov.FolioCSC;
				inContrato.Monto = mov.Monto;
				inContrato.Moneda = mov.Moneda;
				inContrato.Canal = mov.Canal;
				inContrato.p_ModificarCSCDetalle = mov.p_ModificarCSCDetalle;
				
				entradaModificarCSC.p_ModificarCSC.add(inContrato);
				
				entradaModificarCSC.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaModificarCSC.p_IdModulo = entrada.p_IdModulo;
				entradaModificarCSC.p_IdUsuario = entrada.p_IdUsuario;
				entradaModificarCSC.p_SimularOperacion = entrada.p_SimularOperacion;
				entradaModificarCSC.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				
				ModificarCSC modificarCSC = new ModificarCSC();
				SalidaModificarCSC salidaModificarCSC = modificarCSC.logica(con, entradaModificarCSC);	
				salidas.add(salidaModificarCSC);
				folios.add(mov.FolioCSC);
			}	
						
			int i = 0;
			for(SalidaModificarCSC salida : salidas){
				EntidadContrato entidadMandato = new EntidadContrato();
				List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
				
				for (MensajeSalida mensajeSalida : salida.P_OUT_MENSAJES){
					MensajesErrores mensajesErrores = new MensajesErrores();
					mensajesErrores.IdInterno = mensajeSalida.id;
					mensajesErrores.Tipo = mensajeSalida.tipo;
					mensajesErrores.Codigo = mensajeSalida.codigo;
					mensajesErrores.Descripcion = mensajeSalida.descripcion;
					listMensajesErrores.add(mensajesErrores);
					entidadMandato.Mensajes = listMensajesErrores;
				}
			
				entidadMandato.Folio = salida.P_OUT_RESULTADO.size() > 0 ? salida.P_OUT_RESULTADO.get(0).FolioCSC : "";
				entidadMandato.IdFolioCSC = salida.P_OUT_RESULTADO.size() > 0 ? salida.P_OUT_RESULTADO.get(0).IdFolioCSC : 0;
			
				out.CONTRATO.add(entidadMandato);
				
				if(salida.P_OUT_ERRORESCONTADOR > 0)
					out.P_OUT_ERRORESCONTADOR++;
				
				i++;
			}
		
			return out;
			
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(error);
			throw error;
		}	
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ModificarCSC\":[{\"FolioCSC\":1884,\"Monto\":100,\"Moneda\":\"CLP\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"18Q\",\"Porcentaje\":100,\"Regimen\":\"18Q\"}]},{\"FolioCSC\":1884,\"Monto\":100,\"Moneda\":\"CLP\",\"p_ModificarCSCDetalle\":[{\"Serie\":\"18Q\",\"Porcentaje\":100,\"Regimen\":\"18Q\"}]}]}";	
			EntradaModificarCSCManejador  Objin = gson.fromJson(json, EntradaModificarCSCManejador.class);
			ModificarCSCManejador  demo = new ModificarCSCManejador();
			try {
				SalidaModificarCSCManejador ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
	}
}

