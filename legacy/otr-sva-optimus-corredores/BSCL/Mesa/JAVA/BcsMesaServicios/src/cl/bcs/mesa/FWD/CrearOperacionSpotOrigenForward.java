package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.FWD.ReversaAnticipoForward.DatosCarteraReversaForward;
import cl.bcs.mesa.SPT.CrearOperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionSpotOrigenForward implements JavaCall{
	
	private final Log log = new Log("FWD.CrearOperacionSpotOrigenForward");
	
	private final String SQL_INSERT_OPERACIONES_SPOT_ORIGEN_FORWARD_WRAPPER = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_InsertOperacionesSpotOrigenForwardWrapper.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_CUENTA_INVERSION_RECIBE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteCuentaInversionRecibe.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_CUENTA_INVERSION_PAGA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteCuentaInversionPaga.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_RECIBE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteRecibe.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_PAGA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClientePaga.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_NO_POR_DEFECTO_RECIBE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteNoPorDefectoRecibe.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_CLIENTE_NO_POR_DEFECTO_PAGA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoClienteNoPorDefectoPaga.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_MESA_POR_DEFECTO_RECIBE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPorDefectoRecibe.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_MESA_POR_DEFECTO_PAGA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPorDefectoPaga.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_MESA_RECIBE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaRecibe.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_MESA_PAGA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoMesaPaga.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionSpotOrigenForward_ConsultarFormaDePagoOperacionForward.sql");
	
	private long i = 0;
	private long cantFaltaFormaDePagoRecibe = 0;
	private long cantFaltaFormaDePagoPaga = 0;
	private List<FormaPagoObjIn> datosFormaPagoSpot = new ArrayList<FormaPagoObjIn>();
	
	
	public static class SpotOrigenForwardObjIn{
		public Long IdCartera;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdEstado;
		public String EGRIdTipoOperacion;
		public java.sql.Date EGRFechaOperacion;
		public java.sql.Date FechaVencimientoOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public BigDecimal EGRMontoMonedaPrincipal;
		public BigDecimal EGRMontoMonedaSecundaria;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal ValorParidadCierre;
		public BigDecimal ValorTipoCambioCosto;
		public BigDecimal ValorParidadCosto;
		public Long IdMesa;
		public Long IdAgente;
		public String INGGlosa;
		public Long FolioCartera;
		public boolean FaltaFormaDePagoRecibe = true;
		public boolean FaltaFormaDePagoPaga = true;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionSpotOrigenForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<SpotOrigenForwardObjIn> p_datosForward;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SpotOrigenForwardObjOut{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdInternoSpot;
		public Long IdOperacionSpot;
		public String IdDatatec;
		public String EstadoSpot;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionSpotOrigenForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<SpotOrigenForwardObjOut>	P_OUT_SPOTORIGENFORWARD;
		
		public CrearOperacionSpotOrigenForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SPOTORIGENFORWARD = new ArrayList<SpotOrigenForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoSpotOrigenForward{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdFormaDePago;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String EGRIdTipoOperacion;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public BigDecimal EGRMontoMonedaPrincipal;
		public BigDecimal EGRMontoMonedaSecundaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaDePagoOperacionForward{
		public Long IdComprobantePagoOperacion;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String IdFormaDePago;
		public String RecibePaga;
		public String IdMoneda;
		public BigDecimal Monto;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria = 0L;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionSpotOrigenForwardEntrada in = gson.fromJson(jin.toString(), CrearOperacionSpotOrigenForwardEntrada.class);
		CrearOperacionSpotOrigenForwardSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearOperacionSpotOrigenForwardSalida logica(Connection conn, CrearOperacionSpotOrigenForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		CrearOperacionSpotOrigenForwardSalida out = new CrearOperacionSpotOrigenForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: FWD.CrearOperacionSpotOrigenForward");
		
		try {
			if (in.p_datosForward.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			/***** VARIABLES *****/
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			String horaSistemaSecondate = fechaHoraSistemaSecondate.split(" ")[1];
			
			long cantOpeIn = 0;
			
			HashMap<String,Object> params = new HashMap<>();
			List<OperacionObjIn> datosOperacionSpot = new ArrayList<OperacionObjIn>();
			
			List<SpotOrigenForwardObjIn> datosProceso = new ArrayList<SpotOrigenForwardObjIn>();
			
			List<Long> idCartera = new ArrayList<Long>();
			List<Long> idPortafolio = new ArrayList<Long>();
			
			
			/***** DATOS PARA EL INGRESO OPERACION SPOT *****/
			for(SpotOrigenForwardObjIn spotObjIn: in.p_datosForward){
				idCartera.add(spotObjIn.IdCartera);
				idPortafolio.add(spotObjIn.IdPortafolio);
				
				String fechaOperacion = "";
				if(spotObjIn.IdEstado.equals("ANT") || (spotObjIn.IdEstado.equals("VCD") && spotObjIn.EGRIdTipoOperacion.equals("ANTFWD"))){
					fechaOperacion = Fecha.toStringDate(spotObjIn.EGRFechaOperacion);
				}
				else{
					fechaOperacion = Fecha.toStringDate(spotObjIn.FechaVencimientoOperacion);
				} 
				
				OperacionObjIn opSpIn = new OperacionObjIn();
				opSpIn.NumeroOperacion = spotObjIn.IdCartera.toString();
				opSpIn.IdInstrumento = spotObjIn.IdInstrumento;
				opSpIn.IdTipoOperacion = spotObjIn.IdTipoOperacion.equals("COMFWD") ? "COMSPT" : "VENSPT";
				opSpIn.IdTipoProducto = "MEME";
				opSpIn.IdCliente = spotObjIn.IdCliente; 
				opSpIn.IdPortafolio = spotObjIn.IdPortafolio;
				opSpIn.FechaOperacion = Fecha.toTimeStamp(fechaOperacion+" "+horaSistemaSecondate);
				opSpIn.IdMonedaPrincipal = spotObjIn.IdMonedaPrincipal;	
				opSpIn.IdMonedaSecundaria = spotObjIn.IdMonedaSecundaria;
				opSpIn.MontoMonedaPrincipal = spotObjIn.EGRIdTipoOperacion.equals("ANTFWD") ? spotObjIn.EGRMontoMonedaPrincipal : spotObjIn.INGMontoMonedaPrincipal;
				opSpIn.MontoMonedaSecundaria = spotObjIn.EGRIdTipoOperacion.equals("ANTFWD") ? spotObjIn.EGRMontoMonedaSecundaria: spotObjIn.INGMontoMonedaSecundaria;
				opSpIn.TipoCambioCierre = spotObjIn.ValorTipoCambioCierre;
				opSpIn.TipoCambioCierreForward = spotObjIn.ValorTipoCambioCierre;
				opSpIn.ParidadCierre = spotObjIn.ValorParidadCierre;
				opSpIn.ParidadCierreForward = spotObjIn.ValorParidadCierre;
				opSpIn.TipoCambioCosto = spotObjIn.ValorTipoCambioCosto;
				opSpIn.ParidadCosto = spotObjIn.ValorParidadCosto;
				opSpIn.MontoEquivalente = spotObjIn.EGRIdTipoOperacion.equals("ANTFWD") ? spotObjIn.EGRMontoMonedaSecundaria: spotObjIn.INGMontoMonedaSecundaria;
				opSpIn.MontoRentabilidad = BigDecimal.ZERO;
				opSpIn.MontoRentabilidadCanal = BigDecimal.ZERO;
				opSpIn.Margen = BigDecimal.ZERO;
				opSpIn.Utilidad = BigDecimal.ZERO;
				opSpIn.PuntaCompra = BigDecimal.ZERO;
				opSpIn.PuntaVenta = BigDecimal.ZERO;
				opSpIn.SolicitudTipoCambio = "N";
				opSpIn.IdMesa = spotObjIn.IdMesa;
				opSpIn.IdModulo = "SPT";
				opSpIn.IdTipoDolar = 4L;
				opSpIn.TipoComprobanteFacturacion = "FEE";
				opSpIn.IdAgente = spotObjIn.IdAgente;
				opSpIn.BenOur = "BEN";
				opSpIn.EquivalenteConGasto = BigDecimal.ZERO;
				opSpIn.TipoCambioCierreConGasto = BigDecimal.ZERO;
				opSpIn.Observacion = "Spot Origen Forward Folio "+spotObjIn.FolioCartera;
				opSpIn.CodigoComercio = 300206L;
				opSpIn.FolioPlanilla = 0L;
				opSpIn.FolioInterfaz = "0";
				opSpIn.OperacionFicticia = "N";
				opSpIn.IdTipoCanalIngreso = "FWD";
				opSpIn.IdDatatec = "0";
				opSpIn.Estado = "VIG";
				datosOperacionSpot.add(opSpIn);
				
				SpotOrigenForwardObjIn spotProceso = new SpotOrigenForwardObjIn();
				spotProceso.IdCartera = spotObjIn.IdCartera;
				spotProceso.IdInstrumento = spotObjIn.IdInstrumento;
				spotProceso.IdTipoOperacion = opSpIn.IdTipoOperacion;
				spotProceso.IdCliente = spotObjIn.IdCliente;
				spotProceso.IdPortafolio = spotObjIn.IdPortafolio;
				spotProceso.IdEstado = opSpIn.Estado;
				spotProceso.EGRIdTipoOperacion = spotObjIn.EGRIdTipoOperacion;
				spotProceso.EGRFechaOperacion = Fecha.toDate(fechaOperacion);
				spotProceso.FechaVencimientoOperacion = spotObjIn.FechaVencimientoOperacion;
				spotProceso.IdMonedaPrincipal = spotObjIn.IdMonedaPrincipal;
				spotProceso.IdMonedaSecundaria = spotObjIn.IdMonedaSecundaria;
				spotProceso.INGMontoMonedaPrincipal = spotObjIn.INGMontoMonedaPrincipal;
				spotProceso.INGMontoMonedaSecundaria = spotObjIn.INGMontoMonedaSecundaria;
				spotProceso.EGRMontoMonedaPrincipal = spotObjIn.EGRMontoMonedaPrincipal;
				spotProceso.EGRMontoMonedaSecundaria = spotObjIn.EGRMontoMonedaSecundaria;
				spotProceso.ValorTipoCambioCierre = spotObjIn.ValorTipoCambioCierre;
				spotProceso.ValorParidadCierre = spotObjIn.ValorParidadCierre;
				spotProceso.ValorTipoCambioCosto = spotObjIn.ValorTipoCambioCosto;
				spotProceso.ValorParidadCosto = spotObjIn.ValorParidadCosto;
				spotProceso.IdMesa = spotObjIn.IdMesa;
				spotProceso.IdAgente = spotObjIn.IdAgente;
				spotProceso.INGGlosa = "Spot Origen Forward Folio "+spotObjIn.FolioCartera;
				spotProceso.FolioCartera = spotObjIn.FolioCartera;
				datosProceso.add(spotProceso);
				
				cantOpeIn++;
			}
			
			//Lo siguiente se comenta ya que las formas de pago de la operacion spot seran las mismas de la operacion forward
			
			//Insert tabla global
			/*log.write("INSERT en tabla BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso :: "+datosProceso);
			_sql.batch(conn, SQL_INSERT_OPERACIONES_SPOT_ORIGEN_FORWARD_WRAPPER, datosProceso, SpotOrigenForwardObjIn.class);
			
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCartera", idCartera);
			params.put("IdPortafolio", idPortafolio);
			
			
			/***** FORMAS DE PAGO PARA EL INGRESO OPERACION SPOT *****/
			/*List<FormaDePagoSpotOrigenForward> formaDePago = new ArrayList<FormaDePagoSpotOrigenForward>();
			if(cantOpeIn > cantFaltaFormaDePagoRecibe){
				// 1) Se obtienen Formas de Pago cuando Cliente-Portafolio es Cuenta Inversion
				// Recibe
				formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_CUENTA_INVERSION_RECIBE, params, FormaDePagoSpotOrigenForward.class);
				procesaFormaDePago(formaDePago, datosProceso, "RECIBE", false);
				
				if(cantOpeIn > cantFaltaFormaDePagoRecibe){
					// 2) Se obtienen Formas de Pago Por Defecto de Cliente-Portafolio
					// Recibe
					formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_RECIBE, params, FormaDePagoSpotOrigenForward.class);
					procesaFormaDePago(formaDePago, datosProceso, "RECIBE", true);
					
					if(cantOpeIn > cantFaltaFormaDePagoRecibe){
						// 3) Se obtienen Formas de Pago de Cliente-Portafolio (NO Por Defecto)
						// Recibe
						formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_NO_POR_DEFECTO_RECIBE, params, FormaDePagoSpotOrigenForward.class);
						procesaFormaDePago(formaDePago, datosProceso, "RECIBE", true);
						
						if(cantOpeIn > cantFaltaFormaDePagoRecibe){
							// 4) Se obtienen Formas de Pago Por Defecto de Mesa
							// Recibe
							formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_MESA_POR_DEFECTO_RECIBE, params, FormaDePagoSpotOrigenForward.class);
							procesaFormaDePago(formaDePago, datosProceso, "RECIBE", true);
							
							if(cantOpeIn > cantFaltaFormaDePagoRecibe){
								// 5) Se obtienen Formas de Pago de Mesa
								// Recibe
								formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_MESA_RECIBE, params, FormaDePagoSpotOrigenForward.class);
								procesaFormaDePago(formaDePago, datosProceso, "RECIBE", true);
							}
						}
					}
				}
			}
			
			if(cantOpeIn > cantFaltaFormaDePagoPaga){
				// 1) Se obtienen Formas de Pago cuando Cliente-Portafolio es Cuenta Inversion
				// Paga
				formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_CUENTA_INVERSION_PAGA, params, FormaDePagoSpotOrigenForward.class);
				procesaFormaDePago(formaDePago, datosProceso, "PAGA", false);
				
				if(cantOpeIn > cantFaltaFormaDePagoPaga){
					// 2) Se obtienen Formas de Pago Por Defecto de Cliente-Portafolio
					// Paga
					formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_PAGA, params, FormaDePagoSpotOrigenForward.class);
					procesaFormaDePago(formaDePago, datosProceso, "PAGA", true);
					
					if(cantOpeIn > cantFaltaFormaDePagoPaga){
						// 3) Se obtienen Formas de Pago de Cliente-Portafolio (NO Por Defecto)
						// Paga
						formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_CLIENTE_NO_POR_DEFECTO_PAGA, params, FormaDePagoSpotOrigenForward.class);
						procesaFormaDePago(formaDePago, datosProceso, "PAGA", true);
						
						if(cantOpeIn > cantFaltaFormaDePagoPaga){
							// 4) Se obtienen Formas de Pago Por Defecto de Mesa
							// Paga
							formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_MESA_POR_DEFECTO_PAGA, params, FormaDePagoSpotOrigenForward.class);
							procesaFormaDePago(formaDePago, datosProceso, "PAGA", true);
							
							if(cantOpeIn > cantFaltaFormaDePagoPaga){
								// 5) Se obtienen Formas de Pago de Mesa
								// Paga
								formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_MESA_PAGA, params, FormaDePagoSpotOrigenForward.class);
								procesaFormaDePago(formaDePago, datosProceso, "PAGA", true);
							}
						}
					}
				}
			}*/
			
			
			//Obtiene formas de pago de la operacion forward
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCartera", idCartera);
			List<FormaDePagoOperacionForward> formaDePagoOperacionForward = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_OPERACION_FORWARD, params, FormaDePagoOperacionForward.class);
			
			for(FormaDePagoOperacionForward fpof: formaDePagoOperacionForward){
				FormaPagoObjIn fpIn = new FormaPagoObjIn();
				fpIn.IdInternoPagoOperacion = i;
				fpIn.IdComprobantePagoOperacion = 0L;
				if(fpof.IdTipoOperacion.equals("COMFWD")){
					fpIn.IdTipoMovimiento = fpof.RecibePaga.equals("R") ? "MOVEGR" : "MOVING";
				}
				else{
					fpIn.IdTipoMovimiento = fpof.RecibePaga.equals("R") ? "MOVING" : "MOVEGR";
				}
				fpIn.IdFormaDePago = fpof.IdFormaDePago.replace("FWD", "SPT");
				fpIn.IdMoneda = fpof.IdMoneda;
				fpIn.IdMonedaLiquidacion = fpof.IdMoneda;
				fpIn.IdOperacion = fpof.IdOperacion;
				fpIn.RecibePagaPortafolio = fpof.RecibePaga;
				fpIn.MontoFormaDePago = fpof.Monto;
				fpIn.AfectarTesoreria = fpof.AfectarTesoreria;
				fpIn.IdCuentaBancaria = fpof.IdCuentaBancaria;
				datosFormaPagoSpot.add(fpIn);
			}
			
			CrearOperacionSpot crearOperacionSpot = new CrearOperacionSpot();
			CrearOperacionSpotEntrada crearOperacionSpotEntrada = new CrearOperacionSpotEntrada();
			crearOperacionSpotEntrada.p_IdEmpresa = in.p_IdEmpresa;
			crearOperacionSpotEntrada.p_IdUsuario = in.p_IdUsuario;
			crearOperacionSpotEntrada.p_IdModulo = in.p_IdModulo;
			crearOperacionSpotEntrada.p_CalculaMargen = "N";
			crearOperacionSpotEntrada.p_UsaReglasNegocio = "N";
			crearOperacionSpotEntrada.p_DatosOperacion = datosOperacionSpot;
			crearOperacionSpotEntrada.p_DatosFormaPago = datosFormaPagoSpot;
			CrearOperacionSpotSalida crearOperacionSpotSalida = crearOperacionSpot.logica(conn, crearOperacionSpotEntrada);
			
			//Procesa Salida del Ingreso Custodia
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + crearOperacionSpotSalida.P_OUT_ERRORESCONTADOR;
			out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ADVERTENCIASCONTADOR + crearOperacionSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
			out.P_OUT_MENSAJES.addAll(crearOperacionSpotSalida.P_OUT_MENSAJES);
			for(SpotOrigenForwardObjIn spotObjIn: in.p_datosForward){
				for(CrearOperacionSpot.OperacionObjOut opSpSalida: crearOperacionSpotSalida.P_OUT_IDOPERACIONSPOT){
					if(spotObjIn.IdCartera.toString().equals(opSpSalida.IdInterno)){
						SpotOrigenForwardObjOut spotObjOut = new SpotOrigenForwardObjOut();
						spotObjOut.IdCartera = spotObjIn.IdCartera;
						spotObjOut.FolioCartera = spotObjIn.FolioCartera;
						spotObjOut.IdInternoSpot = Long.valueOf(opSpSalida.IdInterno);
						spotObjOut.IdOperacionSpot = opSpSalida.IdOperacionSpot;
						spotObjOut.IdDatatec = opSpSalida.IdDatatec;
						spotObjOut.EstadoSpot = opSpSalida.Estado;
						out.P_OUT_SPOTORIGENFORWARD.add(spotObjOut);
					}
				}
			}
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	public void procesaFormaDePago(List<FormaDePagoSpotOrigenForward> formaDePago, List<SpotOrigenForwardObjIn> datosProceso, String recibePaga, boolean afectaTesoreria){
		for(FormaDePagoSpotOrigenForward forPag: formaDePago){
			for(SpotOrigenForwardObjIn sptOri: datosProceso){
				if(sptOri.IdCartera.longValue() == forPag.IdCartera.longValue() && (sptOri.FaltaFormaDePagoRecibe || sptOri.FaltaFormaDePagoPaga)){
					if(recibePaga.equals("RECIBE")){
						sptOri.FaltaFormaDePagoRecibe = false;
						cantFaltaFormaDePagoRecibe++;
					}
					else if(recibePaga.equals("PAGA")){
						sptOri.FaltaFormaDePagoPaga = false;
						cantFaltaFormaDePagoPaga++;
					}
					
					FormaPagoObjIn fpIn = new FormaPagoObjIn();
					fpIn.IdInternoPagoOperacion = i;
					fpIn.IdComprobantePagoOperacion = 0L;
					fpIn.IdFormaDePago = forPag.IdFormaDePago;
					fpIn.IdOperacion = forPag.IdCartera;
					fpIn.AfectarTesoreria = afectaTesoreria ? (forPag.IdFormaDePago.contains("_CIN_") ? "N" : "S") : "N";
					if(forPag.IdTipoOperacion.equals("COMSPT")){
						if(recibePaga.equals("RECIBE")){
							fpIn.IdTipoMovimiento = "MOVEGR";
							fpIn.IdMoneda = forPag.IdMonedaPrincipal;
							fpIn.RecibePagaPortafolio = "R";
							fpIn.MontoFormaDePago = forPag.EGRIdTipoOperacion.equals("ANTFWD") ? forPag.EGRMontoMonedaPrincipal : forPag.INGMontoMonedaPrincipal;
						}
						else if(recibePaga.equals("PAGA")){
							fpIn.IdTipoMovimiento = "MOVING";
							fpIn.IdMoneda = forPag.IdMonedaSecundaria;
							fpIn.RecibePagaPortafolio = "P";
							fpIn.MontoFormaDePago = forPag.EGRIdTipoOperacion.equals("ANTFWD") ? forPag.EGRMontoMonedaSecundaria: forPag.INGMontoMonedaSecundaria;
						}
					}
					else{
						if(recibePaga.equals("RECIBE")){
							fpIn.IdTipoMovimiento = "MOVING";
							fpIn.IdMoneda = forPag.IdMonedaSecundaria;
							fpIn.RecibePagaPortafolio = "P";
							fpIn.MontoFormaDePago = forPag.EGRIdTipoOperacion.equals("ANTFWD") ? forPag.EGRMontoMonedaSecundaria : forPag.INGMontoMonedaSecundaria;
						}
						else if(recibePaga.equals("PAGA")){
							fpIn.IdTipoMovimiento = "MOVEGR";
							fpIn.IdMoneda = forPag.IdMonedaPrincipal;
							fpIn.RecibePagaPortafolio = "R";
							fpIn.MontoFormaDePago = forPag.EGRIdTipoOperacion.equals("ANTFWD") ? forPag.EGRMontoMonedaPrincipal: forPag.INGMontoMonedaPrincipal;
						}
					}
					fpIn.IdMonedaLiquidacion = fpIn.IdMoneda;
					datosFormaPagoSpot.add(fpIn);
					i++;
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUATConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MFW\",\"p_datosForward\":[{\"IdCartera\":1539,\"IdInstrumento\":133257,\"IdTipoOperacion\":\"COMFWD\",\"IdCliente\":100024,\"IdPortafolio\":100039,\"IdEstado\":\"ANT\",\"EGRIdTipoOperacion\":\"ANTFWD\",\"EGRFechaOperacion\":\"2018-07-09 00:00:00.000\",\"FechaVencimientoOperacion\":\"2018-07-10 00:00:00.000\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"INGMontoMonedaPrincipal\":1000.00000000,\"INGMontoMonedaSecundaria\":624000.00000000,\"EGRMontoMonedaPrincipal\":1000.00000000,\"EGRMontoMonedaSecundaria\":624000.00000000,\"ValorTipoCambioCierre\":624.00000000,\"ValorParidadCierre\":0E-8,\"ValorTipoCambioCosto\":620.99000000,\"ValorParidadCosto\":0E-8,\"IdMesa\":2,\"IdAgente\":20637,\"INGGlosa\":\"\",\"FolioCartera\":49521,\"FaltaFormaDePagoRecibe\":true,\"FaltaFormaDePagoPaga\":true}]}";
			CrearOperacionSpotOrigenForwardEntrada Objin = gson.fromJson(jsonDesa, CrearOperacionSpotOrigenForwardEntrada.class);
			
			CrearOperacionSpotOrigenForward demo = new CrearOperacionSpotOrigenForward();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearOperacionSpotOrigenForwardSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}