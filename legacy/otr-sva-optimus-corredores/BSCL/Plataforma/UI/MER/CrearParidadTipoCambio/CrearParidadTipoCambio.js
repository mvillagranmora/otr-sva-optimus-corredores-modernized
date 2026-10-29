angular.module('ModuloPlataforma').controller('CrearParidadTipoCambioCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$translate){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.modo = 'INGRESAR';
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	
	$scope.formData = {};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.aceptar = function() {	
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
			    p_IdUsuario 		: $scope.SesionIdUsuario,
			    p_IdModulo       	: 'PLA',
			    p_simularOperacion	: 'N',
				p_UsaReglasNegocio	: 'S',
				p_Registros : [ {
					IdInterno 				: 0,
					Fecha					: util.dateSPdate($scope.registro.Fecha),
					IdMoneda				: $scope.registro.IdMoneda,
					ParidadMonedaCambio		: $scope.registro.Paridad,
					IdTipoRegistro 			: $scope.registro.IdTipoRegistro,
				}]
		};
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioPlataforma.Servicios().CrearParidadYTipoCambio(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				var p = bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_REGISTRO_INGRESADO"),
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);

				bcsComunes.ventanaPromesa(p).then(function(){

					bcsComunes.ventanaData($scope.idVentana,{guardar : true});
					bcsComunes.ventanaCerrar($scope.idVentana);
				});

			} else {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsg({
						mensaje:"UI_ERROR_INGRESAR_REGISTRO",
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.msg("error","UI_ERROR","UI_ERROR_INGRESAR_REGISTRO");
		});
	}
});
