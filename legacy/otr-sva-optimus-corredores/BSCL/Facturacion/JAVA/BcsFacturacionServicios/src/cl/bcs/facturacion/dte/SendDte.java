package cl.bcs.facturacion.dte;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cl.bcs.facturacion.dte.pojo.AjusteDocumento;
import cl.bcs.facturacion.dte.pojo.DetalleBoleta;
import cl.bcs.facturacion.dte.pojo.DetalleDocumento;
import cl.bcs.facturacion.dte.pojo.EnvioDTEComprobante;
import cl.bcs.facturacion.dte.pojo.EnvioDTELibro;
import cl.bcs.facturacion.dte.pojo.LibroCv;
import cl.bcs.facturacion.dte.pojo.LibroCvDetalle;
import cl.bcs.facturacion.dte.pojo.LibroCvTotal;
import cl.bcs.facturacion.dte.pojo.ParametroBoleta;
import cl.bcs.facturacion.dte.pojo.ParametroDocumento;
import cl.bcs.facturacion.dte.pojo.ReferenciaBoleta;
import cl.bcs.facturacion.dte.pojo.ReferenciaDocumento;
import cl.bcs.facturacion.dte.pojo.pe.ComprobanteDTEPE;
import cl.bcs.facturacion.dte.pojo.pe.DetalleDocumentoPE;
import cl.bcs.facturacion.dte.pojo.pe.DetallePolizaPE;
import cl.bcs.facturacion.dte.pojo.pe.ReferenciaDocumentoPE;
import cl.bcs.facturacion.dte.pojo.pe.ReferenciaPolizaPE;
import cl.bcs.plataforma.pla.ConvertirNumeroTexto;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class SendDte implements JavaCall{
	private CallerWSDte callDte = null;
	private static final Logger logger = LogManager.getLogger(SendDte.class);
	private List<String> arBoletas = Arrays.asList("BOE","BEE"); // Comprobantes Boletas
	private final String suscripcionEventoCapitalOSAS = "SUECOS";

	// ---------------------------------------------------------------
	// QUERYs
	// ---------------------------------------------------------------
	private final String SQL_SELECT_TAREA_DTE_CL 	= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_SelectTareaDTE_CL.sql");	
	private final String SQL_SELECT_TAREA_DTE_PE 	= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_SelectTareaDTE.sql");	
	private final String SQL_SELECT_DATOS_RESUMEN 	= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_SelectResumen.sql");


	private final String SQL_UPDATE_TAREA_DTE 		= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_UpdateTareaDTE.sql");
	private final String SQL_UPDATE_ERROR_TAREA_DTE	= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_UpdateErrorTareaDTE.sql");	
	private final String SQL_UPDATE_COMPROBANTE		= loadResourceToString("cl/bcs/facturacion/jobs/sql/Dte_UpdateComprobante.sql");
	
	
	// ---------------------------------------------------------------
	// CLASES
	// ---------------------------------------------------------------
	public static class ObjDte2_0 {		
		public Long IdEmpresa;
		//		TIPO VAL			NOMBRE					               in|out	- Definicion		
		//-------------------------------------------------------------------------------------------------------	
		public Long 				IdTareaDTE;				            // E|S	- Id Interno de Tarea DTE
		public Long					idtareadtegrupo;		            // E|S 	- Id agrupador de Tarea DTE, se inicializa en cada nuevo envio
		public String				TipoTareaDTE;	    	            // E|S 	- Indica el tipo de tarea que corresponde el registro
		public Long					IdRespuestaDTE;	    	            // E|S 	- Indica el id que respondio dte al recibir la factura
		public String				RespuestaDTEEnvioSII; 	            // E|S 	- Indica la recepcion del dte de la factura	despues de ser enviado al sii
		public String       		RespuestaLocationDTE;               // E|S 	- Indica el location de la recepcion del dte de la factura 
		public String       		RespuestaPdfDTE;                    // E|S 	- Indica la direccion donde se encuentra pdf
		public String       		RespuestaXmlDTE;                    // E|S 	- Indica la direccion donde se encuentra xml
		public Long					FolioComprobanteRespuestaDTE;       // E|S 	- Indica el folio comprobante que asigno sii
		
		public Long					IdComprobante;		                // E|S 	- 
		public String				IdEstadoTareaDTE;		            // E|S 	- Estado de tarea envio dte
		public String				IdEstadoDTE;	    	            // E|S 	- Estado de dte
		public String				IdEstadoSII;	    	            // E|S 	- Estado de sii	
		public String				IdTipoComprobante;		            // E|S 	- Tipo de comprobante que se esta enviando
		public Long      			TipoComprobanteDTE;                 // E|S  - Tipo de comprobante codificado por el SII
		public String       		periodocontable;                    // E|S  - Indica periodo contable a procesar
		public java.sql.Timestamp  	FechaComprobante;		            // E|S 	- Fecha de comprobante que se esta enviando
		public java.sql.Timestamp   FechaVencimiento;		            // E|S 	- Fecha de vencimiento
		public String     			codigovendedor;  		            // E|S 	-
		public String     			CodigoReceptor;  		            // E|S 	-
		
		public String       		GlosaAjusteComision;                // E|S 	-
		public BigDecimal   		TotalComisionCmbte;                 // E|S  - Total de comision por comprobante
		public String       		GlosaAjusteDerecho;                 // E|S 	-
		public BigDecimal       	TotalDerechoCmbte;                  // E|S  - Total de derecho por comprobante
		public String       		GlosaAjusteGasto;                   // E|S 	-
		public BigDecimal       	TotalGastoCmbte;                    // E|S  - Total de gasto por comprobante
		public String      			GlosaAjusteRetencion;               // E|S 	-
		public BigDecimal       	TotalRetencionCmbte;                // E|S  - Total de retencion por comprobante
		public BigDecimal      		idcomprobantemontonofacturable;     // E|S 	- Monto de comprobante sin cobros
		
		
		//public BigDecimal       	idcomprobantemontoneto;             // E|S 	- Monto de cobros de comprobante
		public BigDecimal           montoNeto;                          //
		public BigDecimal       	idcomprobantemontoexento;           // E|S 	- Monto de comprobante de facturas exentas
		public BigDecimal       	idcomprobantetasaiva;               // E|S 	- Tasa de cobro iva a aplicar
		public BigDecimal       	idcomprobantemontoiva;              // E|S 	- Monto de iva sobre monto cobros de comprobante
		public BigDecimal    		idcomprobantemontototal;            // E|S 	- Monto neto + monto iva 
		public BigDecimal       	idcomprobantevalorapagar;	        // E|S 	- Monto no facturable + monto total
		public BigDecimal       	totalidtipocomprobantemontoneto;    // E|S  - Total Monto de cobros de comprobante - Envio de Libros 
		public BigDecimal       	totalidtipocomprobantemontoexento;  // E|S  - Total Monto de comprobante de facturas exentas - Envio de Libros 
		public BigDecimal       	totalidtipocomprobantemontoiva;     // E|S  - Total Monto de iva sobre monto cobros de comprobante - Envio de Libros
		public BigDecimal       	totalidtipocomprobantemontototal;   // E|S  - Total Monto neto + monto iva - Envio de Libros 
		public Long      			totalidtipocomprobantecantidaddocumentos;  // E|S 	- Indica cantidad de documentos a enviar / para envio libros
		public Long      			totalidtipocomprobantecantidadanulados;    // E|S 	- Indica cantidad de documentos anulados a enviar / para envio libros	
		public String 	    		DocumentoIdentidadEmisor;	        // E|S 	- Documento identidad de emisor(Corredora)
		public String   			RazonSocialEmisor;		            // E|S 	- Razon social de emisor(Corredora)
		
		
		public String   			GiroEmisor;  			            // E|S 	- Giro de emisor(Corredora)
		public String	    		DireccionEmisor;		            // E|S 	- Direccion fisica de emisor(Corredora)
		public String	    		ComunaEmisor;	  	                // E|S 	- Comuna de direccion fisica de emisor(Corredora)
		public String	    		CiudadEmisor;			            // E|S 	- Ciudad de direccion fisica de emisor(Corredora)
		public String	    		EmailEmisor;			            // E|S 	- Direccion electronica de emisor(Corredora)
		public String	    		CondicionPago;			            // E|S 	- Codigo condicion de liquidación de comprobante
		public String   			TelefonoEmisor;			            // E|S 	- Telefono fijo de emisor(Corredora)	
		public String				EmailReceptor;			            // E|S 	- Direccion electronica de receptor(Cliente)
		public String				DocumentoIdentidadReceptor;         // E|S 	- Documento identidad de receptor(IUP)
		public String               DocumentoIdentidadAdicionalReceptor; // E|S - Documento identidad de receptor(RUT-Ruc Cavali)
		public String               DocumentoIdentidadAsociado;          // E|S - Documento identidad de receptor(PASS, DNI ó RUC)
		public String      			CodigoSucursal;                     // E|S 	- Portafolio
		public String				RazonSocialReceptor;                // E|S 	- Razon social de receptor(Cliente)
		public String				GiroReceptor;                       // E|S 	- Giro de receptor(Cliente)
		public String				DireccionReceptor;                  // E|S 	- Direccion fisica de receptor(Cliente)
		public String				ComunaReceptor;                     // E|S 	- Comuna de direccion fisica de receptor(Cliente)
		public String				CiudadReceptor;                     // E|S 	- Ciudad de direccion fisica de receptor(Cliente)
		public String				ContactoReceptor;                   // E|S 	- Contacto de direccion fisica de receptor(Cliente)
		public String				IndBolsa;                           // E|S 	- Bolsa relacionada al comprobante
		public String               IndMensaje_1;                       // E|S 	- Mensaje para facturas COMMEN
		
		//Referencia
		public String	    		TipoReferenciaIdComprobante;        // E|S 	- Referencia DTE de control de campo, en este caso es interno(SAP)
		public Long	    			FolioReferenciaIdComprobante;       // E|S 	- IdComprobante que es en este caso es interno (IdComprobante)
		public String 	    		FechaReferenciaIdComprobante;       // E|S 	- 
		public String	    		RazonReferenciaIdComprobante;       // E|S 	- Indica el uso de estos campos para DTE, en este caso es interno(Nro. Interno SAP)
		public String	    		TipoReferenciaFolioComprobante;     // E|S 	- Referencia DTE de control de campo, en este caso es el que se ve en facturacion(VIS)
		public Long	    			FolioReferenciaFolioComprobante;    // E|S 	- FolioComprobante que es en este caso es visual (FolioComprobante)
		public String 	    		FechaReferenciaFolioComprobante;    // E|S 	- 
		public String	   			RazonReferenciaFolioComprobante;    // E|S 	- Indica el uso de estos campos para DTE, en este caso es visual(Nro. Visible SAP)
		
		
		   //Nota de Debito - Credito
		public Integer      		NumeroReferenciaNota;               //
		public String	    		TipoReferenciaNota;                 // E|S 	- Referencia DTE de control de campo, referenciado con las notas
		public String	    		TipoReferenciaNotaPE;               // E|S 	- Referencia de control de campo, referenciado con las notas
		public String               CodigoTipoReferenciaNotaPE;         // E|S 	- Referencia de control de campo, referenciado con las notas
		public String	    		IdTipoReferenciaNota;               // E|S 	- Referencia de control de campo, referenciado con las notas
		public Long	    			FolioReferenciaNota;                // E|S 	- IdComprobante referenciado notas
		public java.sql.Timestamp	FechaReferenciaNota;                // E|S 	- 
		public String	    		RazonReferenciaNota;                // E|S 	- Indica glosa de notas
		public Long	    			CodigoReferenciaNota;               // E|S 	- Indica comprobante referencia de notas
		public String               TipoComprobanteNota;                 // E|S  - Tipo comprobante nc
		
		//Detalle
		public String	    		IdComprobanteDetalleGlosa;          // E|S 	- 
		public String	    		IdComprobanteDetalleDescripcion;    // E|S 	-
		public String	    		IdComprobanteDetalleNemotecnico;    // E|S 	-
		public String	    		IdComprobanteDetalleNemotecnicoSubyacente;    // E|S 	-
		public String	    		IdComprobanteDetalleBoletaEditable;   // E|S -
		public BigDecimal			IdComprobanteDetalleCantidad;       // E|S 	- Indica la cantidad de la operacion a nivel de linea (ComprobanteDetalle)
		public BigDecimal   		IdComprobanteDetallePrecio;         // E|S 	- Indica el precio de la operacion a nivel de linea (ComprobanteDetalle)
		public Integer      		IdComprobanteDetallePorcentaje;     // E|S 	- Indica el porcentaje de la operacion a nivel de linea (ComprobanteDetalle)
		public Integer      		IdComprobanteDetalleMontoDescuento; // E|S 	- Indica el monto descuento de la operacion a nivel de linea (ComprobanteDetalle)
		public BigDecimal   		IdComprobanteDetalleMonto;          // E|S 	- Indica el monto de la operacion a nivel de linea (ComprobanteDetalle)	
		
		public BigDecimal           IdComprobanteDetalleMontoNota;      // E|S 	- Indica el monto de la operacion Nota a nivel de linea (ComprobanteDetalle)
		public Integer      		IdComprobanteDetalleEsExento;       // E|S 	- Indica si es exento a nivel de linea (ComprobanteDetalle)
		
	
		//Parametro
		public String      			ParametroKeyProducto;               // E|S  - 
		
		//Peru
		public String       		TipoComprobanteDTEPE;
		public String       		AccionTipoOperacion;
		public String       		MonedaTransaccion; //moneda instrumento
		public String       		MonedaLiquidacion;
		public java.sql.Date    	FechaLiquidacion;
		public String       		ProductoOperacion;
		public String       		NacionalidadReceptor;
		public BigDecimal       	CantidadTotal;
		public BigDecimal       	ImporteTotal;
		public BigDecimal       	ImporteLiquidacion;
		
		public BigDecimal       	TotalLiquidacion;
		public String       		MonedaImporte;
		public String               NombreMonedaImporte;
		public BigDecimal       	ImporteAPagar;
		public BigDecimal			CorteMinimo;
		public Long					Tamano;
		public String				IdClasificacionTipoInstrumento;
		public String				IdConcepto;
		public String               IdConceptoNota;
		public String				NombreConcepto;
        public String               NombreConceptoNota;
		public String 				NombreTipoOperacion;
		public BigDecimal			ComisionBVLMonedaLiquidacion;
		public BigDecimal			ComisionSABMonedaLiquidacion;
		public BigDecimal			ComisionCavaliMonedaLiquidacion;
		public BigDecimal			FondoGarantiaMonedaLiquidacion;
		public BigDecimal			OtrasComisionesMonedaLiquidacion;
		public BigDecimal			ImpuestoIGVMonedaLiquidacion;
		public BigDecimal			FondoLiquidacionMonedaLiquidacion;
		public BigDecimal			ComisionSMVMonedaLiquidacion;
		public String 				TipoDocumentoIdentidadAsociado;
		public String				TipoDocumentoIdentidadEmisor;
		public String               Leyenda; 
		public String               LeyendaNota;
		public BigDecimal           ValorTipoCambio;
		public BigDecimal           Tasa;
		public BigDecimal           DiasPlazo;
		public String               NombreBovedaCuentaCustodioPortafolio;
		public String               NombreTipoCustodia;
		public String               NombreCompletoBancoCustodio;
		public String               NombreCompletoBancoLiquidador;
		public String               NombreFormaCustodia;
		public String               CodigoDeCuenta;
		public String               PolizaIngles;
		public BigDecimal           Intereses;
		public String				EsAPlazo;
		public BigDecimal 			ReajustesVAC;
		public String				GeneraOperacionPlazo;
		public String				Observacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	
	
	// ---------------------------------------------------------------
	// LOGICA
	// ---------------------------------------------------------------
    public boolean procesar(Connection con, Long idTareaDteGrupo) throws InterruptedException, Throwable{
    	logger.info(":SENDDTE:  ****** INICIO  *******");
    	
    	// Bandera utilizada para realizar debugs
    	// OJO: al liberar, esto siempre debe ir en false
    	Boolean isDebug = false;
    	if(isDebug)
			trust();

    	SQL sql = new SQL();  
        HashMap<String,Object> params = new HashMap<>();
        HashMap<String,Object> hashCuadroResumen = new HashMap<String,Object>();
        

        String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "PLATAFORMA",  "PLA.LocalizacionSistema");
        String usaCuadroResumen    = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "FACTURACION", "DTE_USACUADRORESUMEN");
        String usaEnvioMultiple    = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "FACTURACION", "DTE_ENVIOMULTIPLE");
        
        Long segundosPorComprobante    = ConfiguracionSistema.getParametroConfiguracionLong(con, 1L, "FACTURACION", "DTE_SEGUNDOS_POR_COMPROBANTE");
        
        
        inicializaCallerWSDte();
        
        //---------------------------------------------------------------------------
        // CHILE
        //---------------------------------------------------------------------------
        if (localizacionSistema.equalsIgnoreCase("CL")) {
        	
                // Obtiene las tareas DTE a enviar
                params.clear();
                params.put("IdTareaDTEGrupo", idTareaDteGrupo);              
                List<ObjDte> lstTareaDTE = sql.query(con, SQL_SELECT_TAREA_DTE_CL, params, ObjDte.class);
               
                
                // Para visualizar cuadro resumen (opcional, depende de parametro)
                if (usaCuadroResumen.equalsIgnoreCase("S"))
    			{  
        	        params.clear();
                    params.put("IdTareaDTEGrupo", idTareaDteGrupo);                    
    	        	List<CuadroResumen> lstCuadroResumen = sql.query(con, SQL_SELECT_DATOS_RESUMEN, params, CuadroResumen.class);    	        	
    	        	hashCuadroResumen = Utils.List2Hash(lstCuadroResumen, "IdTareaDTE");
    			}  
                    
                for(ObjDte tarea : lstTareaDTE)
                {
                	// Para cuadro resumen
                    if (usaCuadroResumen.equalsIgnoreCase("S"))
        			{
	                	CuadroResumen infoCuadroResumen = (CuadroResumen) hashCuadroResumen.get(tarea.idtareadte);	
	                	if (infoCuadroResumen != null)
	                	{
		                    tarea.nombreInstrumento 	= infoCuadroResumen.NombreInstrumento;
		                    tarea.cantidadTotal			= infoCuadroResumen.CantidadTotal.stripTrailingZeros().toPlainString();
		                    tarea.valorPrecioPromedio 	= infoCuadroResumen.ValorPrecioPromedio.stripTrailingZeros().toPlainString();
	                	}
        			}
                    
                    tarea.idestadotareadte = "ERR";                    
                    tarea.toDisplayableString();
                }                
                
            	if(lstTareaDTE.size() > 0)
            	{
            		logger.debug("Funcion " + lstTareaDTE.get(0).tipotareadte);
            		if(lstTareaDTE.get(0).tipotareadte.equalsIgnoreCase("DTEENV"))  // Enviar DTE
            		{
            			if (usaEnvioMultiple.equalsIgnoreCase("S")) {
            				//MRIQUELME 28-03-2022
            				enviarMultipleDteCmbte(lstTareaDTE, con, localizacionSistema, sql, segundosPorComprobante);
            			}
            			else {
            				enviarDteCmbte(lstTareaDTE, con, localizacionSistema, sql, segundosPorComprobante);
            			}
            			  
            		}
            		else if (lstTareaDTE.get(0).tipotareadte.equalsIgnoreCase("DTEACT")) { // Consultar DTE
            			consultarDte(lstTareaDTE, con, new ArrayList<ObjDte>());
            		}
            		else if (lstTareaDTE.get(0).tipotareadte.equalsIgnoreCase("DTELIB")) {
            			enviarDteLibro(lstTareaDTE, con, sql);
            		}
            	}                	
            	else {
            		logger.error("Error al obtener tareas DTE a enviar");
            	}

    
        }
        
        //---------------------------------------------------------------------------
        // PERU
        //--------------------------------------------------------------------------
        else if (localizacionSistema.equalsIgnoreCase("PE")) {

            // Obtiene las tareas DTE a enviar
            params.clear();
            params.put("IdTareaDTEGrupo", idTareaDteGrupo);            
            List<ObjDte2_0> arrTval = sql.query(con, SQL_SELECT_TAREA_DTE_PE, params, ObjDte2_0.class);

            ArrayList<ObjDte2_0> array = new ArrayList<>(arrTval);
            arrTval.toString();
        	
            if(arrTval.size()>0){
            	logger.debug("Funcion :" + arrTval.get(0).TipoTareaDTE);
        		if(arrTval.get(0).TipoTareaDTE.equalsIgnoreCase("DTEENV")) {
        			enviarDteCmbte2_0(array, con, segundosPorComprobante);
        		}
        		else if (arrTval.get(0).TipoTareaDTE.equalsIgnoreCase("DTEACT")) {
        			consultarDte2_0(arrTval, con);
        		}
        		else if (arrTval.get(0).TipoTareaDTE.equalsIgnoreCase("DTELIB")) {
        			enviarDteLibro2_0(arrTval, con);
        		}
            }                	
        	else {
        		logger.error("Error obtener resultset");
        	}

        }
        
        logger.info(":SENDDTE:  ****** FIN  *******");
        return true;
    }
	

    
	// Inicializa CallerWSDTE
	private void inicializaCallerWSDte() throws Exception{
    	if(callDte == null){
    		Properties prop = new Properties();
    		InputStream input = new FileInputStream("dte.properties");
    		//InputStream input = new FileInputStream("C:/GIT/OPTIMUS/optimus-config-files/dte.properties");

            prop.load(input);
            String Server=prop.getProperty("SERVER");
            String User=prop.getProperty("USER");
            String Pass=prop.getProperty("PASS");
            Boolean Depuracion=true;
            input.close();
            logger.debug("Conf para dte, server: "+Server+",user: "+User+",pass: "+Pass);
            callDte = new CallerWSDte(Server,User,Pass,Depuracion);
    	}
    }
	

	
	// ------------------------------------------------------------------------------------------------------
	// CONFIGURACION CHILE
	// ------------------------------------------------------------------------------------------------------	
	
	// Prepara json y Envia DTE del Comprobante	
	public void enviarDteCmbte(List<ObjDte> arrTval, Connection con, String localizacionSistema, SQL sql, Long segundosPorComprobante) throws Exception {
		
		logger.info("****** ENVIAR COMPROBANTES *******");
		
		Map<String,String> respuesta=null;		
		Map<String,String> datosEnviarDTE = prepararJsonAEnviarCmbte(arrTval); // armo json
		List<Long> cmbteEnv = new ArrayList<>();
		List<ObjDte> listActualizarTareaDTE = new ArrayList<>();
		List<ObjDte> listActualizarComprobante = new ArrayList<>();
		List<ObjDte> listArrayPosibleErrores = new ArrayList<>();
		
		for (ObjDte tval : arrTval) {
			if(cmbteEnv.contains(tval.idcomprobante)) continue;
			try {
				String jsonDatosEnviarDTE = datosEnviarDTE.get(tval.idcomprobante.toString());
				respuesta = callDte.enviarDocumento(tval.idcomprobante.toString(), jsonDatosEnviarDTE, localizacionSistema, tval.idtipocomprobante, segundosPorComprobante);
				
				cmbteEnv.add(tval.idcomprobante);
				
				//tratar la respuesta  
	  		    String[] idresp = respuesta.get(tval.idcomprobante.toString()).toString().split("/");
	 	   	    String[] dirresp = respuesta.get(tval.idcomprobante.toString()).toString().split("=");
	 		        
	 		    //Urls informes obtenidos desde DTE
	 	   	    String urlPDF = callDte.getServer()+"/files/pdf/"+idresp[3];
	 	   	    String urlXML = callDte.getServer()+"/files/xml/"+idresp[3];
 		    
	 	   	    // Asignamos estado tarea pendiente para que en segunda iteracion de Daemon actualice a su estado correspondiente
	 	   	    tval.idestadotareadte = "PEN"; 
	 	   	    tval.idestadosii = "PEN";
	 	   	    tval.idestadodte = "REC";
	 	   	    tval.idrespuestadte = respuesta == null ? 0L : Long.parseLong(idresp[3]);
	 	   	    tval.respuestalocationdte = dirresp[0];
	 	   	    tval.respuestapdfdte = urlPDF;
	 	   	    tval.respuestaxmldte = urlXML;
	 	   	    tval.actualizarPorCampo = "IdTareaDTEGrupo";	
	 	   	    tval.actualizarPorCampo2     = "IdComprobante";
	 	   	    
	 	   	    listActualizarTareaDTE.add(tval);
	 	   	    if(idresp[3]!=null) {
	 	   	    	listActualizarComprobante.add(tval);
	 	   	    }
			}catch(Exception e) {
				logger.error(e);
				logger.error(respuesta.get(tval.idcomprobante.toString()));
				if (!respuesta.get(tval.idcomprobante.toString()).contains("Documento ya fue procesado")) {
					
					String[] idresp = respuesta.get(tval.idcomprobante.toString()).toString().split("/");
		 	   	    String[] dirresp = respuesta.get(tval.idcomprobante.toString()).toString().split("=");
		 	   	    
			 	   	String urlPDF = callDte.getServer()+"/files/pdf/"+idresp[3];
		 	   	    String urlXML = callDte.getServer()+"/files/xml/"+idresp[3];
					
					List<ObjDte> listActualizarErrTareaDTE = new ArrayList<>();
					List<ObjDte> listActualizarErrComprobante = new ArrayList<>();
			
					tval.idestadotareadte 		= "ERR";
					tval.idestadodte 			= "ERR";
					tval.idestadosii			= "PEN";
					try {
						tval.respuestadteenviosii   = e.getMessage().toString().substring(e.getMessage().indexOf("{"), e.getMessage().indexOf("}"));
					} catch (Exception e1) {
						tval.respuestadteenviosii = e.toString();
					}

					tval.idrespuestadte 		= respuesta == null ? 0L : Long.parseLong(idresp[3]);
					tval.respuestalocationdte 	= dirresp[0];
					tval.respuestapdfdte 		= urlPDF;
					tval.respuestaxmldte 		= urlXML;		
		 	   	    tval.actualizarPorCampo 	= "IdTareaDTE";	
		 	   	    tval.actualizarPorCampo2    = "IdComprobante";
		 	   	    
		 	   	    listActualizarErrTareaDTE.add(tval);
		 	   	    listActualizarErrComprobante.add(tval);

					updateErrTareaDteCmbte(con, sql, listActualizarErrTareaDTE, listActualizarErrComprobante);
					
				} else {
					tval.idestadotareadte 		= "ERR";
					tval.idestadodte 			= "ERR";
					tval.idestadosii			= "PEN";
					try {
						tval.respuestadteenviosii   = e.getMessage().toString().substring(e.getMessage().indexOf("{"), e.getMessage().indexOf("}"));
					} catch (Exception e1) {
						tval.respuestadteenviosii = e.toString();
					}

					tval.idrespuestadte 		= 0L;
					tval.respuestalocationdte 	= "";
					tval.respuestapdfdte 		= "";
					tval.respuestaxmldte 		= "";		
		 	   	    tval.actualizarPorCampo 	= "IdTareaDTE";	
		 	   	    tval.actualizarPorCampo2    = "IdComprobante";
		 	   	    
					listArrayPosibleErrores.add(tval);
				}
				continue;
			}		 
 	   	}	
		
		try {
  			updateEnvTareaDteCmbte(con, sql, listActualizarTareaDTE, listActualizarComprobante);
		}catch(Exception e ) {
			logger.error(e);
		}
		
		consultarDte(arrTval, con, listArrayPosibleErrores);
 	   
 	   logger.info("****** FIN ENVIAR COMPROBANTES *******");
    }	
	
	// Prepara json y Envia Multiple DTE del Comprobante	
	//MRIQUELME 28-03-2022
	public void enviarMultipleDteCmbte(List<ObjDte> arrTval, Connection con, String localizacionSistema, SQL sql, Long segundosPorComprobante) throws Exception {
		
		logger.info("****** ENVIAR MULTIPLE COMPROBANTES *******");
		Map<String,String> respuesta = new HashMap<>();		
		Map<String,String> respuestaBoletas = new HashMap<>();		
		Map<String,String> datosEnviarDTE = prepararJsonAEnviarCmbte(arrTval); // armo json
		List<Long> cmbteEnv = new ArrayList<>(); //idcomprobante
		List<String> jsonDatosEnviarDTE= new ArrayList<>();
        List<Long> cmbteEnvBoletas = new ArrayList<>();
        List<String> jsonDatosBoletasEnviarDTE= new ArrayList<>();
		List<ObjDte> cmbtEnvConPosibleError = new ArrayList<>();
		List<ObjDte> listActualizarTareaDTE = new ArrayList<>();
		List<ObjDte> listActualizarComprobante = new ArrayList<>();
			String dirresp = "";
			String urlPDF = "";
			String urlXML = "";
			
			for (ObjDte tval : arrTval) 
			{
				if(cmbteEnv.contains(tval.idcomprobante) || cmbteEnvBoletas.contains(tval.idcomprobante)) continue;
				if(arBoletas.contains(tval.idtipocomprobante)) {
					logger.info("Hay boleta");
					jsonDatosBoletasEnviarDTE.add(datosEnviarDTE.get(tval.idcomprobante.toString()));
					cmbteEnvBoletas.add(tval.idcomprobante);
				} else {
					logger.info("Hay otro documento");
					jsonDatosEnviarDTE.add(datosEnviarDTE.get(tval.idcomprobante.toString()));
					cmbteEnv.add(tval.idcomprobante);
				}			
			}
			
		
		if(!cmbteEnv.isEmpty()) {
			respuesta = callDte.enviarMultiplesDocumentos(cmbteEnv,jsonDatosEnviarDTE, localizacionSistema, segundosPorComprobante);
			logger.info("Se obtiene respuesta");
		}
		
		if (!cmbteEnvBoletas.isEmpty()) {
			respuestaBoletas = callDte.enviarMultiplesDocumentos(cmbteEnvBoletas,jsonDatosBoletasEnviarDTE, localizacionSistema, segundosPorComprobante);
			logger.info("Se obtiene respuestaBol");
			respuesta.putAll(respuestaBoletas);
		}

		for (ObjDte tval : arrTval) {
			try {
				//tratar la respuesta (metodo que actualiza tabla) 	 	
				String resp = respuesta.get(tval.idcomprobante.toString()).toString();  
				JSONObject idresp = new JSONObject(resp);
				if(!idresp.get("estado").equals("ERROR")) {
                    dirresp = arBoletas.contains(tval.idtipocomprobante) ? "/api/boletaEditables/" + idresp.get("idDTE") :"/api/documentoEditables/" + idresp.get("idDTE");
					//Urls informes obtenidos desde DTE
					urlPDF = callDte.getServer()+"/files/pdf/"+idresp.get("idDTE");
					urlXML = callDte.getServer()+"/files/xml/"+idresp.get("idDTE");
					
		 	   	    // Asignamos estado tarea pendiente para que en segunda iteracion de Daemon actualice a su estado correspondiente
		 	   	    tval.idestadotareadte = "PEN"; 
					tval.idestadosii = "PEN";
			 	   	tval.idestadodte = "REC";
		 	   	    tval.idrespuestadte = respuesta == null ? 0L : Long.parseLong(idresp.get("idDTE").toString());
		 	   	    tval.respuestalocationdte = dirresp;
		 	   	    tval.respuestapdfdte = urlPDF;
		 	   	    tval.respuestaxmldte = urlXML;
		 	   	    tval.actualizarPorCampo = "IdTareaDTEGrupo";	
		 	   	    tval.actualizarPorCampo2     = "IdComprobante";
		 	   	    listActualizarTareaDTE.add(tval);
		 	   	    if(idresp.get("idDTE")!=null) {
		 	   	    	listActualizarComprobante.add(tval);
		 	   	    }
				}else if (!idresp.get("mensaje").toString().contains("Documento ya fue procesado")) {
					// Si el error es por superar el detalle, ninguno se va a poder procesar, por lo cual, debemos salirnos
					List<ObjDte> listActualizarErrTareaDTE = new ArrayList<>();
					List<ObjDte> listActualizarErrComprobante = new ArrayList<>();
					
					tval.idestadotareadte 		= "ERR";
					tval.idestadodte 			= "ERR";
					tval.idestadosii			= "PEN";
					tval.respuestadteenviosii   = resp.toString();
					tval.idrespuestadte 		= 0L;
					tval.respuestalocationdte 	= "";
					tval.respuestapdfdte 		= "";
					tval.respuestaxmldte 		= "";		
		 	   	    tval.actualizarPorCampo = "IdTareaDTE";	
		 	   	    tval.actualizarPorCampo2     = "IdComprobante";
		 	   	    
		 	   	    listActualizarErrTareaDTE.add(tval);
		 	   	    listActualizarErrComprobante.add(tval);
		 	   	    
					updateErrTareaDteCmbte(con, sql, listActualizarErrTareaDTE, listActualizarErrComprobante);
					
				} else {
					cmbtEnvConPosibleError.add(tval);
				}
			}catch(Exception e ) {
				cmbtEnvConPosibleError.add(tval);
				logger.error(e);
				continue;
			}	 	
 	    }
		
		try {
 			updateEnvTareaDteCmbte(con, sql, listActualizarTareaDTE, listActualizarComprobante);
		}catch(Exception e ) {
			logger.error(e);
		}
		
		consultarDte(arrTval, con, cmbtEnvConPosibleError);
 	    logger.info("****** FINAL ENVIO MULTIPLE COMPROBANTES  *******");
    }	
	
	//Prepara Json para enviar cmbte CHILE
	private Map<String,String> prepararJsonAEnviarCmbte(List<ObjDte> arrTval) {  	
		logger.info("****** preparando json *****");
		ArrayList<Long> documentos = new ArrayList<Long>();
  	    Map<String,String> salida = new HashMap<String, String>();
    	List<String> listTipoComprobanteNotasCredito = Arrays.asList(new String[]{"NCE", "NCR", "NEO", "NEE"});
  	
  	    for(int j=0; j<arrTval.size();j++) {
  		    ObjDte docto = (ObjDte) arrTval.get(j);    		
  		    if (!documentos.contains(docto.idcomprobante)) {
  		    	EnvioDTEComprobante doc = new EnvioDTEComprobante();
 		    	
 		    	
  		    	ArrayList<DetalleDocumento> listaDetalle= new ArrayList<DetalleDocumento>();
  		    	ArrayList<DetalleBoleta> listaDetalleB= new ArrayList<DetalleBoleta>();
			
				logger.info("docto.idtipocomprobante: " + docto.idtipocomprobante);
				logger.info("docto.idTipoInstrumento: " + docto.idTipoInstrumento);
	    		//** Comprobante **//
	        	doc.setId(null);
	        	doc.setFecha(docto.fechacomprobante==null ? null : docto.fechacomprobante.toString());
	        	doc.setTipo(docto.tipocomprobantedte);
	     
	        	if(!arBoletas.contains(docto.idtipocomprobante)) {
	        		doc.setFolio(-1);	
	        	}
	        	else {
	        		doc.setFolio(1);
	        	}
	        	 	         	
	        	
	        	doc.setRutEmisor(docto.documentoidentidademisor);
	        	doc.setRazonSocialEmisor(docto.razonsocialemisor);
	        	doc.setGiroEmisor(docto.giroemisor);
	        	doc.setDireccionEmisor(docto.direccionemisor);
	        	doc.setComunaEmisor(docto.comunaemisor);
	        	doc.setCiudadEmisor(docto.ciudademisor);
	        	
	        	if(!arBoletas.contains(docto.idtipocomprobante)) {
	        		doc.setEmailEmisor(docto.emailemisor);
	        		doc.setCodSIICondicionPago(docto.condicionpago);
	        		doc.setCondicionPago(docto.condicionpago);
	        		doc.setCodigoSucursal(docto.codigoSucursal);
	        		doc.setGiroReceptor(docto.giroreceptor);
	        		doc.setMedioPago("CH");
	        		
	        		if(docto.idtipocomprobante.equalsIgnoreCase("FOE")){
	        			if(docto.condicionpago.equalsIgnoreCase("PH")) {
	        				doc.setFormaPago("1");
	        			}else {
	        				doc.setFormaPago("2");
	        			}	        			
	        		}else { 
	        		  doc.setFormaPago("1");
	        		}
	        		
	        		doc.setCodigoVendedor(docto.codigovendedor);
	        		//CELBUHOPRO
	        		if((docto.idtipocomprobante.equalsIgnoreCase("FEE")) && (docto.idTipoInstrumento.equalsIgnoreCase("SPT") || suscripcionEventoCapitalOSAS.equalsIgnoreCase(docto.idConcepto))) {
	        			doc.setCodigoReceptor(docto.documentoidentidadreceptor);
	        			if("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && docto.idcomprobantemontototal.compareTo(new BigDecimal(0)) == 1){	        				
	        				doc.setMontoNoFacturable(docto.idcomprobantemontototal.multiply(new BigDecimal(-1), MathContext.DECIMAL128));
	        				doc.setMontoPeriodo(("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && (!listTipoComprobanteNotasCredito.contains(docto.idtipocomprobante))? docto.idcomprobantevalorapagar: docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal)).multiply(new BigDecimal(-1), MathContext.DECIMAL128)); 
	        				doc.setValorAPagar(docto.idcomprobantevalorapagar.multiply(new BigDecimal(-1), MathContext.DECIMAL128));	
	        				
	        			}else if("VENTA".equalsIgnoreCase(docto.acciontipooperacion) && docto.idcomprobantemontototal.compareTo(new BigDecimal(0)) == -1){	        				
	        				doc.setMontoNoFacturable(docto.idcomprobantemontototal.multiply(new BigDecimal(-1), MathContext.DECIMAL128));
	        				doc.setMontoPeriodo(("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && (!listTipoComprobanteNotasCredito.contains(docto.idtipocomprobante))? docto.idcomprobantevalorapagar: docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal)).multiply(new BigDecimal(-1), MathContext.DECIMAL128)); 
	        				doc.setValorAPagar(docto.idcomprobantevalorapagar.multiply(new BigDecimal(-1), MathContext.DECIMAL128));	
	        			}else {
	        				doc.setMontoNoFacturable(docto.idcomprobantemontototal);
	        				doc.setMontoPeriodo("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && (!listTipoComprobanteNotasCredito.contains(docto.idtipocomprobante))? docto.idcomprobantevalorapagar: docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal)); 
			        		doc.setValorAPagar(docto.idcomprobantevalorapagar);	
	        			}
		        	} else if (docto.idtipocomprobante.equalsIgnoreCase("NEE") && (docto.idTipoInstrumento.equalsIgnoreCase("SPT") || suscripcionEventoCapitalOSAS.equalsIgnoreCase(docto.idConcepto))) {
		        		BigDecimal montoNF = new BigDecimal(0L);
		        		montoNF = ("COMPRA".equalsIgnoreCase(docto.acciontipooperacion)) ? docto.idcomprobantemontototal : docto.idcomprobantemontototal.multiply(new BigDecimal(-1), MathContext.DECIMAL128);
		        		doc.setCodigoReceptor(docto.documentoidentidadreceptor);
        				doc.setMontoNoFacturable(montoNF);
        				doc.setMontoPeriodo(docto.idcomprobantemontonofacturable.add(montoNF, MathContext.DECIMAL128)); 
        				doc.setValorAPagar(("COMPRA".equalsIgnoreCase(docto.acciontipooperacion)) ? docto.idcomprobantevalorapagar : docto.idcomprobantevalorapagar.multiply(new BigDecimal(-1), MathContext.DECIMAL128));	
		        	}else{
		        		doc.setCodigoReceptor(docto.codigoreceptor);
		        		doc.setMontoNoFacturable(docto.idcomprobantemontonofacturable);docto.idtipocomprobante.equalsIgnoreCase("NEE");
		        		doc.setMontoPeriodo("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && (!listTipoComprobanteNotasCredito.contains(docto.idtipocomprobante))? docto.idcomprobantevalorapagar: docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal)); 
		        		doc.setValorAPagar(docto.idcomprobantevalorapagar);	
		        	}	        		
	        		doc.setIndServicio(3);
	        		doc.setClaveOrigen(docto.idcomprobante.toString());
	        	}

	        	doc.setRutReceptor(docto.documentoidentidadreceptor);

                if(arBoletas.contains(docto.idtipocomprobante)){
                    BigDecimal montoNF = new BigDecimal(0L);
                    BigDecimal vlrPagar = new BigDecimal(0L);
                    BigDecimal totalPeriodo = new BigDecimal(0L);
                    montoNF = docto.idcomprobantemontototal;
                    vlrPagar = docto.idcomprobantevalorapagar;
                    totalPeriodo = docto.idcomprobantemontonofacturable.add(montoNF, MathContext.DECIMAL128);
                    if("COMPRA".equalsIgnoreCase(docto.acciontipooperacion)) {
                    	montoNF = docto.idcomprobantemontototal.multiply(new BigDecimal(-1), MathContext.DECIMAL128);
                    	vlrPagar = docto.idcomprobantevalorapagar.multiply(new BigDecimal(-1), MathContext.DECIMAL128);
                    	totalPeriodo = docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal, MathContext.DECIMAL128).multiply(new BigDecimal(-1), MathContext.DECIMAL128);
                    }
                    doc.setMontoNoFacturable(montoNF);
                    doc.setCodigoReceptor(docto.documentoidentidadreceptor);
                    doc.setMontoPeriodo(totalPeriodo); 
                    doc.setValorAPagar(vlrPagar);
                }

	        	if(!arBoletas.contains(docto.idtipocomprobante)) {
	        		doc.setRazonSocialReceptor(docto.razonsocialreceptor);	
	        	}
	        	else {
	        		doc.setNombreReceptor(docto.razonsocialreceptor);
	        	}	        	
	        	
	        	doc.setDireccionReceptor(docto.direccionreceptor);
	        	doc.setComunaReceptor(docto.comunareceptor);
	        	doc.setCiudadReceptor(docto.ciudadreceptor);
	        	doc.setContactoReceptor(docto.contactoreceptor);
	        	doc.setIndBolsa(docto.indBolsa);
	        	doc.setIndMensaje_1(docto.indMensaje_1);
	        	doc.setEmailReceptor(docto.emailreceptor);
	        	doc.setSaldoAnterior("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) && (!listTipoComprobanteNotasCredito.contains(docto.idtipocomprobante))? BigDecimal.ZERO: (docto.idcomprobantevalorapagar.subtract(docto.idcomprobantemontonofacturable.add(docto.idcomprobantemontototal)))); //montoTotal + dataEnv.valorAPagar;
	        	doc.setTasaIva(docto.idcomprobantetasaiva);
	        	doc.setMontoIva(docto.montoIva);
	        	
	        	
	        	if(!arBoletas.contains(docto.idtipocomprobante)){
	        		//CELBUHOPRO
	        		if((docto.idtipocomprobante.equalsIgnoreCase("FEE") || docto.idtipocomprobante.equalsIgnoreCase("NEE")) && (docto.idTipoInstrumento.equalsIgnoreCase("SPT") || suscripcionEventoCapitalOSAS.equalsIgnoreCase(docto.idConcepto))) {
	        			doc.setMontoTotal(BigDecimal.ZERO);	
	        			doc.setMontoExento(BigDecimal.ZERO);
		        	} else {
		        		doc.setMontoTotal(docto.idcomprobantemontototal);
		        		doc.setMontoExento(docto.idcomprobantemontoexento);
		        	}	        			
	        	} else {
                    if(docto.idtipocomprobante.equalsIgnoreCase("BEE") && docto.idTipoInstrumento.equalsIgnoreCase("SPT")) {
                        doc.setMontoTotal(BigDecimal.ZERO);	
                        doc.setMontoExento(BigDecimal.ZERO);
                    } else {
                        doc.setMontoTotal(docto.idcomprobantevalorapagar);
                        doc.setMontoExento(docto.idcomprobantemontoexento);
                    }
	        	}
	        	
	        	doc.setMontoNeto(docto.montoNeto);
	        	doc.setAnulada(false);
	        	doc.setFechaVencimiento(docto.fechavencimiento==null ? null : docto.fechavencimiento.toString());
	        	doc.setForzarFolio(false);	        	
      	
	        	//** Ajuste Documentos ** //	        	
	        	if(!arBoletas.contains(docto.idtipocomprobante)) {
	        		ArrayList<AjusteDocumento> listaAjuste= new ArrayList<AjusteDocumento>();
                
                   //Comision
	        		AjusteDocumento ajusteCom = new AjusteDocumento();  	    	    
                    ajusteCom.setNumero(listaDetalle.size()+1);
	                ajusteCom.setTipo("R");  	    	    
	                ajusteCom.setGlosa(docto.glosaajustecomision);  	    	      	    	  
	                ajusteCom.setTipoValor("$");
	                ajusteCom.setValor(docto.totalcomisioncmbte);
	                listaAjuste.add(ajusteCom);
                
                
	                //Derecho
	                AjusteDocumento ajusteDer = new AjusteDocumento();
	                ajusteDer.setNumero(listaDetalle.size()+1);
	                ajusteDer.setTipo("R");  	    	    
	                ajusteDer.setGlosa(docto.glosaajustederecho);  	    	      	    	  
	                ajusteDer.setTipoValor("$");
	                ajusteDer.setValor(docto.totalderechocmbte);
	                listaAjuste.add(ajusteDer);
	                
	                //Gasto
	                AjusteDocumento ajusteGas = new AjusteDocumento();
	                ajusteGas.setNumero(listaDetalle.size()+1);
	                ajusteGas.setTipo("R");  	    	    
	                ajusteGas.setGlosa(docto.glosaajustegasto);  	    	      	    	  
	                ajusteGas.setTipoValor("$");
	                ajusteGas.setValor(docto.totalgastocmbte);
	                listaAjuste.add(ajusteGas);
	                
	                //Retencion
	                AjusteDocumento ajusteRet = new AjusteDocumento();
	                ajusteRet.setNumero(listaDetalle.size()+1);
	                ajusteRet.setTipo("R");  	    	    
	                ajusteRet.setGlosa(docto.glosaajusteretencion);  	    	      	    	  
	                ajusteRet.setTipoValor("$");
	                ajusteRet.setValor(docto.totalretencioncmbte);                
	                listaAjuste.add(ajusteRet);
	                
	                doc.setAjusteDocumentos(listaAjuste);
	        	}
                
                
  	    	    //** Parametros Documentos ** //
	        	if(!arBoletas.contains(docto.idtipocomprobante)) {
	  	    	    ArrayList<ParametroDocumento> listaParametro= new ArrayList<ParametroDocumento>();
	  	    	    
	  	    	    //Parametros 1
	  	    	    ParametroDocumento parametros1 = new ParametroDocumento();  	    	
	  	    	    parametros1.setNombre("KEY_PRODUCTO");
	  	    	    parametros1.setValor(docto.parametrokeyproducto!=null?docto.parametrokeyproducto:"");
	  	    	    listaParametro.add(parametros1);
	  	    	    
	  	        	//Parametros 2
	  	    	    if (docto.moneda != null) {
		  	    	    ParametroDocumento parametros2 = new ParametroDocumento();  	    	
		  	    	    parametros2.setNombre("KEY_MonPago");
		  	    	    parametros2.setValor(docto.moneda);
		  	    	    listaParametro.add(parametros2);
	  	    	    }
	  	    	    //Parametros 3
	  	    	    ParametroDocumento parametros3 = new ParametroDocumento();  	    	
	  	    	    parametros3.setNombre("KEY_MtoTotOM");
	  	    	    parametros3.setValor(docto.montoLiquidacionFinal);
	  	    	    listaParametro.add(parametros3);		        	
	  	    	    // JPMorgan
	  	    	    //Parametros 4
	  	    	    ParametroDocumento parametros4 = new ParametroDocumento();  	    	
	  	    	    parametros4.setNombre("KEY_NombreInstrumento");
	  	    	    parametros4.setValor(docto.nombreInstrumento);
	  	    	    listaParametro.add(parametros4);
	  	    	    //Parametros 5
	  	    	    ParametroDocumento parametros5 = new ParametroDocumento();  	    	
	  	    	    parametros5.setNombre("KEY_CantidadTotal");
	  	    	    parametros5.setValor(docto.cantidadTotal);
	  	    	    listaParametro.add(parametros5);
	  	    	    //Parametros 6
	  	    	    ParametroDocumento parametros6 = new ParametroDocumento();  	    	
	  	    	    parametros6.setNombre("KEY_ValorPrecioPromedio");
	  	    	    parametros6.setValor(docto.valorPrecioPromedio);
	  	    	    listaParametro.add(parametros6);
	  	    	    //Parametros 7
	  	    	    ParametroDocumento parametros7 = new ParametroDocumento();  	    	
	  	    	    parametros7.setNombre("KEY_IND_BOLSA");
	  	    	    parametros7.setValor(docto.indBolsa);
	  	    	    listaParametro.add(parametros7);
	  	    	    //Parametros 8
	  	    	    ParametroDocumento parametros8 = new ParametroDocumento();  	    	
	  	    	    parametros8.setNombre("KEY_MENSAJE_1");
	  	    	    parametros8.setValor(docto.indMensaje_1);
	  	    	    listaParametro.add(parametros8);
	  	    	    
	  	    	    if(docto.grupoDireccionesElectronicas != null) {
	  	    	    	ParametroDocumento parametros9 = new ParametroDocumento();  	    	
		  	    	    parametros9.setNombre("KEY_Email-CC");
		  	    	    parametros9.setValor(docto.grupoDireccionesElectronicas);
		  	    	    listaParametro.add(parametros9);
	  	    	    }
	  	    	    
	  	    	    
	  	    	    doc.setParametroDocumentos(listaParametro);
     		    }
	        	else {
	        		ArrayList<ParametroBoleta> listaParametro= new ArrayList<ParametroBoleta>();
	  	    	    doc.setParametroBoletas(null);
	        	}
  	    	    
  	    	    
	        		        	
	        	//** Detalle Comprobante ** //
  			    for(int i=0; i<arrTval.size();i++) {
  			    	
  				    ObjDte doctoDet = (ObjDte) arrTval.get(i);
    				ArrayList<ReferenciaDocumento> listaReferencia= new ArrayList<ReferenciaDocumento>();
    				ArrayList<ReferenciaBoleta> listaReferenciaB= new ArrayList<ReferenciaBoleta>();
  				
	    			if (docto.idcomprobante.equals(doctoDet.idcomprobante)) {
	    				if(!arBoletas.contains(docto.idtipocomprobante)) {
    					
		    				DetalleDocumento detalle = new DetalleDocumento();
							detalle.setId(null);
							detalle.setNumero(listaDetalle.size()+1);
							//CELBUHOPRO
							if((docto.idtipocomprobante.equalsIgnoreCase("FEE") || docto.idtipocomprobante.equalsIgnoreCase("NEE")) && docto.idTipoInstrumento.equalsIgnoreCase("SPT")) {
								detalle.setGlosa(arrTval.get(i).descripcionMonedaBancoCentral.concat(" ").concat(arrTval.get(i).idcomprobantedetalleglosa));								
				        	}else {
				        		detalle.setGlosa(arrTval.get(i).idcomprobantedetalleglosa);
				        	}
							detalle.setIndicadorExento(arrTval.get(i).idcomprobantedetalleesexento);
							detalle.setCantidad(arrTval.get(i).idcomprobantedetallecantidad);
							detalle.setPorcentajeDescuento(arrTval.get(i).idcomprobantedetalleporcentaje);
							detalle.setMonto(arrTval.get(i).idcomprobantedetallemonto);
							detalle.setPrecio(arrTval.get(i).idcomprobantedetalleprecio);
							detalle.setDescripcion(arrTval.get(i).idComprobantedetalledescripcion); 
							detalle.setPrecioOtraMoneda(arrTval.get(i).precioOtraMoneda);
							detalle.setFactorConversion(arrTval.get(i).factorConversion);
							detalle.setMoneda(arrTval.get(i).moneda);
							detalle.setPrecioOtraMonedaSecundaria(arrTval.get(i).precioOtraMonedaSecundaria);
							detalle.setFactorConversionSecundario(arrTval.get(i).factorConversionSecundario);
							detalle.setMonedaSecundaria(arrTval.get(i).monedaSecundaria);
							detalle.setMontoDescuento(arrTval.get(i).idcomprobantedetallemontodescuento);
							//CELBUHOPRO
							if((docto.idtipocomprobante.equalsIgnoreCase("FEE") || docto.idtipocomprobante.equalsIgnoreCase("NEE")) && docto.idTipoInstrumento.equalsIgnoreCase("SPT")) {
								detalle.setItemMoneda(arrTval.get(i).codigoMonedaBancoCentral);
								detalle.setItemNemo(arrTval.get(i).nemoMoneda);
								detalle.setItemTransaccion(arrTval.get(i).idComprobantedetalledescripcion);
				        	}
							// CELBUHOPRO-777
                            if(docto.idtipocomprobante.equalsIgnoreCase("NEE") && docto.idTipoInstrumento.equalsIgnoreCase("SPT")){
                                detalle.setPrecioReferencia("COMPRA".equalsIgnoreCase(docto.acciontipooperacion) ? arrTval.get(i).idcomprobantedetalleprecio : null);
                            }
							listaDetalle.add(detalle);
							
							//** Inicio Referencia Documentos ** //
			  	    	    //Referencia 1
			  	    	    ReferenciaDocumento referencia1 = new ReferenciaDocumento();
			  	    	
			  	    	    referencia1.setId(null);
			  	    	    referencia1.setNumero(0L);
			  	    	    referencia1.setFecha(arrTval.get(i).fechareferenciaidComprobante);  	    	    
			  	    	    referencia1.setTipo(arrTval.get(i).tiporeferenciaidComprobante);
			  	    	    referencia1.setRazon(arrTval.get(i).razonreferenciaidComprobante);  	    	  
			  	    	    referencia1.setFolio(arrTval.get(i).folioreferenciaidcomprobante);
			  	    	    referencia1.setCodigo(0L);
			  	    	    listaReferencia.add(referencia1);	  	    	
			  	    	    	
			  	    	    if(!arrTval.get(i).numeroreferencianota.equals(0L)) {
			  	    	       //Referencia ND / NC
			  	    	       ReferenciaDocumento referenciaND = new ReferenciaDocumento();
			  	    	       referenciaND.setId(null);
			  	    	       referenciaND.setNumero(arrTval.get(i).numeroreferencianota);
			  	    	       /////referenciaND.setFecha(arrTval.get(0).fechareferencianota == null ? "" : arrTval.get(0).fechareferencianota.toString());
			  	    	       referenciaND.setFecha(arrTval.get(i).fechareferencianota == null ? null : arrTval.get(i).fechareferencianota.toString());
			  	    	       referenciaND.setTipo(arrTval.get(i).tiporeferencianota);
			  	    	       referenciaND.setRazon(arrTval.get(i).razonreferencianota);  	    	  
			  	    	       referenciaND.setFolio(arrTval.get(i).folioreferencianota);
			  	    	       referenciaND.setCodigo(arrTval.get(i).codigoreferencianota);
			  	    	       referenciaND.setDocumentoEditable(null);  	    	    
			  	    	       listaReferencia.add(referenciaND);    	    	
			  	    	    }
			  	    	    if(!arrTval.get(i).folioReferenciaOtrosConceptos.equals("")) {
			  	    	    	ReferenciaDocumento referenciaOC = new ReferenciaDocumento();
			  	    	    	referenciaOC.setId(null);
				  	    	    referenciaOC.setNumero(0L);
				  	    	    referenciaOC.setFecha(arrTval.get(i).fechaReferenciaOtrosConceptos.toString());
				  	    	    referenciaOC.setTipo(arrTval.get(i).tipoReferenciaOtrosConceptos);
				  	    	    referenciaOC.setRazon(arrTval.get(i).razonReferenciaOtrosConceptos);
				  	    	    referenciaOC.setFolio(arrTval.get(i).folioReferenciaOtrosConceptos);
				  	    	    referenciaOC.setCodigo(0L);
				  	    	    listaReferencia.add(referenciaOC);				  	    	    
			  	    	    }
			  	    	    doc.setReferenciaDocumentos(listaReferencia);
			  	    	    //** Fin Referencia Documentos ** //
	    				}
	    				else {
    					  	DetalleBoleta detalle = new DetalleBoleta();
							detalle.setId(null);
							detalle.setNumero(listaDetalle.size()+1);			
							detalle.setGlosa(arrTval.get(i).idcomprobantedetalleglosa);
							detalle.setDescripcion(arrTval.get(i).idComprobantedetalledescripcion);
							detalle.setCantidad(arrTval.get(i).idcomprobantedetallecantidad);
							detalle.setPrecio(arrTval.get(i).idcomprobantedetalleprecio);
							detalle.setPorcentajeAjuste(0.0);
							detalle.setMontoAjuste(0.0);
							detalle.setMonto(arrTval.get(i).idcomprobantedetallemonto);	
							
                            if(docto.idtipocomprobante.equalsIgnoreCase("BEE") && docto.idTipoInstrumento.equalsIgnoreCase("SPT")) {
								int indExe = arrTval.get(i).idcomprobantedetalleesexento.intValue();
								detalle.setIndicadorExento(indExe);
				        	} else {
				        		detalle.setIndicadorExento(0);
				        	}								
							
                            if(docto.idtipocomprobante.equalsIgnoreCase("BEE") && docto.idTipoInstrumento.equalsIgnoreCase("SPT")) {
								detalle.setItemMoneda(arrTval.get(i).codigoMonedaBancoCentral);
								detalle.setItemNemo(arrTval.get(i).nemoMoneda);
								detalle.setItemTransaccion(arrTval.get(i).idComprobantedetalledescripcion);
				        	}
                            
							listaDetalleB.add(detalle);
							
							//-----
							//** Inicio Referencia Documentos ** //											    	   	
			    	          //Referencia 1
			    	          ReferenciaBoleta referencia1 = new ReferenciaBoleta();
			    	
			    	          referencia1.setId(0);
			    	          referencia1.setNumero(0);
			    	          referencia1.setFecha(arrTval.get(i).fechareferenciaidComprobante);  	    	    
			    	          referencia1.setTipo(arrTval.get(i).tiporeferenciaidComprobante);
			    	          referencia1.setRazon(arrTval.get(i).razonreferenciaidComprobante);  	    	  
			    	          referencia1.setFolio(arrTval.get(i).folioreferenciaidcomprobante);
			    	          listaReferenciaB.add(referencia1);    	
			    	    		    	
			    	          doc.setReferenciaBoletas(listaReferenciaB);
			    	        //** Fin Referencia Documentos ** //
							//-----
	    				}
	    			}
  			    }
  			
	    		//** Asigna comprobante de la linea que se esta recorriendo    	    	
  	    	    documentos.add(docto.idcomprobante);
  	    	    //**  	    	
	    		
  	    	    if(!arBoletas.contains(docto.idtipocomprobante)) {
  	    	    	doc.setDetalleDocumentos(listaDetalle);  
  	    	    }
  	    	    else {
  	    	    	doc.setDetalleBoletas(listaDetalleB);
  	    	    }
  	    	  
  	    	    Gson gson = new Gson();
  	    	    String jsonInString = gson.toJson(doc);
  	    	    logger.debug(jsonInString);
	        	salida.put(docto.idcomprobante.toString(), jsonInString);
  		    }
  	    }
  	    return salida;
	}

    // Rutina Consultar DTE
    public void consultarDte(List<ObjDte> arrTval, Connection con, List<ObjDte> arrTvalPosibleError) throws Exception{
     	Map<String,String> datosConsultarDTE = prepararJsonAConsultar(arrTval); // armo json
     	
     	if(datosConsultarDTE.isEmpty()) {
     		datosConsultarDTE = prepararJsonAConsultarPorIdComprobanteYRutEmisor(arrTval); 
     	}
     	
     	Map<String,String> respuesta = callDte.consultarDocumento(datosConsultarDTE);   //envio
     	List<Long> cmbteEnv = new ArrayList<>();
     	List<ObjDte> arrTvalTemp = new ArrayList<>();
     	     	    	
     	for(int i=0; i<arrTval.size();i++){
     		if(cmbteEnv.contains(arrTval.get(i).idcomprobante)) continue;
     		
     		arrTvalTemp.add(arrTval.get(i));
     		cmbteEnv.add(arrTval.get(i).idcomprobante);
     	}

     	//tratar la respuesta (metodo que actualiza tabla)
     	for(int i=0; i<respuesta.size();i++){   	       	   
     		try {
     			//parametro de entrada 	
         		ObjDte tval = arrTvalTemp.get(i);  
     			//parametros de respuesta de consulta a dte
         	    JSONObject obj = new JSONObject(respuesta.get(tval.idcomprobante.toString()));
        		   
         	    if (arBoletas.contains(tval.idtipocomprobante)) {
         	    	updateConsTareaDte(con, tval, obj, obj.get("folio").toString(), "NA", false);
         	    }
         	    else{
         	    	updateConsTareaDte(con, tval, obj, obj.get("folio").toString(), obj.getString("estadoSII"), false);
         	    } 
     		}catch(Exception e) {
     			logger.error(e);
     			continue;
     		}	
     	}
     	
     	/* Posible fallo en la respuesta anterior, se puede dar si documentos se encuentren procesados en DTE 
     	 y no se actualizo informacion de Optimus en ese proceso . */
     	if(!arrTvalPosibleError.isEmpty()) {
     		consultarDtePorPosibleErrores(arrTvalPosibleError, con);
     	}
    }   
    
    // Rutina Consultar DTE --Opcional segun errores
    public void consultarDtePorPosibleErrores(List<ObjDte> arrTvalPosibleError, Connection con) throws Exception{
    	logger.debug("inicio consultarDtePorPosibleErrores");
    	SQL sql = new SQL();
    	List<ObjDte> arrTvalPosibleErrorTemp = arrTvalPosibleError.stream().filter(ObjDte->ObjDte.idcomprobante != null).map(ObjDte->ObjDte).distinct().collect(Collectors.toList());
    	Map<String,String>  datosConsultarDTE = null;
    	Map<String,String> respuesta = null;
 		try {
 			datosConsultarDTE = prepararJsonAConsultarPorIdComprobanteYRutEmisor(arrTvalPosibleErrorTemp); // armo json
 			respuesta = callDte.consultarDocumento(datosConsultarDTE);   //envio
 			List<ObjDte> listActualizarTareaDTE = new ArrayList<>();
 			List<ObjDte> listActualizarComprobante = new ArrayList<>();
 			
 			//Preparamos para actualizar rutas
 			for(int i=0; i<respuesta.size();i++){   
 				try {
 					ObjDte tval = arrTvalPosibleErrorTemp.get(i);  
 	         	    JSONObject obj = new JSONObject(respuesta.get(tval.idcomprobante.toString()));
 	         	    if(obj!=null && obj.get("folio")!= null) {
                        String dirresp = arBoletas.contains(tval.idtipocomprobante) ? "/api/boletaEditables/" + obj.get("id") : "/api/documentoEditables/" + obj.get("id");
						//Urls informes obtenidos desde DTE
						String urlPDF = callDte.getServer()+"/files/pdf/"+obj.get("id");
						String urlXML = callDte.getServer()+"/files/xml/"+obj.get("id");
						tval.idestadosii = "PEN";
			 	   	    tval.idestadodte = "REC";
			 	   	    tval.idrespuestadte = obj.get("id") == null ? 0L : Long.parseLong(obj.get("id").toString());
			 	   	    tval.respuestalocationdte = dirresp;
			 	   	    tval.respuestapdfdte = urlPDF;
			 	   	    tval.respuestaxmldte = urlXML;
			 	   	    tval.actualizarPorCampo = "IdTareaDTEGrupo";	
			 	   	    tval.actualizarPorCampo2 = "IdComprobante";
			 	   	    listActualizarTareaDTE.add(tval);
			 	   	    if(obj.get("id") != null) {
			 	   	    	listActualizarComprobante.add(tval);
			 	   	    }
 	         	    }
 				}catch(Exception e ) {
 					logger.error(e);
 				} 				
 			}
 			
 			try {
 				updateEnvTareaDteCmbte(con, sql, listActualizarTareaDTE, listActualizarComprobante);
 			}catch(Exception e) {
 				logger.error(e);
 			}
 			
 			//liquidamos estados finales
 			for(int i=0; i<respuesta.size();i++){
 				try {
 					ObjDte tval = listActualizarTareaDTE.get(i);  
 	         	    JSONObject obj = new JSONObject(respuesta.get(tval.idcomprobante.toString()));
 	         	    if(obj!=null && obj.get("folio")!= null) {
 	         	    	if (arBoletas.contains(tval.idtipocomprobante)) {
     	         	    	updateConsTareaDte(con, tval, obj, obj.get("folio").toString(), "NA", true);
     	         	    }
     	         	    else{
     	         	    	updateConsTareaDte(con, tval, obj, obj.get("folio").toString(), obj.getString("estadoSII"), true);
     	         	    } 
 	         	    }
 				}catch(Exception e) {
 					logger.error(e);
 				}
 			}
 		}catch(Exception e) {
 			logger.error(e);
 		}
 		
 		logger.debug("fin consultarDtePorPosibleErrores");
    }

  	// Prepara Json Consultar DTE
  	public Map<String,String> prepararJsonAConsultar(List<ObjDte> arrTval)
    {    	
		ArrayList<Long> documentos = new ArrayList<Long>();
	    Map<String,String> salida = new HashMap<String, String>();
	
	    for(int j=0; j<arrTval.size();j++)
	      {
		    ObjDte docto = (ObjDte) arrTval.get(j);    		
		    if (!documentos.contains(docto.idcomprobante))
		      {		    	
	        	salida.put(docto.idcomprobante.toString(), docto.respuestalocationdte);

	        	documentos.add(docto.idcomprobante);
		    }
	      }
	    return salida;
    }
  	
  	// Prepara Json Consultar DTE por IdComprobante y RutEmisor
   	public Map<String,String> prepararJsonAConsultarPorIdComprobanteYRutEmisor(List<ObjDte> arrTval){  
   		
   		Map<String,String> salida = new HashMap<String, String>();
   		
   		for(ObjDte obje : arrTval ) {
            String url = arBoletas.contains(obje.idtipocomprobante) ? "/api/boletaEditables/claveOrigen/" : "/api/documentoEditables/claveOrigen/";
   			if( (obje.idcomprobante!=null && !(obje.idcomprobante.compareTo(0L)==0)) && (obje.documentoidentidademisor!=null && !(obje.documentoidentidademisor.trim().length()==0)) ) {
   				salida.put(obje.idcomprobante.toString(), url+obje.idcomprobante.toString()+"/"+obje.documentoidentidademisor.trim());
   			}
   		}

 	    return salida;
     }
  	
   	// Actualiza estados y respuesta en tabla tareadte y comprobante
    private void updateEnvTareaDteCmbte(Connection con,  SQL sql, List<ObjDte> listadoTareaDTE, List<ObjDte> listadoComprobante) throws Exception {	
    	logger.debug("inicio updateEnvTareaDteCmbte");

    	// Actualiza Tarea DTE con respuesta del SII y nuevos estados del DTE
    	sql.batch(con, SQL_UPDATE_TAREA_DTE, listadoTareaDTE, ObjDte.class);
    	
    	if(!listadoComprobante.isEmpty()) {
    		// Actualiza Comprobante con respuesta del SII y nuevos estados del DTE
    		sql.batch(con, SQL_UPDATE_COMPROBANTE, listadoComprobante, ObjDte.class);
    	}
    }

    private void updateErrTareaDteCmbte(Connection con,  SQL sql, List<ObjDte> listadoTareaDTE, List<ObjDte> listadoComprobante) throws Exception {	
    	logger.debug("Inicio updateErrTareaDteCmbte");

    	// Actualiza Tarea DTE con respuesta del SII y nuevos estados del DTE
    	sql.batch(con, SQL_UPDATE_ERROR_TAREA_DTE, listadoTareaDTE, ObjDte.class);
    	
    	if(!listadoComprobante.isEmpty()) {
    		// Actualiza Comprobante con respuesta del SII y nuevos estados del DTE
    		sql.batch(con, SQL_UPDATE_COMPROBANTE, listadoComprobante, ObjDte.class);
    	}
    	logger.debug("Fin updateErrTareaDteCmbte");
    }

    // Actualiza datos en tabla tareadte y comprobante
    private void updateConsTareaDte(Connection con,ObjDte objCons, Object ObjResp, String folio, String estadoSII, boolean flujoSecundario) throws SQLException {	
    	//Reemplaza estadosii segun valor que se obtiene de dte
    	String valor = "";
    	String valorEst = "";
    	
    	if (estadoSII.equalsIgnoreCase("OK") || ((estadoSII.equalsIgnoreCase("0")) && (!"0".equalsIgnoreCase(folio))) || ((estadoSII.equalsIgnoreCase("DOK")) && (!"0".equalsIgnoreCase(folio)))) {	
    		valor = "ACP"; 
    	    valorEst =  "ENV";
    	}
    	else {
    		if(estadoSII.equalsIgnoreCase("NA")){
    			valor = "NAP";
    			valorEst =  "ENV";
    		}
    		else {
    			valor = "ERR";
    	    	valorEst =  "PEN";
    	    	folio= "0";
    		}
    	}  
    	
    	String queryUpd ="UPDATE \"BSCL.Facturacion.BD::FAC.TareaDTE\" SET ";        
		queryUpd+=" \"FolioComprobanteRespuestaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoTareaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoSII\" = ? ,";
    	queryUpd+=" \"RespuestaDTEEnvioSII\" = ? ";
    	

    	queryUpd+=" WHERE \"IdTareaDTEGrupo\"= ? AND \"IdComprobante\"= ? AND \"IdEstadoTareaDTE\" <> 'ACP'";
    	logger.debug(queryUpd);
        
        try(PreparedStatement st = con.prepareStatement(queryUpd);) {
        	st.setObject(1, folio);
        	st.setString(2, valorEst);
        	st.setString(3, valor);
        	
        	logger.debug(ObjResp.toString()); //Registra log
        	
        	String flujSecund = (flujoSecundario == true)? "FS," :"";
        	String objRespTemp = flujSecund+ObjResp.toString();
        	
        	if (objRespTemp.toString().length() > 5000) {
        	  st.setString(4, objRespTemp.toString().substring(0, 4999));
        	}
        	else {
        	  st.setString(4, objRespTemp.toString());
        	}

        	st.setLong(5,objCons.idtareadtegrupo);
        	st.setLong(6,objCons.idcomprobante);
        	
        	
        	st.execute();
          }
        
        
        String queryUpdCmbte ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";
        queryUpdCmbte+=" \"FolioComprobante\" = ? ,";
		queryUpdCmbte+=" \"IdEstadoSII\" = ? ,";
		queryUpdCmbte+=" \"RespuestaDTEEnvioSII\" = ? ";
        queryUpdCmbte+=" WHERE \"IdComprobante\"= ?";
        logger.debug(queryUpdCmbte);
        
        try(PreparedStatement stCmbte = con.prepareStatement(queryUpdCmbte);) {
        	stCmbte.setString(1, folio);
        	stCmbte.setString(2, valor);
        	
        	if (ObjResp.toString().length() > 5000)
        		stCmbte.setString(3, ObjResp.toString().substring(0,5000));
          	else
          		stCmbte.setString(3, ObjResp.toString());
        	
        	stCmbte.setObject(4,objCons.idcomprobante);
        	stCmbte.execute();
        }
        
        if (objCons.codigoreferencianota > 0) {
        	String queryUpdCmbteRef ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";
            queryUpdCmbteRef+=" \"FolioComprobanteReferencia\" = ? ";
            queryUpdCmbteRef+=" WHERE \"IdComprobanteReferencia\"= ?";
            logger.debug(queryUpdCmbteRef);         
            
            try(PreparedStatement stCmbteRef = con.prepareStatement(queryUpdCmbteRef);) {
            	stCmbteRef.setString(1, folio);
            	stCmbteRef.setObject(2,objCons.idcomprobante);
            	stCmbteRef.execute();
            }
        }
        
        
      }
    
    

    // Rutina enviar DTE Libro
    public void enviarDteLibro(List<ObjDte> arrTval, Connection con, SQL sql) throws Exception {
        Map<String, String> datosEnviarDTE = prepararJsonAEnviarLibro(arrTval); // armo json                                                                                
        Map<String, String> respuesta = callDte.enviarWsLibro(datosEnviarDTE); // envio

        // tratar la respuesta (metodo que actualiza tabla)
        for (int i = 0; i < arrTval.size(); i++) {
            ObjDte tval = arrTval.get(i);
            String[] idresp = respuesta.get(tval.idcomprobante.toString()).toString().split("/");
            String[] dirresp = respuesta.get(tval.idcomprobante.toString()).toString().split("=");
            // http://161.238.39.46:1580/files/pdf/1946

            // Urls informes obtenidos desde DTE
            String urlPDF = callDte.getServer() + "/files/pdf/" + idresp[3];
            String urlXML = callDte.getServer() + "/files/xml/" + idresp[3];

            updateEnvTareaDteLibro(con, tval, idresp[3], dirresp[0], urlPDF, urlXML, sql);
        }
    }
  	

  	
    // Prepara Json para enviar libro
  	public Map<String,String> prepararJsonAEnviarLibro(List<ObjDte> arrTval)
    {    	
		ArrayList<Integer> documentos = new ArrayList<Integer>();		
	    Map<String,String> salida = new HashMap<String, String>();
	    
	    ObjDte docto = (ObjDte) arrTval.get(0);    		
	    EnvioDTELibro doc = new EnvioDTELibro();
	    
	    if (!documentos.contains(docto.idcomprobante))
	    {
	    	//** Libro CV **//
	    	LibroCv libenc = new LibroCv();
	    	libenc.setTipo("VENTA");
    		libenc.setPeriodoContable(docto.periodocontable);
    		libenc.setEmpresa(docto.documentoidentidademisor);
   		
    		
    		//** Libro CV Detalle **//
    		ArrayList<LibroCvDetalle> libroCVDetalle= new ArrayList<LibroCvDetalle>();
    		for(int i=0; i<arrTval.size();i++)
    		{
   				LibroCvDetalle librocvdet = new LibroCvDetalle();
   								 
    			librocvdet.setTipoDocumento(arrTval.get(i).tipocomprobantedte);
    			librocvdet.setFolioDocumento(arrTval.get(i).foliocomprobanterespuestadte);
    			librocvdet.setFechaDocumento(arrTval.get(i).fechacomprobante==null ? null : arrTval.get(i).fechacomprobante.toString());    			
    			librocvdet.setRutDocumento(arrTval.get(i).documentoidentidadreceptor);
    			librocvdet.setRazonSocial(arrTval.get(i).razonsocialreceptor);
    			librocvdet.setMontoNeto(arrTval.get(i).montoNeto);
    			librocvdet.setMontoExento(arrTval.get(i).idcomprobantemontoexento);
    			librocvdet.setMontoIva(arrTval.get(i).montoIva);
    			librocvdet.setMontoTotal(arrTval.get(i).idcomprobantemontototal);
	    				 
	    		libroCVDetalle.add(librocvdet);
    		}
    		
	        //** Libro CV Total **//
    		ArrayList<LibroCvTotal> libroCVTotal= new ArrayList<LibroCvTotal>();
    		ArrayList<Long> libroCVTotalNumeros= new ArrayList<Long>();
	    	
	    	for(int i=0; i<arrTval.size();i++)
			{
	    		ObjDte librocvTot = (ObjDte) arrTval.get(i);

	    		if (!libroCVTotalNumeros.contains(librocvTot.tipocomprobantedte))
	    		{
	    			LibroCvTotal librocvtot = new LibroCvTotal();
	    			
	    			librocvtot.setTipoDocumento(arrTval.get(i).tipocomprobantedte);
	    			librocvtot.setCantidadDocumentos(arrTval.get(i).totalidtipocomprobantecantidaddocumentos);
	    			librocvtot.setCantidadAnulados(arrTval.get(i).totalidtipocomprobantecantidadanulados);
	    			librocvtot.setTotalMontoNeto(arrTval.get(i).totalidtipocomprobantemontoneto);
	    			librocvtot.setTotalMontoExento(arrTval.get(i).totalidtipocomprobantemontoexento);
	    			librocvtot.setTotalMontoIva(arrTval.get(i).totalidtipocomprobantemontoiva);
	    			librocvtot.setTotalMontoTotal(arrTval.get(i).totalidtipocomprobantemontototal);
	    				 
	    			libroCVTotal.add(librocvtot);
	    			libroCVTotalNumeros.add(arrTval.get(i).tipocomprobantedte);
	    		}
	    	}        	
	        	
    		doc.setLibroCv(libenc);
    		doc.setLibroCvDetalle(libroCVDetalle);
        	doc.setLibroCVTotal(libroCVTotal);
			    	    		
	    	Gson gson = new Gson();
	        String jsonInString = gson.toJson(doc);
	        logger.debug(jsonInString);
	        salida.put(docto.idcomprobante.toString(), jsonInString);
      }
	      
	    return salida;
    }
  	

  	 
  	
    // Actualiza datos en tabla tareadte y comprobante
    private void updateEnvTareaDteLibro(Connection con,ObjDte obj, String respuesta, String dirresp, String urlPDF, String urlXML, SQL sql) throws SQLException 
    {
        
    	logger.debug("inicio updateEnvTareaDteLibro");
    	
		List<ObjDte> lstObjDteUpdate = new ArrayList<ObjDte>();   	
		obj.idestadotareadte 		= "ENV";
		obj.idestadodte 			= "REC";
		obj.idestadosii				= "PEN";
		obj.idrespuestadte 			= respuesta == null ? 0L : Long.parseLong(respuesta);
		obj.respuestalocationdte 	= dirresp;
		obj.respuestapdfdte 		= urlPDF;
		obj.respuestaxmldte 		= urlXML;		
		obj.actualizarPorCampo      = "IdTareaDTE";		
		lstObjDteUpdate.add(obj);
		
    	// Actualiza Tarea DTE con respuesta del SII y nuevos estados del DTE
    	sql.batch(con, SQL_UPDATE_TAREA_DTE, lstObjDteUpdate, ObjDte.class);
    	
		// Actualiza Comprobante con respuesta del SII y nuevos estados del DTE
		sql.batch(con, SQL_UPDATE_COMPROBANTE, lstObjDteUpdate, ObjDte.class);
		
		logger.debug("fin updateEnvTareaDteLibro");
      }
  	
	

	public static class CuadroResumen 
	{
		public Long 		IdTareaDTE;
		public String 		NombreInstrumento;
		public BigDecimal 	CantidadTotal;
		public BigDecimal 	ValorPrecioPromedio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	
	// ------------------------------------------------------------------------------------------------------
	// CONFIGURACION PERU
	// ------------------------------------------------------------------------------------------------------

	public void enviarDteCmbte2_0(ArrayList<ObjDte2_0> arrTval, Connection con, Long segundosPorComprobante) throws Exception
    {
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "PLATAFORMA", "PLA.LocalizacionSistema");
		Map<String,String> respuesta=null;		

		Map<String,String> datosEnviarDTEPE = prepararJsonAEnviarCmbtePE(arrTval); // armo json		
		
		for (ObjDte2_0 tval : arrTval) {
			String jsonDatosEnviarDTE = datosEnviarDTEPE.get(tval.IdComprobante.toString());
			respuesta = callDte.enviarDocumento(tval.IdComprobante.toString(), jsonDatosEnviarDTE, localizacionSistema, tval.IdTipoComprobante, segundosPorComprobante);
				 	       	
			//tratar la respuesta (metodo que actualiza tabla) 	   
  		    String[] idresp = respuesta.get(tval.IdComprobante.toString()).toString().split("/");
 	   	    String[] dirresp = respuesta.get(tval.IdComprobante.toString()).toString().split("=");
 		        
 		    //Urls informes obtenidos desde DTE
 		    String urlPDF = callDte.getServer()+"/files/pdf/"+idresp[3];
 		    String urlXML = callDte.getServer()+"/files/xml/"+idresp[3];
 		        
 		   updateEnvTareaDteCmbte2_0(con,tval,idresp[3], dirresp[0], urlPDF, urlXML);
 	      }
 	    
		consultarDte2_0(arrTval, con);
           
    }
	
    public void consultarDte2_0(List<ObjDte2_0> arrTval, Connection con) throws Exception{
     	Map<String,String> datosConsultarDTE = prepararJsonAConsultar2_0(arrTval); // armo json
     	Map<String,String> respuesta = callDte.consultarDocumento(datosConsultarDTE);   //envio
     	     	    	
     	//tratar la respuesta (metodo que actualiza tabla)
     	for(int i=0; i<respuesta.size();i++){
     	    //parametro de entrada 	
     	    ObjDte2_0 tval = arrTval.get(i);	
     	       
     	    //parametros de respuesta de consulta a dte
     	    JSONObject obj = new JSONObject(respuesta.get(tval.IdComprobante.toString()));
    		   
            //datos para actualizar comprobante y tareadte     	    
     	   String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "PLATAFORMA", "PLA.LocalizacionSistema");
	
     	   //para polizas no devuelve estado sunat
     	   if (localizacionSistema.equalsIgnoreCase("PE") && arrTval.get(0).IdTipoComprobante.equalsIgnoreCase("POL")) {
     		   updateConsTareaDte2_0(con, tval, obj, obj.get("folio").toString(), "OK");
     	   }
     	   else if (localizacionSistema.equalsIgnoreCase("PE")) {
    		   updateConsTareaDte2_0(con, tval, obj, obj.get("folio").toString(), obj.getString("estadoSunat"));
    	   }
     	   else {
     		  updateConsTareaDte2_0(con, tval, obj, obj.get("folio").toString(), obj.getString("estadoSII"));
     	   }
   		  
     	}
    }
    
    public void enviarDteLibro2_0(List<ObjDte2_0> arrTval, Connection con) throws Exception {
        Map<String, String> datosEnviarDTE = prepararJsonAEnviarLibro2_0(arrTval); // armo
                                                                                // json
        Map<String, String> respuesta = callDte.enviarWsLibro(datosEnviarDTE); // envio

        // tratar la respuesta (metodo que actualiza tabla)
        for (int i = 0; i < arrTval.size(); i++) {
            ObjDte2_0 tval = arrTval.get(i);
            String[] idresp = respuesta.get(tval.IdComprobante.toString()).toString().split("/");
            String[] dirresp = respuesta.get(tval.IdComprobante.toString()).toString().split("=");
            // http://161.238.39.46:1580/files/pdf/1946

            // Urls informes obtenidos desde DTE
            String urlPDF = callDte.getServer() + "/files/pdf/" + idresp[3];
            String urlXML = callDte.getServer() + "/files/xml/" + idresp[3];

            updateEnvTareaDteLibro2_0(con, tval, idresp[3], dirresp[0], urlPDF, urlXML);
        }
    }
    
    
      	public Map<String,String> prepararJsonAEnviarLibro2_0(List<ObjDte2_0> arrTval)
    {    	
		ArrayList<Integer> documentos = new ArrayList<Integer>();		
	    Map<String,String> salida = new HashMap<String, String>();
	    
	    ObjDte2_0 docto = (ObjDte2_0) arrTval.get(0);    		
	    EnvioDTELibro doc = new EnvioDTELibro();
	    
	    if (!documentos.contains(docto.IdComprobante))
	    {
	    	//** Libro CV **
	    	LibroCv libenc = new LibroCv();
	    	libenc.setTipo("VENTA");
    		libenc.setPeriodoContable(docto.periodocontable);
    		libenc.setEmpresa(docto.DocumentoIdentidadEmisor);
   		
    		
    		//** Libro CV Detalle **
    		ArrayList<LibroCvDetalle> libroCVDetalle= new ArrayList<LibroCvDetalle>();
    		for(int i=0; i<arrTval.size();i++)
    		{
   				LibroCvDetalle librocvdet = new LibroCvDetalle();
   								 
    			librocvdet.setTipoDocumento(arrTval.get(i).TipoComprobanteDTE);
    			librocvdet.setFolioDocumento(arrTval.get(i).FolioComprobanteRespuestaDTE);
    			librocvdet.setFechaDocumento(Fecha.toStringDate(arrTval.get(i).FechaComprobante));
    			librocvdet.setRutDocumento(arrTval.get(i).DocumentoIdentidadReceptor);
    			librocvdet.setRazonSocial(arrTval.get(i).RazonSocialReceptor);
    			librocvdet.setMontoNeto(arrTval.get(i).montoNeto);
    			librocvdet.setMontoExento(arrTval.get(i).idcomprobantemontoexento);
    			librocvdet.setMontoIva(arrTval.get(i).idcomprobantemontoiva);
    			librocvdet.setMontoTotal(arrTval.get(i).idcomprobantemontototal);
	    				 
	    		libroCVDetalle.add(librocvdet);
    		}
    		
	        //** Libro CV Total **
    		ArrayList<LibroCvTotal> libroCVTotal= new ArrayList<LibroCvTotal>();
    		ArrayList<Long> libroCVTotalNumeros= new ArrayList<Long>();
	    	
	    	for(int i=0; i<arrTval.size();i++)
			{
	    		ObjDte2_0 librocvTot = (ObjDte2_0) arrTval.get(i);

	    		if (!libroCVTotalNumeros.contains(librocvTot.TipoComprobanteDTE))
	    		{
	    			LibroCvTotal librocvtot = new LibroCvTotal();
	    			
	    			librocvtot.setTipoDocumento(arrTval.get(i).TipoComprobanteDTE);
	    			librocvtot.setCantidadDocumentos(arrTval.get(i).totalidtipocomprobantecantidaddocumentos.intValue());
	    			librocvtot.setCantidadAnulados(arrTval.get(i).totalidtipocomprobantecantidadanulados.intValue());
	    			librocvtot.setTotalMontoNeto(arrTval.get(i).totalidtipocomprobantemontoneto);
	    			librocvtot.setTotalMontoExento(arrTval.get(i).totalidtipocomprobantemontoexento);
	    			librocvtot.setTotalMontoIva(arrTval.get(i).totalidtipocomprobantemontoiva);
	    			librocvtot.setTotalMontoTotal(arrTval.get(i).totalidtipocomprobantemontototal);
	    				 
	    			libroCVTotal.add(librocvtot);
	    			libroCVTotalNumeros.add(arrTval.get(i).TipoComprobanteDTE);
	    		}
	    	}        	
	        	
    		doc.setLibroCv(libenc);
    		doc.setLibroCvDetalle(libroCVDetalle);
        	doc.setLibroCVTotal(libroCVTotal);
			    	    		
	    	Gson gson = new Gson();
	        String jsonInString = gson.toJson(doc);
	        logger.debug(jsonInString);
	        salida.put(docto.IdComprobante.toString(), jsonInString);
      }
	      
	    return salida;
    }
    
    
    	//*** Rutina prepara Json para enviar cmbte PERU
	public Map<String,String> prepararJsonAEnviarCmbtePE(ArrayList<ObjDte2_0> arrTval) {		
		String fechaVcto;
		
		List<String> listTipoComprobante = Arrays.asList(new String[]{"NCE", "NCR", "NEO"});
		ArrayList<Long> documentos = new ArrayList<Long>();
  	    Map<String,String> salida = new HashMap<String, String>();
  	
  	    for(int j=0; j<arrTval.size();j++) {
  	    	ObjDte2_0 docto = (ObjDte2_0) arrTval.get(j);
  		    
  	    	if (!documentos.contains(docto.IdComprobante)) {
  		    	ComprobanteDTEPE doc = new ComprobanteDTEPE();
  		    	
  		    	if (docto.IdTipoComprobante.equalsIgnoreCase("POL")) {
  		    		
  		    		if (docto.PolizaIngles.equalsIgnoreCase("S")) {
  		    			doc.lenguaje = "EN";
  		    		}
  		    		else {
  		    			doc.lenguaje = "ES";
  		    		}  		    			
  		    		
  		    		//** POLIZA **
  		    		doc.id = null;
  		    		doc.fecha = Fecha.toStringDate(docto.FechaComprobante);
  		    		doc.tipo = docto.TipoComprobanteDTE;
  		    		doc.rucEmisor  = docto.DocumentoIdentidadEmisor;
  		    		doc.razonSocialEmisor = docto.RazonSocialEmisor;
  		    		doc.direccionEmisor = docto.DireccionEmisor;
  		    		doc.urbanizacionEmisor = docto.DireccionEmisor;
  		    		doc.provinciaEmisor = docto.ComunaEmisor;
  		    		doc.departamentoEmisor = docto.ComunaEmisor;
  		    		doc.distritoEmisor = docto.CiudadEmisor;
  		    		doc.telefonoEmisor = docto.TelefonoEmisor;
  		    		doc.emailEmisor = docto.EmailEmisor;
  		    		  		    		
  		    		doc.tipoIdentificadorReceptor = "";
  		    		doc.iup = docto.DocumentoIdentidadReceptor; //Linea Cliente (1era linea) 
  		    		doc.razonSocialReceptor = docto.RazonSocialReceptor;  		
  		    		
  		    		doc.rucReceptor = docto.DocumentoIdentidadAsociado; //Linea Documento (2da linea) 
  		    		if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("DNI")) {
  		        		doc.codigoIdentificadorReceptor = "1";
  		        	}
  		        	else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("CE")) {
  		        		doc.codigoIdentificadorReceptor = "4";
  		        	}
  		        	else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("RUC")) {
  		        		doc.codigoIdentificadorReceptor = "6";
  		        	}
  		        	else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("PAS")) {
  		        		doc.codigoIdentificadorReceptor = "7";
  		        	}
  		        	else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("CDI")) {
  		        		doc.codigoIdentificadorReceptor = "A";
  		        	}
  		        	else {
  		        		doc.codigoIdentificadorReceptor = "0";
  		        	}
  		    		
  		    		doc.rutCavali = docto.DocumentoIdentidadAdicionalReceptor; //Linea RUT(3era linea)
  		    		
  		    		doc.direccionReceptor = docto.DireccionReceptor;
  		        	doc.urbanizacionReceptor = docto.DireccionReceptor;
  		        	doc.provinciaReceptor = docto.ComunaReceptor;
  		        	doc.departamentoReceptor = docto.ComunaReceptor;
  		        	doc.distritoReceptor = docto.CiudadReceptor;
  		        	doc.ubigeoReceptor = (docto.CiudadReceptor + " - " + docto.ComunaReceptor + " - " +  docto.ComunaReceptor);
  		        	doc.nacionalidadReceptor = docto.NacionalidadReceptor;
  		        	doc.emailReceptor = docto.EmailReceptor;
  		        	doc.productoOperacion = docto.ProductoOperacion + "";
  		        	if (docto.GeneraOperacionPlazo.equalsIgnoreCase("N")) {
  		        		doc.tipoOperacion = docto.AccionTipoOperacion;
  		        	}
  		        	else {
  		        		
  		        		if (docto.EsAPlazo.equalsIgnoreCase("N")) {
  	  		        		doc.tipoOperacion = docto.AccionTipoOperacion + " CONTADO";	
  	  		        	}
  	  		        	else {
  	  		        		doc.tipoOperacion = docto.AccionTipoOperacion + " PLAZO";
  	  		        	}
  		        	}
  		        	
  		        	doc.claveOrigen = docto.IdComprobante.toString();
  		        	doc.monedaTransaccion = docto.MonedaTransaccion;
  		        	if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
  		        		//RF, cantidad corresponde a los cortes
  		        		BigDecimal corteMinimo = docto.CorteMinimo;
  						if (corteMinimo == null || corteMinimo.compareTo(BigDecimal.ZERO) == 0) {
  							corteMinimo = BigDecimal.ONE;
  						}
  						doc.cantidadTotal = docto.CantidadTotal.divide(corteMinimo,20,BigDecimal.ROUND_HALF_DOWN).longValue();
  						doc.precioPromedio = docto.ImporteTotal.divide(docto.CantidadTotal,20,BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal(100));
  						
  		        	}
  		        	else if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") || docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("DV") || docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
  		        		doc.cantidadTotal = docto.CantidadTotal.longValue();
  		        		if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("DV") && docto.Tamano != null && docto.Tamano > 0) {
  		        			doc.precioPromedio = docto.ImporteTotal.divide(docto.CantidadTotal,20,BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(docto.Tamano),20,BigDecimal.ROUND_HALF_DOWN);
  		        		}
  		        		else {
  		        			doc.precioPromedio = docto.ImporteTotal.divide(docto.CantidadTotal,20,BigDecimal.ROUND_HALF_DOWN);	
  		        		}
  		        		
  		        		
  		        	}
  		        	else {
  		        		doc.cantidadTotal = 0L;
  		        		doc.precioPromedio = BigDecimal.ZERO;
  		        	}
  		        	
  		        	doc.importeTotal = docto.ImporteTotal;
  		        	doc.arancelBVL = docto.ComisionBVLMonedaLiquidacion;
  		        	doc.retribucionCAVALI = docto.ComisionCavaliMonedaLiquidacion;
  		        	doc.contribucionSMV = docto.ComisionSMVMonedaLiquidacion;
  		        	doc.fondoLiquidacion = docto.FondoLiquidacionMonedaLiquidacion;
  		        	doc.fondoGarantia = docto.FondoGarantiaMonedaLiquidacion;
  		        	doc.comisionSAB = docto.ComisionSABMonedaLiquidacion;
  		        	doc.otrasComisiones = docto.OtrasComisionesMonedaLiquidacion;
  		        	doc.monedaLiquidacion = docto.MonedaLiquidacion;
  		        	doc.importeLiquidacion = docto.ImporteLiquidacion;
  		        	doc.comisionLiquidacion = docto.ComisionBVLMonedaLiquidacion
  		        			.add(docto.ComisionCavaliMonedaLiquidacion)
  		        			.add(docto.ComisionSMVMonedaLiquidacion)
  		        			.add(docto.FondoLiquidacionMonedaLiquidacion)
  		        			.add(docto.FondoGarantiaMonedaLiquidacion)
  		        			.add(docto.ComisionSABMonedaLiquidacion)
  		        			.add(docto.OtrasComisionesMonedaLiquidacion);
  		        	doc.igvLiquidacion = docto.ImpuestoIGVMonedaLiquidacion;
  		        	  		        	
  		        	doc.vacLiquidacion = docto.ReajustesVAC;
  		        	
//		        	doc.observacion =  docto.Observacion;
  		        	
  		        	doc.interesLiquidacion = docto.Intereses;
  		        	doc.totalLiquidacion = docto.TotalLiquidacion.abs();
  		        	
  		        	doc.boveda = docto.NombreBovedaCuentaCustodioPortafolio;
  		        	doc.tipoCustodia = docto.NombreTipoCustodia;
  		        	doc.bancoCustodia = docto.NombreCompletoBancoCustodio;
  		        	doc.bancoLiquidador = docto.NombreCompletoBancoLiquidador;
  		        	doc.codigoBoveda = docto.CodigoDeCuenta;
  		        	doc.formaCustodia = docto.NombreFormaCustodia;
  		        	
  		        			  		        	
  		        	doc.fechaLiquidacion = Fecha.toStringDate(docto.FechaLiquidacion);
  		        	
  		        	fechaVcto = (docto.FechaVencimiento != null) ? Fecha.toStringDate(docto.FechaVencimiento) : null;  		        			
  		        	doc.fechaVencimiento = (fechaVcto); 
  		        	
  		        	doc.tasa = docto.Tasa;
  		        	doc.plazo = docto.DiasPlazo;
  		        	doc.tipoCambio  = docto.ValorTipoCambio;
  		        	doc.monedaImporte = docto.MonedaImporte;
  		        	doc.importeAPagar = docto.ImporteAPagar.abs();  		        	
  		        	doc.reemplazada = "";
  		        	doc.leyenda = ConvertirNumeroTexto.convert(docto.ImporteAPagar);
  		        	
  		        	doc.forzarFolio = true;
  		        	
  		        	//** Detalle Polizas **
  		        	ArrayList<DetallePolizaPE> listaDetalle= new ArrayList<DetallePolizaPE>();
  					
  	  			    for(int i=0; i<arrTval.size();i++) {
  	  			    	ObjDte2_0 doctoDet = (ObjDte2_0) arrTval.get(i);
  	  				
  		    			if (docto.IdComprobante.equals(doctoDet.IdComprobante)) {
  		    				DetallePolizaPE detalle = new DetallePolizaPE();
  							detalle.id = null;
  							if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) {
  								BigDecimal corteMinimo = arrTval.get(i).CorteMinimo;
  								if (corteMinimo == null || corteMinimo.compareTo(BigDecimal.ZERO) == 0) {
  									corteMinimo = BigDecimal.ONE;
  								}
  								detalle.cantidad = arrTval.get(i).IdComprobanteDetalleCantidad.divide(corteMinimo,20,BigDecimal.ROUND_HALF_DOWN);
  								detalle.valor = arrTval.get(i).CorteMinimo;
  				        	}
  				        	else if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")) {
  				        		detalle.cantidad = arrTval.get(i).IdComprobanteDetalleCantidad;
  				        		detalle.valor = BigDecimal.ONE;
  				        	}
  				        	else {
  				        		detalle.cantidad = arrTval.get(i).IdComprobanteDetalleCantidad;
  				        		detalle.valor = BigDecimal.ONE;
  				        	}
  							
  							if (docto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
  								detalle.descripcion = arrTval.get(i).IdComprobanteDetalleNemotecnicoSubyacente;
  							}
  							else {
  								detalle.descripcion = arrTval.get(i).IdComprobanteDetalleNemotecnico;	
  							}
  							
  							
  							detalle.precio = arrTval.get(i).IdComprobanteDetallePrecio;
  							
  							detalle.importe = arrTval.get(i).IdComprobanteDetalleMonto;
  							detalle.polizaEditable = null;
  									
  							listaDetalle.add(detalle);
  				    	
  		    			  }
  		    		  }
  	  			    
  	  			    doc.referenciaPolizas = new ArrayList<>();
  	  			    {
  	  			        ReferenciaPolizaPE refPol = new ReferenciaPolizaPE();
  	  			        refPol.tipo = "SEC";
  	  			        refPol.folio = docto.IdComprobante+"";
  	  			        refPol.fecha = Fecha.toStringDate(docto.FechaComprobante);
  	  			        refPol.razon = "Nro. Interno OPTIMUS";
  	  			    	doc.referenciaPolizas.add(refPol);
  	  			    }
  	  			    
  	  			    
  	  			    
  	  	    	    doc.parametropolizas = null;
  	  	    	    
  	  	    	    doc.detallePolizas = listaDetalle;
  		    		
  		    	}else 
  		    	     { //NO POL  		    		
  		    		    doc.id = null;
  		    		    switch (docto.IdTipoComprobante) {
		        	       case "FAE":
		        	       case "FOE":
		        	       case "FEE":
		        	       case "FAC":
		        	       case "FOX":
		        	       case "FOC":
		        	       case "NCR":
		        	       case "NDR":
		        	       case "NCE":
					       case "NEO":
					       case "NDE":	
						     doc.serie = "F001"; 
					         break;
 					       case "BOE":
                           case "BEE":
						     doc.serie = "B001";  
					         break;
					       case "POL":
						     doc.serie = "F001";  
					         break;
					       default:
						     doc.serie = "";
						     break;
		        	     }
  		    		
  		    		    doc.fecha = Fecha.toStringDate(docto.FechaComprobante);
  		    		    doc.tipo = docto.TipoComprobanteDTE;
  		    		    doc.rucEmisor  = docto.DocumentoIdentidadEmisor;
  		    		
  		    		    if (docto.TipoDocumentoIdentidadEmisor.equalsIgnoreCase("DNI"))
  		    		      {
  		        		     doc.tipoDocumentoEmisor = "1";
  		        	      }else if (docto.TipoDocumentoIdentidadEmisor.equalsIgnoreCase("CE")) 
  		        	               {
  		        		              doc.tipoDocumentoEmisor = "4";
  		        	               }else if (docto.TipoDocumentoIdentidadEmisor.equalsIgnoreCase("RUC"))
  		        	                        {
  		        		                       doc.tipoDocumentoEmisor = "6";
  		        	                        }else if (docto.TipoDocumentoIdentidadEmisor.equalsIgnoreCase("PAS")) 
  		        	                                 {
  		        		                                doc.tipoDocumentoEmisor = "7";
  		        	                                 }else if (docto.TipoDocumentoIdentidadEmisor.equalsIgnoreCase("CDI"))
  		        	                                          {
  		        		                                         doc.tipoDocumentoEmisor = "A";
  		        	                                          }else
  		        	                                              {
  		        	                                        	      doc.tipoDocumentoEmisor = "0";
  		        	                                        	  };
  		    		
  		    		    doc.razonSocialEmisor = docto.RazonSocialEmisor;
  		    		    doc.direccionEmisor = docto.DireccionEmisor;
  		    		    doc.nombreComercialEmisor = "";
  		    		    doc.urbanizacionEmisor = docto.DireccionEmisor;
  		    		    doc.provinciaEmisor = docto.ComunaEmisor;
  		    		    doc.departamentoEmisor = docto.ComunaEmisor;
  		    		    doc.distritoEmisor = docto.CiudadEmisor;
  		    		    doc.codigoUbigeoEmisor = "";
  		    		    doc.codigoPaisEmisor = "";
  		    		    doc.emailEmisor = docto.EmailEmisor;
  		    		    doc.telefonoEmisor = docto.TelefonoEmisor;
  		    		    doc.observacion = docto.Observacion;// CGO
  		    		    
  		    		    
  		    		    doc.rucReceptor = docto.DocumentoIdentidadAsociado;
  		    		
  		    		    if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("DNI"))
  		    		       {
  		        		      doc.tipoDocumentoReceptor = "1";
  		        	       }else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("CE"))
  		        	                {
  		        		               doc.tipoDocumentoReceptor = "4";
  		        	                }else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("RUC"))
  		        	                         {
  		        		                        doc.tipoDocumentoReceptor = "6";
  		        		                     }else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("PAS")) 
  		        		                             {
  		        		                                doc.tipoDocumentoReceptor = "7";
  		        		                             }else if (docto.TipoDocumentoIdentidadAsociado.equalsIgnoreCase("CDI"))
  		        		                                      {
  		        		                                         doc.tipoDocumentoReceptor = "A";
  		        		                                      }else {
  		        		                                               doc.tipoDocumentoReceptor = "0";
  		        	                                                }
  		    		  		    		
  		    		    doc.rutCavali = docto.DocumentoIdentidadAdicionalReceptor;
  		    		    doc.razonSocialReceptor = docto.RazonSocialReceptor;
  		    		    doc.direccionReceptor = docto.DireccionReceptor;
  		    		    doc.urbanizacionReceptor = docto.DireccionReceptor;
  		        	    doc.provinciaReceptor = docto.ComunaReceptor;
  		        	    doc.distritoReceptor = docto.CiudadReceptor;
  		        	    doc.codigoUbigeoReceptor = "";
  		        	    doc.codigoPaisReceptor = "";
  		        	    doc.emailReceptor = docto.EmailReceptor;
  		        	
  		        	    doc.codigoTipoOperacion = "01";
  		    		    doc.codigoTipoTributario = "";
  		    		    doc.nombreTipoTributario = "";
  		    		    doc.codigoInternacionalTributo = "VAI";
  		    		
  		    		    if (listTipoComprobante.contains(docto.IdTipoComprobante))
  		    		      {
  		    		         doc.codigoTipoDocumento = "09";
  		    		         doc.tipoDocumento = docto.IdComprobanteDetalleMonto.equals(docto.IdComprobanteDetalleMontoNota)? "ANULACIÓN DE LA OPERACIÓN":"DISMINUCIÓN EN EL VALOR";
  		    		      };   
  		    				
  		    		
  		    		    doc.moneda = docto.MonedaLiquidacion;
  		    		    doc.formaPago = "";
  		    		
  		    		    doc.subTotal = docto.IdComprobanteDetalleMonto.doubleValue();
  		    		    doc.anticipo = 0.0;
  		    		    doc.valor = docto.IdComprobanteDetalleMonto.doubleValue();
  		    		    doc.montoNetoGravada = docto.IdComprobanteDetalleMonto.doubleValue();
  		    		    doc.montoNetoInafecta = 0.0;
  		    		    doc.montoNetoExonerada = 0.0;
  		    		    doc.sumatoriaIgv = docto.ImpuestoIGVMonedaLiquidacion.doubleValue();
  		    		    doc.sumatoriaIsc = 0.0;
  		        	    doc.sumatoriaOtrosTributos = 0.0;
  		        	    doc.sumatoriaOtrosCargos = 0.0;
  		        	    doc.tipoDescuentoTotal = "2005";
  		        	    doc.descuentoTotal = 0.0;
  		        	    doc.importeTotal = docto.ImporteAPagar;
  		        	
  		        	    //Extrae decimales de importa a pagar para mover a leyenda
  		        	    BigDecimal b = (docto.ImporteAPagar);	  		          
  		                DecimalFormat df = new DecimalFormat();
  		            
  		                df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
  		                df.setMaximumFractionDigits(2);
  		                df.setMinimumFractionDigits(2);
  		                String text = df.format(b);
  		                int index = text.indexOf(".");
  		                String decimales = text.substring(index+1,index+3);
  		             		            
  		        	    doc.leyenda = "Son: " + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, ConvertirNumeroTexto.convert(docto.ImporteAPagar)) + "con " + decimales + "/100 " + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, docto.NombreMonedaImporte);
  		        	    //  		        	
  		        	
  		        	   doc.claveOrigen = docto.IdComprobante.toString();
  		        	   doc.estadoSunat = "";
  		        	   doc.forzarFolio = false;
  		        	
  		        	   doc.referenciaDocumentos = new ArrayList<>();  		        	
  		        	    {
	  		        	    ReferenciaDocumentoPE refSec = new ReferenciaDocumentoPE();
	  		        	    refSec.tipo = "SEC";	  		        	
	  		        	
	  		        	    switch (docto.IdTipoReferenciaNota){
		        	           case "FAE":
		        	           case "FOE":
		        	           case "FEE":
		        	           case "FAC":
		        	           case "FOX":
		        	           case "FOC":
		        	           case "NCR":
		        	           case "NDR":
		        	           case "NCE":
					           case "NEO":
					           case "NDE":	
					    	       refSec.serie = "F001"; 
					               break;
					           case "BOE":
                               case "BEE":
					    	       refSec.serie = "B001";  
					               break;
					           case "POL":
					    	      refSec.serie = "F001";  
					              break;				       
					           default:
					    	      refSec.serie = "";
						          break;
		              	     }
	  		        		  		        	
	  		        	
	  		        	    refSec.folio = docto.IdComprobante+"";
	  		        	    refSec.fecha = Fecha.toStringDate(docto.FechaComprobante);
	  		        	    refSec.motivo = "Nro Interno OPTIMUS";
	  		        	    doc.referenciaDocumentos.add(refSec);
  		        	    }
  		        	
  		        	    {
  		        		   if (listTipoComprobante.contains(docto.IdTipoComprobante))  		        		
  		        		      {
  		        		         ReferenciaDocumentoPE refnc = new ReferenciaDocumentoPE();
	
	  		        	         refnc.tipo = docto.TipoReferenciaNotaPE;
	  		        	         refnc.codigoTipo = docto.CodigoTipoReferenciaNotaPE;

	  		        	         switch (docto.IdTipoReferenciaNota) {
	  		        	             case "FAE":
	  		        	             case "FOE":
	  		        	             case "FEE":
	  		        	             case "FAC":
	  		        	             case "FOX":
	  		        	             case "FOC":
	  		        	             case "NCR":
	  		        	             case "NDR":
	  		        	             case "NCE":
	  		        	             case "NEO":
	  		        	             case "NDE":	
	  		        	        	     refnc.serie = "F001"; 
	  		        	        	     break;
	  		        	             case "BOE":
                                     case "BEE":
	  		        	        	     refnc.serie = "B001";  
	  		        	        	     break;
	  		        	             case "POL":
	  		        	        	     refnc.serie = "F001";  
	  		        	        	     break;
	  		        	             default:
						 	             refnc.serie = "";
							             break;
  		        	               }
  		        	
	  		        	         refnc.folio = docto.FolioReferenciaNota+"";
	  		        	         refnc.fecha = (docto.FechaReferenciaNota != null )? Fecha.toStringDate(docto.FechaReferenciaNota) : "0001-01-01";
	  		        	         refnc.codigoMotivo = "09";
	  		        	         refnc.motivo = docto.IdComprobanteDetalleMonto.equals(docto.IdComprobanteDetalleMontoNota)? "ANULACIÓN" + " " + docto.TipoComprobanteNota:"CORRIGE MONTO"; 
	  		                			
	  		        	         doc.referenciaDocumentos.add(refnc);
  		        		      }	
  		        	    }
  		        	
  		        	   doc.detalleDocumentos = new ArrayList<>();
  		        	   for(int i=0; i<arrTval.size();i++) {
  	  			    	  ObjDte2_0 doctoDet = (ObjDte2_0) arrTval.get(i);
  	  			    	
  	  			    	  if (docto.IdComprobante.equals(doctoDet.IdComprobante))
  	  			    	    {
  	  			    		   DetalleDocumentoPE det = new DetalleDocumentoPE();
	  	  			    	
	  	  			    	   if (doctoDet.IdComprobanteDetalleNemotecnico == null) 
	  	  			    	     {
	  	  			    		   if (listTipoComprobante.contains(docto.IdTipoComprobante))
	  	  			    		     {
	  	  			    		       det.codigoProducto = doctoDet.IdConceptoNota;
  	  			    		           det.descripcion = docto.NombreConceptoNota + "-" + docto.LeyendaNota;
	  	  			    		     }else{
	  	  			    			         det.codigoProducto = doctoDet.IdConcepto;
	  	  			    		             det.descripcion = docto.NombreConcepto + "-" + docto.Leyenda;  
	  	  			    		          }
	  	  			    	     }else {
	  	  			    		          det.codigoProducto = doctoDet.IdComprobanteDetalleNemotecnico;
	  	  			    		          det.descripcion = docto.ProductoOperacion+"";
	  	  			    	           }
			        		
			        	       det.unidadMedida = "NIU";
			        		
			        	       if (doctoDet.IdComprobanteDetalleCantidad.doubleValue() == 0) 
			        	         {
			        			    det.cantidad = BigDecimal.ONE;
			        		     }else {
			        		    	      det.cantidad = doctoDet.IdComprobanteDetalleCantidad;
			        		    	   }
			                   det.precio = BigDecimal.ONE;
			        	       det.valor = doctoDet.IdComprobanteDetallePrecio;
			        	       det.monto = doctoDet.IdComprobanteDetalleMonto;
			        	       det.montoIgv = BigDecimal.ZERO;
			        	       det.codigoTipoIgv = "10";
			        	       det.montoIsc = BigDecimal.ZERO;
			        	       det.codigoTipoIsc = "";
			        	       doc.detalleDocumentos.add(det);
  	  			    	    }
  	  			    	}
  		    	     }

  	    	    //** Asigna comprobante de la linea que se esta recorriendo    	    	
  	    	    documentos.add(docto.IdComprobante);
  	    	    GsonBuilder gsonBuilder = new GsonBuilder();
	  	        gsonBuilder.registerTypeAdapter(BigDecimal.class,  new JsonSerializer<BigDecimal>() {
	  	            @Override
					public JsonElement serialize(BigDecimal arg0, Type arg1, JsonSerializationContext arg2) {
						
	  	                return new JsonPrimitive(arg0.setScale(2,RoundingMode.HALF_UP));
					}
	  	        });
	  	        gsonBuilder.registerTypeAdapter(Long.class,  new JsonSerializer<Long>() {
	  	            @Override
					public JsonElement serialize(Long arg0, Type arg1, JsonSerializationContext arg2) {
						
	  	                return new JsonPrimitive(arg0);
					}
	  	        });
	    		Gson gson = gsonBuilder.create();
	    		String jsonInString = gson.toJson(doc);
	        	logger.debug(jsonInString);
	        	salida.put(docto.IdComprobante.toString(), jsonInString);
  		      }
  	      }
  	    return salida;
      }	
    
      
        private void updateEnvTareaDteCmbte2_0(Connection con,ObjDte2_0 obj, String respuesta, String dirresp, String urlPDF, String urlXML) throws SQLException {
    	String queryUpd ="UPDATE \"BSCL.Facturacion.BD::FAC.TareaDTE\" SET ";                                  
		queryUpd+=" \"IdEstadoTareaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoDTE\" = ? ,";
		queryUpd+=" \"IdEstadoSII\" = ? ,";
		queryUpd+=" \"IdRespuestaDTE\" = ? ,";
	  	queryUpd+=" \"RespuestaLocationDTE\" = ? ,";
	  	queryUpd+=" \"RespuestaPdfDTE\" = ? ,";
	  	queryUpd+=" \"RespuestaXmlDTE\" = ? ";
  	
	  	queryUpd+=" WHERE \"IdTareaDTE\"= ?  AND \"IdEstadoTareaDTE\"= 'PEN'";
	  	logger.debug(queryUpd);
	  	try(PreparedStatement st = con.prepareStatement(queryUpd);) {
			st.setString(1, "ENV");
			st.setString(2, "REC");
			st.setString(3, "PEN");
			st.setObject(4, respuesta);
			st.setString(5, dirresp);
			st.setString(6, urlPDF);
			st.setString(7, urlXML);
			
	      	st.setLong(8,obj.IdTareaDTE);
	      	st.execute();
        }//try st
      
      
	  	String queryUpdCmbte ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";                                  
		queryUpdCmbte+=" \"IdEstadoDTE\" = ? ,";
		queryUpdCmbte+=" \"IdEstadoSII\" = ? ,";
		queryUpdCmbte+=" \"IdRespuestaDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaLocationDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaPdfDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaXmlDTE\" = ? ";
		
		queryUpdCmbte+=" WHERE \"IdComprobante\"= ?";
		logger.debug(queryUpdCmbte);
		try(PreparedStatement stCmbte = con.prepareStatement(queryUpdCmbte);) {
			stCmbte.setString(1, "REC");
			stCmbte.setString(2, "PEN");
			stCmbte.setString(3, respuesta);
			stCmbte.setString(4, dirresp);
			stCmbte.setString(5, urlPDF);
			stCmbte.setString(6, urlXML);
			
			stCmbte.setObject(7,obj.IdComprobante);
			stCmbte.execute();
		}
      
		obj.IdEstadoTareaDTE = "ENV";
		obj.IdEstadoDTE = "REC";
		obj.IdEstadoSII = "PEN";
		obj.IdRespuestaDTE = Long.parseLong(respuesta);
		obj.RespuestaLocationDTE = dirresp;
		obj.RespuestaPdfDTE = urlPDF;
		obj.RespuestaXmlDTE = urlXML;
    }
    
    
      //*** Prepara Json Consultar DTE
  	public Map<String,String> prepararJsonAConsultar2_0(List<ObjDte2_0> arrTval)
    {    	
		ArrayList<Integer> documentos = new ArrayList<Integer>();
	    Map<String,String> salida = new HashMap<String, String>();
	
	    for(int j=0; j<arrTval.size();j++)
	      {
	    	ObjDte2_0 docto = (ObjDte2_0) arrTval.get(j);    		
		    if (!documentos.contains(docto.IdComprobante))
		      {		    	
	        	salida.put(docto.IdComprobante.toString(), docto.RespuestaLocationDTE);
		      }
	      }
	    return salida;
    }
    
    private void updateConsTareaDte2_0(Connection con,ObjDte2_0 objCons, Object ObjResp, String folio, String estadoSII) throws SQLException 
    {	
	  	//Reemplaza estadosii segun valor que se obtiene de dte
	  	String valor = "";
	  	String valorEst = "";
	  	
	  	if (estadoSII.equalsIgnoreCase("OK")) {	
	  		valor = "ACP";
	  	    valorEst =  "ENV";
	  	}   
	  	else {	 
	  		valor = "ERR";
	  	    valorEst =  "PEN";
	  	    folio = "0";
	  	}  
	  	String queryUpd ="UPDATE \"BSCL.Facturacion.BD::FAC.TareaDTE\" SET ";        
		queryUpd+=" \"FolioComprobanteRespuestaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoTareaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoSII\" = ? ,";
  	    queryUpd+=" \"RespuestaDTEEnvioSII\" = ? ";
        queryUpd+=" WHERE \"IdTareaDTE\"= ?  AND \"IdEstadoTareaDTE\" <> 'ACP'";
      
        logger.debug(queryUpd);
        try(PreparedStatement st = con.prepareStatement(queryUpd);) {
	      	st.setObject(1, folio);
	      	st.setString(2, valorEst);
	      	st.setString(3, valor);
	      	
	      	logger.debug(ObjResp.toString()); //Registra log
      	
	      	if (ObjResp.toString().length() > 5000) {
	      	  st.setString(4, ObjResp.toString().substring(0, 5000));
	      	}
	      	else {
	      	  st.setString(4, ObjResp.toString());
	      	}

	      	st.setLong(5,objCons.IdTareaDTE);
	      	st.execute();
        }//try st
      
        
        String queryUpdCmbte ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";
	    queryUpdCmbte+=" \"FolioComprobante\" = ? ,";
		queryUpdCmbte+=" \"IdEstadoSII\" = ? ,";
		queryUpdCmbte+=" \"RespuestaDTEEnvioSII\" = ? ";
	    queryUpdCmbte+=" WHERE \"IdComprobante\"= ?";
      
	    logger.debug(queryUpdCmbte);
	    try(PreparedStatement stCmbte = con.prepareStatement(queryUpdCmbte);) {
	      	stCmbte.setString(1, folio);
	      	stCmbte.setString(2, valor);
	      	
	      	if (ObjResp.toString().length() > 5000) {
	      		stCmbte.setString(3, ObjResp.toString().substring(5000));
	      	}
	        else {
	        	stCmbte.setString(3, ObjResp.toString());
	        }
	      	
	      	stCmbte.setObject(4,objCons.IdComprobante);
	      	stCmbte.execute();
        }
      
	    if (objCons.CodigoReferenciaNota > 0) {
	    
	      	String queryUpdCmbteRef ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";
	      	queryUpdCmbteRef+=" \"FolioComprobanteReferencia\" = ? ";
	      	queryUpdCmbteRef+=" WHERE \"IdComprobanteReferencia\"= ?";
	      	logger.debug(queryUpdCmbteRef);
	      	
	      	try(PreparedStatement stCmbteRef = con.prepareStatement(queryUpdCmbteRef);) { 
	      		stCmbteRef.setString(1, folio);
	      		stCmbteRef.setObject(2,objCons.CodigoReferenciaNota);
	      		stCmbteRef.execute();
	      	}
	    }
      
    }
    
    
    private void updateEnvTareaDteLibro2_0(Connection con,ObjDte2_0 obj, String respuesta, String dirresp, String urlPDF, String urlXML) throws SQLException 
    {	
      String queryUpd ="UPDATE \"BSCL.Facturacion.BD::FAC.TareaDTE\" SET ";                                  
		queryUpd+=" \"IdEstadoTareaDTE\" = ? ,";
		queryUpd+=" \"IdEstadoDTE\" = ? ,";
		queryUpd+=" \"IdEstadoSII\" = ? ,";
		queryUpd+=" \"IdRespuestaDTE\" = ? ,";
		queryUpd+=" \"RespuestaLocationDTE\" = ? ,";
		queryUpd+=" \"RespuestaPdfDTE\" = ? ,";
		queryUpd+=" \"RespuestaXmlDTE\" = ? ";
  	
      queryUpd+=" WHERE \"IdTareaDTE\"= ?  AND \"IdEstadoTareaDTE\"= 'PEN'";
      
      try(PreparedStatement st = con.prepareStatement(queryUpd);) 
        {
			st.setString(1, "ENV");
			st.setString(2, "REC");
			st.setString(3, "PEN");
			st.setObject(4, respuesta);
			st.setString(5, dirresp);
			st.setString(6, urlPDF);
			st.setString(7, urlXML);
			
      	//where:
      	st.setLong(8,obj.IdTareaDTE);
      	st.execute();
      	//con.commit();
        }//try st
      
      
      String queryUpdCmbte ="UPDATE \"BSCL.Facturacion.BD::FAC.Comprobante\" SET ";                                  
		queryUpdCmbte+=" \"IdEstadoDTE\" = ? ,";
		queryUpdCmbte+=" \"IdEstadoSII\" = ? ,";
		queryUpdCmbte+=" \"IdRespuestaDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaLocationDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaPdfDTE\" = ? ,";
		queryUpdCmbte+=" \"RespuestaXmlDTE\" = ? ";
		
      queryUpdCmbte+=" WHERE \"IdComprobante\"= ?";
      
      try(PreparedStatement stCmbte = con.prepareStatement(queryUpdCmbte);) 
        {
      	stCmbte.setString(1, "REC");
      	stCmbte.setString(2, "PEN");
      	stCmbte.setString(3, respuesta);
      	stCmbte.setString(4, dirresp);
      	stCmbte.setString(5, urlPDF);
      	stCmbte.setString(6, urlXML);
      	
      	//where:
      	stCmbte.setObject(7,obj.IdComprobante);
      	stCmbte.execute();
      	//con.commit();
        }
      
      logger.debug(queryUpd);
      logger.debug(queryUpdCmbte);
    }
    
	
	// ------------------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ------------------------------------------------------------------------------------------------------	
	public static void main(String[] args) throws InterruptedException, Throwable {
		System.out.println("== TEST ==");
		
		Long IdTareaDteGrupo = 0L;
	
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			try {
				SendDte demo = new SendDte();
				demo.procesar(con, IdTareaDteGrupo);
				//con.commit();
				con.rollback();
				System.out.println("SALIDA");
			} catch (Throwable er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
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
	
	// ------------------------------------------------------------------------------------------------------
	// XXX
	// ------------------------------------------------------------------------------------------------------	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


		
}
