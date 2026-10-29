(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaCarteraCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPC = this;
		plCPC.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPC.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPC.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());