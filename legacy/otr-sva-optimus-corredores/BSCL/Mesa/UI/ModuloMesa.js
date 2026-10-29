angular.module('ModuloMesa', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

angular.module('ModuloMesa').factory('bcsConsultasMesa',function() {
	var url_base = "/BSCL/Plataforma/GWJAVA/PLA/call?t=";
		var consultas = {
				//CarteraConsolidada : 1044, 
				CarteraReservada : 1045,
				CarteraInstrumento : 1230,
				CarteraDisponibleInstrumento : 1231,
				ReportePatrimonio : 700023,
				ConsultarCanasta : 503,
				ConsultarFactoresCanasta : 504,
				ConsultarMatrizRiesgo : 509,
				ConsultarConfiguracionMesa : 432,
				ConsultaMovimientosVencidosFWD : 539,
				
				ConsultarOperacionesMesaRF : 1450,
				ConsultarDetalleOperacionMesaRF: 1477,
				ConsultarCarteraOperacionMesaRF: 12323, 
				ConsultarComprobantePagoOperacionMesaRF : 700723,
                ConsultarComprobantePagoOperacionMesaSpot : 3567,				
				ConsultarFormasPagoOperacion :3603,
				ConsultarMovimientosSpotOtros : 700147,
				
				SocioNegocio : 1051,
				MantenedorItem : 12079,
				ConsultarCierreMesa : 452,
				ConsultarMargenes : 618,
				ConsultarFormaDePago : 1769,//usado en el combo.
				ConsultarFormaDePagoPortafolio : 1785,
				ConsultarPreferenciaPortafolio : 1784,
				ConsultarPreferenciaPago : 1660,
				ConsultarOperacionesMesaRFNew : 4290644,
				ConsultarCarteraDisponibleInstrumentoMesaRF:4300004,
                ConsultarPreferenciaPagoPortafolioCliente: 44290682,
                ConsultarPreferenciaPagoPortafolioCorredor: 44290683,
                ConsultarClienteCuentaPropia: 1275
				
		}
		return function(nombreConsulta) {
			return "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + consultas[nombreConsulta];
		};
});

angular.module('ModuloMesa').factory('bcsServiciosMesa',['$resource',function($resource) {
		
		var generarResource = function() {
			var r = {};
			$.each(resources,function(key,value) {
				r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id};
				
			});
			return r;
		};
		
		var resources = [
			{ nombre : 'EjecutarVencimientosPautaMesa', id : "20002" },
            { nombre : 'CerrarFuncionMesaRF', id : "352" },
            { nombre : 'IngresarReservaCartera', id : "395" },
            { nombre : 'ConsultarReservaCartera', id : "1045" },
            { nombre : 'ConsultarCarteraConsolidada', id : "1044" },
            { nombre : 'ConsultarPosicionMRF', id : "1047" },
            { nombre : 'ConsultarPosicionMRF2', id : "4300011" },
            { nombre : 'ConsultarResumenPosicionMRF', id : "4300012" },
            { nombre : 'ConsultarDetallePosicionMRF', id : "20240718" },
            { nombre : 'AsignacionAutomatica', id : "3135" },
            {nombre  : 'ReportePatrimonio', id : "700023" },
            { nombre : 'ObtenerValores', id : "407" },
            { nombre : 'IngresoForward', id : "410" },
            { nombre : 'IngresoSpot', id : "411" },
            { nombre : 'ConsultarPuntas', id : "415" },
            { nombre : 'ConsultarPuntasMantenedor', id : "3595" },
            { nombre : 'CrearMonedaPorProducto', id : "424" },
            { nombre : 'CrearPuntasSpot', id : "426" },
            { nombre : 'CrearPuntasTramo', id : "428" },
            { nombre : 'EditarEstadoMonedaPorProducto', id : "700418" },
            { nombre : 'ConsultarPuntasPorTramo', id : "430" },
            { nombre : 'ConsultarConfiguracionUsuario', id : "438" },
            { nombre : 'ConsultarConfiguracionCliente', id : "440" },
            { nombre : 'ModificarFechaMovimiento', id : "474" },
            { nombre : 'ConsultarResultadoSpot', id : "441" },
            { nombre : 'CrearOperacionMesaRF', id : "1418" },
            { nombre : 'CrearFormaDePagoOperacionMesaRF', id : "4300008" },
            { nombre : 'EliminarFormaDePagoOperacionMesaRF', id : "4306000" },
            { nombre : 'ModificarFormasDePagoMesa', id : "700172" },
            { nombre : 'AnularOperacionesMesaRF', id : "1735" },
            { nombre : 'ActualizarPactoOperacionesMesaRF', id : "700157" },
            { nombre : 'AnticiparOperacionesMesaRF', id : "1742" },
            { nombre : 'ReverzarAnticipoOperacionesMesaRF', id : "700731" },
            { nombre : 'CrearOperacionPendienteMesaRF',id : "1462" },
            { nombre : 'ObtenerParidadTipoCambio',id : "3155" },
            { nombre : 'MarcarOperacion',id : "627" },
            { nombre : 'ValidarDatosOperacion',id : "101010132" },
            { nombre : 'ConsultarOperacionesMesaRF',id : "4173"},
            { nombre : 'VencimientoPactos',id : "20002"},
            {nombre : 'GenerarProcesoBatch', id : "20043"},
            { nombre : 'CrearCanasta', id : "505" },
            { nombre : 'CrearFactorCanasta', id : "506" },
            { nombre : 'EliminarCanasta', id : "507" },
            { nombre : 'EliminarFactorCanasta', id : "508" },
            { nombre : 'CrearMatrizRiesgo', id : "510" },
            { nombre : 'GrabarCalces',id : "519" },
            { nombre : 'DescalceOperacionForward',id : "522" },
            { nombre : 'ConsultarPosicionPorMoneda',id : "523" },
            { nombre : 'AnticipoForward',id : "534" },
            { nombre : 'VencimientoForward',id : "537" },
            { nombre : 'MovimientoTesoreríaForward',id : "538" },
            { nombre : 'AnulacionForward',id : "540" },
            { nombre : 'ModificacionForward',id : "541" },
            { nombre : 'ReversaAnticipoForward',id : "543" },
            { nombre : 'ConsultarParidadYTipoCambio', id : "544" },
            { nombre : 'ModificarParidadYTipoCambio', id : "546" },
            { nombre : 'DevengoForward', id : "548" },
            { nombre : 'FechaVencimiento', id : "551" },
            { nombre : 'ValorizacionForward', id : "559" },
            
            { nombre : 'ValidaArchivoMasivoRF',	id : "3160" },            
            { nombre : 'ConsultarCargaMasivaOperacionesRF', id : "3164" },
            { nombre : 'GrabaCargaMasivaOperacionesRF',	id : "3165" },
            { nombre : 'ProcesarCargaMasivaOperacionesRF',	id : "3175" },            
            
            { nombre : 'ObtenerDatosDesdeArchivoDAP',	id : "436" },
            { nombre : 'GrabarArchivoCargaDAP',			id : "444" },
            { nombre : 'ProcesarArchivoCargaDAP',		id : "445" },
            { nombre : 'ModificarEstadoArchivoCargaDAP',id : "446" },
            { nombre : 'ConsultarConfiguracionMesa',id : "511" },
            { nombre : 'ConsultarPosicionInicial',id : "403" },
            { nombre : 'CrearPosicionInicial',id : "448" },
            { nombre : 'ConsultaMovimientosVencidosFDW',id : "516" },
            { nombre : 'ConsultarPosicion',id : "518" },
            { nombre : 'CierreAperturaMesa',id : "453" },
            { nombre : 'CrearMesa',			id : "456" },
            { nombre : 'ModificarMesa',		id : "457" },
            { nombre : 'CrearAgenteOperadorMesa',		id : "459" },
            { nombre : 'ModificarAgenteOperadorMesa',	id : "460" },
            { nombre : 'ObtenerValorIndicador',			id : "404" },
            { nombre : 'ConsultarItemsManualesPosicionMRF',id : "466" },
            { nombre : 'GrabarItemsManualesPosicionMRF',id : "467" },
            { nombre : 'CrearMetodoPosicionMRF',		id : "475" },
            { nombre : 'ModificarMetodoPosicionMRF',	id : "476" },
            { nombre : 'CrearTipoOperacionPosicion',	id : "477" },
            { nombre : 'ModificarTipoOperacionPosicion',id : "478" },
            { nombre : 'CrearEventoPosicion',			id : "479" },
            { nombre : 'ModificarEventoPosicion',		id : "480" },
            { nombre : 'CrearMetodoEventoPosicion',		id : "481" },
            { nombre : 'ModificarMetodoEventoPosicion',	id : "482" },
            { nombre : 'ValidarPrecioCierre',			id : "487" },
            { nombre : 'ConsultarProductoPortafolioMesa',id : "488" },
            { nombre : 'ResultadoContableSpot',			id : "489" },
            { nombre : 'ModificarSpot',	id : "535" },
            { nombre : 'AnularSpot',	id : "536" },
            { nombre : 'ConsultaOMA',	id : "545" },
            { nombre : 'GenerarArchivoOMA',	id : "547" },
            { nombre : 'GenerarArchivoCarteraPropia',	id : "900205" },
            { nombre : 'ModificarCodigoComercio',	id : "549" },
            { nombre : 'GenerarArchivoAnexoIX',	id : "568" },
            { nombre : 'ConsultarQuincenalDePactos',	id : "573" },
            { nombre : 'ModificarQuincenalDePactos',	id : "574" },
            { nombre : 'GenerarArchivoPAccmmdd',	id : "581" },
            { nombre : 'GenerarArchivoCCccmmdd',	id : "582" },
            { nombre : 'InsertarQuincenalDePactos',	id : "583" },
            
            { nombre : 'MantenedorItem',	id : "12079" },
            { nombre : 'IngresarCuentaContableItem',	id : "13003" },
            { nombre : 'LiquidacionSpot',	id : "3602" },
            { nombre : 'ConsultarOperacionesFWRF', id : "700148" },
            { nombre : 'ConfirmarOperacionesFWRF', id : "700150" },
            { nombre : 'RechazoLiquidacionSpot', id : "3606" },
            { nombre : 'ConfirmaLiquidacionSpot', id : "3604" },
            { nombre : 'GeneraArchivoDCV',	id : "4160" },
            { nombre : 'CambioEstadoOperacionPendiente',	id : "4166" },            
            
            { nombre : 'ConsultarTasaMercado',	id : "4210" },
            { nombre : 'BovedasPortafolio',	id : "1263" },
            
            { nombre : 'CrearModificarFormaPagoMesa',	id : "13057" },
            { nombre : 'ValidarClienteProductoMesa',    id : "21026" },
            { nombre : 'ModificarSpotFondoMutuo',    id : "300007" },
            
            { nombre : 'IngresoDatatec',id : "517" },            
            { nombre : 'ReprocesoDatatec',	id : "524" },
            { nombre : 'BloquearDATATEC',id : "601000" },
            { nombre : 'MarcarOperacionMandato',id : "601156" },
            { nombre : 'ConsultarMoneda',id : "398"},
            { nombre : 'NotificarCartaLiquidacion',id : "700366"},
            { nombre : 'EjecutarVencimientoPactos',id : "4290720"},
            { nombre : 'IngresarDetalleOperacionMesaRF', id : "44290670" },
            { nombre : 'EliminaReserva', id : "44290681" },
			{ nombre : 'CargaMasivaArchivoOperacionTermino', id : "900207" },
			{ nombre : 'EliminarRegistroCargaMasivaOperacionesTermino', id : "900212" },
			{ nombre : 'ProcesarCargaMasivaArchivoOperacionTermino', id : "900213" },

            { nombre : 'ValidarReasignacionMesaRF', id : "701" },
            { nombre : 'ProcesarReasignacionMesaRF', id : "702" },
            
            { nombre : 'CrearSolicitudPuntas', id : "44290885" },
            { nombre : 'AprobarRechazarSolicitudPuntas', id : "44290886"},
            
            { nombre : 'GuardaParidadYTipoCambioValorPorDefecto', id : "20230303"},
            { nombre : 'ConsultaParidadYTipoCambioValorPorDefecto', id : "20230304"},

            { nombre : 'ConsultarPosicionResumenMRF', id : "1048"},

            { nombre : 'ValorizacionValoresAdicionales', id : "55669988"},            
            { nombre : 'ConsultarCarteraPactosValorizada', id : "101010133"},
            { nombre : 'GenerarArchivoInformeCarteraPacto', id: "101010135"},
	        { nombre : 'ConsultarCarteraPropiaValorizada', id : "101010138"},
            { nombre : 'ConsultarValorizacionPatrimonioMRF', id : "101010140"},
            { nombre : 'ModificarEstadoCajaOperacionSpot', id : "101010402"},
            
  		];
					
 		return $resource("", {},generarResource());
}]);
angular.module('ModuloMesa').directive('bcsResultadoOperaciones', function() {
	return {
		restrict: 'E',
		scope : {
			resultadoPosicionInicial : '=?',
			resultadoPosicionInicial : '=?',
			resultadoInterbancario : '=?',
			resultadoDistribucion : '=?',
			resultadoArbitrajeInterbancario : '=?',
			resultadoArbitrajeDistribucion : '=?',
			resultadoTotal : '=?',
            idMoneda : '=?',
		},
		templateUrl: '../../Mesa/UI/SPT/ResultadoOperaciones/bcsResultadoOperaciones.html',
}});
angular.module('ModuloMesa').directive('bcsPosicion', function() {
	return {
		restrict: 'E',
		scope : {
			hora : '=?',
			tipoCambio :'=?',
			interbancario :'=?',
			arbitrajeInterbancario :'=?',
			distribucion :'=?',
			arbitrajeDistribucion :'=?',
			fwd :'=?',
			consolidadoSpot :'=?',
			resumenOperacionesSpot :'=?',
			idMoneda : '=?',
            idMonedaEquivalente : '=?',
			valorBreakeven : '=?'
		},
		templateUrl: '../../Mesa/UI/SPT/Posicion/bcsPosicion.html',
}});



angular.module('ModuloMesa').directive('fileReaderMesa', function() {
	return {
		//restrict: 'A',
		scope: {
			fileReaderDatatec:"=",
			fileNameDatatec:"="
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
angular.module('ModuloMesa').factory('bcsServiciosCargaMasivaOperacionSpot',['$resource',function($resource) {
    UrlCrearCargaMasivaOperacionSpot			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=1854";
    UrlAnularRegistroCargaMasivaOperacionSpot	=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=1855";
    UrlProcesarCargaMasivaOperacionSpot			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=1856";
    UrlProcesarCargaMasivaOperacionSpotReporte	= 	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3207";
return $resource("", {},
{
'CrearCargaMasivaOperacionSpot'			 :	{ method: "POST", url: UrlCrearCargaMasivaOperacionSpot},
'ProcesarCargaMasivaOperacionSpot'		 :	{ method: "POST", url: UrlProcesarCargaMasivaOperacionSpot},
'AnularRegistroCargaMasivaOperacionSpot' :	{ method: "POST", url: UrlAnularRegistroCargaMasivaOperacionSpot},
'ProcesarCargaMasivaOperacionSpotReporte':	{ method: "POST", url: UrlProcesarCargaMasivaOperacionSpotReporte},
});
}]);
angular.module('ModuloMesa').factory('bcsDinamicosMesa',function() {
	var obj = {};	
	obj.DynPuntas = {
			UpdatePuntas: false
		};
	return obj;
});
