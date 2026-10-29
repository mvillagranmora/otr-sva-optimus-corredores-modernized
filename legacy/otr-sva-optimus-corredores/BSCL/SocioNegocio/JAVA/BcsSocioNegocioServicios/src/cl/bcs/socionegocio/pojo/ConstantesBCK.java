package cl.bcs.socionegocio.pojo;

public class ConstantesBCK {
	
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_LOGIN               			= "BLOCKCHAIN.LOGIN.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_DATA_CORREDORA		= "BLOCKCHAIN.CONSULTADATACORREDORA.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_PRESTAMOSBYID				= "BLOCKCHAIN.PRESTAMOSBYID.URL"; 
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_ANTICIPO_PRESTAMO_TOTAL		= "BLOCKCHAIN.ANTICIPOPRESTAMOTOTAL.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_ANTICIPO_PRESTAMO_PARCIAL	= "BLOCKCHAIN.ANTICIPOPRESTAMOPARCIAL.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_INGRESO_PRESTAMISTA			= "BLOCKCHAIN.ASIGNAPRESTAMISTA.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_INGRESO_PRESTAMO            	= "BLOCKCHAIN.INGRESOPRESTAMOS.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_PRESTAMO   			= "BLOCKCHAIN.CONSULTAANTICIPOPRESTAMO.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_ESTADO_PRESTAMO   	= "BLOCKCHAIN.CONSULTAESTADOPRESTAMO.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_CONTRATO   			= "BLOCKCHAIN.CONSULTACONTRATO.URL";	
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_LOG					= "BLOCKCHAIN.CONSULTALOG.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_FIRMACNT 		    		= "BLOCKCHAIN.FIRMACONTRATO.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_INGRESO_OTROS_PRESTAMO		= "BLOCKCHAIN.INGRESOOTROSPRESTAMOS.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_MODIFICAR_PRESTAMO  			= "BLOCKCHAIN.MODIFICARPRESTAMO.URL";
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_RENOVAR_PRESTAMO        		= "BLOCKCHAIN.RENOVARPRESTAMOS.URL";
	public static final String NOMBRE_PARAMETRO_BLOCKCHAIN_SECUENCIA						= "BLOCKCHAIN.BLOQUE.SECUENCIA"; 
	public static final String NOMBRE_PARAMETRO_TIMEOUT_ESERROR_BLOCKCHAIN	        		= "BLOCKCHAIN.TIMEOUT.ESERROR";		
	public static final String NOMBRE_PARAMETRO_URL_BLOCKCHAIN_CONSULTA_OTROS_PRESTAMOS     = "BLOCKCHAIN.CONSULTAOTROSPRESTAMOS.URL";
	
	public static final String ESTADO_APROBADO_CONTRATO_BLOCKCHAIN			        		= "APPROVED"; 
	public static final String ESTADO_RECHAZADO_CONTRATO_BLOCKCHAIN			    			= "REJECTED";
	public static final String ESTADO_PENDIENTE_CONTRATO_BLOCKCHAIN			    			= "PENDING";
	public static final String ESTADO_SIN_CONTRATO_BLOCKCHAIN					    		= "NOFOUND";
	
	public static final String ID_TIPO_RELACION_ARBITRADOR1					    			= "AR1";
	public static final String ID_TIPO_RELACION_ARBITRADOR2					    			= "AR2";
	
	public static final String CONTRATOPRESTAMOANEXO42									    = "CONTRATOVENTACORTAANEXO42";
	public static final String IDCONTRATOPRESTAMOANEXO42									= "42";
	
	public static final String CONTRATOPRESTAMOANEXO43									    = "CONTRATOPRESTAMOANEXO43";
	public static final String IDCONTRATOPRESTAMOANEXO43									= "43";
	
	public static final String CONTRATOPRESTAMOANEXO48                             			= "CONTRATOPRESTAMOANEXO48";
	public static final String IDCONTRATOPRESTAMOANEXO48								    = "48";
	
	public static final String CONDICION_DE_LIQUIDACION_PH				            		= "PH";
	public static final String CONDICION_DE_LIQUIDACION_T0				            		= "T0";
	public static final String TIPO_MODIF_PRESTAMO		  			                		= "MODIFYLENDINGAGREEMENT";
	public static final String ANULAR_APTMO			            		                	= "APTMO"; 	//ANULA PRESTAMO
	public static final String ANULAR_APTMTA			            		                = "APTMTA"; //ANULA PRESTAMISTA
	public static final String ANULAR_MPTMO			            		                	= "MPTMO"; 	//MODIFICA DATOS PRESTAMO	

	public static final String ACTION_MODIFY												= "MODIFYLENDINGAGREEMENT";
	public static final String ACTION_REQUESTEXTENSION										= "REQUESTEXTENSION";
	public static final String ACCTION_ADDLENDINGCLIENTE									= "ADDLENDINGCLIENT";
	public static final String ACCTION_CREATE												= "CREATE";
	public static final String ACCTION_REQUESTTERMINATION									= "REQUESTTERMINATION";
	public static final String ACCTION_REQUESTPARTIALTERMINATION							= "REQUESTPARTIALTERMINATION";
		
	public static final String SI = "S";
	public static final String NO = "N";

	public static final String ID_BOLSA_POR_DEFECTO				            				= "BCS";
	public static final String MERCADO_PRESTAMOS_BLOCKCHAIN				           			= "RV";
	public static final String MERCADO_PRESTAMOS_BLOCKCHAIN_RENTA_FIJA				        = "RF";
	public static final String MONEDA				           								= "CLP";
	public static final String ESTADO_VENCIMIENTO				           					= "VEN";
	
	public static final String PRESTAMO_TIPO_VENTA_CORTA									= "PRTVTC";
	public static final String PRESTAMO_TIPO_VENTA_CORTA_RENTA_FIJA							= "PRVCRF";
	
	public static final String ACUERDO_TIPO_TRANSACCIONAL									= "T";
}
