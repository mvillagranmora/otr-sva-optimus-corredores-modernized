angular.module('ModuloPlataforma').directive('bcsSaldoCustodia', function($rootScope,$resource,bcsServicioPlataforma,bcsServicioOrden,bcsComunes) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			filtroFlujoCustodia : '@',	//Flujo Custodia
			idInstrumento 		: '=?',
			usoFlujoCustodia 	: '=?',
			idPortafolio 		: '=?',
			idBoveda 			: '=?',
			fechaSaldo 			: '=?'
		},		
		templateUrl: 'directives/bcs-saldo-custodia/bcs-saldo-custodia.html',
    	link: function(scope, element, attrs, fn) {
    	
    		scope.decimales = util.bcs_formato(0,'GEN','Cantidad',$rootScope.SesionIdMonedaSistema,'','').cantidadDecimales;
    		
    		scope.$watchGroup(['idInstrumento','idPortafolio','fechaSaldo','idBoveda'],function(newval,oldval){
    			
    			if (!scope.idInstrumento || !scope.idPortafolio || !scope.fechaSaldo || !scope.idBoveda || !scope.usoFlujoCustodia)
    				return;
    			    			
    			var datos = {
					p_IdEmpresa 			: $rootScope.SesionIdEmpresa,
					p_IdUsuario 			: $rootScope.SesionIdUsuario,
					p_IdModulo 				: 'PLA',						
					p_FechaSaldo 			: util.dateSPdate(scope.fechaSaldo),
					p_IdPais     			: $rootScope.SesionIdPaisSistema,						
					p_DiasProyeccion 		: 0,
					p_ValorizarFlujo		: 'N',
					p_Uso : [{
						UsoTipoOperacion 			: scope.usoFlujoCustodia,
						IdCliente 					: null,
						IdPortafolio 				: scope.idPortafolio,
						BusquedaEspecifica 			: scope.filtroFlujoCustodia !== null ? scope.filtroFlujoCustodia : '',
						IdTipoInstrumentoEspecifico : '',
					}],
    			
					p_FiltrosBusqueda : [{
						IdClasificacionTipoInstrumento : null,
						IdTipoInstrumento : null,
						IdInstrumento : scope.idInstrumento,
						IdEmisor : null,
						FechaVencimiento : null,
						IdTipoReajuste : null,
						EsFungible : null,
						IdBoveda : scope.idBoveda || null,

					}],
					
					p_EsAgrupadoPor : [{
						ClasificacionTipoInstrumento	: null,
						TipoInstrumento					: null,
						Instrumento						: 'S',
						Emisor							: null,
						Nemotecnico						: null,
						NemoSVS							: null,
						FechaEmisionInstrumento			: null,
						FechaVencimientoInstrumento		: null,
						FechaLiquidacionInstrumento		: null,
						TipoReajuste					: null,
						EsFungible						: null,
						CodigoIFRS						: null,
						Boveda							: null,
						Bolsa							: null,
						TipoOperacion					: null,
						Mesa							: null,
						FolioRegistro					: null,
						ItemOrigen						: null	
					}]
				}
    
				bcsServicioPlataforma.ConsultarFlujoCustodia(datos).then(function(result){
					if (result.p_out_FlujoTotalizado[0] !== undefined) {
						scope.saldoCustodia =  Number(result.p_out_FlujoTotalizado[0].Cantidad)
					}
					
					
				},function(result){
					console.log(result);
				});
    		});
    		
    	}
	}
});
