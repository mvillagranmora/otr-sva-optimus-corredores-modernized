angular.module('ModuloFFM', []);

angular.module('ModuloFFM').factory('bcsServicioFondosMutuos',['$resource',function($resource) {
	
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id};
			
		});
		return r;
	};
	
	var resources = [
	        { nombre : 'CrearSolicitudAporteRescateNormal', id : "357" },
	        { nombre : 'AnularSolicitudAporteRescateMasiva', id: "402"},
	        { nombre : 'ConsultarDiaSiguiente', id:"23"},
	        { nombre : 'CrearInstrumentoFondoMutuoMM', id:"1356"},
	        { nombre : 'ModificarInstrumentoFondoMutuoMM', id:"1357"},
	        { nombre : 'ModificarHorarioCierreDefault', id:"1409"},
	        { nombre : 'ModificarHorarioCierreInstrumento', id:"1411"},
	        { nombre : 'AdministrarComision', id:"1428"},
	        { nombre : 'CrearSolicitudAporteOvernight', id:"1431"},
	        { nombre : 'CrearSolicitudAporteRescateProgramado', id:"1434"},
	        { nombre : 'CrearSolicitudRescateReinversion', id:"1436"},
	        
	       
	        { nombre : 'CrearSolicitudRescateReinversion1N', id:"2902"},
	        
	        
	        { nombre : 'CrearSolicitudAporteReinversion', id:"1442"},
	        { nombre : 'CrearSolicitudRescateTraspaso', id:"1437"},
	        { nombre : 'CrearSolicitudRescateTransferencia', id:"1438"},
	        { nombre : 'CrearSolicitudCambioSerie', id:"1456"},
	        { nombre : 'CertificarSolicitudAporteRescate', id:"1490"},
	        { nombre : 'LiquidacionSolicitudAporteRescate', id:"1689"},
	        { nombre : 'ModificarBloqueoInstrumento', id:"1710"},
	        { nombre : 'ModificarAperturaCierreInstrumento', id:"1711"},
	        { nombre : 'CrearFamiliaFondoMutuo', id:"1713"},
	        { nombre : 'AdministrarPerfilFondoMutuo', id:"1714"},
	        { nombre : 'ModificarFamiliaFondoMutuo', id:"1716"},
	        { nombre : 'ConsultarDetalleSolicitud', id:"3013"},
	        { nombre : 'CalcularComisiones', id:"3016"},
	        { nombre : 'ConsultarFechaSolicitudAporteRescate', id:"3022"},
	        { nombre : 'CargarValorCuota', id:"3035"},
	        { nombre : 'CargarValorTipoCambio', id:"3036"},
	        { nombre : 'CargarSolicitudesAporteRescate', id:"3044"},
	        { nombre : 'AnulacionCargaSolicitudAporteRescate', id:"3058"},
	        { nombre : 'CrearProgramaCargaBatch', id:"3109"},
	        { nombre : 'CrearProgramaCertificacionBatch', id:"3112"},
	        { nombre : 'CambiarEstadoSolicitudAgf', id:"3124"},
	        { nombre : 'CalcularMontoNumeroCuota', id:"3137"},
	        { nombre : 'ConsultarFlujoCustodia', id:"3143"},
	        { nombre : 'CrearProgramarGeneraArchivoOperacionesAGF', id:"3146"},
	        { nombre : 'ConfigurarFondoAdministracionLiquidez', id:"3147"},
	        { nombre : 'ConfigurarPortafolioAdministracionLiquidez', id:"3148"},
	        { nombre : 'AperturaAutomaticoFondos', id:"3156"},
	        { nombre : 'CertificarPorFiltrosSolicitudAporteRescate', id:"3163"},
	        { nombre : 'ReaperturaFondos', id:"3166"},
	        { nombre : 'ModificarRegimenFondoMutuo', id: "3228"},
	        { nombre : 'ConsultarRequisitosBasicosCrearSolicitudAporteRescate', id:'3568'},
	        { nombre : 'CrearIngresoRetiroTituloFondoMutuo', id:'13055'},
	        { nombre : 'CrearIngresoRetiroGarantiasFondoMutuo', id:'13056'},
	        { nombre : 'SimularCertificarAporteRescate', id : "3607" },
	        { nombre : 'InscribirProcesoBatch', id:'20043'},
	        { nombre : 'CargarCustodiaFondoMutuo', id:'600011'},
	        { nombre : 'CargarArchivoCuadraturaNacionalUI', id:'600013'},
	        { nombre : 'CargarArchivoCuadraturaInternacionalUI', id:'601124'},
	        { nombre : 'GenerarDataReporteTRXI', id:'600016'},
	        { nombre : 'AutorizarOmnibus', id:'600019'},
	        { nombre : 'CrearTraspaso', id:"300013"},
	        { nombre : 'ModificarTraspaso', id:"300017"},
	        { nombre : 'AnularTraspaso', id:"300019"},
	        { nombre : 'ConfirmarTraspaso', id:"300020"},
	        { nombre : 'RecaudacionTraspaso', id:"300021"},
	        { nombre : 'RevalorizarMovimientosFFMM', id:"600080"},
	        { nombre : 'IngresarCSC', id:"600081"},
	        { nombre : 'ModificarCSC', id:"601113"},
	        { nombre : 'RevocacionContratoCSC', id:"601139"},
	        { nombre : 'AnulacionContratoCSC', id:"601108"},
	        { nombre : 'AnulacionSolicitudAporteRescateFolioContrato', id:"601125"},
		{ nombre : 'CrearCargaMasivaPacRen', id:"601103"},
	        { nombre : 'ConsultarValorDolarFondoMutuo', id:"601158"},
			{ nombre : 'ConsultarValidacionDJ1932PorAgnoComercial', id:"700386"},
			{ nombre : 'ProcesarComisionesNN', id:"700394"},
			{ nombre : 'ActualizarFechaGestionOperaciones', id:"700400"},
			{ nombre : 'HistorialCambiosModificarFechas', id:"700402"},
	        
	        { nombre : 'ConsultarFondo', id:"4290611"},
	        { nombre : 'CrearSolicitudAportePorDividendosCFI', id:"700195"},
	        { nombre : 'ValidaEjecucionProcesoRecPAC', id:"4290625"},
			{ nombre : 'SimulaCalculoSpreadRescate', id:"4290650"},
			{ nombre : 'ConsultarFondosIndustria', id:"900200"},
			{ nombre : 'ActualizaFondosIndustria', id:"900103"},
			{ nombre : 'ConsultarValidacionDJ1932PorAgnoComercial', id:"700386"},
			{ nombre : 'ProcesarComisionesNN', id:"700394"},
			{ nombre : 'AdministrarComisiones', id:"700411"},
	       
	     ];
	return $resource("", {},generarResource());
}]); 