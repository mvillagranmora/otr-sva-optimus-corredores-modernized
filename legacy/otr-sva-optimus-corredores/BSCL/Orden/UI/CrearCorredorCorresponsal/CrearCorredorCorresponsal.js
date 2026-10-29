angular.module('ModuloOrdenes').controller('CrearCorredorCorresponsalCtrl',function ($scope,bcsComunes,bcsServicioOrden ) {

	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.filtros = {}
	
	if ($scope.SesionLocale === 'CL') {
		$scope.filtros.perfilCobros = {
				IdEmpresa				 	:  {field : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				EsPerfilPersonalizado		:  {field : "EsPerfilPersonalizado", "operator" : "eq", "value" : "N"}
		}
	}
	if ($scope.SesionLocale === 'PE') {
		$scope.filtros.perfilCobrosPeru = {
				IdEmpresa				 	:  {field : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				EsPerfilPersonalizado		:  {field : "EsPerfilPersonalizado", "operator" : "eq", "value" : "N"}
		}
	}
	
	$scope.filtros.persona = {
			IdEmpresa			:  {field : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
			IdCorresponsal		:  {field : "EsContraparte", "operator" : "eq", "value" : "UI_SI"}
	}
	
	$scope.corredorCorresponsal = {
			ValorCobro : 0
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	$scope.aceptar = function() {
		var datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'ORD',
				
				p_IdPais 				: $scope.corredorCorresponsal.IdPais,
				p_CodigoCorredor 		: $scope.corredorCorresponsal.CodigoCorredor,
				p_IdContraparte 		: $scope.corredorCorresponsal.personaSeleccionada.IdContraparte,
				p_IdPersona				: $scope.corredorCorresponsal.personaSeleccionada.IdPersona,
				p_IdSocioDeNegocio		: $scope.corredorCorresponsal.personaSeleccionada.IdSocioDeNegocio,
				p_IdPerfilCobros		: $scope.corredorCorresponsal.IdPerfilCobros,
				p_IdEstado 				: $scope.corredorCorresponsal.IdEstado,
				p_DefaultPais 			: $scope.corredorCorresponsal.DefaultPais?'S':'N',
								
		
		};
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioOrden.CrearCorredorCorresponsal(datos).$promise;
		p.then(function(result1){
			bcsComunes.ventanaOverlay(idVentana,false);
			var id = bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result1)
			},idVentana);
			bcsComunes.ventanaPromesa(id).then(function(result2){
				if (result1.P_OUT_ERRORESCONTADOR > 0) return;
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
			})
		},
		function(result1){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result1)
			},idVentana);
		});
			
		
		
	}		
});
