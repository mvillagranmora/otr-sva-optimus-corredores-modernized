(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRFClasifRiesgoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRFCR = this;
		plCPRFCR.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRFCR.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRFCR.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());