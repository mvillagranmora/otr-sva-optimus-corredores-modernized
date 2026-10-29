(function () {
	angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPortafolioPreferenciasPagoClientePagaEnrolCtrl', function ($scope) {
		const pepppcp = this;

		$scope.admSoc.socioNegocioPromise.then(function () {
			if ($scope.admSoc && $scope.admSoc.dataSocioNegocio) {
				pepppcp.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
				pepppcp.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['PREFERENCIAS_PAGO'];
			}
		});
	});
}());
