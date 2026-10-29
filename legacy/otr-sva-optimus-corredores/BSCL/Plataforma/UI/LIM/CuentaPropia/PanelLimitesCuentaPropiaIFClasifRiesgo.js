(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaIFClasifRiesgoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPIFCR = this;
		plCPIFCR.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPIFCR.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPIFCR.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());