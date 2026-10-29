(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRFPlazoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRFP = this;
		plCPRFP.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRFP.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRFP.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());