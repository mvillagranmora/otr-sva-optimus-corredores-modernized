(function() {
	angular.module('ModuloMesa').controller('ConsultarContactoSpotCtrl',
	function($scope,$timeout,bcsComunes)
	{	
		$scope.data	= {};
	
		//Recupera parametros desde la ventana recibida
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.parametros = bcsComunes.ventanaParams(idVentana);
		
		$scope.data.Nombre = $scope.parametros.Nombre;
		$scope.data.Correo = $scope.parametros.Correo;
		$scope.data.Telefono = $scope.parametros.Telefono;
		$scope.data.Calle = $scope.parametros.Calle;					
		$scope.data.NumeroCalle = $scope.parametros.NumeroCalle;
		$scope.data.NombreCiudad = $scope.parametros.NombreCiudad;					
		$scope.data.NombrePais = $scope.parametros.NombrePais;
		
	});
}());