package cl.bcs.tesoreria.webservices.recaudacion;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.tesoreria.RecaudacionBCP;
import cl.bcs.tesoreria.ConsultaBCP.ConsultaBCP_Salida;
import cl.bcs.tesoreria.RecaudacionBCP.RecaudacionBCP_Entrada;
import cl.bcs.tesoreria.RecaudacionBCP.RecaudacionBCP_Salida;
import cl.bcs.tesoreria.RecaudacionBCP.ResultadoProceso_Salida;
import cl.bcs.tesoreria.webservices.recaudacion.SvcRecaudaInqV1_SoapHttpPortImpl.CodigoRespuestaBCP;
import cl.bcs.tesoreria.RecaudacionBCP.Operacion_Entrada;

import javax.naming.Context;
import javax.naming.InitialContext;


import cl.bcs.utils.Log;




@javax.jws.WebService(
                      serviceName = "SvcRecaudaTrnV1",
                      portName = "SvcRecaudaTrnV1_SoapHttpPort",
                      targetNamespace = "http://mfxV2_0",
                      endpointInterface = "cl.bcs.tesoreria.webservices.recaudacion.SvcRecaudaTrnV1PortType")


public class SvcRecaudaTrnV1_SoapHttpPortImpl implements SvcRecaudaTrnV1PortType {


	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	// Log
	private final Log log = new Log("TES.SvcRecaudaTrnV1_TransaccionBCP");
    
	
	
	// ------------------------------------------------------
    // CODIGOS DE ERROR PARA RESPUESTAS A CONSULTAS BCP

    public enum CodigoRespuestaBCP
    {
    	CP0000("CP0000", "INFORMACION", "PROCESO REALIZADO SATISFACTORIAMENTE"), // Conforme OK >> Id depositante Valido Sin restricción (permite todas las operaciones)
    	CP0139("CP0139", "ERROR", "ID DEPOSITANTE RESTRINGIDO PARA EXTORNO"), 	// Error Extorno >> Id depositante Restringido para extorno
    	CP0140("CP0140", "ERROR", "ID DEPOSITANTE RESTRINGIDO PARA PAGO"), 		// Error Pago >> Id depositante Restringido para pago    	
    	CP0138("CP0138", "ERROR", "ERROR AL PROCESAR TRANSACCION");		        // Error >> genérico al procesar transacción (Error Inesperado, no controlado)
    	
    	
    	private String Codigo;
    	private String Severidad;
    	private String Descripcion;
    	
    	public String getCodigo()
    	{
    		return Codigo;
    	}
    	
    	public String getSeveridad()
    	{
    		return Severidad;
    	}
    	
    	public String getDescripcion()
    	{
    		return Descripcion;
    	}
    	
    	private CodigoRespuestaBCP(String codigo, String severidad, String descripcion)
    	{
    		this.Codigo = codigo;
    		this.Severidad = severidad;
    		this.Descripcion = descripcion;
    	}		
    }
    
    
	// ------------------------------------------------------
    // SERVICIOS BCP CONTRATADOS (El servicio identifica la moneda)    
    public enum ServicioBCP_Moneda
    {
    	ServicioBCP_2000("PEN"),
    	ServicioBCP_2001("USD");
    	
    	private String Moneda;
    	
    	public String getMoneda()
    	{
    		return Moneda;
    	}
    	
    	private ServicioBCP_Moneda(String moneda)
    	{
    		this.Moneda = moneda;
    	}		
    }
    
    public static String convertXmlGregorianToString(XMLGregorianCalendar xc)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        GregorianCalendar gCalendar = xc.toGregorianCalendar();

        Date date = gCalendar.getTime();

        String dateString = df.format(date);
 
        return dateString;
    }    
    
	// ------------------------------------------------------
    // LOGICA
	// ------------------------------------------------------    
    /* 
     * @see cl.bcs.tesoreria.webservices.recaudacion.SvcRecaudaTrnV1PortType#svcRecaudaTrn(cl.bcs.tesoreria.webservices.recaudacion.MFXSvcRecaudaTrnType  mfxSvcRecaudaTrn )*
     */
    public void svcRecaudaTrn(javax.xml.ws.Holder<MFXSvcRecaudaTrnType> mfxSvcRecaudaTrn) {

    	log.write("== INICIA EJECUCION DE WS SvcRecaudaTrnV1 ==");

    	
    	RecaudacionBCP recaudacionBCP = new RecaudacionBCP();
    	RecaudacionBCP_Entrada recaudacionBCP_Entrada = new RecaudacionBCP_Entrada();
    	RecaudacionBCP_Salida  recaudacionBCP_Salida = new RecaudacionBCP_Salida();
	        
        //Recibe mensaje de entrada
    	MFXSvcRecaudaTrnType entrada = mfxSvcRecaudaTrn.value;
        //Message xml = PhaseInterceptorChain.getCurrentMessage(); // Falta formato XML para guardar en BD
               
		try
		{	
			//CONEXION PARA QA Y PRODUCCION
			/*
			Context initCtx = new InitialContext();
	        Context envCtx = (Context) initCtx.lookup("java:openejb/Resource");
	        DataSource dataSource = (DataSource) envCtx.lookup("jdbc/hanaDB");
			Connection con = dataSource.getConnection();
			*/
			
			
			//CONEXION PARA PRUEBAS LOCALES
			//Connection con = PlataformaUtils.getDESAPEConnection();  // DESA PE
			Connection con = PlataformaUtils.getQAPEConnection();      // QA PE
		
 			try 
 			{	
 				
 				// --------------------------------------------------------------------------------------------
 		        // PREPARA PARAMETROS DE ENTRADA A SERVICIO DE RECAUDACION 
 				
 				recaudacionBCP_Entrada.IdSolicitudBcp = entrada.svcRecaudaTrnRq.rqUID;  		// Identificador único universal, BCP
 				recaudacionBCP_Entrada.NumeroOperacionBcp = entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.opnNro; // "Número de operación de la transacción BCP (único por día) 				
 				
 				recaudacionBCP_Entrada.FechaOperacionBcp = convertXmlGregorianToString(entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.clientFec);  // Fecha y hora BCP del momento que envía la transacción
 				recaudacionBCP_Entrada.MensajeEntrada =  entrada.toString();
 				
 				 
 				//Datos para extorno 				
 				if (entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.trnExtorno != null)
 				{ 					
 	 				String extornoTp = entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.trnExtorno.extornoTp != null ? entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.trnExtorno.extornoTp.value() : "";
 	 				if (extornoTp.equalsIgnoreCase("P"))   //P: es Extorno
 	 				{
 	 					recaudacionBCP_Entrada.TipoOperacionBCP = "EXTORNO";
 	 					recaudacionBCP_Entrada.NumeroOperacionExtornoBcp = entrada.svcRecaudaTrnRq.msjRqHdr.ctxRqHdr.trnExtorno.opnExtornoNro; // Número de operación BCP que se indica para extornar
 	 				}
 	 				else
 	 				{
 	 					recaudacionBCP_Entrada.TipoOperacionBCP = "PAGO";
 	 					recaudacionBCP_Entrada.NumeroOperacionExtornoBcp = "0";
 	 				}
 	 			} 	 			
 	 			else
	 			{
	 				recaudacionBCP_Entrada.TipoOperacionBCP = "PAGO";
	 				recaudacionBCP_Entrada.NumeroOperacionExtornoBcp = "0";
	 			}
 				
 		        List<Operacion_Entrada> OperacionEntrada = new ArrayList<Operacion_Entrada>();
 		        Operacion_Entrada _operacionEntrada = new Operacion_Entrada(); 		        
 		        _operacionEntrada.IdClienteBcp = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.pagId; 	// Identificador del depositante BCP (DNI naturales /  RUC juridicos)
 		        
 		        try 		        
 		       	{
 		        	_operacionEntrada.IdMoneda = ServicioBCP_Moneda.valueOf("ServicioBCP_" + entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.svcId).getMoneda(); 	//La moneda está asociada sal servicio 		        	
 		       	}
 		        catch (Exception e)
 		        {
 		        	throw new Exception("Cuenta BCP " +  entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.svcId + " no configurada.");	
 		        }
 		       
 		     	_operacionEntrada.Monto = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.medioAbono.mtoMon.mto.abs();  //Importe total de la transacción BCP
 		    	OperacionEntrada.add(_operacionEntrada); 		        
 		        recaudacionBCP_Entrada.OperacionesBcp = OperacionEntrada;
 		        
 		        log.write("PREPARA PARAMETROS DE ENTRADA PARA SERVICIO recaudacionBCP > " + recaudacionBCP_Entrada.toString());
 		       
 		        
 		        
 		        // --------------------------------------------------------------------------------------------
 		        // EJECUTA SERVICIO DE RECAUDACION 		        
 		        recaudacionBCP_Salida = recaudacionBCP.logica(con, recaudacionBCP_Entrada);
 		        recaudacionBCP_Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(recaudacionBCP_Salida.P_OUT_MENSAJES);
 		        
 				log.write("SE EJECUTA SERVICIO DE RECAUDACION(recaudacionBCP)");
 				
 				
 				
 		        // --------------------------------------------------------------------------------------------
 		        // PREPARA SALIDA DEL WS (En formato MFX según WSDL)
 		        MFXSvcRecaudaTrnType salida = new MFXSvcRecaudaTrnType();
 		        salida = procesarSalida(entrada, salida, recaudacionBCP_Salida, recaudacionBCP_Entrada.TipoOperacionBCP);
 		        log.write("PREPARA LA SALIDA DEL WS > " + salida.toString());
  				
 		        
 			    // --------------------------------------------------------------------------------------------
  		        // RETORNA SALIDA
 				mfxSvcRecaudaTrn.value = salida;
 				log.write("SE RETORNA MENSAJE DE SALIDA DEL WS");
 		        
 				if (recaudacionBCP_Salida.P_OUT_ERRORESCONTADOR == 0)				
 					con.commit();
 				else
 					con.rollback(); //si hay errores, debe reversar lo realizado(Ej.: crea comprobnate y falló liquidación) 
				
 			    
 			    log.write("== FINALIZA EJECUCION DE WS SvcRecaudaTrnV1 ==");
 			}
 			catch(Exception er) 
 			{	
 				MFXSvcRecaudaTrnType salida = new MFXSvcRecaudaTrnType();
 				salida = procesarSalidaErrorGenerico(entrada, salida, er);
 				mfxSvcRecaudaTrn.value = salida;
				con.rollback();
			}
 			
 			con.close();
 			
	    }    	
		catch(Exception ex)
		{	
            ex.printStackTrace();
            throw new RuntimeException(ex);			
		}    	

    }
    
    
    
    
	// ------------------------------------------------------------------
    // PRERARA MENSAJE DE SALIDA DEL WS EN FORMATO MFX SEGUN WDSL

    public MFXSvcRecaudaTrnType procesarSalida(MFXSvcRecaudaTrnType entrada,MFXSvcRecaudaTrnType salida, RecaudacionBCP_Salida recaudacionBCP_Salida, String tipoOperacionBCP) throws DatatypeConfigurationException
    {	
    	try
    	{
    		//----------------------------------------------------------------
    		//Setea fecha de proceso de String a XMLGregorianCalendar
    		String fechaProcesoOpt = recaudacionBCP_Salida.P_OUT_FECHAPROCESO;    		
    		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    		Date date = format.parse(fechaProcesoOpt);
    		GregorianCalendar cal = new GregorianCalendar();
    		cal.setTime(date);

    		XMLGregorianCalendar _fechaProcesoOpt =  DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
    		
    		//-------------------------------------------------------------
    		//Obtiene Id de comprobante tesorería generado
    		Long idComprobanteTesoreria = 1L; //Si no se generó comprobante, se retorna 1 (indicado por BCP) 
    		String descripcionSalida = "1";
    		if (recaudacionBCP_Salida.P_OUT_ERRORESCONTADOR == 0)
    		{
				for (ResultadoProceso_Salida resultadoProcesoRec: recaudacionBCP_Salida.P_OUT_RESULTADOPROCESO)
				{
					//Obtiene Id de comprobantes generados
					if (resultadoProcesoRec.Campo.equalsIgnoreCase("IdComprobante") && resultadoProcesoRec.Resultado.equalsIgnoreCase("ING") && resultadoProcesoRec.Servicio.equalsIgnoreCase("BSCL.Tesoreria.BD.Servicios::CrearComprobante"))
					{
						idComprobanteTesoreria =  resultadoProcesoRec.Id;
						descripcionSalida = "Comprobante Corredora N° " + idComprobanteTesoreria.toString();
					}
				}
    		}
			
			
    		salida.svcRecaudaTrnRs = new  SvcRecaudaTrnRsType();
    		salida.svcRecaudaTrnRs.rqUID = entrada.svcRecaudaTrnRq.rqUID;	      // Se retorna tal cual se recibe
    		
    		
 	        //-----------------------------------------------------------------------
    		// STATUS DEL PROCESO (Errores / Ok) 
    		
    		salida.svcRecaudaTrnRs.status   =   new StatusType();
    		if (recaudacionBCP_Salida.P_OUT_ERRORESCONTADOR == 0)
	        {	//CONSULTA SIN ERRORES >> Retorna CP0000: Conforme OK
        		salida.svcRecaudaTrnRs.status.statusCod = CodigoRespuestaBCP.CP0000.getCodigo();
    	        salida.svcRecaudaTrnRs.status.severidad = salida.svcRecaudaTrnRs.status.severidad.INFORMACION;
    	        salida.svcRecaudaTrnRs.status.statusDes = CodigoRespuestaBCP.CP0000.getDescripcion();
    	        
    	        salida.svcRecaudaTrnRs.status.servStatus = new ServStatusType();
    	        salida.svcRecaudaTrnRs.status.servStatus.servStatusCod = idComprobanteTesoreria.toString();
    	        salida.svcRecaudaTrnRs.status.servStatus.severidad = salida.svcRecaudaTrnRs.status.servStatus.severidad.INFORMACION;
    	        salida.svcRecaudaTrnRs.status.servStatus.statusDes = descripcionSalida;
	        }
    		else
	        {		//CONSULTA CON ERRORES >> Retorna códido de error según tipo de error
		        
				for (MensajeSalida mensajeSalida : recaudacionBCP_Salida.P_OUT_MENSAJES)
				{	
					if (mensajeSalida.tipo.equalsIgnoreCase("ERROR"))
					{	
						if (tipoOperacionBCP.equalsIgnoreCase("PAGO"))						 
						{	
					        salida.svcRecaudaTrnRs.status.statusCod = CodigoRespuestaBCP.CP0140.getCodigo();   // Error CP0140 >> Id depositante restringido para realizar pago
			    	        salida.svcRecaudaTrnRs.status.severidad = salida.svcRecaudaTrnRs.status.severidad.ERROR;
			    	        salida.svcRecaudaTrnRs.status.statusDes = CodigoRespuestaBCP.CP0140.getDescripcion();
						}
						
						if (tipoOperacionBCP.equalsIgnoreCase("EXTORNO"))
						{	
					        salida.svcRecaudaTrnRs.status.statusCod = CodigoRespuestaBCP.CP0139.getCodigo(); // Error CP0139 >> Id depositante restringido para realizar extorno
			    	        salida.svcRecaudaTrnRs.status.severidad = salida.svcRecaudaTrnRs.status.severidad.ERROR;
			    	        salida.svcRecaudaTrnRs.status.statusDes = CodigoRespuestaBCP.CP0139.getDescripcion();
						}
						
						salida.svcRecaudaTrnRs.status.servStatus = new ServStatusType();
						salida.svcRecaudaTrnRs.status.servStatus.servStatusCod = mensajeSalida.id.toString(); 	
						salida.svcRecaudaTrnRs.status.servStatus.severidad = salida.svcRecaudaTrnRs.status.servStatus.severidad.ERROR;
		 		        salida.svcRecaudaTrnRs.status.servStatus.statusDes = mensajeSalida.descripcion;
		 		        
					}
				}
	        }
 

    		//FECHA Y NUMERO DE OPERACION DE PROCESO OPTIMUS
    		salida.svcRecaudaTrnRs.msjRsHdr =  new MsjRsHdrType();
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr = new CtxRsHdrType();
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr.servFec = _fechaProcesoOpt; // Fecha de proceso optimus en que se procesó la operacion
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr.opnNroAdic = idComprobanteTesoreria.toString(); // Número de operación optimus con el que se procesó la transaccion enviada
    		
    		//DATOS RETORNADOS TAL CUAL SE RECIBEN
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg =  new SvcRecaudaTrnRegType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst = new SvcEmpLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.ctd = 1;
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp =  new SvcEmpType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcId = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.svcId; // Se retorna tal cual se recibe    		                                                                         
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.pagId = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.pagId;  // Se retorna tal cual se recibe

    		    		
    		//REFERENCIA ADIONAL DEL PAGO (Se usará para la conciliacion, ya que este valor se retorna en archivo de conciliación)
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst = new SvcEmpDocAdicLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.ctd = 1;
    		
    		List<SvcEmpDocAdicType> svcEmpDocAdicType = new ArrayList<SvcEmpDocAdicType>();
    		SvcEmpDocAdicType _svcEmpDocAdicType = new SvcEmpDocAdicType();
    		_svcEmpDocAdicType.docRef = idComprobanteTesoreria.toString();
    		
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.svcEmpDocAdic  = svcEmpDocAdicType; 
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.svcEmpDocAdic.add(_svcEmpDocAdicType);
    		
    		//REFERENCIA ADICIONAL DE LA OPERACION    				
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst =  new OpnRefAdicLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst.ctd = 1;
    		
    		List<String> referenciaAdicional = new ArrayList<String>();
    		referenciaAdicional.add(descripcionSalida);
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst.opnRefAdic = referenciaAdicional;

    	}
    	catch(Exception e)  // Error CP0138: error genérico al procesar transacción (Error Inesperado, no controlado)
    	{
			salida = procesarSalidaErrorGenerico(entrada, salida, e);
    	}
    	
    	return salida;	
    	
    	
    	
    }

   

	// --------------------------------------------------------------------------
    // PRERARA MENSAJE DE RETORNO DEL WS PARA ERROR CP138: ERROR NO CONTROLADO 

    public MFXSvcRecaudaTrnType procesarSalidaErrorGenerico(MFXSvcRecaudaTrnType entrada,MFXSvcRecaudaTrnType salida, Exception er) throws DatatypeConfigurationException
    {
	       //Setea fecha actual de proceso de Timestamp a XMLGregorianCalendar
	       Calendar calendar = Calendar.getInstance();
	       Date now = calendar.getTime();
	       Timestamp currentTimestamp = new Timestamp(now.getTime());
	        
	       LocalDateTime ldt = currentTimestamp.toLocalDateTime();
	       XMLGregorianCalendar fechaProceso = DatatypeFactory.newInstance().newXMLGregorianCalendar();
	       
	       fechaProceso.setYear(ldt.getYear());
	       fechaProceso.setMonth(ldt.getMonthValue());
	       fechaProceso.setDay(ldt.getDayOfMonth());
	       fechaProceso.setHour(ldt.getHour());
	       fechaProceso.setMinute(ldt.getMinute());
	   	   fechaProceso.setSecond(ldt.getSecond());
	  	   fechaProceso.setFractionalSecond(new BigDecimal("0." + ldt.getNano()));
    	    
    		salida.svcRecaudaTrnRs = new  SvcRecaudaTrnRsType();
    		salida.svcRecaudaTrnRs.rqUID = entrada.svcRecaudaTrnRq.rqUID;	      // Se retorna tal cual se recibe    		  		
    		
    		salida.svcRecaudaTrnRs.status   =   new StatusType();							
			salida.svcRecaudaTrnRs.status.statusCod = CodigoRespuestaBCP.CP0138.getCodigo();
			salida.svcRecaudaTrnRs.status.severidad = salida.svcRecaudaTrnRs.status.severidad.ERROR;
			salida.svcRecaudaTrnRs.status.statusDes = CodigoRespuestaBCP.CP0138.getDescripcion();
			
			salida.svcRecaudaTrnRs.status.servStatus = new ServStatusType();
			salida.svcRecaudaTrnRs.status.servStatus.servStatusCod = "0001";
			salida.svcRecaudaTrnRs.status.servStatus.severidad = salida.svcRecaudaTrnRs.status.servStatus.severidad.ERROR;
		 	salida.svcRecaudaTrnRs.status.servStatus.statusDes = er.getMessage() != null ? ((er.getMessage().length() > 500) ? er.getMessage().substring(1, 500) : er.getMessage()) : "ERROR GENERICO AL PROCESAR LA TRANSACCION";
		 			

    		//FECHA Y NUMERO DE OPERACION DE PROCESO OPTIMUS
    		salida.svcRecaudaTrnRs.msjRsHdr =  new MsjRsHdrType();
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr = new CtxRsHdrType();
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr.servFec = fechaProceso; // Fecha de proceso optimus en que se procesó la operacion
    		salida.svcRecaudaTrnRs.msjRsHdr.ctxRsHdr.opnNroAdic = "0"; // Número de operación optimus con el que se procesó la transaccion enviada
    		
    		
    		//DATOS RETORNADOS TAL CUAL SE RECIBEN
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg =  new SvcRecaudaTrnRegType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst = new SvcEmpLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.ctd = 1;
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp =  new SvcEmpType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcId = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.svcId; // Se retorna tal cual se recibe    		                                                                         
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.pagId = entrada.svcRecaudaTrnRq.svcRecaudaTrnRef.svcEmpLst.svcEmp.pagId;  // Se retorna tal cual se recibe
    		                                                                 
    		    		
    		//REFERENCIA ADIONAL DEL PAGO (Se usará para la conciliacion, ya que este valor se retorna en archivo de conciliación)
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst = new SvcEmpDocAdicLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.ctd = 1;
    		
    		List<SvcEmpDocAdicType> svcEmpDocAdicType = new ArrayList<SvcEmpDocAdicType>();
    		SvcEmpDocAdicType _svcEmpDocAdicType = new SvcEmpDocAdicType();
    		_svcEmpDocAdicType.docRef = "1";
    		
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.svcEmpDocAdic  = svcEmpDocAdicType; 
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.svcEmpDocAdicLst.svcEmpDocAdic.add(_svcEmpDocAdicType);
    		
    		//REFERENCIA ADICIONAL DE LA OPERACION    				
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst =  new OpnRefAdicLstType();
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst.ctd = 1;
    		
    		List<String> referenciaAdicional = new ArrayList<String>();
    		referenciaAdicional.add("1");
    		salida.svcRecaudaTrnRs.svcRecaudaTrnReg.svcEmpLst.svcEmp.opnRefAdicLst.opnRefAdic = referenciaAdicional;

    	
    	return salida;	
    	
    }
    

    
}
