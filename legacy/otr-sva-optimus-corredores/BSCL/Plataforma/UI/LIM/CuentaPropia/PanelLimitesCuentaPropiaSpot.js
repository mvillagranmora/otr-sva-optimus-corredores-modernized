(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaSpotCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPSP = this;
		plCPSP.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPSP.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPSP.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());