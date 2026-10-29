angular.module('ModuloTesoreria', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);


//Configuración llamada a SP
angular.module('ModuloTesoreria').factory('bcsServiciosTesoreria',['$resource', function($resource) {
	var urlCrearIngresoEgreso 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=61&c=1";  
	var urlCrearComprobante 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=55";
	var urlLiquidarComprobantes 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=89";	  
	var urlGestionFormaDePago 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=91";
	var urlCrearCaja								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=127";
	var urlCrearCajaMoneda							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=131";
	var urlEdicionMultiple							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=133";
	var urlCrearCajaUsuario							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=135";
	//var urlCertidicarMovimientosTesoreria			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=141";
	var urlAnularMovimientosTesoreria				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=142";
	var urlCrearCajaProducto						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=139";
	var urlModificarMantenedorCaja					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=144";	
	var urlCrearCuentaBancaria			    		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=146&c=1";
	var urlModificarCuentaBancaria					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=147&c=1";
	var urlEliminarCaja								= "/BSCL/Plataforma/GWJAVA/PLA/call?t=155";
	var urlCrearChequera							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=161";	
	var urlEliminarCuentaBancaria					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=163";
	var urlModificarChequera						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=166";
	var urlCrearConfiguracionChequera				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=185";
	var urlModificarConfiguracionChequera			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=189";	
	var urlProcesarClearing	                		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=216";
	var urlRecepcionarNominaTransferencia			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=225";
	var urlValidarComprobantesEgreso          		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=701152"; 
	var urlAnularNominasTransferencias				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=230";
	var urlReprocesarNominasTransferencias			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=231";
	var urlCrearDeposito							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=255";
	var urlCumpleRequisitosCrearPartidaDesdeMov     = "/BSCL/Plataforma/GWJAVA/PLA/call?t=256";   //Validación cumple requisitos para generación de partida de ingreso/egreso desde movimientos
	var urlLiquidarDepositos   						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=259";	
	var urlAnularRegistros  					    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=313";   //Anulación de movimientos y comprobantes 
	var urlConsultaSaldoCuentaBanco					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=314";	
	var urlSolicitudGenerarArchivo   				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1469";
	var urlProcesarLiquidacionBilateral        		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1675";
	var urlModificarIngresoEgreso 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1708";
	var urlGestionDeMonedas 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1728";
	var urlCrearCargaMasiva 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1740";
	var urlAnularDepositos 							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1743";
	var urlEliminarCargaMasiva						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1748";
	var urlProcesarCargaMasiva						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1749";
	var urlGestionDeDeposito						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1750";	
	var urlGestionEnvioDocumentos					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1779&c=1";	 
	var urlCierreDiario					            = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1782";
	var urlAnularCheque					            = "/BSCL/Plataforma/GWJAVA/PLA/call?t=6025";
	var urlActualizarTransaccion		            = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20038";   //PLUGIN_TES.jar@cl.bcs.tesoreria.cloper.ActualizarTransaccion
	var urlCrearTraspaso							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600010";
	var urlCrearCargaMasivaPac 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601102"; /// para el módulo tesoreria PAC  servi
	var urlCrearCargaMasivaPacRen 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601103"; /// para el módulo tesoreria PAC  servi
	var urlProcesarMandatoPAC					    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601104";
	var urlSolicitudGenerarArchivoPac   			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601107";
	var urlRevocacionMandatoPAC   					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601140";
	var urlModificarPAC   							= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601143";
	var urlAnulacionMandatoPAC   					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601147";
	var urlValidarTransferenciaDVP 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700146";
	var urlGirarComprobantesIngreso          		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700152";
	var urlGirarComprobantesEgreso          		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=701153";
	var urlAgruparComprobantesIngreso				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700159";
	var urlLiquidacionMasivaComprobantes 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700160";	
	var urlCertificarMovimientos 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=11";
	var urlValidaEjecucionProcesoRecPAC 		    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290625";
	var urlEdicionMultipleJava						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700350";
	var urlPreLiquidarComprobantes 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=5089";
	var urlQuitarDetalleNominaTransferencia			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=2071415";
	var urlLiquidarTodosComprobantes				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=495";


	return $resource("", {},
	{
		'CrearIngresoEgreso' 					: { method: "POST", url: urlCrearIngresoEgreso},
		'CrearComprobante'						: { method: "POST", url: urlCrearComprobante},
		'LiquidarComprobantes'					: { method: "POST", url: urlLiquidarComprobantes},
		'GestionFormaDePago'					: { method: "POST", url: urlGestionFormaDePago},
		'CrearCaja' 							: { method: "POST", url: urlCrearCaja},
		'CrearCajaMoneda' 						: { method: "POST", url: urlCrearCajaMoneda},
		'EdicionMultiple' 						: { method: "POST", url: urlEdicionMultiple},
		'CrearCajaUsuario' 						: { method: "POST", url: urlCrearCajaUsuario},
		//'CertificarMovimientosTesoreria' 		: { method: "POST", url: urlCertidicarMovimientosTesoreria},
		'AnularMovimientosTesoreria' 			: { method: "POST", url: urlAnularMovimientosTesoreria},
		'CrearCajaProducto' 					: { method: "POST", url: urlCrearCajaProducto},
		'ModificarMantenedorCaja'				: { method: "POST", url: urlModificarMantenedorCaja},		
		'CrearCuentaBancaria'			    	: { method: "POST", url: urlCrearCuentaBancaria},
		'ModificarCuentaBancaria'				: { method: "POST", url: urlModificarCuentaBancaria},
		'EliminarCaja'							: { method: "POST", url: urlEliminarCaja},
		'CrearChequera'							: { method: "POST", url: urlCrearChequera},
		'EliminarCuentaBancaria'				: { method: "POST", url: urlEliminarCuentaBancaria},
		'ModificarChequera'						: { method: "POST", url: urlModificarChequera},
		'CrearConfiguracionChequera'			: { method: "POST", url: urlCrearConfiguracionChequera},
		'ModificarConfiguracionChequera'		: { method: "POST", url: urlModificarConfiguracionChequera},		
		'ProcesarClearing'						: { method: "POST", url: urlProcesarClearing},
		'RecepcionarNominaTransferencia'		: { method: "POST", url: urlRecepcionarNominaTransferencia},		
		'ValidarComprobantesEgreso'				: { method: "POST", url: urlValidarComprobantesEgreso},
		'AnularNominasTransferencias'			: { method: "POST", url: urlAnularNominasTransferencias},
		'ReprocesarNominasTransferencias'		: { method: "POST", url: urlReprocesarNominasTransferencias},
		'CrearDeposito'							: { method: "POST", url: urlCrearDeposito},
		'CumpleRequisitosCrearPartidaDesdeMov'  : { method: "POST", url: urlCumpleRequisitosCrearPartidaDesdeMov},
		'LiquidarDepositos'						: { method: "POST", url: urlLiquidarDepositos},
		'AnularRegistros'					    : { method: "POST", url: urlAnularRegistros},
		'ConsultaSaldoCuentaBanco'				: { method: "POST", url: urlConsultaSaldoCuentaBanco},		
		'SolicitudGenerarArchivo'				: { method: "POST", url: urlSolicitudGenerarArchivo},
		'ProcesarLiquidacionBilateral'			: { method: "POST", url: urlProcesarLiquidacionBilateral},
		'ModificarIngresoEgreso' 				: { method: "POST", url: urlModificarIngresoEgreso},
		'GestionDeMonedas' 						: { method: "POST", url: urlGestionDeMonedas},
		'CrearCargaMasiva' 						: { method: "POST", url: urlCrearCargaMasiva},
		'AnularDepositos' 						: { method: "POST", url: urlAnularDepositos},
		'EliminarCargaMasiva' 					: { method: "POST", url: urlEliminarCargaMasiva},
		'ProcesarCargaMasiva' 					: { method: "POST", url: urlProcesarCargaMasiva},
		'GestionDeDeposito' 					: { method: "POST", url: urlGestionDeDeposito},		
		'GestionEnvioDocumentos' 				: { method: "POST", url: urlGestionEnvioDocumentos},		
		'CierreDiario' 				            : { method: "POST", url: urlCierreDiario},
		'AnularCheque' 				            : { method: "POST", url: urlAnularCheque},
		'ActualizarTransaccion' 				: { method: "POST", url: urlActualizarTransaccion},
		'CrearTraspaso'							: { method: "POST", url: urlCrearTraspaso},
		'CrearCargaMasivaPac' 					: { method: "POST", url: urlCrearCargaMasivaPac}, 
		'CrearCargaMasivaPacRen' 				: { method: "POST", url: urlCrearCargaMasivaPacRen},
		'IngresarMandatoPAC' 					: { method: "POST", url: urlProcesarMandatoPAC},
		'SolicitudGenerarArchivoPac'			: { method: "POST", url: urlSolicitudGenerarArchivoPac},
		'RevocacionMandatoPac'					: { method: "POST", url: urlRevocacionMandatoPAC},
		'ModificarPAC'							: { method: "POST", url: urlModificarPAC},
		'AnulacionMandatoPAC'					: { method: "POST", url: urlAnulacionMandatoPAC},
		'ValidarTransferenciaDVP'				: { method: "POST", url: urlValidarTransferenciaDVP},
		'GirarComprobantesIngreso'				: { method: "POST", url: urlGirarComprobantesIngreso},
		'GirarComprobantesEgreso'				: { method: "POST", url: urlGirarComprobantesEgreso},
		'AgruparComprobantesIngreso'			: { method: "POST", url: urlAgruparComprobantesIngreso},
		'LiquidacionMasivaComprobante'			: { method: "POST", url: urlLiquidacionMasivaComprobantes},
		'CertificarMovimientos'					: { method: "POST", url: urlCertificarMovimientos},
		'ValidaEjecucionProcesoRecPAC'			: { method: "POST", url: urlValidaEjecucionProcesoRecPAC},
		'EdicionMultipleJava'					: { method: "POST", url: urlEdicionMultipleJava},
		'PreLiquidarComprobantes'				: { method: "POST", url: urlPreLiquidarComprobantes},
		'QuitarDetalleNomina'					: { method: "POST", url: urlQuitarDetalleNominaTransferencia},
		'LiquidarTodosComprobantes'				: { method: "POST", url: urlLiquidarTodosComprobantes},
	});
}]);


