package cl.bcs.mesa.SPT.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.mesa.SPT.AnularOperacionSpot;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotEntrada;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.WSConsultaOperacionesSpot.OperacionesSpot;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class AnularOperacionSpotWs implements JavaCall{

	
	static final Logger logger = LogManager.getLogger(AnularOperacionSpot.class);
	
	
	public static class EntradaOperacionSpotWs  extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaOperacionSpot> p_DatosOperacion;
		
		public EntradaOperacionSpotWs() {
			this.p_DatosOperacion = new ArrayList<EntradaOperacionSpot>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class EntradaOperacionSpot extends BcsPojo{
		public Long FolioOperacion; 
	}
	
	
	
	public static class SalidaOperacionSpotWs extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaOperacionSpotWs() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaOperacionSpotWs jin = gson.fromJson(in.toString(), EntradaOperacionSpotWs.class);
		SalidaOperacionSpotWs out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	private static boolean isNumeric(String cadena){
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}
	
	public SalidaOperacionSpotWs logica(Connection con, EntradaOperacionSpotWs entrada) throws Exception {
		logger.info("INICIO::AnularOperacionSpotWs");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaOperacionSpotWs out = anularOperacionSpot(con, entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AnularOperacionSpotWs", finServicio - inicioServicio);
		return out;
	}
	
	

	
	
	private SalidaOperacionSpotWs anularOperacionSpot(Connection con, EntradaOperacionSpotWs entrada) throws Exception {
		logger.debug("INICIO::AnularOperacionSpotWs.validarExistenciaOperacionesSpot");
		
		
		SalidaOperacionSpotWs out = new SalidaOperacionSpotWs();
		

		try {
			

			AnularOperacionSpot anularOperacionSpot = new AnularOperacionSpot();
			
			AnularOperacionSpotEntrada anularOperacionSpotEntrada = new AnularOperacionSpotEntrada();
			anularOperacionSpotEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			anularOperacionSpotEntrada.p_IdModulo = entrada.p_IdModulo;
			anularOperacionSpotEntrada.p_IdUsuario = entrada.p_IdUsuario;
			anularOperacionSpotEntrada.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			anularOperacionSpotEntrada.p_DatosOperacion =  entrada.p_DatosOperacion
					.stream()
					.map(entradaOperacion->{
						OperacionSpot operacion = new OperacionSpot();
						operacion.NumeroOperacion = String.valueOf(entradaOperacion.FolioOperacion);
						return operacion;
					})
					.collect(Collectors.toList());
			
			
			AnularOperacionSpotSalida anularOperacionSpotSalida = anularOperacionSpot.logica(con, anularOperacionSpotEntrada);
			
			List<MensajeSalida> listaMensajeSalida = new ArrayList<MensajeSalida>();

			if(anularOperacionSpotSalida.P_OUT_MENSAJES.size() > 0) {
				for (MensajeSalida salida : anularOperacionSpotSalida.P_OUT_MENSAJES ) {
		
					if(salida.codigo == "UI_ERROR_USUARIO_NO_ES_EMPLEADO" 
							|| salida.codigo == "NO_HAY_OPERACIONES_SPOT_PARA_ANULAR"
							|| salida.codigo == "USUARIO_NO_AUTORIZADO_A_ANULAR_OPERACION") {
						
						out.P_OUT_ERRORESCONTADOR =  MensajeSalida.getNumeroErrores(anularOperacionSpotSalida.P_OUT_MENSAJES);
						out.P_OUT_MENSAJES = anularOperacionSpotSalida.P_OUT_MENSAJES;
						out.P_OUT_ADVERTENCIASCONTADOR = anularOperacionSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
						
						return out;
						
					}
					
					listaMensajeSalida.add(salida);
				}
				
				for (EntradaOperacionSpot objectEntrada : entrada.p_DatosOperacion) {
					
					List<MensajeSalida> total = listaMensajeSalida.stream().filter(p-> p.id.equals(objectEntrada.FolioOperacion)).collect(Collectors.toList());

					if(total.size() == 0) {
						MensajeSalida mensajeCustom = new MensajeSalida();
						mensajeCustom.id = objectEntrada.FolioOperacion;
						mensajeCustom.tipo = MensajeSalida.MENSAJE_OK;
						mensajeCustom.codigo = "ANULACION_OK";
						mensajeCustom.descripcion = "OPERACION FOLIO " + objectEntrada.FolioOperacion + " ANULADA CORRECTAMENTE";
						listaMensajeSalida.add(mensajeCustom);
					}
					
				}
				
			}else if(anularOperacionSpotSalida.P_OUT_MENSAJES.size() == 0) {

				for (EntradaOperacionSpot objectEntrada : entrada.p_DatosOperacion) {
					MensajeSalida mensajeCustom = new MensajeSalida();
					mensajeCustom.id = objectEntrada.FolioOperacion;
					mensajeCustom.tipo = MensajeSalida.MENSAJE_OK;
					mensajeCustom.codigo = "ANULACION_OK";
					mensajeCustom.descripcion = "OPERACION FOLIO " + objectEntrada.FolioOperacion + " ANULADA CORRECTAMENTE";
					listaMensajeSalida.add(mensajeCustom);
					
				}
			}

			
			out.P_OUT_ERRORESCONTADOR =  MensajeSalida.getNumeroErrores(anularOperacionSpotSalida.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = listaMensajeSalida;
			out.P_OUT_ADVERTENCIASCONTADOR = anularOperacionSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		
		
		return out;

	}
	
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {  //getBOLSAConnection
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
						
			
			String jsonOptimusDesa = "{p_IdEmpresa: 1, p_IdUsuario: 14, p_IdModulo: 'MSP', p_UsaReglasNegocio: 'S', p_DatosOperacion: [  {\"FolioOperacion\":11666},{\"FolioOperacion\":100},{\"FolioOperacion\":11673},{\"FolioOperacion\":11674}] }";

			
			EntradaOperacionSpotWs Objin = gson.fromJson(jsonOptimusDesa, EntradaOperacionSpotWs.class);
			AnularOperacionSpotWs demo = new AnularOperacionSpotWs();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaOperacionSpotWs ObjOut = demo.logica(con, Objin);
				
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut);
 				
 				
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
				
				System.out.println(er.getMessage());
			}
 			//con.commit();
			finally {
				con.close();
			}
			
 			System.out.println("== END ==");
		}
		
	}
	

}

