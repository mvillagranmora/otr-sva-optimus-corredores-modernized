angular.module('ModuloNormativa')
		.controller(
				'ModificarConfigController',
				[ '$scope', '$translate', 'bcsComunes', 'bcsAlerta', 'Model',
						'Utils', 'bcsCartolas', 'OPCIONES_BCSALERTA',
						ModificarConfigController ]);

function ModificarConfigController($scope, $translate, bcsComunes, bcsAlerta,
		Model, Utils, bcsCartolas, OPCIONES_BCSALERTA) {

	var windowParams = Utils.getWindowParams($scope);
	$scope.Config = angular.copy(windowParams.ConfigSeleccionada);
	$scope.Flag = $scope.Config.Valor === 'SI';
	$scope.loadFile = null;

	$scope.guardar = function() {
		$scope.Config.IdEmpresa = $scope.SesionIdEmpresa;
		$scope.Config.IdUsuario = $scope.SesionIdUsuario;

		if ($scope.Config.Tipo === 'OPCION') {
			$scope.Config.Valor = $scope.Flag ? 'SI' : 'NO';
		}

		bcsCartolas.grabarConfig($scope.Config, function(reply) {
			if (windowParams.CallBackFunction)
				windowParams.CallBackFunction();
			bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
		}, function(error) {
			Utils.showError(error, 'Modificación de Configuración', bcsComunes
					.ventanaWinId($scope));
		});
	}

}
