angular.module('ModuloFFM').directive('bcsDolarObservadoSerieFondo', function($rootScope,$resource,bcsServicioFondosMutuos) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			idSerieFondo : '=?',
			idTipoSolicitud : '=?',
			fecha : '=?',
			dolarObservadoSerieFondo : '=?'
		},		
		templateUrl: '../../FondoMutuo/UI/bcs-dolar-observado-serie-fondo/bcs-dolar-observado-serie-fondo.html',
    	link: function(scope, element, attrs, fn) {
    		
			scope.$watchGroup(['idSerieFondo','idTipoSolicitud','fecha'],function(newval,oldval){
    			
    			if (!scope.idSerieFondo || !scope.idTipoSolicitud || !scope.fecha)
    				return;
    			
    			var datos = {
					p_IdEmpresa 	: $rootScope.SesionIdEmpresa,
					p_IdUsuario		: $rootScope.SesionIdUsuario,
					p_IdModulo		: 'FMM',
					p_Series 		: [{
						IdSerieFondo : scope.idSerieFondo,
						IdTipoSolicitud : scope.idTipoSolicitud,
						Fecha : util.dateSPdate(scope.fecha)
					}]
				}

    			bcsServicioFondosMutuos.ConsultarValorDolarFondoMutuo(datos).$promise.then(function(result){
					scope.dolarObservadoSerieFondo = 0;
					if(result.P_OUT_RESULTADO[0]){
						scope.dolarObservadoSerieFondo = util.bcs_formato(result.P_OUT_RESULTADO[0].ValorParidad,'GEN','Monto','USD','','').valor;
					}
				},function(result){
					console.log(result);
				});
    		});  		
    	}
	}
});