(function () {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRFMonedaCtrl', function ($scope, bcsComunes, $compile) {

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRFM = this;
		plCPRFM.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRFM.parametros = bcsComunes.ventanaParams($scope.idVentana);
		plCPRFM.domVentana = $("[ventanaid='" + $scope.idVentana + "']");

		/*********************************************
		 * COMBOBOX
		 ********************************************/

		/*********************************************
		 * FUNCIONES
		 ********************************************/
	
	});
}());