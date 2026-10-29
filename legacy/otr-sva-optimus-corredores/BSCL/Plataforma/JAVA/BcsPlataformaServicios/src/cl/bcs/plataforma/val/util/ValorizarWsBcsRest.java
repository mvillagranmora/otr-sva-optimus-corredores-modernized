package cl.bcs.plataforma.val.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import cl.bcs.plataforma.val.util.ValorizarWsBCS.ValorizarWsBCSObj;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.Utils;



public class ValorizarWsBcsRest {
	
	//GLOBALES 
	static final Logger logger = LogManager.getLogger(ValorizarWsBcsRest.class.getName());
	public String server;
	public String user;
	public String pass;
	public boolean depuracion = false;
	public int connTimeOut = 30000;
	public int readTimeOut = 60000;
	public int packageSize = 20;
	public int threadNumber = 5;
		
	public ValorizarWsBcsRest(String server, String user, String pass, Boolean depuracion, int connTimeOut,int readTimeOut,int packageSize,int threadNumber) {
		this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = depuracion;
    	this.connTimeOut = connTimeOut;
    	this.readTimeOut = readTimeOut;
    	this.packageSize = packageSize;
    	this.threadNumber = threadNumber;
	}

	public static class InputWsTipificador {
		public Long ID;
		public String Nemo;
		public String FechaVal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InputWsValorizador {
		public Long ID;
		public String TipoVal;
		public String Nemo;
		public String FechaVal;
		public String FechaEmi;
		public String FechaVcto;
		public String NumeroDias;
		public String TasaTir;
		public String TasaTip;
		public String TasaInteres;
		public String ValRescateP;
		public String ValRescateR;
		public String ValCaptacionP;
		public String ValCaptacionR;
		public String Cantidad;
		public String UnidadMon;
		public String Precio;
		public String MontoPeso;
		public String MontoUF;
		public String MontoUS;
		public String AnoVcto;
		public String MesVcto;
		public String Duration;
		public String ValorBase;
		public String Convexidad;
		public String ValorPar;
		public String ValorPar2;
		public String valori_e_par3;
		public String TipoTasa;
		public String CalculoEst;
		public String APlazo;
		public String ValorRM;
		public String ValorNom;
		public String TipoMon;
		public String CondLiq;
		public String Gestion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public List<OutWsObj> llamadaWsValorizacionUnitaria(List<InputWsValorizador> list) {
		String uuid = UUID.randomUUID().toString();
		Gson gson = Utils.getGsonBuilder();
		String inputString = gson.toJson(list);
		CallRestService rest = new CallRestService(this.server, this.user, this.pass, "/PTValMasivo/api/valmasivo/valmasivos", true);
		Type listType = new TypeToken<List<OutWsObj>>() {}.getType();
		try {
			logger.debug(uuid + " - Inicializando valorizacion unitaria");
			logger.trace(uuid + " " + inputString);
			String outputString = rest.callRestPost(inputString);
			List<OutWsObj> result = gson.fromJson(outputString,listType);
			logger.debug(uuid + " - Fin valorizacion unitaria");
			return result;
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<>();
	}
	
	public List<OutWsObj> llamadaWsValorizacionParalela(List<List<InputWsValorizador>> datos) {
		logger.debug("Inicio llamada ws valorizacion");
		ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
		List<CompletableFuture<List<OutWsObj>>> futures =
				datos.stream().map(t -> CompletableFuture.supplyAsync(() -> llamadaWsValorizacionUnitaria(t), executor))
				.collect(Collectors.toList());
		
		List<List<OutWsObj>> result = futures.stream().map(CompletableFuture::join)
				.collect(Collectors.toList());
		executor.shutdown();
		List<OutWsObj> res = new ArrayList<>();
		for (List<OutWsObj> list : result) {
			res.addAll(list);
		}
		logger.debug("Fin llamada ws valorizacion");
		return res;
	}
	
	public void valorizar(HashMap<Long, ValorizarWsBCSObj> listparams) throws Exception
    {
		logger.debug("Cantidad de valorizaciones " + listparams.values().size());
    	NumberFormat formatter = new DecimalFormat("#0.000000");
    	List<InputWsValorizador> inputList = new ArrayList<>();
    	List<List<InputWsValorizador>> listOfInputList = new ArrayList<>();
    	HashMap<Long,ValorizarWsBCSObj> hashDatosValorizador = new HashMap<>();
    	int index = 0;
    	
    	
    	for (ValorizarWsBCSObj dato : listparams.values()) {
    		
    		InputWsValorizador i = new InputWsValorizador();
    		i.ID = dato.idTarea;
    		i.TipoVal = dato.tipoVal == null? "?" : String.valueOf(dato.tipoVal);
    		i.Nemo = dato.nemo == null? "?" : dato.nemo;
    		i.FechaVal = dato.fechaVal == null? "?" : ValoUtils.dateToString(dato.fechaVal);
    		i.FechaEmi = dato.fechaEmi == null? "?" : ValoUtils.dateToString(dato.fechaEmi);
    		i.FechaVcto = dato.fechaVcto == null? "?" : ValoUtils.dateToString(dato.fechaVcto);
    		i.NumeroDias = dato.numeroDias == null? "?" : String.valueOf(dato.numeroDias);
    		i.TasaTir = dato.tasaTir == null? "?" : dato.tasaTir.toPlainString();
    		i.TasaTip = dato. tasaTip == null? "?" : dato.tasaTip.toPlainString();
    		i.TasaInteres = dato.tasaInteres == null? "?" : dato.tasaInteres.toPlainString();
    		i.ValRescateP = dato.valRescateP == null? "?" : String.valueOf(formatter.format(dato.valRescateP));
    		i.ValRescateR = dato.valRescateR == null? "?" : String.valueOf(formatter.format(dato.valRescateR));
    		i.ValCaptacionP = dato.valCaptacionP == null? "?" : String.valueOf(formatter.format(dato.valCaptacionP));
    		i.ValCaptacionR = dato.valCaptacionR == null? "?" : String.valueOf(formatter.format(dato.valCaptacionR));
    		i.Cantidad = dato.cantidad == null? "?" : String.valueOf(dato.cantidad);
    		i.UnidadMon = dato.unidadMon == null? "?" : dato.unidadMon;
    		i.Precio = dato.precio == null? "?" : String.valueOf(formatter.format(dato.precio));
    		i.MontoPeso = dato.montoPeso == null? "?" : String.valueOf(formatter.format(dato.montoPeso));
    		i.MontoUF = dato.montoUF == null? "?" : String.valueOf(formatter.format(dato.montoUF));
    		i.MontoUS = dato.montoUS == null? "?" : String.valueOf(formatter.format(dato.montoUS));
    		i.AnoVcto = dato.anoVcto == null? "?" : String.valueOf(formatter.format(dato.anoVcto));
    		i.MesVcto = dato.mesVcto == null? "?" : String.valueOf(formatter.format(dato.mesVcto));
    		i.Duration = dato.duration == null? "?" : String.valueOf(dato.duration);
    		i.ValorBase = dato.valorBase == null? "?" : String.valueOf(formatter.format(dato.valorBase));
    		i.Convexidad = dato.convexidad == null? "?" : String.valueOf(dato.convexidad);
    		i.ValorPar = dato.valorPar == null? "?" : String.valueOf(formatter.format(dato.valorPar));
    		i.ValorPar2 = dato.valorPar2 == null? "?" : String.valueOf(formatter.format(dato.valorPar2));
    		i.valori_e_par3 = dato.tasaTir == null? "?" : dato.tasaTir.toPlainString();
    		i.TipoTasa = dato.tipoTasa == null? "?" : dato.tipoTasa;
    		i.CalculoEst = dato.calculoEst == null? "?" : dato.calculoEst;
    		i.APlazo = dato.aPlazo == null? "?" : String.valueOf(dato.aPlazo);
    		i.ValorRM = dato.valorRM == null? "?" : String.valueOf(dato.valorRM);
    		i.ValorNom = dato.valorNom == null? "?" : String.valueOf(dato.valorNom);
    		i.TipoMon = dato.tipoMoneda == null? "?" : String.valueOf(dato.tipoMoneda);
    		i.CondLiq = dato.cLiq == null? "?" : dato.cLiq;
    		i.Gestion = dato.gestion == null? "?" : dato.gestion.equals("S")? "true" : "?";
    		hashDatosValorizador.put(i.ID, dato);
    		inputList.add(i);
    		index++;
    		if (index == packageSize) {
    			listOfInputList.add(inputList);
    			inputList = new ArrayList<>();
    			index = 0;
    		}
    	}
    	
    	if (inputList.size() > 0) {
    		listOfInputList.add(inputList);
    	}
    	
    	List<OutWsObj> salidaWS = llamadaWsValorizacionParalela(listOfInputList);
    	
    	for (OutWsObj datoSalida : salidaWS) {
    		ValorizarWsBCSObj dato = hashDatosValorizador.get(Long.valueOf(datoSalida.id));
    		dato.anoVcto = datoSalida.contenedor.AnoVcto != null? Long.valueOf(datoSalida.contenedor.AnoVcto) : dato.anoVcto;
    		dato.cantidad = datoSalida.contenedor.Cantidad != null? new BigDecimal(datoSalida.contenedor.Cantidad) : dato.cantidad;
    		dato.convexidad = datoSalida.contenedor.Convexidad != null? new BigDecimal(datoSalida.contenedor.Convexidad) : dato.convexidad;
    		dato.duration = datoSalida.contenedor.Duration != null? new BigDecimal(datoSalida.contenedor.Duration) : dato.duration;
    		dato.estado = datoSalida.contenedor.Estado != null? datoSalida.contenedor.Estado : dato.estado;
    		
    		dato.fechaEmi = datoSalida.contenedor.FechaEmi != null? ValoUtils.stringToSqlDate(datoSalida.contenedor.FechaEmi) : dato.fechaEmi;
    		dato.fechaVcto = datoSalida.contenedor.FechaVcto != null? ValoUtils.stringToSqlDate(datoSalida.contenedor.FechaVcto) : dato.fechaVcto;
    		dato.fechaVal = datoSalida.contenedor.FechaVal != null? ValoUtils.stringToSqlDate(datoSalida.contenedor.FechaVal) : dato.fechaVal;
    		dato.glosa = datoSalida.contenedor.glosa != null? datoSalida.contenedor.glosa : dato.glosa;
    		dato.glosaEmisor = datoSalida.contenedor.GlosaEmisor != null? datoSalida.contenedor.GlosaEmisor : dato.glosaEmisor;
    		dato.glosaMensaje = datoSalida.contenedor.GlosaMensaje != null? datoSalida.contenedor.GlosaMensaje : dato.glosaMensaje;
    		dato.hora = datoSalida.contenedor.hora != null? datoSalida.contenedor.hora : dato.hora;
    		dato.mesVcto = datoSalida.contenedor.MesVcto != null? Long.valueOf(datoSalida.contenedor.MesVcto) : dato.mesVcto;
    		
    		dato.montoPeso = datoSalida.contenedor.MontoPeso != null? new BigDecimal(datoSalida.contenedor.MontoPeso) : dato.montoPeso;
    		dato.montoUF = datoSalida.contenedor.MontoUF != null? new BigDecimal(datoSalida.contenedor.MontoUF) : dato.montoUF;
    		dato.montoUS = datoSalida.contenedor.MontoUS != null? new BigDecimal(datoSalida.contenedor.MontoUS) : dato.montoUS;
    		dato.nemo = datoSalida.contenedor.Nemo != null? datoSalida.contenedor.Nemo : dato.nemo;
    		dato.numeroDias = datoSalida.contenedor.NumeroDias != null? Long.valueOf(datoSalida.contenedor.NumeroDias) : dato.numeroDias;
    		
    		dato.precio = datoSalida.contenedor.Precio != null? new BigDecimal(datoSalida.contenedor.Precio) : dato.precio;
    		dato.tasaTir = datoSalida.contenedor.TasaTir != null? new BigDecimal(datoSalida.contenedor.TasaTir) : dato.tasaTir;
    		dato.tasaInteres = datoSalida.contenedor.TasaInteres != null? new BigDecimal(datoSalida.contenedor.TasaInteres) : dato.tasaInteres;
    		dato.tipoVal = datoSalida.contenedor.TipoVal != null? Long.valueOf(datoSalida.contenedor.TipoVal) : dato.tipoVal;
    		dato.tipoMoneda = datoSalida.contenedor.TipoMoneda != null? Long.valueOf(datoSalida.contenedor.TipoMoneda) : dato.tipoMoneda;
    		dato.tipoTasa = datoSalida.contenedor.TipoTasa != null? datoSalida.contenedor.TipoTasa : dato.tipoTasa;
    		
    		dato.unidadMon = datoSalida.contenedor.UnidadMon != null? datoSalida.contenedor.UnidadMon : dato.unidadMon;
    		dato.valorBase = datoSalida.contenedor.ValorBase != null? new BigDecimal(datoSalida.contenedor.ValorBase) : dato.valorBase;
    		dato.valorNom = datoSalida.contenedor.ValorNom != null? new BigDecimal(datoSalida.contenedor.ValorNom) : dato.valorNom;
    		dato.valorPar = datoSalida.contenedor.ValorPar != null? new BigDecimal(datoSalida.contenedor.ValorPar) : dato.valorPar;
    		dato.valorPar2 = datoSalida.contenedor.ValorPar2 != null? new BigDecimal(datoSalida.contenedor.ValorPar2) : dato.valorPar2;
    		
    		dato.valRescateP = datoSalida.contenedor.ValRescateP != null? new BigDecimal(datoSalida.contenedor.ValRescateP) : dato.valRescateP;
    		dato.valRescateR = datoSalida.contenedor.ValRescateR != null? new BigDecimal(datoSalida.contenedor.ValRescateR) : dato.valRescateR;
    		dato.valCaptacionP = datoSalida.contenedor.ValCaptacionP != null? new BigDecimal(datoSalida.contenedor.ValCaptacionP) : dato.valCaptacionP;
    		dato.valCaptacionR = datoSalida.contenedor.ValCaptacionR != null? new BigDecimal(datoSalida.contenedor.ValCaptacionR) : dato.valCaptacionR;
    		
    		
    		if (dato.estado.equalsIgnoreCase("2")) {
    			dato.wsCallerCodSalida = "ERROR";
    			dato.wsCallerDescSalida = dato.glosa;
    		}
    		else if (dato.estado.equalsIgnoreCase("0")){
    			dato.wsCallerCodSalida = "0";
    			dato.wsCallerDescSalida = "OK invocacion WS";
    		}
    		else {
    			dato.wsCallerCodSalida = "-3";
    			dato.wsCallerDescSalida = "Respuesta no esperada";
    		}
    			
    		
    	}
    	
    }
	
	/* TIPIFICADOR */
	public List<OutWsObj> llamadaWsTipificadorUnitaria(List<InputWsTipificador> list) {
		String uuid = UUID.randomUUID().toString();
		Gson gson = Utils.getGsonBuilder();
		String inputString = gson.toJson(list);
		CallRestService rest = new CallRestService(this.server, this.user, this.pass, "/PTValMasivo/api/valmasivo/tipificmasivos", true);
		Type listType = new TypeToken<List<OutWsObj>>() {}.getType();
		try {
			logger.debug(uuid + " - Inicializando tipificacion unitaria");
			logger.trace(uuid + " " + inputString);
			List<OutWsObj> result = gson.fromJson(rest.callRestPost(inputString),listType);
			logger.debug(uuid + " - Fin tipificacion unitaria");
			return result;
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList<>();
	}
	
	public List<OutWsObj> llamadaWsTipificarParalela(List<List<InputWsTipificador>> datos) {
		logger.debug("Inicio llamada ws tipificador");
		ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
		List<CompletableFuture<List<OutWsObj>>> futures =
				datos.stream().map(t -> CompletableFuture.supplyAsync(() -> llamadaWsTipificadorUnitaria(t), executor))
				.collect(Collectors.toList());
		
		List<List<OutWsObj>> result = futures.stream().map(CompletableFuture::join)
				.collect(Collectors.toList());
		executor.shutdown();
		List<OutWsObj> res = new ArrayList<>();
		for (List<OutWsObj> list : result) {
			res.addAll(list);
		}
		logger.debug("Fin llamada ws tipificador");
		return res;
	}
	
	public void obtenerTipoVal(List<ValorizarWsBCSObj> datos) throws Exception
    {
		logger.debug("Cantidad de tipificaciones " + datos.size());
    	List<InputWsTipificador> inputList = new ArrayList<>();
    	List<List<InputWsTipificador>> listOfInputList = new ArrayList<>();
    	HashMap<Long,ValorizarWsBCSObj> hashDatosValorizador = new HashMap<>();
    	int index = 0;
    	int max = 500;
    	for (ValorizarWsBCSObj dato : datos) {
    		
    		InputWsTipificador i = new InputWsTipificador();
    		i.ID = dato.idTarea;
    		i.Nemo = dato.nemo == null? "?" : dato.nemo;
    		i.FechaVal = dato.fechaVal == null? "?" : ValoUtils.dateToString(dato.fechaVal);
    		
    		hashDatosValorizador.put(i.ID, dato);
    		inputList.add(i);
    		index++;
    		if (index > max) {
    			listOfInputList.add(inputList);
    			inputList = new ArrayList<>();
    			index = 0;
    		}
    	}
    	
    	if (inputList.size() > 0) {
    		listOfInputList.add(inputList);
    	}
    	
    	List<OutWsObj> salidaWS = llamadaWsTipificarParalela(listOfInputList);
    	
    	for (OutWsObj datoSalida : salidaWS) {
    		ValorizarWsBCSObj dato = hashDatosValorizador.get(Long.valueOf(datoSalida.id));
    		dato.estado = datoSalida.contenedor.Estado != null? datoSalida.contenedor.Estado : dato.estado;
    		dato.fechaVal = datoSalida.contenedor.FechaVal != null? ValoUtils.stringToSqlDate(datoSalida.contenedor.FechaVal) : dato.fechaVal;
    		dato.glosa = datoSalida.contenedor.glosa != null? datoSalida.contenedor.glosa : dato.glosa;
    		dato.hora = datoSalida.contenedor.hora != null? datoSalida.contenedor.hora : dato.hora;
    		dato.nemo = datoSalida.contenedor.Nemo != null? datoSalida.contenedor.Nemo : dato.nemo;
    		dato.tipoVal = datoSalida.contenedor.TipoVal != null? Long.valueOf(datoSalida.contenedor.TipoVal) : dato.tipoVal;
    		
    		if (dato.estado.equalsIgnoreCase("2")) {
    			dato.wsCallerCodSalida = "ERROR";
    			dato.wsCallerDescSalida = dato.glosa;
    		}
    		else if (dato.estado.equalsIgnoreCase("0")){
    			dato.wsCallerCodSalida = "0";
    			dato.wsCallerDescSalida = "OK invocacion WS";
    		}
    		else {
    			dato.wsCallerCodSalida = "-3";
    			dato.wsCallerDescSalida = "Respuesta no esperada";
    		}
    			
    		
    	}
    }
	
	
	public static class OutWsObj {
		public Contenedor contenedor;
		public String id;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contenedor {
		public String TipoMoneda;
		public String GlosaMensaje;
		public String GlosaEmisor;
		public String ValorNom;
		public String TipoTasa;
		public String ValorPar2;
		public String MontoUS;
		public String ValRescateP;
		public String ValRescateR;
		public String ValCaptacionP;
		public String ValCaptacionR;
		public String TasaInteres;
		public String NumeroDias;
		public String AnoVcto;
		public String Cantidad;
		public String Convexidad;
		public String Duration;
		public String Estado;
		public String FechaEmi;
		public String FechaVal;
		public String FechaVcto;
		public String glosa;
		public String hora;
		public String MesVcto;
		public String MontoPeso;
		public String MontoUF;
		public String Nemo;
		public String Precio;
		public String TasaTir;
		public String TipoVal;
		public String UnidadMon;
		public String ValorBase;
		public String ValorPar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
}
