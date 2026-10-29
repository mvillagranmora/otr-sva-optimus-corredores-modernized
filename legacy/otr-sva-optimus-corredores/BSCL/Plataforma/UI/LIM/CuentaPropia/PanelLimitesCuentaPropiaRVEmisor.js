(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRVEmisorCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRVE = this;
		plCPRVE.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRVE.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRVE.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());