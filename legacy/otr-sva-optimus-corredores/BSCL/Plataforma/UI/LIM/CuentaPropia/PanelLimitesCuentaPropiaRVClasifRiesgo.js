(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRVClasifRiesgoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRVCR = this;
		plCPRVCR.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRVCR.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRVCR.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());