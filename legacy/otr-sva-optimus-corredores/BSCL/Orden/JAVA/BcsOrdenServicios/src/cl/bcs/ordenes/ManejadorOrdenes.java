package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenes.ObjCobrosModificados;
import cl.bcs.ordenes.CrearOrdenes.ObjGarantiasIn;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.StatsD;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ManejadorOrdenes implements JavaCall{
	
	public static final String METRIC_PREFIX = "ordenes.manejador_ordenes";
	public static final String METRIC_TIEMPO_EJECUCION = METRIC_PREFIX + "." + "tiempo_ejecucion";
	public static final String METRIC_ORDENES_INGRESADAS = METRIC_PREFIX + "." + "ordenes_ingresadas";
	public int CantidadOrdenesIngresadas = 0;

	
	private final Log log = new Log("Ordenes.ManejadorOrdenes");
	
	public static class EntradaManejadorOrdenes{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public List<ObjGarantiasIn>			p_Garantias;
		public List<ObjCobrosModificados>	p_CobrosModificados;
		public List<Cobro_Fijo>				p_CobrosFijos;
		public List<ObjOrdenesEntrada>		p_Ordenes;
		
		public EntradaManejadorOrdenes() {
			p_Garantias = new ArrayList<ObjGarantiasIn>();
			p_CobrosModificados = new ArrayList<ObjCobrosModificados>();
			p_Ordenes = new ArrayList<ObjOrdenesEntrada>();
			p_CobrosFijos = new ArrayList<Cobro_Fijo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaManejadorOrdenes{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		//public List<ResultadoOrdenes> P_OUT_RESULTADO; this.P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
		public List<ObjRESULTADO> P_OUT_RESULTADO;
		
		public SalidaManejadorOrdenes() {
			P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaManejadorOrdenes jin = gson.fromJson(in.toString(), EntradaManejadorOrdenes.class);
		SalidaManejadorOrdenes out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public SalidaManejadorOrdenes logica (Connection con, EntradaManejadorOrdenes entrada) throws Exception {
		long inicio = System.currentTimeMillis();
		log.write("INICIO ManejadorOrdenes");
		SalidaManejadorOrdenes out = new SalidaManejadorOrdenes();
		
		try {
			
			if(entrada.p_Ordenes.size() > 0) {
				CrearOrdenes servicio = new CrearOrdenes();
				CrearOrdenesEntrada in = new CrearOrdenesEntrada();
				in.p_IdEmpresa = entrada.p_IdEmpresa;
				in.p_IdUsuario = entrada.p_IdUsuario;
				in.p_IdModulo = entrada.p_IdModulo;
				in.p_simularOperacion = "S";
				in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
				in.p_Ordenes = entrada.p_Ordenes;
				in.p_CobrosFijos = entrada.p_CobrosFijos;
				in.p_CobrosModificados = entrada.p_CobrosModificados;
				in.p_Garantias = entrada.p_Garantias;
				CrearOrdenesSalida salida = servicio.logica(con, in);
				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
				out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
				HashMap<Long,Object> resultado = Utils.List2Hash(salida.P_OUT_RESULTADO, "IdInterno");
				List<CrearOrdenes.ObjOrdenesEntrada> ordenesOK = in.p_Ordenes.parallelStream().filter(orden->resultado.get(orden.IdInterno) != null).collect(Collectors.toList());
				CantidadOrdenesIngresadas = ordenesOK.size();
				List<CrearOrdenes.ObjCobrosModificados> cobrosOK = in.p_CobrosModificados.parallelStream().filter(cobro->resultado.get(cobro.IdInterno) != null).collect(Collectors.toList());
				
				if(ordenesOK.size() > 0 && "N".equalsIgnoreCase(entrada.p_simularOperacion)) {
					
					out.P_OUT_RESULTADO.clear();
					out.P_OUT_MENSAJES.clear();
					in.p_Ordenes = ordenesOK;
					in.p_CobrosModificados = cobrosOK;
					in.p_simularOperacion = entrada.p_simularOperacion;
					salida = servicio.logica(con, in);
					out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
					out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
					
				}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			StatsD.reportCount(METRIC_ORDENES_INGRESADAS, (CantidadOrdenesIngresadas));
			StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_EJECUCION, (System.currentTimeMillis() - inicio));
			return out;
			
		} catch (Exception error) {
			log.write(error);
			throw error;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("-- TEST --");
		try(Connection con = PlataformaUtils.getPropertiesConnection("desa2")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdInterno\":0,\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2019-06-13\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoVigencia\":\"SH\",\"Glosa\":\"\",\"Cantidad\":100,\"MontoNeto\":0,\"ValorRescate\":0,\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"IdPorcentajeVisibilidad\":1,\"PrecioLimite\":8357,\"PrecioMercado\":8357,\"EsFacturaInmediata\":\"N\",\"IdTipoPrecio\":\"M\",\"IdPortafolio\":27499,\"IdTipoCanalIngreso\":\"AGE\",\"IdTipoReajuste\":\"\",\"IdEmisor\":\"LAN\",\"IdBolsa\":\"BCS\",\"EspecificaBolsa\":\"S\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"TipoCambio\":null,\"IdInstrumento\":1368,\"IdInstrumentoSubyacente\":0,\"IdCondicionDeLiquidacionPlazo\":\"\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"EsVentaCorta\":\"N\",\"TasaSimultanea\":0,\"PlazoSimultanea\":0,\"PlazoIF\":0,\"TasaRFIF\":0,\"EsOD\":\"N\",\"EsFungible\":\"N\",\"MontoPlazo\":0,\"IdTipoLibroOrden\":\"NORMAL\",\"IdLibroOrdenes\":null,\"IdCuentaCustodioPortafolio\":null,\"EsPorMonto\":\"N\",\"IdCorredorCorresponsal\":null,\"IdPersonaAutorizadoDarOrdenes\":1750193,\"IdTipoCanalDistribucion\":\"MAN\",\"EsOrdenEspecial\":\"N\",\"PagoCobroCuentaInversion\":\"S\",\"BaseTasa\":0,\"TipoInteres\":\"\",\"EsClienteFinal\":\"N\",\"IdMecanismoCaptacion\":\"OE\",\"FechaConfirmacionPlazo\":\"0001-01-01\"},{\"IdInterno\":1,\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2019-06-13\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoVigencia\":\"SH\",\"Glosa\":\"\",\"Cantidad\":0,\"MontoNeto\":0,\"ValorRescate\":0,\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"IdPorcentajeVisibilidad\":1,\"PrecioLimite\":8357,\"PrecioMercado\":8357,\"EsFacturaInmediata\":\"N\",\"IdTipoPrecio\":\"M\",\"IdPortafolio\":27499,\"IdTipoCanalIngreso\":\"AGE\",\"IdTipoReajuste\":\"\",\"IdEmisor\":\"LAN\",\"IdBolsa\":\"BCS\",\"EspecificaBolsa\":\"S\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"TipoCambio\":null,\"IdInstrumento\":1368,\"IdInstrumentoSubyacente\":0,\"IdCondicionDeLiquidacionPlazo\":\"\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"EsVentaCorta\":\"N\",\"TasaSimultanea\":0,\"PlazoSimultanea\":0,\"PlazoIF\":0,\"TasaRFIF\":0,\"EsOD\":\"N\",\"EsFungible\":\"N\",\"MontoPlazo\":0,\"IdTipoLibroOrden\":\"NORMAL\",\"IdLibroOrdenes\":null,\"IdCuentaCustodioPortafolio\":null,\"EsPorMonto\":\"N\",\"IdCorredorCorresponsal\":null,\"IdPersonaAutorizadoDarOrdenes\":1750193,\"IdTipoCanalDistribucion\":\"MAN\",\"EsOrdenEspecial\":\"N\",\"PagoCobroCuentaInversion\":\"S\",\"BaseTasa\":0,\"TipoInteres\":\"\",\"EsClienteFinal\":\"N\",\"IdMecanismoCaptacion\":\"OE\",\"FechaConfirmacionPlazo\":\"0001-01-01\"},{\"IdInterno\":2,\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2019-06-13\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"IdCondicionDeLiquidacion\":\"T3\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoVigencia\":\"SH\",\"Glosa\":\"\",\"Cantidad\":100,\"MontoNeto\":0,\"ValorRescate\":0,\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"IdPorcentajeVisibilidad\":1,\"PrecioLimite\":6357,\"PrecioMercado\":8357,\"EsFacturaInmediata\":\"N\",\"IdTipoPrecio\":\"M\",\"IdPortafolio\":27499,\"IdTipoCanalIngreso\":\"AGE\",\"IdTipoReajuste\":\"\",\"IdEmisor\":\"LAN\",\"IdBolsa\":\"BCS\",\"EspecificaBolsa\":\"S\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"TipoCambio\":null,\"IdInstrumento\":1368,\"IdInstrumentoSubyacente\":0,\"IdCondicionDeLiquidacionPlazo\":\"\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"EsVentaCorta\":\"N\",\"TasaSimultanea\":0,\"PlazoSimultanea\":0,\"PlazoIF\":0,\"TasaRFIF\":0,\"EsOD\":\"N\",\"EsFungible\":\"N\",\"MontoPlazo\":0,\"IdTipoLibroOrden\":\"NORMAL\",\"IdLibroOrdenes\":null,\"IdCuentaCustodioPortafolio\":null,\"EsPorMonto\":\"N\",\"IdCorredorCorresponsal\":null,\"IdPersonaAutorizadoDarOrdenes\":1750193,\"IdTipoCanalDistribucion\":\"MAN\",\"EsOrdenEspecial\":\"N\",\"PagoCobroCuentaInversion\":\"S\",\"BaseTasa\":0,\"TipoInteres\":\"\",\"EsClienteFinal\":\"N\",\"IdMecanismoCaptacion\":\"OE\",\"FechaConfirmacionPlazo\":\"0001-01-01\"}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"COAI\",\"ValorCobroAplicado\":0.7},{\"IdInterno\":0,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":0.5}]}";
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdInterno\":0,\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2019-06-13\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoVigencia\":\"SH\",\"Glosa\":\"\",\"Cantidad\":100,\"MontoNeto\":0,\"ValorRescate\":0,\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"IdPorcentajeVisibilidad\":1,\"PrecioLimite\":8357,\"PrecioMercado\":8357,\"EsFacturaInmediata\":\"N\",\"IdTipoPrecio\":\"M\",\"IdPortafolio\":27499,\"IdTipoCanalIngreso\":\"AGE\",\"IdTipoReajuste\":\"\",\"IdEmisor\":\"LAN\",\"IdBolsa\":\"BCS\",\"EspecificaBolsa\":\"S\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"TipoCambio\":null,\"IdInstrumento\":1368,\"IdInstrumentoSubyacente\":0,\"IdCondicionDeLiquidacionPlazo\":\"\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"EsVentaCorta\":\"N\",\"TasaSimultanea\":0,\"PlazoSimultanea\":0,\"PlazoIF\":0,\"TasaRFIF\":0,\"EsOD\":\"N\",\"EsFungible\":\"N\",\"MontoPlazo\":0,\"IdTipoLibroOrden\":\"NORMAL\",\"IdLibroOrdenes\":null,\"IdCuentaCustodioPortafolio\":null,\"EsPorMonto\":\"N\",\"IdCorredorCorresponsal\":null,\"IdPersonaAutorizadoDarOrdenes\":1750193,\"IdTipoCanalDistribucion\":\"MAN\",\"EsOrdenEspecial\":\"N\",\"PagoCobroCuentaInversion\":\"S\",\"BaseTasa\":0,\"TipoInteres\":\"\",\"EsClienteFinal\":\"N\",\"IdMecanismoCaptacion\":\"OE\",\"FechaConfirmacionPlazo\":\"0001-01-01\"}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"COAI\",\"ValorCobroAplicado\":0.7},{\"IdInterno\":0,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":0.5}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Ordenes\":[{\"IdInterno\":0,\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2019-06-13\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoVigencia\":\"SH\",\"Glosa\":\"\",\"Cantidad\":100,\"MontoNeto\":0,\"ValorRescate\":0,\"IdBoveda\":\"DCV\",\"IdFormaCustodia\":\"CUS\",\"IdPorcentajeVisibilidad\":1,\"PrecioLimite\":8357,\"PrecioMercado\":0,\"EsFacturaInmediata\":\"N\",\"IdTipoPrecio\":\"M\",\"IdPortafolio\":27499,\"IdTipoCanalIngreso\":\"AGE\",\"IdTipoReajuste\":\"\",\"IdEmisor\":\"LAN\",\"IdBolsa\":\"BCS\",\"EspecificaBolsa\":\"S\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"TipoCambio\":null,\"IdInstrumento\":1368,\"IdInstrumentoSubyacente\":0,\"IdCondicionDeLiquidacionPlazo\":\"\",\"TasaVentaCorta\":0,\"PlazoVentaCorta\":0,\"EsVentaCorta\":\"N\",\"TasaSimultanea\":0,\"PlazoSimultanea\":0,\"PlazoIF\":0,\"TasaRFIF\":0,\"EsOD\":\"N\",\"EsFungible\":\"N\",\"MontoPlazo\":0,\"IdTipoLibroOrden\":\"NORMAL\",\"IdLibroOrdenes\":null,\"IdCuentaCustodioPortafolio\":null,\"EsPorMonto\":\"N\",\"IdCorredorCorresponsal\":null,\"IdPersonaAutorizadoDarOrdenes\":1750193,\"IdTipoCanalDistribucion\":\"MAN\",\"EsOrdenEspecial\":\"N\",\"PagoCobroCuentaInversion\":\"S\",\"BaseTasa\":0,\"TipoInteres\":\"\",\"EsClienteFinal\":\"N\",\"IdMecanismoCaptacion\":\"OE\",\"FechaConfirmacionPlazo\":\"0001-01-01\"}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"COAI\",\"ValorCobroAplicado\":0.7},{\"IdInterno\":0,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":0.5}]}";
			
			
			EntradaManejadorOrdenes Objin = gson.fromJson(json, EntradaManejadorOrdenes.class);
			ManejadorOrdenes demo = new ManejadorOrdenes();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				SalidaManejadorOrdenes ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
			}catch(Exception er) {
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
	
}