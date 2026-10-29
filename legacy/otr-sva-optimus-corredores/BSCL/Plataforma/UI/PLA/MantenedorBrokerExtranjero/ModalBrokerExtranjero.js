angular.module('ModuloPlataforma').controller('ModalBrokerExtranjeroCtrl',function ($scope,bcsComunes,servicioBrokerExtranjero) {

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.datos = {};
	var params = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.formData.filtrosSocioDeNegocio = {
			
	}
	
	
	if (params.brokerExtranjero) {
		$scope.formData.NombreCompleto = params.brokerExtranjero.NombreCompleto;
		$scope.datos.Codigo = params.brokerExtranjero.Codigo;
		$scope.datos.IdEstadoDefecto = params.brokerExtranjero.IdEstado;
	} else {
		$scope.datos.IdEstado = 'ACT';
	}
	
	$scope.formData.aceptar = function() {
		
		
		var prom;
		//nuevo
		if (!params.brokerExtranjero) {
			var datossp = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_Codigo : $scope.datos.Codigo,
					p_IdEstado : $scope.datos.IdEstado,
					p_IdPersona : $scope.datos.SocioDeNegocio.IdPersona 
			}
			prom = servicioBrokerExtranjero.crearBrokerExtranjero(datossp).$promise;
		}
		//modificado
		else {
			var datossp = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_Codigo : $scope.datos.Codigo,
					p_IdEstado : $scope.datos.IdEstado,
					p_IdPersona : params.brokerExtranjero.IdPersona 
			}
			prom = servicioBrokerExtranjero.actualizarBrokerExtranjero(datossp).$promise;
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		prom.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var p = bcsComunes.ventanaMsg({
				mensaje : undefined,
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana)
			
			bcsComunes.ventanaPromesa(p).then(function(){
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					bcsComunes.ventanaCerrar($scope.idVentana);
				}
			})
			 
			
		},function(result){
			bcsComunes.ventanaMsg({
				mensaje : undefined,
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana);
		})	
	}			
});