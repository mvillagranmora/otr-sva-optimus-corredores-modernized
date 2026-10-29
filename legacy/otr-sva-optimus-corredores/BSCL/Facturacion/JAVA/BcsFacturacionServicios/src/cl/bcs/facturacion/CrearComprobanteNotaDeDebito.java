package cl.bcs.facturacion;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
// Servicio que genera Nota de Debito. 
public class CrearComprobanteNotaDeDebito implements JavaCall {
	private final Log log = new Log("CrearComprobanteNotaDeDebito");
	
	private final String SQL_SELECT_COMPROBANTESNOTADEDEBITO             = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectMovimientos.sql");
	private final String SQL_SELECT_COMPROBANTESNOTADEDEBITOFORMASDEPAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosFormasDePago.sql");
	private final String SQL_SELECT_COMPROBANTESNOTADEDEBITOCOBROS       = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosCobros.sql");
	private final String SQL_SELECT_VERIFICAR_COMPROBANTE                = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectVerificarComprobante.sql"); 
	private final String SQL_SELECT_VERIFICAR_MOVIMIENTO                 = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectVerificarMovimiento.sql");
	private final String SQL_SELECT_NOTA_DEBITO_REFERENCIA               = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectMovimientosComprobanteReferencia.sql");
	private final String SQL_UPDATE_ASIGNACIONES 			             = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_UpdateAsignacion.sql");
	private final String SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS            = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_ConsultaCustodia.sql");   //--AnularComprobanteFinal_SelectCarterasCompra.sql

	//ENTRADAS
	public static class CrearComprobanteNotaDeDebito_Entrada {		
	  public Long		p_IdEmpresa;		
	  public Long		p_IdUsuario;   
	  public String		p_IdModulo;   
	  public String		p_simularOperacion;				    // S/N se usa para simular operacion (algunos casos)
	  public String		p_UsaReglasNegocio;				    // S/N se usa en caso de servicios anidados.
	  public String		p_EsIngresoAutomatico        = "N";	// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
	  public String		p_CrearComprobanteAutomatico = "N";
	  public String		p_EsCargaMasiva = "N";
	  public List<Comprobante_Entrada>	P_COMPROBANTE;
	  		
	  public CrearComprobanteNotaDeDebito_Entrada () {
		  P_COMPROBANTE	= new ArrayList<Comprobante_Entrada>();
	  }
	    
	  public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class Comprobante_Entrada {
		public Long   IdPortafolio;
		public Long   IdCliente;
		public Long   IdComprobante;
		public String IdTipoComprobante;
		public Long   IdCargaMasivaDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	//Validacion
	public static class cmbteNDE_Proceso {
		public Long IdComprobanteReferencia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	//Validacion
		public static class consulta_comprobantes {
			
			public Long IdComprobanteReferencia;
			
			public String toString() {
				return Utils.printJson(this, false);
			}	
		}
		public static class consulta_custodia {
			
			public Long IdPortafolio;
			public Long IdInstrumento;
			public String Nemotecnico;
			public Long CustodiaDisponible;
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
	public static class CrearComprobanteNotaDeDebito_Salida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		public List<Movimiento_Salida>				P_OUT_MOVIMIENTO;
		
		public CrearComprobanteNotaDeDebito_Salida (){
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
	public static class ComprobanteNotaDeDebito_Proceso {
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
		Gson gson = Utils.getGsonBuilder(); 
		CrearComprobanteNotaDeDebito_Entrada in = gson.fromJson(jin.toString(), CrearComprobanteNotaDeDebito_Entrada.class);
		CrearComprobanteNotaDeDebito_Salida salida = logica(con, in);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}	
	
	@Trace
	public CrearComprobanteNotaDeDebito_Salida logica(Connection conn, CrearComprobanteNotaDeDebito_Entrada in) throws Exception {
		log.write("INICIO::CrearComprobanteNotaDeDebito");
		CrearComprobanteNotaDeDebito_Salida salida = new CrearComprobanteNotaDeDebito_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		//Long comprobante = null;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		//Validaciones
		if (in.P_COMPROBANTE== null || in.P_COMPROBANTE.isEmpty()) { 
			System.out.println("== No encontro comprobantes ==");
			String codigo = "NO_VIENEN_COMPROBANTES";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}
		HashMap<String, Object> idcomprobante= new HashMap<>(); 
		{
			List<MovimientoComprobante_Entrada> mvtoComprobantesNC = null;
			List<MovimientoComprobanteFormaDePago_Entrada> mvtoComprobantesNCFDePago = null;
			List<MovimientoComprobanteCobro_Entrada> mvtoComprobantesNCCobros = null;
			List<consulta_comprobantes> resultadoComprobantes;
			List<consulta_comprobantes> resultadoMovimiento;
			ArrayList<Long> idsComprobante = new ArrayList<Long>();
						
			//Validacion de Previa nota de debito
			Set<Long> idsComprobantesUnicos = new HashSet<Long>();
			for (Comprobante_Entrada idCmbte : in.P_COMPROBANTE) {   
				idsComprobantesUnicos.add(idCmbte.IdComprobante);
			}
			List<Long> cmbteNDE = idsComprobantesUnicos.stream().collect(Collectors.toList()); 
			idcomprobante.put("IdComprobante", cmbteNDE.stream().findFirst().get());
			
			resultadoComprobantes = sql.query(conn, SQL_SELECT_VERIFICAR_COMPROBANTE, idcomprobante, consulta_comprobantes.class);
			resultadoMovimiento = sql.query(conn, SQL_SELECT_VERIFICAR_MOVIMIENTO, idcomprobante, consulta_comprobantes.class);
			if(!resultadoComprobantes.isEmpty() || !resultadoMovimiento.isEmpty()) { 
				log.write("VALIDACION::Ya existe nota de debito para la nota de credito");	
				String codigo = "UI_EXISTE_NOTA_DE_DEBITO";
				String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobante",idcomprobante).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
			}else {				
				if (!cmbteNDE.isEmpty() && localizacionSistema.equalsIgnoreCase("CL")) {
					HashMap<String,Object> paramsNDE = new HashMap<>();
					paramsNDE.put("idsComprobanteReferencia", cmbteNDE);
					List<cmbteNDE_Proceso> idscmbteRef = sql.query(conn, SQL_SELECT_NOTA_DEBITO_REFERENCIA, paramsNDE, cmbteNDE_Proceso.class);
					List<Long> idscmbte = idscmbteRef.stream().map(id->id.IdComprobanteReferencia).collect(Collectors.toList());
				  					
					for(Comprobante_Entrada registro: in.P_COMPROBANTE){
						if (idscmbte.contains(registro.IdComprobante)) {
							log.write("VALIDACION::No es nota de credito");
							String codigo = "UI_NO_ES_NOTA_DE_CREDITO"; 
							String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobante",registro.IdComprobante).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(registro.IdComprobante, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));							
						} else {	
							idsComprobante.add(registro.IdComprobante); 
						}
					}
				}

		
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("idsComprobante", idsComprobante);
				params.put("fechaSistema", fechaSistema); 
				params.put("CrearComprobanteAutomatico", in.p_CrearComprobanteAutomatico);
											
				mvtoComprobantesNC = sql.query(conn, SQL_SELECT_COMPROBANTESNOTADEDEBITO, params, MovimientoComprobante_Entrada.class);
				HashMap<String,Object> mvtoComprobantesNC_HMAP = new HashMap<String,Object>();
				mvtoComprobantesNC_HMAP.put("IdComprobanteReferencia", mvtoComprobantesNC);
				//Long idInstrumento = mvtoComprobantesNC.get(0).IdInstrumento;
				//String idTipoOperacion = mvtoComprobantesNC.get(0).IdTipoOperacion;
				//verificacion de entradas contra comprobantes
				for (Long idComprobanteReferencia : idsComprobante) {  
					mvtoComprobantesNC_HMAP.get(idComprobanteReferencia); 
					
					if (mvtoComprobantesNC_HMAP.get("IdComprobanteReferencia") == null) {
						String codigo = "CMBTE_ID_COMPROBANTE_REFERENCIA_NO_EXISTE";
						String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobanteReferencia", idComprobanteReferencia).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(idComprobanteReferencia, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
					}
				}
				///////////////////////////////
				
				/*
				if("VENACN".equalsIgnoreCase(idTipoOperacion)){
				HashMap<String,Object> param = new HashMap<>();
					for(Comprobante_Entrada registro: in.P_COMPROBANTE){
						param.put("IdPortafolio",registro.IdPortafolio);
					}
					param.put("IdInstrumento", idInstrumento);  //TODO buscar idnemo
					List<consulta_custodia> consultacustodia = sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, param, consulta_custodia.class);
					if(consultacustodia.isEmpty() || (consultacustodia.get(0).CustodiaDisponible <= 0)) { 
						//VALIDAMOS LAS CARTERAS
						String codigo = "CUSTODIA_NO_VIGENTE";
						String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobanteReferencia", idsComprobante).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));		
					}
				}*/		
				
				///////////////////////////////
				
				
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}
							
				//Forma de pago y cobro
				ArrayList<Long> idsMovimientoComprobanteOld = new ArrayList<Long>();
				
				List<MovimientoComprobante_Entrada> asignacionesActualizar = new ArrayList<>();
				MovimientoComprobante_Entrada asignacionActualizar;
			
				for (MovimientoComprobante_Entrada mvtoCompNC : mvtoComprobantesNC) {
					
					asignacionActualizar = new MovimientoComprobante_Entrada();
					asignacionActualizar.IdAsignacion = mvtoCompNC.IdAsignacion;
					idsMovimientoComprobanteOld.add(mvtoCompNC.NumeroMvtoCmbte);
					asignacionesActualizar.add(asignacionActualizar);
					
				}
				
				HashMap<String,Object> paramsFormPago = new HashMap<String,Object>();
				paramsFormPago.put("idsMovimientoComprobanteOld", idsMovimientoComprobanteOld);
				
				
				mvtoComprobantesNCFDePago = sql.query(conn, SQL_SELECT_COMPROBANTESNOTADEDEBITOFORMASDEPAGO, paramsFormPago, MovimientoComprobanteFormaDePago_Entrada.class);
				mvtoComprobantesNCCobros  = sql.query(conn, SQL_SELECT_COMPROBANTESNOTADEDEBITOCOBROS, paramsFormPago, MovimientoComprobanteCobro_Entrada.class);
				
				CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada  callCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
				callCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
				callCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
				callCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
				callCrearMovimientoComprobante.p_simularOperacion = "N";
				callCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
				callCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
				callCrearMovimientoComprobante.p_Movimientos = mvtoComprobantesNC;
				callCrearMovimientoComprobante.p_MovimientosFormaDePago = mvtoComprobantesNCFDePago;			
				callCrearMovimientoComprobante.p_MovimientosCobros = mvtoComprobantesNCCobros;
				callCrearMovimientoComprobante.p_EsCargaMasiva = in.p_EsCargaMasiva;
				
				CrearMovimientoComprobante mvtonc = new CrearMovimientoComprobante();
				CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resultIngMvtoNC = mvtonc.logica(conn, callCrearMovimientoComprobante);
				
				salida.P_OUT_MOVIMIENTO.addAll(formatearMovimientos(resultIngMvtoNC.p_out_Movimientos, mvtoComprobantesNC));
				salida.P_OUT_RESULTADOPROCESO.addAll(formatearResultadoProceso(resultIngMvtoNC.p_out_ResultadoProceso));
				salida.P_OUT_MENSAJES.addAll(resultIngMvtoNC.P_OUT_MENSAJES);
				
				if (!asignacionesActualizar.isEmpty()) {  //if (asignacionesActualizar.size()>0){ 
					log.write("Actualiza asignaciones");	
					sql.batch(conn, SQL_UPDATE_ASIGNACIONES, asignacionesActualizar, MovimientoComprobante_Entrada.class);	
				}
			
			}
		}	
		log.write("FIN::CrearComprobanteNotaDeDebito");	
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
	 
	 private List<Movimiento_Salida> formatearMovimientos(List<MovimientoComprobante_Salida> p_out_Movimientos, List<MovimientoComprobante_Entrada> mvtoComprobantesNC) {
		 List<Movimiento_Salida> salida = new ArrayList<>();
		 Movimiento_Salida salidaFormateada;
		 for (MovimientoComprobante_Salida elemento : p_out_Movimientos) {
			 for (MovimientoComprobante_Entrada elementoEntrada : mvtoComprobantesNC) {
				if (elementoEntrada.NumeroMvtoCmbte.equals(elemento.NumeroMvtoCmbte)) {
					salidaFormateada = new Movimiento_Salida();
					 salidaFormateada.IdComprobanteReferencia = elementoEntrada.IdComprobanteReferencia;
					 salidaFormateada.IdMovimientoComprobante = elemento.IdMovimientoComprobante+1;
					 salidaFormateada.NumeroMvtoCmbte = elemento.NumeroMvtoCmbte;
					 salida.add(salidaFormateada);
				}
			 }
		 }
		 return salida;
	 }
		
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL NOTA DE DEBITO ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_COMPROBANTE\":[{\"IdPortafolio\":100543,\"IdCliente\":100329,\"IdComprobante\":4053,\"IdTipoComprobante\":\"NCE\"}],\"p_CrearComprobanteAutomatico\":\"S\"}";
			CrearComprobanteNotaDeDebito_Entrada Objin = gson.fromJson(json, CrearComprobanteNotaDeDebito_Entrada.class);
			
			CrearComprobanteNotaDeDebito demo=new CrearComprobanteNotaDeDebito();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearComprobanteNotaDeDebito_Salida ObjOut=demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
			    er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			  con.rollback();
	    }
		System.out.println("== END ==");
		
	}
	
}

	