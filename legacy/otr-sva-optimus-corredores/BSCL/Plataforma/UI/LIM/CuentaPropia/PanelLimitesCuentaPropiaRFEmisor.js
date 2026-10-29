(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRFEmisorCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRFE = this;
		plCPRFE.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRFE.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRFE.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());