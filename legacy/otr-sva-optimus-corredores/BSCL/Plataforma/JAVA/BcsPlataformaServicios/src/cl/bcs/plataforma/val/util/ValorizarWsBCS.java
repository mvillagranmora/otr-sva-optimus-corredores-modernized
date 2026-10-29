package cl.bcs.plataforma.val.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;


/**
* Clase que implementa llamadas al Valorizador de Negociacion 
* mediante el consumo de WS (unitario por el momento).
* 
* @author	: Jorge Garces :: JG
* @date		: 25/07/2016
* @version	: 1.0
* 
*/
public class ValorizarWsBCS {
	//GLOBALES 
	static final Logger logger = LogManager.getLogger(ValorizarWsBCS.class.getName());
	public String server;
	public String user;
	public String pass;
	public boolean depuracion = false;
	public int connTimeOut = 30000;
	public int readTimeOut = 60000;
	
	//CLASES
	public static class ValorizarWsBCSObj{ 
		public Long 			idTarea; //llave
		public Long 			tipoVal; 				// E|S	- Tipo de valorización
		public String			nemo;					// E|S 	- Nemotécnico
		public java.sql.Date	fechaVal;				// E|S 	- Fecha de Valorización (AAAAMMDD)
		public java.sql.Date	fechaEmi;				// E|S 	- Fecha de Emisión del instrumento (AAAAMMDD)
		public java.sql.Date	fechaVcto;				// E|S 	- Fecha de Vencimiento del instrumento (AAAAMMDD)
		public Long				numeroDias;				// E|S 	- N° Días al Vencimiento del instrumento
		public BigDecimal		tasaTir;				// E|S 	- Single Tasa Interna Retorno, Real Anual (IIF, IRF) Single => BIGDECIMAL
		public BigDecimal		tasaTip;				// E|S 	- Single Tasa Flotante Estimada (IIF, IRF)
		public BigDecimal		tasaInteres;			// E|S 	- Single Tasa Interés (IIF)
		public BigDecimal		valRescateP;			// E|S 	- Valor Rescate Pesos a fecha valorización
		public BigDecimal		valRescateR;			// E|S 	- Valor Rescate en U.F. a fecha valorización
		public BigDecimal		valCaptacionP;			// E|S 	- Valor Captación Pesos a fecha valorización
		public BigDecimal		valCaptacionR;			// E|S 	- Valor Captación U.F. a fecha valorización
		public String			glosaEmisor;			// E|S 	- Descripción Emisor
		public String			glosaMensaje;			// E|S 	- Glosa mensaje valorización
		public BigDecimal		cantidad;				// E|S 	- Cantidad (unidades)
		public String			unidadMon;				// E|S 	- Descripción Unidad Monetaria
		public BigDecimal		precio;					// E|S 	- Precio del instrumento
		public BigDecimal		montoPeso;				// E|S 	- Monto en Pesos a fecha valorización
		public BigDecimal		montoUF;				// E|S 	- Monto en U.F. a fecha valorización
		public BigDecimal		montoUS;	 			// E|S 	- Monto en US$ a fecha valorización
		public Long 			anoVcto;				// E|S 	- Año vencimiento del instrumento
		public Long				mesVcto;				// E|S 	- Mes vencimiento del instrumento
		public BigDecimal		duration;				// E|S 	- Valor Duration Macaulay
		public BigDecimal		valorBase;				// E|S 	- Valor Base
		public BigDecimal		convexidad;				// E|S 	- Valor Convexividad
		public BigDecimal		valori_e_par3;				// E|S 	- Valor Par en Pesos
		public BigDecimal		valorPar2;				// E|S 	- Valor Par en Pesos
		public BigDecimal		valorPar;				// E|S 	- Valor Par
		public String			tipoTasa;				// E|S 	- Descripción Tipo Tasa
		public String			calculoEst;				// E|S 	- Indice Cálculo Estimado
		public Long				aPlazo;					// E|S 	- Plazo en años del instrumento
		public BigDecimal 		valorRM;				// E|S 	- Valor Inflación Promedio Anual
		public BigDecimal 		valorNom;				// E|S 	- Valor Nominal
		public Long 			tipoMoneda;				// E|S 	- Código Tipo Moneda: 1=UF, 2=US$
		public String			cLiq;					// E|  	- Condición de Liquidación (PH, PM, CN)
		public String			gestion;				// E|  	- Pendiente de inyectar
		public String 			estado;					//  |S 	- Resultado de la consulta (0, 1, 2)
		public String			glosa;					//  |S 	- Descripcion
		public String			hora;					//  |S 	- FechaHora de la información (AAAAMMDDHHMMSSCC) 
		//------- VARIABLES INTERNAS -------	
		public String 			wsCallerCodSalida;		//  |S 	- WRAPPER - Salida producto invocación del WS.
		public String			wsCallerDescSalida; 	//  |S 	- WRAPPER - Salida producto invocación del WS.
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    /*
     * llamaWSValorizador
     * Constructor de la clase.
     */
    public ValorizarWsBCS(){
    }   
    
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear modo depuracion.
     */
    public ValorizarWsBCS(boolean depuracion){
    	this.depuracion = depuracion;
    } 
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion
     */
    public ValorizarWsBCS(String server, String user, String pass){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    } 
    /*
     * llamaWSValorizador
     * Constructor de la clase. Permite setear datos conexion y modo
     */
    public ValorizarWsBCS(String server, String user, String pass, boolean depuracion, int connTimeOut, int readTimeOut){
    	this.server = server;
    	this.user = user;
    	this.pass = pass;
    	this.depuracion = depuracion;
    	this.connTimeOut = connTimeOut;
    	this.readTimeOut = readTimeOut;
    } 
	
    public boolean llamadaWsTipificar(ValorizarWsBCSObj item) {
  	
		try{
		   	Map<String,Object> mp = obtenerTipoValMAP(item);
		   	procesaMAPTipoVal(item,mp);
		}
		catch(Exception e){ 
			item.wsCallerCodSalida = "- 1";
			String descError = e.getMessage(); 
			item.wsCallerDescSalida = descError; 
			logger.error("Error WS SOAPRequest: " + e.getMessage());
		}
		
		return true;
  }
    
    public void obtenerTipoVal(List<ValorizarWsBCSObj> datos) throws Exception
    {
    	long start = System.nanoTime();
    	
    	depuracion = false;
		  ExecutorService executor = Executors.newFixedThreadPool(Math.min(datos.size(), 30));
		  List<CompletableFuture<Boolean>> futures =
				  datos.stream()
		           .map(t -> CompletableFuture.supplyAsync(() -> llamadaWsTipificar(t), executor))
		           .collect(Collectors.toList());
		 
		  List<Boolean> result =
		      futures.stream()
		             .map(CompletableFuture::join)
		             .collect(Collectors.toList());
		  executor.shutdown();
    	
    	long duration = (System.nanoTime() - start) / 1_000_000;
    	System.out.printf("Tiempo de tipificacion: %d (ms), Cantidad de registros: %d\n", duration,datos.size());
    }
    
    /* 
     * Invoca a WS de Valorización
     * @param ValorizarWsBCSObj Objetos de Valorizacion..
     */
    
    public boolean llamadaWsValorizacion(ValorizarWsBCSObj item) {
    	try{
			if(item.tipoVal == null || item.tipoVal.equals(-1L)){
				item.wsCallerCodSalida = "ERRSINTIPOVAL";
				item.wsCallerDescSalida = "Instrumento sin TipoVal, no se valoriza";
			}else{
				Map<String,Object> mp = obtenerValorizadorMAP(item);
		    	procesaMAPValorizador(item,mp);
			}
			
		}
		catch(Exception e){ 
			item.wsCallerCodSalida = "ERRSOAPREQUEST";
			String descError = e.getMessage(); 
			item.wsCallerDescSalida = descError; 
			logger.error("Error WS SOAPRequest: " + e.getMessage());
		} 
		
		return true;
  }
    
    public void valorizar(List<ValorizarWsBCSObj> datos) throws Exception
    {
    	long start = System.nanoTime();
    	
    	depuracion = false;
    	if(this.server.toUpperCase().startsWith("HTTPS") && depuracion) trust();
		
    	String f = String.format("Valorizando %d registros\n", datos.size());
    	logger.debug(f);
		
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(datos.size(), 10));
		List<CompletableFuture<Boolean>> futures =
				datos.stream().map(t -> CompletableFuture.supplyAsync(() -> llamadaWsValorizacion(t), executor))
				.collect(Collectors.toList());
		 
		List<Boolean> result = futures.stream().map(CompletableFuture::join)
				.collect(Collectors.toList());
		executor.shutdown();
    	
    	long duration = (System.nanoTime() - start) / 1_000_000;
    	f = String.format("Tiempo de valorizacion: %d (ms), Cantidad de registros: %d\n", duration,datos.size());
    	logger.debug(f);
    }
     
    //---------------------
  	// METODOS PRIVADOS
  	//---------------------
	private Map<String, Object> obtenerTipoValMAP(ValorizarWsBCSObj entrada) throws Exception
	{
		Map<String,Object> mp =  createSOAPRequest(entrada,this.server,"/PTValuerTester/ValuerWebService","TipoVal",this.user,this.pass); 
		return mp;
	}
	
	private Map<String, Object> obtenerValorizadorMAP(ValorizarWsBCSObj entrada) throws Exception
	{
		Map<String,Object> mp =  createSOAPRequest(entrada,this.server,"/PTValuerTester/ValuerWebService","Valorizador",this.user,this.pass); 
		return mp;
	}

	private Map<String, Object> createSOAPRequest(Object entrada, String urlServer, String urlWS, String localName, String username, String password) throws Exception {
		SOAPConnection soapConnection = null;
		depuracion = true;
		Map<String, Object> mp = new HashMap<String, Object>();
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("ws", "http://ws.valuer.pt.bcs.cl/");
			// BODY
			SOAPElement soapBodyElem1 = null;
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyElem = obtenerSOAPBodyElement(localName, soapBody, entrada);
			// HEADERS
			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", "http://ws.valuer.pt.bcs.cl/");
			// SEGURIDAD
			String cred = username + ":" + password;
			byte[] credByte = cred.getBytes("UTF-8");
			String authorization = DatatypeConverter.printBase64Binary(credByte);
			// String(Base64.getEncoder().encode(cred.getBytes()));
			headers.addHeader("Authorization", "Basic " + authorization);
			soapMessage.saveChanges();
			// IMPRIMIR ENTRADA
			if (depuracion) {
				ByteArrayOutputStream streamReqSoap = new ByteArrayOutputStream();
				soapMessage.writeTo(streamReqSoap);
				String messageReqSoap = new String(streamReqSoap.toByteArray(), "utf-8"); 
				//soapMessage.writeTo(System.out);
				logger.debug("\nRequest SOAP Message:" + messageReqSoap);
			}
			// CREAR CONEXION
			final int connTimeOut = this.connTimeOut;
			final int readTimeOut = this.readTimeOut;
			
			if (depuracion) {
				logger.debug(": SOAP CONNECTION :" + urlServer + " WS:" + urlWS);
				logger.debug(": SOAP TIMEOUT CONNECTION :" + connTimeOut + " SOAP TIMEOUT READ :" + readTimeOut);
			}
			if (depuracion) {
				try {
					logger.debug(": SOAP PING START");
					long time = System.currentTimeMillis();
					URL website = new URL(urlServer + urlWS);
					URLConnection connection = website.openConnection();
					connection.setConnectTimeout(connTimeOut);
					connection.setReadTimeout(readTimeOut);
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					StringBuilder response = new StringBuilder();
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
					in.close();
					if (response != null && response.toString().contains("cl.bcs.pt.valuer.ws.ValuerWeb")) {
						logger.debug(": SOAP PING OK");
					} else {
						if (response != null)
							logger.debug(": SOAP PING ERROR:\n" + response.toString());
					}
					time = System.currentTimeMillis() - time;
					logger.debug(": SOAP PING END MSEG=" + time);
				} catch (Exception er) {
					logger.debug(": SOAP PING ERROR:" + er.getMessage());
					er.printStackTrace();
				}
			}
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
			URLStreamHandler urlHandler = new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
					connection.setConnectTimeout(connTimeOut);
					connection.setReadTimeout(readTimeOut);
					return (connection);
				}
			};
			URL endpoint = new URL(new URL(urlServer), urlWS, urlHandler);
			if (depuracion)
				logger.debug(": SOAP CALL");
			SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);
			if (depuracion)
				logger.debug(": SOAP REPLY");
			// IMPRIMIR SALIDA
			if (depuracion) {
				StringWriter writer = new StringWriter();
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				Source sourceContent = soapResponse.getSOAPPart().getContent();
				StreamResult result = new StreamResult(writer); //StreamResult result = new StreamResult(System.out);
				transformer.transform(sourceContent, result);
				String strResult = writer.toString();
				logger.debug("\nResponse SOAP Message = " + strResult);//log.write();
			}
			// PROCESAR RESPUESTA Y OBTENER VALORES.
			SOAPBody body = soapResponse.getSOAPBody();
			Iterator it = body.getChildElements();
			String sError = null;
			String SData = null;
			if (it.hasNext()) {
				SOAPElement ele1 = (SOAPElement) it.next();
				it = ele1.getChildElements();
				if (it.hasNext()) {
					ele1 = (SOAPElement) it.next();
					it = ele1.getChildElements();
					while (it.hasNext()) {
						ele1 = (SOAPElement) it.next(); // data
						mp.put(ele1.getNodeName(), ele1.getValue());
						if (depuracion) {
							logger.debug("Dato Recuperado: " + ele1.getNodeName() + ", " + ele1.getValue());
						}
						if (ele1.getNodeName().equalsIgnoreCase("sError"))
							sError = ele1.getValue();
						if (ele1.getNodeName().equalsIgnoreCase("SData"))
							SData = ele1.getValue();
					}
				}
			}
			if (SData != null && sError != null) {
				mp.put("data", SData);
				mp.put("error", sError);
			}
			return mp;
		} catch (Exception er) {
			logger.debug(": createSOAPRequest Exception:");
			logger.debug(Utils.stackTraceToString(er));
			throw er;
			//throw new Exception(er);
		} finally {
			soapConnection.close();
		}
	}
	
	private SOAPElement obtenerSOAPBodyElement(String localName, SOAPBody soapBody, Object entradaGenerica) throws Exception { 
		SOAPElement soapBodyElem1=null;
		SOAPElement soapBodyElem = soapBody.addChildElement(localName, "ws");
		if(localName == "TipoVal") {
			ValorizarWsBCSObj entrada = (ValorizarWsBCSObj)entradaGenerica;
	        soapBodyElem1 = soapBodyElem.addChildElement("nemo"); soapBodyElem1.addTextNode(entrada.nemo); 
	        soapBodyElem1 = soapBodyElem.addChildElement("fechaVal"); soapBodyElem1.addTextNode((entrada.fechaVal!=null)?ValoUtils.dateToString(entrada.fechaVal):"?");

		}
		else if(localName == "Valorizador") {
			NumberFormat formatter = new DecimalFormat("#0.00");     
			ValorizarWsBCSObj entrada = (ValorizarWsBCSObj)entradaGenerica;
			soapBodyElem1 = soapBodyElem.addChildElement("tipoVal"); soapBodyElem1.addTextNode((entrada.tipoVal!=null)?String.valueOf(entrada.tipoVal):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("nemo"); soapBodyElem1.addTextNode((entrada.nemo!=null)?entrada.nemo:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("fechaVal"); soapBodyElem1.addTextNode((entrada.fechaVal!=null)?ValoUtils.dateToString(entrada.fechaVal):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("fechaEmi"); soapBodyElem1.addTextNode((entrada.fechaEmi!=null)?ValoUtils.dateToString(entrada.fechaEmi):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("fechaVcto"); soapBodyElem1.addTextNode((entrada.fechaVcto!=null)?ValoUtils.dateToString(entrada.fechaVcto):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("numeroDias"); soapBodyElem1.addTextNode((entrada.numeroDias!=null)?String.valueOf(entrada.numeroDias):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("tasaTir"); soapBodyElem1.addTextNode((entrada.tasaTir!=null)?entrada.tasaTir.toPlainString():"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("tasaTip"); soapBodyElem1.addTextNode((entrada.tasaTip!=null)?entrada.tasaTip.toPlainString():"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("tasaInteres"); soapBodyElem1.addTextNode((entrada.tasaInteres!=null)?entrada.tasaInteres.toPlainString():"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valRescateP"); soapBodyElem1.addTextNode((entrada.valRescateP!=null)?String.valueOf(formatter.format(entrada.valRescateP)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valRescateR"); soapBodyElem1.addTextNode((entrada.valRescateR!=null)?String.valueOf(formatter.format(entrada.valRescateR)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valCaptacionP"); soapBodyElem1.addTextNode((entrada.valCaptacionP!=null)?String.valueOf(formatter.format(entrada.valCaptacionP)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valCaptacionR"); soapBodyElem1.addTextNode((entrada.valCaptacionR!=null)?String.valueOf(formatter.format(entrada.valCaptacionR)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("glosaEmisor"); soapBodyElem1.addTextNode((entrada.glosaEmisor!=null)?entrada.glosaEmisor:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("glosaMensaje"); soapBodyElem1.addTextNode((entrada.glosaMensaje!=null)?entrada.glosaMensaje:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("cantidad"); soapBodyElem1.addTextNode((entrada.cantidad!=null)?String.valueOf(entrada.cantidad):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("unidadMon"); soapBodyElem1.addTextNode((entrada.unidadMon!=null)?entrada.unidadMon:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("precio"); soapBodyElem1.addTextNode((entrada.precio!=null)?String.valueOf(formatter.format(entrada.precio)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("montoPeso"); soapBodyElem1.addTextNode((entrada.montoPeso!=null)?String.valueOf(formatter.format(entrada.montoPeso)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("montoUF"); soapBodyElem1.addTextNode((entrada.montoUF!=null)?String.valueOf(formatter.format(entrada.montoUF)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("montoUS"); soapBodyElem1.addTextNode((entrada.montoUS!=null)?String.valueOf(formatter.format(entrada.montoUS)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("anoVcto"); soapBodyElem1.addTextNode((entrada.anoVcto!=null)?String.valueOf(formatter.format(entrada.anoVcto)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("mesVcto"); soapBodyElem1.addTextNode((entrada.mesVcto!=null)?String.valueOf(formatter.format(entrada.mesVcto)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("duration"); soapBodyElem1.addTextNode((entrada.duration!=null)?String.valueOf(entrada.duration):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valorBase"); soapBodyElem1.addTextNode((entrada.valorBase!=null)?String.valueOf(formatter.format(entrada.valorBase)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("convexidad"); soapBodyElem1.addTextNode((entrada.convexidad!=null)?String.valueOf(entrada.convexidad):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valorPar2"); soapBodyElem1.addTextNode((entrada.valorPar2!=null)?String.valueOf(formatter.format(entrada.valorPar2)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valorPar"); soapBodyElem1.addTextNode((entrada.valorPar!=null)?String.valueOf(formatter.format(entrada.valorPar)):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("tipoTasa"); soapBodyElem1.addTextNode((entrada.tipoTasa!=null)?entrada.tipoTasa:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("calculoEst"); soapBodyElem1.addTextNode((entrada.calculoEst!=null)?entrada.calculoEst:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("aPlazo"); soapBodyElem1.addTextNode((entrada.aPlazo!=null)?String.valueOf(entrada.aPlazo):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valorRM"); soapBodyElem1.addTextNode((entrada.valorRM!=null)?String.valueOf(entrada.valorRM):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("valorNom"); soapBodyElem1.addTextNode((entrada.valorNom!=null)?String.valueOf(entrada.valorNom):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("tipoMoneda"); soapBodyElem1.addTextNode((entrada.tipoMoneda!=null)?String.valueOf(entrada.tipoMoneda):"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("cLiq"); soapBodyElem1.addTextNode((entrada.cLiq!=null)?entrada.cLiq:"?");
	        soapBodyElem1 = soapBodyElem.addChildElement("gestion"); soapBodyElem1.addTextNode((entrada.gestion!=null)?((entrada.gestion.equals("S"))?"true":"?"):"?"); // SG o true
		}
		else {
			logger.error("Error localName");
			throw new Exception("Error localName");
		}
		return soapBodyElem;
	}
	
	 private void procesaMAPTipoVal(ValorizarWsBCSObj ValorizarWsBCSObj, Map<String, Object> mapTipoVal) 
	 {
		 try { // para el caso de no poder procesar la respuesta.
			 ValorizarWsBCSObj.estado 		= (mapTipoVal.get("Estado")==null)?ValorizarWsBCSObj.estado:(String)mapTipoVal.get("Estado");
			 ValorizarWsBCSObj.glosa 		= (mapTipoVal.get("Glosa")==null)?ValorizarWsBCSObj.glosa:(String)mapTipoVal.get("Glosa");
			 ValorizarWsBCSObj.hora 		= (mapTipoVal.get("Hora")==null)?ValorizarWsBCSObj.hora:(String)mapTipoVal.get("Hora");
			 ValorizarWsBCSObj.nemo 		= (mapTipoVal.get("Nemo")==null)?ValorizarWsBCSObj.nemo:(String)mapTipoVal.get("Nemo");
			 ValorizarWsBCSObj.fechaVal 	= (mapTipoVal.get("FechaVal")==null)?ValorizarWsBCSObj.fechaVal:ValoUtils.stringToSqlDate((String)mapTipoVal.get("FechaVal"));
			 ValorizarWsBCSObj.tipoVal 		= (mapTipoVal.get("TipoVal")==null)?ValorizarWsBCSObj.tipoVal:Long.valueOf((String)mapTipoVal.get("TipoVal"));
			//Evaluacion de error.
			 if(mapTipoVal.get("error")!=null && mapTipoVal.get("error").toString().length()>0) {
				 ValorizarWsBCSObj.wsCallerCodSalida  = (String)mapTipoVal.get("error");
				 ValorizarWsBCSObj.wsCallerDescSalida = "Error invocacion WS";
			 }
			 else {
				 ValorizarWsBCSObj.wsCallerCodSalida  = "0";
				 ValorizarWsBCSObj.wsCallerDescSalida = "OK invocacion WS";
			 }
		 }
		 catch(Exception e){
			 ValorizarWsBCSObj.wsCallerCodSalida  = "-3";
			 ValorizarWsBCSObj.wsCallerDescSalida = "Respuesta no esperada";
		 }
	 }
	 
	 private void procesaMAPValorizador(ValorizarWsBCSObj ValorizarWsBCSObj, Map<String, Object> mapValorizador) 
	 {
		 try { // para el caso de no poder procesar la respuesta.
			 ValorizarWsBCSObj.estado 			= (mapValorizador.get("Estado")==null)?ValorizarWsBCSObj.estado:(String)mapValorizador.get("Estado");											// Resultado de la consulta (0, 1, 2)
			 ValorizarWsBCSObj.glosa 			= (mapValorizador.get("glosa")==null)?ValorizarWsBCSObj.glosa:(String)mapValorizador.get("glosa");												// Descripcion
			 ValorizarWsBCSObj.hora 			= (mapValorizador.get("hora")==null)?ValorizarWsBCSObj.hora:(String)mapValorizador.get("hora");												// FechaHora de la información (AAAAMMDDHHMMSSCC)
			 ValorizarWsBCSObj.tipoVal 			= (mapValorizador.get("TipoVal")==null)?ValorizarWsBCSObj.tipoVal:Long.valueOf((String)mapValorizador.get("TipoVal"));							// Tipo de valorización
			 ValorizarWsBCSObj.nemo 			= (mapValorizador.get("Nemo")==null)?ValorizarWsBCSObj.nemo:(String)mapValorizador.get("Nemo");												// Nemotécnico
			 ValorizarWsBCSObj.fechaVal 		= (mapValorizador.get("FechaVal")==null)?ValorizarWsBCSObj.fechaVal:ValoUtils.stringToSqlDate((String)mapValorizador.get("FechaVal"));						// Fecha de Valorización (AAAAMMDD)
			 ValorizarWsBCSObj.fechaEmi 		= (mapValorizador.get("FechaEmi")==null)?ValorizarWsBCSObj.fechaEmi:ValoUtils.stringToSqlDate((String)mapValorizador.get("FechaEmi")); 						// Fecha de Emisión del instrumento (AAAAMMDD)
			 ValorizarWsBCSObj.fechaVcto 		= (mapValorizador.get("FechaVcto")==null)?ValorizarWsBCSObj.fechaVcto:ValoUtils.stringToSqlDate((String)mapValorizador.get("FechaVcto"));					// Fecha de Vencimiento del instrumento (AAAAMMDD)
			 ValorizarWsBCSObj.numeroDias 		= (mapValorizador.get("NumeroDias")==null)?ValorizarWsBCSObj.numeroDias:Long.valueOf((String)mapValorizador.get("NumeroDias"));				// N° Días al Vencimiento del instrumento
			 ValorizarWsBCSObj.tasaTir 			= (mapValorizador.get("TasaTir")==null)?ValorizarWsBCSObj.tasaTir:new BigDecimal((String)mapValorizador.get("TasaTir"));						// Single	Tasa Interna Retorno, Real Anual (IIF, IRF)
			 ValorizarWsBCSObj.tasaTip 			= (mapValorizador.get("TasaTip")==null)?ValorizarWsBCSObj.tasaTip:new BigDecimal((String)mapValorizador.get("TasaTip"));						// Single	Tasa Flotante Estimada (IIF, IRF)
			 ValorizarWsBCSObj.tasaInteres 		= (mapValorizador.get("TasaInteres")==null)?ValorizarWsBCSObj.tasaInteres:new BigDecimal((String)mapValorizador.get("TasaInteres"));			// Single	Tasa Interés (IIF)
			 ValorizarWsBCSObj.valRescateP 		= (mapValorizador.get("ValRescateP")==null)?ValorizarWsBCSObj.valRescateP:new BigDecimal((String)mapValorizador.get("ValRescateP"));			// Valor Rescate Pesos a fecha valorización
			 ValorizarWsBCSObj.valRescateR 		= (mapValorizador.get("ValRescateR")==null)?ValorizarWsBCSObj.valRescateR:new BigDecimal((String)mapValorizador.get("ValRescateR"));			// Valor Rescate en U.F. a fecha valorización
			 ValorizarWsBCSObj.valCaptacionP 	= (mapValorizador.get("ValCaptacionP")==null)?ValorizarWsBCSObj.valCaptacionP:new BigDecimal((String)mapValorizador.get("ValCaptacionP"));		// Valor Captación Pesos a fecha valorización
			 ValorizarWsBCSObj.valCaptacionR 	= (mapValorizador.get("ValCaptacionR")==null)?ValorizarWsBCSObj.valCaptacionR:new BigDecimal((String)mapValorizador.get("ValCaptacionR"));		// Valor Captación U.F. a fecha valorización
			 ValorizarWsBCSObj.glosaEmisor 		= (mapValorizador.get("GlosaEmisor")==null)?ValorizarWsBCSObj.glosaEmisor:(String)mapValorizador.get("GlosaEmisor");							// Descripción Emisor
			 ValorizarWsBCSObj.glosaMensaje 	= (mapValorizador.get("GlosaMensaje")==null)?ValorizarWsBCSObj.glosaMensaje:(String)mapValorizador.get("GlosaMensaje");						// Glosa mensaje valorización
			 ValorizarWsBCSObj.cantidad 		= (mapValorizador.get("Cantidad")==null)?ValorizarWsBCSObj.cantidad:new BigDecimal((String)mapValorizador.get("Cantidad"));					// Cantidad (unidades)
			 ValorizarWsBCSObj.unidadMon 		= (mapValorizador.get("UnidadMon")==null)?ValorizarWsBCSObj.unidadMon:(String)mapValorizador.get("UnidadMon");									// Descripción Unidad Monetaria
			 ValorizarWsBCSObj.precio 			= (mapValorizador.get("Precio")==null)?ValorizarWsBCSObj.precio:new BigDecimal((String)mapValorizador.get("Precio"));							// Precio del instrumento
			 ValorizarWsBCSObj.montoPeso 		= (mapValorizador.get("MontoPeso")==null)?ValorizarWsBCSObj.montoPeso:new BigDecimal((String)mapValorizador.get("MontoPeso"));					// Monto en Pesos a fecha valorización
			 ValorizarWsBCSObj.montoUF 			= (mapValorizador.get("MontoUF")==null)?ValorizarWsBCSObj.montoUF:new BigDecimal((String)mapValorizador.get("MontoUF"));						// Monto en U.F. a fecha valorización
			 ValorizarWsBCSObj.montoUS 			= (mapValorizador.get("MontoUS")==null)?ValorizarWsBCSObj.montoUS:new BigDecimal((String)mapValorizador.get("MontoUS"));						// Monto en US$ a fecha valorización
			 ValorizarWsBCSObj.anoVcto 			= (mapValorizador.get("AnoVcto")==null)?ValorizarWsBCSObj.anoVcto:Long.valueOf((String)mapValorizador.get("AnoVcto"));							// Año vencimiento del instrumento
			 ValorizarWsBCSObj.mesVcto 			= (mapValorizador.get("MesVcto")==null)?ValorizarWsBCSObj.mesVcto:Long.valueOf((String)mapValorizador.get("MesVcto"));							// Mes vencimiento del instrumento
			 ValorizarWsBCSObj.duration 		= (mapValorizador.get("Duration")==null)?ValorizarWsBCSObj.duration:new BigDecimal((String)mapValorizador.get("Duration"));					// Valor Duration Macaulay
			 ValorizarWsBCSObj.valorBase 		= (mapValorizador.get("ValorBase")==null)?ValorizarWsBCSObj.valorBase:new BigDecimal((String)mapValorizador.get("ValorBase"));					// Valor Base
			 ValorizarWsBCSObj.convexidad 		= (mapValorizador.get("Convexidad")==null)?ValorizarWsBCSObj.convexidad:new BigDecimal((String)mapValorizador.get("Convexidad"));				// Valor Convexividad
			 ValorizarWsBCSObj.valorPar2 		= (mapValorizador.get("ValorPar2")==null)?ValorizarWsBCSObj.valorPar2:new BigDecimal((String)mapValorizador.get("ValorPar2"));					// Valor Par en Pesos
			 ValorizarWsBCSObj.valorPar 		= (mapValorizador.get("ValorPar")==null)?ValorizarWsBCSObj.valorPar:new BigDecimal((String)mapValorizador.get("ValorPar"));					// Valor Par
			 ValorizarWsBCSObj.tipoTasa 		= (mapValorizador.get("TipoTasa")==null)?ValorizarWsBCSObj.tipoTasa:(String)mapValorizador.get("TipoTasa");									// Descripción Tipo Tasa
			 ValorizarWsBCSObj.calculoEst 		= (mapValorizador.get("CalculoEst")==null)?ValorizarWsBCSObj.calculoEst:(String)mapValorizador.get("CalculoEst");								// Indice Cálculo Estimado
			 ValorizarWsBCSObj.aPlazo 			= (mapValorizador.get("Aplazo")==null)?ValorizarWsBCSObj.aPlazo:Long.valueOf((String)mapValorizador.get("Aplazo"));							// Plazo en años del instrumento
			 ValorizarWsBCSObj.valorRM 			= (mapValorizador.get("ValorRM")==null)?ValorizarWsBCSObj.valorRM:new BigDecimal((String)mapValorizador.get("ValorRM"));						// Valor Inflación Promedio Anual
			 ValorizarWsBCSObj.valorNom 		= (mapValorizador.get("ValorNom")==null)?ValorizarWsBCSObj.valorNom:new BigDecimal((String)mapValorizador.get("ValorNom")); 					// Valor Nominal
			 ValorizarWsBCSObj.tipoMoneda 		= (mapValorizador.get("TipoMoneda")==null)?ValorizarWsBCSObj.tipoMoneda:Long.valueOf((String)mapValorizador.get("TipoMoneda"));				// Código Tipo Moneda: 1=UF, 2=US$
			 //Evaluacion de error.
			 if(mapValorizador.get("error")!=null && mapValorizador.get("error").toString().length()>0) {
				 ValorizarWsBCSObj.wsCallerCodSalida  = (String)mapValorizador.get("error");
				 ValorizarWsBCSObj.wsCallerDescSalida = "Error invocacion WS";
			 }
			 else if(mapValorizador.get("Estado")!=null && mapValorizador.get("Estado").equals("2")) {
				 ValorizarWsBCSObj.wsCallerCodSalida  = "ERROR";
				 ValorizarWsBCSObj.wsCallerDescSalida = (String)mapValorizador.get("glosa");
			 }
			 else {
				 ValorizarWsBCSObj.wsCallerCodSalida  = "0";
				 ValorizarWsBCSObj.wsCallerDescSalida = "OK invocacion WS";
			 }
		 }
		 catch(Exception e){
			 ValorizarWsBCSObj.wsCallerCodSalida  = "-3";
			 ValorizarWsBCSObj.wsCallerDescSalida = "Respuesta no esperada";
		 }
	 }

	public static void trust() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts;
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
        try { // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        }
    }
	
	 
	/*
	 * main: Utilizado para Pruebas.
	 */
    public static void main(String args[]) {
    	Log log = new Log("TEST_CallerWSValorizador");
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2018);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 28);
    	try {
    		ValorizarWsBCS test = new ValorizarWsBCS("http://tec.bcs.cl:80","RCERDA-WEB","1",true,30000,60000); // param true habilita log.
    		//PRUEBA 1 - TIPIFICAR
    		ValorizarWsBCSObj itemTipificar = new ValorizarWsBCSObj();
    		itemTipificar.nemo="PAGARE R";
    		itemTipificar.fechaVal=new java.sql.Date(cal.getTime().getTime());
    		List<ValorizarWsBCSObj> listaTipificar = new ArrayList<ValorizarWsBCSObj>();
    		listaTipificar.add(itemTipificar);
    		log.write("Inicio Tipificar");
    		test.obtenerTipoVal(listaTipificar);
    		log.writeAsJson("Resultado Tipificacion",listaTipificar);
    		//PRUEBA 2 - VALORIZAR
    		

        	ValorizarWsBCSObj itemValorizar = new ValorizarWsBCSObj();
        	itemValorizar.tipoVal = new Long(79);
        	itemValorizar.nemo = "BR41250525";
        	
        	itemValorizar.fechaVal=new java.sql.Date(cal.getTime().getTime());
        	itemValorizar.cLiq="PH";        	
          	
        	//itemValorizar.tasaTir=BigDecimal.valueOf(2.4);
        	itemValorizar.tasaTir=null;
        	itemValorizar.tasaInteres=null;
        	itemValorizar.tasaTip=null;        	
        	itemValorizar.precio=null;
        	
          	itemValorizar.valorNom=BigDecimal.valueOf(45000);
        	itemValorizar.montoPeso = BigDecimal.valueOf(4224414);          	

    		
    		List<ValorizarWsBCSObj> listaValorizar = new ArrayList<ValorizarWsBCSObj>();
    		listaValorizar.add(itemValorizar);
    		log.write("Inicio Valorizar");
    		test.valorizar(listaValorizar);
    		log.writeAsJson("Salida Valorizacion",listaValorizar);
    	}
    	catch(Throwable e){
    		e.printStackTrace();
    		System.out.println(e);
    		System.out.println(e.getMessage());
    	}
    }

}
