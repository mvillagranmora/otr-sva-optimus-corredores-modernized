

package cl.bcs.tesoreria.webservices.recaudacion;


import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.tesoreria.ConsultaBCP;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ConsultaBCP.ConsultaBCP_Entrada;
import cl.bcs.tesoreria.ConsultaBCP.ConsultaBCP_Salida;
import cl.bcs.tesoreria.ConsultaBCP.Consulta_Entrada;
import cl.bcs.tesoreria.ConsultaBCP.ResultadoConsulta_Salida;
import cl.bcs.utils.Log;
import java.util.Date;



@javax.jws.WebService(
                      serviceName = "SvcRecaudaInqV1",
                      portName = "SvcRecaudaInqV1_SoapHttpPort",
                      targetNamespace = "http://mfxV2_0",
                      endpointInterface = "cl.bcs.tesoreria.webservices.recaudacion.SvcRecaudaInqV1PortType")
                      




public class SvcRecaudaInqV1_SoapHttpPortImpl implements SvcRecaudaInqV1PortType {
    
	// Log
	private final Log log = new Log("TES.SvcRecaudaInqV1_ConsultaBCP");
	
        
	// ------------------------------------------------------
    // CODIGOS DE RESPUESTA A LA CONSULTA ENVIADA DESDE BCP
	// ------------------------------------------------------
    public enum CodigoRespuestaBCP
    {	
    	CP0000("CP0000", "INFORMACION", "ID DEPOSITANTE VALIDO Y SIN RESTRICCION"),	// Conforme OK >> Id depositante Valido Sin restricción (permite todas las operaciones)    	
    	CP0010("CP0010", "ERROR", "ID DEPOSITANTE NO EXISTE"),  					// Error >> Id depositante No existe    	
    	CP0141("CP0141", "ERROR", "ID DEPOSITANTE RESTRINGIDO PARA CONSULTA"), 		// Error >> Id depositante Restringido para consulta    	
    	CP0138("CP0138", "ERROR", "ERROR AL PROCESAR CONSULTA");		            // Error >> genérico al procesar transacción en Consulta (Error Inesperado, no controlado)
    	
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
    // LOGICA
	// ------------------------------------------------------
    
    /* 
     * @see cl.bcs.tesoreria.webservices.recaudacion.SvcRecaudaInqV1PortType#svcRecaudaInq(cl.bcs.tesoreria.webservices.recaudacion.MFXSvcRecaudaInqType  mfxSvcRecaudaInq )*
     */
    
    public static Map toMap(Context ctx) throws NamingException {
        String namespace = ctx instanceof InitialContext ? ctx.getNameInNamespace() : "";
        HashMap<String, Object> map = new HashMap<String, Object>();
        System.out.println("> Listing namespace: " + namespace);
        NamingEnumeration<NameClassPair> list = ctx.list(namespace); 
        while (list.hasMoreElements()) {
            NameClassPair next = list.next();
            String name = next.getName();
            String jndiPath = namespace + name;
            Object lookup;
            try {
            	System.out.println("> Looking up name: " + jndiPath);
                Object tmp = ctx.lookup(jndiPath);
                if (tmp instanceof Context) {
                    lookup = toMap((Context) tmp);
                } else {
                    lookup = tmp.toString();
                }
            } catch (Throwable t) {
                lookup = t.getMessage();
            }
            map.put(name, lookup);

        }
        return map;
    }
    
    
    public void svcRecaudaInq(javax.xml.ws.Holder<MFXSvcRecaudaInqType> mfxSvcRecaudaInq) {
    	log.write("== INICIA EJECUCION DE WS SvcRecaudaInqV1 ==");

        ConsultaBCP consultaBCP = new ConsultaBCP();
        ConsultaBCP_Entrada consultaBCP_Entrada = new ConsultaBCP_Entrada();
        ConsultaBCP_Salida  consultaBCP_Salida = new ConsultaBCP_Salida();
	        
	        
        //Recibe mensaje de entrada
        MFXSvcRecaudaInqType entrada = mfxSvcRecaudaInq.value;
        //Message xml = PhaseInterceptorChain.getCurrentMessage(); // Falta formato XML para guardar en BD

       
		try  
		{	
			/*
			// CONEXION PARA PRODUCCION 
			Context initCtx = new InitialContext();
	        Context envCtx = (Context) initCtx.lookup("java:openejb/Resource");
	        DataSource dataSource = (DataSource) envCtx.lookup("jdbc/hanaDB");
	        Connection con = dataSource.getConnection();
	        */			
	        //toMap(new InitialContext());
	       
			// CONEXION PARA PRUEBAS LOCALES
			//Connection con = PlataformaUtils.getDESAPEConnection();  // DESA PE
			Connection con = PlataformaUtils.getQAPEConnection();      // QA PE
			
 			try 
 			{	
 				
 		        // --------------------------------------------------------------------------------------------
 		        // PREPARA PARAMETROS PARA CONSULTA (Validacion parcial)
 		        consultaBCP_Entrada.IdSolicitudBcp = entrada.svcRecaudaInqRq.rqUID;
 		        consultaBCP_Entrada.MensajeEntrada = entrada.toString();
 		        
 		        List<Consulta_Entrada> Consulta_Entrada = new ArrayList<Consulta_Entrada>();
 		        Consulta_Entrada _consulta_Entrada = new Consulta_Entrada();
 		        _consulta_Entrada.IdServicioBcp = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.svcId; 	 // Se retorna tal cual se recibe
 		        _consulta_Entrada.IdClienteBcp = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.pagId;     // Identificador de cliente BCP // Se retorna tal cual se recibe
 		        
 		        /*** SOLO PARA PRUEBAS CERTIFICACION - BORRAR UNA VEZ ECHA LA CERTIFICACION DE BCP ***/
 		        /*** ERROR PROVOCADO PARA RESPUESTA CP0138 ***/
 		        Long lngIdClienteBcp = Long.parseLong(_consulta_Entrada.IdClienteBcp);
 		        if (Long.compare(lngIdClienteBcp, 13602934) == 0)
 		        {
 		        	throw new Exception("Se pudrió todo..!!!!");
 		        }
 		        /*** FIN PRUEBAS **********************************************************************/
 		        
 		        Consulta_Entrada.add(_consulta_Entrada);
 		        
 		        consultaBCP_Entrada.ConsultasBcp = Consulta_Entrada;
 		        log.write("PREPARA PARAMETROS DE ENTRADA PARA SERVICIO ConsultaBCP > " + consultaBCP_Entrada.toString());
 		        
 		        
 		        // --------------------------------------------------------------------------------------------
 		        // EJECUTA CONSULTA 		        
 				consultaBCP_Salida = consultaBCP.logica(con, consultaBCP_Entrada);
 				log.write("SE EJECUTA SERVICIO ConsultaBCP SIN ERRORES");
 				

 		        // --------------------------------------------------------------------------------------------
 		        // PREPARA SALIDA DEL WS (En formato MFX según WSDL) 				
 		        MFXSvcRecaudaInqType salida = new MFXSvcRecaudaInqType();
 		        salida = procesarSalida(entrada, salida, consultaBCP_Salida);
 		        log.write("PREPARA LA SALIDA DEL WS > " + salida.toString());
				
 		        
 			    // --------------------------------------------------------------------------------------------
  		        // RETORNA SALIDA 		        
  		        mfxSvcRecaudaInq.value = salida;
  		        log.write("SE RETORNA LA SALIDA DEL WS");
 			
 			   con.commit();
 			  log.write("== FINALIZA EJECUCION DE WS SvcRecaudaInqV1 ==");
 			}
 			catch(Exception er) 
 			{	
 		        MFXSvcRecaudaInqType salida = new MFXSvcRecaudaInqType();
 		        salida = procesarSalidaErrorGenerico(entrada, salida, consultaBCP_Salida);
 		        mfxSvcRecaudaInq.value = salida; 				
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
    // PRERARA MENSAJE DE RETORNO DEL WS EN FORMATO MFX SEGUN WDSL
    
    public MFXSvcRecaudaInqType procesarSalida(MFXSvcRecaudaInqType entrada, MFXSvcRecaudaInqType salida, ConsultaBCP_Salida consultaBCP_Salida) throws DatatypeConfigurationException
    {
        salida.svcRecaudaInqRs = new  SvcRecaudaInqRsType();
        salida.svcRecaudaInqRs.status = new StatusType();
        
    	try
    	{

    		if (consultaBCP_Salida.P_OUT_ERRORESCONTADOR == 0)
	        {	//CONSULTA SIN ERRORES    >> Retorna CP0000: Conforme OK >> Id depositante Valido Sin restricción
	        	salida.svcRecaudaInqRs.status.statusCod = CodigoRespuestaBCP.CP0000.getCodigo();
		        salida.svcRecaudaInqRs.status.severidad = salida.svcRecaudaInqRs.status.severidad.INFORMACION;
		        salida.svcRecaudaInqRs.status.statusDes = CodigoRespuestaBCP.CP0000.getDescripcion();
		        
		        salida.svcRecaudaInqRs.status.servStatus = new ServStatusType();
		        salida.svcRecaudaInqRs.status.servStatus.servStatusCod = consultaBCP_Salida.P_OUT_NUMEROOPERACIONOPTIMUS; 
		        salida.svcRecaudaInqRs.status.servStatus.severidad = salida.svcRecaudaInqRs.status.servStatus.severidad.INFORMACION;
		        salida.svcRecaudaInqRs.status.servStatus.statusDes = CodigoRespuestaBCP.CP0000.getDescripcion();
	        }
    		else
	        {	//CONSULTA CON ERRORES >> Retorna códido de error según tipo de error
	        	
				for (MensajeSalida mensajeSalida : consultaBCP_Salida.P_OUT_MENSAJES)
				{	
					if (mensajeSalida.tipo.equalsIgnoreCase("ERROR") && mensajeSalida.codigo.equalsIgnoreCase("CP0010")) // Error CP0010 >> Id depositante No existe
					{	
						salida.svcRecaudaInqRs.status.statusCod = CodigoRespuestaBCP.CP0010.getCodigo();
				        salida.svcRecaudaInqRs.status.severidad = salida.svcRecaudaInqRs.status.severidad.ERROR;
				        salida.svcRecaudaInqRs.status.statusDes = CodigoRespuestaBCP.CP0010.getDescripcion();
					}
					
					if (mensajeSalida.tipo.equalsIgnoreCase("ERROR") && mensajeSalida.codigo.equalsIgnoreCase("CP0141")) // Error CP0141 >> Id depositante Restringido para consulta
					{	
						salida.svcRecaudaInqRs.status.statusCod = CodigoRespuestaBCP.CP0141.getCodigo();
				        salida.svcRecaudaInqRs.status.severidad = salida.svcRecaudaInqRs.status.severidad.ERROR;
				        salida.svcRecaudaInqRs.status.statusDes = CodigoRespuestaBCP.CP0141.getDescripcion();
					}
					
					salida.svcRecaudaInqRs.status.servStatus = new ServStatusType();
					salida.svcRecaudaInqRs.status.servStatus.servStatusCod = mensajeSalida.id.toString(); 	
					salida.svcRecaudaInqRs.status.servStatus.severidad = salida.svcRecaudaInqRs.status.servStatus.severidad.ERROR;
	 		        salida.svcRecaudaInqRs.status.servStatus.statusDes = mensajeSalida.descripcion;
				}
	        }

	        salida.svcRecaudaInqRs.rqUID = entrada.svcRecaudaInqRq.rqUID;	      // Se retorna tal cual se recibe
	        
	        salida.svcRecaudaInqRs.msjRsHdr =  new MsjRsHdrType();
	        salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr = new  CtxRsHdrType();
	        
	       
	       //Setea fecha de proceso de Timestamp a XMLGregorianCalendar
	       Timestamp fechaProcesoTs = consultaBCP_Salida.P_OUT_FECHAPROCESO;
	       LocalDateTime ldt = fechaProcesoTs.toLocalDateTime();

	       XMLGregorianCalendar fechaProcesoGc = DatatypeFactory.newInstance().newXMLGregorianCalendar();
	        
	       fechaProcesoGc.setYear(ldt.getYear());
	       fechaProcesoGc.setMonth(ldt.getMonthValue());
	       fechaProcesoGc.setDay(ldt.getDayOfMonth());
	       fechaProcesoGc.setHour(ldt.getHour());
	       fechaProcesoGc.setMinute(ldt.getMinute());
	   	   fechaProcesoGc.setSecond(ldt.getSecond());
	  	   fechaProcesoGc.setFractionalSecond(new BigDecimal("0." + ldt.getNano()));
	         
	       salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr.servFec = fechaProcesoGc;
	        		       
			
	       salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr.opnNroAdic = consultaBCP_Salida.P_OUT_NUMEROOPERACIONOPTIMUS; //Se retorna el Id del LOG registrado (BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP)
	        
	        
	       salida.svcRecaudaInqRs.svcRecaudaInqReg = new SvcRecaudaInqRegType();
	       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst = new SvcEmpLstType();
	       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp = new SvcEmpType(); 
	        
	       int cantidadServiciosInformados = 0;  // Cantidad de servicios retornados por la empresa
		   for (ResultadoConsulta_Salida resultadoConsulta : consultaBCP_Salida.P_OUT_RESULTADOCONSULTA)
		   {        
	 		        salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.svcId = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.svcId;   // Se retorna tal cual se recibe
	 		        salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.pagId = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.pagId;   // Se retorna tal cual se recibe
	 		        salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.nomCompleto = resultadoConsulta.NombreCliente != null ? resultadoConsulta.NombreCliente : "";
		 		    salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.docTp = (resultadoConsulta.TipoDocumentoIdentidad != null && !resultadoConsulta.TipoDocumentoIdentidad.isEmpty()) ? resultadoConsulta.TipoDocumentoIdentidad : "1";  // sinó existe tipoDoccumento debe retornar 1
	 		    	salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.docNro = resultadoConsulta.DocumentoIdentidad != null ? resultadoConsulta.DocumentoIdentidad : "";
	 		        
	 		        cantidadServiciosInformados++;
			}
			salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.ctd = cantidadServiciosInformados;
			
    	}
    	catch(Exception e) // Error CP0138: error genérico al procesar transacción en Consulta (Error Inesperado, no controlado)
    	{	
			salida = procesarSalidaErrorGenerico(entrada, salida, consultaBCP_Salida);
    	}
    	
    	return salida;
    }

    

	// ------------------------------------------------------------------
    // PRERARA MENSAJE DE RETORNO DEL WS PARA ERROR CP138: ERROR NO CONTROLADO
    
    public MFXSvcRecaudaInqType procesarSalidaErrorGenerico(MFXSvcRecaudaInqType entrada,MFXSvcRecaudaInqType salida, ConsultaBCP_Salida consultaBCP_Salida) throws DatatypeConfigurationException
    {
        salida.svcRecaudaInqRs = new  SvcRecaudaInqRsType();
        salida.svcRecaudaInqRs.status = new StatusType();

        salida.svcRecaudaInqRs.status.statusCod = CodigoRespuestaBCP.CP0138.getCodigo();  
	    salida.svcRecaudaInqRs.status.severidad = salida.svcRecaudaInqRs.status.severidad.ERROR;
	    salida.svcRecaudaInqRs.status.statusDes = CodigoRespuestaBCP.CP0138.getDescripcion();
	        
	    salida.svcRecaudaInqRs.status.servStatus = new ServStatusType();
		salida.svcRecaudaInqRs.status.servStatus.servStatusCod = "0001"; 	
		salida.svcRecaudaInqRs.status.servStatus.severidad = salida.svcRecaudaInqRs.status.servStatus.severidad.ERROR;
 		salida.svcRecaudaInqRs.status.servStatus.statusDes = ("ERROR GENERICO AL PROCESAR CONSULTA");

 		
        salida.svcRecaudaInqRs.rqUID = entrada.svcRecaudaInqRq.rqUID;	      // Se retorna tal cual se recibe  
        salida.svcRecaudaInqRs.msjRsHdr =  new MsjRsHdrType();
        salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr = new  CtxRsHdrType();
        
       

       //Setea fecha actual de proceso de Timestamp a XMLGregorianCalendar
       Calendar calendar = Calendar.getInstance();
       Date now = calendar.getTime();
       Timestamp currentTimestamp = new Timestamp(now.getTime());
        
       LocalDateTime ldt = currentTimestamp.toLocalDateTime();
       XMLGregorianCalendar fechaProcesoGc = DatatypeFactory.newInstance().newXMLGregorianCalendar();
       
       fechaProcesoGc.setYear(ldt.getYear());
       fechaProcesoGc.setMonth(ldt.getMonthValue());
       fechaProcesoGc.setDay(ldt.getDayOfMonth());
       fechaProcesoGc.setHour(ldt.getHour());
       fechaProcesoGc.setMinute(ldt.getMinute());
   	   fechaProcesoGc.setSecond(ldt.getSecond());
  	   fechaProcesoGc.setFractionalSecond(new BigDecimal("0." + ldt.getNano()));
         
       salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr.servFec = fechaProcesoGc;
 
       salida.svcRecaudaInqRs.msjRsHdr.ctxRsHdr.opnNroAdic = consultaBCP_Salida.P_OUT_NUMEROOPERACIONOPTIMUS != null ? consultaBCP_Salida.P_OUT_NUMEROOPERACIONOPTIMUS : "1";   //Si se cae por error inesperado, puede que ho haya registrado el mensaje de entrada. Para este caso habría que revisar el LOG del servidor 
        
       salida.svcRecaudaInqRs.svcRecaudaInqReg = new SvcRecaudaInqRegType();
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst = new SvcEmpLstType();
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp = new SvcEmpType(); 
       
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.ctd = 1; // 1 servicio retornado 
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.svcId = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.svcId;  // Se retorna tal cual se recibe
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.pagId = entrada.svcRecaudaInqRq.svcRecaudaInqRef.svcEmpLst.svcEmp.pagId;  // Se retorna tal cual se recibe 		
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.nomCompleto = "";
	   salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.docTp = "1";  // sinó existe tipoDoccumento debe retornar 1
       salida.svcRecaudaInqRs.svcRecaudaInqReg.svcEmpLst.svcEmp.docNro = "";
    	
       return salida;
    }    

    
}



