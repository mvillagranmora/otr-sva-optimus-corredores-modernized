angular.module('ModuloAPV', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

angular.module('ModuloAPV').factory('bcsConsultasAPV',function() {
	var url_base = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=";
		var consultas = {
				
				ConsultarDistribucionInversion				: 6010,
				ConsultarCargaDetalleRecaudacion			: 6120,
				ConsultarGestionContratosSAAPV 				: 6004,
				ConsultarFolioContratoSAAPV 				: 6002,
				ConsultarGestionCargaMasiva					: 6013,
				ConsultarRecaudacion						: 6016,
				ConsultarEmpleadorRecaudacion				: 6021,
				ConsultarClienteRecaudacion					: 6022,
				ConsultarRecaudadoDistribuido   			: 6024,
				ConsultarContratoPortafolio					: 6042,
				ConsultarPortafolioMatrizAgrupador 			: 6019,
				ConsultarTraspasos							: 6047,
				ConsultarContratoSAAPV						: 6048,
				ConsultarDetalleTraspasos					: 6050,
				ConsultarTrabajadorSAAPV					: 6055,
				ConsultarEmpleadorSAAPV						: 6056,
				ConsultarPortafolioMatrizAgrupadorDetalle 	: 6059,
				ConsultarOrigenTraspaso						: 6070,
				ConsultarConfiguracionRetencionTraspaso		: 6077,
				ConsultarBonificacion						: 6080,
				ConsultarClientePortafolioSAAPV				: 4290633,
				ConsultarClienteContratoPortafolioSAAPV		: 4290634,
				ConsultarFolioRecaudacionConciliacionMasiva	: 700427,
				ConsultaDetalleTraspasosEgresos				: 55670001
		}
		return function(nombreConsulta) {
			return "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + consultas[nombreConsulta];
		};
});

angular.module('ModuloAPV').factory('bcsConfiguracionAPV',function() {
	var obj = {};
	var CONSTANTES = {
			IdTipoCanalDistribucionTrader	: 'RUT',
			IdTipoCanalDistribucionAgente	: 'RUA',
			IdTipoCanalDistribucionRetail	: 'RUR',
			IdTipoCanalDistribucionManual	: 'MAN',
			IdSistemaBursatilIngresoManual	: 'MANUAL',
			
			//tipo vigencia
			IdTipoVigenciaSoloHoy			: 'SH',
			IdTipoVigenciaPermanente		: 'OP',
			IdTipoVigenciaSemanal			: 'SE',
			IdTipoVigenciaFechaFija			: 'FF',
				
			//tipo precio
			IdTipoPrecioMercado				: 'M',
			IdTipoPrecioLimite				: 'L'
	}

	obj.CONSTANTES = CONSTANTES;
	return obj;
});

angular.module('ModuloAPV').directive('fileReaderAPV', function() {
	return {
		//restrict: 'A',
		scope: {
			fileReaderPrevired:"=",
			fileNamePrevired:"="
		},
		link: function(scope, element) {
			$(element).on('change', function(changeEvent) {
				var files = changeEvent.target.files;
				if (files.length) {
					var r = new FileReader();
					r.onload = function(e) {
						var contents = e.target.result;
						scope.$apply(function () {
							scope.fileReader = contents;
							scope.fileName = files[0].name;
						});
					};
					r.readAsText(files[0],'iso-8859-1');
				}
			});
		}
	};
});


angular.module('ModuloAPV').factory('bcsServiciosAPV',['$resource',function($resource) {
	
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&a=" + value.nombre  + "&t="+value.id  };
			
		});
		return r;
	};		                 
	
	var resources = [
			{ nombre : 'ConsultarFechaSolicitudAporteRescate'	, id : "3022" },
			{ nombre : 'ConsultarDistribucionInversion'			, id : "6010" },
			{ nombre : 'CambiarEstadoContratosSAAPV'			, id : "6009" },
			{ nombre : 'CrearContratoSAAPV'						, id : "6006" },
			{ nombre : 'ValidaContratoSAAPV'					, id : "6011" },
			{ nombre : 'CargaMasivaArchivo'						, id : "6014" },
			{ nombre : 'CrearRecaudacionDeposito'   			, id : "6015" },
			{ nombre : 'CargaRecaudacion'						, id : "6018" },
			{ nombre : 'CrearRecaudacionEmpleador'				, id : "6028" },
			{ nombre : 'CrearRecaudacionCliente'				, id : "6029" },
			{ nombre : 'ConsultarTotalesDistRec'				, id : "6030" },
			{ nombre : 'ConciliarDeposito'						, id : "6033" },
			{ nombre : 'ConsultarTotalesOrigen'					, id : "6036" },
			{ nombre : 'ProcesarDistribucion'					, id : "6037" },
			{ nombre : 'AsignarDepositoEmpleador'				, id : "6038" },
			{ nombre : 'CargaMasivaPrevired'					, id : "6039" },
			{ nombre : 'CargaMasivaRecaudacion'					, id : "6114" },
			{ nombre : 'CargaMasivaBanco'						, id : "6043" },
			{ nombre : 'ConsultarSaldoCtaInvCliente'    		, id : "6045" },
			{ nombre : 'CargaMasivaPAC'				    		, id : "6046" },
			{ nombre : 'CrearSolicitudAporteRescateNormal'		, id : "357"  },
			{ nombre : 'CalcularMontoNumeroCuota'				, id : "3137" },
			{ nombre : 'GrabarContratoTraspaso'					, id : "6064" },
			{ nombre : 'GrabarTraspasos'						, id : "6051" },
			{ nombre : 'GrabarRecaudacionTraspaso'				, id : "6052" },
			{ nombre : 'AsignarContratoARecaudacion'			, id : "6053" },
			{ nombre : 'ConsultarCarteraAPV'					, id : "6054" },
			{ nombre : 'ModificarContratoSAAPV'					, id : "6057" },
			{ nombre : 'CalculoMontosDesinversion'				, id : "6058" },
			{ nombre : 'ConsultarCustodiaPortafolioAPV'			, id : "6060" },
			{ nombre : 'DevolucionPagoRezagos'					, id : "6061" },
			{ nombre : 'CrearTraspasoTesoreria'					, id : "6065" },
			{ nombre : 'ConsultarGestionContratosSAAPV'			, id : "6066" },
			{ nombre : 'EliminarRegistrosRecaudacion'			, id : "6072" },
			{ nombre : 'ModificarPortafolioTraspaso'			, id : "6078" },
			{ nombre : 'CargaBonificacionTGR'					, id : "6079" },
			{ nombre : 'ProcesarBonificacion'					, id : "6082" },
			{ nombre : 'EjecutarBonificacion'					, id : "6081" },
			{ nombre : 'ModificarBonificacion'					, id : "6085" },
			{ nombre : 'CrearOrdenTraspaso'						, id : "6086" },
			{ nombre : 'ModificarOrigenDestino'					, id : "6089" },
			{ nombre : 'CrearTraspaso'							, id : "6091" },
			{ nombre : 'ConsultarCuentaInversionOrdenTraspaso'	, id : "6094" },
			{ nombre : 'CambiarEstadoTraspaso'					, id : "6095" },
			{ nombre : 'CalculoRetencionTraspasos'				, id : "6098" },
			{ nombre : 'ObtenerParidadTipoCambio'				, id : "3155" },
			{ nombre : 'ConsultarPreferenciaPagoPortafolio'		, id : "6101" },
			{ nombre : 'CargaRegistroHistoricoTraspaso'			, id : "6103" },
			{ nombre : 'CargaMasivaSAAPVPAC'					, id : "6104" },
			{ nombre : 'ConsultarClienteContratoSAAPV'			, id : "6106" },
			{ nombre : 'CambiarAPlanActivoContratoSAAPV'		, id : "6107" },
			{ nombre : 'AsociarPerfilesAPV'						, id : "6108" },
			{ nombre : 'NotificacionNominaTraspasoEgreso'		, id : "6111" },
			{ nombre : 'CambiarEstadoEnvioNotificacion'			, id : "700141" },
			{ nombre : 'CrearBancoPrevired'						, id : "700182" },
			{ nombre : 'ModificarBancoPrevired'					, id : "700183" },
			{ nombre : 'EliminarBancoPrevired'					, id : "700184" },
			{ nombre : "IngresoRecaudacionTraspaso"				, id : "700194"},
			{ nombre : 'ValidaEjecucionProcesoRecPAC'			, id : "4290625" },
			{ nombre : 'ConsultarClientePortafolioSAAPV'		, id : "4290633" },
			{ nombre : 'ConsultarClienteContratoPortafolioSAAPV', id : "4290634"},
			{ nombre : 'CambiarAPlanActivoContratoSAAPVWrapper' , id : "4290635"},
			{ nombre : 'AnularTraspasosEgresosMasivo'			, id : "55669994" },
			{ nombre : 'ValidarYAsignarContratosARecaudaciones'	, id : "700378"},
			{ nombre : 'ReaperturarTraspaso'			        , id : "6200"},
			{ nombre : 'ProcesarCargaMasivaRecaudacion'	        , id : "6121"}
		];
				
	return $resource("", {},generarResource());
}]);   
