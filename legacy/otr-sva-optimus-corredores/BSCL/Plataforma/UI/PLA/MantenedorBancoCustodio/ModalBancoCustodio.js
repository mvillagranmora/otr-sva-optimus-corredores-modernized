angular.module('ModuloPlataforma').controller('ModalBancoCustodioCtrl',function ($scope,bcsComunes,servicioBancoCustodio) {

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.datos = {};
	var params = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.formData.filtrosSocioDeNegocio = {
			IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa}
	}
	
	
	if (params.bancoCustodio) {
		$scope.formData.NombreCompleto = params.bancoCustodio.NombreCompleto;
		$scope.datos.Codigo = params.bancoCustodio.Codigo;
		$scope.datos.IdEstadoDefecto = params.bancoCustodio.IdEstado;
		$scope.datos.EsBancoLiquidador = params.bancoCustodio.EsBancoLiquidador==='S'?true:false;
		$scope.datos.EsBancoCustodio = params.bancoCustodio.EsBancoCustodio==='S'?true:false;
		$scope.datos.IdPersona = params.bancoCustodio.IdPersona
		//$scope.datos.IdTipoBancoDefecto = (params.bancoCustodio.EsBancoLiquidador==='S' && params.bancoCustodio.EsBancoCustodio==='S')?'TODOS':(params.bancoCustodio.EsBancoLiquidador==='S' && params.bancoCustodio.EsBancoCustodio==='N')?'LIQ':'CUS';
	}
	
	$scope.formData.aceptar = function() {
		
		
		var prom;
		//nuevo
		if (!params.bancoCustodio) {
			var datossp = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_Codigo : $scope.datos.Codigo,
					p_EsBancoLiquidador :$scope.datos.EsBancoLiquidador?'S':'N', //$scope.datos.IdTipoBancoSeleccionado.Codigo === 'TODOS' || $scope.datos.IdTipoBancoSeleccionado.Codigo === 'LIQ'?'S':'N',
					p_EsBancoCustodio : $scope.datos.EsBancoCustodio?'S':'N',//$scope.datos.IdTipoBancoSeleccionado.Codigo === 'TODOS' || $scope.datos.IdTipoBancoSeleccionado.Codigo === 'CUS'?'S':'N',
					p_IdEstado : $scope.datos.IdEstado,
					p_IdPersona : $scope.datos.SocioDeNegocio.IdPersona 
			}
			prom = servicioBancoCustodio.crearBancoCustodio(datossp).$promise;
		}
		//modificado
		else {
			var datossp = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'PLA',
					p_Codigo : $scope.datos.Codigo,
					p_EsBancoLiquidador : $scope.datos.EsBancoLiquidador?'S':'N',//$scope.datos.IdTipoBancoSeleccionado.Codigo === 'TODOS' || $scope.datos.IdTipoBancoSeleccionado.Codigo === 'LIQ'?'S':'N',
					p_EsBancoCustodio : $scope.datos.EsBancoCustodio?'S':'N',//$scope.datos.IdTipoBancoSeleccionado.Codigo === 'TODOS' || $scope.datos.IdTipoBancoSeleccionado.Codigo === 'CUS'?'S':'N',
					p_IdEstado : $scope.datos.IdEstado,
					p_IdPersona : $scope.datos.IdPersona
					
			}
			prom = servicioBancoCustodio.actualizarBancoCustodio(datossp).$promise;
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		prom.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var p = bcsComunes.ventanaMsg({
				mensaje : undefined,
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana);
			
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