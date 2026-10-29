package cl.bcs.plataforma.val;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasOut;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.val.CuponesWs.InCuponesWs;
import cl.bcs.plataforma.val.CuponesWs.OutCuponesWs;
import cl.bcs.plataforma.val.util.ValoUtils.TareaValorizacion;
import cl.bcs.plataforma.val.util.ValorizarWsBCS;
import cl.bcs.plataforma.val.util.ValorizarWsBcsRest;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.valo2.ValorizadorBonos;
import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.ValorizadorPagares;
import cl.bcs.valo2.ValorizadorPagaresParams;
import cl.bcs.valo2.entidades.Cupon;
import datadog.trace.api.Trace;
/************************************************************
 * @version 	1.0
 * @author 		jgarces - JG
 * 
 * Valorizador Masivo que implementa
 * - Valorizaciones PQ (Ej. RV que realizan calculo PxQ) 
 * - Valorizaciones WS (Ej. RF que invoca servicios externos)
 *   en caso WS se utiliza WebService valorizador Chile
 *   y Clase java para valorizador Peru.
*************************************************************/
public class Valorizar implements JavaCall {	
	

	static final Logger logger = LogManager.getLogger(Valorizar.class.getName());
	
	//private static ValorizarWsBCS 		valoBCS;
	private static ValorizarWsBcsRest	valoBCSRest;
	private static ValorizadorBonos 	valorizadorBonoPE;
	private static ValorizadorPagares 	valorizadorPagarePE;
	private static String 				RutaArchivosConfiguracion;

	/*******************
	 * clases in/out
	 *******************/
	public static class ConsultaPrograma{
		public Long 	IdAgenda;
		public String 	idEstado;
	}
	
	public static class UpdatePrograma{
		public String FechaEjecucion;
		public Long IdPrograma;
	}
	
	public static class MapeoValoParidad {
		public String IdValorizacion;
		public Long IdInternoParidad;
		
		public MapeoValoParidad(String IdValorizacion,Long IdInternoParidad){
			this.IdValorizacion = IdValorizacion;
			this.IdInternoParidad = IdInternoParidad;
		}
	}
	
	
	
	public static class ObjCupon {
		public BigDecimal	 	amortizacion;
		public boolean 			call;
		public java.sql.Date 	fecha;
		public java.sql.Date 	fechaAnterior;
		public BigDecimal 		intereses;
		public String 			nemo;
		public Integer 			numero;
		public BigDecimal 		periodoEnAnos;
		public Integer 			periodoEnDias;
		public BigDecimal 		saldoPorAmortizar;
		public BigDecimal 		vencimientoEnAnos;
		public Integer 			vencimientoEnDias;
		public BigDecimal 		flujo;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//IMPORTANTE: se asume como entrada los valores en la moneda del instrumento
	public static class InValorizar {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String				p_IdModulo;
		public Long 				p_IdPais;
		public List<InstrumentoPQ>  p_InstrumentosPQ;
		public List<InstrumentoWS>  p_InstrumentosWS;
		
		public String 				p_IdMonedaAdicional = null;
		public static class InstrumentoPQ {
			public Long				IdValorizacion; 
			public String			FechaValorizacion; 
			public Long				IdInstrumento; 
			public String			Nemotecnico; 
			public String			IdMonedaInstrumento; 
			public BigDecimal		Cantidad; 
			public BigDecimal		Precio; 
			public BigDecimal		Monto;
			// MSH : Para Simultaneas y Emprestitos
			public BigDecimal		PlazoActual;
			public BigDecimal		Tasa;
			
		}
		public static class InstrumentoWS {
			public Long				IdValorizacion; 
			public String			FechaValorizacion; 
			public Long				IdInstrumento; 
			public String			Nemotecnico; 
			public String			FechaEmision; 
			public String			FechaVencimiento; 
			public String			IdMonedaInstrumento;
			public String			IdMonedaEmisionInstrumento;
			public String			IdTipoReajusteInstrumento;
			public BigDecimal		Cantidad; 
			public BigDecimal		ValorRescate; 
			public BigDecimal		Precio; 
			public BigDecimal		Tasa; 
			public BigDecimal		Monto; 
			public String			IdClasificacionTipoInstrumento;//valoCL
			public String			Emisor;//valoCL
			public String			IdCondicionDeLiquidacion;//valoCL
			public String			TipoValorizacionInstrumento;
			public String			SubTipoValorizacion;
			public Integer			TipoCalculoInteresesCupon = 0;//valoPE
			public Integer			Base;//valoPE
			public String 			ValorizaCorteDeCupon = "N";
			public String 			IdReajusteCabecera;
		
			// Para la UI de la mesa
			public String			OrigenValorizacion;
			public BigDecimal		MontoUI;
			
			
			public String toString() {
				return Utils.printJson(this, true);
			}
		}
		public InValorizar(){
			p_InstrumentosPQ = new ArrayList<InstrumentoPQ>();
			p_InstrumentosWS = new ArrayList<InstrumentoWS>();
		}
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class OutValorizar {
		public Long P_OUT_ERRORESCONTADOR; //asociados al proceso, no al resultado de cada registro.
		public Long P_OUT_EXECUTIONTIME;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		//public List<InstrumentoPQ> P_OUT_INSTRUMENTOSVALORIZADOSPQ;
		public HashMap<Long,InstrumentoPQ> P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ;
		//public List<InstrumentoWS> P_OUT_INSTRUMENTOSVALORIZADOSWS;
		public HashMap<Long,InstrumentoWS> P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS;
		
		public static class InstrumentoPQ {
			public Long				IdValorizacion; 
			public java.sql.Date	FechaValorizacion; 
			//public String			IdTipoInstrumento; 
			public Long				IdInstrumento; 
			public String			Nemotecnico; 
			public String			IdMonedaInstrumento; 
			public BigDecimal		Cantidad; 
			public BigDecimal		Precio; 
			public BigDecimal		MontoMonedaSistema; 
			public BigDecimal		MontoMonedaInstrumento; 
			public BigDecimal		MontoMonedaAdicional;
			public BigDecimal		FactorMonedaInstrumento; 
			public java.sql.Date	FechaFactorMonedaInstrumento; 
			public BigDecimal		FactorMonedaAdicional; 
			public java.sql.Date	FechaFactorMonedaAdicional;  
			public String			ResultadoProceso; 	//OK,ERROR 
			public String			CodigoProceso; 		//UI_XXX:codigos diccionario
			public String			DescripcionProceso; //detalle del procesamiento 

			// MSH : Para Simultaneas y Emprestitos
			public BigDecimal		PlazoActual;
			public BigDecimal		Tasa;
			
		}
		
		public static class InstrumentoWS {
			public Long				IdValorizacion;
			public java.sql.Date	FechaValorizacion;
			public Long				IdInstrumento;
			public String			Nemotecnico;
			public String			IdMonedaInstrumento;
			public BigDecimal		Cantidad;
			public BigDecimal		ValorRescate; //valor presente?
			public BigDecimal		valorPar;
			public BigDecimal		Precio;
			public BigDecimal		Tasa;
			public BigDecimal		Tasa360;
			public BigDecimal		MontoMonedaSistema; 
			public BigDecimal		MontoMonedaInstrumento; 
			public BigDecimal		MontoMonedaAdicional; 
			public String			TipoValorizacionInstrumento;
			public BigDecimal		MontoInteresMonedaSistema; 
			public BigDecimal		MontoInteresMonedaInstrumento; 
			public BigDecimal		MontoInteresMonedaAdicional;  
			public BigDecimal		MontoReajusteMonedaSistema; 
			public BigDecimal		MontoReajusteMonedaInstrumento; 
			public BigDecimal		MontoReajusteMonedaAdicional;  
			
			public BigDecimal		MontoTotalMonedaSistema; 
			public BigDecimal		MontoTotalMonedaInstrumento; 
			public BigDecimal		MontoTotalMonedaAdicional; 
			public BigDecimal		FactorMonedaInstrumento; 
			public java.sql.Date	FechaFactorMonedaInstrumento; 
			public BigDecimal		FactorMonedaAdicional; 
			public java.sql.Date	FechaFactorMonedaAdicional;
			
			public BigDecimal		ProporcionCapital; 
			public BigDecimal		ProporcionIntereses; 
			public BigDecimal		ProporcionReajuste; 
			public BigDecimal		ProporcionTotal;
			public BigDecimal		Duration; 			
			public BigDecimal		Convexidad; 
			public BigDecimal		PlazoValorizador; 
			public String			IdClasificacionTipoInstrumento;//valoCL
			public String			Emisor;//valoCL
			public String			IdCondicionDeLiquidacion;//valoCL
			public String			ResultadoProceso; 	//OK,ERROR 
			public String			CodigoProceso; 		//UI_XXX:codigos proceso
			public String			DescripcionProceso; //detalle del procesamiento 			
			public List<ObjCupon>	Cupones;
			public Date 			FechaVencimiento;
			
			public String			IdTipoReajusteInstrumento;
			public BigDecimal		ValorTipoReajuste;
			
			public InstrumentoWS() {
				Cupones = new ArrayList<ObjCupon>();
			}
			public String toString() {
				return Utils.printJson(this, true);
			}
		}		
		public OutValorizar(){
			//P_OUT_INSTRUMENTOSVALORIZADOSPQ = new ArrayList<InstrumentoPQ>();
			P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ = new HashMap<Long, Valorizar.OutValorizar.InstrumentoPQ>();
			//P_OUT_INSTRUMENTOSVALORIZADOSWS = new ArrayList<InstrumentoWS>();
			P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS = new  HashMap<Long, Valorizar.OutValorizar.InstrumentoWS>();
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_EXECUTIONTIME = 0L;
			
		}
		public String toString() {
			return Utils.printJson(this, true);
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		InValorizar in=gson.fromJson(jin.toString() ,InValorizar.class);
		OutValorizar out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * logica del programa
	 **********************/
	@Trace
	public OutValorizar logica(Connection con, InValorizar entrada) throws Exception {
		logger.info("Inicio Valorizar");
		Long startTime = System.currentTimeMillis();
		String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");		
		String MonedaAdicional 		= entrada.p_IdMonedaAdicional == null?ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional"):entrada.p_IdMonedaAdicional;
		String[] NemosCorreccionTipoVal64 = {};
		try	{NemosCorreccionTipoVal64		= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.NemosCorreccionTipoVal64").split("\\;", -1);} catch(Exception ex){logger.debug(ex);};
		HashMap<String,String> listNemoTipoVal64 = new HashMap<String,String>();
		for(String item:NemosCorreccionTipoVal64){
			listNemoTipoVal64.put(item,item);
		}
		BigDecimal FactorPlazo;

		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);

		CalcularParidad par = new CalcularParidad();
		CalcularParidad.CalcularParidadEntrada parin = new CalcularParidad.CalcularParidadEntrada();
		parin.p_IdEmpresa 		= entrada.p_IdEmpresa;		
		//parin.p_CalculosEntrada = new ArrayList<CalcularParidad.CalculoEntrada>();
		
		logger.debug("Inicia Valorizador");
		OutValorizar salida = new OutValorizar();
		salida.P_OUT_ERRORESCONTADOR = 0L;		
		/******************************
		 * 	VALIDACIONES INICIALES
		 ******************************/
		if(entrada.p_InstrumentosPQ.size()+entrada.p_InstrumentosWS.size()==0){
			logger.debug("no hay registros de entrada");
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_SIN_REGISTROS_ENTRADA","No hay registros de Entrada"));
		}
		if(entrada.p_IdPais==null||(entrada.p_IdPais!=null&&!localizacionSistema.equals("CL")&&!localizacionSistema.equals("PE"))){
			logger.debug("pais ingresado sin valorización configurada");
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_PAIS_SIN_VALORIZACION_CONFIGURADA","pais ingresado sin valorización configurada"));
		}
		if(entrada.p_IdEmpresa==null){
			logger.debug("No se ingresa empresa");
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_NO_INGRESA_EMPRESA","no se ha ingresado la empresa."));
		}
		if(salida.P_OUT_ERRORESCONTADOR>0){
			salida.P_OUT_EXECUTIONTIME=System.currentTimeMillis()-startTime;
			return salida;//salir inmediatamente.
		}
		/******************************
		 * 	PQ - CHILE|PERU
		 ******************************/
		for(InValorizar.InstrumentoPQ item:entrada.p_InstrumentosPQ){
			OutValorizar.InstrumentoPQ itemout = crearPojoSalidaValorizar(item);
			itemout.MontoMonedaInstrumento = item.Monto;
			itemout.ResultadoProceso = "ERROR";
			itemout.CodigoProceso = "UI_REGISTRO_SIN_PROCESAR";
			itemout.DescripcionProceso = "El procesamiento del registro no ha iniciado";
			if(itemout.Precio!=null&&itemout.Cantidad!=null){
				
				itemout.MontoMonedaInstrumento = itemout.Precio.multiply(itemout.Cantidad);
				// MSH : Si viene Plazo y Tasa es una simultanea o Emprestito
				if( itemout.Tasa != null && itemout.PlazoActual !=  null ) {
					FactorPlazo = BigDecimal.ONE.add(itemout.Tasa.divide(BigDecimal.valueOf(100),20,RoundingMode.HALF_UP).multiply(itemout.PlazoActual.divide(BigDecimal.valueOf(30),20,RoundingMode.HALF_UP)));
					itemout.MontoMonedaInstrumento = itemout.MontoMonedaInstrumento.multiply(FactorPlazo);
				}
			}else if(itemout.MontoMonedaInstrumento!=null&&itemout.Precio!=null&&itemout.Precio.doubleValue()>0){
				itemout.Cantidad=itemout.MontoMonedaInstrumento.divide(itemout.Precio,20,RoundingMode.HALF_UP);
			}else if(itemout.MontoMonedaInstrumento!=null&&itemout.Cantidad!=null&&itemout.Cantidad.doubleValue()>0){
				itemout.Precio=itemout.MontoMonedaInstrumento.divide(itemout.Cantidad,20,RoundingMode.HALF_UP);
			}else{
				itemout = crearPojoSalidaValorizar(item);
				itemout.MontoMonedaInstrumento = item.Monto;
				
				itemout.ResultadoProceso = "ERROR";
				itemout.CodigoProceso = "UI_ENTRADA_INCORRECTA";
				itemout.DescripcionProceso = "Entrada insuficiente para realizar el cálculo";
			}
			itemout.ResultadoProceso = "OK";
			itemout.CodigoProceso = "UI_OK";
			itemout.DescripcionProceso = "Registro procesado.";
			salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ.put(itemout.IdValorizacion,itemout);
		}
		logger.debug("Fin Valorizar PQ");
		
		/******************************
		 * 	WS - CHILE
		 ******************************/
		if(localizacionSistema.equals("CL")&&entrada.p_InstrumentosWS.size()>0){
			//iniValorizadorBCS(con,"BSCL",entrada.p_IdEmpresa);
			iniValorizadorBCSRest(con,"BSCL",entrada.p_IdEmpresa);
			//--INICIO TIPIFICAR, mas adelante se podria omitir--
			List<ValorizarWsBCS.ValorizarWsBCSObj> instip = new ArrayList<ValorizarWsBCS.ValorizarWsBCSObj>();
			for(InValorizar.InstrumentoWS item:entrada.p_InstrumentosWS){ //preparar entrada tipificar
				if(item.TipoValorizacionInstrumento==null||item.TipoValorizacionInstrumento.equals("")){
					ValorizarWsBCS.ValorizarWsBCSObj aux = new ValorizarWsBCS.ValorizarWsBCSObj();
					aux.idTarea = item.IdValorizacion;
					aux.nemo = item.Nemotecnico;
					aux.fechaVal = Fecha.toDate(item.FechaValorizacion);
					boolean existe = false;
					for(ValorizarWsBCS.ValorizarWsBCSObj check : instip){
						if (check.nemo.equals(aux.nemo)){
							existe = true;
							break;
						}
					} if(!existe)instip.add(aux);
				}
			}
			if(instip.size()>0){
				valoBCSRest.obtenerTipoVal(instip);
				for(ValorizarWsBCS.ValorizarWsBCSObj tip : instip){ //evaluar tipificacion
					if(listNemoTipoVal64.containsKey(tip.nemo)) {
						tip.tipoVal = 64L;
					}
					for(InValorizar.InstrumentoWS item:entrada.p_InstrumentosWS){
						if(tip.nemo.equals(item.Nemotecnico)){
							item.TipoValorizacionInstrumento = tip.tipoVal != null ? tip.tipoVal.toString() : "-1";
						}
					}
				}
			}
			//--FIN TIPIFICAR--
			HashMap<Long,ValorizarWsBCS.ValorizarWsBCSObj> listparams = new HashMap<Long,ValorizarWsBCS.ValorizarWsBCSObj>();
			logger.debug("Inicio PrepararDatosValorizarBCS");
			for(InValorizar.InstrumentoWS item:entrada.p_InstrumentosWS){ //prepara entrada valorizador
				
				OutValorizar.InstrumentoWS itemout = new OutValorizar.InstrumentoWS();
				copyPojo(item, itemout,false);
				
				salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.put(itemout.IdValorizacion, itemout);
				if(item.TipoValorizacionInstrumento==null||item.TipoValorizacionInstrumento.equals("-1")){
					itemout.ResultadoProceso = "ERROR";
					itemout.CodigoProceso = "UI_INSTRUMENTO_SIN_TIPO_VALORIZACION";
					itemout.DescripcionProceso = "El instrumento no tiene tipo de valorizacion. No se envia a valorizar";
					logger.debug("Instrumento sin tipo de valorizacion: "+itemout.Nemotecnico);
					continue;
				}
				ValorizarWsBCS.ValorizarWsBCSObj aux = new ValorizarWsBCS.ValorizarWsBCSObj();
				try{
					preparaDatosValorizarBCS(item,aux);
					listparams.put(aux.idTarea,aux);
				}catch(Exception e){
					itemout.ResultadoProceso = "ERROR";
					itemout.CodigoProceso = "UI_ERROR_PREPARAR_DATOS_VALORIZAR";
					itemout.DescripcionProceso = "Error: "+Utils.stackTraceToString(e);
					logger.error(e);
				}
				
				
				
			}
			
			logger.debug("Fin PrepararDatosValorizarBCS");
			if(listparams.size()>0){
				try{
					valoBCSRest.valorizar(listparams);
				}catch(Exception e){
					logger.debug("Excepcion en valorizador BCS");
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_EXCEPCION_VALORIZADOR_BCS",Utils.stackTraceToString(e)));
				}
			}else{
				logger.debug("No se han generado registros para la valorizacion. Revisar rutina metodo: preparaDatosValorizarBCS");
			}
			
			parin.p_IdTrabajo = (long)Math.random()*10000;
			
			
			for(OutValorizar.InstrumentoWS itemout: salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){
				
				itemout.ResultadoProceso = "ERROR";
				itemout.CodigoProceso = "UI_REGISTRO_SIN_PROCESAR";
				itemout.DescripcionProceso = "El procesamiento del registro no ha iniciado";
				ValorizarWsBCS.ValorizarWsBCSObj val = listparams.get(itemout.IdValorizacion);
				
				if (val == null) {
					itemout.ResultadoProceso = "ERROR";
					itemout.CodigoProceso = "UI_ERROR";
					itemout.DescripcionProceso = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_VALORIZAR").add("nemotecnico", itemout.Nemotecnico).msg();
				}else if(val.wsCallerCodSalida== null) {
					itemout.ResultadoProceso = "ERROR";
					itemout.CodigoProceso = "UI_ERROR";
					itemout.DescripcionProceso = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ERROR_VALORIZAR").add("nemotecnico", itemout.Nemotecnico).msg();
					logger.info("Resultado Valorizacion: "+val);
				}
				else {				
					if(!val.wsCallerCodSalida.equals("0")){
						
						// MSH
						// No es error cuando instrumento esta vencido... (puede que la valorizacion que esta haciendo es a magnana)
						if ( itemout.FechaVencimiento != null && itemout.FechaValorizacion.equals(itemout.FechaVencimiento)) {
							// Instrumento vencido...
							itemout.ResultadoProceso = "INST.VENCIDO";
							itemout.CodigoProceso = val.wsCallerCodSalida;
							itemout.DescripcionProceso = "Val : Instrumento Vencido (F.Val==F.Venc)";
						} else {
							itemout.ResultadoProceso = "ERROR";
							itemout.CodigoProceso = val.wsCallerCodSalida;
							itemout.DescripcionProceso = "Error WS: "+val.wsCallerDescSalida;
						}
					}
					itemout.Cantidad=(val.cantidad==null)?itemout.Cantidad:val.cantidad;
					//itemout.ValorRescate; //valor presente?
					itemout.Precio=(val.precio==null)?itemout.Precio:val.precio;
					if(itemout.IdClasificacionTipoInstrumento.equals("RF")){
						if(val.tasaTir!=null)itemout.Tasa=val.tasaTir;
						if(val.tasaInteres!=null)itemout.Tasa=val.tasaInteres;
					}else if(itemout.IdClasificacionTipoInstrumento.equals("IF")){
						if(val.tasaTip!=null)itemout.Tasa=val.tasaTip;
						if(val.tasaInteres!=null)itemout.Tasa=val.tasaInteres;
					}	
					if(itemout.IdClasificacionTipoInstrumento.equals("RF")){
						itemout.MontoMonedaSistema		= val.montoPeso;
						if(itemout.IdMonedaInstrumento.equals("CLP"))itemout.MontoMonedaInstrumento=val.montoPeso;
						if(itemout.IdMonedaInstrumento.equals("UF"))itemout.MontoMonedaInstrumento=val.montoUF;
						if(itemout.IdMonedaInstrumento.equals("USD"))itemout.MontoMonedaInstrumento=val.montoUS;
						
						//Correcion del monto entregado por el valorizador (Monto no retornado en la moneda del instrumento)
						if (itemout.MontoMonedaInstrumento == null && itemout.MontoMonedaSistema != null){
							CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();			
							auxmon.IdInterno 		= val.idTarea;
							auxmon.IdTrabajo 		= parin.p_IdTrabajo;
							auxmon.IdUso 			= "VALORIZADOR";
							auxmon.Fecha 			= Fecha.toStringDate(itemout.FechaValorizacion);
							auxmon.IdMonedaOrigen 	= MonedaSistema;
							auxmon.MontoOrigen 		= itemout.MontoMonedaSistema;
							auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
							auxmon.IdMonedaDestino 	= itemout.IdMonedaInstrumento;
							parin.p_CalculosEntrada.add(auxmon);
						}
					}else if(itemout.IdClasificacionTipoInstrumento.equals("IF")){
						itemout.ValorRescate = itemout.IdMonedaInstrumento.equals("CLP") ? val.valRescateP : val.valRescateR;
						if ( itemout.ValorRescate == null && itemout.IdTipoReajusteInstrumento != null && !itemout.IdTipoReajusteInstrumento.equals("CLP") ) {
							// EJ.: TR=USD, LIQ=CLP =>
							itemout.ValorRescate = val.valRescateR;
						}
							
						// MSH Debe conservar lo que llega del valorizador...
						itemout.MontoMonedaSistema	= val.valCaptacionP;
						
						if(itemout.IdMonedaInstrumento.equals("CLP"))itemout.MontoMonedaInstrumento=val.valCaptacionP;
						else if(itemout.IdMonedaInstrumento.equals("UF"))itemout.MontoMonedaInstrumento=val.valCaptacionR;
						else if(itemout.IdMonedaInstrumento.equals("USD"))itemout.MontoMonedaInstrumento=val.valCaptacionR;
						else itemout.MontoMonedaInstrumento=(val.valCaptacionR==null)?itemout.MontoMonedaInstrumento:val.valCaptacionR;
					}	
					itemout.MontoInteresMonedaInstrumento 	= BigDecimal.ZERO;   
					itemout.MontoReajusteMonedaInstrumento 	= BigDecimal.ZERO;
					itemout.MontoTotalMonedaInstrumento		= itemout.MontoMonedaInstrumento; 
					itemout.Duration 						= val.duration; 
					itemout.Convexidad 						= val.convexidad;  
					itemout.valorPar	= val.valorPar != null ?	val.valorPar: BigDecimal.ZERO; 
					itemout.ResultadoProceso 				= "OK";
					itemout.CodigoProceso 					= "UI_OK";
					itemout.DescripcionProceso 				= "Registro procesado.";						
				
					
				}
			}
		}
		logger.debug("Fin Valorizar WS");
	    
		/*
		 * CUPONES CHILE - CUPON CHILE
		 */
		if(localizacionSistema.equals("CL") && entrada.p_InstrumentosWS.size()>0){
			
			InObtenerPlazo entradaObtenerPlazo = new InObtenerPlazo();
			
			HashMap<String,TareaValorizacion> hashTareasCupones = new HashMap<String, TareaValorizacion>();
			for(InValorizar.InstrumentoWS inst: entrada.p_InstrumentosWS){
				OutValorizar.InstrumentoWS itemout = salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.get(inst.IdValorizacion);
				if (itemout.CodigoProceso.equals("ERROR") || !inst.ValorizaCorteDeCupon.equalsIgnoreCase("S")) {
					// Solo buscaremos cupones para instrumentos sin errores (por ejemplo no se debe buscar cupones a instrumentos vencidos
					continue;
				}
				
				if (!hashTareasCupones.containsKey(inst.Nemotecnico + inst.FechaValorizacion + inst.FechaVencimiento)) {
					TareaValorizacion arg0 = new TareaValorizacion();
					arg0.IdTarea = inst.IdValorizacion;
					arg0.Nemo = inst.Nemotecnico;
					arg0.FechaIni = Fecha.toDate(inst.FechaValorizacion);
					arg0.FechaFin = Fecha.toDate(inst.FechaVencimiento);
					arg0.CodTipoMod = "D";
					hashTareasCupones.put(inst.Nemotecnico + arg0.FechaIni +arg0.FechaFin,arg0);
					

					ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
					f.FechaDesde 		= inst.FechaValorizacion;
					f.FechaHasta 		= inst.FechaVencimiento;
					f.IdInterno 		= inst.IdValorizacion;
					f.SoloDiasHabiles 	= "N";
					entradaObtenerPlazo.p_Fechas.add(f);
				}
				
			}
			
			if (entradaObtenerPlazo.p_Fechas.size() > 0) {
				
				logger.debug("Inicio Obtener Plazo");
				entradaObtenerPlazo.p_IdPais = empresa.IdPais;
				ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
				OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(con, entradaObtenerPlazo);
				logger.debug("Fin Obtener Plazo");
				List<FechasOut> Plazos = salidaObtenerPlazo.p_out_Fechas;
				
				for (TareaValorizacion reg : hashTareasCupones.values()) {
					for (FechasOut plazo : Plazos) {
						if (reg.IdTarea.equals(plazo.IdInterno)) {
							reg.Plazo = plazo.Plazo;
							break;
						}
					}
				}
				
			}
			
			if (hashTareasCupones.size() > 0 ){
				
				CuponesWs cupon = new CuponesWs();
				InCuponesWs entradaCupones = new InCuponesWs();
				entradaCupones.idEmpresa 			= entrada.p_IdEmpresa;
				entradaCupones.idGrupo 				= 0L;
				entradaCupones.idModulo 			= entrada.p_IdModulo;
				entradaCupones.idUsuario 			= entrada.p_IdUsuario;
				entradaCupones.rutinaValorizador	= "BCS";
				
				entradaCupones.cupones = new ArrayList<>(hashTareasCupones.values());
				
				//llamado a servicio de cupones
				OutCuponesWs listparamsBono = cupon.logica(con, entradaCupones);
				
				HashMap<String,TareaValorizacion> hashCupones = new HashMap<String, TareaValorizacion>();
				for (TareaValorizacion cuponResult : listparamsBono.cupones) {
					hashCupones.put(cuponResult.Nemo +cuponResult.FechaIni +cuponResult.FechaFin, cuponResult);
				}
				
				
				for (cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS itemSalida : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()) {
					TareaValorizacion reglistparamsBono = hashCupones.get(itemSalida.Nemotecnico + itemSalida.FechaValorizacion + itemSalida.FechaVencimiento);
					if (reglistparamsBono != null) {
						ObjCupon Cupon = new ObjCupon();
						
						if(itemSalida.IdClasificacionTipoInstrumento.equals("IF")){
							Cupon.nemo 			= itemSalida.Nemotecnico;
							Cupon.amortizacion	= itemSalida.ValorRescate;
							Cupon.fecha			= itemSalida.FechaVencimiento;
							Cupon.fechaAnterior	= itemSalida.FechaVencimiento;
							
						}
						else if(itemSalida.IdClasificacionTipoInstrumento.equals("RF")){
							
							if (reglistparamsBono.CuponSecuencia == null) {
								Cupon.fecha				= itemSalida.FechaVencimiento;
								
							}
							else{
								Cupon.nemo				= reglistparamsBono.Nemo;
								Cupon.fecha				= reglistparamsBono.CuponFechaHabil;
								Cupon.fechaAnterior		= reglistparamsBono.CuponFechaEfectiva;
								Cupon.numero			= new Integer(reglistparamsBono.CuponSecuencia.intValue());
								Cupon.amortizacion 		= reglistparamsBono.CuponAmortizacion;
								Cupon.intereses			= reglistparamsBono.CuponIntereses;
								Cupon.flujo				= reglistparamsBono.CuponFlujo;
							};
						}
						else {
							//?
						}
						
						itemSalida.Cupones.add(Cupon);	
						
					}
					else if (itemSalida.IdClasificacionTipoInstrumento.equals("IF")){
						ObjCupon Cupon = new ObjCupon();
						Cupon.nemo 			= itemSalida.Nemotecnico;
						Cupon.amortizacion	= itemSalida.ValorRescate;
						Cupon.fecha			= itemSalida.FechaVencimiento;
						Cupon.fechaAnterior	= itemSalida.FechaVencimiento;
						itemSalida.Cupones.add(Cupon);	
					}
					else {
						
					}
				}
			}
		};
		logger.debug("Fin Cupones");
		if (parin.p_CalculosEntrada.size() > 0){
			CalcularParidad.CalcularParidadSalida parout = par.logica(con, parin);
			HashMap<Long,Object> paridades = Utils.List2Hash(parout.p_out_CalculosSalida, "IdInterno");
			for(OutValorizar.InstrumentoWS item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){//WS
				
				CalcularParidad.CalculoSalida itempar = (CalculoSalida) paridades.get(item.IdValorizacion);
				
				if (itempar == null) {
					continue;
				}
					
				item.MontoMonedaSistema				= itempar.MontoLocal; 
				item.FactorMonedaInstrumento 		= itempar.FactorOrigenLocal; 
				item.FechaFactorMonedaInstrumento 	= itempar.FechaLocalCambio; 
				item.MontoMonedaInstrumento 		= itempar.MontoDestino;
				item.MontoTotalMonedaInstrumento	= item.MontoMonedaInstrumento; 
				item.FactorMonedaInstrumento 		= itempar.FactorCambioLocal; 
				item.FechaFactorMonedaInstrumento 	= itempar.FechaOrigenCambio;
				
			};	
		};
		logger.debug("Fin CalcularParidad Valorizar");
		/******************************
		 * 	WS - PERU
		 ******************************/
		if(localizacionSistema.equals("PE")&&entrada.p_InstrumentosWS.size()>0){
			iniValorizadorPE(con,"BSCL",entrada.p_IdEmpresa);
			
			////////////////// VALORIZACION DE BONOS
			List<ValorizadorBonosParams> listparamsBono = new ArrayList<ValorizadorBonosParams>();
			for(InValorizar.InstrumentoWS item:entrada.p_InstrumentosWS){
				if (!item.SubTipoValorizacion.equalsIgnoreCase("BONO")) continue;
				
				ValorizadorBonosParams aux = new ValorizadorBonosParams();				
				aux.setIdRequerimiento(item.IdValorizacion);
				aux.setNemo(item.Nemotecnico);
				aux.setFechaValorizacion(Fecha.toDate(item.FechaValorizacion));
				aux.setValorNominal(item.Cantidad);//en este caso es el nominal!.					
				aux.setMonedaPago(item.IdMonedaInstrumento);
					aux.setPrecio(item.Precio);
					aux.setTasa(item.Tasa);
					aux.setMontoTotal(item.Monto);
				listparamsBono.add(aux);				
			}
			try{
				//logger.debugAsJson("Entrada a valorizador de bonos Peru", listparamsBono);				
				valorizadorBonoPE.valorizar(listparamsBono);
			}catch(Exception e){
				logger.debug("Excepcion en valorizador PE");
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_EXCEPCION_VALORIZADOR_PE",Utils.stackTraceToString(e)));
			}
			//logger.debugAsJson("resultadoValorizacionBono",listparamsBono);
			for(int vm=0;vm<entrada.p_InstrumentosWS.size();vm++){
				if (!entrada.p_InstrumentosWS.get(vm).SubTipoValorizacion.equalsIgnoreCase("BONO")) continue;
				
				OutValorizar.InstrumentoWS itemout = new OutValorizar.InstrumentoWS();
				itemout.ResultadoProceso = "ERROR";
				itemout.CodigoProceso = "UI_REGISTRO_SIN_PROCESAR";
				itemout.DescripcionProceso = "El procesamiento del registro no ha iniciado";
				InValorizar.InstrumentoWS itemin = entrada.p_InstrumentosWS.get(vm);
				ValorizadorBonosParams itemval = null;
				for(int jg=0;jg<listparamsBono.size();jg++){
					if(itemin.IdValorizacion.equals(listparamsBono.get(jg).getIdRequerimiento())){
						itemval = listparamsBono.get(jg);
						break;
					}
				}
				Utils.copyPojo(itemin, itemout,false);
				if(itemval==null){
					itemout.ResultadoProceso = "ERROR";
					itemout.CodigoProceso = "UI_ERROR";
					itemout.DescripcionProceso = "Registro no valorizado. Revise parametros de entrada";
					
				}else{
					itemout.Precio 							= itemval.getPrecio();
					itemout.Tasa 							= itemval.getTasa();
					itemout.Tasa360							= itemval.getTasa360();
					itemout.MontoMonedaInstrumento 			= itemval.getMontoCapital(); 
					itemout.MontoInteresMonedaInstrumento 	= itemval.getMontoIntereses(); 
					itemout.MontoReajusteMonedaInstrumento 	= itemval.getMontoReajuste(); 
					itemout.MontoTotalMonedaInstrumento 	= itemval.getMontoTotal();
					itemout.ProporcionCapital 				= new BigDecimal(itemval.getProporcionCapital()); 
					itemout.ProporcionIntereses 			= new BigDecimal(itemval.getProporcionIntereses()); 
					itemout.ProporcionReajuste 				= new BigDecimal(itemval.getProporcionReajuste()); 
					itemout.ProporcionTotal 				= new BigDecimal(itemval.getProporcionTotal()); 
					itemout.Duration 						= itemval.getDuration(); 
					itemout.Convexidad 						= itemval.getConvexidad();
					//TODO: Cupones
					if (itemval.getCupones() != null){
						for (Cupon InfoCupon : itemval.getCupones()){
							ObjCupon Cupon = new ObjCupon();
							Cupon.nemo				= InfoCupon.getNemo();
							Cupon.call				= InfoCupon.isCall();
							Cupon.fecha				= new java.sql.Date(InfoCupon.getFecha().getTime());
							Cupon.fechaAnterior		= new java.sql.Date(InfoCupon.getFechaAnterior().getTime());
							Cupon.periodoEnAnos		= new BigDecimal(InfoCupon.getPeriodoEnAnos());
							Cupon.periodoEnDias		= InfoCupon.getPeriodoEnDias();						
							Cupon.vencimientoEnAnos	= new BigDecimal(InfoCupon.getVencimientoEnAnos());
							Cupon.vencimientoEnDias	= InfoCupon.getVencimientoEnDias();
							Cupon.numero			= InfoCupon.getNumero();
							Cupon.amortizacion 		= InfoCupon.getAmortizacion();
							Cupon.intereses			= InfoCupon.getIntereses();
							Cupon.saldoPorAmortizar	= InfoCupon.getSaldoPorAmortizar();
							itemout.Cupones.add(Cupon);
						};
					};

					if(itemval.getCodigoRetorno()==0){
						itemout.ResultadoProceso = "OK";
						itemout.CodigoProceso = "UI_OK";
						itemout.DescripcionProceso = "Registro procesado.";
					}else{
						itemout.ResultadoProceso = "ERROR";
						itemout.CodigoProceso = "UI_ERROR";
						itemout.DescripcionProceso = (itemval.getMensaje()!=null)?itemval.getMensaje():"Registro no valorizado";
					}
				}
				salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.put(itemout.IdValorizacion,itemout);
				//logger.debugAsJson(itemout);				
			};
			
			
			/////////////////// VALORIZACION DE PAGARES
			try{
				for(InValorizar.InstrumentoWS item:entrada.p_InstrumentosWS){
					if (!item.SubTipoValorizacion.equalsIgnoreCase("PAGARE")) continue;
					
					ValorizadorPagaresParams aux = new ValorizadorPagaresParams();
					aux.setIdRequerimiento(item.IdValorizacion);
					aux.setFechaVencimiento(Fecha.toDate(item.FechaVencimiento));
					aux.setFechaCaptacion(Fecha.toDate(item.FechaValorizacion));						
					aux.setNemo(item.Nemotecnico);
					aux.setMonedaPago(item.IdMonedaInstrumento);
					aux.setTipoInteres(item.TipoCalculoInteresesCupon);
					
					if (item.Tasa != null){
						aux.setTasa(item.Tasa);
					} else {
						aux.setValorCaptacion(item.Monto);
					}
					
					aux.setValorRescate(item.ValorRescate);
					aux.setDiasAno(item.Base);
					valorizadorPagarePE.valorizar(aux);
					
					OutValorizar.InstrumentoWS itemout = new OutValorizar.InstrumentoWS();
					Utils.copyPojo(item, itemout,false);
					itemout.Tasa 						= aux.getTasa();					
					itemout.MontoMonedaInstrumento 		= aux.getValorCaptacion();
					itemout.MontoTotalMonedaInstrumento = aux.getValorCaptacion();
					itemout.FechaValorizacion			= Fecha.utilDateToSqlDate(aux.getFechaCaptacion());
					
					if(aux.getCodigoRetorno()==0){
						itemout.ResultadoProceso = "OK";
						itemout.CodigoProceso = "UI_OK";
						itemout.DescripcionProceso = "Registro procesado.";
					}else{
						itemout.ResultadoProceso = "ERROR";
						itemout.CodigoProceso = "UI_ERROR";
						itemout.DescripcionProceso = (aux.getMensaje()!=null)?aux.getMensaje():"Registro no valorizado";
					}

					itemout.Duration = aux.getDuration();

					salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.put(itemout.IdValorizacion,itemout);
					//logger.debugAsJson(itemout);					
				}
			}catch(Exception e){
				logger.debug(e);
				logger.debug("Excepcion en valorizador PE");
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR,MensajeSalida.MENSAJE_ERROR,"UI_EXCEPCION_VALORIZADOR_PE",Utils.stackTraceToString(e)));
				throw e;
			}			
		}
		logger.debug("Fin Valorizacion Peru");
		/******************************
		 * 	PARIDADES
		 ******************************/
		parin.p_IdTrabajo = (long)Math.random()*10000;
		Long idinternopar = 0L;
		parin.p_CalculosEntrada.clear();
		HashMap<String,Long> mapeo = new HashMap<String,Long>();
		for(OutValorizar.InstrumentoPQ item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ.values()){//Paridades para PQ
			if(item.MontoMonedaInstrumento == null) {
				continue;
			}
			CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();
			idinternopar++;
			auxmon.IdInterno 		= idinternopar;
			auxmon.IdTrabajo 		= parin.p_IdTrabajo;
			auxmon.Fecha 			= Fecha.toStringDate(item.FechaValorizacion);
			auxmon.IdMonedaOrigen 	= item.IdMonedaInstrumento;
			auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
			auxmon.MontoOrigen 		= item.MontoMonedaInstrumento;
			auxmon.IdUso 			= "VALORIZADOR";			
			auxmon.IdMonedaDestino 	= MonedaAdicional;
			parin.p_CalculosEntrada.add(auxmon);
			mapeo.put("PQ" + item.IdValorizacion,idinternopar);
		}
		for(OutValorizar.InstrumentoWS item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){//Paridades para WS
			if(item.MontoMonedaInstrumento != null){
//			if(item.MontoMonedaInstrumento == null){
				CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();
				idinternopar++;
				auxmon.IdInterno 		= idinternopar;
				auxmon.IdTrabajo 		= parin.p_IdTrabajo;
				auxmon.IdUso 			= "VALORIZADOR";
				auxmon.Fecha 			= Fecha.toStringDate(item.FechaValorizacion);
				auxmon.IdMonedaOrigen 	= item.IdMonedaInstrumento;
				
				// MSH - Para RF/IF debe ir a buscar la paridad con la moneda del Tipo de Reajuste, no con la de liquidación 
				// if ( ( item.IdClasificacionTipoInstrumento.equals("IF") || item.IdClasificacionTipoInstrumento.equals("RF") ) 
				//		&& item.IdTipoReajusteInstrumento != null ) {
				//	auxmon.IdMonedaOrigen 	= item.IdTipoReajusteInstrumento;
				// }

				auxmon.MontoOrigen 		= item.MontoMonedaInstrumento;
				auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
				auxmon.IdMonedaDestino 	= MonedaAdicional;
				parin.p_CalculosEntrada.add(auxmon);
				mapeo.put("WS" + item.IdValorizacion,idinternopar);				
			}
		}
		
		if (parin.p_CalculosEntrada.size() > 0) {
			CalcularParidad.CalcularParidadSalida parout2 = par.logica(con, parin);
			HashMap<Long, Object> hashParidades = Utils.List2Hash(parout2.p_out_CalculosSalida, "IdInterno");
			
			for(OutValorizar.InstrumentoPQ item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSPQ.values()){//PQ
				Long itemmap = mapeo.get("PQ" + item.IdValorizacion);
				if (itemmap == null) {
					//logger.debug("no encuentro indice calculo paridad");
					continue;
				}
				CalculoSalida paridad = (CalculoSalida) hashParidades.get(itemmap);
				if (paridad == null) {
					//logger.debug("no encuentro resultado calculo paridad");
					continue;
				}
				
				item.MontoMonedaSistema = paridad.MontoLocal; 
				item.FactorMonedaInstrumento = paridad.FactorOrigenLocal; 
				item.FechaFactorMonedaInstrumento = paridad.FechaLocalCambio; 
				item.MontoMonedaAdicional = paridad.MontoDestino;
				item.FactorMonedaAdicional = paridad.IdMonedaDestino.equalsIgnoreCase(MonedaSistema)? BigDecimal.ONE : paridad.FactorCambioLocal; 
				item.FechaFactorMonedaAdicional = paridad.FechaOrigenCambio;  
				
				
			}

			for(OutValorizar.InstrumentoWS item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){//WS
				
				Long itemmap = mapeo.get("WS" + item.IdValorizacion);
				if (itemmap == null) {
					logger.debug("no encuentro indice calculo paridad");
					continue;
				}
				CalculoSalida paridad = (CalculoSalida) hashParidades.get(itemmap);
				if (paridad == null) {
					logger.debug("no encuentro resultado calculo paridad");
					continue;
				}
				
				// MSH : En espera de parche Renzo
				paridad.ValorParametroFinanciero 	= BigDecimal.ONE;
				item.FactorMonedaInstrumento 		= paridad.FactorOrigenLocal; 
				item.FechaFactorMonedaInstrumento 	= paridad.FechaLocalCambio; 
				item.MontoMonedaAdicional 			= paridad.MontoDestino;
				item.FactorMonedaAdicional 			= paridad.IdMonedaDestino.equalsIgnoreCase(MonedaSistema)? BigDecimal.ONE : paridad.FactorCambioLocal; 
				item.FechaFactorMonedaAdicional 	= paridad.FechaOrigenCambio;
				
				// MSH - Los montos valorizados para IF/RF ya vienen Ok desde el valorizador, no se deben modificar por paridades..
				if ( item.IdClasificacionTipoInstrumento.equals("RF") || item.IdClasificacionTipoInstrumento.equals("IF") ) {
					if ( item.MontoMonedaSistema == null ) {
						item.MontoMonedaSistema		= paridad.MontoLocal;
					}
					item.MontoTotalMonedaAdicional 	= item.MontoMonedaAdicional;
					item.MontoTotalMonedaSistema 	= item.MontoMonedaSistema;		

					// MSH - Parche "por mientras"
					// if ( item.IdTipoReajusteInstrumento != null && item.IdTipoReajusteInstrumento.equals("USD") ) {
					//	item.ValorParametroFinanciero = paridad.ValorParametroFinanciero;
					// }

				} else {
					item.MontoMonedaSistema		= paridad.MontoLocal;
				}
				
				
				//if(localizacionSistema.equals("PE")){//otros campos salida valoPE								
				if (item.MontoInteresMonedaInstrumento != null){
					item.MontoInteresMonedaSistema 		= item.FactorMonedaInstrumento != null ? item.MontoInteresMonedaInstrumento.multiply(item.FactorMonedaInstrumento) : item.MontoInteresMonedaInstrumento;
					item.MontoInteresMonedaAdicional 	= item.FactorMonedaAdicional != null ? item.MontoInteresMonedaSistema.multiply(item.FactorMonedaAdicional) : item.MontoInteresMonedaSistema;									
				};
				if (item.MontoReajusteMonedaInstrumento != null){
					item.MontoReajusteMonedaSistema 	= item.FactorMonedaInstrumento != null ? item.MontoReajusteMonedaInstrumento.multiply(item.FactorMonedaInstrumento) : item.MontoReajusteMonedaInstrumento;
					item.MontoReajusteMonedaAdicional 	= item.FactorMonedaAdicional != null ? item.MontoReajusteMonedaSistema.multiply(item.FactorMonedaAdicional) : item.MontoReajusteMonedaSistema;
				};
				if (item.MontoTotalMonedaInstrumento != null && item.FactorMonedaAdicional.compareTo(BigDecimal.ZERO) > 0
					// MSH - Los montos valorizados para IF/RF ya vienen Ok desde el valorizador, no se deben modificar..						
					&& !item.IdClasificacionTipoInstrumento.equals("RF") && !item.IdClasificacionTipoInstrumento.equals("IF") ){
				//if (item.MontoTotalMonedaInstrumento != null) > 0){
					item.MontoTotalMonedaSistema 		= item.FactorMonedaInstrumento != null ? item.MontoTotalMonedaInstrumento.multiply(item.FactorMonedaInstrumento) : item.MontoTotalMonedaInstrumento;
					//item.MontoTotalMonedaAdicional 		= item.FactorMonedaAdicional != null ? item.MontoTotalMonedaSistema.multiply(item.FactorMonedaAdicional) : item.MontoTotalMonedaSistema;
					item.MontoTotalMonedaAdicional 		= item.FactorMonedaAdicional != null ? item.MontoTotalMonedaSistema.divide(item.FactorMonedaAdicional,20,RoundingMode.HALF_UP) : item.MontoTotalMonedaSistema;
				};
			}
		}
		
		
		
		// MSH Paridades por tipo reajuste... (Solo IF/RF)
		// --------------------------------------------------------------------------
		idinternopar = 0L;
		parin.p_CalculosEntrada.clear();
		
		for(OutValorizar.InstrumentoWS item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){
			if ( ( item.IdClasificacionTipoInstrumento.equals("RF") || item.IdClasificacionTipoInstrumento.equals("IF") ) && item.IdTipoReajusteInstrumento != null){
//			if(item.MontoMonedaInstrumento == null){
				CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();
				idinternopar++;
				auxmon.IdInterno 		= idinternopar;
				auxmon.IdTrabajo 		= parin.p_IdTrabajo;
				auxmon.IdUso 			= "VALORIZADOR";
				auxmon.IdMonedaOrigen 	= item.IdTipoReajusteInstrumento;
				auxmon.Fecha 			= Fecha.toStringDate(item.FechaValorizacion);	
				if ( item.IdTipoReajusteInstrumento.equals("USD") ) {
					// Cuando es USD debe obtener el del día anterior para efectos de los intereses y reajustes...
					auxmon.Fecha 			= Fecha.toStringDate(Fecha.addDays(item.FechaValorizacion, -1));
				}
				auxmon.MontoOrigen 		= new BigDecimal ( 1000 );
				auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
				auxmon.IdMonedaDestino 	= item.IdTipoReajusteInstrumento;
				parin.p_CalculosEntrada.add(auxmon);
				mapeo.put("WS" + item.IdValorizacion,idinternopar);				
			}
		}

		if (parin.p_CalculosEntrada.size() > 0) {
			
			CalcularParidad.CalcularParidadSalida parout2 = par.logica(con, parin);
			HashMap<Long, Object> hashParidades = Utils.List2Hash(parout2.p_out_CalculosSalida, "IdInterno");

			for(OutValorizar.InstrumentoWS item : salida.P_OUT_HASH_INSTRUMENTOSVALORIZADOSWS.values()){//WS
				Long itemmap = mapeo.get("WS" + item.IdValorizacion);
				if (itemmap == null) {
					logger.debug("no encuentro indice calculo paridad");
					continue;
				}
				CalculoSalida paridad = (CalculoSalida) hashParidades.get(itemmap);
				if (paridad == null) {
					logger.debug("no encuentro resultado calculo paridad");
					continue;
				}
				
				// MSH : En espera de parche Renzo
				item.ValorTipoReajuste 	= paridad.ValorParametroFinanciero;
				item.FactorMonedaInstrumento = item.ValorTipoReajuste;
			}
			
		}
		logger.debug("Fin Calculo Paridad");		
		
		/*********************************
		 * FIN
		 *********************************/
		salida.P_OUT_EXECUTIONTIME=System.currentTimeMillis()-startTime;
		return salida;
	}
	
	private void copyPojo(cl.bcs.plataforma.val.Valorizar.InValorizar.InstrumentoWS item,cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoWS itemout, boolean b) {
		itemout.IdValorizacion= item.IdValorizacion;
		itemout.FechaValorizacion= Fecha.toDate(item.FechaValorizacion);
		itemout.IdInstrumento= item.IdInstrumento;
		itemout.Nemotecnico= item.Nemotecnico;
		itemout.IdMonedaInstrumento= item.IdMonedaInstrumento;
		itemout.Cantidad= item.Cantidad;
		itemout.ValorRescate= item.ValorRescate; //valor presente?
		itemout.Precio= item.Precio;
		itemout.Tasa= item.Tasa;
//		itemout.Tasa360= item.Tasa360;
//		itemout.MontoMonedaSistema= item.MontoMonedaSistema; 
//		itemout.MontoMonedaInstrumento= item.MontoMonedaInstrumento; 
//		itemout.MontoMonedaAdicional= item.MontoMonedaAdicional; 
		itemout.TipoValorizacionInstrumento= item.TipoValorizacionInstrumento;
		///itemout.MontoInteresMonedaSistema= item.MontoInteresMonedaSistema; 
		//itemout.MontoInteresMonedaInstrumento= item.MontoInteresMonedaInstrumento; 
		//itemout.MontoInteresMonedaAdicional= item.MontoInteresMonedaAdicional;  
		//itemout.MontoReajusteMonedaSistema= item.MontoReajusteMonedaSistema; 
		//itemout.MontoReajusteMonedaInstrumento= item.MontoReajusteMonedaInstrumento; 
		//itemout.MontoReajusteMonedaAdicional= item.MontoReajusteMonedaAdicional;  
		//itemout.MontoTotalMonedaSistema= item.MontoTotalMonedaSistema; 
		//itemout.MontoTotalMonedaInstrumento= item.MontoTotalMonedaInstrumento; 
		//itemout.MontoTotalMonedaAdicional= item.MontoTotalMonedaAdicional; 
		//itemout.FactorMonedaInstrumento= item.FactorMonedaInstrumento; 
		//itemout.FechaFactorMonedaInstrumento= item.FechaFactorMonedaInstrumento; 
		//itemout.FactorMonedaAdicional= item.FactorMonedaAdicional; 
		//itemout.FechaFactorMonedaAdicional= item.FechaFactorMonedaAdicional;
		//itemout.ProporcionCapital= item.ProporcionCapital; 
		//itemout.ProporcionIntereses= item.ProporcionIntereses; 
		//itemout.ProporcionReajuste= item.ProporcionReajuste; 
		//itemout.ProporcionTotal= item.ProporcionTotal;
		//itemout.Duration= item.Duration; 			
		//itemout.Convexidad= item.Convexidad; 
		//itemout.PlazoValorizador= item.PlazoValorizador; 
		itemout.IdClasificacionTipoInstrumento= item.IdClasificacionTipoInstrumento;//valoCL
		itemout.Emisor= item.Emisor;//valoCL
		itemout.IdCondicionDeLiquidacion= item.IdCondicionDeLiquidacion;//valoCL
		//itemout.ResultadoProceso= item.ResultadoProceso; 	//OK,ERROR 
		//itemout.CodigoProceso= item.CodigoProceso; 		//UI_XXX:codigos proceso
		//itemout.DescripcionProceso= item.DescripcionProceso; //detalle del procesamiento 			
		//itemout.Cupones= item.Cupones;
		itemout.FechaVencimiento= Fecha.toDate(item.FechaVencimiento);
		itemout.IdTipoReajusteInstrumento= item.IdTipoReajusteInstrumento;
		//itemout.ValorTipoReajuste= item.ValorTipoReajuste;
	}

	private cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ crearPojoSalidaValorizar(
			cl.bcs.plataforma.val.Valorizar.InValorizar.InstrumentoPQ item) {
		cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ itemout = new cl.bcs.plataforma.val.Valorizar.OutValorizar.InstrumentoPQ();
		
		itemout.IdValorizacion = item.IdValorizacion;
		itemout.FechaValorizacion = Fecha.toDate(item.FechaValorizacion);
		itemout.IdInstrumento = item.IdInstrumento;
		itemout.Nemotecnico = item.Nemotecnico;
		itemout.IdMonedaInstrumento = item.IdMonedaInstrumento;
		itemout.Cantidad = item.Cantidad;
		itemout.Precio = item.Precio;
		/*itemout.MontoMonedaSistema = item.MontoMonedaSistema;
		itemout.MontoMonedaInstrumento = item.MontoMonedaInstrumento;
		itemout.MontoMonedaAdicional = item.MontoMonedaAdicional;
		itemout.FactorMonedaInstrumento = item.FactorMonedaInstrumento;
		itemout.FechaFactorMonedaInstrumento = item.FechaFactorMonedaInstrumento;
		itemout.FactorMonedaAdicional = item.FactorMonedaAdicional;
		itemout.FechaFactorMonedaAdicional = item.FechaFactorMonedaAdicional;
		itemout.ResultadoProceso = item.ResultadoProceso;
		itemout.CodigoProceso = item.CodigoProceso;
		itemout.DescripcionProceso = item.DescripcionProceso; */
		itemout.PlazoActual = item.PlazoActual;
		itemout.Tasa = item.Tasa;
		
		return itemout;
	}

	/*********************************
	 * METODOS UTILITARIOS
	 *********************************/
	public void iniValorizadorPE(Connection con,String schema,Long IdEmpresa)throws Exception{
		Log log = new Log("Valorizar");
		if(RutaArchivosConfiguracion==null)	
			RutaArchivosConfiguracion = Utils.getConfiguracion(con, schema, IdEmpresa, "PLA.RutaArchivosConfiguracion");
		if(RutaArchivosConfiguracion==null)
			throw new Exception("iniValorizadorBCS: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion"); 
		if(valorizadorBonoPE == null  || valorizadorPagarePE == null){
			Properties prop = new Properties();
			InputStream input = null;
			
			try {
				input = new FileInputStream(RutaArchivosConfiguracion+"valorizador.properties");
			}catch(Exception e){
				input = new FileInputStream("Properties/valorizador_pe.properties");
			}
            prop.load(input);
            String dbURL  = prop.getProperty("PE_JDBC");
	        String dbprop = "currentschema=BSCL;";
	        String dbdrv  = "com.sap.db.jdbc.Driver";
	        String dbqry  = "select 1 from dummy";
	        String user   = prop.getProperty("PE_USER");
	        String pass   = prop.getProperty("PE_PASS");
	        input.close();
	        
	        BasicDataSource ds = new BasicDataSource();
	        ds.setDriverClassName(dbdrv);
	        ds.setUsername(user);
	        ds.setPassword(pass);	
	        ds.setUrl(dbURL);
	        ds.setValidationQuery(dbqry);
	        ds.setConnectionProperties(dbprop);
	        ds.setDefaultAutoCommit(false);
	        logger.debug("configuracion valorizador peru, server: "+dbURL+",user: "+user);
	        
	        if(valorizadorBonoPE == null){
	        	valorizadorBonoPE = new ValorizadorBonos(ds);
	        };
	        if(valorizadorPagarePE == null){
	        	valorizadorPagarePE = new ValorizadorPagares(ds);
	        };
            logger.debug("inicia clase estatica valorizador PERU");
    	}else{
        	logger.debug("reutiliza clase estatica valorizador PERU");
        }
	}
	
	public void iniValorizadorBCSRest(Connection con,String schema,Long IdEmpresa) throws Exception{ 
		if(RutaArchivosConfiguracion==null)	
			RutaArchivosConfiguracion = Utils.getConfiguracion(con, schema, IdEmpresa, "PLA.RutaArchivosConfiguracion");
		if(RutaArchivosConfiguracion==null)
			throw new Exception("iniValorizadorBCS: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion"); 
		if(valoBCSRest == null){
    		Properties prop = new Properties();
    		
    		InputStream input = null;
    		try {
    			input = new FileInputStream("Properties/valorizador.properties");
    		}catch(FileNotFoundException fnfe) {
    			input = new FileInputStream(RutaArchivosConfiguracion+"valorizador.properties");
    		}
    		
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=("Y".equalsIgnoreCase(prop.getProperty("DEPURATION"))?true:false);//Y|N
            int ConnTimeOut=Integer.valueOf(prop.getProperty("CONNTIMEOUT"));
            int ReadTimeOut=Integer.valueOf(prop.getProperty("READTIMEOUT"));
            int PackageSize=Integer.valueOf(prop.getProperty("PACKAGESIZE"));
            int ThreadNumber=Integer.valueOf(prop.getProperty("THREADNUMBER"));
            input.close();
            logger.debug("configuracion valorizador, server: "+Server+",user: "+User);
            valoBCSRest=new ValorizarWsBcsRest(Server,User,Pass,Depuracion,ConnTimeOut,ReadTimeOut,PackageSize,ThreadNumber);
            logger.debug("inicia clase estatica valorizador Chile BCS");
    	}else{
        	logger.debug("reutiliza clase estatica valorizador BCS");
        }
    }
	
	/*public void iniValorizadorBCS(Connection con,String schema,Long IdEmpresa) throws Exception{ 
		Log log = new Log("Valorizar");
		if(RutaArchivosConfiguracion==null)	
			RutaArchivosConfiguracion = Utils.getConfiguracion(con, schema, IdEmpresa, "PLA.RutaArchivosConfiguracion");
		if(RutaArchivosConfiguracion==null)
			throw new Exception("iniValorizadorBCS: RutaArchivosConfiguracion no definida. Revise parametro PLA.RutaArchivosConfiguracion"); 
		if(valoBCS == null){
    		Properties prop = new Properties();
    		
    		InputStream input = null;
    		try {
    			input = new FileInputStream(RutaArchivosConfiguracion+"valorizador.properties");
    		}catch(FileNotFoundException fnfe) {
    			input = new FileInputStream("Properties/valorizador.properties");
    		}
    		
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=("Y".equalsIgnoreCase(prop.getProperty("DEPURATION"))?true:false);//Y|N
            int ConnTimeOut=Integer.valueOf(prop.getProperty("CONNTIMEOUT"));
            int ReadTimeOut=Integer.valueOf(prop.getProperty("READTIMEOUT"));
            input.close();
            logger.debug("configuracion valorizador, server: "+Server+",user: "+User);
            valoBCS=new ValorizarWsBCS(Server,User,Pass,Depuracion,ConnTimeOut,ReadTimeOut);
            logger.debug("inicia clase estatica valorizador Chile BCS");
    	}else{
        	logger.debug("reutiliza clase estatica valorizador BCS");
        }
    }*/
	
	private void preparaDatosValorizarBCS(InValorizar.InstrumentoWS origen, ValorizarWsBCS.ValorizarWsBCSObj destino) throws Exception{
		//variables base
		destino.idTarea = origen.IdValorizacion;
		destino.tipoVal = (origen.TipoValorizacionInstrumento==null||origen.TipoValorizacionInstrumento.equals(""))?null:Long.valueOf(origen.TipoValorizacionInstrumento);
		destino.nemo = origen.Nemotecnico;
		destino.fechaVal = Fecha.toDate(origen.FechaValorizacion);
		destino.cLiq = (origen.IdCondicionDeLiquidacion==null||origen.IdCondicionDeLiquidacion.equals(""))?null:origen.IdCondicionDeLiquidacion;
		//variables transformadas
		BigDecimal valRescateP = null;
		BigDecimal valRescateR = null;
		BigDecimal valCaptacionP = null;
		BigDecimal valCaptacionR = null;
		Long tipoMoneda = null;
		if(origen.IdMonedaInstrumento.equals("CLP") && origen.IdMonedaEmisionInstrumento.equals("CLP"))tipoMoneda=1L;
		else if(origen.IdMonedaInstrumento.equals("CLP") && origen.IdMonedaEmisionInstrumento.equals("UF"))tipoMoneda=2L;
		else if(origen.IdMonedaInstrumento.equals("CLP" )&& origen.IdMonedaEmisionInstrumento.equals("USD"))tipoMoneda=4L;
		else if(origen.IdMonedaInstrumento.equals("USD" )&& origen.IdMonedaEmisionInstrumento.equals("USD"))tipoMoneda=4L;
		else if(origen.IdMonedaInstrumento.equals("CLP") && origen.IdMonedaEmisionInstrumento.equals("EUR"))tipoMoneda=15L;
		else if(origen.IdMonedaInstrumento.equals("EUR") && origen.IdMonedaEmisionInstrumento.equals("EUR"))tipoMoneda=15L;
		else if(origen.IdMonedaInstrumento.equals("CLP") && origen.IdMonedaEmisionInstrumento.equals("ICP") && origen.IdTipoReajusteInstrumento.equals("ICP"))tipoMoneda=9L;
		if(origen.IdMonedaEmisionInstrumento.equals("CLP")){
			valRescateP=origen.ValorRescate;
			valCaptacionP=origen.Monto;
			if(destino.nemo.equals("PACTO VENTA")||destino.nemo.equals("PACTO COMPRA")){
				destino.tipoVal=63L;
				destino.nemo="PAGARE NR";
			}
		}else if(!origen.IdMonedaEmisionInstrumento.equals("CLP")){
			valRescateR=origen.ValorRescate;
			valCaptacionR=origen.Monto;
			if(destino.nemo.equals("PACTO VENTA")||destino.nemo.equals("PACTO COMPRA")){
				destino.tipoVal=62L;
				destino.nemo="PAGARE R";
			}
		}
		//limpieza de variables segun definicion Documento WebService
    	switch (destino.tipoVal.intValue()){
    		case 63: // IF - Pagares no reajustables.
    			destino.fechaVcto = Fecha.toDate(origen.FechaVencimiento);
    			destino.tasaInteres = origen.Tasa;
    			destino.valRescateP = valRescateP;
    			destino.valCaptacionP = valCaptacionP;
    			destino.glosaEmisor = origen.Emisor;

				// MSH : Evita error Monto + ValorRescate + tasa a Valorizar => Solo DOS valores deben ir
    			if( destino.valRescateP != null && destino.tasaInteres != null && destino.valCaptacionP != null && 
    				destino.valRescateP.compareTo(BigDecimal.ZERO) == 1  && destino.tasaInteres.compareTo(BigDecimal.ZERO) == 1 && destino.valCaptacionP.compareTo(BigDecimal.ZERO) == 1 ) {
    				destino.valCaptacionP = null;
    			}
    							
    			//destino.numeroDias = origen.;
    			break;
    		case 62: // IF - Pagares reajustables.
    			destino.tipoMoneda = tipoMoneda;
    			destino.fechaVcto = Fecha.toDate(origen.FechaVencimiento);
    			destino.tasaInteres = origen.Tasa;
    			destino.glosaEmisor = origen.Emisor;
    			destino.valRescateR = valRescateR;
    			destino.valCaptacionR = valCaptacionR;
    			destino.valCaptacionP = valCaptacionP;
    			destino.glosaMensaje = "";
    			//destino.numeroDias = origen.NumeroDias;
    			
    			if ( valRescateR == null && valCaptacionR == null && valCaptacionP == null ) {
        			destino.valRescateR = origen.ValorRescate;
        			destino.valCaptacionR = origen.Monto;
    			}

    			// Si viene de la Mesa y esta buscando la tasa como resultado de la valorizacon...
    			if ( origen.OrigenValorizacion != null &&  origen.OrigenValorizacion.equalsIgnoreCase("MESA_UI") ) {
    				if ( origen.Tasa == null && (origen.MontoUI != null || origen.Monto != null )) {
						// llega Valor Rescate + Monto Valorizado => Quiere obtener la tasa.
	    				// desde la mesa los montos de los pagares siempre llegan en CLP's 
	    	    		destino.valCaptacionP = origen.MontoUI!=null?origen.MontoUI:origen.Monto;
	    	    		destino.valCaptacionR = null;
    				} else if ( origen.Tasa != null && origen.MontoUI != null && origen.ValorRescate == null ) {
    					// llega Tasa + Monto Valorizado => Quiere obtener los Nominales.
	    	    		destino.valCaptacionP = origen.MontoUI;
	    	    		destino.valCaptacionR = null;
    				}
    			} else {
    				// Si vienen todos los parametros dejamos ValorRescate + Tasa
    				if ( origen.Tasa != null && destino.valRescateR != null && destino.valCaptacionR != null ) {
    					destino.valCaptacionR = null;
    				}
    			}

    			
    			break;
    		case 88: // IF - Tasa flotante.
    			destino.fechaEmi = destino.fechaVal;//FALTA QA
    			destino.fechaVcto = Fecha.toDate(origen.FechaVencimiento);
    			destino.tasaInteres = origen.Tasa;
    			destino.glosaEmisor = origen.Emisor;
    			destino.cantidad = origen.Cantidad;
    			destino.valCaptacionR = valCaptacionR;
    			destino.valCaptacionP = valCaptacionP;
    			destino.valRescateR = valRescateR;
    			destino.tipoMoneda = tipoMoneda;
    			destino.glosaMensaje = "";
    			//destino.numeroDias = origen.NumeroDias;
    			destino.tasaTip = null;//FALTA QA
    			break;
    		case 64: // RF - Tasa fija.
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.Tasa;
    			destino.unidadMon = tipoMoneda.toString();  //si se cae por null pointer, las monedas del instrumento estan chuecas
    			destino.precio = origen.Precio;
    			// Si viene de la mesa el monto siempre es en CLP
    			if("MESA_UI".equalsIgnoreCase(origen.OrigenValorizacion) || "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion) ) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
    			} else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF") || origen.IdMonedaEmisionInstrumento.equals("USD")?origen.Monto:null;
    			}
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.valorBase = null;//OMITIDO
    			destino.valorPar = null;//OMITIDO
    			destino.glosaMensaje = "";
    			destino.convexidad = null;//OMITIDO
    			
    			// Si viene de la Mesa y esta buscando la tasa como resultado de la valorizacon...
    			if ( origen.OrigenValorizacion != null &&  origen.OrigenValorizacion.equalsIgnoreCase("MESA_UI") && origen.Tasa == null && origen.MontoUI != null) {
    				// Para este tipo de papeles debe ir el Monto en pesos 
    				if ( !origen.IdTipoReajusteInstrumento.equalsIgnoreCase("CLP") ) {
    	    			destino.montoPeso = origen.MontoUI;
    	    			destino.montoUF = null;
    				}
    			}
    			break;
    		case 65: // RF - Tasa flotante.	
    			destino.precio = origen.Precio;
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.Tasa;
    			if("MESA_UI".equalsIgnoreCase(origen.OrigenValorizacion) || "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion) ) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
    			}else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF")?origen.Monto:null;
    			}
    			
    			destino.unidadMon = tipoMoneda.toString();
    			destino.glosaMensaje = "";
    			destino.tasaTip = origen.Tasa;//OMITIDO
    			destino.tipoTasa = null;//OMITIDO
    			destino.valorPar = null;//OMITIDO
    			destino.calculoEst = null;//OMITIDO
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.convexidad = null;//OMITIDO
    			break;
    		case 66: // RF - CORA-PRTP.
    			destino.tasaTir = origen.Tasa;
    			destino.precio = origen.Precio;
    			if("MESA_UI".equalsIgnoreCase(origen.OrigenValorizacion) || "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion) ) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
    			}else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF")?origen.Monto:null;
    			}
    			destino.valorNom = null;//OMITIDO
    			destino.aPlazo = null;//OMITIDO
    			destino.valorRM = null;//OMITIDO
    			destino.valorPar2 = null;//OMITIDO
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.convexidad = null;//OMITIDO
    			break;
    		case 91: // RF - bonos CERO y ZERO.
    			destino.cantidad = origen.Cantidad;
    			destino.tasaTir = origen.Tasa;
    			if("MESA_UI".equalsIgnoreCase(origen.OrigenValorizacion) || "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion) ) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
        			destino.montoUS = null;
    			} else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF") || origen.IdMonedaEmisionInstrumento.equals("USD")?origen.Monto:null;
        			destino.montoUS = origen.IdMonedaEmisionInstrumento.equals("USD")?origen.Monto:null;
    			}
    			destino.unidadMon = tipoMoneda.toString();
    			destino.glosaMensaje = "";
    			destino.calculoEst = null;//OMITIDO
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.convexidad = null;//OMITIDO
    			break;
    		case 79: // RF - BR-CBR.
    			destino.precio = origen.Precio;
    			destino.tasaTir = origen.Tasa;
    			if( "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion)) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
    			}else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF")?origen.Monto:null;
    			}
    			destino.valorNom = origen.Cantidad;//OMITIDO
    			destino.valorPar2 = null;//OMITIDO
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.convexidad = null;//OMITIDO

    			// Si viene de la Mesa y esta buscando la tasa como resultado de la valorizacon...
    			if ( origen.OrigenValorizacion != null &&  origen.OrigenValorizacion.equalsIgnoreCase("MESA_UI") && origen.Tasa == null) {
    				// Solo va el Monto en pesos 
   	    			destino.montoUF = null;
    			}    			
    			break;
    		case 80: // RF - otros.
    			destino.precio = origen.Precio;
    			destino.tasaTir = origen.Tasa;
    			if("MESA_UI".equalsIgnoreCase(origen.OrigenValorizacion) || "CUSTODIA_UI".equalsIgnoreCase(origen.OrigenValorizacion) ) {
    				destino.montoPeso = origen.Monto;
        			destino.montoUF = null;
    			}else {
    				destino.montoPeso = origen.IdMonedaEmisionInstrumento.equals("CLP")?origen.Monto:null;
        			destino.montoUF = origen.IdMonedaEmisionInstrumento.equals("UF")?origen.Monto:null;
    			}
    			destino.valorNom = null;//OMITIDO
    			destino.valorPar2 = null;//OMITIDO
    			destino.mesVcto = null;//OMITIDO
    			destino.anoVcto = null;//OMITIDO
    			destino.duration = null;//OMITIDO
    			destino.convexidad = null;//OMITIDO
    			break;
    		default: // no definido.
    			logger.debug("****** TipoVal " + destino.tipoVal + " no definido *******");
    			destino.wsCallerCodSalida = "TIPO_VAL_NO_DEFINIDO";
    			destino.wsCallerDescSalida = "TipoVal " + destino.tipoVal+" no implementado";
    			break;
    	}
	}
	
	/*********************************
	 * DEPURACION!
	 *********************************/	
	public static void main(String[] args) {
		try {
			//System.out.println(Fecha.toStringDate(Fecha.toDateTime(Utils.getCurrentTimeStamp())));
			Connection con = PlataformaUtils.getDESAConnection();
			//Connection con = PlataformaUtils.getDESAPEConnection();
			//Connection con = PlataformaUtils.getQAPEConnection();
			//Connection con = PlataformaUtils.getQAConnection();
			//Connection con = PlataformaUtils.getMBConnection();
			RutaArchivosConfiguracion="";//para no ir a buscar parametro :3
			JsonParser parser = new JsonParser();
 			String jsontest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_ValorizaRV\":\"N\",\"p_ValorizaRF\":\"S\",\"p_UsaDiccionario\":\"N\",\"p_InstrumentosPQ\":[],\"p_InstrumentosWS\":[{\"IdValorizacion\":1,\"FechaValorizacion\":\"2018-03-24\",\"IdInstrumento\":273636,\"Nemotecnico\":\"BCP0450620\",\"FechaEmision\":\"2015-06-01 00:00:00.000\",\"FechaVencimiento\":\"2020-06-01\",\"IdMonedaInstrumento\":\"CLP\",\"Cantidad\":10000000,\"ValorRescate\":null,\"Precio\":340,\"Tasa\":null,\"Monto\":null,\"Emisor\":\"BCP\",\"TipoValorizacionInstrumento\":\"80\",\"IdCondicionDeLiquidacion\":\"T0\",\"IdClasificacionTipoInstrumento\":\"RF\"}],\"p_OrigenProcesamiento\":\"UIINGRESORETIROTITULOS\"}";
 			
 			JsonObject dataJsonIn = parser.parse(jsontest).getAsJsonObject();
	        //System.out.println("=== JSON ENTRADA ===");
	        // 	System.out.println(dataJsonIn);
	        //System.out.println("====================");
	        Valorizar demo=new Valorizar();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	//con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}