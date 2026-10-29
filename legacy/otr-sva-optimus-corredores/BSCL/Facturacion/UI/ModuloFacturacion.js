angular.module('ModuloFacturacion', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);


//Configuración llamada a Servicios 
angular.module('ModuloFacturacion').factory('bcsServiciosFacturacion',['$resource',function($resource) {
	  var urlCrearMovimientoComprobante 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1506";
	  var urlCrearComprobante 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=13";	  
	  var urlModificarEstadoMovimiento 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=16";
	  var urlAnularMovimiento 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=145";
	  var urlAnularComprobante 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=149";
	  var urlCrearAutorizacionAnularComprobante			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=8";
	  var urlModificarMovimientoComprobante 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=233";
	  //var urlActualizarMontosCobro 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=201";
	  var urlAjusteSencilloComprobante					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=60045";
	  var urlActualizarMontosCobro 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3155";
	  var urlCrearComprobanteNotaCredito 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=257";
	  var urlEnviarComprobante 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1371";
	  var urlConsultarEstadoDteComprobante				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3152";	  
	  var urlCrearContratoCobroPeriodico 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3106";
	  var urlDteEnviarLibro 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3224";
	  var urlCalcularMovimientoComprobanteCobrosCostos	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3229";
	  var urlGenerarInterfazAfp							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20010";
	  var urlCrearComprobanteEmisionMasiva				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4192";
	  //---------------------------------------------------------------------------------------------------
	  var urlCrearComprobanteJava 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20042";	  
	  var urlCrearCargaMasiva 						    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1799";	  
	  var urlEliminarCargaMasiva 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1801";	  
	  var urlProcesarCargaMasiva 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1802";
	  var urlInscribirProcesoBatch						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
	  var urlConsultarIVA								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=5000";
	  var urlInformeDTE                                 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20057"
	  var urlCrearMovimientosDesdeOtroComprobante       = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100014";
	  var urlProcesarPeriodoCierre                      = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4220";
	  var urlSolicitudGenerarArchivo                    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1811";
	  var urlCargaMasivaRefacturacion                   = "/BSCL/Plataforma/GWJAVA/PLA/call?t=300011";
	  var urlCrearComprobanteCobroMensual               = "/BSCL/Plataforma/GWJAVA/PLA/call?t=17";
	  var urlCrearComprobanteNotaDebito                 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700012";
	  var urlEnviarCobrosMensualesFacturar              = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010267";
	  var urlConsultaOperadoresRueda					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1351";
	  var urlModificarMovimientoComprobanteMasivo		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700305";
	  var urlModificarMovimientoComprobanteCallSP		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290659";
	  var urlConsultarMovimientoComprobanteCobrosModificar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700389";
	  var urlModificarMovimientoComprobanteCobrosMasivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700390";
 	  var urlDuplicarFactura 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=900104";
	  var urlModificarRetencionIgk 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306004";
	  var urlAnularMovimientosPorAsignacionMasiva       = "/BSCL/Plataforma/GWJAVA/PLA/call?t=703000";
	  var urlConsultarDetalleAnularMovimientos			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010385";

      return $resource("", {},
      {
    	  'CrearMovimientoComprobante'					: { method: "POST", url: urlCrearMovimientoComprobante},
    	  'CrearComprobante'							: { method: "POST", url: urlCrearComprobante},
    	  'ModificarEstadoMovimiento'					: { method: "POST", url: urlModificarEstadoMovimiento},
    	  'AnularMovimiento'							: { method: "POST", url: urlAnularMovimiento},
    	  'AnularComprobante'							: { method: "POST", url: urlAnularComprobante},
    	  'CrearAutorizacionAnularComprobante'			: { method: "POST", url: urlCrearAutorizacionAnularComprobante},
    	  'ModificarMovimientoComprobante'				: { method: "POST", url: urlModificarMovimientoComprobante},
		  'AjusteSencilloComprobante'					: { method: "POST", url: urlAjusteSencilloComprobante},
    	  'ActualizarMontosCobro' 						: { method: "POST", url: urlActualizarMontosCobro},
    	  'CrearComprobanteNotaCredito' 				: { method: "POST", url: urlCrearComprobanteNotaCredito},
    	  'EnviarComprobante' 							: { method: "POST", url: urlEnviarComprobante}, 
    	  'ConsultarEstadoDteComprobante' 				: { method: "POST", url: urlConsultarEstadoDteComprobante},
    	  'CrearContratoCobroPeriodico'					: { method: "POST", url: urlCrearContratoCobroPeriodico},
    	  'DteEnviarLibro'								: { method: "POST", url: urlDteEnviarLibro},
    	  'CalcularMovimientoComprobanteCobrosCostos'	: { method: "POST", url: urlCalcularMovimientoComprobanteCobrosCostos},
    	  'GenerarInterfazAfp' 							: { method: "POST", url: urlGenerarInterfazAfp},
    	  'CrearComprobanteEmisionMasiva'               : { method: "POST", url: urlCrearComprobanteEmisionMasiva},
    	//---------------------------------------------------------------------------------------------------
    	  'CrearComprobanteJava'						: { method: "POST", url: urlCrearComprobanteJava},
    	  'CrearCargaMasiva'						    : { method: "POST", url: urlCrearCargaMasiva},
    	  'EliminarCargaMasiva'						    : { method: "POST", url: urlEliminarCargaMasiva},
    	  'ProcesarCargaMasiva'						    : { method: "POST", url: urlProcesarCargaMasiva},
    	  'InscribirProcesoBatch'						: { method: "POST", url: urlInscribirProcesoBatch},
    	  'ConsultarIVA'								: { method: "POST", url: urlConsultarIVA},
    	  'InformeDTE'                                  : { method: "POST", url: urlInformeDTE},
    	  'CrearMovimientosDesdeOtroComprobante'        : { method: "POST", url: urlCrearMovimientosDesdeOtroComprobante},
    	  'ProcesarPeriodoCierre'                       : { method: "POST", url: urlProcesarPeriodoCierre},
    	  'SolicitudGenerarArchivo'                     : { method: "POST", url: urlSolicitudGenerarArchivo},
    	  'CargaMasivaRefacturacion'					: { method: "POST", url: urlCargaMasivaRefacturacion},
    	  'CrearComprobanteCobroMensual'                : { method: "POST", url: urlCrearComprobanteCobroMensual},    
    	  'CrearComprobanteNotaDebito'                  : { method: "POST", url: urlCrearComprobanteNotaDebito},    
    	  'EnviarCobrosMensualesFacturar'               : { method: "POST", url: urlEnviarCobrosMensualesFacturar},
    	  'ConsultaOperadoresRueda'						: { method: "POST", url: urlConsultaOperadoresRueda},
    	  'ModificarMovimientoComprobanteMasivo'		: { method: "POST", url: urlModificarMovimientoComprobanteMasivo},
		  'ModificarMovimientoComprobanteCallSP'		: { method: "POST", url: urlModificarMovimientoComprobanteCallSP},
		  'ConsultarMovimientoComprobanteCobrosModificar' : { method: "POST", url: urlConsultarMovimientoComprobanteCobrosModificar},
		  'ModificarMovimientoComprobanteCobrosMasivo' 	: { method: "POST", url: urlModificarMovimientoComprobanteCobrosMasivo},
		  'DuplicarFactura'							 	: { method: "POST", url: urlDuplicarFactura},
		  'ModificarRetencionIgk'						: { method: "POST", url: urlModificarRetencionIgk},
		  'AnularMovimientosPorAsignacionMasiva'		: { method: "POST", url: urlAnularMovimientosPorAsignacionMasiva},
		  'ConsultarDetalleAnularMovimientos'			: { method: "POST", url: urlConsultarDetalleAnularMovimientos}
      });
}]);