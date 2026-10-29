angular.module('ModuloPortafolio', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

(function() {
	angular.module('ModuloPortafolio').factory('servicioPortafolio',['$resource',function($resource) {
		//DATOS BASICOS PORTAFOLIO
		var urlGetPortafolio = "";
		var urlSavePortafolio 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=59";
		var urlUpdatePortafolio 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=57";
		var urlConfirmPortafolio		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=58";
		//USOS PORTAFOLIO
		var urlUpdateUsoPortafolio		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=125";
		//ESTADO PORTAFOLIO
		var urlUpdateEstadoPortafolio	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=178";
		//PORTAFOLIO AUTORIZACIONES
		var urlCrearAutorizadoOrden		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=156";
		//PREFERENCIA PAGO
		var urlSavePreferenciaPago		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=261";
		var urlUpdatePreferenciaPago	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=290";
		//CUENTA CUSTODIO
		var urlSaveCuentaCustodio		= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=700290";
		var urlUpdateCuentaCustodio		= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=1252";
		
		//MATRIZ NO APV
		var urlCrearMatrizAgrupadorNOAPV = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=601202";
		var urlModificarMatrizAgrupadorNOAPV = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=601203";
		var urlEliminarMatrizAgrupadorNOAPV	= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=601204";
		
		var urlConsultaCtainv 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=275";
		var urlServicioObtieneLimites 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700176";

		//MANTENEDOR CREACION DE PORTAFOLIO
		var urlAdministrarPortafolioDefecto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290672";
		
		return $resource("", {},
		{
	  	  	'getPortafolio'			: {method: "GET",	url:urlGetPortafolio},
	  	  	'savePortafolio'		: {method: "POST",	url:urlSavePortafolio},
	  	  	'updatePortafolio'		: {method: "POST",	url:urlUpdatePortafolio},
			'confirmPortafolio'		: {method: "POST",	url:urlConfirmPortafolio},  
			//USOS PORTAFOLIO
	  	  	'updateUsoPortafolio'	: {method: "POST",	url:urlUpdateUsoPortafolio},
	  	  	//ESTADO PORTAFOLIO
	  	  	'updateEstadoPortafolio': {method: "POST",	url:urlUpdateEstadoPortafolio},
	  	  	//PORTAFOLIO AUTORIZACIONES
	  	  	'crearAutorizadoOrden'	: {method: "POST",	url:urlCrearAutorizadoOrden},
	  	  	//PREFERENCIA PAGO
	  	  	'savePreferenciaPago'	: {method: "POST",	url:urlSavePreferenciaPago},
	  	  	'updatePreferenciaPago'	: {method: "POST",	url:urlUpdatePreferenciaPago},
	  	  	//CUENTA CUSTODIO
	  	  	'saveCuentaCustodio'	: {method: "POST",	url:urlSaveCuentaCustodio},
	  	  	'updateCuentaCustodio'	: {method: "POST",	url:urlUpdateCuentaCustodio},
	  	  	//MATRIZ NO APV
	  	  	'CrearMatrizAgrupadorNOAPV'	: {method: "POST",	url:urlCrearMatrizAgrupadorNOAPV},
	  	  	'ModificarMatrizAgrupadorNOAPV'	: {method: "POST",	url:urlModificarMatrizAgrupadorNOAPV},
	  	  	'EliminarMatrizAgrupadorNOAPV'	: {method: "POST",	url:urlEliminarMatrizAgrupadorNOAPV},
	  	  	
	  	  	'consultaCtaInversionServicio'	: {method: "POST",	url:urlConsultaCtainv},
	  	  	'obtieneLimitesServicio'	: {method: "POST",	url:urlServicioObtieneLimites},

			//MANTENEDOR CREACION DE PORTAFOLIO
			'AdministrarPortafolioDefecto'		: {method: "POST", url:urlAdministrarPortafolioDefecto}
	  	  	
	     });		
	}]);
}());


angular.module('ModuloPortafolio').factory('bcsConfiguracionPortafolio',function($translate) {

	var obj = {};
	
	obj.getSchemaSolicitudDeCambio = function() {
		return {
			model: {
				fields : {
					IdSolicitudCambio		: { type: "number"},
					IdSecuenciaCambio		: { type: "number"},
					IdTipoEntidad			: { type: "string"},
					IdEntidadOrigen			: { type: "string"},
					CampoModificado			: { type: "string"},
					TipoDato				: { type: "string"},
					ValorAntiguo			: { type: "string"},
					ValorNuevo				: { type: "string"},
					FechaSolicitud			: { type: "string"},
					FechaRevision			: { type: "string"},
					IdEstadoAutorizacion	: { type: "string"}
				}
			}
		};
	 };
	
	 return obj;
});	


