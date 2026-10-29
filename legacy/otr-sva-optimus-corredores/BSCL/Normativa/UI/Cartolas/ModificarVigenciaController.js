angular.module('ModuloNormativa').controller(
		'ModificarVigenciaController',
		[ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils',
				'bcsCartolas', ModificarVigenciaController ]);

function ModificarVigenciaController($scope, $translate, bcsComunes, Model,
		Utils, bcsCartolas) {

	var windowParams = Utils.getWindowParams($scope);
	$scope.Plantilla = angular.copy(windowParams.PlantillaSeleccionada);
	$scope.Flag = $scope.Plantilla.Vigente === 'SI';

	$scope.actualizarVigencia = function() {
		$scope.Plantilla.IdEmpresa = $scope.SesionIdEmpresa;
		$scope.Plantilla.IdUsuario = $scope.SesionIdUsuario;
		$scope.Plantilla.Vigente = $scope.Flag ? 'SI' : 'NO';

		bcsCartolas.actualizarVigenciaPlantilla($scope.Plantilla, function(
				reply) {
			windowParams.PlantillaSeleccionada.Vigente = $scope.Plantilla.Vigente;
			bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
		}, function(error) {
			Utils.showError(error, 'Modificación de Vigencia', bcsComunes
					.ventanaWinId($scope));
		});
	}

}
