angular.module('ModuloFFM').directive('bcsSaldoCustodiaFondo', function($rootScope,$resource,bcsServicioFondosMutuos) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			modo : '@',
			decimales : '=?',
			decimalesCantidad : '=?',
			idInstrumento : '=?',
			idRegimen : '=?',
			idCliente : '=?',
			idPortafolio : '=?',
			idBoveda : '=?',
			fechaSaldo : '=?',
			idMoneda : '=?',
			saldoCuotas : '=?',
			saldoMonto  : '=?'
		},		
		templateUrl: '../../FondoMutuo/UI/bcs-saldo-custodia-fondo/bcs-saldo-custodia-fondo.html',
    	link: function(scope, element, attrs, fn) {
 			
    		//por defecto se llama a flujo de instrumento
    		if (!scope.modo) {
    		
    			scope.$watchGroup(['idInstrumento','idCliente','idPortafolio','fechaSaldo','idBoveda','idRegimen'],function(newval,oldval){
	    			
	    			if (!scope.idInstrumento || !scope.idCliente || !scope.idPortafolio || !scope.fechaSaldo || !scope.idBoveda || !scope.idMoneda)
	    				return;
	    			
	    			var datos = {
						p_IdEmpresa 	: $rootScope.SesionIdEmpresa,
						p_IdUsuario		: $rootScope.SesionIdUsuario,
						p_IdModulo		: 'FMM',
						p_IdPais     	: $rootScope.SesionIdPaisSistema,
						p_Instrumentos 	: [{
							IdInstrumento : scope.idInstrumento,
							IdRegimenFondoMutuo : scope.idRegimen
						}],
						p_FechaSaldo : util.dateSPdate(scope.fechaSaldo),
						p_DiasProyeccion : 0,
						p_Carteras : [{
							IdCliente : scope.idCliente,
							IdPortafolio : scope.idPortafolio,
						}],
					}
	
	    			bcsServicioFondosMutuos.ConsultarFlujoCustodia(datos).$promise.then(function(result){
						scope.saldoCuotas = 0;
						scope.saldoMonto = 0;
						if (scope.decimales === undefined)
							scope.decimales = util.bcs_formato(0,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'','').cantidadDecimales;
						if (scope.decimalesCantidad === undefined)
							scope.decimalesCantidad = util.bcs_formato(0,'FMM','Cantidad',$rootScope.SesionIdMonedaSistema,'','').cantidadDecimales;
						$.each(result.P_OUT_FLUJOFONDOMUTUOTOTALIZADO,function(key,value){
						
							// Ahora para los rescates totales tambien se especifica el Regimen tributario (18T, 18Q, etc)
							scope.saldoCuotas = util.bcs_formato(value.CantidadDisponible,'FMM','Cantidad',$rootScope.SesionIdMonedaSistema,'','').valorNumerico;
							scope.saldoMonto =  Number(util.truncarNumero(value.MontoDisponible,scope.decimales));
						
						});
					},function(result){
						console.log(result);
					});
	    		});
    		}    		
    	}
	}
});
