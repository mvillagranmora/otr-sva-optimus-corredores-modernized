angular.module('ModuloCuentaInversion', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

//Configuración llamada a Servicios
angular.module('ModuloCuentaInversion').factory('bcsServiciosCuentaInversion',['$resource',function($resource) {
	var urlCrearCargoAbono 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=7";
	var urlConsultarSaldoCtaInv 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21";
	var urlCertificarCargoAbono 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=169";
	var urlAnularCargoAbono 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=170";	
	var urlLiquidarSaldo 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1003";
	var urlGestionCuentaInversion 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1491";
	var urlEjecutarCuadraturaSaldos 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1771";	
	var urlSolicitudGenerarArchivo 	    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1812";
	//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
	var urlCargaMasivaCtaInversion 	    		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3339";
	var urlProcesaCargaSeleccionada 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3340";
	var urlEliminaCargaSeleccionada 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3342";	
	//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos 
	//CELZORRO-272 - MBI Atributtion Analisys - Carga Índices
	var urlProcesaIndicesAtributtionAnalisys  	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601216";
	//CELZORRO-272 - MBI Atributtion Analisys - Carga Índices
	var urlAtributtionAnalysisInforme        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700016";
	var urlConsultarDiferenciaSaldoCtaInversion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=173";
	var urlReportePorPagarYCobrar        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290652";
	var urlCrearOpcionCobroComision        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702159";
	var urlEditarOpcionCobroComision        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702160";
	var urlEliminarOpcionCobroComision        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702161";
	var urlEditarCobroComisionRegistro        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702162";
	var urlEliminarCobroComisionRegistros        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702163";
	var urlConsultarCobroComisionRegitros        	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=702164";
	var urlCrearCobroComisionExclusionConcepto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=701162";
	var urlEliminarCobroComisionExclusionConcepto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=701163";
	var urlProcesarCobroComisionPatrimonio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=701164";
	
	return $resource("", {},
	{
		'AtributtionAnalysisInforme'		: { method: "POST", url: urlAtributtionAnalysisInforme},
		'CrearCargoAbono'			: { method: "POST", url: urlCrearCargoAbono},
		'ConsultarSaldoCtaInv'		: { method: "POST", url: urlConsultarSaldoCtaInv},
		'CertificarCargoAbono'		: { method: "POST", url: urlCertificarCargoAbono},
		'AnularCargoAbono'			: { method: "POST", url: urlAnularCargoAbono},
		'LiquidarSaldo'				: { method: "POST", url: urlLiquidarSaldo},
		'GestionCuentaInversion'	: { method: "POST", url: urlGestionCuentaInversion},
		'EjecutarCuadraturaSaldos'	: { method: "POST", url: urlEjecutarCuadraturaSaldos},		
		'SolicitudGenerarArchivo'	: { method: "POST", url: urlSolicitudGenerarArchivo},
		//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
		'CrearCargaMasiva'	        : { method: "POST", url: urlCargaMasivaCtaInversion},
		'ProcesaCargaSeleccionada'	: { method: "POST", url: urlProcesaCargaSeleccionada},
		'EliminaCargaSeleccionada'  : { method: "POST", url: urlEliminaCargaSeleccionada},
		//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
		//CELZORRO-272 - MBI Atributtion Analisys - Carga Índices
		'ProcesaIndicesAtributtionAnalisys'	: { method: "POST", url: urlProcesaIndicesAtributtionAnalisys},
		'ConsultarDiferenciaSaldoCtaInversion' : { method: "POST", url: urlConsultarDiferenciaSaldoCtaInversion},
		'urlReportePorPagarYCobrar'			: { method: "POST", url: urlReportePorPagarYCobrar},
		//CELZORRO-272 - MBI Atributtion Analisys - Carga Índices		
		'CrearCobroComisionOpcionCobro'			:{ method: "POST", url: urlCrearOpcionCobroComision},
		'EditarCobroComisionOpcionCobro'			:{ method: "POST", url: urlEditarOpcionCobroComision},
		'EliminarCobroComisionOpcionCobro'			:{ method: "POST", url: urlEliminarOpcionCobroComision},
		'EditarCobroComisionRegistro'			:{ method: "POST", url: urlEditarCobroComisionRegistro},
		'EliminarCobroComisionRegistros'			:{ method: "POST", url: urlEliminarCobroComisionRegistros},
		'ConsultarCobroComisionRegitros'			:{ method: "POST", url: urlConsultarCobroComisionRegitros},		
		'CrearCobroComisionExclusionConcepto'		: { method: "POST", url: urlCrearCobroComisionExclusionConcepto},	
		'EliminarCobroComisionExclusionConcepto'	: { method: "POST", url: urlEliminarCobroComisionExclusionConcepto},
		'ProcesarCobroComisionPatrimonio'	: { method: "POST", url: urlProcesarCobroComisionPatrimonio},
	});
}]);