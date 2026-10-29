package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.Date;
import java.sql.Date;
//import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarContratoSAAPV.PortafolioContratoSAAPV;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearOrdenTraspaso implements JavaCall   {

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_ORDENTRASPASO = loadResourceToString("cl/bcs/APV/sql/OrdenTraspaso_Insert.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	public static class OrdenTraspasoEntrada{
		public Long	IdTraspaso;
		public Long IdOrden;
		public Long IdSolicitudAporteRescate;
	}
	
	public static class OrdenTraspaso{
		public Long IdOrdenTraspaso;
		public Long	IdTraspaso;
		public Long IdOrden;
		public Long IdSolicitudAporteRescate;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public 	Long		p_IdEmpresa;
		public	Long		p_IdUsuario;
		public 	String		p_IdModulo;	
		public List<OrdenTraspaso>	p_OrdenTraspaso;
		
		public ParametrosEntradaServicio(){
			p_OrdenTraspaso = new ArrayList<OrdenTraspaso>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		SQL sql = new SQL();	
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		List<Long> IdOrdenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::TRA.IdOrdenTraspaso", (long)in.p_OrdenTraspaso.size());
		
		for(OrdenTraspaso filaOrdenTraspaso : in.p_OrdenTraspaso) {
			if(filaOrdenTraspaso.IdTraspaso > 0){
				OrdenTraspaso ordTraspaso = new OrdenTraspaso();
				List<OrdenTraspaso> 		OrdenTraspaso_insert = new ArrayList<OrdenTraspaso>();
				ordTraspaso.IdOrdenTraspaso = IdOrdenTraspaso.get(0);
				ordTraspaso.IdTraspaso = filaOrdenTraspaso.IdTraspaso;
				ordTraspaso.IdOrden = filaOrdenTraspaso.IdOrden; 
				ordTraspaso.IdSolicitudAporteRescate = filaOrdenTraspaso.IdSolicitudAporteRescate;
				OrdenTraspaso_insert.add(ordTraspaso);
				
				int[] resultado = sql.batch(con, SQL_CREAR_ORDENTRASPASO, OrdenTraspaso_insert, OrdenTraspaso.class);
				if(resultado != null && resultado.length > 0 && resultado[0] == 1){
					msg = new MensajeSalida();
						msg.id = IdOrdenTraspaso.get(0);
						msg.tipo = "OK";
						msg.codigo = "CREAR_ORDEN_TRASPASO_OK";
						msg.descripcion = "Se crea relacion Orden con Traspaso.";
						salida.P_OUT_MENSAJES.add(msg);
					}else{
						msg = new MensajeSalida();
						msg.id = IdOrdenTraspaso.get(0);
						msg.tipo = "ERROR";
						msg.codigo = "CREAR_ORDEN_TRASPASO_ERROR";
						msg.descripcion = "Problema al crear relacion Orden con Traspaso.";
						salida.P_OUT_MENSAJES.add(msg);
					}
			}else{
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_DATOS";
				msg.descripcion = "No hay orden creada para el traspaso " + filaOrdenTraspaso.IdTraspaso.toString();
				salida.P_OUT_MENSAJES.add(msg);
			}	
		};
		
		return salida;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":39,\"p_IdModulo\":\"APV\",\"p_OrdenTraspaso\":[{\"IdOrdenTraspaso\":0,\"IdTraspaso\":66,\"IdOrden\":0,\"IdSolicitudAporteRescate\":23955}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);	
				
				CrearOrdenTraspaso demo = new CrearOrdenTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				//con.commit();
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