package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
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
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearComprobanteCobroMensual implements JavaCall {
	private final String SQL_SELECT_COMPROBANTESCOBROSMENSUAL = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_SelectMovimientos.sql");	
	private final String SQL_SELECT_COMPROBANTESCOBROSMENSUALFORMASDEPAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_SelectMovimientosFormasDePago.sql");
	private final String SQL_SELECT_VALIDACIONCOBROCOMISIONES = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_ValidacionCobroComision.sql");
	private final String SQL_UPDATE_COMPROBANTESCOBROSMENSUAL = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_UpdateComprobantes.sql");
	
	//ENTRADAS
	public static class CrearComprobanteCobroMensual_Entrada {		
	  public Long		p_IdEmpresa;		
	  public Long		p_IdUsuario;   
	  public String		p_IdModulo;   
	  public String		p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
	  public String		p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
	  public String		p_EsIngresoAutomatico = "N";	// S/N si viene de} otro modulo es S para no validar requisitos basicos nuevamente
	  public String		p_CrearComprobanteAutomatico = "S";
	  public String		p_EsCargaMasiva = "N";
	  public String		p_Glosa;
	  public String		p_MesProcesar;
	  
	  public List<Comprobante_Entrada>	P_COMPROBANTECOBROMENSUAL;
	  
		
	  public CrearComprobanteCobroMensual_Entrada (){

		  P_COMPROBANTECOBROMENSUAL	= new ArrayList<Comprobante_Entrada>();
	  }
	    
	  public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	public static class Comprobante_Entrada {
		public Long   IdPortafolio;
		public Long   IdCliente;
		public Long   IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ValidacionComprobante {
		public Long   Validacion;
		public Long   IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};

	public static class Comprobante_Update {
		public Long   IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class MovimientoComprobanteCobroMesualRes{
		public Long IdMovimientoComprobanteMensual;
		public Long IdComprobanteOriginal;
		public Long IdMovimientoComprobanteDestino;
		public Long IdComprobanteDestino;
		public BigDecimal Monto;
		public BigDecimal MontoComprobante;
		public BigDecimal MontoLiquidacion;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobroMesual_Proceso{
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class Movimiento_Salida {
		public Long IdComprobanteReferencia;
		public Long IdMovimientoComprobante;
		public Long NumeroMvtoCmbte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//Salidas
	public static class CrearComprobanteCobroMensual_Salida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		public List<Movimiento_Salida>				P_OUT_MOVIMIENTO;
		
		public CrearComprobanteCobroMensual_Salida (){
			 P_OUT_MENSAJES	= new ArrayList<>();
			 P_OUT_COMPROBANTE = new ArrayList<>();
			 P_OUT_RESULTADOPROCESO = new ArrayList<>();
			 P_OUT_MOVIMIENTO = new ArrayList<>();
		  }
			
		public String toString() {
			return Utils.printJson(this, false);
		 }
	};
	
	//Proceso
	public static class CrearComprobanteCobroMensual_Proceso {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public String IdEstadoComprobante;

		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearComprobanteCobroMensual_Entrada in = gson.fromJson(jin.toString(), CrearComprobanteCobroMensual_Entrada.class);
		CrearComprobanteCobroMensual_Salida salida = logica(con, in);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}	
	 
	public CrearComprobanteCobroMensual_Salida logica(Connection conn, CrearComprobanteCobroMensual_Entrada in) throws Exception {
		CrearComprobanteCobroMensual_Salida salida = new CrearComprobanteCobroMensual_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL _sql = new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		//Validaciones
		if (in.P_COMPROBANTECOBROMENSUAL.size() == 0) {
			String codigo = "NO_VIENEN_COMPROBANTES";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}

		//Lectura de Informacion
		{			
			List<MovimientoComprobante_Entrada> _mvtoComprobantesCobMen = null;
				
			List<MovimientoComprobanteFormaDePago_Entrada> _mvtoComprobantesNCFDePago = null;
			List<MovimientoComprobanteCobro_Entrada> _mvtoComprobantesNCCobros = new ArrayList<>();
			
			ArrayList<Long> idsComprobante = new ArrayList<Long>();
			ArrayList<Long> idsComprobanteReal = new ArrayList<Long>();
			ArrayList<Comprobante_Update> comprobantesUpdate = new ArrayList<>();
			Comprobante_Update comprobanteUpdate;
						
			//Validacion de Previa nota de credito
			List<Long> cmbteCobMens = new ArrayList<Long>();
			for (Comprobante_Entrada idCmbte : in.P_COMPROBANTECOBROMENSUAL) {
				if (!cmbteCobMens.contains(idCmbte.IdComprobante)) {
					comprobanteUpdate = new Comprobante_Update();
					comprobanteUpdate.IdComprobante = idCmbte.IdComprobante;
					comprobantesUpdate.add(comprobanteUpdate);
					idsComprobante.add(idCmbte.IdComprobante);
				}
			}				
			
			if (idsComprobante.size() == 0) {
				return salida; 
		   	 };
		   	 	
		  	 List<Long> idCompro = new ArrayList<Long>();
		   	 for (Long id : idsComprobante) {
				idCompro.add(id);				
			}
		   	 
		   	HashMap<String,Object> idComprobante = new HashMap<String,Object>();
		   	idComprobante.put("IdComprobante", idCompro);		   	
			List<ValidacionComprobante> validacion = _sql.query(conn, SQL_SELECT_VALIDACIONCOBROCOMISIONES, idComprobante, ValidacionComprobante.class);
			
			HashMap<Long,Long> hashIdComprobanteValidacion = new HashMap<>();			
			Comprobante_Update comprobanteUpdateReal;			
			List<Comprobante_Update> comprobantesUpdateReal = new ArrayList<Comprobante_Update>();
			
            //creamos un hash de todos las las validacions
            validacion.forEach(e -> hashIdComprobanteValidacion.put(e.IdComprobante, e.IdComprobante));
            HashMap<String,String> datosMensaje;
            for (Long id : idsComprobante) {
                if (id.equals(hashIdComprobanteValidacion.get(id))) {
                    datosMensaje = new HashMap<String,String>();
                    datosMensaje.put("IdComprobante", id.toString());                                  
                    salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_VALIDACION_JPM",datosMensaje,conn,in.p_IdEmpresa));
                } else {
                	      comprobanteUpdateReal = new Comprobante_Update();
                          idsComprobanteReal.add(id);
                          
                          //Para update de estado
                          comprobanteUpdateReal.IdComprobante = id;
                          comprobantesUpdateReal.add(comprobanteUpdateReal);
                       }
            }	
            
			if(idsComprobanteReal.size() > 0) {
				
			   	// **** Parametros para movimientos cobros mensual agrupados ****// 
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("idsComprobante", idsComprobanteReal);
				params.put("idsFechaSistema", fechaSistema);
				params.put("idsUsuario", in.p_IdUsuario);
				params.put("idsEmpresa", in.p_IdEmpresa);
				params.put("idsCrearComprobanteAutomatico", in.p_CrearComprobanteAutomatico);
				
				in.p_MesProcesar = in.p_MesProcesar != null && in.p_MesProcesar.length() >= 7 ? in.p_MesProcesar.substring(0, 7) : "";
				params.put("idsMesProcesar", in.p_MesProcesar);			
				
				_mvtoComprobantesCobMen = _sql.query(conn, SQL_SELECT_COMPROBANTESCOBROSMENSUAL, params, MovimientoComprobante_Entrada.class);
							
				
				//Forma de pago
				ArrayList<Long> idsMovimientoComprobanteOld = new ArrayList<Long>();			
		
				for (MovimientoComprobante_Entrada mvtoCompNC : _mvtoComprobantesCobMen) {
					idsMovimientoComprobanteOld.add(mvtoCompNC.NumeroMvtoCmbte);
				}
				
				HashMap<String,Object> paramsFormPago = new HashMap<String,Object>();
				paramsFormPago.put("idsMovimientoComprobanteOld", idsMovimientoComprobanteOld);
								
				_mvtoComprobantesNCFDePago = _sql.query(conn, SQL_SELECT_COMPROBANTESCOBROSMENSUALFORMASDEPAGO, paramsFormPago, MovimientoComprobanteFormaDePago_Entrada.class);
				
				CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada  callCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
				callCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
				callCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
				callCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
				callCrearMovimientoComprobante.p_simularOperacion = "N";
				callCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
				callCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
				callCrearMovimientoComprobante.p_Movimientos = _mvtoComprobantesCobMen;
				callCrearMovimientoComprobante.p_MovimientosFormaDePago = _mvtoComprobantesNCFDePago;
				callCrearMovimientoComprobante.p_MovimientosCobros = _mvtoComprobantesNCCobros;
				callCrearMovimientoComprobante.p_EsCargaMasiva = in.p_EsCargaMasiva;
				
				CrearMovimientoComprobante mvtonc = new CrearMovimientoComprobante();
				CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resultIngMvtoNC = mvtonc.logica(conn, callCrearMovimientoComprobante);
				salida.P_OUT_MOVIMIENTO.addAll(formatearMovimientos(resultIngMvtoNC.p_out_Movimientos, _mvtoComprobantesCobMen));
				salida.P_OUT_RESULTADOPROCESO.addAll(formatearResultadoProceso(resultIngMvtoNC.p_out_ResultadoProceso));
				salida.P_OUT_MENSAJES.addAll(resultIngMvtoNC.P_OUT_MENSAJES);
				
				_sql.batch(conn, SQL_UPDATE_COMPROBANTESCOBROSMENSUAL, comprobantesUpdateReal, Comprobante_Update.class);
			}
		}	
				
		return salida;				
	 };
	
	 private List<ResultadoProceso_Salida>  formatearResultadoProceso(List<cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida> p_out_ResultadoProceso) {
		 List<ResultadoProceso_Salida> salida = new ArrayList<>();
		 ResultadoProceso_Salida salidaFormateada;
		 for (cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida elemento : p_out_ResultadoProceso) {
			 salidaFormateada = new ResultadoProceso_Salida();
			 salidaFormateada.Campo = elemento.Campo;
			 salidaFormateada.Id = elemento.Id;
			 salidaFormateada.Servicio = elemento.Servicio;
			 salidaFormateada.Resultado = elemento.Resultado;
			 salida.add(salidaFormateada);
		 }
		 return salida;
	 }
	 
	 private List<Movimiento_Salida> formatearMovimientos(List<MovimientoComprobante_Salida> p_out_Movimientos, List<MovimientoComprobante_Entrada> _mvtoComprobantesNC) {
		 List<Movimiento_Salida> salida = new ArrayList<>();
		 Movimiento_Salida salidaFormateada;
		 for (MovimientoComprobante_Salida elemento : p_out_Movimientos) {
			 for (MovimientoComprobante_Entrada elementoEntrada : _mvtoComprobantesNC) {
				if (elementoEntrada.NumeroMvtoCmbte.equals(elemento.NumeroMvtoCmbte)) {
					salidaFormateada = new Movimiento_Salida();
					 salidaFormateada.IdComprobanteReferencia = elementoEntrada.IdComprobanteReferencia;
					 salidaFormateada.IdMovimientoComprobante = elemento.IdMovimientoComprobante;
					 salidaFormateada.NumeroMvtoCmbte = elemento.NumeroMvtoCmbte;
					 salida.add(salidaFormateada);
				}
			 }
		 }
		 return salida;
	 }
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":16,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_Glosa\":\"Proceso Facturacion de Cobros Mensual\",\"p_MesProcesar\":\"2018-10-16T03:00:00.000Z\",\"P_COMPROBANTECOBROMENSUAL\":[{\"IdPortafolio\":153970,\"IdCliente\":249199,\"IdComprobante\":754234,\"IdTipoComprobante\":\"FAE\",\"FechaComprobante\":\"2018-10-16\"},{\"IdPortafolio\":153970,\"IdCliente\":249199,\"IdComprobante\":754233,\"IdTipoComprobante\":\"FAE\",\"FechaComprobante\":\"2018-10-16\"}]}";
			CrearComprobanteCobroMensual_Entrada Objin = gson.fromJson(json, CrearComprobanteCobroMensual_Entrada.class);
			
			CrearComprobanteCobroMensual demo=new CrearComprobanteCobroMensual();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearComprobanteCobroMensual_Salida ObjOut=demo.logica(con, Objin); 				
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
			    er.printStackTrace();	
				con.rollback();
			    //con.commit();
			}
 			//con.commit();
 			con.rollback();
	    	}		
		 	
		System.out.println("== END ==");		
	}	
}

	