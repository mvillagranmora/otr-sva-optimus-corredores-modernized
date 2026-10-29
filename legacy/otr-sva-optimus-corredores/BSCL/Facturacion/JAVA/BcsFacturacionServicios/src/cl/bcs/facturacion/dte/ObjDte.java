package cl.bcs.facturacion.dte;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.utils.Utils;
 
public class ObjDte {

	
//	TIPO VAL			NOMBRE					in|out	- Definicion		
//-------------------------------------------------------------------------------------------------------	
	public Long 		idtareadte;				            // E|S	- Id Interno de Tarea DTE
	public Long			idtareadtegrupo;		            // E|S 	- Id agrupador de Tarea DTE, se inicializa en cada nuevo envio
	public String		tipotareadte;	    	            // E|S 	- Indica el tipo de tarea que corresponde el registro
	public Long			idrespuestadte;	    	            // E|S 	- Indica el id que respondio dte al recibir la factura
	public String		respuestadteenviosii; 	            // E|S 	- Indica la recepcion del dte de la factura	despues de ser enviado al sii
	public String       respuestalocationdte;               // E|S 	- Indica el location de la recepcion del dte de la factura 
	public String       respuestapdfdte;                    // E|S 	- Indica la direccion donde se encuentra pdf
	public String       respuestaxmldte;                    // E|S 	- Indica la direccion donde se encuentra xml
	public Long      	foliocomprobanterespuestadte;       // E|S 	- Indica el folio comprobante que asigno sii
	
	
	public Long			idcomprobante;		                // E|S 	- 
	public String		idestadotareadte;		            // E|S 	- Estado de tarea envio dte
	public String		idestadodte;	    	            // E|S 	- Estado de dte
	public String		idestadosii;	    	            // E|S 	- Estado de sii	
	public String		idtipocomprobante;		            // E|S 	- Tipo de comprobante que se esta enviando
	public Long      	tipocomprobantedte;                 // E|S  - Tipo de comprobante codificado por el SII
	public String       acciontipooperacion;                // E|S  - Tipo de operacion de comprobante
	public String       periodocontable;                    // E|S  - Indica periodo contable a procesar
	public java.sql.Date  		fechacomprobante;			// E|S 	- Fecha de comprobante que se esta enviando
	public java.sql.Date     	fechavencimiento;			// E|S 	- Fecha de comprobante que es usado como fecha vencimiento
	public String     	codigovendedor;  		            // E|S 	-
	public String     	codigoreceptor;  		            // E|S 	-
	
	
	public String       glosaajustecomision;                // E|S 	-
	public BigDecimal   totalcomisioncmbte;                 // E|S  - Total de comision por comprobante
	public String       glosaajustederecho;                 // E|S 	-
	public BigDecimal   totalderechocmbte;                  // E|S  - Total de derecho por comprobante
	public String       glosaajustegasto;                   // E|S 	-
	public BigDecimal   totalgastocmbte;                    // E|S  - Total de gasto por comprobante
	public String       glosaajusteretencion;               // E|S 	-
	public BigDecimal   totalretencioncmbte;                // E|S  - Total de retencion por comprobante
	public BigDecimal   idcomprobantemontonofacturable;     // E|S 	- Monto de comprobante sin cobros
	



	
	public BigDecimal   montoNeto;                          // E|S 	- Monto de cobros de comprobante 
	public BigDecimal   idcomprobantemontoexento;           // E|S 	- Monto de comprobante de facturas exentas
	public BigDecimal   idcomprobantetasaiva;               // E|S 	- Tasa de cobro iva a aplicar
	public BigDecimal   montoIva;                           // E|S 	- Monto de iva sobre monto cobros de comprobante
	public BigDecimal 	idcomprobantemontototal;            // E|S 	- Monto neto + monto iva 
	public BigDecimal   idcomprobantevalorapagar;	        // E|S 	- Monto no facturable + monto total
	public BigDecimal   totalidtipocomprobantemontoneto;    // E|S  - Total Monto de cobros de comprobante - Envio de Libros 
	public BigDecimal   totalidtipocomprobantemontoexento;  // E|S  - Total Monto de comprobante de facturas exentas - Envio de Libros 
	public BigDecimal   totalidtipocomprobantemontoiva;     // E|S  - Total Monto de iva sobre monto cobros de comprobante - Envio de Libros
	public BigDecimal   totalidtipocomprobantemontototal;   // E|S  - Total Monto neto + monto iva - Envio de Libros 	
	public Integer     	totalidtipocomprobantecantidaddocumentos;  // E|S 	- Indica cantidad de documentos a enviar / para envio libros
	public Integer      totalidtipocomprobantecantidadanulados;    // E|S 	- Indica cantidad de documentos anulados a enviar / para envio libros	
	public String 	    documentoidentidademisor;	        // E|S 	- Documento identidad de emisor(Corredora)
	public String   	razonsocialemisor;		            // E|S 	- Razon social de emisor(Corredora)
	
	
	
	public String   	giroemisor;  			            // E|S 	- Giro de emisor(Corredora)
	public String	    direccionemisor;		            // E|S 	- Direccion fisica de emisor(Corredora)
	public String	    comunaemisor;	  	                // E|S 	- Comuna de direccion fisica de emisor(Corredora)
	public String	    ciudademisor;			            // E|S 	- Ciudad de direccion fisica de emisor(Corredora)
	public String	    emailemisor;			            // E|S 	- Direccion electronica de emisor(Corredora)
	public String	    condicionpago;			            // E|S 	- Codigo condicion de liquidación de comprobante
	public String   	telefonoemisor;			            // E|S 	- Telefono fijo de emisor(Corredora)	
	public String		emailreceptor;			            // E|S 	- Direccion electronica de receptor(Cliente)
	public String		documentoidentidadreceptor;         // E|S 	- Documento identidad de receptor(Cliente)
	public String       codigoSucursal;                     // E|S 	- Codigo Portafolio
	public String		razonsocialreceptor;                // E|S 	- Razon social de receptor(Cliente)
	public String		giroreceptor;                       // E|S 	- Giro de receptor(Cliente)
	public String		direccionreceptor;                  // E|S 	- Direccion fisica de receptor(Cliente)
	public String		comunareceptor;                     // E|S 	- Comuna de direccion fisica de receptor(Cliente)
	public String		ciudadreceptor;                     // E|S 	- Ciudad de direccion fisica de receptor(Cliente)
	public String		contactoreceptor;                   // E|S 	- Contacto de direccion fisica de receptor(Cliente)
	public String       indBolsa;                           // E|S 	- Bolsa relacionada a comprobante 
	public String       indMensaje_1;                       // E|S 	- Mensaje para facturas COMMEN
	
	//Referencia
	public String	    tiporeferenciaidComprobante;        // E|S 	- Referencia DTE de control de campo, en este caso es interno(SAP)
	public Long	    	folioreferenciaidcomprobante;       // E|S 	- IdComprobante que es en este caso es interno (IdComprobante)
	public String 	    fechareferenciaidComprobante;       // E|S 	- 
	public String	    razonreferenciaidComprobante;       // E|S 	- Indica el uso de estos campos para DTE, en este caso es interno(Nro. Interno SAP)
	public String	    tiporeferenciafoliocomprobante;     // E|S 	- Referencia DTE de control de campo, en este caso es el que se ve en facturacion(VIS)
	public Long	    	folioreferenciafoliocomprobante;    // E|S 	- FolioComprobante que es en este caso es visual (FolioComprobante)
	public String 	    fechareferenciafoliocomprobante;    // E|S 	- 
	public String	    razonreferenciafoliocomprobante;    // E|S 	- Indica el uso de estos campos para DTE, en este caso es visual(Nro. Visible SAP)
	

	//Referencia para mov. otros conceptos
	public String		tipoReferenciaOtrosConceptos;
	public String		folioReferenciaOtrosConceptos;
    public java.sql.Date		fechaReferenciaOtrosConceptos;
    public String		razonReferenciaOtrosConceptos;
	
	
	   //Nota de Debito - Credito
	public Long         numeroreferencianota;               //
	public String	    tiporeferencianota;                 // E|S 	- Referencia DTE de control de campo, referenciado con las notas
	public String	    tiporeferencianotape;               // E|S 	- Referencia DTE-PE de control de campo, referenciado con las notas
	public String	    idtiporeferencianota;               // E|S 	- Referencia de control de campo, referenciado con las notas	
	public Long	    	folioreferencianota;                // E|S 	- IdComprobante referenciado notas
	public java.sql.Date 	    fechareferencianota;    // E|S 	- 
	public String	    razonreferencianota;                // E|S 	- Indica glosa de notas
	public Long	    	codigoreferencianota;               // E|S 	- Indica comprobante referencia de notas
	

	
	//Detalle
	public String	    idcomprobantedetalleglosa;          // E|S 	- 
	public String	    idComprobantedetalledescripcion;    // E|S 	-
	public String	    idComprobanteDetalleBoletaEditable;   // E|S -
	
	public BigDecimal   idcomprobantedetallecantidad;       // E|S 	- Indica la cantidad de la operacion a nivel de linea (ComprobanteDetalle)
	public BigDecimal   idcomprobantedetalleprecio;         // E|S 	- Indica el precio de la operacion a nivel de linea (ComprobanteDetalle)
	public BigDecimal   idcomprobantedetalleporcentaje;     // E|S 	- Indica el porcentaje de la operacion a nivel de linea (ComprobanteDetalle)
	public BigDecimal   idcomprobantedetallemontodescuento; // E|S 	- Indica el monto descuento de la operacion a nivel de linea (ComprobanteDetalle)
	public BigDecimal   idcomprobantedetallemonto;          // E|S 	- Indica el monto de la operacion a nivel de linea (ComprobanteDetalle)
	public BigDecimal   precioOtraMoneda;                   // E|S  - Valor captación
	public BigDecimal   precioOtraMonedaSecundaria;         // E|S  - Valor captación = precio * cantidad        
	public BigDecimal   factorConversion;                   // E|S  - Valor paridad
	public BigDecimal   factorConversionSecundario;         // E|S  - Valor paridad moneda Origen a Moneda Sistema  
	public String       moneda;
	public String       monedaSecundaria;                   // E|S  - IdMoneda Origen
	public String       montoLiquidacionFinal;
	public Long         idcomprobantedetalleesexento;       // E|S 	- Indica si es exento a nivel de linea (ComprobanteDetalle)
	//Parametro
	public String       parametrokeyproducto;               // E|S  - 
	
	//Peru
	public String       	tipocomprobantedtepe;
	public String       	monedatransaccionpe;
	public BigDecimal       cantidadtotalpe;
	public String       	monedaliquidacionpe;
	public java.sql.Date    fechaliquidacionpe;
	public String       	acciontipooperacioncmbtepe;
	public String       	nacionalidadreceptorpe;
	public String       	productooperacionpe;
	public BigDecimal       preciopromediope;
	public BigDecimal       importetotalpe;
	public BigDecimal       importeliquidacionpe;
	public BigDecimal       totalliquidacionpe;
	public String       	monedaimportepe;
	public BigDecimal       importeapagarpe;
	
	// JPMorgan
	public String 		nombreInstrumento;
	public String 		valorPrecioPromedio;
	public String 		cantidadTotal;
	

	public String 			actualizarPorCampo;
	public String 			actualizarPorCampo2;

	public String		grupoDireccionesElectronicas;	
	
	//CELBUHOPRO
	public String idTipoInstrumento;
	public String codigoMonedaBancoCentral;
	public String descripcionMonedaBancoCentral;
	public String codigoNacionalidadReceptor;
	public String nemoMoneda;
	
	//CELHUEMUL-3446
	public String idConcepto;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

		
	//Constructor
	public  ObjDte(){
	}
	
	
	public String toDisplayableString(){
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		result.append( this.getClass().getName() );
		result.append( " Object {" );
		result.append(newLine);
		Field[] fields = this.getClass().getDeclaredFields();
		for ( Field field : fields  ) {
			result.append("  ");
		try {
			result.append( field.getName());
			result.append(": ");
				result.append( field.get(this));
			} catch (IllegalAccessException ex) {
				System.out.println(ex);
		    }
		    result.append(newLine);
		}
		result.append("}");
		return result.toString();
	}
}
