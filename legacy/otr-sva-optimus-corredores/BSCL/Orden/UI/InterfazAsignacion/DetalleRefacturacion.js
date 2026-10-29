angular.module('ModuloOrdenes').controller('DetalleRefacturacionCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenesV2) {

		
		console.log("PASE POR ACA!");
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		if (!$scope.parametros)
			console.log("No hay resultados!");
		else {
			$scope.NotasDeCreditoGeneradas = $scope.parametros.NotasDeCreditoGeneradas;
			$scope.ComprobantesAnulados = $scope.parametros.ComprobantesAnulados;
			$scope.MovimientoComprobanteAnulados = $scope.parametros.MovimientoComprobanteAnulados;
			$scope.AsignacionesAnuladas = $scope.parametros.AsignacionesAnuladas;
			$scope.OrdenesAnuladas = $scope.parametros.OrdenesAnuladas;
			$scope.OrdenesGeneradas = $scope.parametros.OrdenesGeneradas;
			$scope.AsignacionesGeneradas = $scope.parametros.AsignacionesGeneradas;
			$scope.MovimientoComprobanteGenerados = $scope.parametros.MovimientoComprobanteGenerados;
			$scope.ComprobantesGenerados = $scope.parametros.ComprobantesGenerados;
		}
});
