package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjProcesamiento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjTipoValorizacionPorTipoInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.val.Valorizar;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizaTasaCompra implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ValorizaTasaCompra.class.getName());
	public String SQL_SELECT_TASACOMPRA = loadResourceToString("cl/bcs/custodia/sql/ValorizarTasaCompra_Select_CarteraTasaCompra.sql");
	public String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/custodia/sql/ValorizarInstrumento_SEL_Instrumento.sql");

	
	public String SQL_SELECT_TIPO_VALORIZACION = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_TipoValorizacionPorTipoInstrumento.sql");
	
	private final String SQL_CREATE_TABLE= "CREATE LOCAL TEMPORARY TABLE #CarteraTasaCompra (\"IdCartera\" BIGINT)";													
	private final String SQL_INSERT_TABLE= "INSERT INTO #CarteraTasaCompra (\"IdCartera\") VALUES (:IdCartera)";
	private final String SQL_DROP_TABLE  = "DROP TABLE #CarteraTasaCompra";
	
	
	
	public static class EntradaValorizaTasaCompra extends BcsPojo{
		
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public Long								p_IdPais = -1L;
		public String							p_IdMonedaAdicional = null;
		public List<ValorizarInstrumento.ObjProcesamiento> p_listaObjValorizador; //lista tipos de valorizaciones sin tasa
		public List<ObjDatoEntrada>				        p_InstrumentosAValorizar; //lista de carteras a valorizar 
		public List<ObjEntradaInstrumentos>		p_Instrumentos;
		
		
		public EntradaValorizaTasaCompra () {
			this.p_InstrumentosAValorizar = new ArrayList<ObjDatoEntrada>();
			this.p_listaObjValorizador  = new ArrayList<ValorizarInstrumento.ObjProcesamiento>();
		}	
		
	}
	
	public static class SalidadaValorizaTasaCompra extends BcsPojo{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ValorizarInstrumento.ObjResultadoValorizacion> P_OUT_RESULTADOS;
	
		public SalidadaValorizaTasaCompra (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOS  = new ArrayList<ValorizarInstrumento.ObjResultadoValorizacion>();
		}		
		
	}
	
	public static class TasaCompraCartera  extends BcsPojo{
		public Long IdCartera;
		public BigDecimal TasaCompra;
	}
	
	public static class Cartera extends BcsPojo{
		public Long IdCartera;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {		
		Gson gson = new Gson();
		EntradaValorizaTasaCompra in = gson.fromJson(jin.toString(), EntradaValorizaTasaCompra.class);
		SalidadaValorizaTasaCompra out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();		
		return dataout;
	}


	public SalidadaValorizaTasaCompra logica(Connection con, EntradaValorizaTasaCompra entrada) throws Exception {
		logger.info("INICIO::ValorizaTasaCompra");
		long inicioServicio = System.currentTimeMillis();		
		SalidadaValorizaTasaCompra salida = new SalidadaValorizaTasaCompra();
		HashSet<Long> hashIntrumentoSinTasa = new HashSet<Long>(); 
		String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.LocalizacionSistema");
		
		
		
		//Instrumentos no valorizados
		hashIntrumentoSinTasa.addAll(entrada.p_listaObjValorizador.stream().map(objeto->objeto.IdInstrumento).collect(Collectors.toList()));
		
		//Carteras con instrumentos no valorizados
		
		List<ObjDatoEntrada> listaCarterasFiltradas = entrada.p_InstrumentosAValorizar.stream().filter(cartera ->
		hashIntrumentoSinTasa.contains(cartera.IdInstrumento)
		).collect(Collectors.toList());
				
		//buscamos las tasa de compra de las carteras 
		Map<Long,TasaCompraCartera> mapTasasCompra = this.obtenerTasaCompraCartera(con, entrada);		
				
		//cambiar las valorizaciones a tasa compra ( esto puede ir mas abajo ) 
		List<ValorizarInstrumento.ObjProcesamiento> listaValorizar= this.cambiarTipoTasa( entrada.p_listaObjValorizador,"MERC");		
		
		//se re hacen todos los objetos de las carteras a valorizar a precio compra
		Long contProcesamiento =20000L;
		List<ValorizarInstrumento.ObjProcesamiento> listaIngresoValorizador = new ArrayList<>();
		for (ObjDatoEntrada carteras : listaCarterasFiltradas) {
					
			TasaCompraCartera tasaCompra = mapTasasCompra.get(carteras.IdCustodia);
			
			if("TRANSITO".equals(carteras.TablaOrigen) || "SUBYACENTE".equals(carteras.TablaOrigen)) {
				continue;
			}
			
			for (ValorizarInstrumento.ObjProcesamiento  objVal : listaValorizar) {
				ValorizarInstrumento.ObjProcesamiento objetoValorizador = new ValorizarInstrumento.ObjProcesamiento();	
				this.copiarObjeto(objVal, objetoValorizador);
				objetoValorizador.IdProcesamiento			= contProcesamiento;
				objetoValorizador.Cantidad					= carteras.Cantidad;
				objetoValorizador.ValorRescate				= carteras.ValorRescate;
				objetoValorizador.Tasa						= tasaCompra.TasaCompra;
				objetoValorizador.Monto						= carteras.MontoCompra;	
				listaIngresoValorizador.add(objetoValorizador);
				contProcesamiento++;
				
			}
		}
		
		//Valorizacion
		Valorizar.OutValorizar ResultValorizador = this.valorizar(con , entrada,listaIngresoValorizador,localizacionSistema);
		
		
		//arreglo para cupones
		//parametros para cuadrar arreglo de cupones
				SQL sql = new SQL();		
				String tFechaHoraSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");	
		//Resultados		
		ValorizarInstrumentoSalida salidaValorizadorINS = this.ordenarResultado(con, entrada, ResultValorizador, listaIngresoValorizador, tFechaHoraSistema);		
		salida.P_OUT_RESULTADOS.addAll(salidaValorizadorINS.P_OUT_INSTRUMENTOSVALORIZADOS);		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValorizaTasaCompra :" , finServicio- inicioServicio);		
		return salida;
	}


	
	public Valorizar.OutValorizar valorizar (Connection con ,EntradaValorizaTasaCompra entrada ,List<ValorizarInstrumento.ObjProcesamiento> listaValorizaciones,String localizacionSistema ) throws Exception {
		logger.trace("INICIO::ValorizaTasaCompra.valorizar");
		long inicioMetodo = System.currentTimeMillis();
		
		Valorizar Valorizador = new Valorizar();
		Valorizar.InValorizar EntradaValorizador = new Valorizar.InValorizar();	
		EntradaValorizador.p_IdEmpresa 				= entrada.p_IdEmpresa;
		EntradaValorizador.p_IdUsuario 				= entrada.p_IdUsuario;
		EntradaValorizador.p_IdModulo 				= entrada.p_IdModulo;
		EntradaValorizador.p_IdPais 				= entrada.p_IdPais;			
		EntradaValorizador.p_IdMonedaAdicional		= entrada.p_IdMonedaAdicional;
		
		
		for(ValorizarInstrumento.ObjProcesamiento ingresoWS : listaValorizaciones) {
			Valorizar.InValorizar.InstrumentoWS InsWS = new Valorizar.InValorizar.InstrumentoWS();
			InsWS.IdValorizacion = ingresoWS.IdValorizacion; 
			InsWS.IdInstrumento = ingresoWS.IdInstrumento; 
			InsWS.Nemotecnico = ingresoWS.Nemotecnico; 
			InsWS.FechaEmision = ingresoWS.FechaEmision; 
			InsWS.FechaVencimiento = ingresoWS.FechaVencimiento; 
			InsWS.IdMonedaInstrumento = ingresoWS.IdMonedaInstrumento;
			InsWS.IdMonedaEmisionInstrumento = ingresoWS.IdMonedaEmisionInstrumento;
			InsWS.IdTipoReajusteInstrumento = ingresoWS.IdTipoReajusteInstrumento;
			InsWS.Cantidad = ingresoWS.Cantidad; 
			InsWS.ValorRescate = ingresoWS.ValorRescate; 
			InsWS.Precio = ingresoWS.Precio; 
			InsWS.Tasa = ingresoWS.Tasa; 
			InsWS.Monto = ingresoWS.Monto; 
			InsWS.IdClasificacionTipoInstrumento = ingresoWS.IdClasificacionTipoInstrumento;
			InsWS.Emisor = ingresoWS.Emisor;
			InsWS.IdCondicionDeLiquidacion = ingresoWS.IdCondicionDeLiquidacion;
			InsWS.TipoValorizacionInstrumento = ingresoWS.TipoValorizacionInstrumento;
			InsWS.SubTipoValorizacion = ingresoWS.SubTipoValorizacion;
			InsWS.TipoCalculoInteresesCupon = ingresoWS.TipoCalculoInteresesCupon;
			InsWS.ValorizaCorteDeCupon = ingresoWS.ValorizaCorteDeCupon;
			InsWS.OrigenValorizacion = ingresoWS.OrigenValorizacion;
			InsWS.MontoUI = ingresoWS.MontoUI;
			
			InsWS.TipoValorizacionInstrumento	= localizacionSistema.equals("CL") && (ingresoWS.TipoValorizacionInstrumento == null || ingresoWS.TipoValorizacionInstrumento.equals("")) ? null : ingresoWS.TipoValorizacionInstrumento; 
			InsWS.IdValorizacion				= ingresoWS.IdProcesamiento;
			InsWS.FechaValorizacion				= ingresoWS.FechaCalculada.substring(0,10);
			InsWS.Base							= 0;//TipoValorizacion.BaseCalculoDeInteres.intValue();
			EntradaValorizador.p_InstrumentosWS.add(InsWS);
		}
		
		Valorizar.OutValorizar ResultValorizador = Valorizador.logica(con, EntradaValorizador);
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::ValorizaTasaCompra.valorizar :" , finMetodo- inicioMetodo);
		
		return ResultValorizador;
		
	}
	
	
	public ValorizarInstrumentoSalida ordenarResultado (Connection con,EntradaValorizaTasaCompra entrada,Valorizar.OutValorizar ResultValorizador,List<ValorizarInstrumento.ObjProcesamiento> listaValorizaciones, String tFechaHoraSistema) throws Exception {
		logger.trace("INICIO::ValorizaTasaCompra.ordenarResultado");
		long inicioMetodo = System.currentTimeMillis();
		
		ValorizarInstrumento valorizarInstrumento = new ValorizarInstrumento();
		ValorizarInstrumento.ValorizarInstrumentoEntrada entradaValorizarInstrumento = new ValorizarInstrumento.ValorizarInstrumentoEntrada();
		entradaValorizarInstrumento.p_Instrumentos.addAll(entrada.p_Instrumentos);
		entradaValorizarInstrumento.p_IdEmpresa = entrada.p_IdEmpresa;
		Map<Long, List<ObjProcesamiento>> hashValorizacionDatosEntrada = listaValorizaciones.stream().collect(Collectors.groupingBy(t -> t.IdValorizacion));
		HashMap<String,Object>  TipoValorizacionPorTipoInstrumento = this.obtenerTipoValorizacion(con, entrada);
		
		HashMap<Long, Object> ResultCalculoInteres = valorizarInstrumento.calcularInteres(con,entradaValorizarInstrumento, hashValorizacionDatosEntrada, TipoValorizacionPorTipoInstrumento);
		HashMap<Long, Object> InfoInstrumentos =  this.obtenerInformacionInstrumento(con, entrada);		
		
		ValorizarInstrumentoSalida salida =valorizarInstrumento.ordenarResultadoaValorizacion(con, entradaValorizarInstrumento, ResultValorizador, hashValorizacionDatosEntrada, ResultCalculoInteres, InfoInstrumentos, tFechaHoraSistema);
				
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::ValorizaTasaCompra.ordenarResultado :" , finMetodo- inicioMetodo);
				
		return salida;
	}
	
	public HashMap<String,Object> obtenerTipoValorizacion(Connection con,EntradaValorizaTasaCompra entrada) throws Exception{
		logger.debug("INICIO::ValorizaTasaCompra.obtenerTipoValorizacion");
		long inicioMetodo = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> paramsTipoValorizacionPorTipoInstrumento = new HashMap<>();
		paramsTipoValorizacionPorTipoInstrumento.put("IdEmpresa",entrada.p_IdEmpresa);
		List<ObjTipoValorizacionPorTipoInstrumento> TVTI = sql.query(con, SQL_SELECT_TIPO_VALORIZACION, paramsTipoValorizacionPorTipoInstrumento, ObjTipoValorizacionPorTipoInstrumento.class);	
		HashMap<String,Object> TipoValorizacionPorTipoInstrumento = Utils.List2Hash(TVTI, "IdTipoInstrumento");
				
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValorizaTasaCompra.obtenerTipoValorizacion :" , finMetodo- inicioMetodo);
		
		return TipoValorizacionPorTipoInstrumento;
	}
	
	
	public HashMap<Long,Object> obtenerInformacionInstrumento (Connection con,EntradaValorizaTasaCompra entrada) throws Exception {
		logger.debug("INICIO::ValorizaTasaCompra.obtenerInformacionInstrumento");
		long inicioMetodo = System.currentTimeMillis();		
		
		SQL sql = new SQL();
		HashMap<String,Object> paramsInstrumento = new HashMap<>();
		List<Long> IdInstrumento = entrada.p_InstrumentosAValorizar.parallelStream().filter(t -> t.IdInstrumento != null).map(t -> t.IdInstrumento).distinct().collect(Collectors.toList());		
		paramsInstrumento.put("IdInstrumento", IdInstrumento);
		
		List<ValorizarInstrumento.ObjInstrumento> listInstrumento = sql.query(con, SQL_SELECT_INSTRUMENTO, paramsInstrumento, ValorizarInstrumento.ObjInstrumento.class);
		HashMap<Long,Object> InfoInstrumentos = Utils.List2Hash(listInstrumento, "IdInstrumento");
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValorizaTasaCompra.obtenerInformacionInstrumento :" , finMetodo- inicioMetodo);
		
		return InfoInstrumentos;
	}
	
	public Map<Long,TasaCompraCartera> obtenerTasaCompraCartera (Connection conn , EntradaValorizaTasaCompra entrada) throws SQLException{
		logger.debug("INICIO::ValorizaTasaCompra.obtenerTasaCompraCartera");
		long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Cartera> ListaIdCustodia = entrada.p_InstrumentosAValorizar.stream().map(cartera ->{
			Cartera car = new Cartera();
			car.IdCartera= cartera.IdCustodia;
			return car;
		}).collect(Collectors.toList());
		
		SQL.exec(conn, SQL_CREATE_TABLE);			
		sql.batch(conn,SQL_INSERT_TABLE ,ListaIdCustodia, Cartera.class);
		
		Map<Long,TasaCompraCartera> mapTasasCompra = sql.query(conn, SQL_SELECT_TASACOMPRA, params, TasaCompraCartera.class)
				.stream().collect(Collectors.toMap(cartera->cartera.IdCartera, cartera->cartera));
		
		
		SQL.exec(conn, SQL_DROP_TABLE);	
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::ValorizaTasaCompra.obtenerTasaCompraCartera :" , finMetodo- inicioMetodo);
		return mapTasasCompra;
	}
	
	public List<ValorizarInstrumento.ObjProcesamiento> cambiarTipoTasa (List<ValorizarInstrumento.ObjProcesamiento> valorizaciones , String  tipoTasaCambio) {
		logger.trace("INICIO::ValorizaTasaCompra.cambiarTipoTasa");
		long inicioMetodo = System.currentTimeMillis();
		
		List<ValorizarInstrumento.ObjProcesamiento>  listaValorizar = valorizaciones.stream().map(valorizacion -> {
			valorizacion.DimTasa=tipoTasaCambio;
			return valorizacion;
		}).collect(Collectors.toList());
		
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::ValorizaTasaCompra.cambiarTipoTasa :" , finMetodo- inicioMetodo);
		
		return listaValorizar;		
	}
	
	

	public void copiarObjeto(ValorizarInstrumento.ObjProcesamiento  objOrigen ,ValorizarInstrumento.ObjProcesamiento objDestino) {
		logger.trace("INICIO::ValorizaTasaCompra.copiarObjeto");
		long inicioMetodo = System.currentTimeMillis();
		
	   
	   objDestino.IdValorizacion = objOrigen.IdValorizacion;
	   objDestino.DimTasa = objOrigen.DimTasa;
	   objDestino.DimTiempo = objOrigen.DimTiempo;
	   objDestino.FechaCalculada = objOrigen.FechaCalculada;
	   objDestino.TipoPrecioTasa = objOrigen.TipoPrecioTasa;
	   objDestino.ValorizaPor = objOrigen.ValorizaPor;
	   objDestino.IdClasificacionTipoInstrumento = objOrigen.IdClasificacionTipoInstrumento;
	   objDestino.ValorClasificacionRiesgo = objOrigen.ValorClasificacionRiesgo;
	   objDestino.IdTipoInstrumento = objOrigen.IdTipoInstrumento;
	   objDestino.IdInstrumento = objOrigen.IdInstrumento;
	   objDestino.Nemotecnico = objOrigen.Nemotecnico;
	   objDestino.FechaEmision = objOrigen.FechaEmision;
	   objDestino.TasaEmision = objOrigen.TasaEmision;
	   objDestino.FechaVencimiento = objOrigen.FechaVencimiento;
	   objDestino.IdMonedaInstrumento = objOrigen.IdMonedaInstrumento;
	   objDestino.IdMonedaEmisionInstrumento = objOrigen.IdMonedaEmisionInstrumento;
	   objDestino.IdTipoReajusteInstrumento = objOrigen.IdTipoReajusteInstrumento;
	   objDestino.Emisor = objOrigen.Emisor;
	   objDestino.IdCondicionDeLiquidacion = objOrigen.IdCondicionDeLiquidacion;
	   objDestino.Tamano = objOrigen.Tamano;   
	   objDestino.Precio = objOrigen.Precio;
	   objDestino.FechaPrecioTasa = objOrigen.FechaPrecioTasa;
	   objDestino.FuentePrecioTasa = objOrigen.FuentePrecioTasa;
	   objDestino.MensajeErrorFuente = objOrigen.MensajeErrorFuente;  
	   objDestino.PrecioSubyacente = objOrigen.PrecioSubyacente;
	   objDestino.TasaSubyacente = objOrigen.TasaSubyacente;
	   objDestino.PrecioGarantia = objOrigen.PrecioGarantia;
	   objDestino.TasaGarantia = objOrigen.TasaGarantia;
	   objDestino.TasaTIF = objOrigen.TasaTIF;
	   objDestino.IdUso = objOrigen.IdUso;
	   objDestino.TipoCartera = objOrigen.TipoCartera;
	   objDestino.IdTipoCriterioTasa = objOrigen.IdTipoCriterioTasa;
	   objDestino.EsCuentaPropia = objOrigen.EsCuentaPropia;
	   objDestino.IndicadorUsoTasa = objOrigen.IndicadorUsoTasa;
	   objDestino.TipoValorizacion = objOrigen.TipoValorizacion;
	   objDestino.TipoValorizacionInstrumento = objOrigen.TipoValorizacionInstrumento;
	   objDestino.SubTipoValorizacion = objOrigen.SubTipoValorizacion;
	   objDestino.CalculaIntereses = objOrigen.CalculaIntereses;
	   objDestino.CalculaInteresesAPlazo = objOrigen.CalculaInteresesAPlazo;
	   objDestino.ValorizaCorteDeCupon = objOrigen.ValorizaCorteDeCupon;
	   objDestino.EsBonoCero = objOrigen.EsBonoCero;
	   objDestino.TipoCalculoInteresesCupon = objOrigen.TipoCalculoInteresesCupon;
	   objDestino.PlazoResidual = objOrigen.PlazoResidual;
	   objDestino.TablaOrigen = objOrigen.TablaOrigen;
	   objDestino.NemoOriginal = objOrigen.NemoOriginal;
	   objDestino.OrigenValorizacion = objOrigen.OrigenValorizacion;
	   objDestino.MontoUI = objOrigen.MontoUI;
	   objDestino.MontoMonedaSistemaOriginal = objOrigen.MontoMonedaSistemaOriginal;
	   
	   
	   
	  long finMetodo = System.currentTimeMillis();
	  logger.trace("FIN::ValorizaTasaCompra.copiarObjeto :" , finMetodo- inicioMetodo);
			
	}
	
	
	
}
