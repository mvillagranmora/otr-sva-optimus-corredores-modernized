angular.module('ModuloOrdenes').controller('ModificarCorredorCorresponsalCtrl',function ($scope,bcsComunes,bcsServicioOrden ) {

	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = $.extend({},bcsComunes.ventanaParams(idVentana));
	
	$scope.defecto = {
		IdPais : parametros.IdPais,
		IdContraparte : parametros.IdContraparte,
		IdPersona : parametros.IdPersona,
		IdSocioDeNegocio : parametros.IdSocioDeNegocio,
		IdTipoProducto : parametros.IdTipoProducto,
		IdPerfilCobros : parametros.IdPerfilCobros,
		IdBoveda : parametros.IdBoveda,
		IdEstado : parametros.IdEstado

	}

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
			IdCorredorCorresponsal : parametros.IdCorredorCorresponsal,
			CodigoCorredor : parametros.CodigoCorredor,
			DefaultPais : parametros.DefaultPais==='S'?true:false,
			
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
				
				p_IdCorredorCorresponsal	: $scope.corredorCorresponsal.IdCorredorCorresponsal,
				p_IdPais 				: $scope.corredorCorresponsal.IdPais,
				p_IdContraparte 		: $scope.corredorCorresponsal.personaSeleccionada.IdContraparte,
				p_IdSocioDeNegocio		: $scope.corredorCorresponsal.personaSeleccionada.IdSocioDeNegocio,
				p_IdPersona				: $scope.corredorCorresponsal.personaSeleccionada.IdPersona,
				p_IdEstado 				: $scope.corredorCorresponsal.IdEstado,
				p_IdTipoProducto		: $scope.SesionLocale === 'PE' ? $scope.corredorCorresponsal.IdTipoProducto : "",
				p_IdPerfilCobros		: $scope.corredorCorresponsal.IdPerfilCobros,
				p_DefaultPais 			: $scope.corredorCorresponsal.DefaultPais?'S':'N',
				p_IdBoveda				: $scope.SesionLocale === 'PE' ? $scope.corredorCorresponsal.IdBoveda : ""
		};
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioOrden.ModificarCorredorCorresponsal(datos).$promise;
		p.then(function(result1){
			bcsComunes.ventanaOverlay(idVentana,false);
			var id = bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result1)
			},idVentana);
			
			bcsComunes.ventanaPromesa(id).then(function(result){
				if (result1.P_OUT_ERRORESCONTADOR > 0) return;
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
			})
			
			
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result)
			},idVentana);
		});
			
		
		
	}		
});
