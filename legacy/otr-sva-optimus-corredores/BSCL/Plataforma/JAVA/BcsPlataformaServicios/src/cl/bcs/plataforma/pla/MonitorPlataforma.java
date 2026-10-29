package cl.bcs.plataforma.pla;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.dte.CallerWSDte;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar.ModuloEmpresa;
import cl.bcs.plataforma.val.ValorizarWs;
import cl.bcs.plataforma.val.util.ValoUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/*************************************
 * @version 	1.2
 * @author 		jgarces
 * SP para monitorear funcionalidades
 * del sistema:
 * - Valorización
 * - Login a DTE
 * - Comparar fechas sistema->máquina
 * 
**************************************/
public class MonitorPlataforma implements JavaCall {	
	private final Log log = new Log("MonitorPlataforma");
	/*******************
	 * clases in/out
	 *******************/
	public static class InMonitorPlataforma {
		public Long 	p_IdEmpresa;
		public String	p_NotificarAdministradoresTI; // Si=S;No=N;Forzar=F
	}
	
	public static class OutMonitorPlataforma {
		public Long 		P_OUT_ERRORESCONTADOR;
		public List<Detalle> P_DETALLE;
		public OutMonitorPlataforma(){
			P_DETALLE = new ArrayList<Detalle>();
		}
		public static class Detalle{
			public String clasificacion;
			public String subClasificacion;
			public String codigoValidacion;
			public String descripcionValidacion;
			public Detalle(){}
			public Detalle(String clasificacion,String subClasificacion,String codigoValidacion,String descripcionValidacion){
				this.clasificacion=clasificacion;
				this.subClasificacion=subClasificacion;
				this.codigoValidacion=codigoValidacion;
				this.descripcionValidacion=descripcionValidacion;
			}
		}
		
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InMonitorPlataforma in=gson.fromJson(jin.toString() ,InMonitorPlataforma.class);
		OutMonitorPlataforma out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {	
		
		//TODO: CREAR UN MAIN PARA SER LLAMADO DESDE UN CRON 
		
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InMonitorPlataforma entrada = new InMonitorPlataforma();
			entrada.p_IdEmpresa = 1L;
			entrada.p_NotificarAdministradoresTI = "S";
 			JsonParser parser 	= new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        MonitorPlataforma demo=new MonitorPlataforma();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutMonitorPlataforma logica(Connection con, InMonitorPlataforma entrada) throws Exception {
		OutMonitorPlataforma salida = new OutMonitorPlataforma();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		String fechaSistemaStr = Utils.getFechaSistema(con, "BSCL", 1L);
		String fechaHoraSistemaStr = Utils.getFechaHoraSistema(con, "BSCL", 1L);
		java.sql.Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(fechaHoraSistemaStr);
		SQL sql=new SQL();
		
		/***************************************
		 * PRUEBA CONECTIVIDAD MARKET DATA  RV
		 **************************************/
        {
			log.write("Inicia prueba Conectividad MARKET DATA RV (telnet puerto)");
			OutMonitorPlataforma.Detalle checkMDRV = new OutMonitorPlataforma.Detalle("MD_RV","Ping TCP port","PEN","");
			File propf=new File("fix.properties");
			if (!propf.exists()) {
				checkMDRV.codigoValidacion="PENDIENTE";
				checkMDRV.descripcionValidacion="Servicio no ha sido habilitado/contratado";
			}
			else {
		        try{
		        	Properties prop = new Properties();
		            InputStream input = new FileInputStream("fix.properties");
		            prop.load(input);
		            String ip=prop.getProperty("MD_RV_IP");
		            String port=prop.getProperty("MD_RV_PORT");
		            String tout=prop.getProperty("MD_RV_TOUT");
		            String echo=prop.getProperty("MD_RV_ECHO");
		            input.close();
		        	String rpta=pingTCP(ip, port, tout, echo);
		            if(rpta.startsWith("OK+:")){
		            	String part[]=rpta.split(":");
		            	checkMDRV.codigoValidacion="OK";
		            	checkMDRV.descripcionValidacion="Comunicación existosa en "+part[1]+" msegs";	            	
		            }else{
		            	checkMDRV.codigoValidacion="ERROR";
		            	checkMDRV.descripcionValidacion="Excepción:"+rpta;
		            }
		        }catch(Exception e){
		        	checkMDRV.codigoValidacion="ERROR";
		        	checkMDRV.descripcionValidacion="Excepción:"+e.toString();
		        }finally{
					salida.P_DETALLE.add(checkMDRV);
				}  
			}
        }
        
        
		/***************************************
		 * PRUEBA CONECTIVIDAD MARKET DATA  RF
		 **************************************/
        {
			log.write("Inicia prueba Conectividad MARKET DATA RF (telnet puerto)");
			OutMonitorPlataforma.Detalle checkMDRF = new OutMonitorPlataforma.Detalle("MD_RF","Ping TCP port","PEN","");
			File propf=new File("fix.properties");
			if (!propf.exists()) {
				checkMDRF.codigoValidacion="PENDIENTE";
				checkMDRF.descripcionValidacion="Servicio no ha sido habilitado/contratado";
			}
			else {			
		        try{
		        	Properties prop = new Properties();
		            InputStream input = new FileInputStream("fix.properties");
		            prop.load(input);
		            String ip=prop.getProperty("MD_RF_IP");
		            String port=prop.getProperty("MD_RF_PORT");
		            String tout=prop.getProperty("MD_RF_TOUT");
		            String echo=prop.getProperty("MD_RF_ECHO");
		            input.close();
		        	String rpta=pingTCP(ip, port, tout, echo);
		            if(rpta.startsWith("OK+:")){
		            	String part[]=rpta.split(":");
		            	checkMDRF.codigoValidacion="OK";
		            	checkMDRF.descripcionValidacion="Comunicación existosa en "+part[1]+" msegs";	            	
		            }else{
		            	checkMDRF.codigoValidacion="ERROR";
		            	checkMDRF.descripcionValidacion="Excepción:"+rpta;
		            }
		        }catch(Exception e){
		        	checkMDRF.codigoValidacion="ERROR";
		        	checkMDRF.descripcionValidacion="Excepción:"+e.toString();
		        }finally{
					salida.P_DETALLE.add(checkMDRF);
				}  
			}
        }
        
		/***********************************
		 * PRUEBA CONECTIVIDAD Valorización 
		 **********************************/
		log.write("Inicia prueba Valorización");
		OutMonitorPlataforma.Detalle checkVALO = new OutMonitorPlataforma.Detalle("Valorización","Valorizar instrumento","PEN","");
		try{
			ValorizarWs val = new ValorizarWs();
			ValorizarWs.InValorizarWs inVal = new ValorizarWs.InValorizarWs();
			ValoUtils.TareaValorizacion itemValo = new ValoUtils.TareaValorizacion();
			itemValo.IdTarea=1L;
			itemValo.GrupoValorizacion=1L;
			itemValo.FechaSolicitud=new java.sql.Timestamp(System.currentTimeMillis());
			itemValo.IdEstado="PEN";
			itemValo.RutinaValorizacion="BCS";
			itemValo.TipoVal=63L;
			itemValo.Nemo="PAGARE NR";
			itemValo.FechaVal=Fecha.toDate(fechaSistemaStr);
			itemValo.FechaVcto=Fecha.toDate(Fecha.toStringDate(Fecha.addHours(fechaHoraSistemaTs,24)));
			itemValo.TasaTir=new BigDecimal(1);
			itemValo.TasaInteres=new BigDecimal(1);
			itemValo.ValRescateP=new BigDecimal(10000);
			itemValo.TipoMoneda=1L;
			itemValo.CLiq="PH";
			itemValo.Gestion="S";
			inVal.instrumentos.add(itemValo);
			inVal.idGrupo = 1L;
			inVal.rutinaValorizador = "BCS";
			ValorizarWs.OutValorizarWs outVal = val.logica(con, inVal);
			if(outVal.erroresContador==0){
				if(!outVal.instrumentos.get(0).WsCallerCodSalida.equals("0")){
					checkVALO.codigoValidacion="ERROR";
					checkVALO.descripcionValidacion="Error al invocar Web Service. Detalle: "+outVal.instrumentos.get(0).WsCallerDescSalida;
				}else if(!outVal.instrumentos.get(0).Estado.equals("0")){
					log.write("Valorización correcta");//log.write("Error generado en la valorización(WS). Detalle: "+outVal.instrumentos.get(0).Glosa);
					checkVALO.codigoValidacion="OK";
					checkVALO.descripcionValidacion="Invocación a WS Valorización realizada con éxito";
				}else{
					checkVALO.codigoValidacion="OK";
					checkVALO.descripcionValidacion="Invocación a WS Valorización realizada con éxito";
				}
			}else{
				checkVALO.codigoValidacion="ERROR";
				checkVALO.descripcionValidacion="Error al ejecutar SP de valorización";
			}
		}catch(Exception e){
			checkVALO.codigoValidacion="ERROR";
			checkVALO.descripcionValidacion="Excepción al ejecutar SP de valorización. Detalle:"+e.toString();
		}finally{
			salida.P_DETALLE.add(checkVALO);
		}
		
		/***********************************
		 * PRUEBA CONECTIVIDAD DTE 
		 **********************************/
		log.write("Inicia prueba Conectividad DTE (hacer login)");
		OutMonitorPlataforma.Detalle checkDTE = new OutMonitorPlataforma.Detalle("DTE","Login","PEN","");
        try{
        	Properties prop = new Properties();
            InputStream input = new FileInputStream("dte.properties");
            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            input.close();
        	CallerWSDte callDte = new CallerWSDte(true);
            String tokenLoginDTE = callDte.loginDTE(Server, User, Pass);
            if(tokenLoginDTE==null||tokenLoginDTE.equals("")){
            	checkDTE.codigoValidacion="ERROR";
            	checkDTE.descripcionValidacion="Prueba DTE Incorrecta";
            }else{
            	checkDTE.codigoValidacion="OK";
            	checkDTE.descripcionValidacion="Prueba DTE Correcta";
            }
        }catch(Exception e){
        	checkDTE.codigoValidacion="ERROR";
        	checkDTE.descripcionValidacion="Excepción al probar Login en DTE. Detalle:"+e.toString();
        }finally{
			salida.P_DETALLE.add(checkDTE);
		}
       
        
        /***********************************
		 * COMPARACION DE FECHAS
		 * SISTEMA - APPSERVER
		 **********************************/
        log.write("Inicia prueba Comparación de fechas Sistema y AppServer");
		OutMonitorPlataforma.Detalle checkFECHAS = new OutMonitorPlataforma.Detalle("Comparación Fechas","Sistema:AppServer","PEN","");
		try{
			String fechaMaquina=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	        if(fechaMaquina.equals(fechaSistemaStr)){
	        	checkFECHAS.codigoValidacion="OK";
	        	checkFECHAS.descripcionValidacion="Fechas máquina y sistema iguales:"+fechaSistemaStr;
	        }else{
	        	checkFECHAS.codigoValidacion="ERROR";
	        	checkFECHAS.descripcionValidacion="Fechas máquina y sistema distintas: Fecha Sistema="+fechaSistemaStr+",Fecha AppServer="+fechaMaquina;
	        }
		}catch(Exception e){
			checkFECHAS.codigoValidacion="ERROR";
			checkFECHAS.descripcionValidacion="Excepción al probar Comparación de fechas. Detalle:"+e.toString();
		}finally{
			salida.P_DETALLE.add(checkFECHAS);
		}

        /***********************************
		 * REVISA ESTADO DEL SISTEMA
		 **********************************/
		log.write("Revisa estado apertura/cierre del módulo sistema");
		OutMonitorPlataforma.Detalle checkESTADOSISTEMA = new OutMonitorPlataforma.Detalle("Estado Módulo Sistema","Apertura:Cierre","PEN","");
		try{
			Calendar cal = Calendar.getInstance();
	        cal.setTimeInMillis(fechaHoraSistemaTs.getTime());
	        int horaAperturaEsperado=9;
	        int horaCierreEsperado=20;
	        int horasOffset=-3;
	        String estadoSistemaEsperado="A"; //A,C
	        if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY||cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
	        	estadoSistemaEsperado="C";
	        }else if(cal.get(Calendar.HOUR)-horasOffset>=horaCierreEsperado&&cal.get(Calendar.HOUR)-horasOffset<horaAperturaEsperado){
	        	estadoSistemaEsperado="C";
	        }
	        String SQL_ModuloEmpresa = "select \"IdCREModuloEmpresa\",\"IdEmpresa\",\"IdModulo\",\"NombreModuloEmpresa\",\"TipoApertura\",\"HoraApertura\",\"TipoCierre\",\"HoraCierre\",\"IdEstado\",\"FechaProceso\",\"TipoEstado\" "
	    			+ "from \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\" where \"IdModulo\" = 'SIS'";
	        HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
			List<ModuloEmpresa> resModuloEmpresa = sql.query(con, SQL_ModuloEmpresa,parmsVacio,ModuloEmpresa.class);
			if(resModuloEmpresa.size()>0){
				String estadoSistema = resModuloEmpresa.get(0).TipoEstado;
				if(estadoSistemaEsperado.equals(estadoSistema)){
					checkESTADOSISTEMA.codigoValidacion="OK";
					checkESTADOSISTEMA.descripcionValidacion="Prueba Estado módulo sistema correcta, Estado:"+estadoSistema+",Estado Esperado:"+estadoSistemaEsperado;
				}else{
					checkESTADOSISTEMA.codigoValidacion="ERROR";
					checkESTADOSISTEMA.descripcionValidacion="Prueba Estado módulo sistema incorrecta, Estado:"+estadoSistema+",Estado Esperado:"+estadoSistemaEsperado;
				}
			}else{
				checkESTADOSISTEMA.codigoValidacion="ERROR";
				checkESTADOSISTEMA.descripcionValidacion="Error al obtener estado del sistema";
			}
		}catch(Exception e){
			checkESTADOSISTEMA.codigoValidacion="ERROR";
			checkESTADOSISTEMA.descripcionValidacion="Excepción al probar Comparación de fechas. Detalle:"+e.toString();
		}finally{
			salida.P_DETALLE.add(checkESTADOSISTEMA);
		}
        	
        /***********************************
		 * PRUEBAS STATUS SERVIDOR HANA
		 **********************************/
		log.write("Revisar status en servidor HANA [PENDIENTE IMPLEMENTAR]");
		OutMonitorPlataforma.Detalle checkSTATUSSERVIDORHANA = new OutMonitorPlataforma.Detalle("Status Servidor Hana","Memoria","PEN","");
		try{
			checkSTATUSSERVIDORHANA.codigoValidacion="PENDIENTE";
			checkSTATUSSERVIDORHANA.descripcionValidacion="Pendiente implementar";
		}catch(Exception e){
			checkSTATUSSERVIDORHANA.codigoValidacion="ERROR";
			checkSTATUSSERVIDORHANA.descripcionValidacion="Excepción al revisar status en servidor HANA. Detalle:"+e.toString();
		}finally{
			salida.P_DETALLE.add(checkSTATUSSERVIDORHANA);
		}

		/***********************************
		 * PRUEBA STATUS APP SERVER
		 **********************************/
		//lista información de las unidades de disco de la máquina
		log.write("Revisar status en servidor HANA [PENDIENTE IMPLEMENTAR]");
		try{
			File[] roots = File.listRoots();
		    for(File root : roots) {
	    		String systemRoot=root.getAbsolutePath();
		    	Long totalSpace=root.getTotalSpace();
		    	Long freeSpace=root.getFreeSpace();
		    	Long usableSpace=root.getUsableSpace();
		    	Long totalSpaceMB=totalSpace/1048576;
		    	Long freeSpaceMB=freeSpace/1048576;
		    	Long usableSpaceMB=totalSpaceMB/1048576;
		    	Double porcentajeEspacioDisponible = (totalSpace==null||totalSpace==0)?0L:Math.floor(freeSpace.doubleValue()*100/totalSpace.doubleValue()*100)/100;
			    String infoDetalle="Espacio Total:"+totalSpaceMB+",Espacio Disponible:"+freeSpaceMB+"Porcentaje disponible:"+porcentajeEspacioDisponible+"%";
			    OutMonitorPlataforma.Detalle checkSTATUSAPPSERVER_DET = new OutMonitorPlataforma.Detalle("Status AppServer","Unidad "+systemRoot,"PEN","");
			    if(totalSpace>0&&porcentajeEspacioDisponible<10){
			    	checkSTATUSAPPSERVER_DET.codigoValidacion="ERROR";
			    	checkSTATUSAPPSERVER_DET.descripcionValidacion="Espacio Disponible inferior al 10%. [Detalle (MB)]"+infoDetalle;
			    }else{
			    	checkSTATUSAPPSERVER_DET.codigoValidacion="OK";
			    	checkSTATUSAPPSERVER_DET.descripcionValidacion="Espacio Disponible superior al 10%. [Detalle (MB)]"+infoDetalle;
			    }
			    if(totalSpace>0)salida.P_DETALLE.add(checkSTATUSAPPSERVER_DET);
		    }
		}catch(Exception e){
			salida.P_DETALLE.add(new OutMonitorPlataforma.Detalle("Status AppServer","","ERROR","Excepción al listar unidades. Detalle:"+e.toString()));
		}
		
	    /***********************************
		 * NOTIFICACION
		 **********************************/
		String hayErrores = "N";
		for(OutMonitorPlataforma.Detalle item:salida.P_DETALLE){
			if(item.codigoValidacion.equals("ERROR")){
				hayErrores="S";
				break;
			}
		}
	    if(entrada.p_NotificarAdministradoresTI.equals("F")
	    ||(entrada.p_NotificarAdministradoresTI.equalsIgnoreCase("S") && hayErrores.equals("S"))){
	    	NotificarAdministrador notiadmin = new NotificarAdministrador();
	 		NotificarAdministrador.NotificarAdministradorEntrada innotiadmin = new NotificarAdministrador.NotificarAdministradorEntrada();
	 		innotiadmin.NombreProceso="Monitor Plataforma";
	 		innotiadmin.IdEmpresa=entrada.p_IdEmpresa;
	 		innotiadmin.EsError=hayErrores;
	 		String styleTabla="<style>table {  font-family: arial, sans-serif;  border-collapse: collapse;  width: 100%;}td, th {  border: 1px solid #dddddd;  text-align: left;  padding: 8px;}</style>";
	 		String tmpHeaderTabla="<tr><th><strong>[[A]]</strong></th><th><strong>[[B]]</strong></th><th><strong>[[C]]</strong></th><th><strong>[[D]]</strong></th></tr>";
	 		String tmpItemTabla="<tr style=\"[[BC]]\"><td>[[A]]</td><td>[[B]]</td><td>[[C]]</td><td>[[D]]</td></tr>";
	 		String tablaDetalle=tmpHeaderTabla
	 				.replace("[[A]]","Clasificación")
	 				.replace("[[B]]","Sub Clasificación")
	 				.replace("[[C]]","Código Validación")
	 				.replace("[[D]]","Descripción Validación");
	 		int vm=0;
	 		for(OutMonitorPlataforma.Detalle item:salida.P_DETALLE){
	 			vm=vm+1;
	 			String tmpItemTablaFormateado=tmpItemTabla.replace("[[BC]]", "background-color: #CEE3F6");
	 			if((vm%2)==0)tmpItemTablaFormateado=tmpItemTabla.replace("[[BC]]", "");
	 			tablaDetalle=tablaDetalle+tmpItemTablaFormateado
	 				.replace("[[A]]",item.clasificacion)
 					.replace("[[B]]",item.subClasificacion)
 					.replace("[[C]]",item.codigoValidacion)
 					.replace("[[D]]",item.descripcionValidacion);
	 		}
	 		tablaDetalle="<div>"+styleTabla+"<table>"+tablaDetalle+"</table>"+"</div>";
	 		log.write(tablaDetalle);
	 		innotiadmin.Mensaje="Detalle de la revisión<br>"+tablaDetalle+"<br><br>Mensaje despachado automáticamente a usuarios administrativos TI.";
	 		log.writeAsJson("Notificación a administradores TI enviada",notiadmin.logica(con, innotiadmin));	
	    }else{
	    	log.write("Se omite notificar a administradores TI");	
	    }
	    log.writeAsJson("Detalle de la revisión",salida.P_DETALLE);
		return salida;
	}
	
    public static String pingTCP(String ip,String port,String tout,String echo) {       
        long timeOpen = System.currentTimeMillis();   
        String rpt="";
        Socket pingSocket = null;
        PrintWriter out = null;
        BufferedReader in=null;        
        try {           
            pingSocket =  new Socket();
            pingSocket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), Integer.parseInt(tout));           
            out = new PrintWriter(pingSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
            out.println(echo.replace("\\n","\n"));
            TimeLimiter timeLimiter = new SimpleTimeLimiter();
            String line=timeLimiter.callWithTimeout(in::readLine, Long.parseLong(tout), TimeUnit.MILLISECONDS,true);
            rpt = "REPLY:"+line;
        }catch (Exception er) {
            return "ERR:TCP ERROR:" + er.getMessage();
	    }finally {
	    	try {
	    		if (out!=null) out.close();
	    		if (in!=null) in.close();	            
	    		if (pingSocket!=null) pingSocket.close();
	    	}catch (Exception er) {}
	    }        
        long timeSend = System.currentTimeMillis();
        return "OK+:" + (timeSend-timeOpen) + ":"+rpt;               
       
    }		
	
    public static String pingHTTP(String url,String tout,String reply) {
        long timeOpen = System.currentTimeMillis();
        String rpt="";
        BufferedReader in=null;
        try {
            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.setConnectTimeout(Integer.parseInt(tout)); 
            connection.setReadTimeout(Integer.parseInt(tout)); 	 	             
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }           
            if (response != null && response.toString().contains(reply)) {
            	 rpt="REPLY OK";             
            } else if (response != null) {
            	return "ERR:HTTP REPLY:" + response.toString();                
            }
        } catch (Exception er) {
        	  return "ERR:HTTP ERROR:" + er.getMessage();
        }finally {
        	try {
        		if (in!=null) in.close();
        	}catch (Exception er) {}
        }
        long timeSend = System.currentTimeMillis();
        return "OK+:" + (timeSend-timeOpen) + ":"+rpt;               
    }         
    
}
