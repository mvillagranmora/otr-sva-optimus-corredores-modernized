package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.IngresoDatatec.DatosDatatecObjOut;
import cl.bcs.mesa.SPT.IngresoDatatec.OperacionSpotDatatec;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CrearAnularOperacionSpotDatatec implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearAnularOperacionSpotDatatec.class);
	
	public static class CrearAnularOperacionSpotDatatecEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Proceso;
		public String p_Accion;
		
		public Long p_IdMesa;
		public String p_TipoComprobanteFacturacion;
		public Long p_IdAgente;
		public String p_IdMedioPago;
		
		public List<OperacionSpotDatatec> p_DatosOperacionSpot;
		
		public CrearAnularOperacionSpotDatatecEntrada() {
			this.p_DatosOperacionSpot = new ArrayList<OperacionSpotDatatec>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearAnularOperacionSpotDatatecSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public List<DatosDatatecObjOut>	P_OUT_DATOSSALIDASPOT;
		
		public CrearAnularOperacionSpotDatatecSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_DATOSSALIDASPOT = new ArrayList<DatosDatatecObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearAnularOperacionSpotDatatecEntrada in = gson.fromJson(jin.toString(), CrearAnularOperacionSpotDatatecEntrada.class);
		CrearAnularOperacionSpotDatatecSalida out = logica(con, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearAnularOperacionSpotDatatecSalida logica(Connection conn, CrearAnularOperacionSpotDatatecEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CrearAnularOperacionSpotDatatec");
		
		CrearAnularOperacionSpotDatatecSalida out = new CrearAnularOperacionSpotDatatecSalida();
		
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			if (in.p_DatosOperacionSpot.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			/***** CREAR OPERACIONES SPOT *****/
			if ("CREAR".equals(in.p_Accion)) {
				ArrayList<OperacionObjIn> operacionesSpot = new ArrayList<>();
				
				for(OperacionSpotDatatec ope: in.p_DatosOperacionSpot) {
					OperacionObjIn operacionSpot = new OperacionObjIn();
					operacionSpot.NumeroOperacion = ope.IdUnicoDatatec.toString();
					operacionSpot.IdInstrumento = ope.IdInstrumento;
					operacionSpot.IdTipoOperacion = ope.IdTipoOperacion;
					operacionSpot.IdTipoProducto = "MEME";
					operacionSpot.IdCliente = ope.IdCliente; 
					operacionSpot.IdPortafolio = ope.IdPortafolio;
					operacionSpot.FechaOperacion = "INGRESO".equals(in.p_Proceso) ? ope.FechaOperacion : ope.FechaReproceso;
					operacionSpot.IdMonedaPrincipal = ope.IdMonedaPrincipal;	
					operacionSpot.IdMonedaSecundaria = ope.IdMonedaSecundaria;
					operacionSpot.MontoMonedaPrincipal = ope.MontoMonedaPrincipal;
					operacionSpot.MontoMonedaSecundaria = ope.MontoMonedaSecundaria;
					operacionSpot.TipoCambioCierre = ope.TipoCambioCierre;
					operacionSpot.TipoCambioCierreForward = BigDecimal.ZERO;
					operacionSpot.ParidadCierre = BigDecimal.ZERO;
					operacionSpot.ParidadCierreForward = BigDecimal.ZERO;
					operacionSpot.TipoCambioCosto = ope.TipoCambioCierre;
					operacionSpot.ParidadCosto = BigDecimal.ZERO;
					operacionSpot.MontoEquivalente = ope.MontoMonedaSecundaria;
					operacionSpot.MontoRentabilidad = BigDecimal.ZERO;
					operacionSpot.MontoRentabilidadCanal = BigDecimal.ZERO;
					operacionSpot.Margen = BigDecimal.ZERO;
					operacionSpot.Utilidad = BigDecimal.ZERO;
					operacionSpot.PuntaCompra = ope.PuntaCompra;
					operacionSpot.PuntaVenta = ope.PuntaVenta;
					operacionSpot.SolicitudTipoCambio = "N";
					operacionSpot.IdMesa = in.p_IdMesa;
					operacionSpot.IdModulo = "SPT";
					operacionSpot.IdTipoDolar = 1L;
					operacionSpot.TipoComprobanteFacturacion = in.p_TipoComprobanteFacturacion;
					operacionSpot.IdAgente = in.p_IdAgente;
					operacionSpot.BenOur = "BEN";
					operacionSpot.EquivalenteConGasto = BigDecimal.ZERO;
					operacionSpot.TipoCambioCierreConGasto = BigDecimal.ZERO;
					operacionSpot.Observacion = "Archivo Datatec";
					operacionSpot.CodigoComercio = 10000L;
					operacionSpot.FolioPlanilla = 0L;
					operacionSpot.FolioInterfaz = "";
					operacionSpot.OperacionFicticia = "N";
					operacionSpot.IdTipoCanalIngreso = "DAT";
					operacionSpot.IdDatatec = ope.IdDatatec;
					operacionSpot.Estado = "VIG";
					
					operacionesSpot.add(operacionSpot);
				}
				
				//Formas de Pago Operacion Spot
				List<FormaPagoObjIn> datosFormaPagoSpot = new ArrayList<FormaPagoObjIn>();
				long j = 0;
				
				for(OperacionObjIn ope: operacionesSpot) {
					//Forma De Pago Entregamos
					FormaPagoObjIn fpIn = new FormaPagoObjIn();
					fpIn.IdInternoPagoOperacion = j;
					fpIn.IdComprobantePagoOperacion = 0L;
					fpIn.IdOperacion = Long.parseLong(ope.NumeroOperacion);
					fpIn.RecibePagaPortafolio = "P";
					if(ope.IdTipoOperacion.equals("COMSPT")){
						fpIn.IdTipoMovimiento = "MOVING";
						fpIn.IdMoneda = ope.IdMonedaSecundaria;
						//fpIn.IdFormaDePago = ope.IdTipoOperacion+"_"+idMedioPago+"_"+fpIn.IdMoneda+"_0";
						fpIn.IdFormaDePago = "VENSPT" + "_" + in.p_IdMedioPago.replace("MONEDA", fpIn.IdMoneda);//parametro idMedioPago es de la forma: IDMEDIODEPAGO_MONEDA_VALUTA
						fpIn.MontoFormaDePago = ope.MontoMonedaSecundaria;
					}
					else{
						fpIn.IdTipoMovimiento = "MOVEGR";
						fpIn.IdMoneda = ope.IdMonedaPrincipal;
						fpIn.IdFormaDePago = ope.IdTipoOperacion + "_" + in.p_IdMedioPago.replace("MONEDA", fpIn.IdMoneda);//parametro idMedioPago es de la forma: IDMEDIODEPAGO_MONEDA_VALUTA
						fpIn.MontoFormaDePago = ope.MontoMonedaPrincipal;
					}
					fpIn.IdMonedaLiquidacion = fpIn.IdMoneda;
					fpIn.AfectarTesoreria = fpIn.IdFormaDePago.contains("_CIN_") ? "N" : "S";
					datosFormaPagoSpot.add(fpIn);
					j++;
					
					//Forma De Pago Recibimos
					fpIn = new FormaPagoObjIn();
					fpIn.IdInternoPagoOperacion = j;
					fpIn.IdComprobantePagoOperacion = 0L;
					fpIn.IdOperacion = Long.parseLong(ope.NumeroOperacion);
					fpIn.RecibePagaPortafolio = "R";
					if(ope.IdTipoOperacion.equals("COMSPT")){
						fpIn.IdTipoMovimiento = "MOVEGR";
						fpIn.IdMoneda = ope.IdMonedaPrincipal;
						fpIn.IdFormaDePago = ope.IdTipoOperacion + "_" + in.p_IdMedioPago.replace("MONEDA", fpIn.IdMoneda);//parametro idMedioPago es de la forma: IDMEDIODEPAGO_MONEDA_VALUTA
						fpIn.MontoFormaDePago = ope.MontoMonedaPrincipal;
					}
					else{
						fpIn.IdTipoMovimiento = "MOVING";
						fpIn.IdMoneda = ope.IdMonedaSecundaria;
						fpIn.IdFormaDePago = "COMSPT" + "_" + in.p_IdMedioPago.replace("MONEDA", fpIn.IdMoneda);//parametro idMedioPago es de la forma: IDMEDIODEPAGO_MONEDA_VALUTA
						fpIn.MontoFormaDePago = ope.MontoMonedaSecundaria;
					}
					fpIn.IdMonedaLiquidacion = fpIn.IdMoneda;
					fpIn.AfectarTesoreria = fpIn.IdFormaDePago.contains("_CIN_") ? "N" : "S";
					datosFormaPagoSpot.add(fpIn);
					j++;
				}
				
				CrearOperacionSpot crearOperacionSpot = new CrearOperacionSpot();
				CrearOperacionSpot.CrearOperacionSpotEntrada crearOperacionSpotEntrada = new CrearOperacionSpot.CrearOperacionSpotEntrada();
				crearOperacionSpotEntrada.p_IdEmpresa = in.p_IdEmpresa;
				crearOperacionSpotEntrada.p_IdUsuario = in.p_IdUsuario;
				crearOperacionSpotEntrada.p_IdModulo = in.p_IdModulo;
				crearOperacionSpotEntrada.p_CalculaMargen = "N";
				crearOperacionSpotEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearOperacionSpotEntrada.p_DatosOperacion = operacionesSpot;
				crearOperacionSpotEntrada.p_DatosFormaPago = datosFormaPagoSpot;
				CrearOperacionSpot.CrearOperacionSpotSalida crearOperacionSpotSalida = crearOperacionSpot.logica(conn, crearOperacionSpotEntrada);
				
				//out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + crearOperacionSpotSalida.P_OUT_ERRORESCONTADOR;
				//out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ADVERTENCIASCONTADOR + crearOperacionSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
				//out.P_OUT_MENSAJES.addAll(crearOperacionSpotSalida.P_OUT_MENSAJES);
				
				//Procesa la salida del servicio creacion spot
				for(OperacionSpotDatatec ope: in.p_DatosOperacionSpot) {
					String idEstado = "";
					String glosaIng = "";
					String glosaErr = "";
					String glosaAdv = "";
					String glosaAut = "";
					
					DatosDatatecObjOut datosOk = new DatosDatatecObjOut();
					datosOk.IdUnicoDatatec = ope.IdUnicoDatatec;
					datosOk.NumeroRegistro = ope.NumeroRegistro;
					datosOk.IdDatatec = ope.IdDatatec;
					datosOk.IdOperacionSpot = 0L;
					
					//Operaciones Spot Ingresadas VIG y PAU
					for(CrearOperacionSpot.OperacionObjOut opSpSalida: crearOperacionSpotSalida.P_OUT_IDOPERACIONSPOT){
						if(ope.IdUnicoDatatec.equals(opSpSalida.IdInterno)){
							datosOk.IdOperacionSpot = opSpSalida.IdOperacionSpot;
							idEstado = "ING";
							glosaIng = "INGRESADO: Operacion SPOT " + datosOk.IdOperacionSpot + ".";
						}
					}
					
					//Operaciones Spot Error y Autorizacion
					for(MensajeSalida msjSalida: crearOperacionSpotSalida.P_OUT_MENSAJES){
						if(ope.IdUnicoDatatec.equals(msjSalida.id.toString())){
							if(msjSalida.tipo.equals("ERROR")) {
								idEstado = datosOk.IdOperacionSpot.longValue() != 0 ? idEstado : "ERR";
								glosaErr = glosaErr.length() > 0 ? glosaErr + ", " + msjSalida.descripcion : msjSalida.descripcion;
							}
							else if(msjSalida.tipo.equals("AUTORIZACION")) {
								idEstado = datosOk.IdOperacionSpot.longValue() != 0 ? idEstado : "ERR".equals(idEstado) ? idEstado : "PEN";
								glosaAut = glosaAut.length() > 0 ? glosaAut + ", " + msjSalida.descripcion : msjSalida.descripcion;
							}
							else if(msjSalida.tipo.equals("ADVERTENCIA")) {
								idEstado = datosOk.IdOperacionSpot.longValue() != 0 ? idEstado : "ERR".equals(idEstado) ? idEstado : "PEN";
								glosaAdv = glosaAdv.length() > 0 ? glosaAdv + ", " + msjSalida.descripcion : msjSalida.descripcion;
							}
						}
					}
					
					glosaErr = glosaErr.length() > 0 ? " ERRORES SPOT: "+glosaErr+". "+ "CLIENTE: "+ope.NombreCliente+ ". " : glosaErr;
					glosaAut = glosaAut.length() > 0 ? " AUTORIZACIONES SPOT: "+glosaAut+". " : glosaAut;
					glosaAdv = glosaAdv.length() > 0 ? " ADVERTENCIAS SPOT: "+glosaAdv+". " : glosaAdv;
					
					datosOk.IdEstadoRegistro = idEstado;
					datosOk.GlosaProceso = glosaIng + glosaErr + glosaAut + glosaAdv;
					out.P_OUT_DATOSSALIDASPOT.add(datosOk);
				}
			}
			else if ("ANULAR".equals(in.p_Accion)) {
				ArrayList<OperacionSpot> operacionesSpotAnu = new ArrayList<>();
				
				for(OperacionSpotDatatec ope: in.p_DatosOperacionSpot) {
					if (ope.IdOperacionSpot.longValue() != 0L) {
						OperacionSpot operacionSpot = new OperacionSpot();
						operacionSpot.NumeroOperacion = ope.IdOperacionSpot.toString();
						operacionesSpotAnu.add(operacionSpot);
					}
					else {
						ope.IdEstadoRegistro = "ERR";
						//ope.GlosaProceso = ope.GlosaProceso.length() > 0 ? ope.GlosaProceso+". ANULACION: SIN_OPERACION_SPOT_ASOCIADA" : ope.GlosaProceso;
						ope.GlosaProceso = " ANULACION: SIN_OPERACION_SPOT_ASOCIADA";
					}
				}
				
				AnularOperacionSpot anularOperacionSpot = new AnularOperacionSpot();
				AnularOperacionSpot.AnularOperacionSpotEntrada anularOperacionSpotEntrada = new AnularOperacionSpot.AnularOperacionSpotEntrada();
				anularOperacionSpotEntrada.p_IdEmpresa = in.p_IdEmpresa;
				anularOperacionSpotEntrada.p_IdUsuario = in.p_IdUsuario;
				anularOperacionSpotEntrada.p_IdModulo = in.p_IdModulo;
				anularOperacionSpotEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				anularOperacionSpotEntrada.p_DatosOperacion = operacionesSpotAnu;
				AnularOperacionSpot.AnularOperacionSpotSalida anularOperacionSpotSalida = anularOperacionSpot.logica(conn, anularOperacionSpotEntrada);
				
				//Procesa la salida del servicio Anulacion Spot
				for(OperacionSpotDatatec ope: in.p_DatosOperacionSpot) {
					String glosaErr = "";
					
					DatosDatatecObjOut datosOk = new DatosDatatecObjOut();
					datosOk.IdUnicoDatatec = ope.IdUnicoDatatec;
					datosOk.NumeroRegistro = ope.NumeroRegistro;
					datosOk.IdDatatec = ope.IdDatatec;
					datosOk.IdOperacionSpot = ope.IdOperacionSpot;
					datosOk.IdEstadoRegistro = ope.IdEstadoRegistro;
					datosOk.GlosaProceso = ope.GlosaProceso;
					
					//Operaciones Spot Error Anulacion
					for(MensajeSalida msjSalida: anularOperacionSpotSalida.P_OUT_MENSAJES){
						if(ope.IdOperacionSpot.equals(msjSalida.id)){
							if(msjSalida.tipo.equals("ERROR")) {
								datosOk.IdEstadoRegistro = "ERR";
								glosaErr = glosaErr.length() > 0 ? glosaErr + ", " + msjSalida.codigo : msjSalida.codigo;
							}
						}
					}
					
					datosOk.GlosaProceso = glosaErr.length() > 0 ? ". ERRORES ANULACION: "+glosaErr+". " : datosOk.GlosaProceso;
					out.P_OUT_DATOSSALIDASPOT.add(datosOk);
				}
			}
		}
		catch(Exception e){
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Termino del servicio CrearAnularOperacionSpotDatatec, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MSP\",\"p_CalculaMargen\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":1,\"IdInstrumento\":346390,\"IdTipoOperacion\":\"COMSPT\",\"IdTipoProducto\":\"MEME\",\"IdCliente\":130826,\"IdPortafolio\":34993,\"FechaOperacion\":\"2018-10-19\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoMonedaPrincipal\":1000,\"MontoMonedaSecundaria\":654000,\"TipoCambioCierre\":654,\"TipoCambioCierreForward\":0,\"ParidadCierre\":0,\"ParidadCierreForward\":0,\"TipoCambioCosto\":598.1,\"ParidadCosto\":0,\"MontoEquivalente\":654000,\"MontoRentabilidad\":0,\"MontoRentabilidadCanal\":0,\"Margen\":-55899.99999999998,\"Utilidad\":-55899.99999999998,\"PuntaCompra\":599,\"PuntaVenta\":600,\"SolicitudTipoCambio\":\"N\",\"IdMesa\":6,\"IdModulo\":\"SPT\",\"IdTipoDolar\":1,\"TipoComprobanteFacturacion\":\"FEE\",\"IdAgente\":1335561,\"BenOur\":\"BEN\",\"EquivalenteConGasto\":0,\"TipoCambioCierreConGasto\":0,\"Observacion\":\"\",\"CodigoComercio\":\"10005\",\"FolioPlanilla\":0,\"FolioInterfaz\":\"\",\"OperacionFicticia\":\"N\",\"IdTipoCanalIngreso\":\"MES\",\"IdDatatec\":0,\"Estado\":\"VIG\",\"IdSolicitudAporteRescate\":0}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENSPT_CIN_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":654000,\"AfectarTesoreria\":\"N\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMSPT_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"N\"}]}";
			CrearAnularOperacionSpotDatatecEntrada Objin = gson.fromJson(json, CrearAnularOperacionSpotDatatecEntrada.class);
			
			CrearAnularOperacionSpotDatatec demo = new CrearAnularOperacionSpotDatatec();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearAnularOperacionSpotDatatecSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.rollback();
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}