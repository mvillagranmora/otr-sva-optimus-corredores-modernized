angular.module('ModuloFFM').directive('bcsComisionSolicitudFondo', function($rootScope,$resource,bcsServicioFondosMutuos) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			decimales			: '=?',
			idSerieFondo 		: '=?',
			idInstrumento 		: '=?',
			idCliente			: '=?',
			idPortafolio 		: '=?',
			idMoneda			: '=?',
			idMonedaOperacion 	: '=?',
			tipoSolicitud		: '=?',
			cantidad 			: '=?',
			tipoMovimiento		: '=?',
			fechaValorCuota		: '=?',
			fechaCurse			: '=?',
			esTraspaso			: '=?',
			comision			: '=?'
		},		
		templateUrl: '../../FondoMutuo/UI/bcs-comision-solicitud-fondo/bcs-comision-solicitud-fondo.html',
    	link: function(scope, element, attrs, fn) {
 			
			scope.$watchGroup(['idSerieFondo','idInstrumento','idCliente','idPortafolio','cantidad'],function(newval,oldval){
    			
    			if (!scope.idSerieFondo || !scope.idInstrumento || !scope.idCliente || !scope.idPortafolio || !scope.cantidad || !scope.idMoneda || !scope.tipoSolicitud || !scope.idMonedaOperacion){
    				scope.comision = 0;
    				return;
    			}
    			var fechaCurseValida = scope.fechaCurse ? util.dateInicioDia(scope.fechaCurse) : null;

    			var datos = {
    				p_IdEmpresa 		: $rootScope.SesionIdEmpresa, 
    				p_IdUsuario     	: $rootScope.SesionIdUsuario,
    				p_IdModulo     		: 'FMM',
    				p_SimularOperacion	: 'N',
    				p_UsaReglasNegocio	: 'S',
    				p_DatosCustodia 	: [{
    					IdInterno : 1,
    					IdSerieFondo : scope.idSerieFondo,
						IdInstrumento : scope.idInstrumento,
						IdCliente : scope.idCliente,
						IdPortafolio : scope.idPortafolio,
						Cantidad : scope.cantidad,
						IdMonedaFondo: scope.idMoneda,
						IdMonedaOperacion: scope.idMonedaOperacion,
						TipoSolicitud : scope.tipoSolicitud,
						IdTipoMovimientoFondoMutuo : scope.tipoMovimiento,
						FechaValorCuota : util.dateSPdate(util.dateInicioDia(scope.fechaValorCuota)),						
						FechaCurse : fechaCurseValida ? util.dateSPdate(fechaCurseValida) : null,
						EsTraspaso : scope.esTraspaso
					}],
				}

    			bcsServicioFondosMutuos.CalcularComisiones(datos).$promise.then(function(result){
					scope.comision = 0;
				
					if (scope.decimales === undefined){
						scope.decimales = util.bcs_formato(0,'FMM','Cantidad',scope.idMonedaOperacion,'','RESCFM').cantidadDecimales;
					}else if (scope.decimales !== undefined && scope.idMonedaOperacion ==='CLP'){
						scope.decimales = util.bcs_formato(0,'FMM','Cantidad',scope.idMonedaOperacion,'','RESCFM').cantidadDecimales;
					}
					
					if(result.P_OUT_RESULTADO[0] != undefined && result.P_OUT_ERRORESCONTADOR === 0){		
						scope.comision =util.bcs_formato(result.P_OUT_RESULTADO[0].ComisionLocal,'FMM','Monto',scope.idMonedaOperacion,'','RESCFM').valor;
					}
					
				},function(result){
					console.log(result);
				});
    		});   		
    	}
	}
});
