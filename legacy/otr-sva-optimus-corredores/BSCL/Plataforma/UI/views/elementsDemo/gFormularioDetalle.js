angular.module('ModuloPlataforma').controller('gFormularioDetalleCtrl',function ($scope,bcsComunes) {
		/****************************************
		 *
		 * Declaraciones
		 *
		 * **************************************/
		//Obtiene los parametros pasado a la ventana
		$scope.IdVentana = bcsComunes.ventanaWinId ($scope);   
		$scope.dato = bcsComunes.ventanaParams($scope.IdVentana);
		/****************************************
		 *
		 * Eventos
		 *
		 * **************************************/
		$scope.cerrar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN CERRAR");
			bcsComunes.ventanaCerrar($scope.IdVentana);
			
		};
		$scope.aceptar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN ACEPTAR");
			bcsComunes.ventanaCerrar($scope.IdVentana);			
		};		
		/****************************************
		 *
		 * Logica
		 *
		 * **************************************/
		if ($scope.dato == undefined || $scope.dato=="") {
			$scope.dato="SIN PARAMETRO";
		}
});
