package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjCobrosModificados;
import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;


import cl.bcs.ordenes.CrearOrdenes.ObjCobrosModificados;
import cl.bcs.ordenes.CrearOrdenes.Cobro_Fijo;


import cl.bcs.ordenes.ProcesarCargaMasiva.ResultadoProceso;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearOrdenesMasiva implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearOrdenesMasiva.class);
	
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/CrearordenesMasiva_Select_Instumentros.sql");	
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");	
	private final String SQL_UPDATE_CANTAR_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenesMasiva_UpdateCantarOrden.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
			
	private static final String COMPRANACIONAL = "COMACN";
	private static final String VENTANACIONAL = "VENACN";
	private static final String COMPRAEXTRANJERA = "COMACX";
	private static final String VENTAEXTRANJERA = "VENACX";
	
	private static final String COMPRACFI = "COMCFI"; // COMPRA CFI NACIONAL
	private static final String VENTACFI = "VENCFI"; // VENTA CFI NACIONAL
	private static final String COMPRACFX = "COMCFX"; // COMPRA CFI EXTRANJERO 
	private static final String VENTACFX = "VENCFX"; // VENTA CFI EXTRANJERO 
	
	private static final String COMPRAOSA = "COMOSA";  // COMPRA OSA
	private static final String VENTAOSA = "VENOSA";  // COMPRA OSA

	private static final String COMPRAETN = "COMETN";  // COMPRA ETF NACIONAL
	private static final String VENTAETN = "VENETN";  // VENTA ETF NACIONAL 
	private static final String COMPRAETX = "COMETX";  // COMPRA ETF EXTRANJERO
	private static final String VENTAETX = "VENETX";  // VENTA ETF EXTRANJERO
	
	private static final String COMPRA = "C";
	private static final String NACIONAL = "ACN";
	private static final String EXTRANJERA = "ACX";
	private static final String CFI = "CFI";
	private static final String CFX = "CFX";
	private static final String ETN = "ETN";
	private static final String ETX = "ETX";


	
	// ----------------------------------------------------------
	// CLASES DE ENTRADA
	// ----------------------------------------------------------
	public static class 	EntradaCrearOrdenesMasiva {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
		public String 		p_UsaReglasNegocio;		
		public List<DetalleOrden> p_DetalleOrden;

		public EntradaCrearOrdenesMasiva() {
			p_DetalleOrden = new ArrayList<DetalleOrden>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	
	
	public static class DetalleOrden {
		public Long         IdInterno;
		public String 		DocumentoIdentidad;
		public String   	CodigoPortafolio;
		public String 		TipoOperacion;
		public String 		IdMecanismoCaptacion;
		public BigDecimal 	Cantidad;
		public String 		Nemotecnico;
		public BigDecimal 	Precio;
		public String 		IdCondicionDeLiquidacion;
		public String 		IdTipoVigenciaOrden;
		public String 		FechaVigencia;
		public String 		IdFormaCustodia;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public String 		IdTipoPrecioOrden;
		public Long         IdPersona;
		public Long         IdPortafolio;
		
		public String 		IdTipoCanalDistribucion;
	
		public String 		Negociacion; 			// Indica si la orden debe quedar Ofertada o Ingresada (OFE: ofertada  ING: ingresasa (por cantar))
		public String 		FechaOperacion;
		public String 		UsarPlantillaCobros;    // S  N
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	
	// ----------------------------------------------------------
	// CLASES DE SALIDA
	// ----------------------------------------------------------

	
	public static class SalidaCrearOrdenesMasiva {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;		
		public List<ObjRESULTADO>	P_OUT_RESULTADO;

		public SalidaCrearOrdenesMasiva() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<ObjRESULTADO>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	
	
	// ----------------------------------------------------------
	// CLASES COMPLEMENTARIAS
	// ----------------------------------------------------------
	public static class Instrumento {
		public Long 		IdInstrumento;
		public String 		Nemotecnico;
		public String 		IdEmisor;
		public String 		IdMoneda;
		public String 		IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Orden {
		public Long 		IdOrden;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------------------
	// CALL
	// ----------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearOrdenesMasiva in = gson.fromJson(jin.toString(), EntradaCrearOrdenesMasiva.class);
		SalidaCrearOrdenesMasiva out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	
	
	
	
	// ----------------------------------------------------------
	// LOGICA
	// ----------------------------------------------------------
	
	@Trace
	public SalidaCrearOrdenesMasiva logica(Connection con, EntradaCrearOrdenesMasiva in) throws Exception {
		logger.info("INICIO :: CrearOrdenesMasiva");
		
		SQL sql = new SQL();
		SalidaCrearOrdenesMasiva out = new SalidaCrearOrdenesMasiva();
		HashMap<String, Object> param = new HashMap<>();
		List<Cobro_Fijo>	lstCobrosFijos = new ArrayList<Cobro_Fijo>();
		String cantarOrdenes = "N";
	
		ArrayList<String> nemotecnicos = new ArrayList<String>();
		ArrayList<Long> idsPortafolio  = new ArrayList<>();
		
		
		Long localOffset = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");		
		Fecha.setLocalOffSet(localOffset.intValue());
		
		
		for (DetalleOrden orden : in.p_DetalleOrden)
		{
			if (!nemotecnicos.contains(orden.Nemotecnico))			
				nemotecnicos.add(orden.Nemotecnico);
			
			if (!idsPortafolio.contains(orden.IdPortafolio))	
				idsPortafolio.add(orden.IdPortafolio);
			
			if (orden.Negociacion.equalsIgnoreCase("OFE"))
			{
				cantarOrdenes = "S";
			}
			

			           
			
		}
		
		// -----------------------------------------------------------------
		// INDICADORES PORTAFOLIO
		HashMap<Long, IndicadoresPortafolio> indicadoresPortafolio = new HashMap<>();
		{
			HashMap<String,Object> paramsInIndicadorPortafolio = new HashMap<>();
			
			paramsInIndicadorPortafolio.put("idsPortafolio", idsPortafolio);
			List<IndicadoresPortafolio> _indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsInIndicadorPortafolio, IndicadoresPortafolio.class);
			HashMap<Object, Object>__indicadoresPortafolio = Utils.List2Hash(_indicadoresPortafolio, "IdPortafolio");
			for(Object key: __indicadoresPortafolio.keySet()) {
				indicadoresPortafolio.put((Long)key,(IndicadoresPortafolio) __indicadoresPortafolio.get(key));
			}
		}
		

		
		
		// -----------------------------------------------------------------
		// OBTIENE DATOS DE INSTRUMENTOS
		
		
		for (DetalleOrden orden : in.p_DetalleOrden)
		{
			if (!nemotecnicos.contains(orden.Nemotecnico))			
				nemotecnicos.add(orden.Nemotecnico);			
		}
		
		param.clear();		
		param.put("Nemotecnicos", nemotecnicos);
		List<Instrumento> lstInstrumento = sql.query(con,SQL_SELECT_INSTRUMENTO , param, Instrumento.class);		
		HashMap<String, Object> hashInstrumento = Utils.List2Hash(lstInstrumento, "Nemotecnico");
		

		
		List<ObjOrdenesEntrada> lstOrdenes = new ArrayList<>();
		ObjOrdenesEntrada orden;
		


		
		
		
		for (DetalleOrden res: in.p_DetalleOrden) {
			orden = new ObjOrdenesEntrada();				
			Instrumento infoInstrumento =  (Instrumento) hashInstrumento.get(res.Nemotecnico);				
	
			orden.IdInterno  					=  res.IdInterno;				
			orden.FechaRecepcion 				= "0001-01-01 00:00:00";
			orden.FechaVigencia 				= res.FechaVigencia;
			orden.FechaEmision 					= "0001-01-01";
			

						
			String fechaOperacion = "0001-01-01";			
			if (res.FechaOperacion != null && !res.FechaOperacion.equalsIgnoreCase("0001-01-01"))
			{
				java.sql.Timestamp date1 = Fecha.toDateTime(res.FechaOperacion);
				fechaOperacion = Fecha.toStringDatetime(Fecha.toUTCTimeStamp(date1));
			}			
			orden.FechaOperacion 				= fechaOperacion;

	
			orden.IdTipoVigencia 				= res.IdTipoVigenciaOrden;				
			orden.Glosa 						= _c.GLOSA_CARGA_MASIVA;
			orden.Cantidad 						= res.Cantidad;
			orden.MontoNeto 					= BigDecimal.ZERO;
			orden.ValorRescate 					= BigDecimal.ZERO;
			orden.IdBoveda 						= res.IdBoveda;				
			orden.IdFormaCustodia     			= res.IdFormaCustodia;				
			orden.IdPorcentajeVisibilidad 		= 1L;
			orden.PrecioLimite 					= res.Precio;
			orden.EsFacturaInmediata 			= "N";				
			orden.IdTipoPrecio 					= res.IdTipoPrecioOrden;	
			orden.IdPortafolio 					= res.IdPortafolio;
			orden.IdTipoLibroOrden 				= "NORMAL";
			orden.IdLibroOrdenes 				= null;
			orden.IdTipoCanalIngreso 			= "AGE";
			orden.IdTipoReajuste 				= "";
			orden.IdBolsa 						= res.IdBolsa;
			orden.EspecificaBolsa 				= "S";
			orden.IdMonedaOrden 				= " "; // solo para IF
			orden.TipoCambio 					= null;
			orden.IdInstrumento 				= infoInstrumento.IdInstrumento;
			orden.IdEmisor 						= infoInstrumento.IdEmisor;
			orden.IdMonedaLiquidacionOrden 		= infoInstrumento.IdMoneda;
				

			if (res.TipoOperacion.equalsIgnoreCase(COMPRA)) {
				if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(NACIONAL))) {
					orden.IdTipoOperacion = COMPRANACIONAL;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(EXTRANJERA))){
					orden.IdTipoOperacion = COMPRAEXTRANJERA;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(CFI))){
					orden.IdTipoOperacion = COMPRACFI;						
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(CFX))){
					orden.IdTipoOperacion = COMPRACFX;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(ETN))){
					orden.IdTipoOperacion = COMPRAETN;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(ETN))){
					orden.IdTipoOperacion = COMPRAETX;
				}else {
					orden.IdTipoOperacion = COMPRAOSA;
				}
			} else {
				if(infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(NACIONAL)) {
					orden.IdTipoOperacion = VENTANACIONAL;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(EXTRANJERA))){
					orden.IdTipoOperacion = VENTAEXTRANJERA;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(CFI))){
					orden.IdTipoOperacion = VENTACFI;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(CFX))){
					orden.IdTipoOperacion = VENTACFX;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(ETN))){
					orden.IdTipoOperacion = VENTAETN;
				}else if((infoInstrumento.IdTipoInstrumento.equalsIgnoreCase(ETX))){
					orden.IdTipoOperacion = VENTAETX;
				}else {
					orden.IdTipoOperacion = VENTAOSA;
				}
			}

				
			orden.IdInstrumentoSubyacente 		= 0L;
			orden.IdCondicionDeLiquidacionPlazo = "";
			orden.TasaVentaCorta 				= BigDecimal.ZERO;
			orden.TasaSimultanea 				= BigDecimal.ZERO;
			orden.TasaRFIF 						= BigDecimal.ZERO;
			orden.PlazoIF 						= 0L;
			orden.PlazoSimultanea 				= 0L;
			orden.PlazoVentaCorta 				= 0L;
			orden.EsVentaCorta 					= "N";         
			orden.EsOD 							= "N";
			orden.EsFungible 					= "N";
			orden.IdCorredorCorresponsal 		= null;
			orden.IdPersonaAutorizadoDarOrdenes = null;
			orden.IdTipoCanalDistribucion 		= res.IdTipoCanalDistribucion;
			orden.TasaReporte 					= BigDecimal.ZERO;
			orden.PlazoReporte 					= 0L;
			orden.BaseTasa 						= 0L; 
			orden.TipoInteres 					= ""; //C o S (Compuesto Simple) 
			orden.IdCondicionDeLiquidacion 		= res.IdCondicionDeLiquidacion;				
			orden.IdCuentaCustodioPortafolio 	= null;
			orden.EsPorMonto 					= "N";
			orden.EsOrdenEspecial 				= "N";
			orden.PagoCobroCuentaInversion 		= indicadoresPortafolio.get(res.IdPortafolio).OperaCuentaInversion; // S N
			orden.IdPreferenciaPagoPortafolio 	= 0L;
			orden.IdMecanismoCaptacion 			= res.IdMecanismoCaptacion;
			orden.TasaPromedioReporte 			= BigDecimal.ZERO;
			orden.TasaReportanteReferencia 		= BigDecimal.ZERO;
			orden.NemotecnicoSVS 				= "";
			orden.IdSistemaBursatil 			= "MANUAL";
			orden.InstrumentoAcreditado 		= "N";
			orden.FormadorMercado 				= "N";
			orden.EsClienteFinal 				= "N";
			orden.MontoInteresContado 			= BigDecimal.ZERO;
			orden.MontoInteresPlazo 			= BigDecimal.ZERO;
			orden.PorcentajeAllIn 				= BigDecimal.ZERO;
			orden.FechaConfirmacionPlazo 		= "0001-01-01";

			orden.IdOrdenReferencia 			= null;

				
			lstOrdenes.add(orden);
			
			if (res.UsarPlantillaCobros.equalsIgnoreCase("N"))
			{	//Sino usa plantilla cobros crea un cobro en cero
				Cobro_Fijo	objCobro_Fijo = new Cobro_Fijo();
				
				objCobro_Fijo.IdInterno 			= res.IdInterno; 
				objCobro_Fijo.IdInternoCobro 		= res.IdInterno;
				objCobro_Fijo.IdPortafolio 			= res.IdPortafolio; 
				objCobro_Fijo.IdBolsa 				= res.IdBolsa;
				objCobro_Fijo.IdTipoProducto 		= "";
				objCobro_Fijo.IdTipoOperacion 		= orden.IdTipoOperacion;
				objCobro_Fijo.IdTipoCanalIngreso 	= "AGE";
				objCobro_Fijo.IdCobro 				= "COVA";
				objCobro_Fijo.ValorCobro 			= BigDecimal.ZERO; 
				objCobro_Fijo.FormaCobro 			= "V"; 
				objCobro_Fijo.ValorTopeMinimoOrden 	= BigDecimal.ZERO; 
				objCobro_Fijo.IdMoneda 				= infoInstrumento.IdMoneda;
				objCobro_Fijo.CodigoCobroClasificacion = "GASTO";
				objCobro_Fijo.PermiteCambiarValor 	= "S";
				
				
				lstCobrosFijos.add(objCobro_Fijo);
			}

		}
		
		
		CrearOrdenes serviceCrearOrdenes = new CrearOrdenes();

		CrearOrdenesEntrada entradaCrearOrden = new CrearOrdenesEntrada();
		entradaCrearOrden.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaCrearOrden.p_IdUsuario 			= in.p_IdUsuario;
		entradaCrearOrden.p_IdModulo 			= in.p_IdModulo;
		entradaCrearOrden.p_UsaReglasNegocio 	= in.p_UsaReglasNegocio;
		entradaCrearOrden.p_simularOperacion 	= in.p_SimularOperacion;
		entradaCrearOrden.p_Ordenes 			= lstOrdenes;	
		
		if (lstCobrosFijos.size() > 0)
		{
			entradaCrearOrden.p_CobrosFijos 	= lstCobrosFijos;	
		}
		
		
		
		
		
		try 
		{
			cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida salidaCrearOrdenes = serviceCrearOrdenes.logica(con, entradaCrearOrden);
			
			out.P_OUT_ERRORESCONTADOR 	= salidaCrearOrdenes.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES 			= salidaCrearOrdenes.P_OUT_MENSAJES;
			out.P_OUT_RESULTADO 		= salidaCrearOrdenes.P_OUT_RESULTADO;
			
		}
		catch(Exception e) {
			logger.error(e);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_EN_INGRESO_ORDENES",null,con,in.p_IdEmpresa)); 
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		


				  
	
		if (out.P_OUT_RESULTADO.size() > 0 && cantarOrdenes.equalsIgnoreCase("S"))
		{	
			List<Orden> lstOrden = new ArrayList<Orden>();
			for(ObjRESULTADO resultado : out.P_OUT_RESULTADO) {
				Orden objOrden = new Orden();
				objOrden.IdOrden = resultado.IdOrden;
				lstOrden.add(objOrden);
			}			
			
			sql.batch(con, SQL_UPDATE_CANTAR_ORDEN, lstOrden, Orden.class);
		}
		
		
		
		logger.info("TERMINO :: CrearOrdenesMasiva");
		return out;

	}
	
	
	
	
	
	// ----------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ----------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new Gson();

			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"ORD\",\"p_IdCargaMasiva\":40,\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_ArchivoEntrada\":[]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":107,\"p_IdModulo\":\"ORD\",\"p_IdCargaMasiva\":118,\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_DetalleOrden\":[{\"DocumentoIdentidad\":\"15983577-4\",\"CodigoPortafolio\":\"01\",\"IdPortafolio\":1451920,\"TipoOperacion\":\"C\",\"IdMecanismoCaptacion\":\"OE\",\"Cantidad\":249,\"Nemotecnico\":\"CFMITNIPSA\",\"Precio\":75434.340,\"IdCondicionDeLiquidacion\":\"T0\",\"IdTipoVigenciaOrden\":\"SH\",\"FechaVigencia\":\"2020-10-15\",\"IdFormaCustodia\":\"CUS\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"IdTipoPrecioOrden\":\"L\", \"IdTipoCanalDistribucion\":\"MAN\", \"Negociacion\":\"OFE\", \"UsarPlantillaCobros\": \"N\"}]}";

			EntradaCrearOrdenesMasiva Objin = gson.fromJson(json, EntradaCrearOrdenesMasiva.class);

			CrearOrdenesMasiva demo=new CrearOrdenesMasiva();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearOrdenesMasiva ObjOut=demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}

}
