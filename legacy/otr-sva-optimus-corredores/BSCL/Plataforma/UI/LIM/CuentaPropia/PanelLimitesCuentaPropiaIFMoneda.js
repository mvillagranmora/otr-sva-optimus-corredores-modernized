(function () {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaIFMonedaCtrl', function ($scope, bcsComunes, $compile) {

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		const self = this;
		self.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		self.parametros = bcsComunes.ventanaParams($scope.idVentana);
		self.domVentana = $("[ventanaid='" + $scope.idVentana + "']");

		/*********************************************
		 * COMBOBOX
		 ********************************************/

		/*********************************************
		 * FUNCIONES
		 ********************************************/

	});
}());