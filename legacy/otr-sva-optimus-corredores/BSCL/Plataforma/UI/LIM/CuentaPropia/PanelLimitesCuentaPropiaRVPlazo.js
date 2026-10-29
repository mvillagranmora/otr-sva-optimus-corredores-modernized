(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRVPlazoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRVP = this;
		plCPRVP.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRVP.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRVP.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());