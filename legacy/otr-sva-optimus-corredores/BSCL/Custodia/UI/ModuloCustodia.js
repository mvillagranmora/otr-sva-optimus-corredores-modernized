angular.module('ModuloCustodia', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);
  
angular.module('ModuloCustodia').factory('bcsServiciosMovTransito',['$resource',function($resource) {
	
		CertificarMovimiento			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=11";
		AnularMovimiento				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21016";
		AnularMovimientoCertificado		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700027";
		CertificarMovimientoAgrupado	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=63022";
		CertificarMovimientoTotalizador	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=63023";
		GeneracionTraspasos				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=514";		
		CalcularMontoPorTipoInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=71";
		ActualizarPrecioMonedaInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=900102";
		GenerarMovimientoContraparteGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700429";
	      return $resource("", {},
	      {	          
	    	  'CertificarMovimiento'			:	{ method: "POST", url: CertificarMovimiento},
	    	  'AnularMovimiento'				:	{ method: "POST", url: AnularMovimiento},
	    	  'AnularMovimientoCertificado'		:	{ method: "POST", url: AnularMovimientoCertificado},
			  'CertificarMovimientoAgrupado'	:	{ method: "POST", url: CertificarMovimientoAgrupado},
			  'CertificarMovimientoTotalizador'	:	{ method: "POST", url: CertificarMovimientoTotalizador},
	    	  'GeneracionTraspasos'				:	{ method: "POST", url: GeneracionTraspasos},
	    	  'CalcularMontoPorTipoInstrumento'	:	{ method: "POST", url: CalcularMontoPorTipoInstrumento},
	    	  'ActualizarPrecioMonedaInstrumento'	:	{ method: "POST", url: ActualizarPrecioMonedaInstrumento},
	    	  'GenerarMovimientoContraparteGarantia'	:	{ method: "POST", url: GenerarMovimientoContraparteGarantia},
	       });
}]);

angular.module('ModuloCustodia').factory('bcsServiciosCustodia',['$resource',function($resource) {
	var urlIngresarTituloJava				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21012";
	//var urlClienteTieneCustodia				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=342";
	var urlClienteTieneCustodia				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100009";
    var urlClienteSinCustodia				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290648";
	var urlConsultarFlujoInstrumento		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=19";
	var urlClaveOperacionesPendientes		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3240";
	var urlCrearEnvioOperacionesPendientes	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700364";
	var urlTarjetaExistencia				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1793";
	var urlEnviarOperacionesPendientes 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20028";
	var urlRespaldarCustodia 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=300002";
	var urlValidaRetiroCustodia				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600059";
	var urlConsultarDetallePrecioPromedio	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700028";
	var urlConsultarSigaCrmHistorico		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600000";
	var urlIngresarEjercicio				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700151";
	var urlConsultarEjercicioBloqueo		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700153";
	var urlConsultarFechaCorteHistorica		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700";
	var urlFusionCarteras					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290690";
	var urlFusionCarterasClientes           = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290693";
	var urlCuadrarCircular1518				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700393";
	var urlCargarArchivoL02					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700395";
	var urlCargarArhcivoL043				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700412";
	var urlCargarArchivoOtrosSaldos			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700396";
	var urlCargarArchivoOptimus				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700397";
	var urlProcesarCuadraturaCustodia		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700410";
	// Boton para SVARESGIN-7050
	var urlLimpiarCargasCircular1518		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010501";
	var urlCrearCargaMasivaISFUT            = "/BSCL/Plataforma/GWJAVA/PLA/call?t=800100";
	var urlObtenerVersionEsquema 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1679";
	var urlCircularizacionGuardarParametros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3019";
	var urlConsultarInformeMayorYMenorValor = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010141";
	var urlConsultarInformeSimultaneaCuentaPropia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010144";
	var urlGenerarArchivoInformeCuentaPropia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010145";

	return $resource("", {},
  	{
		'IngresarTituloJava'				:	{ method: "POST", url: urlIngresarTituloJava},
		'ClienteTieneCustodia'				:	{ method: "POST", url: urlClienteTieneCustodia},
		'ClienteSinCustodia'				:	{ method: "POST", url: urlClienteSinCustodia},
		'ConsultarFlujoInstrumento'			:	{ method: "POST", url: urlConsultarFlujoInstrumento},
		'CrearEnvioOperacionesPendientes'	:   { method: "POST", url: urlCrearEnvioOperacionesPendientes},
		'ClaveOperacionesPendientes'		:   { method: "POST", url: urlClaveOperacionesPendientes},
		'TarjetaExistencia'					:   { method: "POST", url: urlTarjetaExistencia},
		'EnviarOperacionesPendientes'		:	{ method: "POST", url: urlEnviarOperacionesPendientes},
		'RespaldarCustodia'					:	{ method: "POST", url: urlRespaldarCustodia},
		'ValidaRetiroCustodia'				:	{ method: "POST", url: urlValidaRetiroCustodia},
		'ConsultarDetallePrecioPromedio'	:	{ method: "POST", url: urlConsultarDetallePrecioPromedio},
		'ConsultarSigaCrmHistorico'			:	{ method: "POST", url: urlConsultarSigaCrmHistorico},
		'IngresarEjercicio'					:	{ method: "POST", url: urlIngresarEjercicio},
		'ConsultarEjercicioBloqueo'			:	{ method: "POST", url: urlConsultarEjercicioBloqueo},
		'ConsultarFechaCorteHistorica'		:	{ method: "POST", url: urlConsultarFechaCorteHistorica},
		'FusionarCarteras'					:	{ method: "POST", url: urlFusionCarteras},
		'FusionarCarterasClientes'			:	{ method: "POST", url: urlFusionCarterasClientes},
		'CuadrarCircular1518'				:	{ method: "POST", url: urlCuadrarCircular1518},
		'CargarArchivoL02'					:	{ method: "POST", url: urlCargarArchivoL02},
		'CargarArchivoL043'					:	{ method: "POST", url: urlCargarArhcivoL043},
		'CargarArchivoOtrosSaldos'			:	{ method: "POST", url: urlCargarArchivoOtrosSaldos},
		'CargarArchivoOptimus'				:	{ method: "POST", url: urlCargarArchivoOptimus},
		'ProcesarCuadraturaCustodia'		:	{ method: "POST", url: urlProcesarCuadraturaCustodia},
		// Boton para SVARESGIN-7050
		'LimpiarCargasCircular1518'			:	{ method: "POST", url: urlLimpiarCargasCircular1518},
		'CrearCargaMasivaISFUT'             :   { method: "POST", url: urlCrearCargaMasivaISFUT},
		'ObtenerVersionEsquema'                     : { method: "POST", url: urlObtenerVersionEsquema},
		'CircularizacionGuardarParametros'          : { method: "POST", url: urlCircularizacionGuardarParametros},
		'ConsultarInformeMayorYMenorValor'          : { method: "POST", url: urlConsultarInformeMayorYMenorValor},
		'ConsultarInformeSimultaneaCuentaPropia'    : { method: "POST", url: urlConsultarInformeSimultaneaCuentaPropia},
		'GenerarArchivoInformeCuentaPropia'     : { method: "POST", url: urlGenerarArchivoInformeCuentaPropia},
   	});
}]);


angular.module('ModuloCustodia').factory('bcsServiciosGarantia',['$resource',function($resource) {
	var obj = {};
	
	var urlIngresarGarantia 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21017";
	var urlRetirarGarantia 								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21018";
	var urlRetiroGarantiasAgrupadas                     = "/BSCL/Plataforma/GWJAVA/PLA/call?t=47525";
	var urlSolicitudGenerarArchivoGarantiaOperaciones	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21022";
	var urlSolicitudGenerarArchivoGarantiaMargen 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21023";
	var urlSolicitudGenerarArchivoGarantiaOtros 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21024";
	var urlObtenerParidadTipoCambio 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3155";
	
	return $resource("", {},
  	{
		'IngresarGarantia'								:	{ method: "POST", url: urlIngresarGarantia},
		'RetirarGarantia'								:	{ method: "POST", url: urlRetirarGarantia},
		'RetiroGarantiasAgrupadas'                      :   { method: "POST", url: urlRetiroGarantiasAgrupadas},
		'SolicitudGenerarArchivoGarantiaOperaciones'	:	{ method: "POST", url: urlSolicitudGenerarArchivoGarantiaOperaciones},
		'SolicitudGenerarArchivoGarantiaMargen'			:	{ method: "POST", url: urlSolicitudGenerarArchivoGarantiaMargen},
		'SolicitudGenerarArchivoGarantiaOtros'			:	{ method: "POST", url: urlSolicitudGenerarArchivoGarantiaOtros},
		'ObtenerParidadTipoCambio'						:	{ method: "POST", url: urlObtenerParidadTipoCambio},
   	});
	
	return obj;
}]);


angular.module('ModuloCustodia').factory('bcsServiciosSimultanea',['$resource',function($resource) {
	var obj = {};
	
	obj.AnticipoSimultanea = function(datos,success_fn,error_fn){
		var urlAnticipoSimultanea= "/BSCL/Plataforma/GWJAVA/PLA/call?t=51";
		var r = $resource("", {},{
			'AnticipoSimultanea': { method: "POST", url: urlAnticipoSimultanea}
		});
		var post = r.AnticipoSimultanea(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.VencimientoSimultanea = function(datos,success_fn,error_fn){
		var urlVencimientoSimultanea = "/BSCL/Plataforma/GWJAVA/PLA/call?t=56"; 
		var r = $resource("", {},{
			'VencimientoSimultanea': { method: "POST", url: urlVencimientoSimultanea}
		});
		var post = r.VencimientoSimultanea(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	return obj;
}]);


angular.module('ModuloCustodia').factory('bcsServiciosValorizacion',['$resource',function($resource) {
	var obj = {};
	
	var urlValorizarInstrumentos				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20200";
	var urlValorizarCustodiaMasiva				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3136";
	var urlValorizarCustodiaMasivaUI			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3154";
	var urlConsultarPrecioMedioDelDia			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100010";
	var urlCargaSimultaneasDesdeSebra			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100067";

	
	return $resource("", {},
  	{
		'ValorizarInstrumento'					:	{ method: "POST", url: urlValorizarInstrumentos},
		'ValorizarCustodiaMasiva'				:	{ method: "POST", url: urlValorizarCustodiaMasiva},
		'ValorizarCustodiaMasiva'				:	{ method: "POST", url: urlValorizarCustodiaMasivaUI},
		'ConsultarPrecioMedioDelDia'			:	{ method: "POST", url: urlConsultarPrecioMedioDelDia},
		'CargaSimultaneasDesdeSebra'			:	{ method: "POST", url: urlCargaSimultaneasDesdeSebra},
   	});

	return obj;
}]);


angular.module('ModuloCustodia').factory('bcsServiciosPrestamo',['$resource',function($resource) {
	var obj = {};
	
	obj.ConsultarContratoCustodia = function(datos,success_fn,error_fn){
		var urlConsultarContratoCustodia= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100007";
		var r = $resource("", {},{
			'ConsultarContratoCustodia': { method: "POST", url: urlConsultarContratoCustodia}
		});
		var post = r.ConsultarContratoCustodia(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.CrearPrestamo = function(datos,success_fn,error_fn){
		var urlCrearPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=62";
		var r = $resource("", {},{
			'CrearPrestamo': { method: "POST", url: urlCrearPrestamo}
		});
		var post = r.CrearPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.ModificarPrestamo = function(datos,success_fn,error_fn){
		var urlModificarPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100018";
		var r = $resource("", {},{
			'ModificarPrestamo': { method: "POST", url: urlModificarPrestamo}
		});
		var post = r.ModificarPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.ModificarEmprestito = function(datos,success_fn,error_fn){
		var urlModificarEmprestito= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3336";
		var r = $resource("", {},{
			'ModificarEmprestito': { method: "POST", url: urlModificarEmprestito}
		});
		var post = r.ModificarEmprestito(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.RenovarPrestamoMasivo = function RenovarPrestamoMasivo(payload, sfn, efn) {
		const resource = $resource('', {}, { 'RenovarPrestamoMasivo': { method: 'POST', url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700725' } });
		resource.RenovarPrestamoMasivo(payload).$promise.then(sfn).catch(efn);
	};
	
	obj.CrearEmprestito = function(datos,success_fn,error_fn){
		var urlCrearEmprestito= "/BSCL/Plataforma/GWJAVA/PLA/call?t=64";
		var r = $resource("", {},{
			'CrearEmprestito': { method: "POST", url: urlCrearEmprestito}
		});
		var post = r.CrearEmprestito(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.AnticiparPrestamo = function(datos,success_fn,error_fn){
		var urlAnticiparPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=77";
		var r = $resource("", {},{
			'AnticiparPrestamo': { method: "POST", url: urlAnticiparPrestamo}
		});
		var post = r.AnticiparPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.AnticiparEmprestito = function(datos,success_fn,error_fn){
		var urlAnticiparEmprestito= "/BSCL/Plataforma/GWJAVA/PLA/call?t=78";
		var r = $resource("", {},{
			'AnticiparEmprestito': { method: "POST", url: urlAnticiparEmprestito}
		});
		var post = r.AnticiparEmprestito(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.AnticipoEmprestitoSinCustodia = function(datos,success_fn,error_fn){
		var urlAnticipoEmprestitoSinCustodia= "/BSCL/Plataforma/GWJAVA/PLA/call?t=78000000";
		var r = $resource("", {},{
			'AnticipoEmprestitoSinCustodia': { method: "POST", url: urlAnticipoEmprestitoSinCustodia}
		});
		var post = r.AnticipoEmprestitoSinCustodia(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.ReversarAnticipoPrestamo = function(datos,success_fn,error_fn){
		var urlReversarAnticipoPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=629";
		var r = $resource("", {},{
			'ReversarAnticipoPrestamo': { method: "POST", url: urlReversarAnticipoPrestamo}
		});
		var post = r.ReversarAnticipoPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};	
	
	obj.AnularPrestamo = function(datos,success_fn,error_fn){
		var urlAnularPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=85";
		var r = $resource("", {},{
			'AnularPrestamo': { method: "POST", url: urlAnularPrestamo}
		});
		var post = r.AnularPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.AnularEmprestito = function(datos,success_fn,error_fn){
		var urlAnularEmprestito= "/BSCL/Plataforma/GWJAVA/PLA/call?t=86";
		var r = $resource("", {},{
			'AnularEmprestito': { method: "POST", url: urlAnularEmprestito}
		});
		var post = r.AnularEmprestito(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.VencerPrestamo = function(datos,success_fn,error_fn){
		var urlVencerPrestamo= "/BSCL/Plataforma/GWJAVA/PLA/call?t=88";
		var r = $resource("", {},{
			'VencerPrestamo': { method: "POST", url: urlVencerPrestamo}
		});
		var post = r.VencerPrestamo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.CalcularPremios = function(datos,success_fn,error_fn){
		var urlCalcularPremios= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100011";
		var r = $resource("", {},{
			'CalcularPremios': { method: "POST", url: urlCalcularPremios}
		});
		var post = r.CalcularPremios(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	obj.ReprocesarPrestamos = function(datos,success_fn,error_fn){
		var urlReprocesarPrestamos= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601205";
		var r = $resource("", {},{
			'ReprocesarPrestamos': { method: "POST", url: urlReprocesarPrestamos}
		});
		var post = r.ReprocesarPrestamos(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	return obj;
}]);


angular.module('ModuloCustodia').factory('bcsServiciosEventoCapital',['$resource',function($resource) {
		CrearEventoCapital 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=199";
		ProcesarEventoCapital 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=198";
		ReprocesarEventoCapital  				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=617";
		ProcesarEventoCapitalEmisionPago		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=323";///eliminar
		NotificarEventoCapital					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=326";
		CrearEventoCapitalCliente 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=208";
		ModificarEventoCapitalCliente 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=229";
		ModificarEventoCapital 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=215";
		EliminarEventoCapital 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=227";
		EliminarEventoCapitalCliente 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=237";
		ConsultaEventoCapital 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=238"; 
		urlConsultaSecuenciaEvento				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=2000";
		CerrarEventoCapitalDV 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=299";//eliminar
		CerrarEventoCapitalDA 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=300";//eliminar
		CerrarEventoCapitalEmisionLiberada		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20";//eliminar
		
		CalcularParidad 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3155";
		ConsultarImpuestoDivExtranjero			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=343434";
		ConsultarGastoEventoCapitalDV			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=343435";
		
		CerrarEventoCapitalRC		            = "/BSCL/Plataforma/GWJAVA/PLA/call?t=2008";
		
		ActualizarCustodiaOSA 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=327";
		ConsultaCarterasSuscripcionCliente		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=338";
		CertificaSuscripcionOSA					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=343";
		SuscripcionEmisionPago					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20003";
		NotificaSuscripcionOSA					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=345";
		AnulaSuscripcionOSA						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=346";
		RetirarCustodiaOSA						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=347";
		FacturarEventoCapitalCliente			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=348";
		RenovarEventoCapitalEmisionPago			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=354";
		ProcesarEventoCapitalCorteCupon			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=420";///eliminar
		CerrarEventoCapitalCorteCupon			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=425";//eliminar
		ProcesarEventoCapitalCanje				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=431";///eliminar
		CerrarEventoCapitalCanje				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=435";//eliminar
		ProcesarEventoCapitalDivisionSociedad	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=442";///eliminar
		CerrarEventoCapitalDivisionSociedad		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=443";	//eliminar
		ActualizaContraparte 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=616";
		CerrarEventoCapitalCambioNemo 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=628";
		ActualizaIndice							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=670";
		ProrrateoSorteoLetras					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=198";//1419";
		CerrarEventoCapital			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1425";//antees era CerrarEventoCapitalSorteoLetras		
		ModificarFormaDePagoEventoCapitalCliente= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3059";
		ProcesarCambiodeNemo					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=674";///eliminar
		ProcesarEventoCapitalEmisionLiberada	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=10";///eliminar
		ProcesarEventoCapitalLiberada			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3";///eliminar
		ReplicarEventoCapital					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=142";
		CargaMasivaDividendosEnDineroFFMM		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100038";
		ProcesarEventoCapitalDividendosEnDinero	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=100041";///eliminar
		InscribirClientePrepagoBono				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1821";  //PB: Prepaglo Bono (PERU)
		CertificarEventoCapital					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1822";  //PB: Prepaglo Bono (PERU)
		EnviarFacturarPrepagoBono				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1823";  //PB: Prepaglo Bono (PERU)
		CerrarEventoCapitalDividendosEnDinero	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600006";//eliminar
		CerrarEventoCapitalEmisionPagada		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1824";
		CerrarEventoCapitalSuscripcion			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1827";
		FacturarEventoCapitalClienteMasivo		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290656";
		CertificarEventoCapitalClienteMasivo	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290657";
		CerrarEventoCapitalMasivo               = "/BSCL/Plataforma/GWJAVA/PLA/call?t=2009";
		
	return $resource("", {},
  {
	  'CrearEventoCapital':							{ method: "POST", url: CrearEventoCapital},
	  'CrearEventoCapitalCliente':					{ method: "POST", url: CrearEventoCapitalCliente},
	  'ProcesarEventoCapital':						{ method: "POST", url: ProcesarEventoCapital},
	  'ReprocesarEventoCapital':					{ method: "POST", url: ReprocesarEventoCapital},
	  'ProcesarEventoCapitalEmisionPago':			{ method: "POST", url: ProcesarEventoCapitalEmisionPago},
	  'NotificarEventoCapital':						{ method: "POST", url: NotificarEventoCapital},
	  'ModificarEventoCapital':						{ method: "POST", url: ModificarEventoCapital},
	  'ModificarEventoCapitalCliente':				{ method: "POST", url: ModificarEventoCapitalCliente},
	  'EliminarEventoCapital':						{ method: "POST", url: EliminarEventoCapital},
	  'EliminarEventoCapitalCliente':				{ method: "POST", url: EliminarEventoCapitalCliente},
	  'ConsultaEventoCapital':						{ method: "POST", url: ConsultaEventoCapital},
	  'ConsultaSecuenciaEvento':					{ method: "POST", url: urlConsultaSecuenciaEvento},
	  'CerrarEventoCapitalDV':						{ method: "POST", url: CerrarEventoCapitalDV},
	  
	  'CalcularParidad':							{method: "POST", url: CalcularParidad},
	  'ConsultarImpuestoDivExtranjero':				{method: "POST", url: ConsultarImpuestoDivExtranjero},
	  'ConsultarGastoEventoCapitalDV':				{method: "POST", url: ConsultarGastoEventoCapitalDV},

	  'CerrarEventoCapitalRC':						{ method: "POST", url: CerrarEventoCapitalRC},
	  
	  'CerrarEventoCapitalDA':						{ method: "POST", url: CerrarEventoCapitalDA},
	  'CerrarEventoCapitalEmisionLiberada':			{ method: "POST", url: CerrarEventoCapitalEmisionLiberada},
	  'ActualizarCustodiaOSA':						{ method: "POST", url: ActualizarCustodiaOSA},
	  'ConsultaCarterasSuscripcionCliente':			{ method: "POST", url: ConsultaCarterasSuscripcionCliente},
	  'CertificaSuscripcionOSA':					{ method: "POST", url: CertificaSuscripcionOSA},
	  'SuscripcionEmisionPago' :					{ method: "POST", url: SuscripcionEmisionPago},	
	  'NotificaSuscripcionOSA' :					{ method: "POST", url: NotificaSuscripcionOSA},
	  'AnulaSuscripcionOSA'    :					{ method: "POST", url: AnulaSuscripcionOSA},
	  'RetirarCustodiaOSA'     :					{ method: "POST", url: RetirarCustodiaOSA},
	  'FacturarEventoCapitalCliente':				{ method: "POST", url: FacturarEventoCapitalCliente},
	  'RenovarEventoCapitalEmisionPago':			{ method: "POST", url: RenovarEventoCapitalEmisionPago},
	  'ProcesarEventoCapitalCorteCupon':			{ method: "POST", url: ProcesarEventoCapitalCorteCupon},
	  'CerrarEventoCapitalCorteCupon':				{ method: "POST", url: CerrarEventoCapitalCorteCupon},
	  'ProcesarEventoCapitalCanje':					{ method: "POST", url: ProcesarEventoCapitalCanje},
	  'CerrarEventoCapitalCanje':					{ method: "POST", url: CerrarEventoCapitalCanje},	  
	  'ProcesarEventoCapitalDivisionSociedad':		{ method: "POST", url: ProcesarEventoCapitalDivisionSociedad},	  
	  'CerrarEventoCapitalDivisionSociedad':		{ method: "POST", url: CerrarEventoCapitalDivisionSociedad},
	  'ActualizaContraparte':						{ method: "POST", url: ActualizaContraparte},
	  'CerrarEventoCapitalCambioNemo':				{ method: "POST", url: CerrarEventoCapitalCambioNemo},
	  'ActualizaIndice':							{ method: "POST", url: ActualizaIndice},	  
	  'ProrrateoSorteoLetras':						{ method: "POST", url: ProrrateoSorteoLetras},
	  'CerrarEventoCapital':			{ method: "POST", url: CerrarEventoCapital},
	  'ModificarFormaDePagoEventoCapitalCliente':	{ method: "POST", url: ModificarFormaDePagoEventoCapitalCliente},
	  'ProcesarCambiodeNemo':						{ method: "POST", url: ProcesarCambiodeNemo},
	  'ProcesarEventoCapitalEmisionLiberada':		{ method: "POST", url: ProcesarEventoCapitalEmisionLiberada},
	  'ReplicarEventoCapital':						{ method: "POST", url: ReplicarEventoCapital},
	  'ProcesarEventoCapitalLiberadaPE':			{ method: "POST", url: ProcesarEventoCapitalLiberada},
	  'CargaMasivaDividendosEnDineroFFMM':			{ method: "POST", url: CargaMasivaDividendosEnDineroFFMM},
	  'ProcesarEventoCapitalDividendosEnDinero':	{ method: "POST", url: ProcesarEventoCapitalDividendosEnDinero},
	  'InscribirClientePrepagoBono':				{ method: "POST", url: InscribirClientePrepagoBono},
	  'CertificarEventoCapital':					{ method: "POST", url: CertificarEventoCapital},
	  'EnviarFacturarPrepagoBono':					{ method: "POST", url: EnviarFacturarPrepagoBono},
	  'CerrarEventoCapitalDividendosEnDinero':		{ method: "POST", url: CerrarEventoCapitalDividendosEnDinero},
	  'CerrarEventoCapitalEmisionPagada':			{ method: "POST", url: CerrarEventoCapitalEmisionPagada},
	  'CerrarEventoCapitalSuscripcion':				{ method: "POST", url: CerrarEventoCapitalSuscripcion},
	  'FacturarEventoCapitalClienteMasivo':			{ method: "POST", url: FacturarEventoCapitalClienteMasivo},
	  'CertificarEventoCapitalClienteMasivo':		{ method: "POST", url: CertificarEventoCapitalClienteMasivo},
	  'CerrarEventoCapitalMasivo':                  { method: "POST", url: CerrarEventoCapitalMasivo}
   });
}]);

	
angular.module('ModuloCustodia').factory('bcsServiciosCuadraturaCustodia',['$resource',function($resource) {
		UrlGenerarMovimientosCuadraturaCustodia	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1746";
		UrlCerrarCuadraturaCustodia	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1788";
		UrlGenerarMovimientosCuadraturaCustodiaBVL = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4229";
		UrlGuardarCuadraturaCustodia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4269";
	return $resource("", {},
  {
		'GenerarMovimientosCuadraturaCustodia':	{ method: "POST", url: UrlGenerarMovimientosCuadraturaCustodia},
		'CerrarCuadraturaCustodia':	{ method: "POST", url: UrlCerrarCuadraturaCustodia},
		'GenerarMovimientosCuadraturaCustodiaBVL':	{ method: "POST", url: UrlGenerarMovimientosCuadraturaCustodiaBVL},
		'GuardarCuadraturaCustodia':	{ method: "POST", url: UrlGuardarCuadraturaCustodia}
   });
}]);

angular.module('ModuloCustodia').factory('bcsServiciosCargaMasivaCustodia',['$resource',function($resource) {
		UrlCrearCargaMasivaCustodia				=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3202";
		UrlProcesarCargaMasivaCustodia			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3204";
		UrlEliminarCargaMasivaCustodia			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3206";
		UrlProcesarCargaMasivaCustodiaReporte	= 	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3267";
		UrlInscribirProcesoBatch				= 	"/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
	return $resource("", {},
  {
	'CrearCargaMasivaCustodia'			:	{ method: "POST", url: UrlCrearCargaMasivaCustodia},
	'ProcesarCargaMasivaCustodia'		:	{ method: "POST", url: UrlProcesarCargaMasivaCustodia},
	'EliminarCargaMasivaCustodia'		:	{ method: "POST", url: UrlEliminarCargaMasivaCustodia},
	'ProcesarCargaMasivaCustodiaReporte':	{ method: "POST", url: UrlProcesarCargaMasivaCustodiaReporte},
	'InscribirProcesoBatch'				:	{ method: "POST", url: UrlInscribirProcesoBatch},
   });
}]);

angular.module('ModuloCustodia').factory('bcsServiciosJuntaAccionista',['$resource',function($resource) {
	
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id};
			
		});
		return r;
	};
	
	var resources = [
	        { nombre : 'CrearEventoJuntaAccionista', id : "3173" },
	        { nombre : 'ConsultarInstructivoVotacionJuntaAccionistas', id : "3176" },
	        { nombre : "ModificarEventoJuntaAccionista", id:"3179"},
	        { nombre : "EliminarEventoJuntaAccionista", id:"3180"},
	        { nombre : "CerrarEventoJuntaAccionista", id:"3181"},
	        { nombre : "ProcesarClientesConDerechoJuntaAccionista", id:"3184"},
	        { nombre : "EliminarProcesoClientesConDerechoJuntaAccionista", id:"3185"},
	        { nombre : "CrearClienteConDerechoJuntaAccionista", id:"3186"},
	        { nombre : "ModificarClienteConDerechoJuntaAccionista", id:"3187"},
	        { nombre : "EliminarClienteConDerechoJuntaAccionista", id:"3188"},
	        { nombre : "CrearClienteNominaJuntaAccionista", id:"3189"},
	        { nombre : "NotificarJuntaAccionistas", id:"3191"},
	        { nombre : "ConsultarNominaAccionistasParaNotificar", id:"3197"}
	     ];
	return $resource("", {},generarResource());
}]);

angular.module('ModuloCustodia').factory('bcsConfiguracionCustodia',function($translate, $rootScope, $q) {
	var obj = {};
	
	var CONFIGURACION = {};
	CONFIGURACION.defer = $q.defer();
	CONFIGURACION.promise = CONFIGURACION.defer.promise;
	
	var cargarConfiguracion = function() {
		var ds = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				filter : { field : "CodigoParametro", operator : "eq", value : "CUS.RangoMaximoMontoAnticipoSimultanea"},
				transport: {
					read: {
						url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=30000',
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							CodigoParametro : { type : "string"},
							Descripcion 	: { type : "string"},
							Valor 			: { type : "string"},
						}
					}
				}
			}
		});
		
		ds.fetch().then(function(){
			var data = ds.data();
			$.each(data,function(key,value){
				CONFIGURACION[value.CodigoParametro] = value.Valor;
			})
			CONFIGURACION.defer.resolve();
		},function() {
			console.error("ERROR AL CARGAR CONFIGURACION CUSTODIA");
		})
		
	};
	cargarConfiguracion();
	
	obj.CONFIGURACION = CONFIGURACION;
	 
	return obj;
});