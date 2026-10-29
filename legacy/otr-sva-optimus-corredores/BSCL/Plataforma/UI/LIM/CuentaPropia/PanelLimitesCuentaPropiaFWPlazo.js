(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaFWPlazoCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPFWP = this;
		plCPFWP.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPFWP.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPFWP.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());