package cl.bcs.mesa.MRF;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.NotificarVencimientoInstrumentos;
import cl.bcs.custodia.EjecutarVencimiento.CustodiaVencer;
import cl.bcs.custodia.EjecutarVencimiento.ProcesamientoCustodia;
import cl.bcs.custodia.NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos;
import cl.bcs.mesa.MRF.VencerOperacion;
import cl.bcs.mesa.MRF.VencerOperacion.Operacion;
import cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaEntrada;
import cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EjecutarVencimientoPactos implements JavaCall{
	private static final Logger logger = LogManager.getLogger(VencerOperacion.class);
	
	private final String SQL_SELECT_CUSTODIA_VENCER = loadResourceToString("cl/bcs/mesa/sql/EjecutarVencimientoPactos_SelectCustodiaVencer.sql");
		
	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_SimularOperacion;
		public String p_IdTipoVencimiento; //PAC
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_TICKETATENCIONCUSTODIASIMULACION;
		public Long P_OUT_TICKETATENCIONCUSTODIAEJECUCION;
		public String P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ProcesamientoCustodia> P_OUT_PROCESAMIENTO;
		public List<LogEjecucion> P_OUT_LOGEJECUCION;

		public Salida() {
			this.P_OUT_MENSAJES 						= new ArrayList<MensajeSalida>();
			this.P_OUT_PROCESAMIENTO 					= new ArrayList<ProcesamientoCustodia>();
			this.P_OUT_LOGEJECUCION						= new ArrayList<LogEjecucion>();
			this.P_OUT_ERRORESCONTADOR 					= 0L;
			this.P_OUT_ADVERTENCIASCONTADOR 			= 0L;
			this.P_OUT_TICKETATENCIONCUSTODIASIMULACION = 0L;
			this.P_OUT_TICKETATENCIONCUSTODIAEJECUCION	= 0L;
			this.P_OUT_DESCRIPCION						= "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class LogEjecucion {
		public Long TimeMillis;
		public String Mensaje;
		public String EsError;
		public String EsInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * Metodo Principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		logger.info("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("*** Fin de la ejecucion ***");
		return dataout;
	}
	
	/**********************
	 * logica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		logger.info("INICIO::MRF.EjecutarVencimientoPactos");
		SQL sql = new SQL();
		Salida out = new Salida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		out.P_OUT_TICKETATENCIONCUSTODIASIMULACION = new Long(0);
		out.P_OUT_TICKETATENCIONCUSTODIAEJECUCION = new Long(0);
		
		List<CustodiaVencer> listCustodiaVencer = new ArrayList<CustodiaVencer>();
		List<ProcesamientoCustodia> procVC = new ArrayList<ProcesamientoCustodia>();
		
		out.P_OUT_PROCESAMIENTO = procVC;
		
		HashMap<String, Object> parmsCustodiaVencer = new HashMap<String, Object>();
		parmsCustodiaVencer.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsCustodiaVencer.put("Fecha", entrada.p_Fecha);
		
		listCustodiaVencer = sql.query(con, SQL_SELECT_CUSTODIA_VENCER, parmsCustodiaVencer, CustodiaVencer.class);
		
		for (CustodiaVencer item : listCustodiaVencer) {
			item.FechaLiquidacion = entrada.p_Fecha;
			procVC.add(carteraAProcesamiento(item));
		}
		
		if(listCustodiaVencer.size() > 0) {
			VencerOperacion venPac = new VencerOperacion();
			VencerOperacionMesaEntrada entradaVenPac = preparaDatosMesa("S",listCustodiaVencer,entrada);
			VencerOperacionMesaSalida salidaVenPac = venPac.logica(con,entradaVenPac);
			out.P_OUT_MENSAJES.addAll(salidaVenPac.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
				logger.info("FIN::MRF.EjecutarVencimientoPactos");
				return out;
			}
		} else {
			MensajeSalida mensaje = new MensajeSalida();
			mensaje.codigo = "";
			mensaje.descripcion = "";
			mensaje.id = 0L;
			mensaje.tipo = "";
			HashMap<String,String> mapMsg = new HashMap<String,String>();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"NO_EXISTEN_PACTOS_A_VENCER",mapMsg,con,entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			logger.info("FIN::MRF.EjecutarVencimientoPactos");
			return out;
		}
		
		
		if (out.P_OUT_ERRORESCONTADOR == 0) {
			logger.info("FIN::EjecutarVencimiento");
			try {
				NotificarVencimientoInstrumentos notificar = new NotificarVencimientoInstrumentos();
				EntradaNotificarVencimientoInstrumentos entradaNotificar = new NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos();
				List<Long> idsCarteras = out.P_OUT_PROCESAMIENTO.stream().map( vencimiento -> vencimiento.IdCartera ).collect(Collectors.toList());
				entradaNotificar.p_IdEmpresa= entrada.p_IdEmpresa;
				entradaNotificar.p_IdModulo= entrada.p_IdModulo;
				entradaNotificar.p_IdUsuario = entrada.p_IdUsuario;
				entradaNotificar.vencimientos = idsCarteras; 
				if (entradaNotificar.vencimientos != null && !entradaNotificar.vencimientos.isEmpty()) {
					notificar.logica(con, entradaNotificar);
				}
			} catch (Exception e) {
				logger.info(e);
			}
			logger.info("FIN::EjecutarVencimiento");
		}
		
		logger.info("FIN::MRF.EjecutarVencimientoPactos");
		return out;
	}
	
	private VencerOperacionMesaEntrada preparaDatosMesa(String simular,List<CustodiaVencer> lCV, Entrada entrada){//, String fechaSistemaAnterior){
		VencerOperacionMesaEntrada paramInSPRetiro = new VencerOperacionMesaEntrada();
		paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
		paramInSPRetiro.p_IdModulo			= entrada.p_IdModulo;
		paramInSPRetiro.p_IdUsuario			= entrada.p_IdUsuario;
		paramInSPRetiro.p_UsaReglasNegocio	= "S";
		paramInSPRetiro.p_FechaVencimiento	= entrada.p_Fecha;//fechaSistemaAnterior;
		//public List<Operacion> 		p_Operacion;
		
		List<Operacion> p_RetiroCartera = new ArrayList<Operacion>();
		for(CustodiaVencer item : lCV){
			Operacion ObjEntrada = new Operacion();
			ObjEntrada.IdInternoOperacion = new Long(lCV.indexOf(item));
			ObjEntrada.IdOperacion = item.IdOperacion;
			ObjEntrada.IdEstadoSiguiente = "LIQ";
			ObjEntrada.MontoOperacion=item.INGMontoCompraActualMonedaInstrumento;
			ObjEntrada.IdCondicionLiquidacion=item.IdCondicionLiquidacion;
			p_RetiroCartera.add(ObjEntrada);
		}
		paramInSPRetiro.p_Operacion = p_RetiroCartera;
		return paramInSPRetiro;
	}
	
	public ProcesamientoCustodia carteraAProcesamiento(CustodiaVencer item) {
		ProcesamientoCustodia aux = new ProcesamientoCustodia();
		aux.IdCartera = item.IdCartera;
		aux.FolioCartera = item.FolioCartera;
		aux.Nemotecnico = item.Nemotecnico;
		aux.EsPagaderoEn = item.EsPagaderoEn;
		aux.INGMontoAPlazo = item.INGMontoAPlazo;
		aux.INGMontoATasaCompraActual = item.INGMontoATasaCompraActual;
		aux.INGMontoCompraActualMonedaInstrumento = item.INGMontoCompraActualMonedaInstrumento;
		aux.IdClasificacionTipoInstrumento = item.IdClasificacionTipoInstrumento;
		aux.IdTipoInstrumento = item.IdTipoInstrumento;
		aux.IdInstrumento = item.IdInstrumento;
		aux.IdPortafolio = item.IdPortafolio;
		aux.IdMonedaInstrumento = item.IdMonedaInstrumento;
		aux.IdTipoProducto = item.IdTipoProducto;
		aux.IdTipoOperacion = item.IdTipoOperacion;
		aux.OperaContraCuentaInversion = item.OperaContraCuentaInversion;
		aux.IdPersona = item.IdPersona;
		aux.IdCliente = item.IdCliente;
		aux.CobraPrimaAlInicio = item.CobraPrimaAlInicio;
		// sacados de sp simultanea antiguo
		aux.PrecioAPlazo = item.PrecioAPlazo;
		aux.MontoAPlazo = item.MontoAPlazo;
		aux.FechaComprobante = item.FechaComprobante;
		aux.FechaLiquidacion = item.FechaLiquidacion;
		aux.IdOperacion = item.IdOperacion;
		aux.IdMonedaSistema = item.IdMonedaSistema;
		aux.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
		aux.INGCantidad = item.INGCantidad;
		aux.INGValorRescate = item.INGValorRescate;
		aux.ValorRescate = item.ValorRescate;
		aux.INGTasaCompra = item.INGTasaCompra;
		aux.PrecioCompra = item.PrecioCompra;
		aux.INGPrecioCompra = item.INGPrecioCompra;		
		aux.AccionOperacion = item.AccionOperacion; // lo usa la simultanea		
		aux.IdTipoComprobante = item.IdTipoComprobante; // lo usa simultanea
		aux.FolioComprobante = item.FolioComprobante; // lo usa simultanea
		
		return aux;
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPREBCOESTADOConnection();
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":288,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoVencimiento\":\"PAC\",\"p_SimularOperacion\":\"N\",\"p_Fecha\":\"2022-06-01\",\"p_IdPais\":44}";
			Entrada Objin = gson.fromJson(json, Entrada.class);
			
			EjecutarVencimientoPactos ven = new EjecutarVencimientoPactos();
			Salida ObjOut = ven.logica(conn, Objin);
			
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			System.out.println(ObjOut.toString());
			
			conn.rollback();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	}
}