(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaIFPlazoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPIFP = this;
		plCPIFP.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPIFP.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPIFP.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());