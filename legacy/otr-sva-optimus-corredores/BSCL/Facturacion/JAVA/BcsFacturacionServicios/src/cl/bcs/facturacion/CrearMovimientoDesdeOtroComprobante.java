package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearComprobante.ResultadoProceso_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearMovimientoDesdeOtroComprobante implements JavaCall {
	
	private final Log log =new Log("FAC.CrearMovimientosDesdeOtroComprobante");
	
	private final String SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoDesdeOtroComprobante_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTEFORMASDEPAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoDesdeOtroComprobante_SelectMovimientosFormasDePago.sql");
	private final String SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTECOBROS = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoDesdeOtroComprobante_SelectMovimientosCobro.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	public static class CrearMovimientoDesdeOtroComprobante_Entrada{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long     p_IdPortafolio;
		public Long     p_IdCliente;
		public String   p_simularOperacion;
		public String   p_UsaReglasNegocio;
		public String   p_EsIngresoAutomatico = "N";
		public String	p_CrearComprobanteAutomatico = "N";    
		public List<Comprobante_Entrada> P_COMPROBANTE;
		  		
		public CrearMovimientoDesdeOtroComprobante_Entrada (){
			P_COMPROBANTE	= new ArrayList<Comprobante_Entrada>();
		 }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobante_Entrada {		
		public Long   IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	
	
	
	
	public static class CrearMovimientoDesdeOtroComprobante_Salida{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long                P_OUT_ERRORESCONTADOR;
		public Long                P_OUT_ADVERTENCIASCONTADOR;		
		
		public List<Comprobante_Salida>		 P_OUT_COMPROBANTE;
		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;
		
		
		public CrearMovimientoDesdeOtroComprobante_Salida(){
			                                                  P_OUT_MENSAJES = new ArrayList<>();
			                                                  P_OUT_COMPROBANTE = new ArrayList<>();
															  P_OUT_RESULTADOPROCESO = new ArrayList<>();			
		                                                   }
		
		public String toString() {
			                        return Utils.printJson(this, false);
		                         }
	};
	
	
	
	
	/**********************
	 * lógica del programa
	 **********************/	
	public CrearMovimientoDesdeOtroComprobante_Salida logica(Connection conn, CrearMovimientoDesdeOtroComprobante_Entrada in) throws Exception {
		
		CrearMovimientoDesdeOtroComprobante_Salida salida = new CrearMovimientoDesdeOtroComprobante_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
				
		SQL _sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");				
		
		List<MovimientoComprobante_Entrada> movimientoComprobanteInsert = null;
		List<MovimientoComprobanteFormaDePago_Entrada> movimientoComprobanteFormaDePagoInsert = null;		
		List<MovimientoComprobanteCobro_Entrada> movimientoComprobanteCobroInsert = null;
		
		ArrayList<Long> idsComprobante = new ArrayList<Long>();
		
		//Acumula registros entrada para validación
		for (Comprobante_Entrada idCmbte : in.P_COMPROBANTE){
			idsComprobante.add(idCmbte.IdComprobante);
		}
					
		if(idsComprobante.size() > 0){			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("idsComprobante", idsComprobante);
			params.put("idsCrearComprobanteAutomatico", 'N');
			params.put("idCliente", in.p_IdCliente);
			params.put("idPortafolio", in.p_IdPortafolio);			
			params.put("idUsuario", in.p_IdUsuario);		
			params.put("fechaSistema", fechaSistema);
			
			movimientoComprobanteInsert = _sql.query(conn, SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTE, params, MovimientoComprobante_Entrada.class);
						
			if (movimientoComprobanteInsert.size() > 0){
				//Forma de pago y cobro
			    ArrayList<Long> idsMovimientoComprobanteOld = new ArrayList<Long>();
			    for (MovimientoComprobante_Entrada mvtoCompNFac : movimientoComprobanteInsert) {
				   idsMovimientoComprobanteOld.add(mvtoCompNFac.NumeroMvtoCmbte);				
			     }
			
			    HashMap<String,Object> paramsFormPago = new HashMap<String,Object>();
			    paramsFormPago.put("idsMovimientoComprobanteOld", idsMovimientoComprobanteOld);			
			
			    movimientoComprobanteFormaDePagoInsert = _sql.query(conn, SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTEFORMASDEPAGO, paramsFormPago, MovimientoComprobanteFormaDePago_Entrada.class);
			    movimientoComprobanteCobroInsert = _sql.query(conn, SQL_SELECT_MOVIMIENTOSDESDECOMPROBANTECOBROS, paramsFormPago, MovimientoComprobanteCobro_Entrada.class);
			
			    CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada  callCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
			    callCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
			    callCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
			    callCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
			    callCrearMovimientoComprobante.p_simularOperacion = "N";
			    callCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
			    callCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
			    callCrearMovimientoComprobante.p_Movimientos = movimientoComprobanteInsert;
			    callCrearMovimientoComprobante.p_MovimientosFormaDePago = movimientoComprobanteFormaDePagoInsert;
			    callCrearMovimientoComprobante.p_MovimientosCobros = movimientoComprobanteCobroInsert;
			
			    CrearMovimientoComprobante mvtonc = new CrearMovimientoComprobante();
			    CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resultIngMvtoNC = mvtonc.logica(conn, callCrearMovimientoComprobante);
			    
			    if (resultIngMvtoNC.p_out_ResultadoProceso.size() > 0){
					if (!resultIngMvtoNC.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: resultIngMvtoNC.P_OUT_MENSAJES){
							salida.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, recorreErr.codigo, new HashMap<>() ,conn,in.p_IdEmpresa));
							salida.P_OUT_ERRORESCONTADOR = resultIngMvtoNC.P_OUT_ERRORESCONTADOR;
							return salida;
						  }
					  }else {					
						       String codigo = "", descripcion = "";
						       Long ids = 0L;
						       for (MensajeSalida recorre: resultIngMvtoNC.P_OUT_MENSAJES){							          
							     ArrayList<Long> idsMovimientoComprobante = new ArrayList<Long>();									            
							     for (MovimientoComprobante_Salida mov : resultIngMvtoNC.p_out_Movimientos) {
								     if (!idsMovimientoComprobante.contains(mov.IdMovimientoComprobante)) {
									     idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
								       }
							       }
							
							     codigo = recorre.codigo;
							     ids = recorre.id;
							     descripcion = Diccionario.getMensaje(conn,in.p_IdEmpresa, codigo)
									.add("IdMovimientoComprobante", idsMovimientoComprobante).msg();
						       }
						
						       salida.P_OUT_MENSAJES.add(new MensajeSalida(ids,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
			                   salida.P_OUT_ERRORESCONTADOR = 0L;
						       salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
					        }
					
				 }
			    
			   }else{
				      String codigo = "NO_EXISTE_COMPROBANTES";
				      String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
				      salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				      return salida;
			        };			
		}else{
			   String codigo = "NO_VIENEN_COMPROBANTES";
			   String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			   salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			   return salida;
		     };
		 
		return salida;		
	}
	
	
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		CrearMovimientoDesdeOtroComprobante_Entrada in=gson.fromJson(jin.toString() ,CrearMovimientoDesdeOtroComprobante_Entrada.class);
		CrearMovimientoDesdeOtroComprobante_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":16,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_IdCliente\":249199,\"p_IdPortafolio\":153970,\"P_COMPROBANTE\":[{\"IdComprobante\":\"752742\"}]}";
			
			
			CrearMovimientoDesdeOtroComprobante_Entrada Objin = gson.fromJson(json, CrearMovimientoDesdeOtroComprobante_Entrada.class);
			
			CrearMovimientoDesdeOtroComprobante demo=new CrearMovimientoDesdeOtroComprobante();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearMovimientoDesdeOtroComprobante_Salida ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
 			  }
	    	}
		System.out.println("== END ==");
	}
	
}
