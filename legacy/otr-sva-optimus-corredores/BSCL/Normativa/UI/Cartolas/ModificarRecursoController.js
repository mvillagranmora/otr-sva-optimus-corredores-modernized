angular.module('ModuloNormativa')
		.controller(
				'ModificarRecursoController',
				[ '$scope', '$translate', 'bcsComunes', 'bcsAlerta', 'Model',
						'Utils', 'bcsCartolas', 'OPCIONES_BCSALERTA',
						ModificarRecursoController ]);

function ModificarRecursoController($scope, $translate, bcsComunes, bcsAlerta,
		Model, Utils, bcsCartolas, OPCIONES_BCSALERTA) {

	var windowParams = Utils.getWindowParams($scope);
	$scope.Recurso = angular.copy(windowParams.RecursoSeleccionado);
	$scope.Flag = $scope.Recurso.Valor === 'SI';
	$scope.loadFile = null;

	$scope.guardar = function() {
		var t = bcsAlerta
				.crear(
						'PREGUNTA_SI_NO',
						'Al modificar un recurso está afectando a las cartolas ya creadas. Se recomienda crear una plantilla solo para afectar cartolas futuras ¿Está seguro de continuar?');
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				actualizarRecurso();
			}
		});
	}

	function actualizarRecurso() {
		$scope.Recurso.IdEmpresa = $scope.SesionIdEmpresa;
		$scope.Recurso.IdUsuario = $scope.SesionIdUsuario;

		if ($scope.Recurso.Tipo === 'IMAGEN') {
			bcsCartolas.subirImagen($scope.loadFile, $scope.Recurso, function(
					reply) {
				if (reply.code === 'OK') {
					bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
				} else {
					Utils.showError(reply, 'Modificación de Recurso',
							bcsComunes.ventanaWinId($scope));
				}
			}, function(http, status, fnc, httpObj) {
				Utils.showError('ERROR', 'Modificación de Recurso', bcsComunes
						.ventanaWinId($scope));
			});
		} else {
			if ($scope.Recurso.Tipo === 'OPCION') {
				$scope.Recurso.Valor = $scope.Flag ? 'SI' : 'NO';
			}

			bcsCartolas.actualizarValorRecurso($scope.Recurso, function(reply) {
				if (windowParams.CallBackFunction)
					windowParams.CallBackFunction();
				bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
			}, function(error) {
				windowParams.CallBackFunction();
				Utils.showError(error, 'Modificación de Recurso', bcsComunes
						.ventanaWinId($scope));
			});
		}
	}

}
